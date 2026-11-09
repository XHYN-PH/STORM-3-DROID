# Hidden settings audit — Android v0.6.16

This audit was prepared against the original upstream APK before the R1 build. Reproducible customization inputs are in this directory. See `TESTING.md` for current build and Android validation results. R1 adds a Logging switch, removes the PT-BR control and forces English. Other audited candidates below remain unexposed.

Inspected the upstream release APK, package `com.naruto.storm3`, version code 46. Original SHA-256: `15fcff17b64b29de84a205ea763cef71172f1086d61981c94b0d42f6257c9ecf`.

## Best candidates to expose

These have direct evidence beyond a string appearing in the binary. Device testing is still needed to establish their practical effect.

| Candidate | Current behavior and evidence | Implementation needed |
|---|---|---|
| Menu video on/off | LauncherActivity reads the boolean intent extra `trailer`, default `true`. No visible toggle is provided. This controls the animated opening-video panel. | Add a saved preference and a switch. This is the smallest change. |
| Brightness / gamma | Launcher always writes `xnr.renut_brightness_gamma = 1.0`. Native code registers the corresponding floating-point setting with default 1.0 at address `0x232fd9c`. The README mentions brightness, but this APK has no brightness control. | Add a bounded slider/presets and stop forcing 1.0. Validate image changes on a device. |
| Shadow resolution | Launcher forces `xnr.szr_shadow_map_max_height = 720`. Native code registers both `szr_shadow_map_max_height` and `szr_shadow_map_min_height`. Its error message lists 360–2160 as render-height bounds; not every integer in that range is necessarily valid. | Add explicit supported quality presets. Keep performance-mode behavior intact. Test cost and visual correctness. |
| GPU memory budget | `xnr.gpu_budget_mb` is chosen automatically from total RAM: 900 MB up to 4608 MB RAM; 1400 MB up to 6656 MB RAM; 0 above that or if RAM is unknown. Native code reads `XNR_GPU_BUDGET_MB` and converts MB to bytes (`0x230d364`). A zero value must be treated as the existing automatic/default behavior, not proof of unlimited usable memory. | Add an Auto/manual expert setting with bounds; change the launcher's generated value. More memory is not inherently faster. |
| Streaming cache size | Launcher writes `xnr.banjo_renut_stream_cache_mb`: 64 MB for known RAM up to 6656 MB, otherwise 128 MB. Native registration at `0x23bf268` initializes the cache setting to 128 MB. | Expose Auto and a small set of tested sizes. Requires overriding the launcher policy. |
| Shader prewarming / precompile limit | Launcher enables `xnr.szr_prebuild_warm` when its bundled base pipeline list is available. Native `szr_prebuild_max` is registered at `0x2397a14`; a diagnostic reports how many cached pipelines were built ahead. | Add startup-precompile controls and a clear cache/rebuild action. Disabling prewarming can trade shorter startup for gameplay stutter. A clear-cache UI would be new code, not an existing hidden button. |
| Picture crop / aspect | Launcher forces `xnr.szr_present_trim = 0` and `xnr.szr_present_aspect = 1.7777778` (16:9). Native `szr_present_trim` is a registered integer (native default 15; constructor `0x232fe00`). Existing native help describes image-height trimming as a percentage. | Add a crop control with a validated range; retain the existing 16:9/stretch choices. |

Brightness, shadow quality, and the video switch are the most useful first additions. Memory/cache controls belong in an advanced section.

## Existing features that are easy to miss

- **In-game Shadow-emblem menu:** aspect ratio/stretch, show FPS, edit buttons, touch plus controller, and hide the emblem. Tap the same corner to restore a hidden emblem. These already work through visible controls; they are not new unlocks.
- **GPU Driver → Change → Import driver:** exists, but Qualcomm detection gates the entire row and driver configuration. Removing the gate would not make Turnip compatible with Mali or other GPUs. Improving detection for Qualcomm custom ROMs would be a separate compatibility change.
- **Controller 1 and Controller 2 mapping:** already present.
- **Frame generation Off/2x/3x/4x, Performance/Quality, 60/90/120 Hz:** already exposed. Do not market these as hidden features.

## Automation and developer hooks

LauncherActivity handles these intent extras:

| Extra | Type | Effect |
|---|---|---|
| `trailer` | boolean | Enable/disable the opening-video panel. |
| `gameDirectory` | string | Persist a game directory if it contains `default.xex`. |
| `previewControls` | string path | Display the touch overlay over an image without starting the game. This is a preview path, not the complete editor. |
| `importDriver` | string path | Import and select an AdrenoTools ZIP. |
| `selectDriver` | string | Select an installed driver's identifier. |
| `importSave` | string path | Import a save using the normal importer. |

The APK also has code to auto-import ZIPs from `assets/drivers/` on Qualcomm devices; the inspected release contains no bundled driver ZIPs. A bundled-driver feature would require adding assets, choosing compatible versions, and retaining their notices.

The exported JNI method `nativeShadowSet(String key, String value)` calls a generic registered-setting setter (`0x23c4f40`), returning false for unknown names. Java currently uses it for live stretch changes. This offers a possible route for an in-game advanced panel, but registration does not mean every setting is safe or effective to change while rendering. Some values are sampled only during startup or resource creation.

## Experimental candidates — not ready for normal settings

- **`szr_async_pipelines`:** confirmed native boolean registration, default false (`0x2397938`). Its own diagnostics say draws are omitted while their pipelines compile. This is a quality tradeoff requiring testing, not a free performance fix.
- **`renut_anisotropic_filtering` / `renut_post_process_precision`:** confirmed registered native options (`0x2387078` / `0x2387094`), absent from the Java menu. Accepted values and their effects in this game's renderer still need tracing/testing; the inherited Xenia anisotropic override's enum must not be assumed to apply to these separate options.
- **Frame-generation fine tuning:** native `szr_fg_light` is an integer (native default 0; launcher forces 2). `szr_fg_flow_scale` is a floating-point setting (native default 0.5; exposed presets 0.25/0.5). More presets might be possible, but quality, speed and compatibility are unverified.
- **Audio buffering:** native `NARUTO_AUDIO_CUSHION` is read through getenv and parsed as an integer, default 8 and clamped to 2–48 (`0x2402e28`). Its units and latency/stability effects need further tracing before an end-user control. Other `NARUTO_AUDIO_*` names are present; presence alone does not validate a useful option.
- **Vulkan debug layers:** registered and false by default. The APK does not bundle validation-layer libraries, so merely exposing the switch would not supply them.

No safe 60-FPS gameplay unlock was established. The visible 20/24/30 choices map to internal 40/48/60 configuration values; those internal numbers alone do not establish that the game can safely run at double speed. No hidden cheat/developer-menu UI or Linux support was found in the Java launcher.

## Why editing naruto.ini alone is insufficient

The launcher reads the existing `naruto.ini`, removes entries for keys it owns, preserves unrelated entries, and appends newly generated settings before launching. This is confirmed in original smali (`LauncherActivity.smali`, `Map.containsKey` branch), rather than relying on the decompiler's malformed rendering of that method.

Brightness, shadow cap, crop/aspect, GPU budget, streaming cache, and several rendering flags are launcher-owned. Any exposed controls must feed this writer; otherwise user edits get overwritten. Unknown keys can survive, but their surviving in the file does not prove the renderer supports them.

## Translation and validation limits

The R1 changes replace UI text, supporting error/status text, and explanatory license-file prose. Original license bodies, copyright notices, package identity, configuration keys, renderer code, assets, and game files are retained. R1 adds a default-off Logging switch and forces English without a PT-BR option; other audited native settings remain unchanged. Particle/jutsu bursts are disabled in their shared handler; card/button bounce and press feedback are retained. The update dialog replaces externally fetched Portuguese release notes with an English notice; update detection/download handling is retained.

This hidden-settings audit uses static validation: exact string replacement, smali changes limited to translations, one update-notice string load, and one decorative-effect handler, plus the documented English-only and logging UI/lifecycle patches, unchanged native executable sections, format-placeholder preservation, and searches for untranslated Portuguese UI text. See `TESTING.md` for APK assembly, signing, installation and layout checks; full gameplay is outside the original audit.

The latest footer reads **Modified and Translated by XHYN_PH** and opens https://youtube.com/@xhyn_ph. The native logging helper is built by `build_r1.py`; see `native-logging/README.md` for output-suppression scope and validation still required. Android logging integration results are recorded separately in `TESTING.md`.
