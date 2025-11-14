package pe.com.zzynan.procardapp.domain.usecase

import pe.com.zzynan.procardapp.data.mappers.toEntity
import pe.com.zzynan.procardapp.data.repository.UserProfileRepository
import pe.com.zzynan.procardapp.domain.model.UserProfile

/**
 * Use case que persiste el nombre de usuario aplicando saneamiento básico.
 */
class SaveUserProfileUseCase(
    private val userProfileRepository: UserProfileRepository
) {

    suspend operator fun invoke(rawName: String) {
        val sanitized = rawName.ifBlank { UserProfile.DEFAULT_DISPLAY_NAME }
        userProfileRepository.upsertUserProfile(UserProfile(sanitized).toEntity())
    }
}
