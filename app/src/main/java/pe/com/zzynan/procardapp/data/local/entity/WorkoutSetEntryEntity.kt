package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set_entries",
    indices = [Index(value = ["sessionId", "exerciseId", "setIndex"], unique = true)]
)
data class WorkoutSetEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val exerciseId: Int,
    val setIndex: Int,
    val weight: Float? = null,
    val reps: Int? = null,
    val isCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
