package pe.com.zzynan.procardapp.core.steps

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Holder centralizado para exponer el estado del contador mediante StateFlow sin fugas de memoria.
 */
object StepCounterStateHolder {

    private val internalState = MutableStateFlow(StepCounterState())

    val state: StateFlow<StepCounterState> = internalState

    fun updateSteps(steps: Int) {
        internalState.update { current -> current.copy(stepsToday = steps.coerceAtLeast(0)) }
    }

    fun setRunning(isRunning: Boolean) {
        internalState.update { current -> current.copy(isRunning = isRunning) }
    }
}
