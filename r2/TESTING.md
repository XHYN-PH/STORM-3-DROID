# R2 verification

The refined build is undergoing Android validation. Historical results for the earlier candidate do not establish correctness of this revision.

## Source and host checks

- 28 isolated save regression assertions: read-only export, active 100% protection, staged restore, pre-restore backup, symlink/traversal and locking checks.
- 15 crop/configuration/display-rate assertions: uniform scaling, settings preservation/replacement, automatic output matching and invalid rates.
- Added Java compiles, converts to DEX and disassembles. Signature-only stubs are excluded from runtime DEX.
- Engine executable/linkage sections, bundled data and license bodies are unchanged.
- Original touch view, mapping activity, SDL haptics and FPS widget are byte-identical to R1 decoded source.
- Smali branch targets, register bounds, English config enforcement, footer, updater, manifest and ARM64 logging helper are checked before APK assembly.

## Android checks

Pending: refined launcher/display/game-menu appearance and interactions, cancelled/confirmed exit actions, stock mapping list, new presets, native loading/logging markers, save round trip and original-importer compatibility.

Menu tests use the live launcher with an injected action receiver; they test menu layout and dispatch without claiming to run game data. Native game shutdown and save completion need physical gameplay testing. No game data or physical Xbox/Adreno hardware is available in CI.

No measured FPS improvement or complete regression-free gameplay is claimed from source/UI checks alone.
