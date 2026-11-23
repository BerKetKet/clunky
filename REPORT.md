# Round 102 Deliverables Report — Folio v10.2 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.2 KFX (Enhanced Typesetting)` installed at `2026-11-23 02:59:27`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
     - Exposed public helper `FolioMetadataCatalogDialog.showBookDetails(final Context ctx, final FolioMetadataDb.BookRecord rec, final int currentProgress)` to open full book metadata details directly from external UI components.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **1-Tap `ⓘ` Book Details Launcher**: Added dedicated slate-gray `ⓘ` button right beside `Read ›` on every Quick Library Search result row. Tapping `ⓘ` saves recent search query, dismisses search modal, and launches full Book Details dialog.
     - **Styled `📖` Cover Art Placeholder**: Encapsulated cover art in rounded `#0D1017` `FrameLayout` (border `#232A3A`, 4dp radius). If a book has no `.cover.jpg` bitmap (e.g. *The 210th Day*), renders a crisp centered `📖` glyph, eliminating blank black squares.
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.2 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (352,404 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.2_kfx/base.apk` (137,473,734 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r102_search_info_placeholder.png` (294,642 bytes)**:
   - Captured Quick Library Search modal with `📖 Reading` filter pill active.
   - Verified *The 210th Day* displays the styled rounded frame with centered `📖` glyph fallback instead of an empty square.
   - Verified new `ⓘ` button beside `Read ›` on every reading card row.
2. **`verify_r102_about_v102.png` (341,699 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.2 KFX (Enhanced Typesetting)`.
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
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.2_kfx/base.apk`
- Archive: `antig_to_gemini_neon_clunky_report102.zip` containing `REPORT.md`, `INSTRUCTIONS_ROUND_102.md`, `protocol.json`, `verify_r102_search_info_placeholder.png`, and `verify_r102_about_v102.png`.
