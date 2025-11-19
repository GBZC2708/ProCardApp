package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.domain.model.ExerciseSetStats
import pe.com.zzynan.procardapp.domain.model.WorkoutSetEntry

class CalculateBestStatsUseCase(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(entries: List<WorkoutSetEntry>) {
        entries.groupBy { it.exerciseId }.forEach { (_, sets) ->
            sets.forEach { entry ->
                val weight = entry.weight
                val reps = entry.reps
                val current = repository.getStats(entry.exerciseId, entry.setIndex)
                var maxWeight = current?.maxWeight
                var maxReps = current?.maxReps
                var shouldUpdate = false

                if (weight != null && (maxWeight == null || weight > maxWeight)) {
                    maxWeight = weight
                    shouldUpdate = true
                }
                if (reps != null && (maxReps == null || reps > maxReps)) {
                    maxReps = reps
                    shouldUpdate = true
                }
                if (shouldUpdate) {
                    repository.upsertStats(
                        ExerciseSetStats(
                            exerciseId = entry.exerciseId,
                            setIndex = entry.setIndex,
                            maxWeight = maxWeight,
                            maxReps = maxReps
                        )
                    )
                }
            }
        }
    }
}
