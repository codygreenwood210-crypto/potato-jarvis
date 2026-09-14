package com.potato.jarvis

import com.potato.jarvis.core.SseFrameAccumulator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SseFrameAccumulatorTest {
    @Test fun joinsMultilineDataAndPreservesEventType() {
        val parser = SseFrameAccumulator()
        assertNull(parser.accept("event: response.delta"))
        assertNull(parser.accept("data: {\"delta\":"))
        assertNull(parser.accept("data: \"hello\"}"))
        val frame = parser.accept("")
        assertEquals("response.delta", frame?.eventType)
        assertEquals("{\"delta\":\n\"hello\"}", frame?.data)
    }

    @Test fun flushesFinalFrameAtEndOfStream() {
        val parser = SseFrameAccumulator()
        parser.accept("data: {\"type\":\"done\"}")
        val frame = parser.finish()
        assertEquals("{\"type\":\"done\"}", frame?.data)
        assertNull(parser.finish())
    }

    @Test fun ignoresCommentsAndNonDataFields() {
        val parser = SseFrameAccumulator()
        assertNull(parser.accept(": keep-alive"))
        assertNull(parser.accept("id: 123"))
        assertNull(parser.accept(""))
    }
}
