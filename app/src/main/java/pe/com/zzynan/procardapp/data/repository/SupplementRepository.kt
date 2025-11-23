package pe.com.zzynan.procardapp.data.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.local.dao.DailySupplementDao
import pe.com.zzynan.procardapp.data.local.dao.SupplementDao
import pe.com.zzynan.procardapp.data.local.entity.DailySupplementEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.SupplementItemEntity
import pe.com.zzynan.procardapp.domain.model.SupplementTimeSlot
import pe.com.zzynan.procardapp.domain.model.toSupplementTimeSlot

data class DailySupplementEntry(
    val id: Long,
    val userName: String,
    val date: LocalDate,
    val supplementId: Long,
    val timeSlot: SupplementTimeSlot,
    val amount: Double?,
    val unit: String?,
    val orderInSlot: Int
)

data class DailySupplementPlan(
    val entries: List<DailySupplementEntry>,
    val isInherited: Boolean
)

class SupplementRepository(
    private val supplementDao: SupplementDao,
    private val dailySupplementDao: DailySupplementDao
) {

    fun getSupplementsForUser(userName: String): Flow<List<SupplementItemEntity>> =
        supplementDao.getSupplementsForUser(userName)

    suspend fun upsertSupplement(item: SupplementItemEntity): Long =
        supplementDao.upsertSupplement(item)

    suspend fun softDeleteSupplement(id: Long) {
        supplementDao.softDeleteSupplement(id)
    }

    fun getDailyPlan(userName: String, date: LocalDate): Flow<DailySupplementPlan> {
        val epoch = date.toEpochDay()
        return dailySupplementDao.getDailyEntries(userName, epoch).map { entries ->
            if (entries.isNotEmpty()) {
                DailySupplementPlan(entries.map { it.toDomain(date) }, false)
            } else {
                val lastDate = dailySupplementDao.findLastDateWithPlanBefore(userName, epoch)
                if (lastDate != null) {
                    val inherited = dailySupplementDao.getDailyEntries(userName, lastDate).first()
                    DailySupplementPlan(
                        entries = inherited.map { it.toDomain(LocalDate.ofEpochDay(epoch)) },
                        isInherited = true
                    )
                } else {
                    DailySupplementPlan(emptyList(), false)
                }
            }
        }
    }

    suspend fun ensurePlanForDateOnFirstChange(
        userName: String,
        date: LocalDate,
        currentEntries: List<DailySupplementEntry>
    ) {
        val epoch = date.toEpochDay()
        val existing = dailySupplementDao.getDailyEntries(userName, epoch).first()
        if (existing.isNotEmpty()) return

        val entriesToCopy = if (currentEntries.isNotEmpty()) {
            currentEntries
        } else {
            val lastDate = dailySupplementDao.findLastDateWithPlanBefore(userName, epoch)
            if (lastDate != null) {
                dailySupplementDao.getDailyEntries(userName, lastDate).first()
                    .map { it.toDomain(LocalDate.ofEpochDay(epoch)) }
            } else {
                emptyList()
            }
        }

        entriesToCopy.forEach { entry ->
            dailySupplementDao.upsertEntry(entry.toEntity().copy(id = 0L, dateEpochDay = epoch))
        }
    }

    suspend fun addOrUpdateDailyEntry(
        userName: String,
        date: LocalDate,
        supplementId: Long,
        timeSlot: SupplementTimeSlot,
        amount: Double?,
        unit: String?
    ) {
        val epoch = date.toEpochDay()
        ensurePlanForDateOnFirstChange(userName, date, emptyList())
        val existing = dailySupplementDao.getDailyEntries(userName, epoch).first()
        val match = existing.firstOrNull { it.supplementId == supplementId && it.timeSlot == timeSlot.name }
        val updated = DailySupplementEntryEntity(
            id = match?.id ?: 0L,
            userName = userName,
            dateEpochDay = epoch,
            supplementId = supplementId,
            timeSlot = timeSlot.name,
            amount = amount,
            unit = unit,
            orderInSlot = match?.orderInSlot ?: 0
        )
        dailySupplementDao.upsertEntry(updated)
    }

    suspend fun updateDailyEntryAmount(
        userName: String,
        date: LocalDate,
        entryId: Long,
        newAmount: Double?
    ) {
        val epoch = date.toEpochDay()
        ensurePlanForDateOnFirstChange(userName, date, emptyList())
        val current = dailySupplementDao.getDailyEntries(userName, epoch).first()
            .firstOrNull { it.id == entryId } ?: return
        dailySupplementDao.upsertEntry(current.copy(amount = newAmount))
    }

    suspend fun deleteDailyEntry(
        userName: String,
        date: LocalDate,
        entryId: Long
    ) {
        ensurePlanForDateOnFirstChange(userName, date, emptyList())
        dailySupplementDao.deleteEntryById(userName, date.toEpochDay(), entryId)
    }

    private fun DailySupplementEntryEntity.toDomain(targetDate: LocalDate): DailySupplementEntry =
        DailySupplementEntry(
            id = id,
            userName = userName,
            date = targetDate,
            supplementId = supplementId,
            timeSlot = timeSlot.toSupplementTimeSlot(),
            amount = amount,
            unit = unit,
            orderInSlot = orderInSlot
        )

    private fun DailySupplementEntry.toEntity(): DailySupplementEntryEntity =
        DailySupplementEntryEntity(
            id = id,
            userName = userName,
            dateEpochDay = date.toEpochDay(),
            supplementId = supplementId,
            timeSlot = timeSlot.name,
            amount = amount,
            unit = unit,
            orderInSlot = orderInSlot
        )
}
