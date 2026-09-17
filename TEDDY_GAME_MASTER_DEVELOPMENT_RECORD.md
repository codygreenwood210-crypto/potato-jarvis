# Teddy Game — Master Development Record

> Canonical continuity record for the current Teddy Game production state. This is a project handoff record, not POTATO Jarvis application state.

## Authority and current status

- Project: **Teddy Game**
- Genre: **2D side-scrolling action RPG**
- Primary target: **Android**
- Primary engine: **Godot 4.x stable**
- Primary language: **GDScript**
- Desktop builds remain useful for development/testing.
- Current source asset archive: `Teddy_Game_ULTIMATE_Complete_Gothic_Halloween_Asset_Pack_v1.1.zip`.
- Preserve the master ZIP unchanged.
- The user-supplied 134-section **TEDDY GAME — COMPLETE DEVELOPMENT ROADMAP** is the controlling production plan unless a newer explicit user decision or verified technical constraint supersedes part of it.
- The user-supplied story in the current production room is the controlling story canon.
- Completion standard: do **not** call the game complete until the exact playable build passes the user-required Judge **11/10** gate with evidence.

## Critical production reset — 2026-09-17

The previous native Android v0.2 prototype is **FAILED / SUPERSEDED as a production foundation**.

Historical evidence for that prototype included build output, passing unit tests, lint output and successful Android emulator launch/resume. That evidence remains valid only for those narrow claims.

The user then tested the app and reported that:

- the game looked terrible;
- the controls did not work;
- the result was nothing like the promised Teddy Game.

The supplied screenshot showed crude geometric/block environment art, oversized HUD/control overlays and an experience that did not use the supplied production asset pack at the promised quality level.

Therefore:

- `BUILD_VERIFIED` or launch success is **not** `PLAYABLE_VERIFIED`;
- an installable APK is not proof of a functional game;
- touch controls require runtime/input verification;
- visual fidelity to the actual asset/specification is part of acceptance;
- the failed native prototype must not be used as the visual/gameplay foundation of the replacement game;
- future Judge review must score the requested player experience, not merely technical startup.

## Core development philosophy

**BUILD SMALL -> PROVE IT WORKS -> EXPAND -> TEST -> POLISH**

Production loop:

**PLAN -> BUILD -> RUN -> TEST -> IDENTIFY PROBLEMS -> FIX -> VERIFY -> SAVE CHECKPOINT -> EXPAND**

Foundation quality outranks content volume. When choosing between new content and fixing broken movement/combat/save/controls, fix the core system first.

## Portable project requirement

Final project root: `TeddyGame/`

The complete project must be copyable to another compatible computer, open in the documented Godot version and run/export without reconstructing missing local machine files.

Required top-level documentation includes:

- `project.godot`
- `README.md`
- `CHANGELOG.md`
- `DEVELOPMENT_ROADMAP.md`
- `docs/`
- `assets/`
- `scenes/`
- `scripts/`
- `data/`
- `testing/`

Use Godot project-relative paths only. Never depend on mysterious machine-specific absolute paths.

## Engine and rendering rules

- Godot 4.x stable.
- Landscape Android target.
- Recommended internal starting resolution: **640 x 360**.
- Pixel-art-compatible rendering.
- Nearest-neighbour filtering.
- Disable unwanted smoothing/mipmaps where appropriate.
- No stretched/blurry sprites or half-pixel visual artifacts.
- Camera must respect level bounds and support boss-room locking.

## Input architecture

Named actions must separate gameplay from input device:

- `move_left`
- `move_right`
- `jump`
- `attack`
- `secondary_attack`
- `dash`
- `interact`
- `ability`
- `heal`
- `pause`
- `inventory`
- `map`

Keyboard, controller and touch must trigger these same actions.

Touch art is not enough: every touch control must be runtime-tested to prove it triggers the correct action.

## Collision architecture

Initial logical layers:

1. World
2. Player
3. Enemies
4. Player attacks
5. Enemy attacks
6. Items
7. Hazards
8. Interactables
9. NPCs
10. Triggers

Use reusable hitbox/hurtbox components instead of embedding damage logic everywhere.

## Player architecture

Create a real `Player.tscn` based on `CharacterBody2D`, with animation, collision, hurtbox, attack origin, ground/wall checks, camera target, interaction detector, audio and state machine.

Player state machine should cover at least:

- Idle
- Walk/Run
- Jump/Fall/Land
- Attack/Heavy/Special
- Dash
- Hurt
- Heal
- Interact
- Dead
- Respawn
- Victory

Optional later states include wall movement, ledge systems, swimming and other traversal features.

Movement requires configurable speed, acceleration, deceleration, gravity, jump velocity, variable jump height, air control and fall behavior.

## Canonical Teddy visual identity

Teddy remains one character with:

- vertically split golden/yellow and violet/purple body;
- central stitched seam;
- purple/Nightmare eye and purple-side horn;
- red stitched heart;
- one yellow leg and one purple leg;
- red/purple/gold jester clothing with bells/patch/diamond motifs;
- ornate double-headed axe/halberd.

Directional sprites must preserve anatomical continuity. Do not blindly mirror Teddy if it makes the golden/purple body sides switch identity incorrectly.

Production Teddy animation requires genuine multi-frame sequences rather than a static sprite sliding across the screen.

Priority animation coverage includes idle, walk, run, turn, jump rise/apex/fall/land, double jump, dash/dodge, attacks/combos/heavy/air/special, hurt/knockback/stun/death/respawn, interaction and story/emotion states.

## Story canon — immutable rules

- Teddy is **ONE person**.
- Nightmare magic is **not evil**.
- Nightmare magic is not a second personality.
- There is **no separate evil Shadow Teddy**.
- Teddy must never split into independent golden and purple beings.
- Heartland and Nightmare are both necessary.
- Heartland represents hope, love, courage, imagination, focus, protection and constructive power.
- Nightmare represents fear, anger, grief, painful memory, instinct, uncertainty, chaos and immense emotional power.
- The red stitched heart is Teddy's living bridge between both realms.
- Teddy was created from the remaining magic of two ancient guardians: one Heartland-linked and one Nightmare-linked.
- Being created for a purpose does not mean that purpose owns Teddy's life.
- The Unraveler was once Keeper of the Heart Lantern and became obsessed with eliminating painful emotions; his failure teaches the danger of confusing protection with control.
- The Unraveler now seeks permanent separation of Heartlands and Nightmare Realm.
- Teddy's existence is living evidence against that worldview.

Legacy files such as `dark_teddy`, `corrupted_teddy`, `shadow_teddy` and `giant_dark_teddy` must **not** be used as an evil second Teddy in main canon. Rename as unrelated/non-canon content or exclude them.

## Confirmed world and story progression

### Moonstitch Hollow / Halloween Festival

Friendly Gothic-Halloween village between Heartlands and Nightmare Realm. Important NPCs include Ghost Child, Witch Guide, Skeleton Blacksmith, Vampire Merchant and Mummy Healer. Heart Lantern contains intertwined gold and violet flames. Teddy approaches it, his stitched heart resonates, the Lantern cracks/explodes, Heart Shards scatter and The Unraveler recognizes Teddy.

### Whispering Forest

Nightmare energy destabilises the forest but is not proof of moral evil. Teddy battles the Pumpkin King and discovers he is trapped in unstable magic. Teddy uses Heartland stabilization plus Nightmare force together. Pumpkin King is freed and becomes an ally. First Heart Shard recovered.

### Forgotten Graveyard

Forgotten spirits and painful memories reveal that darkness/pain are not automatically corruption. Headless Knight distrusts Teddy's Nightmare power and tests him. Teddy learns not to fear the darkness inside himself or assume he fully understands it.

### Broken Castle

Teddy discovers murals/records showing the two ancient guardians and the truth of his origin. Anger and fear trigger a Chaos Surge. No separate being appears. Teddy learns that suppressing difficult emotions can worsen the chaos; control can mean giving chaos useful direction rather than making it vanish.

### Bone Caverns

Bone Dragon is not corrupted and tests Teddy's deliberate use of both kinds of magic. Teddy learns the Keeper/Unraveler history and the danger of forced emotional removal.

### Nightmare Collision

Moonstitch and Nightmare Realm overlap. Earlier allies return and the village reflects prior relationships. Ghost Child is overwhelmed by Nightmare energy. Teddy protects rather than treats the child as evil and helps Ghost Child regain control through their shared memories.

### The Last Stitch — current confirmed cutoff

The Unraveler opens the final rift and attacks Teddy's stitched heart because Teddy is a living connection between both realms. Teddy becomes unstable but never splits into separate beings. The Unraveler tells him he cannot control what he is. Teddy responds: **“Maybe not all the time.”** and **“But it's still me.”**

Teddy stops trying to silence Nightmare energy and gives it direction. Purple chaos becomes movement, strength, unpredictable attacks and deliberate destructive power. **Golden magic gives that power focus.**

**This is the current confirmed story cutoff.**

Any assistant-created continuation after this point — including a restored Heart Lantern ending, sparing/restoring the Keeper, rebuilt Moonstitch epilogue, a final Ghost Child “Both” scene, or post-credits material — is **PROVISIONAL / NON-CANON unless the user explicitly reconfirms it**.

## Core gameplay systems

Required production systems include:

- responsive side-scrolling movement;
- variable jumping and double jump;
- dash/dodge;
- melee and ranged combat architecture;
- one canonical primary weapon first, then optional additional weapons;
- Heartland abilities;
- Nightmare abilities;
- combined Heartland/Nightmare abilities;
- health/damage/invulnerability/knockback/death/respawn;
- reusable enemy AI state machine;
- elite variants;
- reusable multi-phase boss framework;
- NPC interaction;
- dialogue data;
- quest state machine;
- items/inventory/equipment;
- collectibles and memory fragments;
- chests/doors/switches/shrines/teleporters/other interactables;
- hazards;
- checkpoints;
- versioned save files and autosave;
- map/fast travel;
- main menu, pause, settings and accessibility;
- HUD and boss UI;
- audio manager, music, ambience and SFX;
- event bus/global game manager;
- debug tools disabled in production.

## First vertical slice — required proving ground

Target: **Haunted/Whispering Forest vertical slice**.

Must contain:

- Jester Teddy using real production art;
- movement;
- jump;
- combat;
- one weapon;
- dash or double jump;
- one normal enemy;
- one ranged/flying enemy;
- one elite;
- hazards;
- collectibles;
- one checkpoint;
- one NPC;
- one chest;
- one secret;
- HUD;
- music and SFX;
- working mobile controls;
- one boss;
- death/respawn;
- save/load;
- level completion.

### Vertical slice acceptance

Pass only when:

- game starts;
- player moves;
- player jumps;
- player attacks;
- player takes damage;
- player dies and respawns correctly;
- enemy AI functions;
- boss fight functions;
- checkpoint functions;
- save functions;
- reload/Continue functions;
- touch controls function in runtime;
- music/SFX function;
- HUD functions;
- a complete level playthrough has no blocker/game-breaking error.

## Development phase order

0. Preservation
1. Godot Foundation
2. Player
3. Player Polish
4. Combat
5. First Enemy
6. Level System
7. Checkpoint & Save
8. UI
9. Mobile Controls
10. First Boss
11. Vertical Slice
12. NPC / Dialogue / Quests
13. Inventory / Items / Weapons
14. Abilities / Progression
15. Additional Enemies / Elites
16. Remaining Core Worlds
17. Remaining Core Bosses
18. Complete Story Integration
19. Android Optimisation
20. Full QA
21. Replace Weak Placeholders
22. Commercial Polish
23. Android Release Candidate
24. Physical-Device Verification
25. Version 1.0 Release

The user's longer roadmap also covers optional additional biomes, achievements, bestiary, completion systems, accessibility, map/fast travel, balancing data, object pooling and future expansion architecture.

## Save system requirements

Versioned save format. Persist only useful state such as:

- world/level/checkpoint;
- upgrades/abilities/weapons;
- inventory/currency;
- boss victories;
- quest states;
- opened chests;
- collected fragments;
- settings;
- playtime.

Autosave on checkpoint, boss victory, major quest completion, area transition and important unlocks. Validate loaded values and handle missing/older/corrupt save data without crashing the game.

## Current asset-pack facts and production gaps

The v1.1 pack is broad, but production quality and story-specific coverage require audit.

High-priority missing or incomplete story-specific sets:

1. Heart Lantern full state/animation set.
2. Heart Shards.
3. The Unraveler / Original Keeper.
4. Ancient Heartland Guardian.
5. Ancient Nightmare Guardian.
6. Teddy full multi-frame directional animation coverage.
7. Teddy stitched-heart states.
8. Heartland/Nightmare/combined VFX.
9. Pumpkin King boss set plus friendly/stabilised versions.
10. Headless Knight production set.
11. Bone Dragon production set.
12. Ghost Child emotional/story set.
13. Halloween Festival Moonstitch.
14. Heart Lantern disaster Moonstitch.
15. Nightmare Collision Moonstitch.
16. Broken Castle origin chambers/murals/tapestry.
17. Bone Caverns story art.
18. The Last Stitch final-region environment.
19. Cutscene/story illustrations.
20. Expanded music.
21. Expanded SFX.
22. Compact commercial phone HUD.
23. Functional touch-control art/states.
24. Major-character portrait expression sets.
25. Final store/release graphics.

Current audio breadth is not enough for a polished full RPG; expand music, boss themes, ambience, player/enemy/boss SFX and UI feedback.

## Asset quality labels

Every visual asset entering production should be classified as:

- FINAL QUALITY
- GOOD ENOUGH FOR V1
- PLACEHOLDER
- REPLACE BEFORE RELEASE
- REJECTED
- UNUSED

Audio:

- FINAL
- TEMPORARY
- REPLACE
- UNUSED

The source ZIP itself stays preserved; cleanup happens in the development copy and production-approved subsets.

## Evidence ladder

Use explicit evidence states:

- PLANNED
- IN_PROGRESS
- IMPLEMENTED_UNVERIFIED
- SOURCE_VERIFIED
- TEST_VERIFIED
- BUILD_VERIFIED
- EMULATOR_VERIFIED
- DEVICE_VERIFIED
- JUDGE_VERIFIED
- BLOCKED / FAILED / SUPERSEDED as applicable

Never upgrade evidence because code merely exists.

## Bug severity

1. BLOCKER — game cannot progress
2. CRITICAL — crash/save corruption or equivalent destructive failure
3. HIGH — major system broken
4. MEDIUM — significant gameplay problem
5. LOW — cosmetic/minor issue

Fix in that order.

## Android/release requirements

Development APKs are useful for testing. Store release preparation requires current Android/Google Play rules to be rechecked against official sources at release time.

Release candidate must not be called ready until:

- main story is completable;
- no blocker bugs;
- no known save corruption;
- required bosses and abilities work;
- touch controls are tested;
- menus/settings work and settings persist;
- audio is balanced;
- credits/licensing are present and reviewed;
- app builds successfully;
- physical Android testing passes;
- performance is acceptable.

Physical-device final test should include fresh install, new save, complete playthrough, close/reopen/continue, app switching, pause/resume, repeated death/boss retries, save reloads, audio interruptions and display/performance checks where practical.

## Immediate authoritative next action

Do **not** resume from the failed native prototype.

Proceed in this order:

1. Preserve original v1.1 ZIP unchanged.
2. Audit Teddy animation frames and directional continuity.
3. Audit Forest tiles/backgrounds.
4. Audit first enemy candidates.
5. Audit Pumpkin King.
6. Audit Ghost Child.
7. Audit HUD/touch assets.
8. Create/fill missing Heart Lantern and Heart Shard assets.
9. Create the Godot 4.x project foundation.
10. Prove real Teddy movement, camera, collision and animation in a test room using actual production art.
11. Only after that foundation passes runtime testing, add combat and the first enemy.
12. Continue through the roadmap to the complete Forest vertical slice.
13. Run real gameplay regression and Android touch tests.
14. Judge the exact build against the user's requested experience.
15. If Judge is below 11/10, fix and retest rather than declaring completion.

## Commercial truth rule

The objective is not to prove the asset pack contains many files. The objective is to turn its useful material into a coherent, enjoyable, stable and commercially usable game.

Every decision should answer:

> Does this make Teddy Game more playable, understandable, reliable, enjoyable or commercially valuable?

If not, it is lower priority than work that does.
