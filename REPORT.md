# Round 117 Deliverables Report — Folio v11.7 KFX (Reading Goal Pace Telemetry & Share Challenge)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.7 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:54:57`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **3-Tile Reading Pace Telemetry Strip (`showReadingGoalDialog`)**: Added an interactive pace dashboard right below the annual progress bar showing:
       * **`DAYS LEFT`** (sky-blue `#38BDF8`): Days remaining in the active year computed dynamically from calendar telemetry.
       * **`TO GO`** (amber `#F59E0B` or emerald `#34D399` when met): Number of books left to reach target (`curGoal - finishedCount`).
       * **`NEEDED PACE`** (violet `#C084FC`): Calculated required pace (e.g. `1 / 9d` or `🏆 Met!`).
     - **`↗ Share` Reading Challenge Action**: Added a soft sky-blue `↗ Share` button in the header right beside `🎯 2026 Reading Goal`. Dispatches Android's native `Intent.ACTION_SEND` with reading goal progress %, days remaining, required pace, and completed books list.
     - **Unified Trophy Shelf Covers**: Upgraded Completed Books Trophy Shelf to use `makeMiniCoverView(...)` with rounded `#0D1017` border + `📖` fallback.
     - **Compiler Variable Shadowing Fix**: Resolved variable name clash on `prLp` (reassigned preset row layout params to `presetLp`).
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.7 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (404,120 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:54:57`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.7_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r117_reading_goal_pace.png` (235,328 bytes)**:
   - Captured **🎯 2026 Reading Goal** modal opened from Control Center.
   - Shows `🎯 2026 Reading Goal` header alongside the new `↗ Share` action pill.
   - Shows the 3-tile Reading Pace Telemetry strip: `DAYS LEFT`, `TO GO`, `NEEDED PACE`.
   - Shows the `🏆 COMPLETED BOOKS (1)` Trophy Shelf with date stamp and cover preview.
2. **`verify_r117_about_v117.png` (353,990 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.7 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Indexed Library Books: 34 · Enriched Metadata Records: 32`
     * `Reading Progress: 5 in progress · 1 finished`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * `Privacy Mode: Local-First (Zero Amazon Telemetry)`
3. **Crash & Invariant Log Check**:
   - Zero crashes logged for `com.amazon.kindle.epub`.
   - Stock Kindle (`com.amazon.kindle` v8.160.1.0) verified **100% untouched** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Package Verification Artifacts
- Archive: `antig_to_gemini_neon_clunky_report117.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_117.md`
  * `protocol.json`
  * `verify_r117_reading_goal_pace.png`
  * `verify_r117_about_v117.png`
