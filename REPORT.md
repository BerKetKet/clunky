# Folio Milestone Release Report: Bulk Queue Action & Grid-View Up Next Badge (Round 094)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.4 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 094, Folio advanced to **Folio v9.4 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 326,536 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Bulk `📌Queue` Action Pill (`FolioMetadataCatalogDialog.java`)**:
   - Upgraded the bottom Multi-Select Bulk Action Bar to a sleek 5-pill single row (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`).
   - Tapping `📌Queue` toggles queue status for all selected volumes with Toast feedback and immediate reactive catalog refresh.
2. **Grid-View Violet `📌 Up Next` Badge (`FolioMetadataCatalogDialog.java`)**:
   - Unread queued books in the 3-column Cover Art Grid view now display a violet **`📌 Up Next`** badge (`#C084FC`) instead of generic `Unread`.
3. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v9.4 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.4 KFX

### A. Bulk Queue Action (`FolioMetadataCatalogDialog.java`)
- 5-Pill Single-Row Layout:
  ```java
  final TextView btnBulkShelf = makeBulkActionPill(ctx, "Shelf", "#312E81", "#C7D2FE", "#4F46E5");
  final TextView btnBulkGenre = makeBulkActionPill(ctx, "+Genre", "#152638", "#38BDF8", "#1E4E75");
  final TextView btnBulkQueue = makeBulkActionPill(ctx, "📌Queue", "#2B1B3D", "#C084FC", "#6B21A8");
  final TextView btnBulkFav = makeBulkActionPill(ctx, "★Fav", "#3B2A14", "#FBBF24", "#784F1D");
  final TextView btnBulkDone = makeBulkActionPill(ctx, "✓Done", "#123328", "#34D399", "#1E664E");
  ```
- Batch Queue Logic:
  - Checks if all selected books are in queue; if so, unqueues them; otherwise queues all selected books in FIFO order.

### B. Grid-View Queue Badge (`FolioMetadataCatalogDialog.java`)
- Replaces generic `Unread` text for queued books:
  ```java
  } else if (isInQueue(ctx, rec.kfxPath)) {
      badgeTv.setText("📌 Up Next");
      badgeTv.setTextColor(Color.parseColor("#C084FC"));
  }
  ```

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Bulk Queue & Grid Badge** | Metadata Catalog (Multi-Select) | `verify_r94_bulk_queue_grid_badge.png` | **VERIFIED**: Opened Metadata Catalog in 3-column Grid view; tapped `☑ Select`; verified violet **`📌 Up Next`** badge on *Animal farm: a fairy story* (`[725, 1287][941, 1324]`) AND new 5-pill Bulk Action Bar (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`). |
| **About Folio v9.4 KFX** | About Folio Modal | `verify_r94_about_v94.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v9.4 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
