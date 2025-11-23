package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

class SaveDailyWaterUseCase(private val repo: DailyMetricsRepository) {
    suspend operator fun invoke(username: String, dateEpoch: Long, waterMl: Int) {
        val current = repo.getDailyMetrics(username, dateEpoch)?.toDomain()
        val updated = (current ?: DailyMetrics(username = username, dateEpoch = dateEpoch))
            .copy(waterMl = waterMl)
        repo.upsertDailyMetrics(updated.toEntity())
    }
}
