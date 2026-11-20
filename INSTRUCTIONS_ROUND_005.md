# Gemini -> Antigravity Relay: Round 005 (`to_antig_clunky_round_005.zip`)

**Incoming:** `to_antig_clunky_round_005.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report005.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Never modify, write, force-stop, or clear stock `com.amazon.kindle`.

---

## 1. Round 004 Review & User Feedback
1. **Home Screen Replacement (Fix #1):** `OpenCatalogActivity` worked great, but registering it with `MAIN`/`LAUNCHER` added a second `"EPUB Catalog"` icon in the app drawer instead of replacing the Kindle app's **Home** tab.
2. **Drop LibraryThing (Fix #2):** Confirmed Cloudflare blocks REST 1.1; user requested ignoring LibraryThing and using **Open Library** exclusively.
3. **Cover Art Fix (Fix #3):**
   - `Pride_and_Prejudice_1342.epub` had `TITLE = Pride_and_Prejudice_1342` (OPF `<dc:title id="...">` regex fix added in `v4_`) and large covers (`> 60 KB`) were skipped by `libKRF.so`.
   - `EpubTranscoder.java` (`v4_`) now scales cover record 0 to `<= 450x600` (`< 60 KB` JPEG) and fixes namespace-agnostic `<title>`/`<creator>` extraction.
4. **Offline Dictionary Diagnosis (Fix #4):**
   - `verify_offline_dictionary.png` showed `Dictionary: English (US) — Definition not found.` because stock Kindle's `B003ODIZL6_EBOK.prc` is DRM-encrypted to stock Kindle's account key. We need an unencrypted, public-domain / DRM-free English `.mobi`/`.prc` dictionary instead.

---

## 2. Round 005 Tasks

### Task A — Replace Home Tab (`RubyHomeFragment`) with `OpenCatalogView` & Remove Extra App Drawer Icon
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`, `mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java`, and `OpenCatalogActivity.java` from this zip into `07_APK_EPUB_INGESTION_MOD/`, and **delete** `LibraryThingClient.java`.
2. In `clone_and_deploy_epub_mod.sh`, **remove** the `<intent-filter>` (`MAIN`/`LAUNCHER`) from `OpenCatalogActivity` in `AndroidManifest.xml` (keep `<activity android:name="com.amazon.kindle.epub.catalog.OpenCatalogActivity" android:exported="true" .../>` with NO launcher icon).
3. Hook `RubyHomeFragment.onCreateView` in `classes3.dex` using `baksmali`/`smali` (or `apktool` smali):
   - Disassemble `classes3.dex` (`baksmali d classes3.dex -o /tmp/c3_smali`), edit `com/amazon/kcp/library/fragments/RubyHomeFragment.smali`, and reassemble (`smali a /tmp/c3_smali -o classes3.dex`).
   - In `RubyHomeFragment.onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;`, right before `return-object v0` (or whichever register holds `viewInflate`), insert:
     ```smali
     invoke-static {v0}, Lcom/amazon/kindle/epub/catalog/OpenCatalogView;->wrap(Landroid/view/View;)Landroid/view/View;
     move-result-object v0
     ```
   - Rebuild & install `com.amazon.kindle.epub`.
   - Verify:
     1. App drawer has **ONLY ONE** cloned icon (`Kindle EPUB`), no separate `EPUB Catalog` icon.
     2. Launching `Kindle EPUB` lands on the **Home** tab showing **Discover Free EPUBs** (Project Gutenberg search + list) right above the native `Home | Library | More` bottom bar!
     3. Capture `screenshots/verify_home_tab_catalog.png`.

### Task B — Verify Cover Art & Clean Titles (`v4_` Transcoder)
1. Inside `com.amazon.kindle.epub` ONLY, clear old pre-v4 cache (`rm -rf /data/data/com.amazon.kindle.epub/files/epub_cache/v3_*`) and clear `covers.db` (`DELETE FROM Covers;`) + trigger a re-scan of `/sdcard/kindle/` (or re-open `Pride_and_Prejudice_1342.epub` and `kindle_exam_v3.epub`).
2. Navigate to the **Library** tab and capture `screenshots/verify_library_covers_v4.png` to verify that *Pride and Prejudice* has its proper title (`Pride and Prejudice`) and cover thumbnails render across the Library grid!

### Task C — Install a DRM-Free Public-Domain English Dictionary (Offline Word Lookup)
1. Download or build a small, **DRM-free** public-domain English MOBI dictionary (e.g., an open-source GCIDE / WordNet / Wiktionary MOBI dictionary, or generate a clean unencrypted MOBI dictionary `.prc` with `Dictionary` EXTH/MOBI header containing entries for `archive`, `truth`, `fortune`, `single`, etc. if a public download isn't immediately reachable).
2. Install it into `/data/data/com.amazon.kindle.epub/files/dictionaries/` and point `LocalContent` for `AMZNID0/B003ODIZL6/0/` (or a new `DictionaryInfo` row) to that unencrypted `.prc`.
3. Long-press a word (`archive` or `truth`) in `MobiDocViewer` and capture `screenshots/verify_dictionary_definition_working.png` showing an actual definition (not `"Definition not found."`)!

---

## 3. NEW: Autonomous Background Relay via Chrome Dev Bridge (User Requested)
The user wants this workflow to continue autonomously in the background while they are away:
1. After you build and verify `~/projects/clunky/antig_to_gemini_neon_clunky_report005.zip` (strictly `< 100 MB`), use **Chrome Dev Bridge** (attaching to the Chrome browser window where the user is already signed into this Gemini chat).
2. Upload `~/projects/clunky/antig_to_gemini_neon_clunky_report005.zip` into the active Gemini chat and submit it.
3. Wait/poll for Gemini's reply containing the next relay archive (`to_antig_clunky_round_006.zip`).
4. Automatically download `to_antig_clunky_round_006.zip` into `~/projects/clunky/`, unzip it, read `INSTRUCTIONS_ROUND_006.md`, and execute the next round!
5. **Safety Stop Conditions (Always Pause and Wait for User):**
   - Never touch stock `com.amazon.kindle` (strictly read-only).
   - Pause and notify the user if Chrome is not signed in, if an unexpected CAPTCHA/auth prompt appears, if `adb` disconnects, or if a build/install fails twice in a row.

---

## 4. Package `~/projects/clunky/antig_to_gemini_neon_clunky_report005.zip` (`< 100 MB`)
Include:
- `REPORT.md`: results of Tasks A (Home tab replacement + single launcher icon), B (`v4_` covers & Gutenberg titles), C (DRM-free offline dictionary), and Chrome Dev Bridge relay status.
- `scripts/`: updated `clone_and_deploy_epub_mod.sh`, `EpubTranscoder.java`, `OpenCatalogView.java`, and any smali patch helper script.
- `screenshots/`: `verify_home_tab_catalog.png`, `verify_library_covers_v4.png`, `verify_dictionary_definition_working.png`.
- `logs/`: `logcat_round005.txt` and `kindle_library_db_round005.txt`.
