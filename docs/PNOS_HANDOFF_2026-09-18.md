# Potato Network OS — Recovery Handoff — 2026-09-18

## Purpose

Preserve the current PNOS state before the user may close or turn off ChatGPT, so a future session can resume without reconstructing this conversation.

## Project goal

PNOS is intended as a free/local-first multi-AI collaboration OS in which durable specialist agents can use interchangeable local models, collaborate, challenge one another, hand off work, use permissioned tools, persist memory/evidence, and loop through the Universal Team Judge until 11/10.

Preferred local stack: Windows, VS Code, Git, Python/FastAPI, SQLite/FTS, React/TypeScript/Vite, Ollama, optional terminal OpenCode, pytest/Vitest/Playwright. Avoid mandatory paid APIs/cloud services and unnecessary infrastructure. Cloud/paid fallback is only allowed if the user explicitly enables it.

## Important environment history

- NVIDIA GeForce GTX 1070 with 8 GB VRAM.
- Ollama originally failed with a CUDA/PTX unsupported-toolchain error.
- A compatible NVIDIA driver update resolved that issue.
- `qwen2.5-coder:3b` subsequently ran locally and became the practical OpenCode model.
- Larger local models may fail or be too resource-heavy; PNOS is designed to preserve agent identity while falling back through smaller compatible models.

## Earlier PNOS milestone history

Earlier interaction evidence reported progress through resource-aware model routing, multi-agent collaboration, persistent memory/Archivist, Tool Gateway/permissions, and developer-tool integration. Those historical milestone reports should not be silently discarded, but any claim that matters to current recovery must be rechecked against the actual repository when needed.

The current failure is specifically the **Universal Capability Discovery / real-host discovery** milestone.

## What went wrong

The workflow conflated three different environments:

1. ChatGPT's own reasoning/tool environment.
2. OpenCode / the local LLM runtime.
3. The user's actual Windows host, filesystem, Git repository, and installed tools.

OpenCode/qwen2.5-coder:3b repeatedly generated narrative reports and plans that looked like execution evidence. Examples included fixture-like Linux system data, fake-looking model inventories, claims that all tools were installed, Unix-only commands such as `find | grep`, invented endpoints, and claims that a verifier file or evidence artifact had been created when direct Windows inspection did not confirm it.

Large multi-phase recovery prompts made the small model less reliable. The correct recovery strategy is short, bounded, deterministic tasks plus direct host verification.

## Direct Windows evidence

The user ran real Windows PowerShell and established:

- Working directory: `C:\Users\codyg\OneDrive\Desktop\PotatoNetworkOS`
- `Get-ChildItem .\scripts -Force`: the `scripts` directory existed but was empty.
- `Get-ChildItem .\artifacts -Force`: only `development-mission.png` and `pnos-browser.png` were present.
- The expected real-host evidence JSON did not exist.

Therefore earlier claims that `verify_real_windows_host.ps1` or the JSON host-evidence artifact had been created/run are not accepted.

A screenshot containing old text such as “VERIFICATION COMPLETE” is not proof of a new run unless its temporal provenance is established.

## Current Judge state

**Universal Capability Discovery / real-host verification: 3/10 — FAIL / REWORK_REQUIRED.**

Passed:
- environment-conflation root cause identified;
- real project directory established;
- direct filesystem inspection performed;
- unsupported host-evidence claims rejected.

Still unknown/unverified:
- actual Git branch and repository state;
- which OpenCode changes really exist;
- whether fake fixtures remain in production code;
- real host-discovery implementation;
- Ollama and VS Code extension discovery through PNOS production code;
- persistence/API cross-check;
- regression tests;
- security/permission behavior;
- valid commits for this milestone.

## Exact next action

Do not let OpenCode edit more files yet.

Open a separate real Windows PowerShell/Windows Terminal and run:

```powershell
cd "C:\Users\codyg\OneDrive\Desktop\PotatoNetworkOS"
git status
git branch --show-current
git log --oneline -10
git diff --stat
git diff
```

Use that direct Git evidence to decide what should be kept, fixed, or reverted before resuming host-discovery implementation.

## Permanent lesson

**AI coding-agent narration is not host execution evidence.**

For any local/remote agent workflow, preserve evidence-domain boundaries. A host-machine claim must be supported by host-machine evidence. Treat agent prose as a proposal until verified.

## Proof role correction

A new role called **Proof — Final Response Integrity Reviewer** had been written into the Universal Team records as 11/10 certified. Whole-chat review determined that admission was not supported by a genuinely independent practical assessment, and the role did not prevent the very failure pattern it was intended to catch.

Current canonical state:
- Proof role specification may be retained as a useful candidate.
- Proof status: **REWORK_REQUIRED / candidate**.
- Proof is **not** an admitted primary operator.
- Active Universal Team V2 primary-operator count remains **80**.
- Future admission requires predefined practical cases, direct evidence, independent Judge review, and 11/10.

## Matching continuity records

Google Drive:
- `HANDOFF-2026-013`
- `UTM-2026-016`
- `LESSON-2026-014`
- PNOS Project Registry entry
- START HERE PNOS continuity correction
- Master Roster Proof correction

GitHub:
- `docs/UNIVERSAL_TEAM_MEMORY.md`
- `docs/UNIVERSAL_TEAM_MASTER_ROSTER.md`
- this file

## Evidence state

- Windows path/filesystem findings: **USER_OBSERVED**
- This handoff and continuity update: **WRITE VERIFIED** once committed and read back
- PNOS capability-discovery milestone: **REWORK_REQUIRED**
- No new runtime/test/build/host-discovery success is claimed by this handoff.
