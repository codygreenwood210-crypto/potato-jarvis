# Teddy Game Asset Pack — Commercial Sale-Readiness Record

> Cross-project commercial QA record for the Teddy Game asset pack. This is not POTATO Jarvis application state.

## Current decision

The original uploaded source pack `Teddy_Game_ULTIMATE_Complete_Gothic_Halloween_Asset_Pack_v1.1.zip` is **NOT APPROVED FOR PAID SALE**.

This decision is project/commercial QA only. It does not change Teddy Game story canon.

## Evidence-backed positives

- ZIP integrity/CRC check passed.
- 1,172 PNG files opened successfully.
- 25 JSON files parsed successfully.
- 23 WAV files were valid mono 16-bit 44.1 kHz audio.
- The package contains an organized folder structure, manifest, licence, AI disclosure, provenance notes, art bible, import guide and listing draft.

## Blocking findings

Exact-hash analysis found 961 unique PNG hashes across 1,172 PNG files, meaning 211 redundant duplicate copies.

Key audited categories included:

- Weapons: 16 PNG files / 5 unique hashes.
- Buildings: 29 / 8.
- UI: 61 / 26.
- Story/dialogue: 16 / 3.
- Jester individual frames: 74 / 61.
- NPC individuals: 54 / 35.
- Standard enemies: 37 / 18.
- Elites: 13 / 7.
- Bosses: 10 / 7.

Representative visual review confirmed that some differently named weapons, buildings and UI elements are identical or near-template variants rather than genuinely distinct sellable assets.

## Marketing-photo status

The source pack's reviewed storefront/promotional images are:

- `itch_cover_630x500.png`
- `promo_board_01.png`
- `promo_board_02.png`
- `promo_board_03.png`
- `promo_board_04.png`

These images were shown to the user during review. They should be treated as **historical/review references, not approved storefront creatives**.

The promotional material overstates the audited pack. Examples include `OVER 10,000+ ASSETS` and category/variety claims that are not supported by the actual file counts or unique-content counts. The boards also show richer/more varied art than some of the corresponding production files.

Before a paid release, rebuild all store images using only real included production assets and only auditable quantity/variety claims.

## Other blockers

- Documentation contains v1.0/v1.1 version-name inconsistencies.
- Internal `BUILD_SUMMARY.txt` / `QA_REPORT.json` states that human visual review is required before paid publication; that review found real blockers.
- Engine/platform logos in marketing should be reviewed for trademark/logo-use risk; safer default wording is plain-text compatibility language unless logo use is confirmed.

## Required remediation before the original Teddy pack can be sold

1. Replace or honestly consolidate duplicated/mislabeled assets.
2. Ensure named categories contain genuinely distinct assets where the listing implies variety.
3. Rebuild every promotional image from actual included production files.
4. Replace unsupported numerical claims with audited counts.
5. Synchronize product/version naming across all documentation and listing material.
6. Review/remove engine logos or replace with plain-text compatibility wording where appropriate.
7. Rerun structural QA, representative human visual QA, Marketing/Trust review and final Judge sale gate.

## Separate remediated commercial candidate

Universal Team Memory later records a separate product candidate, `Potato_Pixel_Art_Studios_Friendly_Gothic_Halloween_Mega_Pack_v1.0.zip`, created from generic reusable Gothic/Halloween production assets while excluding Teddy/Jester hero art, character portraits, story-specific material, Teddy branding and the old misleading promotional boards. That separate candidate is recorded as structurally TEST_VERIFIED with 775 unique PNG visuals and 23 unique WAV files and zero exact duplicate PNG hashes, but it is a separate commercial product and does **not** reverse the failed-sale decision on the original Teddy v1.1 pack.

Do not claim the generic candidate as LIVE_VERIFIED, REVENUE_VERIFIED or PROFIT_VERIFIED without external evidence.

## Canonical continuity references

- Google Drive `Universal Team — Interaction & Knowledge Ledger`: `UTM-2026-010` for the original Teddy v1.1 failed-sale audit.
- Google Drive `Universal Team — Interaction & Knowledge Ledger`: `UTM-2026-011` for the separate generic commercial candidate.
- Google Drive `Universal Team — Handoff Ledger`: latest commercial handoff referencing these states.
- `UNIVERSAL_TEAM_TEDDY_GAME_HANDOFF.md` for Teddy Game story/canon continuity.

## Evidence state

- Original Teddy v1.1 package: SOURCE VERIFIED / TEST VERIFIED for integrity and counts; representative VISUAL REVIEW completed; **JUDGE NOT APPROVED FOR SALE**.
- Generic remediated product: TEST VERIFIED only as recorded in Universal Team Memory; not externally live/revenue/profit verified.
