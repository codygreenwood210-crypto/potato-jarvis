# POTATO V5.5 — AccessibilityService declaration

POTATO is not an accessibility tool. Its optional AccessibilityService is used only to receive TYPE_WINDOW_STATE_CHANGED and store the foreground application package name. `canRetrieveWindowContent=false`. The service does not inspect UI text, passwords, messages, financial data, click controls, type, perform gestures, change device settings, or autonomously control another app.

Before normal app usage, POTATO presents a separate prominent disclosure explaining exactly what data is accessed, what it is used for, its local storage behavior, and that the feature is optional. The user must choose Accept or Decline. Enabling the Android AccessibilityService still requires a separate explicit user action in Android system settings.

Suggested Play Console purpose: **optional foreground-app context** to improve user-requested context awareness. This is a submission draft; Google Play approval is an external review and is not claimed by the build.
