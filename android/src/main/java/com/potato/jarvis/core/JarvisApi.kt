package com.potato.jarvis.core

import com.potato.jarvis.BuildConfig

import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Base64

class JarvisApi(
    private val baseUrl: String,
    private val token: String? = null,
) {
    companion object {
        const val DEFAULT_BASE_URL = BuildConfig.DEFAULT_BACKEND_URL
        private const val CONNECT_TIMEOUT_MS = 12_000
        private const val READ_TIMEOUT_MS = 120_000
    }

    private fun validatedBaseUrl(): String {
        val clean = baseUrl.trim().trimEnd('/')
        require(clean.isNotBlank()) { "Backend URL is not configured. Open Settings to connect POTATO." }
        val parsed = runCatching { URL(clean) }.getOrElse {
            error("Backend URL is invalid. Use http:// or https:// followed by a host.")
        }
        require(parsed.protocol == "http" || parsed.protocol == "https") {
            "Backend URL must use http:// or https://."
        }
        if (!BuildConfig.ALLOW_HTTP_BACKEND) {
            require(parsed.protocol == "https") {
                "Release builds require an HTTPS backend URL."
            }
        }
        return clean
    }

    private fun connection(path: String, method: String, contentType: String = "application/json"): HttpURLConnection {
        val url = URL(validatedBaseUrl() + path)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", contentType)
            token?.takeIf(String::isNotBlank)?.let { setRequestProperty("Authorization", "Bearer $it") }
            doInput = true
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        if (code !in 200..299) {
            val detail = runCatching { JSONObject(text).optString("detail") }.getOrNull().orEmpty()
            throw ApiException(code, detail.ifBlank { text.take(700).ifBlank { "Request failed." } })
        }
        return text
    }

    private fun request(path: String, method: String = "GET", body: JSONObject? = null): JSONObject {
        val connection = connection(path, method)
        try {
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { out -> out.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            }
            val text = readResponse(connection)
            return JSONObject(if (text.isBlank()) "{}" else text)
        } catch (error: Throwable) {
            connection.disconnect()
            throw error
        }
    }

    fun health(): Health {
        val json = request("/v1/health")
        return Health(json.optString("status"), json.optString("model"), json.optString("version"))
    }

    fun sessions(): List<SessionInfo> {
        val array = request("/v1/sessions").optJSONArray("sessions") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            SessionInfo(item.optString("id"), item.optString("title"), item.optString("updated_at"))
        }
    }

    fun createSession(): String = request("/v1/sessions", "POST").optString("session_id")

    fun sessionMessages(sessionId: String): List<ChatMessage> {
        val array = request("/v1/sessions/${URLEncoder.encode(sessionId, "UTF-8")}/messages").optJSONArray("messages") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ChatMessage(item.optString("role"), item.optString("content"))
        }
    }

    fun agents(): List<AgentInfo> {
        val array = request("/v1/agents").optJSONArray("agents") ?: return emptyList()
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            AgentInfo(item.optString("id"), item.optString("name"), item.optString("role"), item.optString("description"), item.optBoolean("enabled", true))
        }
    }

    fun multiAgent(requestText: String, roles: List<String> = emptyList(), sessionId: String? = null): AgentRunResult {
        val body = JSONObject().put("request", requestText)
        if (roles.isNotEmpty()) {
            val array = org.json.JSONArray()
            roles.forEach(array::put)
            body.put("roles", array)
        }
        sessionId?.let { body.put("session_id", it) }
        val json = request("/v1/agent/multi", "POST", body)
        val roleArray = json.optJSONArray("roles")
        val parsedRoles = if (roleArray == null) emptyList() else (0 until roleArray.length()).map { roleArray.optString(it) }
        return AgentRunResult(json.optString("id"), json.optString("trace_id"), parsedRoles, json.optString("reply"), json.optString("status"))
    }

    fun agentRun(request: String, sessionId: String?, useWeb: Boolean, mode: String = "auto"): org.json.JSONObject {
        val body = org.json.JSONObject().put("request", request).put("use_web", useWeb).put("mode", mode)
        if (!sessionId.isNullOrBlank()) body.put("session_id", sessionId)
        return request("/v1/agent/run", "POST", body)
    }

    fun streamChat(
        sessionId: String?,
        message: String,
        useWeb: Boolean = false,
        onEvent: (StreamEvent) -> Unit,
    ) {
        val body = JSONObject().put("message", message).put("use_web", useWeb).put("auto_plan", false)
        sessionId?.let { body.put("session_id", it) }
        val connection = connection("/v1/chat/stream", "POST", "application/json")
        connection.setRequestProperty("Accept", "text/event-stream")
        connection.setRequestProperty("Cache-Control", "no-cache")
        connection.doOutput = true
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val text = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val detail = runCatching { JSONObject(text).optString("detail") }.getOrNull().orEmpty()
                throw ApiException(code, detail.ifBlank { text.take(700).ifBlank { "Streaming request failed." } })
            }
            val frames = SseFrameAccumulator()
            connection.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    frames.accept(line)?.let { frame ->
                        parseStreamEvent(frame.data, frame.eventType)?.let(onEvent)
                    }
                }
            }
            frames.finish()?.let { frame ->
                parseStreamEvent(frame.data, frame.eventType)?.let(onEvent)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseStreamEvent(raw: String, eventType: String? = null): StreamEvent? {
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return null
        return StreamEvent(
            type = json.optString("type").ifBlank { eventType.orEmpty() },
            delta = json.optString("delta"),
            reply = json.optString("reply"),
            sessionId = json.optString("session_id"),
            traceId = json.optString("trace_id"),
            error = json.optString("error"),
        )
    }

    fun chat(sessionId: String?, message: String, useWeb: Boolean = false): ChatResponse {
        val body = JSONObject().put("message", message).put("use_web", useWeb).put("auto_plan", true)
        sessionId?.let { body.put("session_id", it) }
        val json = request("/v1/chat", "POST", body)
        val events = buildList {
            json.optJSONArray("events")?.let { array ->
                for (index in 0 until array.length()) {
                    array.optJSONObject(index)?.optString("type")?.takeIf(String::isNotBlank)?.let(::add)
                }
            }
        }
        return ChatResponse(json.optString("session_id"), json.optString("trace_id"), json.optString("reply"), json.optString("status"), events)
    }

    fun exportPrivacyData(): JSONObject = request("/v1/privacy/export")

    fun deletePrivacyData(): JSONObject = request(
        "/v1/privacy/delete",
        "POST",
        JSONObject().put("confirmation", "DELETE ALL POTATO DATA"),
    )

    fun memories(query: String = "", limit: Int = 20): List<MemoryItem> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val json = request("/v1/memory?query=$encodedQuery&limit=$limit")
        val array = json.optJSONArray("memories") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            MemoryItem(item.optString("id"), item.optString("type"), item.optString("content"), item.optDouble("importance"), item.optDouble("confidence", 0.8), item.optDouble("relevance", 0.0), item.optString("expires_at").ifBlank { null }, item.optString("source").ifBlank { null })
        }
    }

    fun addMemory(content: String, type: String = "semantic", importance: Double = 0.5, confidence: Double = 0.8, expiresAt: String? = null): String =
        request("/v1/memory", "POST", JSONObject().put("content", content).put("memory_type", type).put("importance", importance).put("confidence", confidence).put("consented", true).put("explicit", true).apply { expiresAt?.let { put("expires_at", it) } }).optString("id")

    fun updateMemory(id: String, content: String? = null, type: String? = null, importance: Double? = null, confidence: Double? = null, expiresAt: String? = null): JSONObject =
        request("/v1/memory/${URLEncoder.encode(id, "UTF-8")}", "PATCH", JSONObject().apply { content?.let { put("content", it) }; type?.let { put("memory_type", it) }; importance?.let { put("importance", it) }; confidence?.let { put("confidence", it) }; expiresAt?.let { put("expires_at", it) } })

    fun exportMemory(): JSONObject = request("/v1/memory/export")

    fun deleteMemory(id: String) {
        request("/v1/memory/${URLEncoder.encode(id, "UTF-8")}", "DELETE")
    }

    fun personality(): PersonalityProfile {
        val p = request("/v1/personality").optJSONObject("personality") ?: JSONObject()
        return PersonalityProfile(
            name = p.optString("name", "POTATO"),
            style = p.optString("style", "warm"),
            formality = p.optString("formality", "balanced"),
            humor = p.optString("humor", "light"),
            verbosity = p.optString("verbosity", "concise"),
            proactivity = p.optString("proactivity", "permission_based"),
            responseStyle = p.optString("response_style", "clear"),
            greeting = p.optString("greeting", "natural"),
            units = p.optString("units", "metric"),
            language = p.optString("language", "auto"),
            voice = p.optString("voice", "default"),
            instructions = p.optString("instructions", ""),
        )
    }

    fun updatePersonality(profile: PersonalityProfile): PersonalityProfile {
        val body = JSONObject()
            .put("name", profile.name)
            .put("style", profile.style)
            .put("formality", profile.formality)
            .put("humor", profile.humor)
            .put("verbosity", profile.verbosity)
            .put("proactivity", profile.proactivity)
            .put("response_style", profile.responseStyle)
            .put("greeting", profile.greeting)
            .put("units", profile.units)
            .put("language", profile.language)
            .put("voice", profile.voice)
            .put("instructions", profile.instructions)
        val p = request("/v1/personality", "PUT", body).optJSONObject("personality") ?: body
        return PersonalityProfile(
            name = p.optString("name", "POTATO"), style = p.optString("style", "warm"),
            formality = p.optString("formality", "balanced"), humor = p.optString("humor", "light"),
            verbosity = p.optString("verbosity", "concise"), proactivity = p.optString("proactivity", "permission_based"),
            responseStyle = p.optString("response_style", "clear"), greeting = p.optString("greeting", "natural"),
            units = p.optString("units", "metric"), language = p.optString("language", "auto"),
            voice = p.optString("voice", "default"), instructions = p.optString("instructions", "")
        )
    }

    fun webSearch(query: String, domains: List<String> = emptyList()): WebSearchResult {
        require(query.trim().isNotEmpty() && query.length <= 2_000) { "Search query must be 1-2000 characters." }
        val body = JSONObject().put("query", query.trim())
        val domainArray = org.json.JSONArray()
        domains.take(20).forEach { domainArray.put(it) }
        body.put("domains", domainArray)
        val json = request("/v1/web/search", "POST", body)
        val citations = buildList {
            json.optJSONArray("citations")?.let { array ->
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val url = item.optString("url").trim()
                    if (url.isNotBlank()) add(WebCitation(url, item.optString("title").ifBlank { url }))
                }
            }
        }
        return WebSearchResult(json.optString("id"), json.optString("trace_id"), json.optString("query"), json.optString("answer"), citations)
    }

    fun tools(): List<ToolInfo> {
        val array = request("/v1/tools").optJSONArray("tools") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ToolInfo(
                item.optString("name"),
                item.optString("description"),
                item.optInt("risk"),
                item.optDouble("timeout_seconds", 30.0),
                item.optBoolean("retryable", false),
                item.optInt("max_result_bytes", 10000),
            )
        }
    }

    fun diagnostics(): Diagnostic = Diagnostic(request("/v1/diagnostics").toString(2))

    fun approvals(): List<Approval> {
        val array = request("/v1/approvals").optJSONArray("approvals") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Approval(item.optString("id"), item.optString("tool"), item.optString("arguments"), item.optInt("risk"), item.optString("status"), item.optBoolean("biometric_required"), item.optString("expires_at"))
        }
    }

    fun registerBiometricKey(publicKeyBase64: String): JSONObject =
        request("/v1/security/device-key", "POST", JSONObject().put("public_key", publicKeyBase64))

    fun biometricChallenge(id: String): JSONObject =
        request("/v1/approvals/${URLEncoder.encode(id, "UTF-8")}/challenge")

    fun approve(id: String, allow: Boolean, biometricChallenge: String? = null, biometricSignature: String? = null): JSONObject {
        val body = JSONObject().put("allow", allow)
        biometricChallenge?.let { body.put("biometric_challenge", it) }
        biometricSignature?.let { body.put("biometric_signature", it) }
        return request("/v1/approvals/${URLEncoder.encode(id, "UTF-8")}", "POST", body)
    }
    fun createPlan(requestText: String, sessionId: String?): JSONObject = request("/v1/plan", "POST", JSONObject().put("request", requestText).apply { sessionId?.let { put("session_id", it) } })
    fun executePlan(planId: String): JSONObject = request("/v1/plan/execute", "POST", JSONObject().put("plan_id", planId))
    fun proactiveSettings(): ProactiveSettings {
        val item = request("/v1/proactive/settings").optJSONObject("settings") ?: JSONObject()
        return ProactiveSettings(item.optBoolean("enabled", true), item.optString("mode", "permission_based"), item.optInt("daily_limit", 5), item.optInt("quiet_start", 22), item.optInt("quiet_end", 7))
    }

    fun updateProactiveSettings(settings: ProactiveSettings): ProactiveSettings {
        val body = JSONObject().put("enabled", settings.enabled).put("mode", settings.mode).put("daily_limit", settings.dailyLimit).put("quiet_start", settings.quietStart).put("quiet_end", settings.quietEnd)
        val item = request("/v1/proactive/settings", "PUT", body).optJSONObject("settings") ?: JSONObject()
        return ProactiveSettings(item.optBoolean("enabled", true), item.optString("mode", "permission_based"), item.optInt("daily_limit", 5), item.optInt("quiet_start", 22), item.optInt("quiet_end", 7))
    }

    fun runProactive(): JSONObject = request("/v1/proactive/run", "POST")

    fun notifications(unreadOnly: Boolean = false, limit: Int = 100): List<NotificationItem> {
        val json = request("/v1/notifications?limit=${limit.coerceIn(1, 100)}&unread_only=$unreadOnly")
        val array = json.optJSONArray("notifications") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            NotificationItem(
                item.optString("id"), item.optString("type"), item.optString("title"), item.optString("body"),
                item.optString("priority", "normal"), item.optString("deep_link").ifBlank { null },
                item.optString("scheduled_at").ifBlank { null }, item.optString("created_at"),
                item.optString("delivered_at").ifBlank { null }, item.optString("read_at").ifBlank { null },
                item.optString("dismissed_at").ifBlank { null }, item.optString("trace_id").ifBlank { null },
            )
        }
    }

    fun markNotificationRead(id: String): NotificationItem = parseNotification(request("/v1/notifications/${URLEncoder.encode(id, "UTF-8")}/read", "POST"))
    fun markNotificationDelivered(id: String): NotificationItem = parseNotification(request("/v1/notifications/${URLEncoder.encode(id, "UTF-8")}/delivered", "POST"))
    fun dismissNotification(id: String): NotificationItem = parseNotification(request("/v1/notifications/${URLEncoder.encode(id, "UTF-8")}/dismiss", "POST"))

    private fun parseNotification(item: JSONObject): NotificationItem = NotificationItem(
        item.optString("id"), item.optString("type"), item.optString("title"), item.optString("body"),
        item.optString("priority", "normal"), item.optString("deep_link").ifBlank { null },
        item.optString("scheduled_at").ifBlank { null }, item.optString("created_at"),
        item.optString("delivered_at").ifBlank { null }, item.optString("read_at").ifBlank { null },
        item.optString("dismissed_at").ifBlank { null }, item.optString("trace_id").ifBlank { null },
    )

    fun smartHomeHomes(): List<SmartHomeHome> {
        val array = request("/v1/smart-home/homes").optJSONArray("homes") ?: return emptyList()
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            SmartHomeHome(item.optString("id"), item.optString("name"), item.optString("provider"), item.optString("base_url"))
        }
    }

    fun smartHomeDevices(homeId: String, refresh: Boolean = true): List<SmartHomeDevice> {
        val encoded = URLEncoder.encode(homeId, "UTF-8")
        val array = request("/v1/smart-home/homes/$encoded/devices?refresh=$refresh").optJSONArray("devices") ?: return emptyList()
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val caps = item.optJSONObject("capabilities")?.optJSONArray("actions")
            val capabilities = if (caps == null) emptyList() else (0 until caps.length()).map { caps.optString(it) }
            SmartHomeDevice(item.optString("id"), item.optString("external_id"), item.optString("name"), item.optString("kind"), capabilities, item.optJSONObject("state")?.optString("state", "unknown") ?: "unknown")
        }
    }

    fun smartHomeAction(homeId: String, deviceId: String, action: String, payload: JSONObject = JSONObject()): JSONObject =
        request("/v1/smart-home/action", "POST", JSONObject().put("home_id", homeId).put("device_id", deviceId).put("action", action).put("payload", payload))

    fun automations(): List<AutomationItem> {
        val array = request("/v1/automations").optJSONArray("automations") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val trigger = item.optJSONObject("trigger")
            val actions = item.optJSONArray("actions")
            AutomationItem(item.optString("id"), item.optString("name"), item.optInt("enabled", 0) != 0, trigger?.optString("type", "interval") ?: "interval", actions?.length() ?: 0, item.optString("last_run").ifBlank { null }, item.optString("failure_policy", "continue"), item.optInt("max_runs_per_hour", 20), item.optInt("action_budget", 12))
        }
    }

    fun enableAutomation(id: String, enabled: Boolean): JSONObject = request("/v1/automations/${URLEncoder.encode(id, "UTF-8")}/${if (enabled) "enable" else "disable"}", "POST")
    fun runAutomation(id: String): JSONObject = request("/v1/automations/${URLEncoder.encode(id, "UTF-8")}/run", "POST")
    fun automationRuns(id: String): JSONArray = request("/v1/automations/${URLEncoder.encode(id, "UTF-8")}/runs").optJSONArray("runs") ?: JSONArray()

    fun taskStatus(taskId: String): JSONObject = request("/v1/tasks/${URLEncoder.encode(taskId, "UTF-8")}")
    fun cancelTask(taskId: String): JSONObject = request("/v1/tasks/${URLEncoder.encode(taskId, "UTF-8")}/cancel", "POST")
    fun tasks(status: String? = null, limit: Int = 100): List<TaskStatus> {
        val query = buildString {
            append("/v1/tasks?limit=").append(limit.coerceIn(1, 100))
            status?.takeIf { it.isNotBlank() }?.let { append("&status=").append(URLEncoder.encode(it, "UTF-8")) }
        }
        val array = request(query).optJSONArray("tasks") ?: return emptyList()
        return (0 until array.length()).map { index -> parseTask(array.getJSONObject(index)) }
    }

    fun createTask(description: String, priority: String = "normal", dueAt: String? = null, reminderAt: String? = null, recurrence: String = "none", dependencies: List<String> = emptyList(), notes: String = "", sessionId: String? = null): TaskStatus {
        val body = JSONObject().put("description", description).put("priority", priority).put("recurrence", recurrence).put("dependencies", org.json.JSONArray(dependencies)).put("notes", notes)
        dueAt?.let { body.put("due_at", it) }; reminderAt?.let { body.put("reminder_at", it) }; sessionId?.let { body.put("session_id", it) }
        return parseTask(request("/v1/tasks", "POST", body))
    }

    fun updateTask(taskId: String, fields: JSONObject): TaskStatus = parseTask(request("/v1/tasks/${URLEncoder.encode(taskId, "UTF-8")}", "PATCH", fields))
    fun deleteTask(taskId: String): JSONObject = request("/v1/tasks/${URLEncoder.encode(taskId, "UTF-8")}", "DELETE")

    private fun parseTask(item: JSONObject): TaskStatus {
        val dependenciesJson = item.optJSONArray("dependencies")
        val dependencies = if (dependenciesJson == null) emptyList() else (0 until dependenciesJson.length()).map { dependenciesJson.optString(it) }
        val stepsJson = item.optJSONArray("steps")
        val steps = if (stepsJson == null) emptyList() else (0 until stepsJson.length()).map { index ->
            val step = stepsJson.getJSONObject(index)
            TaskStepStatus(step.optInt("step"), step.optString("description"), step.optString("tool").ifBlank { null }, step.optString("status"), step.optInt("retry_count"), step.optInt("max_retries"), step.optString("last_error").ifBlank { null })
        }
        return TaskStatus(item.optString("id"), item.optString("description"), item.optString("status"), item.optString("priority", "normal"), item.optString("due_at").ifBlank { null }, item.optString("reminder_at").ifBlank { null }, item.optString("recurrence", "none"), dependencies, item.optString("notes"), item.optString("source", "manual"), steps)
    }

    fun listFiles(): List<FileItem> {
        val array = request("/v1/files").optJSONArray("files") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            FileItem(item.optString("id"), item.optString("name"), item.optString("mime"), item.optLong("size"), item.optString("sha256"), item.optString("extraction_status", "pending"))
        }
    }

    fun readFile(id: String): String = request("/v1/files/${URLEncoder.encode(id, "UTF-8")}").optString("content")
    fun searchFiles(query: String, limit: Int = 20): List<FileSearchResult> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val array = request("/v1/files/search?query=$encoded&limit=$limit").optJSONArray("results") ?: return emptyList()
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            FileSearchResult(item.optString("file_id"), item.optString("name"), item.optString("mime"), item.optInt("chunk_index"), item.optDouble("score"), item.optString("snippet"))
        }
    }

    fun reindexFile(id: String): JSONObject = request("/v1/files/${URLEncoder.encode(id, "UTF-8")}/reindex", "POST", JSONObject())


    fun uploadStream(name: String, mime: String, length: Long?, input: InputStream): JSONObject {
        require(length == null || length <= 20_000_000) { "File is too large." }
        val boundary = "----POTATO${System.currentTimeMillis()}"
        val connection = connection("/v1/files", "POST", "multipart/form-data; boundary=$boundary").apply { doOutput = true }
        try {
            connection.outputStream.use { out ->
                fun write(text: String) = out.write(text.toByteArray(StandardCharsets.UTF_8))
                val safeName = name.replace(Regex("[\\\"\\r\\n]"), "_")
                val safeMime = mime.ifBlank { "application/octet-stream" }.replace(Regex("[\\r\\n]"), "_")
                write("--$boundary\r\n")
                write("Content-Disposition: form-data; name=\"file\"; filename=\"$safeName\"\r\n")
                write("Content-Type: $safeMime\r\n\r\n")
                input.use { stream ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= 20_000_000) { "File is too large." }
                        out.write(buffer, 0, read)
                    }
                }
                write("\r\n--$boundary--\r\n")
            }
            return JSONObject(readResponse(connection))
        } catch (error: Throwable) {
            connection.disconnect()
            throw error
        }
    }

    fun vision(file: File, prompt: String, traceId: String? = null): com.potato.jarvis.core.VisionResult {
        require(file.exists() && file.isFile) { "Image file is unavailable." }
        require(file.length() in 1..10_000_000) { "Image must be between 1 byte and 10 MB." }
        val boundary = "----POTATOVISION${System.currentTimeMillis()}"
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val connection = connection("/v1/vision?prompt=$encodedPrompt", "POST", "multipart/form-data; boundary=$boundary").apply { doOutput = true }
        traceId?.takeIf { it.isNotBlank() }?.let { connection.setRequestProperty("X-Trace-ID", it.take(128)) }
        try {
            connection.outputStream.use { out ->
                fun write(text: String) = out.write(text.toByteArray(StandardCharsets.UTF_8))
                val safeMime = when (file.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                write("--$boundary\r\n")
                write("Content-Disposition: form-data; name=\"file\"; filename=\"image.${file.extension.lowercase()}\"\r\n")
                write("Content-Type: $safeMime\r\n\r\n")
                file.inputStream().use { input -> input.copyTo(out, 64 * 1024) }
                write("\r\n--$boundary--\r\n")
            }
            val json = JSONObject(readResponse(connection))
            return VisionResult(
                id = json.optString("id"),
                traceId = json.optString("trace_id"),
                reply = json.optString("reply"),
                mime = json.optString("mime"),
                width = json.optInt("width"),
                height = json.optInt("height"),
            )
        } catch (error: Throwable) {
            connection.disconnect()
            throw error
        }
    }


}

class ApiException(val statusCode: Int, detail: String) : Exception(
    when (statusCode) {
        401 -> "Authentication failed. Check your POTATO API token."
        403 -> "POTATO refused this operation."
        404 -> "POTATO endpoint was not found. Check the backend version."
        409 -> "POTATO could not complete the request because the resource changed."
        429 -> "POTATO is rate-limited. Please try again shortly."
        500 -> "POTATO backend encountered an internal error."
        503 -> "POTATO backend requires configuration or is temporarily unavailable."
        else -> "POTATO request failed (HTTP $statusCode): $detail"
    }
)
