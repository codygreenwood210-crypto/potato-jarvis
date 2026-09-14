# POTATO V3.2 — Complete Codebase Audit, Web-Verified Remediation & Re-Audit

## 1. Executive Summary

Overall health: **MEDIUM / IMPROVED, NOT PRODUCTION-READY**.

The V3.0 archive contained a runnable/testable FastAPI backend, a Kotlin/Compose Android client, Gradle configuration, CI, documentation, and a 46-test backend suite. The backend was executable and initially passed 46/46 tests, but the audit found several confirmed issues that materially affected correctness and security:

- a confirmed Kotlin syntax error in the Android multipart upload implementation;
- stale Android/root version metadata;
- missing Android `USE_BIOMETRIC` permission;
- a confirmed high-risk approval biometric bypass: the backend accepted `allow=true` without cryptographic proof of biometric authentication;
- approval listing exposed raw approval arguments, including potentially sensitive file content;
- plan approval authorization was not sufficiently scoped to the exact plan step and was not claimed before execution;
- production authentication accepted anonymous mode or weak bearer tokens when misconfigured;
- `.env.example` documented `POTATO_HOST`/`POTATO_PORT` while the launcher actually uses `HOST`/`PORT`;
- Android build verification remains blocked by the missing official Gradle Wrapper JAR and unavailable Android SDK/system Gradle.

The remediation added a server-verifiable biometric challenge/signature protocol using an Android Keystore EC signing key gated by strong biometric authentication, exact plan approval scoping, approval redaction, production authentication guardrails, storage permission hardening, metadata synchronization, and regression coverage.

### Scores

- Code Quality: **7.5/10** — coherent architecture and strong central security gateway, but `backend/main.py` remains large and some concerns are tightly coupled.
- Security: **7.5/10** — major approval weakness fixed and upload/device protections are strong; rate limiting, multi-user isolation, and production device/network hardening still need additional work.
- Stability: **7/10** — backend tests are strong; Android runtime/build behavior remains unverified.
- Production Readiness: **5.5/10** — backend is close to a controlled deployment baseline, but Android build/device verification and dependency scanning remain outstanding.

Overall risk: **HIGH until Android build/device verification and deployment controls are completed; MEDIUM for the tested backend after V3.2 remediation.**

## 2. Technology Stack

- Backend: Python 3.13 environment, FastAPI, Uvicorn, SQLite, Pydantic, HTTPX, python-dotenv, multipart uploads, pypdf, python-docx, openpyxl, cryptography.
- AI provider: OpenAI Responses API through `backend/providers.py`.
- Android: Kotlin, Jetpack Compose, AndroidX Activity/Lifecycle/WorkManager/Biometric/Fragment, Android Keystore, SpeechRecognizer/TTS, FileProvider.
- Build: Gradle Kotlin DSL, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Gradle 8.9 distribution declaration.
- CI: GitHub Actions with Python backend tests and an Android build gate using Gradle Wrapper validation.
- Database: SQLite on backend and Android local SQLite history/settings.

## 3. Project Areas Inspected

Inspected all 46 files in the V3.0 archive, including:

- backend application and provider integration;
- all backend tests;
- database schema/migrations;
- authentication and authorization/security gateway;
- approvals, planner, orchestrator, task state machine, automations, devices;
- file upload/extraction and vision processing;
- Android Activity/UI/ViewModel/API/database/security/voice/automation/accessibility code;
- Android manifests, FileProvider/accessibility resources and theme;
- Gradle build configuration and wrapper properties/scripts;
- GitHub Actions CI;
- README, API, architecture, security and roadmap documentation;
- environment example and verification scripts.

Static source inspection was supplemented with Python compilation, pytest execution, live Uvicorn startup, HTTP smoke tests, Kotlin parser/compiler checks, and direct security probes.

## 4. Complete Bug Report

### BUG-001
Status: **CONFIRMED**

File: `android/src/main/java/com/potato/jarvis/core/JarvisApi.kt`
Location: multipart upload header construction.
Severity: **CRITICAL**
Category: Android syntax/build.

Problem: the filename header string contained a malformed Kotlin string literal split across lines (`"\r\` followed by a newline). `kotlinc` directly reported `unexpected token` and `expecting '"'`.

Impact: the Android module could not compile even if the Android/Gradle toolchain were otherwise available.

Fix: replaced the malformed literal with a valid `"\\r\\n"` sequence.

Web research: NO; this was directly demonstrated by compilation.

Testing: standalone Kotlin parser/compiler invocation after remediation produced no Kotlin parser/syntax errors; Android/AndroidX classpath diagnostics remain because the SDK is unavailable.

### BUG-002
Status: **CONFIRMED**

Files: `settings.gradle.kts`, Android build metadata, README/UI.
Severity: **MEDIUM**
Category: release/version consistency.

Problem: root project name and UI/documentation contained stale V2.x/V3.0 identifiers while the archive was labeled V3.0.

Fix: synchronized V3.2 metadata and Android versionCode 31/versionName 3.1.

### BUG-003
Status: **CONFIRMED**

File: `android/src/main/AndroidManifest.xml`.
Severity: **HIGH**
Category: Android authentication functionality/security.

Problem: `BiometricPrompt` is used for sensitive approvals but `android.permission.USE_BIOMETRIC` was not declared.

Fix: added `USE_BIOMETRIC`.

Web research: YES. Android's current BiometricPrompt documentation states biometric authentication requires `USE_BIOMETRIC` and documents CryptoObject-based authentication. See the Android API reference.

### BUG-004
Status: **CONFIRMED**

Files: backend approval endpoint and Android Security UI.
Severity: **CRITICAL**
Category: authorization / approval bypass.

Problem: the backend marked risk-3 approvals as `biometric_required`, but `/v1/approvals/{id}` accepted `{allow:true}` without verifying that Android biometric authentication had actually occurred. A direct authenticated API request could approve a risk-3 action without biometric proof.

Reproduction performed during audit: a risk-3 `delete_note` approval was approved successfully with only `{allow:true}`. The tool then attempted execution and returned `note not found`, proving the approval gate itself had been bypassed.

Fix: V3.2 registers an Android Keystore EC public key, issues a short-lived single-use server challenge, requires a signature over `approval_id:challenge`, and verifies that signature server-side before approving a risk-3 action. The Android private key is gated by `BIOMETRIC_STRONG` through Android Keystore/CryptoObject.

Web research: YES. Android's current KeyGenParameterSpec documentation describes authentication-gated keys and BiometricPrompt CryptoObjects; Android's BiometricPrompt documentation states CryptoObjects are intended to unlock cryptographic operations after authentication.

### BUG-005
Status: **CONFIRMED**

File: `/v1/approvals` response.
Severity: **HIGH**
Category: sensitive data exposure / API object property exposure.

Problem: the endpoint returned raw `args_json`. A `write_file` or `write_note` approval could therefore expose the entire content being approved to any authenticated API client/UI.

Fix: V3.2 returns a sanitized `arguments` view. File/note content is replaced by a redacted marker and length; credential-like fields are redacted.

### BUG-006
Status: **CONFIRMED**

File: `execute_plan` approval handling.
Severity: **HIGH**
Category: authorization / approval binding / concurrency.

Problem: plan execution could consult generic approval state rather than requiring the approval to belong to the exact plan step. Approved state was also not claimed before tool execution, leaving a concurrency window.

Fix: V3.2 scopes plan approvals by tool + exact argument hash + `source_type=plan` + `source_id=plan_id` + `step_index`, and atomically marks the approval consumed before executing the tool.

### BUG-007
Status: **CONFIRMED**

File: `require_auth`.
Severity: **HIGH**
Category: security misconfiguration / authentication.

Problem: the configuration permitted anonymous mode or weak bearer tokens if deployment settings were careless.

Fix: when `POTATO_ENV=production`, anonymous mode is rejected and the bearer token must exist and be at least 32 characters.

### BUG-008
Status: **CONFIRMED**

File: `backend/.env.example`.
Severity: **LOW**
Category: configuration inconsistency.

Problem: the example used `POTATO_HOST`/`POTATO_PORT` while the launcher uses `HOST`/`PORT`.

Fix: example now documents `HOST` and `PORT`, plus explicit `POTATO_ENV`.

### BUG-009
Status: **LIKELY**

Area: backend device HTTP adapters.
Severity: **HIGH**
Category: SSRF / DNS rebinding.

Problem: device hostnames are resolved for validation, but HTTPX later resolves the hostname again at request time. A hostile or compromised DNS response could theoretically change a previously validated public address to a private/reserved address between validation and connection.

Current mitigation: private/loopback/link-local/reserved addresses are blocked by default, redirects are disabled, credentials in URLs are rejected, and paths are constrained.

Required future fix: pin resolved addresses or use an explicit host/IP allowlist for device adapters, with careful TLS/SNI handling.

Classification is LIKELY rather than CONFIRMED because exploitation depends on controlling DNS resolution between validation and connection.

## 5. OWASP Security Report

The audit used OWASP API Security Top 10 2023 and OWASP mobile security guidance. OWASP identifies broken authorization, broken authentication, unrestricted resource consumption, SSRF, security misconfiguration, and unsafe API consumption as major API risks. The project directly intersects these categories. citeturn0search0turn0search3

### SEC-001 — Broken Function/Object Authorization around approvals
Status: **CONFIRMED / FIXED**
OWASP: API2, API5, API6.

The original biometric approval was client-enforced only. V3.2 adds server-verifiable proof.

### SEC-002 — Sensitive approval argument exposure
Status: **CONFIRMED / FIXED**
OWASP: API3.

Raw approval arguments are no longer exposed through the approval-list API.

### SEC-003 — Approval scope/replay
Status: **CONFIRMED / FIXED**
OWASP: API1/API5/API6.

Plan approvals are now exact-step scoped and claimed atomically before execution.

### SEC-004 — Production authentication misconfiguration
Status: **CONFIRMED / FIXED**
OWASP: API2/API8.

Production now rejects anonymous mode and weak tokens.

### SEC-005 — SSRF/DNS rebinding risk in device adapters
Status: **LIKELY / PARTIALLY MITIGATED**
OWASP: API7.

Default private-address blocking and redirect denial are strong controls, but hostname resolution should eventually be pinned or allowlisted.

### SEC-006 — Unrestricted resource consumption
Status: **LIKELY / PARTIALLY MITIGATED**
OWASP: API4.

The application has upload, vision, tool-call, plan-step, automation, archive-expansion, concurrency, and payload limits. A production deployment still needs network-level rate limiting and request quotas.

### Mobile security

OWASP MASVS is the appropriate mobile verification baseline. The app uses Android Keystore for the API token and now also uses a Keystore-backed biometric signing key. Android's security guidance emphasizes minimization, protected storage, network security, and permission minimization. citeturn0search10turn0search15

## 6. Dependency & Configuration Report

The repository pins direct Python dependencies but has no lockfile and no executed dependency vulnerability scanner.

Current web-verified observations:

- FastAPI 0.141.1 is the current PyPI release observed during this audit; the project pins 0.128.2. citeturn1search3
- Uvicorn 0.52.4 is the current PyPI release observed; the project pins 0.48.0. citeturn1search1
- python-dotenv 1.2.3 is current; the project pins 1.2.2. citeturn2search1
- python-multipart 0.0.32 is current; the project pins 0.0.29. citeturn3search0
- pypdf 6.17.0 is current; the project pins 5.9.0. citeturn1search2
- pytest 9.1.1 is current; the project pins 9.0.2. citeturn2search2
- cryptography 50.0.1 is current and is now explicitly pinned by V3.2. citeturn5search0
- HTTPX 0.28.1 remains the latest stable release shown by PyPI; the project is current on that package. citeturn3search2

These are **outdated-version findings, not confirmed vulnerabilities**. Upgrades should be performed in a build-capable environment and validated against the application before changing the remaining pins.

Android current documentation shows that AGP 8.7 requires Gradle 8.9, so the selected 8.7.3/8.9 pairing is compatible. However, current Android tooling is already on AGP 9.x, so V3.2 should plan a controlled AGP/Kotlin migration after the build environment is restored rather than mixing an unverified upgrade into this security release. citeturn8search0turn8search1

Compose's current stable BOM is 2026.08.00; the project uses 2025.02.00. AndroidX Activity 1.13.0, Lifecycle 2.11.0, and Core 1.19.0 are current stable releases. These should be upgraded only with a working Android build because newer AndroidX releases can impose newer compileSdk/AGP requirements. citeturn4search0turn4search2turn2search3turn4search3

## 7. Architecture Review

Strengths:

- central ToolRegistry;
- application-side security authority;
- exact argument hashing;
- durable task/plan state;
- upload/path validation;
- SSRF-resistant default device policy;
- server-side AI credentials;
- trace/audit model;
- Android Keystore storage;
- lifecycle-aware voice resources.

Weaknesses:

- `backend/main.py` is very large and combines routing, persistence, domain logic, security, orchestration, and adapters;
- single bearer-token authentication is single-user oriented and not suitable for multi-user deployment;
- SQLite is appropriate for a personal assistant but needs a clear concurrency/deployment boundary;
- audit/tool tables can grow without retention/rotation policy;
- no production rate limiting layer is implemented.

## 8. Performance & Stability Review

Positive controls:

- bounded upload sizes;
- bounded extracted text;
- bounded archive expansion;
- bounded vision concurrency;
- bounded tool rounds/calls;
- bounded plan steps;
- automation action budget;
- WorkManager retry classification;
- no automatic retry for mutating tools.

Remaining concerns:

- synchronous SQLite calls are used from FastAPI request functions; most are short, but production concurrency should be measured;
- PDF/DOCX/XLSX extraction is CPU/file-I/O work and should eventually be isolated or queued for larger deployments;
- device HTTP calls use a synchronous HTTPX client inside a worker thread, which is acceptable at current scale but should be measured under load.

## 9. Web Research & Verified Recommendations

1. OWASP API Security Top 10 2023 was used for authorization, authentication, resource consumption, SSRF, misconfiguration and unsafe API-consumption classification. citeturn0search0turn0search3
2. OWASP MASVS/mobile guidance was used for Android security review. citeturn0search10
3. Android's current BiometricPrompt and KeyGenParameterSpec documentation was used to design the cryptographic biometric approval flow. citeturn6search0turn6search8turn7search7
4. Gradle's current security guidance was used to retain distribution checksum verification and to avoid inserting an unverified Wrapper JAR. citeturn0search4turn0search8
5. Current PyPI release data was used to distinguish outdated dependencies from confirmed vulnerabilities. citeturn1search3turn1search1turn3search0turn1search2turn2search1turn2search2turn5search0
6. Current AndroidX/Compose release data was used to identify upgrade opportunities without falsely claiming compatibility. citeturn4search1turn4search3

## 10. Prioritized Fix Plan

STEP 1 — restore verified Android build toolchain.

STEP 2 — run APK build, lint and instrumentation/device tests.

STEP 3 — test actual biometric Keystore signing on API 28+ devices/emulators with strong biometric support.

STEP 4 — implement rate limiting at the deployment edge or application layer.

STEP 5 — resolve device hostname/DNS rebinding risk with address pinning or explicit allowlists.

STEP 6 — migrate AndroidX/Compose/backend packages in controlled batches after the build is green.

STEP 7 — split the monolithic backend module into security, persistence, tools, planner, device, and API modules.

## 11. Complete Corrected Files

The complete copy/paste-ready corrected files are stored in `COMPLETE_CORRECTED_FILES_V3.2.md` in this release package. No patch fragments are used.

Files changed in V3.2:

- `backend/main.py`
- `backend/requirements.txt`
- `backend/.env.example`
- `backend/tests/test_main.py`
- `android/src/main/AndroidManifest.xml`
- `android/src/main/java/com/potato/jarvis/MainActivity.kt`
- `android/src/main/java/com/potato/jarvis/core/JarvisApi.kt`
- `android/src/main/java/com/potato/jarvis/core/SecureTokenStore.kt`
- `android/src/main/java/com/potato/jarvis/ui/JarvisViewModel.kt`
- `android/build.gradle.kts`
- `settings.gradle.kts`
- `VERSION`
- `README.md`
- `docs/API.md`
- `docs/SECURITY.md`
- `docs/ARCHITECTURE.md`
- `RELEASE_REPORT.md`

## 12. Testing Plan

### EXECUTED

- `python3 -m pytest -q backend/tests`
- `python3 -m compileall -q backend`
- `./scripts/verify_backend.sh`
- live `backend.main:app` startup and HTTP smoke tests
- production authentication tests
- biometric challenge/signature tests using generated EC test keys
- Kotlin parser/compiler source scan
- `./gradlew :android:assembleDebug` attempt

### RECOMMENDED / NOT YET EXECUTED

- Android `assembleDebug`
- Android lint
- Android instrumentation tests
- physical-device biometric tests
- camera/voice lifecycle tests
- real OpenAI provider E2E
- load testing
- `pip-audit` or equivalent dependency scanner
- dynamic SSRF/DNS-rebinding test against a controlled resolver

## 13. Regression Testing

Final backend suite: **49/49 PASS**.

Coverage includes authentication, sessions, memory, preferences, tools, chat persistence, file safety, approvals, exact arguments, planner dependencies, tasks, automations, device security, diagnostics/traces, provider failures, vision validation, biometric approval proof, production authentication, and approval redaction.

No backend regression was detected after the V3.2 fixes.

## 14. Post-Fix Re-Audit

Original issues fixed:

- Android multipart syntax error — fixed.
- Version metadata drift — fixed.
- Missing biometric permission — fixed.
- Client-only biometric authorization — fixed with server cryptographic assertion.
- Raw approval argument exposure — fixed.
- Plan approval scope/claim weakness — fixed.
- Production anonymous/weak-token configuration — fixed.
- Environment variable documentation mismatch — fixed.

New issues discovered:

- Device hostname DNS-rebinding risk remains LIKELY and should be addressed before exposing device adapters to untrusted networks.
- Dependency versions are behind current releases, but no vulnerability was inferred solely from age.

Security regressions: none found in the tested backend suite.

Build concerns: Android build remains blocked by the missing official Gradle Wrapper JAR and unavailable Android SDK/system Gradle.

## 15. Final Verification Checklist

BUILD

- [x] Syntax checked
- [x] Backend compilation checked
- [x] Imports checked for backend
- [x] Dependencies reviewed
- [x] Build command identified
- [ ] Android build executed successfully

RUNTIME

- [x] Backend startup reviewed
- [x] Backend main workflows covered by tests
- [x] Async tool paths reviewed
- [x] Error handling reviewed
- [x] Live backend smoke executed

SECURITY

- [x] OWASP API review completed
- [x] Android/mobile security review completed
- [x] Authentication reviewed
- [x] Authorization reviewed
- [x] Input validation reviewed
- [x] Sensitive data reviewed
- [x] Secrets reviewed
- [x] Network/device security reviewed
- [x] Dependency versions researched
- [ ] Automated dependency vulnerability scanner executed

QUALITY

- [x] Duplicate code reviewed
- [x] Dead/stale metadata reviewed
- [x] Error handling reviewed
- [x] Architecture reviewed
- [x] Performance/stability reviewed

PRODUCTION

- [x] Environment configuration reviewed
- [x] Build configuration reviewed
- [x] Deployment configuration reviewed
- [x] Logging/audit behavior reviewed
- [x] Monitoring considerations documented
- [x] Remaining risks documented

## 16. Remaining Risks

1. **Android build/toolchain unavailable** — blocks production APK verification.
2. **Real biometric hardware unverified** — the protocol is source/test verified but not device verified.
3. **Real OpenAI provider E2E unavailable** — no production credential was supplied.
4. **Device DNS rebinding risk** — LIKELY, should be fixed before broad smart-device exposure.
5. **No application-level rate limiting** — production deployment should use an API gateway/reverse proxy or a bounded rate-limit implementation.
6. **Single-user bearer token model** — not suitable for multi-user deployment without identity/object ownership controls.
7. **No dependency vulnerability scanner executed** — `pip-audit` is unavailable in this environment.
8. **Older Android/backend dependency pins** — not automatically vulnerabilities, but should be upgraded in controlled, build-verified batches.

## 17. Limitations

- No Android SDK is available.
- No system Gradle is available.
- The official `gradle-wrapper.jar` is absent.
- Network DNS is unavailable to the container for downloading Gradle artifacts.
- No emulator or physical Android device is available.
- No production OpenAI API credential is available.
- No production deployment environment is available.
- `pip-audit` is not installed.
- Kotlin compiler can detect source syntax but cannot resolve Android/AndroidX dependencies without the Android SDK/classpath.

Final status: **🟡 STABLE WITH KNOWN LIMITATIONS**. The backend security and correctness baseline is materially improved and verified; the project is not yet honestly certifiable as production-ready until Android build/device verification is completed.
