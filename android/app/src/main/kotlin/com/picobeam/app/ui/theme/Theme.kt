package com.picobeam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00F0FF),
    onPrimary = Color(0xFF06282C),
    primaryContainer = Color(0xFF0A3A40),
    onPrimaryContainer = Color(0xFFB6F8FF),
    secondary = Color(0xFF9A6CFF),
    onSecondary = Color(0xFF160630),
    secondaryContainer = Color(0xFF2A1950),
    onSecondaryContainer = Color(0xFFE0D6FF),
    background = Color(0xFF0B0F17),
    onBackground = Color(0xFFE7EDF7),
    surface = Color(0xFF111826),
    onSurface = Color(0xFFE7EDF7),
    surfaceVariant = Color(0xFF1C2740),
    onSurfaceVariant = Color(0xFF8A94A6),
    outline = Color(0xFF2A3550),
    error = Color(0xFFFF5C5C),
    onError = Color(0xFF2A0000),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF007E92),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB6ECF4),
    onPrimaryContainer = Color(0xFF00363D),
    secondary = Color(0xFF5B21FF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6DCFF),
    onSecondaryContainer = Color(0xFF230466),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF101828),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFE6EDF6),
    onSurfaceVariant = Color(0xFF5B6675),
    outline = Color(0xFFCFD9E6),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun PicoBeamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}