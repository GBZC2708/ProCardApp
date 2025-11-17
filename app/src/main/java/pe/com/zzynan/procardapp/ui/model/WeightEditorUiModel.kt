package pe.com.zzynan.procardapp.ui.model

import java.time.LocalDate

/**
 * Estado de la ventana de historial/edición de peso.
 */
data class WeightEditorUiModel(
    val isVisible: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weightText: String = "",
    val placeholder: String? = null,
    val canNavigateNext: Boolean = false
)
