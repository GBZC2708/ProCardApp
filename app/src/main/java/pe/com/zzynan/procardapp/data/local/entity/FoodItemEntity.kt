package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["userName"]),
        Index(value = ["userName", "name"])
    ]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userName: String,
    val name: String,
    val baseAmount: Double,
    val baseUnit: String,
    val proteinBase: Double,
    val fatBase: Double,
    val carbBase: Double
)
