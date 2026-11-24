# Round 141 Instructions — Azath v14.7 KFX (Single-Header Control Center Collapse + Clean Home Author Formatting & GitHub Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.7 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 141
1. **Single-Header `More` (Control Center) TopBar Collapse (`patch_classes3_azath.sh`)**:
   - Apply the exact same clean `HeaderMode;->c` (empty header) collapse used on `HomeScreenlet` to the **`More`** tab's Screenlet so the redundant stock `More` + gear + `Search Azath` bar above our `Control Center` disappears cleanly, allowing **`Control Center`** (which already has bespoke Search, Compass, and Settings access) to extend right up under the status bar just like **`Azath`** on Home!
   - Keep the **`Library`** tab's header & search bar intact.
2. **Clean Author Formatting & Direct Bell Intent on Home (`OpenCatalogView.java`)**:
   - Added `formatHomeAuthor(...)` normalizing `"Soseki, Natsume"` -> `"Natsume Soseki"` and trimming multi-author semicolons (`"Ryunosuke Akutagawa;..."` -> `"Ryunosuke Akutagawa"`) on Home's `Continue Reading` cards.
   - Permanently synced the direct `com.amazon.kindle.inapp.notifications.InAppNotificationsActivity` launch in `makeBellIconButton`.
3. **Version Bump & Top Padding (`FolioMoreView.java`)**:
   - Adjusted top container padding (`12dp`) for single-header alignment and bumped to **`Azath v14.7 KFX`**.

## Task A: Extend Header Patch, Build, Deploy, Stage & GitHub Sync
1. Copy `OpenCatalogView.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Extend `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` to also switch the `More` Screenlet's `HeaderMode` to `HeaderMode;->c` (leaving `Library` untouched).
3. Rebuild via `rebuild_fast.sh` (`azath_v14.7_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.7_kfx/base.apk` with SHA256, and push `Azath v14.7 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report141.zip`:
1. `verify_r141_azath_control_center.png`: **Azath More (Control Center)** tab showing **`Control Center`** (`v14.7 KFX`) extending cleanly to the top directly under the status bar with zero redundant `More` / `Search Azath` double-header.
2. `verify_r141_azath_home.png`: **Azath Home tab** confirming clean normalized author names (`Natsume Soseki`, `Ryunosuke Akutagawa`) under `Continue Reading`.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
