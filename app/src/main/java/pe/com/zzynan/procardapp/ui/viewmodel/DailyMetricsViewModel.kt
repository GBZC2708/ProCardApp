package pe.com.zzynan.procardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.data.local.entity.TrainingStage
import pe.com.zzynan.procardapp.data.local.entity.toDatabaseValue
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository

/**
 * ViewModel de ejemplo que demuestra cómo consumir el repositorio en una capa de presentación.
 * Se usa MutableStateFlow para mantener el usuario y la fecha actuales, permitiendo observabilidad reactiva.
 */
class DailyMetricsViewModel(
    private val dailyMetricsRepository: DailyMetricsRepository
) : ViewModel() {

    /**
     * Usuario activo cuyas métricas se desean observar o actualizar.
     */
    private val activeUsername: MutableStateFlow<String> = MutableStateFlow("usuario_demo")

    /**
     * Fecha seleccionada en formato epochDay para alinear con la entidad de la base de datos.
     */
    private val activeDateEpoch: MutableStateFlow<Long> = MutableStateFlow(LocalDate.now().toEpochDayLong())

    /**
     * Exposición reactiva de las métricas del día actual. Al cambiar usuario o fecha, el flujo se actualiza.
     */
    val todayMetrics: StateFlow<DailyMetricsEntity?> =
        combine(activeUsername, activeDateEpoch) { username, dateEpoch ->
            username to dateEpoch
        }
            .flatMapLatest { (username, dateEpoch) ->
                dailyMetricsRepository.observeDailyMetrics(username, dateEpoch)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    /**
     * Exposición reactiva del historial completo del usuario ordenado por fecha descendente.
     */
    val history: StateFlow<List<DailyMetricsEntity>> =
        activeUsername
            .flatMapLatest { username ->
                dailyMetricsRepository.observeUserHistory(username)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        /**
         * Ejemplo de inserción inicial usando datos simulados para el usuario por defecto.
         * En una app real, este bloque podría dispararse desde la UI al capturar los datos del día.
         */
        viewModelScope.launch {
            val metricsExample = DailyMetricsEntity(
                username = activeUsername.value,
                dateEpoch = activeDateEpoch.value,
                weightFasted = 86.22f,
                dailySteps = 10_000,
                cardioMinutes = 35,
                trainingDone = true,
                waterMl = 4_000,
                saltGramsX10 = 35,
                sleepMinutes = 420,
                stage = TrainingStage.DEFINICION.toDatabaseValue()
            )
            dailyMetricsRepository.upsertDailyMetrics(metricsExample)
        }
    }

    /**
     * Permite a la UI cambiar el usuario activo; los flujos se actualizan automáticamente.
     */
    fun setActiveUsername(username: String) {
        activeUsername.value = username
    }

    /**
     * Permite actualizar la fecha observada. Se espera un LocalDate para mantener la API expresiva.
     */
    fun setActiveDate(date: LocalDate) {
        activeDateEpoch.value = date.toEpochDayLong()
    }

    /**
     * Guarda o actualiza las métricas del día activo utilizando parámetros detallados.
     * La capa de UI puede invocar este método tras recolectar los datos del usuario.
     */
    fun upsertMetricsForActiveDay(
        weightFasted: Float,
        dailySteps: Int,
        cardioMinutes: Int,
        trainingDone: Boolean,
        waterMl: Int,
        saltGramsX10: Int,
        sleepMinutes: Int,
        stage: TrainingStage
    ) {
        viewModelScope.launch {
            val metrics = DailyMetricsEntity(
                username = activeUsername.value,
                dateEpoch = activeDateEpoch.value,
                weightFasted = weightFasted,
                dailySteps = dailySteps,
                cardioMinutes = cardioMinutes,
                trainingDone = trainingDone,
                waterMl = waterMl,
                saltGramsX10 = saltGramsX10,
                sleepMinutes = sleepMinutes,
                stage = stage.toDatabaseValue()
            )
            dailyMetricsRepository.upsertDailyMetrics(metrics)
        }
    }

    /**
     * Permite limpiar el historial del usuario activo, útil al cerrar sesión o reducir cache.
     */
    fun clearActiveUserHistory() {
        viewModelScope.launch {
            dailyMetricsRepository.clearUserHistory(activeUsername.value)
        }
    }
}
