package pe.com.zzynan.procardapp.data.repository

import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.dao.DailyMetricsDao
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.domain.model.TrainingStage

/**
 * Repositorio que actúa como punto único de acceso a los datos de métricas diarias.
 * Se diseñó para ser fácilmente inyectable y mantener la capa de UI desacoplada.
 */
class DailyMetricsRepository(
    private val dailyMetricsDao: DailyMetricsDao
) {

    /**
     * Guarda o actualiza el registro de métricas para un usuario en una fecha específica.
     */
    suspend fun upsertDailyMetrics(metrics: DailyMetricsEntity) {
        dailyMetricsDao.upsertMetrics(metrics)
    }

    /**
     * Observa el registro del día solicitado en tiempo real para integrarse con StateFlow/LiveData.
     */
    fun observeDailyMetrics(username: String, dateEpoch: Long): Flow<DailyMetricsEntity?> {
        return dailyMetricsDao.observeMetricsForDay(username, dateEpoch)
    }

    /**
     * Obtiene de forma puntual las métricas del día, ideal para servicios que solo necesitan un snapshot.
     */
    suspend fun getDailyMetrics(username: String, dateEpoch: Long): DailyMetricsEntity? {
        return dailyMetricsDao.getMetricsForDay(username, dateEpoch)
    }

    /**
     * Observa el historial completo ordenado por fecha descendente para mostrar tendencias rápidamente.
     */
    fun observeUserHistory(username: String): Flow<List<DailyMetricsEntity>> {
        return dailyMetricsDao.observeUserHistory(username)
    }

    /**
     * Observa un rango acotado de fechas para alimentar gráficos semanales.
     */
    fun observeMetricsBetween(
        username: String,
        startEpoch: Long,
        endEpoch: Long
    ): Flow<List<DailyMetricsEntity>> {
        return dailyMetricsDao.observeMetricsBetween(username, startEpoch, endEpoch)
    }

    /**
     * Obtiene el último peso registrado hasta la fecha indicada, útil para placeholders.
     */
    suspend fun getLastWeightOnOrBefore(username: String, dateEpoch: Long): DailyMetricsEntity? {
        return dailyMetricsDao.getLastWeightOnOrBefore(username, dateEpoch)
    }

    /**
     * Permite limpiar todos los registros de un usuario cuando ya no se necesitan localmente.
     */
    suspend fun clearUserHistory(username: String) {
        dailyMetricsDao.deleteAllForUser(username)
    }

    /**
     * Permite eliminar registros muy antiguos para optimizar almacenamiento sin perder datos recientes.
     */
    suspend fun purgeHistoryBefore(username: String, thresholdEpochDay: Long) {
        dailyMetricsDao.deleteOlderThan(username, thresholdEpochDay)
    }

    /**
     * Obtiene las métricas del día. Si no existen, las crea.
     * Además garantiza que la suplementación siempre inicie en false cada día.
     */
    suspend fun getOrCreateTodayMetrics(
        username: String,
        dateEpoch: Long
    ): DailyMetricsEntity {

        val today = dailyMetricsDao.getMetricsForDay(username, dateEpoch)

        return if (today == null) {

            val newMetrics = DailyMetricsEntity(
                username = username,
                dateEpoch = dateEpoch,
                weightFasted = 0f,
                dailySteps = 0,
                cardioMinutes = 0,
                trainingDone = false,
                waterMl = 0,
                saltGramsX10 = 0,
                sleepMinutes = 0,
                stage = TrainingStage.DEFINICION.value
            )

            dailyMetricsDao.upsertMetrics(newMetrics)
            newMetrics

        } else {
            today
        }
    }
}

