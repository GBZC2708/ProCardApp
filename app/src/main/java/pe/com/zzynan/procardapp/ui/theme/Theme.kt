package pe.com.zzynan.procardapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// File: app/src/main/java/pe/com/zzynan/procardapp/ui/theme/Theme.kt

// Paleta Material 3 para el modo oscuro.
val ProcardDarkColorScheme = darkColorScheme(
    primary = OlympiaRed, // Color principal para botones, íconos activos y resaltados.
    onPrimary = PureWhite, // Texto en botones/elementos primarios en modo oscuro.
    primaryContainer = DarkPrimaryDark, // Contenedor primario (chips, cards enfatizados) en modo oscuro.
    onPrimaryContainer = PureWhite, // Texto sobre contenedores primarios oscuros.
    secondary = SteelSilver, // Texto secundario en modo oscuro.
    onSecondary = DarkBackground,
    background = DarkBackground, // Fondo general de la app en modo oscuro.
    onBackground = PureWhite, // Texto principal sobre el fondo oscuro.
    surface = DarkCard, // Superficies elevadas como app bar y tarjetas.
    onSurface = PureWhite, // Texto sobre superficies oscuras.
    surfaceVariant = Charcoal, // Superficies alternativas para secciones diferenciadas.
    onSurfaceVariant = SteelSilver, // Texto secundario sobre superficies alternativas.
    outline = DarkBorder, // Bordes y divisores en modo oscuro.
    tertiary = DarkPrimaryLight, // Colores de énfasis complementarios (por ejemplo, íconos del bottom nav).
    onTertiary = DarkBackground
)

// Paleta Material 3 para el modo claro.
val ProcardLightColorScheme = lightColorScheme(
    primary = OlympiaRed, // Color primario para acciones principales en modo claro.
    onPrimary = PureWhite, // Texto sobre botones primarios en modo claro.
    primaryContainer = LightPrimaryLight, // Contenedores resaltados como app bar o tarjetas especiales.
    onPrimaryContainer = OlympiaBlack, // Texto dentro de contenedores primarios claros.
    secondary = LightSecondaryText, // Texto secundario y etiquetas en modo claro.
    onSecondary = LightBackground,
    background = LightBackground, // Fondo general blanco puro.
    onBackground = OlympiaBlack, // Texto principal sobre fondo claro.
    surface = LightSurface, // Superficies de app bar, tarjetas y paneles.
    onSurface = OlympiaBlack, // Texto principal sobre superficies claras.
    surfaceVariant = Platinum, // Superficies alternativas para secciones diferenciadas.
    onSurfaceVariant = IronGray, // Texto secundario sobre superficies alternativas claras.
    outline = LightBorder, // Bordes y divisores en modo claro.
    tertiary = LightPrimaryDark, // Tonalidad complementaria para íconos o indicadores activos.
    onTertiary = PureWhite
)

@Composable
fun ProcardTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Selecciona el esquema de colores acorde al estado del tema.
    val colorScheme = if (darkTheme) ProcardDarkColorScheme else ProcardLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProcardTypography,
        content = content
    )
}
