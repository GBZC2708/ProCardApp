package pe.com.zzynan.procardapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import pe.com.zzynan.procardapp.ui.viewmodel.DailyMetricsViewModel
import pe.com.zzynan.procardapp.ui.viewmodel.DailyRegisterViewModel
import androidx.compose.runtime.LaunchedEffect
import java.time.LocalDate


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
                onToggleStepCounter = viewModel::onToggleStepCounter,
                onOpenHistory = viewModel::onOpenHistory,
                onDismissHistory = viewModel::onDismissHistory,
                onPreviousHistory = viewModel::onHistoryPreviousDay,
                onNextHistory = viewModel::onHistoryNextDay,
                onHistoryWeightChange = viewModel::onHistoryWeightChanged,
                onConfirmHistory = viewModel::onConfirmHistory
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
        composable(ProcardScreen.Graficos.route) { backStackEntry ->
            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ProcardScreen.Registro.route)
            }
            val viewModel: DailyRegisterViewModel = viewModel(
                parentEntry,
                factory = DailyRegisterViewModel.provideFactory(context)
            )
            val metricsViewModel: DailyMetricsViewModel = viewModel(
                parentEntry,
                factory = DailyMetricsViewModel.provideFactory(context)
            )
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val weeklyMetricsUiState = metricsViewModel.weeklyMetricsUiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.value.userName) {
                // sincroniza el usuario para que los gráficos lean los mismos datos que Registro
                metricsViewModel.setActiveUsername(uiState.value.userName)
                // opcional, pero recomendable: asegurar que el rango termine en hoy
                metricsViewModel.setActiveDate(LocalDate.now())
            }

            GraficosScreen(
                weightPoints = weeklyMetricsUiState.value.weightPoints,
                stepsPoints = weeklyMetricsUiState.value.stepsPoints,
                weightEditor = uiState.value.weightEditor,
                onWeightPointSelected = viewModel::onChartWeightSelected,
                onDismissHistory = viewModel::onDismissHistory,
                onPreviousHistory = viewModel::onHistoryPreviousDay,
                onNextHistory = viewModel::onHistoryNextDay,
                onHistoryWeightChange = viewModel::onHistoryWeightChanged,
                onConfirmHistory = viewModel::onConfirmHistory
            )
        }
    }
}
