# POTATO-JARVIS V5.1 Changes

V5.1 is the audited remediation release derived from the supplied V5.0 build-fixed archive.

## Changes
1. Production AI provider transport hardening: `OPENAI_BASE` must be an absolute HTTP(S) URL and must use HTTPS when `POTATO_ENV=production`.
2. Added two regression tests covering rejection of production HTTP and acceptance/normalization of production HTTPS.
3. Version advanced from 5.0 / versionCode 50 to 5.1 / versionCode 51.
4. Active README/API version labels corrected.
5. Executable bits restored for POSIX launch/verification scripts in the delivered archive.
6. Added `FINAL_AUDIT_REPORT_V5.1.md` and `FINAL_MANIFEST_V5.1.md`.

No database schema or API contract was intentionally changed.
