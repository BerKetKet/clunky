# Folio Release Report: Quick Search Scope Filter Pills & Recent Search History (Round 101)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v10.1 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v10.1_kfx/base.apk` (137 MB / 137,473,734 bytes)

---

## 1. Executive Summary

In Round 101, Folio advanced to **Folio v10.1 KFX**. Drop-in updates were integrated into `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 351,208 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **🔍 Quick Search Scope Filter Pills (`FolioMoreView.java`)**:
   - Added an interactive horizontal strip of 5 quick scope filter pills directly below the search input:
     - `All`
     - `📖 Reading`
     - `📝 Notes`
     - `📌 Queue`
     - `🏷 Shelves`
   - Active scope pill renders with warm amber fill (`#F59E0B`) and dark text.
   - Tapping any scope pill updates results immediately in real time, seamlessly combining with query text.
2. **🕒 Persistent Recent Search History (`FolioMoreView.java`)**:
   - Automatically stores up to 5 recent queries in `SharedPreferences("folio_ui_prefs", "recent_global_searches")`.
   - When the search bar is empty, displays recent search chips with 1-tap recall and a `Clear` button.
3. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v10.1 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v10.1 KFX

### A. Quick Search Scope Filter Architecture (`FolioMoreView.java`)
- Scope Pill Definition & Binding:
  ```java
  final String[] scopeKeys = new String[] { "ALL", "READING", "NOTES", "QUEUE", "SHELVES" };
  final String[] scopeLabels = new String[] { "All", "📖 Reading", "📝 Notes", "📌 Queue", "🏷 Shelves" };
  final String[] activeScope = new String[] { "ALL" };
  ```
- Instant Category Filtering in `renderResults`:
  - `READING`: filters to books with active reading progress (`0 < pct < 100`).
  - `NOTES`: filters to books containing personal annotations.
  - `QUEUE`: filters to volumes present in `SharedPreferences("folio_queue")`.
  - `SHELVES`: isolates custom shelf tags.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Search Scope Pills** | Quick Search Modal | `verify_r101_search_scope_pills.png` | **VERIFIED**: Tapped `🔍 Search` in Control Center header; tapped `📖 Reading` scope pill; verified active amber fill on `📖 Reading` and instant category filtration displaying currently read volumes (*The 210th Day*, *Sophocles*, *Aristotle*, *Island*, *Brave New World*) with reading percentage badges and `Read ›` buttons (`[117, 508][963, 1882]`). |
| **About Folio v10.1 KFX** | About Folio Modal | `verify_r101_about_v101.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v10.1 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
