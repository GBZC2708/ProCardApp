package pe.com.zzynan.procardapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Locale
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.domain.model.TrainingStage
import pe.com.zzynan.procardapp.ui.components.CardioCard
import pe.com.zzynan.procardapp.ui.components.NutritionSummaryCard
import pe.com.zzynan.procardapp.ui.components.SaltCard
import pe.com.zzynan.procardapp.ui.components.SleepCard
import pe.com.zzynan.procardapp.ui.components.StepCounterCard
import pe.com.zzynan.procardapp.ui.components.SupplementationCard
import pe.com.zzynan.procardapp.ui.components.TrainingDoneCard
import pe.com.zzynan.procardapp.ui.components.TrainingStageCard
import pe.com.zzynan.procardapp.ui.components.WaterCard
import pe.com.zzynan.procardapp.ui.components.WeightCard
import pe.com.zzynan.procardapp.ui.components.WeightHistoryDialog
import pe.com.zzynan.procardapp.ui.components.StepsLineChart
import pe.com.zzynan.procardapp.ui.components.WeightLineChart
import pe.com.zzynan.procardapp.ui.components.WeeklyCaloriesChart
import pe.com.zzynan.procardapp.ui.model.WeeklyStepsPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyWeightPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyCaloriesPointUiModel
import pe.com.zzynan.procardapp.ui.state.DailyRegisterUiState
import pe.com.zzynan.procardapp.ui.model.WeightEditorUiModel

@Composable
fun RegistroScreen(
    uiState: DailyRegisterUiState,
    onToggleStepCounter: () -> Unit,
    onOpenHistory: (LocalDate) -> Unit,
    onDismissHistory: () -> Unit,
    onPreviousHistory: () -> Unit,
    onNextHistory: () -> Unit,
    onHistoryWeightChange: (String) -> Unit,
    onConfirmHistory: () -> Unit,
    onStageSelected: (TrainingStage) -> Unit,
    onSleepHoursConfirmed: (Float) -> Unit,
    onCardioMinutesConfirmed: (Int) -> Unit,
    onWaterIncrement: () -> Unit,
    onWaterDecrement: () -> Unit,
    onWaterTargetsChanged: (Int, Int) -> Unit,
    onSaltIncrement: () -> Unit,
    onSaltDecrement: () -> Unit,
    onTrainingDoneChanged: (Boolean) -> Unit,
    onSupplementationToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = uiState.metrics
    val stage = metrics?.stage ?: TrainingStage.DEFINICION
    val sleepMinutes = metrics?.sleepMinutes ?: 0
    val sleepHoursText = if (sleepMinutes > 0) String.format(Locale.US, "%.2f h", sleepMinutes / 60f) else "0 h"
    val cardioMinutes = metrics?.cardioMinutes ?: 0
    val waterLiters = (metrics?.waterMl ?: 0) / 1000
    val saltGramsX10 = metrics?.saltGramsX10 ?: 0
    val saltText = String.format(Locale.US, "%.1f g", saltGramsX10 / 10f)
    val trainingDone = metrics?.trainingDone ?: false

    val sleepRegex = Regex("^\\d*(?:\\.\\d{0,2})?$")
    val cardioRegex = Regex("^\\d{0,4}$")

    val defaultCardioMinutes = 30

    val (showStageDialog, setShowStageDialog) = remember { mutableStateOf(false) }
    val (showSleepDialog, setShowSleepDialog) = remember { mutableStateOf(false) }
    val (showCardioDialog, setShowCardioDialog) = remember { mutableStateOf(false) }
    val (showWaterTargetsDialog, setShowWaterTargetsDialog) = remember { mutableStateOf(false) }

    val (sleepInput, setSleepInput) = remember(sleepMinutes) {
        mutableStateOf(if (sleepMinutes > 0) String.format(Locale.US, "%.2f", sleepMinutes / 60f) else "")
    }
    val (cardioInput, setCardioInput) = remember(cardioMinutes) {
        mutableStateOf(if (cardioMinutes > 0) cardioMinutes.toString() else "")
    }
    val (trainingTargetInput, setTrainingTargetInput) = remember(uiState.waterTargetTrainingLiters) {
        mutableStateOf(uiState.waterTargetTrainingLiters.toString())
    }
    val (restTargetInput, setRestTargetInput) = remember(uiState.waterTargetRestLiters) {
        mutableStateOf(uiState.waterTargetRestLiters.toString())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TrainingStageCard(stage = stage, onStageClick = { setShowStageDialog(true) })
        StepCounterCard(
            uiModel = uiState.stepCounter,
            onPlayPauseClick = onToggleStepCounter,
            modifier = Modifier.fillMaxWidth()
        )
        WeightCard(
            uiModel = uiState.weightCard,
            onValueClick = { onOpenHistory(LocalDate.now()) },
            modifier = Modifier.fillMaxWidth()
        )
        SleepCard(
            hoursText = sleepHoursText,
            hasValue = sleepMinutes > 0,
            onEditClick = { setShowSleepDialog(true) }
        )
        CardioCard(
            minutes = cardioMinutes,
            isDone = cardioMinutes > 0,
            onEditMinutesClick = { setShowCardioDialog(true) },
            onToggleDone = {
                if (cardioMinutes > 0) onCardioMinutesConfirmed(0) else onCardioMinutesConfirmed(defaultCardioMinutes)
            }
        )
        WaterCard(
            currentLiters = waterLiters,
            targetTrainingLiters = uiState.waterTargetTrainingLiters,
            targetRestLiters = uiState.waterTargetRestLiters,
            onIncrement = onWaterIncrement,
            onDecrement = onWaterDecrement,
            onTargetsClick = { setShowWaterTargetsDialog(true) }
        )
        SaltCard(
            gramsText = saltText,
            hasValue = saltGramsX10 > 0,
            onIncrement = onSaltIncrement,
            onDecrement = onSaltDecrement
        )
        TrainingDoneCard(
            isDone = trainingDone,
            onToggle = { onTrainingDoneChanged(!trainingDone) }
        )
        NutritionSummaryCard(summary = uiState.nutritionSummary)
        SupplementationCard(isDone = uiState.supplementationDone, onToggle = onSupplementationToggled)
        uiState.metrics?.let { metrics ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.daily_steps_saved, metrics.dailySteps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(id = R.string.daily_last_update_user, uiState.userName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showStageDialog) {
        AlertDialog(
            onDismissRequest = { setShowStageDialog(false) },
            confirmButton = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrainingStage.values().forEach { option ->
                        TextButton(onClick = {
                            onStageSelected(option)
                            setShowStageDialog(false)
                        }) {
                            Text(text = option.toReadableLabel())
                        }
                    }
                }
            }
        )
    }

    if (showSleepDialog) {
        AlertDialog(
            onDismissRequest = { setShowSleepDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    val normalized = sleepInput.replace(',', '.')
                    val hours = normalized.toFloatOrNull()?.takeIf { sleepRegex.matches(normalized) }
                    if (hours != null) {
                        onSleepHoursConfirmed(hours)
                        setShowSleepDialog(false)
                    }
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowSleepDialog(false) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Horas de sueño de hoy", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = sleepInput,
                        onValueChange = { value ->
                            val normalized = value.replace(',', '.')
                            if (normalized.isBlank() || sleepRegex.matches(normalized)) {
                                setSleepInput(normalized)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        )
    }

    if (showCardioDialog) {
        AlertDialog(
            onDismissRequest = { setShowCardioDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = cardioInput.toIntOrNull()?.takeIf { cardioRegex.matches(cardioInput) }
                    if (minutes != null) {
                        onCardioMinutesConfirmed(minutes)
                        setShowCardioDialog(false)
                    }
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowCardioDialog(false) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Minutos de cardio en ayunas", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = cardioInput,
                        onValueChange = { value -> if (cardioRegex.matches(value)) setCardioInput(value) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        )
    }

    if (showWaterTargetsDialog) {
        AlertDialog(
            onDismissRequest = { setShowWaterTargetsDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    val trainingLiters = trainingTargetInput.toIntOrNull()
                    val restLiters = restTargetInput.toIntOrNull()
                    if (trainingLiters != null && restLiters != null) {
                        onWaterTargetsChanged(trainingLiters, restLiters)
                        setShowWaterTargetsDialog(false)
                    }
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowWaterTargetsDialog(false) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Metas de hidratación", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = trainingTargetInput,
                        onValueChange = { value -> if (cardioRegex.matches(value)) setTrainingTargetInput(value) },
                        label = { Text("Litros día entreno") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = restTargetInput,
                        onValueChange = { value -> if (cardioRegex.matches(value)) setRestTargetInput(value) },
                        label = { Text("Litros día descanso") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        )
    }

    WeightHistoryDialog(
        uiModel = uiState.weightEditor,
        onDismiss = onDismissHistory,
        onPrevious = onPreviousHistory,
        onNext = onNextHistory,
        onSave = onConfirmHistory,
        onWeightChange = onHistoryWeightChange
    )
}

private fun TrainingStage.toReadableLabel(): String = when (this) {
    TrainingStage.DEFINICION -> "Definición"
    TrainingStage.MANTENIMIENTO -> "Mantenimiento"
    TrainingStage.DEFICIT -> "Déficit"
}

@Composable
fun GraficosScreen(
    weightPoints: List<WeeklyWeightPoint>,
    stepsPoints: List<WeeklyStepsPoint>,
    caloriesPoints: List<WeeklyCaloriesPointUiModel>,
    weightEditor: WeightEditorUiModel,
    onWeightPointSelected: (LocalDate) -> Unit,
    onDismissHistory: () -> Unit,
    onPreviousHistory: () -> Unit,
    onNextHistory: () -> Unit,
    onHistoryWeightChange: (String) -> Unit,
    onConfirmHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeightLineChart(
            points = weightPoints,
            onPointSelected = onWeightPointSelected,
            modifier = Modifier.fillMaxWidth()
        )

        StepsLineChart(
            points = stepsPoints,
            modifier = Modifier.fillMaxWidth()
        )

        WeeklyCaloriesChart(
            points = caloriesPoints,
            modifier = Modifier.fillMaxWidth()
        )
    }

    WeightHistoryDialog(
        uiModel = weightEditor,
        onDismiss = onDismissHistory,
        onPrevious = onPreviousHistory,
        onNext = onNextHistory,
        onSave = onConfirmHistory,
        onWeightChange = onHistoryWeightChange
    )
}

@Composable
fun AlimentacionScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // TODO: agregar contenido de esta pantalla.
    }
}

@Composable
fun SuplementacionScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // TODO: agregar contenido de esta pantalla.
    }
}

@Composable
fun CalculadoraScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // TODO: agregar contenido de esta pantalla.
    }
}
