package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.core.steps.StepCounterManager
import pe.com.zzynan.procardapp.core.steps.StepCounterState
import pe.com.zzynan.procardapp.domain.model.DailyMetrics
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.model.TrainingStage
import pe.com.zzynan.procardapp.domain.usecase.GetLastWeightOnOrBeforeUseCase
import pe.com.zzynan.procardapp.domain.usecase.GetTodayMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveTodayNutritionSummaryUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveWeeklyMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyCardioUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailySaltUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailySleepUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyStageUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyTrainingDoneUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyWaterUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyWeightUseCase
import pe.com.zzynan.procardapp.ui.mappers.toUiModel
import pe.com.zzynan.procardapp.ui.model.DailyNutritionSummaryUiModel
import pe.com.zzynan.procardapp.ui.model.StepCounterUiModel
import pe.com.zzynan.procardapp.ui.model.WeightCardUiModel
import pe.com.zzynan.procardapp.ui.model.WeightEditorUiModel
import pe.com.zzynan.procardapp.ui.model.WeightStatus
import pe.com.zzynan.procardapp.ui.model.WeeklyMetricsUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyStepsPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyWeightPoint
import pe.com.zzynan.procardapp.ui.state.DailyRegisterUiState


/**
 * ViewModel de la pantalla de registro diario. Orquesta pasos, métricas y nombre del usuario.
 */
class DailyRegisterViewModel(
    private val getTodayMetricsUseCase: GetTodayMetricsUseCase,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val stepCounterManager: StepCounterManager,
    private val saveDailyWeightUseCase: SaveDailyWeightUseCase,
    private val observeWeeklyMetricsUseCase: ObserveWeeklyMetricsUseCase,
    private val getLastWeightOnOrBeforeUseCase: GetLastWeightOnOrBeforeUseCase,
    private val observeTodayNutritionSummaryUseCase: ObserveTodayNutritionSummaryUseCase,
    private val saveDailyCardioUseCase: SaveDailyCardioUseCase,
    private val saveDailySleepUseCase: SaveDailySleepUseCase,
    private val saveDailyWaterUseCase: SaveDailyWaterUseCase,
    private val saveDailySaltUseCase: SaveDailySaltUseCase,
    private val saveDailyTrainingDoneUseCase: SaveDailyTrainingDoneUseCase,
    private val saveDailyStageUseCase: SaveDailyStageUseCase,
    private val appContext: Context
) : ViewModel() {

    private val todayFlow = MutableStateFlow(LocalDate.now())
    private val historyDateFlow = MutableStateFlow(LocalDate.now())
    private val isHistoryVisibleFlow = MutableStateFlow(false)
    private val historyWeightTextFlow = MutableStateFlow("")
    private val historyPlaceholderFlow = MutableStateFlow<String?>(null)

    private val prefs = appContext.getSharedPreferences("daily_register_prefs", Context.MODE_PRIVATE)

    private val waterTargetTrainingFlow = MutableStateFlow(loadWaterTargetTraining())
    private val waterTargetRestFlow = MutableStateFlow(loadWaterTargetRest())
    private val supplementationDoneFlow = MutableStateFlow(false)

    private val profileFlow = observeUserProfileUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserProfile(UserProfile.DEFAULT_DISPLAY_NAME)
        )

    private val metricsFlow = profileFlow
        .combine(todayFlow) { profile, date -> profile.displayName to date.toEpochDayLong() }
        .flatMapLatest { (username, epochDay) ->
            getTodayMetricsUseCase.observe(username, epochDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val recentMetricsFlow = profileFlow
        .combine(todayFlow) { profile, today -> profile.displayName to today.toEpochDayLong() }
        .flatMapLatest { (username, endDate) ->
            observeWeeklyMetricsUseCase(username, endDate, days = 30)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val nutritionSummaryFlow: StateFlow<DailyNutritionSummaryUiModel?> = profileFlow
        .combine(todayFlow) { profile, date -> profile.displayName to date }
        .flatMapLatest { (username, date) ->
            observeTodayNutritionSummaryUseCase(username, date).map { it?.toUiModel() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val stepStateFlow = stepCounterManager.observeState()

    private val weightCardFlow: StateFlow<WeightCardUiModel> = combine(
        metricsFlow,
        recentMetricsFlow,
        todayFlow,
        profileFlow
    ) { metrics, recent, today, profile ->
        val todayWeight = metrics?.weightFasted?.takeIf { it > 0f }
        val fallback = if (todayWeight == null) {
            lastWeightBefore(today, recent) ?: getLastWeightOnOrBeforeUseCase(
                profile.displayName,
                today.minusDays(1).toEpochDayLong()
            )
        } else {
            null
        }
        val display = todayWeight ?: fallback ?: 0f
        WeightCardUiModel(
            displayValue = formatWeight(display),
            status = if (todayWeight != null) WeightStatus.Saved else WeightStatus.Pending,
            hasFallback = fallback != null && todayWeight == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeightCardUiModel()
    )

    private val weightEditorFlow: StateFlow<WeightEditorUiModel> = combine(
        isHistoryVisibleFlow,
        historyDateFlow,
        historyWeightTextFlow,
        historyPlaceholderFlow,
        todayFlow
    ) { visible, date, text, placeholder, today ->
        WeightEditorUiModel(
            isVisible = visible,
            selectedDate = date,
            weightText = text,
            placeholder = placeholder,
            canNavigateNext = date.isBefore(today)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeightEditorUiModel()
    )

    private val weeklyUiFlow: StateFlow<WeeklyMetricsUiModel> = combine(
        recentMetricsFlow,
        todayFlow
    ) { metrics, today ->
        val startDate = today.minusDays(6)
        val mapped = metrics.associateBy { it.dateEpoch }
        val weightPoints = (0 until 7).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val metric = mapped[date.toEpochDay()]
            WeeklyWeightPoint(date = date, weight = metric?.weightFasted?.takeIf { it > 0f })
        }
        val stepsPoints = (0 until 7).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val metric = mapped[date.toEpochDay()]
            WeeklyStepsPoint(date = date, steps = metric?.dailySteps ?: 0)
        }
        WeeklyMetricsUiModel(weightPoints = weightPoints, stepsPoints = stepsPoints)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeeklyMetricsUiModel()
    )

    val weightLast7Days: StateFlow<List<WeeklyWeightPoint>> = weeklyUiFlow
        .map { it.weightPoints }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val stepsLast7Days: StateFlow<List<WeeklyStepsPoint>> = weeklyUiFlow
        .map { it.stepsPoints }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<DailyRegisterUiState> =
        combine(
            profileFlow,
            metricsFlow,
            stepStateFlow,
            weightCardFlow,
            weightEditorFlow,
            weeklyUiFlow,
            nutritionSummaryFlow,
            waterTargetTrainingFlow,
            waterTargetRestFlow,
            supplementationDoneFlow
        ) { values ->
            val profile = values[0] as UserProfile
            val metrics = values[1] as pe.com.zzynan.procardapp.domain.model.DailyMetrics?
            val stepState = values[2] as StepCounterState
            val weightCard = values[3] as WeightCardUiModel
            val weightEditor = values[4] as WeightEditorUiModel
            val weeklyUi = values[5] as WeeklyMetricsUiModel
            val nutritionSummary = values[6] as DailyNutritionSummaryUiModel?
            val waterTraining = values[7] as Int
            val waterRest = values[8] as Int
            val supplementationDone = values[9] as Boolean

            DailyRegisterUiState(
                userName = profile.displayName,
                dateEpoch = todayFlow.value.toEpochDayLong(),
                stepCounter = StepCounterUiModel(
                    stepsToday = stepState.stepsToday,
                    isRunning = stepState.isRunning
                ),
                metrics = metrics?.toUiModel(),
                nutritionSummary = nutritionSummary,
                waterTargetTrainingLiters = waterTraining,
                waterTargetRestLiters = waterRest,
                supplementationDone = supplementationDone,
                weightCard = weightCard,
                weightEditor = weightEditor,
                weeklyMetrics = weeklyUi,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyRegisterUiState()
        )

    init {
        stepCounterManager.ensureServiceRunning()
        viewModelScope.launch {
            while (isActive) {
                val today = LocalDate.now()
                if (today != todayFlow.value) {
                    todayFlow.value = today
                }
                delay(TimeUnit.MINUTES.toMillis(1))
            }
        }
        viewModelScope.launch {
            historyDateFlow.collect { date ->
                syncHistoryForDate(date)
            }
        }
        viewModelScope.launch {
            combine(profileFlow, todayFlow) { profile, date -> profile.displayName to date }
                .collect { (username, date) ->
                    supplementationDoneFlow.value = loadSupplementation(username, date)
                }
        }
    }

    fun onToggleStepCounter() {
        stepCounterManager.toggle()
    }

    fun onOpenHistory(date: LocalDate = todayFlow.value) {
        viewModelScope.launch {
            val target = date.coerceAtMost(todayFlow.value)
            historyDateFlow.value = target
            syncHistoryForDate(target)
            isHistoryVisibleFlow.value = true
        }
    }

    fun onDismissHistory() {
        onConfirmHistory()
    }

    fun onHistoryPreviousDay() {
        viewModelScope.launch {
            saveHistoryForCurrentDate()
            historyDateFlow.value = historyDateFlow.value.minusDays(1)
        }
    }

    fun onHistoryNextDay() {
        viewModelScope.launch {
            saveHistoryForCurrentDate()
            if (historyDateFlow.value.isBefore(todayFlow.value)) {
                historyDateFlow.value = historyDateFlow.value.plusDays(1)
            }
        }
    }

    fun onHistoryWeightChanged(value: String) {
        historyWeightTextFlow.value = sanitizeInput(value, historyWeightTextFlow.value)
    }

    fun onChartWeightSelected(date: LocalDate) {
        onOpenHistory(date)
    }

    fun onConfirmHistory() {
        viewModelScope.launch {
            saveHistoryForCurrentDate()
            isHistoryVisibleFlow.value = false
        }
    }

    fun onStageSelected(stage: TrainingStage) {
        viewModelScope.launch {
            saveDailyStageUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                stage
            )
        }
    }

    fun onSleepHoursConfirmed(hours: Float) {
        val minutes = (hours * 60f).roundToInt().coerceAtLeast(0)
        viewModelScope.launch {
            saveDailySleepUseCase(profileFlow.value.displayName, todayFlow.value.toEpochDayLong(), minutes)
        }
    }

    fun onCardioMinutesConfirmed(minutes: Int) {
        val safeMinutes = minutes.coerceAtLeast(0)
        viewModelScope.launch {
            saveDailyCardioUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                safeMinutes
            )
        }
    }

    fun onWaterIncrement() {
        viewModelScope.launch {
            val current = metricsFlow.value?.waterMl ?: 0
            saveDailyWaterUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                current + 1_000
            )
        }
    }

    fun onWaterDecrement() {
        viewModelScope.launch {
            val current = metricsFlow.value?.waterMl ?: 0
            saveDailyWaterUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                (current - 1_000).coerceAtLeast(0)
            )
        }
    }

    fun onWaterTargetsChanged(trainingLiters: Int, restLiters: Int) {
        waterTargetTrainingFlow.value = trainingLiters
        waterTargetRestFlow.value = restLiters
        prefs.edit()
            .putInt(KEY_WATER_TARGET_TRAINING, trainingLiters)
            .putInt(KEY_WATER_TARGET_REST, restLiters)
            .apply()
    }

    fun onSaltIncrement() {
        viewModelScope.launch {
            val current = metricsFlow.value?.saltGramsX10 ?: 0
            saveDailySaltUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                current + 5
            )
        }
    }

    fun onSaltDecrement() {
        viewModelScope.launch {
            val current = metricsFlow.value?.saltGramsX10 ?: 0
            saveDailySaltUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                (current - 5).coerceAtLeast(0)
            )
        }
    }

    fun onTrainingDoneChanged(done: Boolean) {
        viewModelScope.launch {
            saveDailyTrainingDoneUseCase(
                profileFlow.value.displayName,
                todayFlow.value.toEpochDayLong(),
                done
            )
        }
    }

    fun onSupplementationToggled() {
        val username = profileFlow.value.displayName
        val dateEpoch = todayFlow.value.toEpochDayLong()
        val newValue = !supplementationDoneFlow.value
        supplementationDoneFlow.value = newValue
        persistSupplementation(username, dateEpoch, newValue)
    }

    private suspend fun syncHistoryForDate(date: LocalDate) {
        val savedWeight = weightForDate(date)
        val fallback = if (savedWeight == null && date == todayFlow.value) {
            lastWeightBefore(date, recentMetricsFlow.value) ?: getLastWeightOnOrBeforeUseCase(
                profileFlow.value.displayName,
                date.minusDays(1).toEpochDayLong()
            )
        } else {
            null
        }
        if (savedWeight != null) {
            historyWeightTextFlow.value = formatWeight(savedWeight)
            historyPlaceholderFlow.value = null
        } else {
            val fallbackText = fallback?.let { formatWeight(it) }
            historyWeightTextFlow.value = fallbackText ?: ""
            historyPlaceholderFlow.value = fallbackText
        }
    }

    private suspend fun saveHistoryForCurrentDate() {
        val parsedWeight = parseWeight(historyWeightTextFlow.value) ?: return
        val rounded = parsedWeight.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
        persistWeightForDate(rounded, historyDateFlow.value)
        historyWeightTextFlow.value = formatWeight(rounded)
    }

    private suspend fun persistWeightForDate(weight: Float, date: LocalDate) {
        saveDailyWeightUseCase(profileFlow.value.displayName, date.toEpochDayLong(), weight)
    }

    private suspend fun weightForDate(date: LocalDate): Float? {
        val cached = recentMetricsFlow.value.firstOrNull { it.dateEpoch == date.toEpochDayLong() }
            ?.weightFasted
            ?.takeIf { it > 0f }
        if (cached != null) return cached
        val todayMetrics = metricsFlow.value?.takeIf { date == todayFlow.value }
            ?.weightFasted
            ?.takeIf { it > 0f }
        if (todayMetrics != null) return todayMetrics
        return getTodayMetricsUseCase.current(profileFlow.value.displayName, date.toEpochDayLong())
            ?.weightFasted
            ?.takeIf { it > 0f }
    }

    private fun lastWeightBefore(date: LocalDate, recent: List<DailyMetrics>): Float? {
        val targetEpoch = date.minusDays(1).toEpochDayLong()
        return recent
            .filter { it.dateEpoch <= targetEpoch && it.weightFasted > 0f }
            .maxByOrNull { it.dateEpoch }
            ?.weightFasted
    }

    private fun parseWeight(value: String): Float? {
        if (value.isBlank()) return null
        val normalized = value.replace(',', '.').trimEnd('.')
        if (!weightInputRegex.matches(normalized)) return null
        return normalized.toBigDecimalOrNull()?.toFloat()
    }

    private fun formatWeight(value: Float): String =
        String.format(Locale.US, "%.2f", value)

    private fun sanitizeInput(value: String, previous: String): String {
        val normalized = value.replace(',', '.')
        return if (weightInputRegex.matches(normalized)) normalized else previous
    }

    private val weightInputRegex = Regex("^\\d*(?:\\.\\d{0,2})?$")

    private fun loadWaterTargetTraining(): Int {
        val stored = prefs.getInt(KEY_WATER_TARGET_TRAINING, DEFAULT_WATER_TRAINING_LITERS)
        return if (stored > 0) stored else DEFAULT_WATER_TRAINING_LITERS
    }

    private fun loadWaterTargetRest(): Int {
        val stored = prefs.getInt(KEY_WATER_TARGET_REST, DEFAULT_WATER_REST_LITERS)
        return if (stored > 0) stored else DEFAULT_WATER_REST_LITERS
    }

    private fun loadSupplementation(username: String, date: LocalDate): Boolean {
        if (username.isBlank()) return false
        val key = supplementationKey(username, date.toEpochDayLong())
        return prefs.getBoolean(key, false)
    }

    private fun persistSupplementation(username: String, dateEpoch: Long, value: Boolean) {
        if (username.isBlank()) return
        prefs.edit().putBoolean(supplementationKey(username, dateEpoch), value).apply()
    }

    private fun supplementationKey(username: String, dateEpoch: Long): String =
        "supplementation_done_${username}_${dateEpoch}"

    companion object {
        private const val DEFAULT_WATER_TRAINING_LITERS = 5
        private const val DEFAULT_WATER_REST_LITERS = 4
        private const val KEY_WATER_TARGET_TRAINING = "water_target_training_liters"
        private const val KEY_WATER_TARGET_REST = "water_target_rest_liters"

        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appContext = context.applicationContext
                val dailyMetricsRepository = ServiceLocator.provideDailyMetricsRepository(appContext)
                val userProfileRepository = ServiceLocator.provideUserProfileRepository(appContext)
                val getUseCase = GetTodayMetricsUseCase(dailyMetricsRepository)
                val observeProfileUseCase = ObserveUserProfileUseCase(userProfileRepository)
                val saveWeightUseCase = SaveDailyWeightUseCase(dailyMetricsRepository)
                val weeklyUseCase = ObserveWeeklyMetricsUseCase(dailyMetricsRepository)
                val lastWeightUseCase = GetLastWeightOnOrBeforeUseCase(dailyMetricsRepository)
                val foodRepository = ServiceLocator.provideFoodRepository(appContext)
                val observeNutritionUseCase = ObserveTodayNutritionSummaryUseCase(foodRepository)
                val saveCardioUseCase = SaveDailyCardioUseCase(dailyMetricsRepository)
                val saveSleepUseCase = SaveDailySleepUseCase(dailyMetricsRepository)
                val saveWaterUseCase = SaveDailyWaterUseCase(dailyMetricsRepository)
                val saveSaltUseCase = SaveDailySaltUseCase(dailyMetricsRepository)
                val saveTrainingDoneUseCase = SaveDailyTrainingDoneUseCase(dailyMetricsRepository)
                val saveStageUseCase = SaveDailyStageUseCase(dailyMetricsRepository)
                val manager = StepCounterManager(appContext)
                @Suppress("UNCHECKED_CAST")
                return DailyRegisterViewModel(
                    getUseCase,
                    observeProfileUseCase,
                    manager,
                    saveWeightUseCase,
                    weeklyUseCase,
                    lastWeightUseCase,
                    observeNutritionUseCase,
                    saveCardioUseCase,
                    saveSleepUseCase,
                    saveWaterUseCase,
                    saveSaltUseCase,
                    saveTrainingDoneUseCase,
                    saveStageUseCase,
                    appContext
                ) as T
            }
        }
    }
}
