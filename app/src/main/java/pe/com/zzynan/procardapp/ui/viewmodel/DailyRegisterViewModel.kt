package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.core.steps.StepCounterManager
import pe.com.zzynan.procardapp.core.steps.StepCounterState
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.usecase.GetLastWeightOnOrBeforeUseCase
import pe.com.zzynan.procardapp.domain.usecase.GetTodayMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveWeeklyMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveDailyWeightUseCase
import pe.com.zzynan.procardapp.ui.mappers.toUiModel
import pe.com.zzynan.procardapp.ui.model.StepCounterUiModel
import pe.com.zzynan.procardapp.ui.model.WeightCardUiModel
import pe.com.zzynan.procardapp.ui.model.WeightEditorUiModel
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
    private val getLastWeightOnOrBeforeUseCase: GetLastWeightOnOrBeforeUseCase
) : ViewModel() {

    private val todayFlow = MutableStateFlow(LocalDate.now())
    private val historyDateFlow = MutableStateFlow(LocalDate.now())
    private val isHistoryVisibleFlow = MutableStateFlow(false)
    private val historyWeightTextFlow = MutableStateFlow("")
    private val todayWeightTextFlow = MutableStateFlow("")

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

    private val historyMetricsFlow = profileFlow
        .combine(historyDateFlow) { profile, date -> profile.displayName to date.toEpochDayLong() }
        .flatMapLatest { (username, epochDay) ->
            getTodayMetricsUseCase.observe(username, epochDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val weeklyMetricsFlow = profileFlow
        .combine(todayFlow) { profile, today -> profile.displayName to today.toEpochDayLong() }
        .flatMapLatest { (username, endDate) ->
            observeWeeklyMetricsUseCase(username, endDate)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val stepStateFlow = stepCounterManager.observeState()

    private val weightCardFlow: StateFlow<WeightCardUiModel> = profileFlow
        .combine(metricsFlow) { profile, metrics -> profile.displayName to metrics }
        .combine(todayFlow) { (username, metrics), today -> Triple(username, metrics, today) }
        .combine(todayWeightTextFlow) { (username, metrics, today), input ->
            WeightCardSource(username = username, metrics = metrics, today = today, input = input)
        }
        .mapLatest { source ->
            val todayWeight = source.metrics?.weightFasted ?: 0f
            val savedToday = todayWeight > 0f
            val fallback = if (savedToday) {
                todayWeight
            } else {
                getLastWeightOnOrBeforeUseCase(
                    source.username,
                    source.today.minusDays(1).toEpochDayLong()
                ) ?: 0f
            }
            WeightCardUiModel(
                value = if (source.input.isNotEmpty()) {
                    source.input
                } else if (savedToday) {
                    formatWeight(todayWeight)
                } else {
                    ""
                },
                placeholder = if (!savedToday && fallback > 0f) formatWeight(fallback) else null,
                statusLabel = if (savedToday) "Hoy guardado" else "Hoy pendiente",
                isSavedToday = savedToday
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightCardUiModel()
        )

    private val weightEditorFlow: StateFlow<WeightEditorUiModel> = combine(
        isHistoryVisibleFlow,
        historyDateFlow,
        historyWeightTextFlow,
        todayFlow
    ) { visible, date, text, today ->
        WeightEditorUiModel(
            isVisible = visible,
            selectedDate = date,
            weightText = text,
            canNavigateNext = date.isBefore(today)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeightEditorUiModel()
    )

    private val weeklyUiFlow: StateFlow<WeeklyMetricsUiModel> = combine(
        weeklyMetricsFlow,
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

    val uiState: StateFlow<DailyRegisterUiState> =
        combine(
            profileFlow,
            metricsFlow,
            stepStateFlow,
            weightCardFlow,
            weightEditorFlow,
            weeklyUiFlow
        ) { values ->
            val profile = values[0] as UserProfile
            val metrics = values[1] as pe.com.zzynan.procardapp.domain.model.DailyMetrics?
            val stepState = values[2] as StepCounterState
            val weightCard = values[3] as WeightCardUiModel
            val weightEditor = values[4] as WeightEditorUiModel
            val weeklyUi = values[5] as WeeklyMetricsUiModel

            DailyRegisterUiState(
                userName = profile.displayName,
                dateEpoch = todayFlow.value.toEpochDayLong(),
                stepCounter = StepCounterUiModel(
                    stepsToday = stepState.stepsToday,
                    isRunning = stepState.isRunning
                ),
                metrics = metrics?.toUiModel(),
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
            metricsFlow.collect { metrics ->
                todayWeightTextFlow.value = metrics?.weightFasted?.takeIf { it > 0f }
                    ?.let { formatWeight(it) }
                    ?: ""
            }
        }
        viewModelScope.launch {
            historyMetricsFlow.collect { metrics ->
                historyWeightTextFlow.value = metrics?.weightFasted?.takeIf { it > 0f }
                    ?.let { formatWeight(it) }
                    ?: ""
            }
        }
    }

    fun onToggleStepCounter() {
        stepCounterManager.toggle()
    }

    fun onWeightValueChanged(value: String) {
        val sanitized = sanitizeInput(value)
        todayWeightTextFlow.value = sanitized
        persistWeightForDate(sanitized, todayFlow.value)
    }

    fun onOpenHistory(date: LocalDate = todayFlow.value) {
        historyDateFlow.value = date
        isHistoryVisibleFlow.value = true
    }

    fun onDismissHistory() {
        isHistoryVisibleFlow.value = false
    }

    fun onHistoryPreviousDay() {
        historyDateFlow.value = historyDateFlow.value.minusDays(1)
    }

    fun onHistoryNextDay() {
        if (historyDateFlow.value.isBefore(todayFlow.value)) {
            historyDateFlow.value = historyDateFlow.value.plusDays(1)
        }
    }

    fun onHistoryWeightChanged(value: String) {
        val sanitized = sanitizeInput(value)
        historyWeightTextFlow.value = sanitized
        persistWeightForDate(sanitized, historyDateFlow.value)
    }

    fun onChartWeightSelected(date: LocalDate) {
        historyDateFlow.value = date
        isHistoryVisibleFlow.value = true
    }

    private fun persistWeightForDate(value: String, date: LocalDate) {
        val weightValue = value.toFloatOrNull() ?: return
        viewModelScope.launch {
            val username = profileFlow.value.displayName
            saveDailyWeightUseCase(username, date.toEpochDayLong(), weightValue)
        }
    }

    private fun formatWeight(value: Float): String =
        String.format(Locale.US, "%.2f", value)

    private fun sanitizeInput(value: String): String = value.replace(",", ".")

    private data class WeightCardSource(
        val username: String,
        val metrics: pe.com.zzynan.procardapp.domain.model.DailyMetrics?,
        val today: LocalDate,
        val input: String
    )

    companion object {
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
                val manager = StepCounterManager(appContext)
                @Suppress("UNCHECKED_CAST")
                return DailyRegisterViewModel(
                    getUseCase,
                    observeProfileUseCase,
                    manager,
                    saveWeightUseCase,
                    weeklyUseCase,
                    lastWeightUseCase
                ) as T
            }
        }
    }
}
