package pe.com.zzynan.procardapp.domain.model

/**
 * Modelo de dominio del perfil de usuario. Mantiene un único nombre mostrado en la interfaz.
 */
data class UserProfile(
    val displayName: String = DEFAULT_DISPLAY_NAME,
    val sex: Sex? = null,
    val age: Int? = null,
    val heightCm: Float? = null,
    val usesPharmacology: Boolean = false,
    val neckCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,
    val chestCm: Float? = null,
    val wristCm: Float? = null,
    val thighCm: Float? = null,
    val calfCm: Float? = null,
    val relaxedBicepsCm: Float? = null,
    val flexedBicepsCm: Float? = null,
    val forearmCm: Float? = null,
    val footCm: Float? = null
) {
    companion object {
        /** Valor por defecto optimizado para inicializaciones rápidas sin hits a BD. */
        const val DEFAULT_DISPLAY_NAME: String = "Atleta"
    }
}

enum class Sex {
    Male,
    Female
}
