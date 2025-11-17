package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository

/**
 * Use case para obtener el último peso registrado previo a una fecha.
 */
class GetLastWeightOnOrBeforeUseCase(
    private val dailyMetricsRepository: DailyMetricsRepository
) {
    suspend operator fun invoke(username: String, dateEpoch: Long): Float? {
        return dailyMetricsRepository.getLastWeightOnOrBefore(username, dateEpoch)?.weightFasted
    }
}
