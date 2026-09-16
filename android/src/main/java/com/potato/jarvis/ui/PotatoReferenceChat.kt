package com.potato.jarvis.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.potato.jarvis.core.ChatMessage
import com.potato.jarvis.core.TaskStatus

@Composable
fun PotatoChatLanding(
    tasks: List<TaskStatus>,
    online: Boolean,
    onSearchWeb: () -> Unit,
    onTakePhoto: () -> Unit,
    onScanText: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 760.dp
        Column(
            Modifier.fillMaxSize().padding(horizontal = if (wide) 22.dp else 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (wide) {
                Box(Modifier.fillMaxWidth().weight(0.95f)) {
                    PotatoHeroScenery(Modifier.fillMaxSize())
                    PotatoWelcomeHero(
                        online = online,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Row(
                    Modifier.fillMaxWidth().weight(0.66f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PotatoTaskPreview(tasks = tasks, modifier = Modifier.weight(1.52f).fillMaxHeight())
                    PotatoQuoteCard(Modifier.weight(0.78f).fillMaxHeight())
                }
            } else {
                Box(Modifier.fillMaxWidth().height(260.dp)) {
                    PotatoHeroScenery(Modifier.fillMaxSize())
                    PotatoWelcomeHero(online = online, modifier = Modifier.fillMaxSize())
                }
                PotatoTaskPreview(tasks = tasks, modifier = Modifier.fillMaxWidth().weight(1f))
            }
            PotatoQuickActions(
                onSearchWeb = onSearchWeb,
                onTakePhoto = onTakePhoto,
                onScanText = onScanText,
                onVoice = onVoice,
            )
        }
    }
}

@Composable
private fun PotatoHeroScenery(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            Brush.horizontalGradient(
                listOf(
                    PotatoVisuals.Black,
                    Color(0xFF071412),
                    Color(0xFF0A2722),
                    Color(0xFF0B342C),
                )
            )
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val moon = Offset(w * 0.82f, h * 0.23f)
            drawCircle(Color(0x163DE8B3), radius = size.minDimension * 0.23f, center = moon)
            drawCircle(Color(0x354EDAB7), radius = size.minDimension * 0.15f, center = moon)
            drawCircle(Color(0xFFE5EEB4), radius = size.minDimension * 0.095f, center = moon)

            listOf(
                Offset(w * 0.48f, h * 0.16f),
                Offset(w * 0.56f, h * 0.10f),
                Offset(w * 0.65f, h * 0.20f),
                Offset(w * 0.74f, h * 0.12f),
                Offset(w * 0.91f, h * 0.13f),
            ).forEachIndexed { index, star ->
                drawCircle(if (index % 2 == 0) Color(0x88D5F7E9) else Color(0x55FFFFFF), 1.5f + (index % 2), star)
            }

            fun ridge(points: List<Offset>, color: Color) {
                val path = Path().apply {
                    moveTo(w * 0.40f, h)
                    lineTo(w * 0.40f, points.first().y)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(w, points.last().y)
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color)
            }

            ridge(
                listOf(
                    Offset(w * 0.47f, h * 0.58f),
                    Offset(w * 0.59f, h * 0.23f),
                    Offset(w * 0.66f, h * 0.48f),
                    Offset(w * 0.75f, h * 0.17f),
                    Offset(w * 0.84f, h * 0.49f),
                    Offset(w * 0.93f, h * 0.31f),
                    Offset(w, h * 0.44f),
                ),
                Color(0xFF1A4B40),
            )
            ridge(
                listOf(
                    Offset(w * 0.43f, h * 0.70f),
                    Offset(w * 0.55f, h * 0.43f),
                    Offset(w * 0.67f, h * 0.65f),
                    Offset(w * 0.78f, h * 0.37f),
                    Offset(w * 0.89f, h * 0.67f),
                    Offset(w, h * 0.53f),
                ),
                Color(0xFF10332D),
            )
            ridge(
                listOf(
                    Offset(w * 0.41f, h * 0.80f),
                    Offset(w * 0.54f, h * 0.61f),
                    Offset(w * 0.66f, h * 0.78f),
                    Offset(w * 0.79f, h * 0.58f),
                    Offset(w * 0.91f, h * 0.77f),
                    Offset(w, h * 0.67f),
                ),
                Color(0xFF091F1C),
            )

            val lake = Path().apply {
                moveTo(w * 0.47f, h * 0.74f)
                quadraticBezierTo(w * 0.72f, h * 0.64f, w * 0.96f, h * 0.76f)
                lineTo(w * 0.90f, h * 0.96f)
                quadraticBezierTo(w * 0.70f, h * 0.84f, w * 0.50f, h * 0.94f)
                close()
            }
            drawPath(
                lake,
                Brush.verticalGradient(
                    listOf(Color(0x7759C9A9), Color(0x1919C37D)),
                    startY = h * 0.65f,
                    endY = h,
                ),
            )
            drawLine(Color(0x557CE8C7), Offset(w * 0.63f, h * 0.78f), Offset(w * 0.86f, h * 0.78f), 2f)
            drawLine(Color(0x333DC69D), Offset(w * 0.57f, h * 0.84f), Offset(w * 0.90f, h * 0.84f), 1.5f)

            fun pine(x: Float, base: Float, height: Float, color: Color) {
                val tree = Path().apply {
                    moveTo(x, base - height)
                    lineTo(x - height * 0.23f, base - height * 0.49f)
                    lineTo(x - height * 0.10f, base - height * 0.51f)
                    lineTo(x - height * 0.31f, base - height * 0.17f)
                    lineTo(x + height * 0.31f, base - height * 0.17f)
                    lineTo(x + height * 0.10f, base - height * 0.51f)
                    lineTo(x + height * 0.23f, base - height * 0.49f)
                    close()
                }
                drawPath(tree, color)
            }
            for (i in 0..9) {
                val x = w * (0.46f + i * 0.058f)
                val treeHeight = h * (0.14f + (i % 3) * 0.018f)
                pine(x, h * 0.95f, treeHeight, if (i % 2 == 0) Color(0xFF061512) else Color(0xFF081B17))
            }
        }

        Text(
            "Small steps, bigger things. 🌱",
            color = PotatoVisuals.BrandSoft,
            style = MaterialTheme.typography.labelLarge,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 18.dp),
        )
    }
}

@Composable
private fun PotatoWelcomeHero(online: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.padding(horizontal = 26.dp, vertical = 18.dp)) {
        Column(
            Modifier.align(Alignment.CenterStart).widthIn(max = 610.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                PotatoMascot(
                    Modifier.size(126.dp),
                    expression = if (online) PotatoMascotExpression.HAPPY else PotatoMascotExpression.SLEEPY,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        "Hey Cody 👋",
                        color = PotatoVisuals.TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "What can I help you with today?",
                        color = PotatoVisuals.TextSecondary,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PotatoVisuals.SurfaceRaised.copy(alpha = 0.88f),
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .border(1.dp, PotatoVisuals.Border, RoundedCornerShape(20.dp)),
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Text(
                        "I'm here to help you think, plan, create, and get things done.",
                        color = PotatoVisuals.TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Same goals. Brighter days. 🌱",
                        color = PotatoVisuals.BrandBright,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PotatoTaskPreview(tasks: List<TaskStatus>, modifier: Modifier = Modifier) {
    PotatoPanel(modifier = modifier, padding = 20.dp) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("▣", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(10.dp))
                Text("Your tasks for today", color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = PotatoVisuals.Brand.copy(alpha = 0.16f)) {
                    Text(
                        "${tasks.size} tasks",
                        color = PotatoVisuals.BrandBright,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (tasks.isEmpty()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text("A clear day ✨", style = MaterialTheme.typography.titleMedium, color = PotatoVisuals.TextPrimary)
                    Text("Add a task or ask POTATO to plan something for you.", color = PotatoVisuals.TextSecondary, modifier = Modifier.padding(top = 5.dp))
                }
            } else {
                tasks.take(4).forEach { task ->
                    PotatoTaskRow(task)
                }
                Spacer(Modifier.weight(1f))
            }
            Text(
                "You've got this! 🌱",
                color = PotatoVisuals.BrandBright,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun PotatoTaskRow(task: TaskStatus) {
    val done = task.status == "completed"
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(23.dp)
                .background(if (done) PotatoVisuals.BrandBright else Color.Transparent, RoundedCornerShape(6.dp))
                .border(1.dp, if (done) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Text("✓", color = PotatoVisuals.Black, fontWeight = FontWeight.Black)
        }
        Text(
            task.description,
            color = if (done) PotatoVisuals.TextSecondary else PotatoVisuals.TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            taskTimeLabel(task),
            color = if (done) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun taskTimeLabel(task: TaskStatus): String {
    if (task.status == "completed") return "Completed! 🎉"
    val due = task.dueAt ?: return when (task.priority) {
        "urgent" -> "Urgent"
        "high" -> "High priority"
        "low" -> "Low priority"
        else -> "Today"
    }
    val match = Regex("""T(\d{2}):(\d{2})""").find(due) ?: return "Today"
    val hour24 = match.groupValues[1].toIntOrNull() ?: return "Today"
    val minute = match.groupValues[2]
    val suffix = if (hour24 < 12) "AM" else "PM"
    val hour12 = when (val value = hour24 % 12) {
        0 -> 12
        else -> value
    }
    return "$hour12:$minute $suffix"
}

@Composable
private fun PotatoScenicCard(modifier: Modifier = Modifier) {
    PotatoPanel(modifier = modifier, padding = 0.dp) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B2A27), Color(0xFF071413), PotatoVisuals.Black)))) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val moon = Offset(w * 0.79f, h * 0.20f)
                drawCircle(Color(0x183DE8B3), radius = size.minDimension * 0.22f, center = moon)
                drawCircle(Color(0x33BCE7C7), radius = size.minDimension * 0.15f, center = moon)
                drawCircle(Color(0xFFE1EDAE), radius = size.minDimension * 0.105f, center = moon)

                listOf(
                    Offset(w * 0.10f, h * 0.15f), Offset(w * 0.19f, h * 0.10f), Offset(w * 0.31f, h * 0.19f),
                    Offset(w * 0.46f, h * 0.11f), Offset(w * 0.60f, h * 0.16f), Offset(w * 0.91f, h * 0.10f),
                ).forEachIndexed { index, point ->
                    drawCircle(if (index % 2 == 0) Color(0x99D5F7E9) else Color(0x66FFFFFF), radius = 1.6f + index % 2, center = point)
                }

                fun mountain(points: List<Offset>, color: Color) {
                    val path = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, points.first().y)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(w, points.last().y)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path, color)
                }

                mountain(
                    listOf(
                        Offset(w * 0.12f, h * 0.53f), Offset(w * 0.26f, h * 0.24f), Offset(w * 0.35f, h * 0.48f),
                        Offset(w * 0.53f, h * 0.19f), Offset(w * 0.68f, h * 0.49f), Offset(w * 0.86f, h * 0.31f), Offset(w, h * 0.46f),
                    ),
                    Color(0xFF17433A),
                )
                mountain(
                    listOf(
                        Offset(w * 0.07f, h * 0.66f), Offset(w * 0.23f, h * 0.40f), Offset(w * 0.38f, h * 0.61f),
                        Offset(w * 0.58f, h * 0.35f), Offset(w * 0.74f, h * 0.63f), Offset(w * 0.91f, h * 0.48f), Offset(w, h * 0.59f),
                    ),
                    Color(0xFF102F2A),
                )
                mountain(
                    listOf(
                        Offset(w * 0.05f, h * 0.77f), Offset(w * 0.19f, h * 0.58f), Offset(w * 0.33f, h * 0.73f),
                        Offset(w * 0.50f, h * 0.55f), Offset(w * 0.65f, h * 0.75f), Offset(w * 0.82f, h * 0.61f), Offset(w, h * 0.71f),
                    ),
                    Color(0xFF09201D),
                )

                val lake = Path().apply {
                    moveTo(w * 0.12f, h * 0.73f)
                    quadraticBezierTo(w * 0.48f, h * 0.64f, w * 0.87f, h * 0.73f)
                    lineTo(w * 0.74f, h * 0.92f)
                    quadraticBezierTo(w * 0.46f, h * 0.84f, w * 0.20f, h * 0.91f)
                    close()
                }
                drawPath(lake, Brush.verticalGradient(listOf(Color(0x8846B79B), Color(0x2219C37D)), startY = h * 0.65f, endY = h))
                drawLine(Color(0x5572E6C4), Offset(w * 0.38f, h * 0.76f), Offset(w * 0.67f, h * 0.76f), strokeWidth = 2f)
                drawLine(Color(0x334AC5A4), Offset(w * 0.29f, h * 0.82f), Offset(w * 0.71f, h * 0.82f), strokeWidth = 1.4f)

                fun pine(x: Float, base: Float, height: Float, color: Color) {
                    drawRect(color, topLeft = Offset(x - 1.5f, base - height * 0.20f), size = androidx.compose.ui.geometry.Size(3f, height * 0.20f))
                    val tree = Path().apply {
                        moveTo(x, base - height)
                        lineTo(x - height * 0.24f, base - height * 0.48f)
                        lineTo(x - height * 0.10f, base - height * 0.50f)
                        lineTo(x - height * 0.31f, base - height * 0.18f)
                        lineTo(x + height * 0.31f, base - height * 0.18f)
                        lineTo(x + height * 0.10f, base - height * 0.50f)
                        lineTo(x + height * 0.24f, base - height * 0.48f)
                        close()
                    }
                    drawPath(tree, color)
                }
                for (i in 0..11) {
                    val x = w * (0.025f + i * 0.087f)
                    val height = h * (0.13f + (i % 4) * 0.018f)
                    pine(x, h * 0.91f, height, if (i % 2 == 0) Color(0xFF061513) else Color(0xFF081B18))
                }
            }
            Column(Modifier.align(Alignment.TopStart).padding(22.dp)) {
                Text("Good ideas", color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.headlineSmall, fontStyle = FontStyle.Italic)
                Text("happen here 🌱", color = PotatoVisuals.BrandSoft, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
            }
            Text(
                "A SMARTER YOU\nA HAPPIER TOMORROW",
                color = PotatoVisuals.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            )
        }
    }
}

@Composable
private fun PotatoQuoteCard(modifier: Modifier = Modifier) {
    PotatoPanel(modifier = modifier) {
        Column {
            Text("“", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
            Text(
                "A more organized today leads to a brighter tomorrow. 🌱",
                color = PotatoVisuals.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
            )
            Text("POTATO", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 18.dp))
        }
    }
}

@Composable
fun PotatoQuickActions(
    onSearchWeb: () -> Unit,
    onTakePhoto: () -> Unit,
    onScanText: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PotatoQuickAction("◎", "Search web", "Find anything", onSearchWeb)
        PotatoQuickAction("▣", "Take photo", "See the world", onTakePhoto)
        PotatoQuickAction("⌗", "Scan text", "Turn text into ideas", onScanText)
        PotatoQuickAction("●", "Voice note", "Speak your mind", onVoice)
    }
}

@Composable
private fun PotatoQuickAction(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PotatoVisuals.Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = PotatoVisuals.Surface.copy(alpha = 0.9f)),
        modifier = Modifier.widthIn(min = 190.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 15.dp, vertical = 13.dp),
    ) {
        Box(
            Modifier.size(42.dp).background(PotatoVisuals.Brand.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(11.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(title, color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(subtitle, color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PotatoMessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val user = message.role == "user"
    Row(
        modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = if (user) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!user) {
            PotatoMascot(Modifier.size(40.dp), expression = PotatoMascotExpression.HAPPY)
            Spacer(Modifier.width(9.dp))
        }
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (user) PotatoVisuals.Brand.copy(alpha = 0.22f) else PotatoVisuals.SurfaceRaised,
            modifier = Modifier
                .widthIn(max = 610.dp)
                .border(1.dp, if (user) PotatoVisuals.BorderBright else PotatoVisuals.Border, RoundedCornerShape(22.dp)),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                if (!user) Text("POTATO", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(message.content, color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun PotatoComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    onTools: () -> Unit,
    enabled: Boolean,
    voiceActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = PotatoVisuals.SurfaceRaised.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PotatoVisuals.Border),
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onTools,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PotatoVisuals.BrandBright, contentColor = PotatoVisuals.Black),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.size(46.dp),
            ) { Text("+", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { Text("Ask POTATO anything…", color = PotatoVisuals.TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = PotatoVisuals.TextPrimary,
                    unfocusedTextColor = PotatoVisuals.TextPrimary,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 4,
            )
            OutlinedButton(
                onClick = onVoice,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (voiceActive) PotatoVisuals.BrandBright else PotatoVisuals.Border),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.size(46.dp),
            ) { Text(if (voiceActive) "■" else "🎙", color = if (voiceActive) PotatoVisuals.BrandBright else PotatoVisuals.TextPrimary) }
            Button(
                enabled = value.isNotBlank() && enabled,
                onClick = onSend,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PotatoVisuals.BrandBright,
                    contentColor = PotatoVisuals.Black,
                    disabledContainerColor = PotatoVisuals.Brand.copy(alpha = 0.30f),
                    disabledContentColor = PotatoVisuals.BrandSoft.copy(alpha = 0.72f),
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.size(46.dp),
            ) { Text("➤", fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
fun PotatoWorkingCard(
    title: String,
    subtitle: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        PotatoPanel(Modifier.fillMaxWidth(), padding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PotatoMascot(Modifier.size(42.dp), expression = PotatoMascotExpression.WORKING)
                Column {
                    Text(title, color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
