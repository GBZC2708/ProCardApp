package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.data.repository.FoodRepository
import pe.com.zzynan.procardapp.domain.model.DailyFoodEntry
import pe.com.zzynan.procardapp.domain.model.DailyNutritionSummary
import pe.com.zzynan.procardapp.domain.model.FoodItem
import pe.com.zzynan.procardapp.domain.model.WeeklyCaloriesPoint
import pe.com.zzynan.procardapp.ui.mappers.toUiModel
import pe.com.zzynan.procardapp.ui.model.FoodTab
import pe.com.zzynan.procardapp.ui.model.FoodUiState


class FoodViewModel(
    private val foodRepository: FoodRepository,
    private val appContext: Context
) : ViewModel() {

    private val activeUserName = MutableStateFlow("")
    private val activeDate = MutableStateFlow(LocalDate.now())
    private val selectedTab = MutableStateFlow(FoodTab.TODAY_PLAN)

    private val isTodaySaved = MutableStateFlow(false)

    private val prefs = appContext.getSharedPreferences("food_prefs", Context.MODE_PRIVATE)

    private fun savedKey(userName: String, date: LocalDate): String =
        "food_saved_${userName}_${date}"  // date.toString() → yyyy-MM-dd

    private fun loadSavedState(userName: String, date: LocalDate): Boolean {
        if (userName.isBlank()) return false
        return prefs.getBoolean(savedKey(userName, date), false)
    }

    private fun persistSavedState(userName: String, date: LocalDate, isSaved: Boolean) {
        if (userName.isBlank()) return
        prefs.edit().putBoolean(savedKey(userName, date), isSaved).apply()
    }

    private fun refreshSavedState() {
        val user = activeUserName.value
        val date = activeDate.value
        isTodaySaved.value = loadSavedState(user, date)
    }

    private fun markUnsaved() {
        isTodaySaved.value = false
        val user = activeUserName.value
        isTodaySaved.value = false
        persistSavedState(user, activeDate.value, false)
    }
    private val catalogFlow = activeUserName.flatMapLatest { user ->
        if (user.isBlank()) flowOf(emptyList()) else foodRepository.observeCatalog(user)
    }

    private val entriesFlow = combine(activeUserName, activeDate) { user, date ->
        user to date
    }.flatMapLatest { (user, date) ->
        if (user.isBlank()) flowOf(emptyList()) else foodRepository.observeDailyEntries(user, date)
    }

    private val summaryFlow = entriesFlow.map { entries ->
        if (entries.isEmpty()) null else entries.toSummary()
    }

    private val weeklyCaloriesFlow: StateFlow<List<WeeklyCaloriesPoint>> =
        combine(activeUserName, activeDate) { user, date -> user to date }
            .flatMapLatest { (user, date) ->
                if (user.isBlank()) flowOf(emptyList()) else flow {
                    emit(foodRepository.getWeeklyCalories(user, date))
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val isCopyVisibleFlow: StateFlow<Boolean> = combine(
        activeUserName,
        activeDate,
        entriesFlow
    ) { user, date, entries ->
        Triple(user, date, entries.isEmpty())
    }.flatMapLatest { (user, date, isEmpty) ->
        if (user.isBlank() || !isEmpty) {
            flowOf(false)
        } else {
            flow { emit(foodRepository.hasEntries(user, date.minusDays(1))) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val uiState: StateFlow<FoodUiState> =
        combine(
            selectedTab,
            catalogFlow,
            entriesFlow,
            summaryFlow,
            weeklyCaloriesFlow,
            isCopyVisibleFlow,
            isTodaySaved
        ) { values ->
            // values es Array<Any?>
            val tab = values[0] as FoodTab
            val catalog = values[1] as List<FoodItem>
            val entries = values[2] as List<DailyFoodEntry>
            val summary = values[3] as DailyNutritionSummary?
            val weeklyCalories = values[4] as List<WeeklyCaloriesPoint>
            val copyVisible = values[5] as Boolean
            val isSaved = values[6] as Boolean

            FoodUiState(
                currentTab = tab,
                catalog = catalog.map(FoodItem::toUiModel),
                todayEntries = entries.map(DailyFoodEntry::toUiModel),
                todaySummary = summary?.toUiModel(),
                weeklyCalories = weeklyCalories.map(WeeklyCaloriesPoint::toUiModel),
                isCopyFromYesterdayVisible = copyVisible,
                hasEntriesToday = entries.isNotEmpty(),
                isTodaySaved = isSaved
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            FoodUiState()
        )


    fun setActiveUser(userName: String) {
        activeUserName.value = userName
        refreshSavedState()
    }

    fun setActiveDate(date: LocalDate) {
        activeDate.value = date
        refreshSavedState()
    }

    fun onTabSelected(tab: FoodTab) {
        selectedTab.value = tab
    }

    fun onAddFoodClicked() {
        viewModelScope.launch {
            val user = activeUserName.value
            if (user.isNotBlank()) {
                foodRepository.createBlankFood(user)
            }
        }
    }

    fun onEditName(id: Long, newName: String) {
        viewModelScope.launch { foodRepository.updateFoodName(id, newName) }
    }

    fun onEditBaseAmount(id: Long, newAmount: Double) {
        viewModelScope.launch { foodRepository.updateFoodBaseAmount(id, newAmount) }
    }

    fun onEditBaseUnit(id: Long, newUnit: String) {
        viewModelScope.launch { foodRepository.updateFoodBaseUnit(id, newUnit) }
    }

    fun onEditProtein(id: Long, newProtein: Double) {
        viewModelScope.launch { foodRepository.updateFoodProtein(id, newProtein) }
    }

    fun onEditFat(id: Long, newFat: Double) {
        viewModelScope.launch { foodRepository.updateFoodFat(id, newFat) }
    }

    fun onEditCarb(id: Long, newCarb: Double) {
        viewModelScope.launch { foodRepository.updateFoodCarb(id, newCarb) }
    }

    fun onAddEntryForFood(foodId: Long) {
        viewModelScope.launch {
            val user = activeUserName.value
            if (user.isNotBlank()) {
                foodRepository.addEntryForFood(user, activeDate.value, foodId)
                markUnsaved()
            }
        }
    }

    fun onConsumedAmountEdited(entryId: Long, newAmount: Double) {
        viewModelScope.launch { foodRepository.updateConsumedAmount(entryId, newAmount) }
        markUnsaved()
    }

    fun onCopyFromYesterdayClicked() {
        viewModelScope.launch {
            val user = activeUserName.value
            if (user.isNotBlank()) {
                foodRepository.copyFromYesterday(user, activeDate.value)
                markUnsaved()
            }
        }
    }

    fun onRemoveEntry(entryId: Long) {
        viewModelScope.launch { foodRepository.deleteEntry(entryId) }
        markUnsaved()
    }

    fun onSaveTodayClicked() {
        val user = activeUserName.value
        if (user.isNotBlank()) {
            isTodaySaved.value = true
            persistSavedState(user, activeDate.value, true)
        }
    }

   fun onDeleteFood(id: Long) {
               viewModelScope.launch {
                      foodRepository.deleteFood(id)
                  }
           }



    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appContext = context.applicationContext
                val repository = ServiceLocator.provideFoodRepository(context.applicationContext)
                @Suppress("UNCHECKED_CAST")
                return FoodViewModel(repository, appContext) as T
            }
        }
    }
}

private fun List<DailyFoodEntry>.toSummary(): DailyNutritionSummary {
    val date = firstOrNull()?.date ?: LocalDate.now()
    return fold(DailyNutritionSummary(date, 0.0, 0.0, 0.0, 0.0)) { acc, entry ->
        acc.copy(
            totalProtein = acc.totalProtein + entry.protein,
            totalFat = acc.totalFat + entry.fat,
            totalCarb = acc.totalCarb + entry.carb,
            totalCalories = acc.totalCalories + entry.calories
        )
    }
}
