package com.potato.jarvis.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PotatoVisuals {
    val Black = Color(0xFF06090B)
    val Background = Color(0xFF090A0F)
    val Surface = Color(0xFF0E1518)
    val SurfaceRaised = Color(0xFF121C20)
    val SurfaceSoft = Color(0xFF182329)
    val Brand = Color(0xFF10A37F)
    val BrandBright = Color(0xFF19C37D)
    val BrandSoft = Color(0xFF72E6C4)
    val Border = Color(0xFF175D4C)
    val BorderBright = Color(0xFF1B8067)
    val TextPrimary = Color(0xFFF4F7F2)
    val TextSecondary = Color(0xFFA8B3BD)
    val Error = Color(0xFFFF6B6B)
    val Warning = Color(0xFFFFC857)
    val Mascot = Color(0xFFC98B49)
    val MascotLight = Color(0xFFE8B66D)
    val Leaf = Color(0xFF52D273)

    val ScreenPadding = 20.dp
    val SectionGap = 16.dp
    val CardRadius = 24.dp
    val ControlRadius = 20.dp
    val SmallRadius = 14.dp
}

@Composable
fun PotatoBackdrop(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF061013),
                    PotatoVisuals.Background,
                    Color(0xFF070B0D),
                )
            )
        )
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x3319C37D), Color.Transparent),
                        center = Offset(1100f, 120f),
                        radius = 850f,
                    )
                )
        )
        content()
    }
}

@Composable
fun PotatoPanel(
    modifier: Modifier = Modifier,
    border: Boolean = true,
    padding: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.then(
            if (border) Modifier.border(1.dp, PotatoVisuals.Border, RoundedCornerShape(PotatoVisuals.CardRadius))
            else Modifier
        ),
        shape = RoundedCornerShape(PotatoVisuals.CardRadius),
        color = PotatoVisuals.Surface.copy(alpha = 0.94f),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Box(Modifier.padding(padding)) { content() }
    }
}

@Composable
fun PotatoStatusPill(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = if (active) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary
    Row(
        modifier = modifier
            .background(tint.copy(alpha = 0.11f), RoundedCornerShape(50))
            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(8.dp).background(tint, CircleShape))
        Text(text, color = tint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PotatoBrand(
    online: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PotatoMascot(Modifier.size(if (compact) 42.dp else 54.dp), expression = if (online) PotatoMascotExpression.HAPPY else PotatoMascotExpression.SLEEPY)
        Column {
            Text(
                "POTATO",
                color = PotatoVisuals.BrandBright,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            if (!compact) {
                Text("Your AI assistant. A more productive you.", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.size(2.dp))
        PotatoStatusPill(if (online) "ONLINE" else "OFFLINE", online)
    }
}

enum class PotatoMascotExpression { HAPPY, THINKING, WORKING, SUCCESS, SLEEPY }

@Composable
fun PotatoMascot(
    modifier: Modifier = Modifier,
    expression: PotatoMascotExpression = PotatoMascotExpression.HAPPY,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bodyLeft = w * 0.18f
        val bodyTop = h * 0.22f
        val bodyW = w * 0.64f
        val bodyH = h * 0.66f

        drawOval(
            brush = Brush.radialGradient(
                listOf(PotatoVisuals.MascotLight, PotatoVisuals.Mascot),
                center = Offset(w * 0.42f, h * 0.42f),
                radius = w * 0.6f,
            ),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyW, bodyH),
        )
        drawOval(Color(0x66000000), topLeft = Offset(bodyLeft, bodyTop), size = Size(bodyW, bodyH), style = Stroke(width = w * 0.025f))

        val leaf = Path().apply {
            moveTo(w * 0.5f, h * 0.23f)
            cubicTo(w * 0.44f, h * 0.10f, w * 0.30f, h * 0.08f, w * 0.31f, h * 0.20f)
            cubicTo(w * 0.37f, h * 0.26f, w * 0.45f, h * 0.23f, w * 0.5f, h * 0.23f)
        }
        drawPath(leaf, PotatoVisuals.Leaf, style = Fill)
        val leaf2 = Path().apply {
            moveTo(w * 0.5f, h * 0.23f)
            cubicTo(w * 0.54f, h * 0.09f, w * 0.70f, h * 0.10f, w * 0.68f, h * 0.21f)
            cubicTo(w * 0.62f, h * 0.27f, w * 0.55f, h * 0.23f, w * 0.5f, h * 0.23f)
        }
        drawPath(leaf2, Color(0xFF7DDF63), style = Fill)
        drawLine(Color(0xFF2E7D49), Offset(w * 0.5f, h * 0.24f), Offset(w * 0.49f, h * 0.13f), strokeWidth = w * 0.025f, cap = StrokeCap.Round)

        val eyeY = h * 0.52f
        if (expression == PotatoMascotExpression.SLEEPY) {
            drawLine(Color(0xFF172126), Offset(w * 0.34f, eyeY), Offset(w * 0.42f, eyeY), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
            drawLine(Color(0xFF172126), Offset(w * 0.58f, eyeY), Offset(w * 0.66f, eyeY), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
        } else {
            drawCircle(Color(0xFF172126), w * 0.035f, Offset(w * 0.38f, eyeY))
            drawCircle(Color(0xFF172126), w * 0.035f, Offset(w * 0.62f, eyeY))
        }
        drawCircle(Color(0x55FF6677), w * 0.055f, Offset(w * 0.30f, h * 0.61f))
        drawCircle(Color(0x55FF6677), w * 0.055f, Offset(w * 0.70f, h * 0.61f))

        when (expression) {
            PotatoMascotExpression.THINKING -> {
                drawCircle(Color(0xFF172126), w * 0.026f, Offset(w * 0.50f, h * 0.65f), style = Stroke(width = w * 0.018f))
                drawCircle(PotatoVisuals.BrandBright, w * 0.025f, Offset(w * 0.82f, h * 0.30f))
                drawCircle(PotatoVisuals.BrandBright, w * 0.016f, Offset(w * 0.90f, h * 0.21f))
            }
            PotatoMascotExpression.WORKING -> {
                drawLine(Color(0xFF172126), Offset(w * 0.45f, h * 0.66f), Offset(w * 0.55f, h * 0.66f), strokeWidth = w * 0.022f, cap = StrokeCap.Round)
                drawRoundRect(Color(0xFF2A3942), topLeft = Offset(w * 0.27f, h * 0.70f), size = Size(w * 0.46f, h * 0.16f))
            }
            else -> {
                val smile = Path().apply {
                    moveTo(w * 0.43f, h * 0.64f)
                    quadraticBezierTo(w * 0.50f, h * 0.71f, w * 0.58f, h * 0.64f)
                }
                drawPath(smile, Color(0xFF172126), style = Stroke(width = w * 0.025f, cap = StrokeCap.Round))
            }
        }

        drawLine(Color(0xFF172126), Offset(w * 0.21f, h * 0.67f), Offset(w * 0.10f, h * 0.61f), strokeWidth = w * 0.028f, cap = StrokeCap.Round)
        drawLine(Color(0xFF172126), Offset(w * 0.79f, h * 0.67f), Offset(w * 0.90f, h * 0.60f), strokeWidth = w * 0.028f, cap = StrokeCap.Round)
        drawLine(Color(0xFF172126), Offset(w * 0.40f, h * 0.86f), Offset(w * 0.36f, h * 0.94f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
        drawLine(Color(0xFF172126), Offset(w * 0.60f, h * 0.86f), Offset(w * 0.64f, h * 0.94f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)

        if (expression == PotatoMascotExpression.SUCCESS) {
            drawCircle(PotatoVisuals.BrandBright, w * 0.025f, Offset(w * 0.12f, h * 0.18f))
            drawCircle(PotatoVisuals.Warning, w * 0.020f, Offset(w * 0.86f, h * 0.17f))
            drawCircle(Color(0xFFFF72C6), w * 0.018f, Offset(w * 0.92f, h * 0.34f))
        }
    }
}

@Composable
fun PotatoSectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
