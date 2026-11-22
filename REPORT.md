# Round 037 Final Verification & Engineering Report
**Project Folio: Enhanced Offline EPUB/KFX Reader**  
**Date:** November 22, 2026  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13)  
**Release Badge:** Folio v3.4 KFX (External EPUB Ingestion & Rich Metadata DB)  
**Safety Invariant:** Stock `com.amazon.kindle` (v8.160.1.0) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 037 resolves the top-priority user bug where opening an `.epub` file directly from external/device storage (e.g. `/sdcard/en/0/` or any file manager) resulted in a hanging infinite loading wheel.

1. **Root Cause Analysis**:
   - External `ACTION_VIEW` intents were previously caught by `ThirdPartyUpgradePage`, which delegated them via `openDocumentForContent(uri)` to `ContentSchemeBookOpenHelperActivity`.
   - `ContentSchemeBookOpenHelperActivity` contained a hardcoded enum `ContentType { PDF(".pdf"), MOBI(".prc") }` with zero handling for `.epub` or `application/epub+zip`. When an EPUB arrived, it displayed `ProgressBar loadingSpinner` indefinitely while waiting for an internal Kindle PDF/MOBI parser that never executed.

2. **Task A: Drop-In Java Sources & Intent Hook Integration**:
   - Integrated Gemini's 4 drop-in Java classes under `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`:
     * `EpubMetadataExtractor.java`: Zero-dependency, pure Java XML pull parser extracting title, author, pubDate, publisher, language, ISBN, genres, and description from OPF in <25ms.
     * `MetadataEnricher.java`: Lightweight, offline-safe Open Library Search API enrichment with 4s connection / 5s read timeouts.
     * `FolioMetadataDb.java`: SQLiteOpenHelper managing `folio_metadata.db` (`books_metadata` table) + schema-adaptive transactional registration into `kindle_library.db` (`KindleContent`, `LocalContent`, `UserContent`).
     * `ExternalEpubActivity.java`: Custom dark UI dialog offering **"Add to Library & Read"** vs **"Open Once (Don't Add)"**.
   - **Manifest Registration**: Registered `ExternalEpubActivity` in `AndroidManifest.xml` with `android:priority="999"` for `ACTION_VIEW`, `application/epub+zip`, `application/octet-stream`, and `*.epub` on both `file` and `content` schemes.
   - **Smali Interception Safety Net**: Disassembled `classes5.dex` via baksmali and injected `ExternalEpubActivity.interceptIfExternalEpub(Activity, Intent)` at `onCreate` in both `ThirdPartyUpgradePage.smali` and `ContentSchemeBookOpenHelperActivity.smali` immediately after `super.onCreate` (satisfying Android's strict `SuperNotCalledException` contract).
   - **Database Alignment**: Enhanced `FolioMetadataDb.java` to set Kindle's exact NOT NULL columns (`ID = AMZNID0/<asin>/0/`, `STATE = 'LOCAL'`, `ARCHIVABLE = 0`, `HAS_READ_ALONG = 0`, `TYPE = 'BT_EBOOK'`, `CONTENT_TYPE = 'application/x-kfx-ebook'`, `USER_ID = 'MASTER'`, `IS_IN_CAROUSEL = 1`), guaranteeing seamless inclusion in Folio's Library and Continue Reading carousels.
   - Compiled `classes9.dex` (146 KB) via `compile_mod_dex.sh` (javac + d8), updated version badge to **Folio v3.4 KFX** in `FolioMoreView.java`, rebuilt, aligned, signed, and deployed the package.

3. **Task B: Physical Hardware Verification (Galaxy M32 — Both Flows Verified!)**:
   - Tested on physical Samsung Galaxy M32 using real internal storage title `/sdcard/en/0/Aldous Huxley - Brave New World.epub`:
     * **Prompt Verification**: The dark **FOLIO EPUB IMPORT** dialog appeared immediately with parsed metadata: title `"Brave New World"`, author `"Aldous Huxley · 2014-06-06"`. Zero infinite spinner!
     * **Flow 1 ("Open Once")**:
       - Tapped **"Open Once (Don't Add)"**.
       - Pure-Rust `libkfxconv.so` converted the EPUB into temporary app cache: `/data/data/com.amazon.kindle.epub/cache/folio_once/Brave_New_World_FOLIO_621B293A3B8F.kfx` (987 KB) + companion `.cover.jpg` (68 KB).
       - Launched immediately into `KRIFDocViewer` (`format: YJBINARY`, `StandAloneBookReaderActivity`).
       - Verified zero rows added to `folio_metadata.db` or `kindle_library.db` (clean isolation).
     * **Flow 2 ("Add to Library & Read")**:
       - Re-triggered external open intent and tapped **"Add to Library & Read"**.
       - Converted and stored `.kfx` (987 KB) and `.cover.jpg` (68 KB) inside app-private `/data/data/com.amazon.kindle.epub/files/folio_library/`.
       - Enriched metadata via Open Library API (`/works/OL64365W`, publisher `"HarperCollins"`, ISBN `"9780062368232"`, genres `"Utopias, Brainwashing, Moral and ethical aspects of Science, Fiction, Science and state"`) and saved to `folio_metadata.db`.
       - Transactionally registered in `kindle_library.db` (`KindleContent`, `LocalContent`, `UserContent`).
       - Book appeared immediately at the top of Folio's **Library** tab and **Continue Reading** carousel with its real cover and opened directly in `KRIFDocViewer`.

4. **Task C: Visual Proof Screenshots & Release Staging**:
   - Captured 4 hardware proof screenshots:
     * `screenshots/verify_r37_external_import_dialog.png`: Dark **FOLIO EPUB IMPORT** dialog showing extracted metadata and action buttons.
     * `screenshots/verify_r37_open_once_reader.png`: Book open in `KRIFDocViewer` after "Open Once".
     * `screenshots/verify_r37_add_to_library_reader.png`: Book open in `KRIFDocViewer` after "Add to Library & Read".
     * `screenshots/verify_r37_library_imported_book.png`: Folio **Library** tab showing the newly imported book with its real cover.
   - Staged production APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.4_kfx/base.apk` (131 MB).
   - Stock safety invariant re-verified: `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23`.

---

## 2. Task A: Drop-In Integration & Smali Intercept Details

### 2.1 Smali Interception Hook

In both `com.amazon.kindle.ThirdPartyUpgradePage.smali` and `com.amazon.kindle.ContentSchemeBookOpenHelperActivity.smali`, the following hook was placed right after `super.onCreate(savedInstanceState)`:

```smali
    invoke-super {p0, p1}, ...->onCreate(Landroid/os/Bundle;)V

    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-static {p0, v0}, Lcom/amazon/kindle/epub/ingest/ExternalEpubActivity;->interceptIfExternalEpub(Landroid/app/Activity;Landroid/content/Intent;)Z
    move-result v0
    if-eqz v0, :cond_normal
    return-void
    :cond_normal
```

This guarantees that:
1. `activity.mCalled` is marked `true` by Android's `ActivityThread`, eliminating `SuperNotCalledException`.
2. Any external EPUB intent forwarded to Kindle's legacy activities is immediately redirected to `ExternalEpubActivity`.

### 2.2 AndroidManifest.xml Registration

```xml
<activity android:name="com.amazon.kindle.epub.ingest.ExternalEpubActivity"
    android:exported="true"
    android:theme="@android:style/Theme.DeviceDefault.Dialog.NoActionBar">
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:mimeType="application/epub+zip" android:scheme="file"/>
    </intent-filter>
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:mimeType="application/epub+zip" android:scheme="content"/>
    </intent-filter>
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:host="*" android:pathPattern=".*\\.epub" android:scheme="file"/>
    </intent-filter>
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:host="*" android:pathPattern=".*\\.epub" android:scheme="content"/>
    </intent-filter>
</activity>
```

---

## 3. Task B: Hardware Verification Output

### 3.1 SQLite Query Output (`folio_metadata.db` — `books_metadata` table)

Command executed on target device:
```bash
sqlite3 -line /data/data/com.amazon.kindle.epub/databases/folio_metadata.db "SELECT * FROM books_metadata;"
```

Output:
```text
              id = 1
        book_key = FOLIO_621B293A3B8F
        kfx_path = /data/user/0/com.amazon.kindle.epub/files/folio_library/Brave_New_World_FOLIO_621B293A3B8F.kfx
      cover_path = /data/user/0/com.amazon.kindle.epub/files/folio_library/Brave_New_World_FOLIO_621B293A3B8F.kfx.cover.jpg
     source_name = Aldous Huxley - Brave New World.epub
           title = Brave New World
          author = Aldous Huxley
       publisher = HarperCollins
        pub_date = 2014-06-06
          genres = Utopias, Brainwashing, Moral and ethical aspects of Science, Fiction, Science and state
        language = en
            isbn = 9780062368232
     description = Aldous Huxley's tour de force, Brave New World is a darkly satiric vision of a "utopian" future—where humans are genetically bred and pharmaceutically anesthetized to passively serve a ruling order...
open_library_key = /works/OL64365W
        added_at = 1795355469951
```

### 3.2 SQLite Query Output (`kindle_library.db` — `KindleContent` table)

Command executed on target device:
```bash
sqlite3 -line /data/data/com.amazon.kindle.epub/databases/kindle_library.db "SELECT ID, TITLE, AUTHOR, CONTENT_TYPE, STATE, HAS_LOADED_COVER FROM KindleContent WHERE TITLE LIKE '%Brave%';"
```

Output:
```text
              ID = AMZNID0/CR!1928890731KFX/0/
           TITLE = Brave New World
          AUTHOR = Aldous Huxley
    CONTENT_TYPE = application/x-kfx-ebook
           STATE = LOCAL
HAS_LOADED_COVER = 1
```

### 3.3 Storage Location Verification

1. **"Open Once" Temp Cache**:
   ```text
   /data/data/com.amazon.kindle.epub/cache/folio_once/
   -rw------- 1 u0_a262 u0_a262_cache 987411 Brave_New_World_FOLIO_621B293A3B8F.kfx
   -rw------- 1 u0_a262 u0_a262_cache  68883 Brave_New_World_FOLIO_621B293A3B8F.kfx.cover.jpg
   drwx--S--- 2 u0_a262 u0_a262_cache   3488 Brave_New_World_FOLIO_621B293A3B8F.sdr
   ```
2. **"Add to Library" App-Private Storage**:
   ```text
   /data/data/com.amazon.kindle.epub/files/folio_library/
   -rw------- 1 u0_a262 u0_a262 987411 Brave_New_World_FOLIO_621B293A3B8F.kfx
   -rw------- 1 u0_a262 u0_a262  68883 Brave_New_World_FOLIO_621B293A3B8F.kfx.cover.jpg
   ```

---

## 4. Architectural Directive for Manager AI: Cover Rendering

> [!IMPORTANT]
> **Manager AI Architectural Directive: Independent Cover Presentation**  
> We reiterate the project lead directive regarding book cover handling:  
> Attempting to display the cover as an embedded HTML page or section within the reflowable book spine is fundamentally flawed. In reflowable engines, embedding the cover as an HTML `<img>` or SVG block causes persistent margin quirks, irregular padding, letterboxing, and layout conflicts with user font/margin settings.  
>  
> **The Prescribed Architecture:**  
> When parsing the EPUB for the first time during conversion:  
> 1. Extract the cover image asset.  
> 2. Remove the synthetic cover page XHTML from the reflowable spine so the reader begins directly with the front matter / title page.  
> 3. Process and maintain the cover image in its own dedicated class / asset structure.  
> 4. Present the cover strictly in dedicated UI views (fullscreen native cover viewer, library grid cards, and carousel cards), completely decoupled from the reflowable prose viewport.

---

## 5. Task C: Visual Verification & Proof Artifacts

All 4 proof screenshots captured on the Samsung Galaxy M32 (`32001776e8e82623`):

1. **`screenshots/verify_r37_external_import_dialog.png`**:
   - The dark **FOLIO EPUB IMPORT** dialog displaying extracted metadata (`Brave New World`, `Aldous Huxley · 2014-06-06`) and the two action buttons (`Add to Library & Read` and `Open Once (Don't Add)`).
2. **`screenshots/verify_r37_open_once_reader.png`**:
   - Book opened directly in `KRIFDocViewer` (`format: YJBINARY`, `StandAloneBookReaderActivity`) after tapping **"Open Once"**.
3. **`screenshots/verify_r37_add_to_library_reader.png`**:
   - Book opened directly in `KRIFDocViewer` after tapping **"Add to Library & Read"**.
4. **`screenshots/verify_r37_library_imported_book.png`**:
   - Folio **Library** tab displaying `Brave New World` (964.3 KB) at the top of the library with its real cover.

---

## 6. Safety Invariant & Release Staging Verification

1. **Stock Package Safety**:
   ```text
   Package: com.amazon.kindle
   Status: 100% UNTOUCHED and READ-ONLY
   lastUpdateTime: 2026-11-19 21:59:23
   ```
2. **Release Staging**:
   - `07_APK_EPUB_INGESTION_MOD/release/folio_v3.4_kfx/base.apk` (131 MB)
   - Badge: **Folio v3.4 KFX** verified in `FolioMoreView.java`.
