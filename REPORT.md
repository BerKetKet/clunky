# Round 131 Deliverables Report — Folio v13.1 KFX (Library Insights Reading Time Portfolio & Kindle Theme)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v13.1 KFX` installed on device at `2026-11-24 23:21:14`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioMoreView.java` (`showLibraryInsightsDialog`)**:
     * Added **`READING TIME PORTFOLIO (250 WPM)`** 3-tile dashboard at the top of **Library Insights** showing **`TOTAL LIBRARY`** (`651h`), **`TIME READ`** (`14.9h`, weighted across finished and in-progress books in Kindle blue-slate `#162330` / `#2C5278` / `#4DA6FF`), and **`REMAINING`** (`636h`), scaled dynamically by `getReadingSpeedWpm(ctx)`.
     * Repaired drop-in `FolioTheme.applyToDialog` placement: moved post-`.show()` for `insightDlgRef[0]`, `goalDlgRef[0]`, `dlgRef[0]`, `storDlgRef[0]`, and `aboutDlg` (`About Folio`) so Bookerly headings, Amazon Ember controls, and styled `#4DA6FF` / `#C4CBD2` / `#8E959C` footer buttons render immediately on dialog creation.
     * Bumped Control Center badge and `About Folio` dialog version to **`Folio v13.1 KFX`** (`Folio v13.1 KFX (Enhanced Typesetting)`).

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (457,600 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 23:21:14`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v13.1_kfx/base.apk` (137,740,774 bytes ~132 MB, SHA256: `52745681d89e0a22f71306c335da927778de3146eb635d2f350de80c5bef7108`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r131_insights_reading_time.png` (174,011 bytes)**:
   - Captured **Library Insights** modal showing:
     * Bookerly serif heading (`Library Insights`).
     * `READING TIME PORTFOLIO (250 WPM)` 3-tile strip (`651h TOTAL LIBRARY`, `14.9h TIME READ` highlighted in Kindle blue-slate `#162330`/`#2C5278`/`#4DA6FF`, `636h REMAINING`).
     * Reading status stacked bar (`Finished (1)`, `Reading (5)`, `Unread (28)`), metadata health (`72% COMPLETE`), top genres, and styled Kindle footer buttons (`CLOSE`, `COPY SUMMARY`, `SHARE`).
2. **`verify_r131_about_v131.png` (338,914 bytes)**:
   - Captured **About Folio** modal showing:
     * Bookerly/Ember typography.
     * Version line: `Version: Folio v13.1 KFX (Enhanced Typesetting)`.
     * Hardware info: `TrebleDroid with GApps · Android 13 (arm64-v8a)`.
     * Styled footer buttons (`CLOSE`, `CLEAR TEMP CACHE`).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.
