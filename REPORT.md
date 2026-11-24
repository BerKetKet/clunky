# Round 114 Deliverables Report — Folio v11.4 KFX (Authors Directory Sort Pills & Progress Telemetry)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.4 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:28:54`.

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMetadataCatalogDialog.java`, `FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
     - **Interactive Sort Switcher (`showAuthorsDirectoryDialog`)**: Added 3 sort pills above the search bar:
       * **`Most Books`**: Default catalog volume ranking.
       * **`A–Z`**: Alphabetical collation by author name.
       * **`Most Read`**: Ranks authors by active reading engagement (finished books first, followed by in-progress reading, then highest average rating).
     - **Circular Monogram Avatars (`authorMonogram(...)`)**: Formats 2-letter initials inside rounded badges (teal `#5EEAD4` on `#0E2A28` for active/finished authors; amber `#F59E0B` on `#2B1F0A` for unread).
     - **Live Per-Author Reading & Rating Telemetry**: Renders live engagement sub-lines beneath each author title (e.g., `✓ 1 finished`, `📖 2 reading`, `★ 5.0`).
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.4 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (388,080 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:28:54`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.4_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r114_authors_directory.png` (186,067 bytes)**:
   - Captured **👤 Authors Directory** modal sorted by **`Most Read`**.
   - Shows active/finished/rated authors at the top:
     * *Herman Melville* (`HM` monogram, `✓ 1 finished`)
     * *Aldous Huxley* (`AH` monogram, `📖 2 reading`)
     * *Aristotle* (`AR` monogram, `📖 1 reading`)
     * *Natsume Soseki* (`NS` monogram, `📖 1 reading`)
     * *Sophocles* (`SO` monogram, `📖 1 reading`)
     * *George Orwell* (`GO` monogram, `★ 5.0`)
   - Verifies all 3 sort pills (`Most Books`, `A–Z`, `Most Read`), circular monogram avatars, and per-author telemetry rendering.
2. **`verify_r114_about_v114.png` (353,937 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.4 KFX (Enhanced Typesetting)`
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
