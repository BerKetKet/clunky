# Folio Milestone Release Report: Frequency-Ranked Genre Chips & Unified Local Scan (Round 048)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.4 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 048, Folio advanced to **Folio v4.4 KFX**. Gemini delivered drop-in implementations for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both drop-in files, compiled `classes9.dex` (198,512 B), rebuilt the APK via Apktool, aligned and signed it with Android SDK 36.0.0, deployed the build to Samsung Galaxy M32, and captured two hardware verification screenshots confirming the new frequency-ranked Genre Chips with live counts and the Folio v4.4 KFX Control Center.

---

## 2. Integrated Features in Folio v4.4 KFX

### A. Frequency-Ranked Genre Chips with Live Counts
- In `FolioMetadataCatalogDialog.java`, replaced arbitrary genre ordering with frequency-ranked categorization across `folio_metadata.db`.
- Filters out verbose compound Library of Congress subheadings (omits strings containing `--`) and noise tags (`isNoiseTag`).
- Computes exact match counts for each genre pill: `All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)`.
- Renders clean, high-signal literary taxonomy chips in the scrollable top chip bar.

### B. Unified Local Storage Scanning Pipeline
- In `FolioMoreView.java`, updated `scanLocalLibrary()` to convert newly discovered EPUBs directly into Folio's app-private storage (`files/folio_library/`).
- Auto-extracts metadata via `EpubMetadataExtractor`, enriches via `MetadataEnricher`, and registers into both `folio_metadata.db` and native `kindle_library.db` as `BT_EBOOK_PDOC`.

### C. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v4.4 KFX`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Frequency-Ranked Genre Chips & Counts** | Home → All Metadata › | `screenshots/verify_r48_ranked_genre_chips_counts.png` | **PASS** — Genre Chips strip displays `All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)` with zero raw `--` strings. |
| **Folio v4.4 KFX Control Center** | More Tab | `screenshots/verify_r48_control_center_v44.png` | **PASS** — Displays `Folio v4.4 KFX` badge and offline Control Center tools. |
| **Stock Package Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` completely untouched and intact. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifact Verification

- Staged Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB)
- Hardware Screenshots:
  - `screenshots/verify_r48_ranked_genre_chips_counts.png` (336 KB)
  - `screenshots/verify_r48_control_center_v44.png` (218 KB)
- Archive Budget: `antig_to_gemini_neon_clunky_report048.zip` (~650 KB total, well below 5 MB ceiling).
