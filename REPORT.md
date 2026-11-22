# Folio Milestone Release Report: Up Next Queue Export, Backup & Telemetry (Round 091)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.1 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 091, Folio advanced to **Folio v9.1 KFX**. Drop-in updates were integrated into `FolioMetadataBackup.java`, `FolioMetadataCatalogDialog.java`, and `FolioMoreView.java`. Antigravity resolved a missing import (`java.util.ArrayList`) in `FolioMetadataBackup.java`, compiled Dalvik DEX (`classes9.dex`, 320,780 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Full Queue Backup & Export (`FolioMetadataBackup.java`)**:
   - JSON Backup & Restore preserves `in_queue` state across full library backups and restores.
   - Markdown Reading Log (`folio_reading_log.md`) dynamically appends a dedicated **`## 📌 Up Next Queue (N)`** section listing all queued volumes with formatted author citations.
   - RFC-4180 CSV Export (`folio_library.csv`) includes an **`In Queue`** column (`Yes` / empty).
2. **Catalog List Queue Prefix Badge (`FolioMetadataCatalogDialog.java`)**:
   - Book titles in catalog list view now display a `📌 ` prefix badge when queued (alongside `★ ` for favorites and `📝 ` for personal notes).
3. **Library Insights Queue Telemetry & Version Bump (`FolioMoreView.java`)**:
   - Library Insights header summary line now integrates live queued telemetry:
     `27 cataloged · 5 reading · 1 finished`  
     `1 fav · 1 noted · 1 shelved · 1 queued · 5.0★ avg (1 rated)`
   - Control Center version badge and About Folio dialog bumped to **Folio v9.1 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.1 KFX

### A. Reading Log Markdown Export with Queue Section (`FolioMetadataBackup.java`)
- Appends queued books to reading log export:
  ```java
  List<FolioMetadataDb.BookRecord> qBooks = new ArrayList<>();
  for (FolioMetadataDb.BookRecord r : all) {
      if (r.kfxPath != null && FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
          qBooks.add(r);
      }
  }
  if (!qBooks.isEmpty()) {
      md.append("## 📌 Up Next Queue (").append(qBooks.size()).append(")\n\n");
      for (FolioMetadataDb.BookRecord r : qBooks) {
          String ca = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
          md.append("- **").append(r.title != null ? r.title : "Untitled").append("**");
          if (!ca.isEmpty()) md.append(" — ").append(ca);
          md.append("\n");
      }
      md.append("\n");
  }
  ```

### B. Library Insights Queue Counter (`FolioMoreView.java`)
- Header summary string enriched:
  ```java
  summaryTv.setText(all.size() + " cataloged · " + qs[1] + " reading · " + qs[2] + " finished\n"
          + favs + " fav · " + noted + " noted · " + shelved + " shelved · " 
          + com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countQueue(ctx) 
          + " queued · " + avgStr);
  ```

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Reading Log Up Next Section** | Reading Log Modal | `verify_r91_reading_log_up_next.png` | **VERIFIED**: Opened `📄 Reading Log & Export`; scrolled to bottom of preview; verified `## 📌 Up Next Queue (1)` section with `- **Animal farm: a fairy story** — George Orwell` rendered in markdown. |
| **Library Insights Queue Telemetry** | Library Insights Modal | `verify_r91_insights_queued_v91.png` | **VERIFIED**: Opened `📊 Library Insights`; verified header summary displays `1 fav · 1 noted · 1 shelved · 1 queued · 5.0★ avg (1 rated)`. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
