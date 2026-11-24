# Round 118 Deliverables Report — Folio v11.8 KFX (Saved Backups & Exports File Browser)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.8 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 04:01:29`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Interactive `Backups ›` Footprint Tile & Saved Backups Browser (`showSavedBackupsDialog`)**: Tapping the emerald `Backups ›` tile inside `💾 Storage & Duplicates` opens an interactive dark file browser targeting `/sdcard/kindle/backups/`:
       * Sorts existing files newest-first.
       * Displays color-coded file extension badges (`HTML` sky-blue, `MD` violet, `JSON` amber, `CSV` emerald, `DB` slate).
       * Displays human-readable file sizes and modification timestamps.
       * Inline **`👁` Monospace File Preview** (`showBackupFilePreviewDialog`): Allows reading text backups directly inside a scrollable monospace dialog with a dedicated `📋 Copy` action.
       * Inline **`✕` Delete Action**: Allows removing outdated backups with confirmation prompt and live list refresh.
     - **Synced `presetLp` Variable Scope**: Cleanly integrated the Round 117 variable scoping in `showReadingGoalDialog`.
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.8 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (410,476 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 04:01:29`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.8_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r118_saved_backups_browser.png` (220,317 bytes)**:
   - Captured **📦 Saved Backups & Exports (11 files)** modal opened from `💾 Storage & Duplicates › Backups ›`.
   - Shows directory `/sdcard/kindle/backups`.
   - Lists exported files with badges:
     * `MD` `folio_reading_notes.md` (114 B · 2026-11-24 03:38)
     * `HTML` `folio_library.html` (7 KB · 2026-11-23 03:32)
     * `MD` `folio_reading_log.md` (1 KB · 2026-11-23 03:32)
     * `CSV` `folio_library.csv` (3 KB · 2026-11-22 23:18)
     * `DB` `20261122_181316_kindle_library.db` (196 KB)
   - Shows action buttons `👁` (preview) and `✕` (delete) on each file row.
2. **`verify_r118_about_v118.png` (354,531 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.8 KFX (Enhanced Typesetting)`
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
- Archive: `antig_to_gemini_neon_clunky_report118.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_118.md`
  * `protocol.json`
  * `verify_r118_saved_backups_browser.png`
  * `verify_r118_about_v118.png`
