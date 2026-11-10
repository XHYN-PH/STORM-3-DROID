# R2.1 verification

## Startup regression

The real activity test reproduced R2's startup failure: `GameLoop.tick` called the private `NarutoActivity.nativeShaderPrepare` method, causing `IllegalAccessError` and closing the `:game` activity. The compile-only stub exposed the method as public, and the earlier UI tests never executed that game-start path.

R2.1 keeps native methods private and adds public static bridges inside their declaring activity. The loading monitor, statistics, touch reset, FPS toggle and brightness calls use those bridges. The original Send log link and listener are restored; the added diagnostics exporter remains absent.

## Checks

[Real startup comparison](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42747649314) passed: the exact published R2 APK reproduced `nativeShaderPrepare` access failure, while the corrected activity stayed alive and reached the expected native missing-game-file message. The published APK is re-signed with a temporary CI key only for installing the control alongside the test build; its runtime code is unchanged.

[Android validation](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42747649299) passed at normal and 130% text size. The corrected runtime source is commit `7b584b29043653969d09b2b141aacabd6298ed01`; later changes only refine tests and documentation.

- 4 linkage regression tests reject the published private-method call, private bridges and static/instance mismatches; a public bridge is accepted.
- 79 cross-package member references are checked against the real decoded APK, independently of compile-only stubs. This runs before APK assembly.
- 28 isolated save regression assertions preserve read-only export, active 100% protection, staged restore, backups, symlink/traversal guards and locking.
- 15 crop/configuration/display assertions and 70 native logging assertions under ASan/UBSan.
- Android interface validation covers both normal and 130% text size, restored Send log visibility/action, all five public native bridges, settings, save round trip, mapping and logging behavior.
- The real startup check launches the actual `NarutoActivity` in `:game`, confirms the native SDL main entry point, checks process survival and reaches the expected native missing-`default.xex` dialog without the Java startup exception. R1 is a control run. It uses an empty game fixture, not copyrighted game data. The game-menu UI/actions and all five bridge declarations are checked separately; actual in-game menu use needs game data.

The native engine executable/linkage sections, bundled 100% payload and original notices remain protected by the source checks. Original touch, mapping, SDL haptics and FPS-widget source remain unchanged.

## Limits

These checks cover the reproduced Java startup crash and actual menu/native call access. Full gameplay, save completion on exit, Xbox hardware and vendor-specific Adreno/Turnip behavior still need phone testing with game data. No complete regression-free gameplay or measured FPS gain is claimed.

## Signed hotfix

`STORM-3-R2.1-arm64.apk`: version R2.1 / code 49, package `com.naruto.storm3`, ARM64, Android 11+. SHA-256: `fe5c789e63b745e739887db80e921a4cc5ba56e5a5d0fe84b7aa6fab6a8c20a1`. Signature, 16 KB alignment and archive checks passed. The R1/R2 signing certificate is retained, allowing an update without uninstalling or clearing app data.
