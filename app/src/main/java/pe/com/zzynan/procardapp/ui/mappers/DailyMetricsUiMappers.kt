package pe.com.zzynan.procardapp.ui.mappers

import pe.com.zzynan.procardapp.domain.model.DailyMetrics
import pe.com.zzynan.procardapp.ui.model.DailyMetricsUiModel

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
