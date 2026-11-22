# Folio Milestone Release Report: Storage Footprint & Duplicate Titles Manager (Round 088)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v8.8 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.8_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 088, Folio advanced to **Folio v8.8 KFX**. Gemini pre-authored drop-in Java updates for `FolioMoreView.java`. Antigravity integrated the update into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`, compiled Dalvik DEX (`classes9.dex`, 305,060 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **💾 Storage & Duplicates Action Card (`FolioMoreView.java`)**: Added a dedicated action card to Control Center situated directly below `🏷 Manage Shelves` (`"Inspect largest KFX books, cache size & duplicate titles"`).
2. **Interactive Storage & Duplicates Modal Subsystem**:
   - **STORAGE FOOTPRINT**: 3 centered live storage telemetry tiles showing **App Library** (`13.3 MB`), **Backups** (`682 KB`), and **Temp Cache** (`1.3 MB`).
   - **LARGEST BOOKS (TAP TO LOCATE)**: Top 5 largest cataloged KFX books with amber horizontal proportional size bars and 1-tap locate drill-down into the Metadata Catalog:
     * *Complete Works of Homer* (31.8 MB)
     * *Cicero - Delphi Complete Works* (26.3 MB)
     * *Complete Works of Plutarch* (24.6 MB)
     * *Delphi Complete Works of Aristotle* (22.9 MB)
     * *Complete Works of Xenophon* (11.1 MB)
   - **DUPLICATE TITLES AUDIT**: Title normalization scanner over all cataloged volumes dynamically displaying an emerald badge (`✓ No duplicate titles found across 27 cataloged books`).
   - **Cache Reclamation**: Includes a 1-tap `Clear Temp Cache` button allowing instant purge of staging/open-once scratch files and live modal refresh.
3. **Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v8.8 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v8.8 KFX

### A. Storage & Duplicates Subsystem (`FolioMoreView.java`)
- Storage calculation:
  ```java
  long appLibBytes = dirSizeBytes(new File(ctx.getFilesDir(), "folio_library"))
          + dirSizeBytes(new File(ctx.getFilesDir(), "folio_covers"));
  long backupBytes = dirSizeBytes(new File("/sdcard/kindle/backups"));
  long tempCacheBytes = dirSizeBytes(new File(ctx.getCacheDir(), "folio_once"))
          + dirSizeBytes(new File(ctx.getCacheDir(), "folio_staging"));
  ```
- Largest books sorting & bar visualization:
  - Iterates cataloged books, measures corresponding KFX payload size on disk, and extracts Top 5 largest.
  - Constructs amber horizontal proportional bars (`0xFFF59E0B`) with 1-tap click listener routing into `FolioMetadataCatalogDialog.showWithSearch(ctx, item.rec.title)`.
- Duplicate titles audit:
  - Normalizes book titles (lowercase, punctuation stripped).
  - Groups books by normalized title and flags clusters with count > 1 or renders emerald pass badge.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Control Center Card & Badge** | More Tab (Control Center) | `verify_r88_control_center_v88.png` | **VERIFIED**: Opened Control Center; verified new `💾 Storage & Duplicates` card positioned directly below `🏷 Manage Shelves` and confirmed top badge reads `Folio v8.8 KFX`. |
| **Storage & Duplicates Modal** | Storage & Duplicates Dialog | `verify_r88_storage_duplicates_modal.png` | **VERIFIED**: Tapped card; verified modal showing 3 storage tiles (`13.3 MB App Library`, `682 KB Backups`, `1.3 MB Temp Cache`), Top 5 Largest Books amber bars (*Homer*, *Cicero*, *Plutarch*, *Aristotle*, *Xenophon*), and Duplicate Titles Audit status (`✓ No duplicate titles found across 27 cataloged books`). |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
