# Round 142 Instructions — Azath v14.8 KFX (Batch Importer Editorial Polish, Bottom Clearance & Library Tab Visual Audit)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.8 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 142
1. **Batch EPUB Importer Editorial Polish (`FolioFolderBrowserDialog.java`)**:
   - Upgraded `Import EPUBs from Storage` heading to `22sp` Regular **EB Garamond** (`-0.015f` tracking) in warm archival ivory `#F2EFE9`, replaced remaining `Color.WHITE` occurrences with `#F2EFE9`, and switched the Sort button to `999dp` pill corners.
2. **Bottom Scroll Clearance & Version Bump (`OpenCatalogView.java`, `FolioMoreView.java`)**:
   - Increased bottom `ScrollView` padding from `80dp` to `96dp` on both Home and Control Center so bottom rows clear the floating last-read book cover cleanly, and bumped to **`Azath v14.8 KFX`**.

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy all 3 drop-in Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Rebuild via `rebuild_fast.sh` (`azath_v14.8_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.8_kfx/base.apk` with SHA256, and push `Azath v14.8 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification & Library Tab Visual Audit (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report142.zip`:
1. `verify_r142_azath_library.png`: **Azath Library tab** (3rd main tab) so we can audit its header, filter pills, covers, and typography for any remaining stock Kindle blue or styling mismatches.
2. `verify_r142_azath_importer.png`: **Import EPUBs from Storage** modal opened from Control Center verifying the `22sp` Regular EB Garamond heading, warm ivory text, and Obsidian-Ivory-Crimson controls.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
