package pe.com.zzynan.procardapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.com.zzynan.procardapp.ui.components.ProcardBottomNav
import pe.com.zzynan.procardapp.ui.components.ProcardTopBar
import pe.com.zzynan.procardapp.ui.navigation.ProcardNavHost
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen
import pe.com.zzynan.procardapp.ui.theme.ProcardTheme
import pe.com.zzynan.procardapp.ui.viewmodel.TopBarViewModel

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/ProcardApp.kt
@Composable
fun ProcardApp() {
    // Leer el tema del sistema desde un contexto composable
    val systemDarkTheme = isSystemInDarkTheme()
    // Controla el estado del tema claro/oscuro y lo guarda tras recomposiciones y recreaciones.
    var isDarkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }
    val navController = rememberNavController()
    val screens = ProcardScreen.allScreens
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = screens.firstOrNull { screen ->
        screen.route == backStackEntry?.destination?.route
    } ?: ProcardScreen.Registro

    val context = LocalContext.current
    val topBarViewModel: TopBarViewModel = viewModel(
        factory = TopBarViewModel.provideFactory(context)
    )
    val topBarUiState = topBarViewModel.uiState.collectAsStateWithLifecycle()

    ProcardTheme(darkTheme = isDarkTheme) {
        // Estructura raíz de la aplicación que organiza barra superior, contenido y barra inferior.
        Scaffold(
            topBar = {
                // Barra superior personalizada con saludo, título dinámico y control de tema.
                ProcardTopBar(
                    currentScreen = currentScreen,
                    userName = topBarUiState.value.displayName,
                    onUserNameChange = topBarViewModel::onUserNameChange,
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
