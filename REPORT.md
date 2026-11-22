# Folio Milestone Release Report: 1-Tap Select New / Clear Batch Pill in Folder Importer (Round 055)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v5.5 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v5.5_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 055, Folio advanced to **Folio v5.5 KFX**. Gemini provided drop-in implementations for `FolioFolderBrowserDialog.java` and `FolioMoreView.java`. Antigravity compiled Dalvik DEX (`classes9.dex`, 208,204 B), built, aligned, signed, and deployed the updated APK to the Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. 1-Tap **Select New** / **Clear (N)** batch selection pill in the folder importer: searching `/sdcard/en/0` for `Orwell` and tapping `Select New` automatically checked all 12 unimported Orwell EPUBs in 1 tap (skipping already-imported *1984* and *Animal Farm*), toggled the pill to amber `Clear (12)`, and updated the bottom CTA to `Import Selected (12)`.
2. Control Center **About Folio** dialog verified live displaying **`Folio v5.5 KFX (Enhanced Typesetting)`** alongside updated live reading stats (`5 in progress · 1 finished`).
3. Stock package `com.amazon.kindle` remains 100% untouched, intact, and read-only.

---

## 2. Integrated Features in Folio v5.5 KFX

### A. 1-Tap Select New / Clear (N) Batch Selection (`FolioFolderBrowserDialog.java`)
- Added a compact pill button beside the folder match count:
  - Default state: teal `Select New` (`#5EEAD4`).
  - Active selection state: amber `Clear (N)` (`#F59E0B`).
- Tapping `Select New`:
  - Scans currently visible files in the directory.
  - Automatically filters out files present in `importedSourceNames` (skipping already-imported titles).
  - Selects up to 25 new EPUBs in a single tap.
  - Updates the bottom import button to `Import Selected (N)`.
  - Toggles the batch pill to `Clear (N)`.
- Tapping `Clear (N)` clears all selections and restores the teal `Select New` state.

### B. Version Bump & Telemetry (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog to **`Folio v5.5 KFX (Enhanced Typesetting)`**.
- Confirmed live reading stats accurately report `5 in progress · 1 finished`.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **1-Tap Select New & Clear (12) Batch Pill** | Home → Import → /sdcard/en/0 → "Orwell" → Select New | `screenshots/verify_r55_select_new_orwell.png` | **PASS** — Tapping `Select New` automatically selects 12 new Orwell EPUBs (skipping already-imported *1984* and *Animal Farm*), toggles pill to amber `Clear (12)`, and updates bottom CTA to `Import Selected (12)`. |
| **Control Center About Folio v5.5** | More → About Folio | `screenshots/verify_r55_about_v55.png` | **PASS** — Dialog displays `Version: Folio v5.5 KFX (Enhanced Typesetting)` and accurate live reading stats (`5 in progress · 1 finished`). |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report055.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_055.md`
  - `protocol.json`
  - `screenshots/verify_r55_select_new_orwell.png`
  - `screenshots/verify_r55_about_v55.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v5.5_kfx/base.apk` (131 MB)
