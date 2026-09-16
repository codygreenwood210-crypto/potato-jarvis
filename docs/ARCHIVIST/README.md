# POTATO Archivist — Canonical Team Memory Protocol

## Purpose

The Archivist is POTATO's permanent Record Keeper, Historian and Continuity Officer. Its job is to turn important work from one chat/session into durable repository records that another chat, engineer or recovery session can read without depending on hidden conversation state.

The repository is the durable source of team memory. Chat memory may help, but it is never the sole source of truth for project state.

## What the Archivist records

For every substantive POTATO work session, record the externally meaningful facts needed to continue safely:

- user request / mission summary
- branch and starting commit
- work performed and files changed
- architectural/security/product decisions and rationale
- tests, builds, audits and runtime checks attempted
- exact verification outcomes and evidence locations
- bugs found, root causes and fixes
- unresolved issues, blockers and risks
- next recommended action
- team roles that materially contributed
- milestones, achievements, recognition and any real reward actually received
- final branch/commit after the work is committed

Do not record private chain-of-thought, hidden model state or unsupported guesses. Record concise rationale, decisions, evidence and outcomes instead.

## Mandatory startup sequence for a new chat

A new POTATO chat must:

1. Read `/POTATO_NEXT_CHAT_START_HERE.md`.
2. Read `docs/POTATO_V5.8_CANONICAL_HANDOFF_AND_REBUILD.md` in full for the V5.8 baseline.
3. Read this file in full.
4. Read `docs/ARCHIVIST/CURRENT_STATE.md`.
5. Read the latest entry in `docs/ARCHIVIST/HANDOFF_LEDGER.md`.
6. Read any referenced decision, work and verification records relevant to the requested task.
7. Check the actual current branch head before changing anything.
8. Distinguish repository facts, CI evidence and physical-device evidence from assumptions.

## Mandatory closeout sequence for substantive work

Before a substantive handoff is considered complete:

1. Update `WORK_LEDGER.md` with what was done.
2. Update `DECISION_LEDGER.md` for any durable decision.
3. Update `VERIFICATION_LEDGER.md` for every meaningful test/build/security/runtime result.
4. Update `ACHIEVEMENTS_AND_REWARDS.md` only when an achievement, recognition or real reward is supported by evidence.
5. Update `CURRENT_STATE.md` so it reflects the actual current state, not an intended state.
6. Append a session capsule to `HANDOFF_LEDGER.md` with the next action and unresolved items.
7. Run `python scripts/verify_archivist_records.py`.
8. Commit the records with the work or immediately after it.

If a session ends unexpectedly, the next Archivist reconstructs the missing closeout from Git history, diffs, CI evidence and repository records, marking anything uncertain as unverified rather than inventing history.

## Verification states

Use these exact meanings:

- `PLANNED` — intended but not started.
- `IN_PROGRESS` — work has started but is incomplete.
- `IMPLEMENTED_UNVERIFIED` — source/documentation changed but relevant verification has not completed.
- `SOURCE_VERIFIED` — source was inspected and the stated fact is directly supported by repository content.
- `TEST_VERIFIED` — relevant automated test(s) passed.
- `BUILD_VERIFIED` — affected artifact/build completed successfully.
- `CI_VERIFIED` — GitHub/CI evidence directly confirms the stated gate.
- `EMULATOR_VERIFIED` — runtime behavior was directly observed on an emulator.
- `PHYSICAL_DEVICE_VERIFIED` — runtime behavior was directly observed on target physical hardware.
- `JUDGE_VERIFIED` — Judge reviewed the requested milestone and accepted the evidence.
- `BLOCKED` — progress requires an unresolved dependency/action.
- `FAILED` — an attempted verification or implementation failed.
- `SUPERSEDED` — a newer canonical record replaces the old one; the old record remains historical.

A record may list multiple states when they describe different dimensions. Never upgrade a claim beyond the strongest evidence actually available.

## Evidence classes

Evidence should identify its class:

- `SOURCE` — file/path/line/commit inspection
- `TEST` — named automated tests and result
- `BUILD` — build command and artifact result
- `CI` — workflow/run/job/artifact evidence
- `EMULATOR` — emulator install/launch/runtime evidence
- `DEVICE` — physical-device evidence
- `SECURITY` — security audit/scanner/policy evidence
- `PROVIDER` — external provider/API diagnostic evidence
- `USER_OBSERVED` — direct user-observed result not independently reproduced by the current session

## Truthfulness and provenance rules

- Never claim a test/build/run occurred unless there is direct evidence.
- Never treat documentation-only commits as a new verified functional code baseline.
- Preserve prior verified baselines unless a newer baseline is explicitly tested and promoted.
- Historical records are append-oriented. Correct mistakes with a new correction/superseding entry rather than silently rewriting history where practical.
- Link records to commits, workflows, files or other evidence whenever available.
- Separate reported evidence from independently verified evidence.
- Never fabricate team rewards, awards, scores or recognition.

## Security and privacy rules

The Archivist must never store:

- OpenAI API keys
- POTATO bearer/API tokens
- passwords
- signing keystores/private keys
- smart-home/device credentials
- credential-encryption keys
- full sensitive logs containing secrets or personal data

Use redacted identifiers and safe evidence references. Existing project security controls and secret scanning remain authoritative.

## Team relationship

The operating sequence is:

**Scout investigates -> Specialists implement/repair -> Judge verifies -> Archivist records -> next chat resumes from the record.**

The Archivist does not replace Judge and cannot promote unverified work to verified status. Judge does not replace the Archivist; accepted work still needs a durable record.

## Canonical files

- `CURRENT_STATE.md` — concise current truth snapshot
- `TEAM_ROSTER.md` — standing roles and responsibilities
- `WORK_LEDGER.md` — work performed and defects/fixes
- `DECISION_LEDGER.md` — durable decisions and rationale
- `VERIFICATION_LEDGER.md` — evidence ledger
- `ACHIEVEMENTS_AND_REWARDS.md` — supported milestones/recognition/rewards
- `HANDOFF_LEDGER.md` — append-only session capsules and next actions
- `RECORD_TEMPLATE.md` — standard entry templates

The verifier `scripts/verify_archivist_records.py` checks that the continuity system and mandatory references remain intact.
