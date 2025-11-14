package pe.com.zzynan.procardapp.ui.model

import pe.com.zzynan.procardapp.domain.model.TrainingStage

/**
 * Modelo para Compose que evita exponer entidades pesadas y mantiene la UI ligera.
 */
data class DailyMetricsUiModel(
    val username: String,
    val dateEpoch: Long,
    val weightFasted: Float,
    val dailySteps: Int,
    val cardioMinutes: Int,
    val trainingDone: Boolean,
    val waterMl: Int,
    val saltGramsX10: Int,
    val sleepMinutes: Int,
    val stage: TrainingStage
)
