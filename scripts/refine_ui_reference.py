from pathlib import Path

CHAT_PATH = Path("android/src/main/java/com/potato/jarvis/ui/PotatoReferenceChat.kt")
SHELL_PATH = Path("android/src/main/java/com/potato/jarvis/ui/PotatoShell.kt")


def replace_between(text: str, start: str, end: str, replacement: str) -> str:
    try:
        start_index = text.index(start)
        end_index = text.index(end, start_index)
    except ValueError as exc:
        raise SystemExit(f"UI refinement marker missing: {exc}") from exc
    return text[:start_index] + replacement.rstrip() + "\n\n" + text[end_index:]


chat = CHAT_PATH.read_text(encoding="utf-8")

landing = r'''@Composable
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
}'''

chat = replace_between(
    chat,
    "@Composable\nfun PotatoChatLanding(",
    "@Composable\nprivate fun PotatoTaskPreview(",
    landing,
)

task_row = r'''@Composable
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
}'''

chat = replace_between(
    chat,
    "@Composable\nprivate fun PotatoTaskRow(",
    "@Composable\nprivate fun PotatoScenicCard(",
    task_row,
)

CHAT_PATH.write_text(chat, encoding="utf-8")

shell = SHELL_PATH.read_text(encoding="utf-8")
if "import androidx.compose.foundation.layout.Arrangement\n" not in shell:
    shell = shell.replace(
        "import androidx.compose.foundation.layout.Box\n",
        "import androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\n",
    )
if "import androidx.compose.foundation.layout.size\n" not in shell:
    shell = shell.replace(
        "import androidx.compose.foundation.layout.padding\n",
        "import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\n",
    )

shell_block = r'''@Composable
private fun PotatoTopBar(online: Boolean, compact: Boolean, onScreenSelected: (Screen) -> Unit) {
    Surface(color = PotatoVisuals.Black.copy(alpha = 0.78f), tonalElevation = 0.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (compact) 66.dp else 78.dp)
                .padding(horizontal = if (compact) 14.dp else 22.dp),
        ) {
            if (compact) {
                Box(Modifier.align(Alignment.CenterStart)) {
                    PotatoBrand(online = online, compact = true)
                }
            } else {
                Box(Modifier.align(Alignment.Center)) {
                    PotatoBrand(online = online, compact = false)
                }
            }
            Row(
                Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!compact) {
                    PotatoTopAction("⌕") { onScreenSelected(Screen.TOOLS) }
                    PotatoTopAction("⚙") { onScreenSelected(Screen.SETTINGS) }
                }
            }
        }
    }
}

@Composable
private fun PotatoTopAction(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PotatoVisuals.SurfaceRaised.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun PotatoNavigationRail(
    screen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreOpen by remember { mutableStateOf(false) }
    NavigationRail(
        modifier = modifier.background(PotatoVisuals.Black.copy(alpha = 0.94f)).padding(vertical = 14.dp),
        containerColor = PotatoVisuals.Black.copy(alpha = 0.94f),
        header = { Spacer(Modifier.height(10.dp)) },
    ) {
        primaryDestinations.forEach { destination ->
            val selected = destination.screen == screen || (destination.screen == null && moreDestinations.any { it.first == screen })
            Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) PotatoVisuals.Brand.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable {
                            if (destination.screen != null) onScreenSelected(destination.screen)
                            else moreOpen = true
                        }
                        .padding(horizontal = 9.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(34.dp)
                            .background(
                                if (selected) PotatoVisuals.BrandBright else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(3.dp),
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            destination.symbol,
                            color = if (selected) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            destination.label,
                            color = if (selected) PotatoVisuals.TextPrimary else PotatoVisuals.TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
                if (destination.screen == null) {
                    DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                        moreDestinations.forEach { (target, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    moreOpen = false
                                    onScreenSelected(target)
                                },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("🌱", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleMedium)
        Text("Small", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Steps", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Bigger", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Things", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))
        Text("POTATO", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("V5.8 UI", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}'''

shell = replace_between(
    shell,
    "@Composable\nprivate fun PotatoTopBar(",
    "@Composable\nprivate fun PotatoBottomBar(",
    shell_block,
)

SHELL_PATH.write_text(shell, encoding="utf-8")

print("POTATO_UI_THIRD_PASS=APPLIED")
