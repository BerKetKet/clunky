# Folio Milestone Release Report: Bulk Genre Tagging & Metadata Health Tile Polish (Round 087)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v8.7 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.7_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 087, Folio advanced to **Folio v8.7 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 298,276 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Bulk `+ Genre` Action Pill in Multi-Select Bar (`FolioMetadataCatalogDialog.java`)**: Added a 4th dedicated action pill **`+ Genre`** (sky-blue accent `#38BDF8`, `#152638` container) situated alongside `🏷 Shelf`, `★ Fav`, and `✓ Done` in the floating bottom Bulk Action Bar.
2. **Interactive Bulk Add Genre Modal (`FolioMetadataCatalogDialog.java`)**:
   - Tapping `+ Genre` launches `showBulkGenrePicker(...)` (`🏷 Add Genre (N books)`).
   - Features 1-tap horizontal genre preset chips (`+ Fiction`, `+ History`, `+ Rome`, `+ Biography`, `+ Correspondence`) populated dynamically from existing library genres.
   - Includes custom comma-separated genre text input field with hint `"Or type genre(s), comma-separated..."`.
   - Safely merges selected/typed genres into `books_metadata.genres` in `folio_metadata.db` for all selected volumes, deduplicating case-insensitively and preserving existing tags.
3. **Metadata Health Tile Polish (`FolioMoreView.java`)**: Added `Gravity.CENTER` to both value (`valTv`) and label (`lblTv`) inside `makeHealthBadge(...)` ensuring balanced alignment across all 3 audit tiles (`No Cover`, `No Genre`, `No Synopsis`).
4. **Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v8.7 KFX (Enhanced Typesetting)`**.
5. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v8.7 KFX

### A. Bulk Genre Tagging Subsystem (`FolioMetadataCatalogDialog.java`)
- 4-pill Bulk Action Bar:
  ```java
  final TextView btnBulkShelf = makeBulkActionPill(ctx, "🏷 Shelf", "#312E81", "#C7D2FE", "#4F46E5");
  final TextView btnBulkGenre = makeBulkActionPill(ctx, "+ Genre", "#152638", "#38BDF8", "#1E4E75");
  final TextView btnBulkFav = makeBulkActionPill(ctx, "★ Fav", "#3B2A14", "#FBBF24", "#784F1D");
  final TextView btnBulkDone = makeBulkActionPill(ctx, "✓ Done", "#123328", "#34D399", "#1E664E");
  ```
- Add Genre Modal (`showBulkGenrePicker`):
  - In-memory merge logic:
    ```java
    // Deduplicates case-insensitively and formats each token with Title-Case
    java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>();
    for (String g : existing.split(",")) if (!g.trim().isEmpty()) merged.add(cleanGenre(g));
    for (String g : newGenres.split(",")) if (!g.trim().isEmpty()) merged.add(cleanGenre(g));
    ```
  - Persists directly into `folio_metadata.db` and refreshes catalog and filter chips reactively.

### B. Health Audit Tile Alignment (`FolioMoreView.java`)
- Centered typography:
  ```java
  valTv.setGravity(Gravity.CENTER);
  lblTv.setGravity(Gravity.CENTER);
  ```

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Bulk `+ Genre` Action & Modal** | Add Genre Dialog / Catalog | `verify_r87_bulk_genre_bar.png` | **VERIFIED**: Drilled into `No Genre` (2 books: *Seneca*, *Cato the Elder*), selected all, and tapped `+ Genre`; verified dark `🏷 Add Genre (2 books)` modal with horizontal genre chips (`+ Fiction`, `+ History`, etc.) and text input. |
| **Centered Metadata Health Strip** | Library Insights Modal | `verify_r87_insights_centered_v87.png` | **VERIFIED**: Opened `📊 Library Insights`; verified center-aligned 3-tile Metadata Health strip (`1 › No Cover`, `2 › No Genre`, `21 › No Synopsis`) and confirmed Control Center badge reads `Folio v8.7 KFX`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
