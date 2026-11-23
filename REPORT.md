# Round 107 Deliverables Report — Folio v10.7 KFX & Fight Club Visual QC Comparison
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.7 KFX (Enhanced Typesetting)` installed at `2026-11-23 03:41:09`.

---

## 1. Task A: Drop-In Code Integration & Release Staging
1. **Drop-In Files Integrated**:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`:
     - **⬆ Parent Folder Button**: Added a compact, styled `⬆` parent-directory button directly to the left of the path input, enabling 1-tap upward directory traversal (`/sdcard/en/0` -> `/sdcard/en` -> `/sdcard`).
     - **Clickable 📁 Subfolder Rows**: Renders non-hidden subdirectories at the top of the folder list as clickable warm-amber `📁 <folderName>  Open ›` cards, allowing instant drilling into nested EPUB folders without typing paths manually.
     - **Hardened Event Binding**: Cleanly decoupled `btnUpDir` click handling from the quick-chip loop so parent navigation works immediately upon dialog opening and dynamically refreshes pinned/quick directory chips.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Version Bump**: Bumped badge and About Folio dialog to `Folio v10.7 KFX (Enhanced Typesetting)`.
   - Invariant: `MetadataEnricher.java` and `OpenCatalogView.java` were strictly preserved and not overwritten.

2. **Compilation, Packaging & Deployment**:
   - Rebuilt `classes9.dex` (366,152 B) via `d8`.
   - Reassembled APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`32001776e8e82623`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk` (137,486,022 B).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
1. **`verify_r107_importer_subfolders.png` (137,798 bytes)**:
   - Captured Batch EPUB Importer after stepping up from `/sdcard/en/0` to `/sdcard/en` via the new `⬆` parent button ([83, 558][153, 645]).
   - Verified path input updated to `/sdcard/en` ([169, 557][742, 646]).
   - Verified clickable subfolder rows rendered at top of list:
     - `📁 0  Open ›` ([115, 968][970, 1015])
     - `📁 1  Open ›` ([115, 1079][970, 1126])
   - Tapped `📁 0` to verify seamless downward navigation and restore `/sdcard/en/0` as `last_import_dir` (displaying all 719 EPUBs).
2. **`verify_r107_about_v107.png` (341,434 bytes)**:
   - Captured About Folio dialog confirming `Version: Folio v10.7 KFX (Enhanced Typesetting)`.
   - Live telemetry: 29 indexed books, 27 enriched records, 5 in progress, 1 finished, 1 queued.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED).

---

## 3. Visual QC & Side-by-Side Audit: *Fight Club* (Folio vs Stock Kindle)
Both apps were verified on the physical Galaxy M32 under the exact same theme settings:
- **Theme**: `Compact` theme (`checked=true`)
- **Page Color**: `White` background (`checked=true`)
- **Font Family**: `Bookerly` font (`checked=true`)
- **Margins**: `Thin margins` (`checked=true`)

### Side-by-Side Screenshots Included in Report Archive:
1. **Page 1 (Cover Page)**:
   - `folio_fight_club_page1.png` (1,910,864 bytes) vs `kindle_fight_club_page1.png` (1,926,911 bytes)
   - **Visual Finding**: Mean pixel delta is only **0.54** (nearly identical). Both apps render the cover image fullscreen without border clipping or distortion.
2. **Page 2 (Title Page — "FIGHT CLUB / Chuck Palahniuk")**:
   - `folio_fight_club_page2.png` (37,768 bytes) vs `kindle_fight_club_page2.png` (27,713 bytes)
   - **Layout Delta**:
     * In **Stock Kindle**: The title block is centered vertically with generous top padding (`Y = [388, 697]`, block height 309 px), stylized with uppercase letter-spacing (`$761: [$760]`) and sans-serif typography. No footer is rendered.
     * In **Folio**: The title block is positioned near the top of the screen (`Y = [174, 411]`, block height 237 px) with generic serif styling and smaller leading. Additionally, Folio stamps a reading footer (`Location 4 of 2463 • 0%`) at the bottom (`Y = [2259, 2283]`).
3. **Page 3 (Chapter 1 Opening)**:
   - `folio_fight_club_page3.png` (343,990 bytes) vs `kindle_fight_club_page3.png` (304,243 bytes)
   - **Safe-Area Insets & Vertical Bounds**:
     * In **Stock Kindle**: `CHAPTER 1` starts at `Y = 296` and body text ends at `Y = 2173` (vertical text height = 1,877 px). Kindle reserves generous safe margins for the status bar (top ~213 px) and system navigation bar / reading buffer (bottom ~167 px). No footer text is displayed during reading mode.
     * In **Folio**: `CHAPTER 1` starts at `Y = 133` (only 50 px below the 83 px status bar) and text extends down to `Y = 2240` with the footer at `Y = [2259, 2283]` (vertical text height = 2,150 px). Folio draws text almost all the way to the navigation bar.
   - **Pagination Consequence**:
     * Due to Kindle's larger top/bottom margins, the opening chapter text on Page 3 cuts off at `"Mix the nitro with sawdust... A lot of folks mix their nitro with cotton"`, pushing the rest of that paragraph to Page 4.
     * In Folio, because of the larger 2,150 px vertical text canvas, the entire paragraph (`"...Paraffin has never, ever worked for me."`) fits onto Page 3.
   - **KFX Styling Engine**:
     * In Stock Kindle KFX (`CR!HTGKZMM11X16S9ZHPAFFYQVVFEP5.kfx`), Amazon's compiler preserved publisher CSS rules: title margin-top `3.66em`, margin-bottom `15em`, letter-spacing `$760`, and paragraph text indent `2.18%`.
     * In Folio KFX (`Palahniuk_Chuck_FOLIO_7313DDB45B1B.kfx`), `libkfxconv.so` applies default fallback styles (`style_h1_center` with margin-top 1.2em, `style_p` with indent 1.0em).

---

## 4. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 5. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk`
- Hardware Screenshots:
  - `verify_r107_importer_subfolders.png` (Task B1)
  - `verify_r107_about_v107.png` (Task B2)
  - `folio_fight_club_page1.png` (Folio Cover)
  - `folio_fight_club_page2.png` (Folio Title Page)
  - `folio_fight_club_page3.png` (Folio Chapter 1)
  - `kindle_fight_club_page1.png` (Kindle Cover)
  - `kindle_fight_club_page2.png` (Kindle Title Page)
  - `kindle_fight_club_page3.png` (Kindle Chapter 1)
- Instructions & Protocol: `INSTRUCTIONS_ROUND_107.md`, `protocol.json`
