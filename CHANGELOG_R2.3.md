# R2.3

STORM 3 R2.3 improves the FPS panel, touch rendering, display recovery, log sharing and game-exit feedback.

- **Separate game and output FPS:** the panel now distinguishes the game's frame rate from frame-generated output. A `~` marks calculated output. The existing size slider, dragging and collapse controls remain.
- **Lighter collapsed FPS mode:** memory readings and unused CPU/GPU/RAM labels are skipped, and unchanged FPS values no longer redraw the panel.
- **Cached stick artwork:** the left stick reuses its base, arrows and knob gradients while moving. Its appearance, layout and input behavior are preserved.
- **Foreground refresh recovery:** the selected display preference is reapplied after returning to gameplay or an unexpected mode change. Recovery pauses when gameplay loses focus and uses limited retries. Android can still override display preferences.
- **More useful Send log attachments:** the original share action now adds `session-details.txt` with the app version, selected settings, GPU report and display observations. Current settings and the last recorded session are labelled separately; older logs are flagged when their timestamps do not fit. Enable Logging before starting the game to record session observations.
- **Clearer return to launcher:** a matching **Closing game…** dialog stays visible while normal shutdown holds the game/save lock. It closes once that lock becomes available. This does not force-kill the game or create a manual save.

Install over earlier XHYN_PH releases without uninstalling or clearing app data. Wait for the game's save indicator to finish before leaving. No game files are included.

Modified and Translated by **XHYN_PH** · [YouTube](https://youtube.com/@xhyn_ph)
