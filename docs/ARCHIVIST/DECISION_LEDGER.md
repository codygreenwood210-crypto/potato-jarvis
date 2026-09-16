# POTATO Archivist — Decision Ledger

Durable decisions live here so future chats know not only what POTATO does, but why the team chose the rule.

## Record format

Each decision should include: `ID`, `Date`, `Status`, `Decision`, `Context`, `Rationale`, `Consequences`, `Evidence/links`, and any `Supersedes/Superseded by` relation.

---

### DEC-2026-001 — Repository-backed memory is authoritative

- **Date:** 2026-09-16
- **Status:** ACCEPTED
- **Decision:** use version-controlled repository records as POTATO's durable team memory rather than relying on one chatbot's conversational memory.
- **Context:** separate chats do not share every internal detail or transient tool state reliably enough for an auditable engineering program.
- **Rationale:** repository records are inspectable, versioned, linkable to commits/evidence and available to future sessions.
- **Consequences:** substantive sessions must update Archivist records before handoff; chat memory is supplementary only.
- **Evidence/links:** `POTATO_NEXT_CHAT_START_HERE.md`, `docs/ARCHIVIST/README.md`.

### DEC-2026-002 — Preserve evidence levels; Archivist cannot self-verify

- **Date:** 2026-09-16
- **Status:** ACCEPTED
- **Decision:** the Archivist records verification status but does not replace Judge or promote claims beyond evidence.
- **Context:** documentation can otherwise turn an unverified claim into apparent fact through repetition.
- **Rationale:** separating source/test/build/CI/emulator/device/Judge evidence preserves the project's existing truthfulness standard.
- **Consequences:** every significant claim should identify evidence class and strongest verified state.
- **Evidence/links:** `docs/ARCHIVIST/README.md`, canonical V5.8 handoff.

### DEC-2026-003 — Record externally meaningful work, not private chain-of-thought

- **Date:** 2026-09-16
- **Status:** ACCEPTED
- **Decision:** store mission, actions, changed files, decisions, concise rationale, commands/results, evidence and next steps; do not attempt to persist private chain-of-thought or hidden model state.
- **Context:** continuity needs reconstructable engineering facts, not inaccessible internal reasoning.
- **Rationale:** this is safer, more portable and more useful to the next engineer/chat.
- **Consequences:** session capsules must be operationally complete even though they are not transcripts of hidden reasoning.
- **Evidence/links:** `docs/ARCHIVIST/README.md`, `RECORD_TEMPLATE.md`.

### DEC-2026-004 — Rewards and achievements require provenance

- **Date:** 2026-09-16
- **Status:** ACCEPTED
- **Decision:** never invent a reward or award. Record achievements/recognition only with evidence; distinguish formal rewards from internal milestone recognition.
- **Context:** the user asked for team work and rewards to persist across chats.
- **Rationale:** a durable ledger must remain truthful and auditable.
- **Consequences:** `ACHIEVEMENTS_AND_REWARDS.md` includes evidence type and provenance for each entry.
- **Evidence/links:** `docs/ARCHIVIST/ACHIEVEMENTS_AND_REWARDS.md`.

### DEC-2026-005 — Secrets never enter the memory archive

- **Date:** 2026-09-16
- **Status:** ACCEPTED
- **Decision:** Archivist records references to secret-dependent verification, never secret values.
- **Context:** durable memory increases exposure if sensitive values are copied into records.
- **Rationale:** continuity must preserve POTATO's backend-only provider credentials and existing security model.
- **Consequences:** record only safe diagnostics such as “credential loaded” or provider HTTP result, with redacted evidence references.
- **Evidence/links:** canonical V5.8 handoff security/secrets policy and `docs/ARCHIVIST/README.md`.
