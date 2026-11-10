# STORM 3 — R2.2 customization

Modified and Translated by **XHYN_PH**.

R2.1 fixes the game-start crash caused by inaccessible native methods. The original **Send log** link is restored; the added diagnostics exporter stays removed. Private engine methods are called through public bridges inside the activity, and real APK member access is checked before assembly.

R2 keeps the launcher layout and stock controller mapping screen, with a shared dark Material theme, rounded dialogs and gold accents. Added settings stay in one compact **More options** card.

## Included

- STORM 3 name and circular gold Leaf icon.
- Cached stock touch-button artwork, redundant redraw suppression and conservative transparency-layer bounds. Input mappings, layout and macro timings remain original.
- Smaller stock FPS/statistics panel with a saved 50–125% size slider in Display and Game options. Memory sampling and label formatting run off the UI thread.
- Supported native early logging cutoff when Logging is off, in addition to output suppression. Original Send log remains available.
- The added Performance presets menu is removed; current graphics selections are preserved.
- Auto refresh chooses a display mode suited to generated output, preferring a lower matching refresh over a higher one. Manual refresh selections remain available.
- Hidden FPS statistics no longer trigger native statistics or memory polling. Completed loading preparation stops polling. Game pause/destruction cancels polling and resume restarts only needed tasks.
- Launcher ambient/logo animation pauses while the launcher is hidden. Drawing loops stop scheduling when hidden, and animation resumes when the launcher returns.
- Original cached SDL motion ranges and original axis behavior, with allocation-free joystick lookup. No added calibration per axis/event.
- Rounded in-game options: Fit / Stretch / Crop, Show FPS, touch visibility, touch with controller, opacity, brightness, touch-layout editing, menu-button visibility, and confirmed return to launcher.
- Proportional fullscreen crop with adjustable amount; original Fit and Stretch remain.
- Opening-video preference, gamma, shadow cap, GPU-memory and streaming-cache choices; overrides run after the stock config writer.
- Save ZIP export/share and backup create/export/restore/delete; driver information and system-driver reset.

Added rumble, invisible right stick, axis calibration, controller preview/reset, performance history and diagnostics export have been removed. Touch input logic, controller mappings and SDL haptics retain the R1 behavior. The stock FPS panel keeps its appearance and gestures, with the requested size and sampling changes. The original controller binding list still includes its stock L3/R3 bindings.

The game menu applies only controls supported at runtime. Settings in the launcher apply on the next game launch. Reopen the launcher for the opening-video preference.

## Returning to the launcher

Wait for the game's save indicator to finish, then select **Return to launcher** and confirm. Input is released, polling stops, and Android/SDL performs its normal activity and native shutdown. The save lock is released after shutdown before the launcher is opened. This does not force-kill the game process or create a manual save; unsaved progress can be lost.

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
  --output /path/to/STORM-3-R2.2-arm64.apk
```

`compile_runtime.py` compiles the added Java classes using signature-only stubs for upstream classes. Those stubs are excluded from the DEX. Apktool disassembles the runtime and `customize.py` applies checked hooks to the translated base. All engine executable sections, bundled data and notices remain protected by source verification. R1 builds keep their original defaults unless `--r2` is explicitly supplied.

## Testing limits

Actual FPS gains depend on the device, scene and selected driver. Presets reduce graphics work; the engine stays binary-only and no 60 FPS gameplay unlock is claimed. Crop fills the display by cropping the image; it does not widen the game camera/HUD.

See [TESTING.md](TESTING.md) for the exact automated coverage and outstanding device checks. Gameplay shutdown/save completion, Xbox behavior and physical Adreno/Turnip presentation require game-data testing on a phone.

Original R2 Java, scripts and tests are MIT-licensed; upstream rights and notices remain unchanged.

See [performance implementation notes](PERFORMANCE-NOTES.md) for native logging evidence and the remaining device-trace requirement.
