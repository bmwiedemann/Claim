<p align="center">
  <a href="https://github.com/yigit-guven/Claim">
    <img src="https://raw.githubusercontent.com/yigit-guven/Claim/9d066da2a0d31a3442bdabc085a4e936cb484cbd/src/main/resources/logo.png" alt="Claim Logo" width="400"/>
  </a>
</p>

# Claim

Claim is a land management system for Minecraft designed for territory security and administration. It provides a robust protection system, a 3D management interface, and deep integration with Xaero's World Map.

## Features

- **Territory Security**: Prevents unauthorized block modifications (break/place), fluid manipulation (buckets), and crop trampling.
- **Access Control**: Configurable permission modes (PRIVATE/PUBLIC) for block interactions such as chests, doors, and buttons.
- **3D Management Interface**: Visual dashboard for managing claims, featuring real-time 3D projector previews with terrain elevation support.
- **Xaero's World Map Integration**: Automatic claim overlays on the world map with custom color support and foreign territory highlighting.
- **Granular Trust System**: Manage trusted players per-claim via a dedicated UI.
- **Administrative Control**: Server-wide configuration for Operator (OP) bypass and administrative overrides.

## Commands

| Command | Description |
| :--- | :--- |
| `/claim` | Claims the area selected with the selection tool. Supports relative coordinates. |
| `/claim list` | Opens the graphical management dashboard for your claims. |

## Configuration

Configuration settings are located in `config/claim-common.toml`:

- **selectionTool**: The item ID used for area selection (Default: `minecraft:wooden_shovel`).
- **opBypass**: Toggles whether server operators bypass claim protections (Default: `true`).

## Protection Logic

- **Modifications**: Block breaking, placing, and fluid usage are restricted to the owner and trusted players.
- **Interactions**:
  - **PRIVATE**: Restricted to owner and trusted players.
  - **PUBLIC**: Accessible to all players.

## Technical Integration

- **[Xaero's World Map](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map)**: Fully compatible with high-performance claim overlays. Available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) and [Modrinth](https://modrinth.com/mod/xaeros-world-map).
- **Persistence**: Claims are stored in `world/data/claim/claims.json` for easy backup and management.

---
© 2026 Yigit Guven. Licensed under GNU GPLv3.
