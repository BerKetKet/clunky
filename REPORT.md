# Folio Milestone Release Report: Folder Importer Sort Toggle & Selected Size CTA (Round 077)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.7 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.7_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 077, Folio advanced to **Folio v7.7 KFX**. Gemini pre-authored drop-in Java updates for `FolioFolderBrowserDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 258,456 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Folder Importer Sort Toggle (`FolioFolderBrowserDialog.java`)**: Added a 4-mode cycling sort button (`btnSortFolder`) right beside `Hide Imported: OFF` in the Batch EPUB Importer header (`Sort: A–Z → Newest → Largest → Smallest`). When set to `Sort: Largest`, multi-MB EPUBs (*The 4-Hour Body* at 38.8 MB, *The 4-Hour Chef* at 24.4 MB) sort cleanly to the top of the 719-file directory.
2. **Live Cumulative Selected Payload Size CTA (`FolioFolderBrowserDialog.java`)**: Enhanced `updateImportLabel` to compute cumulative byte size across `selectedFiles`. Displays formatted payload size directly on the emerald bottom import button (`Import Selected (25 · 238.6 MB)`).
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.7 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.7 KFX

### A. 4-Way Folder Sorting (`FolioFolderBrowserDialog.java`)
- Positioned `btnSortFolder` in the top header row:
  - Modes: `folderSortLabels = new String[] { "A–Z", "Newest", "Largest", "Smallest" }`.
  - Tapping advances index modulo 4 and invokes `renderList.run()`.
  - Comparator sorts filtered files:
    - `sMode == 1`: `Long.compare(b.lastModified(), a.lastModified())` (Newest first).
    - `sMode == 2`: `Long.compare(b.length(), a.length())` (Largest first).
    - `sMode == 3`: `Long.compare(a.length(), b.length())` (Smallest first).
    - `default`: Case-insensitive filename alphabetical sort.

### B. Live Cumulative Size Calculation (`FolioFolderBrowserDialog.java`)
- Computes sum of `sf.length()` for all files in `selectedFiles`.
- Formats bytes dynamically:
  - $\ge 1\text{ MB}$: `String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))`.
  - $< 1\text{ MB}$: `Math.max(1L, totalBytes / 1024L) + " KB"`.
- Updates button label live: `btnImport.setText("Import Selected (" + nSel + " · " + szStr + ")");`.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.7 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Folder Importer `Sort: Largest`** | Home → Import → `Sort: Largest` | `screenshots/verify_r77_folder_sort_largest.png` | **VERIFIED**: Tapped Sort button twice to `Sort: Largest`; 719-file list sorted descending by size, placing 38.8 MB and 24.4 MB EPUBs at the top. |
| **Selected Size CTA** | Batch EPUB Importer | `screenshots/verify_r77_selected_size_cta.png` | **VERIFIED**: Tapped `Select New`; emerald CTA updated live to `Import Selected (25 · 238.6 MB)`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
