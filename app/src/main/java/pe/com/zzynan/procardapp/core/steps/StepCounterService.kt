package pe.com.zzynan.procardapp.core.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pe.com.zzynan.procardapp.MainActivity
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.usecase.GetTodayMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyStepsUseCase

/**
 * Servicio en primer plano que mantiene el conteo de pasos aun cuando la app está cerrada.
 * Se optimiza al reaccionar solo a eventos de sensor y verificaciones periódicas ligeras.
 */
class StepCounterService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private val notificationManager: NotificationManager? by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private val dailyMetricsRepository by lazy {
        ServiceLocator.provideDailyMetricsRepository(applicationContext)
    }
    private val userProfileRepository by lazy {
        ServiceLocator.provideUserProfileRepository(applicationContext)
    }

    private val observeUserProfileUseCase by lazy { ObserveUserProfileUseCase(userProfileRepository) }
    private val getTodayMetricsUseCase by lazy { GetTodayMetricsUseCase(dailyMetricsRepository) }
    private val saveDailyStepsUseCase by lazy { SaveDailyStepsUseCase(dailyMetricsRepository) }

    private var currentUsername: String = UserProfile.DEFAULT_DISPLAY_NAME
    private var currentDate: LocalDate = LocalDate.now()
    private var baseSensorValue: Float? = null
    private var stepsToday: Int = 0
    private var isForeground = false
    private var hasLoadedInitialMetrics = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        createNotificationChannel()
        observeProfileChanges()
        observeStateUpdates()
        monitorDateChanges()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()
        when (intent?.action) {
            ACTION_START -> startCounter()
            ACTION_PAUSE -> pauseCounter()
            ACTION_TOGGLE -> if (StepCounterStateHolder.state.value.isRunning) pauseCounter() else startCounter()
            ACTION_INITIALIZE -> requestInitialLoad()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        runBlocking {
            saveDailyStepsUseCase(currentUsername, currentDate.toEpochDayLong(), stepsToday)
        }
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        StepCounterStateHolder.setRunning(false)
        super.onDestroy()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event?.sensor ?: return
        if (!StepCounterStateHolder.state.value.isRunning) return
        when (sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> handleCounterEvent(event)
            Sensor.TYPE_STEP_DETECTOR -> handleDetectorEvent(event)
        }
    }

    private fun handleCounterEvent(event: SensorEvent) {
        val totalSteps = event.values.firstOrNull() ?: return
        if (baseSensorValue == null) {
            baseSensorValue = totalSteps - stepsToday
        }
        val computed = (totalSteps - (baseSensorValue ?: 0f)).toInt()
        if (computed >= 0 && computed != stepsToday) {
            stepsToday = computed
            StepCounterStateHolder.updateSteps(stepsToday)
        }
    }

    private fun handleDetectorEvent(event: SensorEvent) {
        val increment = event.values.size
        if (increment > 0) {
            stepsToday += increment
            StepCounterStateHolder.updateSteps(stepsToday)
        }
    }

    private fun startCounter() {
        if (StepCounterStateHolder.state.value.isRunning) return
        StepCounterStateHolder.setRunning(true)
        baseSensorValue = null
        stepSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        updateNotification()
    }

    private fun pauseCounter() {
        if (!StepCounterStateHolder.state.value.isRunning) return
        StepCounterStateHolder.setRunning(false)
        sensorManager.unregisterListener(this)
        updateNotification()
        serviceScope.launch {
            saveDailyStepsUseCase(currentUsername, currentDate.toEpochDayLong(), stepsToday)
        }
    }

    private fun requestInitialLoad() {
        if (!hasLoadedInitialMetrics) {
            serviceScope.launch { loadStepsForCurrentUser() }
        } else {
            updateNotification()
        }
    }

    private fun observeProfileChanges() {
        serviceScope.launch {
            observeUserProfileUseCase()
                .collect { profile ->
                    val newName = profile.displayName
                    if (!hasLoadedInitialMetrics) {
                        currentUsername = newName
                        loadStepsForCurrentUser()
                        hasLoadedInitialMetrics = true
                    } else if (currentUsername != newName) {
                        saveDailyStepsUseCase(currentUsername, currentDate.toEpochDayLong(), stepsToday)
                        currentUsername = newName
                        loadStepsForCurrentUser()
                    }
                }
        }
    }

    private fun observeStateUpdates() {
        serviceScope.launch {
            StepCounterStateHolder.state.collect { state ->
                if (isForeground) {
                    notificationManager?.notify(NOTIFICATION_ID, buildNotification(state))
                }
            }
        }
    }

    private fun monitorDateChanges() {
        serviceScope.launch {
            while (isActive) {
                val today = LocalDate.now()
                if (today != currentDate) {
                    handleDateChange(today)
                }
                delay(TimeUnit.MINUTES.toMillis(1))
            }
        }
    }

    private suspend fun handleDateChange(newDate: LocalDate) {
        saveDailyStepsUseCase(currentUsername, currentDate.toEpochDayLong(), stepsToday)
        currentDate = newDate
        stepsToday = 0
        StepCounterStateHolder.updateSteps(0)
        baseSensorValue = null
        loadStepsForCurrentUser()
    }

    private suspend fun loadStepsForCurrentUser() {
        val metrics = getTodayMetricsUseCase.current(currentUsername, currentDate.toEpochDayLong())
        stepsToday = metrics?.dailySteps ?: 0
        StepCounterStateHolder.updateSteps(stepsToday)
        baseSensorValue = null
        updateNotification()
    }

    private fun ensureForeground() {
        if (!isForeground) {
            val state = StepCounterStateHolder.state.value
            startForeground(NOTIFICATION_ID, buildNotification(state))
            isForeground = true
        }
    }

    private fun updateNotification() {
        if (isForeground) {
            notificationManager?.notify(NOTIFICATION_ID, buildNotification(StepCounterStateHolder.state.value))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_steps_content, 0)
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(state: StepCounterState): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = PendingIntent.getService(
            this,
            REQUEST_TOGGLE,
            intent(this, ACTION_TOGGLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionText = if (state.isRunning) {
            getString(R.string.step_counter_pause)
        } else {
            getString(R.string.step_counter_resume)
        }
        val actionIcon = if (state.isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_steps_content, state.stepsToday))
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(actionIcon, actionText, toggleIntent)
            .build()
    }

    companion object {
        const val ACTION_INITIALIZE = "pe.com.zzynan.procardapp.core.steps.ACTION_INITIALIZE"
        const val ACTION_START = "pe.com.zzynan.procardapp.core.steps.ACTION_START"
        const val ACTION_PAUSE = "pe.com.zzynan.procardapp.core.steps.ACTION_PAUSE"
        const val ACTION_TOGGLE = "pe.com.zzynan.procardapp.core.steps.ACTION_TOGGLE"

        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN_APP = 200
        private const val REQUEST_TOGGLE = 201

        fun intent(context: Context, action: String): Intent {
            return Intent(context, StepCounterService::class.java).apply {
                this.action = action
            }
        }
    }
}
