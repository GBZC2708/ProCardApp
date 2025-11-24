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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.zzynan.procardapp.domain.model.Sex
import java.time.format.DateTimeFormatter
import pe.com.zzynan.procardapp.ui.components.MiniBodySparkline
import pe.com.zzynan.procardapp.ui.model.BodyDashboardAlert
import pe.com.zzynan.procardapp.ui.model.BodyDashboardUiState
import pe.com.zzynan.procardapp.ui.model.BodyMeasurementsUi
import pe.com.zzynan.procardapp.ui.model.PhysicalDataUi
import pe.com.zzynan.procardapp.ui.viewmodel.BodyDashboardViewModel

@Composable
fun BodyDashboardScreen(
    viewModel: BodyDashboardViewModel = viewModel(factory = BodyDashboardViewModel.provideFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BodyDashboardContent(
        uiState = uiState,
        onQuickModeChange = viewModel::onQuickReviewModeChange,
        onSexChange = viewModel::onSexChange,
        onPharmaUsageChange = viewModel::onPharmaUsageChange,
        onAgeChange = viewModel::onAgeChange,
        onHeightChange = viewModel::onHeightChange,
        onMeasurementChange = viewModel::onMeasurementChange
    )
}

@Composable
fun BodyDashboardContent(
    uiState: BodyDashboardUiState,
    onQuickModeChange: (Boolean) -> Unit,
    onSexChange: (Sex) -> Unit,
    onPharmaUsageChange: (Boolean) -> Unit,
    onAgeChange: (Int?) -> Unit,
    onHeightChange: (Float?) -> Unit,
    onMeasurementChange: (String, Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
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

        // Alertas
        if (uiState.alerts.isNotEmpty()) {
            item {
                AlertsSection(uiState.alerts)
            }
        }

        // Toggle de modo rápido
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Modo revisión rápida", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Desactívalo para editar datos y ver detalles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isQuickReviewMode,
                            onCheckedChange = onQuickModeChange
                        )
                    }
                }
            }
        }

        // Mini sparkline
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Mini gráfica de peso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    MiniBodySparkline(data = uiState.weightSparkline, highlightLast = true)
                }
            }
        }

        // Datos físicos y medidas SOLO cuando NO está en revisión rápida
        if (!uiState.isQuickReviewMode) {
            item {
                PhysicalDataCard(
                    physicalData = uiState.physicalData,
                    measurements = uiState.measurements,
                    onSexChange = onSexChange,
                    onPharmaUsageChange = onPharmaUsageChange,
                    onAgeChange = onAgeChange,
                    onHeightChange = onHeightChange,
                    onMeasurementChange = onMeasurementChange
                )
            }
        }

        // Resumen KPIs SIEMPRE visible
        item {
            SummaryCard(uiState)
        }

        // Composición + metabolismo SOLO cuando NO está en revisión rápida
        if (!uiState.isQuickReviewMode) {
            item {
                CompositionCard(uiState)
            }
            item {
                MetabolismCard(uiState)
            }
        }

        // Macros y objetivos SIEMPRE
        item {
            MacrosCard(uiState)
        }
        item {
            GoalsCard(uiState)
        }
    }
}

@Composable
private fun PhysicalDataCard(
    physicalData: PhysicalDataUi,
    measurements: BodyMeasurementsUi,
    onSexChange: (Sex) -> Unit,
    onPharmaUsageChange: (Boolean) -> Unit,
    onAgeChange: (Int?) -> Unit,
    onHeightChange: (Float?) -> Unit,
    onMeasurementChange: (String, Float?) -> Unit
) {
    val numberRegex = remember { Regex("^\\d*(?:[\\.,]\\d{0,2})?$") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Datos físicos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Bloque A – Datos básicos
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Datos básicos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sexo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sexo:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    ElevatedAssistChip(
                        onClick = { onSexChange(Sex.Male) },
                        label = { Text("Hombre") },
                        enabled = physicalData.sex != Sex.Male
                    )
                    ElevatedAssistChip(
                        onClick = { onSexChange(Sex.Female) },
                        label = { Text("Mujer") },
                        enabled = physicalData.sex != Sex.Female
                    )
                }

                // Farmacología
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Uso de farmacología",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (physicalData.usesPharma) "Con farmacología" else "Natural",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = physicalData.usesPharma,
                        onCheckedChange = onPharmaUsageChange
                    )
                }

                // Edad + Altura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var ageText by remember(physicalData.age) {
                        mutableStateOf(physicalData.age?.toString().orEmpty())
                    }
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() } || value.isEmpty()) {
                                ageText = value
                                onAgeChange(value.toIntOrNull())
                            }
                        },
                        label = { Text("Edad (años)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    var heightText by remember(physicalData.height) {
                        mutableStateOf(
                            physicalData.height?.let {
                                if (it % 1f == 0f) it.toInt().toString() else it.toString()
                            }.orEmpty()
                        )
                    }
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { value ->
                            val normalized = value.replace(',', '.')
                            if (normalized.isBlank() || numberRegex.matches(normalized)) {
                                heightText = value
                                val parsed = normalized.toFloatOrNull()
                                onHeightChange(parsed)
                            }
                        },
                        label = { Text("Altura (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Altura y edad se configuran una vez y solo se ajustan si realmente cambian.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bloque B – Medidas corporales
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Medidas (cm) – en ayunas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeasurementField(
                            label = "Cuello",
                            value = measurements.neck,
                            onValueChange = { onMeasurementChange("neck", it) }
                        )
                        MeasurementField(
                            label = "Cintura",
                            value = measurements.waist,
                            onValueChange = { onMeasurementChange("waist", it) }
                        )
                        MeasurementField(
                            label = "Cadera",
                            value = measurements.hip,
                            onValueChange = { onMeasurementChange("hip", it) }
                        )
                        MeasurementField(
                            label = "Pecho",
                            value = measurements.chest,
                            onValueChange = { onMeasurementChange("chest", it) }
                        )
                        MeasurementField(
                            label = "Muñeca",
                            value = measurements.wrist,
                            onValueChange = { onMeasurementChange("wrist", it) }
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeasurementField(
                            label = "Muslo",
                            value = measurements.thigh,
                            onValueChange = { onMeasurementChange("thigh", it) }
                        )
                        MeasurementField(
                            label = "Pantorrilla",
                            value = measurements.calf,
                            onValueChange = { onMeasurementChange("calf", it) }
                        )
                        MeasurementField(
                            label = "Bíceps relajado",
                            value = measurements.relaxedBiceps,
                            onValueChange = { onMeasurementChange("relaxedBiceps", it) }
                        )
                        MeasurementField(
                            label = "Bíceps flexionado",
                            value = measurements.flexedBiceps,
                            onValueChange = { onMeasurementChange("flexedBiceps", it) }
                        )
                        MeasurementField(
                            label = "Antebrazo",
                            value = measurements.forearm,
                            onValueChange = { onMeasurementChange("forearm", it) }
                        )
                    }
                }

                MeasurementField(
                    label = "Pie",
                    value = measurements.foot,
                    onValueChange = { onMeasurementChange("foot", it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Las medidas se guardan automáticamente. Úsalas siempre en ayunas y con la misma cinta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MeasurementField(
    label: String,
    value: Float?,
    onValueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberRegex = remember { Regex("^\\d*(?:[\\.,]\\d{0,2})?$") }

    var text by remember(value) {
        mutableStateOf(
            value?.let {
                if (it % 1f == 0f) it.toInt().toString() else String.format("%.1f", it)
            }.orEmpty()
        )
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            val normalized = newText.replace(',', '.')
            if (normalized.isBlank() || numberRegex.matches(normalized)) {
                text = newText
                val parsed = normalized.toFloatOrNull()
                onValueChange(parsed)
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        modifier = modifier
    )
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Resumen actual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KpiColumn(uiState.kpiSummary.bodyFat, "Grasa corporal")
                KpiColumn(uiState.kpiSummary.fatFreeMass, "Masa magra")
                KpiColumn(uiState.kpiSummary.calories, "Calorías objetivo (fase)")
                KpiColumn(uiState.kpiSummary.weightVelocity, "Velocidad de peso")
            }
        }
    }
}

@Composable
private fun KpiColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Cuerpo y composición",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Peso: ${uiState.composition.weight}")
                    Text("Altura: ${uiState.composition.height}")
                    Text("Edad: ${uiState.composition.age}")
                    Text("IMC: ${uiState.composition.bmi}")
                    Text("FFMI: ${uiState.composition.ffmi}")
                }
                VerticalDivider()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Metabolismo y actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("BMR (Katch): ${uiState.metabolism.bmrKatch}")
            Text("BMR (Mifflin): ${uiState.metabolism.bmrMifflin}")
            Text("Gasto total estimado (TDEE): ${uiState.metabolism.tdeeTheoretical}")
            Text("Factor actividad (TDEE / BMR Mifflin): ${uiState.metabolism.factor}")
            Text("Pasos: ${uiState.activity.steps} → ${uiState.activity.stepsKcal.toInt()} kcal")
            Text("Cardio: ${uiState.activity.cardioMinutes} min → ${uiState.activity.cardioKcal.toInt()} kcal")
            Text(
                "Entrenamiento: ${if (uiState.activity.trained) "Sí" else "No"} → ${
                    uiState.activity.gymKcal.toInt()
                } kcal"
            )
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Calorías y macros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Calorías objetivo según fase: ${uiState.macros.calories}")
            Text("Proteínas (por FFM): ${uiState.macros.protein}")
            Text("Grasas (por FFM): ${uiState.macros.fat}")
            Text("Carbohidratos ajustados: ${uiState.macros.carbs}")
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Objetivos de físico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            uiState.goals.targets.forEach { target ->
                Text(target)
            }
            if (uiState.goals.summary.isNotEmpty()) {
                Text(
                    uiState.goals.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
