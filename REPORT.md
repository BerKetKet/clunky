# Folio Milestone Release Report: 3-Column Cover Art Grid View Toggle (⊞ / ☰) in Metadata Catalog (Round 080)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Milestone Folio v8.0 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Milestone Round 080, Folio advanced to **Milestone Folio v8.0 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 267,104 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Interactive 3-Column Cover Art Grid View (`FolioMetadataCatalogDialog.java`)**: Added a persistent `⊞` / `☰` view-mode toggle pill in the catalog top bar. Tapping it switches cleanly between the rich List view and a 3-column Cover Art Grid showcasing extracted `.cover.jpg` covers, 2-line titles, compact status/rating badges (`✓ Done`, `X%`, `★ 5/5`, `Unread`), and `ⓘ` details shortcuts.
2. **Persistent View Mode Memory (`FolioMetadataCatalogDialog.java`)**: The user's preferred catalog view mode (`catalog_grid_mode`) is saved and restored automatically via `SharedPreferences("folio_ui_prefs")`.
3. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Milestone Folio v8.0 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Milestone Folio v8.0 KFX

### A. 3-Column Cover Art Grid Mode (`FolioMetadataCatalogDialog.java`)
- Maintains persistent grid state:
  ```java
  boolean savedGrid = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
          .getBoolean("catalog_grid_mode", false);
  final boolean[] gridMode = new boolean[] { savedGrid };
  ```
- Renders top-bar view mode toggle pill (`⊞` when in List mode, `☰` when in Grid mode):
  ```java
  btnViewMode.setOnClickListener(v -> {
      gridMode[0] = !gridMode[0];
      btnViewMode.setText(gridMode[0] ? "☰" : "⊞");
      ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
              .edit().putBoolean("catalog_grid_mode", gridMode[0]).apply();
      renderList();
  });
  ```
- Constructs balanced 3-column rows using horizontal `LinearLayout` wrappers with `weight=1f` per card:
  - Extracted cover art (`.cover.jpg`) loaded via `BitmapFactory.decodeFile()`.
  - 2-line title with ellipsis (`TextUtils.TruncateAt.END`).
  - Compact status pill (`✓ Done` emerald, `X% read` sky blue, `Unread` slate) and star rating (`★ 5/5` amber).
  - Inline `ⓘ` details button for immediate book inspection and editing.

### B. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Milestone Folio v8.0 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **3-Column Cover Art Grid** | All Metadata › (Grid Mode) | `screenshots/verify_r80_catalog_cover_grid_view.png` | **VERIFIED**: Tapped `⊞` in catalog top bar; verified 3-column grid displaying covers, titles, status pills (`✓ Done`, `Unread`), rating badges (`★ 5/5`), `ⓘ` buttons, and `☰` toggle pill. |
| **Milestone Folio v8.0 KFX Badge** | More Tab (Control Center) | `screenshots/verify_r80_control_center_v80.png` | **VERIFIED**: Opened More tab; verified amber `Folio v8.0 KFX` milestone badge and offline tools header. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
