# Round 148 Instructions — Azath v16.2 KFX (Complete Limestone Light Mode Parity Across Home, Control Center & Icons + Instant Theme Recreate)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v16.2 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.  
**Strict Policy**: **DO NOT launch, inspect, or reference Play Store Folio (`ct.reader`).**

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Fixes Implemented in Round 148 (`Azath v16.2 KFX`)
Resolves all 3 honest hardware findings from Report 147:
1. **Home Screen Limestone Light Mode Parity (`OpenCatalogView.java`)**:
   - Genre chips (`Sci-Fi`, `Fantasy`, `Mystery`, `Horror`, `Classic`) now use `FolioTheme.bgSurface(ctx)` (`#E7E1D5`) + `FolioTheme.textPrimary(ctx)` (`#1A1B1E`).
   - `Continue Reading` & remote carousel book titles, authors, and fallback archival covers now use `FolioTheme.textPrimary(ctx)` (`#1A1B1E` deep granite ink on `#F4F0E8` warm limestone — zero invisible light text!).
2. **Control Center Full Limestone Light Mode & Dark-Ink Icons (`FolioMoreView.java` & `FolioIcons.java`)**:
   - Dynamicized ALL Control Center inner cards (`makeStatTile`, `2026 Reading Goal`, `Continue Reading`, `Up Next Queue`, `LIBRARY & IMPORT` grouped rows, `v16.2 KFX` badge, Compass/Search buttons, and section headers) to read `FolioTheme.bgSurface(ctx)`, `FolioTheme.bgControl(ctx)`, `FolioTheme.border(ctx)`, `FolioTheme.textPrimary(ctx)`, `FolioTheme.textMuted(ctx)`, and `FolioTheme.accent*(ctx)`.
   - `FolioIcons.java` now permanently includes `AUTHORS = 28` (`azath_icons/ic_authors.png`) and automatically tints bespoke ivory PNG icons to `#1A1B1E` deep granite ink (`PorterDuff.Mode.SRC_IN`) when `FolioTheme.isLimestoneLight(ctx)` is active!
3. **Instant In-App Theme Switching (`FolioMoreView.java`)**:
   - Tapping the `Granite` / `Limestone` pill now invokes `((Activity) ctx).recreate()` directly so BOTH Home and Control Center refresh immediately without force-stopping!

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy `FolioIcons.java`, `OpenCatalogView.java`, and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Rebuild via `rebuild_fast.sh` (`azath_v16.2_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.2_kfx/base.apk` with SHA256, and push **`Azath v16.2 KFX`** to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report148.zip`:
1. `verify_r148_azath_control_center_limestone.png`: Tap `Granite` -> `Limestone` pill in Control Center (verifying instant `Activity.recreate()` with ZERO force-stop!) and capture Control Center showing ALL inner cards in warm carved Limestone `#E7E1D5` with deep granite `#1A1B1E` ink and dark-tinted bespoke icons!
2. `verify_r148_azath_home_limestone.png`: Switch directly to **Home** tab (no force-stop!) confirming warm `#E7E1D5` genre chips and crisp `#1A1B1E` deep granite book titles under `Continue Reading`! (Then toggle back to `Granite` dark mode after capture.)
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
