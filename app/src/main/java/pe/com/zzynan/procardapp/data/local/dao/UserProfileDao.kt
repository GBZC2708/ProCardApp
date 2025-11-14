package pe.com.zzynan.procardapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity

/**
 * DAO enfocado en operaciones ligeras del perfil de usuario para evitar bloquear el hilo principal.
 */
@Dao
interface UserProfileDao {

    /**
     * Observa cambios del perfil usando Flow para reacciones eficientes sin pooling manual.
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    /**
     * Usa Upsert para ahorrar roundtrips y garantizar idempotencia al persistir el nombre.
     */
    @Upsert
    suspend fun upsertUserProfile(entity: UserProfileEntity)
}
