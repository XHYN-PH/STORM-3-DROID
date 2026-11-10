# R2 verification

## Android runtime

[Final Android validation](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42738642598) passed with **62 assertions at each of 100% and 130% font scale**, plus the six Java/SDL logcat On/Off marker checks in each run. The validated runtime tree is recorded at `d99229df7f0996cd8e3446ea5f1f975c7403b466` and matches the CI revision.

Coverage includes launcher/settings installation, app name and circular icon, readable Display dialog actions at both text sizes, removed runtime classes, expanded game-menu controls and action dispatch, exit confirmation/cancellation, preset persistence, polling cancellation, save-lock ordering, joystick lookup/removal, restored mapping selection/cancellation, native library/logging-hook loading and typed game-method verification. Android save fixtures verify read-only export, 100% protections, staged restore and exported ZIP compatibility with the unchanged stock importer.

Menu tests use the live launcher with an injected action receiver. Shutdown ordering uses a simulated SDL thread and a real Android file lock. These test the menu and lock behavior without claiming to run game data or complete a real game save.

## Source and host checks

- 28 isolated save regression assertions: read-only export, active 100% protection, staged restore, pre-restore backup, symlink/traversal and locking checks.
- 15 crop/configuration/display-rate assertions: uniform scaling, settings preservation/replacement, automatic output matching and invalid rates.
- 70 native logging assertions under ASan/UBSan.
- Added Java compiles, converts to DEX and disassembles. Signature-only stubs are excluded from runtime DEX.
- Engine executable/linkage sections, bundled data and license bodies are unchanged.
- Original touch view, mapping activity, SDL haptics and FPS widget are byte-identical to R1 decoded source.
- Smali branch targets, register bounds, English config enforcement, footer, updater, manifest and ARM64 logging helper are checked before APK assembly.

## Appearance review

Both font scales were checked using Android screenshots. Review caught a clipped Display footer; its explanation now scrolls with the content and a footer-bounds assertion guards against recurrence. Game options keep Resume and Return to launcher visible below scrolling settings. The original mapping grid is restored.

## Device testing still needed

Full gameplay, native shutdown and save completion, physical Xbox behavior, brightness changes, Adreno/Turnip presentation/crop and measured FPS gains require game-data testing on a phone. CI has no game data or physical Xbox/Adreno hardware. No complete regression-free gameplay, manual save on exit, true ultrawide camera or 60 FPS gameplay unlock is claimed.

## Signed candidate

`STORM-3-R2-arm64.apk` SHA-256: `abe7553daf00f2c9c2ec92b2f1a98198025c974e8dd9cfe1bb24b13bab2f23e3`. Package `com.naruto.storm3`, version R2 / code 48, ARM64, Android 11+. The R1 signing certificate is retained; APK signature, 16 KB alignment and ZIP integrity checks passed. The exact release-signed APK was rebuilt locally from the tested runtime source; it was not installed on physical hardware.
