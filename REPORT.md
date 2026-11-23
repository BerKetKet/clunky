# Round 105 Deliverables Report — Milestone Folio v10.5 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Milestone Folio v10.5 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:22:12`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
     - **Exact Star Rating Filter Support**: Extended `FolioMetadataCatalogDialog.show(...)` to accept `STAR:1..5` filter parameters.
     - **Active Filter Chip**: Displays a dedicated active gold `★ {N}-Star ({count})` chip in the top genre/filter bar when active.
     - **Predicate Filtering**: Strictly filters catalog books to matching ratings via `matchesStarFilter(ctx, rec, starStr)`.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Drill-Down Navigation**: Wired Star Ratings distribution bars in `📊 Library Insights` to launch `FolioMetadataCatalogDialog.show(ctx, "STAR:" + starTarget)`.
     - **`★ Rated` Scope Pill in Quick Search**: Added 6th scope pill (`All`, `📖 Reading`, `★ Rated`, `📝 Notes`, `📌 Queue`, `🏷 Shelves`) to Quick Library Search for instant rated volume isolation.
     - **Version Bump**: Bumped badge and About Folio dialog to `Milestone Folio v10.5 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (358,032 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.5_kfx/base.apk` (137,477,830 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r105_star_filter_drilldown.png` (220,592 bytes)**:
   - Captured Metadata Catalog after tapping the `★★★★★ (5★)` bar in `📊 Library Insights`.
   - Verified active gold `★ 5-Star (1)` filter chip ([260, 684][480, 760]) showing *★ 1984* ([99, 1197][359, 1292]) with *★ 5/5* rating badge ([99, 1297][314, 1334]).
2. **`verify_r105_about_v105.png` (341,817 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.5 KFX (Enhanced Typesetting)`.
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
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.5_kfx/base.apk`
- Archive: `antig_to_gemini_neon_clunky_report105.zip` containing `REPORT.md`, `INSTRUCTIONS_ROUND_105.md`, `protocol.json`, `verify_r105_star_filter_drilldown.png`, and `verify_r105_about_v105.png`.
