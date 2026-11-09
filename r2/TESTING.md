# R2 validation

Before the first full test APK assembly:

- 28 isolated save assertions passed, including export byte equality, 100% save flag/original-backup preservation, blocked restore while active, backup/restore/delete behavior, interprocess-lock exclusion, traversal rejection and linked-path rejection.
- 19 input/display/configuration/rumble assertions passed, including radial clamping, drift dead zones, inversion, sensitivity, equal-dimension fullscreen crop, config overwrite/preservation and amplitude/stop bounds.
- Existing native logging regression suite passed all 70 sanitizer assertions.
- Added Java classes compiled and disassembled. Source patches applied successfully and engine/data/license preservation, smali labels/registers and manifest checks passed.

The initial test APK assembled and passed signature, ZIP-integrity and 16 KB alignment checks. It uses the same XHYN_PH key as R1 for compatible updates. It is not a published R2 release.

Android runtime checks and screenshots are pending in the R2 Android validation workflow. These exercise actual launcher/card installation, independent multi-finger stick input, center crossing, release/cancel, button hit-test priority, invisibility, controller diagram/reset controls, R3 key mapping and the existing native logging integration.

Unverified: full gameplay, genuine game-triggered phone rumble, physical Xbox firmware/transport compatibility, physical Adreno/Turnip behavior, shadow visual/performance effects and proportional SurfaceView crop across vendor Vulkan implementations. FPS-derived timing estimates are labeled as estimates. True ultrawide camera/HUD behavior is not an implemented feature.
