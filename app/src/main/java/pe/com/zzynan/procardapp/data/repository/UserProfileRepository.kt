package pe.com.zzynan.procardapp.data.repository

import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.procardapp.data.local.dao.UserProfileDao
import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity

/**
 * Repositorio ligero para el perfil de usuario. Centraliza el acceso a Room y facilita pruebas.
 */
class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {

    /**
     * Observa cambios del perfil usando Flow para mantener la UI sincronizada sin polling.
     */
    fun observeUserProfile(): Flow<UserProfileEntity?> = userProfileDao.observeUserProfile()

    /**
     * Persiste el perfil aprovechando upsert para minimizar escrituras redundantes.
     */
    suspend fun upsertUserProfile(entity: UserProfileEntity) {
        userProfileDao.upsertUserProfile(entity)
    }
}
