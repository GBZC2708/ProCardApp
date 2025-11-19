package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.TrainingRepository

class SaveWorkoutSetUseCase(private val repository: TrainingRepository) {
    suspend fun updateWeight(setId: Int, weight: Float?) {
        repository.updateSetWeight(setId, weight)
    }

    suspend fun updateReps(setId: Int, reps: Int?) {
        repository.updateSetReps(setId, reps)
    }

    suspend fun toggleCompletion(setId: Int, completed: Boolean) {
        repository.toggleSetCompletion(setId, completed)
    }
}
