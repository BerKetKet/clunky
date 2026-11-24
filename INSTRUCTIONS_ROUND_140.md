# Round 140 Instructions — Azath v14.6 KFX (Single-Header Home Bar Collapse + Integrated Notification Bell)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.6 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## User Directive Addressed in Round 140
- **Remove Redundant Stock Top `Home` Bar & Move Bell into `Azath` Header (`OpenCatalogView.java`)**:
  - `hideSearchKindleRecursive(...)` now detects the redundant stock top `"Home"` header bar (strictly within the top third of the screen so the bottom navigation `"Home"` tab is untouched), captures a `WeakReference` to its clickable notification bell (`sStockBellRef`), and sets the redundant top bar to `View.GONE` so our **Azath** header extends cleanly to the top of the screen.
  - Added a crisp circular `#181515`/`#2A2423` Notification Bell icon button (`makeBellIconButton`) directly beside `+ OPDS` and `Backup` on the `Azath` header row, forwarding taps to the captured stock notification bell (or showing a clean fallback toast if unavailable).
  - Note for Antigravity: If the stock top `Home` bar is rendered by Jetpack Compose (`AndroidComposeView` rather than an Android `TextView`) and therefore doesn't expose a `TextView("Home")` to the View tree, inspect `uiautomator dump` on Home to confirm its container view ID/height and collapse that specific header container cleanly in `OpenCatalogView.java` (preserving the bell tap handler if possible).
- **Permanent Carryover Sync & Version Bump (`FolioMoreView.java`)**:
  - Permanently wired `rec.title` into all `makeMiniCoverView` callers, updated `"Azath Library Insights\n"`, and bumped to **`Azath v14.6 KFX`**.

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy `OpenCatalogView.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Rebuild `classes9.dex` + APK via `rebuild_fast.sh` (`azath_v14.6_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.6_kfx/base.apk` with SHA256, and push `Azath v14.6 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report140.zip`:
1. `verify_r140_azath_home.png`: **Azath Home tab** confirming the redundant stock `Home` top bar is gone, **`Azath`** extends cleanly to the top with `+ OPDS`, `Backup`, and the new circular **Notification Bell** icon button on the same row.
2. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
