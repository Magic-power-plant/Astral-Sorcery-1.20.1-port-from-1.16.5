# Astral Sorcery (1.20.1 Unofficial Educational Port)

**[English](README.md) | [简体中文](README_CN.md)**

---

> ### ⚠️ IMPORTANT LEGAL & COPYRIGHT NOTICE / 重要声明
>
> 1. **Educational Purpose Only**: This repository is an **unofficial, non-commercial educational project** created solely for studying Minecraft 1.20.1 (Forge) mod architecture and cross-version porting techniques from 1.16.5.
> 2. **Original Ownership**: **Astral Sorcery** and all of its original source code, game design, models, textures, sounds, and intellectual property belong exclusively to the original authors:
>    - **HellFirePvP** (Code & Design)
>    - **wiiv** (Models & Textures)
>    - **VulpTheHorseDog** (Models & Textures)
> 3. **NO DISTRIBUTION OF BUILD ARTIFACTS**: In strict accordance with the [Original License](./LICENSE), **it is EXPRESSLY FORBIDDEN to distribute, publish, upload, or share any compiled binary/build artifacts (`.jar` files) or derivative works of this project in any form** (including but not limited to CurseForge, Modrinth, forums, social groups, or public modpacks) **without explicit prior written permission from the original author (HellFirePvP)**. Any local compilation is strictly restricted to private, personal, and educational use.

---

## ✨ About Astral Sorcery

**Astral Sorcery** is a classic magic mod for Minecraft Forge focused on harnessing the power of **starlight** and the **constellations**.

Look up at the night sky, chart the stars through telescopes, uncover ancient marble shrines hidden across the world, and channel cosmic energy to craft celestial tools, construct grand multi-block altars, attune yourself to constellations, and unlock a deep skill tree of astral perks.

---

## 🌌 Core Gameplay Features

### 🔭 1. Constellations & The Night Sky
- **16 Unique Constellations**: Discover Bright (Major), Faint (Minor), and Dim constellations—such as *Aevitas*, *Discidia*, *Armara*, *Vicio*, *Evorsio*, and *Horologium*—each governing distinct aspects of nature, combat, defense, movement, and time.
- **Stargazing**: Use the **Looking Glass** and **Telescope** at night to trace constellation patterns in the sky and record your findings in the **Astral Tome**.

### 🏛️ 2. Multi-Block Structures & Starlight Crafting
- **Four Tiers of Crafting Altars**: Progress from the initial **Luminous Crafting Table (Discovery Altar)** to the **Starlight Crafting Altar (Attunement)**, **Celestial Altar (Constellation)**, and the ultimate **Iridescent Altar (Radiance)**, supported by magnificent marble structures.
- **Starlight Infuser & Lightwell**: Liquefy starlight from Aquamarine and Rock Crystals in the **Lightwell**, and infuse items with pure Liquid Starlight in the **Starlight Infuser**.
- **Starlight Network**: Link **Collector Crystals** to **Prism Lenses**, **Lenses** (Ignition, Break, Growth, Push, etc.), and altars to build complex starlight transmission networks.

### 🌟 3. Attunement, Rituals & The Perk Tree
- **Player Attunement**: Build the **Attunement Altar** beneath the open sky to attune your soul to a discovered constellation.
- **Astral Perk Tree**: Gain Perk Experience by performing actions aligned with your attuned constellation. Unlock nodes across an expansive perk web, socket **Perk Gems**, and seal or customize passive bonuses.
- **Ritual Pedestal**: Attune Rock or Celestial Crystals to constellations and place them on a **Ritual Pedestal** to project powerful area-of-effect blessings—from regenerating life and repelling monsters to accelerating crop growth or halting time.

### 💎 4. Crystal Mechanics & Astral Artifacts
- **Dynamic Crystal Attributes**: Grow, split, and refine **Rock Crystals** and **Celestial Crystals** with customizable properties (*Size*, *Purity*, *Shape*, *Tools*, and *Constellation Focus*).
- **Wands & Mantles**: Wield the **Resonating Wand**, **Illumination Wand**, **Conversion Wand**, **Architect's Wand**, **Grappling Wand**, and don the **Mantle of the Stars** attuned to your chosen constellation.
- **Refraction Table & Celestial Gateway**: Etch constellation-infused enchantments and potion effects onto items using the **Refraction Table**, or travel across dimensions and vast distances via the **Celestial Gateway**.

---

## 🚀 Getting Started (Player Guide)

1. **Explore the World**: Search for white **Marble Shrines** (Large, Small, or Desert shrines) generated across the world, and mine **Rock Crystal Ore** deep underground near bedrock (easily located by holding a **Resonating Wand** at night to see glowing starlight plumes).
2. **Craft the Astral Tome & Resonating Wand**: Locate a floating Collector Crystal inside a Large Ancient Shrine. Place a vanilla Crafting Table underneath its starlight beam and drop a Book or craft your first **Luminous Crafting Table** and **Resonating Wand**.
3. **Follow the Astral Tome**: Open the **Astral Tome** (`Right-Click` while holding it) to browse interactive research chapters, 3D multi-block previews, and step-by-step progression guides.

---

## 🛠️ Environment & Compatibility (1.20.1 Port)

| Requirement / Integration | Version / Details |
| :--- | :--- |
| **Minecraft** | `1.20.1` |
| **Mod Loader** | Minecraft Forge `47.4.20+` (`[47,)`) |
| **Java** | **Java 17** or newer |
| **ObserverLib** | **Embedded / Built-in** (No standalone ObserverLib mod download is required in this 1.20.1 port) |
| **JEI (Just Enough Items)** | Supported (Recipe viewing & Altar recipe transfer) |
| **KubeJS** | Supported (Native recipe schemas for `altar`, `lightwell`, `infuser`, `liquid_interaction`, `block_transmutation`; see [`docs/kubejs/README.md`](docs/kubejs/README.md)) |
| **Curios API** | Optional (`[1.20.1-5.1.1,)`) |

---

## 💻 Local Evaluation (For Private & Educational Use Only)

Because **distributing pre-compiled `.jar` builds is strictly prohibited without the original author's permission**, anyone wishing to study or test this project privately must run or compile it locally from source:

1. Ensure **JDK 17** is installed and configured.
2. Clone the repository locally.
3. Launch the development client directly:
   ```bash
   ./gradlew runClient
   ```
   *(On Windows, use `.\gradlew.bat runClient`)*
4. Or compile for personal/private study only:
   ```bash
   ./gradlew build
   ```
   > **Reminder**: Do **NOT** upload, share, or distribute the generated `.jar` files under `build/libs/` to any third party or public platform.

---

## 📜 License & Credits

This project directly inherits the original license of **Astral Sorcery**. Please read the full [LICENSE](./LICENSE) file before interacting with this repository.

- **Original Code & Design**: [HellFirePvP](https://github.com/HellFirePvP)
- **Original Art, Models & Textures**: **wiiv**, **VulpTheHorseDog**, **HellFirePvP**
- **Official Upstream Repository**: [HellFirePvP/AstralSorcery](https://github.com/HellFirePvP/AstralSorcery)
- **Official CurseForge Page**: [Astral Sorcery on CurseForge](https://www.curseforge.com/minecraft/mc-mods/astral-sorcery)
- **Official Discord**: [Astral Sorcery Discord](https://discord.gg/q37VRcT)

**All rights reserved by the original authors.**
