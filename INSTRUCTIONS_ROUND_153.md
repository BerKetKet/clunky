# Round 153 Instructions — Azath v17.3 KFX (Limestone Light Mode Touch-Passthrough Bottom Navigation Bar Overlay)

## MANDATORY ANTIGRAVITY ROLE & FAST-EXECUTION PROTOCOL
- **Your Role**: Fast Executor & Data Collector ONLY.
- **Execute Verbatim & Fast**: Copy the 3 drop-in files as-is, run `rebuild_fast.sh`, deploy to **Samsung Galaxy M30**, capture ONLY the 2 requested screenshots, and zip the report immediately.
- **STOP-AND-REPORT Rule**: If ANY issue occurs, **DO NOT self-debug or invent workarounds**. Immediately capture the raw output/screenshot and return it to Gemini in `REPORT.md`.
- **Safety & Policy**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**. **DO NOT** launch, inspect, or reference Play Store Folio (`ct.reader`).

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Solution Implemented in Round 153 (`Azath v17.3 KFX`)
Resolves the dark Compose bottom bar in Limestone Light Mode cleanly with ZERO `Activity.recreate()` and ZERO `SIGSYS`:
- **`FolioTheme.StoneBottomBarView`**: A lightweight, 100% non-clickable touch-passthrough overlay mounted at `Gravity.BOTTOM` (`63dp` height) on `android.R.id.content` when Limestone Light Mode is active (and `GONE` in Granite Dark Mode).
  - Paints warm **Limestone `#EFEAE0`** with a `1dp` `#CBC3B3` top hairline, crisp **Home / Library / More** icons and **Inter** labels (`#2D3138` active, `#73757B` inactive), and a clean cutout (`26dp..76dp`) preserving the floating mini book cover on the left!
  - All taps pass straight through to the underlying Compose bottom bar natively.
- **`OpenCatalogView.java` & `FolioMoreView.java`**: Sync active tab index (`0` Home, `2` More) on build/attach, and bumped to **`Azath v17.3 KFX`**.

## Task A: Verbatim Drop-In Build, Deploy & Stage (FAST)
1. Copy `FolioTheme.java`, `OpenCatalogView.java`, and `FolioMoreView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Run `rebuild_fast.sh` (staging `azath_v17.3_kfx`) and deploy `com.azath.reader` to **Samsung Galaxy M30**.

## Task B: Quick Hardware Verification (Samsung Galaxy M30)
1. Capture `verify_r153_azath_home_limestone_complete.png`: Toggle `Granite -> Limestone` in Control Center (`v17.3 KFX`), tap **Home** tab (verifying taps pass straight through the Limestone bottom bar!), and screenshot Home showing the **warm Limestone `#EFEAE0` bottom navigation bar** seamlessly matching the rest of the screen.
2. Toggle back to **Granite** (`More` tab) and confirm `ZERO_CRASHES_AND_NO_SIGSYS` in `logcat` + untouched `com.amazon.kindle`.
