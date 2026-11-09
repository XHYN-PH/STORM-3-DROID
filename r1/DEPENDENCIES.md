# Pinned build dependencies

Preserve HTTPS and verify checksums before using downloaded tools.

| Tool | Official download | Checksum |
|---|---|---|
| Apktool 3.0.3 | https://github.com/iBotPeaches/Apktool/releases/download/v3.0.3/apktool_3.0.3.jar | SHA-256 `dbf930b076c6b9be08d57c449cacefc3bdd6b71ebd59b3066fc0e1f5b14f9423` |
| Android NDK r28c, Linux | https://dl.google.com/android/repository/android-ndk-r28c-linux.zip | SHA-1 `a7b54a5de87fecd125a17d54f73c446199e72a64` |
| Android Build Tools 35.0.0, Linux | https://dl.google.com/android/repository/build-tools_r35_linux.zip | SHA-1 `2cfaa0bbb2336e9ec18ed3ecea84fa2e2af607bc` |

Android archive checksums are the values in Google's official repository metadata
at https://dl.google.com/android/repository/repository2-1.xml. The base APK's
strong SHA-256 pin is enforced by the build script. Java and GCC should come
from verified OS packages or their official distributions.
