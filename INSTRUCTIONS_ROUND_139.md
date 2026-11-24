# Round 139 Instructions — Azath v14.5 KFX (Home Screen Archival EB Garamond Covers, Offline Mode Card, Dark Offline WebView Verification & GitHub Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.5 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 139
1. **Home Screen Archival EB Garamond Covers & `.cover.jpg` Lookup (`OpenCatalogView.java`)**:
   - `buildLocalCoverCard` now checks `lb.filePath + ".cover.jpg"` when `lb.coverPath` is empty so local KFX covers render on Home automatically.
   - Upgraded coverless local book cards from plain centered sans text to **Literary Archival Edition Covers**: large Regular **EB Garamond** drop-cap initial (`34sp`), `#5C2D26` crimson spine rule, and EB Garamond title on `#211C1B`/`#2A2423` cards.
2. **Graceful Offline Mode Card on Home (`OpenCatalogView.java`)**:
   - Replaced raw `Catalog error: Unable to resolve host "gutendex.com"...` text under `Popular Free EPUBs` with a calm `#181515` rounded **Offline Mode** card featuring EB Garamond heading, Inter copy, a tonal ember-crimson **`Retry`** pill, and a **`Local Catalog`** pill opening `FolioMetadataCatalogDialog`.
3. **Version Bump (`FolioMoreView.java`)**:
   - Bumped to **`Azath v14.5 KFX`**.

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy `OpenCatalogView.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/` (keeping Round 137/138 `FolioIcons.java`, `FolioTheme.java`, `FolioWebBrowserDialog.java`, `assets/azath_fonts/`, and `assets/azath_icons/` intact).
2. Rebuild `classes9.dex` + APK via `rebuild_fast.sh` (`azath_v14.5_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.5_kfx/base.apk` with SHA256, and push `Azath v14.5 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report139.zip`:
1. `verify_r139_azath_home.png`: **Azath Home tab** showing the new EB Garamond archival drop-cap covers + crimson spine rules in `Continue Reading` AND the clean **Offline Mode** card (`Retry` + `Local Catalog` pills) under `Popular Free EPUBs`.
2. `verify_r139_azath_web_browser_offline.png`: Open **Web Book Browser** while offline to verify the Round 138 dark Obsidian-Ivory-Crimson `Page unavailable offline` card with `Try Again` button.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
