package pe.com.zzynan.procardapp.ui.state

import java.time.LocalDate
import pe.com.zzynan.procardapp.core.extensions.toEpochDayLong
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.ui.model.DailyMetricsUiModel
import pe.com.zzynan.procardapp.ui.model.StepCounterUiModel
import pe.com.zzynan.procardapp.ui.model.WeightCardUiModel
import pe.com.zzynan.procardapp.ui.model.WeightEditorUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyMetricsUiModel

/**
 * Estado que agrupa los datos necesarios para la pantalla de registro diario.
 * Se usa una única instancia inmutable para minimizar recomposiciones.
 */
data class DailyRegisterUiState(
    val userName: String = UserProfile.DEFAULT_DISPLAY_NAME,
    val dateEpoch: Long = LocalDate.now().toEpochDayLong(),
    val stepCounter: StepCounterUiModel = StepCounterUiModel(),
    val metrics: DailyMetricsUiModel? = null,
    val weightCard: WeightCardUiModel = WeightCardUiModel(),
    val weightEditor: WeightEditorUiModel = WeightEditorUiModel(),
    val weeklyMetrics: WeeklyMetricsUiModel = WeeklyMetricsUiModel(),
    val isLoading: Boolean = true
)
