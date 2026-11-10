# R2 scope

The refined R2 keeps the launcher layout, background, settings cards, original mapping grid and touch layout. Dialogs and the in-game menu use a consistent dark Material style with gold accents. No additional settings activity is introduced.

Performance refinements 1–6: Balanced 720p/900p presets; output-matched automatic refresh; statistics/memory work only when shown; completed loading polling stops; hidden launcher animations stop; stock cached axis processing with allocation-free joystick lookup.

Remove added right-stick/R3 tools, calibration, rumble, mapping visualization/reset, diagnostics export and performance history. Restore stock touch/controller/haptic/FPS-widget source exactly. Keep the original mapping list and native bindings.

Expand the in-game menu with supported display, visibility, opacity and brightness actions. Launcher return confirms first, clears touch input, stops polling and uses normal SDL shutdown, then releases the save lock and opens the launcher. Never claim this creates a manual save.

Save export is read-only. Never modify assets/save100, save100-original, save100.active or the upstream save-import code. Restore is blocked while the 100% save is active or the game is running. Restrict backup deletion to save-backups; stage restoration and back up the active save first.

Controller profiles, settings backup, shader-prewarming controls and touch-layout profiles stay excluded. Crop changes presentation, not camera/HUD rendering.
