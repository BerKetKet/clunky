# Round 110 Deliverables Report — Milestone Folio v11.0 KFX (Control Center "🎲 Tonight's Pick" Unread Book Recommender)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Milestone Folio v11.0 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 02:54:45`.

---

## 1. Task A: Source Integration & Milestone Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Control Center Header Upgraded**: Added twin compact icon pills right beside the **`Folio v11.0 KFX`** badge:
       * `🎲` (soft-violet pill, `#C084FC` on `#2B1B3D` with rounded stroke) triggering the **🎲 Tonight's Pick** recommendation modal.
       * `🔍` (sky-blue pill, `ACCENT_TEAL` on `#152638`) for instant search.
     - **🎲 Tonight's Pick Modal (`showTonightsPickDialog`)**:
       * Draws exclusively from unread cataloged books in the local metadata database.
       * Renders standalone `.cover.jpg` art (`makeMiniCoverView`), title, normalized author, year/publisher tags, and genre pills.
       * Displays a 4-line synopsis preview with ellipsis fallback.
       * Features an in-modal **`🎲 Pick Another`** reroll button that dynamically swaps the card in-place without dialog dismiss/reopen.
       * Features **`ⓘ Book Details`** deeplinking into `FolioMetadataCatalogDialog.showBookDetails` and **`Start Reading`** footer CTA launching `ExternalEpubActivity.launchKfxReader`.
     - **Version Bump**: Bumped header badge and About Folio dialog to `Folio v11.0 KFX (Enhanced Typesetting)`.
     - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (371,500 bytes) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:54:45`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.0_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Both apps were verified on physical hardware under identical theme settings:

1. **`verify_r110_tonights_pick_v11.png` (294,292 bytes)**:
   - Captured **🎲 Tonight's Pick** recommendation modal launched directly from the `🎲` header icon pill.
   - Displays unread book recommendation card (*Delphi Complete Works of Tacitus* / Publius Cornelius Tacitus · 1830 · IndyPublish), genre tags (`History`, `Tacitus`), cover image, `🎲 Pick Another` reroll button ([154, 1287][529, 1373]), `ⓘ Book Details` ([550, 1287][926, 1373]), `Close` ([538, 1453][706, 1595]), and `Start Reading` ([706, 1453][978, 1595]).
2. **`verify_r110_about_v110.png` (353,863 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.0 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps - Android 13 (arm64-v8a)`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 4. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.0_kfx/base.apk` (132 MB)
- Hardware Screenshots:
  - `verify_r110_tonights_pick_v11.png`
  - `verify_r110_about_v110.png`
- Instructions & Protocol: `INSTRUCTIONS_ROUND_110.md`, `protocol.json`
