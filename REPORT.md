# Folio Milestone Release Report: RFC-4180 CSV Library Export & Reading Goal in Markdown/JSON Backup (Round 072)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.2 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.2_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 072, Folio advanced to **Folio v7.2 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataBackup.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 245,752 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **RFC-4180 CSV Library Export (`FolioMetadataBackup.java`)**: Added `exportLibraryCsv(Context)` producing a structured CSV spreadsheet saved directly to `/sdcard/kindle/backups/folio_library.csv` (verified 3,289 bytes). Includes columns `Title,Author,Year,Publisher,ISBN,Status,Shelf,Rating,Favorite,Note` with strict CSV quote escaping.
2. **Reading Goal in Markdown & JSON Backup (`FolioMetadataBackup.java`)**: Injected `**<Year> Reading Goal:** <finished> / <target> books finished (<pct>%)` directly into the Markdown Reading Log header. Serialized `reading_goal_target` in portable JSON metadata export and restore.
3. **1-Tap `📊 Export CSV` in Reading Log Header (`FolioMoreView.java`)**: Surfaced an amber `📊 Export CSV` pill inside the top bar of the Reading Log preview dialog. Tapping it triggers on-device CSV generation, updates the status banner live to `Saved CSV: /sdcard/kindle/backups/folio_library.csv`, and displays a Toast confirmation.
4. **Control Center Version Bump**: Header badge and About Folio dialog bumped to **`Folio v7.2 KFX (Enhanced Typesetting)`**.
5. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.2 KFX

### A. Full Library CSV Spreadsheet Export (`FolioMetadataBackup.java`)
- `exportLibraryCsv(Context)` gathers all library books and active reading progress.
- Compiles standard CSV formatted row data:
  `csv.append("Title,Author,Year,Publisher,ISBN,Status,Shelf,Rating,Favorite,Note\n");`
- `csvEsc` sanitizes internal commas, double quotes, and CRLF line breaks per RFC-4180 standards.
- Writes to `/sdcard/kindle/backups/folio_library.csv` via UTF-8 `FileOutputStream`.

### B. Reading Goal Markdown & Portable Backup Persistence (`FolioMetadataBackup.java`)
- Queries `reading_goal_target` from `SharedPreferences("folio_ui_prefs")`.
- Emits formatted Reading Goal status into Markdown log:
  `**2026 Reading Goal:** 1 / 5 books finished (20%)`.
- Adds `"reading_goal_target"` key to portable JSON metadata backup root object and restores it during JSON import.

### C. Reading Log Header Bar CSV Action (`FolioMoreView.java`)
- Configured horizontal header row in `showReadingLogDialog(Context)` containing:
  - Saved path banner (`weight: 1f`).
  - Amber `📊 Export CSV` button (`#F59E0B` text on `#282216` fill with `#66F59E0B` border).
- Clicking `📊 Export CSV` calls `exportLibraryCsv(ctx)`, updates banner text, and shows Toast.

### D. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.2 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Reading Log Goal & Export CSV Pill** | Reading Log Dialog | `screenshots/verify_r72_reading_log_goal_and_csv_btn.png` | **PASS** — Monospace dialog renders `**2026 Reading Goal:** 1 / 5 books finished (20%)`, `- **Shelf:** Classics` under *1984*, and amber `📊 Export CSV` pill in header bar. |
| **Live CSV Export & Updated Banner** | Reading Log Dialog | `screenshots/verify_r72_csv_exported_banner.png` | **PASS** — Tapping `📊 Export CSV` updates banner to `Saved CSV: /sdcard/kindle/backups/folio_library.csv`. File verified on device storage (3,289 B). |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report072.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_072.md`
  - `protocol.json`
  - `screenshots/verify_r72_reading_log_goal_and_csv_btn.png`
  - `screenshots/verify_r72_csv_exported_banner.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v7.2_kfx/base.apk` (132 MB)
