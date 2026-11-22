# Folio Milestone Release Report: Publication Eras Chart & Era Filtering (Round 093)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.3 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 093, Folio advanced to **Folio v9.3 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 325,684 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **📅 Publication Eras Timeline Bar Chart (`FolioMoreView.java`)**:
   - Parses valid publication years across all cataloged books, computes library historical span (`PUBLICATION ERAS · 1531–2017 (TAP TO FILTER)`), and plots proportional violet horizontal bars across chronological eras (`Pre-1900`, `1900–1949`, `1950–1999`, `2000–2009`, `2010–2019`, `2020+`).
   - Tapping any era bar dismisses Insights and jumps straight into the catalog filtered to that exact publication era.
2. **Smart `ERA:` Filter Chip & Query Parsing (`FolioMetadataCatalogDialog.java`)**:
   - Supports `ERA:<bucket>` deep-links with a dedicated active `📅 <Era> (N)` filter chip and exact year-range filtering.
3. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v9.3 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.3 KFX

### A. Publication Eras Timeline Bar Chart (`FolioMoreView.java`)
- Chronological Year Parsing & Era Bucketing:
  ```java
  int bYr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.parseBookYear(r.pubDate);
  if (bYr > 0) {
      datedCount++;
      if (bYr < minYear) minYear = bYr;
      if (bYr > maxYear) maxYear = bYr;
      String eb = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.eraBucketForYear(bYr);
      if (eraFreq.containsKey(eb)) eraFreq.put(eb, eraFreq.get(eb) + 1);
  }
  ```
- 1-Tap Era Drilldown:
  - Tapping any era bar navigates to `FolioMetadataCatalogDialog.show(ctx, "ERA:" + eraName)`.

### B. Smart Era Catalog Filtering (`FolioMetadataCatalogDialog.java`)
- Filter chip displays `📅 <Era> (N)` with violet border styling (`#C084FC`).
- Matches records via `matchesEraFilter(rec, era)`.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Publication Eras Chart** | Library Insights Modal | `verify_r93_insights_pub_eras.png` | **VERIFIED**: Opened `📊 Library Insights` and scrolled down; verified `PUBLICATION ERAS · 1531–2017 (TAP TO FILTER)` section displaying violet bar chart with `Pre-1900` (13), `1900–1949` (1), `1950–1999` (4), `2000–2009` (4), and `2010–2019` (5). |
| **Era Filter Drilldown** | Catalog Modal | `verify_r93_era_filter_drilldown.png` | **VERIFIED**: Tapped `📅 2000–2009` bar; verified catalog opens with active violet `📅 2000–2009 (4)` chip and filters down to *1984*, *Moby Dick*, and *Animal Farm*. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
