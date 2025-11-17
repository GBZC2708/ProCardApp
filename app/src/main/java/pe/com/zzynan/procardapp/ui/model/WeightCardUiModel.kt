package pe.com.zzynan.procardapp.ui.model

/**
 * Estado compacto para mostrar el card de peso en ayunas.
 */
data class WeightCardUiModel(
    val value: String = "",
    val placeholder: String? = null,
    val statusLabel: String = "",
    val isSavedToday: Boolean = false
)
