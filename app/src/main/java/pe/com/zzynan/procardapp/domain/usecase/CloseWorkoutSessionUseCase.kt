package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.domain.model.WorkoutSession

class CloseWorkoutSessionUseCase(
    private val repository: TrainingRepository,
    private val calculateBestStatsUseCase: CalculateBestStatsUseCase
) {
    suspend operator fun invoke(session: WorkoutSession, endTimestamp: Long) {
        val duration = (endTimestamp - session.startedAt).coerceAtLeast(0L)
        repository.markSessionCompleted(session.id, duration)
        val sets = repository.getSessionSets(session.id)
        calculateBestStatsUseCase(sets)
    }
}
