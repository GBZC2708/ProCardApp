package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity

/**
 * DAO que encapsula las operaciones de acceso a los datos para la tabla de métricas diarias.
 * Se priorizan operaciones suspend y flujos reactivas para integrarse con corrutinas y Jetpack.
 */
@Dao
interface DailyMetricsDao {

    /**
     * Inserta o actualiza (upsert) el registro de métricas diarias, evitando duplicados por clave compuesta.
     */
    @Upsert
    suspend fun upsertMetrics(metrics: DailyMetricsEntity)

    /**
     * Observa en tiempo real las métricas de un usuario en un día específico utilizando Flow.
     */
    @Query(
        "SELECT * FROM daily_metrics WHERE username = :username AND dateEpoch = :dateEpoch LIMIT 1"
    )
    fun observeMetricsForDay(username: String, dateEpoch: Long): Flow<DailyMetricsEntity?>

    /**
     * Obtiene una instantánea rápida del registro del día sin mantener un Flow activo, ideal para servicios.
     */
    @Query(
        "SELECT * FROM daily_metrics WHERE username = :username AND dateEpoch = :dateEpoch LIMIT 1"
    )
    suspend fun getMetricsForDay(username: String, dateEpoch: Long): DailyMetricsEntity?

    /**
     * Observa el historial completo de un usuario ordenado por fecha descendente para lecturas rápidas.
     */
    @Query(
        "SELECT * FROM daily_metrics WHERE username = :username ORDER BY dateEpoch DESC"
    )
    fun observeUserHistory(username: String): Flow<List<DailyMetricsEntity>>

    /**
     * Elimina todos los registros de un usuario específico para liberar memoria local rápidamente.
     */
    @Query("DELETE FROM daily_metrics WHERE username = :username")
    suspend fun deleteAllForUser(username: String)

    /**
     * Elimina registros antiguos de un usuario antes de una fecha dada para conservar almacenamiento.
     */
    @Query("DELETE FROM daily_metrics WHERE username = :username AND dateEpoch < :threshold")
    suspend fun deleteOlderThan(username: String, threshold: Long)
}
