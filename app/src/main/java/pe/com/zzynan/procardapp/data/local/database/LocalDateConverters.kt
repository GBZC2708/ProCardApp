package pe.com.zzynan.procardapp.data.local.database

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
