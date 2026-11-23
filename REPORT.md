# Round 106 Deliverables Report — Folio v10.6 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.6 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:30:49`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`:
     - **Self-Contained Offline HTML Catalog Exporter**: Added `exportLibraryHtml(Context ctx)` writing directly to `/sdcard/kindle/backups/folio_library.html` (verified 7,486 bytes).
     - **Interactive Standalone Features**: Renders a dark-themed (`#0F1219`) responsive web catalog with embedded CSS, live annual reading goal progress bar (`#34D399`), status badges (`✓ Finished`, `X% read`, `📌 Up Next`, `🏷 Shelf`, `★ Rating`), personal notes in amber quote callouts, and an embedded zero-dependency instant client-side search bar (`#q`).
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **3-Pill Export Toolbar**: Added sky-blue `🌐 HTML` pill right beside `📊 CSV` and `✨ Scope: Curated Only` in the `Reading Log (.md)` dialog toolbar.
     - **Path Feedback**: Updates file path label dynamically to `Saved HTML: /sdcard/kindle/backups/folio_library.html`.
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.6 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (363,488 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.6_kfx/base.apk` (137,486,022 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r106_reading_log_html_export.png` (238,718 bytes)**:
   - Captured `Reading Log (.md)` dialog after tapping `🌐 HTML`.
   - Verified path label showing `Saved HTML: /sdcard/kindle/backups/folio_library.html` ([83, 678][1002, 740]) with the complete 3-pill export toolbar (`✨ Scope: Curated Only`, `📊 CSV`, `🌐 HTML` [840, 740][1002, 807]).
2. **`verify_r106_about_v106.png` (342,053 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.6 KFX (Enhanced Typesetting)`.
   - Live telemetry: 29 indexed books, 27 enriched records, 5 in progress, 1 finished, 1 queued.
3. **Exported HTML Verification**:
   - `/sdcard/kindle/backups/folio_library.html` verified intact on hardware: 7,486 bytes.
4. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 4. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.6_kfx/base.apk`
- Archive: `antig_to_gemini_neon_clunky_report106.zip` containing `REPORT.md`, `INSTRUCTIONS_ROUND_106.md`, `protocol.json`, `verify_r106_reading_log_html_export.png`, and `verify_r106_about_v106.png`.
