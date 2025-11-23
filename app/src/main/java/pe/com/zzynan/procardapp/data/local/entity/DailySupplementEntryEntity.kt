package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_supplement_entries",
    foreignKeys = [
        ForeignKey(
            entity = SupplementItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userName"),
        Index("dateEpochDay"),
        Index("supplementId")
    ]
)
data class DailySupplementEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userName: String,
    val dateEpochDay: Long,
    val supplementId: Long,
    val timeSlot: String,
    val amount: Double?,
    val unit: String?,
    val orderInSlot: Int = 0
)
