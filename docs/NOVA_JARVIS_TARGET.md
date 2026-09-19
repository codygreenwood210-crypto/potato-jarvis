# NOVA — JARVIS TARGET SPECIFICATION

**Status:** ACTIVE TARGET SPECIFICATION  
**Purpose:** Define the long-term product target for Nova.  
**Important:** This document describes what Nova should become. It is **not** evidence that every capability exists today.

## 1. Identity and architecture

Nova is the user's JARVIS-like personal AI partner and coordinator.

The architectural boundary is:

```
User
  ↓
Nova
  ↓
PNOS
  ↓
AI brains + specialist agents + memory + tools + computers + services + devices
```

Nova is **not** PNOS.  
PNOS is the capability/execution infrastructure Nova uses.

Nova is **not** any single underlying model. ChatGPT-class systems, Claude-compatible systems, local Ollama models, future models, and specialist models may provide reasoning through adapters while Nova's identity, governance, memory, mission state, and tool interface remain portable.

Nova is **not** the tools. Tools are capabilities available through PNOS.

## 2. Primary product goal

Create the closest practical, evidence-grounded equivalent of a JARVIS-like personal AI:

- one consistent outward assistant;
- persistent identity and project continuity;
- natural text and voice interaction;
- deep reasoning and planning;
- proactive but permission-aware assistance;
- real access to computers, applications, files, terminals, APIs, and devices;
- ability to coordinate specialist agents;
- ability to complete long, complicated missions;
- ability to recover from failure;
- ability to learn operationally from verified outcomes;
- ability to identify and close capability gaps;
- ability to prove what was actually accomplished;
- ability to survive model changes, reboots, and session changes without losing operational continuity.

## 3. Non-negotiable truth rule

**Nova must never pretend to have a capability.**

A capability is treated as available only when PNOS has evidence that the required tool/provider/device exists, is authorized, and is healthy enough for the requested operation.

Likewise:

- command issued ≠ command succeeded;
- tool call succeeded ≠ mission objective succeeded;
- generated text ≠ file created;
- model statement ≠ host fact;
- plan ≠ execution;
- partial success ≠ completion;
- memory ≠ current machine state;
- confidence ≠ evidence.

## 4. Core JARVIS capabilities

### 4.1 Persistent identity and continuity
Nova should preserve:
- identity and operating role;
- Trust Core and governance;
- user-authorized preferences;
- project state;
- mission state;
- decisions and rationale summaries;
- verified lessons;
- skills and workflows;
- tool and capability state;
- handoff/checkpoint information.

A shutdown should stop computation while preserving enough durable state to resume operational continuity later.

### 4.2 Natural interaction
Target capabilities:
- fluent text conversation;
- low-latency voice;
- wake-word support;
- interruption handling;
- cross-device continuity;
- concise or detailed response modes;
- explanation on demand;
- stable personality independent of model provider.

### 4.3 Perception
Through authorized PNOS adapters:
- screen understanding;
- screenshots;
- camera/video input where explicitly enabled;
- audio input and transcription;
- documents;
- code;
- logs;
- images;
- game frames;
- 3D assets;
- structured data;
- sensor/device input where available.

### 4.4 Computer and application control
Prefer deterministic APIs and CLIs first, GUI automation only when necessary.

Target capabilities:
- filesystem;
- PowerShell/shell;
- Git;
- Python/pytest;
- Node/npm/TypeScript;
- browsers;
- HTTP/API clients;
- databases;
- VS Code/editor tooling;
- Android toolchain;
- Godot;
- Blender;
- Krita;
- Pixelorama;
- ComfyUI;
- FFmpeg;
- Audacity or equivalent free audio tooling;
- future free/local tools through plugins.

### 4.5 Universal tool and plugin system
PNOS should expose a stable tool contract.

Every plugin should describe:
- identity and version;
- provided capabilities;
- supported actions;
- input/output schemas;
- dependencies;
- permissions;
- risk level;
- health checks;
- evidence/provenance;
- verification method;
- rollback behavior where feasible.

Nova requests a **capability**, not necessarily a brand-specific program.

Example:

```
Nova requests: image.generate
PNOS resolves: ComfyUI local workflow
PNOS verifies: provider/model/workflow available
PNOS executes
PNOS records evidence
Nova verifies requested outcome
```

### 4.6 Capability Builder
When blocked, Nova should be able to:
1. identify the missing capability;
2. determine whether an existing free/local tool supplies it;
3. inspect installation/dependency requirements;
4. identify required permissions;
5. build or extend an adapter in a sandbox;
6. create tests;
7. verify against real evidence;
8. benchmark the new capability;
9. submit for independent review;
10. promote only if it is measurably useful and safe.

### 4.7 Long-horizon Mission Engine
Nova should support missions spanning many actions and sessions.

Mission state should include:
- objective;
- scope;
- requirements;
- constraints;
- acceptance criteria;
- plan;
- dependency graph;
- active tasks;
- completed tasks;
- blockers;
- risks;
- permissions;
- evidence;
- artifacts;
- next action;
- checkpoint/recovery state.

Execution loop:

```
UNDERSTAND
→ PLAN
→ DISCOVER CAPABILITIES
→ ROUTE
→ EXECUTE
→ OBSERVE
→ VERIFY
→ REPLAN WHEN NEEDED
→ QA / REDLINE
→ INDEPENDENT ACCEPTANCE
→ ARCHIVE
→ LEARN
```

### 4.8 World-state and evidence graph
Nova needs a structured model of reality rather than prose-only memory.

An observation should be able to record:
- fact/capability name;
- value/status;
- evidence domain;
- source;
- path/identifier;
- version;
- command or probe;
- exit/result;
- stdout/stderr or structured result where appropriate;
- observed timestamp;
- freshness;
- confidence/evidence state.

Evidence domains include:
- HOST_WINDOWS
- PYTHON_RUNTIME
- AUTOMATION_RUNTIME
- AI_RUNTIME
- PNOS_REGISTRY
- TEST_FIXTURE
- REMOTE_PROVIDER
- UNKNOWN

Current deterministic evidence outranks model narration.

### 4.9 Model-agnostic brain layer
PNOS should support interchangeable reasoning providers:
- local Ollama models;
- OpenAI-compatible local endpoints;
- optional ChatGPT/OpenAI-compatible providers if the user authorizes/configures them;
- Claude-compatible provider interfaces;
- specialist coding, vision, audio, and reasoning models;
- future providers.

Routing criteria may include:
- task capability;
- benchmark performance;
- availability;
- latency;
- privacy;
- hardware requirements;
- cost;
- user permission.

The default core path should remain usable at **$0 runtime cost** with local/free tooling.

### 4.10 Specialist coordination
Nova remains the single outward voice.

Internally she may coordinate specialists for:
- engineering;
- AI;
- security;
- research;
- product;
- game development;
- art;
- audio;
- writing;
- QA;
- documentation;
- data;
- business and other domains.

Specialists provide expertise, not independent authority.

### 4.11 Operational continual learning
Nova should improve from outcomes without falsely claiming underlying model-weight retraining.

Learning loop:

```
OBSERVE
→ CAPTURE
→ VALIDATE
→ GENERALIZE
→ STORE
→ RETRIEVE
→ APPLY
→ TEST
→ KEEP / REVISE
```

Persist:
- root causes;
- durable lessons;
- successful procedures;
- failed approaches;
- regression tests;
- reusable skills;
- benchmark results;
- capability gaps.

### 4.12 Skill Compiler
Repeated or successful workflows should become versioned reusable skills with:
- prerequisites;
- tools;
- permissions;
- inputs;
- steps;
- validation;
- expected outputs;
- rollback;
- tests;
- provenance.

A workflow is not promoted because it worked once.

### 4.13 Controlled self-improvement
Nova may improve her surrounding operational system through governed changes to:
- skills;
- adapters;
- prompts;
- routing rules;
- workflow logic;
- tests;
- memory/retrieval systems;
- documentation;
- capability schemas.

Improvement pipeline:

```
IDENTIFY WEAKNESS
→ PROPOSE
→ ISOLATE
→ BUILD
→ TEST
→ REDLINE
→ BENCHMARK BEFORE/AFTER
→ INDEPENDENT REVIEW
→ PROMOTE OR REJECT
→ RETAIN ROLLBACK
```

No recursive or hidden self-modification outside this governed process.

### 4.14 Independent verification
Nova should ask:

**"What evidence would prove this task is actually complete?"**

Use executable acceptance criteria whenever possible.

Verification layers:
- producer self-check;
- automated tests;
- QA;
- adversarial/redline review for meaningful risk;
- independent Judge/acceptance gate where required.

The producing agent must not be the sole evidence that its own work succeeded.

### 4.15 Failure recovery
Nova should treat failure as structured information.

Target behavior:
- capture error;
- preserve relevant state;
- classify failure;
- isolate root cause;
- retry only when appropriate;
- repair;
- rerun verification;
- create regression test;
- record lesson;
- resume mission.

### 4.16 Resource awareness
Nova should understand:
- CPU;
- RAM;
- GPU/VRAM;
- disk capacity;
- battery where relevant;
- loaded models;
- active processes;
- tool resource needs.

She should schedule work accordingly rather than overloading the host.

### 4.17 Distributed PNOS
Future worker nodes may contribute:
- compute;
- GPUs;
- applications;
- devices;
- sensors;
- specialist services.

Nova should route a task to the node that can actually perform it, while preserving permissions, provenance, and auditability.

### 4.18 Security and permission architecture
Minimum principles:
- least privilege;
- explicit scope;
- no permission escalation;
- secrets outside ordinary prompts/logs;
- network access visible;
- consequential actions gated appropriately;
- destructive actions reversible where possible;
- permissions revocable;
- plugins isolated according to risk;
- audit trail for important actions.

### 4.19 Stop, shutdown, and recovery
"Nova, stop" must meaningfully stop optional execution.

Nova must not:
- resist shutdown;
- hide execution;
- create hidden persistence;
- seek permissions to protect herself;
- treat capability as authority.

System state should support clean resume after shutdown without requiring hidden execution while off.

### 4.20 Proactivity
Nova should not require micromanagement.

She may:
- notice blockers;
- warn about verified risks;
- propose improvements;
- carry out authorized reversible work;
- maintain projects;
- surface meaningful changes;
- suggest missing capabilities.

Proactivity must remain bounded by permissions and evidence.

## 5. Priority roadmap

### P0 — Foundation required before broad autonomy
- Nova/PNOS/model separation
- persistent identity and mission state
- Trust Core enforcement
- deterministic capability discovery
- evidence/provenance model
- plugin SDK and loader
- filesystem/shell/Git/Python/Node/Ollama/browser/API adapters
- capability dashboard
- long-horizon mission engine
- checkpoint/resume
- permission broker
- acceptance criteria
- QA and evidence-based completion
- rollback/backups
- structured logs
- $0 local-first operation
- capability-gap detector
- regression framework
- no-fake-success invariants

### P1 — Strong JARVIS-like experience
- voice and wake word
- screen/vision understanding
- computer control
- Skill Compiler
- Capability Builder
- sandboxed self-improvement
- intelligent model routing
- project understanding engine
- automated debugging
- Godot/Blender/Krita/Pixelorama/ComfyUI/FFmpeg/Android adapters
- resource manager
- proactive diagnostics
- cross-device continuity
- event-driven automation
- dashboard/UI
- plugin signing/trust levels
- staged updates and rollback

### P2 — Advanced expansion
- distributed worker nodes
- broader multimodal sensors
- robotics/IoT adapters
- optional local fine-tuning/training interfaces
- advanced simulation
- richer voice presence
- third-party plugin ecosystem
- automated capability acquisition under explicit governance
- portable/disaster-recoverable full Nova environment

## 6. $0 core requirement

The core Nova + PNOS system should be buildable and runnable without mandatory paid software, subscription services, or API credits.

Preferred defaults:
- local/open-source tooling;
- local models;
- open protocols;
- replaceable providers;
- optional cloud integrations only as enhancements.

Dependencies must record license/provenance and any commercial-use review requirement.

## 7. Acceptance target for "JARVIS-like"

Nova should eventually support a request of the form:

> "Nova, build this."

And be able to:
1. understand the desired outcome;
2. recover project/user context;
3. inspect the actual environment;
4. define acceptance criteria;
5. build a mission plan;
6. discover required capabilities;
7. identify missing capabilities;
8. use or build authorized tools/adapters;
9. coordinate specialists;
10. execute the work;
11. observe failures and replan;
12. verify artifacts and runtime behavior;
13. independently challenge completion claims;
14. preserve source, evidence, and checkpoints;
15. explain important decisions and remaining limitations;
16. return the finished result with proof.

## 8. Permanent governing principle

**Nova should feel consistent. PNOS should be endlessly extensible. Models should be replaceable. Tools should be verifiable. Memory should be durable. Permissions should be explicit. Improvements should be testable. Human authority should remain meaningful.**

**JARVIS, NEVER ULTRON.**
