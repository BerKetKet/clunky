# Round 151 Instructions — Azath v17.1 KFX (UIX Consistency Pass: Uniform 26sp Headers, Limestone Modal Heading/Footer Fix & Fast Bottom-Bar Data Dump)

## MANDATORY ANTIGRAVITY ROLE & FAST-EXECUTION PROTOCOL
- **Your Role**: Fast Executor & Data Collector ONLY.
- **Execute Verbatim & Fast**: Copy the drop-in files as-is, run `rebuild_fast.sh`, deploy to **Samsung Galaxy M30**, capture ONLY the requested screenshots and data snippets, and zip the report immediately.
- **STOP-AND-REPORT Rule**: If you hit ANY compile error, runtime bug, or visual issue, **DO NOT spend time debugging, refactoring, or inventing patches/workarounds on your own**. Immediately stop, include the exact raw error/logcat/XML/smali snippet in `REPORT.md`, and send the zip back to Gemini to solve in the next round.
- **Safety & Policy**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**. **DO NOT** launch, inspect, or reference Play Store Folio (`ct.reader`).

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes Included in Round 151 Drop-Ins (`Azath v17.1 KFX`)
1. **Limestone Modal Heading & AlertDialog Footer Fix (`FolioTheme.java`, `FolioMetadataCatalogDialog.java`)**:
   - `FolioTheme.styleDialog(...)` now paints the AlertDialog's bottom button panel (`CLOSE` bar) with `bgModal(ctx)` (`#EFEAE0` in Limestone Light Mode, `#15171A` in Granite Dark Mode) so `CLOSE` is crisp dark granite on warm limestone.
   - `FolioMetadataCatalogDialog.java` replaces remaining `Color.WHITE` (including `"Library Metadata & Genres (24)"`) with `FolioTheme.textPrimary()` and permanently includes the Round 150 `javac` fixes.
2. **Uniform Page Header Sizing & System Bars (`OpenCatalogView.java`, `FolioMoreView.java`, `FolioTheme.java`)**:
   - Unified `Azath` (Home) and `Control Center` (More) to identical **`26sp` Regular EB Garamond**, `20dp` horizontal padding, and `12dp` top padding.
   - Added `FolioTheme.applyWindowBars(ctx)` to sync Android's status bar and system navigation bar to Granite/Limestone.
   - Bumped to **`Azath v17.1 KFX`**.

## Task A: Verbatim Drop-In Build & Deploy (FAST — No Extra Editing)
1. Copy all 4 Java files from `dropin_src/` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Run `rebuild_fast.sh` (staging `azath_v17.1_kfx`) and deploy `com.azath.reader` to **Samsung Galaxy M30**.

## Task B: Quick Screenshots + Raw Data Collection for Round 152 (Bottom Bar + `Library` Header Size)
1. Capture `verify_r151_azath_catalog_limestone.png`: Toggle `Granite -> Limestone` in Control Center (`v17.1 KFX`) and open **Library Metadata & Genres** (confirming dark `#1A1B1E` heading + warm `#EFEAE0` `CLOSE` footer bar; then toggle back to Granite).
2. **Quick Data Collection Only (DO NOT patch yet — just paste raw output into `REPORT.md`)**:
   - Paste the `Lib/b.<clinit>` smali lines around the dark & light bottom-bar background/icon color constants (`0xffd4c9b8L` / `0xff121212L` / `0xff000000L` in `classes2.dex`) so Gemini can author the exact Granite/Limestone bottom-bar patch in Round 152.
   - Paste the `uiautomator` bounds of the `"Library"` text node on the Library tab so Gemini can match its exact size/baseline to `Azath` and `Control Center`.
3. Confirm `com.amazon.kindle` remains untouched and zero crashes in `logcat`.
