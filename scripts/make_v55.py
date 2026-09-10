from pathlib import Path
import os

root = Path(os.environ["PROJECT_ROOT"])

# Version and Android 16 / API 36 modernization.
(root / "VERSION").write_text("5.5\n")
for rel in ["settings.gradle.kts", "README.md", "docs/API.md", "backend/main.py", "backend/tests/test_main.py"]:
    p = root / rel
    p.write_text(p.read_text().replace("V5.4", "V5.5").replace("5.4", "5.5"))

p = root / "android/build.gradle.kts"
s = p.read_text()
s = s.replace("compileSdk = 35", "compileSdk = 36")
s = s.replace("targetSdk = 35", "targetSdk = 36")
s = s.replace("versionCode = 54", "versionCode = 55")
s = s.replace('versionName = "5.4"', 'versionName = "5.5"')
s = s.replace('\\"5.4\\"', '\\"5.5\\"')
p.write_text(s)

p = root / "build.gradle.kts"
p.write_text(p.read_text().replace('com.android.application") version "8.7.3"', 'com.android.application") version "8.12.2"'))

# Modern AGP provides its own compatible lint runtime.
p = root / "gradle.properties"
lines = [x for x in p.read_text().splitlines() if not x.startswith("android.experimental.lint.version=")]
p.write_text("\n".join(lines) + "\n")

# Explicitly identify this as a non-accessibility-tool service.
p = root / "android/src/main/res/xml/accessibility_service_config.xml"
s = p.read_text()
if "android:isAccessibilityTool=" not in s:
    s = s.replace('android:notificationTimeout="250"', 'android:notificationTimeout="250"\n    android:isAccessibilityTool="false"')
p.write_text(s)

# Separate, prominent, first-use Accessibility disclosure with Accept / Decline.
p = root / "android/src/main/java/com/potato/jarvis/MainActivity.kt"
s = p.read_text()
if "import androidx.compose.material3.AlertDialog\n" not in s:
    s = s.replace("import androidx.compose.material3.Button\n", "import androidx.compose.material3.AlertDialog\nimport androidx.compose.material3.Button\n", 1)

anchor = "    var voiceManager by remember { mutableStateOf<VoiceManager?>(null) }\n"
replacement = '''    var voiceManager by remember { mutableStateOf<VoiceManager?>(null) }\n    val accessibilityDisclosurePrefs = remember(context) {\n        context.getSharedPreferences("potato_accessibility_disclosure", Context.MODE_PRIVATE)\n    }\n    var showAccessibilityDisclosure by remember {\n        mutableStateOf(!accessibilityDisclosurePrefs.getBoolean("decision_recorded", false))\n    }\n'''
if anchor not in s:
    raise SystemExit("PotatoApp voice state anchor not found")
s = s.replace(anchor, replacement, 1)

anchor = "    PotatoTheme {\n        if (state.setupRequired) {\n"
replacement = '''    PotatoTheme {\n        if (showAccessibilityDisclosure) {\n            AlertDialog(\n                onDismissRequest = { },\n                title = { Text("Optional Accessibility context") },\n                text = {\n                    Text(\n                        "POTATO can optionally use Android Accessibility to receive only the package name of the foreground app when the active window changes. " +\n                            "This helps POTATO understand which app you are using. POTATO does not read screen text, passwords, messages or financial information, and it cannot click, type, perform gestures, change settings, or control other apps. " +\n                            "The package name stays in POTATO's private local app storage unless you deliberately include device context in a request. You can use POTATO without enabling this feature."\n                    )\n                },\n                confirmButton = {\n                    Button(onClick = {\n                        accessibilityDisclosurePrefs.edit()\n                            .putBoolean("decision_recorded", true)\n                            .putBoolean("accepted", true)\n                            .apply()\n                        showAccessibilityDisclosure = false\n                    }) { Text("Accept") }\n                },\n                dismissButton = {\n                    TextButton(onClick = {\n                        accessibilityDisclosurePrefs.edit()\n                            .putBoolean("decision_recorded", true)\n                            .putBoolean("accepted", false)\n                            .apply()\n                        showAccessibilityDisclosure = false\n                    }) { Text("Decline") }\n                },\n            )\n        } else if (state.setupRequired) {\n'''
if anchor not in s:
    raise SystemExit("PotatoTheme setup anchor not found")
s = s.replace(anchor, replacement, 1)

old = '''                Button(\n                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },\n                    enabled = accessibilityConsent,\n'''
new = '''                Button(\n                    onClick = {\n                        context.getSharedPreferences("potato_accessibility_disclosure", Context.MODE_PRIVATE)\n                            .edit()\n                            .putBoolean("decision_recorded", true)\n                            .putBoolean("accepted", true)\n                            .apply()\n                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))\n                    },\n                    enabled = accessibilityConsent,\n'''
if old not in s:
    raise SystemExit("Accessibility settings button anchor not found")
s = s.replace(old, new, 1)
p.write_text(s)

(root / "CHANGES_V5.5.md").write_text('''# POTATO V5.5 changes\n\n- Keeps every V5.4 feature, including optional Accessibility context.\n- Targets Android 16 / API 36 for current Google Play submission requirements.\n- Upgrades Android Gradle Plugin to 8.12.2 and Gradle to 8.13.\n- Removes the temporary external lint-runtime override after toolchain modernization.\n- Adds a separate first-use Accessibility disclosure with affirmative Accept or Decline before normal app use.\n- Explicitly declares the service is not an accessibility tool.\n- Keeps Accessibility narrowly scoped to foreground package-name events with window-content retrieval disabled.\n- Adds Play Console declaration and release-readiness documentation.\n- Adds release App Bundle generation. Production signing remains a controlled credential step.\n''')

(root / "docs/PLAY_CONSOLE_ACCESSIBILITY_DECLARATION_V5.5.md").write_text('''# POTATO V5.5 — AccessibilityService declaration\n\nPOTATO is not an accessibility tool. Its optional AccessibilityService is used only to receive TYPE_WINDOW_STATE_CHANGED and store the foreground application package name. `canRetrieveWindowContent=false`. The service does not inspect UI text, passwords, messages, financial data, click controls, type, perform gestures, change device settings, or autonomously control another app.\n\nBefore normal app usage, POTATO presents a separate prominent disclosure explaining exactly what data is accessed, what it is used for, its local storage behavior, and that the feature is optional. The user must choose Accept or Decline. Enabling the Android AccessibilityService still requires a separate explicit user action in Android system settings.\n\nSuggested Play Console purpose: **optional foreground-app context** to improve user-requested context awareness. This is a submission draft; Google Play approval is an external review and is not claimed by the build.\n''')

(root / "docs/RELEASE_READINESS_V5.5.md").write_text('''# POTATO V5.5 release readiness\n\n## Automated build target\n- compileSdk 36\n- targetSdk 36\n- minSdk 26\n- JDK 17\n- Android Gradle Plugin 8.12.2\n- Gradle 8.13\n\n## Outputs\nCI produces a debug APK for development/device diagnostics and a release Android App Bundle (AAB). The release AAB is not considered publishable until it is signed with the developer upload key and accepted by Google Play.\n\n## Manual/external gates\nGoogle Play Console app creation, Play App Signing enrollment, upload-key custody, Accessibility Permission Declaration Form/review, Data safety/privacy declarations, Play Protect classification, internal-track installation, and physical-device regression testing are external/manual gates.\n''')

# Replace the old Gradle-8.9-specific verifier with exact V5.5 pins obtained
# from the official Gradle distribution service by CI before this script runs.
dist_sha = os.environ["GRADLE_813_DIST_SHA"].strip()
jar_sha = os.environ["GRADLE_813_JAR_SHA"].strip()
(root / "scripts/verify_gradle_wrapper.sh").write_text(f'''#!/usr/bin/env bash\nset -euo pipefail\nROOT="$(cd "$(dirname "${{BASH_SOURCE[0]}}")/.." && pwd)"\nPROPS="$ROOT/gradle/wrapper/gradle-wrapper.properties"\nJAR="$ROOT/gradle/wrapper/gradle-wrapper.jar"\nGRADLEW="$ROOT/gradlew"\nEXPECTED_URL="https://services.gradle.org/distributions/gradle-8.13-bin.zip"\nEXPECTED_DIST="{dist_sha}"\nEXPECTED_JAR="{jar_sha}"\nfail() {{ echo "GRADLE_WRAPPER=FAIL: $1" >&2; exit 1; }}\n[[ -f "$PROPS" ]] || fail "missing wrapper properties"\nDIST_URL="$(sed -n 's/^[[:space:]]*distributionUrl[[:space:]]*=[[:space:]]*//p' "$PROPS" | tail -n1 | sed 's/\\\\:/:/g')"\nDIST_SHA="$(sed -n 's/^[[:space:]]*distributionSha256Sum[[:space:]]*=[[:space:]]*//p' "$PROPS" | tail -n1 | tr -d '[:space:]')"\n[[ "$DIST_URL" == "$EXPECTED_URL" ]] || fail "unexpected distribution URL: $DIST_URL"\n[[ "$DIST_SHA" == "$EXPECTED_DIST" ]] || fail "Gradle 8.13 distribution checksum mismatch"\n[[ -x "$GRADLEW" ]] || fail "gradlew is not executable"\nsh -n "$GRADLEW" || fail "gradlew shell syntax error"\ngrep -q 'distributionSha256Sum' "$GRADLEW" || fail "bootstrap does not verify distributionSha256Sum"\nif [[ -f "$JAR" ]]; then\n  ACTUAL="$(sha256sum "$JAR" | awk '{{print $1}}')"\n  [[ "$ACTUAL" == "$EXPECTED_JAR" ]] || fail "official Gradle 8.13 wrapper JAR checksum mismatch"\nfi\necho "GRADLE_WRAPPER=PASS Gradle 8.13 distribution and wrapper integrity pins verified"\n''')

# Exact wrapper configuration for AGP 8.12.x.
(root / "gradle/wrapper/gradle-wrapper.properties").write_text(f'''distributionBase=GRADLE_USER_HOME\ndistributionPath=wrapper/dists\ndistributionUrl=https\\://services.gradle.org/distributions/gradle-8.13-bin.zip\ndistributionSha256Sum={dist_sha}\nnetworkTimeout=10000\nvalidateDistributionUrl=true\nzipStoreBase=GRADLE_USER_HOME\nzipStorePath=wrapper/dists\n''')
