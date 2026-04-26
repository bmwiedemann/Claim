## [2.0.0-alpha.2] - 2026-04-26

### Added
- Optional **Claim Block Economy** system (Disabled by default).
- **Playtime Rewards**: Automatic claim block rewards granted for every hour of active play.
- **Dynamic UI**: Real-time display of available claim blocks in the management dashboard.
- **Auto-Save**: Periodic background saving for claims and player data.
- **Categorized Configuration**: Reorganized `claim-common.toml` into `[general]` and `[economy]` sections.

### Fixed
- Fixed area selection logic to correctly calculate and enforce block costs.
- Improved persistence handling for multi-player server environments.