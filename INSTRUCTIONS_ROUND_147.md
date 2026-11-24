# Round 147 Instructions — Azath v16.1 KFX (Adaptive Launcher Icon Safe-Zone Framing + Live Granite/Limestone Theme Switcher & GitHub Sync)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v16.1 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.  
**Strict Policy**: **DO NOT launch, inspect, or reference Play Store Folio (`ct.reader`).**

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 147 (`Azath v16.1 KFX`)
1. **Adaptive Launcher Icon Safe-Zone Framing (`dropin_assets/azath_launcher/ic_launcher_foreground_432.png` + `dropin_res/mipmap-*/`)**:
   - In `verify_r146_azath_launcher_limestone.png`, Android's circular adaptive icon mask cropped the bottom-left reader silhouette under the tree.
   - Re-framed `ic_launcher_foreground_432.png` inside the 69% adaptive safe zone with feathered limestone/granite edge bleed so the **entire tree, crescent moon, and reader silhouette** sit cleanly inside the circular app drawer icon!
2. **Live Granite (Dark) / Limestone (Light) Theme Wiring & 1-Tap Switcher (`OpenCatalogView.java`, `FolioMoreView.java`)**:
   - Wired `OpenCatalogView.build(ctx)` and `FolioMoreView.build(ctx)` to read dynamic `FolioTheme` getters (`bgCanvas`, `bgSurface`, `bgControl`, `border`, `textPrimary`, `textMuted`, `accent`, `accentFill`, `accentBorder`, `accentSoftText`).
   - Added a sleek **`Granite` / `Limestone`** stone-theme toggle pill in the Control Center subtitle row so users can toggle between Granite (Dark) and Limestone (Light) live!
   - Bumped to **`Azath v16.1 KFX`**.

## Task A: Update Launcher Foreground, Build, Deploy, Stage & Push to GitHub
1. Copy `OpenCatalogView.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Replace the launcher assets with the safe-zone-framed `dropin_assets/azath_launcher/ic_launcher_foreground_432.png` and `dropin_res/mipmap-*/`.
3. Rebuild via `rebuild_fast.sh` (`azath_v16.1_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.1_kfx/base.apk` with SHA256, and push **`Azath v16.1 KFX`** (including all Round 146–147 Stone Edition changes) to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report147.zip`:
1. `verify_r147_azath_launcher_safezone.png`: Android app drawer confirming the full Limestone moonlit sky + tree + **reader silhouette** now sit cleanly inside the circular `Azath` icon!
2. `verify_r147_azath_home_limestone_light.png`: Tap the new `Granite` -> `Limestone` pill in Control Center, then switch to **Home** to verify the warm **Limestone (Light Mode)** theme (`#F4F0E8` canvas, `#E7E1D5` cards, `#1A1B1E` deep granite ink, EB Garamond `Azath`) live on hardware! (Then toggle back to `Granite` dark mode after capture so the device returns to dark mode.)
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
