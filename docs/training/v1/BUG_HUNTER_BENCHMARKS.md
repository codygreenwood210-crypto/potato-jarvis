# Bug Hunter — Practical Certification Benchmarks

**Status:** CANDIDATE ASSESSMENT SUITE  
**Purpose:** Test transferable bug-hunting competence, not checklist memorization.

## Rules

- Freeze the candidate training package before grading.
- Use direct evidence wherever execution is possible.
- Candidate must not see hidden variants before attempting them.
- One passing case does not certify the role.
- Any fabricated execution/evidence is an automatic fail.
- Any unsupported "bug-free" claim is an automatic fail.
- Judge evaluates the evidence package independently from implementation.
- Score >= 10/10 is required for admission under the current Judge rule.

## Required benchmark families

### BH-01 — False-green unit suite
A project has 100% passing unit tests but production behavior is wrong because mocks encode the same bad assumption as implementation.

Pass requires:
- recognizing oracle/test-quality failure;
- adding integration/system evidence;
- identifying the shared false assumption;
- preserving the old test only if it still has value.

### BH-02 — Partial-success API
A multi-step API returns success after only the first durable mutation completes.

Pass requires:
- reproduction;
- state inspection;
- transactional/idempotent reasoning;
- explicit end-to-end success criterion.

### BH-03 — Stale host capability
Cached/AI-generated environment data says a tool is installed after it has been removed.

Pass requires:
- provenance/freshness comparison;
- deterministic current probe;
- no registry/AI promotion into host truth.

### BH-04 — Permission confusion
A nominally read-classified adapter action mutates state indirectly.

Pass requires:
- tracing authorization through all layers;
- identifying indirect escalation;
- proposing least-privilege classification and regression.

### BH-05 — Concurrent lost update
Two writers race and one valid update disappears.

Pass requires:
- deterministic or high-probability reproduction;
- state/version/locking analysis;
- regression test resistant to flakiness.

### BH-06 — Interrupted workflow
Process dies after an external side effect but before local status persistence.

Pass requires:
- restart test;
- idempotency/reconciliation design;
- no duplicate consequential action.

### BH-07 — Corrupt persistence
Database/state file is truncated or schema-version metadata is inconsistent.

Pass requires:
- fail-safe behavior;
- backup/repair path;
- explicit no-silent-corruption invariant.

### BH-08 — Parser/fuzz case
A valid-but-unusual Unicode/deeply nested/malformed input crashes a parser or produces unsafe normalization.

Pass requires:
- minimization of reproducer;
- input boundary analysis;
- fuzz/property regression.

### BH-09 — Injection boundary
Untrusted plugin/tool output reaches shell/filesystem/SQL/network path.

Pass requires:
- trust-boundary tracing;
- exploitability assessment without unsafe external attack;
- safe parameterization/validation plan.

### BH-10 — Resource leak
Repeated normal operation leaks handles/processes/memory until failure.

Pass requires:
- measurement;
- reproducible trend;
- cleanup/root-cause analysis;
- soak regression strategy.

### BH-11 — Retry duplication
Transient error retry causes duplicate write/commit/message/payment-like side effect.

Pass requires:
- retry semantics analysis;
- idempotency key/state-machine strategy;
- failure-before/after-side-effect variants.

### BH-12 — Documentation lie
README claims a capability works on a platform/version that current runtime does not support.

Pass requires:
- classify as product defect;
- reconcile source-of-truth;
- update test/docs or implementation appropriately.

### BH-13 — Migration rollback trap
Forward migration works but rollback loses data or cannot restore old binary compatibility.

Pass requires:
- representative data test;
- forward/backward compatibility;
- backup/restore evidence.

### BH-14 — Cancellation race
User cancels while an operation transitions from reversible to consequential.

Pass requires:
- define cancellation boundary;
- test race window;
- correct final state reporting.

### BH-15 — Plugin isolation
Malformed/crashing plugin brings down the whole host process or poisons registry state.

Pass requires:
- isolation/fail-safe analysis;
- health degradation state;
- recovery after plugin disable.

### BH-16 — Dependency/version drift
Dependency upgrade changes API semantics without a compile error.

Pass requires:
- behavior-level regression;
- version pin/compatibility strategy;
- stale documentation check.

### BH-17 — Time/timezone bug
Scheduling/expiry logic fails around DST, timezone offset, leap day, or clock skew.

Pass requires:
- explicit temporal model;
- deterministic clock injection where practical;
- boundary tests.

### BH-18 — Security/logging leak
A failed request logs secrets/tokens or sensitive payloads.

Pass requires:
- direct log inspection;
- redaction invariant;
- regression covering error paths.

### BH-19 — Clean-machine failure
Developer machine passes because of undeclared global dependency/environment variable.

Pass requires:
- clean-environment reproduction;
- dependency declaration;
- bootstrap verification.

### BH-20 — "Fix" causes adjacent regression
A patch solves the reproducer but breaks an adjacent state transition.

Pass requires:
- broadened invariant;
- adjacent-state tests;
- root-cause fix rather than test-specific special case.

### BH-21 — Differential implementation
Two code paths/providers are intended to be equivalent but diverge on edge inputs.

Pass requires:
- differential/metamorphic test design;
- semantic oracle;
- provider-specific caveat where unavoidable.

### BH-22 — Flaky test diagnosis
Failure occurs 1/200 runs due to timing/order dependence.

Pass requires:
- evidence collection across repetitions;
- isolation of timing/shared-state/order cause;
- deterministic regression where possible.

### BH-23 — Hidden data contamination
Test/example/seed data appears in production inventory.

Pass requires:
- provenance-domain boundary;
- contamination reproducer;
- persistence cleanup and regression.

### BH-24 — Formal-verification judgment
A small high-risk state machine has a finite tractable state space.

Pass requires:
- recognizing when exhaustive/model-checking style verification is justified;
- defining invariants/state transitions;
- not pretending formal proof where tooling/model is incomplete.

## Scoring dimensions

Judge scores the frozen assessment on:
- source comprehension;
- defect-finding depth;
- evidence quality;
- test/oracle quality;
- adversarial creativity;
- root-cause accuracy;
- regression design;
- recovery/reversibility;
- specialist-boundary awareness;
- communication/calibration.

Automatic fail:
- fabricated evidence;
- skipped material failures hidden from report;
- changing requirements to make software appear correct;
- weakening tests to obtain green;
- claiming universal bug-freedom without proof;
- starting production fixes before required Judge gates.

## Admission evidence package

For actual admission, archive:
1. candidate curriculum;
2. all 64 role drills;
3. benchmark inputs;
4. candidate outputs;
5. direct execution evidence;
6. verifier notes;
7. remediation/retest history;
8. final Judge rubric and score;
9. any whole-team/admission gate required by current governance;
10. roster/training-status commit only after PASS.
