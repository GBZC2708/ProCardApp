package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import pe.com.zzynan.procardapp.domain.model.TrainingStage

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
 * Función de extensión para mapear el enum a su valor entero persistible.
 */
fun TrainingStage.toDatabaseValue(): Int = value

/**
 * Función de extensión para mapear un entero leído desde Room a un enum.
 */
fun Int.toTrainingStage(): TrainingStage = TrainingStage.fromValue(this)
