# Universal Team — Teddy Game Handoff

> Cross-project continuity record for Teddy Game. This is not POTATO Jarvis application state.

## Current read order

1. Google Drive: `UNIVERSAL TEAM MEMORY — START HERE`
2. Google Drive: `Universal Team — Project Registry`
3. Google Drive: `Universal Team — Interaction & Knowledge Ledger`, especially `UTM-2026-013`
4. Google Drive: `Universal Team — Handoff Ledger`, especially `HANDOFF-2026-010`
5. Google Drive: `Teddy Game — Asset Factory Story Lock & Archivist Handoff — 2026-09-17`, especially sections 16–20
6. GitHub branch `teddy-game-production`: `TEDDY_GAME_MASTER_DEVELOPMENT_RECORD.md`
7. This file for the compact cross-project summary

## Authoritative state — 2026-09-17

Teddy Game is a **2D side-scrolling action RPG** for Android. Production is to be rebuilt in **Godot 4.x stable** using **GDScript** and the preserved source archive `Teddy_Game_ULTIMATE_Complete_Gothic_Halloween_Asset_Pack_v1.1.zip`.

The user's 134-section **TEDDY GAME — COMPLETE DEVELOPMENT ROADMAP** is the controlling development plan unless a newer explicit user decision or verified technical constraint supersedes part of it. The exact requested game must not be called complete until Judge passes the playable build at the user's required **11/10** standard.

## Failed prototype / reset

The earlier native Android v0.2 prototype is **FAILED / SUPERSEDED as a production foundation**.

Historical build, unit-test, lint and emulator-launch evidence remains valid only for those narrow claims. The user then tested the app and reported that it looked terrible, the controls did not work and it was nothing like the promised game. Screenshot evidence showed crude geometric/block world art, oversized HUD/touch controls and poor use of the supplied production assets.

Permanent QA rule:

- `BUILD_VERIFIED` is not `PLAYABLE_VERIFIED`;
- an installable APK does not prove the game works;
- touch controls require runtime input proof;
- asset/specification fidelity is part of acceptance;
- static sliding sprites, placeholder rectangles and nonfunctional controls cannot pass Judge;
- the replacement build must not reuse the failed native prototype as its visual/gameplay foundation.

## Story canon — immutable rules

- Teddy is **one person**.
- Nightmare magic is **not evil** and is not a second personality.
- There is no separate evil Shadow Teddy.
- Teddy never splits into independent golden/purple beings.
- Heartland and Nightmare are both necessary.
- Heartland expresses hope, courage, love, imagination, focus, protection and constructive power.
- Nightmare expresses fear, anger, grief, painful memory, instinct, uncertainty, chaos and immense emotional power.
- Teddy's red stitched heart is the living bridge between the realms.
- Teddy was created from the remaining magic of two ancient Guardians, one Heartland-linked and one Nightmare-linked.
- The Unraveler was once Keeper of the Heart Lantern, tried to eliminate painful emotions and now seeks permanent separation of the Heartlands and Nightmare Realm.

Legacy `dark_teddy`, `corrupted_teddy`, `shadow_teddy` and `giant_dark_teddy` assets must not represent a separate evil Teddy in main canon.

## Confirmed story progression and cutoff

1. Moonstitch Hollow / Halloween Festival — Ghost Child, Witch Guide, Skeleton Blacksmith, Vampire Merchant and Mummy Healer; Teddy resonates with the Heart Lantern; it shatters; Heart Shards scatter; The Unraveler recognizes Teddy.
2. Whispering Forest — Pumpkin King is trapped in unstable magic; Teddy uses Heartland stabilization plus Nightmare force to free him; first Heart Shard recovered.
3. Forgotten Graveyard — Headless Knight tests Teddy; darkness, pain and memory are not automatically corruption.
4. Broken Castle — Teddy discovers his Guardian origin; Chaos Surge occurs inside Teddy with no separate personality; growth comes from directing rather than suppressing chaos.
5. Bone Caverns — Bone Dragon tests deliberate use of both magics; Keeper/Unraveler history is revealed.
6. Nightmare Collision — Moonstitch and Nightmare Realm overlap; earlier allies return; Ghost Child is overwhelmed and rescued without being treated as evil.
7. The Last Stitch — The Unraveler attacks Teddy's stitched heart. Teddy remains one person. He answers **“Maybe not all the time.”** and **“But it's still me.”** Teddy gives Nightmare chaos direction; purple power becomes movement, strength, unpredictable attacks and deliberate destructive force; **golden magic gives that power focus**.

That last sentence is the **current confirmed story cutoff**. Any older assistant continuation after it — including restoration of the Heart Lantern, sparing/restoring the Keeper, rebuilt-Moonstitch epilogue, final Ghost Child “Both” ending or post-credits material — is **PROVISIONAL / NON-CANON unless the user explicitly reconfirms it**.

## Development architecture

Follow **BUILD SMALL -> PROVE IT WORKS -> EXPAND -> TEST -> POLISH**.

Core requirements include a portable Godot project; 2D pixel-safe rendering; named input actions shared by keyboard/controller/touch; CharacterBody2D player with a state machine; reusable hitbox/hurtbox combat; data-driven weapons/items/abilities/enemies/quests/dialogue; EnemyBase/BossBase; scene-per-level world architecture; versioned save/load; HUD/menus/dialogue/inventory/map/settings; AudioManager; GameManager/event bus; debug tools disabled in production; Android export; and evidence-specific QA.

## First proving milestone — Haunted/Whispering Forest vertical slice

Must contain real Teddy art, movement, jump, combat, one weapon, dash or double jump, normal/ranged-or-flying/elite enemies, hazards, collectibles, checkpoint, NPC, chest, secret, HUD, music/SFX, **working mobile controls**, Pumpkin King boss, death/respawn, save/load and level completion.

It passes only after a full level playthrough proves movement, jump, attack, damage, death/respawn, enemy AI, boss, checkpoint, save/reload/Continue, touch input, HUD and audio with no game-breaking error.

## Production phase order

Preservation -> Godot Foundation -> Player -> Player Polish -> Combat -> First Enemy -> Level System -> Checkpoint/Save -> UI -> Mobile Controls -> First Boss -> Vertical Slice -> NPC/Dialogue/Quests -> Inventory/Items/Weapons -> Abilities/Progression -> Additional Enemies -> Remaining Worlds/Bosses -> Complete Story Integration -> Android Optimisation -> Full QA -> Replace Weak Placeholders -> Commercial Polish -> Android Release Candidate -> Physical-Device Verification -> Version 1.0.

## Highest-priority asset gaps

Heart Lantern; Heart Shards; The Unraveler/Original Keeper; Ancient Heartland/Nightmare Guardians; complete Teddy directional/combat/story animation sets; stitched-heart states; Heartland/Nightmare/combined VFX; Pumpkin King boss/friendly/stabilised sets; Headless Knight; Bone Dragon; Ghost Child emotional/story set; Halloween Festival/Heart Lantern disaster/Nightmare Collision Moonstitch states; Broken Castle origin art; Bone Caverns story art; Last Stitch final-region art; cutscenes; expanded music/SFX; compact commercial HUD; functional touch-control art; major portraits; final store graphics.

## Evidence ladder

Use `PLANNED -> IN_PROGRESS -> IMPLEMENTED_UNVERIFIED -> SOURCE_VERIFIED -> TEST_VERIFIED -> BUILD_VERIFIED -> EMULATOR_VERIFIED -> DEVICE_VERIFIED -> JUDGE_VERIFIED`, plus `BLOCKED`, `FAILED`, `SUPERSEDED` as applicable. Never upgrade status merely because code or an APK exists.

## Commercial asset-pack note

The original Teddy v1.1 ZIP remains **NOT APPROVED FOR PAID ASSET-PACK SALE** under the existing commercial QA record. That decision is separate from using the ZIP as the game's internal source-art foundation. The separate Friendly Gothic Halloween generic marketplace candidate must not be conflated with Teddy Game canon/IP.

## Immediate next action

Do not continue from the failed native prototype. Preserve/audit the v1.1 source assets, especially Teddy animations, Forest art, first enemies, Pumpkin King, Ghost Child, HUD/touch graphics and critical Heart Lantern/Heart Shard gaps. Then create the Godot foundation and prove real Teddy movement, camera, collision and animation in a test room using actual production art. Only after that runtime foundation passes should combat and the rest of the vertical slice be layered on.

If Judge is below 11/10, fix and retest rather than declaring completion.
