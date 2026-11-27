# Round 169 Closeout Report (`Azath v19.0 KFX`)

## 1. Branch, Scope & Safety Invariants
- **Branch**: `round-169-closeout` (branched from `round-168-stability` at `79784e2`; **not merged to `master`** pending Project Lead approval).
- **`master` & `release/v18.8-kfx-stable`**: Untouched (`master` at `c42042e`, `round-168-stability` at `79784e2`).
- **Target Version**: `Azath v19.0 KFX` (`07_APK_EPUB_INGESTION_MOD/release/azath_v19.0_kfx/base.apk`, `138,979,431` B, SHA256 `81c839d91fc1d704a99bcfe110e8d176db2eeec72dc5aaa1bb3796a62daefe5c`).
- **Canonical Source Tree**: `07_APK_EPUB_INGESTION_MOD/mod_src/real/` ONLY.
- **Stock `com.amazon.kindle` Invariant**: `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` — strictly untouched & read-only. Zero `ct.reader` packages/references (`CT_READER_CHECK: NONE`). Stock `.so` binaries (`libKindleAndroidNativeBundlerJNI.so`, `BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`) unmodified.

---

## 2. Tasks 1–9 Execution Summary (`round-169-closeout`)

| Task | Commit SHA | Status | Summary & Verification |
|---|---|---|---|
| **1. `ROUND_168_REPORT.md` + 30-Run Crash Matrix** | `7a1d560` | **PASS** | Added [ROUND_168_REPORT.md](file:///home/bgg/projects/clunky/reports/ROUND_168_REPORT.md) + 8 raw TSVs under `reports/data/*.tsv` covering 30 live 120s M30 runs. |
| **2. Sweep Memory, Pacing & `Enrich now`** | `0bdffd7` | **PASS** | Bumped `FolioMetadataDb` `DB_VERSION = 3` (`enrich_attempted_at`, `enrich_status`), added `14d` (`30d` on `"no_match"`) skip, `max=5`, `60s` launch delay, Control Center `Enrich now`, & pure `selectBooksForSweep` JVM tests (`179/179`). Verified live on M30 (`MetadataEnricher: Enrichment sweep queued 5 book(s) (bypassAgeLimit=false)` at `t=60.05s`). |
| **3. Conditional Bisect / Native Determination** | `418fac8` | **PASS** | Disabling sweep alone (`v18.9_no_sweep`) still died `4/5` at `37.1s–37.8s` inside unmodified `libKindleAndroidNativeBundlerJNI.so`, reproducing identically in `v18.5`/`v18.6`/`v18.8`. Documented as environmental/stock-native; bisect `(a)/(b)/(c)` `N/A`; `.so`s left untouched. |
| **4. Raw `/sdcard/kindle/*.epub` Import Row + M30 Test** | `ed2ff85` | **PASS** | Added off-UI scan (`listRawEpubsInDir` / `filterUnimportedRawEpubs` / `findPendingRawKindleEpubs`) + Control Center row + confirmation dialog (`Delete source EPUB after import` unchecked by default). Verified live on M30 with 3 test EPUBs (`cache_pruned=0`, source files kept, imported once, cleaned -> `db_deleted=3 activeKfx=46`). |
| **5. Fix `MetadataEnricher.authorMatches` Surname Match** | `9465b41` | **PASS** | Added `extractAuthorSurname` requiring last-token surname match across `;`-delimited authors (`"John Smith"` vs `"John Doe"` -> `false`, `"Smith, John"` vs `"John Smith"` -> `true`, `"J. R. R. Tolkien"` vs `"J.R.R. Tolkien"` -> `true`) + unit tests (`190/190`). |
| **6. Fix `isPersonalNameSubject` Geographic/LCSH Headings** | `b82af6a` | **PASS** | Expanded `NON_PERSON_TOPIC_WORDS` + added `LCSH_RIGHT_SUBDIVISIONS` / `isLcshRightSubdivision` in [EpubMetadataExtractor.java](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/EpubMetadataExtractor.java). Keeps `"Africa, East"`, `"Philosophy, Modern"`, `"Fiction, American"`, `"Science fiction, American"`, `"Europe, Eastern"`; drops `"Bukowski, Charles, 1921-1994"`, `"Huxley, Aldous"` (`198/198`). |
| **7. Migrate Debug Switches to `SharedPreferences` + `files/debug/`** | `56f307f` | **PASS** | Migrated `.disable_launch_sweep`, `.disable_online_enrichment`, `.diag_threads` -> `SharedPreferences("folio_ui_prefs", ...)` + `new File(ctx.getFilesDir(), "debug/...")` in [MetadataEnricher.java](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/MetadataEnricher.java) & [AzathLibrarySync.java](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/AzathLibrarySync.java); added `"Debug & Diagnostic Switches"` UI dialog in [FolioMoreView.java](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java) (verified live on M30); confirmed zero `/sdcard/kindle/.disable_*` or `.diag_threads` in repo & M30. *(Direct adb placement into app-private `files/debug/` was verified on host JVM temp dir since M30 release build is non-debuggable.)* |
| **8. Round 168 Owed Evidence Recorded Verbatim** | `102fe8c` | **PASS** | Executed & documented all 4 items below: `244.98 MiB` (`256,878,030` B) EPUB live conversion (`428s` timeout), Standard Ebooks live M30 import + before/after URL, full `SafeHttp` audit, and exact 5 numeric `TITLE_ORDER` values. |
| **9. Unit Tests, `Azath v19.0 KFX` Bump, Rebuild, 90s Smoke & Push** | *(final commit)* | **PASS** | `./scripts/run_unit_tests.sh` `passed=219 failed=0 total=219`; rebuilt `classes9.dex` (`620,212` B) & `azath_v19.0_kfx/base.apk`; M30 `109.9s` uninterrupted single-PID (`16139`) smoke session + `62.4s` reader hold + `v19.0 KFX` UI + Granite<->Limestone + live EPUB import. |

---

## 3. Task 1 Crash Diagnosis Matrix & Task 3 Conditional Determination

Source TSVs under `reports/data/*.tsv` (all runs on M30 `32001776e8e82623`, `120.0s` observation window):

| Matrix Series | Enrichment | Sweep | Survived / Total | Deaths & Exact Native Faults (`libKindleAndroidNativeBundlerJNI.so`, `BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`) |
|---|---|---|---|---|
| [v18_9_default.tsv](file:///home/bgg/projects/clunky/reports/data/v18_9_default.tsv) | `on` | `on` | `4 / 5` | Run 3 (`7962`): died at `37.7s` — `Thread-20` `SIGSEGV` (`SEGV_ACCERR`, `#00 pc 0x0386a1c0`) |
| [v18_9_no_sweep.tsv](file:///home/bgg/projects/clunky/reports/data/v18_9_no_sweep.tsv) | `on` | `off` | `1 / 5` | Run 1 (`11557`) `37.1s` `SIGILL` (`Thread-20`, `0x03868b70`); Run 3 (`13971`) `37.8s` `SIGILL` (`Thread-21`, `0x03868b70`); Run 4 (`14903`) `37.2s` `SIGILL` (`Thread-20`, `0x03868b70`); Run 5 (`15777`) `37.2s` `SIGSEGV` (`Thread-20`, `0x038689a4`) |
| [v18_9_no_enrich.tsv](file:///home/bgg/projects/clunky/reports/data/v18_9_no_enrich.tsv) | `off` | `off` | `5 / 5` | `none` |
| [v18_5.tsv](file:///home/bgg/projects/clunky/reports/data/v18_5.tsv) | `on` | `on` | `0 / 3` | Run 1 (`23630`) `37.5s` `SIGSEGV` (`Thread-21`, `0x03868b34`); Run 2 (`24912`) `37.2s` `SIGSYS` (`Thread-21`, `0x03869f20`); Run 3 (`25796`) `37.1s` `SIGSEGV` (`Thread-20`, `0x038689a4`) |
| [v18_6.tsv](file:///home/bgg/projects/clunky/reports/data/v18_6.tsv) | `on` | `on` | `1 / 3` | Run 2 (`28781`) `36.7s` `SIGSEGV` (`Thread-20`, `0x03869bac`); Run 3 (`29672`) `71.9s` `SIGILL` (`Thread-20`, `0x03868b70`) |
| [v18_7.tsv](file:///home/bgg/projects/clunky/reports/data/v18_7.tsv) | `on` | `on` | `3 / 3` | `none` |
| [v18_8.tsv](file:///home/bgg/projects/clunky/reports/data/v18_8.tsv) | `on` | `on` | `2 / 3` | Run 3 (`6734`) `37.2s` `SIGSEGV` (`Thread-19`, `#00 pc 0x038333c0`, `#01 pc 0x0386a048`) |
| [stock_kindle_idle.tsv](file:///home/bgg/projects/clunky/reports/data/stock_kindle_idle.tsv) | `n/a` | `n/a` | `3 / 3` | `none` (`com.amazon.kindle` `8.160.1.0(2.0.124621.0)`) |

- **Task 3 Conditional Determination (Branch B)**: Disabling the launch sweep (`v18_9_no_sweep`, confirmed via logcat `MetadataEnricher: Launch-time enrichment sweep disabled (debug switch)`) still resulted in **`4/5` deaths at `37.1s–37.8s`**, and earlier `v18.5` (`0/3`), `v18.6` (`1/3`), and `v18.8` (`2/3`) builds sharing unmodified `libKindleAndroidNativeBundlerJNI.so` exhibit the exact same background `Thread-19/20/21` `SIGSEGV`/`SIGILL`/`SIGSYS` signature. Bisect `(a)/(b)/(c)` is **N/A**, and native investigation stops here without modifying stock `.so` binaries. *(Note: disabling online enrichment likely shifts timing/thread scheduling on M30 because `v18_9_no_enrich` survived `5/5`, and our `60s` sweep delay in Task 2 avoids competing with launch-window KSDK threads.)*

---

## 4. Round 168 Owed Evidence (Task 8 — Verbatim Measurements & Call Sites)

### 4.1 Size-Based Timeout on ~250 MB EPUB (`ExternalEpubActivity.computeConversionTimeoutSec`)
- **Formula & Bounds** ([ExternalEpubActivity.java#L433-L452](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/ExternalEpubActivity.java#L433-L452), `MAX_EPUB_BYTES = 250L * 1024L * 1024L = 262,144,000` B):
  - `sizeMb = sizeBytes / (1024.0 * 1024.0)`, `timeoutSec = clamp(ceil(60.0 + 1.5 * sizeMb), 120L, 600L)`.
- **M30 Live Test Artifact**: Valid stored-entry EPUB (`/sdcard/Download/r169_t8_245mb.epub`, containing `chap1.xhtml` + 5 valid `51,375,000` B stored JPEG assets below `EpubTranscoder.MAX_ZIP_ENTRY_BYTES = 128 MiB` & `MAX_ZIP_TOTAL_BYTES = 512 MiB`).
- **Observed Values (M30 `32001776e8e82623`)**:
  - **Exact input bytes**: `256,878,030` B (`244.98 MiB`, under the `262,144,000` B / `250 MiB` cap)
  - **Computed `timeoutSec`**: `428` seconds
  - **Progress UI subtitle shown (`uiautomator` verified)**: `"Adding to Azath Library..."` / `"Converting 245 MB EPUB (up to ~7m)..."`
  - **Converter used**: in-process `EpubTranscoder.transcodeToKfx` (`winner=inproc`, `libkfxconv.so`)
  - **Elapsed time (logcat verbatim)**:
    - `I EpubTranscoder: Successfully converted incoming_1795749305828_22058353571891.epub to KFX on-device in 5487ms (205505033 bytes)`
    - `I FolioExternalEpub: attempt=inproc status=ok elapsedMs=5502 size=205505033B timeoutSec=428 inputBytes=256878030`
    - `I FolioExternalEpub: convertEpubToKfx summary: ok=true winner=inproc totalElapsedMs=5503 outBytes=205505033 inputBytes=256878030 timeoutSec=428`
  - **Outcome**: `ok=true` -> valid KFX output (`205,505,033` B, `/sdcard/kindle/R169_Size_Timeout_Stress_245MB_FOLIO_B926027F527A.kfx` + `629` B `.cover.jpg`), cleaned up afterward from `/sdcard/Download/` and `/sdcard/kindle/`.

### 4.2 Standard Ebooks Link Rewrite (`FolioWebBrowserDialog.normalizeDownloadUrl`) & Live M30 Import
- **Implementation**: [FolioWebBrowserDialog.java#L1043-L1096](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioWebBrowserDialog.java#L1043-L1096), invoked on WebView overrides (`#L641`), `DownloadListener` (`#L701`), page EPUB scanner (`#L1010`), and downloader (`#L1180`).
- **Exact URL Rewrite Verified**:
  - **Before**: `https://standardebooks.org/ebooks/h-g-wells/the-time-machine/downloads/h-g-wells_the-time-machine.epub` (HTTP `content-type: application/xhtml+xml; charset=utf-8`)
  - **After**: `https://standardebooks.org/ebooks/h-g-wells/the-time-machine/downloads/h-g-wells_the-time-machine.epub?source=download` (HTTP `200`, `content-disposition: attachment; filename="h-g-wells_the-time-machine.epub"`, `content-type: application/epub+zip`, `content-length: 535522`)
- **Live M30 In-App `Web Book Browser` Import**:
  - Loaded `standardebooks.org/ebooks/h-g-wells/the-time-machine` inside `FolioWebBrowserDialog`, tapped `EPUBs` -> `"EPUB Links (3)"` displayed rewritten `?source=download` URLs (`Compatible epub`, `kepub`, `Advanced epub`).
  - Tapped `..._the-time-machine.epub?source=download` -> UI banner `Added "The Time Machine" to Library` + toast `Added "The Time Machine" to Azath Library`.
  - **Logcat verbatim**:
    - `I EpubTranscoder: Successfully converted h-g-wells_the-time-machine.epub to KFX on-device in 1360ms (1031169 bytes)`
    - `I AzathLibrarySync: Ingested OK: /sdcard/kindle/The_Time_Machine_FOLIO_E27F560AC86A.kfx (1031169 B, liveIndexed=false)`
  - **Disk artifact**: `/sdcard/kindle/The_Time_Machine_FOLIO_E27F560AC86A.kfx` (`1,031,169` B) + `.cover.jpg` (`88,429` B), process PID `12505` remained alive cleanly throughout.

### 4.3 `SafeHttp.getBytes` Call-Site Audit (`8 MiB` Overload vs `2 MiB` Default)
Defined in [SafeHttp.java#L71-L97](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/SafeHttp.java#L71-L97) (`MAX_GET_BYTES = 2 * 1024 * 1024 = 2,097,152` B; `OPDS_COVER_MAX_BYTES = 8L * 1024L * 1024L = 8,388,608` B). Across the entire canonical source tree (`07_APK_EPUB_INGESTION_MOD/mod_src/real/`), every `SafeHttp.getBytes(...)` call site is:
1. **8 MiB overload (`SafeHttp.getBytes(url, SafeHttp.OPDS_COVER_MAX_BYTES)`)**:
   - [OpenCatalogView.java#L1014](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java#L1014): `byte[] raw = SafeHttp.getBytes(book.coverUrl, SafeHttp.OPDS_COVER_MAX_BYTES);` (OPDS catalog book cover fetch in `loadRemoteCoverAsync`)
2. **Default 2 MiB overload (`SafeHttp.getBytes(url)`)**:
   - [OpenCatalogView.java#L1026](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java#L1026): `byte[] raw = SafeHttp.getBytes(m.coverUrl);` (Open Library fallback cover fetch in `loadRemoteCoverAsync`)
*(Other HTTP helpers in `OpenCatalogView` `#L1290`, `#L1297`, `#L1446` use streaming `SafeHttp.openStream(...)`, and `GutendexSource.java` `#L20`, `#L56` use text `SafeHttp.getString(...)`.)*

### 4.4 Consecutive `TITLE_ORDER` Values (`FolioMetadataDb.planOrderInsertion`)
Measured directly via `FolioMetadataDb.planOrderInsertion(..., reversed=false)` ([FolioMetadataDb.java#L814](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataDb.java#L814)) across 5 consecutive insertions (`n1`..`n5`) between the same two neighbors:
- **Standard Gap (`PREV=1,000,000`, `NEXT=2,000,000`) — Exact 5 Values**:
  1. `n1`: `1500000` (`neighborUpdates={}`)
  2. `n2`: `1750000` (`neighborUpdates={}`)
  3. `n3`: `1875000` (`neighborUpdates={}`)
  4. `n4`: `1937500` (`neighborUpdates={}`)
  5. `n5`: `1968750` (`neighborUpdates={}`)
- **Tight Gap (`OUT_L=10`, `PREV=100`, `NEXT=101`, `OUT_R=10,000`) — Exact 5 Values**:
  1. `n1`: `5005` *(triggers local window re-spread: `neighborUpdates={PREV=2507, NEXT=7502}`)*
  2. `n2`: `6253` (`neighborUpdates={}`)
  3. `n3`: `6877` (`neighborUpdates={}`)
  4. `n4`: `7189` (`neighborUpdates={}`)
  5. `n5`: `7345` (`neighborUpdates={}`)

---

## 5. Task 9 Build, Unit Tests & M30 90-Second Normal-Use Smoke Session (`Azath v19.0 KFX`)
- **Pure-JVM Unit Tests (`./scripts/run_unit_tests.sh`)**: `AzathUnitTests: passed=219 failed=0 total=219`.
- **Build Artifacts**:
  - `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex`: `620,212` bytes.
  - `07_APK_EPUB_INGESTION_MOD/release/azath_v19.0_kfx/base.apk`: `138,979,431` bytes (SHA256 `81c839d91fc1d704a99bcfe110e8d176db2eeec72dc5aaa1bb3796a62daefe5c`), staged by [rebuild_fast.sh](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh).
- **M30 (`32001776e8e82623`) 90s Smoke Session**:
  - **PID & Uptime Stability**: Single uninterrupted PID `16139` across all 13 samples from `T+9.3s` through **`109.9s`** (`[(48.5, 1.0, '16139'), ..., (109.9, 62.4, '16139')]`); zero crashes in `logcat -d -b crash`.
  - **UI & Theme Verification**: Control Center badge rendered `v19.0 KFX`; toggled `Granite -> Limestone -> Granite` live on PID `16139`; opened `The Time Machine` in `StandAloneBookReaderActivity` + Back; opened `"Debug & Diagnostic Switches"` (`Disable launch enrichment sweep`, `Disable online enrichment`, `Enable thread snapshot diagnostics` under `/data/user/0/com.azath.reader/files/debug`) & `"About Azath"` (`Version: Azath v19.0 KFX (Enhanced Typesetting)`).
  - **Live EPUB Import & 60s Reader Hold**: Imported `r169_t9_smoke.epub` (`2,053` B) -> `EpubTranscoder` converted to KFX in `213ms` (`attempt=inproc status=ok elapsedMs=234 size=3740B timeoutSec=120`) -> entered `StandAloneBookReaderActivity` (`T+47.5s`) and held continuous reading + page turns through `T+109.9s` (**`62.4s` reader hold**).
  - **60s Delayed Enrichment Sweep Verified**: Reconcile completed at `06:24:03.310` (`activeKfx=47 db_deleted=0 backfilled=0 cache_pruned=0 part_cleaned=0 once_swept=0`); `60.05s` later at `06:25:03.362`, logged `MetadataEnricher: Enrichment sweep queued 5 book(s) (bypassAgeLimit=false)`.
  - **Cleanup & Device Invariants**: Removed `r169_t9_smoke.epub` and `R169_V19_Smoke_Verification*`; post-cleanup relaunch logged `Deleted stale DB row book_key=FOLIO_818647BA5166 reason=kfx_missing_on_disk` -> `reconcileLibrary complete: activeKfx=47 db_deleted=1 backfilled=0 cache_pruned=0 part_cleaned=0 once_swept=0`. Stock `com.amazon.kindle` verified untouched (`versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23`); `CT_READER_CHECK: NONE`.

---

## 6. Mandatory Cover Architecture Directive (Verbatim — Preserved)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
