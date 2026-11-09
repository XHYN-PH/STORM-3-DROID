# R1 customization source

R1 modifications and English translation by **XHYN_PH**.

This directory publishes all R1 customization inputs: the native logging helper,
added Android classes in smali form, translations, exact patches, regression
tests, and the build/verification scripts. The original engine source is not
available. Rebuilding R1 uses the checksum-pinned upstream v0.6.16 APK as a base;
it does not rebuild the upstream engine from source.

Original R1 code is MIT-licensed; see [LICENSE](LICENSE) for scope. Upstream
copyright and third-party notices remain intact.

## Build on Linux

Requirements: Python 3.11+, GCC with AddressSanitizer/UBSan, Java 21, Apktool
3.0.3, Android NDK r28c, and Android Build Tools 35.0.0. Download tools from their
official sources and verify them using [DEPENDENCIES.md](DEPENDENCIES.md).

Get the upstream asset `NarutoStorm3FullBurst-PortNativo-v0.6.16-arm64.apk` from
[the upstream v0.6.16 release](https://github.com/Masterllgod/naruto-storm-3-full-burst-port-nativo/releases/tag/v0.6.16).
The build requires this exact SHA-256:

```text
15fcff17b64b29de84a205ea763cef71172f1086d61981c94b0d42f6257c9ecf
```

Supply your own signing keystore and a private file containing its password.
Never commit either. A differently signed build cannot update an installed R1
APK; compatible updates require the same release key.

```sh
python3 r1/build_r1.py \
  --upstream /path/to/upstream-v0.6.16.apk \
  --apktool /path/to/apktool_3.0.3.jar \
  --ndk /path/to/android-ndk-r28c \
  --build-tools /path/to/android-sdk/build-tools/35.0.0 \
  --keystore /private/path/release.p12 \
  --password-file /private/path/keystore.pass \
  --alias xhyn-r1 \
  --output /path/to/output/STORM-3-DROID-R1-arm64.apk
```

The script checks the original APK checksum, runs native regression tests,
applies the customization, sets version **R1** / code **47**, compiles the ARM64
helper for Android 11+, verifies protected upstream files, builds and aligns the
APK, signs it, verifies the signature/alignment/archive, and writes SHA256SUMS
and a build report. Private keys are not part of this repository.

## Tests and limits

```sh
gcc -std=c11 -Wall -Wextra -Werror -g \
  -fsanitize=address,undefined -fno-omit-frame-pointer -pthread \
  r1/tests/test_logging.c -ldl -o /tmp/r1-test-logging
/tmp/r1-test-logging
```

See [TESTING.md](TESTING.md) for release validation and untested cases.
[HIDDEN-SETTINGS.md](HIDDEN-SETTINGS.md) records additional candidates for future
work; those controls are not R1 features.
