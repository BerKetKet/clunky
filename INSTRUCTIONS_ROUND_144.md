# Round 144 Instructions — Major Milestone Azath v15.0 KFX (Unified Obsidian-Ivory-Crimson Edition, Reader Verification & Full GitHub Milestone Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v15.0 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Major Milestone Summary (`Azath v15.0 KFX`)
With Round 143's Dark Obsidian Library generic cover patch verified on hardware, all three main tabs (**Home**, **Library**, **Control Center**) and all secondary modals (**Metadata Catalog**, **Batch Importer**, **Web Book Browser**, **Reading Goal**, **Library Insights**, **Reading Log**, **Offline HTML Export**) are now 100% unified under:
- **Brand & Package**: **Azath** (`com.azath.reader`), crimson boy-under-tree launcher icon, 17 bespoke ivory+crimson icons (`assets/azath_icons/`).
- **Typography**: Bundled **EB Garamond** (editorial serif headings `>= 17sp`) + **Inter** (crisp UI sans `< 17sp`) in `assets/azath_fonts/`.
- **Palette & Chrome**: Obsidian-Ivory-Crimson (`#0A0909` / `#121010` / `#181515` / `#211C1B` / `#2A2423` / `#F2EFE9` / `#8E867E` / `#E05A47` / `#2A1816` / `#5C2D26`), Ember Crimson bottom navigation (`patch_classes2_azath.py`), single-header Home & Control Center (`patch_classes3_azath.sh`), and Dark Obsidian generic Library covers (`patch_res_azath_covers.py`).

## Task A: Milestone Build, Staging & Full GitHub Push (`Azath v15.0 KFX`)
1. Copy `FolioMoreView.java` (`Azath v15.0 KFX`) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Update `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v15.0_kfx/base.apk`, run a clean rebuild (applying `patch_classes2_azath.py`, `patch_classes3_azath.sh`, and `patch_res_azath_covers.py`), and deploy `com.azath.reader` to **Samsung Galaxy M30**.
3. Commit and push the complete **`Azath v15.0 KFX`** milestone (all updated Java sources, `assets/azath_icons/`, `assets/azath_fonts/`, `patch_classes2_azath.py`, `patch_classes3_azath.sh`, `patch_res_azath_covers.py`, and `rebuild_fast.sh`) to GitHub (`origin master`) and record the commit SHA in `REPORT.md`.

## Task B: End-to-End Milestone Verification (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report144.zip`:
1. `verify_r144_azath_control_center_v15.png`: **Azath Control Center** confirming the **`v15.0 KFX`** milestone pill badge.
2. `verify_r144_azath_reader.png`: Tap a book from Home or Library (e.g., *The 210th Day* or *Brave New World*) to verify clean KFX reader opening and zero reader regressions on `com.azath.reader`.
3. Confirm `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and zero fatal crashes in `logcat`.
