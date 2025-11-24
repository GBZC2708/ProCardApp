package pe.com.zzynan.procardapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import pe.com.zzynan.procardapp.R

/**
 * Representación de cada pantalla de la app.
 *
 * Cada pantalla tiene:
 * - route: Ruta de navegación.
 * - labelRes: Nombre del tab (string resource).
 * - title / description: Ya no se usan en la TopBar actual (pero se mantienen por si luego sirven).
 * - iconFilled / iconOutlined: Iconos para estado seleccionado y no seleccionado.
 */
sealed class ProcardScreen(
    val route: String,
    @StringRes val labelRes: Int,
    val title: String,
    val description: String,
    private val iconFilled: ImageVector,
    private val iconOutlined: ImageVector
) {

    /**
     * Renderiza el ícono del BottomNav.
     *
     * El ícono cambia según:
     * - selected = true: iconFilled + color primario.
     * - selected = false: iconOutlined + color onSurfaceVariant.
     *
     * Se usa desde ProcardBottomNav.
     */
    @Composable
    open fun Icon(selected: Boolean) {
        Icon(
            imageVector = if (selected) iconFilled else iconOutlined,
            contentDescription = title,
            tint = if (selected)
                MaterialTheme.colorScheme.primary   // color destacado
            else
                MaterialTheme.colorScheme.onSurfaceVariant // color apagado
        )
    }

    // -------------------------------------------------------------------------
    // DEFINICIÓN DE CADA SCREEN
    // -------------------------------------------------------------------------

    /**
     * Pantalla: Registro de datos diarios
     */
    data object Registro : ProcardScreen(
        route = "registro",
        labelRes = R.string.screen_registro,
        title = "Registro",
        description = "Registra tus datos diarios de progreso.",
        iconFilled = Icons.Filled.EditNote,
        iconOutlined = Icons.Outlined.EditNote
    )

    /**
     * Pantalla: Alimentación - comidas, macros, calorías
     */
    data object Alimentacion : ProcardScreen(
        route = "alimentacion",
        labelRes = R.string.screen_alimentacion,
        title = "Alimentación",
        description = "Controla tus comidas, macros y calorías.",
        iconFilled = Icons.Filled.Restaurant,
        iconOutlined = Icons.Outlined.Restaurant
    )

    /**
     * Pantalla: Entrenamiento y rutinas
     */
    data object Entrenamiento : ProcardScreen(
        route = "entrenamiento",
        labelRes = R.string.screen_entrenamiento,
        title = "Entrenamiento",
        description = "Registra tus rutinas y cargas de entrenamiento.",
        iconFilled = Icons.Filled.FitnessCenter,
        iconOutlined = Icons.Outlined.FitnessCenter
    )

    /**
     * Pantalla: Suplementación
     */
    data object Suplementacion : ProcardScreen(
        route = "suplementacion",
        labelRes = R.string.screen_suplementacion,
        title = "Suplementación",
        description = "Lleva el seguimiento de tu suplementación diaria.",
        iconFilled = Icons.Filled.Medication,
        iconOutlined = Icons.Outlined.Medication
    )

    /**
     * Pantalla: Calculadora de calorías, macros y objetivos
     */
    data object Calculadora : ProcardScreen(
        route = "calculadora",
        labelRes = R.string.screen_calculadora,
        title = "Body Dashboard",
        description = "Panel de control corporal.",
        iconFilled = Icons.Filled.Calculate,
        iconOutlined = Icons.Outlined.Calculate
    )

    /**
     * Pantalla: Gráficos de evolución
     */
    data object Graficos : ProcardScreen(
        route = "graficos",
        labelRes = R.string.screen_graficos,
        title = "Gráficos",
        description = "Visualiza tu evolución en peso, calorías y rendimiento.",
        iconFilled = Icons.Filled.ShowChart,
        iconOutlined = Icons.Outlined.ShowChart
    )

    // -------------------------------------------------------------------------
    // LISTA DE TODAS LAS PANTALLAS PARA EL BottomNav
    // -------------------------------------------------------------------------
    companion object {
        val allScreens = listOf(
            Registro,
            Alimentacion,
            Entrenamiento,
            Suplementacion,
            Calculadora,
            Graficos
        )
    }
}
