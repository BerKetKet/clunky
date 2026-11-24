# Round 132 Deliverables Report — Folio v13.2 KFX (Insights WPM Tap-to-Calibrate, Snapshot Telemetry & Clean Dialog Theming Sync)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v13.2 KFX` installed on device at `2026-11-24 23:32:53`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration & Audit**:
   - **`FolioMoreView.java`**:
     * Integrated **Tap-to-Calibrate WPM in Library Insights**: updated section header to `READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)` and wired `calibrateClick` across both `rtHdr` and `rtRow` to launch `showReadingGoalDialog` with an auto-reopen callback to `showLibraryInsightsDialog(ctx)` on save.
     * Added missing **Reading Time in Shareable Insights Snapshot**: inserted `Reading Time (250 WPM): 651h total · 14.9h read · 636h remaining` into `snapshotText` (`snap`) for `COPY SUMMARY` and `SHARE` exports.
     * Confirmed clean post-`.show()` `FolioTheme.applyToDialog(...)` placements across all 5 dialogs (`goalDlgRef[0]`, `dlgRef[0]`, `storDlgRef[0]`, `insightDlgRef[0]`, `aboutDlg`).
     * Bumped Control Center badge and `About Folio` dialog version string to **`Folio v13.2 KFX`** (`Folio v13.2 KFX (Enhanced Typesetting)`).

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (`458,448` bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 23:32:53`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v13.2_kfx/base.apk` (`137,740,774` bytes ~132 MB, SHA256: `430a7b2d35adbc016a99248d4ce6b9e7a9c9e28fa853692d68444cd8071aaba2`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r132_insights_calibrate.png` (176,328 bytes)**:
   - Captured **Library Insights** modal showing:
     * Bookerly serif heading (`Library Insights`).
     * Updated interactive prompt: **`READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)`** over the 3-tile strip (`651h TOTAL LIBRARY`, `14.9h TIME READ` highlighted in Kindle blue-slate `#162330`/`#2C5278`/`#4DA6FF`, `636h REMAINING`).
     * Interactive verification: tapped the tile strip on hardware and verified transition directly into `2026 Reading Goal` calibrator (`READING SPEED (WPM)`).
2. **`verify_r132_about_v132.png` (339,562 bytes)**:
   - Captured **About Folio** modal showing `Version: Folio v13.2 KFX (Enhanced Typesetting)`, `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`, and styled Kindle action buttons (`CLOSE`, `CLEAR TEMP CACHE`).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.
