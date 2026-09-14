package com.potato.jarvis.accessibility

import android.content.Context

object AccessibilityConsent {
    const val DISCLOSURE_PREFS = "potato_accessibility_disclosure"
    const val DECISION_RECORDED = "decision_recorded"
    const val ACCEPTED = "accepted"

    fun isAccepted(context: Context): Boolean =
        context.getSharedPreferences(DISCLOSURE_PREFS, Context.MODE_PRIVATE)
            .getBoolean(ACCEPTED, false)

    fun hasDecision(context: Context): Boolean =
        context.getSharedPreferences(DISCLOSURE_PREFS, Context.MODE_PRIVATE)
            .getBoolean(DECISION_RECORDED, false)

    fun record(context: Context, accepted: Boolean) {
        context.getSharedPreferences(DISCLOSURE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(DECISION_RECORDED, true)
            .putBoolean(ACCEPTED, accepted)
            .apply()
        if (!accepted) clearCapturedContext(context)
    }

    fun clearCapturedContext(context: Context) {
        context.getSharedPreferences(PotatoAccessibilityService.PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(PotatoAccessibilityService.KEY_FOREGROUND_PACKAGE)
            .apply()
    }
}
