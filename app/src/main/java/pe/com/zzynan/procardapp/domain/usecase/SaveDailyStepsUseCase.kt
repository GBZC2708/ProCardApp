package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

/**
 * Use case encargado de persistir los pasos diarios asegurando consistencia con los datos previos.
 */
class SaveDailyStepsUseCase(
    private val dailyMetricsRepository: DailyMetricsRepository
) {

    /**
     * Guarda los pasos reutilizando registros previos para no perder datos complementarios.
     */
    suspend operator fun invoke(username: String, dateEpoch: Long, steps: Int) {
        val current = dailyMetricsRepository.getDailyMetrics(username, dateEpoch)?.toDomain()
        val updated = (current ?: DailyMetrics(username = username, dateEpoch = dateEpoch))
            .copy(dailySteps = steps)
        dailyMetricsRepository.upsertDailyMetrics(updated.toEntity())
    }
}
