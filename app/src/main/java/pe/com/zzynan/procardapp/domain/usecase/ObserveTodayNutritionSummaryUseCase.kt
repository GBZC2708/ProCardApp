package pe.com.zzynan.procardapp.domain.usecase

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.repository.FoodRepository
import pe.com.zzynan.procardapp.domain.model.DailyNutritionSummary

class ObserveTodayNutritionSummaryUseCase(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(username: String, date: LocalDate): Flow<DailyNutritionSummary?> {
        return foodRepository.observeDailyEntries(username, date)
            .map { entries ->
                if (entries.isEmpty()) null else {
                    val dateValue = entries.firstOrNull()?.date ?: date
                    entries.fold(DailyNutritionSummary(dateValue, 0.0, 0.0, 0.0, 0.0)) { acc, entry ->
                        acc.copy(
                            totalProtein = acc.totalProtein + entry.protein,
                            totalFat = acc.totalFat + entry.fat,
                            totalCarb = acc.totalCarb + entry.carb,
                            totalCalories = acc.totalCalories + entry.calories
                        )
                    }
                }
            }
    }
}
