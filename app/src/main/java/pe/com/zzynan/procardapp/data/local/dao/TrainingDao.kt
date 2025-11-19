package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.ExerciseSetStatsEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineDayEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSessionEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSetEntryEntity

@Dao
interface TrainingDao {
    @Query("SELECT * FROM workout_exercises ORDER BY isActive DESC, name ASC")
    fun observeExercises(): Flow<List<WorkoutExerciseEntity>>

    @Insert
    suspend fun insertExercise(entity: WorkoutExerciseEntity): Long

    @Update
    suspend fun updateExercise(entity: WorkoutExerciseEntity)

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getExerciseById(id: Int): WorkoutExerciseEntity?

    @Transaction
    @Query("SELECT * FROM routine_days ORDER BY dayOfWeek ASC")
    fun observeRoutineDaysWithExercises(): Flow<List<RoutineDayWithExercises>>

    @Query("SELECT * FROM routine_days ORDER BY dayOfWeek ASC")
    suspend fun getRoutineDaysOnce(): List<RoutineDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineDay(day: RoutineDayEntity): Long

    @Update
    suspend fun updateRoutineDay(day: RoutineDayEntity)

    @Insert
    suspend fun insertRoutineExercise(entity: RoutineExerciseEntity): Long

    @Delete
    suspend fun deleteRoutineExercise(entity: RoutineExerciseEntity)

    @Query("SELECT * FROM routine_exercises WHERE id = :id")
    suspend fun getRoutineExerciseById(id: Int): RoutineExerciseEntity?

    @Query("SELECT * FROM routine_exercises WHERE routineDayId = :dayId ORDER BY id ASC")
    suspend fun getRoutineExercisesForDay(dayId: Int): List<RoutineExerciseEntity>

    @Transaction
    @Query("SELECT * FROM routine_days WHERE id = :id LIMIT 1")
    suspend fun getRoutineDayWithExercises(id: Int): RoutineDayWithExercises?

    @Insert
    suspend fun insertSession(entity: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(entity: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSessionById(id: Int): Flow<WorkoutSessionEntity?>

    @Query(
        "SELECT * FROM workout_sessions WHERE routineDayId = :routineDayId AND date = :date ORDER BY startedAt DESC LIMIT 1"
    )
    suspend fun getSessionForDate(routineDayId: Int, date: String): WorkoutSessionEntity?

    @Query(
        "SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate"
    )
    fun observeSessionsBetween(startDate: String, endDate: String): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insertSetEntry(entity: WorkoutSetEntryEntity): Long

    @Update
    suspend fun updateSetEntry(entity: WorkoutSetEntryEntity)

    @Delete
    suspend fun deleteSetEntry(entity: WorkoutSetEntryEntity)

    @Query("SELECT * FROM workout_set_entries WHERE id = :id")
    suspend fun getSetEntryById(id: Int): WorkoutSetEntryEntity?

    @Query(
        "SELECT * FROM workout_set_entries WHERE sessionId = :sessionId ORDER BY exerciseId ASC, setIndex ASC"
    )
    fun observeSetEntries(sessionId: Int): Flow<List<WorkoutSetEntryEntity>>

    @Query(
        "SELECT * FROM workout_set_entries WHERE sessionId = :sessionId ORDER BY exerciseId ASC, setIndex ASC"
    )
    suspend fun getSetEntriesForSession(sessionId: Int): List<WorkoutSetEntryEntity>

    @Query(
        "SELECT * FROM workout_set_entries WHERE sessionId = :sessionId AND exerciseId = :exerciseId ORDER BY setIndex ASC"
    )
    suspend fun getSetEntriesForExercise(sessionId: Int, exerciseId: Int): List<WorkoutSetEntryEntity>

    @Query("SELECT * FROM exercise_set_stats WHERE exerciseId IN (:exerciseIds)")
    fun observeStats(exerciseIds: List<Int>): Flow<List<ExerciseSetStatsEntity>>

    @Query(
        "SELECT * FROM exercise_set_stats WHERE exerciseId = :exerciseId AND setIndex = :setIndex LIMIT 1"
    )
    suspend fun getStatsForExerciseSet(exerciseId: Int, setIndex: Int): ExerciseSetStatsEntity?

    @Insert
    suspend fun insertStats(entity: ExerciseSetStatsEntity): Long

    @Update
    suspend fun updateStats(entity: ExerciseSetStatsEntity)
}

data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineDayId",
        entity = RoutineExerciseEntity::class
    )
    val exercises: List<RoutineExerciseWithExercise>
)

data class RoutineExerciseWithExercise(
    @Embedded val routineExercise: RoutineExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: WorkoutExerciseEntity
)
