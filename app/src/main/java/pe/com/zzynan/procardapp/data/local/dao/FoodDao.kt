package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryWithFood
import pe.com.zzynan.procardapp.data.local.entity.FoodItemEntity

@Dao
interface FoodDao {

    // Catálogo
    @Query("SELECT * FROM food_items WHERE userName = :userName ORDER BY name ASC")
    fun observeFoodCatalog(userName: String): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Update
    suspend fun updateFoodItem(item: FoodItemEntity)

    @Delete
    suspend fun deleteFoodItem(item: FoodItemEntity)

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodItemById(id: Long): FoodItemEntity?

    // Plan de comidas del día
    @Transaction
    @Query(
        """
        SELECT * FROM daily_food_entries
        WHERE userName = :userName AND date = :date
        ORDER BY id ASC
    """
    )
    fun observeDailyFoodEntries(userName: String, date: LocalDate): Flow<List<DailyFoodEntryWithFood>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyFoodEntry(entry: DailyFoodEntryEntity): Long

    @Update
    suspend fun updateDailyFoodEntry(entry: DailyFoodEntryEntity)

    @Delete
    suspend fun deleteDailyFoodEntry(entry: DailyFoodEntryEntity)

    @Query("SELECT * FROM daily_food_entries WHERE id = :id LIMIT 1")
    suspend fun getDailyFoodEntryById(id: Long): DailyFoodEntryEntity?

    // Entradas por rango para gráfico 7 días
    @Transaction
    @Query(
        """
        SELECT * FROM daily_food_entries
        WHERE userName = :userName AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """
    )
    suspend fun getDailyFoodEntriesInRange(
        userName: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyFoodEntryWithFood>

    // Entradas de un día concreto (para copiar desde ayer)
    @Transaction
    @Query(
        """
        SELECT * FROM daily_food_entries
        WHERE userName = :userName AND date = :date
        ORDER BY id ASC
    """
    )
    suspend fun getDailyFoodEntriesOnce(
        userName: String,
        date: LocalDate
    ): List<DailyFoodEntryWithFood>
}
