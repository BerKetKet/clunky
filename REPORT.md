# Folio Milestone Release Report: Accurate Reading Progress SQL Fix & Dynamic Reading Filter Chip (Round 051)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v5.1 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v5.1_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 051, Folio reached **Folio v5.1 KFX**. Gemini delivered pre-authored drop-in updates for `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`. Antigravity integrated both drop-ins (preserving Round 049's resilient HTTP fallback in `MetadataEnricher.java`), compiled `classes9.dex` (202,356 B), built, aligned, signed, and deployed the APK to the Samsung Galaxy M32 hardware. Hardware verification confirmed the new teal `Reading (5)` quick-filter chip in the catalog header (which immediately isolates all in-progress books showing their `X% read` badges) and the corrected live `Reading Progress: 5 in progress · 0 finished` statistics line in About Folio.

---

## 2. Integrated Features in Folio v5.1 KFX

### A. Accurate Reading Progress SQL Fix (`FolioMoreView.java`)
- Fixed SQLite query in `showAboutDialog()`: `KindleContent.READ_STATE` stores text (`'READ'` / `'UNREAD'`), which previously caused integer comparison `> 0` to misclassify all books as finished.
- Implemented robust SQL queries:
  - **Finished**: `UPPER(COALESCE(k.READ_STATE, '')) = 'READ' OR COALESCE(u.READING_PROGRESS, 0) >= 100`
  - **In Progress**: `COALESCE(u.READING_PROGRESS, 0) > 0 AND COALESCE(u.READING_PROGRESS, 0) < 100 AND UPPER(COALESCE(k.READ_STATE, '')) != 'READ'`
- Accurately reports: `• Reading Progress: 5 in progress · 0 finished`.

### B. Dynamic Reading (N) Quick-Filter Chip (`FolioMetadataCatalogDialog.java`)
- Dynamically scans in-progress books from `kindle_library.db` and inserts a high-contrast teal filter chip (`#5EEAD4` text, `#14B8A6` active background) right beside `All (27)`.
- When selected, filters the list to only books where `progress > 0 && progress < 100`.
- Verified live rendering:
  - *The 210th Day*: `4% read` badge
  - *Delphi Complete Works of Sophocles*: `2% read` badge
  - *Delphi Complete Works of Aristotle*

### C. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v5.1 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Reading (5) Filter Chip & In-Progress Badges** | Home → All Metadata › | `screenshots/verify_r51_reading_chip_filter.png` | **PASS** — Tapping teal `Reading (5)` chip immediately filters catalog down to the 5 in-progress books, displaying their `4% read` and `2% read` badges with zero crashes. |
| **Accurate Reading Stats in About Folio** | More Tab → About Folio | `screenshots/verify_r51_about_v51_accurate_stats.png` | **PASS** — About Folio displays `Version: Folio v5.1 KFX (Enhanced Typesetting)` and corrected live `Reading Progress: 5 in progress · 0 finished` (perfect parity with the 5 reading chip items). |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report051.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_051.md`
  - `protocol.json`
  - `screenshots/verify_r51_reading_chip_filter.png`
  - `screenshots/verify_r51_about_v51_accurate_stats.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v5.1_kfx/base.apk` (131 MB)
