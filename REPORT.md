# Round 119 Deliverables Report — Folio v11.9 KFX (Simplified Web Book Browser & Auto-Ingest EPUB Pipeline)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.9 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 04:10:44`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioWebBrowserDialog.java`** (`com.amazon.kindle.epub.ingest.FolioWebBrowserDialog`):
     - **Simplified Web Book Browser (`show(ctx, onImported)`)**: Implemented a dark modal `WebView` with compact navigation bar (`←`, `↻`, `🏠`, clickable host/path pill, `✨` Simplified View CSS toggle, `⚙` URL Settings, `✕` Close) and linear load progress bar.
     - **Configurable Homepage URL & Simplified View Settings (`showUrlSettingsDialog`)**: Persisted in `SharedPreferences("folio_ui_prefs", "web_browser_home_url")` (defaults to `https://standardebooks.org/ebooks`) with 1-tap presets (`Standard Ebooks`, `Project Gutenberg`, `Open Library`, `Calibre LAN`) and a `✨ Simplified View` toggle (`web_browser_clean_mode`, ON by default) that injects CSS stripping clutter, sticky headers, and cookie banners.
     - **Automatic EPUB Download -> KFX Conversion -> Library Ingestion**: Intercepts `.epub` navigations and `DownloadListener` events (forwarding active WebView session cookies & User-Agent across redirects), validates downloaded ZIP/EPUB headers, converts to native KFX via `ExternalEpubActivity.convertEpubToKfx`, enriches metadata via `MetadataEnricher.enrichInPlace`, indexes into `FolioMetadataDb` + `kindle_library.db`, and presents an inline green success banner `✓ Added "<Title>" to Library` with a 1-tap **`Open ›`** button.
   - **`FolioMoreView.java`**:
     - Added **`🌐 Web Book Browser`** card right beneath `📥 Import EPUBs from Storage`.
     - Added **`🔗 Web Browser URL`** settings card right beneath `⚙️ Reader & App Settings`.
     - Bumped Control Center badge and `About Folio` dialog string to `Folio v11.9 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (430,344 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 04:10:44`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.9_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r119_web_browser.png` (194,690 bytes)**:
   - Captured **🌐 Web Book Browser** modal opened from Control Center.
   - Shows compact navigation bar: `←`, `↻`, `🏠`, address pill `standardebooks.org/ebooks`, `✨` (clean view enabled), `⚙`, and `✕`.
   - Shows active `WebView` container displaying the Standard Ebooks catalog.
2. **`verify_r119_web_browser_settings.png` (173,369 bytes)**:
   - Captured **🌐 Web Browser Settings** modal opened from Control Center / browser toolbar.
   - Shows configurable Homepage URL input field (`https://standardebooks.org/ebooks`).
   - Shows 1-tap quick preset chips: `Standard Ebooks`, `Project Gutenberg`, `Open Library`, `Calibre LAN`.
   - Shows `✨ Simplified View: ON  (hides clutter & popups)` toggle and `Save` / `Cancel` buttons.
3. **Crash & Invariant Log Check**:
   - Zero crashes logged for `com.amazon.kindle.epub`.
   - Stock Kindle (`com.amazon.kindle` v8.160.1.0) verified **100% untouched** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Package Verification Artifacts
- Archive: `antig_to_gemini_neon_clunky_report119.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_119.md`
  * `protocol.json`
  * `verify_r119_web_browser.png`
  * `verify_r119_web_browser_settings.png`
