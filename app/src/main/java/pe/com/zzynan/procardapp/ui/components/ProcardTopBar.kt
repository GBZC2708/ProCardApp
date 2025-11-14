package pe.com.zzynan.procardapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/components/ProcardTopBar.kt
@Composable
fun ProcardTopBar(
    currentScreen: ProcardScreen,
    userName: String,
    onUserNameChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Controla la visibilidad del diálogo para editar el nombre del usuario.
    var isNameDialogVisible by rememberSaveable { mutableStateOf(false) }
    // Almacena temporalmente el valor del nombre dentro del diálogo antes de confirmarlo.
    var pendingName by rememberSaveable { mutableStateOf(userName) }
    // Formatea la fecha actual en español siguiendo el estilo requerido.
    val formattedDate = rememberSaveable {
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val today = LocalDate.now()
        formatter.format(today).replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(Locale("es", "ES")) else character.toString()
        }
    }

    // Mantiene sincronizado el texto editable con el nombre actual al abrir el diálogo.
    LaunchedEffect(isNameDialogVisible) {
        if (isNameDialogVisible) {
            pendingName = userName
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bloque izquierdo con fecha y saludo interactivo.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { isNameDialogVisible = true }) {
                    Text(
                        text = stringResource(id = R.string.greeting_format, userName),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bloque central con título y descripción de la pantalla actual.
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = currentScreen.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentScreen.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Bloque derecho con el interruptor de tema claro/oscuro.
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkTheme) {
                            stringResource(id = R.string.light_theme_content_description)
                        } else {
                            stringResource(id = R.string.dark_theme_content_description)
                        },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (isNameDialogVisible) {
        // Diálogo que permite editar y guardar el nombre del usuario.
        AlertDialog(
            onDismissRequest = { isNameDialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUserNameChange(pendingName.ifBlank { userName })
                        isNameDialogVisible = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.save_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { isNameDialogVisible = false }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.edit_name_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(id = R.string.edit_name_support),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = pendingName,
                        onValueChange = { updatedName -> pendingName = updatedName },
                        singleLine = true
                    )
                }
            }
        )
    }
}
