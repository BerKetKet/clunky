# Folio Milestone Release Report: Up Next Reading Queue (Round 090)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.0 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Milestone Round 090, Folio advanced to **Milestone Folio v9.0 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity resolved a minor syntax quote escaping issue in `FolioMetadataCatalogDialog.java` (line 1515), compiled Dalvik DEX (`classes9.dex`, 320,168 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **📌 Up Next Reading Queue Core (`FolioMetadataCatalogDialog.java`)**:
   - Backed by persistent `SharedPreferences("folio_queue")` storing book paths with insertion timestamps for FIFO ordering.
   - Helper methods implemented: `isInQueue(ctx, kfxPath)`, `setInQueue(ctx, kfxPath, inQ)`, `loadQueuePaths(ctx)`, `loadQueueMap(ctx)`.
2. **Book Details Queue Toggle Pill (`FolioMetadataCatalogDialog.java`)**:
   - Integrated dynamic violet pill right beside the Custom Shelf pill:
     * Unqueued state: **`📌 Up Next`** (`#2E1065` fill, `#7C3AED` border).
     * Queued state: **`📌 Queued`** (`#4C1D95` fill, `#A78BFA` border).
   - Tapping toggles status with live Toast confirmation and catalog refresh.
3. **Dynamic Catalog Queue Filter Strip (`FolioMetadataCatalogDialog.java`)**:
   - Whenever 1+ books are queued, the horizontal filter strip automatically injects a violet **`📌 Up Next (N)`** chip.
   - 1-tap filtering isolates queued books.
4. **Control Center `📌 Up Next Queue` Carousel (`FolioMoreView.java`)**:
   - Located on Control Center directly below `📖 Continue Reading`.
   - When empty, presents a compact `+ Pick ›` prompt.
   - When books are queued, displays a horizontal carousel featuring cover thumbnail, book title, author, **`Start Reading ›`** (1-tap instant launch into KFX reader), an inline **`✕`** unqueue button, and header **`See All (N) ›`** deep-link directly into the catalog queue filter.
5. **Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center badge and About Folio dialog string to **`Milestone Folio v9.0 KFX (Enhanced Typesetting)`**.
6. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.0 KFX

### A. Up Next Reading Queue Architecture (`FolioMetadataCatalogDialog.java` & `FolioMoreView.java`)
- Storage & Retrieval:
  ```java
  public static boolean isInQueue(Context ctx, String kfxPath) {
      if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return false;
      try {
          return ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).contains(kfxPath);
      } catch (Throwable ignored) { return false; }
  }
  public static void setInQueue(Context ctx, String kfxPath, boolean inQ) {
      if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
      try {
          SharedPreferences.Editor ed = ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).edit();
          if (inQ) ed.putLong(kfxPath, System.currentTimeMillis());
          else ed.remove(kfxPath);
          ed.apply();
      } catch (Throwable ignored) {}
  }
  ```
- Carousel UI in Control Center:
  - Dynamically sorts queued books by FIFO insertion timestamp.
  - Resolves cover art from `.cover.jpg` sidecar or fallback placeholder.
  - Launches reader via `OpenCatalogView.launchReaderForFile(ctx, rec.kfxPath)`.
  - Inline `✕` allows instant unqueuing without opening the book details modal.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Book Details Up Next Queue** | Book Details Modal | `verify_r90_book_details_queue_v90.png` | **VERIFIED**: Opened *Animal Farm* Details (`ⓘ`); tapped `📌 Up Next` to queue it; verified active violet **`📌 Queued`** pill alongside Custom Shelf pill (`[455, 1499][655, 1569]`). |
| **Control Center Up Next Carousel** | Control Center (More Tab) | `verify_r90_up_next_control_center.png` | **VERIFIED**: Switched to More tab; verified top header showing **`Folio v9.0 KFX`** badge, `📖 Continue Reading` carousel, and live **`📌 Up Next Queue`** carousel card with cover thumbnail, *Animal Farm*, *George Orwell*, **`Start Reading ›`**, and **`✕`** unqueue button. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
