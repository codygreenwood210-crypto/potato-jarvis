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
            Modifier.fillMaxSize().padding(horizontal = if (wide) 28.dp else 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (wide) {
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1.55f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PotatoWelcomeHero(online = online, modifier = Modifier.fillMaxWidth())
                        PotatoTaskPreview(tasks = tasks, modifier = Modifier.fillMaxWidth().weight(1f))
                    }
                    Column(Modifier.weight(0.78f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PotatoScenicCard(Modifier.fillMaxWidth().weight(1f))
                        PotatoQuoteCard(Modifier.fillMaxWidth())
                    }
                }
            } else {
                PotatoWelcomeHero(online = online, modifier = Modifier.fillMaxWidth())
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
private fun PotatoWelcomeHero(online: Boolean, modifier: Modifier = Modifier) {
    PotatoPanel(modifier = modifier, padding = 22.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            PotatoMascot(
                Modifier.size(112.dp),
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
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PotatoVisuals.SurfaceRaised.copy(alpha = 0.9f),
                    modifier = Modifier.border(1.dp, PotatoVisuals.Border, RoundedCornerShape(18.dp)),
                ) {
                    Column(Modifier.padding(16.dp)) {
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
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
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
                Text("Your tasks for today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
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
            if (done) "Completed! 🎉" else task.dueAt?.take(10) ?: task.priority,
            color = if (done) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun PotatoScenicCard(modifier: Modifier = Modifier) {
    PotatoPanel(modifier = modifier, padding = 0.dp) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B2A27), Color(0xFF071413), PotatoVisuals.Black)))) {
            Canvas(Modifier.fillMaxSize()) {
                val moon = Offset(size.width * 0.78f, size.height * 0.22f)
                drawCircle(Color(0xFFDAE8A7), radius = size.minDimension * 0.11f, center = moon)
                val far = Path().apply {
                    moveTo(0f, size.height * 0.62f)
                    lineTo(size.width * 0.22f, size.height * 0.30f)
                    lineTo(size.width * 0.34f, size.height * 0.52f)
                    lineTo(size.width * 0.52f, size.height * 0.20f)
                    lineTo(size.width * 0.69f, size.height * 0.55f)
                    lineTo(size.width, size.height * 0.36f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(far, Color(0xFF174038))
                val near = Path().apply {
                    moveTo(0f, size.height * 0.72f)
                    lineTo(size.width * 0.28f, size.height * 0.48f)
                    lineTo(size.width * 0.42f, size.height * 0.68f)
                    lineTo(size.width * 0.60f, size.height * 0.41f)
                    lineTo(size.width * 0.80f, size.height * 0.70f)
                    lineTo(size.width, size.height * 0.53f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(near, Color(0xFF0B211F))
                drawOval(Color(0x442AC79F), topLeft = Offset(size.width * 0.08f, size.height * 0.72f), size = androidx.compose.ui.geometry.Size(size.width * 0.82f, size.height * 0.15f))
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
        Text(symbol, color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleLarge)
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
            ) { Text(if (voiceActive) "■" else "●", color = if (voiceActive) PotatoVisuals.BrandBright else PotatoVisuals.TextPrimary) }
            Button(
                enabled = value.isNotBlank() && enabled,
                onClick = onSend,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PotatoVisuals.BrandBright, contentColor = PotatoVisuals.Black),
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
