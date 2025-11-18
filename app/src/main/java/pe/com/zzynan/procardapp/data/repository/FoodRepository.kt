package pe.com.zzynan.procardapp.data.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.local.dao.FoodDao
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.domain.model.DailyFoodEntry
import pe.com.zzynan.procardapp.domain.model.DailyNutritionSummary
import pe.com.zzynan.procardapp.domain.model.FoodItem
import pe.com.zzynan.procardapp.domain.model.WeeklyCaloriesPoint

interface FoodRepository {
    fun observeCatalog(userName: String): Flow<List<FoodItem>>
    fun observeDailyEntries(userName: String, date: LocalDate): Flow<List<DailyFoodEntry>>
    suspend fun createBlankFood(userName: String): FoodItem
    suspend fun updateFoodName(id: Long, newName: String)
    suspend fun updateFoodBaseAmount(id: Long, newAmount: Double)
    suspend fun updateFoodBaseUnit(id: Long, newUnit: String)
    suspend fun updateFoodProtein(id: Long, newProtein: Double)
    suspend fun updateFoodFat(id: Long, newFat: Double)
    suspend fun updateFoodCarb(id: Long, newCarb: Double)
    suspend fun addEntryForFood(userName: String, date: LocalDate, foodId: Long): DailyFoodEntry
    suspend fun updateConsumedAmount(entryId: Long, newAmount: Double)
    suspend fun deleteEntry(entryId: Long)
    suspend fun getDailySummary(userName: String, date: LocalDate): DailyNutritionSummary?
    suspend fun getWeeklyCalories(userName: String, endDate: LocalDate): List<WeeklyCaloriesPoint>
    suspend fun copyFromYesterday(userName: String, today: LocalDate)
    suspend fun hasEntries(userName: String, date: LocalDate): Boolean
}

class FoodRepositoryImpl(private val foodDao: FoodDao) : FoodRepository {
    override fun observeCatalog(userName: String): Flow<List<FoodItem>> =
        foodDao.observeFoodCatalog(userName).map { items -> items.map { it.toDomain() } }

    override fun observeDailyEntries(userName: String, date: LocalDate): Flow<List<DailyFoodEntry>> =
        foodDao.observeDailyFoodEntries(userName, date).map { entries -> entries.map { it.toDomain() } }

    override suspend fun createBlankFood(userName: String): FoodItem {
        val entity = FoodItem(
            id = 0L,
            userName = userName,
            name = "Nuevo alimento",
            baseAmount = 100.0,
            baseUnit = "g",
            proteinBase = 0.0,
            fatBase = 0.0,
            carbBase = 0.0
        ).toEntity()
        val id = foodDao.insertFoodItem(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun updateFoodName(id: Long, newName: String) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(name = newName))
    }

    override suspend fun updateFoodBaseAmount(id: Long, newAmount: Double) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(baseAmount = newAmount))
    }

    override suspend fun updateFoodBaseUnit(id: Long, newUnit: String) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(baseUnit = newUnit))
    }

    override suspend fun updateFoodProtein(id: Long, newProtein: Double) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(proteinBase = newProtein))
    }

    override suspend fun updateFoodFat(id: Long, newFat: Double) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(fatBase = newFat))
    }

    override suspend fun updateFoodCarb(id: Long, newCarb: Double) {
        val current = foodDao.getFoodItemById(id) ?: return
        foodDao.updateFoodItem(current.copy(carbBase = newCarb))
    }

    override suspend fun addEntryForFood(
        userName: String,
        date: LocalDate,
        foodId: Long
    ): DailyFoodEntry {
        val food = foodDao.getFoodItemById(foodId)?.toDomain()
            ?: throw IllegalArgumentException("Food item not found")

        // 1) Creamos el modelo de dominio
        val domainEntry = DailyFoodEntry(
            id = 0L,
            userName = userName,
            date = date,
            foodItem = food,
            consumedAmount = food.baseAmount
        )

        // 2) Lo convertimos a entidad y lo insertamos
        val entity = domainEntry.toEntity()
        val id = foodDao.insertDailyFoodEntry(entity)

        // 3) Devolvemos el modelo de dominio con el id asignado
        return domainEntry.copy(id = id)
    }


    override suspend fun updateConsumedAmount(entryId: Long, newAmount: Double) {
        val current = foodDao.getDailyFoodEntryById(entryId) ?: return
        foodDao.updateDailyFoodEntry(current.copy(consumedAmount = newAmount))
    }

    override suspend fun deleteEntry(entryId: Long) {
        val current = foodDao.getDailyFoodEntryById(entryId) ?: return
        foodDao.deleteDailyFoodEntry(current)
    }

    override suspend fun getDailySummary(userName: String, date: LocalDate): DailyNutritionSummary? {
        val entries = foodDao.getDailyFoodEntriesOnce(userName, date).map { it.toDomain() }
        if (entries.isEmpty()) return null
        val totals = entries.fold(DailyNutritionSummary(date, 0.0, 0.0, 0.0, 0.0)) { acc, entry ->
            acc.copy(
                totalProtein = acc.totalProtein + entry.protein,
                totalFat = acc.totalFat + entry.fat,
                totalCarb = acc.totalCarb + entry.carb,
                totalCalories = acc.totalCalories + entry.calories
            )
        }
        return totals
    }

    override suspend fun getWeeklyCalories(userName: String, endDate: LocalDate): List<WeeklyCaloriesPoint> {
        val startDate = endDate.minusDays(6)
        val entries = foodDao.getDailyFoodEntriesInRange(userName, startDate, endDate)
            .map { it.toDomain() }
            .groupBy { it.date }

        return (0 until 7).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val calories = entries[date]?.sumOf { it.calories } ?: 0.0
            WeeklyCaloriesPoint(
                dayLabel = dayLabelFor(date),
                date = date,
                calories = calories
            )
        }
    }

    override suspend fun copyFromYesterday(userName: String, today: LocalDate) {
        val yesterday = today.minusDays(1)
        val entries = foodDao.getDailyFoodEntriesOnce(userName, yesterday)
        entries.forEach { entry ->
            val copy = entry.entry.copy(id = 0L, date = today)
            foodDao.insertDailyFoodEntry(copy)
        }
    }

    override suspend fun hasEntries(userName: String, date: LocalDate): Boolean {
        return foodDao.getDailyFoodEntriesOnce(userName, date).isNotEmpty()
    }

    private fun dayLabelFor(date: LocalDate): String = when (date.dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "Lun"
        java.time.DayOfWeek.TUESDAY -> "Mar"
        java.time.DayOfWeek.WEDNESDAY -> "Mie"
        java.time.DayOfWeek.THURSDAY -> "Jue"
        java.time.DayOfWeek.FRIDAY -> "Vie"
        java.time.DayOfWeek.SATURDAY -> "Sab"
        java.time.DayOfWeek.SUNDAY -> "Dom"
    }
}
