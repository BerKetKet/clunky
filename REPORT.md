# Round 130 Deliverables Report — Major Milestone Folio v13.0 KFX (Reading Speed WPM Calibration, Pure-Kindle Book Details & GitHub Sync)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v13.0 KFX` installed on device at `2026-11-24 06:33:02`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioMoreView.java` (Reading Speed WPM Calibration & Milestone Bump)**:
     * Added **`READING SPEED (WPM)`** 4-pill selector (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`) inside the **2026 Reading Goal** dialog.
     * Persisted reading speed in `SharedPreferences("folio_ui_prefs", "reading_speed_wpm")` (default: 250 WPM, bounds: 100–800 WPM).
     * Repaired drop-in dialog theming: moved `FolioTheme.applyToDialog` post-`.show()` across reading goal, Up Next queue clear, cache clear, and About Folio dialogs.
     * Bumped badge to **`Folio v13.0 KFX`** and updated `About Folio` version string to `Folio v13.0 KFX (Enhanced Typesetting)`.
   - **`FolioMetadataCatalogDialog.java` (Calibrated Reading-Time & Pure-Kindle Blue-Slate Sweep)**:
     * Dynamic reading-time scaling: scaled `Format: Enhanced KFX · 1.2 MB · ~7h 8m read` dynamically using user's configured `reading_speed_wpm` (`Math.round((bytes / 1024.0) * 0.34 * (250.0 / wpm))`).
     * Unified remaining legacy indigo/amber borders across `Favorited` (`#2C5278`), `Shelf` (`#2C5278`), `Personal Note` (`#2C5278`), and bulk action bar (`Shelf`, `+Genre`, `+Queue` in `#2C5278` / `#162330`).

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (456,172 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 06:33:02`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v13.0_kfx/base.apk` (137,740,774 bytes ~132 MB, SHA256: `332b90e59b0750fee936496d62d3d0ba677f4c8b7168bedb074882e4fffa3463`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r130_reading_goal_wpm.png` (193,335 bytes)**:
   - Captured **2026 Reading Goal** modal showing:
     * Header `2026 Reading Goal`.
     * Goal preset pills (`12`, `24`, `52`, `100` books) and target input.
     * New section: **`READING SPEED (WPM)`** with 4 interactive pills (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`) styled with active teal accent (`#26A69A`) and Kindle-themed dark backgrounds.
     * Native buttons `Cancel` and `Save Goal`.
2. **`verify_r130_book_details_pure_kindle.png` (267,884 bytes)**:
   - Captured **Book Details (`ⓘ`)** for *1984* showing:
     * Bookerly serif title (`1984`).
     * Calibrated reading-time estimate: `Format: Enhanced KFX · 1.2 MB · ~7h 8m read`.
     * Pure-Kindle blue-slate borders on `★ Favorited` (`#2C5278`), `Shelf: Classics ›` (`#2C5278`), and `Personal Note (tap to edit)` (`#2C5278`).
     * Clean Kindle footer action buttons (`READ NOW`, `EDIT METADATA`, `CLOSE`).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Status**: Milestone v13.0 KFX staged, committed, and pushed to `origin/master`.
