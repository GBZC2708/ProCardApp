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
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val activeUserName = MutableStateFlow("")
    private val activeDate = MutableStateFlow(LocalDate.now())
    private val selectedTab = MutableStateFlow(FoodTab.TODAY_PLAN)

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
            isCopyVisibleFlow
        ) { values ->
            // values es Array<Any?>
            val tab = values[0] as FoodTab
            val catalog = values[1] as List<FoodItem>
            val entries = values[2] as List<DailyFoodEntry>
            val summary = values[3] as DailyNutritionSummary?
            val weeklyCalories = values[4] as List<WeeklyCaloriesPoint>
            val copyVisible = values[5] as Boolean

            FoodUiState(
                currentTab = tab,
                catalog = catalog.map(FoodItem::toUiModel),
                todayEntries = entries.map(DailyFoodEntry::toUiModel),
                todaySummary = summary?.toUiModel(),
                weeklyCalories = weeklyCalories.map(WeeklyCaloriesPoint::toUiModel),
                isCopyFromYesterdayVisible = copyVisible
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            FoodUiState()
        )


    fun setActiveUser(userName: String) {
        activeUserName.value = userName
    }

    fun setActiveDate(date: LocalDate) {
        activeDate.value = date
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
            }
        }
    }

    fun onConsumedAmountEdited(entryId: Long, newAmount: Double) {
        viewModelScope.launch { foodRepository.updateConsumedAmount(entryId, newAmount) }
    }

    fun onCopyFromYesterdayClicked() {
        viewModelScope.launch {
            val user = activeUserName.value
            if (user.isNotBlank()) {
                foodRepository.copyFromYesterday(user, activeDate.value)
            }
        }
    }

    fun onRemoveEntry(entryId: Long) {
        viewModelScope.launch { foodRepository.deleteEntry(entryId) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val repository = ServiceLocator.provideFoodRepository(context.applicationContext)
                @Suppress("UNCHECKED_CAST")
                return FoodViewModel(repository) as T
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
