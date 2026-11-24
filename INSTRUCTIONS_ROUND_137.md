# Round 137 Instructions — Azath v14.3 KFX (Continue Reading Archival Monograms, Bespoke Catalog Authors Icon, Web Browser Verification & GitHub Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.3 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 137
1. **Archival Spine Monograms on Continue Reading & Global Search (`FolioMoreView.java`)**:
   - Replaced hardcoded sans-serif `"A"` fallback placeholders in `makeMiniCoverView` and Global Search with dynamic Regular Bookerly initial monograms (`extractCoverInitial(...)` — e.g. `B` for *Brave New World*) paired with a `#5C2D26` crimson spine rule on `#211C1B` / `#2A2423` rounded cards, matching the `Library Metadata & Genres` monogram cards identically.
   - Bumped Control Center badge & About dialog to **`Azath v14.3 KFX`**.
2. **Bespoke Ivory+Crimson Authors Icon in Catalog Toolbar (`FolioMetadataCatalogDialog.java`)**:
   - Switched the catalog toolbar Authors button from vector `FolioIcons.USER` to bespoke PNG `FolioIcons.AUTHORS` (`ic_authors.png`).

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy both drop-in Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Run `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` (preserving `patch_classes2_azath.py` + XML ember-crimson bottom nav patches) to deploy `com.azath.reader` (`Azath v14.3 KFX`) to the **Samsung Galaxy M30**.
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/azath_v14.3_kfx/base.apk` with SHA256.
4. Commit and push `Azath v14.3 KFX` (including `patch_classes2_azath.py`, `assets/azath_icons/`, and all updated sources) to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report137.zip`:
1. `verify_r137_azath_control_center.png`: Control Center showing `v14.3 KFX`, *Brave New World* displaying the new Regular Bookerly `B` archival monogram + crimson spine rule in `Continue Reading`, and `#E05A47` Ember Crimson bottom navigation `More` tab.
2. `verify_r137_azath_web_browser.png`: Open **Web Book Browser** from Control Center to verify the unified Obsidian-Ivory-Crimson palette across the browser toolbar, bookmarks bar, and `Downloads` / `EPUBs` chips.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
