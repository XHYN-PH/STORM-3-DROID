# R1 logging control

This source builds the helper required by the Logging switch. The R1 build script compiles it with the Android NDK and packages it in the APK. It is not supplied as a precompiled library in the source directory.

Default: off. The setting is persisted independently of `naruto.ini`, so the launcher's configuration writer does not overwrite it. It is applied in both the launcher and separate game process. Changing it applies to the next game launch; the original Play action restarts that process.

When off, the staged implementation:

- Installs a discard callback and silent priority through Android API 30's process-local liblog API. This covers app logcat output, including Java/SDL/FFmpeg paths routed through liblog.
- Redirects the process's stdout and stderr to `/dev/null`.
- After the native libraries load and before the SDL main thread starts, redirects writable `.log` opens to `/dev/null` in the APK's own libraries. This covers the inspected `naruto.log`, `xenia.log` and `plume_stderr.log` file sinks; it does not merely hide Send Log.
- Reports a visible error if initialization or hook installation fails, rather than claiming logging is off.

When on, original file paths and the ordinary Android logd callback are used. File hooks remain installed but pass the original paths through. The policy does not delete old logs.

Boundaries: Android system/crash logs outside this process are not controlled. GPU capability, driver-recovery, configuration, save and shader-cache files remain intact. No claim of zero logging CPU overhead is made: compiled logging calls can still format messages or write to `/dev/null`. Arbitrary debug dumps enabled by future settings need their own audit.

Static checks are insufficient to establish complete output suppression. Validate liblog behavior, ELF import hooks, permission restoration, logging On/Off, restarting the game, and actual files/logcat on Android 11+ before distributing an APK. Partial hook installation is reported as a failure. The code does not modify the original ELF files or patch native instructions.

## Build prerequisites

Use an Android NDK with CMake 3.22+, `arm64-v8a`, API 30 or newer. Configure `CMakeLists.txt` with the NDK toolchain, build `xhynlog`, and place the resulting `libxhynlog.so` in `../apk-work/lib/arm64-v8a/`. The R1 build script runs source/integrity checks before APK assembly. The helper uses 16 KB-compatible segment alignment.

See `../TESTING.md` for completed checks and device-testing limitations. The host regression suite covers suppression/restoration and preservation of original per-library file-opening chains.
