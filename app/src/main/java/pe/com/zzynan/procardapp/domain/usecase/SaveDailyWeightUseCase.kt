package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

/**
 * Use case para guardar el peso en ayunas manteniendo el resto de métricas intactas.
 */
class SaveDailyWeightUseCase(
    private val dailyMetricsRepository: DailyMetricsRepository
) {

    suspend operator fun invoke(username: String, dateEpoch: Long, weight: Float) {
        val current = dailyMetricsRepository.getDailyMetrics(username, dateEpoch)?.toDomain()
        val updated = (current ?: DailyMetrics(username = username, dateEpoch = dateEpoch))
            .copy(weightFasted = weight)
        dailyMetricsRepository.upsertDailyMetrics(updated.toEntity())
    }
}
