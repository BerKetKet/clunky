# Folio Milestone Release Report: Library Insights Visual Analytics Modal (Round 075)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.5 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.5_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 075, Folio advanced to **Folio v7.5 KFX**. Gemini pre-authored a single drop-in Java update for `FolioMoreView.java`. Antigravity integrated this file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 254,516 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Library Insights Visual Analytics Action Card (`FolioMoreView.java`)**: Added `📊 Library Insights` action card under Reading Log in the Control Center ("Top authors, top genres, average rating & shelf stats").
2. **Visual Analytics Dialog with Proportional Horizontal Bar Charts (`FolioMoreView.java`)**: Tapping opens a dark visual analytics modal displaying:
   - Live curation summary: `27 cataloged · 5 reading · 1 finished\n1 favorited · 1 annotated · 1 shelved · 5.0★ avg (1 rated)`.
   - **TOP GENRES** horizontal proportional bar chart with blue bars (`0xFF60A5FA`): Fiction (7), History (5), Rome (3), Biography (2), Correspondence (2).
   - **TOP AUTHORS** horizontal proportional bar chart with amber bars (`0xFFF59E0B`): Aldous Huxley (4), George Orwell (2), Aeschylus (1).
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.5 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.5 KFX

### A. Action Card `📊 Library Insights` (`FolioMoreView.java`)
- Positioned in Control Center under Reading Log:
  ```java
  container.addView(makeActionCard(
      ctx, d,
      "📊", "Library Insights",
      "Top authors, top genres, average rating & shelf stats",
      v -> showLibraryInsightsDialog(ctx)
  ));
  ```

### B. Proportional Visual Analytics Engine (`FolioMoreView.java`)
- `showLibraryInsightsDialog(Context)` queries `FolioMetadataDb.getAllBooks()`:
  - Aggregates author frequency with display name normalization and case-insensitive keys.
  - Cleans and aggregates genre tags, filtering out noise words and sub-tags.
  - Tallying user metadata telemetry: favorites, notes, shelves, and star ratings (`ratingSum / ratedCount` yielding `5.0★ avg`).
- Bar row renderer (`makeInsightBarRow`):
  - Track: `#1E2433` background with 4dp rounded corners, 6dp height.
  - Fill: proportional ratio (`count / maxCount`, clamped `[0.06f, 1.0f]`) using `LinearLayout.LayoutParams` weights with rounded colored fill.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.5 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Library Insights Modal** | Control Center → 📊 Library Insights | `screenshots/verify_r75_library_insights_modal.png` | **VERIFIED**: Opened modal showing live curation stats (`27 cataloged · 5 reading · 1 finished · 5.0★ avg`), proportional blue TOP GENRES bars (Fiction 7, History 5, Rome 3), and amber TOP AUTHORS bars (Aldous Huxley 4, George Orwell 2). |
| **Control Center Badge Bump** | Control Center Header | `screenshots/verify_r75_control_center_v75.png` | **VERIFIED**: Header badge verified displaying `Folio v7.5 KFX` across 4-tile stat strip. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
