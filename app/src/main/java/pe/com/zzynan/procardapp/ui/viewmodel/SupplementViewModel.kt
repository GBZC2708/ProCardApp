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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.data.local.entity.SupplementItemEntity
import pe.com.zzynan.procardapp.data.repository.DailySupplementEntry
import pe.com.zzynan.procardapp.data.repository.SupplementRepository
import pe.com.zzynan.procardapp.domain.model.SupplementTimeSlot
import pe.com.zzynan.procardapp.ui.model.DailySupplementGroupUi
import pe.com.zzynan.procardapp.ui.model.DailySupplementItemUi
import pe.com.zzynan.procardapp.ui.model.SupplementTab
import pe.com.zzynan.procardapp.ui.model.SupplementUiItem
import pe.com.zzynan.procardapp.ui.model.SupplementUiState

class SupplementViewModel(
    private val repository: SupplementRepository
) : ViewModel() {

    private val activeUser = MutableStateFlow("")
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedTab = MutableStateFlow(SupplementTab.CATALOG)

    private val supplementsFlow = activeUser.flatMapLatest { user ->
        if (user.isBlank()) flowOf(emptyList()) else repository.getSupplementsForUser(user)
    }

    private val dailyPlanFlow = combine(activeUser, selectedDate) { user, date -> user to date }
        .flatMapLatest { (user, date) ->
            if (user.isBlank()) flowOf(null) else repository.getDailyPlan(user, date)
        }

    val uiState: StateFlow<SupplementUiState> = combine(
        selectedTab,
        selectedDate,
        supplementsFlow,
        dailyPlanFlow
    ) { tab, date, supplements, plan ->
        val supplementUi = supplements.map { it.toUiModel() }
        val entries = plan?.entries.orEmpty()
        val grouped = entries
            .mapNotNull { entry ->
                val name = supplementUi.firstOrNull { it.id == entry.supplementId }?.name ?: return@mapNotNull null
                entry.toUiModel(name)
            }
            .groupBy { it.timeSlot }
            .toList()
            .sortedBy { it.first.ordinal }
            .map { (slot, items) ->
                DailySupplementGroupUi(
                    timeSlot = slot,
                    items = items.sortedBy { it.id }
                )
            }

        SupplementUiState(
            selectedTab = tab,
            selectedDate = date,
            supplements = supplementUi,
            dailyPlan = grouped,
            isUsingInheritedPlan = plan?.isInherited == true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SupplementUiState())

    fun setActiveUser(userName: String) {
        activeUser.value = userName
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun onTabSelected(tab: SupplementTab) {
        selectedTab.value = tab
    }

    fun onAddSupplement(name: String, baseAmount: Double?, baseUnit: String?) {
        val user = activeUser.value
        if (user.isBlank()) return
        viewModelScope.launch {
            repository.upsertSupplement(
                SupplementItemEntity(
                    id = 0L,
                    userName = user,
                    name = name,
                    baseAmount = baseAmount,
                    baseUnit = baseUnit,
                    isActive = true
                )
            )
        }
    }

    fun onEditSupplement(id: Long, name: String, baseAmount: Double?, baseUnit: String?) {
        val user = activeUser.value
        if (user.isBlank()) return
        viewModelScope.launch {
            repository.upsertSupplement(
                SupplementItemEntity(
                    id = id,
                    userName = user,
                    name = name,
                    baseAmount = baseAmount,
                    baseUnit = baseUnit,
                    isActive = true
                )
            )
        }
    }

    fun onDeleteSupplement(id: Long) {
        viewModelScope.launch { repository.softDeleteSupplement(id) }
    }

    fun onAddDailyEntry(supplementId: Long, timeSlot: SupplementTimeSlot, amount: Double?, unit: String?) {
        val user = activeUser.value
        val date = selectedDate.value
        if (user.isBlank()) return
        viewModelScope.launch {
            repository.addOrUpdateDailyEntry(user, date, supplementId, timeSlot, amount, unit)
        }
    }

    fun onChangeDailyAmount(entryId: Long, newAmount: Double?) {
        val user = activeUser.value
        val date = selectedDate.value
        if (user.isBlank()) return
        viewModelScope.launch {
            repository.updateDailyEntryAmount(user, date, entryId, newAmount)
        }
    }

    fun onDeleteDailyEntry(entryId: Long) {
        val user = activeUser.value
        val date = selectedDate.value
        if (user.isBlank()) return
        viewModelScope.launch {
            repository.deleteDailyEntry(user, date, entryId)
        }
    }

    fun onPreviousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun onNextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val repository = ServiceLocator.provideSupplementRepository(context.applicationContext)
                @Suppress("UNCHECKED_CAST")
                return SupplementViewModel(repository) as T
            }
        }
    }
}

private fun SupplementItemEntity.toUiModel(): SupplementUiItem =
    SupplementUiItem(
        id = id,
        name = name,
        baseAmount = baseAmount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() },
        baseUnit = baseUnit
    )

private fun DailySupplementEntry.toUiModel(name: String): DailySupplementItemUi =
    DailySupplementItemUi(
        id = id,
        supplementId = supplementId,
        name = name,
        timeSlot = timeSlot,
        amount = amount,
        unit = unit,
        amountLabel = listOfNotNull(
            amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() },
            unit
        ).joinToString(" ")
    )
