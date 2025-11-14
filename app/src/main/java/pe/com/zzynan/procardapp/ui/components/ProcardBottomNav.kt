package pe.com.zzynan.procardapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.com.zzynan.procardapp.ui.navigation.ProcardScreen

@Composable
fun ProcardBottomNav(
    screens: List<ProcardScreen>,
    currentDestination: ProcardScreen,
    onNavigate: (ProcardScreen) -> Unit
) {
    Box(
        modifier = Modifier.navigationBarsPadding() // 👈 esto empuja TODO por encima de los 3 botones
    ) {
        val navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationBar(
            modifier = Modifier.height(62.dp)        // barra compacta pero visible
        ) {
            screens.forEach { screen ->
                val selected = screen.route == currentDestination.route

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(screen) },
                    icon = {
                        screen.Icon(selected = selected)
                    },
                    label = {
                        Text(text = stringResource(id = screen.labelRes))
                    },
                    alwaysShowLabel = false,
                    colors = navigationBarItemColors
                )
            }
        }
    }
}
