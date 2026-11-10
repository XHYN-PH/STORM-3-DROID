# STORM 3 — R2.4

Modified and Translated by **XHYN_PH**.

- **Experimental Mali and Xclipse driver imports:** use the existing GPU Driver picker for compatible Android PanVK and RADV Xclipse ZIPs. Turnip imports remain available on Adreno. Drivers are not bundled, and STORM 3 gameplay on these additional GPU families still needs device testing.
- **Driver compatibility checks:** imports check GPU family, minimum Android version, ARM64 library headers and ZIP contents. The linked FristOneRR PanVK 3.7.0 package is blocked on its unsupported Mali generations. Its experimental label and original package notices are retained.
- **More reliable driver changes:** imports and selection are blocked while the game/save lock is held. Re-importing the same package reuses its installed copy, and changing drivers clears the previous driver's compatibility result so a different driver can be tried. The system-driver option and original crash-recovery prompt remain.
- **Less work on the UI thread:** the remaining native statistics read now runs on a dedicated worker. Overlapping reads are prevented, and cancelled results cannot update a paused or closed game.
- **Tighter touch-overlay drawing:** compact custom layouts can use a smaller transparency layer, including horizontal bounds. Group opacity, button artwork, controller mappings and touch behavior are preserved.
- **Refresh handling:** requests also follow surface recreation and detect changed refresh rates even when the display mode ID stays the same. Requests remain limited to foreground gameplay with bounded retries; a cooldown allows recovery from a later independent rate change.
- **Clearer startup feedback:** the original shader-preparation panel can switch to Loading game until the first valid frame-rate observation. Feedback is bounded, passes input through after preparation, and does not force FPS collection when statistics are off.
- **Better existing Send log details:** default settings resolve to their effective values, including 30 FPS, and the recorded session keeps the selected driver name. Reports include refresh-recovery/sample counters and observed rendering-path/frame-pacing details. Native frame-generation counters are labelled to avoid confusing them with totals or measured input latency.
- **Clearer scaling information:** Display explains that frame generation can use a different upscaling path; Send log records the last renderer-reported path when available.

Install over earlier XHYN_PH releases without uninstalling or clearing app data. Save protections and the bundled 100% save are retained. Wait for the game's save indicator to finish before leaving. No game files are included.

The original engine is closed source. Intermittent native stalls and frame-generation queue tuning remain under investigation; this release does not claim those stalls are fixed or promise higher gameplay FPS.

[Driver requirements and downloads](r2/DRIVERS.md) · [YouTube](https://youtube.com/@xhyn_ph)
