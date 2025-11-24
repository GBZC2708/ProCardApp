package pe.com.zzynan.procardapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import pe.com.zzynan.procardapp.ui.components.MiniBodySparkline
import pe.com.zzynan.procardapp.ui.model.BodyDashboardAlert
import pe.com.zzynan.procardapp.ui.model.BodyDashboardUiState
import pe.com.zzynan.procardapp.ui.viewmodel.BodyDashboardViewModel

@Composable
fun BodyDashboardScreen(
    viewModel: BodyDashboardViewModel = viewModel(factory = BodyDashboardViewModel.provideFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BodyDashboardContent(
        uiState = uiState,
        onQuickModeChange = viewModel::onQuickReviewModeChange
    )
}

@Composable
fun BodyDashboardContent(
    uiState: BodyDashboardUiState,
    onQuickModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Body Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.lastDate?.let {
                            Text(
                                text = "Último registro usado: ${formatter.format(it)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    ElevatedAssistChip(onClick = {}, label = { Text(uiState.phaseLabel) })
                }
                Text(
                    text = "Fase definida en Registro diario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (uiState.alerts.isNotEmpty()) {
            item {
                AlertsSection(uiState.alerts)
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Modo revisión rápida", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Muestra solo lo esencial",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = uiState.isQuickReviewMode, onCheckedChange = onQuickModeChange)
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Mini gráfica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    MiniBodySparkline(data = uiState.weightSparkline, highlightLast = true)
                }
            }
        }
        item {
            SummaryCard(uiState)
        }
        item {
            CompositionCard(uiState)
        }
        item {
            MetabolismCard(uiState)
        }
        item {
            MacrosCard(uiState)
        }
        item {
            GoalsCard(uiState)
        }
    }
}

@Composable
private fun AlertsSection(alerts: List<BodyDashboardAlert>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        alerts.forEach { alert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = alert.message,
                    modifier = Modifier.padding(12.dp),
                    color = if (alert.isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: BodyDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumen actual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                KpiColumn(uiState.kpiSummary.bodyFat, "Grasa corporal")
                KpiColumn(uiState.kpiSummary.fatFreeMass, "Masa magra")
                KpiColumn(uiState.kpiSummary.calories, "Calorías recomendadas")
                KpiColumn(uiState.kpiSummary.weightVelocity, "Velocidad de peso")
            }
        }
    }
}

@Composable
private fun KpiColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CompositionCard(uiState: BodyDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cuerpo y composición", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Peso: ${uiState.composition.weight}")
                    Text("Altura: ${uiState.composition.height}")
                    Text("Edad: ${uiState.composition.age}")
                    Text("IMC: ${uiState.composition.bmi}")
                    Text("FFMI: ${uiState.composition.ffmi}")
                }
                VerticalDivider()
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("% grasa: ${uiState.composition.bodyFat}")
                    Text("Masa grasa: ${uiState.composition.fatMass}")
                    Text("Masa magra: ${uiState.composition.leanMass}")
                    Text("WHR: ${uiState.composition.whr}")
                    Text("WHtR: ${uiState.composition.whtr}")
                }
            }
        }
    }
}

@Composable
private fun MetabolismCard(uiState: BodyDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Metabolismo y actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("BMR (Katch): ${uiState.metabolism.bmrKatch}")
            Text("BMR (Mifflin): ${uiState.metabolism.bmrMifflin}")
            Text("TDEE real: ${uiState.metabolism.tdeeReal}")
            Text("TDEE teórico: ${uiState.metabolism.tdeeTheoretical}")
            Text("Factor actividad: ${uiState.metabolism.factor}")
            Text("Pasos: ${uiState.activity.steps} → ${uiState.activity.stepsKcal.toInt()} kcal")
            Text("Cardio: ${uiState.activity.cardioMinutes} min → ${uiState.activity.cardioKcal.toInt()} kcal")
            Text("Entrenamiento: ${if (uiState.activity.trained) "Sí" else "No"} → ${uiState.activity.gymKcal.toInt()} kcal")
        }
    }
}

@Composable
private fun MacrosCard(uiState: BodyDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Calorías y macros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Calorías recomendadas: ${uiState.macros.calories}")
            Text("Proteínas: ${uiState.macros.protein}")
            Text("Grasas: ${uiState.macros.fat}")
            Text("Carbohidratos: ${uiState.macros.carbs}")
        }
    }
}

@Composable
private fun GoalsCard(uiState: BodyDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Objetivos de físico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            uiState.goals.targets.forEach { target ->
                Text(target)
            }
            if (uiState.goals.summary.isNotEmpty()) {
                Text(uiState.goals.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
