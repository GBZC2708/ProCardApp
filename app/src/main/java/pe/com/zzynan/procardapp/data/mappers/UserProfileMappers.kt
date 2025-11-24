package pe.com.zzynan.procardapp.data.mappers

import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity
import pe.com.zzynan.procardapp.domain.model.UserProfile

/**
 * Mapeos de perfil compactos para reducir transformaciones en caliente.
 */
fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    displayName = displayName,
    sex = sex?.let { runCatching { pe.com.zzynan.procardapp.domain.model.Sex.valueOf(it) }.getOrNull() },
    age = age,
    heightCm = heightCm,
    usesPharmacology = usesPharmacology,
    neckCm = neckCm,
    waistCm = waistCm,
    hipCm = hipCm,
    chestCm = chestCm,
    wristCm = wristCm,
    thighCm = thighCm,
    calfCm = calfCm,
    relaxedBicepsCm = relaxedBicepsCm,
    flexedBicepsCm = flexedBicepsCm,
    forearmCm = forearmCm,
    footCm = footCm
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    displayName = displayName,
    sex = sex?.name,
    age = age,
    heightCm = heightCm,
    usesPharmacology = usesPharmacology,
    neckCm = neckCm,
    waistCm = waistCm,
    hipCm = hipCm,
    chestCm = chestCm,
    wristCm = wristCm,
    thighCm = thighCm,
    calfCm = calfCm,
    relaxedBicepsCm = relaxedBicepsCm,
    flexedBicepsCm = flexedBicepsCm,
    forearmCm = forearmCm,
    footCm = footCm
)
