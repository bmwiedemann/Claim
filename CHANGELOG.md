## [2.0.0-alpha.1] - 2026-04-26

### Added
- Comprehensive Protection System (blocks break/place, bucket usage, and farmland trample).
- Permission Modes (PRIVATE/PUBLIC) for fine-grained interaction control (chests, doors, buttons).
- Configuration System (`claim-common.toml`) with toggleable OP bypass settings.
- Item-based (configurable) position selection system.
- `/claim` command with support for selections and relative coordinates (~).
- JSON-based claim persistence (saves to `world/data/claim/claims.json`).
- networking system to sync claims from server to client.
* `/claim list` command with a custom 3D-styled management screen.
- Claim metadata system (trusted players, permission modes, color, and timestamps).
- Overlap protection to prevent players from claiming already occupied areas.
- Deep optimistic updates for instant configuration synchronization.
- Specialized Developer Testing rules (Creative/Dev bypass overrides).

### Fixed
- Overhauled Xaero's Map integration: fixed color bleeding and corrected ABGR/RGBA conversion.
- Improved 3D previews to support terrain elevation variations and strict Y-boundary clipping.
- Compacted Configuration UI to support all screen sizes and GUI scales.
- Synchronized Configuration UI borders and 3D previews with selected color presets.

### Changed
- Updated mod logo.
- Cleaned up source code and assets for a full project rewrite.
- Initialized alpha stage for version 2.0.0.