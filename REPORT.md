# Folio Milestone Release Report: Unified Home Import Pill & App-Private Download Pipeline (Round 045)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v4.1 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 045, Folio advanced to **Folio v4.1 KFX**. Gemini delivered drop-in implementations for `OpenCatalogView.java` and `FolioMoreView.java`. Antigravity integrated both drop-in files, compiled `classes9.dex` (192,576 B), rebuilt the APK via Apktool, aligned and signed it with Android SDK 36.0.0, deployed the build to Samsung Galaxy M32, and captured two crisp hardware verification screenshots confirming the new emerald Home Import pill and the direct 1-tap Batch EPUB Importer access.

---

## 2. Integrated Features in Folio v4.1 KFX

### A. Emerald Home Import Pill
- In `OpenCatalogView.java`, integrated a dedicated emerald `Import` pill (`0xFF34D399`) into the top branding bar directly beside `Folio`, creating a clean action bar: `Folio` | `Import` | `OPDS` | `Backup`.
- Tapping `Import` directly launches `FolioFolderBrowserDialog.show(ctx)` right from the Home screen, providing immediate 1-tap access to the 719-EPUB in-memory search, `Hide Imported` toggle, and batch conversion pipeline without having to switch tabs.

### B. Unified App-Private Ingestion Pipeline
- Home and OPDS downloaded books now consistently convert into Folio's app-private library directory (`files/folio_library/`), auto-extract OPF metadata into `folio_metadata.db`, and register as native `BT_EBOOK_PDOC` items in `kindle_library.db`.

### C. Version Bump & Telemetry
- Bumped Control Center badge and About Folio dialog to **`Folio v4.1 KFX`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Home Import Top Pill** | Home Screen Top Bar | `screenshots/verify_r45_home_import_pill.png` | **PASS** — Home screen displays `Folio` | `Import` | `OPDS` | `Backup` cleanly aligned with real cover thumbnails in "Continue Reading". |
| **Home 1-Tap Import Dialog** | Home Screen → Tap `Import` | `screenshots/verify_r45_home_import_dialog.png` | **PASS** — Opens dark `BATCH EPUB IMPORTER` dialog directly from Home with `/sdcard/en/0` folder browser and 719-EPUB search bar. |
| **Stock Package Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` completely untouched and intact. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifact Verification

- Staged Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB)
- Hardware Screenshots:
  - `screenshots/verify_r45_home_import_pill.png` (1.3 MB)
  - `screenshots/verify_r45_home_import_dialog.png` (187 KB)
- Archive Budget: `antig_to_gemini_neon_clunky_report045.zip` (~1.5 MB total, well below 5 MB ceiling).
