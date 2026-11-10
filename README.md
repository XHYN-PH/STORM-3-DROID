# STORM 3

**Modified and Translated by XHYN_PH** · [YouTube](https://youtube.com/@xhyn_ph)

## R2 device-test build

R2 renames the app to **STORM 3** and adds a circular Leaf icon, refined dark menus, performance presets, save export/backups and proportional fullscreen crop. Controller mapping and touch input use their original layout.

- [Download the R2 test APK](https://github.com/XHYN-PH/STORM-3-DROID/raw/refs/heads/r2-candidate/STORM-3-R2-arm64.apk)
- [R2 changes](CHANGELOG_R2.md) · [Customization source and build tools](r2/README.md)

R2 uses the same signing key as R1 and can update it. Back up saves first. This is a device-test build; full gameplay, launcher-return/save completion and vendor Vulkan crop behavior still need device feedback. R1 remains the published release.


## R1 — English Edition

R1 provides an English interface, quieter menu interactions and a default-off
logging switch, based on the upstream v0.6.16 Android port.

[Download R1](https://github.com/XHYN-PH/STORM-3-DROID/releases/tag/R1) ·
[Full R1 changelog](CHANGELOG_R1.md) · [R1 source and build tools](r1/README.md) ·
[Testing and limitations](r1/TESTING.md)

All R1 customization code, translations, patches and build/test tools are
published here. Original R1 code is [MIT-licensed](r1/LICENSE). The original
engine/game source is not available and is not relicensed by this fork.
Original copyright and third-party license notices are retained.

R1 uses its own signing key. Back up your saves/settings before replacing an
original upstream installation; Android cannot install a differently signed APK
as an update. No game files are included.

The base game's documentation follows. The screenshots show the upstream UI;
see the R1 changelog for the changes in this fork.

---

<div align="center">

<img src="docs/screenshots/icone.png" alt="Icon" width="128">

# 🐸 Naruto Shippuden: Ultimate Ninja Storm 3 Full Burst — Native Port

**The Xbox 360 game running natively on Android (ARM64) and PC (Windows x64), without CPU emulation.**

![Android](https://img.shields.io/badge/Android-11%2B-3DDC84?logo=android&logoColor=white)
![Windows](https://img.shields.io/badge/Windows-10%20%7C%2011-0078D6?logo=windows&logoColor=white)
![Architecture](https://img.shields.io/badge/arm64--v8a-64%20bits-orange)
![Graphics](https://img.shields.io/badge/Vulkan-shadowZero-AC162C?logo=vulkan&logoColor=white)
![Version](https://img.shields.io/badge/version-R1-blue)

[**⬇️ Download (Android and PC)**](../../releases) · [Requirements](#-requirements) · [Installation](#-installation) · [Settings](#%EF%B8%8F-settings) · [FAQ](#-frequently-asked-questions)

<img src="docs/screenshots/menu-pc.jpg" alt="Main menu" width="90%">

</div>

---

## 📖 About

This port **is not an Xbox 360 emulator**. The game's executable (`default.xex`) was **statically recompiled** from PowerPC to ARM64 (mobile) and x86-64 (PC), producing native code that runs directly on the processor. Since v0.4.1, graphics have used **shadowZero**, a native renderer that takes the game's Direct3D commands and renders directly through Vulkan, without emulating the Xbox 360 GPU. This makes the game much lighter to run and allows internal resolutions from **360p to 4K**. On phones whose GPUs do not meet shadowZero's requirements, the app automatically falls back to Xenia's Vulkan backend, as in earlier versions.

> [!IMPORTANT]
> **The APK and PC package do not contain any game files.** You need your **own copy** of *Naruto Shippuden: Ultimate Ninja Storm 3 Full Burst* for Xbox 360, in the version specified under [Required game version](#-required-game-version).

### ✨ Highlights

- **Battles at 30 FPS** (the game's own limit), with **Full Burst** content loaded.
- **Native shadowZero renderer** (Direct3D → Vulkan), with internal resolutions of **360p, 480p, 720p, 900p, 1080p, 2K, and 4K** (480p by default). Works on Mali, Adreno (including Adreno 610), and with Turnip drivers.
- **Choose the FPS limit in the menu:** 20, 24, or 30 (the game's limit), with game logic running at the correct speed, without slow motion.
- **⚡ Performance mode:** removes shadows **at any resolution**, rather than locking the resolution to 480p.
- **Brightness and colors matching the Xbox 360:** bloom and exposure during battles no longer blow out the image.
- **Lower GPU load at high resolutions:** on a POCO F3 (Adreno 650), battles at 1080p improved from 18 to 30 FPS, and the new **900p** option runs comfortably at 30 FPS.
- **FXAA and FXAA+ antialiasing** and AMD **FSR 1.0 / CAS scaling**, which can be turned on or off.
- **On-screen controls styled after Naruto mobile games:** a controller-style Y/X/B/A diamond, colored seals with each button's letter, labeled LT/RT/LB/RB, START and SELECT buttons, an **AWAKEN** button (right stick to the side), and shortcuts for **jutsu**, **ultimate jutsu**, and **chakra dash**.
- **Two controllers:** the first connected controller automatically becomes **player 1**, and the second becomes **player 2**. In **MAP (MAPEAR)**, you can remap each one (**Controller 1 / Controle 1** and **Controller 2 / Controle 2**); otherwise, both use the default layout.
- **Automatic physical controller detection:** connect a controller and the on-screen buttons disappear; disconnect it and they return. Use the Shadow emblem in the corner to **edit buttons** or use **touch + controller** together.
- **Menu featuring the game's own opening video**, playing silently in the left column, larger text, and **100% Save enabled by default**.
- **Frame generation (AMD FG):** Off / 2x / 3x / 4x. The game runs at 30 FPS while the display receives 60, 90, or 120 frames per second. Choose **Performance** mode (lightweight, the same as Storm 4 on PC) or **Quality**, and **Display (Hz)** Auto / 60 / 90 / 120. Based on AMD optical flow adapted for mobile (Mali, Adreno, and Xclipse), with no external file required.
- **No more ARM freezes:** fixes the GPU command ring issue that froze the game after a few minutes on phones and tablets, sometimes with error `0x0BADF00D`.
- **Android 11 and 12:** the app no longer closes at startup when connected to the internet.
- **Automatic updates:** when you open the menu, the app notifies you of new GitHub releases and can download and install them.
- **Custom GPU drivers (Turnip)** on **Snapdragon** phones, imported directly from the menu.
- **Stutter-free audio** during falls, jutsus, and support attacks.
- **Performance overlay:** FPS, resolution, and CPU, GPU, and RAM usage. Can be dragged around the screen or collapsed.
- **16:9 or stretched mode** to fill the screen.
- Animated **Jiraiya-themed** 🐸 main menu, shared by mobile and PC: Mount Myoboku at sunset, leaves and golden chakra surrounding the 油 seal, with a toad's eyes peeking out.
- **PC version** with windowed or fullscreen mode, keyboard support, and Xbox or PlayStation controllers.

## 📸 Screenshots

| Title screen | Game modes |
|:---:|:---:|
| <img src="docs/screenshots/titulo.jpg" width="100%"> | <img src="docs/screenshots/modos.jpg" width="100%"> |
| **Character selection** | **Before the battle** |
| <img src="docs/screenshots/selecao.jpg" width="100%"> | <img src="docs/screenshots/versus.jpg" width="100%"> |
| **Battle** | |
| <img src="docs/screenshots/luta.jpg" width="100%"> | |

## 📱 Requirements

### Android

| | Minimum | Recommended |
|---|---|---|
| **Android** | 11 (API 30) | 13 or newer |
| **Processor** | ARM64 (arm64-v8a) | Snapdragon 8 Gen 1 / Dimensity 8000 or better |
| **GPU** | Vulkan 1.1 | Mali-G710 / Adreno 730 or better |
| **RAM** | 4 GB | 8 GB or more |
| **Storage** | ~7 GB (game files) | — |

### 🖥️ PC

| | Minimum | Recommended |
|---|---|---|
| **Operating system** | Windows 10 64-bit | Windows 11 |
| **Processor** | x86-64 with AVX | 4 cores or more |
| **Graphics card** | Vulkan 1.1 (up-to-date drivers) | GTX 1060 / RX 580 or better (2K and 4K: RTX 3060 / RX 6700 or better) |
| **RAM** | 4 GB | 8 GB or more |

## 💿 Required game version

This port was made for **one specific executable version**, from the **North American disc** 🇺🇸🇨🇦. The code was recompiled from this file, so your `default.xex` must be **identical**: check the hash below.

| Field | Value |
|---|---|
| Game | Naruto Shippuden: Ultimate Ninja Storm 3 Full Burst (Xbox 360) |
| **Disc region** | **North America, NTSC-U** |
| Title ID | `4E4D085C` |
| Media ID | `2576927D` |
| Executable version | `0.0.0.9` |
| Executable region lock | NTSC-U (North America) only |

| File | Size | SHA-1 | MD5 |
|---|---|---|---|
| `default.xex` | 11,218,944 bytes | `f7ad65e26a00df6c29c9d34b291819593e455e9f` | `fd8ac164bcc8e380e84644402b6a0666` |

<details>
<summary><b>How to check the hash</b></summary>

- **Windows (PowerShell):** `Get-FileHash default.xex -Algorithm SHA1`
- **Linux / macOS:** `sha1sum default.xex`
- **Android:** any hash checker app from the Play Store.

</details>

### Game folder structure

Extract the disc and copy **the entire folder** to your phone. It must contain:

```
Naruto Shippuden Ultimate Ninja Storm 3 Full Burst/
├── default.xex        ← executable (must match the hash above)
├── nxeart
├── AvatarAssetPack
├── data/              ← game data
└── datafb/            ← Full Burst content
```

## 🚀 Installation

### Android

1. Download the **APK** from the [**Releases**](../../releases/latest) page and install it. You may need to allow *installation from unknown sources*.
2. Copy the game folder to your phone, for example `Internal storage/Games/Naruto Storm 3 Full Burst`.
3. Open the app and tap **CHOOSE FOLDER (ESCOLHER PASTA)**.
4. Grant **all files access**: the game reads its data directly from the folder without copying anything.
5. Select the folder containing `default.xex`.
6. Adjust the settings and tap **PLAY (JOGAR)** 🐸

### 🖥️ PC (Windows)

1. Download the **Windows `.zip`** from the [**Releases**](../../releases/latest) page and extract it to a folder.
2. Open **`NarutoStorm3FullBurst.exe`**.
3. Click **CHOOSE FOLDER (ESCOLHER PASTA)** and select the game folder containing `default.xex`.
4. Adjust the settings and click **PLAY (JOGAR)**, or press Enter.

Settings, saves, and the shader cache are stored in the folder where you extracted the port (`naruto.ini`, `data/`, `cache/`), rather than in the game folder.

> [!TIP]
> The first time each scene appears, the game compiles shaders and may stutter slightly. Those shaders are cached for subsequent runs.

## ⚙️ Settings

| Option | What it does | Performance impact |
|---|---|---|
| **360p / 480p resolution** | Renders fewer pixels (480p is the default) | 🟢 Higher FPS: ideal for less powerful phones |
| **720p resolution** | Original Xbox 360 resolution | ⚪ Moderate |
| **900p resolution** | Nearly as sharp as 1080p with far fewer pixels (1600x900) | 🟡 A good balance for powerful phones |
| **1080p resolution** | Sharper image on your phone's screen | 🟠 Slightly more demanding |
| **2K / 4K resolution** | Renders 4× to 9× as many pixels | 🔴 Much more demanding |
| **FPS: 20 / 24 / 30** | Frame rate limit (30 is the game's own limit) | 🟢 20 and 24 give the device more headroom |
| **Display: 16:9 / Stretch** | Keeps the original aspect ratio or stretches to fill the screen | ⚪ None |
| **Antialiasing: FXAA / FXAA+** | Smooths jagged edges | 🟠 Slightly more demanding |
| **Scaling: FSR / CAS** | Enlarges the image with improved sharpness (AMD FidelityFX) | 🟠 Slightly more demanding |
| **⚡ Performance mode** | Disables shadows and antialiasing at your chosen resolution | 🟢 Less demanding |
| **FPS overlay** | Shows FPS, resolution, CPU, GPU, and RAM | ⚪ Almost none |
| **GPU Driver (Driver da GPU)** (Snapdragon only) | Uses the system driver or an imported **Turnip** driver (`.zip` in AdrenoTools format) | Depends on the device and driver |

> [!NOTE]
> If FPS drops, enable **⚡ Performance mode**, or set **Antialiasing: Off** and **Scaling: Normal**, then lower the resolution.

## 🎮 Controls

- **Movement:** touch the analog stick area, which defaults to the **left half** of the screen. The stick appears there and follows your finger.
- **Buttons:** a controller-style diamond (Y at the top, X on the left, B on the right, A at the bottom), each with a colored letter seal (green A, red B, blue X, yellow Y), matching the game's button prompts.

| Button | In battle |
|---|---|
| B (the largest) | Attack |
| A | Jump |
| X | Ninja tools |
| Y | Chakra |
| RT | Guard |
| LT | Substitution |
| LB / RB | Call support characters |
| AWAKEN (DESPERTAR / 覚) | Right stick to the side (awakening) |
| JUTSU / OUGI / DASH (AVANÇO) shortcuts | Y B / Y Y B / Y A |
| D-pad ↑ ↓ ← → | Items |
| SELECT / START | Menu / pause |

- **Shadow emblem** (in the corner): tap to open **Edit buttons (Editar botões)** or enable **Touch + controller (Toque + controle)**. Drag buttons to reposition them. **Size (Tamanho)** changes all buttons when none is selected, or just the selected button.
- **Physical controllers:** Android-compatible Bluetooth and USB controllers are detected automatically, and the on-screen buttons disappear while a controller is connected.
- **Two controllers:** the first connected controller becomes player 1, and the second becomes player 2. In the menu's **MAP (MAPEAR)** screen, select **Controller 1 (Controle 1)** or **Controller 2 (Controle 2)**, then tap an action to change its button assignment.

### 🖥️ PC

Xbox and PlayStation controllers work out of the box. Keyboard controls:

| Key | Button | Key | Button |
|---|---|---|---|
| **W A S D** | Left stick | **Arrow keys** | Right stick |
| **I J K L** | D-pad | **Enter** / **Tab** | START / BACK |
| **Space** | A | **E** | B |
| **Q** | X | **F** | Y |
| **1** / **3** | LB / RB | **Z** / **C** | LT / RT |
| **Left / right Ctrl** | Stick clicks | **F11** / **Alt+Enter** | Fullscreen |

## ❓ Frequently asked questions

<details>
<summary><b>The game closes at startup / shows a black screen</b></summary>

- Check that `default.xex` matches the hash above.
- Check that **all files access** has been granted.
- Check that you selected the folder containing `default.xex`, rather than its parent folder.
- If you changed **GPU Driver (Driver da GPU)**, switch back to **System (Sistema)**.
- On Android, the menu's **Send log (Enviar log)** button shares a record of what happened. Attach it when opening an issue.

</details>

<details>
<summary><b>Can I play at 60 FPS?</b></summary>

No. The original game's timing is tied to frames and locked to 30 per second. At 60, it simply runs twice as fast. A stable 30 FPS matches the Xbox 360.

</details>

<details>
<summary><b>How do I switch to English?</b></summary>

R1 is English-only. Its launcher, settings, dialogs and touch controls are translated, and the generated game configuration always selects English. The old PT-BR option has been removed.

</details>

<details>
<summary><b>Which Turnip driver should I use? Does it work on Mali?</b></summary>

The **GPU Driver (Driver da GPU)** option only appears on **Snapdragon** (Adreno) phones: Turnip drivers are made for those GPUs. Import an AdrenoTools-format `.zip` directly from the menu. If the game closes with a new driver, the menu offers to switch back to the system driver. On phones with Mali GPUs, the game always uses the system driver.

In Android v0.6.16, open **Driver da GPU** → **Trocar** (Change) → **+ Importar driver (.zip do AdrenoTools)**. The app shows this row only when it detects a Qualcomm chipset from Android's reported SoC manufacturer or hardware name.

</details>

<details>
<summary><b>Does the European (PAL) version or another disc work?</b></summary>

The tested version is **North American (NTSC-U)**. The code was recompiled from that `default.xex`, so another disc only works if its `default.xex` is **identical**, with the same SHA-1 shown in the table. If the hash differs, it will not work.

</details>

<details>
<summary><b>Does it work on 32-bit or x86 phones? What about Linux or macOS?</b></summary>

No. For now, only Android arm64-v8a and Windows x64 are supported.

</details>

<details>
<summary><b>Is the source code available?</b></summary>

The R1 customization code, translations, patches and build/test tools are available in [r1/](r1/README.md). The original engine/game source is not available. Existing third-party components retain their own licenses, listed below.

</details>

## 🙏 Credits

This project was made possible by these open-source projects:

- [**XenonRecomp**](https://github.com/hedge-dev/XenonRecomp): static PowerPC (Xbox 360) recompiler
- [**Xenia Canary**](https://github.com/xenia-canary/xenia-canary): Xbox 360 hardware reference and the basis of the Vulkan backend
- [**SDL 3**](https://github.com/libsdl-org/SDL): windowing, input, and audio
- [**FFmpeg**](https://ffmpeg.org): XMA audio decoding
- [**AMD FidelityFX FSR / CAS**](https://github.com/GPUOpen-Effects): scaling and sharpening
- [**AMD FidelityFX SDK**](https://github.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK) (MIT): optical flow and frame generation interpolation
- [**libadrenotools**](https://github.com/bylaws/libadrenotools): custom GPU drivers on Android
- [**reNut**](https://github.com/masterspike52/reNut) and the [masseffect-nx](https://github.com/nebadasSwifty/masseffect-nx), [nfsmw-nx](https://github.com/StevensND/nfsmw-nx), banjo-nx, and sa2-nx ports: the basis of the native shadowZero renderer

The full licenses are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and inside the app under **Licenses (Licenças)** in the menu.

## ⚖️ Legal notice

This is a **nonprofit fan project**, with no affiliation with Bandai Namco, CyberConnect2, Ubisoft, VIZ Media, Masashi Kishimoto, or Microsoft. *Naruto* and *Naruto Shippuden: Ultimate Ninja Storm 3 Full Burst* are trademarks and works of their respective owners.

The APK **does not include** game files, videos, sounds, or textures. You need your own original copy. Do not request or share game files in the issues.

---

<div align="center">
Made with 🧡 and plenty of toad oil (油).
</div>
