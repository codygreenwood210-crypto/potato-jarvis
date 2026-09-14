package com.potato.jarvis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkRouterTest {
    @Test fun routesKnownPotatoLinks() {
        assertEquals(Screen.TASKS, screenForPotatoDeepLink("potato://tasks/abc-123"))
        assertEquals(Screen.TOOLS, screenForPotatoDeepLink("potato://automations/run_1"))
        assertEquals(Screen.SECURITY, screenForPotatoDeepLink("potato://approvals/a1"))
    }
    @Test fun rejectsExternalOrMalformedLinks() {
        assertNull(screenForPotatoDeepLink("https://evil.example"))
        assertNull(screenForPotatoDeepLink("potato://tasks/../../oops"))
        assertNull(screenForPotatoDeepLink("potato://unknown/abc"))
        assertNull(screenForPotatoDeepLink("potato://tasks/abc?redirect=https://evil.example"))
    }
}
