# Folio Milestone Release Report: Home "All Metadata ›" Link & Clean Library Book Removal (Round 046)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.2 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 046, Folio reached version **Folio v4.2 KFX**. Gemini delivered `OpenCatalogView.java` with the Home "All Metadata ›" link and designed the "Remove from Folio Library" action. Antigravity integrated `OpenCatalogView.java`, updated `FolioMetadataCatalogDialog.java` with the red remove action and safe deletion cascade (removing from `folio_metadata.db`, `kindle_library.db`, and app-private cache while leaving original EPUBs intact), and bumped `FolioMoreView.java` to Folio v4.2 KFX. After compiling `classes9.dex` (195,312 B) and rebuilding the APK, the package was deployed to the physical Samsung Galaxy M32 and verified across two hardware screenshots.

---

## 2. Integrated Features in Folio v4.2 KFX

### A. Home "All Metadata ›" Direct Link
- In `OpenCatalogView.java`, integrated a teal `All Metadata ›` shortcut link (`ACCENT_TEAL`) directly aligned to the right of the `Continue Reading` section header.
- Tapping this link launches `FolioMetadataCatalogDialog.show(ctx)` immediately from the Home tab, letting users jump straight into full metadata browsing, search, and sorting without navigating to the More tab.

### B. Safe Library Removal Action
- In `FolioMetadataCatalogDialog.java`, added a red `🗑 Remove from Folio Library` (`#EF4444`) action inside the scrollable Book Details (`ⓘ`) dialog.
- Features an explicit confirmation dialog: `"Remove [Book] from Folio Library? Original files on storage will not be deleted."`
- Cascades cleanup cleanly:
  - Deletes row from `folio_metadata.db` (`books_metadata`).
  - Cleans up registration records in `kindle_library.db` (`LocalContent`, `KindleContent`, `UserContent`).
  - Purges app-private converted `.kfx` copies in `files/folio_library/` and cached cover thumbnails in `files/folio_covers/`.
  - External storage files (e.g. in `/sdcard/en/0` or `/sdcard/Download`) are strictly preserved.

### C. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v4.2 KFX`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Home "All Metadata ›" Link** | Home Screen → Continue Reading Header | `screenshots/verify_r46_home_all_metadata_link.png` | **PASS** — Home screen displays `Continue Reading` with the teal `All Metadata ›` link cleanly aligned on the right. |
| **Book Details Remove Action** | Home → `All Metadata ›` → `ⓘ` on Book | `screenshots/verify_r46_book_details_remove_action.png` | **PASS** — Book Details popup displays full metadata and the red `🗑 Remove from Folio Library` action button. |
| **Stock Package Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` completely untouched and intact. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifact Verification

- Staged Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB)
- Hardware Screenshots:
  - `screenshots/verify_r46_home_all_metadata_link.png` (1.2 MB)
  - `screenshots/verify_r46_book_details_remove_action.png` (200 KB)
- Archive Budget: `antig_to_gemini_neon_clunky_report046.zip` (~1.4 MB total, well below 5 MB ceiling).
