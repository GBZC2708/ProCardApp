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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.core.steps.StepCounterManager
import pe.com.zzynan.procardapp.core.steps.StepCounterState
import pe.com.zzynan.procardapp.domain.model.DailyMetrics
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
    private val getLastWeightOnOrBeforeUseCase: GetLastWeightOnOrBeforeUseCase
) : ViewModel() {

    private val todayFlow = MutableStateFlow(LocalDate.now())
    private val historyDateFlow = MutableStateFlow(LocalDate.now())
    private val isHistoryVisibleFlow = MutableStateFlow(false)
    private val historyWeightTextFlow = MutableStateFlow("")
    private val historyPlaceholderFlow = MutableStateFlow<String?>(null)
    private val eventsFlow = MutableSharedFlow<UiEvent>()

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

    val events = eventsFlow.asSharedFlow()



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
        eventsFlow.emit(UiEvent.ShowMessage(R.string.weight_saved_message))
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

    sealed class UiEvent {
        data class ShowMessage(val messageRes: Int) : UiEvent()
    }

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
