# Folio Milestone Release Report: Live Reading Progress Badges & Metadata Polish (Round 047)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.3 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 047, Folio advanced to **Folio v4.3 KFX**. Gemini provided drop-in updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both drop-in files, compiled `classes9.dex` (197,224 B), rebuilt the APK via Apktool, aligned and signed it with Android SDK 36.0.0, deployed the build to Samsung Galaxy M32, and captured two hardware verification screenshots confirming the new live reading progress badges in `Sort: Progress` mode and the metadata display cleanup.

---

## 2. Integrated Features in Folio v4.3 KFX

### A. Live Reading Progress Badges & "Sort: Progress"
- In `FolioMetadataCatalogDialog.java`, queried live reading progress (`UserContent.READING_PROGRESS` & `KindleContent.READ_STATE`) directly from `kindle_library.db`.
- Renders crisp teal **`X% read`** badges on book cards.
- Expanded the Sort pill cycle to include **`Sort: Progress`** (`Recent` → `Title` → `Author` → `Year` → `Progress`), floating actively read books (e.g. *Brave New World* at 9%, *Aristotle* at 8%, *The 210th Day* at 4%) directly to the top.

### B. Metadata Display Polish
- `cleanGenresForDisplay()`: Dynamically filters Calibre reading status noise tags (`Read`, `Unread`, `To Read`, etc.) out of card genre lines and Book Details popups. *Animal Farm* now displays clean `Genres: Fiction`.
- Suppresses undefined language tags (e.g. `Language: UND`), eliminating unwanted visual clutter in Book Details.

### C. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v4.3 KFX`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Sort: Progress & Progress Badges** | Library Metadata & Genres Dialog → Sort Pill | `screenshots/verify_r47_sort_progress_badges.png` | **PASS** — `Sort: Progress` mode floats books with live progress badges (`9% read`, `8% read`, `4% read`) to the top. |
| **Clean Book Details (No Noise / UND)** | Library Metadata & Genres → `ⓘ` on Animal Farm | `screenshots/verify_r47_clean_book_details.png` | **PASS** — Book Details popup displays crisp `Genres: Fiction` without `"Read"` noise tag, and completely suppresses `Language: UND`. |
| **Stock Package Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` completely untouched and intact. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifact Verification

- Staged Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB)
- Hardware Screenshots:
  - `screenshots/verify_r47_sort_progress_badges.png` (373 KB)
  - `screenshots/verify_r47_clean_book_details.png` (192 KB)
- Archive Budget: `antig_to_gemini_neon_clunky_report047.zip` (~650 KB total, well below 5 MB ceiling).
