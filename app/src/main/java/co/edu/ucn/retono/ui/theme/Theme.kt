package co.edu.ucn.retono.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = Foliage,
    onPrimary = Paper,
    primaryContainer = Moss,
    onPrimaryContainer = Ink,
    secondary = MossDark,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = TextMute,
    error = Clay,
    onError = Paper,
    outline = PaperDim
)

private val EsquemaOscuro = darkColorScheme(
    primary = Moss,
    onPrimary = Ink,
    primaryContainer = FoliageDark,
    onPrimaryContainer = Paper,
    secondary = Foliage,
    onSecondary = Paper,
    background = Ink,
    onBackground = Paper,
    surface = InkSoft,
    onSurface = Paper,
    surfaceVariant = InkSoft,
    onSurfaceVariant = PaperDim,
    error = Clay,
    onError = Paper,
    outline = TextMute
)

/**
 * No se usa color dinámico de Material You a propósito: los colores de estado de
 * sincronización deben ser estables entre dispositivos. Que el ocre de PENDIENTE
 * cambie según el fondo de pantalla del técnico sería un defecto, no una función.
 */
@Composable
fun RetonoTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = RetonoTypography,
        content = content
    )
}
