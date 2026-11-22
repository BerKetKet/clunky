# Folio v3.7 KFX Hardware Verification & Release Report (Round 040)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v3.7 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 040 delivers **Missing-Cover Auto-Repair, Catalog Sort Controls, and Editable Metadata** for Folio v3.7 KFX:
1. **Cover Auto-Repair (`Sync & Enrich`)**: Upgraded `LibraryMetadataBackfiller.java` with a dedicated Pass 2 repair sweep. For books with missing cover thumbnails (such as *The 210th Day*), it resolves sibling variants and automatically downloads the verified cover image via Open Library's Covers API (`https://covers.openlibrary.org/b/isbn/<isbn>-M.jpg?default=false`) into app-private storage (`files/folio_covers/<bookKey>.jpg`). Added explicit HTTP 302 cross-host redirect handling to follow Archive.org downloads. *The 210th Day* now renders with a rich 17.8 KB cover thumbnail (eliminating the blank gray box).
2. **Catalog Sort Controls**: Added a top-row `Sort` pill (`Sort: Recent` → `Sort: Title` → `Sort: Author` → `Sort: Year`) to `FolioMetadataCatalogDialog.java`. Tapping cycles through sort modes and instantly re-orders all 21 library books cleanly.
3. **Editable Metadata Form**: Added an `Edit Metadata` button inside the Book Details (`ⓘ`) popup. Opens a clean dark form pre-populated with Title, Author, Published Date / Year, Publisher, Genres, and ISBN. Tapping `Save` persists updates cleanly into `folio_metadata.db` and immediately refreshes the active catalog.
4. **Control Center Version Bump**: Bumped version badge in `FolioMoreView.java` and internal engine string in the **About Folio** dialog to **`Folio v3.7 KFX`**.

---

## 2. Drop-In Integration & Technical Details (Task A)

- **Drop-In Files**:
  - `LibraryMetadataBackfiller.java` and `FolioMetadataCatalogDialog.java` updated in `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
  - Preserved all Round 038 & 039 fixes in `FolioMetadataDb.java`, `MetadataEnricher.java`, and `EpubMetadataExtractor.java`.
- **Key Enhancements**:
  1. *Cover Auto-Repair Pass*: In `LibraryMetadataBackfiller.java`, Pass 2 sweeps existing records for `r.coverPath == null || !new File(r.coverPath).exists()`. Resolves sibling covers (`.cover.jpg`, `.azw3.cover.jpg`, `.kfx.cover.jpg`, `.jpg`) or downloads from Open Library Covers API by ISBN.
  2. *HTTP 302 Redirect Following*: Added explicit redirect handling in `downloadCover()` to follow 301/302 redirects across domains (from `covers.openlibrary.org` to `archive.org`), properly writing the image stream to `files/folio_covers/<bookKey>.jpg`.
  3. *Sort Controller*: Added `btnSort` cycling through `Recent`, `Title A-Z`, `Author A-Z`, and `Year`.
  4. *Edit Metadata Dialog*: Added `showEditMetadataDialog` with pre-filled `EditText` fields and SQLite transaction update on `books_metadata`.
- **Build & Badge**:
  - `FolioMoreView.java` badge and `About Folio` version string updated to **`Folio v3.7 KFX`**.
  - `classes9.dex` recompiled (174,188 bytes) and injected as `classes4.dex`.
  - Rebuilt with Apktool, aligned, signed, and deployed to Galaxy M32.

---

## 3. Hardware Verification Results (Task B)

- **Cover Auto-Repair Verification**:
  - Tapped `Sync & Enrich` in the catalog dialog. Pass 2 detected missing cover for *The 210th Day* (ISBN `0804833206`).
  - Downloaded 17,825-byte cover image to `/data/user/0/com.amazon.kindle.epub/files/folio_covers/FOLIO_F5C88D207A8D.jpg` and updated SQLite row `cover_path`.
  - Verified *The 210th Day* renders with a crisp, genuine cover thumbnail on physical hardware with zero gray placeholder boxes.
- **Sort Controls Verification**:
  - Tapped `Sort: Recent`: Displays *The 210th Day* at top.
  - Tapped to `Sort: Title`: Re-orders cleanly with *Brave New World* (B) and *Cicero* (C) at top.
  - Cycled through `Author` and `Year` without UI lag or crashes.
- **Edit Metadata Verification**:
  - Tapped `ⓘ` on *The 210th Day* → tapped `EDIT METADATA`.
  - Form opened displaying all pre-populated metadata fields.
  - Tapped `SAVE`: Database updated cleanly and dialog returned to the catalog view.

---

## 4. Visual Verification Proofs (Task C)

Three physical hardware screenshots captured on Samsung Galaxy M32:
1. `screenshots/verify_r40_covers_fixed_and_sorted.png` (420 KB): Catalog dialog after `Sync & Enrich` showing *The 210th Day* with its real cover thumbnail and the active `Sort: Recent` pill.
2. `screenshots/verify_r40_edit_metadata_dialog.png` (129 KB): Dark **Edit Book Metadata** form opened from Book Details with pre-filled fields (`Title`, `Author`, `Published Date / Year`, `Publisher`, `Genres`, `ISBN`) and `SAVE` / `CANCEL` actions.
3. `screenshots/verify_r40_about_v37.png` (288 KB): Control Center **About Folio** dialog showing `Version: Folio v3.7 KFX (Enhanced Typesetting)` and live engine stats.

---

## 5. Architectural Directive on Cover Handling

As instructed by the Project Lead, we reiterate the architectural directive for future rounds:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 6. Staging & Package Deliverables

- **Release APK**: Staged signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.7_kfx/base.apk` (131 MB).
- **Stock Integrity**: Stock package `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED).
- **Delivery Archive**: `antig_to_gemini_neon_clunky_report040.zip` (< 15 MB) containing `REPORT.md`, `INSTRUCTIONS_ROUND_040.md`, `protocol.json`, 3 hardware screenshots, and concise verification logs.
