from pathlib import Path

chat_path = Path("android/src/main/java/com/potato/jarvis/ui/PotatoReferenceChat.kt")
chat = chat_path.read_text(encoding="utf-8")

chat = chat.replace(
    'Text("Your tasks for today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)',
    'Text("Your tasks for today", color = PotatoVisuals.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)',
)

old_canvas = '''            Canvas(Modifier.fillMaxSize()) {
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
'''
new_canvas = '''            Canvas(Modifier.fillMaxSize()) {
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
'''
if old_canvas not in chat:
    if "fun pine(x: Float" not in chat:
        raise SystemExit("Scenic canvas target not found")
else:
    chat = chat.replace(old_canvas, new_canvas, 1)

old_quick = '''    ) {
        Text(symbol, color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(11.dp))
        Column(horizontalAlignment = Alignment.Start) {
'''
new_quick = '''    ) {
        Box(
            Modifier.size(42.dp).background(PotatoVisuals.Brand.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(11.dp))
        Column(horizontalAlignment = Alignment.Start) {
'''
if old_quick in chat:
    chat = chat.replace(old_quick, new_quick, 1)

old_voice = '''            ) { Text(if (voiceActive) "■" else "●", color = if (voiceActive) PotatoVisuals.BrandBright else PotatoVisuals.TextPrimary) }
            Button(
                enabled = value.isNotBlank() && enabled,
                onClick = onSend,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PotatoVisuals.BrandBright, contentColor = PotatoVisuals.Black),
'''
new_voice = '''            ) { Text(if (voiceActive) "■" else "🎙", color = if (voiceActive) PotatoVisuals.BrandBright else PotatoVisuals.TextPrimary) }
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
'''
if old_voice in chat:
    chat = chat.replace(old_voice, new_voice, 1)

chat_path.write_text(chat, encoding="utf-8")

shell_path = Path("android/src/main/java/com/potato/jarvis/ui/PotatoShell.kt")
shell = shell_path.read_text(encoding="utf-8")
if "import androidx.compose.foundation.clickable" not in shell:
    shell = shell.replace("import androidx.compose.foundation.background\n", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\n", 1)

shell = shell.replace(
    "PotatoTopBar(online = online, compact = false)",
    "PotatoTopBar(online = online, compact = false, onScreenSelected = onScreenSelected)",
)
shell = shell.replace(
    "PotatoTopBar(online = online, compact = true)",
    "PotatoTopBar(online = online, compact = true, onScreenSelected = onScreenSelected)",
)
shell = shell.replace(
    "private fun PotatoTopBar(online: Boolean, compact: Boolean) {",
    "private fun PotatoTopBar(online: Boolean, compact: Boolean, onScreenSelected: (Screen) -> Unit) {",
)

old_top_right = '''            if (!compact) {
                Text(
                    "Good ideas happen here  ·  small steps, bigger things",
                    color = PotatoVisuals.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
'''
new_top_right = '''            if (!compact) {
                Text(
                    "Good ideas happen here  ·  small steps, bigger things",
                    color = PotatoVisuals.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    "⌕",
                    color = PotatoVisuals.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(PotatoVisuals.SurfaceRaised)
                        .clickable { onScreenSelected(Screen.TOOLS) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "⚙",
                    color = PotatoVisuals.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(PotatoVisuals.SurfaceRaised)
                        .clickable { onScreenSelected(Screen.SETTINGS) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
'''
if old_top_right in shell:
    shell = shell.replace(old_top_right, new_top_right, 1)

old_header = '''        header = {
            PotatoMascot(Modifier.width(52.dp).height(58.dp))
            Spacer(Modifier.height(20.dp))
        },
'''
new_header = '''        header = {
            Spacer(Modifier.height(8.dp))
        },
'''
if old_header in shell:
    shell = shell.replace(old_header, new_header, 1)

old_footer = '''        Spacer(Modifier.weight(1f))
        Text("POTATO", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("V5.8 UI", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
'''
new_footer = '''        Spacer(Modifier.weight(1f))
        Text("🌱", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleMedium)
        Text("Small", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Steps", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Bigger", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Things", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(14.dp))
        Text("POTATO", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("V5.8 UI", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
'''
if old_footer in shell:
    shell = shell.replace(old_footer, new_footer, 1)

shell_path.write_text(shell, encoding="utf-8")
print("POTATO_UI_REFERENCE_REFINEMENT=PASS")
