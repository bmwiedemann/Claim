## Changelog

## [2.0.0-beta.2] - 2026-06-02

### Added
- New optional land permit system for claim creation with config-driven item and amount requirements.
- New config option to consume land permits on successful claim creation (`consumeLandPermitOnUse`) for economy-focused servers.
- Added craftable `claim:land_permit` item and survival recipe support.
- Added craftable `claim:claim_compass` item that opens the claim list on right-click.
- New `/claim unclaim` command to remove a single claim at the player's current position.
- New `/claim setvisit` command to set a per-claim teleport destination for visits.
- New `/claim visit <claimId>` command to teleport to a specific claim.
- New `/claim info` command to show the claim ID, name, and owner for the claim at the player's current position.
- New `/claim trustall <player>` and `/claim untrustall <player>` commands to manage trust across all owned claims at once.
- New admin utility commands `/claim admin givepermit [player] [amount]` and `/claim admin givecompass [player] [amount]` for quick setup/testing.
- New config option `visitOwnerOnly` to restrict `/claim visit` usage to claim owners.
- New per-claim `visitPos` metadata synced to clients.
- Added recipe advancement unlocks for land permit and claim compass so recipes naturally appear when players obtain key ingredients.

### Changed
- Improved visit teleport safety by preferring configured claim visit position and falling back to a safer center search to reduce lava/unsafe teleports.
- Added optional suppression for entry/exit indicator spam when moving between adjacent claims with the same owner and name (`suppressSameOwnerNameTransitions`).
- Localized new and updated command/event messages using translation keys in `en_us.json`.
- Updated selection, visit, unclaim, and claim limit/permit feedback to use translatable chat/actionbar components.
- Updated default permit item config to `claim:land_permit` for first-party survival progression.

### Fixed
- Fixed a `/claim visit` bug where teleporting to an unloaded claim could resolve to a very low Y level (for example around `-63`) and cause suffocation or unsafe spawns.

### Technical
- Extended claim sync payload to include optional visit position data.
- Added claim lookup and visit utility APIs in `ClaimManager` for command/event reuse.