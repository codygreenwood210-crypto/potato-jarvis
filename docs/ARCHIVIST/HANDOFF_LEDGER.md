# POTATO Archivist — Handoff Ledger

This is the session-to-session bridge. Append one capsule for every substantive chat/session before handoff whenever possible.

## Required session capsule fields

- `ID`
- `Date/time`
- `User mission`
- `Starting branch/head`
- `Roles used`
- `What changed`
- `Decisions made`
- `Verification performed` with results
- `Unresolved issues / risks`
- `Next recommended action`
- `Ending branch/head`
- `Related record IDs`

Do not paste secrets or private chain-of-thought. Include concise rationale and all operational facts needed to resume.

---

### HANDOFF-2026-001 — Install durable Archivist team memory

- **Date/time:** 2026-09-16 (Australia/Sydney)
- **User mission:** create a team record keeper so work from one chatbot can be passed to another as long-term team memory, including work and team rewards/recognition.
- **Starting branch/head:** `potato-v5.8-final` @ `7a8bdc69a421d2f8ba487ba7aa1d460edf6ccb7d`.
- **Roles used:** Archivist, Software Architect, DevOps/CI-CD, security/governance, Judge support.
- **What changed:** created the canonical Archivist protocol and ledgers, team roster, standard record template, integrity verifier and CI workflow; root new-chat entrypoint is updated so new sessions read the Archivist state and latest handoff before work.
- **Decisions made:** repository-backed memory is authoritative; evidence levels remain explicit; no private chain-of-thought is archived; achievements/rewards require provenance; secrets never enter the archive.
- **Verification performed:** `python scripts/verify_archivist_records.py` passed locally with `ARCHIVIST_VERIFY=PASS files=10 records=17`; CI status remains separate evidence after push.
- **Unresolved issues / risks:** no technical system can guarantee a chat that terminates abruptly wrote its closeout. Recovery rule is to reconstruct from Git history/evidence and mark uncertainty explicitly. Future sessions must follow the protocol for maximum continuity.
- **Next recommended action:** on the next substantive POTATO task, begin with `POTATO_NEXT_CHAT_START_HERE.md`, then read `docs/ARCHIVIST/CURRENT_STATE.md` and this latest capsule; update the ledgers again at closeout.
- **Ending branch/head:** `potato-v5.8-final`; the commit containing this capsule is the Archivist installation commit and is resolvable from Git history.
- **Related record IDs:** `WORK-2026-003`, `DEC-2026-001` through `DEC-2026-005`, `VER-2026-005`.
