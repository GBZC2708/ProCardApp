package pe.com.zzynan.procardapp.data.mappers

import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity
import pe.com.zzynan.procardapp.domain.model.UserProfile

/**
 * Mapeos de perfil compactos para reducir transformaciones en caliente.
 */
fun UserProfileEntity.toDomain(): UserProfile = UserProfile(displayName)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(displayName = displayName)
