# STORM 3 — R2 customization

Modified and Translated by **XHYN_PH**.

R2 keeps the stock launcher and adds one compact **More options** card. Its dialogs reuse the original cards, animated switches and button styling. The existing controller mapping screen gains a controller diagram, live input feedback, reset actions and a switch back to the original list.

## Included

- STORM 3 app name and circular gold Leaf icon.
- Invisible floating right stick: touch an unused part of the right half to establish its origin, then drag. Buttons win hit testing. Fingers retain their roles across the center; releasing/cancelling returns the stick to neutral.
- Global controller dead zones, right-Y inversion and right-axis-pair selection; live stick/button/trigger feedback and R3 mapping/reset in the existing controller screen.
- SDL feedback routing to phone/controller, adjustable strength, stop handling and a motor-test action.
- Proportional fullscreen crop, with adjustable amount; original Fit and Stretch remain.
- Opening-video preference, gamma control, shadow cap presets and Auto/manual GPU-memory/streaming-cache choices. The configuration writer applies these after generating stock defaults.
- Save ZIP export/share, backup creation, export, restore and deletion.
- Driver information/system-driver reset and on-demand diagnostics export. When logging is off, diagnostics does not collect logs.
- Optional game/output FPS history. Frame-time values are **estimates from the engine's one-second FPS samples**, not individual Vulkan frame timings. Missing generated-output measurements remain unknown.

Controller profiles, settings backup, shader-prewarming controls and touch-layout profiles are excluded as requested. Existing features are retained.

Game-start settings apply on the next game launch. Reopen the launcher to apply the opening-video preference. The controller tester shows raw device values; calibration is applied to the game input path.

## Save protection

The bundled `assets/save100` payload is verified unchanged. New save operations never modify `save100-original` or `save100.active`. Export reads the active save; it does not enable/disable the 100% save. Backup restore is blocked while the 100% save is active. The native game process and save manager share an OS file lock; save operations require closing the game. Restoration copies to a staging directory and preserves the current save as a normal backup before swapping. Backup selection and deletion reject traversal and symlinks.

## Build and checks

Use the same verified dependencies and upstream APK as [R1](../r1/DEPENDENCIES.md), plus Android API 30's `android.jar`.

```sh
python3 r2/preflight.py \
  --android-jar /path/to/android.jar \
  --build-tools /path/to/build-tools/35.0.0 \
  --upstream /path/to/upstream-v0.6.16.apk \
  --apktool /path/to/apktool_3.0.3.jar

python3 r1/build_r1.py --r2 \
  --android-jar /path/to/android.jar \
  --upstream /path/to/upstream-v0.6.16.apk \
  --apktool /path/to/apktool_3.0.3.jar \
  --ndk /path/to/android-ndk-r28c \
  --build-tools /path/to/build-tools/35.0.0 \
  --keystore /private/path/release.p12 \
  --password-file /private/path/keystore.pass \
  --output /path/to/STORM-3-R2-arm64.apk
```

`compile_runtime.py` compiles the added Java classes using signature-only stubs for upstream classes. Those stubs are excluded from the DEX. Apktool disassembles the runtime and `customize.py` applies checked hooks to the translated base. All engine executable sections, bundled data and notices remain protected by source verification. R1 builds keep their original defaults unless `--r2` is explicitly supplied.

## Investigation and testing limits

PT4Droid's touch implementation was reviewed as requested. Its native camera-delta interface is game-specific; STORM uses its existing gamepad right-axis bridge instead. Genuine ultrawide rendering was not established: presentation aspect/crop settings do not change the game's camera or HUD. R2 supplies crop rather than advertising an unsupported expanded camera view.

The stock Xbox axis/R3 event path exists. R2 adds calibration, selectable axis sources, a live tester and mapping resets, but physical Xbox compatibility and actual gameplay-triggered rumble still require a phone/controller and game-data test. The rumble test confirms motor access separately from the engine producing rumble events. See [TESTING.md](TESTING.md) for completed checks and outstanding results.

The engine remains binary-only. Original R2 Java, scripts and tests are MIT-licensed; upstream rights and notices remain unchanged.
