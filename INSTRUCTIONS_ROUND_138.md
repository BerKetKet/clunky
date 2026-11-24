# Round 138 Instructions — Azath v14.4 KFX (EB Garamond + Inter UI Typography, Home Screen UI Unification & Dark Offline Browser Error Page)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v14.4 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## User Directives Addressed in Round 138
1. **EB Garamond (Serif) + Inter (Sans) UI Typography (`FolioTheme.java` + `assets/azath_fonts/`)**:
   - `dropin_assets/azath_fonts/` already includes **`Inter-Regular.otf`** and **`Inter-SemiBold.otf`**.
   - **Action for Antigravity**: Copy `dropin_assets/azath_fonts/*` to `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` AND place **`EBGaramond-Regular.ttf`** and **`EBGaramond-SemiBold.ttf`** alongside them in `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` (fetch from Google Fonts GitHub `https://github.com/google/fonts/raw/main/ofl/ebgaramond/static/EBGaramond-Regular.ttf` and `EBGaramond-SemiBold.ttf`, or copy from local system `/usr/share/fonts/`).
   - Wire `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` -> `/tmp/kindle_clone_build/base_dec/assets/azath_fonts/` in `rebuild_fast.sh`.
   - `FolioTheme.java` now prioritizes `assets/azath_fonts/` (**EB Garamond Regular** for display headings `>= 17sp`, **Inter Regular/SemiBold** for UI controls `< 17sp`) before falling back to Bookerly/Ember.
2. **Complete Home Screen UI Unification (`OpenCatalogView.java`)**:
   - Replaced all legacy slate/amber/cyan colors (`#101216`, `#1B1F27`, `#232833`, `#E6A23C`, `#38BDF8`) with Azath's unified Obsidian-Ivory-Crimson palette (`#0A0909`, `#181515`, `#211C1B`, `#2A2423`, `#F2EFE9`, `#8E867E`, `#E05A47`, `#2A1816`, `#5C2D26`, `#F5B8AE`).
   - Updated top brand header to **`Azath`** in `30sp` Regular EB Garamond (`-0.02f` tracking) with tonal `#2A1816`/`#5C2D26` `Backup` pill, rounded `#181515` search bar with `#E05A47` `Search` CTA, `20sp` Regular EB Garamond section headings, and `FolioTheme.applyToTree` / `FolioTheme.applyToDialog` across the Home view and all 3 Home dialogs (OPDS, Book Details, Backup).
3. **Dark Offline WebView Error Page (`FolioWebBrowserDialog.java`)**:
   - Added `onReceivedError` injecting an Azath Obsidian-Ivory-Crimson offline fallback card (`Page unavailable offline` + crimson `Try Again` button) instead of Chromium's unreadable black-on-dark error screen.
4. **Version Bump (`FolioMoreView.java`)**:
   - Bumped to **`Azath v14.4 KFX`** and preserved the `makeMiniCoverView(..., String titleHint, ...)` overload.

## Task A: Bundle Fonts, Integrate, Build, Deploy & GitHub Sync
1. Copy all 4 drop-in Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` (keeping `FolioIcons.java` and `ic_authors.png` from Round 137 intact).
2. Populate `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` with `Inter-Regular.otf`, `Inter-SemiBold.otf`, `EBGaramond-Regular.ttf`, and `EBGaramond-SemiBold.ttf`, and sync them in `rebuild_fast.sh`.
3. Rebuild `classes9.dex` + APK (with `patch_classes2_azath.py` + XML bottom nav patches), deploy `com.azath.reader` (`Azath v14.4 KFX`) to the **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.4_kfx/base.apk` with SHA256, and push to GitHub (`origin master`).

## Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report138.zip`:
1. `verify_r138_azath_home.png`: **Azath Home tab** showing the redesigned Obsidian-Ivory-Crimson Home UI, **`Azath`** in Regular EB Garamond, Inter search bar & chips, EB Garamond section headings, and `#E05A47` active `Home` tab in the bottom nav.
2. `verify_r138_azath_control_center.png`: **Azath Control Center** showing `v14.4 KFX` with **EB Garamond** display headings (`Control Center`, stat numbers) paired with **Inter** UI controls.
3. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
