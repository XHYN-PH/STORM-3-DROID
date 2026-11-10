# R2.4 follow-up from the 10-minute R2.3 capture

The new capture identifies R2.3/code 51, Android 17, Adreno 750, 1080p, FSR and 2x Quality FG. Across 60 ten-second reports, median game speed is 30 FPS, render-busy time 5.065 ms and reported GPU time 14.525 ms. FG separately reports median 7.76 ms. Those measurement boundaries are not established as additive. The earlier capture used 900p and different scenes, so changes in medians are not an R2.3 benchmark.

After the first 30 seconds, seven windows contain frame maxima of 120.08–262.97 ms. Two FG reports show 77/79 shown-late counters, and four native reports show 121 Hz despite 60 Hz being selected. The final Android session snapshot is paused at 120 Hz, with its last foreground observation at 60 Hz. This does not prove a sustained foreground override. FG's five-vsync lead is not a measured input-latency value. Image acquisition waits include presentation pacing and are not CPU execution time.

The capture shows 701 prewarmed pipelines with zero failures. GPU allocation peaks at 864 MiB against a 2594–3080 MiB budget, and stream-cache use peaks at 10 MiB in a 64 MiB pool. No crash/device-loss/OOM is recorded. Increasing these budgets or clearing a working shader cache is not a justified performance fix.

Implemented app-side changes: background native statistics reads (single worker, one outstanding read per activity, generation-checked publication), first-frame feedback using the original panel, horizontal as well as vertical bounds for grouped touch alpha, one tracked surface callback for refresh-hint reapplication, rate comparison in addition to mode IDs, a two-retry budget renewed by a later display event after a 30-second cooldown, and summary counters in the stock Send log. Compact/custom layouts can reduce the compositing area; a default layout spanning the display may see little change. No gameplay FPS gain is claimed.

JNI audit: `nativeStatsValues` at `0x23fa044` writes a new Java array, calls the native counter/clock sampler at `0x23f92ac` and reads FG scalars. The real-frame count at `0x23d506c` is an acquire load from a process-lifetime counter; FG reads at `0x23c6f9c`/`0x23c6fa8` use process-lifetime scalars. The sampler maintains previous clock/counter state, so it is serialized on one worker. Android-only publication and lifecycle cancellation are tested. The engine binary remains unchanged; full gameplay/shutdown still needs phone testing.

The existing `tools/capture_stall_trace.py` now captures filtered display and thermal context before/after a local Perfetto trace, plus activity/window and GPU-frequency events where available. No ADB device is connected to this cloud environment. Native stall fixes, frame-generation queue-depth changes, and a proven correction to the native presenter's deadlines remain pending that evidence. Environment/setting names in a binary are not enough to safely change synchronization.

---

# R2.3 performance follow-up

The latest supplied renderer capture spans roughly 9.5 minutes. Its median ten-second game rate is 30 FPS, median render-busy time 5.6 ms and median reported GPU time 13.7 ms. Frame generation separately reports a median 6.7 ms; those numbers must not be added without knowing their measurement boundaries. Individual reported frame maxima still reach 154–231 ms after startup. GPU usage peaks at 836 MB against a 3566–3668 MB budget; actual streaming-cache usage peaks at 9 MB in its 64 MB pool. No renderer errors or memory pressure are recorded.

The last FG report changes from 60 to 121 Hz with 33 shown-late frames. Its foreground/background context is absent, so that report alone does not establish a foreground refresh bug. R2.3 reaffirms the selected rate after resume/focus and a mismatching display-mode change only while gameplay owns focus. Recovery is debounced and limited to two display-change retries per focus period; pause removes listeners and pending callbacks. Manual selections remain authoritative. Surface hints use the existing surface directly, without registering repeated SurfaceHolder callbacks. Android may override app preferences.

Collapsed statistics mode skips memory collection, CPU/GPU/RAM text preparation and unchanged readout redraws. Expanding requests a full sample using the most recent native values. Generation checks prevent a late compact/full sample from applying to the wrong mode or reappearing after hide/pause. The native statistics getter itself is retained; no claim is made that its internal work has been removed.

The stock panel now labels game FPS separately from generated output. Calculated output is prefixed with `~`; missing values remain unavailable. Its size control and gestures remain. The remaining left-stick base, arrows and knob gradients are cached independently, letting the knob move without rebuilding artwork. The original stock renderer remains the editor/software fallback, and input mappings/deadzones are unchanged.

The native-stall investigation remains open. This workspace has no ADB-connected phone or actual game files. `tools/capture_stall_trace.py` is ready to collect a local system trace during the affected scene. No unsafe asynchronous-pipeline flag, native synchronization patch, preset or game-speed unlock is introduced. Full game performance still needs device measurement.

---

# R2.2 implementation and native investigation

The touch renderer records the original translated button commands in bounded-count RenderNodes (two states per button). Moving/resizing a button invalidates both recordings; software canvases and the editor use the original method. No button layout, input mapping, stick behavior or macro timings change. Repeated input still reaches the stock input code immediately; redraw requests are skipped only when the visible state is unchanged. The original grouped opacity operation remains, with conservative bounds excluding empty vertical bands where the layout permits. Default layouts spanning the whole display may get no reduction from that bound.

The stock statistics panel defaults to 70% scale, with a saved 50–125% control in Display and Game options. Android's view transform scales text, gauges and local touch coordinates together. Drag limits use the transformed dimensions. Memory sampling and label formatting run on a single background worker; only one sample per panel can be outstanding. Generation checks reject samples cancelled by hiding or pausing the panel. Resident-memory calculation queries the actual OS page size instead of assuming 4096 bytes.

## Early native logging cutoff

Evidence is against the pinned v0.6.16 engine checksum in `r1/build_r1.py`:

- `0x23c5114..0x23c5140` reads `XNR_LOG_LEVEL` with getenv/strtol into `0x27d3208` (default 2).
- `0x23c49c8..0x23c49d8` returns `configured_level <= message_level`.
- There are 319 direct calls to that predicate. For example, `0x230d618` tests level 4 and branches past formatting work when disabled.
- Logging Off sets the supported environment threshold to 5 before engine initialization. Logging On restores the original environment override, including its original absence. This takes effect at the next game start; file/stdout/logcat suppression is retained.

This skips guarded messages. Direct printf/FFmpeg/other independent loggers can still format messages before their output is discarded. No claim of zero logging CPU work is made, and no native instructions are patched.

## Intermittent frame stalls

The supplied renderer capture reports maximum frame times of 160 and 187 ms in two ten-second windows while window-average GPU times remain below the 33.3 ms game-frame interval. Window averages cannot establish the cause of an individual slow frame. Presentation image-acquisition waits include pacing and must not be treated as measured CPU work. Memory pressure is not evidenced. Pipeline-cache save timestamps do not establish a consistent match to the longest stalls. No safe synchronization patch is established by these logs.

`tools/capture_stall_trace.py` records a local Android system trace through an already-authorized ADB connection while gameplay is running. It adds no app menu, uploader, performance history or diagnostics exporter. Run it on the affected phone during the same scene to distinguish scheduling, GC and presentation delays. FrameTimeline data depends on Android version; driver-specific GPU timelines may require additional vendor tooling. Do not publish private traces in this repository.

Actual game files and the affected phone are not present in the cloud workspace. Android UI/startup tests do not measure gameplay FPS or prove these particular stalls are fixed.
