package pe.com.zzynan.procardapp.ui.screens.supplement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.domain.model.SupplementTimeSlot
import pe.com.zzynan.procardapp.domain.model.label
import pe.com.zzynan.procardapp.ui.model.DailySupplementItemUi
import pe.com.zzynan.procardapp.ui.model.SupplementTab
import pe.com.zzynan.procardapp.ui.model.SupplementUiItem
import pe.com.zzynan.procardapp.ui.model.SupplementUiState
import pe.com.zzynan.procardapp.ui.viewmodel.SupplementViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import java.util.Locale
import kotlin.math.round

@Composable
fun SupplementScreen(
    viewModel: SupplementViewModel,
    onBack: () -> Unit // se mantiene por compatibilidad, pero no se usa
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var catalogDialogState by remember { mutableStateOf<SupplementUiItem?>(null) }
    var isAddCatalogDialogVisible by remember { mutableStateOf(false) }
    var addDailyDialogVisible by remember { mutableStateOf(false) }
    var amountEditEntryId by remember { mutableStateOf<Long?>(null) }
    var amountEditValue by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            when (uiState.selectedTab) {
                SupplementTab.CATALOG -> FloatingActionButton(onClick = { isAddCatalogDialogVisible = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }

                SupplementTab.DAILY_PLAN -> FloatingActionButton(onClick = { addDailyDialogVisible = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SupplementTabs(uiState.selectedTab, onTabSelected = viewModel::onTabSelected)
            when (uiState.selectedTab) {
                SupplementTab.CATALOG -> SupplementCatalogTab(
                    supplements = uiState.supplements,
                    onEdit = { catalogDialogState = it },
                    onDelete = viewModel::onDeleteSupplement
                )

                SupplementTab.DAILY_PLAN -> SupplementDailyPlanTab(
                    uiState = uiState,
                    onPreviousDay = viewModel::onPreviousDay,
                    onNextDay = viewModel::onNextDay,
                    onDeleteEntry = viewModel::onDeleteDailyEntry,
                    onAmountClick = { item ->
                        amountEditEntryId = item.id
                        amountEditValue = item.amount?.let { formatSupplementAmount(it) } ?: ""
                    }
                )
            }
        }
    }

    if (isAddCatalogDialogVisible || catalogDialogState != null) {
        SupplementEditDialog(
            initial = catalogDialogState,
            onDismiss = {
                catalogDialogState = null
                isAddCatalogDialogVisible = false
            },
            onSave = { name, amount, unit ->
                val target = catalogDialogState
                if (target == null) {
                    viewModel.onAddSupplement(name, amount, unit)
                } else {
                    viewModel.onEditSupplement(target.id, name, amount, unit)
                }
                catalogDialogState = null
                isAddCatalogDialogVisible = false
            }
        )
    }

    if (addDailyDialogVisible) {
        DailyEntryDialog(
            supplements = uiState.supplements,
            onDismiss = { addDailyDialogVisible = false },
            onSave = { supplementId, timeSlot, amount ->
                val unit = uiState.supplements.firstOrNull { it.id == supplementId }?.baseUnit
                viewModel.onAddDailyEntry(supplementId, timeSlot, amount, unit)
                addDailyDialogVisible = false
            }
        )
    }

    amountEditEntryId?.let { entryId ->
        AlertDialog(
            onDismissRequest = {
                amountEditEntryId = null
            },
            title = { Text(text = stringResource(id = R.string.supplement_edit_amount)) },
            text = {
                TextField(
                    value = amountEditValue,
                    onValueChange = { amountEditValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amountEditValue.toDoubleOrNull()
                    viewModel.onChangeDailyAmount(entryId, value)
                    amountEditEntryId = null
                }) {
                    Text(text = stringResource(id = R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { amountEditEntryId = null }) {
                    Text(text = stringResource(id = R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun SupplementTabs(selected: SupplementTab, onTabSelected: (SupplementTab) -> Unit) {
    val titles = listOf(R.string.supplement_tab_catalog, R.string.supplement_tab_daily_plan)
    TabRow(selectedTabIndex = selected.ordinal) {
        SupplementTab.values().forEachIndexed { index, tab ->
            Tab(
                selected = selected == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = stringResource(id = titles[index]),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun SupplementCatalogTab(
    supplements: List<SupplementUiItem>,
    onEdit: (SupplementUiItem) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(supplements, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(item) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                        val subtitle = listOfNotNull(item.baseAmount, item.baseUnit)
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString(" ")
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SupplementDailyPlanTab(
    uiState: SupplementUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onAmountClick: (DailySupplementItemUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateSelector(date = uiState.selectedDate, onPrevious = onPreviousDay, onNext = onNextDay)
        if (uiState.isUsingInheritedPlan) {
            AssistChip(
                onClick = {},
                label = { Text(text = stringResource(id = R.string.supplement_inherited_plan)) }
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.dailyPlan, key = { it.timeSlot }) { group ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = group.timeSlot.label(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    group.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = item.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = item.amountLabel,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable { onAmountClick(item) }
                                )
                            }
                            IconButton(onClick = { onDeleteEntry(item.id) }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onPrevious) { Text(text = "<") }
        Text(
            text = date.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onNext) { Text(text = ">") }
    }
}

@Composable
private fun SupplementEditDialog(
    initial: SupplementUiItem?,
    onDismiss: () -> Unit,
    onSave: (String, Double?, String?) -> Unit
) {
    val unitOptions = listOf(
        "mg",
        "mcg",
        "g",
        "IU",
        "ml",
        "cápsula",
        "tableta",
        "scoop",
        "gota"
    )

    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.baseAmount.orEmpty()) }
    var unit by remember {
        mutableStateOf(
            initial?.baseUnit?.takeIf { it in unitOptions }.orEmpty()
        )
    }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.supplement_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(id = R.string.supplement_field_name)) }
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(text = stringResource(id = R.string.supplement_field_base_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Dropdown manual para unidad base
                Column {
                    Text(
                        text = stringResource(id = R.string.supplement_field_base_unit),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { unitExpanded = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (unit.isBlank()) stringResource(id = R.string.supplement_field_base_unit)
                            else unit,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 4.dp),
                            color = if (unit.isBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option) },
                                onClick = {
                                    unit = option
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name.trim(), amount.toDoubleOrNull(), unit.trim().ifBlank { null })
            }) {
                Text(text = stringResource(id = R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyEntryDialog(
    supplements: List<SupplementUiItem>,
    onDismiss: () -> Unit,
    onSave: (Long, SupplementTimeSlot, Double?) -> Unit
) {
    var selectedSupplementId by remember { mutableStateOf(supplements.firstOrNull()?.id ?: 0L) }
    var selectedSlot by remember { mutableStateOf(SupplementTimeSlot.FASTED) }

    val selectedChipColors = AssistChipDefaults.assistChipColors(
        containerColor = MaterialTheme.colorScheme.primary,
        labelColor = MaterialTheme.colorScheme.onPrimary
    )
    val unselectedChipColors = AssistChipDefaults.assistChipColors()

    var supplementMenuExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.supplement_add_entry_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState)
            ) {
                // selector de suplemento
                Text(text = stringResource(id = R.string.supplement_field_select_supplement))
                val selectedSupplement =
                    supplements.firstOrNull { it.id == selectedSupplementId }

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { supplementMenuExpanded = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedSupplement?.name
                                ?: stringResource(id = R.string.supplement_field_select_supplement),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    DropdownMenu(
                        expanded = supplementMenuExpanded,
                        onDismissRequest = { supplementMenuExpanded = false }
                    ) {
                        supplements.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.name) },
                                onClick = {
                                    selectedSupplementId = item.id
                                    supplementMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // momento del día
                Text(text = stringResource(id = R.string.supplement_field_time_slot))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sections = listOf(
                        "Mañana" to listOf(
                            SupplementTimeSlot.FASTED,
                            SupplementTimeSlot.BEFORE_BREAKFAST,
                            SupplementTimeSlot.WITH_BREAKFAST,
                            SupplementTimeSlot.AFTER_BREAKFAST,
                            SupplementTimeSlot.MID_MORNING
                        ),
                        "Entreno" to listOf(
                            SupplementTimeSlot.PRE_WORKOUT,
                            SupplementTimeSlot.INTRA_WORKOUT,
                            SupplementTimeSlot.POST_WORKOUT
                        ),
                        "Mediodía / Tarde" to listOf(
                            SupplementTimeSlot.BEFORE_LUNCH,
                            SupplementTimeSlot.WITH_LUNCH,
                            SupplementTimeSlot.AFTER_LUNCH,
                            SupplementTimeSlot.AFTERNOON_SNACK
                        ),
                        "Noche" to listOf(
                            SupplementTimeSlot.BEFORE_DINNER,
                            SupplementTimeSlot.WITH_DINNER,
                            SupplementTimeSlot.AFTER_DINNER,
                            SupplementTimeSlot.BEFORE_SLEEP
                        )
                    )

                    sections.forEach { (title, items) ->
                        Text(text = title, style = MaterialTheme.typography.labelMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items.forEach { slot ->
                                AssistChip(
                                    onClick = {
                                        selectedSlot = slot
                                        if (selectedSupplementId != 0L) {
                                            val baseAmount = supplements
                                                .firstOrNull { it.id == selectedSupplementId }
                                                ?.baseAmount
                                                ?.toDoubleOrNull()

                                            onSave(selectedSupplementId, slot, baseAmount)
                                        }
                                        onDismiss()
                                    },
                                    label = { Text(text = slot.label()) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = if (selectedSlot == slot)
                                        selectedChipColors
                                    else
                                        unselectedChipColors
                                )
                            }
                        }
                    }
                }

                val unitLabel =
                    supplements.firstOrNull { it.id == selectedSupplementId }?.baseUnit
                unitLabel?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {}, // sin Guardar
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

private fun formatSupplementAmount(value: Double): String {
    // redondear a 3 decimales
    val rounded = round(value * 1000.0) / 1000.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        var s = String.format(Locale.US, "%.3f", rounded)
        // quitar ceros y punto sobrantes
        while (s.contains('.') && s.endsWith("0")) {
            s = s.dropLast(1)
        }
        if (s.endsWith(".")) {
            s = s.dropLast(1)
        }
        s
    }
}


