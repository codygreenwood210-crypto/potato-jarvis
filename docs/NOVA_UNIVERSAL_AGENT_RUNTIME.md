# Nova Universal Team — Functional Agent Runtime

**Status:** IMPLEMENTED / BACKEND REGRESSION VERIFIED  
**Canonical branch:** `potato-v5.8-final`  
**Manager:** Nova  
**Certified primary agent seats:** 80  
**Candidate roles present but disabled from certified routing:** Bug Hunter, Proof

## Purpose

This runtime turns the Universal Team roster into invokable, routable specialist agents under Nova's control.

Nova remains the single outward manager identity. The 80 certified specialists are bounded agents with explicit:
- role;
- department;
- embedded specialties/aliases;
- routing keywords;
- specialist prompt;
- evidence discipline;
- authority boundary;
- mission-pod participation.

The runtime does **not** turn the specialists into uncontrolled autonomous processes. Tool execution, approvals, permission changes, and persistent mutations remain under Nova/application orchestration and the existing security gateway.

## Control architecture

```
User
  ↓
Nova
  ↓
Universal Team router
  ↓
Single Responsible Owner
  ↓
Mission pod (normally 1–8, hard API max 12)
  ↓
Specialist advisory outputs
  ↓
Nova synthesis
  ↓
Authorized plan/tool gateway when action is required
  ↓
Verification / QA / Judge as required
```

Judge is never automatically selected as an implementation owner.

## Runtime files

- `backend/universal_agents.py` — canonical 80-agent runtime registry, prompts, aliases, router, SRO logic
- `backend/main.py` — API integration, Nova manager synthesis, persistence and audit integration
- `backend/tests/test_universal_agents.py` — registry/router/authority invariants
- `backend/tests/test_main.py` — API and orchestration regression coverage

## Agent states

### Certified and routable
All 80 canonical primary operators from `docs/UNIVERSAL_TEAM_MASTER_ROSTER.md`.

### Candidate, not certified/routable
- Bug Hunter
- Proof

Candidate roles are intentionally represented separately. They cannot be selected through the certified Universal Team router until their admission gates are satisfied.

## APIs

### List persisted certified agents

`GET /v1/agents`

Returns the 80 active certified primary operator rows.

### Invoke one specialist

`POST /v1/agents`

Example body:

```json
{
  "role": "Backend",
  "task": "Review this API design for idempotency and partial-failure risk."
}
```

Embedded aliases and legacy generic API aliases resolve to a certified primary operator.

Examples:
- `coding` → Forge
- `research` → Scout
- `security` → Guard
- `memory` → Archivist
- `SRE` → DevOps
- `EnemyAI` → Combat
- `UX` → UI

These aliases do not create extra team seats.

### Full roster

`GET /v1/team/roster`

Returns:
- Nova manager metadata;
- all 80 certified agents;
- candidate roles;
- roster invariants.

### Route a mission without running specialists

`POST /v1/team/route`

Nova's router scores the mission against all 80 agents and returns a bounded mission pod and Single Responsible Owner.

### Run a Nova-controlled mission pod

`POST /v1/team/run`

Each selected specialist receives:
- the user request;
- its canonical role prompt;
- the mission SRO;
- Nova authority constraints.

Specialist calls do not receive executable tools.

Nova then synthesizes the advisory reports into one answer.

The older `POST /v1/agent/multi` endpoint remains compatible and uses the same Universal Team runtime.

## Authority model

A specialist may:
- analyze;
- critique;
- research conceptually from supplied/current context;
- identify evidence needs;
- recommend plans;
- surface risks;
- define completion conditions;
- disagree with another specialist.

A specialist may not independently:
- execute tools;
- approve actions;
- escalate permissions;
- change persistent state;
- claim tests ran without evidence;
- certify whole-team consensus;
- self-promote candidate roles;
- override Nova;
- override Judge;
- override the Protected Trust Core.

When real action is required, Nova routes it through the application's existing authorized execution/approval system.

## Routing

Automatic routing:
1. scores explicit role/name references;
2. scores role-specific keywords and specialties;
3. scores department relevance;
4. conditionally adds verification/security/currentness/continuity specialists where warranted;
5. excludes Judge from automatic implementation routing;
6. selects one non-Judge SRO.

Explicit routing can choose up to 12 certified roles.

Normal mission pods should remain small rather than invoking all 80 agents unnecessarily.

## Persistence and audit

Mission-pod runs reuse the existing:
- `agent_runs` persistence;
- trace IDs;
- audit events;
- session integration.

Audit events distinguish Nova-team execution:
- `nova_team_started`
- `nova_team_completed`
- `nova_team_failed`

## Legacy database migration

At database initialization:
- obsolete generic agent rows are removed from the roster;
- all 80 certified Universal Team rows are inserted/upserted;
- existing `vision` is updated into the canonical Vision role.

This prevents the database from reporting 80 certified roles plus stale generic duplicates.

## Verification

The Universal Team registry has regression tests for:
- exactly 80 certified agents;
- exact department counts;
- candidate exclusion;
- alias resolution;
- relevant automatic routing;
- explicit role routing;
- Judge independence from implementation SRO;
- Nova authority boundaries in every agent prompt;
- Nova not being counted as a specialist seat.

The integrated backend regression suite passed after the runtime integration on 2026-09-22.

Full application CI additionally covers the broader Android/build/runtime system; its result should be checked separately before making a whole-application release claim.

## Functional meaning

"Functional agent" here means the role is represented in executable runtime code and can be:
- discovered;
- invoked;
- routed;
- combined into a mission pod;
- given role-specific instructions;
- persisted in run history;
- audited;
- synthesized by Nova.

It does not mean a separate continuously running consciousness/process or a specialist with independent authority.

## Standing rule

**ONE NOVA. 80 CERTIFIED SPECIALISTS. SMALL MISSION PODS. ONE RESPONSIBLE OWNER. REAL EVIDENCE. CENTRALIZED AUTHORITY.**

**JARVIS, NEVER ULTRON.**
