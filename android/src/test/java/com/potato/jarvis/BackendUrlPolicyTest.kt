package com.potato.jarvis

import com.potato.jarvis.core.BackendUrlPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackendUrlPolicyTest {
    @Test fun acceptsHttpsAndNormalizesTrailingSlash() = assertEquals("https://example.com", BackendUrlPolicy.normalize(" https://example.com/ ", false))
    @Test fun rejectsHttpForRelease() { assertThrows(IllegalArgumentException::class.java) { BackendUrlPolicy.normalize("http://example.com", false) } }
    @Test fun rejectsCredentialsQueryFragmentAndPath() {
        listOf("https://u:p@example.com", "https://example.com?q=1", "https://example.com/#x", "https://example.com/api").forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { BackendUrlPolicy.normalize(value, false) }
        }
    }
}
