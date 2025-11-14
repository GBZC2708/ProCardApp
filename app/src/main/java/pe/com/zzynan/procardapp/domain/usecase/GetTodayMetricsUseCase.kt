package pe.com.zzynan.procardapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

/**
 * Use case que centraliza la lectura reactiva y puntual de las métricas del día.
 */
class GetTodayMetricsUseCase(
    private val dailyMetricsRepository: DailyMetricsRepository
) {

    /**
     * Exposición reactiva para la capa de presentación.
     */
    fun observe(username: String, dateEpoch: Long): Flow<DailyMetrics?> {
        return dailyMetricsRepository.observeDailyMetrics(username, dateEpoch)
            .map { entity -> entity?.toDomain() }
    }

    /**
     * Snapshot puntual para servicios o tareas en background sin mantener Flow abierto.
     */
    suspend fun current(username: String, dateEpoch: Long): DailyMetrics? {
        return dailyMetricsRepository.getDailyMetrics(username, dateEpoch)?.toDomain()
    }
}
