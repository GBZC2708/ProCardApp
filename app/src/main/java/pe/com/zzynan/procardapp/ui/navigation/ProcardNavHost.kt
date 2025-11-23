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
import pe.com.zzynan.procardapp.ui.screens.FoodScreen
import pe.com.zzynan.procardapp.ui.screens.CalculadoraScreen
import pe.com.zzynan.procardapp.ui.screens.GraficosScreen
import pe.com.zzynan.procardapp.ui.screens.RegistroScreen
import pe.com.zzynan.procardapp.ui.screens.supplement.SupplementScreen
import pe.com.zzynan.procardapp.ui.viewmodel.DailyMetricsViewModel
import pe.com.zzynan.procardapp.ui.viewmodel.DailyRegisterViewModel
import pe.com.zzynan.procardapp.ui.viewmodel.FoodViewModel
import pe.com.zzynan.procardapp.ui.viewmodel.TrainingViewModel
import pe.com.zzynan.procardapp.ui.viewmodel.SupplementViewModel
import pe.com.zzynan.procardapp.ui.screens.EntrenamientoScreen
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
            val context = LocalContext.current
            val parentEntry = remember(it) {
                navController.getBackStackEntry(ProcardScreen.Registro.route)
            }
            val registerViewModel: DailyRegisterViewModel = viewModel(
                parentEntry,
                factory = DailyRegisterViewModel.provideFactory(context)
            )
            val foodViewModel: FoodViewModel = viewModel(
                parentEntry,
                factory = FoodViewModel.provideFactory(context)
            )
            val uiState = registerViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.value.userName, uiState.value.dateEpoch) {
                foodViewModel.setActiveUser(uiState.value.userName)
                foodViewModel.setActiveDate(LocalDate.ofEpochDay(uiState.value.dateEpoch))
            }

            FoodScreen(
                userName = uiState.value.userName,
                viewModel = foodViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        composable(ProcardScreen.Entrenamiento.route) {
            val context = LocalContext.current
            val parentEntry = remember(it) {
                navController.getBackStackEntry(ProcardScreen.Registro.route)
            }
            val viewModel: TrainingViewModel = viewModel(
                parentEntry,
                factory = TrainingViewModel.provideFactory(context)
            )
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val finishDialog = viewModel.finishDialogVisible.collectAsStateWithLifecycle()
            EntrenamientoScreen(
                uiState = uiState.value,
                finishDialogVisible = finishDialog.value,
                onSelectTab = viewModel::onSelectTab,
                onOpenAddExercise = viewModel::onOpenAddExercise,
                onEditExercise = viewModel::onEditExercise,
                onToggleExerciseActive = viewModel::onToggleExerciseActive,
                onExerciseEditorNameChange = viewModel::onExerciseEditorNameChange,
                onExerciseEditorGroupChange = viewModel::onExerciseEditorGroupChange,
                onDismissExerciseEditor = viewModel::onDismissExerciseEditor,
                onConfirmExerciseEditor = viewModel::onConfirmExerciseEditor,
                onOpenRoutineDialog = viewModel::onOpenRoutineDialog,
                onDismissRoutineDialog = viewModel::onDismissRoutineDialog,
                onRoutineLabelChange = viewModel::onRoutineLabelChange,
                onAddRoutineExercise = viewModel::onAddRoutineExercise,
                onRemoveRoutineExercise = viewModel::onRemoveRoutineExercise,
                onTrainingDaySelected = viewModel::onTrainingDaySelected,
                onDismissTrainingDayDialog = viewModel::onDismissTrainingDayDialog,
                onSoloVer = viewModel::onSoloVerSelected,
                onStartTraining = viewModel::onStartTraining,
                onCloseSession = viewModel::onCloseSessionScreen,
                onSetWeightChange = viewModel::onSetWeightChange,
                onSetRepsChange = viewModel::onSetRepsChange,
                onToggleSetCompleted = viewModel::onToggleSetCompleted,
                onAddSet = viewModel::onAddSet,
                onRemoveSet = viewModel::onRemoveSet,
                onShowFinishDialog = viewModel::onShowFinishDialog,
                onDismissFinishDialog = viewModel::onDismissFinishDialog,
                onConfirmFinish = viewModel::onConfirmFinishSession,
                onDeleteExercise = viewModel::onDeleteExercise,
                onToggleTimerPause = viewModel::onToggleTimerPause,
                onResetTimer = viewModel::onResetTimer,
                onMoveRoutineExerciseUp = viewModel::onMoveRoutineExerciseUp,     // 👈 nuevo
                onMoveRoutineExerciseDown = viewModel::onMoveRoutineExerciseDown
            )

        }
        composable(ProcardScreen.Suplementacion.route) {
            val context = LocalContext.current
            val parentEntry = remember(it) {
                navController.getBackStackEntry(ProcardScreen.Registro.route)
            }
            val registerViewModel: DailyRegisterViewModel = viewModel(
                parentEntry,
                factory = DailyRegisterViewModel.provideFactory(context)
            )
            val supplementViewModel: SupplementViewModel = viewModel(
                parentEntry,
                factory = SupplementViewModel.provideFactory(context)
            )

            val uiState = registerViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.value.userName, uiState.value.dateEpoch) {
                supplementViewModel.setActiveUser(uiState.value.userName)
                supplementViewModel.setSelectedDate(LocalDate.ofEpochDay(uiState.value.dateEpoch))
            }

            SupplementScreen(
                viewModel = supplementViewModel,
                onBack = { navController.navigateUp() }
            )
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
            val foodViewModel: FoodViewModel = viewModel(
                parentEntry,
                factory = FoodViewModel.provideFactory(context)
            )

            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val foodUiState = foodViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.value.userName, uiState.value.dateEpoch) {
                // sincroniza solo comida, los pasos/peso ya salen del DailyRegisterViewModel
                foodViewModel.setActiveUser(uiState.value.userName)
                foodViewModel.setActiveDate(LocalDate.ofEpochDay(uiState.value.dateEpoch))
            }

            GraficosScreen(
                weightPoints = uiState.value.weeklyMetrics.weightPoints,
                stepsPoints = uiState.value.weeklyMetrics.stepsPoints,
                caloriesPoints = foodUiState.value.weeklyCalories,
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
