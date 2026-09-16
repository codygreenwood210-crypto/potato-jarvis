# POTATO Archivist — Record Templates

Copy the appropriate template and replace placeholders. Preserve existing historical entries.

## Work entry

```markdown
### WORK-YYYY-NNN — <title>

- **Date:** <date>
- **Mission:** <user/project mission>
- **Roles:** <roles that materially contributed>
- **Start:** <branch/commit/state>
- **Work:** <externally meaningful actions>
- **Files/areas:** <paths/components>
- **Outcome:** <result>
- **Verification refs:** <VER IDs / commits / CI / device evidence>
- **Open items:** <remaining work or none>
- **End commit:** <commit or pending>
```

## Decision entry

```markdown
### DEC-YYYY-NNN — <title>

- **Date:** <date>
- **Status:** ACCEPTED | SUPERSEDED
- **Decision:** <decision>
- **Context:** <problem/background>
- **Rationale:** <concise rationale, not hidden chain-of-thought>
- **Consequences:** <tradeoffs/required follow-up>
- **Evidence/links:** <paths/commits/records>
- **Supersedes / Superseded by:** <optional>
```

## Verification entry

```markdown
### VER-YYYY-NNN — <title>

- **Date:** <date>
- **Scope:** <what was actually checked>
- **Evidence class:** SOURCE | TEST | BUILD | CI | EMULATOR | DEVICE | SECURITY | PROVIDER | USER_OBSERVED
- **Command/check:** <safe command or description>
- **Result:** <exact result, including failures>
- **State:** <verification state(s) from README>
- **Artifact/reference:** <safe evidence reference>
- **Notes/limitations:** <scope limitations>
```

## Achievement / recognition / reward entry

```markdown
### ACH-YYYY-NNN — <title>

- **Date:** <date>
- **Type:** Achievement | Recognition | Reward
- **Recipient(s):** <team/role/person as appropriate>
- **Title:** <short title>
- **Reason:** <why it was earned/received>
- **Evidence:** <safe provenance>
- **Notes:** <scope/limitations>
```

## Handoff capsule

```markdown
### HANDOFF-YYYY-NNN — <title>

- **Date/time:** <timestamp/timezone>
- **User mission:** <mission>
- **Starting branch/head:** <branch @ sha>
- **Roles used:** <roles>
- **What changed:** <summary and important paths>
- **Decisions made:** <DEC IDs or none>
- **Verification performed:** <VER IDs/results>
- **Unresolved issues / risks:** <items or none>
- **Next recommended action:** <single clear next path>
- **Ending branch/head:** <branch @ sha>
- **Related record IDs:** <IDs>
```
