# Round 107 Deliverables Report — Folio v10.7 KFX
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.7 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:41:09`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`:
     - **⬆ Parent Folder Button**: Added a compact, styled `⬆` parent-directory button directly to the left of the path input, enabling 1-tap upward directory traversal (`/sdcard/en/0` -> `/sdcard/en` -> `/sdcard`).
     - **Clickable 📁 Subfolder Rows**: Renders non-hidden subdirectories at the top of the folder list as clickable warm-amber `📁 <folderName>  Open ›` cards, allowing instant drilling into nested EPUB folders without typing paths manually.
     - **Hardened Event Binding**: Cleanly decoupled `btnUpDir` click handling from the quick-chip loop so parent navigation works immediately upon dialog opening and dynamically refreshes pinned/quick directory chips.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.7 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (366,152 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk` (137,486,022 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r107_importer_subfolders.png` (137,798 bytes)**:
   - Captured Batch EPUB Importer after stepping up from `/sdcard/en/0` to `/sdcard/en` via the new `⬆` parent button ([83, 558][153, 645]).
   - Verified path input updated to `/sdcard/en` ([169, 557][742, 646]).
   - Verified clickable subfolder rows rendered at top of list:
     - `📁 0  Open ›` ([115, 968][970, 1015])
     - `📁 1  Open ›` ([115, 1079][970, 1126])
   - Tapped `📁 0` to verify seamless downward navigation and restore `/sdcard/en/0` as `last_import_dir` (displaying all 719 EPUBs).
2. **`verify_r107_about_v107.png` (341,434 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.7 KFX (Enhanced Typesetting)`.
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
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk`
- Hardware Screenshots:
  - `verify_r107_importer_subfolders.png`
  - `verify_r107_about_v107.png`
- Instructions & Protocol: `INSTRUCTIONS_ROUND_107.md`, `protocol.json`
