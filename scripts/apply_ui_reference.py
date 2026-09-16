from pathlib import Path
import re

path = Path("android/src/main/java/com/potato/jarvis/MainActivity.kt")
text = path.read_text(encoding="utf-8")

imports_anchor = "import com.potato.jarvis.ui.PotatoTheme\n"
imports_extra = """import com.potato.jarvis.ui.PotatoAppShell
import com.potato.jarvis.ui.PotatoChatLanding
import com.potato.jarvis.ui.PotatoComposer
import com.potato.jarvis.ui.PotatoMessageBubble
import com.potato.jarvis.ui.PotatoQuickActions
import com.potato.jarvis.ui.PotatoWorkingCard
"""
if imports_extra not in text:
    if imports_anchor not in text:
        raise SystemExit("PotatoTheme import anchor not found")
    text = text.replace(imports_anchor, imports_anchor + imports_extra, 1)

old_shell = '''            Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("POTATO") },
                    actions = { Text(if (state.online) "ONLINE" else "OFFLINE", modifier = Modifier.padding(end = 16.dp)) },
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf(
                        Screen.CHAT to "Chat",
                        Screen.MEMORY to "Memory",
                        Screen.TASKS to "Tasks",
                        Screen.NOTIFICATIONS to "Alerts",
                        Screen.PROACTIVE to "Proactive",
                        Screen.DEVICES to "Device",
                        Screen.SMART_HOME to "Home",
                        Screen.TOOLS to "Tools",
                        Screen.SECURITY to "Security",
                        Screen.SETTINGS to "Settings",
                    ).forEach { (target, label) ->
                        NavigationBarItem(
                            selected = screen == target,
                            onClick = { screen = target },
                            icon = { Text(label.take(1)) },
                            label = { Text(label) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (screen) {
                    Screen.CHAT -> ChatScreen(vm, state, draft, { draft = it }, { vm.runAgent(draft); draft = "" }, voiceManager)
                    Screen.MEMORY -> MemoryScreen(vm, state)
                    Screen.TASKS -> TaskScreen(vm, state)
                    Screen.NOTIFICATIONS -> NotificationScreen(vm, state)
                    Screen.PROACTIVE -> ProactiveScreen(vm, state)
                    Screen.DEVICES -> DeviceScreen(vm, state)
                    Screen.SMART_HOME -> SmartHomeScreen(vm, state)
                    Screen.TOOLS -> ToolsScreen(vm, state)
                    Screen.SECURITY -> SecurityScreen(vm, state)
                    Screen.SETTINGS -> SettingsScreen(vm, state)
                }
            }
        }
'''
new_shell = '''            PotatoAppShell(
                screen = screen,
                online = state.online,
                onScreenSelected = { screen = it },
            ) {
                when (screen) {
                    Screen.CHAT -> ChatScreen(vm, state, draft, { draft = it }, { vm.runAgent(draft); draft = "" }, voiceManager)
                    Screen.MEMORY -> MemoryScreen(vm, state)
                    Screen.TASKS -> TaskScreen(vm, state)
                    Screen.NOTIFICATIONS -> NotificationScreen(vm, state)
                    Screen.PROACTIVE -> ProactiveScreen(vm, state)
                    Screen.DEVICES -> DeviceScreen(vm, state)
                    Screen.SMART_HOME -> SmartHomeScreen(vm, state)
                    Screen.TOOLS -> ToolsScreen(vm, state)
                    Screen.SECURITY -> SecurityScreen(vm, state)
                    Screen.SETTINGS -> SettingsScreen(vm, state)
                }
            }
'''
if old_shell in text:
    text = text.replace(old_shell, new_shell, 1)
elif "PotatoAppShell(" not in text:
    raise SystemExit("Original app shell block not found")

chat_start = text.index("@Composable\nprivate fun ChatScreen(")
chat_end = text.index("@Composable\nprivate fun Bubble", chat_start)
new_chat = r'''@Composable
private fun ChatScreen(
    vm: JarvisViewModel,
    state: JarvisViewModel.State,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    voice: VoiceManager?,
) {
    val context = LocalContext.current
    var imageFile by remember { mutableStateOf<File?>(null) }
    var toolsVisible by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val capture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = imageFile
        if (success && file != null) {
            vm.vision(file, "Analyze this image and tell me the most useful information.")
        } else {
            file?.delete()
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            copyUriToVisionCache(context, uri)?.let { file -> vm.vision(file, "Analyze this image and tell me the most useful information.") }
        }
    }
    val ocrPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) copyUriToVisionCache(context, uri)?.let(vm::visionOcr)
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voice?.listen()
    }

    fun toggleVoice() {
        if (state.voiceState == "LISTENING") {
            voice?.stopListening()
        } else if (state.voiceState == "SPEAKING") {
            voice?.stopSpeaking()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voice?.listen()
        } else {
            audioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) { vm.loadTasks() }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        val landing = state.messages.isEmpty() && !state.streaming && state.streamText.isBlank()
        if (landing) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                PotatoChatLanding(
                    tasks = state.tasks,
                    online = state.online,
                    onSearchWeb = { if (!state.useWeb) vm.toggleWeb() },
                    onTakePhoto = { launchCamera(capture, context) { imageFile = it } },
                    onScanText = { ocrPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onVoice = ::toggleVoice,
                )
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = vm::newConversation, enabled = !state.busy) { Text("New chat") }
                OutlinedButton(onClick = vm::loadSessions, enabled = !state.busy) { Text("History") }
                if (state.useWeb) FilterChip(selected = true, onClick = vm::toggleWeb, label = { Text("Web on") })
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.messages, key = { "${it.createdAt}-${it.role}-${it.content.hashCode()}" }) { message ->
                    PotatoMessageBubble(message)
                }
            }
        }

        PotatoWorkingCard(
            title = when {
                state.streaming -> "POTATO is responding"
                state.busy -> "POTATO is thinking"
                else -> "POTATO is ready"
            },
            subtitle = when {
                state.streaming -> "Building your answer…"
                state.busy -> "Working on the next step…"
                else -> "Ready when you are."
            },
            visible = state.streaming || state.busy,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        )

        AnimatedVisibility(state.streaming && state.streamText.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("POTATO", style = MaterialTheme.typography.titleMedium)
                    Text(state.streamText)
                    OutlinedButton(onClick = vm::cancelResponse, modifier = Modifier.padding(top = 8.dp)) { Text("Stop") }
                }
            }
        }

        AnimatedVisibility(state.lastVision.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Vision result", style = MaterialTheme.typography.titleMedium)
                    state.lastVisionResult?.let { Text("${it.width} × ${it.height}", style = MaterialTheme.typography.labelSmall) }
                    Text(state.lastVision)
                }
            }
        }
        AnimatedVisibility(state.lastOcrText.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("OCR", style = MaterialTheme.typography.titleMedium)
                    Text(state.lastOcrText)
                }
            }
        }

        AnimatedVisibility(state.error != null) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
        }

        AnimatedVisibility(toolsVisible) {
            PotatoQuickActions(
                onSearchWeb = { vm.toggleWeb() },
                onTakePhoto = { launchCamera(capture, context) { imageFile = it } },
                onScanText = { ocrPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onVoice = ::toggleVoice,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }

        if (state.voicePartialText.isNotBlank()) {
            Text(
                "Hearing: ${state.voicePartialText}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
            )
        }

        PotatoComposer(
            value = draft,
            onValueChange = onDraftChange,
            onSend = onSend,
            onVoice = ::toggleVoice,
            onTools = { toolsVisible = !toolsVisible },
            enabled = !state.busy,
            voiceActive = state.voiceState == "LISTENING" || state.voiceState == "SPEAKING",
        )
    }
}

'''
text = text[:chat_start] + new_chat + text[chat_end:]

bubble_pattern = re.compile(
    r'@Composable\nprivate fun Bubble\(message: ChatMessage\) \{.*?\n\}\n\n@Composable\nprivate fun MemoryScreen',
    re.S,
)
replacement = '''@Composable
private fun Bubble(message: ChatMessage) {
    PotatoMessageBubble(message)
}

@Composable
private fun MemoryScreen'''
text, count = bubble_pattern.subn(replacement, text, count=1)
if count != 1 and "PotatoMessageBubble(message)" not in text:
    raise SystemExit(f"Bubble replacement failed: {count}")

path.write_text(text, encoding="utf-8")
print("POTATO_UI_REFERENCE_INTEGRATION=PASS")
