# Teddy Game — Master Asset Requirements & Production Reset

> Archivist record synced 2026-09-17. This is a Teddy Game production record, not POTATO Jarvis application state.

## Authority

This file preserves the current asset-production requirements, canon safety rules, failed-prototype lessons, and immediate production gate for Teddy Game.

Controlling sources, in priority order:

1. The user's explicit current instructions.
2. The user-supplied **TEDDY GAME — COMPLETE DEVELOPMENT ROADMAP** (134 sections), except where a newer explicit user decision or verified technical constraint supersedes a particular item.
3. The user-supplied Teddy Game story, with the current confirmed cutoff documented below.
4. Source/runtime evidence from the project and asset pack.

The game must not be called complete until the exact requested playable build passes the user's Judge **11/10** gate with real evidence.

---

## Current production state

The previous native Android v0.2 prototype is **FAILED / SUPERSEDED as a production foundation**.

The earlier APK/build/unit/lint/emulator-launch evidence remains valid only for the narrow facts it actually proved. Direct user device testing showed that the result looked unacceptable, the controls did not work, and the game did not resemble the promised Teddy Game. The screenshot showed crude geometric/block environment art, oversized HUD/touch overlays, and poor use of the supplied production assets.

Permanent QA conclusions:

- `BUILD_VERIFIED` is not `PLAYABLE_VERIFIED`.
- An APK that installs/launches is not proof that the game works.
- Touch controls require real runtime input verification.
- Visual/specification fidelity is part of acceptance.
- Static sprites sliding across the screen do not satisfy animation requirements.
- Placeholder rectangles/geometric environments cannot replace production art where production art exists.
- The failed native prototype must not be reused as the visual/gameplay foundation of the replacement game.
- Judge must evaluate the requested player experience, not startup alone.

Current production status is therefore reset to **early pre-production / asset audit / Godot foundation preparation**.

---

## Core platform and architecture lock

- Engine: **Godot 4.x stable**.
- Primary language: **GDScript**.
- Primary target: **Android**.
- Desktop remains supported for development/test builds.
- Start from a portable project root `TeddyGame/`.
- Preserve `Teddy_Game_ULTIMATE_Complete_Gothic_Halloween_Asset_Pack_v1.1.zip` unchanged.
- Use project-relative paths only.
- Development philosophy: **BUILD SMALL -> PROVE IT WORKS -> EXPAND -> TEST -> POLISH**.
- Execution loop: **PLAN -> BUILD -> RUN -> TEST -> IDENTIFY PROBLEMS -> FIX -> VERIFY -> SAVE CHECKPOINT -> EXPAND**.

The first proving milestone remains the Haunted/Whispering Forest vertical slice with real Teddy art, movement, jump, combat, one weapon, traversal ability, normal/ranged-or-flying/elite enemies, hazards, collectibles, checkpoint, NPC, chest, secret, HUD, music/SFX, working mobile controls, Pumpkin King boss, death/respawn, save/load/Continue, and level completion.

---

## Story canon lock

Immutable rules:

- Teddy is **ONE person**.
- Nightmare magic is **not evil**.
- Nightmare magic is not a second personality or another being.
- There is no separate evil Shadow Teddy in main canon.
- Teddy never splits into independent golden and purple people.
- Heartland and Nightmare are both necessary.
- Heartland expresses hope, courage, love, imagination, focus, creation and protection.
- Nightmare expresses fear, anger, grief, painful memory, instinct, uncertainty, chaos and immense emotional power.
- Teddy's red stitched heart is the living bridge between the realms.
- Teddy was created from the remaining magic of two ancient guardians, one Heartland-linked and one Nightmare-linked.
- Being created for a purpose does not mean that purpose owns Teddy's life.
- The Unraveler was once Keeper of the Heart Lantern. His attempt to remove painful emotions evolved into forced separation and control.
- Teddy's story is about integration and directed choice, not destroying a dark half.

Legacy files such as `dark_teddy`, `corrupted_teddy`, `shadow_teddy` and `giant_dark_teddy` must **not** represent an evil second Teddy in main canon. Exclude them from the main story by default unless later repurposed as unrelated/non-canon content without contradicting canon.

### Current confirmed story cutoff

The currently confirmed user-supplied story reaches The Last Stitch. The Unraveler attacks Teddy's stitched heart. Teddy remains one person. Teddy answers:

> “Maybe not all the time.”
>
> “But it's still me.”

Teddy stops trying to silence Nightmare energy and gives it direction. Purple chaos becomes movement, strength, unpredictable attacks and deliberate destructive power. **Golden magic gives that power focus.**

That is the current confirmed story cutoff. Any older assistant-created continuation after this point — restored Heart Lantern ending, sparing/restoring the Keeper, rebuilt-Moonstitch epilogue, final Ghost Child “Both” scene, post-credits material, or any other continuation — is **PROVISIONAL / NON-CANON unless the user explicitly reconfirms it**.

---

## Asset-label semantics

This file is an **audit and production requirements** record.

Use these visual statuses:

- `FINAL QUALITY`
- `GOOD ENOUGH FOR V1`
- `PLACEHOLDER`
- `REPLACE BEFORE RELEASE`
- `REJECTED`
- `UNUSED`

Use these audio statuses:

- `FINAL`
- `TEMPORARY`
- `REPLACE`
- `UNUSED`

Any `HAVE` or `VERIFY` description from planning discussions is provisional until the development copy is source-audited and visually inspected. A similarly named file is not enough to call the requirement satisfied.

---

# MASTER ASSET REQUIREMENTS

## 1. Production references and registries

Required durable production references:

- Art Bible
- Character Bible
- Environment Bible
- Animation Bible
- VFX Bible
- UI Bible
- Audio Bible
- Story Bible
- Asset Registry
- Asset Gap Report
- Duplicate Asset Report
- Replacement Asset List
- Asset Provenance Register
- Licensing Register
- AI-assisted asset disclosure register
- asset naming rules
- animation naming rules
- sprite scale rules
- lighting rules
- palette rules
- outline rules
- pixel-density rules
- grounding/baseline rules
- export settings
- Godot import settings

## 2. Teddy — canonical master design

Maintain:

- vertically split golden/yellow and purple/violet body
- central stitched seam
- golden-side eye treatment consistent with final art direction
- glowing violet/Nightmare eye
- purple-side horn
- tongue where appropriate
- red stitched heart
- one golden/yellow leg and one purple/violet leg
- red/purple/gold jester hat with bells
- matching collar/costume/diamond/patch motifs
- ornate double-headed axe/halberd

Directional sprites must preserve anatomical continuity. Do **not** blindly mirror Teddy if mirroring swaps the intended golden/purple anatomical side.

## 3. Teddy movement animation

Audit/create complete multi-frame coverage for:

- idle / breathing / blinking
- look up / look down
- turn around
- walk
- run start / loop / stop
- jump anticipation / rise / apex / fall / land
- hard landing
- double jump
- dodge / roll
- ground dash / air dash
- crouch / crouch movement where used
- ladder climb where used
- optional ledge grab / ledge climb / wall slide / wall jump / swimming / rope/zipline traversal if retained

## 4. Teddy combat animation

Audit/create:

- light Attack 1 / 2 / 3
- heavy attack
- charged attack
- running attack
- air attack
- down-air attack
- spin attack
- crouch attack if used
- special attack
- Heartland ability animations
- Nightmare ability animations
- combined Heartland/Nightmare attack
- Heart-Shard-powered attacks if used
- final integrated combat ability when canonically appropriate
- optional parry / block / counter if retained

Associated VFX needs include axe arcs, heavy arcs, Heartland/Nightmare/combined slashes, air trails, charged aura, hit impacts and weapon blur.

## 5. Teddy damage / status / death

Audit/create:

- light hurt
- heavy hurt
- knockback
- stun
- dizzy
- poison/burn/freeze/curse states where used
- low-health state
- invulnerability feedback
- death
- respawn
- revive only if the design retains it
- temporary invulnerability flash
- death transition/dissolve
- respawn stitch effect

## 6. Teddy story and emotion states

Audit/create:

- talk / listen
- angry
- sad
- surprised
- laugh / cheer
- point / wave
- read
- sit / sleep
- hold/pick up item
- chest interaction
- lever interaction
- push/pull if required
- quest complete
- victory
- game-complete state when ending is locked
- afraid
- determined
- confused
- emotionally hurt
- furious but controlled
- Nightmare-energy instability
- Chaos Surge
- regaining direction/control
- Heart Lantern resonance
- Heart Shard reaction
- stitched-heart reaction/damage/tearing/instability
- integrated magic state

## 7. Teddy portraits

Need a consistent major-expression set including neutral, happy, laughing, determined, worried, afraid, angry, sad, shocked, hurt, suspicious, confused, relieved, exhausted, Heartland-powered, Nightmare-powered, Chaos Surge and integrated state.

## 8. Teddy stitched-heart story set

Critical story-specific states:

- normal
- faint glow
- gold response
- violet response
- both colours
- overloaded
- stitches loosening
- partial tear
- severe damage
- magical stitch threads
- golden stitch energy
- violet stitch energy
- combined thread energy

## 9. Heartland magic visual language

Create/verify a coherent gold visual language for shield, healing, restoration, stabilisation/purification, focused beam, shrine activation, protective dome/barrier, defensive pulse, particles, aura, trails, impacts, runes and cooldown UI.

## 10. Nightmare magic visual language

Create/verify a coherent violet visual language for attack bursts, chaotic projectile, barrier breaking, environment warping, strength boost, displacement, aura, particles, trails, impacts, unstable sparks, runes and cooldown UI.

Player-facing terminology must not frame Nightmare magic as moral evil.

## 11. Combined magic

Signature combined assets:

- gold/violet aura
- combined attack
- stabilisation vortex
- dual-colour projectile
- dual-colour shield
- dual-colour environmental interaction
- dual-colour boss finisher
- Heart Shard resonance
- red-stitch connection effect

## 12. Heart Lantern — critical gap

Required production set:

- full environment object
- close-up
- inactive
- normal
- gold flame
- violet flame
- intertwined flames
- responding to Teddy
- vibration
- first crack
- heavy cracks
- explosion animation
- empty pedestal
- ruined state
- magical fragments
- energy wave
- restoration-ready state

Do not lock the exact final/end state until the ending itself is confirmed.

## 13. Heart Shards — critical gap

Required:

- master design
- inventory icon
- world pickup
- floating animation
- collected animation
- HUD icon
- map icon
- quest icon
- resonance VFX
- boss reward VFX
- cutscene close-up
- optional distinct chapter variants if useful

## 14. Ghost Child

Need production coverage for idle, floating movement, talk, laugh/tease, curious, worried, afraid, sad, happy, shocked, hurt, Nightmare-overwhelmed state, lashing-out story/combat state, stabilised/recovered state, Halloween festival version and final-reunion state only after ending lock.

Portraits: neutral, happy, funny, curious, worried, scared, overwhelmed, relieved.

Story prop: Ghost Child's Halloween charm, inventory icon and close-up art.

## 15. Witch Guide

Need idle, talk, walk, spellcasting, shocked, concerned, serious/reveal, festival and damaged-village states plus portrait-expression coverage.

## 16. Skeleton Blacksmith

Need idle, hammering, talk, walk, weapon upgrade, comedy bone-loss/retrieval if retained, festival, post-disaster and upgraded-shop states.

## 17. Vampire Merchant

Need idle, talk, gesture, bargaining, presenting items, dramatic reactions, festival, damaged-village and upgraded-shop states.

## 18. Mummy Healer

Need idle, talk, heal, bandaging, item-from-bandages gag if retained, festival and post-disaster states.

## 19. Pumpkin King

Need distinct friendly, unstable/corrupted-looking, partially stabilised and stabilised forms.

Boss animation needs include idle, walk, roar, vine attack, slam, projectile, root summon, charge, hurt, stagger, phase transition, vulnerable state, stabilisation sequence and later ally actions.

Portrait set: unstable, ashamed, relieved, friendly, determined ally.

## 20. Headless Knight

Need idle, walk, combat stance, weapon attacks, defence, special attack, hurt, stagger, test-fight resolution, non-hostile idle, later ally actions and portrait/cutscene art.

## 21. Bone Dragon

Need idle/breathing, resting/sleeping, awakening, locomotion, bite/claw/tail/breath/magic attacks, defensive movement, hurt, trial-complete state, speaking pose, later ally action and portrait/cutscene art.

## 22. The Unraveler / Original Keeper — critical gap

Create:

- Original Keeper historical appearance
- Keeper portrait
- early emotional-removal experiment imagery
- beginning-to-unravel state
- Unraveler standard form
- Unraveler portrait
- full boss silhouette
- Heartland-order state
- Nightmare-chaos state
- combined final-combat state if canonically required
- damage/stagger states

Animation needs include idle, locomotion/float, speaking, pointing/accusing, Heartland attack, Nightmare attack, teleport, shield, phase transitions, stitched-heart attack, realm-separation attack if retained, damage and stagger.

Do not lock his definitive post-defeat fate until the ending is confirmed.

## 23. Ancient Guardians — critical gap

Create Heartland Guardian and Nightmare Guardian full characters, portraits, paired scene, battle-wounded versions, final-sacrifice scene, magic-transfer-to-Teddy imagery, castle murals, tapestry and statue/carving versions.

## 24. Moonstitch Hollow world states

Need coherent production variants for:

- normal Moonstitch
- Halloween Festival Moonstitch
- Heart Lantern disaster
- post-disaster repair hub
- Nightmare Collision Moonstitch
- rebuilt/final Moonstitch only after ending lock

Festival needs include banners, bunting, pumpkin lanterns, stalls, sweets, games, decorations, skeleton musician zone, merchant displays, crowds, floating ghosts, festival lighting and Heart Lantern ceremony dressing.

Disaster/collision states need broken plaza, cracks, debris, magical fires/rifts, frightened villagers, repairs, boarded structures, altered NPC positions, impossible geometry, floating/upside-down architecture, gold/violet storms, physical memories and unstable portals.

## 25. Moonstitch buildings/interiors

Audit/create production-ready Skeleton Blacksmith forge, Vampire Merchant shop, Mummy Healer room, Witch Guide home/workspace, Ghost Child location, tavern, town hall, potion/alchemy location, Teddy home if used, archive/library and Heart Lantern plaza.

## 26. Whispering Forest

Need peaceful and unstable variants, whispering flowers, dream fragments, gold lights, violet shadows, twisting roots, moving branches, breakable Nightmare barriers, Heartland shrine, secrets, checkpoint, NPC area, chest areas, boss entrance, Pumpkin King arena and post-boss stabilised state.

## 27. Forgotten Graveyard

Need erased/partial tombstones, memory wisps, forgotten spirits, fog layers, ghost paths, memorial shrine, memory-fragment zones, Headless Knight arena, Heart Shard location, checkpoint, secret crypt and optional tomb interiors.

## 28. Broken Castle

Need ruined guardian castle exterior/interior, main hall, Guardian murals, Teddy tapestry/unfinished tapestry, origin chamber, Guardian records, ancient magical machinery, Chaos Surge variants, warped hallways/furniture, unstable walls, purple-energy cracks, archive, checkpoint and Heart Shard chamber.

## 29. Bone Caverns

Need giant fossil walls, ancient remains, dragon-bone structures, crystal chambers, pre-realm carvings, Bone Dragon arena/resting platform, trial rooms, dual-magic puzzle structures, Heart Shard altar and checkpoint.

## 30. Nightmare Collision region

Need fused Moonstitch/Nightmare tiles, floating chunks, broken-gravity elements, distorted buildings, living shadows, memory manifestations, violet storm backgrounds, golden sky cracks, portals, rescue spaces and Ghost Child encounter arena.

## 31. The Last Stitch / final region

Need final-realm master tiles, fragments of Moonstitch/Forest/Graveyard/Castle/Caverns, floating Heart Shard structures, torn-world edges, golden-order structures, violet-chaos structures, reality tears, final-boss approach/arena, realm-separation visuals, stitched-world imagery and damaged Heart Lantern fragments.

Exact ending-specific assets wait for story lock.

## 32. Heartlands / Nightmare Realm backgrounds

Need Heartlands distant view, Nightmare Realm distant view, rift glimpses, overlapping realms, realm boundary, collapsing boundary and temporary separated-space visuals if needed during final combat.

## 33. Standard enemies and archetypes

Every enemy used must have production-ready idle, patrol/movement, chase where applicable, attack telegraph, attack frames, hurt, knockback/stun where appropriate, death and loot/drop feedback.

Archetypes: walker, charger, flying, ranged, tank, fast, ambusher, shield, caster, spawner, elite.

Biome sets should cover suitable Forest, Graveyard, Castle, Cavern and late-game overlap enemies.

Nightmare influence must not automatically communicate moral evil.

## 34. Elite enemies

Each elite needs a visual marker, full core animation set, at least one special mechanic/attack, elite aura/effect and better reward feedback. Elites must differ mechanically rather than only through inflated health.

## 35. Boss master requirements

Every boss needs idle, intro, movement, readable telegraphs, several attacks, special attack, hurt, stagger, phase transitions, low-health state, defeat/story-resolution state, portrait, icon, nameplate/health-bar treatment, arena dressing, custom VFX and custom SFX.

## 36. Weapons

Primary Teddy weapon is the canonical double-headed axe/halberd. Need attack-frame alignment, charged state, Heartland-infused, Nightmare-infused, combined variant, upgrade visuals where appropriate, inventory icon and blacksmith-upgrade icon.

Other pack weapons are optional until core combat is proven.

## 37. Items and collectibles

Need Heart Shard, Ghost Child charm, Guardian records, chapter quest items, memory items, cavern relics and final-region quest items. Audit health items, currency, keys, materials, memory fragments, relics, books/journals and food.

Collectible classes may include coins, Heart Shards, memory fragments, lore pages, hidden relics, boss tokens and region secrets. Each needs world art, inventory/codex icon where relevant, pickup VFX and pickup sound.

## 38. Ability icons

Need canon-safe Heartland, Nightmare and combined branches plus traversal icons such as dash/double jump/air dash where retained. Do not label Nightmare abilities as evil/corrupt mode.

## 39. Interactables

Audit/create checkpoint/save point, chests, doors, locks, boss doors, secret doors, lever, switch, pressure plate, moving/falling platforms, elevator/lift, breakables, shrine, teleporter, fast-travel marker, quest board, healing fountain/rest point, Heart Lantern interaction, Heart Shard pedestal, Guardian archive and memory-manifestation interaction.

## 40. Hazards

Audit/create spikes, saws, swinging axes, falling rocks, poison, lava, collapsing bridges, falling platforms, fire/magic/projectile traps, Nightmare instability zones, reality tears, unstable-memory hazards and final-region separation hazards.

## 41. Checkpoint/chest/door states

Checkpoint: inactive, activation, active, respawn/save effect and fast-travel-unlocked state.

Chests: genuinely distinct closed/open variants for common/rare/legendary/story/locked/mimic as actually used.

Doors: normal, locked, boss, story, one-way and transition variants as required.

## 42. TileSets

Audit biome families and create Godot TileSet resources with production ground/platform/wall/ceiling/corners/slopes if used/one-way platforms/ledges/decorative/background/foreground/breakable/hazard/secret-wall/door-frame/boss-room tiles, collision shapes and terrain/autotile data.

## 43. Parallax / atmosphere / lighting

For each used world verify sky/far/mid/near/foreground layers. Story-specific variants include Halloween festival, destroyed Moonstitch, Nightmare Collision, final realm and Heart Lantern explosion/realm-collapse skies.

Atmosphere needs may include mist, fog, leaves, rain, dust, ash, magic motes, storms and realm-tear particles.

Lighting needs include lantern/candle/torch glow, Heart Lantern glow, gold/violet/dual magic, boss warnings and secret-room cues.

## 44. VFX master set

Audit/create slash/hit/jump/dash/heal/teleport/death/boss aura/level-up/pickup effects plus Heart Lantern crack/explosion, Heart Shard flight/pickup/resonance, realm rift/collision, memory apparition, Chaos Surge, stitched-heart glow/tear/thread effects, Unraveler order/chaos attacks, dual-energy clash and stabilisation effects.

## 45. HUD

Final phone HUD must be compact and readable. Required systems include health, Heartland resource, Nightmare resource, combined/balance indicator if retained, selected ability/item, currency, Heart Shard count, XP/level, quest objective, save/checkpoint indicator, interaction prompt, boss health, status effects, low-health feedback and cooldowns.

The HUD must not dominate the gameplay view.

## 46. Mobile controls

Critical functional assets/actions:

- move left/right or virtual joystick
- jump
- attack
- heavy/secondary if used
- dodge/dash
- Heartland ability
- Nightmare ability
- combined ability when unlocked
- interact
- item/heal
- pause

Each button needs normal, pressed, disabled and cooldown states where applicable. Size/opacity settings are required; repositioning is optional if practical.

Graphics alone do not satisfy this requirement: each control must trigger the intended named input action in runtime.

## 47. Menus and UI systems

Need functional title/main menu, Continue/New Game, save slots if used, pause, inventory, equipment, skills/abilities, quest log, map/world map, bestiary/lore if retained, shop, blacksmith, healer, settings/audio/graphics/controls/accessibility, death/respawn, boss/chapter completion, ending, credits and legal/licensing screens.

## 48. Dialogue / quest / inventory / skill-tree / map UI

Need reusable dialogue box, speaker plate, portrait frame, continue indicator, choices and special story/boss/memory variants.

Quest UI needs start/update/complete/objective tracker, main/side/world quest icons and log states.

Inventory/equipment UI needs slots, selected/equipped/locked states, quantity, rarity, descriptions, comparison and quest-item markers.

Skill tree should visually separate Heartland, Nightmare and combined/intertwined progression without moralising the Nightmare branch.

Map needs Teddy, checkpoint, boss, NPC, quest, shop, healer, blacksmith, secret, treasure, portal, fast travel, Heart Lantern and relevant Heart Shard markers with discovered/undiscovered states.

## 49. Major portraits

Priority full-expression sets: Teddy, Ghost Child, Witch Guide, Skeleton Blacksmith, Vampire Merchant, Mummy Healer, Pumpkin King, Headless Knight, Bone Dragon, The Unraveler/Original Keeper, Heartland Guardian and Nightmare Guardian.

## 50. Cutscene / story illustrations

Need key art for Festival, Teddy/Ghost Child, Heart Lantern reaction/shattering, Heart Shards scattering, first Unraveler appearance, departure, Ghost Child charm, Forest/Pumpkin King, Graveyard/Headless Knight/memories, Castle Guardians/Teddy tapestry/Chaos Surge, Bone Dragon/Keeper history, Nightmare Collision/allies/Ghost Child rescue and final-region Unraveler/stitched-heart/integrated magic.

Definitive ending art waits for ending lock.

## 51. Story-memory/flashback art

Need Keeper helping people, children/nightmares, grief, fear/danger, anger/injustice, emotional-removal process, emotionally empty world, accumulated Nightmare energy, Keeper consumed and Unraveler transformation.

## 52. Audio

The current source pack's audio breadth is not enough for a polished full RPG.

Music requirements include title, festival, Heart Lantern ceremony, disaster, post-disaster Moonstitch, each core chapter exploration theme, relevant boss themes, Chaos Surge, Nightmare Collision, Ghost Child rescue, final-region theme, multi-phase Unraveler music, victory/resolution, ending and credits when canon is locked.

SFX requirements include player movement/land/combat/magic/item/status, enemy family sounds, boss-specific sounds, Moonstitch/festival/forest/graveyard/castle/cavern/Nightmare-collision/final-region ambience and complete UI feedback.

## 53. Fonts / status icons / tutorials

Verify phone readability, punctuation, numbers/symbols and localisation readiness. Add heading/dialogue/HUD/accessibility variants if needed.

Need status-effect icons for poison/burn/freeze/stun/curse/regeneration/shield/attack/defence/movement boosts where implemented.

Tutorial visuals must cover movement, jump, attack, dodge, interact, Heartland, Nightmare, combined magic, checkpoint, inventory and quest systems for touch plus desktop/controller testing where relevant.

## 54. Save / loading / death / boss UI / accessibility

Need autosave indicator, loading spinner/cards, save-slot frame, corrupted-save warning/recovery UI, death/respawn treatment, checkpoint return VFX, boss UI/title treatment and accessibility icons/layouts for text size, high-contrast prompts, reduced motion/screen shake, vibration, touch size and touch opacity.

## 55. Android and store assets

Need final application icon, adaptive icon foreground/background, splash/launch branding and release-safe Android presentation.

Before Google Play release create feature graphic, phone/tablet/gameplay screenshots, Moonstitch/Forest/RPG/boss imagery, key art, promo banners, trailer thumbnail/trailer and press-kit logos. Recheck exact store dimensions and policy requirements against current official Google Play documentation at release time.

## 56. Marketing art

Need Teddy hero key art, Teddy + Ghost Child, Moonstitch, Pumpkin King, Headless Knight, Bone Dragon, Unraveler, ensemble art, Heart Lantern, social/store banners and platform-specific capsule art where needed.

## 57. Optional completion/bestiary/achievement art

Only after core gameplay is stable: bestiary portraits/silhouettes/discovery states and achievement artwork for major milestones, secrets, memory completion, no-damage boss, full upgrades and 100% completion.

## 58. World map

Need Moonstitch Hollow, Whispering Forest, Forgotten Graveyard, Broken Castle, Bone Caverns, Nightmare Collision and Last Stitch/final area, route lines, locked/unlocked routes, boss/quest/secret/fast-travel markers.

## 59. Animation technical metadata

Every production animation requires frame sequence/spritesheet, frame timing, animation name, loop flag, hit-frame markers, attack-active frames, movement-lock timing and SFX/VFX event timing as applicable.

## 60. Collision/gameplay metadata

Need player collision/hurtboxes, weapon hitboxes, enemy/boss hurtboxes, hazard/tile collisions, interactable areas, navigation/AI boundaries, camera limits and boss-room boundaries.

## 61. Level-design data

Every playable level needs scene data, spawn points, enemy placements/patrols, hazards, checkpoints, secrets, collectibles, chests, NPCs, quest/story triggers, boss trigger, camera/audio regions, transitions and exits.

## 62. Story data

Need dialogue database, quest database, character/chapter state, story flags, conditional NPC dialogue, boss dialogue, memory/lore text, item descriptions and bestiary descriptions where retained.

---

## Highest-priority production gaps

The immediate priority list is:

1. Heart Lantern complete production set.
2. Heart Shards.
3. The Unraveler / Original Keeper.
4. Ancient Heartland Guardian.
5. Ancient Nightmare Guardian.
6. Teddy full multi-frame directional/movement/combat/story animation coverage.
7. Teddy stitched-heart states.
8. Heartland/Nightmare/combined VFX.
9. Pumpkin King full boss/friendly/stabilised set.
10. Headless Knight production set.
11. Bone Dragon production set.
12. Ghost Child emotional/story set.
13. Halloween Festival Moonstitch.
14. Heart Lantern disaster Moonstitch.
15. Nightmare Collision Moonstitch.
16. Broken Castle origin chambers/murals/tapestry.
17. Bone Caverns story art.
18. Last Stitch final-region environment.
19. Story/cutscene illustrations.
20. Expanded soundtrack.
21. Expanded SFX/ambience.
22. Compact commercial HUD.
23. Functional Android touch-control graphics/states.
24. Major-character portrait expression sets.
25. Final store/release graphics.

---

## Quality and evidence gates

Use the evidence ladder:

`PLANNED -> IN_PROGRESS -> IMPLEMENTED_UNVERIFIED -> SOURCE_VERIFIED -> TEST_VERIFIED -> BUILD_VERIFIED -> EMULATOR_VERIFIED -> DEVICE_VERIFIED -> JUDGE_VERIFIED`

Also use `BLOCKED`, `FAILED`, and `SUPERSEDED` where appropriate.

A feature is never “done” simply because code exists. Runtime behavior, edge cases, integration and relevant Android testing are required.

Bug severity order:

1. BLOCKER — progression impossible.
2. CRITICAL — crash/save corruption/destructive failure.
3. HIGH — major system broken.
4. MEDIUM — significant gameplay defect.
5. LOW — cosmetic/minor issue.

Fix in that order.

---

## Immediate authoritative production gate

Proceed in this order:

1. Preserve the original v1.1 ZIP unchanged.
2. Audit Teddy animation frames and directional continuity.
3. Audit Whispering Forest tiles/backgrounds.
4. Audit first enemy candidates.
5. Audit Pumpkin King.
6. Audit Ghost Child.
7. Audit HUD/touch assets.
8. Audit/create Heart Lantern and Heart Shard sets.
9. Create the portable Godot 4.x project foundation.
10. Prove real Teddy movement, camera, collision and animation in a test room using actual production art.
11. Only after that runtime foundation passes, add combat and the first enemy.
12. Continue through checkpoint/save/UI/mobile controls/Pumpkin King and the full Forest vertical slice.
13. Run complete gameplay regression and Android touch tests.
14. Judge the exact build against the user's requested experience.
15. If Judge is below 11/10, fix and retest rather than declaring completion.

---

## Definition of complete

The project is not complete merely because it builds, launches, has code, or contains many assets.

Final success requires a portable Godot project that can be copied to another compatible computer, opened with the documented Godot version, run, exported to Android, installed on the target Android device, played from beginning to ending, closed, reopened, and continued from a valid save without reconstructing missing project pieces manually.

Commercial release additionally requires current store-policy review, licensing/provenance review, complete story/gameplay, no blocker bugs, no known save corruption, functional bosses/abilities/menus/settings/audio, acceptable performance, tested touch controls, and physical Android device verification.

The exact requested game must not be called complete until Judge passes the exact build at **11/10**.