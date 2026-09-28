package com.picobeam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PicoColors = darkColorScheme(
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

@Composable
fun PicoBeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PicoColors, content = content)
}