# Folio Milestone Release Report: Custom Shelves Manager & 1-Tap Filter Drill-Down (Round 083)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v8.3 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 083, Folio advanced to **Folio v8.3 KFX**. Gemini pre-authored drop-in Java updates for `FolioMoreView.java`. Antigravity integrated the single file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`, compiled Dalvik DEX (`classes9.dex`, 274,900 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Manage Shelves Action Card (`FolioMoreView.java`)**: Added a dedicated `🏷 Manage Shelves` action card directly under `📊 Library Insights` in Control Center (`"Rename, remove, or open your custom reading shelves"`).
2. **Interactive Custom Shelves Manager Modal (`FolioMoreView.java`)**: Tapping the card opens a dark modal reading from `folio_shelves` SharedPreferences, listing active shelves sorted by book count descending (`🏷 Classics · 1 book ›`).
3. **1-Tap Catalog Drill-Down**: Tapping any shelf row directly dismisses the modal and launches the Metadata Catalog pre-filtered to that shelf (`SHELF:Classics` showing `1 of 27` cataloged books).
4. **Rename & Remove Management Pills**: Each shelf row includes interactive `Rename` (allowing live renaming and merging across books) and `Remove` (with confirmation to unassign the shelf while preserving books and metadata).
5. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v8.3 KFX (Enhanced Typesetting)`**.
6. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v8.3 KFX

### A. Custom Shelves Action Card (`FolioMoreView.java`)
- Card added to Control Center layout:
  ```java
  container.addView(makeActionCard(
          ctx, d,
          "🏷", "Manage Shelves",
          "Rename, remove, or open your custom reading shelves",
          new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  showManageShelvesDialog(ctx);
              }
          }
  ));
  ```

### B. Custom Shelves Modal Implementation (`FolioMoreView.java`)
- Reads shelf assignments across all indexed books from `folio_shelves`:
  ```java
  final SharedPreferences shelfPrefs = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);
  final java.util.Map<String, ?> rawAll = shelfPrefs.getAll();
  // Count frequency and sort descending
  ```
- Renders row for each shelf:
  - Shelf badge & counter: `🏷 Classics · 1 book ›` (taps invoke `FolioMetadataCatalogDialog.showWithShelf(ctx, s)`).
  - Rename pill: prompts for new name and updates all matching entries in `folio_shelves`.
  - Remove pill: confirms unassignment and removes keys matching that shelf.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v8.3 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Manage Shelves Card** | More Tab (Control Center) | `screenshots/verify_r83_control_center_v83.png` | **VERIFIED**: Scrolled Control Center; verified `🏷 Manage Shelves` action card rendered under `📊 Library Insights` and above `⚙️ Reader & App Settings`. |
| **Manage Shelves Modal & Drill-Down** | Manage Shelves Dialog | `screenshots/verify_r83_manage_shelves.png` | **VERIFIED**: Tapped `Manage Shelves`; verified modal displaying `🏷 Classics` (`1 book ›`) with `Rename` and `Remove` pills. Tapped `🏷 Classics` row and verified catalog opened filtered to `🏷 Classics (1)` with *1984*. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
