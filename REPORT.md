# Folio Milestone Release Report: Continue Reading Quick-Resume Carousel (Round 084)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v8.4 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 084, Folio advanced to **Folio v8.4 KFX**. Gemini pre-authored drop-in Java updates for `FolioMoreView.java`. Antigravity integrated the single file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`, compiled Dalvik DEX (`classes9.dex`, 278,892 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Continue Reading Quick-Resume Carousel (`FolioMoreView.java`)**: Added a horizontal card carousel directly below `🎯 2026 Reading Goal` on the Control Center screen. Dynamically lists in-progress books (`0 < progress < 100%`) sorted by highest % read first, accompanied by a `See All (5) ›` deep-link into the `Reading` filter.
2. **Cover Art & Progress Cards**: Each horizontal card renders a standalone `.cover.jpg` thumbnail (`FIT_CENTER` on `#0D1017`), 2-line title, normalized author, `X% read · Resume ›` badge, and a sleek 3dp sky-blue progress bar.
3. **1-Tap Instant Reader Resume**: Tapping any card directly launches the book into the KFX reader via `ExternalEpubActivity.launchKfxReader(...)`.
4. **Reactive Lifecycle Refresh**: Carousel auto-populates and updates dynamically alongside the 4 stat counters and the Reading Goal card whenever library state shifts.
5. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v8.4 KFX (Enhanced Typesetting)`**.
6. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v8.4 KFX

### A. Continue Reading Strip Implementation (`FolioMoreView.java`)
- Filter and sort in-progress books:
  ```java
  final Map<String, Integer> progMap = FolioMetadataCatalogDialog.loadProgressMap(ctx);
  // Matches records where 0 < pct < 100, sorts descending by pct
  ```
- Header row with deep-link:
  ```java
  headerRow.addView(hdrTv); // "📖 CONTINUE READING"
  seeAllTv.setText("See All (" + inProgress.size() + ") ›");
  seeAllTv.setOnClickListener(v -> FolioMetadataCatalogDialog.showWithReadingStatus(ctx, "Reading"));
  ```
- Horizontal card construction:
  - Width: 180dp, height: ~195dp, margin: 10dp right
  - Thumbnail: 100dp height, `ScaleType.FIT_CENTER`, `#0D1017` matte
  - 3dp progress bar: sky-blue `#38BDF8` filled proportionally
  - Title: 2-line bold white with ellipsis
  - Author: 1-line muted `#94A3B8`
  - Action label: `X% read · Resume ›` (accent amber `#F59E0B`)
  - Click listener: `ExternalEpubActivity.launchKfxReader(ctx, item.rec.kfxPath, item.rec.title)`

### B. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v8.4 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Continue Reading Carousel** | More Tab (Control Center Top) | `screenshots/verify_r84_continue_reading.png` | **VERIFIED**: Opened Control Center top; verified `Folio v8.4 KFX` badge, 4-tile stats (`29 BOOKS`, `5 READING`, `1 FINISHED`, `1 ★ FAVS`), `🎯 2026 Reading Goal`, and the new `📖 Continue Reading` horizontal carousel with covers (*Brave New World* 9%, *Aristotle* 8%, etc.), progress bars, and `See All (5) ›`. |
| **About Folio v8.4 KFX** | About Folio Modal | `screenshots/verify_r84_about_v84.png` | **VERIFIED**: Tapped badge; verified modal confirming `Version: Folio v8.4 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
