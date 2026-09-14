package com.potato.jarvis.core

data class ChatMessage(val role: String, val content: String, val createdAt: Long = System.currentTimeMillis())
data class ChatResponse(val sessionId: String, val traceId: String, val reply: String, val status: String, val events: List<String> = emptyList())
data class StreamEvent(val type: String, val delta: String = "", val reply: String = "", val sessionId: String = "", val traceId: String = "", val error: String = "")
data class Health(val status: String, val model: String, val version: String)
data class MemoryItem(val id: String, val type: String, val content: String, val importance: Double, val confidence: Double = 0.8, val relevance: Double = 0.0, val expiresAt: String? = null, val source: String? = null)
data class Approval(val id: String, val tool: String, val arguments: String, val risk: Int, val status: String, val requiresBiometric: Boolean = false, val expiresAt: String = "")
data class ToolInfo(
    val name: String,
    val description: String,
    val risk: Int,
    val timeoutSeconds: Double = 30.0,
    val retryable: Boolean = false,
    val maxResultBytes: Int = 10000,
)
data class PlanStep(val description: String, val tool: String?, val arguments: Map<String, Any?>, val dependencies: List<Int>, val risk: Int)
data class Plan(val taskId: String, val request: String, val steps: List<PlanStep>)
data class Diagnostic(val raw: String)
data class SessionInfo(val id: String, val title: String, val updatedAt: String)
data class FileItem(val id: String, val name: String, val mime: String, val size: Long, val sha256: String = "", val extractionStatus: String = "pending")
data class FileSearchResult(val fileId: String, val name: String, val mime: String, val chunkIndex: Int, val score: Double, val snippet: String)

data class TaskStatus(val id: String, val description: String, val status: String, val priority: String = "normal", val dueAt: String? = null, val reminderAt: String? = null, val recurrence: String = "none", val dependencies: List<String> = emptyList(), val notes: String = "", val source: String = "manual", val steps: List<TaskStepStatus> = emptyList())
data class TaskStepStatus(val step: Int, val description: String, val tool: String?, val status: String, val retryCount: Int, val maxRetries: Int, val lastError: String?)


data class PersonalityProfile(
    val name: String = "POTATO",
    val style: String = "warm",
    val formality: String = "balanced",
    val humor: String = "light",
    val verbosity: String = "concise",
    val proactivity: String = "permission_based",
    val responseStyle: String = "clear",
    val greeting: String = "natural",
    val units: String = "metric",
    val language: String = "auto",
    val voice: String = "default",
    val instructions: String = "",
)


data class WebCitation(val url: String, val title: String)
data class WebSearchResult(val id: String, val traceId: String, val query: String, val answer: String, val citations: List<WebCitation>)
data class VisionResult(val id: String, val traceId: String, val reply: String, val mime: String, val width: Int, val height: Int)
data class OcrResult(val text: String, val width: Int, val height: Int)


data class NotificationItem(val id: String, val type: String, val title: String, val body: String, val priority: String = "normal", val deepLink: String? = null, val scheduledAt: String? = null, val createdAt: String = "", val deliveredAt: String? = null, val readAt: String? = null, val dismissedAt: String? = null, val traceId: String? = null)


data class AutomationItem(val id: String, val name: String, val enabled: Boolean, val triggerType: String, val actionCount: Int, val lastRun: String? = null, val failurePolicy: String = "continue", val maxRunsPerHour: Int = 20, val actionBudget: Int = 12)


data class SmartHomeHome(val id: String, val name: String, val provider: String, val baseUrl: String)
data class SmartHomeDevice(val id: String, val externalId: String, val name: String, val kind: String, val capabilities: List<String>, val state: String)

data class DeviceContextSnapshot(
    val batteryPercent: Int?,
    val charging: Boolean,
    val network: String,
    val online: Boolean,
    val device: String,
    val androidVersion: String,
    val appVersion: String,
    val nextCalendarEvent: String? = null,
    val contactCount: Int? = null,
    val location: String? = null,
    val locationPermission: Boolean = false,
    val calendarPermission: Boolean = false,
    val contactsPermission: Boolean = false,
    val foregroundPackage: String? = null,
    val capturedAt: Long = System.currentTimeMillis(),
)


data class ProactiveSettings(val enabled: Boolean = true, val mode: String = "permission_based", val dailyLimit: Int = 5, val quietStart: Int = 22, val quietEnd: Int = 7)


data class AgentInfo(val id: String, val name: String, val role: String, val description: String, val enabled: Boolean)
data class AgentRunResult(val id: String, val traceId: String, val roles: List<String>, val reply: String, val status: String)
