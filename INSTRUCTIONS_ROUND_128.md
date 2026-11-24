# Round 128 — Folio v12.8 KFX (Web Browser Recent Downloads History Drawer)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioWebBrowserDialog.java`**:
  1. **Persistent `Downloads` History Chip & Drawer (`showRecentDownloadsDialog`)**: Added a Kindle-native `Downloads` chip (using `FolioIcons.IMPORT`) right beside `EPUBs` in the Web Book Browser bookmarks bar. Every EPUB automatically downloaded and converted to KFX is recorded in `SharedPreferences("folio_ui_prefs", "web_recent_imports")` (up to 15 recent entries) with title, author, timestamp, and 1-tap `Open ›` reader launch + `Clear History` action, styled with `FolioTheme.applyToDialog`.
- **`FolioMoreView.java` & `FolioMetadataCatalogDialog.java`**:
  - Synced Round 127 cleanups (`Edit` shelf pill, `🕒 ` removal, `+Queue` bulk pill) and bumped Control Center badge and `About Folio` dialog to `Folio v12.8 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy all 3 files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.8_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r128_web_browser_downloads_bar.png`**: Open **Web Book Browser** (`Folio v12.8 KFX`) and capture the monochrome bookmarks bar showing `Pin`, `EPUBs`, and the new **`Downloads`** chip.
2. **`verify_r128_web_downloads_drawer.png`**: Tap **`Downloads`** to open the Kindle-native `Web Downloads` drawer (Bookerly header, Amazon Ember body, styled footer buttons) and capture it.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report128.zip`.
