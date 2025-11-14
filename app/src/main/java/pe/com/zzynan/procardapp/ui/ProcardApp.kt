package pe.com.zzynan.procardapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.com.zzynan.procardapp.ui.components.ProcardBottomNav
import pe.com.zzynan.procardapp.ui.components.ProcardTopBar
import pe.com.zzynan.procardapp.ui.navigation.ProcardNavHost
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen
import pe.com.zzynan.procardapp.ui.theme.ProcardTheme

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/ProcardApp.kt
@Composable
fun ProcardApp() {
    // Leer el tema del sistema desde un contexto composable
    val systemDarkTheme = isSystemInDarkTheme()
    // Controla el estado del tema claro/oscuro y lo guarda tras recomposiciones y recreaciones.
    var isDarkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }
    // Controla el nombre del usuario mostrado en la barra superior y lo persiste tras recreaciones.
    var userName by rememberSaveable { mutableStateOf("Atleta") }
    // Controla la navegación entre pantallas usando Navigation Compose.
    val navController = rememberNavController()
    val screens = ProcardScreen.allScreens
    // Obtiene la entrada actual del back stack para reaccionar a cambios de ruta.
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Determina la pantalla actual a partir de la ruta activa en el NavHost.
    val currentScreen = screens.firstOrNull { screen ->
        screen.route == backStackEntry?.destination?.route
    } ?: ProcardScreen.Registro

    ProcardTheme(darkTheme = isDarkTheme) {
        // Estructura raíz de la aplicación que organiza barra superior, contenido y barra inferior.
        Scaffold(
            topBar = {
                // Barra superior personalizada con saludo, título dinámico y control de tema.
                ProcardTopBar(
                    currentScreen = currentScreen,
                    userName = userName,
                    onUserNameChange = { updatedName -> userName = updatedName },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            },
            bottomBar = {
                // Barra de navegación inferior que permite cambiar entre pantallas principales.
                ProcardBottomNav(
                    screens = screens,
                    currentDestination = currentScreen,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            // Evita apilar destinos duplicados y restaura estado guardado.
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Contenedor de navegación principal respetando el padding del Scaffold.
            ProcardNavHost(
                navController = navController,
                padding = innerPadding
            )
        }
    }
}
