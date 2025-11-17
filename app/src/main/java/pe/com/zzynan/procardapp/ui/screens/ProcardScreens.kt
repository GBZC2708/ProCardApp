package pe.com.zzynan.procardapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.components.StepCounterCard
import pe.com.zzynan.procardapp.ui.components.WeightCard
import pe.com.zzynan.procardapp.ui.components.WeightHistoryDialog
import pe.com.zzynan.procardapp.ui.components.StepsLineChart
import pe.com.zzynan.procardapp.ui.components.WeightLineChart
import pe.com.zzynan.procardapp.ui.state.DailyRegisterUiState

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepCounterCard(
            uiModel = uiState.stepCounter,
            onPlayPauseClick = onToggleStepCounter,
            modifier = Modifier.fillMaxWidth()
        )
        WeightCard(
            uiModel = uiState.weightCard,
            onValueClick = { onOpenHistory(LocalDate.now()) },
            onHistoryClick = { onOpenHistory(LocalDate.now()) },
            modifier = Modifier.fillMaxWidth()
        )
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

    WeightHistoryDialog(
        uiModel = uiState.weightEditor,
        onDismiss = onDismissHistory,
        onPrevious = onPreviousHistory,
        onNext = onNextHistory,
        onSave = onConfirmHistory,
        onWeightChange = onHistoryWeightChange
    )
}

@Composable
fun GraficosScreen(
    uiState: DailyRegisterUiState,
    onWeightPointSelected: (LocalDate) -> Unit,
    onDismissHistory: () -> Unit,
    onPreviousHistory: () -> Unit,
    onNextHistory: () -> Unit,
    onHistoryWeightChange: (String) -> Unit,
    onConfirmHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    LaunchedEffect(uiState.weeklyMetrics) {
        horizontalScrollState.scrollTo(horizontalScrollState.maxValue)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepsLineChart(points = uiState.weeklyMetrics.stepsPoints)
            WeightLineChart(
                points = uiState.weeklyMetrics.weightPoints,
                onPointSelected = onWeightPointSelected
            )
        }
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

@Composable
fun AlimentacionScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // TODO: agregar contenido de esta pantalla.
    }
}

@Composable
fun EntrenamientoScreen(modifier: Modifier = Modifier) {
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
