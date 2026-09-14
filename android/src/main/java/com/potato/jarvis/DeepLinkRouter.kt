package com.potato.jarvis

import java.net.URI

internal fun screenForPotatoDeepLink(value: String?): Screen? {
    val raw = value?.trim().orEmpty()
    if (raw.isBlank() || raw.length > 500) return null
    val uri = runCatching { URI(raw) }.getOrNull() ?: return null
    if (!uri.scheme.equals("potato", ignoreCase = true)) return null
    if (uri.userInfo != null || uri.query != null || uri.fragment != null) return null
    val host = uri.host?.lowercase() ?: return null
    val id = uri.path.orEmpty().trim('/').takeIf { it.isNotBlank() }
    if (id != null && !id.matches(Regex("[A-Za-z0-9_-]{1,120}"))) return null
    return when (host) {
        "tasks" -> Screen.TASKS
        "automations" -> Screen.TOOLS
        "approvals" -> Screen.SECURITY
        "notifications" -> Screen.NOTIFICATIONS
        else -> null
    }
}
