package pe.com.zzynan.procardapp.ui.mappers

import java.time.LocalDate
import pe.com.zzynan.procardapp.domain.model.DailyMetrics
import pe.com.zzynan.procardapp.ui.model.DailyMetricsUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyMetricsUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyStepsPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyWeightPoint

/**
 * Mapas de dominio a UI para evitar cálculos costosos durante recomposiciones.
 */
fun DailyMetrics.toUiModel(): DailyMetricsUiModel = DailyMetricsUiModel(
    username = username,
    dateEpoch = dateEpoch,
    weightFasted = weightFasted,
    dailySteps = dailySteps,
    cardioMinutes = cardioMinutes,
    trainingDone = trainingDone,
    waterMl = waterMl,
    saltGramsX10 = saltGramsX10,
    sleepMinutes = sleepMinutes,
    stage = stage
)

fun List<DailyMetrics>.toWeeklyMetricsUiModel(
    endDate: Long
): WeeklyMetricsUiModel {
    val endLocalDate = LocalDate.ofEpochDay(endDate)
    val startDate = endLocalDate.minusDays(6)
    val mapped = associateBy { it.dateEpoch }

    val weightPoints = (0 until 7).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        val metric = mapped[date.toEpochDay()]
        WeeklyWeightPoint(
            date = date,
            weight = metric?.weightFasted
        )
    }

    val stepsPoints = (0 until 7).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        val metric = mapped[date.toEpochDay()]
        WeeklyStepsPoint(
            date = date,
            steps = metric?.dailySteps ?: 0
        )
    }

    return WeeklyMetricsUiModel(
        weightPoints = weightPoints,
        stepsPoints = stepsPoints
    )
}
