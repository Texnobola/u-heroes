# Changelog

All notable changes to U-Heroes will be documented in this file.

## [Unreleased]

### Added
- Neural Flux energy system with HUD overlay
- NanoTech Hero armor set with progressive bonuses
- Laser Sword with LASER and INFERNO EDGE modes
- Bilingual support (English and Uzbek)
- Debug commands for flux management
- GeckoLib 3D models for armor and weapons
- Multiplayer compatibility with proper sync

### Technical
- Forge Capability system for player data
- Client-server networking via SimpleChannel
- Event-driven architecture
- Proper null checks to prevent world exit crashes

## [0.1.0] - Initial Development

### Core Systems
- Project structure and build configuration
- Deferred registration system
- Bilingual language support framework
- Neural Flux capability attachment
- Packet networking infrastructure

### NanoTech Hero
- 4-piece Nano-Tech Suit armor
  - Helmet, Chestplate, Leggings, Boots
  - Set bonuses (2/3/4 pieces)
  - Self-repair and regeneration
- Laser Sword weapon
  - Dual mode system (LASER/INFERNO EDGE)
  - Mode switching with flux cost
  - Particle effects per mode

### Client Features
- Flux Meter HUD overlay
- Armor and weapon renderers
- Particle effects system
- Client-side flux data caching

### Commands
- `/uh_flux` command family for debugging

---

**Format**: [Version] - YYYY-MM-DD
**Types**: Added, Changed, Deprecated, Removed, Fixed, Security
