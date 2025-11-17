package pe.com.zzynan.procardapp.ui.model

/**
 * Estado compacto para mostrar el card de peso en ayunas.
 */
data class WeightCardUiModel(
    val displayValue: String = "0.00",
    val status: WeightStatus = WeightStatus.Pending,
    val hasFallback: Boolean = false
)

enum class WeightStatus { Saved, Pending }
