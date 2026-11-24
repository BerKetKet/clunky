# Round 120 (Milestone) — Folio v12.0 KFX (Web Book Browser Bookmarks Bar & Live EPUB Download Progress)

## Summary of Changes (Drop-In Ready)
- **`FolioWebBrowserDialog.java`**:
  1. **Quick Bookmarks Bar**: Added a persistent horizontally scrollable bookmarks strip right below the top navigation bar (`+ ★` amber pin button to save the current page, 1-tap `★` chips to jump between catalogs, long-press any chip to remove; pre-seeded with `Standard Ebooks` & `Gutenberg`).
  2. **Live EPUB Download Progress & Referer Forwarding**: Streams `.epub` downloads with live KB/% progress updates (`⬇ Downloading book.epub (64% · 412 KB)...`) on the status banner and forwards the configured `Referer` header alongside WebView cookies & User-Agent.
- **`FolioMoreView.java`**:
  - Bumped Control Center header badge and `About Folio` dialog to **Milestone `Folio v12.0 KFX`** and added `• Simplified Web Book Browser & Auto-Ingest EPUB Pipeline` to the `About Folio` feature list.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.0_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r120_web_browser_bookmarks.png`**: Open `🌐 Web Book Browser` from the Control Center (`Folio v12.0 KFX`) and capture the browser modal showing the new Quick Bookmarks Bar (`+ ★`, `★ Standard Ebooks`, `★ Gutenberg`) right below the navigation bar.
2. **`verify_r120_about_v120.png`**: Open `About Folio` and capture `Version: Folio v12.0 KFX (Enhanced Typesetting)` with the new `Simplified Web Book Browser & Auto-Ingest EPUB Pipeline` line.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report120.zip`.
