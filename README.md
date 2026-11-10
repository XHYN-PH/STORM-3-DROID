<div align="center">
  <img src="docs/screenshots/icone.png" alt="STORM 3 circular gold Leaf logo" width="144">

# STORM 3

**Modified and Translated by XHYN_PH**

An English Android edition of the STORM 3 Full Burst native port, with refined menus, save backups and a smaller, adjustable FPS panel.

[**Download the latest APK**](https://github.com/XHYN-PH/STORM-3-DROID/releases/latest) · [**R2.2 changes**](CHANGELOG_R2.2.md) · [**YouTube**](https://youtube.com/@xhyn_ph)

</div>

## What changed from the original?

Based on the original Android **v0.6.16** release.

| Feature | Original app | This edition |
| --- | --- | --- |
| Interface language | Portuguese launcher and settings | English launcher, settings, controls and messages |
| Language switch | PT-BR option | Removed; English is enforced |
| App identity | Original name and icon | **STORM 3** with the circular gold Leaf icon above |
| Settings and game menu | Legacy Android dialogs | Rounded dark menus with gold accents |
| Menu effects | Decorative particle bursts | Particles removed; simple bounce and press feedback retained |
| FPS/statistics panel | Fixed size | Smaller by default, with a **50–125%** size slider |
| Repeated interface work | Button artwork rebuilt during redraws; statistics memory reads on the UI thread | Cached button artwork, fewer unnecessary redraws and background statistics collection |
| Logging | Regular app log capture | Optional Logging switch, off by default; original **Send log** retained |
| Save management | Save import | Import, export, backups and protected restoration |
| Display choices | Fit and Stretch | Fit, Stretch and proportional Crop to fill |
| In-game options | Basic display/control options | Touch visibility, opacity, brightness, panel size and confirmed return to launcher |

The original touch layout and controller mapping screen are retained. The added Performance presets menu has been removed in R2.2; the original graphics controls remain available.

## Install or update

You need **Android 11 or newer**, a **64-bit ARM device**, Vulkan support, and the game's files. Game files are not included in the APK.

1. Open the [latest release](https://github.com/XHYN-PH/STORM-3-DROID/releases/latest) and download **STORM-3-R2.2-arm64.apk**.
2. Install the APK. R1, R2 and R2.1 users can update directly, without uninstalling or resetting app data.
3. Open **STORM 3**, select the extracted game folder containing `default.xex`, then tap **Play**.

If Android reports a signing conflict with the original upstream app, back up your saves before replacing that installation. This fork uses its own signing key.

## Useful controls

- **Resize the FPS panel:** open **More options → Display → FPS / statistics size**. The same control is available in **Game options**, where resizing applies immediately. The default is 70%; Reset restores it. Drag the panel to move it, or use its minus button to collapse it.
- **Open Game options:** tap the floating menu button during gameplay, or use Android Back.
- **Return to the launcher:** wait for the game's save indicator to finish, then choose **Return to launcher** and confirm. This exits the game; it does not create a manual save.
- **Export or back up saves:** open **More options → Saves**. Export reads your current save. Restoring a backup is blocked while the game is running or the bundled 100% save is active.
- **Fill the display:** use **Fit**, **Stretch**, or **Crop**. Crop keeps the image proportions and cuts off edges; it does not expand the game's camera view.
- **Collect a log:** enable **Logging**, restart the game, reproduce the issue, then use the original **Send log** link.

## GPU drivers

Rendering uses **Vulkan**. The system GPU driver is available by default. Compatible Qualcomm/Adreno devices can import custom drivers such as Turnip through **GPU Driver → Change → Import driver**, using the app's AdrenoTools integration. Importing an Adreno driver does not add support for other GPU families.

## Release history

- [R2.2 — Smaller statistics panel and interface optimizations](CHANGELOG_R2.2.md)
- [R2.1 — Game startup fix and restored Send log](CHANGELOG_R2.1.md)
- [R2 — Refined menus, save tools and app identity](CHANGELOG_R2.md)
- [R1 — English translation and quieter menu effects](CHANGELOG_R1.md)

## Source code

The customization code, translations, patches and build tools are published in this repository. See the [build instructions](r2/README.md). The upstream native engine's source is not included; this project customizes its pinned Android release.

Customization code is [MIT-licensed](r1/LICENSE). Existing engine, game and third-party notices are retained in [Third-party notices](THIRD_PARTY_NOTICES.md).
