# U-Heroes

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.2.0-orange.svg)](https://files.minecraftforge.net/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)]()

A hero-based combat mod for Minecraft 1.20.1 Forge. Choose your hero, master their unique abilities, and become a legend!

## 🦸 About

U-Heroes introduces a revolutionary hero system to Minecraft, where each hero comes with:
- **Unique Nano-Tech Suit** - Advanced armor with special abilities
- **Signature Weapons** - Powerful tools with multiple modes
- **Neural Flux System** - Bio-electric energy powering all abilities
- **Companion AI** - Your personal assistant (Coming Soon)

## ⚡ Features

### Neural Flux System
- **Flux Meter** - Track your energy in real-time with an elegant HUD
- **Flux Overcharge** - Push beyond your limits for devastating power
- **Flux Burnout** - Manage your energy carefully or face consequences
- **Regeneration** - Energy naturally recovers over time

### NanoTech Hero (First Hero)

#### 🛡️ Nano-Tech Suit
Full set of advanced armor with progressive bonuses:
- **2 Pieces**: Speed I
- **3 Pieces**: Speed I + Strength I
- **4 Pieces**: Speed I + Strength II + Self-Repair + Adaptive Regeneration + Flux Amplification

**Stats:**
- Defense: 24 (Diamond-tier protection)
- Toughness: 3.5
- Knockback Resistance: 20%
- Durability: 2,475 total (self-repairing)

#### ⚔️ Laser Sword
Switchable weapon with two combat modes:

**LASER Mode** (Default)
- Cyan energy blade
- Applies Weakness I to enemies
- Electric spark particle effects
- Base damage: 10

**INFERNO EDGE Mode**
- Orange flame blade
- Sets enemies on fire (5 seconds)
- +5 bonus fire damage
- Flame particle effects

*Switch modes: Crouch + Right-Click (costs 5 Flux)*

### 🎮 Commands
Debug commands for testing (OP level 2):
- `/uh_flux get` - Check your current flux
- `/uh_flux set <amount>` - Set flux amount
- `/uh_flux setmax <amount>` - Set max flux capacity
- `/uh_flux consume <amount>` - Consume flux
- `/uh_flux fill` - Fill flux to maximum
- `/uh_flux regen <rate>` - Set regeneration rate

## 🌍 Language Support

U-Heroes is fully bilingual:
- 🇺🇸 **English** (en_us)
- 🇺🇿 **Uzbek** (uz_uz)

All text, tooltips, and messages are available in both languages!

## 📦 Dependencies

### Required
- [GeckoLib 4](https://www.curseforge.com/minecraft/mc-mods/geckolib) (4.7.3+) - 3D animations
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) (5.9.1+) - Equipment slots
- [Player Animator](https://www.curseforge.com/minecraft/mc-mods/playeranimator) (1.0.2-rc1+) - Player animations
- [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) (11.1.136+) - Configuration GUI

### Optional
- [Cutscene API](https://www.curseforge.com/minecraft/mc-mods/cutscene-api) (1.6.4+) - Cinematic scenes

## 🚀 Installation

1. Install Minecraft Forge 47.2.0 or higher for Minecraft 1.20.1
2. Download and install all required dependencies
3. Download U-Heroes mod JAR file
4. Place the JAR file in your `mods` folder
5. Launch Minecraft and enjoy!

## 🛠️ For Developers

### Building from Source

```bash
git clone https://github.com/yourusername/u-heroes.git
cd u-heroes
gradlew build
```

The compiled JAR will be in `build/libs/`

### Project Structure
```
com.uheroes.mod
├── core/              # Core systems (Flux, networking, commands)
├── init/              # Registration classes
├── event/             # Event handlers
├── client/            # Client-side code (HUD, renderers, particles)
├── heroes/            # Hero implementations
│   └── nanotech/      # NanoTech hero (armor, weapons, abilities)
└── UHeroesMod.java    # Main mod class
```

### Technical Details
- **Java Version**: 17
- **Mappings**: Official (1.20.1)
- **Architecture**: Capability-based player data, packet-based networking
- **Multiplayer**: Fully compatible with proper client-server sync

## 📋 Roadmap

### ✅ Completed
- [x] Neural Flux energy system
- [x] Flux HUD overlay
- [x] NanoTech Armor with GeckoLib models
- [x] Laser Sword with dual modes
- [x] Bilingual support (English/Uzbek)
- [x] Debug commands
- [x] Multiplayer compatibility

### 🔄 In Progress
- [ ] AVA Companion AI entity
- [ ] Advanced abilities system
- [ ] Burst Strike (rocket-boosted punch)
- [ ] Seismic Slam (ground pound)
- [ ] Afterburner (forward boost)
- [ ] IRIS Protocol (scanning system)

### 🔮 Planned
- [ ] Additional heroes with unique playstyles
- [ ] Hero unlock system
- [ ] Cutscene integration
- [ ] Configuration GUI
- [ ] Keybind customization
- [ ] Particle effects system
- [ ] Sound effects
- [ ] Advancement system
- [ ] Loot tables and crafting recipes

## 🎨 Credits

**Mod Development**: [Your Name]
**3D Models**: [Model Artist Name]
**Textures**: [Texture Artist Name]

### Special Thanks
- GeckoLib team for the animation library
- Forge team for the modding framework
- Minecraft modding community

## 📄 License

All Rights Reserved. This mod is proprietary software.

## 🐛 Bug Reports

Found a bug? Please report it on our [Issue Tracker](https://github.com/yourusername/u-heroes/issues)

## 💬 Community

- **Discord**: [Join our server](#)
- **CurseForge**: [Download page](#)
- **Modrinth**: [Download page](#)

---

**Made with ❤️ for the Minecraft community**
