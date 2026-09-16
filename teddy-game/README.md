# Teddy Game — Android Vertical Slice v0.1

Playable Android-first proof for **Teddy Game**, a commercial 2D side-scrolling RPG action-platformer by Potato Pixel Art Studios.

## Playable content
Moonstitch Hollow Halloween opening, touch/keyboard/controller movement, jump and double-jump, axe combat, Heartland healing, Nightmare burst, XP/levels, coins, potions, blacksmith axe upgrades, friendly Gothic-Halloween NPC interactions, Heart Lantern catastrophe, Whispering Forest enemies, corrupted Pumpkin King boss, dual-power purification, first Heart Shard, return-home completion state and persistent local save data.

## Canon guardrails
Teddy is one person. His violet/Nightmare nature is chaotic, instinctive, volatile and powerful, **not evil and not a separate personality**. The boss resolution explicitly requires both Heartland focus and Nightmare force.

## Android
`compileSdk 36`, `targetSdk 36`, `minSdk 26`, Java, Android Gradle Plugin 9.4.0. No network permission or backend is required for the vertical slice.

## Build
Requires JDK 17+, Android SDK 36 and Gradle 9.6-compatible tooling.

```bash
gradle testDebugUnitTest lintDebug assembleDebug bundleDebug
```

Evidence status must follow the Universal Team ladder: source, tests, build, emulator and physical-device evidence are separate claims.
