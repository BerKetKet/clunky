# Round 121 — Folio v12.1 KFX (Web Browser Go-to-URL Bar & 1-Tap EPUB Page Scanner)

## Summary of Changes (Drop-In Ready)
- **`FolioWebBrowserDialog.java`**:
  1. **1-Tap `⬇ EPUBs` Page Scanner (`scanPageForEpubLinks`)**: Added an emerald `⬇ EPUBs` pill right beside `+ ★` in the Quick Bookmarks Bar. Tapping it scans the active webpage DOM for `.epub` / `/epub` download links and displays them in a clean dark picker sheet (`📥 EPUB Links Found (N)`) so you can download, convert to KFX, and add any EPUB on the page to your library in 1 tap.
  2. **Quick `🔗 Go to URL` Navigation Modal (`showGoToUrlDialog`)**: Tapping the address pill in the top bar now opens a dedicated `Go to URL` dialog (`Go` / `Set Home` / `Cancel`) for fast one-off navigation without overwriting your saved homepage (`⚙` remains the full Web Browser Settings modal).
- **`FolioMoreView.java`**:
  - Bumped Control Center header badge and `About Folio` dialog to `Folio v12.1 KFX` and added `• Simplified Web Book Browser & Auto-Ingest EPUB Pipeline` to `About Folio`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.1_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r121_web_browser_epub_scanner.png`**: Open `🌐 Web Book Browser` (`Folio v12.1 KFX`) showing the new emerald **`⬇ EPUBs`** pill in the bookmarks bar right beside `+ ★` (bonus: tap into any Standard Ebooks book page and tap `⬇ EPUBs` to show the `📥 EPUB Links Found` sheet!).
2. **`verify_r121_about_v121.png`**: Open `About Folio` and capture `Version: Folio v12.1 KFX (Enhanced Typesetting)` with the new `Simplified Web Book Browser & Auto-Ingest EPUB Pipeline` line.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report121.zip`.
