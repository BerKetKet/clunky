# Round 136 Instructions — Azath v14.2 KFX (Bottom Nav Ember-Crimson Alignment, Archival Monogram Covers & Tonal Catalog Polish)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.2 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Changes in Round 136
1. **Archival Spine Monogram Fallback Covers (`FolioMetadataCatalogDialog.java`)**:
   - Replaced flat gray empty cover rectangles with refined 56x84dp rounded `#211C1B` archival cards featuring a Regular Bookerly title-initial monogram (`B`, `I`, etc.) and a subtle `#5C2D26` crimson spine rule at the bottom.
2. **Tonal Editorial Filter Chips & Genre Hierarchy (`FolioMetadataCatalogDialog.java`)**:
   - Unified active filter pills (`All (24)`, `Reading`, `Unread`) to tonal `#2A1816` fill + `#5C2D26` border + `#F5B8AE` text (matching Control Center pills and Play Store Folio's calm restraint), while keeping `Sync & Enrich` as the single primary `#E05A47` CTA.
   - Softened card genre tags to warm muted `#8E867E` and book titles to warm ivory `#F2EFE9`, reserving `#E05A47` ember-crimson strictly for progress indicators and interactive actions.
3. **Bottom Navigation Bar Active Tint (`Home` / `Library` / `More`) — APK Resource Patch**:
   - In `verify_r135_azath_control_center.png`, the bottom navigation bar's active tab (`More`) still uses stock Kindle sky-blue (`#3880E8` / `#4DA6FF` / `bottom_nav_selected` in `base_dec/res/values/colors.xml` or `base_dec/res/color/`).
   - Inspect `base_dec/res/color/` and `base_dec/res/values-night/colors.xml` / `base_dec/res/values/colors.xml` for the bottom navigation selected icon/text color token(s) and patch the selected state to Azath Ember Crimson `#E05A47` (and persist the resource patch in `rebuild_fast.sh` so future builds keep it!).
4. **Version Bump**: `Azath v14.2 KFX` in `FolioMoreView.java`.

## Task A: Integrate, Patch Bottom Nav Tint, Build & Stage
1. Copy both drop-in Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Patch the bottom navigation bar active color token in `base_dec/res/` to `#E05A47` (and persist in `rebuild_fast.sh`).
3. Rebuild `classes9.dex` & APK, align, sign, deploy to `com.azath.reader` on the **Samsung Galaxy M30**, and stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.2_kfx/base.apk` with SHA256.
4. Commit and push `Azath v14.2 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report136.zip`:
1. `verify_r136_azath_control_center.png`: Control Center showing `v14.2 KFX` and the bottom navigation `More` tab tinted in Azath Ember Crimson `#E05A47`.
2. `verify_r136_azath_catalog_polish.png`: `Library Metadata & Genres` modal showing tonal `#2A1816`/`#5C2D26` active filter pill, archival Bookerly monogram covers on books without cover art, and muted warm genre lines.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
