# POTATO — NEW CHAT START HERE

This file is the first entry point for any new ChatGPT conversation or engineer continuing POTATO Jarvis. Do not reconstruct the project from vague memory and do not ask the user to re-explain the project before reading the repository.

## Mandatory startup sequence

1. Open repository `codygreenwood210-crypto/potato-jarvis`.
2. Use branch `potato-v5.8-final` unless the user explicitly promotes a newer branch/version.
3. Read `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md` **in full** before changing code.
4. Read `docs/ARCHIVIST/README.md` **in full**.
5. Read `docs/ARCHIVIST/CURRENT_STATE.md` and the latest entry in `docs/ARCHIVIST/HANDOFF_LEDGER.md`.
6. Then read `README.md`, `HOW_TO_BUILD.md`, `CHANGES_V5.8.md`, `RELEASE_REPORT.md`, relevant current workflows, recent commits, and any Archivist records referenced by the latest handoff.
7. Check the current branch head and distinguish documentation/governance-only commits from the verified functional code baseline.
8. Run or inspect the relevant tests/build/runtime evidence before claiming completion.
9. Before handing substantive work to another chat, follow the Archivist closeout sequence and run `python scripts/verify_archivist_records.py`.

## Current verified functional baseline

- Version: POTATO Jarvis V5.8
- Android package: `com.potato.jarvis`
- Verified functional code baseline: `6cc4f2076d46bf11e9bd3c7d0cbdabff4d9d232a`
- The branch may contain newer documentation/governance/handoff commits after that baseline.
- Android: Kotlin + Jetpack Compose / Material 3
- Backend: FastAPI + Uvicorn + SQLite + httpx
- OpenAI provider: Responses API, default model `gpt-5.6-luna`
- Android build baseline: compile/target SDK 36, min SDK 26, AGP 8.12.2, Kotlin 2.0.21, Gradle 8.13, Java/Kotlin target 17

## Physical-device milestone already achieved

POTATO V5.8 is installed on a real Samsung Android tablet and has completed a real OpenAI-backed chat request on that physical device.

Verified physical-tablet evidence:

- APK installed and `MainActivity` launched.
- POTATO connected to the V5.8 backend running locally on the same tablet through Termux + Ubuntu/proot.
- Android app displayed `ONLINE`.
- Backend full regression suite passed on the tablet: `132 passed, 2 warnings`.
- Direct OpenAI Responses API diagnostic returned HTTP `200` from the tablet environment.
- Real POTATO chat sent `hello` and received: `Hello! I’m POTATO 🥔 How can I help today?`
- The OpenAI optional-tool strict-schema failure was found from the real backend audit log and permanently fixed.
- A UTC-vs-local-time proactive test bug exposed by the Australian tablet environment was permanently fixed.

The earlier exact verified CI-built V5.8 APK SHA-256 recorded by the project is:

`c4901417a74c35de6217590e11e6c86591b919c39b84127ded684daa2f50ea70`

Do not claim a later Android source change is present in that binary unless a new APK was built and hashed after the change.

## Current tablet development connection pattern

When app and backend run on the same tablet:

- Backend URL in POTATO: `http://127.0.0.1:8000`
- Local development backend can use `POTATO_ENV=development POTATO_ALLOW_ANONYMOUS=true` for smoke testing.
- Android API-token field may be blank only for that explicit anonymous development mode.
- `OPENAI_API_KEY` belongs only on the backend and must never be pasted into the Android API-token field.

The canonical handoff contains the exact Termux / Ubuntu / venv / backend-start commands and the safe OpenAI diagnostic procedure.

## User working style and continuity rules

- The user calls the assistant **Nova**.
- Treat POTATO as a long-term software/product program, not a one-off coding request.
- The user prefers **one clear recommended path**, not a menu of choices unless alternatives are genuinely required.
- When the user says `continue`, resume from the current repository state and act; do not ask them to repeat known context.
- Prefer permanent root-cause fixes over patches/workarounds.
- Do not fabricate builds, tests, security checks, device verification, signatures, hashes, rewards, recognition or release readiness.
- Distinguish source inspection, CI evidence, emulator evidence and physical-device evidence.
- Keep all secrets out of chat, screenshots, logs, commits, docs and memory summaries.

## Judge / Scout / Archivist / team operating model

POTATO uses a coordinated senior engineering-team model.

- **Scout** re-checks current external technical evidence when needed.
- **Specialists** investigate, design, implement, repair and test within their disciplines.
- **Judge** is the independent task-quality and completion arbiter.
- **The Archivist** is the permanent Record Keeper, Historian and Continuity Officer. It records externally meaningful work, decisions, verification, handoffs, contributions, milestones, achievements and real rewards/recognition in `docs/ARCHIVIST/`.

Operating sequence:

**Scout investigates -> Specialists perform the work -> Judge verifies -> Archivist records -> next chat resumes from the record.**

Important standing rules:

- Judge must not inflate scores. A 10/10 requires direct evidence for the requested milestone.
- If a milestone is below the requested completion bar and work can continue, continue fixing/testing rather than pretending it is complete.
- The Archivist does not replace Judge and cannot turn an unverified statement into verified history.
- The Archivist records concise rationale and evidence, not private chain-of-thought or hidden model state.
- Add/strengthen specialist responsibilities when a real failure exposes a capability gap, then record the lesson and add regression coverage where practical.

## Archivist closeout rule

For every substantive POTATO work session, update the applicable files under `docs/ARCHIVIST/` before handoff:

- `WORK_LEDGER.md`
- `DECISION_LEDGER.md` when a durable decision changed
- `VERIFICATION_LEDGER.md`
- `ACHIEVEMENTS_AND_REWARDS.md` only for supported achievements/recognition/rewards
- `CURRENT_STATE.md`
- `HANDOFF_LEDGER.md`

Then run:

```bash
python scripts/verify_archivist_records.py
```

If a prior session ended before closeout, reconstruct only what can be supported by Git history, diffs, CI/runtime evidence and existing records. Mark uncertainty; never invent history.

## Critical fixes that must not regress

1. OpenAI function tool strictness must only be enabled when the schema is strict-compatible; tools with legitimate optional properties must not be sent as invalid strict schemas.
2. Proactive quiet-hour tests must use the same local-time basis as production or use deterministic injected/frozen time; do not assume UTC.
3. Model/tool output is not the security authority; POTATO application code enforces authorization/risk/approval rules.
4. OpenAI credentials stay backend-only.
5. Production backend requires proper authentication and HTTPS; anonymous mode is development-only.
6. Accessibility remains narrowly scoped unless a new explicit privacy/security review approves expansion.

## Production work still separate from the achieved physical-device smoke milestone

Do not confuse the successful real-tablet V5.8 development smoke test with final Play production distribution. Remaining production/release gates can include developer-controlled release signing / Play App Signing, Play policy declarations, Play internal testing, production bearer auth, HTTPS deployment, secrets management, physical update testing, privacy/security regression and fresh artifact/hash recording after future source changes.

## Source of truth

The durable project memory is:

1. repository source and Git history,
2. `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md`,
3. `docs/ARCHIVIST/` records,
4. tests,
5. CI/build evidence,
6. emulator/physical-device runtime evidence.

Never substitute an assumption or remembered claim for those sources.

## Copy/paste instruction for a brand-new ChatGPT chat

Use this as the first message in a new chat:

> Open my GitHub repository `codygreenwood210-crypto/potato-jarvis`, use branch `potato-v5.8-final`, and read `POTATO_NEXT_CHAT_START_HERE.md`, `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md`, `docs/ARCHIVIST/README.md`, `docs/ARCHIVIST/CURRENT_STATE.md`, and the latest entry in `docs/ARCHIVIST/HANDOFF_LEDGER.md` in full before doing anything. Continue POTATO Jarvis from the recorded verified state. Preserve the Judge/Scout/Archivist/team operating model, security model, tests, build evidence and real-device evidence. Do not ask me to repeat information already recorded there, do not expose secrets, and do not claim completion without direct verification. Before handing off substantive work, update the Archivist records.
