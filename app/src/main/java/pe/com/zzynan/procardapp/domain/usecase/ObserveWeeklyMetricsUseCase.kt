package pe.com.zzynan.procardapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

/**
 * Use case que expone los últimos días de métricas para los gráficos.
 */
class ObserveWeeklyMetricsUseCase(
    private val dailyMetricsRepository: DailyMetricsRepository
) {
    operator fun invoke(
        username: String,
        endDateEpoch: Long,
        days: Int = 7
    ): Flow<List<DailyMetrics>> {
        val startEpoch = endDateEpoch - (days - 1)
        return dailyMetricsRepository.observeMetricsBetween(username, startEpoch, endDateEpoch)
            .map { list -> list.map { it.toDomain() } }
    }
}
