from pathlib import Path

CHAT_PATH = Path("android/src/main/java/com/potato/jarvis/ui/PotatoReferenceChat.kt")
SHELL_PATH = Path("android/src/main/java/com/potato/jarvis/ui/PotatoShell.kt")


def must_replace(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f"Missing refinement marker: {label}")
    return text.replace(old, new, 1)


def replace_between(text: str, start: str, end: str, replacement: str) -> str:
    try:
        start_index = text.index(start)
        end_index = text.index(end, start_index)
    except ValueError as exc:
        raise SystemExit(f"UI refinement marker missing: {exc}") from exc
    return text[:start_index] + replacement.rstrip() + "\n\n" + text[end_index:]


chat = CHAT_PATH.read_text(encoding="utf-8")

# Give the lower task/quote region enough vertical room to show all four tasks,
# bringing the composition closer to the approved reference.
chat = must_replace(
    chat,
    "Box(Modifier.fillMaxWidth().weight(0.95f))",
    "Box(Modifier.fillMaxWidth().weight(0.82f))",
    "hero weight",
)
chat = must_replace(
    chat,
    "Modifier.fillMaxWidth().weight(0.66f)",
    "Modifier.fillMaxWidth().weight(0.76f)",
    "task row weight",
)

# Tighten the task card while adding the reference-style date treatment.
task_start = chat.index("@Composable\nprivate fun PotatoTaskPreview(")
task_end = chat.index("@Composable\nprivate fun PotatoTaskRow(", task_start)
task = chat[task_start:task_end]
task = must_replace(task, "PotatoPanel(modifier = modifier, padding = 20.dp)", "PotatoPanel(modifier = modifier, padding = 17.dp)", "task padding")
task = must_replace(
    task,
    "Spacer(Modifier.weight(1f))\n                Surface(shape = CircleShape, color = PotatoVisuals.Brand.copy(alpha = 0.16f))",
    "Spacer(Modifier.weight(1f))\n                Text(\n                    todayLabel(),\n                    color = PotatoVisuals.TextSecondary,\n                    style = MaterialTheme.typography.labelMedium,\n                )\n                Spacer(Modifier.width(12.dp))\n                Surface(shape = CircleShape, color = PotatoVisuals.Brand.copy(alpha = 0.16f))",
    "task date",
)
task = task.replace("Spacer(Modifier.height(12.dp))", "Spacer(Modifier.height(7.dp))", 1)
task = task.replace("modifier = Modifier.padding(top = 10.dp)", "modifier = Modifier.padding(top = 4.dp)", 1)
chat = chat[:task_start] + task + chat[task_end:]

row_start = chat.index("@Composable\nprivate fun PotatoTaskRow(")
row_end = chat.index("@Composable\nprivate fun PotatoScenicCard(", row_start)
row = chat[row_start:row_end]
row = row.replace("Modifier.fillMaxWidth().padding(vertical = 8.dp)", "Modifier.fillMaxWidth().padding(vertical = 6.dp)", 1)
row = row.replace(
    "val suffix = if (hour24 < 12) \"AM\" else \"PM\"\n    val hour12 = when (val value = hour24 % 12) {\n        0 -> 12\n        else -> value\n    }\n    return \"$hour12:$minute $suffix\"",
    "if (hour24 >= 17) return \"This evening\"\n    if (hour24 >= 12) return \"Today\"\n    val hour12 = when (val value = hour24 % 12) {\n        0 -> 12\n        else -> value\n    }\n    return \"$hour12:$minute AM\"",
    1,
)
row = row.rstrip() + "\n\nprivate fun todayLabel(): String =\n    java.text.SimpleDateFormat(\"EEE, MMM d\", java.util.Locale.getDefault()).format(java.util.Date())\n\n"
chat = chat[:row_start] + row + chat[row_end:]

# Stretch the four quick actions evenly across tablet width, while preserving
# horizontal scrolling on compact screens.
quick_actions = r'''@Composable
fun PotatoQuickActions(
    onSearchWeb: () -> Unit,
    onTakePhoto: () -> Unit,
    onScanText: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wide = maxWidth >= 760.dp
        if (wide) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PotatoQuickAction("◎", "Search web", "Find anything", onSearchWeb, Modifier.weight(1f))
                PotatoQuickAction("▣", "Take photo", "See the world", onTakePhoto, Modifier.weight(1f))
                PotatoQuickAction("⌗", "Scan text", "Turn text into ideas", onScanText, Modifier.weight(1f))
                PotatoQuickAction("●", "Voice note", "Speak your mind", onVoice, Modifier.weight(1f))
            }
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PotatoQuickAction("◎", "Search web", "Find anything", onSearchWeb, Modifier.widthIn(min = 190.dp))
                PotatoQuickAction("▣", "Take photo", "See the world", onTakePhoto, Modifier.widthIn(min = 190.dp))
                PotatoQuickAction("⌗", "Scan text", "Turn text into ideas", onScanText, Modifier.widthIn(min = 190.dp))
                PotatoQuickAction("●", "Voice note", "Speak your mind", onVoice, Modifier.widthIn(min = 190.dp))
            }
        }
    }
}

@Composable
private fun PotatoQuickAction(
    symbol: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PotatoVisuals.Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = PotatoVisuals.Surface.copy(alpha = 0.9f)),
        modifier = modifier.height(76.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 11.dp),
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
}'''
chat = replace_between(
    chat,
    "@Composable\nfun PotatoQuickActions(",
    "@Composable\nfun PotatoMessageBubble(",
    quick_actions,
)

# Keep send visibly prominent like the reference while remaining semantically disabled
# until there is message text.
chat = must_replace(
    chat,
    "disabledContainerColor = PotatoVisuals.Brand.copy(alpha = 0.30f),\n                    disabledContentColor = PotatoVisuals.BrandSoft.copy(alpha = 0.72f),",
    "disabledContainerColor = PotatoVisuals.BrandBright.copy(alpha = 0.88f),\n                    disabledContentColor = PotatoVisuals.Black.copy(alpha = 0.66f),",
    "disabled send treatment",
)

CHAT_PATH.write_text(chat, encoding="utf-8")

shell = SHELL_PATH.read_text(encoding="utf-8")

# Move the brand cluster toward the same upper-left-of-content position as the reference.
shell = must_replace(
    shell,
    "Box(Modifier.align(Alignment.Center)) {\n                    PotatoBrand(online = online, compact = false)\n                }",
    "Box(Modifier.align(Alignment.CenterStart).padding(start = 58.dp)) {\n                    PotatoBrand(online = online, compact = false)\n                }",
    "brand placement",
)

# Add the third top action seen in the reference. Both appearance shortcuts safely
# route to existing Settings instead of inventing a new capability.
shell = must_replace(
    shell,
    "PotatoTopAction(\"⌕\") { onScreenSelected(Screen.TOOLS) }\n                    PotatoTopAction(\"⚙\") { onScreenSelected(Screen.SETTINGS) }",
    "PotatoTopAction(\"⌕\") { onScreenSelected(Screen.TOOLS) }\n                    PotatoTopAction(\"☼\") { onScreenSelected(Screen.SETTINGS) }\n                    PotatoTopAction(\"⚙\") { onScreenSelected(Screen.SETTINGS) }",
    "top actions",
)

shell = must_replace(shell, "Text(\"V5.8 UI\"", "Text(\"V5.8\"", "version label")

SHELL_PATH.write_text(shell, encoding="utf-8")

print("POTATO_UI_FOURTH_PASS=APPLIED")
