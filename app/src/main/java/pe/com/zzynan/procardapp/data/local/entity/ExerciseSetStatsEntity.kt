package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_set_stats",
    indices = [Index(value = ["exerciseId", "setIndex"], unique = true)]
)
data class ExerciseSetStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val setIndex: Int,
    val maxWeight: Float? = null,
    val maxReps: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
