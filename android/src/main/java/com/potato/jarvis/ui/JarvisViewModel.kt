package com.potato.jarvis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.potato.jarvis.automation.PotatoWorker
import com.potato.jarvis.core.ApiException
import com.potato.jarvis.core.BackendUrlPolicy
import com.potato.jarvis.core.Approval
import com.potato.jarvis.core.AutomationItem
import com.potato.jarvis.core.ChatMessage
import com.potato.jarvis.core.Diagnostic
import com.potato.jarvis.core.DeviceContextSnapshot
import com.potato.jarvis.core.FileItem
import com.potato.jarvis.core.FileSearchResult
import com.potato.jarvis.core.Health
import com.potato.jarvis.core.JarvisApi
import com.potato.jarvis.core.MemoryItem
import com.potato.jarvis.core.NotificationItem
import com.potato.jarvis.core.ProactiveSettings
import com.potato.jarvis.core.PersonalityProfile
import com.potato.jarvis.core.SecureTokenStore
import com.potato.jarvis.core.SessionInfo
import com.potato.jarvis.core.StreamEvent
import com.potato.jarvis.core.SmartHomeHome
import com.potato.jarvis.core.SmartHomeDevice
import com.potato.jarvis.core.ToolInfo
import com.potato.jarvis.core.TaskStatus
import org.json.JSONObject
import com.potato.jarvis.core.WebSearchResult
import com.potato.jarvis.core.VisionResult
import com.potato.jarvis.db.JarvisDb
import com.potato.jarvis.device.DeviceContext
import com.potato.jarvis.vision.VisionProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class JarvisViewModel(app: Application) : AndroidViewModel(app) {
    private val db = JarvisDb(app)
    private val tokenStore = SecureTokenStore(app)
    private val visionProcessor = VisionProcessor()
    private val deviceContext = DeviceContext(app)

    private var api = buildApi()
    private var streamJob: Job? = null

    data class State(
        val messages: List<ChatMessage> = emptyList(),
        val busy: Boolean = false,
        val online: Boolean = false,
        val error: String? = null,
        val sessionId: String? = null,
        val useWeb: Boolean = false,
        val voiceState: String = "IDLE",
        val voicePartialText: String = "",
        val voiceError: String = "",
        val voiceMode: Boolean = true,
        val voiceRate: Float = 1.0f,
        val memory: List<MemoryItem> = emptyList(),
        val personality: PersonalityProfile = PersonalityProfile(),
        val approvals: List<Approval> = emptyList(),
        val tools: List<ToolInfo> = emptyList(),
        val files: List<FileItem> = emptyList(),
        val fileSearchResults: List<FileSearchResult> = emptyList(),
        val fileSearching: Boolean = false,
        val diagnostics: String = "",
        val lastVision: String = "",
        val lastVisionResult: VisionResult? = null,
        val lastOcrText: String = "",
        val tasks: List<TaskStatus> = emptyList(),
        val notifications: List<NotificationItem> = emptyList(),
        val notificationsEnabled: Boolean = true,
        val proactiveSettings: ProactiveSettings = ProactiveSettings(),
        val proactiveMessage: String = "",
        val automations: List<AutomationItem> = emptyList(),
        val taskResult: String = "",
        val taskId: String? = null,
        val taskStatus: String = "",
        val setupRequired: Boolean = false,
        val connecting: Boolean = false,
        val connectionMessage: String = "",
        val backendVersion: String = "",
        val sessions: List<SessionInfo> = emptyList(),
        val agentMode: String = "",
        val agentTraceId: String = "",
        val streaming: Boolean = false,
        val streamText: String = "",
        val streamTraceId: String = "",
        val webResult: WebSearchResult? = null,
        val webSearching: Boolean = false,
        val deviceContext: DeviceContextSnapshot? = null,
        val clipboardText: String? = null,
        val smartHomes: List<SmartHomeHome> = emptyList(),
        val smartDevices: List<SmartHomeDevice> = emptyList(),
        val smartHomeId: String? = null,

    )

    private val _state = MutableStateFlow(
        State(
            messages = db.load(),
            setupRequired = !hasBackendUrl(),
            voiceMode = db.getSetting("voice_mode", "true") == "true",
            voiceRate = db.getSetting("voice_rate", "1.0").toFloatOrNull()?.coerceIn(0.5f, 2.0f) ?: 1.0f,
        ),
    )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        migrateLegacyToken()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            "potato-notifications",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<PotatoWorker>(15, TimeUnit.MINUTES).build(),
        )
        if (hasBackendUrl()) {
            loadNotifications()
            health()
            loadSessions()
            loadPersonality()
        } else update { it.copy(connectionMessage = "Connect POTATO to a backend to begin.") }
    }

    private fun buildApi(): JarvisApi = JarvisApi(backendUrl(), tokenStore.read())

    private fun migrateLegacyToken() {
        val legacy = db.getSetting("api_token").trim()
        if (legacy.isNotBlank() && tokenStore.read().isNullOrBlank()) tokenStore.save(legacy)
        if (legacy.isNotBlank()) db.deleteSetting("api_token")
    }

    private fun hasBackendUrl(): Boolean = db.getSetting("backend_url").trim().isNotBlank()

    fun testConnection(url: String, token: String) = viewModelScope.launch(Dispatchers.IO) {
        val clean = normalizeUrl(url)
        update { it.copy(connecting = true, error = null, connectionMessage = "Connecting…") }
        runCatching {
            val candidate = JarvisApi(clean, token.trim().ifBlank { null })
            val health = candidate.health()
            candidate.sessions() // authenticated endpoint: rejects an invalid bearer token
            Triple(candidate, health, token.trim())
        }.fold(
            onSuccess = { (candidate, health, suppliedToken) ->
                db.putSetting("backend_url", clean)
                if (suppliedToken.isNotBlank()) tokenStore.save(suppliedToken)
                api = JarvisApi(clean, tokenStore.read())
                val biometricWarning = if (!tokenStore.read().isNullOrBlank()) {
                    runCatching { api.registerBiometricKey(tokenStore.biometricPublicKeyBase64()) }
                        .exceptionOrNull()?.let { " High-risk biometric approvals are unavailable: ${friendlyError(it)}" }
                        .orEmpty()
                } else {
                    ""
                }
                val message = "Connected to POTATO ${health.version.ifBlank { "backend" }}.$biometricWarning"
                update { it.copy(online = health.status == "ok", connecting = false, setupRequired = false, connectionMessage = message, backendVersion = health.version, error = null) }
                loadSessions()
                loadPersonality()
                // Health is public; verify authenticated access when a token is supplied.
                if (tokenStore.read().isNullOrBlank()) {
                    update { it.copy(connectionMessage = "$message Authentication token still needs to be configured unless the server explicitly allows anonymous access.") }
                }
            },
            onFailure = { error ->
                update { it.copy(online = false, connecting = false, setupRequired = true, connectionMessage = friendlyError(error), error = friendlyError(error)) }
            },
        )
    }

    fun health() = viewModelScope.launch(Dispatchers.IO) {
        if (!hasBackendUrl()) return@launch
        update { it.copy(connecting = true) }
        runCatching { api.health() }
            .onSuccess { health -> update { it.copy(online = health.status == "ok", connecting = false, setupRequired = false, backendVersion = health.version, error = null, connectionMessage = "Connected") } }
            .onFailure { error -> update { it.copy(online = false, connecting = false, error = friendlyError(error), connectionMessage = friendlyError(error)) } }
    }

    fun loadSessions() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.sessions() }.onSuccess { items -> update { it.copy(sessions = items) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun newConversation() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.createSession() }.onSuccess { id ->
            db.clearMessages()
            update { it.copy(sessionId = id, messages = emptyList(), error = null) }
            loadSessions()
        }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun selectConversation(id: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.sessionMessages(id) }.onSuccess { messages ->
            db.replaceMessages(messages)
            update { it.copy(sessionId = id, messages = messages, error = null) }
        }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun toggleWeb() = update { it.copy(useWeb = !it.useWeb) }

    fun runAgent(text: String, speak: ((String) -> Unit)? = null) {
        val clean = text.trim()
        val snapshot = _state.value
        if (clean.isEmpty() || snapshot.busy || snapshot.setupRequired) return
        val user = ChatMessage("user", clean)
        db.save(user)
        update { it.copy(messages = it.messages + user, busy = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { api.agentRun(clean, snapshot.sessionId, snapshot.useWeb) }
                .onSuccess { result ->
                    val mode = result.optString("mode")
                    val status = result.optString("status")
                    val reply = result.optString("reply")
                    val display = if (mode == "chat") reply else "Task ${result.optString("task_id")} is ${status.replace('_', ' ')}."
                    if (display.isNotBlank()) {
                        val answer = ChatMessage("assistant", display)
                        db.save(answer)
                        update { it.copy(messages = it.messages + answer, busy = false, online = true, sessionId = result.optString("session_id").ifBlank { snapshot.sessionId }, agentMode = mode, agentTraceId = result.optString("trace_id"), taskId = result.optString("task_id").ifBlank { it.taskId }, taskStatus = status, taskResult = result.toString(2), error = null) }
                        if (speak != null) withContext(Dispatchers.Main) { speak(display) }
                    } else {
                        update { it.copy(busy = false, online = true, agentMode = mode, agentTraceId = result.optString("trace_id"), taskStatus = status, taskResult = result.toString(2), error = null) }
                    }
                }
                .onFailure { error -> update { it.copy(busy = false, error = friendlyError(error)) } }
        }
    }

    fun send(text: String, speak: ((String) -> Unit)? = null) {
        val clean = text.trim()
        val snapshot = _state.value
        if (clean.isEmpty() || snapshot.busy) return
        if (snapshot.setupRequired) {
            update { it.copy(error = "Connect POTATO in Settings before starting a conversation.") }
            return
        }
        val user = ChatMessage("user", clean)
        db.save(user)
        update { it.copy(messages = it.messages + user, busy = true, streaming = true, streamText = "", streamTraceId = "", error = null) }
        streamJob?.cancel()
        streamJob = viewModelScope.launch(Dispatchers.IO) {
            val accumulated = StringBuilder()
            var finalReply = ""
            var finalSession = snapshot.sessionId
            var finalTrace = ""
            try {
                api.streamChat(snapshot.sessionId, clean, snapshot.useWeb) { event ->
                    when (event.type) {
                        "delta" -> {
                            accumulated.append(event.delta)
                            update { it.copy(streamText = accumulated.toString(), streamTraceId = event.traceId) }
                        }
                        "done" -> {
                            finalReply = event.reply.ifBlank { accumulated.toString() }
                            finalSession = event.sessionId.ifBlank { snapshot.sessionId }
                            finalTrace = event.traceId
                        }
                        "error" -> throw IllegalStateException(event.error.ifBlank { "Streaming request failed." })
                    }
                }
                if (finalReply.isBlank()) finalReply = accumulated.toString()
                if (finalReply.isNotBlank()) {
                    val answer = ChatMessage("assistant", finalReply)
                    db.save(answer)
                    update { it.copy(messages = it.messages + answer, busy = false, streaming = false, streamText = "", streamTraceId = finalTrace, online = true, sessionId = finalSession, error = null) }
                    if (speak != null) withContext(Dispatchers.Main) { speak(finalReply) }
                } else {
                    update { it.copy(busy = false, streaming = false, streamText = "", online = true, error = "POTATO returned an empty response.") }
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                update { it.copy(busy = false, streaming = false, streamText = "", error = null) }
            } catch (error: Throwable) {
                update { it.copy(busy = false, streaming = false, streamText = "", error = friendlyError(error), online = error !is ApiException || error.statusCode < 500) }
            }
        }
    }

    fun cancelResponse() {
        streamJob?.cancel()
        streamJob = null
        update { it.copy(busy = false, streaming = false, streamText = "", error = null) }
    }

    fun setVoiceState(state: String) = update { it.copy(voiceState = state, voiceError = if (state == "ERROR") it.voiceError else "") }
    fun setVoicePartialText(text: String) = update { it.copy(voicePartialText = text) }
    fun setVoiceError(message: String) = update { it.copy(voiceError = message, voiceState = "ERROR") }
    fun setVoiceMode(enabled: Boolean) {
        db.putSetting("voice_mode", enabled.toString())
        update { it.copy(voiceMode = enabled) }
    }
    fun setVoiceRate(rate: Float) {
        val safe = rate.coerceIn(0.5f, 2.0f)
        db.putSetting("voice_rate", safe.toString())
        update { it.copy(voiceRate = safe) }
    }
    fun loadPersonality() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.personality() }
            .onSuccess { profile -> update { it.copy(personality = profile, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun updatePersonality(profile: PersonalityProfile) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.updatePersonality(profile) }
            .onSuccess { saved -> update { it.copy(personality = saved, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadMemory(query: String = "") = viewModelScope.launch(Dispatchers.IO) { runCatching { api.memories(query) }.onSuccess { items -> update { it.copy(memory = items, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun remember(content: String, type: String = "semantic", importance: Double = 0.5, expiresAt: String? = null) = viewModelScope.launch(Dispatchers.IO) { runCatching { api.addMemory(content, type, importance, expiresAt = expiresAt) }.onSuccess { loadMemory() }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun deleteMemory(id: String) = viewModelScope.launch(Dispatchers.IO) { runCatching { api.deleteMemory(id) }.onSuccess { loadMemory() }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun exportMemory(onDone: (String) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        val result = runCatching { api.exportMemory().toString(2) }.fold({ it }, { "Export failed: ${friendlyError(it)}" })
        withContext(Dispatchers.Main) { onDone(result) }
    }
    fun loadApprovals() = viewModelScope.launch(Dispatchers.IO) { runCatching { api.approvals() }.onSuccess { items -> update { it.copy(approvals = items, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun biometricChallenge(id: String, onReady: (String) -> Unit, onFailure: (String) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            api.biometricChallenge(id).optString("challenge")
                .takeIf { it.isNotBlank() }
                ?: error("Biometric challenge was empty")
        }
            .onSuccess { challenge -> withContext(Dispatchers.Main) { onReady(challenge) } }
            .onFailure { error -> withContext(Dispatchers.Main) { onFailure(friendlyError(error)) } }
    }

    fun approve(id: String, allow: Boolean) = viewModelScope.launch(Dispatchers.IO) { runCatching { api.approve(id, allow) }.onSuccess { loadApprovals() }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }

    fun approveWithBiometric(id: String, challenge: String, signature: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.approve(id, true, challenge, signature) }
            .onSuccess { loadApprovals() }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }
    fun webSearch(query: String) = viewModelScope.launch(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) return@launch
        update { it.copy(webSearching = true, error = null) }
        runCatching { api.webSearch(clean) }
            .onSuccess { result -> update { it.copy(webResult = result, webSearching = false, error = null) } }
            .onFailure { e -> update { it.copy(webSearching = false, error = friendlyError(e)) } }
    }

    fun loadTools() = viewModelScope.launch(Dispatchers.IO) { runCatching { api.tools() }.onSuccess { items -> update { it.copy(tools = items, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun loadFiles() = viewModelScope.launch(Dispatchers.IO) { runCatching { api.listFiles() }.onSuccess { items -> update { it.copy(files = items, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } } }
    fun searchFiles(query: String) = viewModelScope.launch(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) return@launch
        update { it.copy(fileSearching = true, error = null) }
        runCatching { api.searchFiles(clean) }
            .onSuccess { results -> update { it.copy(fileSearchResults = results, fileSearching = false) } }
            .onFailure { e -> update { it.copy(fileSearching = false, error = friendlyError(e)) } }
    }

    fun reindexFile(id: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.reindexFile(id) }
            .onSuccess { loadFiles() }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun uploadFile(name: String, mime: String, length: Long?, openStream: () -> InputStream, onDone: (Boolean) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { openStream().use { api.uploadStream(name, mime, length, it) } }
            .onSuccess { loadFiles(); withContext(Dispatchers.Main) { onDone(true) } }
            .onFailure { e -> update { it.copy(error = "Upload failed: ${friendlyError(e)}") }; withContext(Dispatchers.Main) { onDone(false) } }
    }

    fun readFile(id: String, onDone: (String) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val result = runCatching { api.readFile(id) }.fold({ it }, { "Read failed: ${friendlyError(it)}" })
        withContext(Dispatchers.Main) { onDone(result) }
    }

    fun exportPrivacyData(onDone: (String) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.exportPrivacyData().toString(2) }
            .onSuccess { withContext(Dispatchers.Main) { onDone(it) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun deletePrivacyData(onDone: () -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.deletePrivacyData() }
            .onSuccess {
                db.clearMessages()
                loadMemory(); loadTasks(); loadNotifications(); loadAutomations(); loadSessions()
                withContext(Dispatchers.Main) { onDone() }
            }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadDiagnostics() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.diagnostics().raw }.onSuccess { text -> update { it.copy(diagnostics = text, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun planAndExecute(request: String) = viewModelScope.launch(Dispatchers.IO) {
        update { it.copy(busy = true, taskResult = "") }
        runCatching { api.createPlan(request, _state.value.sessionId) }.fold(
            onSuccess = { plan ->
                runCatching { api.executePlan(plan.optString("task_id")) }
                    .onSuccess { result ->
                        val taskId = plan.optString("task_id").takeIf { it.isNotBlank() }
                        update { it.copy(taskId = taskId, taskStatus = result.optString("status"), taskResult = result.toString(2), busy = false, error = null) }
                    }
                    .onFailure { e -> update { it.copy(taskResult = friendlyError(e), busy = false, error = friendlyError(e)) } }
            },
            onFailure = { e -> update { it.copy(taskResult = friendlyError(e), busy = false, error = friendlyError(e)) } },
        )
    }

    fun loadSmartHomes() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.smartHomeHomes() }.onSuccess { homes ->
            update { it.copy(smartHomes = homes, smartHomeId = it.smartHomeId ?: homes.firstOrNull()?.id) }
            val id = _state.value.smartHomeId
            if (!id.isNullOrBlank()) loadSmartDevices(id)
        }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadSmartDevices(homeId: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.smartHomeDevices(homeId) }.onSuccess { devices -> update { it.copy(smartHomeId = homeId, smartDevices = devices, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun smartHomeAction(deviceId: String, action: String) = viewModelScope.launch(Dispatchers.IO) {
        val homeId = _state.value.smartHomeId ?: return@launch
        runCatching { api.smartHomeAction(homeId, deviceId, action) }.onSuccess { result ->
            val status = result.optString("status")
            update { it.copy(connectionMessage = if (status == "waiting_for_approval") "Smart-home action is waiting for approval." else "Smart-home action $status") }
            loadSmartDevices(homeId)
        }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun refreshDeviceContext() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { deviceContext.snapshot(includeCalendar = true, includeContacts = true, includeLocation = true) }
            .onSuccess { snapshot -> update { it.copy(deviceContext = snapshot, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun readClipboardExplicitly() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { deviceContext.readClipboard() }
            .onSuccess { value -> update { it.copy(clipboardText = value, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadProactiveSettings() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.proactiveSettings() }
            .onSuccess { value -> update { it.copy(proactiveSettings = value, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun saveProactiveSettings(settings: ProactiveSettings) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.updateProactiveSettings(settings) }
            .onSuccess { value -> update { it.copy(proactiveSettings = value, error = null, proactiveMessage = "Proactive settings saved.") } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun runProactiveNow() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.runProactive() }
            .onSuccess { result -> update { it.copy(proactiveMessage = "POTATO created ${result.optInt("created")} suggestion(s).", error = null) }; loadNotifications() }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadNotifications() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.notifications(limit = 100) }
            .onSuccess { value -> update { it.copy(notifications = value, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun readNotification(id: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.markNotificationRead(id) }
            .onSuccess { updated -> update { it.copy(notifications = it.notifications.map { item -> if (item.id == id) updated else item }) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun dismissNotification(id: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.dismissNotification(id) }
            .onSuccess { updated -> update { it.copy(notifications = it.notifications.map { item -> if (item.id == id) updated else item }) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadAutomations() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.automations() }.onSuccess { value -> update { it.copy(automations = value, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun toggleAutomation(id: String, enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.enableAutomation(id, enabled); api.automations() }.onSuccess { value -> update { it.copy(automations = value, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun runAutomation(id: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.runAutomation(id) }.onSuccess { result -> update { it.copy(taskResult = "Automation run: ${result.optInt("runs")} run(s)", error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun loadTasks() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.tasks() }.onSuccess { value -> update { it.copy(tasks = value, error = null) } }.onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun createTask(description: String, priority: String = "normal") = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.createTask(description, priority = priority, sessionId = _state.value.sessionId) }
            .onSuccess { task -> update { it.copy(tasks = listOf(task) + it.tasks, taskId = task.id, taskStatus = task.status, taskResult = "Created ${task.description}", error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun setTaskStatus(taskId: String, status: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.updateTask(taskId, JSONObject().put("status", status)) }
            .onSuccess { task -> update { it.copy(tasks = it.tasks.map { existing -> if (existing.id == task.id) task else existing }, taskId = task.id, taskStatus = task.status, taskResult = "${task.description}: ${task.status}", error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun deleteTaskById(taskId: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching { api.deleteTask(taskId) }
            .onSuccess { update { it.copy(tasks = it.tasks.filterNot { task -> task.id == taskId }, taskId = if (it.taskId == taskId) null else it.taskId, taskStatus = if (it.taskId == taskId) "" else it.taskStatus, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun refreshTask() = viewModelScope.launch(Dispatchers.IO) {
        val id = _state.value.taskId ?: return@launch
        runCatching { api.taskStatus(id) }
            .onSuccess { result -> update { it.copy(taskStatus = result.optString("status"), taskResult = result.toString(2), error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun cancelTask() = viewModelScope.launch(Dispatchers.IO) {
        val id = _state.value.taskId ?: return@launch
        runCatching { api.cancelTask(id) }
            .onSuccess { result -> update { it.copy(taskStatus = result.optString("status"), taskResult = result.toString(2), busy = false, error = null) } }
            .onFailure { e -> update { it.copy(error = friendlyError(e)) } }
    }

    fun vision(file: File, prompt: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            runCatching { api.vision(file, prompt) }
                .onSuccess { result -> update { it.copy(lastVision = result.reply, lastVisionResult = result, error = null) } }
                .onFailure { e -> update { it.copy(error = "Vision failed: ${friendlyError(e)}") } }
        } finally {
            runCatching { file.delete() }
        }
    }

    fun visionOcr(file: File) = viewModelScope.launch(Dispatchers.IO) {
        try {
            runCatching { visionProcessor.recognize(getApplication(), android.net.Uri.fromFile(file)) }
                .onSuccess { result -> update { it.copy(lastOcrText = result, error = null) } }
                .onFailure { e -> update { it.copy(error = "OCR failed: ${friendlyError(e)}") } }
        } finally {
            runCatching { file.delete() }
        }
    }

    override fun onCleared() {
        streamJob?.cancel()
        visionProcessor.close()
        db.close()
        super.onCleared()
    }

    fun connection(url: String, token: String) = testConnection(url, token)

    fun backendUrl(): String = db.getSetting("backend_url", JarvisApi.DEFAULT_BASE_URL)
    fun hasToken(): Boolean = !tokenStore.read().isNullOrBlank()
    fun clearToken() { tokenStore.clear(); api = buildApi(); update { it.copy(setupRequired = true, online = false, connectionMessage = "API token cleared. Reconnect with a token or explicitly configured anonymous backend.") }; health() }
    fun appContext() = getApplication<Application>()

    private fun normalizeUrl(url: String): String = BackendUrlPolicy.normalize(url, com.potato.jarvis.BuildConfig.ALLOW_HTTP_BACKEND)

    private fun friendlyError(error: Throwable): String = when (error) {
        is ApiException -> error.message ?: "Backend request failed."
        is SocketTimeoutException -> "POTATO timed out. Check that the backend is running and reachable."
        is ConnectException -> "POTATO could not connect. Check the backend URL and make sure the server is running."
        is java.net.UnknownHostException -> "POTATO could not resolve the backend host. Check the URL and network connection."
        is java.net.MalformedURLException -> "Backend URL is invalid."
        else -> error.message ?: "POTATO request failed."
    }

    private fun update(transform: (State) -> State) { _state.update(transform) }
}
