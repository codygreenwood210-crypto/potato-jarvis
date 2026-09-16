# POTATO-JARVIS V5.8 Release Verification Report

**Release:** 5.8 (`versionCode 58`)  
**Verified code baseline:** `6cc4f2076d46bf11e9bd3c7d0cbdabff4d9d232a`  
**Status:** Development/runtime working-copy verification passed; production distribution gates remain separate.

## Verified evidence

The V5.8 code baseline has completed the following evidence chain:

- full backend regression suite passed on a real Samsung Android tablet: `132 passed, 2 warnings`
- direct OpenAI Responses API diagnostic returned HTTP `200`
- OpenAI function-tool schema compatibility defect found during physical-device testing was fixed and regression-tested
- non-UTC proactive-test portability defect found on the physical device was fixed and regression-tested
- Android application installed and launched on the physical tablet
- Android application connected to the backend running on the same tablet through Termux + Ubuntu/proot
- app displayed `ONLINE`
- POTATO chat sent `hello` and received a real OpenAI-backed response on the physical device
- prior V5.8 CI verification covered backend tests, Android unit tests, Gradle wrapper verification, lint/build gates, emulator install/launch/connectivity, APK/AAB build evidence and APK signature verification at the validated stage

The previously verified CI-built V5.8 APK SHA-256 was:

```text
c4901417a74c35de6217590e11e6c86591b919c39b84127ded684daa2f50ea70
```

Later V5.8 provider/timezone fixes were backend/test changes and did not modify Android source. Any future Android source change requires a fresh APK/AAB build and new artifact hashes.

## Production distribution remains a separate gate

Do not treat development-mode anonymous access or debug-style sideloading as the intended production deployment. A production release still requires, as applicable:

- developer-controlled release signing / Play App Signing
- production bearer authentication
- HTTPS for exposed backend transport
- Play policy/privacy declarations
- Play internal testing and update/install verification
- final privacy/security regression checks
- current artifact hashes/signature verification

A Google Play Protect block was encountered during earlier sideload testing. The development test proceeded only in a controlled environment. Normal distribution should use proper release signing and Play testing rather than relying on Play Protect being disabled.

## Canonical handoff

For complete recovery, architecture, local-tablet setup, security rules, rebuild commands, provider-debug procedure, known fixes, team operating model, and future-chat continuation instructions, read:

`docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md`

That handoff is the durable continuity source for rebuilding and resuming POTATO V5.8.
