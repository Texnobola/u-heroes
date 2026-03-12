# Changelog

All notable changes to U-Heroes will be documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### Added
- Asteroid impact cinematic on first spawn (movie-style with fireball, flash, camera lock)
- NanoCreature entity (GeckoLib, Venom-style) that merges with player on contact
- NanoAlloyBlock ore — generates underground in all overworld biomes (depth 0–32)
- Life Essence — 2% drop chance from any leaves block
- Global loot modifier system for Life Essence drops
- AsteroidCraterGenerator — sphere radius 6, scatters 50 NanoAlloy blocks on first login
- FirstSpawnHandler — one-time origin trigger per player using persistent NBT flag
- Server-to-client packets: AsteroidPositionPacket, TriggerImpactSequencePacket
- NanoSuit armor set (GeckoLib 4.x, per-slot model/texture switching)
- 10-attack combo system with 3 phases and smoothstep lerp
- Attacks 8–10 cost Neural Flux (1/2/3) with damage multipliers (1.4×/1.8×/2.5×)
- Blocking mechanic integrated into SaberComboHandler
- Laser Sword with 3 attack animations (PlayerAnimator)
- Saber slash VFX via AAAParticles (Chloe Maven) with per-swing roll angles
- Hit stop and screen shake on attack
- Screen edge hit flash overlay
- Combo counter HUD
- Neural Flux HUD — futuristic segmented cyan bar with scanlines and pulse effect
- Custom sounds: nano_suit_equip, laser_sword_swing, laser_sword_hit
- NanoCreature item — right-click to spawn entity
- NanoAlloyFragment and LifeEssence items with recipes and loot tables
- Discord community server: U-heroes

### Fixed
- NanoCreature texture path corrected to `textures/entity/armor/`
- Saber slash direction now syncs with animation (per-swing roll via lastSwingIndex)
- NanoAlloyBlock now mineable with iron pickaxe (added block tags)
- Cinematic TRIANGLE_FAN rendering replaced with GuiGraphics.fill() for Embeddium compatibility
- Camera yaw now locks toward crater so fireball appears on screen
- Cinematic trigger delayed until world is fully loaded (no more instant black screen)
- NanoSuit chestplate arm bones separated from body bone (arms now move correctly)
- SoundEvents lookup uses ForgeRegistries instead of SoundEvents fields (mapping-safe)

### Technical
- Forge 1.20.1-47.4.10, GeckoLib 4.8.3, PlayerAnimator 1.0.2-rc1+1.20
- AAAParticles via mod.chloeprime maven (replaced Photon)
- Forge JSON biome modifier for ore worldgen (no Java worldgen code needed)
- GlobalLootModifier system for leaf drops
- Block tags via data/minecraft/tags for tool requirements

---

## [0.1.0] - 2026-01-01 — Initial Setup

### Added
- Project structure and Forge MDK build configuration
- Deferred registration system for blocks, items, entities, sounds
- Neural Flux capability and Flux Meter HUD
- SimpleChannel packet networking infrastructure
- `/uh_flux` debug commands
- Language file framework (en_us)