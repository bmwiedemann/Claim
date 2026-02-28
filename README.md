![Hero Banner](https://raw.githubusercontent.com/yigit-guven/Claim/refs/heads/1.20.1-forge/src/main/resources/logo.png)

# 🗺️ Claim: Your Land, Your Rules

**Claim** is a powerful yet lightweight land management mod for Minecraft Forge. It empowers players to secure their hard-earned progress by claiming chunks of the world, preventing griefing, and creating shared territories with friends.

---

## ✨ Key Features

- **📍 Chunk-Based Claiming**: Secure your territory one 16x16 chunk at a time. Simple, intuitive, and effective.
- **🛡️ Comprehensive Protection**: Keep your builds safe from block breaking, unauthorized interactions (chests, doors), and even explosions.
- **🤝 Granular Trust System**: Cooperate with friends! Assign specific permission levels like `BUILD`, `INTERACT`, or `MANAGE`.
- **🚀 Instant Travel**: Give your claims unique names and teleport between them with the `/claim visit` command.
- **🏗️ Visual Tools**: Use the **Surveyor's Compass** to see exactly where your borders end and the wild begins.
- **📜 Professional Management**: Use **Land Permits** to formalize your ownership and rename your territories.

---

## 🛠️ How to Get Started

### 1. Secure Your First Chunk
Stand in the area you want to claim and type:
```bash
/claim [OptionalName]
```
Alternatively, use a **Land Permit** item to claim the land you're standing on.

### 2. Manage Your Territory
- **Info**: `/claim info` - See who owns the chunk you're in.
- **List**: `/claim list` - View all your active claims.
- **Name**: `/claim rename <NewName>` - Give your claim a unique identity (requires a Land Permit).

### 3. Share with Friends
Trust another player to allow them to help you build or access your chests:
```bash
/claim trust <PlayerName> <Level>
```
*Levels: `BUILD`, `INTERACT`, `CONTAINERS`, `MANAGE`*

---

## ⚙️ Configuration

The mod can be extensively configured via the `serverconfig/claim-server.toml` file in your world directory.

### 🏠 General Settings
| Option | Description | Default |
| :--- | :--- | :--- |
| `maxClaims` | Maximum chunks a player can claim **per dimension**. | `10` |
| `protectBlocks` | Prevents block breaking/placing for non-trusted players. | `true` |
| `protectInteract` | Prevents interacting with blocks (doors, buttons, chests). | `true` |
| `protectExplosions` | Makes claimed chunks immune to explosion damage. | `true` |
| `enableActionBarNotifications` | Shows enter/leave messages above the hotbar. | `true` |
| `requireNameOnClaim` | Forces players to provide a name when claiming. | `false` |

### 🛠️ Claiming Methods
| Option | Description | Default |
| :--- | :--- | :--- |
| `useItems` | Allow players to use **Land Permits** for claiming. | `true` |
| `useCommands` | Allow players to use `/claim` command. | `true` |

### 🚀 Naming & Visiting
| Option | Description | Default |
| :--- | :--- | :--- |
| `uniqueNames` | If true, claim names must be unique globally. | `true` |
| `caseSensitive` | Whether name checks distinguish between upper/lower case. | `false` |
| `enableVisit` | Enables the `/claim visit <name>` command. | `true` |
| `tpCooldown` | Seconds a player must stand still before teleporting. | `3` |
| `cancelOnMove` | Cancels teleport if the player moves. | `true` |
| `cancelOnDamage` | Cancels teleport if the player takes damage. | `true` |

---

## 📥 Downloads & Support

- **Modrinth**: [Download on Modrinth](https://modrinth.com/mod/claim)
- **CurseForge**: [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/claim)
- **Bug Reports**: Found an issue? Report it on our [GitHub Issue Tracker](https://github.com/yigit-guven/Claim/issues).
- **Suggestions**: Have an idea for a feature? Create a suggestion via [GitHub Issue Tracker](https://github.com/yigit-guven/Claim/issues)
- **Discord**: [Join our community](https://discord.gg/aPk7Qs5d4H)
- [**Wiki**](https://github.com/yigit-guven/Claim/wiki)
- [**Source Code**](https://github.com/yigit-guven/Claim)

---

## 📜 License & Credits

- **Author**: [Yigit Guven](https://github.com/yigit-guven/)
- **License**: [GNU GPLv3](LICENSE)
- **Special Thanks**: Community and all our testers!

> **Pro Tip**: Keep a **Surveyor's Compass** in your hotbar to always stay aware of your territory boundaries!
