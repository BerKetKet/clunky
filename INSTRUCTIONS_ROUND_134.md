# Round 134 (Major Milestone) — Azath v14.0 KFX (Malazan Rebrand, Bespoke Generated Icon Set & Play Store Folio Reference Study)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).
- **Stock Kindle Invariant**: `com.amazon.kindle` (v8.160.1.0) MUST remain **100% UNTOUCHED and READ-ONLY**.

## Summary of Changes (Drop-In Ready)
1. **Malazan Rebrand -> `Azath` (`com.azath.reader`)**:
   - Renamed visible app branding across all 6 Java UI/backup modules to **Azath** (`Azath v14.0 KFX`, `About Azath`, `AZATH`, `Azath Library`, `Azath Offline Library Catalog`) while keeping internal class paths, SharedPreferences, and DB filenames compatible.
2. **Bespoke Generated Editorial Icon Set (`dropin_assets/azath_icons/` — 16 Transparent 96x96 PNGs)**:
   - Handcrafted fine-line ivory (`#EFECE6`) + subtle crimson-ember (`#D9483B`) icons matching the crimson boy-under-tree app icon (`ic_library`, `ic_import`, `ic_globe`, `ic_feed`, `ic_refresh`, `ic_notes`, `ic_shelves`, `ic_insights`, `ic_log`, `ic_backup`, `ic_storage`, `ic_link`, `ic_settings`, `ic_info`, `ic_compass`, `ic_search`).
   - Upgraded `FolioIcons.java` to load and cache these PNGs from `assets/azath_icons/` automatically (with vector fallback for UI arrows/chevrons) and sized Control Center row icons to `24dp`.

## Task A: Rebrand Package Name, Wire Bespoke Icons, Build, Deploy & Stage
1. Copy all 6 Java files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Copy all 16 PNG icons from `dropin_assets/azath_icons/` into the decoded APK's `assets/azath_icons/`.
3. **App & Package Rename (`com.amazon.kindle.epub` -> `com.azath.reader`)**:
   - In the decoded APK's `AndroidManifest.xml` (and any `provider` `android:authorities` / `permission` / `queries` referencing `com.amazon.kindle.epub`), update `com.amazon.kindle.epub` -> `com.azath.reader` so the APK installs cleanly as `com.azath.reader` without colliding with `com.amazon.kindle` or `com.amazon.kindle.epub`.
   - Update the launcher label (`android:label` / `res/values*/strings.xml` app name) from `Folio` -> **`Azath`**. (Also update `Search Folio` hint string in `strings.xml` to `Search Azath` if present in resources!)
   - *(Note: If renaming `package=` in the cloned Kindle manifest triggers any internal Kindle resource/authority crash at launch, keep `android:label="Azath"` and document the exact error in `REPORT.md` so we can patch the specific authority cleanly in Round 135 — otherwise deploy `com.azath.reader` directly!)*
4. Recompile `classes9.dex`, rebuild APK with `apktool b`, align via `zipalign`, sign with `apksigner`, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
5. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/azath_v14.0_kfx/base.apk`.

## Task B: Hardware Verification & Play Store Folio Reference Capture (4 Screenshots)
1. **`verify_r134_azath_control_center.png`**: Open **Azath** -> `More` tab (Control Center) and capture the new bespoke ivory+crimson generated icons (`ic_library`, `ic_import`, `ic_globe`, `ic_compass`, `ic_search`, etc.) and `v14.0 KFX` badge.
2. **`verify_r134_azath_launcher.png`**: Capture the Galaxy M30 home screen / app drawer showing the **Azath** crimson launcher icon and label.
3. **`ref_playstore_folio_1.png`** & **`ref_playstore_folio_2.png`**: Launch the Play Store **Folio** reader app installed on the Galaxy M30 and capture 2 clean screenshots (e.g., its main library/home screen and a settings/details/menu screen) so Gemini can study its exact serif + sans font pairing and spacing rhythm for Round 135's typography unification pass!

Package `REPORT.md` + all 4 screenshots into `antig_to_gemini_neon_clunky_report134.zip`.
