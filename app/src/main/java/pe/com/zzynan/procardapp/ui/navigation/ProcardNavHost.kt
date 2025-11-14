package pe.com.zzynan.procardapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.com.zzynan.procardapp.ui.screens.AlimentacionScreen
import pe.com.zzynan.procardapp.ui.screens.CalculadoraScreen
import pe.com.zzynan.procardapp.ui.screens.EntrenamientoScreen
import pe.com.zzynan.procardapp.ui.screens.GraficosScreen
import pe.com.zzynan.procardapp.ui.screens.RegistroScreen
import pe.com.zzynan.procardapp.ui.screens.SuplementacionScreen
import pe.com.zzynan.procardapp.ui.viewmodel.DailyRegisterViewModel

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/navigation/ProcardNavHost.kt
@Composable
fun ProcardNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // NavHost que gestiona las rutas principales aplicando el padding del Scaffold.
    NavHost(
        navController = navController,
        startDestination = ProcardScreen.Registro.route,
        modifier = modifier.padding(padding)
    ) {
        composable(ProcardScreen.Registro.route) {
            val context = LocalContext.current
            val viewModel: DailyRegisterViewModel = viewModel(
                factory = DailyRegisterViewModel.provideFactory(context)
            )
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            RegistroScreen(
                uiState = uiState.value,
                onToggleStepCounter = viewModel::onToggleStepCounter
            )
        }
        composable(ProcardScreen.Alimentacion.route) {
            // Contenido placeholder para la pantalla de alimentación.
            AlimentacionScreen()
        }
        composable(ProcardScreen.Entrenamiento.route) {
            // Contenido placeholder para la pantalla de entrenamiento.
            EntrenamientoScreen()
        }
        composable(ProcardScreen.Suplementacion.route) {
            // Contenido placeholder para la pantalla de suplementación.
            SuplementacionScreen()
        }
        composable(ProcardScreen.Calculadora.route) {
            // Contenido placeholder para la pantalla de calculadora.
            CalculadoraScreen()
        }
        composable(ProcardScreen.Graficos.route) {
            // Contenido placeholder para la pantalla de gráficos.
            GraficosScreen()
        }
    }
}
