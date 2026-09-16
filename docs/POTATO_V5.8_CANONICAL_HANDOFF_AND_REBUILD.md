# POTATO-JARVIS V5.8 — Canonical Handoff, Recovery and Full Rebuild Blueprint

> **Purpose:** This is the durable continuity document for POTATO-JARVIS V5.8. A future ChatGPT conversation, engineer, or recovery session should be able to use this file plus the repository to understand the current system, reproduce the verified state, rebuild the application, and continue development without depending on hidden chat history.
>
> **Canonical repository:** `codygreenwood210-crypto/potato-jarvis`  
> **Canonical branch:** `potato-v5.8-final`  
> **Verified code baseline:** `6cc4f2076d46bf11e9bd3c7d0cbdabff4d9d232a`  
> **Version:** 5.8 / Android `versionCode 58`  
> **Package:** `com.potato.jarvis`
>
> If later commits are documentation-only, the code baseline above remains the verified functional reference unless a newer code commit is explicitly tested and promoted.

---

## 1. Current verified milestone

POTATO V5.8 has now been installed and run on a real Samsung Android tablet and has completed a real OpenAI-backed chat request from the physical device.

Verified physical-device sequence:

1. Android APK installed on the tablet.
2. `MainActivity` launched successfully.
3. POTATO connected to the V5.8 backend running locally on the same tablet through Termux + Ubuntu/proot.
4. Android app showed `ONLINE`.
5. Backend regression suite ran on the tablet and reported:
   - `132 passed`
   - `2 warnings`
   - no test failures.
6. A direct OpenAI Responses API diagnostic returned HTTP `200` using the configured OpenAI API key and `gpt-5.6-luna`.
7. A provider/tool-schema compatibility bug was identified from the real backend audit log and fixed permanently.
8. A timezone-dependent proactive test bug exposed by the Australian tablet environment was identified and fixed permanently.
9. After the fixes, the POTATO Android chat UI sent `hello` and received a real model response:
   - `Hello! I’m POTATO 🥔 How can I help today?`

**Judge milestone:** the physical-tablet backend regression step is 10/10 and the real OpenAI-powered physical-device chat step is 10/10 based on direct runtime evidence.

This is not the same thing as saying all future production-distribution work is permanently finished. Release signing, Play distribution/policy work, future regressions, and future feature changes remain separate gates.

---

## 2. Verified V5.8 source facts

### Android

- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Package / namespace: `com.potato.jarvis`
- `compileSdk = 36`
- `targetSdk = 36`
- `minSdk = 26`
- `versionCode = 58`
- `versionName = "5.8"`
- Android Gradle Plugin: `8.12.2`
- Kotlin: `2.0.21`
- Compose plugin: `2.0.21`
- Gradle wrapper distribution: `8.13`
- Java/Kotlin target: 17
- Gradle 8.13 distribution checksum:
  - `20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78`

Important Android dependencies currently include Compose BOM `2025.02.00`, Activity Compose `1.10.1`, Lifecycle `2.9.2`, Core KTX `1.15.0`, Fragment KTX `1.8.5`, Biometric `1.1.0`, WorkManager `2.11.2`, coroutines Android `1.9.0`, ML Kit text recognition `16.0.1`, and JUnit `4.13.2`.

Build behavior:

- Debug default backend URL: `http://10.0.2.2:8000`
- Debug allows HTTP backend connections.
- Release default backend URL is blank.
- Release does not allow cleartext HTTP.
- POTATO version exposed to Android BuildConfig: `5.8`.

### Backend

- Python FastAPI service
- Uvicorn server
- SQLite persistence
- `httpx` provider/network layer
- OpenAI Responses API provider
- Default model: `gpt-5.6-luna`
- Backend version constant: `5.8`

Pinned backend dependencies:

```text
fastapi==0.141.1
starlette==1.6.0
uvicorn==0.48.0
httpx==0.28.1
python-dotenv==1.2.2
python-multipart==0.0.32
pypdf==6.18.0
python-docx==1.2.0
openpyxl==3.1.5
python-pptx==1.0.2
pytest==9.1.1
cryptography==50.0.1
Pillow==12.3.0
```

---

## 3. Core architecture and major features

POTATO is a local-first Android AI assistant with an Android client and Python backend.

### Android responsibilities

The Android app provides:

- setup/connection screen
- chat UI
- streaming/non-streaming conversation flows
- session history
- memory UI
- tasks/plans
- notifications and proactive UI
- device and smart-home screens
- tools/security/settings screens
- Android Keystore-backed token protection
- biometric approval signing
- device context collection within explicit permissions
- camera capture
- photo selection
- OCR through ML Kit
- voice input/output
- WorkManager background execution
- privacy export/share support
- optional accessibility foreground-package context

The current GUI is a dark/black ChatGPT-inspired layout with green/teal POTATO accents. The main chat screen includes New, Refresh, Web, Camera, Photo, OCR and Voice controls plus the bottom feature navigation.

### Backend responsibilities

The backend owns:

- OpenAI provider calls
- security decisions and tool authorization
- session/message persistence
- memory persistence and retrieval
- task/plan execution
- approvals and biometric challenge verification
- files and notes
- automations
- proactive notifications
- smart-home and configured-device adapters
- web/search flows
- privacy export/delete
- traces and audit events
- server-side security policy

The model is **not** the security authority. POTATO application code makes authorization decisions.

---

## 4. Important source locations

The next engineer/chat should inspect these first:

### Root/build

- `README.md`
- `HOW_TO_BUILD.md`
- `CHANGES_V5.8.md`
- `RELEASE_REPORT.md`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `scripts/verify_gradle_wrapper.sh`
- `scripts/package_clean_source.py`
- `scripts/verify_source_archive.py`

### Backend

- `backend/main.py` — main FastAPI app, database, security, tools, agent flows, automations, privacy and most backend logic
- `backend/providers.py` — provider/Responses API transport and validation
- `backend/web.py` — web citation/sanitization support
- `backend/requirements.txt`
- `backend/.env.example`
- `backend/run.sh`
- `backend/tests/test_main.py`
- other files in `backend/tests/`

### Android

- `android/build.gradle.kts`
- `android/src/main/AndroidManifest.xml`
- `android/src/main/java/com/potato/jarvis/MainActivity.kt`
- `android/src/main/java/com/potato/jarvis/ui/JarvisViewModel.kt`
- `android/src/main/java/com/potato/jarvis/ui/PotatoTheme.kt`
- `android/src/main/java/com/potato/jarvis/core/JarvisApi.kt`
- `android/src/main/java/com/potato/jarvis/core/Models.kt`
- `android/src/main/java/com/potato/jarvis/core/SecureTokenStore.kt`
- `android/src/main/java/com/potato/jarvis/security/SecurityGateway.kt`
- `android/src/main/java/com/potato/jarvis/db/JarvisDb.kt`
- `android/src/main/java/com/potato/jarvis/automation/`
- `android/src/main/java/com/potato/jarvis/device/`
- `android/src/main/java/com/potato/jarvis/vision/`
- `android/src/main/java/com/potato/jarvis/voice/`
- `android/src/main/res/xml/file_paths.xml`

---

## 5. Security model that must be preserved

POTATO uses an application-enforced risk model. Do not move security authority into the language model.

Intended risk behavior:

- risk 0–1: low-risk operations may run automatically
- risk 2: explicit approval required
- risk 3: explicit approval plus biometric-gated Android Keystore signature
- risk 4: deny

Important rules:

- approvals bind to normalized tool arguments
- approvals expire
- denied actions cannot create approval records as a bypass
- the model cannot self-authorize tools
- OpenAI credentials stay on the backend
- Android bearer/API token is stored through Android Keystore-backed protection
- production backend should require authentication
- production Android should use HTTPS
- secrets/signing keys must not be committed
- web/doc/tool output must be treated as untrusted input
- audit data is redacted/bounded
- device and smart-home networking remains constrained
- privacy export/delete must cover both database and managed filesystem content

### Accessibility

Accessibility is intentionally narrow and must remain narrow unless the product requirements explicitly change and a new privacy/security review approves it.

Current design:

- explicit user disclosure/consent
- foreground application/package context only
- cannot retrieve window content
- cannot read screen text
- cannot click/type/gesture
- `isAccessibilityTool=false`
- app remains usable without the service

---

## 6. Authentication and secrets — do not confuse these

There are two different secrets:

### `OPENAI_API_KEY`

- belongs only on the backend
- lets backend call OpenAI
- never paste it into the Android POTATO API-token field
- never commit it
- never place it in screenshots or chat logs

### POTATO backend API token / `POTATO_API_TOKEN`

- authenticates the Android app to the POTATO backend
- production should use a strong token
- Android stores it securely
- development-only anonymous mode may leave the Android field blank when `POTATO_ALLOW_ANONYMOUS=true`

The real physical-tablet smoke test deliberately used anonymous local development mode so the client/backend transport could be verified before production token deployment.

---

## 7. Physical-tablet local backend setup that worked

The successful real-device environment was:

- Android tablet
- Termux
- `proot-distro`
- Ubuntu inside Termux
- Python virtual environment
- POTATO repository cloned from GitHub

### Install Termux Ubuntu prerequisites

From Termux:

```bash
pkg install proot-distro -y
proot-distro install ubuntu
proot-distro login ubuntu
```

Inside Ubuntu:

```bash
apt update
apt install -y python3 python3-pip python3-venv git curl unzip
```

Clone V5.8:

```bash
git clone -b potato-v5.8-final https://github.com/codygreenwood210-crypto/potato-jarvis.git ~/POTATO-JARVIS-V5.8
cd ~/POTATO-JARVIS-V5.8
```

Create and activate the virtual environment:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

Install backend packages:

```bash
python -m pip install -r backend/requirements.txt
```

Load the OpenAI key without echoing it:

```bash
read -s -p "OpenAI API key: " OPENAI_API_KEY; echo
export OPENAI_API_KEY
```

For the local development smoke test, start backend with:

```bash
POTATO_ENV=development POTATO_ALLOW_ANONYMOUS=true \
python -m uvicorn backend.main:app --host 0.0.0.0 --port 8000
```

Leave that terminal running.

### Android connection value on the same tablet

When the backend runs on the same physical Android tablet, use:

```text
http://127.0.0.1:8000
```

For the anonymous local smoke test, leave the Android `API token` field blank.

### Important address distinctions

- `10.0.2.2:8000` is the Android emulator alias for the host machine.
- `127.0.0.1:8000` means the same physical device and was the correct address for backend + app on the same tablet.
- If backend runs on another computer on the LAN, use that computer's LAN IP, not the tablet's IP, and bind the backend appropriately.
- Production/exposed deployments should use HTTPS and authentication.

---

## 8. Backend verification commands

Before a backend change is accepted:

```bash
python -m compileall -q backend
python -m pytest -q backend/tests
```

Current physical-tablet result at the verified V5.8 state:

```text
132 passed, 2 warnings
```

The two warnings observed were dependency deprecation warnings in the Starlette/FastAPI TestClient stack and were not test failures.

Do not call a failed test “acceptable” without determining whether it is an actual product defect, environment defect, or test defect.

---

## 9. The two real-world bugs found during physical-device verification

### A. OpenAI strict tool-schema compatibility

Symptom in Android:

```text
I couldn't complete the AI request safely. The provider or network is unavailable.
```

The actual audit error was an OpenAI HTTP 400 similar to:

```text
Invalid schema for function 'remember' ... 'required' is required to be supplied and to be an array including every key in properties. Missing 'memory_type'.
```

Root cause:

POTATO emitted every function tool with:

```python
"strict": True
```

but tools such as `remember` intentionally had optional schema properties (`memory_type`, `importance`). OpenAI strict function schema validation rejected that definition.

Permanent fix in `backend/main.py`:

```python
"strict": set(spec.args_schema.get("required", [])) == set(spec.args_schema.get("properties", {}).keys())
```

This keeps strict mode for schemas in which every property is required while allowing non-strict mode for schemas with legitimate optional fields. POTATO's own server-side argument validation remains in place.

Fix commit:

```text
56056f6afbfcdcaf1d97ec275ec91c3cc9553aea
```

The dedicated hotfix workflow ran the complete backend suite and strictness checks successfully.

### B. Timezone-dependent proactive test

Symptom on the Australian tablet:

```text
1 failed, 131 passed
AssertionError: assert 'no_candidates' == 'quiet_hours'
```

Root cause:

The test chose its quiet-hour range using UTC:

```python
main.datetime.now(main.timezone.utc).hour
```

while production code uses local time:

```python
datetime.now().astimezone()
```

This happened to pass on UTC GitHub runners but failed correctly on a non-UTC physical device.

Permanent fix:

Make the test use the same local-time basis as production.

Verified combined portability/provider commit:

```text
6cc4f2076d46bf11e9bd3c7d0cbdabff4d9d232a
```

After the fix, the full backend suite passed again.

**Lesson:** POTATO must test in at least one non-UTC environment or explicitly inject/freeze time for timezone-sensitive logic.

---

## 10. Direct OpenAI diagnostic used during recovery

When POTATO's generic provider error hides the real failure, first determine whether the raw API path itself works.

A safe diagnostic pattern is:

```bash
python - <<'PY'
import os, json, urllib.request, urllib.error

key = os.getenv("OPENAI_API_KEY")
if not key:
    print("ERROR: OPENAI_API_KEY is not loaded")
    raise SystemExit

payload = json.dumps({
    "model": "gpt-5.6-luna",
    "input": "Reply with exactly: POTATO OK"
}).encode()

req = urllib.request.Request(
    "https://api.openai.com/v1/responses",
    data=payload,
    headers={
        "Authorization": f"Bearer {key}",
        "Content-Type": "application/json",
    },
    method="POST",
)

try:
    with urllib.request.urlopen(req, timeout=60) as response:
        print("OPENAI API SUCCESS:", response.status)
        response.read()
except urllib.error.HTTPError as exc:
    print("OPENAI API ERROR:", exc.code)
    print(exc.read().decode()[:1500])
except Exception as exc:
    print("NETWORK ERROR:", type(exc).__name__, str(exc))
PY
```

Verified recovery result was:

```text
OPENAI API SUCCESS: 200
```

That proved key, credits, internet path, model access and Responses endpoint were functional, which isolated the remaining defect to POTATO's request/tool definition layer.

---

## 11. How to inspect the hidden POTATO AI failure

POTATO intentionally gives the Android user a safe generic provider error. The real exception is stored in the backend audit database.

Useful diagnostic:

```bash
python - <<'PY'
import sqlite3, os

db = os.path.expanduser("~/.potato/potato.db")
con = sqlite3.connect(db)
row = con.execute("""
SELECT created_at, event, data
FROM audit_events
WHERE event IN ('ai_error', 'agent_run_ai_error')
ORDER BY id DESC
LIMIT 1
""").fetchone()

if row:
    print("LATEST POTATO AI ERROR")
    print("Time :", row[0])
    print("Event:", row[1])
    print("Error:", row[2])
else:
    print("No AI error found")
PY
```

Do not print or log secret keys while debugging.

---

## 12. Android build and verification from a clean machine

Prerequisites:

- JDK 17 or compatible newer JDK supported by the selected AGP
- Android SDK platform 36
- Android build tools required by AGP
- Gradle wrapper from the repository

Run:

```bash
./scripts/verify_gradle_wrapper.sh
./gradlew :android:testDebugUnitTest --no-daemon
./gradlew :android:assembleDebug --no-daemon
./gradlew :android:lint --no-daemon --max-workers=1
./gradlew :android:bundleRelease --no-daemon
```

For a stricter rebuild gate, use:

```bash
./gradlew clean \
  :android:testDebugUnitTest \
  :android:lintDebug \
  :android:assembleDebug \
  :android:bundleRelease
```

Expected main outputs are under Android Gradle build output directories such as debug APK and release bundle paths.

Do not claim a fresh binary contains source changes unless it was built after those source changes.

---

## 13. CI/release evidence already established

V5.8 CI has previously demonstrated:

- backend test suite success
- Android unit test success
- Gradle wrapper verification
- secret scanning/dependency checks in the verification chain
- Android lint with no blocking errors at the validated stage
- debug APK compilation
- release AAB compilation
- emulator installation
- `MainActivity` launch
- backend connectivity
- chat GUI smoke flow
- APK signature verification

The earlier exact verified CI-built V5.8 APK had SHA-256:

```text
c4901417a74c35de6217590e11e6c86591b919c39b84127ded684daa2f50ea70
```

The Android binary did not require modification for the later provider/tool-schema fix because that defect was backend-side. Nevertheless, any future Android source modification requires a new binary build and a new checksum.

A sideload test previously triggered Google Play Protect because it was a debug/development-style sideload with sensitive capabilities. That does not prove the APK was malicious. For normal distribution, use proper release signing / Play App Signing, Play policy declarations and internal testing rather than relying on disabling Play Protect.

---

## 14. Production release gates still separate from the development smoke test

For production-quality deployment, require all applicable gates:

1. backend full regression suite
2. Android unit tests
3. lint
4. APK/AAB build
5. signature verification
6. release signing under developer-controlled process / Play App Signing
7. no committed secrets
8. production bearer authentication
9. HTTPS backend transport
10. production environment variables/secrets management
11. Play policy/privacy declarations
12. Play internal testing
13. physical-device install/update test
14. core user journey test
15. live backend/provider test
16. privacy export/delete test
17. approval/biometric security test
18. background automation/notification checks
19. accessibility disclosure and no-overreach verification
20. reproducible artifact/hash recording

Development-mode anonymous access must not silently become the production default.

---

## 15. Current product surface

Current user-facing areas include:

- Chat
- Memory
- Tasks
- Alerts/Notifications
- Proactive
- Device
- Home / smart-home
- Tools
- Security
- Settings

Current chat action surface includes:

- New conversation
- Refresh
- Web
- Camera
- Photo
- OCR
- Voice
- text composer/send

The app is now confirmed to render and perform a live AI response on a physical tablet.

---

## 16. Database / persistence model overview

Backend persistence includes SQLite tables for areas including:

- sessions
- messages
- memories
- preferences
- approvals
- biometric keys/challenges
- tool runs
- security events
- audit events
- tasks and task steps
- automations and automation runs
- proactive settings
- notifications
- device/smart-home configuration
- other feature state

The managed POTATO root defaults under the backend user's home directory (`~/.potato`) with database, notes and file areas.

Device/smart-home credentials are encrypted at rest through the backend credential-key mechanism. Production must provide/secure appropriate credential material rather than relying on development conveniences.

---

## 17. Tool/agent design notes

Representative server tools include:

- `get_time`
- `remember`
- note read/write/list/delete
- private file read/write/delete
- `web_search`
- configured `device_action`
- smart-home state/action

Tool argument schemas are not themselves the full security boundary. Server-side validation, risk classification, approval capabilities and execution policy must remain authoritative.

Tool call budgets, tool rounds, response limits and other bounds are enforced in the backend to reduce runaway execution and memory/network abuse.

---

## 18. Rebuild/recovery order if the app is lost

If POTATO must be rebuilt from scratch, use this sequence:

1. Clone `potato-v5.8-final`.
2. Verify repository head and read this file.
3. Verify Gradle wrapper checksum.
4. Set up JDK and Android SDK 36.
5. Create a clean backend Python virtual environment.
6. Install pinned backend requirements.
7. Run `compileall` and full backend pytest suite.
8. Run Android unit tests and lint.
9. Build debug APK and release AAB.
10. Verify signatures/artifact hashes.
11. Start backend with development configuration for smoke testing.
12. Install APK on emulator/device.
13. Configure emulator URL (`10.0.2.2`) or physical same-device URL (`127.0.0.1`) as appropriate.
14. Verify setup/connection screen.
15. Verify health/session calls.
16. Verify a raw OpenAI Responses request if provider debugging is needed.
17. Verify POTATO `hello` chat response.
18. Verify memory/tasks/files/security flows.
19. Verify approvals and biometric path.
20. Move to production auth/HTTPS/release signing only after development smoke passes.

If a test passes only in UTC but fails on a real non-UTC device, treat it as a real portability finding and fix the deterministic time model/test rather than ignoring the device result.

---

## 19. Canonical continuation instructions for a future ChatGPT chat

A future chat should begin by doing the following, not by guessing from memory:

1. Open repository `codygreenwood210-crypto/potato-jarvis`.
2. Use branch `potato-v5.8-final` unless the user explicitly promotes a newer branch/version.
3. Read `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md` in full.
4. Read `README.md`, `HOW_TO_BUILD.md`, `CHANGES_V5.8.md`, and the current release/CI evidence.
5. Check the current branch head and recent commits.
6. Run or inspect current tests before editing.
7. Do not regress the OpenAI tool-schema compatibility fix.
8. Do not regress the local-time proactive test fix.
9. Never ask the user to reveal OpenAI keys/tokens.
10. Distinguish verified facts, source inspection, CI evidence and physical-device evidence.
11. Do not call work complete merely because code looks correct; build/test/run when possible.
12. Keep Android client credentials separate from backend provider credentials.
13. Keep production signing keys and secrets out of the repository.
14. Prefer root-cause fixes over patches/workarounds.
15. Preserve security/privacy behavior while adding features.

Suggested future-chat instruction from the user:

```text
Open the POTATO Jarvis repository, branch potato-v5.8-final, and read docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md before doing anything. Continue from the verified V5.8 state and preserve all security, tests, and physical-device evidence.
```

---

## 20. Engineering team operating model / “level-up” rules

Treat the POTATO effort as a coordinated senior engineering organization, not a one-off code generator.

Standing roles/capabilities should include:

- Technical Manager / integration lead
- Android/Kotlin/Compose lead
- Gradle/build engineer
- Backend/FastAPI/Python lead
- API/contract engineer
- AI/provider/tool-schema engineer
- Agent/memory architecture engineer
- Database/persistence engineer
- Application security / OWASP mobile engineer
- Privacy/governance engineer
- Authentication/cryptography engineer
- Automation/distributed execution engineer
- IoT/smart-home engineer
- Networking engineer
- QA/test automation engineer
- Performance/reliability engineer
- Accessibility engineer
- DevOps/CI/CD engineer
- Google Play/release engineer
- Debugging/forensics specialist
- Build Environment Specialist
- Physical Device Verification Coordinator
- Release Gatekeeper
- Contract Synchronization Engineer
- Migration/Upgrade Engineer
- Failure Injection Engineer
- End-to-End Release Simulation Engineer
- User Journey Validation Engineer
- Final Integration Engineer
- Scout — current external technical research/evidence
- Judge — independent completion-quality arbiter

### Automatic team evolution rule

Whenever a real failure exposes a capability gap, add the appropriate specialist responsibility to the operating model, document the lesson here or in a successor handoff, add a regression test where practical, and update the verification gate so the same class of defect is less likely to escape again.

Examples from V5.8:

- OpenAI strict-schema failure -> strengthen provider contract/schema compatibility review.
- UTC-only passing test -> strengthen timezone/locale/environment portability verification.
- Play Protect sideload block -> strengthen release-signing/Play-policy/distribution verification.
- sandbox build limitations in earlier work -> strengthen cloud CI/build-environment fallback capability.

### Judge rules

Judge must not inflate scores. Judge evaluates:

- exact user request
- correctness
- completeness
- verification evidence
- security impact
- regression risk
- maintainability
- artifact/release integrity
- unresolved manual/external gates

A 10/10 milestone means the requested milestone has direct evidence, not simply confidence.

---

## 21. Known future hardening / improvement areas

Do not assume V5.8 can never be improved. Areas previously identified for further hardening or product evolution include, depending on current source state and priorities:

- release signing / Play distribution hardening
- deeper physical-device regression matrix
- Android navigation density/accessibility refinements
- continued provider/network resiliency
- bounded streaming/file handling verification
- automation continuation/failure semantics
- distributed automation leasing if multi-instance backend is introduced
- task dependency/cycle/recurrence semantics
- provider client reuse/retry strategy
- Android stale async response prevention
- background worker responsibility consolidation
- local data protection and migration strategy
- permission UX and policy timing
- deep-link hardening
- state restoration and form persistence
- privacy cache cleanup
- Android feature parity tests
- CI truthfulness and immutable dependency pinning
- supply-chain scanning and CodeQL-style checks
- architecture decomposition of large backend/UI modules
- performance/scalability of search/memory as data grows

Before acting on any historical item, inspect the current branch: some issues may already be resolved in V5.8 or later.

---

## 22. Secrets and sensitive information policy for handoffs

Never place any of the following in this file, GitHub, issue comments, screenshots, logs, or future-chat memory summaries:

- full OpenAI API key
- POTATO bearer token
- passwords
- signing keystore/private key
- private smart-home/device credentials
- credential encryption key

A future chat should ask the user to enter secrets locally and should verify presence through non-secret checks such as:

```bash
test -n "$OPENAI_API_KEY" && echo "API KEY LOADED" || echo "API KEY MISSING"
```

---

## 23. Final verified V5.8 continuity snapshot

At the time this handoff was written:

- V5.8 repository is available and canonical.
- Gradle wrapper is present and pinned to 8.13 with checksum verification.
- Backend dependencies are pinned.
- Backend full regression suite passes 132 tests on the physical tablet after portability fixes.
- Raw OpenAI Responses API request succeeds with HTTP 200 from the physical tablet backend environment.
- OpenAI function-tool schema compatibility issue is permanently fixed.
- Non-UTC proactive test portability issue is permanently fixed.
- Android app is installed and launches on the physical tablet.
- Android app connects to local backend at `127.0.0.1:8000`.
- POTATO shows `ONLINE`.
- Real POTATO chat returned an OpenAI-powered response on the physical device.
- The user has direct visual evidence of that successful conversation.

This is the baseline to preserve and improve from.

---

## 24. Recovery mantra

**Source of truth is the repository + tests + build evidence + runtime evidence. Do not replace evidence with assumptions.**

When something fails:

1. reproduce it,
2. expose the real error,
3. isolate the failing layer,
4. fix the root cause,
5. add/repair regression coverage,
6. rerun the full relevant suite,
7. rebuild affected artifacts,
8. verify on the real target environment,
9. record the new canonical state.

That is how POTATO should continue to “level up.”
