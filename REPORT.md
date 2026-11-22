# Folio Milestone Release Report: Pass-3 Missing-Metadata Auto-Repair & Clean Author Formatting (Round 049)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.5 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.5_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 049, Folio reached **Folio v4.5 KFX**. Gemini authored the drop-in updates for `LibraryMetadataBackfiller.java`, `FolioMetadataCatalogDialog.java`, and `FolioMoreView.java`. Antigravity integrated all drop-in files, resolved high-latency mobile TLS handshake aborts by adding cleartext HTTP fallback in `MetadataEnricher.java`, compiled Dalvik DEX (`classes9.dex`, 200,500 B), rebuilt, aligned, signed, and deployed the build to the Samsung Galaxy M32 hardware. Tapping `Sync & Enrich` successfully triggered Pass 3 metadata auto-repair across the local catalog, populating missing publication dates, ISBNs, and genres for *1984*, *Moksha*, and *Crome Yellow*, while cleanly formatting *Moby Dick*'s author byline as `"Herman Melville"`.

---

## 2. Integrated Features in Folio v4.5 KFX

### A. Pass 3 Missing-Metadata Auto-Repair (`LibraryMetadataBackfiller.java`)
- Pass 3 inspects existing indexed records in `folio_metadata.db` that lack genres or pub_date.
- Invokes `MetadataEnricher.enrichInPlace()` via Open Library Search API with mobile network resiliency (HTTP fallback).
- Updates `books_metadata` in-place and triggers catalog UI re-render and genre frequency re-ranking upon completion.
- Successfully enriched:
  - **1984**: `pub_date=2003`, `genres=Totalitarianism, Fiction, Totalitarianism -- Fiction, Dystopias, Textbooks for foreign speakers`, `isbn=0582777313`
  - **Moksha**: `pub_date=2012-04-12`, `genres=Hallucinogenic drugs, Psychological aspects of Hallucinogenic drugs, Hallucinogenic drugs and religious experience, Collected works, Psychological aspects`, `isbn=9781594775178`
  - **Crome Yellow**: `pub_date=2011-01-06`, `genres=Fiction, Intellectuals, Country homes, England, fiction, Fiction, humorous`, `isbn=9781407020938`
  - **The Dream of the Red Chamber**: `pub_date=2011`, `genres=Chinese fiction, Translations into English, China, fiction, Fiction, Fiction`, `isbn=9780804840965`

### B. Clean Natural-Name Author Formatting (`FolioMetadataCatalogDialog.java`)
- `formatAuthorForDisplay()` parses last-name-first strings (e.g., `"Melville, Herman"`) into natural reading order (`"Herman Melville"`).
- Preserves multi-author semicolons and prevents awkward catalogue inversions across metadata cards.
- *Moby Dick* verified displaying `"Herman Melville · 2001-07-01"`.

### C. Dynamic Genre Chip Re-Ranking
- Top genre chips dynamically recalculate upon Pass 3 completion:
  - `All (27)`
  - `Fiction (7)` (elevated from 4 after backfill)
  - `History (5)`
  - `Early Works To 1800 (3)`

### D. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v4.5 KFX`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Pass 3 Metadata & Author Formatting** | Home → All Metadata › | `screenshots/verify_r49_pass3_enriched_all.png` | **PASS** — *Moby Dick* displays `"Herman Melville · 2001-07-01"`; *1984* displays `"George Orwell · 2003 · Houghton Mifflin Harcourt"`, ISBN `0582777313`, and enriched genres `Totalitarianism, Fiction, Dystopias...`; `Fiction (7)` chip reflects updated count. |
| **Backfilled Moksha & Crome Yellow** | Home → All Metadata › (Scrolled) | `screenshots/verify_r49_moksha_crome_yellow.png` | **PASS** — *Moksha* displays `2012-04-12`, ISBN `9781594775178`, and Hallucinogenic drugs/Psychological aspects genres; *Crome Yellow* displays `2011-01-06`, ISBN `9781407020938`, and Fiction/Intellectuals/Country homes genres. |
| **Folio v4.5 KFX Control Center & About** | More Tab → About Folio | `screenshots/verify_r49_about_v45.png` | **PASS** — Displays `Version: Folio v4.5 KFX (Enhanced Typesetting)`, 29 indexed books, 27 enriched records, and 13.3 MB App Library storage. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched and pristine. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report049.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_049.md`
  - `protocol.json`
  - `screenshots/verify_r49_pass3_enriched_all.png`
  - `screenshots/verify_r49_moksha_crome_yellow.png`
  - `screenshots/verify_r49_about_v45.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.5_kfx/base.apk` (131 MB)
