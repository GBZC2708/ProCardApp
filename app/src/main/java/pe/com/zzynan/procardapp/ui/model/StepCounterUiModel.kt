package pe.com.zzynan.procardapp.ui.model

/**
 * Modelo simple para representar el contador de pasos en Compose sin recalcular en cada recomposición.
 */
data class StepCounterUiModel(
    val stepsToday: Int = 0,
    val isRunning: Boolean = false
)
