# Folio Milestone Release Report: "More by Author" Sibling Carousel in Book Details (Round 095)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Milestone Folio v9.5 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Milestone Round 095, Folio advanced to **Milestone Folio v9.5 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 328,932 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **"MORE BY [AUTHOR] (N)" Interactive Sibling Carousel (`FolioMetadataCatalogDialog.java`)**:
   - Automatically queries `folio_metadata.db` to discover all other cataloged volumes by the same normalized author.
   - Situates an interactive horizontal card carousel right below the Personal Note card.
   - Each sibling card features a standalone `.cover.jpg` thumbnail, 2-line title, and live dynamic badge (`✓ Done ›`, `X% read ›`, `📌 Up Next ›`, or publication year).
   - Tapping any card switches immediately to that book's Details (`ⓘ`) modal in 1 tap.
2. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Milestone Folio v9.5 KFX (Enhanced Typesetting)**.
3. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.5 KFX

### A. Author Sibling Discovery & Card Layout (`FolioMetadataCatalogDialog.java`)
- Sibling Query:
  ```java
  String curAuthorClean = formatAuthorForDisplay(rec.author);
  if (!curAuthorClean.isEmpty() && !"Unknown Author".equalsIgnoreCase(curAuthorClean)) {
      List<FolioMetadataDb.BookRecord> siblings = new ArrayList<>();
      FolioMetadataDb sibDb = new FolioMetadataDb(ctx);
      try {
          for (FolioMetadataDb.BookRecord cand : sibDb.getAllBooks()) {
              if (cand.id == rec.id) continue;
              if (curAuthorClean.equalsIgnoreCase(formatAuthorForDisplay(cand.author))) {
                  siblings.add(cand);
              }
          }
      } finally { sibDb.close(); }
  ...
  ```
- 1-Tap Sibling Navigation:
  - Tapping a sibling card calls `showBookDetailDialog(...)` with the sibling record, allowing smooth lateral exploration across an author's complete library catalog.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **More by Author Details** | Book Details Modal | `verify_r95_more_by_author_details.png` | **VERIFIED**: Opened Book Details for *1984*; verified `MORE BY GEORGE ORWELL (1)` horizontal strip right below Personal Note card, showing *Animal farm: a fairy story* with standalone cover thumbnail, title, and violet `📌 Up Next ›` status badge (`[215, 1774][515, 1858]`). |
| **About Folio v9.5 KFX** | About Folio Modal | `verify_r95_about_v95.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v9.5 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
