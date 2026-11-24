# Round 120 Deliverables Report — Milestone Folio v12.0 KFX (Web Book Browser Bookmarks Bar & Live EPUB Download Progress)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Milestone Folio v12.0 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 04:17:54`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Milestone Release Staging
1. **Drop-In Source Integration**:
   - **`FolioWebBrowserDialog.java`** (`com.amazon.kindle.epub.ingest.FolioWebBrowserDialog`):
     - **Quick Bookmarks Bar**: Added a persistent, horizontally scrollable bookmark chip bar right beneath the browser navigation bar:
       * **`+ ★` Amber Pin Button**: Pins the active webpage title and URL as a bookmark with 1 tap.
       * **1-Tap `★` Bookmark Chips**: Pre-seeded with `★ Standard Ebooks` (`standardebooks.org/ebooks`) and `★ Gutenberg` (`m.gutenberg.org`). Tapping any chip immediately navigates the WebView.
       * **Long-Press Deletion**: Long-pressing custom bookmark chips triggers deletion with instant list refresh.
     - **Live EPUB Download Progress & Referer Forwarding**:
       * Streaming EPUB downloader now posts dynamic progress updates (`⬇ Downloading <file>.epub (X% · Y KB)...`) to the status banner.
       * Forwards configured `Referer` headers along with session cookies and User-Agent to ensure compatibility with CDNs.
   - **`FolioMoreView.java`**:
     - Bumped Control Center badge and `About Folio` dialog string to **Milestone `Folio v12.0 KFX (Enhanced Typesetting)`**.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (434,840 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 04:17:54`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.0_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r120_web_browser_bookmarks.png` (198,509 bytes)**:
   - Captured **🌐 Web Book Browser** modal opened from Control Center (`Folio v12.0 KFX`).
   - Shows compact navigation bar (`←`, `↻`, `🏠`, `standardebooks.org/ebooks`, `✨`, `⚙`, `✕`).
   - Shows new Quick Bookmarks Bar:
     * `+ ★` amber pin action
     * `★ Standard Ebooks`
     * `★ Gutenberg`
   - Shows active `WebView` container displaying the Standard Ebooks catalog.
2. **`verify_r120_about_v120.png` (355,086 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v12.0 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Indexed Library Books: 34 · Enriched Metadata Records: 32`
     * `Reading Progress: 5 in progress · 1 finished`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * `Privacy Mode: Local-First (Zero Amazon Telemetry)`
3. **Crash & Invariant Log Check**:
   - Zero crashes logged for `com.amazon.kindle.epub`.
   - Stock Kindle (`com.amazon.kindle` v8.160.1.0) verified **100% untouched** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Package Verification Artifacts
- Archive: `antig_to_gemini_neon_clunky_report120.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_120.md`
  * `protocol.json`
  * `verify_r120_web_browser_bookmarks.png`
  * `verify_r120_about_v120.png`
