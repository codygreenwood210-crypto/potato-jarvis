# Universal Team — Chat Archive — Zero-Cost Commercial AI Game Pipeline

UNIVERSAL TEAM — CHAT ARCHIVE
ZERO-COST COMMERCIAL AI GAME CREATION PIPELINE

Archive scope
This archive preserves the visible user/assistant conversation in this chat about building a reusable, project-agnostic game-creation pipeline using free AI and free/open-source tools, with the specific goal of eventually selling the resulting game without depending on paid AI tokens or generation credits.

Important boundary
This is NOT a Teddy Game-specific plan. The user explicitly clarified that the pipeline is intended for any game they may want to create.

FULL VISIBLE CHAT TRANSCRIPT — RECONSTRUCTED FROM CURRENT CONVERSATION

USER:
I need a list of how I can make AAA game graphics assests and spirit and animate them using ai

ASSISTANT:
Explained that a modern AI-assisted game-art pipeline can cover concept art, sprites, 3D models, textures, rigging, animation and engine integration, but that AAA production quality still needs consistency, topology/UV cleanup, animation cleanup, lighting/material work and in-engine testing. Recommended tools at that stage included Scenario/Layer for concept and 2D work, Meshy/Tripo for 3D generation, Aseprite/Spine for sprites and 2D animation, Substance 3D for materials, DeepMotion/Cascadeur for animation, MetaHuman for photoreal humans and Unreal for final presentation.

USER:
This isn't for the teddy game but any game I want to create

ASSISTANT:
Confirmed the system should be treated as a universal AI Game Asset Factory, not tied to Teddy Game. Defined the reusable pipeline as:
Idea -> Style Bible -> AI Concept -> Asset Generation -> Cleanup -> Rig/Animate -> QA -> Game Engine -> Asset Library.
The system should support 2D, 2.5D, stylized 3D, realistic 3D, pixel art, mobile, indie and AAA-style production.

USER:
What AIs can do all those thighs

ASSISTANT:
Explained that no single AI reliably does every AAA game-production task end-to-end. Described a practical stack:
- Layer AI: broad game-content workspace.
- Scenario: 2D characters, sprites and style consistency.
- Meshy / Tripo: 3D model generation.
- Cascadeur: character animation and cleanup.
- DeepMotion: video-to-motion-capture.
- MetaHuman: realistic humans.
- Substance 3D: professional PBR materials.
Recommended thinking in terms of a pipeline rather than a single AI.

USER:
So if I wanted to make a game from strach how would I do it

ASSISTANT:
Outlined a complete game-development sequence:
Idea -> Design -> Prototype -> Art Direction -> Assets -> Rigging/Animation -> Game Systems -> Levels -> Audio/VFX -> Testing -> Optimization -> Release.
Strongly recommended proving one small playable vertical slice before creating hundreds of final assets.

USER:
Which free AIs would I use

ASSISTANT:
Initially proposed a free/free-tier stack including ChatGPT, ComfyUI, Scenario, Tripo, Meshy, DeepMotion, Cascadeur, Blender and Unreal/Godot. Then highlighted an important licensing distinction: free-to-try is not the same as free for commercial use. Tripo, DeepMotion and Cascadeur free tiers can carry restrictions; Meshy and other cloud tools can depend on credits or changing plan terms.

USER:
My goal is literally spend $0 and still eventually sell the game

ASSISTANT:
The plan was revised toward a commercial-safe, zero-cost development stack:
- Godot for the game engine.
- Blender for 3D modelling, rigging and animation.
- Krita for 2D finishing.
- Pixelorama for pixel art and sprite sheets.
- ComfyUI for local AI workflows.
- FLUX.1-schnell locally for AI image/concept generation.
- Ollama + Apache-2.0 Qwen-family models for local coding/design assistance.
- FreeMoCap / MediaPipe-style local motion-capture workflows.
- Mixamo treated as optional rather than foundational because it is a hosted service whose future terms cannot be guaranteed.
The central rule became: local/open tools first, cloud free tiers only when they are optional and never required for production continuity.

USER:
Cheak if this method is 100% free and doesn't require me to ever but tokens

ASSISTANT:
The earlier method was corrected. Stability AI models with revenue thresholds were removed from the strict "never pay" core. The strict token-free pipeline was defined as:
Ollama + Qwen -> ComfyUI + FLUX.1-schnell -> Krita/Pixelorama -> Blender -> FreeMoCap/MediaPipe -> Blender animation cleanup -> Godot.
Cloud-credit tools such as Meshy, Tripo, DeepMotion and similar services were removed from the required path.
The assistant also noted that "free forever" can only be grounded in the licenses/releases archived today; no one can guarantee a third-party service will remain available forever. The safest strategy is to download and preserve exact local software/model versions and their license files.

USER:
Create a road map for creating a game from start to completion using nothing but completely free ai that I can sell

ASSISTANT:
A strict zero-cost commercial development roadmap was produced. Its controlling direction is preserved below.

ZERO-COST COMMERCIAL GAME ROADMAP

1. Create the game concept.
Use a local assistant through Ollama with an Apache-2.0 Qwen model. Define genre, core loop, controls, target platform, visual style, story, characters, progression, enemies, levels and technical requirements.

2. Create the Game Design Document.
Turn the concept into a production specification. Separate the full dream game from a small vertical slice. The first playable target should be one character, one level/area, one enemy, one objective, one menu and a complete win/lose loop.

3. Create the project in Godot.
Use Godot as the strict zero-cost engine. Build project structure, input controls, scene management, camera, collision, save architecture and a basic player controller before final art.

4. Use local AI to help program the prototype.
Use a local Qwen coding model through Ollama. Work one system at a time: movement, combat, enemies, health, inventory, dialogue, quests, save/load, UI, etc. Test each addition in the actual engine.

5. Prototype with placeholder assets.
Use primitives and verified free assets while proving the gameplay. Do not spend weeks generating polished graphics before the game is fun.

6. Create the visual identity with local AI.
Use ComfyUI locally with FLUX.1-schnell for commercially usable AI image generation under its present Apache-2.0 licensing. Generate concept art, character/environment references, weapons, props, vehicles, architecture, UI ideas and palette exploration.

7. Create an Art Bible.
Lock proportions, palette, materials, lighting, character rules, environment rules and UI language. Every later asset should follow this document so the game does not look like unrelated AI generations.

8. Produce final 2D assets.
Use ComfyUI + FLUX.1-schnell for generation/reference. Finish and correct assets in Krita. For pixel art, use Pixelorama. Enforce clean silhouettes, palettes, transparency, consistent proportions and game-ready sizes.

9. Produce 3D assets.
For the strict no-token route, use AI primarily for reference images and design, then create or finish production-ready meshes in Blender. Process:
AI reference -> Blender model/sculpt -> retopology -> UV -> textures/materials -> LODs -> collision mesh.
Do not make the commercial pipeline depend on a cloud 3D generator.

10. Rig and animate.
Use Blender's rigging and animation tools. Use FreeMoCap or other locally runnable open motion-capture tooling where useful:
record movement -> motion capture -> skeleton data -> Blender -> cleanup -> Godot.
Keep cloud animation services optional, never required.

11. Build environments and levels.
Generate visual references with local AI, then turn them into modular level kits in Blender/Godot. Reuse walls, floors, cliffs, vegetation, buildings, props and other modular pieces.

12. Build gameplay systems and enemies.
Implement systems incrementally with local AI assistance and engine testing:
combat, enemy state machines, bosses, inventory, crafting, dialogue, quests, NPCs, progression, checkpoints, difficulty, etc.

13. Create sound and music.
Do not force unverified AI music models into a strict commercial pipeline. Use free/open tools such as Audacity, self-recorded audio, procedural synthesis and properly verified CC0/commercially permitted assets. Archive the license/provenance of every external asset.

14. Polish.
Add lighting, particles, shaders, animation transitions, footsteps, impact feedback, camera effects, controller support, accessibility, options, save slots, tutorials and presentation polish.

15. QA.
Test every level, item, enemy, save condition, resolution, control method and restart/death state. Maintain a bug record with reproduction steps, expected result, actual result, severity and fix status. Local AI can inspect scripts/logs without API tokens.

16. Commercial license audit.
Maintain a LEGAL/LICENSES archive with exact licenses for:
- engine
- AI model/checkpoint
- LoRA
- asset packs
- fonts
- audio
- Blender add-ons
- libraries
- code dependencies
Do not assume that a model is commercially usable merely because it runs in ComfyUI.

17. Create release builds.
Export target-platform builds. Test them on clean devices. Prepare icons, screenshots, trailers, store text, privacy disclosures, credits and AI disclosures where required.

18. Sell the game.
A no-upfront-fee storefront such as itch.io can be used initially. Other platforms may charge listing, submission, certification, transaction or payment-processing fees. These are distribution/business costs rather than required AI-token costs.

STRICT CORE STACK

- Local AI design/programming: Ollama + Qwen-family Apache-2.0 model
- Local AI image generation: ComfyUI + FLUX.1-schnell
- 2D finishing: Krita
- Pixel art: Pixelorama
- 3D modelling, UV, rigging, animation: Blender
- Motion capture: FreeMoCap / locally runnable open pose/mocap tooling
- Audio editing: Audacity
- Game engine: Godot
- Optional starter/reference assets: verified CC0 sources such as Kenney
- Initial storefront: itch.io where appropriate

NON-NEGOTIABLE ZERO-TOKEN RULE
The production pipeline must not require:
- paid AI API calls
- purchased generation credits
- subscription-only AI services
- cloud tools that become mandatory to reopen/edit/rebuild the game
Cloud free tiers may be used only if they are optional and their output license is independently acceptable.

COMMERCIAL LICENSING RULE
"Free" is not enough. Every model, checkpoint, LoRA, add-on, font, sound, texture and asset must have a license that permits the intended commercial use. Exact downloaded versions and license text should be archived with the project. Re-check important licenses before release because third-party terms can change.

COPYRIGHT / HUMAN-AUTHORSHIP PRACTICE
For important commercial assets, use AI as a generator/assistant and retain meaningful human direction and editing: selection, compositing, modelling, painting, animation, layout, transformation and integration. Preserve source files and creation history. Legal treatment of purely AI-generated material can vary by jurisdiction and over time.

REAL-WORLD COST BOUNDARY
The development stack is designed to require no AI tokens or mandatory software subscriptions. It does not eliminate electricity/hardware costs already borne by the user, and it cannot eliminate store fees, payment-processing fees, taxes, console certification or other distribution costs that may arise when selling.

CURRENT CANONICAL DECISION FROM THIS CHAT
This is a reusable Universal Team game-development pipeline for ANY future game, not a Teddy Game-specific pipeline.

USER:
Read my Google drive and git hub then save this to this chat to the archives

USER (clarified while archive work was underway):
Read my Google drive and git hub then save this whole chat to the archives

ARCHIVIST CLOSEOUT
The archive system was grounded against:
- Google Drive Universal Team Memory folder and its Interaction & Knowledge Ledger, Handoff Ledger, Project Registry, Lessons Learned and START HERE documents.
- GitHub repository codygreenwood210-crypto/potato-jarvis, branch potato-v5.8-final, including the Archivist protocol and Universal Team master roster.
The GitHub project protocol says durable records should preserve user request/mission, work performed, decisions and rationale, verification evidence, unresolved risks and next action, while never using chat memory as the only source of truth.

Verification state for this archive:
USER-DIRECTED / SOURCE-VERIFIED for the archive placement and continuity structure.
The zero-cost tool/licensing claims were researched earlier in the same conversation; they should be re-verified against current licenses before an actual commercial release because external terms can change.

NEXT ACTION
When the user starts a specific new game, instantiate this universal roadmap into a project-specific Game Design Document, Mission Contract, vertical-slice plan, asset/style bible, repository structure and license/provenance folder without changing the universal $0/no-token rule unless the user explicitly chooses to do so.
