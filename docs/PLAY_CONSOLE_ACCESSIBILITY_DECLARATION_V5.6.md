# POTATO V5.6 Accessibility disclosure/declaration notes

POTATO's optional AccessibilityService is not presented as a disability-support accessibility tool. Its purpose is optional foreground-app context for the assistant after a separate prominent disclosure and affirmative user consent.

The service subscribes only to window-state-change events, stores only the foreground package name, has `canRetrieveWindowContent=false`, and does not read screen text, passwords, financial information, click controls, type, perform gestures, change settings, or autonomously control another app. Declining or revoking POTATO's consent clears the captured package context and prevents further recording by the service even if the Android system service remains enabled.

Before Play submission, the developer must ensure the Play Console Accessibility declaration, Data Safety answers, privacy policy and in-app disclosure match the exact shipped behavior and current Google Play policy wording. This document is implementation evidence, not a substitute for the Play Console declaration process.
