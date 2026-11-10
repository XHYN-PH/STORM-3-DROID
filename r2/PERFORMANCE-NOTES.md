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
