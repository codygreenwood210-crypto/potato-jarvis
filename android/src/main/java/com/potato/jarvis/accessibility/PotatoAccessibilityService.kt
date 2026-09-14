package com.potato.jarvis.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Optional, user-enabled foreground-app context.
 *
 * POTATO stores only the package name supplied by TYPE_WINDOW_STATE_CHANGED.
 * It does not retrieve window content, inspect text, click controls, type into
 * other apps, or perform gestures. The user must explicitly enable this service
 * from Android Accessibility settings after reading the in-app disclosure.
 */
class PotatoAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!AccessibilityConsent.isAccepted(this)) {
            AccessibilityConsent.clearCapturedContext(this)
            return
        }
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString()?.trim().orEmpty()
        if (packageName.isBlank()) return
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putString(KEY_FOREGROUND_PACKAGE, packageName)
            .apply()
    }

    override fun onInterrupt() = Unit

    companion object {
        const val PREFS = "potato_accessibility_context"
        const val KEY_FOREGROUND_PACKAGE = "foreground_package"
    }
}
