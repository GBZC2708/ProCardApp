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
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "sex") val sex: String? = null,
    @ColumnInfo(name = "age") val age: Int? = null,
    @ColumnInfo(name = "heightCm") val heightCm: Float? = null,
    @ColumnInfo(name = "usesPharmacology") val usesPharmacology: Boolean = false,
    @ColumnInfo(name = "neckCm") val neckCm: Float? = null,
    @ColumnInfo(name = "waistCm") val waistCm: Float? = null,
    @ColumnInfo(name = "hipCm") val hipCm: Float? = null,
    @ColumnInfo(name = "chestCm") val chestCm: Float? = null,
    @ColumnInfo(name = "wristCm") val wristCm: Float? = null,
    @ColumnInfo(name = "thighCm") val thighCm: Float? = null,
    @ColumnInfo(name = "calfCm") val calfCm: Float? = null,
    @ColumnInfo(name = "relaxedBicepsCm") val relaxedBicepsCm: Float? = null,
    @ColumnInfo(name = "flexedBicepsCm") val flexedBicepsCm: Float? = null,
    @ColumnInfo(name = "forearmCm") val forearmCm: Float? = null,
    @ColumnInfo(name = "footCm") val footCm: Float? = null
)
