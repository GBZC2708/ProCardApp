package pe.com.zzynan.procardapp.core.steps

/**
 * Estado compartido entre servicio y ViewModels para evitar lecturas directas del sensor desde la UI.
 */
data class StepCounterState(
    val stepsToday: Int = 0,
    val isRunning: Boolean = false
)
