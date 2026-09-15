package com.potato.jarvis.core

internal data class SseFrame(val eventType: String?, val data: String)

/** Minimal Server-Sent Events frame accumulator used by the Android streaming client. */
internal class SseFrameAccumulator(private val maxFrameChars: Int = 256_000) {
    private var eventType: String? = null
    private val dataLines = mutableListOf<String>()

    fun accept(line: String): SseFrame? = when {
        line.startsWith("event:") -> {
            eventType = line.removePrefix("event:").trim().ifBlank { null }
            null
        }
        line.startsWith("data:") -> {
            val value = line.removePrefix("data:").removePrefix(" ")
            val projected = dataLines.sumOf { it.length + 1 } + value.length
            require(projected <= maxFrameChars) { "SSE frame exceeded safety limit." }
            dataLines += value
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
