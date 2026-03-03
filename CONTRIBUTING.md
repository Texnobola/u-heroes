# Contributing to U-Heroes

Thank you for your interest in contributing to U-Heroes! This document provides guidelines for contributing to the project.

## 🚫 Current Status

**U-Heroes is currently in closed development.** We are not accepting external contributions at this time.

## 🐛 Bug Reports

If you find a bug, please report it by:

1. Checking if the issue already exists in our [Issue Tracker](https://github.com/yourusername/u-heroes/issues)
2. Creating a new issue with:
   - Clear title describing the bug
   - Minecraft version and mod version
   - Steps to reproduce
   - Expected vs actual behavior
   - Crash logs (if applicable)
   - Screenshots/videos (if relevant)

## 💡 Feature Requests

We welcome feature suggestions! Please:

1. Check existing issues to avoid duplicates
2. Create a new issue with the `enhancement` label
3. Describe the feature clearly
4. Explain why it would benefit the mod
5. Consider how it fits with existing features

## 📋 Code Standards

For future reference when contributions are accepted:

### Java Code
- Use Java 17 features where appropriate
- Follow existing package structure
- Document all public methods with Javadoc
- Use meaningful variable and method names
- No magic numbers - use constants
- Proper null checks and error handling

### Minecraft/Forge
- Use DeferredRegister for all registrations
- Client code only in client packages
- Server logic only on server side
- Proper packet sync for multiplayer
- Use Component.translatable() for all text

### Localization
- ALL text must have entries in both en_us.json AND uz_uz.json
- Use descriptive translation keys
- Follow existing key naming patterns

### Commit Messages
- Use clear, descriptive commit messages
- Start with verb (Add, Fix, Update, Remove)
- Reference issue numbers when applicable

## 🔒 Code of Conduct

- Be respectful and constructive
- No harassment or discrimination
- Keep discussions on-topic
- Help create a welcoming community

## 📞 Contact

For questions or discussions:
- Open an issue on GitHub
- Join our Discord server (link in README)
- Email: [your-email@example.com]

---

**Note**: These guidelines may change as the project evolves. Check back regularly for updates.
