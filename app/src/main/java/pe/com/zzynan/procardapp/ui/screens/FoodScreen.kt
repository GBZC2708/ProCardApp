package pe.com.zzynan.procardapp.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.model.DailyFoodEntryUiModel
import pe.com.zzynan.procardapp.ui.model.DailyNutritionSummaryUiModel
import pe.com.zzynan.procardapp.ui.model.FoodItemUiModel
import pe.com.zzynan.procardapp.ui.model.FoodTab
import pe.com.zzynan.procardapp.ui.viewmodel.FoodViewModel

@Composable
fun FoodScreen(
    userName: String,
    onBackClick: () -> Unit,
    viewModel: FoodViewModel = viewModel(factory = FoodViewModel.provideFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputDialogState by remember { mutableStateOf<InputDialogState?>(null) }
    var isAddEntryDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(userName) {
        viewModel.setActiveUser(userName)
        viewModel.setActiveDate(LocalDate.now())
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = stringResource(id = R.string.food_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.Restaurant, contentDescription = stringResource(id = R.string.food_screen_title))
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.currentTab == FoodTab.TODAY_PLAN) {
                FloatingActionButton(onClick = { isAddEntryDialogVisible = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.food_add_entry_cd))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FoodTabs(
                selectedTab = uiState.currentTab,
                onTabSelected = viewModel::onTabSelected
            )
            when (uiState.currentTab) {
                FoodTab.CATALOG -> FoodCatalogSection(
                    items = uiState.catalog,
                    onAddFoodClick = viewModel::onAddFoodClicked,
                    onEditNameClick = { item ->
                        inputDialogState = InputDialogState.Text(
                            title = stringResource(id = R.string.food_edit_name),
                            initialValue = item.name,
                            onConfirmText = { value -> viewModel.onEditName(item.id, value) }
                        )
                    },
                    onEditBaseAmountClick = { item ->
                        inputDialogState = InputDialogState.Number(
                            title = stringResource(id = R.string.food_edit_base_amount),
                            initialValue = item.baseAmount,
                            onConfirmValue = { value -> viewModel.onEditBaseAmount(item.id, value) }
                        )
                    },
                    onEditBaseUnitClick = { item ->
                        inputDialogState = InputDialogState.Text(
                            title = stringResource(id = R.string.food_edit_base_unit),
                            initialValue = item.baseUnit,
                            onConfirmText = { value -> viewModel.onEditBaseUnit(item.id, value) }
                        )
                    },
                    onEditProteinClick = { item ->
                        inputDialogState = InputDialogState.Number(
                            title = stringResource(id = R.string.food_edit_protein),
                            initialValue = item.protein,
                            onConfirmValue = { value -> viewModel.onEditProtein(item.id, value) }
                        )
                    },
                    onEditFatClick = { item ->
                        inputDialogState = InputDialogState.Number(
                            title = stringResource(id = R.string.food_edit_fat),
                            initialValue = item.fat,
                            onConfirmValue = { value -> viewModel.onEditFat(item.id, value) }
                        )
                    },
                    onEditCarbClick = { item ->
                        inputDialogState = InputDialogState.Number(
                            title = stringResource(id = R.string.food_edit_carb),
                            initialValue = item.carb,
                            onConfirmValue = { value -> viewModel.onEditCarb(item.id, value) }
                        )
                    }
                )
                FoodTab.TODAY_PLAN -> TodayFoodPlanSection(
                    entries = uiState.todayEntries,
                    summary = uiState.todaySummary,
                    isCopyFromYesterdayVisible = uiState.isCopyFromYesterdayVisible,
                    onAddEntryClick = { isAddEntryDialogVisible = true },
                    onConsumedAmountClick = { entry ->
                        inputDialogState = InputDialogState.Number(
                            title = stringResource(id = R.string.food_edit_consumed_amount),
                            initialValue = entry.consumedAmount,
                            onConfirmValue = { value -> viewModel.onConsumedAmountEdited(entry.id, value) }
                        )
                    },
                    onCopyFromYesterdayClick = viewModel::onCopyFromYesterdayClicked,
                    onRemoveEntryClick = { entry -> viewModel.onRemoveEntry(entry.id) }
                )
            }
        }
    }

    if (isAddEntryDialogVisible) {
        FoodPickerDialog(
            items = uiState.catalog,
            onDismiss = { isAddEntryDialogVisible = false },
            onItemSelected = { item ->
                viewModel.onAddEntryForFood(item.id)
                isAddEntryDialogVisible = false
            }
        )
    }

    inputDialogState?.let { state ->
        FoodInputDialog(
            state = state,
            onDismiss = { inputDialogState = null }
        )
    }
}

@Composable
private fun FoodTabs(selectedTab: FoodTab, onTabSelected: (FoodTab) -> Unit) {
    val tabs = listOf(FoodTab.TODAY_PLAN, FoodTab.CATALOG)
    TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
        tabs.forEach { tab ->
            val icon = if (tab == FoodTab.CATALOG) Icons.Filled.Restaurant else Icons.Filled.Today
            val contentDescription = if (tab == FoodTab.CATALOG) {
                stringResource(id = R.string.food_catalog_tab_cd)
            } else {
                stringResource(id = R.string.food_plan_tab_cd)
            }
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {},
                icon = { Icon(imageVector = icon, contentDescription = contentDescription) }
            )
        }
    }
}

@Composable
fun FoodCatalogSection(
    items: List<FoodItemUiModel>,
    onAddFoodClick: () -> Unit,
    onEditNameClick: (FoodItemUiModel) -> Unit,
    onEditBaseAmountClick: (FoodItemUiModel) -> Unit,
    onEditBaseUnitClick: (FoodItemUiModel) -> Unit,
    onEditProteinClick: (FoodItemUiModel) -> Unit,
    onEditFatClick: (FoodItemUiModel) -> Unit,
    onEditCarbClick: (FoodItemUiModel) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.clickable { onEditNameClick(item) },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = item.baseAmount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable { onEditBaseAmountClick(item) }
                                )
                                Text(
                                    text = item.baseUnit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable { onEditBaseUnitClick(item) }
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MacroChip(
                                label = item.protein,
                                icon = Icons.Filled.FitnessCenter,
                                onClick = { onEditProteinClick(item) }
                            )
                            MacroChip(
                                label = item.fat,
                                icon = Icons.Filled.Opacity,
                                onClick = { onEditFatClick(item) }
                            )
                            MacroChip(
                                label = item.carb,
                                icon = Icons.Filled.Grain,
                                onClick = { onEditCarbClick(item) }
                            )
                            MacroChip(
                                label = item.calories,
                                icon = Icons.Filled.LocalFireDepartment,
                                enabled = false
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        FloatingActionButton(
            onClick = onAddFoodClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.food_add_item_cd))
        }
    }
}

@Composable
fun TodayFoodPlanSection(
    entries: List<DailyFoodEntryUiModel>,
    summary: DailyNutritionSummaryUiModel?,
    isCopyFromYesterdayVisible: Boolean,
    onAddEntryClick: () -> Unit,
    onConsumedAmountClick: (DailyFoodEntryUiModel) -> Unit,
    onCopyFromYesterdayClick: () -> Unit,
    onRemoveEntryClick: (DailyFoodEntryUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onAddEntryClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.food_add_entry_cd))
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.foodName,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.consumedAmount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable { onConsumedAmountClick(entry) }
                                )
                                Text(
                                    text = " ${entry.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { onRemoveEntryClick(entry) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MacroChip(
                                label = entry.protein,
                                icon = Icons.Filled.FitnessCenter,
                                enabled = false
                            )
                            MacroChip(
                                label = entry.fat,
                                icon = Icons.Filled.Opacity,
                                enabled = false
                            )
                            MacroChip(
                                label = entry.carb,
                                icon = Icons.Filled.Grain,
                                enabled = false
                            )
                            MacroChip(
                                label = entry.calories,
                                icon = Icons.Filled.LocalFireDepartment,
                                enabled = false
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            summary?.let {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = it.protein, style = MaterialTheme.typography.bodySmall)
                        Text(text = it.fat, style = MaterialTheme.typography.bodySmall)
                        Text(text = it.carb, style = MaterialTheme.typography.bodySmall)
                        Text(text = it.calories, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (isCopyFromYesterdayVisible) {
                IconButton(onClick = onCopyFromYesterdayClick) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.food_copy_cd))
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    AssistChip(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        label = {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    )
}

@Composable
private fun FoodInputDialog(state: InputDialogState, onDismiss: () -> Unit) {
    var text by remember(state) { mutableStateOf(state.initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                when (state) {
                    is InputDialogState.Number -> state.onConfirmValue(text.parseNumber())
                    is InputDialogState.Text -> state.onConfirmText(text)
                }
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.food_dialog_confirm))
            }
        },
        title = { Text(text = state.title) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = state.keyboardType)
            )
        }
    )
}

@Composable
private fun FoodPickerDialog(
    items: List<FoodItemUiModel>,
    onDismiss: () -> Unit,
    onItemSelected: (FoodItemUiModel) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                        Text(text = item.calories, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    )
}

private sealed class InputDialogState(
    val title: String,
    val initialValue: String,
    val keyboardType: KeyboardType
) {
    class Number(title: String, initialValue: String, val onConfirmValue: (Double) -> Unit) :
        InputDialogState(title, initialValue, KeyboardType.Decimal)

    class Text(title: String, initialValue: String, val onConfirmText: (String) -> Unit) :
        InputDialogState(title, initialValue, KeyboardType.Text)
}

private fun String.parseNumber(): Double {
    val normalized = replace(",", ".").substringBefore(" ")
    return normalized.toDoubleOrNull() ?: 0.0
}
