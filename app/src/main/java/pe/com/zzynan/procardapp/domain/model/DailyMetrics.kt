package pe.com.zzynan.procardapp.domain.model

/**
 * Modelo de dominio liviano para operar las métricas del día sin exponer detalles de almacenamiento.
 */
data class DailyMetrics(
    val username: String,
    val dateEpoch: Long,
    val weightFasted: Float = 0f,
    val dailySteps: Int = 0,
    val cardioMinutes: Int = 0,
    val trainingDone: Boolean = false,
    val waterMl: Int = 0,
    val saltGramsX10: Int = 0,
    val sleepMinutes: Int = 0,
    val stage: TrainingStage = TrainingStage.DEFINICION
)
