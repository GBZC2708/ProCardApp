package pe.com.zzynan.procardapp.data.mappers

import java.time.LocalDate
import pe.com.zzynan.procardapp.data.local.dao.RoutineDayWithExercises
import pe.com.zzynan.procardapp.data.local.dao.RoutineExerciseWithExercise
import pe.com.zzynan.procardapp.data.local.entity.ExerciseSetStatsEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineDayEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSessionEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSetEntryEntity
import pe.com.zzynan.procardapp.domain.model.ExerciseSetStats
import pe.com.zzynan.procardapp.domain.model.RoutineDay
import pe.com.zzynan.procardapp.domain.model.RoutineExercise
import pe.com.zzynan.procardapp.domain.model.SessionExerciseSets
import pe.com.zzynan.procardapp.domain.model.WorkoutExercise
import pe.com.zzynan.procardapp.domain.model.WorkoutSession
import pe.com.zzynan.procardapp.domain.model.WorkoutSessionStatus
import pe.com.zzynan.procardapp.domain.model.WorkoutSetEntry

fun WorkoutExerciseEntity.toDomain() = WorkoutExercise(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    isActive = isActive,
    createdAt = createdAt
)

fun RoutineDayWithExercises.toDomain(): RoutineDay = RoutineDay(
    id = day.id,
    dayOfWeek = day.dayOfWeek,
    label = day.label,
    exercises = exercises.map { it.toDomain() }
)

fun RoutineExerciseWithExercise.toDomain(): RoutineExercise = RoutineExercise(
    id = routineExercise.id,
    routineDayId = routineExercise.routineDayId,
    exercise = exercise.toDomain(),
    defaultSets = routineExercise.defaultSets
)

fun RoutineDayEntity.toDomain(exercises: List<RoutineExercise>): RoutineDay = RoutineDay(
    id = id,
    dayOfWeek = dayOfWeek,
    label = label,
    exercises = exercises
)

fun RoutineExerciseEntity.toDomain(exercise: WorkoutExerciseEntity): RoutineExercise = RoutineExercise(
    id = id,
    routineDayId = routineDayId,
    exercise = exercise.toDomain(),
    defaultSets = defaultSets
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    date = LocalDate.parse(date),
    routineDayId = routineDayId,
    dayOfWeek = dayOfWeek,
    dayLabelSnapshot = dayLabelSnapshot,
    startedAt = startedAt,
    durationMillis = durationMillis,
    status = if (status == WorkoutSessionStatus.COMPLETED.name) {
        WorkoutSessionStatus.COMPLETED
    } else {
        WorkoutSessionStatus.IN_PROGRESS
    }
)

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    date = date.toString(),
    routineDayId = routineDayId,
    dayOfWeek = dayOfWeek,
    dayLabelSnapshot = dayLabelSnapshot,
    startedAt = startedAt,
    durationMillis = durationMillis,
    status = status.name
)

fun WorkoutSetEntryEntity.toDomain(): WorkoutSetEntry = WorkoutSetEntry(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    setIndex = setIndex,
    weight = weight,
    reps = reps,
    isCompleted = isCompleted,
    updatedAt = updatedAt
)

fun WorkoutSetEntry.toEntity(): WorkoutSetEntryEntity = WorkoutSetEntryEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    setIndex = setIndex,
    weight = weight,
    reps = reps,
    isCompleted = isCompleted,
    updatedAt = updatedAt
)

fun ExerciseSetStatsEntity.toDomain(): ExerciseSetStats = ExerciseSetStats(
    exerciseId = exerciseId,
    setIndex = setIndex,
    maxWeight = maxWeight,
    maxReps = maxReps
)

fun ExerciseSetStats.toEntity(existingId: Int? = null): ExerciseSetStatsEntity = ExerciseSetStatsEntity(
    id = existingId ?: 0,
    exerciseId = exerciseId,
    setIndex = setIndex,
    maxWeight = maxWeight,
    maxReps = maxReps,
    updatedAt = System.currentTimeMillis()
)

fun SessionExerciseSets.toWorkoutSetEntities(): List<WorkoutSetEntryEntity> = sets.map { it.toEntity() }
