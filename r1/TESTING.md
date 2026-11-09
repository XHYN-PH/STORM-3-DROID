# R1 validation

## Completed

- Official upstream APK SHA-256 verified before modification.
- Native logging regression suite: **70 assertions passed**, with AddressSanitizer and UBSan enabled.
- Checks cover suppression of writable `.log` files; preservation of status/save files and old logs; distinct per-library file-opening chains; stdout/stderr restoration; repeated hook installation; and restoration of read-only relocation pages.
- ARM64 native helper compiled using Android NDK r28c, API 30, with warnings treated as errors and 16 KB-compatible ELF segment alignment.
- Original engine executable/linkage sections, other native libraries and bundled data/assets verified unchanged. Only the explicitly listed native text strings differ in `libmain.so`.
- License bodies and copyright notices verified preserved, allowing normalization of line endings and translation of one explanatory sentence.
- English-only configuration, custom footer/link, fork update feed, particle suppression and retained bounce/press animations checked.
- Smali branch labels and invocation-register constraints checked before assembly; Apktool successfully assembled the APK.
- Version **R1**, code **47**, package `com.naruto.storm3`, minimum API 30 and target API 35 confirmed from the built APK.
- APK ZIP integrity, alignment and signing verified. The private signing key is not in the repository.

## Android runtime validation

An Android 11 Google APIs emulator with ARM64 native translation is being used
for the actual release APK. The host has no hardware CPU virtualization, so
software emulation is slow. Results will be recorded here after the checks
complete; installation and UI checks must not be assumed passed yet.

## Not established by these checks

- Full gameplay: requires a legitimate game-data copy, which is not present here.
- Snapdragon/Adreno and custom Turnip driver behavior on physical hardware.
- Mali/Xclipse performance, every Android vendor ROM, and every display refresh rate.
- Android system/crash-log suppression: these logs are outside the app's control.

The inherited engine is binary-only in this repository. Structural equality
checks preserve it but do not replace testing the game on real hardware.
