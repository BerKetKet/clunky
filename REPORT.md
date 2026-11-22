# Folio Release Report: Persistent Finished Date Tracking & Export (Round 098)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.8 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.8_kfx/base.apk` (137 MB / 137,457,350 bytes)

---

## 1. Executive Summary

In Round 098, Folio advanced to **Folio v9.8 KFX**. Drop-in updates were integrated into `FolioMetadataCatalogDialog.java`, `FolioMetadataBackup.java`, and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 337,456 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **📅 Persistent Finished Date Tracking (`FolioMetadataCatalogDialog.java`)**:
   - Automatically tracks completion dates stored persistently in `SharedPreferences("folio_finished_dates")`.
   - In Book Details (`ⓘ`), books with 100% progress display an emerald interactive completion status pill:
     - `✓ Finished · Set date ›` when unset.
     - `✓ Finished on YYYY-MM-DD · Edit ›` once configured.
   - Tapping the status pill opens the dark **Finished Date** editor dialog pre-populated with today's date (`YYYY-MM-DD`), supporting 1-tap `Save Date`, `Clear`, and `Cancel`.
   - Marking any book finished automatically records today's timestamp; resetting to unread clears the entry.
2. **Backup, Reading Log & CSV Export Integration (`FolioMetadataBackup.java`)**:
   - Preserves `finished_date` field in JSON Backup and Restore (`folio_metadata_backup.json`).
   - Appends completion date `(YYYY-MM-DD)` next to `✓ Finished` in Markdown reading logs (`folio_reading_log.md`).
   - Adds a dedicated `Finished Date` column to the RFC-4180 CSV export (`folio_library.csv`).
3. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v9.8 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.8 KFX

### A. Finished Date Management Architecture (`FolioMetadataCatalogDialog.java`)
- Storage & Accessors:
  ```java
  public static String getFinishedDate(Context ctx, String kfxPath) {
      if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return "";
      try {
          String s = ctx.getSharedPreferences("folio_finished_dates", Context.MODE_PRIVATE).getString(kfxPath, "");
          return s != null ? s.trim() : "";
      } catch (Throwable ignored) { return ""; }
  }

  public static void setFinishedDate(Context ctx, String kfxPath, String dateStr) {
      if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
      try {
          android.content.SharedPreferences.Editor ed =
                  ctx.getSharedPreferences("folio_finished_dates", Context.MODE_PRIVATE).edit();
          if (dateStr == null || dateStr.trim().isEmpty()) ed.remove(kfxPath);
          else ed.putString(kfxPath, dateStr.trim());
          ed.apply();
      } catch (Throwable ignored) {}
  }
  ```
- Interactive Status UI in Book Details:
  - Header displays: `progTv.setText(finDate.isEmpty() ? "✓ Finished · Set date ›" : "✓ Finished on " + finDate + " · Edit ›");`
  - Tapping summons `showEditFinishedDateDialog` for immediate inline modification.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Finished Date in Book Details** | Book Details Modal | `verify_r98_finished_date_details.png` | **VERIFIED**: Opened Book Details (`ⓘ`) for *Moby Dick; Or, The Whale*; tapped `✓ Finished · Set date ›`, saved `2026-11-23`, and verified header status line dynamically updated to `✓ Finished on 2026-11-23 · Edit ›` (`[294, 702][996, 751]`). |
| **About Folio v9.8 KFX** | About Folio Modal | `verify_r98_about_v98.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v9.8 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
