# Folio Milestone Release Report: Searchable Authors Directory & Long Author Name Ellipsis Polish (Round 074)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.4 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.4_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 074, Folio advanced to **Folio v7.4 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 250,316 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Searchable Authors Directory (`FolioMetadataCatalogDialog.java`)**: Added an inline live `Search authors...` filter input (`EditText authSearch`) directly inside the dark Authors Directory modal with real-time text watching (`addTextChangedListener`), live rendering (`renderAuthors`), and dynamic header counter updating (`Authors Directory (1 of 23)`).
2. **Author Name Truncation & Layout Polish (`FolioMetadataCatalogDialog.java`)**: Configured `maxLines(1)`, `ellipsize(TextUtils.TruncateAt.END)`, and `10dp` right padding on author title labels. Long names (e.g., *Gaius Plinius Caecilius Secundus Pliny the Younger*) truncate cleanly without overlapping or crowding the amber `X books ›` badge.
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.4 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.4 KFX

### A. Live Author Search Filter (`FolioMetadataCatalogDialog.java`)
- Positioned `EditText authSearch` directly above the directory scroll view:
  - Hint: `"Search authors..."` (`#64748B`), white text, single line.
  - Background: `#1A1F2E` with `#2A3142` border stroke and 10dp corner radius.
- `renderAuthors` filters keys case-insensitively via `Locale.ROOT`:
  `if (!q.isEmpty() && !k.contains(q)) continue;`
- Dynamically updates header:
  `hdr.setText(q.isEmpty() ? "Authors Directory (" + keys.size() + ")" : "Authors Directory (" + shown + " of " + keys.size() + ")");`

### B. Single-Line Ellipsis & Padding (`FolioMetadataCatalogDialog.java`)
- Author labels (`nameTv`) configured with:
  ```java
  nameTv.setMaxLines(1);
  nameTv.setEllipsize(TextUtils.TruncateAt.END);
  nameTv.setPadding(0, 0, dp(ctx, 10), 0);
  ```
- Prevents text wrapping or layout shifts when displaying lengthy names alongside the amber badge.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.4 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Authors Search & Ellipsis** | Authors Directory modal | `screenshots/verify_r74_authors_search_ellipsis.png` | **VERIFIED**: Opened Authors Directory (23); verified inline `Search authors...` input at top and single-line layout on author rows. |
| **Live Author Filtering** | Authors Directory search input | `screenshots/verify_r74_authors_filtered_live.png` | **VERIFIED**: Typed `"Orwell"`; header updated to `Authors Directory (1 of 23)`; filtered dynamically to `George Orwell (2 books ›)`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
