# Folio Milestone Release Report: Deduplicated Genre Display, Clean Chips & Live Reading Stats (Round 050)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v5.0 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v5.0_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 050, Folio achieved its major milestone release: **Folio v5.0 KFX**. Gemini delivered pre-authored drop-in updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both drop-ins (while strictly preserving Round 049's resilient HTTP fallback in `MetadataEnricher.java`), compiled `classes9.dex` (201,684 B), built, aligned, signed, and deployed the build to the Samsung Galaxy M32 hardware. Hardware verification confirmed clean literary Genre Chips at the top of the catalog, case-insensitive deduplicated Title-Cased genre tags on book cards, and live reading progress statistics (`0 in progress · 29 finished`) in the Control Center About Folio dialog.

---

## 2. Integrated Features in Folio v5.0 KFX

### A. Clean Literary Genre Chips (`FolioMetadataCatalogDialog.java`)
- `isCatalogMetaTag()` filters out bibliographic and catalog meta-tags (`early works`, `translations into`, `textbooks for`, `protected daisy`, `accessible book`, `in library`, etc.).
- Frequency ranking surfaces high-signal literary categories across the local library:
  - `All (27)`
  - `Fiction (7)`
  - `History (5)`
  - `Rome (3)`
  - `Biography (2)`

### B. Case-Insensitive Deduplicated Title-Case Genres
- `cleanGenresForDisplay()` parses raw metadata tags, eliminates compound `--` Library of Congress noise, deduplicates entries case-insensitively, and formats output in clean Title Case.
- **The Dream of the Red Chamber**: Displays `Chinese Fiction, China, Fiction` (eliminating prior repetitive `fiction, Fiction, Fiction` clutter and meta-tags).
- **Crome Yellow**: Displays `Fiction, Intellectuals, Country Homes, England, Humorous`.
- **Moby Dick**: Displays `Sea Stories, Psychological Fiction, Adventure Stories, Ahab`.

### C. Live Reading Progress Stats (`FolioMoreView.java`)
- `showAboutDialog()` queries `kindle_library.db` for active reading state counts:
  - `READ_STATE > 0` for finished books.
  - `READING_PROGRESS > 0 AND READ_STATE = 0` for books currently in progress.
- Renders `• Reading Progress: 0 in progress · 29 finished` directly in the About Folio dialog alongside library book counts and storage breakdowns.

### D. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v5.0 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Deduplicated Genres & Clean Chips** | Home → All Metadata › | `screenshots/verify_r50_clean_genres_and_chips.png` | **PASS** — Top chips display clean literary taxonomy (`All (27)`, `Fiction (7)`, `History (5)`, `Rome (3)`, `Biography (2)` with zero meta-tags). *Crome Yellow* and *The Dream of the Red Chamber* cards display deduplicated Title-Cased genres without duplicate spam. |
| **Folio v5.0 KFX About & Reading Stats** | More Tab → About Folio | `screenshots/verify_r50_about_v50_reading_stats.png` | **PASS** — About Folio displays `Version: Folio v5.0 KFX (Enhanced Typesetting)` and live `Reading Progress: 0 in progress · 29 finished` alongside storage statistics. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report050.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_050.md`
  - `protocol.json`
  - `screenshots/verify_r50_clean_genres_and_chips.png`
  - `screenshots/verify_r50_about_v50_reading_stats.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v5.0_kfx/base.apk` (131 MB)
