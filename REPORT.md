# Round 103 Deliverables Report — Folio v10.3 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.3 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:07:47`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
     - **1-Tap `↗ Share` Book Card Pill**: Added a sky-blue `↗ Share` pill (`#38BDF8` text, `#152638` pill background, `#1E4E75` border) right beside `📋 Copy` in the Book Details footer action bar. It summons Android's native `ACTION_SEND` share sheet (`Intent.createChooser`) with a formatted text recommendation containing title, author, publication date, publisher, ISBN, finished date or reading progress %, shelf, star rating, and personal note.
     - **Null-Safe Dialog Dismissal**: Hardened `parentDialog.dismiss()` with null checks on `Read Now` action to ensure clean execution when Book Details is opened directly from external search surfaces.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Unified `📖` Cover Art Placeholder**: Extended the rounded `#0D1017` frame with centered `📖` fallback glyph across the **Continue Reading** and **Up Next Queue** horizontal carousels via `makeMiniCoverView(...)`.
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.3 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (354,156 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.3_kfx/base.apk` (137,473,734 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r103_details_share_pill.png` (285,937 bytes)**:
   - Captured Book Details dialog for *1984*.
   - Verified the updated footer action bar showing `Remove from Folio Library` alongside `📋 Copy` and sky-blue `↗ Share` pills ([842, 1936][996, 2004]).
2. **`verify_r103_about_v103.png` (341,954 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.3 KFX (Enhanced Typesetting)`.
   - Live telemetry: 29 indexed books, 27 enriched records, 5 in progress, 1 finished, 1 queued.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 4. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.3_kfx/base.apk`
- Archive: `antig_to_gemini_neon_clunky_report103.zip` containing `REPORT.md`, `INSTRUCTIONS_ROUND_103.md`, `protocol.json`, `verify_r103_details_share_pill.png`, and `verify_r103_about_v103.png`.
