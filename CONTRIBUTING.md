# Contributing to U-Heroes

## 🚫 Current Status

U-Heroes is in active closed development. External code contributions are not accepted yet, but bug reports and feature suggestions are always welcome.

## 🐛 Bug Reports

1. Check [existing issues](https://github.com/Texnobola/u-heroes/issues) first
2. Create a new issue with:
   - Minecraft version: 1.20.1 / Forge 47.4.10
   - Mod version
   - Steps to reproduce
   - Expected vs actual behavior
   - Crash log or `latest.log` from `.minecraft/logs/`
   - Screenshots or video if relevant

## 💡 Feature Requests

1. Check existing issues to avoid duplicates
2. Open an issue with the `enhancement` label
3. Describe the feature and how it fits the U-Heroes power fantasy theme

## 📋 Code Standards (for future contributors)

### Java
- Java 17, Forge 1.20.1 conventions
- Follow existing package structure (`init`, `heroes`, `core`, `client`, `origin`)
- DeferredRegister for all registrations
- Client-only code inside `@OnlyIn(Dist.CLIENT)` or client packages
- Use `ForgeRegistries` lookups instead of hardcoded field references

### Assets
- GeckoLib models: arm bones must be separate from body bones
- Textures: `textures/entity/armor/` for entity textures
- Sounds: `.ogg` format, registered via `ModSounds`

### Commits
- Start with a verb: `Add`, `Fix`, `Update`, `Remove`, `Refactor`
- Example: `Fix saber slash direction not syncing with animation`
- Reference issues when applicable: `Fix #12`

## 📞 Contact

- GitHub Issues: [github.com/Texnobola/u-heroes/issues](https://github.com/Texnobola/u-heroes/issues)
- Discord:  https://discord.gg/MyKBmdXY
- Email: pixeluzpro@gmail.com