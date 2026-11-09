# R2 scope

Selected: 1–5, 7–9, 11–13, 15, 17–23. Excluded: controller profiles (6), settings backup (10), shader-prewarming controls (14), touch-layout profiles (16).

Keep the stock launcher, backgrounds, card constructors, switches, controller mapping grid and Android dialogs. Add a controller visualization/test and reset controls to the existing mapping screen. Add one compact More options card to the existing right-hand launcher container; do not introduce another settings activity or navigation system.

The right touch stick follows PT4Droid's invisible floating-stick capture: a free touch in the right half establishes its origin, buttons take priority, its pointer remains captured across the center, and lift/cancel resets its axes. STORM's existing gamepad-axis bridge is used; PT4Droid's game-specific native camera-delta API is not available here.

Save export is read-only. Never modify assets/save100, save100-original, save100.active or the upstream save-import code. Restoring backups is blocked while the 100% save is active or the game is running. Backups are restricted to save-backups; deletion cannot address the bundled save or active save. Restoration stages a copy and backs up the current save before swapping directories.

True ultrawide investigation: the confirmed szr_present_aspect/trim settings affect presentation, not the game's camera/HUD. They do not establish an expanded game view. R2 provides proportional fullscreen crop and original Fit/Stretch; no unsupported true-ultrawide switch is advertised.

Phone/controller rumble routes actual SDL feedback and includes a manual test. Touch presses are not substituted for missing game feedback. Real gameplay rumble and Xbox compatibility require physical-device testing.
