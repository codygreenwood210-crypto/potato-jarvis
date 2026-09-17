# Universal Team Judge — 11/10 Enforcement Standard

**Effective:** 2026-09-17  
**Scope:** Universal Team and Potato Network OS Judge-controlled substantive tasks  
**Status:** CURRENT / USER-DIRECTED / SUPERSEDES PRIOR ORDINARY 10/10 COMPLETION RULE

## Core rule

The Judge is a mandatory enforcement gate, not merely an advisory reviewer.

For every Judge-controlled task, the operating loop is:

**WORK -> TEST -> JUDGE -> FAIL -> REWORK_REQUIRED -> REASSIGN/FIX/REDO -> RETEST -> JUDGE AGAIN**

Repeat until PASS.

A task cannot move to `COMPLETE` while Judge scores it below **11/10**. Scores from **1/10 through 10/10 are FAIL / REWORK REQUIRED**. Only **11/10 is PASS / JUDGE VERIFIED**.

This newer user-directed rule supersedes the earlier Universal Team rule that ordinary project steps could complete at 10/10. Historical scores remain historical evidence and must not be silently rewritten.

The user remains the ultimate authority and may explicitly override a mission gate, but no team member may silently bypass Judge.

## Mandatory failure behavior

Whenever Judge scores below 11/10, Judge must provide:

- exact score;
- PASS/FAIL state;
- acceptance criteria that passed;
- acceptance criteria that failed or remain missing;
- evidence reviewed;
- defects and unresolved risks;
- exact corrections required;
- specialist/role that should perform the rework;
- required retest or verification;
- next state: `REWORK_REQUIRED`.

The work then returns automatically to active rework. After correction and retesting, it must return to Judge. This loop continues until 11/10 or until the user explicitly changes/cancels/overrides the mission.

## Universal 1/10–11/10 grading rubric

### 1/10 — Catastrophic failure — FAIL
The task is essentially not accomplished. Output is unusable, fundamentally incorrect, dangerous to the project, fabricated, corrupted, or unrelated to the request. A restart is normally required.

### 2/10 — Severe failure — FAIL
Only a small amount of relevant work exists. The core requirement is absent or broken and substantial replacement is required.

### 3/10 — Major failure — FAIL
The direction is recognizable, but major systems, requirements, evidence, or outputs are missing or wrong. Significant redesign or rebuilding is required.

### 4/10 — Weak / incomplete — FAIL
Some useful work exists, but major gaps, defects, or unmet acceptance criteria remain and would create substantial rework.

### 5/10 — Half-complete — FAIL
Core work is partially functional but clearly unfinished. Important integration, edge cases, tests, documentation, quality, or verification remain unresolved.

### 6/10 — Functional prototype — FAIL
The main concept works in a limited form but remains prototype quality: fragile, placeholder-heavy, incompletely tested, weakly integrated, or insufficiently evidenced.

### 7/10 — Good working draft — FAIL
Most major requirements are present and generally work, but noticeable defects, missing refinement, reliability work, testing, or polish remain.

### 8/10 — Strong implementation — FAIL
The task is substantially correct and useful, with limited weaknesses, but at least one meaningful requirement, edge case, validation step, integration issue, usability problem, or evidence gap remains.

### 9/10 — Excellent but not finished — FAIL
High-quality work with no major architectural failure, but identifiable issues such as minor bugs, inconsistencies, incomplete documentation, insufficient runtime evidence, or requirement mismatch prevent final acceptance.

### 10/10 — Fully meets ordinary specification — FAIL / FINAL REWORK REQUIRED
The task appears complete, correct, tested, documented, and professionally executed, but Judge has not yet established the additional confidence required for Universal Team / Potato Network OS final acceptance: exact user-outcome verification, appropriate regression protection, integration consistency, no material unresolved risk, and sufficient direct evidence.

A 10/10 result is extremely close, but it still returns for the final gap.

### 11/10 — JUDGE VERIFIED — PASS
The task fully satisfies the Mission Contract and exact user request. All mandatory requirements and acceptance criteria are met. Relevant tests and runtime verification pass. Artifacts are correct and usable. Integration with existing work is sound. Important edge cases are addressed. No known blocker, critical, high, or material unresolved defect remains. Security, privacy, canon, policy, and project rules are satisfied where applicable. Documentation and handoff are sufficient. Every completion claim is supported by evidence rather than assumption.

Only this grade permits transition to `COMPLETE` after Archivist closeout when applicable.

## Five mandatory 11/10 questions

Before awarding 11/10, Judge must answer **YES** to all five:

1. Did we build exactly what was requested?
2. Does it actually work rather than merely exist?
3. Do we have evidence proving that?
4. Did we avoid introducing meaningful new problems?
5. Would the user reasonably consider the task genuinely finished?

Any **NO**, **UNKNOWN**, **ASSUMED**, or **UNVERIFIED** answer prevents 11/10.

## Scoring integrity

Judge may not raise or lower a score arbitrarily. Every score must trace back to:

- Mission Contract;
- exact user request;
- explicit acceptance criteria;
- relevant project/canon/security rules;
- evidence;
- tests;
- runtime/device observations when applicable;
- known defects and unresolved risks.

Judge must not inflate a score because substantial effort was spent, because code exists, because a build launches, or because a previous agent claimed success.

## Repeated-failure escalation

If the same task repeatedly fails Judge, automatically activate **Capability Gap Hunter** and **Skills Trainer**.

They must determine whether the root cause is primarily:

- wrong agent or specialist;
- weak role instructions;
- model capability;
- missing tool;
- architecture/design problem;
- process/handoff weakness;
- missing or poor tests;
- missing evidence;
- stale/incorrect memory;
- dependency/blocker;
- another identified capability gap.

Then strengthen the team/process, add regression protection where practical, reassign the work if needed, and run the task again.

## Potato Network OS state-machine rule

```text
JUDGE_REVIEW
    |
    +-- score 1..10 --> REWORK_REQUIRED
    |                    |
    |                    v
    |             ASSIGN/FIX/REDO
    |                    |
    |                    v
    |                  RETEST
    |                    |
    |                    +----> JUDGE_REVIEW
    |
    +-- score 11 ------> JUDGE_VERIFIED
                         |
                         v
                      ARCHIVIST
                         |
                         v
                      COMPLETE
```

This standard is intended to be implemented as a hard workflow rule in Potato Network OS rather than only as prompt wording.