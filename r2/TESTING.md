# R2 validation

Before the first full test APK assembly:

- 28 isolated save assertions passed, including export byte equality, 100% save flag/original-backup preservation, blocked restore while active, backup/restore/delete behavior, interprocess-lock exclusion, traversal rejection and linked-path rejection.
- 19 input/display/configuration/rumble assertions passed, including radial clamping, drift dead zones, inversion, sensitivity, equal-dimension fullscreen crop, config overwrite/preservation and amplitude/stop bounds.
- Existing native logging regression suite passed all 70 sanitizer assertions.
- Added Java classes compiled and disassembled. Source patches applied successfully and engine/data/license preservation, smali labels/registers and manifest checks passed.

The initial test APK assembled and passed signature, ZIP-integrity and 16 KB alignment checks. It uses the same XHYN_PH key as R1 for compatible updates. It is not a published R2 release.

Android run [42667789029](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42667789029)
passed **53 assertions** on code commit `283f4e1226d783d3bdf3e919882d6ba16e8daac4`, plus all six Java/SDL logcat On/Off marker checks. Earlier runs passed the 47-check baseline and the 50-check controller refinement.

Checks include stock launcher/card/dialog installation, circular-icon transparency and the STORM 3 label; independent multi-finger input, center crossing, release/cancel, button priority and right-stick invisibility; controller visualization/reset controls, actual diagram R3 touch targets/cancel/edge hit testing and R3 key mapping; native library/hook integration and typed game-method verification; actual Android save export/restore, 100% protections and re-importing an exported ZIP through the unchanged stock importer in an isolated context; diagnostics collecting enabled app logs and excluding logs when disabled.

The complete launcher/controller previews were visually reviewed through the UI inspection workflow. The launcher retains its stock layout, colors and widgets; the controller visualization is within the original mapping activity, with the original list still available. The logging toast visible in the controller capture is transient test feedback.

The distributable uses the private XHYN_PH key. Final APK SHA-256:
`75d9b75ad4c05c28e556512d312c376dba6df72204dbeab30cb795773d9df2dc`.
The CI build uses a separate ephemeral test key and never publishes that key or test APK.

R1 compatibility was also checked: the original Android validation run [42658707705](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42658707705) and sanitizer/source run [42658707715](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42658707715) passed after adding the optional R2 build path.


Unverified: full gameplay, genuine game-triggered phone rumble, physical Xbox firmware/transport compatibility, physical Adreno/Turnip behavior, shadow visual/performance effects and proportional SurfaceView crop across vendor Vulkan implementations. FPS-derived timing estimates are labeled as estimates. True ultrawide camera/HUD behavior is not an implemented feature.
