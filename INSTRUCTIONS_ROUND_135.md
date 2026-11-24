# Round 135 Instructions — Azath v14.1 KFX (Editorial Serif/Sans Typography & Unified Obsidian-Ivory-Crimson Design System)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.1 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Design Changes in Round 135 (Inspired by Play Store Folio `ct.reader` Reference Study)
1. **Harmonious Editorial Typography (`FolioTheme.java`)**:
   - Switched display headings (`>= 17sp`) from chunky `Bookerly Bold` to airy **Regular-weight `Bookerly`** (`bookerlyReg`) with `-0.018f` display tracking (`>= 19.5sp`), matching the serene editorial serif + clean sans hierarchy seen in Play Store Folio (`ref_playstore_folio_1.png`, `ref_playstore_folio_2.png`).
   - Kept all card titles, chips, buttons, and metadata (`< 17sp`) in crisp **Amazon Ember** sans-serif (`emberReg` / `emberBold`).
   - Unified `AlertDialog` window frames (`BG_MODAL = #121010`, 18dp rounded corners, 1dp `#2A2423` warm hairline border) and footer buttons (`#E05A47` Positive, `#8E867E` Negative, `#C4BDB5` Neutral).
2. **100% Unified Obsidian-Ivory-Crimson Palette (All 6 Modules)**:
   - Eliminated all ~35 stray cold-slate/navy/Kindle-blue/amber/emerald/purple hexes across `FolioTheme`, `FolioMoreView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioWebBrowserDialog`, and `FolioMetadataBackup` (including offline HTML export CSS).
   - Harmonized every screen, modal, checkbox, progress bar, pill, and border with the Azath crimson launcher icon and bespoke ivory+crimson icons:
     - Canvas `#0A0909` · Modal `#121010` · Surface `#181515` · Control `#211C1B` · Hairline `#2A2423`
     - Text Primary `#F2EFE9` (archival ivory) · Secondary `#C4BDB5` · Muted `#8E867E`
     - Unified Signature Accent `#E05A47` (ember crimson) · Soft Pill Fill `#2A1816` · Soft Pill Border `#5C2D26` · Soft Text `#F5B8AE`
3. **Control Center Header Polish (`FolioMoreView.java`)**:
   - `Control Center` rendered in `26sp` Regular `Bookerly` with `-0.02f` tracking, softer `12dp` icon buttons, and a pill-shaped `v14.1 KFX` ember-crimson badge.

## Task A: Build, Deploy & Stage Release (`com.azath.reader`)
1. Copy all 6 drop-in Java files from `dropin_src/com/amazon/kindle/epub/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Run `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to recompile `classes9.dex`, rebuild APK, align, sign, and deploy to `com.azath.reader` on the **Samsung Galaxy M30**.
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/azath_v14.1_kfx/base.apk` with SHA256.
4. Commit and push `Azath v14.1 KFX` to GitHub (`origin master`) if git working tree is clean.

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report135.zip`:
1. `verify_r135_azath_control_center.png`: **Azath** More tab (`Control Center` in Regular Bookerly, `v14.1 KFX` crimson pill, ember-crimson `Edit` / `+ Pick ›` accents, bespoke ivory+crimson icons).
2. `verify_r135_azath_modal_uniformity.png`: Open **Library Metadata & Genres** OR **2026 Reading Goal** modal to verify the unified Obsidian-Ivory-Crimson palette and Regular Bookerly modal heading.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
