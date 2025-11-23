package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics
import pe.com.zzynan.procardapp.domain.model.TrainingStage

class SaveDailyStageUseCase(private val repo: DailyMetricsRepository) {
    suspend operator fun invoke(username: String, dateEpoch: Long, stage: TrainingStage) {
        val current = repo.getDailyMetrics(username, dateEpoch)?.toDomain()
        val updated = (current ?: DailyMetrics(username = username, dateEpoch = dateEpoch))
            .copy(stage = stage)
        repo.upsertDailyMetrics(updated.toEntity())
    }
}
