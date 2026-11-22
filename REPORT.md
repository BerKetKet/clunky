# Folio Milestone Release Report: Stacked Reading Status Bar & Top Publishers Chart in Library Insights (Round 079)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.9 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 079, Folio advanced to **Folio v7.9 KFX**. Gemini pre-authored drop-in Java updates for `FolioMoreView.java`. Antigravity integrated the single file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`, compiled Dalvik DEX (`classes9.dex`, 263,236 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Interactive Reading Status Proportional Stacked Bar (`FolioMoreView.java`)**: Added `READING STATUS (TAP TO FILTER)` horizontal stacked bar at the top of the Library Insights modal (`● Finished (1)` emerald `#10b981` · `● Reading (5)` sky blue `#0284c7` · `● Unread (21)` slate `#64748b`). Tapping any bar segment or legend pill immediately launches the catalog filtered to that reading status.
2. **Top Publishers Bar Chart (`FolioMoreView.java`)**: Added `TOP PUBLISHERS (TAP TO FILTER)` teal horizontal bar chart at the bottom of the Library Insights modal, displaying publishers with book count badges (`Focus Publishing/R. Pullins Company 2`, `HarperCollins 2`, `Airlift 1`, `Apud Janssonio-Waesbergios 1`). Tapping any publisher row opens the catalog pre-filtered to that publisher.
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.9 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.9 KFX

### A. Reading Status Stacked Proportional Bar (`FolioMoreView.java`)
- Computes library-wide reading statuses dynamically across cataloged EPUBs: `finishedCount`, `readingCount`, `unreadCount`.
- Renders a multi-segment horizontal proportional bar with rounded corner caps:
  - Finished (emerald `#10b981`)
  - Reading (sky blue `#0284c7`)
  - Unread (slate `#64748b`)
- Renders interactive legend pills below the bar: `● Finished (X)`, `● Reading (Y)`, `● Unread (Z)`.
- Tapping any segment or pill invokes `FolioMetadataCatalogDialog.showWithReadingStatus(ctx, status)` to immediately view matching books.

### B. Top Publishers Horizontal Bar Chart (`FolioMoreView.java`)
- Extracts publishers from cataloged metadata, counts occurrences, sorts descending, and caps at top 5.
- Displays proportional teal (`#14b8a6`) horizontal bars with right-aligned count badges.
- Tapping any publisher row executes `FolioMetadataCatalogDialog.showWithSearch(ctx, publisher)` to filter catalog books by that publisher.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.9 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Reading Status Stacked Bar** | More → 📊 Library Insights | `screenshots/verify_r79_insights_stacked_status.png` | **VERIFIED**: Opened Library Insights modal; verified stacked proportional `READING STATUS` bar with emerald Finished (1), sky blue Reading (5), and slate Unread (21) segments and legend pills above `TOP GENRES`. |
| **Shelves & Top Publishers Chart** | More → 📊 Library Insights (Scrolled) | `screenshots/verify_r79_insights_shelves_publishers.png` | **VERIFIED**: Scrolled down inside modal; verified `CUSTOM SHELVES` (`🏷 Classics 1`) and `TOP PUBLISHERS` teal bars (`Focus Publishing/R. Pullins Company 2`, `HarperCollins 2`, `Airlift 1`, `Apud Janssonio-Waesbergios 1`) and `CLOSE` button. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
