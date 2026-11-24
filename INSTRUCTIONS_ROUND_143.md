# Round 143 Instructions — Azath v14.9 KFX (Dark Obsidian Library Generic Book Covers + Tonal Importer Pills & GitHub Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.9 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 143
1. **Dark Obsidian Generic Cover Tiles in `Library` Tab (Fixes Round 142 Audit Finding!)**:
   - In `verify_r142_azath_library.png`, coverless books (*Moksha*, *Crome Yellow*, *The Dream of the Red Chamber*, *Island*, *Brave New World*, *Confessions of Zeno*) render as bright white/light-grey stock Kindle generic cover cards (`#E6E6E6` / `#F2F2F2`) with dark open-book icons, clashing harshly against Azath's dark Obsidian UI.
   - **Action for Antigravity**: Locate the stock Library generic cover layout / drawable / color tokens in `base_dec/res/` (e.g., `lib_book_cover*`, `generic_cover*`, `placeholder_cover*`, `cover_default*`, or `ic_book*` + text colors in `res/layout/`, `res/drawable*/`, `res/values*/colors.xml`) — or its Compose/smali color constants if Compose-rendered — and patch:
     - Generic cover tile background -> **Azath Obsidian `#211C1B`** (or `#181515`) with subtle `#2A2423` border
     - Generic cover title text -> **Archival Ivory `#F2EFE9`**
     - Generic cover author text & open-book icon -> **Warm Ash `#8E867E`** (or subtle `#5C2D26` crimson accent)
   - Persist this patch cleanly in `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` so future builds keep dark Obsidian generic covers automatically!
2. **Tonal Editorial Filter Pills in Batch Importer (`FolioFolderBrowserDialog.java`)**:
   - Unified active folder chips (`en/0`, etc.) and `Hide Imported: ON` pill to tonal `#2A1816` fill + `#5C2D26` border + `#F5B8AE` text (`999dp` radius), matching Catalog & Control Center while keeping `Open` and `Import Selected (N)` as primary `#E05A47` CTAs.
3. **Version Bump (`FolioMoreView.java`)**:
   - Bumped to **`Azath v14.9 KFX`**.

## Task A: Patch Generic Library Covers, Build, Deploy, Stage & GitHub Sync
1. Copy `FolioFolderBrowserDialog.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Apply & persist the Dark Obsidian Library generic cover tile patch in `rebuild_fast.sh`.
3. Rebuild via `rebuild_fast.sh` (`azath_v14.9_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.9_kfx/base.apk` with SHA256, and push `Azath v14.9 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report143.zip`:
1. `verify_r143_azath_library_dark_covers.png`: **Azath Library tab** confirming coverless books (*Moksha*, *Crome Yellow*, *The Dream of the Red Chamber*, *Island*, *Brave New World*, *Confessions of Zeno*) now render as dark Obsidian `#211C1B` tiles with warm ivory `#F2EFE9` titles and muted ash icons — ZERO blinding white placeholders!
2. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
