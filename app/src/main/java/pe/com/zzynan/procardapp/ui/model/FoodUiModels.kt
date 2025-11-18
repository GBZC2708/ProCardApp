package pe.com.zzynan.procardapp.ui.model

data class FoodItemUiModel(
    val id: Long,
    val name: String,
    val baseAmount: String,
    val baseUnit: String,
    val protein: String,
    val fat: String,
    val carb: String,
    val calories: String
)

data class DailyFoodEntryUiModel(
    val id: Long,
    val foodName: String,
    val consumedAmount: String,
    val unit: String,
    val protein: String,
    val fat: String,
    val carb: String,
    val calories: String
)

data class DailyNutritionSummaryUiModel(
    val protein: String,
    val fat: String,
    val carb: String,
    val calories: String
)

data class WeeklyCaloriesPointUiModel(
    val dayLabel: String,
    val date: java.time.LocalDate,
    val calories: Float
)

enum class FoodTab { CATALOG, TODAY_PLAN }

data class FoodUiState(
    val currentTab: FoodTab = FoodTab.TODAY_PLAN,
    val catalog: List<FoodItemUiModel> = emptyList(),
    val todayEntries: List<DailyFoodEntryUiModel> = emptyList(),
    val todaySummary: DailyNutritionSummaryUiModel? = null,
    val weeklyCalories: List<WeeklyCaloriesPointUiModel> = emptyList(),
    val isCopyFromYesterdayVisible: Boolean = false
)
