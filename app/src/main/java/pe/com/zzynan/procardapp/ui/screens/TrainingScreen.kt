package pe.com.zzynan.procardapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pe.com.zzynan.procardapp.ui.model.ExerciseEditorUiModel
import pe.com.zzynan.procardapp.ui.model.RoutineDayUiModel
import pe.com.zzynan.procardapp.ui.model.RoutineExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.SessionExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingDayDialogState
import pe.com.zzynan.procardapp.ui.model.TrainingDayStatusUi
import pe.com.zzynan.procardapp.ui.model.TrainingDayUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingSessionUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingTab
import pe.com.zzynan.procardapp.ui.model.TrainingUiState
import pe.com.zzynan.procardapp.ui.model.WorkoutExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.WorkoutSetUiModel

@Composable
fun EntrenamientoScreen(
    uiState: TrainingUiState,
    finishDialogVisible: Boolean,
    onSelectTab: (TrainingTab) -> Unit,
    onOpenAddExercise: () -> Unit,
    onEditExercise: (WorkoutExerciseUiModel) -> Unit,
    onToggleExerciseActive: (Int) -> Unit,
    onExerciseEditorNameChange: (String) -> Unit,
    onExerciseEditorGroupChange: (String) -> Unit,
    onDismissExerciseEditor: () -> Unit,
    onConfirmExerciseEditor: () -> Unit,
    onOpenRoutineDialog: () -> Unit,
    onDismissRoutineDialog: () -> Unit,
    onRoutineLabelChange: (Int, String) -> Unit,
    onAddRoutineExercise: (Int, Int) -> Unit,
    onRemoveRoutineExercise: (Int) -> Unit,
    onTrainingDaySelected: (TrainingDayUiModel) -> Unit,
    onDismissTrainingDayDialog: () -> Unit,
    onSoloVer: (Int, Int?) -> Unit,
    onStartTraining: (Int, Boolean) -> Unit,
    onCloseSession: () -> Unit,
    onSetWeightChange: (Int, String) -> Unit,
    onSetRepsChange: (Int, String) -> Unit,
    onToggleSetCompleted: (Int, Boolean) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onShowFinishDialog: () -> Unit,
    onDismissFinishDialog: () -> Unit,
    onConfirmFinish: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TrainingTabs(selected = uiState.selectedTab, onSelect = onSelectTab)
            when (uiState.selectedTab) {
                TrainingTab.Catalog -> CatalogTab(
                    exercises = uiState.exercises,
                    onAddExercise = onOpenAddExercise,
                    onEditExercise = onEditExercise,
                    onToggleExerciseActive = onToggleExerciseActive,
                    onOpenRoutine = onOpenRoutineDialog
                )
                TrainingTab.Train -> TrainingDaysTab(
                    trainingDays = uiState.trainingDays,
                    onDaySelected = onTrainingDaySelected
                )
            }
        }
    }

    ExerciseEditorDialog(
        uiModel = uiState.exerciseEditor,
        muscleGroups = uiState.muscleGroups,
        onNameChange = onExerciseEditorNameChange,
        onGroupChange = onExerciseEditorGroupChange,
        onDismiss = onDismissExerciseEditor,
        onConfirm = onConfirmExerciseEditor
    )

    RoutineDialog(
        isVisible = uiState.isRoutineDialogVisible,
        routineDays = uiState.routineDays,
        catalog = uiState.exercises,
        onDismiss = onDismissRoutineDialog,
        onLabelChange = onRoutineLabelChange,
        onAddExercise = onAddRoutineExercise,
        onRemoveExercise = onRemoveRoutineExercise
    )

    TrainingDayDialog(
        dialogState = uiState.trainingDayDialog,
        onDismiss = onDismissTrainingDayDialog,
        onSoloVer = onSoloVer,
        onStart = onStartTraining
    )

    TrainingSessionOverlay(
        uiModel = uiState.sessionUi,
        onClose = onCloseSession,
        onSetWeightChange = onSetWeightChange,
        onSetRepsChange = onSetRepsChange,
        onToggleSetCompleted = onToggleSetCompleted,
        onAddSet = onAddSet,
        onRemoveSet = onRemoveSet,
        onShowFinishDialog = onShowFinishDialog
    )

    if (finishDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissFinishDialog,
            title = { Text("Finalizar entrenamiento") },
            text = { Text("¿Deseas guardar este entrenamiento, detener el tiempo y marcar el día como completado?") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmFinish()
                }) {
                    Text("Guardar y finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissFinishDialog) {
                    Text("Seguir entrenando")
                }
            }
        )
    }
}

@Composable
private fun TrainingTabs(selected: TrainingTab, onSelect: (TrainingTab) -> Unit) {
    val tabs = listOf(TrainingTab.Catalog to "Catálogo", TrainingTab.Train to "Entrenar")
    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selected }.coerceAtLeast(0)) {
        tabs.forEachIndexed { index, pair ->
            Tab(
                selected = selected == pair.first,
                onClick = { onSelect(pair.first) },
                text = { Text(pair.second) }
            )
        }
    }
}

@Composable
private fun CatalogTab(
    exercises: List<WorkoutExerciseUiModel>,
    onAddExercise: () -> Unit,
    onEditExercise: (WorkoutExerciseUiModel) -> Unit,
    onToggleExerciseActive: (Int) -> Unit,
    onOpenRoutine: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onAddExercise) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Agregar ejercicio")
            }
            FilledTonalButton(onClick = onOpenRoutine) {
                Text("Rutina")
            }
        }
        if (exercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin ejercicios. Agrega tus movimientos favoritos.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(exercises, key = { it.id }) { exercise ->
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    exercise.muscleGroup,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!exercise.isActive) {
                                    Text(
                                        text = "Inactivo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            ExerciseMenu(
                                exercise = exercise,
                                onEdit = { onEditExercise(exercise) },
                                onToggleActive = { onToggleExerciseActive(exercise.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseMenu(
    exercise: WorkoutExerciseUiModel,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text(if (exercise.isActive) "Desactivar" else "Activar") },
                onClick = {
                    expanded = false
                    onToggleActive()
                }
            )
        }
    }
}

@Composable
private fun ExerciseEditorDialog(
    uiModel: ExerciseEditorUiModel,
    muscleGroups: List<String>,
    onNameChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!uiModel.isVisible) return
    var expanded by remember { mutableStateOf(false) }
    val group = uiModel.selectedGroup.ifEmpty { muscleGroups.firstOrNull().orEmpty() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiModel.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiModel.name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Column {
                    Text("Grupo muscular", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        enabled = true,
                        readOnly = true
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        muscleGroups.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onGroupChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(uiModel.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RoutineDialog(
    isVisible: Boolean,
    routineDays: List<RoutineDayUiModel>,
    catalog: List<WorkoutExerciseUiModel>,
    onDismiss: () -> Unit,
    onLabelChange: (Int, String) -> Unit,
    onAddExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    if (!isVisible) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Rutina semanal",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                routineDays.forEach { day ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = day.label,
                            onValueChange = { onLabelChange(day.id, it) },
                            label = { Text(dayTitle(day.dayOfWeek)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (day.exercises.isEmpty()) {
                            Text(
                                text = "Sin ejercicios",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            day.exercises.forEach { exercise ->
                                RoutineExerciseRow(exercise = exercise, onRemove = { onRemoveExercise(exercise.id) })
                            }
                        }
                        RoutineAddExerciseButton(
                            dayId = day.id,
                            catalog = catalog,
                            onAddExercise = onAddExercise
                        )
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun RoutineExerciseRow(exercise: RoutineExerciseUiModel, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name)
            Text(exercise.muscleGroup, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onRemove) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        }
    }
}

@Composable
private fun RoutineAddExerciseButton(
    dayId: Int,
    catalog: List<WorkoutExerciseUiModel>,
    onAddExercise: (Int, Int) -> Unit
) {
    var expanded by remember(dayId) { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(4.dp))
            Text("Agregar ejercicio")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            catalog.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        expanded = false
                        onAddExercise(dayId, exercise.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TrainingDaysTab(
    trainingDays: List<TrainingDayUiModel>,
    onDaySelected: (TrainingDayUiModel) -> Unit
) {
    if (trainingDays.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Configura tu rutina para comenzar a entrenar.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(trainingDays, key = { it.dayId }) { day ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onDaySelected(day) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(day.label, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = dayTitle(day.dayOfWeek),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(day.statusLabel, color = statusColor(day.status))
                }
            }
        }
    }
}

@Composable
private fun TrainingDayDialog(
    dialogState: TrainingDayDialogState?,
    onDismiss: () -> Unit,
    onSoloVer: (Int, Int?) -> Unit,
    onStart: (Int, Boolean) -> Unit
) {
    dialogState ?: return
    val title = dialogState.day.label
    val actionLabel = when (dialogState.status) {
        TrainingDayStatusUi.NOT_STARTED -> "Iniciar entrenamiento"
        TrainingDayStatusUi.IN_PROGRESS -> "Reanudar sesión"
        TrainingDayStatusUi.COMPLETED -> "Iniciar nueva sesión"
    }
    val forceNew = dialogState.status == TrainingDayStatusUi.COMPLETED
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(dialogState.statusMessage) },
        confirmButton = {
            TextButton(onClick = {
                onStart(dialogState.day.id, forceNew)
            }) { Text(actionLabel) }
        },
        dismissButton = {
            TextButton(onClick = {
                onSoloVer(dialogState.day.id, dialogState.sessionId)
            }) { Text("Solo ver") }
        }
    )
}

@Composable
private fun TrainingSessionOverlay(
    uiModel: TrainingSessionUiModel,
    onClose: () -> Unit,
    onSetWeightChange: (Int, String) -> Unit,
    onSetRepsChange: (Int, String) -> Unit,
    onToggleSetCompleted: (Int, Boolean) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onShowFinishDialog: () -> Unit
) {
    AnimatedVisibility(visible = uiModel.isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SessionTopBar(
                        title = uiModel.dayLabel,
                        timerText = uiModel.timerText,
                        onClose = onClose
                    )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiModel.exercises, key = { it.exerciseId }) { exercise ->
                        SessionExerciseCard(
                            exercise = exercise,
                            isReadOnly = uiModel.isReadOnly,
                            onSetWeightChange = onSetWeightChange,
                            onSetRepsChange = onSetRepsChange,
                            onToggleCompleted = onToggleSetCompleted,
                            onAddSet = onAddSet,
                            onRemoveSet = onRemoveSet
                        )
                    }
                }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(uiModel.statusText, style = MaterialTheme.typography.bodySmall)
                        if (uiModel.showFinishButton) {
                            Button(
                                onClick = onShowFinishDialog,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Text("Guardar y finalizar entrenamiento")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionTopBar(title: String, timerText: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(timerText, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }
    }
}

@Composable
private fun SessionExerciseCard(
    exercise: SessionExerciseUiModel,
    isReadOnly: Boolean,
    onSetWeightChange: (Int, String) -> Unit,
    onSetRepsChange: (Int, String) -> Unit,
    onToggleCompleted: (Int, Boolean) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontWeight = FontWeight.SemiBold)
                    Text(exercise.muscleGroup, style = MaterialTheme.typography.bodySmall)
                }
                if (!isReadOnly) {
                    Row {
                        IconButton(onClick = { onRemoveSet(exercise.exerciseId) }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                        }
                        IconButton(onClick = { onAddSet(exercise.exerciseId) }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
            exercise.sets.forEach { set ->
                SetRow(
                    set = set,
                    readOnly = !set.isEditable || isReadOnly,
                    onWeightChange = { onSetWeightChange(set.id, it) },
                    onRepsChange = { onSetRepsChange(set.id, it) },
                    onToggleCompleted = { onToggleCompleted(set.id, it) }
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: WorkoutSetUiModel,
    readOnly: Boolean,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onToggleCompleted: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text("${set.label} (${set.bestLabel})", fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = set.weightText,
            onValueChange = onWeightChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            enabled = !readOnly,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            label = { Text("Peso") }
        )
        Text("x", modifier = Modifier.padding(horizontal = 4.dp))
        OutlinedTextField(
            value = set.repsText,
            onValueChange = onRepsChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            enabled = !readOnly,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            label = { Text("Reps") }
        )
        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = if (readOnly) null else onToggleCompleted,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun dayTitle(dayIndex: Int): String = when (dayIndex) {
    0 -> "Lunes"
    1 -> "Martes"
    2 -> "Miércoles"
    3 -> "Jueves"
    4 -> "Viernes"
    5 -> "Sábado"
    else -> "Domingo"
}

@Composable
private fun statusColor(status: TrainingDayStatusUi): Color = when (status) {
    TrainingDayStatusUi.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
    TrainingDayStatusUi.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    TrainingDayStatusUi.COMPLETED -> MaterialTheme.colorScheme.tertiary
}
