# Round 119 — Folio v11.9 KFX (Simplified Web Book Browser & Auto-Ingest EPUB Pipeline)

## Summary of Changes (Drop-In Ready)
- **NEW `FolioWebBrowserDialog.java`** (`com.amazon.kindle.epub.ingest.FolioWebBrowserDialog`):
  1. **Simplified Web Book Browser (`show(ctx, onImported)`)**: Full-height dark modal `WebView` with compact navigation bar (`←`, `↻`, `🏠`, clickable host/path pill, `✨` Simplified View CSS toggle, `⚙` URL Settings, `✕` Close) and linear load progress bar.
  2. **Configurable Homepage URL & Simplified View Settings (`showUrlSettingsDialog`)**: Stored in `SharedPreferences("folio_ui_prefs", "web_browser_home_url")` (defaults to `https://standardebooks.org/ebooks`) with 1-tap presets (`Standard Ebooks`, `Project Gutenberg`, `Open Library`, `Calibre LAN`) and a `✨ Simplified View` toggle (`web_browser_clean_mode`, ON by default) that strips clutter/cookie overlays.
  3. **Automatic EPUB Download -> KFX Conversion -> Library Ingestion**: Intercepts `.epub` navigations and `DownloadListener` events (preserving active WebView cookies & User-Agent across redirects), validates the downloaded ZIP/EPUB header, converts via `ExternalEpubActivity.convertEpubToKfx`, enriches via `MetadataEnricher.enrichInPlace`, saves to `FolioMetadataDb` + `kindle_library.db`, and displays a live green banner `✓ Added "<Title>" to Library` with a 1-tap **`Open ›`** button.
- **`FolioMoreView.java`**:
  1. Added **`🌐 Web Book Browser`** card right beneath `📥 Import EPUBs from Storage`.
  2. Added **`🔗 Web Browser URL`** settings card right beneath `⚙️ Reader & App Settings`.
  3. Bumped Control Center badge and `About Folio` dialog to `Folio v11.9 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.9_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r119_web_browser.png`**: Open `🌐 Web Book Browser` from the Control Center (`Folio v11.9 KFX`), let the configured catalog page (`standardebooks.org/ebooks`) load in Simplified View, and capture the browser modal (plus optionally tap a public-domain `.epub` download link to verify the live auto-ingest banner).
2. **`verify_r119_web_browser_settings.png`**: Tap `⚙` (or `🔗 Web Browser URL` on the Control Center) and capture the `🌐 Web Browser Settings` modal showing the configurable Homepage URL field, preset chips, and `✨ Simplified View` toggle.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report119.zip`.
