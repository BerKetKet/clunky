# Round 113 Deliverables Report — Folio v11.3 KFX (Smart Metadata Quick-Fix Toolbar)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.3 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:20:02`.

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMetadataCatalogDialog.java`, `FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
     - **Smart Metadata Quick-Fix Toolbar (`showEditMetadataDialog`)**: Added 3-pill toolbar beneath Title & Author input fields:
       * **`⇄ Swap T/A`** (soft-violet `#C084FC` on `#2B1B3D` with rounded stroke): Instantly swaps Title & Author fields to fix inverted metadata (e.g., *Palahniuk, Chuck / Choke*).
       * **`Aa Fix Author`** (sky-blue `#38BDF8` on `#082F49` with rounded stroke): Normalizes author format from `Last, First` to natural `First Last` (`Chuck Palahniuk`).
       * **`Aa Title Case`** (warm amber `#F59E0B` on `#331E05` with rounded stroke): Smart-capitalizes lowercase or inconsistent titles with smart lowercase stop-word handling (`a`, `an`, `the`, `of`, `in`, `to`, `for`).
     - **Immediate In-Memory Record Sync**: Saving updates in-memory `BookRecord` attributes alongside `books_metadata` SQLite and `kindle_library.db` sync.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.3 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (384,276 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:20:02`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.3_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r113_edit_metadata_quickfix.png` (149,786 bytes)**:
   - Captured **Edit Book Metadata** dialog for *Choke* opened from Book Details (`ⓘ`).
   - Shows the 3 quick-fix toolbar pills: `⇄ Swap T/A`, `Aa Fix Author`, and `Aa Title Case`.
   - Demonstrates 1-tap `⇄ Swap T/A` followed by `Aa Fix Author`: Title successfully updated to `Choke` and Author updated to `Chuck Palahniuk` prior to pressing Save.
2. **`verify_r113_about_v113.png` (354,685 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.3 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:

> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
