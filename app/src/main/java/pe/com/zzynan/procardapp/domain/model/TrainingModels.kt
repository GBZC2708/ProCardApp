package pe.com.zzynan.procardapp.domain.model

import java.time.LocalDate

data class WorkoutExercise(
    val id: Int,
    val name: String,
    val muscleGroup: String,
    val isActive: Boolean,
    val createdAt: Long
)

data class RoutineExercise(
    val id: Int,
    val routineDayId: Int,
    val exercise: WorkoutExercise,
    val defaultSets: Int
)

data class RoutineDay(
    val id: Int,
    val dayOfWeek: Int,
    val label: String,
    val exercises: List<RoutineExercise>
)

data class TrainingDayStatus(
    val routineDay: RoutineDay,
    val status: TrainingDayState,
    val activeSession: WorkoutSession?
)

enum class TrainingDayState { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class WorkoutSession(
    val id: Int,
    val date: LocalDate,
    val routineDayId: Int,
    val dayOfWeek: Int,
    val dayLabelSnapshot: String,
    val startedAt: Long,
    val durationMillis: Long?,
    val status: WorkoutSessionStatus
)

enum class WorkoutSessionStatus { IN_PROGRESS, COMPLETED }

data class WorkoutSetEntry(
    val id: Int,
    val sessionId: Int,
    val exerciseId: Int,
    val setIndex: Int,
    val weight: Float?,
    val reps: Int?,
    val isCompleted: Boolean,
    val updatedAt: Long
)

data class ExerciseSetStats(
    val exerciseId: Int,
    val setIndex: Int,
    val maxWeight: Float?,
    val maxReps: Int?
)

data class SessionExerciseSets(
    val exercise: WorkoutExercise,
    val sets: List<WorkoutSetEntry>
)
