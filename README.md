<div align="center">
  <img src="docs/screenshots/icone.png" alt="STORM 3 circular gold Leaf logo" width="144">

# STORM 3

**Modified and Translated by XHYN_PH**

An English Android edition of the STORM 3 Full Burst native port, with refined menus, save tools and a smaller, adjustable FPS panel.

[**Download the latest APK**](https://github.com/XHYN-PH/STORM-3-DROID/releases/latest) · [**Telegram**](https://t.me/xhynports) · [**YouTube**](https://youtube.com/@xhyn_ph) · [**R2.4 changes**](CHANGELOG_R2.4.md)

</div>

## About this edition

STORM 3 is my modified English edition of the [original native port](https://github.com/Masterllgod/naruto-storm-3-full-burst-port-nativo), based on Android **v0.6.16**. It runs native ARM64 code and renders through **Vulkan**.

The focus is a cleaner interface, more useful Android options and less unnecessary work in the menus and overlays. Performance and compatibility still depend on the phone, driver and game scene.

## What changed from the original?

| Feature | Original app | This edition |
| --- | --- | --- |
| Language | Portuguese launcher and a PT-BR switch | English launcher, settings, controls and messages; PT-BR switch removed |
| App identity | Original name and icon | **STORM 3** with the circular gold Leaf icon above |
| Menus and effects | Legacy dialogs and decorative particles | Rounded dark menus with gold accents; simple bounce and press feedback retained |
| FPS/statistics panel | Fixed size | Smaller by default, adjustable from **50–125%**, with separate game/output FPS and a compact mode |
| Save management | Save import | Import, export, backups and restoration safeguards |
| Display choices | Fit and Stretch | Fit, Stretch and proportional Crop to fill the display |
| In-game options | Basic display/control options | Touch visibility and opacity, brightness, panel size and confirmed return to launcher |
| GPU driver imports | Qualcomm-only picker | Turnip for Adreno, plus experimental PanVK for compatible Mali and RADV for compatible Xclipse |
| Logging | Regular app log capture | Optional **Logging**, off by default; original **Send log** retained with added session details |

The original touch layout, controller mapping screen and graphics controls are retained. Interface refinements also reuse button artwork, collect statistics in the background and stop unnecessary polling when panels are hidden. See the [release history](#release-history) for changes in each version.

## Requirements

- **Android 11 or newer** on a **64-bit ARM device**, with Vulkan support.
- Your own copy of **Naruto Shippuden: Ultimate Ninja Storm 3 Full Burst for Xbox 360, North American / NTSC-U version**.
- The complete extracted game folder, including `default.xex`, `data/` and `datafb/`.

**Game files are not included in the APK or this repository.**

## Install or update

1. Open the [latest release](https://github.com/XHYN-PH/STORM-3-DROID/releases/latest) and download **STORM-3-R2.4-arm64.apk**.
2. Install the APK. If you already use an earlier **XHYN_PH release**, install over it to keep your settings and saves.
3. Open **STORM 3** and grant file access when requested.
4. Select the extracted game folder containing `default.xex`, choose your settings, then tap **Play**.

This edition uses its own signing key. If Android reports a signing conflict with the original app or another build, **back up your saves before uninstalling it**.

<details>
<summary>Check the required game executable</summary>

The port requires the exact supported `default.xex`. A different region or executable revision may not work, even if the game title matches.

| File | Size | SHA-1 |
| --- | --- | --- |
| `default.xex` | 11,218,944 bytes | `f7ad65e26a00df6c29c9d34b291819593e455e9f` |

Copy the complete extracted disc folder; selecting an executable alone is not enough. See the [original project's game requirements](https://github.com/Masterllgod/naruto-storm-3-full-burst-port-nativo#readme) for more details.

</details>

## Useful options

- **Resize the FPS panel:** open **More options → Display → FPS / statistics size**. The same control is available in **Game options**, where resizing applies immediately. The default is 70%; Reset restores it. Drag the panel to move it, or use its minus button to collapse it.
- **Read the FPS panel:** **Game** is the game's frame rate; **Output** includes frame generation. Higher output FPS does not mean the game itself runs faster. `~` means an estimate. Compact mode uses **G** and **O** for the same two values.
- **Open Game options:** tap the floating menu button during gameplay, or use Android Back.
- **Return to the launcher:** wait for the game's save indicator to finish, then choose **Return to launcher** and confirm. Wait for **Closing game…** to finish before managing saves. Exiting does not create a manual save.
- **Export or back up saves:** open **More options → Saves**. Export reads your current save without changing it. Backup restoration is blocked while the game is running or the bundled 100% save is active. The bundled 100% save is preserved.
- **Fill the display:** **Fit** keeps the full image, **Stretch** changes its proportions, and **Crop** fills the display by cutting off edges while keeping proportions. Crop does not expand the game's camera view.
- **Collect a log:** enable **Logging**, restart the game, reproduce the issue, then use the original **Send log** link. App and session details are attached automatically.

## GPU drivers

The phone's **system Vulkan driver** is available by default. To try a custom driver, open **GPU Driver → Change → Import driver** and choose a compatible Android Vulkan ZIP. Close the game and wait for shutdown to finish before importing or changing drivers. Changes apply on the next game launch.

| Phone GPU | Custom driver option |
| --- | --- |
| Qualcomm Adreno | Compatible Turnip / AdrenoTools-format packages |
| ARM Mali | **Experimental** PanVK imports for supported Mali families |
| Samsung Xclipse | **Experimental** RADV Xclipse imports; the linked v1.8.0 package requires Android 14 or newer |

Drivers must match your GPU and Android version. PanVK and RADV Xclipse cannot replace Turnip on an Adreno phone. The linked FristOneRR PanVK 3.7.0 package excludes newer CSF/Immortalis GPUs.

Driver packages are downloaded separately. Successful import does not guarantee working gameplay; the Mali/Xclipse paths still need testing on real devices. See [driver requirements and downloads](r2/DRIVERS.md) for supported packages and restrictions.

## Updates and support

- **[Telegram — @xhynports](https://t.me/xhynports)**
- **[YouTube — @xhyn_ph](https://youtube.com/@xhyn_ph)**
- **[GitHub releases](https://github.com/XHYN-PH/STORM-3-DROID/releases)** — APK downloads and release notes.
- **[Report an issue](https://github.com/XHYN-PH/STORM-3-DROID/issues)** — describe what happened and how to reproduce it.

For bug reports, include your phone model, Android version, app version, GPU driver and the affected game mode or scene. If possible, attach a log collected using **Logging → restart game → reproduce issue → Send log**.

## Release history

- [R2.4 — Experimental Mali/Xclipse drivers, background sampling and startup refinements](CHANGELOG_R2.4.md)
- [R2.3 — Clearer FPS, lighter overlays, better logs and exit feedback](CHANGELOG_R2.3.md)
- [R2.2 — Smaller statistics panel and interface optimizations](CHANGELOG_R2.2.md)
- [R2.1 — Game startup fix and restored Send log](CHANGELOG_R2.1.md)
- [R2 — Refined menus, save tools and app identity](CHANGELOG_R2.md)
- [R1 — English translation and quieter menu effects](CHANGELOG_R1.md)

## Source code and limitations

The original port's engine is **closed source**, and I do not have access to its source code. This limits the bugs I can fix, the performance improvements I can make and the features I can add, especially inside the engine and renderer. Some crashes, graphical problems and compatibility issues require changes to that engine.

My customization code, translations, patches and build tools are published in this repository. They cover the launcher, interface, settings and Android integration; they are not the full engine source. See the [build instructions](r2/README.md).

Customization code is [MIT-licensed](r1/LICENSE). The original port and its components retain their existing authorship and licenses; see [Third-party notices](THIRD_PARTY_NOTICES.md). This is an unofficial modification, not an official game release.
