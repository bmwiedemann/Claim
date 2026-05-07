## [2.0.0-beta1] - 2026-04-27

### Added
- Configurable maximum claims per player.
- Configurable maximum blocks per individual claim.
- Configurable maximum total blocks across all claims owned by a player.
- Claim entry/exit indicators (Action Bar, Title, and Chat notifications).
- Configuration option to automatically claim all Y levels (`claimAllY`).
- Color coding for claim status messages and indicators.
- OP bypass system for claim limits when `opBypass` is enabled.

### Changed
- Default claim limits set to unlimited (-1).
- Improved claim volume calculation to handle 3D space and world height normalization.