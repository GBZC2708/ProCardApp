package pe.com.zzynan.procardapp.ui.state

import pe.com.zzynan.procardapp.domain.model.UserProfile

/**
 * Estado inmutable de la barra superior para evitar recomputaciones innecesarias.
 */
data class TopBarUiState(
    val displayName: String = UserProfile.DEFAULT_DISPLAY_NAME
)
