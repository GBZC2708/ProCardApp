package pe.com.zzynan.procardapp.ui.model

import java.time.LocalDate
import pe.com.zzynan.procardapp.domain.model.SupplementTimeSlot

enum class SupplementTab { CATALOG, DAILY_PLAN }

data class SupplementUiItem(
    val id: Long,
    val name: String,
    val baseAmount: String?,
    val baseUnit: String?,
)

data class DailySupplementItemUi(
    val id: Long,
    val supplementId: Long,
    val name: String,
    val timeSlot: SupplementTimeSlot,
    val amount: Double?,
    val unit: String?,
    val amountLabel: String
)

data class DailySupplementGroupUi(
    val timeSlot: SupplementTimeSlot,
    val items: List<DailySupplementItemUi>
)

data class SupplementUiState(
    val selectedTab: SupplementTab = SupplementTab.CATALOG,
    val selectedDate: LocalDate = LocalDate.now(),
    val supplements: List<SupplementUiItem> = emptyList(),
    val dailyPlan: List<DailySupplementGroupUi> = emptyList(),
    val isLoading: Boolean = false,
    val isUsingInheritedPlan: Boolean = false,
    val errorMessage: String? = null
)
