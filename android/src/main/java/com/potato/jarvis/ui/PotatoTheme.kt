package com.potato.jarvis.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val PotatoDarkColors = darkColorScheme(
    primary = PotatoVisuals.Brand,
    onPrimary = PotatoVisuals.Black,
    primaryContainer = Color(0xFF0D4A3C),
    onPrimaryContainer = PotatoVisuals.BrandSoft,
    secondary = PotatoVisuals.BrandBright,
    onSecondary = PotatoVisuals.Black,
    secondaryContainer = Color(0xFF123E36),
    onSecondaryContainer = Color(0xFFB4F5DF),
    background = PotatoVisuals.Background,
    onBackground = PotatoVisuals.TextPrimary,
    surface = PotatoVisuals.Surface,
    onSurface = PotatoVisuals.TextPrimary,
    surfaceVariant = PotatoVisuals.SurfaceSoft,
    onSurfaceVariant = PotatoVisuals.TextSecondary,
    outline = PotatoVisuals.Border,
    outlineVariant = Color(0xFF24363D),
    error = PotatoVisuals.Error,
    onError = Color(0xFF290000),
)

private val PotatoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
)

private val PotatoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(PotatoVisuals.SmallRadius),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(PotatoVisuals.ControlRadius),
    extraLarge = RoundedCornerShape(PotatoVisuals.CardRadius),
)

@Composable
fun PotatoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PotatoDarkColors,
        typography = PotatoTypography,
        shapes = PotatoShapes,
        content = content,
    )
}
