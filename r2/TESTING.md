# R2.2 verification

Runtime source: `34105f2edba4c1a8cf697c4670eceb6df4ca6029`. Later README, logo and changelog updates do not alter the APK.

- [Android interface run](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42769548812) passed: normal and 130% text size, hardware Canvas comparisons of cached and original touch artwork, stock press/cancel delivery, unchanged-layout editor return, asynchronous statistics publication/cancellation, panel scale/clamping/collapse and existing menu/save/logging checks.
- [Real game-activity startup](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42768745165): the final touch-rendering runtime reaches native SDL and the expected missing-game-file dialog. The subsequent native helper change adds only a private same-ABI logging-policy reader for integration checks; it does not change the game's native library or startup hooks.
- [Native helper integration](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42769548707) and [source checks](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42769548932).
- 85 ASan/UBSan native logging assertions, 28 isolated save assertions, 15 display/configuration assertions and 4 linkage-regression tests.
- 191 real APK cross-package member references checked before assembly. The preflight additionally compares stock touch input/editor methods and panel gestures against the verified R1 reference.

The early logging threshold is read inside the ARM helper used by the engine. Reading it through the emulator's host Java libc produced a misleading failure because the native bridge can maintain a separate environment. The integration check now reads the same ABI that sets the threshold; host sanitizer tests also verify cutoff, repeated application and restoration of an existing/absent override.

`STORM-3-R2.2-arm64.apk`: version R2.2 / code 50, Android 11+, ARM64, package `com.naruto.storm3`. SHA-256: `781431866bea98084b4892348a1bb055be6d3dd44ce8dc6e2ce35f02426b538b`. Signing, 16 KB alignment and archive checks pass. The existing signing certificate is retained for installation over R1/R2/R2.1. The native game library and bundled 100% save payload are byte-for-byte identical to R2.1.

No actual game files or affected phone are available in the cloud environment. These checks do not measure gameplay FPS, test Xbox hardware, or attribute the intermittent native frame stalls. See [performance notes](PERFORMANCE-NOTES.md) and the developer-only local trace collector for that remaining investigation. No in-app diagnostics exporter or performance history has been reintroduced.

---

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
