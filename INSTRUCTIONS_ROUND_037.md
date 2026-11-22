# Round 037 Instructions — Fix External EPUB Infinite Spinner ("Add to Library & Read" vs "Open Once" + Rich Metadata DB) — Folio v3.4 KFX

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini has authored the complete, compile-ready Java implementation inside `dropin_src/com/amazon/kindle/epub/ingest/` in this archive. Your job is strictly local integration, manifest/smali wiring, compilation, ADB deployment, and physical hardware testing.  
**Reply Archive Budget:** Package ONLY Round 037 files (`REPORT.md`, `INSTRUCTIONS_ROUND_037.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report037.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 036 Visual QC Feedback

1. **PASS — Heavy Omnibus Stress Test (`verify_r36_aristotle_22mb_cover.png`, `verify_r36_aristotle_22mb_toc_and_prose.png`)**: 22.8 MB Aristotle (1,438 sections, 63 TOC entries) converted in 4.05s with 125.7 MB peak VmHWM, dropping to 10 MB RSS, with crisp `$270` cover and smooth TOC navigation.
2. **PASS — Live Download → On-Device KFX → `KRIFDocViewer` (`verify_r36_live_download_kfx_reader.png`, `verify_r36_home_continue_reading_updated.png`)**: *Confessions of Zeno* downloaded from Standard Ebooks, auto-converted to KFX on-device, and launched cleanly with real covers across Home Continue Reading.

---

## User-Reported Bug & Requirement (Top Priority)

When opening an `.epub` directly from internal storage (e.g., `/sdcard/en/0/` or any path via a file manager), Folio currently prompts *"Do you want to open book from external source?"* and then **hangs on an infinite loading wheel forever** when tapping Yes.

We have written 4 complete, zero-dependency drop-in Java files in `dropin_src/com/amazon/kindle/epub/ingest/`:
1. `EpubMetadataExtractor.java` — Extracts title, author, publisher, pubDate, language, ISBN, genres (`dc:subject`), and description from OPF XML.
2. `MetadataEnricher.java` — Fast 4s-timeout Open Library Search API enrichment for missing author, publishing date, genres, publisher, and ISBN (gracefully offline-safe).
3. `FolioMetadataDb.java` — Persistent `folio_metadata.db` (`books_metadata` table) + schema-adaptive transactional registration into `kindle_library.db` (`KindleContent`, `LocalContent`, `UserContent`).
4. `ExternalEpubActivity.java` — Dark Folio dialog offering **"Add to Library & Read"** (converts via `libkfxconv.so`, stores parsed `.kfx` + `.cover.jpg` inside Folio's app-private data folder `files/folio_library/`, enriches & saves metadata, registers in Library, opens in `KRIFDocViewer`) vs **"Open Once (Don't Add)"** (converts into `cache/folio_once/` and opens directly in `KRIFDocViewer` without adding to the Library DB).

---

## Task A: Integrate Drop-In Java Sources & Wire External EPUB Intent Hook

1. **Copy Drop-In Files**:
   - Copy all 4 `.java` files from `dropin_src/com/amazon/kindle/epub/ingest/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
   - Compile them with the existing mod Java sources into `classes*.dex`.
2. **Register `ExternalEpubActivity` in `AndroidManifest.xml`**:
   - Add `<activity android:name="com.amazon.kindle.epub.ingest.ExternalEpubActivity" android:exported="true" android:theme="@android:style/Theme.DeviceDefault.Dialog.NoActionBar">` with high-priority (`android:priority="999"`) `ACTION_VIEW` intent filters matching `application/epub+zip` and `*.epub` on both `content` and `file` schemes.
3. **Hook Existing External-Open Activity (Safety Net)**:
   - Identify whichever activity in `com.amazon.kindle.epub` currently shows the *"open book from external source?"* prompt (likely `ThirdPartyUpgradePage` or `StandAloneBookReaderActivity` or `SideLoadActivity`) and insert at the top of `onCreate`:
     ```smali
     invoke-static {p0, v_intent}, Lcom/amazon/kindle/epub/ingest/ExternalEpubActivity;->interceptIfExternalEpub(Landroid/app/Activity;Landroid/content/Intent;)Z
     move-result v_hit
     if-eqz v_hit, :continue_normal
     return-void
     :continue_normal
     ```
   - This guarantees ANY external `.epub` open intent (regardless of storage path or MIME type like `application/octet-stream`) lands cleanly in `ExternalEpubActivity` instead of Kindle's hanging spinner.
4. **Verify `launchKfxReader()`**:
   - Confirm `StandAloneBookReaderActivity` opens `.kfx` files from `context.getFilesDir()/folio_library/` and `context.getCacheDir()/folio_once/` cleanly (if `StandAloneBookReaderActivity` expects a specific intent extra or action used by `OpenCatalogView.registerDownloadedBook`, align `launchKfxReader()` in `ExternalEpubActivity.java` with that exact intent).
5. **Version Badge & Deploy**:
   - Update badge in `FolioMoreView.java` to **Folio v3.4 KFX**, rebuild, sign, and deploy to Galaxy M32.

---

## Task B: Hardware End-to-End Verification (Both Flows!)

Test using a real `.epub` located in an external internal-storage folder (e.g., `/sdcard/en/0/` or `/sdcard/Download/` — verify with `am start -a android.intent.action.VIEW -d "file:///sdcard/en/0/<book>.epub" -t "application/epub+zip" -p com.amazon.kindle.epub` AND a `content://` URI if available):

1. **Prompt Verification**: Confirm the dark **FOLIO EPUB IMPORT** dialog appears immediately with extracted title/author and **"Add to Library & Read"** / **"Open Once (Don't Add)"** buttons (zero infinite loading spinner!).
2. **Flow 1 — "Open Once"**:
   - Tap **"Open Once (Don't Add)"**.
   - Verify `.kfx` is created in `/data/data/com.amazon.kindle.epub/cache/folio_once/`, opens straight into `KRIFDocViewer` (`YJBINARY`), and is **NOT** added to `folio_metadata.db` or `kindle_library.db`.
3. **Flow 2 — "Add to Library & Read"**:
   - Re-trigger the external EPUB open intent and tap **"Add to Library & Read"**.
   - Verify:
     * Converted `.kfx` + `.cover.jpg` are stored inside Folio's app data folder `/data/data/com.amazon.kindle.epub/files/folio_library/`.
     * `folio_metadata.db` (`books_metadata` table) contains the saved row with `title`, `author`, `pub_date`, `genres`, `kfx_path`, and `cover_path` (print the `sqlite3` query output in `REPORT.md`).
     * Book appears in Folio's **Library** tab with its real cover and opens directly in `KRIFDocViewer`!

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.4 KFX`)

Capture and place all 4 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report037.zip`:

1. `screenshots/verify_r37_external_import_dialog.png`: The dark **FOLIO EPUB IMPORT** dialog showing extracted title/author and **"Add to Library & Read"** / **"Open Once (Don't Add)"** buttons.
2. `screenshots/verify_r37_open_once_reader.png`: Book opened directly in `KRIFDocViewer` after tapping **"Open Once"**.
3. `screenshots/verify_r37_add_to_library_reader.png`: Book opened directly in `KRIFDocViewer` after tapping **"Add to Library & Read"**.
4. `screenshots/verify_r37_library_imported_book.png`: Folio **Library** tab showing the newly imported book stored in app-private `files/folio_library/` with its real cover.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.4_kfx/base.apk`.
