package com.potato.jarvis.core

internal data class SseFrame(val eventType: String?, val data: String)

/** Minimal Server-Sent Events frame accumulator used by the Android streaming client. */
internal class SseFrameAccumulator {
    private var eventType: String? = null
    private val dataLines = mutableListOf<String>()

    fun accept(line: String): SseFrame? = when {
        line.startsWith("event:") -> {
            eventType = line.removePrefix("event:").trim().ifBlank { null }
            null
        }
        line.startsWith("data:") -> {
            dataLines += line.removePrefix("data:").removePrefix(" ")
            null
        }
        line.isBlank() -> flush()
        else -> null
    }

    fun finish(): SseFrame? = flush()

    private fun flush(): SseFrame? {
        if (dataLines.isEmpty()) {
            eventType = null
            return null
        }
        val frame = SseFrame(eventType, dataLines.joinToString("\n"))
        eventType = null
        dataLines.clear()
        return frame
    }
}
