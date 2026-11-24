# Round 133 Deliverables Report — Folio v13.3 KFX (Kindle-Native Reading Log Modal & Editorial Offline HTML Export)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v13.3 KFX` installed on device at `2026-11-24 23:41:43`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioMetadataBackup.java` (`exportLibraryHtml`)**:
     * Redesigned exported `/sdcard/kindle/backups/folio_library.html` CSS to **Kindle-Native Editorial Dark Theme** (`#000000` canvas, `#141618` cards, `1px #262A2E` hairlines, Georgia serif display headings & book titles, `#4DA6FF` Kindle blue progress bar & badges, and `#162330`/`#2C5278` note callout — zero neon colors).
   - **`FolioMoreView.java`**:
     * Wired `FolioTheme.applyToDialog(ctx, logDlg)` post-`.show()` onto **Reading Log & Export** (`showReadingLogDialog`).
     * Synced `rtHdr` click-to-calibrate listener using a single shared `calibrateClick` listener across `rtHdr` and `rtRow`, while preserving the `Reading Time (WPM)` snapshot line inside `snapshotText`.
     * Bumped Control Center badge and `About Folio` dialog to **`Folio v13.3 KFX`** (`Folio v13.3 KFX (Enhanced Typesetting)`).

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (`458,484` bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 23:41:43`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v13.3_kfx/base.apk` (`137,740,774` bytes ~132 MB, SHA256: `b46ca94d9cd3e58bf3d846da0fa5091bc0f89f5a77888fcd99c638d19fedb144`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r133_reading_log_kindle.png` (234,189 bytes)**:
   - Opened **Reading Log & Export** (`Reading Log (.md)`), tapped **HTML**, confirmed toast `"Exported offline HTML catalog (folio_library.html)"`, updated path text `Saved HTML: /sdcard/kindle/backups/folio_library.html`, and styled Kindle footer buttons (`CLOSE`, `COPY MARKDOWN`, `SHARE`).
   - Verified regenerated `/sdcard/kindle/backups/folio_library.html` on device (`8,778` bytes vs previous `7,486` bytes, timestamp `2026-11-24 23:43`) and pulled HTML confirming `#000000` canvas, Georgia serif titles, and `#4DA6FF` Kindle blue accents.
2. **`verify_r133_about_v133.png` (338,933 bytes)**:
   - Captured **About Folio** modal showing `Version: Folio v13.3 KFX (Enhanced Typesetting)`, `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`, and styled Kindle action buttons (`CLOSE`, `CLEAR TEMP CACHE`).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.
