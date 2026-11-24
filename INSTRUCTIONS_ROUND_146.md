# Round 146 Instructions — Major Stone Edition Azath v16.0 KFX (Granite Dark & Limestone Light Themes, Limestone App & UI Icons, EB Garamond Library Header)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v16.0 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.  
**Strict Policy**: **DO NOT launch, inspect, or reference Play Store Folio (`ct.reader`) ever again.** Azath now stands completely on its own architectural Stone design system.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## User Directives Implemented in Round 146 (`Azath v16.0 KFX`)
1. **Granite (Dark Mode) & Limestone (Light Mode) Stone Palettes (`FolioTheme.java` + All 8 Java Modules)**:
   - **Granite (Dark)**: Deep honed granite canvas `#0E0F11`, modal `#15171A`, surface `#1C1E22`, control `#25282D`, seam `#32353B`, pale limestone ivory text `#ECE8E0` / `#BDB8AE` / `#8A857D`, luminous warm **Limestone** accent `#D4C9B8`, tonal granite-limestone pill `#282520` / `#524B41` / `#E6DDD0`.
   - **Limestone (Light)**: Warm quarried limestone canvas `#F4F0E8`, modal `#EFEAE0`, surface `#E7E1D5`, control `#DDD6C7`, seam `#CBC3B3`, deep granite ink `#1A1B1E` / `#4A4C52` / `#73757B`, deep honed **Granite** accent `#2D3138`, tonal limestone pill `#DFD8CA` / `#AFA695`.
   - Swept ALL 8 Java modules (`FolioTheme`, `FolioMoreView`, `OpenCatalogView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioWebBrowserDialog`, `FolioMetadataBackup`, `ExternalEpubActivity`) from crimson to the Stone palette, and updated `"FOLIO EPUB IMPORT"` / `"ALREADY IN FOLIO LIBRARY"` -> `"AZATH EPUB IMPORT"` / `"ALREADY IN AZATH LIBRARY"`.
2. **New Limestone & Granite Launcher Icon + Re-Harmonized Bespoke Limestone UI Icons**:
   - `dropin_res/mipmap-*/ic_launcher*.png` + `dropin_assets/azath_launcher/azath_icon_limestone_512.png` & `ic_launcher_foreground_432.png`: brand-new Limestone moonlit sky + honed Granite tree/reader launcher icon (zero crimson!).
   - `dropin_assets/azath_icons/*.png`: all 16 bespoke icons re-tinted to warm Limestone ivory (`#ECE8E0`) + pale Limestone accent (`#D4C9B8`) — also re-tint `ic_authors.png` in `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/` to `#ECE8E0`/`#D4C9B8`!
3. **Update DEX & Resource Patchers to Granite/Limestone**:
   - **`patch_classes2_azath.py`**: Update bottom navigation active tint from `0xffe05a47L` (crimson) to **`0xffd4c9b8L`** (luminous warm **Limestone**), and update XML color tokens (`#ffe05a47` -> `#ffd4c9b8`).
   - **`patch_res_azath_covers.py`**: Update Dark Granite generic Library cover tiles to `#1C1E22` fill, `#32353B` border, `#8A857D` open-book emblem, `#524B41` stone hairline, and `#ECE8E0` ivory text.
4. **Match `Library` Screen Heading Font to Regular EB Garamond**:
   - On the Library tab (`verify_r143_azath_library_dark_covers.png`), the top-left `Library` title still renders in chunky stock Kindle bold serif instead of airy Regular **EB Garamond** like `Azath` (Home) and `Control Center` (More).
   - **Action for Antigravity**: Inspect the `Library` TopAppBar title typography in `base_dec/res/font/` or Compose `NavigationControllerTopAppBar` / `LibraryScreenlet` (`classes2.dex`/`classes3.dex`) and patch the `Library` UI header font to **EB Garamond Regular** (without breaking Bookerly inside the book reader!), persisting the patch in `rebuild_fast.sh`.

## Task A: Integrate Stone Edition, Update Patchers, Build, Stage & Push to GitHub
1. Copy all 8 Java files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Copy `dropin_assets/azath_icons/*` into `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/` (and re-tint `ic_authors.png` to `#ECE8E0`/`#D4C9B8`), and install the new Limestone launcher icons from `dropin_res/mipmap-*/` (updating adaptive icon background color to `#141517`).
3. Update `patch_classes2_azath.py` (`0xffd4c9b8L`), `patch_res_azath_covers.py` (Granite `#1C1E22`/`#32353B`/`#524B41`), and patch the `Library` heading font to **EB Garamond Regular**.
4. Rebuild via `rebuild_fast.sh` (`azath_v16.0_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.0_kfx/base.apk` with SHA256, and push `Azath v16.0 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report146.zip`:
1. `verify_r146_azath_launcher_limestone.png`: App drawer showing the new **Limestone & Granite** `Azath` launcher icon.
2. `verify_r146_azath_home_granite.png`: **Azath Home** in the Granite + Limestone Stone Edition palette with `#D4C9B8` active `Home` tab.
3. `verify_r146_azath_library_garamond.png`: **Azath Library** confirming the `Library` heading matches **EB Garamond Regular** alongside dark Granite `#1C1E22` generic covers and `#D4C9B8` active `Library` tab.
4. `verify_r146_azath_control_center_granite.png`: **Azath Control Center** (`v16.0 KFX`) showing Granite surfaces, Limestone accents, and harmonized Limestone UI icons.
5. Confirm `com.amazon.kindle` remains untouched, zero crashes in `logcat`, and zero Play Store Folio (`ct.reader`) launches.
