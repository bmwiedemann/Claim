## [2.0.0-beta2] - 2026-05-08

### Added
- Lockable settings for claim attributes (Name, Description, Permissions, Color, Trusted) to prevent player changes.
- Default value settings for new claims (Default Name, Description, Permissions, and Color).
- Server-side enforcement and verification for locked claim settings.
- New admin subcommands `/claim admin unclaim` and `/claim admin clear <player>` for operators.

### Changed
- Reorganized the configuration file into logical categories (General, Limits, Indicators, Defaults, Locks) for better usability.