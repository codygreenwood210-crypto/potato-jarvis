package com.potato.jarvis.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PotatoBlack = Color(0xFF090A0F)
private val PotatoPanel = Color(0xFF12151C)
private val PotatoPanelAlt = Color(0xFF1A1E27)
private val PotatoGreen = Color(0xFF10A37F)
private val PotatoGreenBright = Color(0xFF19C37D)
private val PotatoText = Color(0xFFF4F7F2)
private val PotatoMuted = Color(0xFF9AA3AE)
private val PotatoError = Color(0xFFFF6B6B)

private val PotatoDarkColors = darkColorScheme(
    primary = PotatoGreen,
    onPrimary = PotatoBlack,
    primaryContainer = Color(0xFF0D4A3C),
    onPrimaryContainer = Color(0xFF6FE6C1),
    secondary = PotatoGreenBright,
    background = PotatoBlack,
    onBackground = PotatoText,
    surface = PotatoPanel,
    onSurface = PotatoText,
    surfaceVariant = PotatoPanelAlt,
    onSurfaceVariant = PotatoMuted,
    error = PotatoError,
)

@Composable
fun PotatoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PotatoDarkColors,
        content = content,
    )
}
