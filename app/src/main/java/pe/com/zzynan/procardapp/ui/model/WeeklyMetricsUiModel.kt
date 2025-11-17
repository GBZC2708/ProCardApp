package pe.com.zzynan.procardapp.ui.model

import java.time.LocalDate

/**
 * Representa los puntos necesarios para los gráficos de los últimos 7 días.
 */
data class WeeklyMetricsUiModel(
    val weightPoints: List<WeeklyWeightPoint> = emptyList(),
    val stepsPoints: List<WeeklyStepsPoint> = emptyList()
)

data class WeeklyWeightPoint(
    val date: LocalDate,
    val weight: Float?
)

data class WeeklyStepsPoint(
    val date: LocalDate,
    val steps: Int
)
