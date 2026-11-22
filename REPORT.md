# Folio Release Report: Curated-Only Reading Log Toggle & Top-Placed Up Next Queue (Round 097)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v9.7 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v9.7_kfx/base.apk` (137 MB / 137,453,254 bytes)

---

## 1. Executive Summary

In Round 097, Folio advanced to **Folio v9.7 KFX**. Drop-in updates were integrated into `FolioMetadataBackup.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 334,348 B), rebuilt, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Curated-Only vs All Books Reading Log Toggle (`FolioMetadataBackup.java` + `FolioMoreView.java`)**:
   - Added interactive **`✨ Scope: Curated Only` / `📚 Scope: All Books`** scope toggle pill right beside `📊 Export CSV` in the `Reading Log (.md)` modal.
   - Defaults to **Curated Only** mode: filters the markdown export strictly to books with active reading engagement (Finished, Reading In-Progress, Favorited, Star-Rated, Annotated with Notes, Assigned to Shelves, or in Up Next Queue).
   - Tapping the scope pill toggles dynamically between Curated Only and All Books with instant preview re-rendering and live saved file updating (`folio_reading_log.md`).
2. **Top-Placed Up Next Queue in Markdown Journal (`FolioMetadataBackup.java`)**:
   - Promoted **`## 📌 Up Next Queue (N)`** to the prominent top section of the Markdown log right beneath the library summary and Reading Goal stats, eliminating the need to scroll through all entries to view queued reading targets.
3. **Control Center & About Version Bump (`FolioMoreView.java`)**:
   - Bumped Control Center header badge and About Folio dialog string to **Folio v9.7 KFX (Enhanced Typesetting)**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v9.7 KFX

### A. Curated Scope & Top Queue Architecture (`FolioMetadataBackup.java`)
- Scope Filtering Logic:
  ```java
  public static String buildReadingLogMarkdown(Context ctx, boolean curatedOnly) {
      ...
      if (curatedOnly && pct <= 0 && !isFav && stars <= 0 && note.isEmpty() && shCheck.isEmpty() && !inQ) {
          continue;
      }
      ...
  }
  ```
- Top Queue Placement:
  - Formatted directly after Reading Goal:
    ```markdown
    # Folio Curated Reading Log (YYYY-MM-DD)

    **Library:** ...
    **2026 Reading Goal:** ...

    ## 📌 Up Next Queue (1)

    - **Animal farm: a fairy story** — George Orwell
    ```

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Curated Reading Log** | Reading Log Modal | `verify_r97_curated_reading_log.png` | **VERIFIED**: Opened `📄 Reading Log & Export`; verified `✨ Scope: Curated Only` toggle pill alongside `📊 Export CSV` (`[83, 740][416, 807]`), `# Folio Curated Reading Log`, and `## 📌 Up Next Queue (1)` immediately visible at top of preview. |
| **About Folio v9.7 KFX** | About Folio Modal | `verify_r97_about_v97.png` | **VERIFIED**: Tapped Control Center version badge; verified modal confirming `Version: Folio v9.7 KFX (Enhanced Typesetting)` and live stats. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
