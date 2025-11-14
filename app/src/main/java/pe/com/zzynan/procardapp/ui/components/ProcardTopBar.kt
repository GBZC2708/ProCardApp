package pe.com.zzynan.procardapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProcardTopBar(
    currentScreen: ProcardScreen,
    userName: String,
    onUserNameChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var isNameDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingName by rememberSaveable { mutableStateOf(userName) }

    // Fecha compacta en 2 líneas
    val (dayOfWeek, fullDate) = rememberSaveable {
        val today = LocalDate.now()
        val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES"))
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

        val rawDay = dayFormatter.format(today)
        val capitalizedDay = rawDay.replaceFirstChar { it.titlecase(Locale("es", "ES")) }

        capitalizedDay to dateFormatter.format(today)
    }

    LaunchedEffect(isNameDialogVisible) {
        if (isNameDialogVisible) {
            pendingName = userName
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // IZQUIERDA: Botón tema
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // CENTRO: Greeting más grande
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { isNameDialogVisible = true }) {
                    Text(
                        text = stringResource(id = R.string.greeting_format, userName),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // DERECHA: Fecha compacta
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = fullDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

// DIALOGO PARA EDITAR NOMBRE (súper compacto y estrecho)
    if (isNameDialogVisible) {
        AlertDialog(
            onDismissRequest = { isNameDialogVisible = false },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = {
                            onUserNameChange(pendingName.ifBlank { userName })
                            isNameDialogVisible = false
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.save_action),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.edit_name_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = pendingName,
                        onValueChange = { pendingName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.7f),    // 👈 más estrecho
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(0.75f) // 👈 diálogo más angosto aún
        )
    }

}
