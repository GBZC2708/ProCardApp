package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Entidad de Room que representa las métricas diarias de un usuario específico en un día determinado.
 * La combinación (username, dateEpoch) es la clave primaria compuesta para optimizar consultas.
 */
@Entity(tableName = "daily_metrics", primaryKeys = ["username", "dateEpoch"])
data class DailyMetricsEntity(
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "dateEpoch")
    val dateEpoch: Long,
    @ColumnInfo(name = "weightFasted")
    val weightFasted: Float,
    @ColumnInfo(name = "dailySteps")
    val dailySteps: Int,
    @ColumnInfo(name = "cardioMinutes")
    val cardioMinutes: Int,
    @ColumnInfo(name = "trainingDone")
    val trainingDone: Boolean,
    @ColumnInfo(name = "waterMl")
    val waterMl: Int,
    @ColumnInfo(name = "saltGramsX10")
    val saltGramsX10: Int,
    @ColumnInfo(name = "sleepMinutes")
    val sleepMinutes: Int,
    @ColumnInfo(name = "stage")
    val stage: Int
)

/**
 * Enum orientado a la capa de dominio que ayuda a interpretar el valor entero de la etapa.
 * Se almacena como Int en la base de datos para reducir uso de memoria y optimizar consultas.
 */
enum class TrainingStage(val value: Int) {
    DEFINICION(0),
    MANTENIMIENTO(1),
    DEFICIT(2);

    companion object {
        /**
         * Convierte el valor entero almacenado en base de datos a un enum legible.
         */
        fun fromValue(value: Int): TrainingStage = when (value) {
            DEFINICION.value -> DEFINICION
            MANTENIMIENTO.value -> MANTENIMIENTO
            DEFICIT.value -> DEFICIT
            else -> DEFICIT
        }
    }
}

/**
 * Función de extensión para mapear el enum a su valor entero persistible.
 */
fun TrainingStage.toDatabaseValue(): Int = value

/**
 * Función de extensión para mapear un entero leído desde Room a un enum.
 */
fun Int.toTrainingStage(): TrainingStage = TrainingStage.fromValue(this)
