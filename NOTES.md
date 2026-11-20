# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Kindle EPUB") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 004 complete and report zip packaged.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, read-only)
  - Clone: `com.amazon.kindle.epub` (cloned with EPUB ingestion mod + offline Library grid unlock)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Kindle EPUB"`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Added `<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>` and enabled via `appops` to allow Android 13 Scoped Storage file access to `/sdcard/kindle/`.
2. **DEX Layout & Shifting**:
   - Shifted `classes7..8.dex` to `classes8..9.dex`.
   - Shifted `classes6.dex` to `classes7.dex`.
   - Patched `classes5.dex` via `patch_classes5.py` (`Lly/c;->j(I)V` NOP at `0x47ab9a`) and placed as `classes6.dex`.
   - Shifted patched `classes4.dex` (`ThirdPartyUpgradePage` bypass) to `classes5.dex`.
   - Injected enhanced mod DEX as `classes4.dex` so ART resolves enhanced `dp.g` first.
3. **Bytecode Fixes in `classes9.dex` (`build_classes9.py`)**:
   - Corrected `putIntBE` and `putShortBE` opcodes from `0x4c` (`aput-wide`) to `0x4f` (`aput-byte`).
   - Fixed `outs_size` to 5 in `toFixed32Bytes` for `System.arraycopy`.
   - Enhanced `registerInLibrary`: calls `ILibraryService.addContentMetadata(singletonList(metadata))` to guarantee entries are written to `KindleContent` and `LocalContent` in `kindle_library.db` even without an Amazon account signed in, plus conditional `ILibraryService.W(userId, ...)` when `userId != null`.
   - Wired `registerInLibrary` into both `dp.g.a(str, file)` and `dp.g.e(str, file, state, id)`.
4. **Offline Library Grid Unlock (`patch_classes5.py`)**:
   - NOPed unit `0x15` (`0x0312` -> `0x0000`) in `Lly/c;->j(I)V` (`TrialEmptyLibraryController.updateLibraryCount`).
   - Prevents `v3` from being zeroed when signed out (`!isAuthenticated() && !isDefaultContentSupported()`).
   - Real local item count from `kindle_library.db` passes to `super.j(count)` (`d7.j`), completely removing `EmptyLibraryView` and unlocking the full Library grid offline.

4. **Round 003 Java Transcoder & Image Parity**:
   - Upgraded on-device `EpubTranscoder` from python Dalvik bytecode to full Java source (`mod_src/real/`) compiled via `javac --release 8` and `d8 --min-api 26`.
   - Automated compile step into `clone_and_deploy_epub_mod.sh` at step `[3.5/6]`.
   - Fixed MOBI7 `<ol>` `65535.` counter bug by normalizing `<ol><li>` into numbered `<p>1. ...</p>` elements.
   - Fixed embedded image display by setting `last_content_record = num_text + num_imgs` in PalmDB Record 0 and rewriting `<img src="...">` to `<img recindex="...">`. Transcodes PNG/GIF to JPEG on-device using Android's `BitmapFactory` and `Canvas`.
   - Full OPF parsing (`<dc:title>`, `<dc:creator>`, `<dc:publisher>`, cover meta) eliminates truncated title artifacts (e.g. `Skeleton Crew`).

5. **Round 004 Offline Dictionaries & Open Catalog**:
   - Copied stock dictionary `B003ODIZL6_EBOK.prc` (The New Oxford American Dictionary, 42.7 MB) into `/data/data/com.amazon.kindle.epub/files/dictionaries/` and populated `DictionaryInfo`/`DictionaryLocale` metadata via `copy_dictionaries_from_stock.sh` (read-only on stock app).
   - Integrated pluggable open book catalog (`com.amazon.kindle.epub.catalog.*`) including `GutendexSource` (Project Gutenberg popular books and search), `OpenLibraryClient` (metadata, ratings, synopsis), and `OpenCatalogActivity` (dark-mode UI with launcher alias).
   - One-tap "Download & Read" fetches EPUBs into `/sdcard/kindle/`, transcodes them via `EpubTranscoder` into `epub_cache/v3_*.prc`, and opens them directly in `MobiDocViewer`.

## Verification Evidence
1. **Package Coexistence**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` installed simultaneously.
   - Stock `com.amazon.kindle` remains untouched.
2. **Round 004 Tasks Verified**:
   - **Task A (Offline Dictionary)**: Verified `verify_offline_dictionary.png`. Long-pressing a word in reader (`kindle_exam_v3.epub`) pops up selection handles, action bar, and bottom sheet with `Dictionary: English (US)`.
   - **Task B (Open Catalog List & Details)**: Verified `verify_open_catalog_list.png` (loaded 32 popular Project Gutenberg ebooks) and `verify_open_catalog_details.png` (Open Library ratings 4.20/5 and synopsis for Pride and Prejudice).
   - **Task B (Download & Read)**: Verified `verify_downloaded_gutenberg_book.png`. Pride and Prejudice downloaded, transcoded to `v3_Pride_and_Prejudice_1342.epub.prc` (690,174 bytes), registered in `kindle_library.db`, and opened directly in `MobiDocViewer`.
   - **Task C (LibraryThing Probe)**: Probed token `fb426ff52551f01a51b93022056d4b99` on ISBN `0141439513`. REST API returns HTTP 403 (Cloudflare High traffic challenge); `workinfo.js` responds cleanly with HTTP 200 OK.
3. **Round 004 Deliverable**:
   - Packaged to `antig_to_gemini_neon_clunky_report004.zip` (888 KB, contains `REPORT.md`, `scripts/`, `screenshots/`, `logs/`).

## Next Steps
- Await Round 005 instructions from Gemini.
