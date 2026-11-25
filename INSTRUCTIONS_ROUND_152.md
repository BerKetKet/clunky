# Round 152 Instructions — Azath v17.2 KFX (Identical 22sp EB Garamond Tab Headers Across Home/Library/More + Dynamic Granite/Limestone Compose Bottom Bar Sync)

## MANDATORY ANTIGRAVITY ROLE & FAST-EXECUTION PROTOCOL
- **Your Role**: Fast Executor & Data Collector ONLY.
- **Execute Verbatim & Fast**: Copy the 3 drop-in files as-is, run `rebuild_fast.sh`, deploy to **Samsung Galaxy M30**, capture ONLY the 2 requested screenshots, and zip the report immediately.
- **STOP-AND-REPORT Rule**: If ANY issue occurs (compile error, bottom bar not recomposing, crash), **DO NOT self-debug or invent workarounds**. Immediately capture the raw output/screenshot and return it to Gemini in `REPORT.md`.
- **Safety & Policy**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**. **DO NOT** launch, inspect, or reference Play Store Folio (`ct.reader`).

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes Included in Round 152 Drop-Ins (`Azath v17.2 KFX`)
1. **Identical Top-Level Tab Headers Across All 3 Tabs (`OpenCatalogView.java`, `FolioMoreView.java`)**:
   - Matched **`Azath`** (Home) and **`Control Center`** (More) to **`22sp` Regular EB Garamond** with **`16dp` horizontal / `14dp` top padding**, matching **`Library`**'s measured `x=43px, height=63px` header baseline and cap height across all three main tabs!
2. **Dynamic Granite / Limestone Bottom Navigation Bar Sync (`FolioTheme.java`)**:
   - Added `FolioTheme.syncComposeBottomBarColors(ctx)` (called automatically on `applyWindowBars(ctx)` and theme toggles) reflecting directly onto `ib.b`'s Compose `Color` static fields (`functionalNeutralSurface`, `functionalNeutralSurfaceContainerLow/Lowest`, `functionalCorePrimary`, `functionalNeutralOnSurface`, `functionalNeutralOnSurfaceVariant`, `functionalNeutralOutlineVariant`) so the bottom navigation bar switches between **Granite `#15171A` / `#D4C9B8`** and **Limestone `#EFEAE0` / `#2D3138`** dynamically with zero `Activity.recreate()`!
3. **Version Bump**: **`Azath v17.2 KFX`**.

## Task A: Verbatim Drop-In Build, Deploy & Stage (FAST)
1. Copy `FolioTheme.java`, `OpenCatalogView.java`, and `FolioMoreView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Run `rebuild_fast.sh` (staging `azath_v17.2_kfx`) and deploy `com.azath.reader` to **Samsung Galaxy M30**.

## Task B: Quick Hardware Verification (Samsung Galaxy M30)
1. Capture `verify_r152_azath_home_limestone_bar.png`: Toggle `Granite -> Limestone` in Control Center (`v17.2 KFX`), then tap **Home** tab (to trigger bottom-bar recomposition) and screenshot Home showing both the unified `22sp` `Azath` header AND the bottom navigation bar state in Limestone Light Mode.
2. Capture `verify_r152_azath_control_center_granite.png`: Toggle back to **Granite** and screenshot Control Center (`v17.2 KFX`) confirming the unified `22sp` `Control Center` header and Granite bottom bar.
3. Confirm `com.amazon.kindle` remains untouched and zero crashes / zero `SIGSYS` in `logcat`.
