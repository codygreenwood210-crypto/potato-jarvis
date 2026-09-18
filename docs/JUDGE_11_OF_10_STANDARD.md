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

## Mandatory pre-pass gates — effective 2026-09-18

Judge may not award PASS or JUDGE_VERIFIED unless **all** of the following gates have completed successfully. These are necessary conditions in addition to the existing 11/10 rule.

### Gate 1 — Final proofread / response-integrity review

Before Judge can pass a user-facing deliverable, the complete final response must be independently reread after all edits are finished.

The reviewer must verify that:
- the response is exactly what was intended to be sent to the user;
- every part of the user's request is answered;
- commands, paths, filenames, branch names, IDs, links, code and quoted text are literal and copy/paste-safe;
- no stale screenshot/output is being treated as current evidence;
- no file, test, commit, API result, runtime result, tool execution or artifact is claimed without supporting evidence;
- there are no contradictions, unsupported assumptions, invented results, placeholders or circular instructions;
- no change has been made after the review. Any post-review edit invalidates the gate and requires a fresh review.

Gate result must be recorded as `PROOFREAD_PASS` or `PROOFREAD_FAIL`. Anything other than `PROOFREAD_PASS` blocks Judge PASS.

### Gate 2 — Whole-Team Completion Check

Before Judge can pass a substantive task, Judge must ask the **entire certified Universal Team** whether the task is complete.

Operationally, the completion check is sent to every certified primary operator in the current canonical roster. Each primary operator is responsible for reviewing the task from its own discipline and all embedded specialties/aliases it represents.

Each reviewer returns exactly one state:
- `COMPLETE`
- `NOT_COMPLETE`
- `UNKNOWN`
- `NOT_APPLICABLE`

Judge may proceed only when:
- every certified primary operator has been asked;
- every applicable response is `COMPLETE`;
- no applicable response is `NOT_COMPLETE` or `UNKNOWN`;
- any `NOT_APPLICABLE` response includes a brief reason.

Silence, missing responses, assumed agreement, or a partial mission-pod poll does **not** satisfy this gate.

### Gate 3 — Rubric test

Judge must score the completed work against the current Universal 1/10–11/10 grading rubric using direct evidence.

A score below **10/10** automatically blocks PASS.

A score of **10/10** satisfies this minimum-rubric gate but remains **FAIL / FINAL REWORK_REQUIRED** under the standing Universal Team standard.

Only **11/10**, after Gates 1 and 2 also pass, may become `JUDGE_VERIFIED`.

### Mandatory order

For final acceptance, use this order:

`WORK -> TEST -> FINAL DRAFT -> PROOFREAD GATE -> WHOLE-TEAM COMPLETENESS CHECK -> JUDGE RUBRIC -> PASS/REWORK`

Judge is prohibited from scoring first and using the score to infer that the proofread or whole-team gates must have passed. Each gate requires its own evidence.

### Anti-self-certification rule

The person/agent that produced the final deliverable may not be the sole evidence source for any of these gates. Judge must rely on independent review and direct evidence where available.

