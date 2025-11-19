package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_days")
data class RoutineDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: Int,
    val label: String
)
