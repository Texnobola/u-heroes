# U-Heroes

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.10-orange.svg)](https://files.minecraftforge.net/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-U--Heroes-5865F2.svg)](https://discord.gg/your-invite)

A hero-origin combat mod for Minecraft 1.20.1 Forge. Your story begins the moment something crashes from the sky.

---

## ⚡ The Origin

On your first login, an asteroid strikes the world near your spawn. A **NanoCreature** — a living swarm of nanobots — emerges from the crater and merges with you, bonding its mass to your body and granting you the **NanoSuit**.

This is not a mod where you craft your power. You earn it.

---

## 🦸 NanoTech Hero

### 🛡️ NanoSuit Armor
A full GeckoLib 3D armor set with progressive set bonuses:

| Pieces | Bonus |
|--------|-------|
| 2 | Speed I |
| 3 | Speed I + Strength I |
| 4 | Speed I + Strength II + Self-Repair + Adaptive Regen + Flux Amplification |

- **Defense**: 24 (diamond-tier)
- **Toughness**: 3.5
- **Knockback Resistance**: 20%
- **Durability**: 2,475 total (self-repairing at 4 pieces)

### ⚔️ Laser Sword
A 10-hit combo weapon with 3 attack phases. Each swing plays a unique animation and slash VFX that matches the attack direction.

**Late combo attacks (hits 8–10) consume Neural Flux:**
| Hit | Flux Cost | Damage Multiplier |
|-----|-----------|-------------------|
| 8   | 1         | 1.4×              |
| 9   | 2         | 1.8×              |
| 10  | 3         | 2.5×              |

### ⚡ Neural Flux System
Bio-electric energy that powers all advanced abilities. Tracked by a futuristic segmented HUD with scanlines and pulse animation. Depletes on heavy attacks, regenerates passively.

---

## 🌍 The World

### NanoAlloy Ore
Scattered underground across all overworld biomes (depth 0–32). Requires an **iron pickaxe** or better to mine. Rarer than iron, more common than diamond.

### Life Essence
Dropped by breaking **any leaves block** with a 2% chance. A rare organic component tied to the nanobot origin story.

### Crafting
- NanoAlloy Block → crafted from NanoAlloy Fragments
- Laser Sword → crafted with NanoAlloy components
- Full NanoSuit → crafted with NanoAlloy Blocks and Fragments

---

## 📦 Required Dependencies

| Mod | Version | Purpose |
|-----|---------|---------|
| [Forge](https://files.minecraftforge.net/) | 47.4.10 | Mod loader |
| [GeckoLib 4](https://www.curseforge.com/minecraft/mc-mods/geckolib) | 4.8.3+ | 3D armor & entity models |
| [PlayerAnimator](https://www.curseforge.com/minecraft/mc-mods/playeranimator) | 1.0.2-rc1+ | Attack animations |
| [AAAParticles](https://github.com/chloeprime/AAAParticles) | 1.20.1-1.4.11+ | Slash VFX |
| [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) | 5.9.1+ | Equipment slots |
| [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) | 11.1.136+ | Config GUI |

---

## 🚀 Installation

1. Install **Minecraft Forge 47.4.10** for Minecraft 1.20.1
2. Download and install all required dependencies above
3. Download `u_heroes-x.x.x.jar` from the releases page
4. Drop it into your `mods/` folder
5. Launch and play — no config needed

---

## 🛠️ Building from Source

```bash
git clone https://github.com/Texnobola/u-heroes.git
cd u-heroes
./gradlew build
```

Output: `build/libs/u_heroes-x.x.x.jar`

### Project Structure
```
com.uheroes.mod
├── core/
│   ├── loot/          # Global loot modifiers (Life Essence drops)
│   └── network/       # S2C/C2S packets
├── init/              # DeferredRegister classes
├── event/             # Forge event handlers
├── client/
│   ├── animation/     # PlayerAnimator attack layers
│   ├── hud/           # Flux meter, combo counter, hit flash
│   └── renderer/      # GeckoLib entity & armor renderers
├── heroes/
│   └── nanotech/
│       ├── armor/     # NanoSuit items & handler
│       └── weapon/    # Laser Sword item
├── origin/            # NanoCreature, crater gen, cinematic
└── UHeroesMod.java
```

### Debug Commands *(OP level 2)*
```
/uh_flux get
/uh_flux set <amount>
/uh_flux fill
/uh_flux consume <amount>
/uh_flux setmax <amount>
/uh_flux regen <rate>
```

---

## 📋 Roadmap

### ✅ Done
- [x] Origin cinematic (asteroid impact, movie-style)
- [x] NanoCreature entity with merge behavior
- [x] NanoAlloy ore worldgen
- [x] Life Essence leaf drops
- [x] NanoSuit (GeckoLib, full set bonuses)
- [x] Laser Sword with 10-hit combo system
- [x] Neural Flux HUD
- [x] Saber slash VFX (AAAParticles, per-swing direction)
- [x] Hit stop + screen shake
- [x] Combo counter HUD

### 🔄 In Progress
- [ ] Phase 4 cutscene — NanoCreature merges with player (GeckoLib assembly animation)
- [ ] Advanced abilities (Burst Strike, Seismic Slam, Afterburner)
- [ ] IRIS Protocol scanning system

### 🔮 Planned
- [ ] Second hero with unique origin
- [ ] Hero unlock system
- [ ] AVA Companion AI entity
- [ ] Advancement system
- [ ] Configuration GUI

---

## 🎨 Credits

**Development**: Texnobola  
**Libraries**: GeckoLib team, KosmX (PlayerAnimator), chloeprime (AAAParticles), Forge team

---

## 📄 License

All Rights Reserved. See [LICENSE](LICENSE) for details.  
Contact: pixeluzpro@gmail.com

## 💬 Community

- **Discord**: [discord.gg/your-invite](https://discord.gg/your-invite)
- **GitHub Issues**: [github.com/Texnobola/u-heroes/issues](https://github.com/Texnobola/u-heroes/issues)