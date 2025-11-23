package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.DailySupplementEntryEntity

@Dao
interface DailySupplementDao {

    @Query(
        """
        SELECT * FROM daily_supplement_entries
        WHERE userName = :userName AND dateEpochDay = :dateEpochDay
        ORDER BY timeSlot, orderInSlot, id
        """
    )
    fun getDailyEntries(
        userName: String,
        dateEpochDay: Long
    ): Flow<List<DailySupplementEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: DailySupplementEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: DailySupplementEntryEntity)

    @Query(
        """
        DELETE FROM daily_supplement_entries
        WHERE userName = :userName AND dateEpochDay = :dateEpochDay AND id = :entryId
        """
    )
    suspend fun deleteEntryById(
        userName: String,
        dateEpochDay: Long,
        entryId: Long
    )

    @Query(
        """
        SELECT DISTINCT dateEpochDay FROM daily_supplement_entries
        WHERE userName = :userName AND dateEpochDay < :dateEpochDay
        ORDER BY dateEpochDay DESC
        LIMIT 1
        """
    )
    suspend fun findLastDateWithPlanBefore(
        userName: String,
        dateEpochDay: Long
    ): Long?
}
