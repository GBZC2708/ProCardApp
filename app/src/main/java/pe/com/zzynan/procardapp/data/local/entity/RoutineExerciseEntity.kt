package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercises",
    indices = [Index(value = ["routineDayId", "exerciseId"], unique = true)]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineDayId: Int,
    val exerciseId: Int,
    val defaultSets: Int = 1,
    val orderIndex: Int = 0        // 👈 nuevo campo de orden
)

