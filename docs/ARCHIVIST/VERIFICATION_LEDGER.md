# POTATO Archivist — Verification Ledger

This ledger records evidence, including failures. A failed check is useful history and must not be rewritten as a pass.

## Record format

Each entry should include: `ID`, `Date`, `Scope`, `Evidence class`, `Command/check`, `Result`, `State`, `Artifact/reference`, and `Notes/limitations`.

---

### VER-2026-001 — Physical-tablet backend regression suite

- **Date:** 2026-09-16 (recorded from canonical V5.8 handoff)
- **Scope:** V5.8 backend on real Samsung Android tablet environment.
- **Evidence class:** DEVICE + TEST
- **Command/check:** full backend regression suite in the tablet Ubuntu/proot environment.
- **Result:** `132 passed, 2 warnings`, no test failures.
- **State:** PHYSICAL_DEVICE_VERIFIED + TEST_VERIFIED + JUDGE_VERIFIED for this milestone.
- **Artifact/reference:** `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md`, `RELEASE_REPORT.md`.
- **Notes/limitations:** warnings were dependency deprecation warnings; this does not imply all future production gates are complete.

### VER-2026-002 — Direct OpenAI Responses API diagnostic

- **Date:** 2026-09-16 (recorded from canonical V5.8 handoff)
- **Scope:** provider connectivity from physical-tablet backend environment.
- **Evidence class:** DEVICE + PROVIDER
- **Command/check:** safe direct Responses API diagnostic using locally loaded backend credential.
- **Result:** HTTP `200`.
- **State:** PHYSICAL_DEVICE_VERIFIED.
- **Artifact/reference:** canonical V5.8 handoff section describing provider recovery diagnostic.
- **Notes/limitations:** secret value was not and must not be recorded.

### VER-2026-003 — Real POTATO physical-device chat

- **Date:** 2026-09-16 (recorded from canonical V5.8 handoff)
- **Scope:** Android client + local backend + OpenAI provider on physical tablet.
- **Evidence class:** DEVICE + PROVIDER + USER_OBSERVED
- **Command/check:** app sent `hello` after showing `ONLINE`.
- **Result:** received `Hello! I’m POTATO 🥔 How can I help today?`.
- **State:** PHYSICAL_DEVICE_VERIFIED + JUDGE_VERIFIED for the live chat milestone.
- **Artifact/reference:** canonical V5.8 handoff and release report.
- **Notes/limitations:** development/runtime milestone, not production Play-distribution verification.

### VER-2026-004 — Earlier CI-built V5.8 debug APK identity

- **Date:** historical V5.8 verification, carried into 2026-09-16 continuity records.
- **Scope:** earlier CI-built V5.8 debug APK.
- **Evidence class:** CI + BUILD
- **Command/check:** artifact hashing/signature verification in the V5.8 evidence chain.
- **Result:** SHA-256 `c4901417a74c35de6217590e11e6c86591b919c39b84127ded684daa2f50ea70`.
- **State:** CI_VERIFIED + BUILD_VERIFIED for that exact artifact.
- **Artifact/reference:** `RELEASE_REPORT.md` and canonical V5.8 handoff.
- **Notes/limitations:** do not associate later Android source changes with this binary without a fresh rebuild/hash.

### VER-2026-005 — Archivist continuity verifier

- **Date:** 2026-09-16
- **Scope:** Archivist record-system structure and mandatory continuity references.
- **Evidence class:** TEST
- **Command/check:** `python scripts/verify_archivist_records.py`
- **Result:** `ARCHIVIST_VERIFY=PASS files=10 records=17`.
- **State:** TEST_VERIFIED for the Archivist record-system structure.
- **Artifact/reference:** `scripts/verify_archivist_records.py`.
- **Notes/limitations:** executed locally against the complete proposed Archivist file set before repository commit. This verifier checks record-system integrity; it does not verify the Android/backend product itself. CI status remains separate evidence.
