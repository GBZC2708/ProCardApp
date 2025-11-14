package pe.com.zzynan.procardapp.domain.model

/**
 * Modelo de dominio del perfil de usuario. Mantiene un único nombre mostrado en la interfaz.
 */
data class UserProfile(
    val displayName: String
) {
    companion object {
        /** Valor por defecto optimizado para inicializaciones rápidas sin hits a BD. */
        const val DEFAULT_DISPLAY_NAME: String = "Atleta"
    }
}
