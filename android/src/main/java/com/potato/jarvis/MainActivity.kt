package com.potato.jarvis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.clickable
import android.os.Bundle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.potato.jarvis.accessibility.PotatoAccessibilityService
import com.potato.jarvis.accessibility.AccessibilityConsent
import com.potato.jarvis.core.ChatMessage
import com.potato.jarvis.core.FileItem
import com.potato.jarvis.core.ToolInfo
import com.potato.jarvis.core.PersonalityProfile
import com.potato.jarvis.core.TaskStatus
import com.potato.jarvis.core.NotificationItem
import com.potato.jarvis.core.ProactiveSettings
import com.potato.jarvis.ui.JarvisViewModel
import com.potato.jarvis.ui.PotatoTheme
import com.potato.jarvis.voice.VoiceManager
import android.util.Base64
import java.io.File
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MainActivity : FragmentActivity() {
    private val viewModel by viewModels<JarvisViewModel>()
    private val navigationTargets = MutableSharedFlow<Screen>(replay = 1, extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        handleIntent(intent)
        setContent { PotatoApp(viewModel, navigationTargets) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        screenForPotatoDeepLink(intent?.dataString)?.let(navigationTargets::tryEmit)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel("potato_assistant", "POTATO Assistant", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Important POTATO assistant messages and task results."
            },
            NotificationChannel("potato_approvals", "POTATO Approvals", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Security approval requests requiring your attention."
            },
            NotificationChannel("potato_tasks", "POTATO Tasks", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Task and automation reminders."
            },
        )
        channels.forEach(manager::createNotificationChannel)
    }
}

enum class Screen { CHAT, MEMORY, TASKS, NOTIFICATIONS, PROACTIVE, DEVICES, SMART_HOME, TOOLS, SECURITY, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PotatoApp(vm: JarvisViewModel, navigationTargets: SharedFlow<Screen>) {
    var screen by rememberSaveable { mutableStateOf(Screen.CHAT) }
    var draft by rememberSaveable { mutableStateOf("") }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var voiceManager by remember { mutableStateOf<VoiceManager?>(null) }
    var showAccessibilityDisclosure by remember {
        mutableStateOf(!AccessibilityConsent.hasDecision(context))
    }

    LaunchedEffect(navigationTargets) {
        navigationTargets.collect { target -> screen = target }
    }

    DisposableEffect(context) {
        val manager = VoiceManager(
            context = context,
            onText = { heard -> vm.setVoicePartialText(""); vm.runAgent(heard) { answer -> if (vm.state.value.voiceMode) voiceManager?.speak(answer) } },
            onPartialText = vm::setVoicePartialText,
            onState = { voiceState -> vm.setVoiceState(voiceState.name) },
            onError = vm::setVoiceError,
        )
        manager.configure(speechRate = state.voiceRate)
        voiceManager = manager
        onDispose {
            manager.close()
            voiceManager = null
        }
    }

    LaunchedEffect(state.voiceRate) {
        voiceManager?.configure(speechRate = state.voiceRate)
    }

    PotatoTheme {
        if (showAccessibilityDisclosure) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Optional Accessibility context") },
                text = {
                    Text(
                        "POTATO can optionally use Android Accessibility to receive only the package name of the foreground app when the active window changes. " +
                            "This helps POTATO understand which app you are using. POTATO does not read screen text, passwords, messages or financial information, and it cannot click, type, perform gestures, change settings, or control other apps. " +
                            "The package name stays in POTATO's private local app storage unless you deliberately include device context in a request. You can use POTATO without enabling this feature."
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        AccessibilityConsent.record(context, true)
                        showAccessibilityDisclosure = false
                    }) { Text("Accept") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        AccessibilityConsent.record(context, false)
                        showAccessibilityDisclosure = false
                    }) { Text("Decline") }
                },
            )
        } else if (state.setupRequired) {
            SetupScreen(vm, state)
        } else {
            Scaffold(
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
        }
    }
}

@Composable
private fun NotificationScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.loadNotifications() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "Notifications remain disabled.", Toast.LENGTH_SHORT).show()
    }
    val enabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications", style = MaterialTheme.typography.headlineSmall)
            if (!enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Button(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Enable") }
            }
        }
        Text(if (enabled) "Notifications are enabled." else "Notifications are disabled by Android.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.notifications, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.body, modifier = Modifier.padding(top = 4.dp))
                        Text("${item.type} • ${item.priority}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            if (item.readAt == null) OutlinedButton(onClick = { vm.readNotification(item.id) }) { Text("Read") }
                            TextButton(onClick = { vm.dismissNotification(item.id) }) { Text("Dismiss") }
                        }
                    }
                }
            }
            if (state.notifications.isEmpty()) item { Text("No notifications.") }
        }
    }
}


@Composable
private fun ProactiveScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    LaunchedEffect(Unit) { vm.loadProactiveSettings() }
    val settings = state.proactiveSettings
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Proactive Intelligence", style = MaterialTheme.typography.headlineSmall)
        Text("POTATO can surface useful reminders and suggestions, but it never grants itself permission to act.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow proactive suggestions")
                    Switch(checked = settings.enabled, onCheckedChange = { vm.saveProactiveSettings(settings.copy(enabled = it)) })
                }
                Text("Mode: ${settings.mode}", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("off", "permission_based", "helpful").forEach { mode ->
                        FilterChip(selected = settings.mode == mode, onClick = { vm.saveProactiveSettings(settings.copy(mode = mode)) }, label = { Text(mode) })
                    }
                }
                Text("Daily suggestion limit: ${settings.dailyLimit}")
                Slider(value = settings.dailyLimit.toFloat(), onValueChange = { }, valueRange = 0f..20f, steps = 19, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.saveProactiveSettings(settings.copy(dailyLimit = (settings.dailyLimit + 1).coerceAtMost(20))) }) { Text("More") }
                    OutlinedButton(onClick = { vm.saveProactiveSettings(settings.copy(dailyLimit = (settings.dailyLimit - 1).coerceAtLeast(0))) }) { Text("Less") }
                }
                Text("Quiet hours: ${settings.quietStart}:00–${settings.quietEnd}:00")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.saveProactiveSettings(settings.copy(quietStart = (settings.quietStart + 1) % 24)) }) { Text("Quiet start +1h") }
                    OutlinedButton(onClick = { vm.saveProactiveSettings(settings.copy(quietEnd = (settings.quietEnd + 1) % 24)) }) { Text("Quiet end +1h") }
                }
                Button(onClick = { vm.runProactiveNow() }, enabled = settings.enabled && settings.mode != "off") { Text("Check now") }
                if (state.proactiveMessage.isNotBlank()) Text(state.proactiveMessage, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Recent proactive suggestions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        val suggestions = state.notifications.filter { it.type == "background" }
        if (suggestions.isEmpty()) Text("No proactive suggestions yet.")
        else LazyColumn(Modifier.fillMaxSize()) {
            items(suggestions, key = { it.id }) { item ->
                ListItem(headlineContent = { Text(item.title) }, supportingContent = { Text(item.body) })
            }
        }
    }
}


@Composable
private fun SmartHomeScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    LaunchedEffect(Unit) { vm.loadSmartHomes() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Smart Home", style = MaterialTheme.typography.headlineSmall)
        Text("POTATO uses a provider adapter. Device-changing actions remain approval-gated.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(12.dp))
        if (state.smartHomes.isEmpty()) {
            Text("No smart-home provider is configured on the backend.")
        } else {
            state.smartHomes.forEach { home ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(home.name, style = MaterialTheme.typography.titleMedium)
                        Text(home.provider)
                        TextButton(onClick = { vm.loadSmartDevices(home.id) }) { Text("Refresh devices") }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.smartDevices, key = { it.id }) { device ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium)
                        Text("${device.kind} • ${device.state}")
                        device.capabilities.forEach { action ->
                            TextButton(onClick = { vm.smartHomeAction(device.id, action) }) { Text(action) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.refreshDeviceContext() }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.refreshDeviceContext() }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.refreshDeviceContext() }
    val locationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val calendarGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    val contactsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) { vm.refreshDeviceContext() }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Device", style = MaterialTheme.typography.headlineSmall)
        Text("Local device information is read on this phone. POTATO does not request unrestricted cross-app control.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(12.dp))
        Button(onClick = vm::refreshDeviceContext, modifier = Modifier.fillMaxWidth()) { Text("Refresh device status") }
        val snapshot = state.deviceContext
        if (snapshot != null) {
            DeviceRow("Device", snapshot.device)
            DeviceRow("Android", snapshot.androidVersion)
            DeviceRow("POTATO", snapshot.appVersion)
            DeviceRow("Battery", snapshot.batteryPercent?.let { "$it%${if (snapshot.charging) " • charging" else ""}" } ?: "Unavailable")
            DeviceRow("Network", if (snapshot.online) snapshot.network else "Offline")
            DeviceRow("Calendar", snapshot.nextCalendarEvent ?: if (calendarGranted) "No upcoming event" else "Permission not granted")
            DeviceRow("Contacts", snapshot.contactCount?.toString() ?: if (contactsGranted) "No data" else "Permission not granted")
            DeviceRow("Location", snapshot.location ?: if (locationGranted) "Unavailable" else "Permission not granted")
            DeviceRow("Foreground app", snapshot.foregroundPackage ?: "Accessibility context not enabled")
        }
        Spacer(Modifier.height(12.dp))
        Text("Optional access", style = MaterialTheme.typography.titleMedium)
        Text("These permissions are requested only when you choose them. POTATO does not continuously collect this information.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        if (!locationGranted) {
            OutlinedButton(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) }, modifier = Modifier.fillMaxWidth()) { Text("Allow location context") }
        }
        if (!calendarGranted) {
            OutlinedButton(onClick = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Allow calendar context") }
        }
        if (!contactsGranted) {
            OutlinedButton(onClick = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Allow contact count") }
        }
        if (locationGranted && calendarGranted && contactsGranted) {
            Text("Optional device permissions are enabled.", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        Text("Clipboard", style = MaterialTheme.typography.titleMedium)
        Text("Clipboard contents are never read automatically. Tap the button when you explicitly want POTATO to read the current text clipboard.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = vm::readClipboardExplicitly, modifier = Modifier.fillMaxWidth()) { Text("Read clipboard now") }
        state.clipboardText?.let { text ->
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(text, Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Computer control", style = MaterialTheme.typography.titleMedium)
                Text("Unrestricted cross-app control remains disabled. Server-side device actions continue to require the existing security and approval pipeline.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun DeviceRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(max = 230.dp))
    }
    HorizontalDivider()
}

@Composable
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

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::newConversation) { Text("New") }
            OutlinedButton(onClick = vm::loadSessions) { Text("Refresh") }
        }
        if (state.sessions.isNotEmpty()) {
            LazyColumn(Modifier.fillMaxWidth().height(72.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.sessions.take(6)) { session ->
                    OutlinedButton(onClick = { vm.selectConversation(session.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(session.title.ifBlank { "Conversation" }, maxLines = 1)
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(selected = state.useWeb, onClick = vm::toggleWeb, label = { Text("Web") })
            OutlinedButton(onClick = { launchCamera(capture, context) { imageFile = it } }) { Text("Camera") }
            OutlinedButton(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Photo") }
            OutlinedButton(onClick = { ocrPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("OCR") }
            OutlinedButton(onClick = {
                if (state.voiceState == "LISTENING") {
                    voice?.stopListening()
                } else if (state.voiceState == "SPEAKING") {
                    voice?.stopSpeaking()
                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    voice?.listen()
                } else {
                    audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) { Text(when (state.voiceState) { "LISTENING" -> "Stop"; "SPEAKING" -> "Stop voice"; else -> "Voice" }) }
            if (state.voicePartialText.isNotBlank()) {
                Text("Hearing: ${state.voicePartialText}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp))
            }
            if (state.streaming) {
                Text("Streaming…")
                OutlinedButton(onClick = vm::cancelResponse) { Text("Stop") }
            } else if (state.busy) Text("Thinking…")
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.messages, key = { "${it.createdAt}-${it.role}-${it.content.hashCode()}" }) { message -> Bubble(message) }
        }

        AnimatedVisibility(state.streaming && state.streamText.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("POTATO", style = MaterialTheme.typography.titleMedium)
                    Text(state.streamText)
                }
            }
        }

        AnimatedVisibility(state.lastVision.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Vision result", style = MaterialTheme.typography.titleMedium)
                    state.lastVisionResult?.let { Text("${it.width} × ${it.height}", style = MaterialTheme.typography.labelSmall) }
                    Text(state.lastVision)
                }
            }
        }
        AnimatedVisibility(state.lastOcrText.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("OCR", style = MaterialTheme.typography.titleMedium)
                    Text(state.lastOcrText)
                }
            }
        }

        AnimatedVisibility(state.error != null) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
        }

        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Talk to POTATO…") },
                maxLines = 5,
                enabled = !state.busy,
            )
            Spacer(Modifier.width(8.dp))
            Button(enabled = draft.isNotBlank() && !state.busy, onClick = onSend) { Text("Send") }
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage) {
    val user = message.role == "user"
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = if (user) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 360.dp),
        ) {
            Text(message.content, Modifier.padding(14.dp))
        }
    }
}

@Composable
private fun MemoryScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    var query by remember { mutableStateOf("") }
    var newMemory by remember { mutableStateOf("") }
    var memoryType by remember { mutableStateOf("semantic") }
    var importance by remember { mutableStateOf(0.7f) }
    var expiresAt by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(true) }
    var exportResult by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.loadMemory() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Long-term memory", style = MaterialTheme.typography.headlineSmall)
        Text("POTATO only stores long-term memory when you explicitly choose to remember it. Ordinary conversation is not silently saved as memory.", Modifier.padding(vertical = 6.dp))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(query, { query = it }, Modifier.weight(1f), placeholder = { Text("Search memory") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.loadMemory(query) }) { Text("Search") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(newMemory, { newMemory = it }, Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("What should POTATO remember?") })
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(memoryType, { memoryType = it }, Modifier.weight(1f), label = { Text("Category") }, singleLine = true)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(expiresAt, { expiresAt = it }, Modifier.weight(1f), label = { Text("Expires ISO-8601 (optional)") }, singleLine = true)
        }
        Text("Importance ${"%.2f".format(importance)}", Modifier.padding(top = 6.dp))
        Slider(value = importance, onValueChange = { importance = it }, valueRange = 0f..1f)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = consent, onCheckedChange = { consent = it })
            Text("I explicitly consent to saving this memory", Modifier.padding(start = 8.dp))
        }
        Row {
            Button(enabled = newMemory.isNotBlank() && consent, onClick = {
                vm.remember(newMemory, memoryType.ifBlank { "semantic" }, importance.toDouble(), expiresAt.ifBlank { null })
                newMemory = ""
            }) { Text("Remember") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { vm.exportMemory { exportResult = it } }) { Text("Export") }
        }
        if (exportResult.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(exportResult, Modifier.verticalScroll(rememberScrollState()), maxLines = 5)
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(state.memory, key = { it.id }) { memory ->
                ListItem(
                    headlineContent = { Text(memory.content) },
                    supportingContent = {
                        val expiry = memory.expiresAt?.let { " • expires $it" }.orEmpty()
                        Text("${memory.type} • relevance ${"%.2f".format(memory.relevance)} • importance ${"%.2f".format(memory.importance)} • confidence ${"%.2f".format(memory.confidence)}$expiry")
                    },
                    trailingContent = {
                        TextButton(onClick = { vm.deleteMemory(memory.id) }) { Text("Forget") }
                    },
                )
            }
        }
    }
}

@Composable
private fun TaskScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    var request by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }
    LaunchedEffect(Unit) { vm.loadTasks() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tasks & Planner", style = MaterialTheme.typography.headlineSmall)
        Text("Create tasks directly or use the planner for multi-step work. Due dates, reminders, recurrence, dependencies, status, and history are stored server-side.", Modifier.padding(vertical = 8.dp))
        OutlinedTextField(request, { request = it }, Modifier.fillMaxWidth(), minLines = 2, enabled = !state.busy, label = { Text("Task description") })
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("low", "normal", "high", "urgent").forEach { value ->
                FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.createTask(request, priority); request = "" }, enabled = request.isNotBlank() && !state.busy) { Text("Add Task") }
            OutlinedButton(onClick = { vm.planAndExecute(request) }, enabled = request.isNotBlank() && !state.busy) { Text("Plan & Execute") }
            OutlinedButton(onClick = { vm.loadTasks() }, enabled = !state.busy) { Text("Refresh") }
        }
        if (state.taskId != null) {
            Text("Active: ${state.taskId} • ${state.taskStatus}", Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.refreshTask() }) { Text("Details") }
                OutlinedButton(onClick = { vm.cancelTask() }, enabled = state.taskStatus !in setOf("completed", "failed", "cancelled")) { Text("Cancel") }
            }
        }
        if (state.taskResult.isNotBlank()) {
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(state.taskResult, Modifier.padding(12.dp))
            }
        }
        Text("Task list", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.tasks, key = { it.id }) { task ->
                TaskCard(task, vm)
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskStatus, vm: JarvisViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(task.description, style = MaterialTheme.typography.titleMedium)
            Text("${task.priority} • ${task.status}${task.dueAt?.let { " • due $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
            if (task.recurrence != "none") Text("Repeats: ${task.recurrence}", style = MaterialTheme.typography.bodySmall)
            if (task.notes.isNotBlank()) Text(task.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                if (task.status !in setOf("completed", "cancelled")) {
                    OutlinedButton(onClick = { vm.setTaskStatus(task.id, if (task.status == "in_progress") "completed" else "in_progress") }) {
                        Text(if (task.status == "in_progress") "Complete" else "Start")
                    }
                }
                TextButton(onClick = { vm.deleteTaskById(task.id) }, enabled = task.status != "in_progress") { Text("Delete") }
            }
        }
    }
}


@Composable
private fun ToolsScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    val context = LocalContext.current
    var selectedContent by remember { mutableStateOf("") }
    var webQuery by remember { mutableStateOf("") }
    var fileQuery by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        vm.loadTools()
        vm.loadFiles()
        vm.loadAutomations()
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val name = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "upload.bin" } ?: "upload.bin"
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val length = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
                    ?.takeIf { it >= 0 }
                vm.uploadFile(
                    name = name,
                    mime = mime,
                    length = length,
                    openStream = {
                        context.contentResolver.openInputStream(uri)
                            ?: error("Unable to open selected file")
                    },
                )
            }.onFailure { selectedContent = "Upload failed: ${it.message}" }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tools & web", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = webQuery,
                onValueChange = { webQuery = it.take(2_000) },
                label = { Text("Search the web") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { vm.webSearch(webQuery) }, enabled = webQuery.isNotBlank() && !state.webSearching) {
                if (state.webSearching) CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp) else Text("Search")
            }
        }
        state.webResult?.let { result ->
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(result.answer)
                    if (result.citations.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Sources", style = MaterialTheme.typography.titleSmall)
                        result.citations.forEach { citation ->
                            Text(
                                citation.title,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(citation.url))) }.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = fileQuery,
                onValueChange = { fileQuery = it.take(500) },
                label = { Text("Search files") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { vm.searchFiles(fileQuery) }, enabled = fileQuery.isNotBlank() && !state.fileSearching) {
                if (state.fileSearching) CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp) else Text("Search")
            }
        }
        state.fileSearchResults.take(8).forEach { result ->
            Card(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Column(Modifier.padding(10.dp)) {
                    Text(result.name, style = MaterialTheme.typography.titleSmall)
                    Text("Score ${"%.1f".format(result.score)} • chunk ${result.chunkIndex}", style = MaterialTheme.typography.labelSmall)
                    Text(result.snippet.take(900), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        Button(onClick = { picker.launch(arrayOf(
            "text/plain", "text/markdown", "text/csv", "application/json", "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "image/jpeg", "image/png", "image/webp", "image/gif", "text/*",
        )) }) { Text("Upload document") }
        LazyColumn(Modifier.weight(1f)) {
            item { Text("Tools", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
            items(state.tools, key = { it.name }) { tool: ToolInfo ->
                ListItem(
                    headlineContent = { Text(tool.name) },
                    supportingContent = { Text("Risk ${tool.risk} • ${tool.description} • ${tool.timeoutSeconds}s timeout${if (tool.retryable) " • retryable" else ""}") },
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { Text("Automations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
            items(state.automations, key = { it.id }) { automation ->
                ListItem(
                    headlineContent = { Text(automation.name) },
                    supportingContent = { Text("${automation.triggerType} • ${automation.actionCount} action(s) • ${automation.failurePolicy} • ${automation.maxRunsPerHour}/hour") },
                    trailingContent = { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = { vm.toggleAutomation(automation.id, !automation.enabled) }) { Text(if (automation.enabled) "Disable" else "Enable") }
                        OutlinedButton(onClick = { vm.runAutomation(automation.id) }) { Text("Run") }
                    } },
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { Text("Files", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp)) }
            items(state.files, key = { it.id }) { file: FileItem ->
                ListItem(
                    headlineContent = { Text(file.name) },
                    supportingContent = { Text("${file.mime} • ${file.size} bytes • ${file.extractionStatus}") },
                    trailingContent = { OutlinedButton(onClick = { vm.readFile(file.id) { selectedContent = it } }) { Text("Read") } },
                )
            }
        }
        AnimatedVisibility(selectedContent.isNotBlank()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Text(selectedContent.take(12_000), Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun SecurityScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    val activity = LocalActivity.current as? FragmentActivity
    LaunchedEffect(Unit) { vm.loadApprovals() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Security & approvals", style = MaterialTheme.typography.headlineSmall)
        Text("Risk 0–1 can run automatically. Risk 2–3 requires explicit approval; risk 3 additionally requires device authentication.", Modifier.padding(vertical = 8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(state.approvals, key = { it.id }) { approval ->
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(approval.tool, style = MaterialTheme.typography.titleMedium)
                        Text("Risk ${approval.risk}")
                        Text(approval.arguments, Modifier.padding(vertical = 6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                enabled = approval.risk < 3 || activity != null,
                                onClick = {
                                    if (approval.risk >= 3) {
                                        val host = activity ?: return@Button
                                        vm.biometricChallenge(
                                            approval.id,
                                            onReady = { challenge ->
                                                authenticate(host, approval.id, challenge) { signature ->
                                                    vm.approveWithBiometric(approval.id, challenge, signature)
                                                }
                                            },
                                        )
                                    } else {
                                        vm.approve(approval.id, true)
                                    }
                                },
                            ) { Text(if (approval.risk >= 3) "Authenticate" else "Approve") }
                            OutlinedButton(onClick = { vm.approve(approval.id, false) }) { Text("Deny") }
                        }
                    }
                }
            }
        }
    }
}

private fun authenticate(activity: FragmentActivity, approvalId: String, challenge: String, onSuccess: (String) -> Unit) {
    val store = com.potato.jarvis.core.SecureTokenStore(activity)
    val signature = try {
        store.biometricSignature()
    } catch (error: Throwable) {
        Toast.makeText(activity, "Secure biometric key is unavailable. Reconnect this device and try again.", Toast.LENGTH_LONG).show()
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val cryptoSignature = result.cryptoObject?.signature
            if (cryptoSignature == null) {
                Toast.makeText(activity, "Biometric approval could not initialize a secure signature.", Toast.LENGTH_LONG).show()
                return
            }
            try {
                cryptoSignature.update(approvalPayload(approvalId, challenge).toByteArray(StandardCharsets.UTF_8))
                onSuccess(Base64.encodeToString(cryptoSignature.sign(), Base64.NO_WRAP))
            } catch (_: Throwable) {
                Toast.makeText(activity, "Biometric approval failed to create a secure assertion.", Toast.LENGTH_LONG).show()
            }
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Toast.makeText(activity, "Biometric approval cancelled or unavailable.", Toast.LENGTH_SHORT).show()
        }
    }
    val prompt = BiometricPrompt(activity, executor, callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("POTATO approval")
        .setSubtitle("Authenticate to approve this sensitive action")
        .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
    prompt.authenticate(info, BiometricPrompt.CryptoObject(signature))
}

private fun approvalPayload(approvalId: String, challenge: String): String = "$approvalId:$challenge"

@Composable
private fun SetupScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    var url by remember { mutableStateOf(vm.backendUrl()) }
    var token by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text("🥔 POTATO", style = MaterialTheme.typography.displaySmall)
        Text("Connect your AI assistant", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Backend URL") },
            supportingText = { Text("Emulator: http://10.0.2.2:8000 • Phone: use your computer's LAN IP • Release: HTTPS") },
            singleLine = true,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API token") },
            visualTransformation = PasswordVisualTransformation(),
            supportingText = { Text("Stored using Android Keystore. Leave blank only when the backend explicitly allows anonymous access.") },
            singleLine = true,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { vm.testConnection(url, token) },
            enabled = !state.connecting && url.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.connecting) CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp) else Text("Test & Connect")
        }
        if (state.connectionMessage.isNotBlank()) {
            Text(state.connectionMessage, modifier = Modifier.padding(top = 12.dp))
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(28.dp))
        Text("POTATO V${BuildConfig.POTATO_VERSION}", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SettingsScreen(vm: JarvisViewModel, state: JarvisViewModel.State) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(vm.backendUrl()) }
    var token by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var deleteConfirmation by remember { mutableStateOf("") }
    var name by remember(state.personality.name) { mutableStateOf(state.personality.name) }
    var instructions by remember(state.personality.instructions) { mutableStateOf(state.personality.instructions) }
    var profile by remember(state.personality) { mutableStateOf(state.personality) }
    var accessibilityConsent by rememberSaveable { mutableStateOf(AccessibilityConsent.isAccepted(context)) }
    var foregroundPackage by remember {
        mutableStateOf(
            context.getSharedPreferences(PotatoAccessibilityService.PREFS, Context.MODE_PRIVATE)
                .getString(PotatoAccessibilityService.KEY_FOREGROUND_PACKAGE, null)
                .orEmpty()
        )
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Personality", style = MaterialTheme.typography.titleLarge)
        Text("These preferences shape how POTATO responds. Security permissions are never overridden by personality.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Assistant name") })
        PersonalityChoice("Style", profile.style, listOf("warm", "professional", "playful", "direct", "calm")) { profile = profile.copy(style = it) }
        PersonalityChoice("Formality", profile.formality, listOf("casual", "balanced", "formal")) { profile = profile.copy(formality = it) }
        PersonalityChoice("Humor", profile.humor, listOf("none", "light", "frequent")) { profile = profile.copy(humor = it) }
        PersonalityChoice("Verbosity", profile.verbosity, listOf("brief", "concise", "detailed", "thorough")) { profile = profile.copy(verbosity = it) }
        PersonalityChoice("Proactivity", profile.proactivity, listOf("off", "permission_based", "helpful")) { profile = profile.copy(proactivity = it) }
        PersonalityChoice("Response style", profile.responseStyle, listOf("clear", "conversational", "structured", "technical")) { profile = profile.copy(responseStyle = it) }
        PersonalityChoice("Greeting", profile.greeting, listOf("natural", "minimal", "friendly", "none")) { profile = profile.copy(greeting = it) }
        PersonalityChoice("Units", profile.units, listOf("metric", "imperial", "auto")) { profile = profile.copy(units = it) }
        PersonalityChoice("Language", profile.language, listOf("auto", "en", "es", "fr", "de", "it", "pt", "ja", "ko", "zh")) { profile = profile.copy(language = it) }
        PersonalityChoice("Voice profile", profile.voice, listOf("default", "calm", "bright", "deep", "neutral")) { profile = profile.copy(voice = it) }
        OutlinedTextField(
            instructions,
            { instructions = it.take(500) },
            Modifier.fillMaxWidth(),
            label = { Text("Extra behavior instructions") },
            supportingText = { Text("Optional, user-authored instructions. Max 500 characters.") },
            minLines = 3,
        )
        Button(
            onClick = {
                vm.updatePersonality(profile.copy(name = name.trim().ifBlank { "POTATO" }, instructions = instructions.trim()))
                saved = "Personality saved"
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Save personality") }
        Text(saved, Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(20.dp))
        Text("Voice", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Voice mode")
                Text("Automatically speak POTATO replies", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = state.voiceMode, onCheckedChange = vm::setVoiceMode)
        }
        if (state.voiceError.isNotBlank()) Text(state.voiceError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text("Speech speed: ${String.format(java.util.Locale.US, "%.1fx", state.voiceRate)}")
        Slider(value = state.voiceRate, onValueChange = vm::setVoiceRate, valueRange = 0.5f..2.0f, steps = 5)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("Backend URL") })
        OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("API token") }, visualTransformation = PasswordVisualTransformation(), supportingText = { Text(if (vm.hasToken()) "A token is stored securely on this device. Leave this blank to keep it." else "No token is stored.") })
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.connection(url, token); token = ""; saved = "Connection updated" }) { Text("Save connection") }
            if (vm.hasToken()) OutlinedButton(onClick = { vm.clearToken(); saved = "Stored token cleared" }) { Text("Clear token") }
        }
        Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Optional accessibility context", style = MaterialTheme.typography.titleMedium)
                Text(
                    "If you enable POTATO in Android Accessibility settings, POTATO receives only the foreground app package name when the active window changes. " +
                        "It does not read screen text, inspect passwords or financial information, click controls, type, perform gestures, change settings, or control other apps.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(checked = accessibilityConsent, onCheckedChange = { accessibilityConsent = it })
                    Text("I understand and choose to enable this optional context.", modifier = Modifier.padding(start = 6.dp))
                }
                Button(
                    onClick = {
                        AccessibilityConsent.record(context, true)
                        accessibilityConsent = true
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    enabled = accessibilityConsent,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                ) { Text("Open Accessibility settings") }
                if (AccessibilityConsent.isAccepted(context)) {
                    OutlinedButton(
                        onClick = {
                            AccessibilityConsent.record(context, false)
                            accessibilityConsent = false
                            foregroundPackage = ""
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) { Text("Revoke POTATO accessibility context consent") }
                }
                OutlinedButton(
                    onClick = {
                        foregroundPackage = context
                            .getSharedPreferences(PotatoAccessibilityService.PREFS, Context.MODE_PRIVATE)
                            .getString(PotatoAccessibilityService.KEY_FOREGROUND_PACKAGE, null)
                            .orEmpty()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                ) { Text("Refresh foreground-app context") }
                Text(
                    if (foregroundPackage.isBlank()) "No foreground-app context recorded." else "Last foreground app: $foregroundPackage",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    "Cross-app computer control remains disabled. Server-side device actions still require the existing approval pipeline.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = vm::loadDiagnostics) { Text("Run diagnostics") }
        AnimatedVisibility(state.diagnostics.isNotBlank()) {
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(state.diagnostics, Modifier.padding(12.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Privacy & data", style = MaterialTheme.typography.titleLarge)
        Text("Export your POTATO data or permanently delete server-side personal data and credentials.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { vm.exportPrivacyData { exported -> sharePrivacyExport(context, exported); saved = "Privacy export ready to share" } }, modifier = Modifier.padding(top = 8.dp)) { Text("Export data") }
        OutlinedTextField(
            deleteConfirmation,
            { deleteConfirmation = it.take(64) },
            Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Type DELETE ALL POTATO DATA") },
        )
        Button(
            onClick = { vm.deletePrivacyData { deleteConfirmation = ""; saved = "Server-side POTATO data deleted" } },
            enabled = deleteConfirmation == "DELETE ALL POTATO DATA",
            modifier = Modifier.padding(top = 8.dp),
        ) { Text("Delete all data permanently") }
        Text("Deletion is permanent and includes server-side personal data, uploaded files, approvals, device credentials, and smart-home credentials.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        Text("POTATO V${BuildConfig.POTATO_VERSION}", Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun PersonalityChoice(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(option.replace('_', ' ')) })
            }
        }
    }
}

private fun copyUriToVisionCache(context: Context, uri: Uri): File? {
    val directory = File(context.cacheDir, "vision").apply { mkdirs() }
    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
    val extension = when (mime.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    val file = File(directory, "potato_${System.currentTimeMillis()}.$extension")
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
        } ?: error("Unable to read selected image")
        require(file.length() in 1..10_000_000) { "Selected image is too large." }
        file
    }.getOrElse {
        file.delete()
        null
    }
}

private fun launchCamera(capture: ActivityResultLauncher<Uri>, context: Context, onFile: (File) -> Unit) {
    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(directory, "potato_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    onFile(file)
    capture.launch(uri)
}


private fun sharePrivacyExport(context: Context, json: String) {
    val directory = File(context.cacheDir, "privacy").apply { mkdirs() }
    val file = File(directory, "potato-privacy-export.json")
    file.writeText(json, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export POTATO data"))
}
