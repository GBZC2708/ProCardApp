package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val routineDayId: Int,
    val dayOfWeek: Int,
    val dayLabelSnapshot: String,
    val startedAt: Long,
    val durationMillis: Long? = null,
    val status: String
)
