# Round 112 Deliverables Report — Folio v11.2 KFX (Up Next Queue Reorder Manager & Live Sync)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.2 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:12:18`.

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **`⇅ Reorder` Queue Manager Modal (`showQueueManagerDialog`)**: Added a compact violet `⇅ Reorder` pill right beside `See All (N) ›` in the Control Center `📌 Up Next Queue` header. Opening it displays all queued books with numbered rank badges (`#1` highlighted in violet, `#2`, ...), standalone `.cover.jpg` thumbnails, `↑` / `↓` reorder controls that persist ordered timestamps to `SharedPreferences("folio_queue")`, inline `✕` remove, and a `Clear Queue` action.
     - **Live Control Center Sync from `🎲 Tonight's Pick`**: Wired `refreshStats` callback into `showTonightsPickDialog(ctx, refreshStats)` so toggling `📌 Queue` / `📌 Queued` inside Tonight's Pick immediately updates the `📌 Up Next Queue` carousel behind the modal without reopening.
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.2 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (381,236 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:12:18`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.2_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r112_queue_reorder.png` (259,778 bytes)**:
   - Captured **📌 Up Next Queue** Reorder Manager modal opened via the `⇅ Reorder` header pill.
   - Shows promoted `#1` (*Choke* by Chuck Palahniuk) and `#2` (*Animal farm: a fairy story* by George Orwell) after tapping `↑` on `#2`.
   - Shows full reorder controls on each book card (`↑`, `↓`, `✕`), as well as `Clear Queue` and `Done` footer buttons.
2. **`verify_r112_about_v112.png` (354,336 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.2 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:

> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
