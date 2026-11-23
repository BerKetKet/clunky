# Round 104 Deliverables Report — Folio v10.4 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.4 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:14:33`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Star Ratings Distribution Bar Chart**: Built a gold `STAR RATINGS · 5.0★ AVG (1 RATED) (TAP TO BROWSE)` distribution section at the base of `📊 Library Insights` showing per-star ratings breakdown (`★★★★★ (5★)` down to `★ (1★)`) with interactive drill-down browsing.
     - **Shareable Library Insights Summary**: Added comprehensive plaintext snapshot generation (total books, status breakdown, annual reading goal %, curation metrics, metadata health %, publication span `1531–2017`, top genres, top authors).
     - **1-Tap `COPY SUMMARY` & `SHARE` Buttons**: Added neutral `COPY SUMMARY` (clipboard) and negative `SHARE` (`ACTION_SEND` chooser) buttons to the Library Insights dialog footer.
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.4 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (357,084 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.4_kfx/base.apk` (137,477,830 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r104_insights_ratings_share.png` (135,189 bytes)**:
   - Captured scrolled `📊 Library Insights` dialog.
   - Verified the gold `STAR RATINGS · 5.0★ AVG (1 RATED) (TAP TO BROWSE)` header ([83, 1491][991, 1590]) with `★★★★★ (5★)` distribution row ([83, 1590][977, 1637]).
   - Verified the updated 3-button footer row: `CLOSE`, `COPY SUMMARY`, and `SHARE` ([41, 1754][1044, 1880]).
2. **`verify_r104_about_v104.png` (341,437 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.4 KFX (Enhanced Typesetting)`.
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
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.4_kfx/base.apk`
- Archive: `antig_to_gemini_neon_clunky_report104.zip` containing `REPORT.md`, `INSTRUCTIONS_ROUND_104.md`, `protocol.json`, `verify_r104_insights_ratings_share.png`, and `verify_r104_about_v104.png`.
