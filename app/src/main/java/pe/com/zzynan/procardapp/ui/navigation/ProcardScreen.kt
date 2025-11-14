package pe.com.zzynan.procardapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pe.com.zzynan.procardapp.R

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/navigation/ProcardScreen.kt
sealed class ProcardScreen(
    val route: String,
    @StringRes val labelRes: Int,
    val title: String,
    val description: String,
    private val iconContent: @Composable () -> Unit
) {
    // Color utilizado para íconos seleccionados en la barra inferior.
    open val selectedIconColor: Color @Composable get() = MaterialTheme.colorScheme.primary
    // Color utilizado para íconos no seleccionados en la barra inferior.
    open val unselectedIconColor: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    // Color del indicador de selección en la barra inferior.
    open val indicatorColor: Color @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)

    // Renderiza el icono asociado a la pantalla dentro de la barra de navegación.
    @Composable
    fun icon() = iconContent()

    data object Registro : ProcardScreen(
        route = "registro",
        labelRes = R.string.screen_registro,
        title = "Registro",
        description = "Registra tus datos diarios de progreso.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = null
            )
        }
    )

    data object Alimentacion : ProcardScreen(
        route = "alimentacion",
        labelRes = R.string.screen_alimentacion,
        title = "Alimentación",
        description = "Controla tus comidas, macros y calorías.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.LocalDining,
                contentDescription = null
            )
        }
    )

    data object Entrenamiento : ProcardScreen(
        route = "entrenamiento",
        labelRes = R.string.screen_entrenamiento,
        title = "Entrenamiento",
        description = "Registra tus rutinas y cargas de entrenamiento.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null
            )
        }
    )

    data object Suplementacion : ProcardScreen(
        route = "suplementacion",
        labelRes = R.string.screen_suplementacion,
        title = "Suplementación",
        description = "Lleva el seguimiento de tu suplementación diaria.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = null
            )
        }
    )

    data object Calculadora : ProcardScreen(
        route = "calculadora",
        labelRes = R.string.screen_calculadora,
        title = "Calculadora",
        description = "Realiza cálculos de macros, calorías y objetivos.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.Calculate,
                contentDescription = null
            )
        }
    )

    data object Graficos : ProcardScreen(
        route = "graficos",
        labelRes = R.string.screen_graficos,
        title = "Gráficos",
        description = "Visualiza tu evolución en peso, calorías y rendimiento.",
        iconContent = {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null
            )
        }
    )

    companion object {
        // Listado ordenado de pantallas para construir la navegación inferior.
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
