package pe.com.zzynan.procardapp.data.repository

import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pe.com.zzynan.procardapp.data.local.dao.TrainingDao
import pe.com.zzynan.procardapp.data.local.entity.RoutineDayEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSessionEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSetEntryEntity
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.domain.model.ExerciseSetStats
import pe.com.zzynan.procardapp.domain.model.RoutineDay
import pe.com.zzynan.procardapp.domain.model.RoutineExercise
import pe.com.zzynan.procardapp.domain.model.TrainingDayState
import pe.com.zzynan.procardapp.domain.model.TrainingDayStatus
import pe.com.zzynan.procardapp.domain.model.WorkoutExercise
import pe.com.zzynan.procardapp.domain.model.WorkoutSession
import pe.com.zzynan.procardapp.domain.model.WorkoutSessionStatus
import pe.com.zzynan.procardapp.domain.model.WorkoutSetEntry

interface TrainingRepository {
    fun observeExercises(): Flow<List<WorkoutExercise>>
    suspend fun addExercise(name: String, muscleGroup: String): WorkoutExercise
    suspend fun renameExercise(id: Int, newName: String)
    suspend fun changeMuscleGroup(id: Int, newGroup: String)
    suspend fun toggleExerciseActive(id: Int)

    suspend fun deleteExercise(id: Int)
    fun observeRoutine(): Flow<List<RoutineDay>>
    suspend fun ensureRoutineDays()
    suspend fun updateRoutineLabel(dayId: Int, newLabel: String)
    suspend fun addExerciseToRoutine(dayId: Int, exerciseId: Int): RoutineExercise
    suspend fun removeRoutineExercise(entryId: Int)
    fun observeTrainingDays(weekStart: LocalDate): Flow<List<TrainingDayStatus>>
    suspend fun getRoutineDay(dayId: Int): RoutineDay?
    suspend fun getRoutineDayByWeekIndex(dayOfWeek: Int): RoutineDay?
    suspend fun getSessionForDate(routineDayId: Int, date: LocalDate): WorkoutSession?
    suspend fun createSession(routineDay: RoutineDay, date: LocalDate, startedAt: Long): WorkoutSession
    fun observeSession(sessionId: Int): Flow<WorkoutSession?>
    fun observeSessionSets(sessionId: Int): Flow<List<WorkoutSetEntry>>
    suspend fun updateSetWeight(setId: Int, weight: Float?)
    suspend fun updateSetReps(setId: Int, reps: Int?)
    suspend fun toggleSetCompletion(setId: Int, isCompleted: Boolean)
    suspend fun addSet(sessionId: Int, exerciseId: Int): WorkoutSetEntry
    suspend fun removeLastSet(sessionId: Int, exerciseId: Int): Boolean
    suspend fun markSessionCompleted(sessionId: Int, durationMillis: Long)
    suspend fun getSessionSets(sessionId: Int): List<WorkoutSetEntry>
    fun observeStats(exerciseIds: List<Int>): Flow<List<ExerciseSetStats>>
    suspend fun upsertStats(stat: ExerciseSetStats)
    suspend fun getStats(exerciseId: Int, setIndex: Int): ExerciseSetStats?
}

class TrainingRepositoryImpl(private val dao: TrainingDao) : TrainingRepository {

    private val routineSeedFlow = MutableStateFlow(false)

    override fun observeExercises(): Flow<List<WorkoutExercise>> =
        dao.observeExercises().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addExercise(name: String, muscleGroup: String): WorkoutExercise {
        val entity = WorkoutExerciseEntity(
            name = name,
            muscleGroup = muscleGroup,
            isActive = true
        )
        val id = dao.insertExercise(entity).toInt()
        return dao.getExerciseById(id)!!.toDomain()
    }

    override suspend fun renameExercise(id: Int, newName: String) {
        val current = dao.getExerciseById(id) ?: return
        dao.updateExercise(current.copy(name = newName))
    }

    override suspend fun changeMuscleGroup(id: Int, newGroup: String) {
        val current = dao.getExerciseById(id) ?: return
        dao.updateExercise(current.copy(muscleGroup = newGroup))
    }

    override suspend fun toggleExerciseActive(id: Int) {
        val current = dao.getExerciseById(id) ?: return
        dao.updateExercise(current.copy(isActive = !current.isActive))
    }

    override suspend fun deleteExercise(id: Int) {
        val current = dao.getExerciseById(id) ?: return
        dao.deleteExercise(current)
    }
    override fun observeRoutine(): Flow<List<RoutineDay>> {
        return dao.observeRoutineDaysWithExercises()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun ensureRoutineDays() {
        if (routineSeedFlow.value) return
        routineSeedFlow.value = true
        withContext(Dispatchers.IO) {
            val existing = dao.getRoutineDaysOnce()
            val needed = DayOfWeek.values().map { it.ordinal }
            val missing = needed.filter { dayIndex -> existing.none { it.dayOfWeek == dayIndex } }
            missing.forEach { index ->
                dao.insertRoutineDay(
                    RoutineDayEntity(
                        dayOfWeek = index,
                        label = defaultLabelFor(index)
                    )
                )
            }
        }
    }

    override suspend fun updateRoutineLabel(dayId: Int, newLabel: String) {
        val current = dao.getRoutineDaysOnce().firstOrNull { it.id == dayId } ?: return
        dao.updateRoutineDay(current.copy(label = newLabel))
    }

    override suspend fun addExerciseToRoutine(dayId: Int, exerciseId: Int): RoutineExercise {
        val exercise = dao.getExerciseById(exerciseId) ?: throw IllegalArgumentException("Exercise not found")
        val entry = RoutineExerciseEntity(
            routineDayId = dayId,
            exerciseId = exerciseId,
            defaultSets = 1
        )
        val id = dao.insertRoutineExercise(entry).toInt()
        val inserted = dao.getRoutineExerciseById(id) ?: throw IllegalStateException("Routine exercise not found")
        return inserted.toDomain(exercise)
    }

    override suspend fun removeRoutineExercise(entryId: Int) {
        val current = dao.getRoutineExerciseById(entryId) ?: return
        dao.deleteRoutineExercise(current)
    }

    override fun observeTrainingDays(weekStart: LocalDate): Flow<List<TrainingDayStatus>> {
        val weekEnd = weekStart.plusDays(6)
        return combine(
            observeRoutine(),
            dao.observeSessionsBetween(weekStart.toString(), weekEnd.toString())
                .map { sessions -> sessions.map { it.toDomain() } }
        ) { routine, sessions ->
            val grouped = sessions.groupBy { it.routineDayId }
            routine.sortedBy { it.dayOfWeek }.map { day ->
                val session = grouped[day.id]?.maxByOrNull { it.startedAt }
                val status = when (session?.status) {
                    WorkoutSessionStatus.IN_PROGRESS -> TrainingDayState.IN_PROGRESS
                    WorkoutSessionStatus.COMPLETED -> TrainingDayState.COMPLETED
                    null -> TrainingDayState.NOT_STARTED
                }
                TrainingDayStatus(
                    routineDay = day,
                    status = status,
                    activeSession = session)
            }
        }
    }

    override suspend fun getRoutineDay(dayId: Int): RoutineDay? {
        val data = dao.getRoutineDayWithExercises(dayId) ?: return null
        return data.toDomain()
    }

    override suspend fun getRoutineDayByWeekIndex(dayOfWeek: Int): RoutineDay? {
        val days = dao.getRoutineDaysOnce()
        val day = days.firstOrNull { it.dayOfWeek == dayOfWeek } ?: return null
        val exercises = dao.getRoutineExercisesForDay(day.id).mapNotNull { routineExercise ->
            val exercise = dao.getExerciseById(routineExercise.exerciseId) ?: return@mapNotNull null
            routineExercise.toDomain(exercise)
        }
        return day.toDomain(exercises)
    }

    override suspend fun getSessionForDate(routineDayId: Int, date: LocalDate): WorkoutSession? {
        return dao.getSessionForDate(routineDayId, date.toString())?.toDomain()
    }

    override suspend fun createSession(
        routineDay: RoutineDay,
        date: LocalDate,
        startedAt: Long
    ): WorkoutSession {
        val sessionEntity = WorkoutSessionEntity(
            date = date.toString(),
            routineDayId = routineDay.id,
            dayOfWeek = routineDay.dayOfWeek,
            dayLabelSnapshot = routineDay.label,
            startedAt = startedAt,
            durationMillis = null,
            status = WorkoutSessionStatus.IN_PROGRESS.name
        )
        val sessionId = dao.insertSession(sessionEntity).toInt()
        routineDay.exercises.forEach { routineExercise ->
            val setCount = routineExercise.defaultSets.coerceAtLeast(1)
            repeat(setCount) { index ->
                dao.insertSetEntry(
                    WorkoutSetEntryEntity(
                        sessionId = sessionId,
                        exerciseId = routineExercise.exercise.id,
                        setIndex = index + 1,
                        updatedAt = startedAt
                    )
                )
            }
        }
        return dao.getSessionById(sessionId)?.toDomain()
            ?: throw IllegalStateException("Unable to load created session")
    }

    override fun observeSession(sessionId: Int): Flow<WorkoutSession?> {
        return dao.observeSessionById(sessionId).map { it?.toDomain() }
    }

    override fun observeSessionSets(sessionId: Int): Flow<List<WorkoutSetEntry>> {
        return dao.observeSetEntries(sessionId)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun updateSetWeight(setId: Int, weight: Float?) {
        val current = dao.getSetEntryById(setId) ?: return
        dao.updateSetEntry(current.copy(weight = weight, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateSetReps(setId: Int, reps: Int?) {
        val current = dao.getSetEntryById(setId) ?: return
        dao.updateSetEntry(current.copy(reps = reps, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun toggleSetCompletion(setId: Int, isCompleted: Boolean) {
        val current = dao.getSetEntryById(setId) ?: return
        dao.updateSetEntry(current.copy(isCompleted = isCompleted, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun addSet(sessionId: Int, exerciseId: Int): WorkoutSetEntry {
        val existing = dao.getSetEntriesForExercise(sessionId, exerciseId)
        val nextIndex = (existing.maxOfOrNull { it.setIndex } ?: 0) + 1
        val id = dao.insertSetEntry(
            WorkoutSetEntryEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                setIndex = nextIndex
            )
        ).toInt()
        return dao.getSetEntryById(id)!!.toDomain()
    }

    override suspend fun removeLastSet(sessionId: Int, exerciseId: Int): Boolean {
        val entries = dao.getSetEntriesForExercise(sessionId, exerciseId)
        if (entries.size <= 1) return false
        val last = entries.maxByOrNull { it.setIndex } ?: return false
        dao.deleteSetEntry(last)
        return true
    }

    override suspend fun markSessionCompleted(sessionId: Int, durationMillis: Long) {
        val current = dao.getSessionById(sessionId) ?: return
        dao.updateSession(current.copy(durationMillis = durationMillis, status = WorkoutSessionStatus.COMPLETED.name))
    }

    override suspend fun getSessionSets(sessionId: Int): List<WorkoutSetEntry> =
        dao.getSetEntriesForSession(sessionId).map { it.toDomain() }

    override fun observeStats(exerciseIds: List<Int>): Flow<List<ExerciseSetStats>> {
        if (exerciseIds.isEmpty()) return flow { emit(emptyList()) }
        return dao.observeStats(exerciseIds).map { stats -> stats.map { it.toDomain() } }
    }

    override suspend fun upsertStats(stat: ExerciseSetStats) {
        val existing = dao.getStatsForExerciseSet(stat.exerciseId, stat.setIndex)
        if (existing == null) {
            dao.insertStats(stat.toEntity())
        } else {
            dao.updateStats(stat.toEntity(existing.id))
        }
    }

    override suspend fun getStats(exerciseId: Int, setIndex: Int): ExerciseSetStats? {
        return dao.getStatsForExerciseSet(exerciseId, setIndex)?.toDomain()
    }

    private fun defaultLabelFor(dayIndex: Int): String = when (DayOfWeek.of(dayIndex + 1)) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }
}
