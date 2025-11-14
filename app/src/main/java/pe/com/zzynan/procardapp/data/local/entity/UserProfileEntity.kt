package pe.com.zzynan.procardapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad compacta para el perfil de usuario. Se usa un único registro para minimizar lecturas y escrituras.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "displayName") val displayName: String
)
