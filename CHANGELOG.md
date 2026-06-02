## Changelog

## [2.0.0-beta.2] - 2026-06-02

### Added
- New optional land permit system for claim creation with config-driven item and amount requirements.
- New config option to consume land permits on successful claim creation (`consumeLandPermitOnUse`) for economy-focused servers.
- New `/claim unclaim` command to remove a single claim at the player's current position.
- New `/claim setvisit` command to set a per-claim teleport destination for visits.
- New `/claim visit <claimId>` command to teleport to a specific claim.
- New `/claim info` command to show the claim ID, name, and owner for the claim at the player's current position.
- New config option `visitOwnerOnly` to restrict `/claim visit` usage to claim owners.
- New per-claim `visitPos` metadata synced to clients.

### Changed
- Improved visit teleport safety by preferring configured claim visit position and falling back to a safer center search to reduce lava/unsafe teleports.
- Added optional suppression for entry/exit indicator spam when moving between adjacent claims with the same owner and name (`suppressSameOwnerNameTransitions`).
- Localized new and updated command/event messages using translation keys in `en_us.json`.
- Updated selection, visit, unclaim, and claim limit/permit feedback to use translatable chat/actionbar components.

### Fixed
- Fixed a `/claim visit` bug where teleporting to an unloaded claim could resolve to a very low Y level (for example around `-63`) and cause suffocation or unsafe spawns.

### Technical
- Extended claim sync payload to include optional visit position data.
- Added claim lookup and visit utility APIs in `ClaimManager` for command/event reuse.