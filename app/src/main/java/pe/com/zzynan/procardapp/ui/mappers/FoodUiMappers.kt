package pe.com.zzynan.procardapp.ui.mappers

import java.util.Locale
import pe.com.zzynan.procardapp.domain.model.DailyFoodEntry
import pe.com.zzynan.procardapp.domain.model.DailyNutritionSummary
import pe.com.zzynan.procardapp.domain.model.FoodItem
import pe.com.zzynan.procardapp.domain.model.WeeklyCaloriesPoint
import pe.com.zzynan.procardapp.ui.model.DailyFoodEntryUiModel
import pe.com.zzynan.procardapp.ui.model.DailyNutritionSummaryUiModel
import pe.com.zzynan.procardapp.ui.model.FoodItemUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyCaloriesPointUiModel

fun FoodItem.toUiModel(): FoodItemUiModel = FoodItemUiModel(
    id = id,
    name = name,
    baseAmount = formatNumber(baseAmount),
    baseUnit = baseUnit,
    protein = "${formatNumber(proteinBase)} g",
    fat = "${formatNumber(fatBase)} g",
    carb = "${formatNumber(carbBase)} g",
    calories = "${formatNumber(caloriesBase)} kcal"
)

fun DailyFoodEntry.toUiModel(): DailyFoodEntryUiModel = DailyFoodEntryUiModel(
    id = id,
    foodName = foodItem.name,
    consumedAmount = formatNumber(consumedAmount),
    unit = foodItem.baseUnit,
    protein = "${formatNumber(protein)} g",
    fat = "${formatNumber(fat)} g",
    carb = "${formatNumber(carb)} g",
    calories = "${formatNumber(calories)} kcal"
)

fun DailyNutritionSummary.toUiModel(): DailyNutritionSummaryUiModel = DailyNutritionSummaryUiModel(
    protein = "P: ${formatNumber(totalProtein)} g",
    fat = "G: ${formatNumber(totalFat)} g",
    carb = "C: ${formatNumber(totalCarb)} g",
    calories = "🔥 ${formatNumber(totalCalories)} kcal"
)

fun WeeklyCaloriesPoint.toUiModel(): WeeklyCaloriesPointUiModel = WeeklyCaloriesPointUiModel(
    dayLabel = dayLabel,
    date = date,
    calories = calories.toFloat()
)

private fun formatNumber(value: Double): String {
    val rounded = String.format(Locale.US, "%.1f", value)
    return if (rounded.endsWith(".0")) rounded.dropLast(2) else rounded
}
