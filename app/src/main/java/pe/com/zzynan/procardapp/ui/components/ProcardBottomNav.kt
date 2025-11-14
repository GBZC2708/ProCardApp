package pe.com.zzynan.procardapp.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/components/ProcardBottomNav.kt
@Composable
fun ProcardBottomNav(
    screens: List<ProcardScreen>,
    currentDestination: ProcardScreen,
    onNavigate: (ProcardScreen) -> Unit
) {
    // Barra de navegación inferior que muestra únicamente iconos y gestiona la pantalla activa.
    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = screen.route == currentDestination.route,
                onClick = { onNavigate(screen) },
                icon = { screen.icon() },
                label = {
                    Text(text = stringResource(id = screen.labelRes))
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = screen.selectedIconColor,
                    selectedTextColor = screen.selectedIconColor,
                    indicatorColor = screen.indicatorColor,
                    unselectedIconColor = screen.unselectedIconColor,
                    unselectedTextColor = screen.unselectedIconColor
                )
            )
        }
    }
}
