package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

@Entity(
    tableName = "daily_food_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userName"]),
        Index(value = ["date"]),
        Index(value = ["userName", "date"]),
        Index(value = ["foodItemId"])
    ]
)
data class DailyFoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userName: String,
    val date: LocalDate,
    val foodItemId: Long,
    val consumedAmount: Double
)

data class DailyFoodEntryWithFood(
    @Embedded val entry: DailyFoodEntryEntity,
    @Relation(
        parentColumn = "foodItemId",
        entityColumn = "id"
    )
    val food: FoodItemEntity
)
