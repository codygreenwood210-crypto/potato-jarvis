package com.potato.jarvis.accessibility

import android.content.Context

/**
 * Core sideload build: Accessibility integration is intentionally unavailable.
 * The service is not declared in AndroidManifest.xml, so no disclosure or
 * Accessibility permission/setup flow is presented to the user.
 */
object AccessibilityConsent {
    const val DISCLOSURE_PREFS = "potato_accessibility_disclosure"
    const val DECISION_RECORDED = "decision_recorded"
    const val ACCEPTED = "accepted"

    fun isAccepted(context: Context): Boolean = false

    fun hasDecision(context: Context): Boolean = true

    fun record(context: Context, accepted: Boolean) {
        clearCapturedContext(context)
    }

    fun clearCapturedContext(context: Context) {
        context.getSharedPreferences(PotatoAccessibilityService.PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(PotatoAccessibilityService.KEY_FOREGROUND_PACKAGE)
            .apply()
    }
}
