package pe.com.zzynan.procardapp.domain.usecase

import java.time.LocalDate
import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.domain.model.RoutineDay
import pe.com.zzynan.procardapp.domain.model.WorkoutSession
import pe.com.zzynan.procardapp.domain.model.WorkoutSessionStatus

class CreateOrResumeWorkoutSessionUseCase(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(
        routineDay: RoutineDay,
        date: LocalDate,
        startedAt: Long,
        forceNew: Boolean = false
    ): WorkoutSession {
        val existing = repository.getSessionForDate(routineDay.id, date)
        return if (existing != null && existing.status == WorkoutSessionStatus.IN_PROGRESS && !forceNew) {
            existing
        } else if (existing != null && !forceNew) {
            existing
        } else {
            repository.createSession(routineDay, date, startedAt)
        }
    }
}
