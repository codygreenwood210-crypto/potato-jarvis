from pathlib import Path
import os

root = Path(os.environ["PROJECT_ROOT"])

device_context = root / "android/src/main/java/com/potato/jarvis/device/DeviceContext.kt"
text = device_context.read_text()
old = '''    private fun lastKnownLocation(): String? = runCatching {
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = manager.getProviders(true)
        val locations = providers.mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        val best = locations.maxByOrNull { it.time } ?: return null
        "%.5f, %.5f".format(java.util.Locale.US, best.latitude, best.longitude)
    }.getOrNull()
'''
new = '''    private fun lastKnownLocation(): String? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return null

        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        return try {
            val locations = manager.getProviders(true).mapNotNull { provider ->
                try {
                    manager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }
            val best = locations.maxByOrNull { it.time } ?: return null
            "%.5f, %.5f".format(java.util.Locale.US, best.latitude, best.longitude)
        } catch (_: SecurityException) {
            null
        }
    }
'''
if old not in text:
    raise SystemExit("DeviceContext lastKnownLocation anchor not found")
device_context.write_text(text.replace(old, new, 1))

roadmap = root / "docs/POTATO_V5.5_COMPLETION_ROADMAP.md"
roadmap.write_text('''# POTATO V5.5 completion roadmap

This roadmap is both the implementation plan and the acceptance checklist for V5.5.

## 1. Preserve the verified V5.4 baseline
Starter command:
```bash
gh run download 34468722357 -n POTATO-JARVIS-FINAL-V5.4 -D input
```
Acceptance: use the previously verified V5.4 source artifact as the migration base.

## 2. Modernize the Android release toolchain
Starter configuration:
```kotlin
android {
    compileSdk = 36
    defaultConfig { targetSdk = 36 }
}
```
```kotlin
plugins {
    id("com.android.application") version "8.12.2" apply false
}
```
Starter wrapper target: Gradle 8.13, JDK 17.
Acceptance: official Gradle distribution and wrapper JAR checksum verification passes.

## 3. Preserve all user-facing features
Starter policy:
```text
No feature removal for release compliance. Sensitive features remain optional,
least-privilege, disclosed, consented, and guarded at runtime.
```
Acceptance: Accessibility, voice, camera/OCR, contacts, calendar, location,
notifications, biometrics, AI/backend, local data, and file handling remain present.

## 4. Accessibility compliance hardening
Starter configuration:
```xml
android:isAccessibilityTool="false"
android:canRetrieveWindowContent="false"
```
Starter UX:
```text
Separate prominent first-use disclosure -> Accept / Decline -> optional system enablement.
```
Acceptance: service cannot inspect screen contents or control other apps; disclosure is explicit.

## 5. Runtime permission safety
Starter Kotlin pattern:
```kotlin
val granted = ContextCompat.checkSelfPermission(context, permission) ==
    PackageManager.PERMISSION_GRANTED
if (!granted) return null
return try { sensitiveCall() } catch (_: SecurityException) { null }
```
Acceptance: every revocable sensitive API used by V5.5 is guarded and lint reports zero errors.

## 6. Backend verification
Starter commands:
```bash
python -m compileall -q backend
python -m pytest -q backend/tests
```
Acceptance: compile succeeds and all backend tests pass.

## 7. Android resource and debug compilation
Starter commands:
```bash
./gradlew :android:processDebugResources --no-daemon
./gradlew :android:assembleDebug --no-daemon
```
Acceptance: API-36 resources and debug APK compile successfully.

## 8. Android tests/checks
Starter command:
```bash
./gradlew :android:testDebugUnitTest --no-daemon
```
Acceptance: task succeeds when tests exist; if no test sources exist, record that explicitly.

## 9. Static analysis
Starter command:
```bash
./gradlew :android:lint --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8"
```
Acceptance: zero lint errors. Warnings are reviewed rather than suppressed blindly.

## 10. Release artifact build
Starter command:
```bash
./gradlew :android:bundleRelease --no-daemon
```
Acceptance: release AAB is generated successfully. Production signing remains under developer-controlled credentials / Play App Signing.

## 11. Complete source package and integrity
Starter commands:
```bash
zip -qr POTATO-JARVIS-FINAL-V5.5.zip POTATO-JARVIS-V5.5
sha256sum POTATO-JARVIS-FINAL-V5.5.zip
unzip -t POTATO-JARVIS-FINAL-V5.5.zip
```
Acceptance: complete post-fix source archive passes ZIP integrity and has a recorded SHA-256.

## 12. Device / Play verification
Starter procedure:
```text
Upload the release AAB to Play internal testing, complete required policy declarations,
install from the Play testing track, launch, grant optional permissions in context,
and execute critical user flows on physical Android hardware.
```
Acceptance: install, launch, permissions, accessibility opt-in, backend connectivity,
voice/camera/location/contact/calendar flows, notifications, and core AI flow are physically tested.

## 13. Final score gate
Scoring weights:
- Build/toolchain correctness: 2.0
- Automated tests: 1.5
- Lint/static analysis: 1.5
- Security/privacy/compliance: 2.0
- Release artifact integrity: 1.5
- Physical device / Play validation: 1.5

A score below 9.8/10 is not accepted as complete. Any unavailable external/manual gate is scored honestly and prevents a false 10/10 claim.
''')
