package pe.com.zzynan.procardapp.data.mappers

import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.data.local.entity.toDatabaseValue
import pe.com.zzynan.procardapp.data.local.entity.toTrainingStage
import pe.com.zzynan.procardapp.domain.model.DailyMetrics

/**
 * Mapeos entre entidad y dominio optimizados para evitar copias innecesarias.
 */
fun DailyMetricsEntity.toDomain(): DailyMetrics = DailyMetrics(
    username = username,
    dateEpoch = dateEpoch,
    weightFasted = weightFasted,
    dailySteps = dailySteps,
    cardioMinutes = cardioMinutes,
    trainingDone = trainingDone,
    waterMl = waterMl,
    saltGramsX10 = saltGramsX10,
    sleepMinutes = sleepMinutes,
    stage = stage.toTrainingStage()
)

fun DailyMetrics.toEntity(): DailyMetricsEntity = DailyMetricsEntity(
    username = username,
    dateEpoch = dateEpoch,
    weightFasted = weightFasted,
    dailySteps = dailySteps,
    cardioMinutes = cardioMinutes,
    trainingDone = trainingDone,
    waterMl = waterMl,
    saltGramsX10 = saltGramsX10,
    sleepMinutes = sleepMinutes,
    stage = stage.toDatabaseValue()
)
