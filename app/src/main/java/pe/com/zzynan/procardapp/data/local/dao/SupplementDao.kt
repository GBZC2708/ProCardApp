package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.SupplementItemEntity

@Dao
interface SupplementDao {

    @Query("SELECT * FROM supplement_items WHERE userName = :userName AND isActive = 1 ORDER BY name")
    fun getSupplementsForUser(userName: String): Flow<List<SupplementItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplement(item: SupplementItemEntity): Long

    @Update
    suspend fun updateSupplement(item: SupplementItemEntity)

    @Query("UPDATE supplement_items SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteSupplement(id: Long)
}
