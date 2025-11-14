package pe.com.zzynan.procardapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.com.zzynan.procardapp.data.mappers.toDomain
import pe.com.zzynan.procardapp.data.repository.UserProfileRepository
import pe.com.zzynan.procardapp.domain.model.UserProfile

/**
 * Use case que expone el perfil usando Flow con valor por defecto para evitar nulls en la UI.
 */
class ObserveUserProfileUseCase(
    private val userProfileRepository: UserProfileRepository
) {

    operator fun invoke(): Flow<UserProfile> {
        return userProfileRepository.observeUserProfile()
            .map { entity -> entity?.toDomain() ?: UserProfile(UserProfile.DEFAULT_DISPLAY_NAME) }
    }
}
