# POTATO V5.6 — Master Repair Roadmap and Acceptance Gates

This roadmap is executable. A stage is complete only when its acceptance gate has evidence.

## 1 — Freeze V5.5 evidence
Starter code: `sha256sum POTATO-JARVIS-FINAL-V5.5.zip && unzip -t POTATO-JARVIS-FINAL-V5.5.zip`
Gate: exact successful V5.5 artifact retained and identified.

## 2 — Create a clean reproducible V5.6 source tree
Starter code: `python scripts/package_clean_source.py /tmp/POTATO-JARVIS-FINAL-V5.6.zip`
Gate: no `build/`, `.gradle/`, APK/AAB, caches, or secrets in source archive; manifest hashes every source file.

## 3 — Enforce Accessibility consent at the data source
Starter code: `if (!AccessibilityConsent.isAccepted(this)) return` inside the service before recording events.
Gate: declining/revoking consent clears captured foreground context and the service no-ops.

## 4 — Preserve Accessibility functionality without cross-app control
Starter code: `TYPE_WINDOW_STATE_CHANGED`, `canRetrieveWindowContent=false`, foreground package only.
Gate: package-name context remains available locally; no text retrieval, click, typing, gesture or setting-control capability is added.

## 5 — Minimize runtime permissions without removing features
Starter code: request location/calendar/contacts independently and only after the relevant user action. Use system `TakePicture` without declaring CAMERA. Request coarse+fine location as its own location group, separately from calendar and contacts.
Gate: camera/OCR remains functional while CAMERA permission is absent; location, calendar, and contacts can be granted or denied independently while precise location remains available when the user chooses it.

## 6 — Harden risk-3 biometric approval on API 26+
Starter code: `setUserAuthenticationValidityDurationSeconds(-1)` on API 26–29 and `setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)` on API 30+.
Gate: every signature operation requires biometric authorization and signs `approvalId:challenge`.

## 7 — Fix streaming correctness
Starter code: join multiple SSE `data:` lines with `\n`, parse events at blank-frame boundaries, and flush a final frame at EOF.
Gate: regression/build checks pass and streaming remains cancellable by disconnect when its coroutine is cancelled.

## 8 — Validate notification deep links on both server and client
Starter code: accept only `potato://tasks/...`, `potato://automations/...`, `potato://approvals/...`, or `potato://notifications/...`; map them to internal screens.
Gate: external/malformed links are rejected and notification taps navigate to a valid POTATO screen.

## 9 — Stop background work from silently reporting configuration failures as success
Starter code: `IllegalArgumentException -> Result.failure()` and `IllegalStateException -> Result.failure()`.
Gate: transient network failures retry; permanent local configuration/programming failures are visible failures.

## 10 — Redact audit secrets before persistence
Starter code: `encoded = json.dumps(redact_sensitive(data or {}))` before INSERT and enforce a bounded audit size.
Gate: tests prove bearer/API secrets never exist in the raw `audit_events.data` row.

## 11 — Validate provider response envelopes and use explicit timeout phases
Starter code: reject non-object provider payloads and non-list `output`; configure connect/read/write/pool timeout values.
Gate: provider validation tests pass and production HTTP OpenAI base URLs remain rejected.

## 12 — Add meaningful Android unit tests
Starter code: JUnit tests for `SecurityGateway` and internal deep-link routing; add future pure parsers here rather than accepting `NO-SOURCE`.
Gate: Gradle executes actual tests; CI verifies test result XML contains test cases.

## 13 — Preserve privacy/backup boundaries
Starter code: `android:allowBackup="false"`, `android:fullBackupContent="false"`, plus API31+ data extraction exclusions.
Gate: merged manifest contains the intended release backup and cleartext settings.

## 14 — Harden GitHub Actions supply chain
Starter code: pin `actions/*` to full verified commit SHAs and set `permissions: contents: read`.
Gate: canonical V5.6 workflow uses immutable action references; legacy version workflows no longer run on every branch push.

## 15 — Build and static-analysis gates
Starter code: `./gradlew :android:testDebugUnitTest :android:assembleDebug :android:lint :android:bundleRelease --no-daemon`.
Gate: resources, tests, debug APK, lint, and release AAB all succeed. No lint baseline or blanket suppression is allowed.

## 16 — Backend gates
Starter code: `python -m compileall -q backend && python -m pytest -q backend/tests`.
Gate: all tests, including new V5.6 security regressions, pass.

## 17 — API and privilege-boundary re-audit
Starter code: compare every Android `/v1/` route with FastAPI routes; grep privileged implementations and confirm model/planner paths cross `execute_tool_async`.
Gate: no orphan required client endpoint and no direct high-risk execution bypass.

## 18 — Release/package integrity
Starter code: hash APK/AAB/source ZIP, `unzip -t` archives, verify package version/API/toolchain markers and source manifest.
Gate: downloadable artifacts match recorded SHA-256 values.

## 19 — Physical device and Google Play gates
Starter procedure: upload signed AAB to Play internal testing, complete Accessibility/Data Safety declarations, install through Play, and run camera, mic, optional context, notifications, biometric approval and backend flows.
Gate: manual evidence required. CI cannot manufacture this result.

## 20 — Independent second audit and score
Starter code: rerun source scans, tests, lint, archive inspection and security checklist from a clean copy.
Gate: engineering-task score >= 9.8/10. Production readiness is reported separately and cannot be PASS while external device/Play/signing gates remain unverified.

## V5.6 second-pass additions discovered while executing the roadmap

The repair loop found additional defects after the first V5.6 draft. They are now part of the acceptance baseline: valid maximum-length upload filenames must not fail because of internal UUID prefixes; audit-size bounding must remain valid JSON; provider streaming must tolerate non-object JSON values; privacy deletion must leave audit history deleted; backend notification links must match the Android route allowlist; `OPENAI_BASE` must reject credentials/query/fragment; background-worker permanent 4xx failures must not be reported as success; the source ZIP must have the canonical root and a non-self-stale manifest; packaging must exclude local secrets/signing/generated output; and all current-facing release documentation must describe V5.6 rather than an older release.

Additional CI starter gates:

```bash
python -m pip install pip-audit==2.10.1
pip-audit -r backend/requirements.txt
python scripts/package_clean_source.py /tmp/POTATO-JARVIS-FINAL-V5.6.zip
python scripts/verify_source_archive.py /tmp/POTATO-JARVIS-FINAL-V5.6.zip
```

Acceptance: dependency audit has no known vulnerable installed requirements, the clean package verifier passes, and the exact archive verified by CI is the archive delivered for the second independent audit.
