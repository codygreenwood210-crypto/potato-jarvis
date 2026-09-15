# POTATO-JARVIS V5.8

POTATO-JARVIS is an Android personal AI assistant backed by a FastAPI service. V5.8 is the final working-copy candidate built from the verified V5.7 baseline, carrying forward its security/privacy hardening plus explicit approval-capability fixes, automation continuation repairs, side-effect result-contract regression tests, and a ChatGPT-inspired green/black Android theme.

## Current release baseline

- Android: Kotlin + Jetpack Compose
- Android application ID: `com.potato.jarvis`
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`
- Android Gradle Plugin 8.12.2, Gradle 8.13, JDK 17
- Version code 58 / version name 5.8
- Backend: Python 3.13-compatible FastAPI/Uvicorn/SQLite/httpx
- OpenAI provider: Responses API; default model `gpt-5.6-luna`
- Production Android cleartext traffic: disabled
- Production backend authentication: bearer token required; anonymous mode is development-only

## Features preserved in V5.8

V5.8 retains the V5.7 feature surface: chat/AI, memory, tasks/plans, approvals, tools, web-enabled backend flows, voice input/output, camera capture and OCR/vision, file intelligence, notifications, automations, local device context, optional calendar/contacts/location context, biometric approval, optional Accessibility foreground-app context, and server-side device/smart-home adapters.

The Accessibility feature remains deliberately narrow: it can observe only foreground package-name changes after an explicit POTATO disclosure/consent flow. It cannot retrieve window content, read screen text, click, type, perform gestures, or control another app.

## Security model

The model is not the authorization authority. Tool risk is evaluated by application code. Low-risk actions may run automatically; higher-risk actions require explicit approval, risk-3 actions additionally require a biometric-gated Android Keystore signature, and denied-risk actions cannot self-authorize. Server approvals are bound to normalized arguments and expire.

OpenAI credentials stay on the backend. The Android bearer token is protected with Android Keystore-backed encryption. Production OpenAI endpoints must use HTTPS. Private/loopback smart-device networks are blocked unless explicitly enabled by server configuration.

V5.8 additionally enforces Accessibility consent inside the service, validates internal notification deep links on both client and server, redacts sensitive audit data before persistence, bounds persisted audit data as valid JSON, validates provider response/stream envelopes, and separates permanent background-worker failures from retryable failures.

## Android permissions

Permissions are optional and requested by feature rather than as one large bundle. Location retains both approximate and precise capability when the user chooses it. Contacts and calendar are separate. Camera capture is delegated to the system camera through `ActivityResultContracts.TakePicture`, so V5.8 does not declare the direct `CAMERA` runtime permission. Microphone, notifications, biometrics and other sensitive capabilities remain feature-scoped.

## Backend setup

```bash
python3 -m venv .venv
. .venv/bin/activate
python -m pip install -r backend/requirements.txt
cp backend/.env.example backend/.env
# Set OPENAI_API_KEY and a strong POTATO_API_TOKEN for deployment.
./backend/run.sh
```

Important environment variables include `OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_BASE`, `POTATO_API_TOKEN`, `POTATO_ALLOW_ANONYMOUS`, `POTATO_ALLOW_PRIVATE_DEVICE_NETWORKS`, `POTATO_HOME`, `POTATO_DB`, `HOST`, and `PORT`.

For production, use a strong bearer token, `POTATO_ENV=production`, HTTPS for exposed services, and server-side secret management. Do not package backend credentials in the APK.

## Android build

The repository carries the Gradle wrapper and pinned Gradle 8.13 distribution metadata. With Android SDK platform 36 installed:

```bash
./scripts/verify_gradle_wrapper.sh
./gradlew :android:testDebugUnitTest --no-daemon
./gradlew :android:assembleDebug --no-daemon
./gradlew :android:lint --no-daemon --max-workers=1
./gradlew :android:bundleRelease --no-daemon
```

`bundleRelease` produces an unsigned release bundle unless developer-controlled signing / Play App Signing is configured. Signing keys must never be committed to source control.

## Verification

Backend verification:

```bash
python -m compileall -q backend
python -m pytest -q backend/tests
```

Clean source packaging and integrity verification:

```bash
python scripts/package_clean_source.py /tmp/POTATO-JARVIS-FINAL-V5.8.zip
python scripts/verify_source_archive.py /tmp/POTATO-JARVIS-FINAL-V5.8.zip
```

The clean source archive has a single canonical root `POTATO-JARVIS-V5.8/`, excludes build/cache/local-secret/signing/binary output, and contains `SOURCE_MANIFEST.sha256` covering every packaged source file except the manifest itself.

See `RELEASE_REPORT.md` for the exact CI evidence state. Older V5.6/V5.7 roadmap files are retained as historical audit lineage.

## Release status

A green CI build is not the same as production distribution. A production-ready release still requires developer-controlled signing / Play App Signing, policy declarations, Play internal testing, physical-device installation and regression testing, and live deployment verification. POTATO previously encountered a Google Play Protect sensitive-permission block during sideload testing; V5.8 preserves the improved permission/consent design but no source-only change can guarantee an external Play Protect classification outcome.

## Historical audit material

Files whose names explicitly reference V5.1, V5.4 or V5.5 are retained as historical audit/release lineage. They do not describe the current V5.8 build. Current release guidance is `README.md`, `HOW_TO_BUILD.md`, `RELEASE_REPORT.md`, and `CHANGES_V5.8.md`.
