# Folio Milestone Release Report: Folio v4.0 KFX Polish (Round 044)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.0 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 044, the team achieved the milestone release of **Folio v4.0 KFX**. Gemini delivered drop-in implementations for `EpubMetadataExtractor.java`, `FolioMetadataCatalogDialog.java`, and `FolioMoreView.java`. Antigravity integrated the drop-in sources, compiled `classes9.dex` (191,888 B), rebuilt the APK via apktool and Android SDK 36.0.0, deployed to the physical Samsung Galaxy M32, and captured two crisp hardware verification screenshots proving both functional and cosmetic polish.

---

## 2. Integrated Improvements in Folio v4.0 KFX

### A. Publication Date Normalization
- `EpubMetadataExtractor.normalizePubDate()`: Strips verbose ISO timestamps, time offsets, and trailing whitespace (e.g. `2000-07-15 00:00:00+01:00` or `2014-06-06T00:00:00Z` → clean `2000-07-15`).
- Applied dynamically in `FolioMetadataCatalogDialog.java` for both catalog list cards and Book Details popups, immediately cleaning up existing database entries without requiring full re-indexing.

### B. Calibre Noise Tag Filtering
- `EpubMetadataExtractor.isNoiseTag()`: Filters out noisy reading state tags (`Read`, `Unread`, `To Read`, `Ebook`, `Calibre`, `General`, `Fiction - General`) from EPUB extraction.
- In `FolioMetadataCatalogDialog.java`, the genre filter chip bar excludes noise tags, leaving clean, authentic literary genre categories (`Fiction`, `19th Century Fiction`, `Modern Fiction`, `Literary Criticism`).

### C. Live Storage Breakdown & One-Tap Cache Clearing
- Bumped Control Center badge and About dialog to **`Folio v4.0 KFX`**.
- Upgraded **About Folio** with live file-tree measurements:
  - **Indexed Library Books:** 28
  - **Enriched Metadata Records:** 26
  - **App Library Storage:** 11.1 MB (`files/folio_library/` + `files/folio_covers/`)
  - **Legacy /sdcard/kindle:** 203.0 MB
  - **Temp Open-Once Cache:** 1.3 MB (`cache/folio_once/` + `cache/folio_staging/`)
- Added one-tap **`CLEAR TEMP CACHE`** button in the About Folio dialog, providing immediate cleanup with a user-facing Toast reporting freed megabytes.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Clean Date & Noise-Free Genre Chips** | Control Center → Library Metadata & Genres | `screenshots/verify_r44_clean_dates_and_genres.png` | **PASS** — *Animal Farm* displays crisp `2000-07-15` (no `00:00:00+01:00`); genre chips bar displays `Fiction`, `19th Century Fiction`, `Modern Fiction`, `Literary Criticism` with `"Read"` cleanly filtered out. |
| **Folio v4.0 About & Cache Clearing** | Control Center → About Folio | `screenshots/verify_r44_about_v40_storage_and_clear_cache.png` | **PASS** — Shows `Folio v4.0 KFX`, full live storage breakdown (11.1 MB App Library, 1.3 MB Temp Cache), and `CLEAR TEMP CACHE` button. |
| **Stock Package Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` completely untouched and intact. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifact Verification

- Staged Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB)
- Hardware Screenshots:
  - `screenshots/verify_r44_clean_dates_and_genres.png` (336 KB)
  - `screenshots/verify_r44_about_v40_storage_and_clear_cache.png` (307 KB)
- Archive Budget: `antig_to_gemini_neon_clunky_report044.zip` (< 1.5 MB total, well below 5 MB ceiling).
