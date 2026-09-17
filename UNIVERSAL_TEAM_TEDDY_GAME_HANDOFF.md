# Universal Team — Teddy Game Handoff

> Temporary cross-project continuity record until Teddy Game has a dedicated repository. Do not treat this file as POTATO Jarvis application state.

## Read first

1. Google Drive: `UNIVERSAL TEAM MEMORY — START HERE`
2. Google Drive: `Universal Team — Project Registry`
3. Google Drive: `Universal Team — Interaction & Knowledge Ledger`, especially `UTM-2026-013`
4. Google Drive: `Universal Team — Handoff Ledger`, especially `HANDOFF-2026-010`
5. Google Drive: `Teddy Game — Asset Factory Story Lock & Archivist Handoff — 2026-09-17`, especially sections 16–20
6. This branch: `TEDDY_GAME_MASTER_DEVELOPMENT_RECORD.md`
7. This file for the compact continuity summary

## Current authoritative project state

- Teddy Game is a **2D side-scrolling action RPG** for Android.
- Production engine is **Godot 4.x stable** with **GDScript**.
- Desktop remains a development/testing target.
- Preserve `Teddy_Game_ULTIMATE_Complete_Gothic_Halloween_Asset_Pack_v1.1.zip` unchanged as the source archive.
- The user's 134-section **TEDDY GAME — COMPLETE DEVELOPMENT ROADMAP** is the controlling development plan unless a newer explicit user decision or verified technical constraint supersedes part of it.
- Do not call the game complete until the exact playable build passes the user-required Judge **11/10** gate with evidence.

## Critical production reset — 2026-09-17

The earlier native Android v0.2 prototype is **FAILED / SUPERSEDED as a production foundation**.

Historical evidence showed only that an APK could build, pass its recorded unit tests/lint checks and launch in an emulator. The user then tested the app and reported that it looked terrible, controls did not work and it was nothing like the promised game. The supplied device screenshot showed crude block/rectangle world art, oversized HUD/touch controls and weak visual fidelity to the supplied asset pack.

Permanent acceptance rule:

- build/install/launch success != playable game;
- touch controls require runtime/input proof;
- the actual supplied production art must drive the game visuals unless the user explicitly authorizes placeholders;
- static sliding sprites, placeholder rectangles, giant UI overlays and nonfunctional touch buttons cannot pass the vertical-slice gate;
- Judge must evaluate the requested player experience, not merely application startup.

## Canon lock — Teddy is one person

- Teddy is **ONE person**.
- Nightmare magic is **not evil** and is not another personality.
- There is no separate evil Shadow Teddy.
- Teddy must never split into separate golden/purple beings.
- Heartland and Nightmare are both necessary.
- Heartland expresses hope, courage, love, imagination, protection, focus and constructive power.
- Nightmare expresses fear, anger, grief, painful memory, instinct, uncertainty, chaos and immense emotional power.
- Teddy's red stitched heart is the living bridge between the realms.
- Teddy was created from the remaining magic of two ancient Guardians, one Heartland-linked and one Nightmare-linked.
- The Unraveler was once Keeper of the Heart Lantern and became obsessed with eliminating painful emotions; he now seeks permanent separation of the realms.

Legacy assets named `dark_teddy`, `corrupted_teddy`, `shadow_teddy` or `giant_dark_teddy` must not represent a separate evil Teddy in the main story. Exclude them, rename them as unrelated content, or keep them explicitly non-canon.

## Confirmed story progression

1. **Moonstitch Hollow / Halloween Festival** — Ghost Child, Witch Guide, Skeleton Blacksmith, Vampire Merchant and Mummy Healer are introduced. Teddy's stitched heart responds to the Heart Lantern. The Lantern cracks/explodes, Heart Shards scatter and The Unraveler recognizes Teddy.
2. **Whispering Forest** — Pumpkin King is trapped in unstable magic rather than morally evil. Teddy combines Heartland stabilization with Nightmare force, frees him and gains the first Heart Shard.
3. **Forgotten Graveyard** — Headless Knight tests Teddy. Pain/darkness/memory are not automatically corruption.
4. **Broken Castle** — Teddy discovers the two ancient Guardians and his origin. His anger/fear trigger a Chaos Surge. No separate being appears; Teddy learns to direct rather than suppress chaos.
5. **Bone Caverns** — Bone Dragon is not corrupted and tests deliberate use of both magics. The Keeper/Unraveler history is revealed.
6. **Nightmare Collision** — Moonstitch and Nightmare Realm overlap. Earlier allies return. Ghost Child is overwhelmed and rescued through protection, memory and stabilization rather than being treated as evil.
7. **The Last Stitch — confirmed text cutoff** — The Unraveler attacks Teddy's stitched heart. Teddy becomes unstable but remains one person. Teddy answers, **“Maybe not all the time.”** and **“But it's still me.”** He gives Nightmare chaos direction; purple energy becomes movement, strength, unpredictable attacks and deliberate destructive force; **golden magic gives that power focus**.

That is the current confirmed story cutoff. Any older assistant-created continuation after that point — including restoration of the Heart Lantern, sparing/restoring the Keeper, a rebuilt-Moonstitch epilogue, a final Ghost Child “Both” ending scene or post-credits sequel material — is **PROVISIONAL / NON-CANON unless the user explicitly reconfirms it**.

## Development architecture

Follow **BUILD SMALL -> PROVE IT WORKS -> EXPAND -> TEST -> POLISH**.

Core architecture includes:

- portable Godot project with project-relative paths;
- pixel-safe 2D rendering around a 640x360 starting internal resolution;
- named input actions shared by keyboard/controller/touch;
- CharacterBody2D player and state machine;
- separate reusable hitbox/hurtbox damage architecture;
- data-driven weapons/items/abilities/enemies/quests/dialogue;
- EnemyBase and BossBase frameworks;
- scene-per-level world structure;
- versioned save/load and autosave;
- GameManager/event bus;
- reusable HUD/menu/dialogue/quest/map/inventory/settings UI;
- AudioManager and region/boss music/SFX;
- debug tools disabled in production;
- Android export plus device QA.

## First proving milestone — Haunted/Whispering Forest vertical slice

Must include real Teddy art, movement, jump, combat, one weapon, dash or double jump, one normal enemy, one ranged/flying enemy, one elite, hazards, collectibles, checkpoint, one NPC, one chest, one secret, HUD, music/SFX, working mobile controls, Pumpkin King boss, death/respawn, save/load and level completion.

It passes only when the game starts; movement/jump/attack work; player takes damage/dies/respawns; enemy AI works; boss works; checkpoint works; save/reload works; touch controls actually work in runtime; audio/HUD work; and a full level playthrough has no game-breaking error.

## Production phase order

Preservation -> Godot Foundation -> Player -> Player Polish -> Combat -> First Enemy -> Level System -> Checkpoint/Save -> UI -> Mobile Controls -> First Boss -> Vertical Slice -> NPC/Dialogue/Quests -> Inventory/Items/Weapons -> Abilities/Progression -> Additional Enemies -> Remaining Worlds/Bosses -> Complete Story Integration -> Android Optimisation -> Full QA -> Replace Weak Placeholders -> Commercial Polish -> Android Release Candidate -> Physical-Device Verification -> Version 1.0.

## Highest-priority asset gaps

1. Heart Lantern full state set
2. Heart Shards
3. The Unraveler / Original Keeper
4. Ancient Heartland Guardian
5. Ancient Nightmare Guardian
6. Teddy full multi-frame directional/combat/story animation coverage
7. Teddy stitched-heart states
8. Heartland/Nightmare/combined VFX
9. Pumpkin King boss + friendly/stabilised forms
10. Headless Knight production set
11. Bone Dragon production set
12. Ghost Child emotional/story set
13. Halloween Festival Moonstitch
14. Heart Lantern disaster Moonstitch
15. Nightmare Collision Moonstitch
16. Broken Castle origin chambers/murals/tapestry
17. Bone Caverns story art
18. The Last Stitch final-region environment
19. Story/cutscene illustrations
20. Expanded music/SFX
21. Compact commercial HUD
22. Functional touch-control art/states
23. Major-character portrait sets
24. Final store/release graphics

## Evidence ladder

Use: `PLANNED -> IN_PROGRESS -> IMPLEMENTED_UNVERIFIED -> SOURCE_VERIFIED -> TEST_VERIFIED -> BUILD_VERIFIED -> EMULATOR_VERIFIED -> DEVICE_VERIFIED -> JUDGE_VERIFIED`, plus `BLOCKED`, `FAILED` and `SUPERSEDED` where appropriate.

Never upgrade status merely because code or an APK exists.

## Commercial asset-pack note

The original Teddy v1.1 ZIP remains **NOT APPROVED FOR PAID ASSET-PACK SALE** under the prior commercial QA record. That sale-readiness result is separate from using the archive as the internal visual/audio foundation of the game. A generic Friendly Gothic Halloween marketplace pack exists as a separate commercial candidate and must not be conflated with Teddy Game canon/IP.

## Immediate next action

Do not continue from the failed native prototype. Preserve the source ZIP, audit Teddy/Forest/enemies/Pumpkin King/Ghost Child/HUD-touch assets, fill the Heart Lantern/Heart Shard critical gaps, then create the Godot project foundation and prove real Teddy movement, collision, camera and animation in a test room using actual production art. Only after that runtime foundation passes should combat and content expansion continue.

Judge completion remains 11/10: if the exact build is below the bar, fix and retest rather than declaring completion.
