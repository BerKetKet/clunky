# Folio Milestone Release Report: Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering (Round 073)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v7.3 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v7.3_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 073, Folio advanced to **Folio v7.3 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 248,776 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Authors Directory Modal (`FolioMetadataCatalogDialog.java`)**: Added a compact **`👤`** (Authors Directory) button beside `🎲` in the catalog top bar. Tapping it opens a dark `Authors Directory (23)` dialog listing every author in the user's library sorted by book count descending with amber `X books ›` / `X book ›` badges (`showAuthorsDirectoryDialog`).
2. **1-Tap Author Filtering (`FolioMetadataCatalogDialog.java`)**: Tapping any author row (e.g. `Aldous Huxley · 4 books ›`) automatically dismisses the directory modal, resets the genre filter to "All", and populates the search query with the author name, instantly narrowing the catalog display to `(4 of 27)` books.
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v7.3 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v7.3 KFX

### A. Compact `👤` Authors Directory Button (`FolioMetadataCatalogDialog.java`)
- Positioned in the catalog top bar immediately left of `🎲`:
  `topRow.addView(btnAuthors);`
- Styled with dark `#1E2433` pill background, `#334155` border stroke, 14dp rounded corners, and `#E2E8F0` icon.
- Tapping triggers `showAuthorsDirectoryDialog(ctx, allBooks, author -> { selectedGenre[0] = "All"; searchBox.setText(author); searchBox.setSelection(author.length()); });`.

### B. Authors Directory Modal & Frequency Aggregation (`FolioMetadataCatalogDialog.java`)
- Aggregates library authors via normalized display names (`formatAuthorForDisplay(r.author)`), mapping case-insensitive keys to counts.
- Sorts authors descending by book count (`Integer.compare(cb, ca)`), breaking ties alphabetically (`a.compareTo(b)`).
- Dynamically populates a vertical scroll list of dark card rows (`#1A1F2E` fill, `#2A3142` border, 10dp radius).
- Left: bold white author name (`nameTv`). Right: amber pill text (`#F59E0B`, `n + (n == 1 ? " book ›" : " books ›")`).
- Tapping any row dismisses the dialog via `dlgRef[0].dismiss()` and fires `onPick.onFilterByAuthor(name)`.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v7.3 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Authors Directory Modal (`👤`)** | All Metadata › top bar `👤` | `screenshots/verify_r73_authors_directory_modal.png` | **VERIFIED**: Tapped `👤` beside `🎲`; dark `Authors Directory (23)` opened listing `Aldous Huxley (4 books ›)`, `George Orwell (2 books ›)`, `Aeschylus (1 book ›)`, etc. |
| **1-Tap Author Filtering** | Authors Directory row selection | `screenshots/verify_r73_author_selected_filter.png` | **VERIFIED**: Tapped `Aldous Huxley`; modal dismissed; catalog dynamically filtered to `Library Metadata & Genres (4 of 27)` with search query `Aldous Huxley`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
