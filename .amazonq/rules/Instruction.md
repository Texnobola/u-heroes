You are building a Forge 1.20.1 mod called "U-Heroes" (modid: u_heroes).

═══════════════════════════════════════════════════════════
IDENTITY
═══════════════════════════════════════════════════════════
- Mod Name: U-Heroes
- Mod ID: u_heroes
- Package: com.uheroes.mod
- Forge: 1.20.1-47.2.0
- Java: 17
- Mappings: Parchment 2023.09.03-1.20.1
- Description: A hero-based combat mod. Each hero has unique suit, weapons, abilities, and companion. First hero: NanoTech.

═══════════════════════════════════════════════════════════
ORIGINAL NAMING — DO NOT USE JJK / ANIME TERMS
═══════════════════════════════════════════════════════════
- The power system is called "Neural Flux" (shortened to "Flux")
- The energy bar is called "Flux Meter"
- Energy overflow state is called "Flux Overcharge"
- Energy depletion is called "Flux Burnout"
- Rocket-boosted punch is called "Burst Strike" (aerial version: "Aerial Burst Strike")
- Ground slam is called "Seismic Slam"
- Forward rocket boost is called "Afterburner"
- Self-healing is called "Adaptive Regeneration"
- AVA's projectile attack is called "Flux Bolt"
- Scanning system is called "IRIS Protocol" (Integrated Recon & Intelligence System)
- NEVER use "Cursed Energy", "Cursed Technique", "Domain", "Shikigami", or any Jujutsu Kaisen terminology
- NEVER use terminology from other anime/manga
- All names should feel sci-fi / nano-tech / military-tech themed

═══════════════════════════════════════════════════════════
LANGUAGE SUPPORT
═══════════════════════════════════════════════════════════
- The mod MUST support TWO languages: English (en_us) and Uzbek (uz_uz)
- Every single translatable string must have entries in BOTH lang files
- Use Component.translatable() for ALL user-facing text (never hardcode display strings)
- Translation keys follow pattern: "category.u_heroes.key_name"
- Uzbek translations must use modern Latin Uzbek script (O'zbek tili)
- Create BOTH lang files every time a new translatable string is added

═══════════════════════════════════════════════════════════
ARCHITECTURE RULES
═══════════════════════════════════════════════════════════
1. Use DeferredRegister for ALL registrations (items, entities, sounds, particles, etc.)
2. Use Forge Capabilities (ICapabilityProvider, LazyOptional) for custom player data
3. Use SimpleChannel for ALL client-server networking
4. NEVER use deprecated Forge methods
5. Use @SubscribeEvent and @Mod.EventBusSubscriber for event handling
6. Every feature must consume or interact with the Neural Flux capability system
7. All abilities must have cooldowns stored server-side in capability or HashMap<UUID, CooldownData>
8. All visual effects (particles, HUD, overlays) must be client-side only
9. All damage/logic/state changes must be server-side only
10. Write clean, documented code with Javadoc on every public method
11. Use Java 17 features where appropriate (records, sealed classes, enhanced switch)
12. The mod is designed for MULTIPLAYER compatibility — always sync state properly
13. When I say "create", generate ALL necessary files: Java, JSONs, BOTH lang files, data files
14. After each task, list ALL files you created or modified in a summary table

═══════════════════════════════════════════════════════════
PACKAGE STRUCTURE
═══════════════════════════════════════════════════════════
15. Core systems: com.uheroes.mod.core
16. Flux system: com.uheroes.mod.core.flux
17. Networking: com.uheroes.mod.core.network
18. Commands: com.uheroes.mod.core.command
19. Config: com.uheroes.mod.core.config
20. Cutscene system: com.uheroes.mod.core.cutscene
21. Hero unlock system: com.uheroes.mod.core.hero
22. Registration: com.uheroes.mod.init
23. Events: com.uheroes.mod.event
24. Client code: com.uheroes.mod.client
25. Client HUD: com.uheroes.mod.client.hud
26. Client particles: com.uheroes.mod.client.particle
27. Client renderers: com.uheroes.mod.client.renderer
28. Client input: com.uheroes.mod.client.input
29. Client animations: com.uheroes.mod.client.animation
30. NanoTech hero: com.uheroes.mod.heroes.nanotech
31. NanoTech armor: com.uheroes.mod.heroes.nanotech.armor
32. NanoTech weapons: com.uheroes.mod.heroes.nanotech.weapon
33. NanoTech abilities: com.uheroes.mod.heroes.nanotech.ability
34. NanoTech AVA: com.uheroes.mod.heroes.nanotech.ava

═══════════════════════════════════════════════════════════
DEPENDENCIES (Mandatory)
═══════════════════════════════════════════════════════════
35. GeckoLib 4 (geckolib-forge-1.20.1-4.7.3) — 3D animations
36. Curios API (curios-forge-5.9.1+1.20.1) — custom equipment slots
37. playerAnimator (player-animation-lib-forge-1.0.2-rc1+1.20) — player body animations
38. Caelus API (caelus-forge-3.2.0+1.20.1) — flight mechanics
39. Cloth Config (cloth-config-forge-11.1.136) — config GUI

═══════════════════════════════════════════════════════════
DEPENDENCIES (Soft / Optional)
═══════════════════════════════════════════════════════════
40. Cutscene API by thewinnt (mc1.20.1-forge-1.6.4) — cinematic scenes, SOFT DEPENDENCY
41. ALL Cutscene API class references isolated in CutsceneAPIHandler.java only
42. Check ModList.get().isLoaded("cutscenes") before any Cutscene API call
43. When Cutscene API absent, use CutsceneFallback.java (vanilla titles + overlay)
44. Cutscenes are datapack JSONs in data/u_heroes/cutscenes/
45. All cutscenes FIRST-TIME-ONLY by default (tracked via player persistent data)
46. Cutscenes must be skippable if config allows

═══════════════════════════════════════════════════════════
CODING STANDARDS
═══════════════════════════════════════════════════════════
47. No magic numbers — use named constants or config values
48. Group related constants in enums or constant classes
49. Use Optional/LazyOptional properly — never .orElse(null) without handling
50. Packet handlers must verify correct side via ctx.get().getDirection()
51. All entity data synced via SynchedEntityData or custom packets
52. Use ResourceLocation("u_heroes", "name") — never hardcode strings
53. Prefer composition over inheritance
54. Every enum must have fromString/fromNBT recovery method
55. NBT keys must be static final String constants
56. ALL user-facing text uses Component.translatable() with keys in BOTH en_us.json AND uz_uz.json