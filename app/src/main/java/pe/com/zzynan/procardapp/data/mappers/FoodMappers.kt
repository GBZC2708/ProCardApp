package pe.com.zzynan.procardapp.data.mappers

import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryWithFood
import pe.com.zzynan.procardapp.data.local.entity.FoodItemEntity
import pe.com.zzynan.procardapp.domain.model.DailyFoodEntry
import pe.com.zzynan.procardapp.domain.model.FoodItem

fun FoodItemEntity.toDomain(): FoodItem = FoodItem(
    id = id,
    userName = userName,
    name = name,
    baseAmount = baseAmount,
    baseUnit = baseUnit,
    proteinBase = proteinBase,
    fatBase = fatBase,
    carbBase = carbBase
)

fun FoodItem.toEntity(): FoodItemEntity = FoodItemEntity(
    id = id,
    userName = userName,
    name = name,
    baseAmount = baseAmount,
    baseUnit = baseUnit,
    proteinBase = proteinBase,
    fatBase = fatBase,
    carbBase = carbBase
)

fun DailyFoodEntryWithFood.toDomain(): DailyFoodEntry = DailyFoodEntry(
    id = entry.id,
    userName = entry.userName,
    date = entry.date,
    foodItem = food.toDomain(),
    consumedAmount = entry.consumedAmount
)

fun DailyFoodEntry.toEntity(): DailyFoodEntryEntity = DailyFoodEntryEntity(
    id = id,
    userName = userName,
    date = date,
    foodItemId = foodItem.id,
    consumedAmount = consumedAmount
)
