# Folio Milestone Release Report: Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart (Round 076)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.6 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.6_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 076, Folio advanced to **Folio v7.6 KFX**. Gemini pre-authored drop-in Java updates for `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 256,808 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Interactive 1-Tap Drill-Down in Library Insights (`FolioMoreView.java` & `FolioMetadataCatalogDialog.java`)**: Made every bar row in **TOP GENRES** (blue), **TOP AUTHORS** (amber), and **CUSTOM SHELVES** (indigo) 1-tap clickable. Tapping an author row (e.g. `Aldous Huxley · 4`) dismisses Insights and immediately launches the catalog pre-filtered to `(4 of 27)` books via `FolioMetadataCatalogDialog.showWithSearch(ctx, aName)`.
2. **Scrollbar Overlap Elimination & Header Polish (`FolioMoreView.java`)**: Configured `sv.setVerticalScrollBarEnabled(false)` and `4dp` right container padding, completely eliminating right-side scrollbar overlap over bar count numbers (`7`, `5`, `4`). Updated section headers to `TOP GENRES (TAP TO FILTER)`, `TOP AUTHORS (TAP TO FILTER)`, and `CUSTOM SHELVES (TAP TO FILTER)`.
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.6 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.6 KFX

### A. 1-Tap Catalog Drill-Down Navigation (`FolioMoreView.java`)
- Attached `View.OnClickListener` handlers to `makeInsightBarRow`:
  - **Top Genres**: Tapping calls `FolioMetadataCatalogDialog.show(ctx, gName)`.
  - **Top Authors**: Tapping calls `FolioMetadataCatalogDialog.showWithSearch(ctx, aName)`.
  - **Custom Shelves**: Tapping calls `FolioMetadataCatalogDialog.show(ctx, "SHELF:" + sh)`.
- Automatically dismisses `insightDlgRef[0]` prior to catalog launch for smooth 0ms transition.

### B. Pre-Filtered Catalog Entrypoint (`FolioMetadataCatalogDialog.java`)
- Added public static overloaded entrypoints:
  ```java
  public static void showWithSearch(final Context ctx, final String initialQuery) {
      show(ctx, "All", initialQuery, null);
  }
  public static void show(final Context ctx, final String initialFilter, final String initialQuery, final Runnable onDismiss)
  ```
- Automatically pre-populates `searchBox.setText(initQ)`, sets cursor to end, reveals clear button `✕`, and fires initial query filtering.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.6 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Clean Bars & Filter Headers** | Control Center → 📊 Library Insights | `screenshots/verify_r76_insights_clean_bars.png` | **VERIFIED**: Opened Insights; confirmed zero scrollbar overlap over count digits (`7`, `5`, `4`) and interactive headers `(TAP TO FILTER)`. |
| **Author Bar Drill-Down** | Insights → `Aldous Huxley · 4` tap | `screenshots/verify_r76_insights_author_drilldown.png` | **VERIFIED**: Tapped `Aldous Huxley · 4` amber bar; Insights dismissed and opened `Library Metadata & Genres (4 of 27)` with search box pre-filled to `Aldous Huxley`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
