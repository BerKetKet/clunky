# Round 150 Instructions — Milestone Azath v17.0 KFX (Full Granite & Limestone Dual-Theme Parity Across Metadata Catalog, Batch Importer & Web Book Browser + GitHub Milestone Push)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v17.0 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.  
**Strict Policy**: **DO NOT launch, inspect, or reference Play Store Folio (`ct.reader`).**

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Milestone Round 150 (`Azath v17.0 KFX`)
1. **Zero-Arg Cached Stone Theme Getters (`FolioTheme.java`)**:
   - Added thread-safe `sLastLight` state and parameterless `bgCanvas()`, `bgModal()`, `bgSurface()`, `bgControl()`, `border()`, `textPrimary()`, `textSecondary()`, `textMuted()`, `accent()`, `accentFill()`, `accentBorder()`, `accentSoftText()` helpers.
2. **Full Limestone Light Mode & Granite Dark Mode Parity Across All 3 Primary Modals**:
   - **`FolioMetadataCatalogDialog.java`**: Every modal card, filter chip, search box, archival monogram cover, Book Details card, and CTA dynamically renders in **Granite (Dark)** or **Limestone (Light)**!
   - **`FolioFolderBrowserDialog.java`**: Batch EPUB Importer dynamically adapts every card, folder chip, Sort/Hide pill, and checkbox to Granite or Limestone!
   - **`FolioWebBrowserDialog.java`**: Web Book Browser toolbar, bookmarks bar, EPUBs/Downloads chips, and modals dynamically adapt to Granite or Limestone!
3. **Milestone Bump (`FolioMoreView.java`)**: Bumped to **`Azath v17.0 KFX`**.

## Task A: Build, Deploy, Stage & GitHub Milestone Push (`Azath v17.0 KFX`)
1. Copy all 5 Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Rebuild via `rebuild_fast.sh` (`azath_v17.0_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v17.0_kfx/base.apk` with SHA256, and push **`Azath v17.0 KFX`** to GitHub (`origin master`).

## Task B: Hardware Verification (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report150.zip`:
1. `verify_r150_azath_catalog_limestone.png`: Toggle `Granite -> Limestone` in Control Center (`v17.0 KFX`), then open **Library Metadata & Genres** to verify the entire catalog modal (frame, chips, cards, EB Garamond heading, dark-tinted icons, monogram covers) renders cleanly in warm Limestone Light Mode! (Then toggle back to `Granite` dark mode after capture.)
2. Confirm `com.amazon.kindle` remains untouched and zero crashes / zero `SIGSYS` in `logcat`.
