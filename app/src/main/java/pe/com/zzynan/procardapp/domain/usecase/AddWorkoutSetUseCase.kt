package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.domain.model.WorkoutSetEntry

class AddWorkoutSetUseCase(private val repository: TrainingRepository) {
    suspend operator fun invoke(sessionId: Int, exerciseId: Int): WorkoutSetEntry {
        return repository.addSet(sessionId, exerciseId)
    }
}
