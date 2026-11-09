# R1 signed candidate APK

Modified and Translated by XHYN_PH.

Download STORM-3-DROID-R1-arm64.apk and verify SHA256SUMS.
This replaces the earlier candidate with the completed Java/SDL logging fix.
Build, signing, source preservation and 70 host logging assertions passed.
Android 11 validation passed 17 interface/native integration checks, plus
On/Off checks for Java logs, SDL stack traces and standard-error messages.
Full gameplay and physical GPU/Turnip testing remain unverified.

Back up saves/settings before replacing an upstream installation. R1 uses a
new signing key and cannot update an APK signed by another developer.
No game files are included. Original engine/third-party rights are retained.

Source/build tools: https://github.com/XHYN-PH/STORM-3-DROID/tree/main/r1
Validation: https://github.com/XHYN-PH/STORM-3-DROID/blob/main/r1/TESTING.md
Changelog: https://github.com/XHYN-PH/STORM-3-DROID/blob/main/CHANGELOG_R1.md
YouTube: https://youtube.com/@xhyn_ph

This branch is a candidate download only. No formal GitHub Release has been
created; personal-account publication is pending GitHub API access.
