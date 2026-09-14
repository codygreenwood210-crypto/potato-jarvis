# POTATO-JARVIS V5.6 changes

V5.6 is a repair/verification release built from the exact V5.5 source artifact that passed GitHub Actions. It preserves the V5.5 user-facing feature set while closing defects that the earlier CI gates did not detect.

## Android and privacy

- Enforces Accessibility disclosure consent inside `PotatoAccessibilityService`; decline/revoke clears captured foreground-app context and makes collection a no-op.
- Keeps Accessibility limited to foreground package-name changes with `canRetrieveWindowContent=false`; no screen reading, clicking, typing, gestures or cross-app control is added.
- Connects consented foreground-package context into `DeviceContextSnapshot`.
- Removes the unnecessary direct `CAMERA` permission while preserving system-camera capture and OCR/vision flows.
- Separates location, calendar and contacts permission requests while preserving both approximate and precise location capability.
- Uses authentication-per-use for risk-3 Android Keystore signatures on API 26–29 and strong biometric authentication parameters on API 30+.
- Adds strict internal `potato://` notification routing on Android and mirrors the contract on the backend.
- Fixes SSE multi-line frame assembly and EOF flushing with a pure parser plus JVM regression tests.
- Treats HTTP 408/429 and server/network failures as retryable background-work failures; other 4xx/configuration failures are permanent failures rather than false successes.
- Keeps Android backups disabled with legacy and API31+ backup/data-extraction controls.
- Makes relevant Compose UI state saveable across activity recreation where applicable.

## Backend and security

- Redacts sensitive audit values before database persistence, not only when returning diagnostics.
- Bounds oversized audit records using a valid JSON truncation envelope rather than truncating serialized JSON into invalid data.
- Ensures privacy deletion does not immediately recreate an audit row after deleting audit history.
- Validates notification deep links against the same narrow POTATO route contract used by Android.
- Validates OpenAI base URLs more strictly, rejecting embedded credentials, query strings and fragments; production still requires HTTPS.
- Validates provider response envelopes and safely handles streaming JSON values that are valid JSON but not objects.
- Adds explicit HTTP connect/read/write/pool timeout phases.
- Stores uploaded files under bounded internal UUID-derived names while retaining the original filename as metadata, avoiding failures for valid maximum-length user filenames.
- Expands regression coverage for audit redaction/truncation, privacy deletion, provider shape/base URL handling, upload filename boundaries and deep-link contracts.

## Tests, CI and packaging

- Adds real Android JUnit tests for the security gateway, deep-link router and SSE frame accumulator; CI fails if zero tests execute.
- Pins GitHub Actions dependencies to immutable commit SHAs and limits workflow permissions to read-only repository contents.
- Makes Android lint blocking and removes stale diagnostic workflow assumptions from the release source.
- Adds Python dependency vulnerability auditing to the canonical V5.6 verification workflow.
- Replaces the V5.5 build-output-heavy source package with a clean reproducible source packager.
- Enforces the canonical archive root `POTATO-JARVIS-V5.6/`.
- Excludes build/cache/IDE output, local environment files, signing material, APK/AAB output and other generated artifacts.
- Generates a fresh `SOURCE_MANIFEST.sha256` and verifies complete manifest coverage and file hashes.
- Retains older version-named audit files as explicit historical lineage while current V5.6 documentation is synchronized to this release.

## Dependency security refresh
- FastAPI 0.141.1 and Starlette 1.6.0.
- python-multipart 0.0.32.
- pypdf 6.18.0.
- pytest 9.1.1.
- `pip-audit` is a mandatory zero-known-vulnerability release gate.

