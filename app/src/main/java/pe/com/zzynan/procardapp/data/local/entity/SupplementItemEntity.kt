package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplement_items",
    indices = [Index("userName")]
)
data class SupplementItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userName: String,
    val name: String,
    val baseAmount: Double?,
    val baseUnit: String?,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
