# Round 121 Deliverables Report — Folio v12.1 KFX (Web Browser Go-to-URL Bar & 1-Tap EPUB Page Scanner)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v12.1 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 04:26:04`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioWebBrowserDialog.java`** (`com.amazon.kindle.epub.ingest.FolioWebBrowserDialog`):
     - **1-Tap `⬇ EPUBs` Page Scanner (`scanPageForEpubLinks`)**: Added an emerald `⬇ EPUBs` pill in the Quick Bookmarks Bar directly beside `+ ★`. When tapped, it queries the active WebView DOM for all `.epub` / `/epub` links and displays them in a clean dark picker sheet (`📥 EPUB Links Found (N)`), enabling 1-tap download, native KFX conversion, metadata enrichment, and library addition.
     - **Quick `🔗 Go to URL` Navigation Modal (`showGoToUrlDialog`)**: Tapping the address pill in the top bar now opens a dedicated `Go to URL` dialog (`Go`, `Set Home`, `Cancel`) for fast one-off navigation without altering the default homepage setting (`⚙` remains the dedicated Web Browser Settings modal).
   - **`FolioMoreView.java`**:
     - Bumped Control Center badge and `About Folio` dialog string to **`Folio v12.1 KFX (Enhanced Typesetting)`**.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (440,480 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 04:26:04`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.1_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r121_web_browser_epub_scanner.png` (204,724 bytes)**:
   - Captured **🌐 Web Book Browser** modal opened from Control Center (`Folio v12.1 KFX`).
   - Shows compact navigation bar (`←`, `↻`, `🏠`, `standardebooks.org/ebooks`, `✨`, `⚙`, `✕`).
   - Shows upgraded Quick Bookmarks Bar:
     * `+ ★` amber pin action
     * `⬇ EPUBs` emerald page scanner pill
     * `★ Standard Ebooks`
     * `★ Gutenberg`
   - Shows active `WebView` container displaying the Standard Ebooks catalog.
2. **`verify_r121_about_v121.png` (370,656 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v12.1 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `• Simplified Web Book Browser & Auto-Ingest EPUB Pipeline`
     * `Indexed Library Books: 34 · Enriched Metadata Records: 32`
     * `Reading Progress: 5 in progress · 1 finished`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * `Privacy Mode: Local-First (Zero Amazon Telemetry)`
3. **Crash & Invariant Log Check**:
   - Zero crashes logged for `com.amazon.kindle.epub`.
   - Stock Kindle (`com.amazon.kindle` v8.160.1.0) verified **100% untouched** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Package Verification Artifacts
- Archive: `antig_to_gemini_neon_clunky_report121.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_121.md`
  * `protocol.json`
  * `verify_r121_web_browser_epub_scanner.png`
  * `verify_r121_about_v121.png`
