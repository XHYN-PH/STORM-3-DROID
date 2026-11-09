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

GitHub Actions run [42648091497](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42648091497)
passed all 17 instrumentation assertions on Android 11 with ARM64 native
translation. These cover the English controls/footer, removal of PT-BR,
interactive logging On/Off persistence, actual upstream ARM64 library loading,
repeated native hook installation, forced-English configuration and repeated
initialization. A separate logcat check confirmed the real app Java logging
marker appears when enabled and is absent when disabled.

The final expanded Android run [42648957828](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42648957828)
passed on source commit `7b2a8e5bc920aabdfa98840ae83358aa2f668437`. All 17
instrumentation assertions passed, and separate logcat checks confirmed all
three app output paths (Java logger, SDL stack traces and SDL standard-error
messages) emit their markers with logging On and suppress them with logging
Off. The final host/sanitizer run [42648957813](https://github.com/XHYN-PH/STORM-3-DROID/actions/runs/42648957813) also passed.

The signed distributable built from that source has SHA-256
`1d73e7e5998563adc8d8331b1adf29c3ca714fd595088909eb6f982afb3827ef`.
Its signing, 16 KB ZIP alignment, archive integrity and source-preservation
checks passed. The test-only instrumentation package is not bundled in the release APK.

Local software emulation installed and opened the signed APK, but Android
system ANRs made it unsuitable for completing instrumentation. It is not
counted as a passing instrumentation run. Full CI builds use a temporary test
signature; the distributable APK uses the private XHYN_PH release key.

## Not established by these checks

- Full gameplay: requires a legitimate game-data copy, which is not present here.
- Snapdragon/Adreno and custom Turnip driver behavior on physical hardware.
- Mali/Xclipse performance, every Android vendor ROM, and every display refresh rate.
- Android system/crash-log suppression: these logs are outside the app's control.

The inherited engine is binary-only in this repository. Structural equality
checks preserve it but do not replace testing the game on real hardware.
