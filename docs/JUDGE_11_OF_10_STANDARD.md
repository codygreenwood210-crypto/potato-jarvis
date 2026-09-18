# Universal Team Judge — Exact User Pass Standard

**Effective:** 2026-09-19  
**Scope:** Universal Team and Potato Network OS Judge-controlled substantive tasks  
**Status:** CURRENT / USER-DIRECTED / SUPERSEDES PRIOR 11-ONLY PASS RULE

## Canonical pass rule

Judge may mark a task **PASS / JUDGE_VERIFIED** only when **all three mandatory gates below pass**.

A task that fails any one gate is **REWORK_REQUIRED**.

### Gate 1 — Final proofread / response-integrity check

Before Judge may pass a user-facing deliverable, the **complete final version** must be independently proofread **after all edits are finished**.

The proofread must confirm that the final response or deliverable:

- is exactly what was intended to be sent to the user;
- answers every part of the user's request;
- contains no accidental omissions, substitutions, contradictions, duplicated or circular instructions;
- preserves literal commands, paths, filenames, branch names, IDs, links, code and quoted text exactly where accuracy matters;
- is copy/paste-safe where the user is expected to copy it;
- distinguishes current evidence from stale screenshots, prior runs, fixtures, examples and assumptions;
- does not claim that files, tests, commits, API calls, runtime results, tools, builds, artifacts or external outcomes exist unless evidence supports the claim;
- contains no invented results, placeholders presented as facts, or unsupported completion claims.

The author/implementer may not be the sole reviewer for this gate.

The proofread result is one of:

- `PROOFREAD_PASS`
- `PROOFREAD_FAIL`

Any edit after `PROOFREAD_PASS` invalidates the gate and requires a fresh proofread of the new final version.

Judge cannot infer this gate from a high rubric score. It requires its own review evidence.

### Gate 2 — Whole-Team Completion Check

Before Judge may pass a substantive task, Judge must ask the **whole current certified Universal Team** whether the task is complete.

"Whole team" means **every certified primary operator in the current canonical roster at the time of review**. Each primary operator reviews the task from its own discipline and all embedded specialties/aliases it represents.

Each primary operator must return exactly one state:

- `COMPLETE`
- `NOT_COMPLETE`
- `UNKNOWN`
- `NOT_APPLICABLE`

Judge may pass this gate only when:

- every certified primary operator has been asked;
- every certified primary operator has responded;
- every applicable response is `COMPLETE`;
- no applicable response is `NOT_COMPLETE` or `UNKNOWN`;
- each `NOT_APPLICABLE` response has a brief reason;
- any material objection raised by a team member has been resolved and the affected reviewers have been asked again after the fix.

Silence, a missing response, assumed agreement, a partial mission-pod poll, or "nobody objected" does **not** satisfy the gate.

The completion-check record must include at minimum:

- roster version or roster snapshot used;
- number of certified primary operators;
- number asked;
- number responded;
- counts of `COMPLETE`, `NOT_COMPLETE`, `UNKNOWN`, and `NOT_APPLICABLE`;
- unresolved objections, if any.

Any change to the underlying work after the whole-team check invalidates the affected completion opinions and requires the relevant team review again.

### Gate 3 — Judge grading-rubric test

Judge must test the completed work against the current Universal **1/10–11/10 grading rubric** using direct evidence.

The pass threshold is now:

- **1/10 through 9/10 — FAIL / REWORK_REQUIRED**
- **10/10 — PASS / JUDGE_VERIFIED**
- **11/10 — PASS / JUDGE_VERIFIED / EXCEPTIONAL**

This means the work must score **at least 10/10**.

This 2026-09-19 user-directed rule **supersedes the prior rule that only 11/10 could pass**. Historical 11-only records remain historical evidence and must not be silently rewritten.

Judge may not award 10/10 or 11/10 based on effort, confidence, an agent's self-report, code merely existing, a build merely launching, or an impressive-looking report. The score must trace to the exact request, acceptance criteria, evidence, tests, runtime/device observations where relevant, known defects, and unresolved risks.

## Rubric

### 1/10 — Catastrophic failure — FAIL
The task is essentially not accomplished, unusable, fundamentally incorrect, fabricated, corrupted, unsafe to the project, or unrelated.

### 2/10 — Severe failure — FAIL
Only a small amount of relevant work exists. The core requirement is absent or broken and substantial replacement is required.

### 3/10 — Major failure — FAIL
The direction is recognizable, but major systems, requirements, evidence, or outputs are missing or wrong.

### 4/10 — Weak / incomplete — FAIL
Some useful work exists, but major gaps, defects, or unmet acceptance criteria remain.

### 5/10 — Half-complete — FAIL
Core work is partially functional but important integration, edge cases, testing, documentation, quality or verification remain unresolved.

### 6/10 — Functional prototype — FAIL
The main concept works in a limited form but remains prototype quality, fragile, placeholder-heavy, incompletely tested or insufficiently evidenced.

### 7/10 — Good working draft — FAIL
Most major requirements are present and generally work, but noticeable defects, missing refinement, reliability work, testing or polish remain.

### 8/10 — Strong implementation — FAIL
The task is substantially correct and useful, but at least one meaningful requirement, edge case, validation step, integration issue, usability problem or evidence gap remains.

### 9/10 — Excellent but not complete — FAIL
High-quality work with no major architectural failure, but identifiable defects, incomplete evidence, insufficient runtime verification, documentation gaps or request mismatch still remain.

### 10/10 — Complete to the requested standard — PASS
The task fully satisfies the user's request and Mission Contract. Mandatory requirements and acceptance criteria are met. Relevant tests and verification pass. Artifacts are usable. Integration is sound. No known material defect or blocker remains. Security, privacy, canon, policy and project rules are satisfied where applicable. Completion claims are supported by evidence. Gates 1 and 2 must also have passed.

### 11/10 — Exceptional completion — PASS
Everything required for 10/10 is satisfied, with additional demonstrable excellence beyond the ordinary requested standard, such as especially strong resilience, regression protection, polish, clarity, automation, or evidence quality. Judge must not inflate to 11/10 merely because the task passed.

## Mandatory order

Final acceptance uses this order:

`WORK -> TEST -> FINAL VERSION -> PROOFREAD GATE -> WHOLE-TEAM COMPLETION CHECK -> JUDGE RUBRIC -> PASS OR REWORK`

Judge may not reorder these steps by scoring first and assuming the other gates passed.

## Anti-self-certification rule

The person or agent that produced the work may not be the sole evidence source for final acceptance.

For any material factual claim, use the strongest appropriate direct evidence available:

- filesystem evidence for file existence;
- Git output for Git state;
- test-runner output for tests;
- runtime/device observation for runtime/device claims;
- provider output for provider/model inventory;
- direct source inspection for source claims.

Agent prose is supporting narrative, not authoritative execution evidence.

## Rework behavior

If any gate fails, Judge must return:

- exact gate that failed;
- rubric score if Gate 3 was reached;
- PASS/FAIL state;
- acceptance criteria passed;
- acceptance criteria missing or failed;
- evidence reviewed;
- defects and unresolved risks;
- exact corrections required;
- responsible specialist or owner;
- required retest/re-review;
- next state: `REWORK_REQUIRED`.

After correction:

- rerun affected tests;
- regenerate the final version if needed;
- rerun the proofread gate;
- rerun the whole-team completion check for any materially changed work;
- rerun the rubric.

## Five final questions

Before PASS, Judge must answer **YES** to all five:

1. Was the final version proofread after all edits and confirmed to be exactly what was intended to be sent?
2. Was the whole certified team asked whether the task is complete, with all applicable reviewers returning COMPLETE?
3. Did the work score at least 10/10 on the grading rubric?
4. Does direct evidence support the important completion claims?
5. Are there no known material unresolved defects, blockers or contradictions?

Any **NO**, **UNKNOWN**, **ASSUMED**, **UNVERIFIED**, or missing answer blocks PASS.

## State-machine rule

```text
WORK
  |
  v
TEST
  |
  v
FINAL_VERSION
  |
  v
PROOFREAD_GATE
  |-- FAIL --> REWORK_REQUIRED
  |
  v
WHOLE_TEAM_COMPLETION_CHECK
  |-- NOT_COMPLETE / UNKNOWN / MISSING --> REWORK_REQUIRED
  |
  v
JUDGE_RUBRIC
  |-- 1..9 --> REWORK_REQUIRED
  |-- 10..11 --> JUDGE_VERIFIED
                      |
                      v
                   ARCHIVIST
                      |
                      v
                   COMPLETE
```

## User authority

The user remains the final authority over goals and may explicitly change, cancel or override a mission or this standard. No team member may silently bypass these gates.

## Historical note

The 2026-09-17 standard required 11/10 for PASS. On 2026-09-19 the user explicitly refined Judge so that PASS requires:

1. final proofread correctness;
2. whole-team confirmation of completeness;
3. grading-rubric score of **at least 10/10**.

The newer rule is canonical.
