package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.TrainingRepository

class RemoveWorkoutSetUseCase(private val repository: TrainingRepository) {
    suspend operator fun invoke(sessionId: Int, exerciseId: Int): Boolean {
        return repository.removeLastSet(sessionId, exerciseId)
    }
}
