# R1 — English Edition

**Modified and Translated by XHYN_PH**

R1 is based on the upstream Android v0.6.16 port. It focuses on an English
interface, simpler menu interactions and control over app logging.

## What changed

- **English throughout the app:** translated the launcher, settings, popups,
  controller mapping, touch-control editor, shader-loading messages and app
  error/status messages. Explanatory text in the license screen is also English;
  original license texts and copyright notices are preserved.
- **English game configuration:** removed the PT-BR switch. R1 always writes
  English into the game configuration, even if Portuguese was previously enabled.
- **Cleaner option clicks:** removed particle/jutsu bursts and large decorative
  effects when selecting options. Button/card bounce, press feedback, selection
  animations and vibration remain.
- **A Logging switch, off by default:** added a dedicated control in the old
  PT-BR slot. It controls app logcat output, stdout/stderr and writable `.log`
  files in the app's native libraries. Turn it on when troubleshooting and
  restart the game after changing it.
- **Useful data stays safe:** logging off does not delete old logs, saves,
  settings or shader caches. GPU compatibility and driver-recovery status files
  remain because the app needs them. Android's own system/crash logs are outside
  this app setting's control. Logging calls are not removed from the engine, so
  this is not a promise of zero logging overhead.
- **Your YouTube footer:** the menu now shows “Modified and Translated by
  XHYN_PH” and opens https://youtube.com/@xhyn_ph.
- **Updates follow this fork:** the update checker now looks at
  `XHYN-PH/STORM-3-DROID`, rather than offering upstream APKs with a different
  signing key. Future compatible R releases must use the same R1 signing key.
- **R1 version label:** Android reports version **R1**, version code **47**.
- **Customization source published:** the translations, exact patches, added
  Android classes, native logging helper, regression tests and reproducible
  build tools are in `r1/`. Original R1 code is MIT-licensed. The upstream
  engine/game source is not included or relicensed by this fork.

## What stays the same

The original game engine's executable code, bundled game-support assets,
rendering choices and Turnip/AdrenoTools compatibility are retained. This does
not add Turnip support to Mali or other non-Adreno GPUs. Additional hidden
settings are documented for future work; they are not enabled as R1 features.

## Installation

- Android 11 or newer, ARM64; the upstream Vulkan hardware requirements still apply.
- The APK does not include the game. Use your own compatible Xbox 360 game files.
- **Back up saves and settings first.** R1 is signed with a new XHYN_PH release
  key. Android cannot install it over an upstream APK signed by a different
  developer. Do not uninstall your existing copy until you have a backup.
- Keep the same R1 signing key for future updates. Public certificate information
  is in `r1/R1-signing-certificate.pem`; the private key is not published.

## Validation

The native logging suite passes 70 regression assertions under memory and
undefined-behavior sanitizers. The build verifies the original engine code,
assets and notices, version/metadata, patched Android code, APK integrity,
alignment and signing. See [the testing report](r1/TESTING.md) for Android checks
and the exact limits of this release's validation.

Full gameplay and physical GPU/Turnip testing require a supported device and
legitimate game files. No blanket claim of being bug-free is made.
