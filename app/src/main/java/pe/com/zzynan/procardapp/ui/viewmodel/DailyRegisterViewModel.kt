package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.core.steps.StepCounterManager
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.usecase.GetTodayMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.ui.mappers.toUiModel
import pe.com.zzynan.procardapp.ui.model.StepCounterUiModel
import pe.com.zzynan.procardapp.ui.state.DailyRegisterUiState

/**
 * ViewModel de la pantalla de registro diario. Orquesta pasos, métricas y nombre del usuario.
 */
class DailyRegisterViewModel(
    private val getTodayMetricsUseCase: GetTodayMetricsUseCase,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val stepCounterManager: StepCounterManager
) : ViewModel() {

    private val todayFlow = MutableStateFlow(LocalDate.now())

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

    private val stepStateFlow = stepCounterManager.observeState()

    val uiState: StateFlow<DailyRegisterUiState> = combine(
        profileFlow,
        metricsFlow,
        stepStateFlow
    ) { profile, metrics, stepState ->
        DailyRegisterUiState(
            userName = profile.displayName,
            dateEpoch = todayFlow.value.toEpochDayLong(),
            stepCounter = StepCounterUiModel(
                stepsToday = stepState.stepsToday,
                isRunning = stepState.isRunning
            ),
            metrics = metrics?.toUiModel(),
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
    }

    fun onToggleStepCounter() {
        stepCounterManager.toggle()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appContext = context.applicationContext
                val dailyMetricsRepository = ServiceLocator.provideDailyMetricsRepository(appContext)
                val userProfileRepository = ServiceLocator.provideUserProfileRepository(appContext)
                val getUseCase = GetTodayMetricsUseCase(dailyMetricsRepository)
                val observeProfileUseCase = ObserveUserProfileUseCase(userProfileRepository)
                val manager = StepCounterManager(appContext)
                @Suppress("UNCHECKED_CAST")
                return DailyRegisterViewModel(getUseCase, observeProfileUseCase, manager) as T
            }
        }
    }
}
