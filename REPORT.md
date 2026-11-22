# Folio Release Report: 1-Tap Sort Picker Modal, Direction Toggle & Size/Queue Sorts (Round 096)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.6 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.6_kfx/base.apk` (137 MB / 137,453,254 bytes)

---

## 1. Executive Summary

In Round 096, Folio advanced to **Folio v9.6 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 333,020 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **1-Tap Dark `Sort Catalog` Picker Modal (`FolioMetadataCatalogDialog.java`)**:
   - Replaced linear cycling of the `Sort: ...` button with a dark modal (`showSortPickerDialog(...)`) providing 1-tap direct selection across all **9 sort modes**:
     1. `Recently Added`
     2. `Title (A–Z)`
     3. `Author (A–Z)`
     4. `Publication Year`
     5. `Reading Progress`
     6. `Favorites First`
     7. `Star Rating`
     8. `File Size (Largest)`
     9. `📌 Up Next Queue First`
   - Active sort mode is prominently badged with an amber `✓` and warm gold tint (`#F59E0B`).
   - Added persistent **`↓ Standard / ↑ Reversed`** direction toggle pill in the modal header, stored in `SharedPreferences("folio_ui_prefs", "catalog_sort_rev")`.
   - The top bar `Sort:` pill displays dynamic live arrow indicators (`↓`/`↑`) reflecting the current sort direction.
2. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v9.6 KFX (Enhanced Typesetting)**.
3. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.6 KFX

### A. Sort Picker Modal Architecture (`FolioMetadataCatalogDialog.java`)
- Direction state is read from and written to `folio_ui_prefs`:
  ```java
  boolean sortReversed = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
      .getBoolean("catalog_sort_rev", false);
  ```
- Tapping the sort direction toggle updates SharedPreferences and reverses sorting comparisons cleanly across all 9 modes (including file size and queue priority).
- When a user selects any of the 9 sort options, the active mode updates immediately and the catalog list re-sorts and renders in-place.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Sort Catalog Picker Modal** | Metadata Catalog Modal | `verify_r96_sort_picker_modal.png` | **VERIFIED**: Tapped `Sort: Added ↓` on top bar; verified dark `Sort Catalog` modal showing `↓ Standard` toggle pill and all 9 sort modes with amber `✓` on `Recently Added` (`[120, 480][960, 1680]`). |
| **About Folio v9.6 KFX** | About Folio Modal | `verify_r96_about_v96.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v9.6 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
