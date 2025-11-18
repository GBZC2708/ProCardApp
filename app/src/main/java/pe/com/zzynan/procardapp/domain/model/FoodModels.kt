package pe.com.zzynan.procardapp.domain.model

import java.time.LocalDate

data class FoodItem(
    val id: Long,
    val userName: String,
    val name: String,
    val baseAmount: Double,
    val baseUnit: String,
    val proteinBase: Double,
    val fatBase: Double,
    val carbBase: Double
) {
    val caloriesBase: Double
        get() = proteinBase * 4.0 + carbBase * 4.0 + fatBase * 9.0
}

data class DailyFoodEntry(
    val id: Long,
    val userName: String,
    val date: LocalDate,
    val foodItem: FoodItem,
    val consumedAmount: Double
) {
    private val factor: Double
        get() = if (foodItem.baseAmount == 0.0) 0.0 else consumedAmount / foodItem.baseAmount

    val protein: Double
        get() = foodItem.proteinBase * factor

    val fat: Double
        get() = foodItem.fatBase * factor

    val carb: Double
        get() = foodItem.carbBase * factor

    val calories: Double
        get() = protein * 4.0 + carb * 4.0 + fat * 9.0
}

data class DailyNutritionSummary(
    val date: LocalDate,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarb: Double,
    val totalCalories: Double
)

data class WeeklyCaloriesPoint(
    val dayLabel: String,
    val date: LocalDate,
    val calories: Double
)
