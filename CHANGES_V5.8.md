# POTATO-JARVIS V5.8 changes

V5.8 is based on the verified V5.7 source and is focused on the requested downloadable working copy.

- Replaces the lime accent with a ChatGPT-inspired green/black Material 3 palette (`#10A37F`, `#19C37D`, near-black surfaces).
- Restores side-effect result schemas so real successful device and smart-home results validate instead of being converted into false failures.
- Removes ambient reuse of approved plan rows; privileged plan steps require the exact explicit one-shot approval capability.
- Separates automation DENY from approval-required decisions.
- Binds automation approvals to an action index and resumes the original waiting run after approval.
- Makes automation runs with continued action failures report failure truthfully.
- Adds V5.8 regression tests for these security and side-effect invariants.
- Adds CI emulator installation/launch/setup/UI smoke verification before final artifacts are published.
