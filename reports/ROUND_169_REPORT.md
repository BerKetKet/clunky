# Round 169 Closeout Report (`Azath v19.0 KFX`)

## 1. Branch, Scope & Safety Invariants
- **Branch**: `round-169-closeout` (branched from `round-168-stability` at `79784e2`; **not merged to `master`** pending Project Lead approval).
- **Target Version**: `Azath v19.0 KFX` (`07_APK_EPUB_INGESTION_MOD/release/azath_v19.0_kfx/base.apk`).
- **Canonical Source Tree**: `07_APK_EPUB_INGESTION_MOD/mod_src/real/` ONLY.
- **Stock `com.amazon.kindle` Invariant**: `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` — strictly untouched & read-only. Zero `ct.reader` packages/references. Stock `.so` binaries (`libKindleAndroidNativeBundlerJNI.so`) untouched.

---

## 2. Task 3 Conditional Determination (Driven by Task 1 Matrix)

Per Task 3 rules, we evaluated whether disabling only the launch sweep (`v18.9` with `/sdcard/kindle/.disable_launch_sweep`) eliminates the native `~37s` crash observed across `v18.5`–`v18.9`.

- **Result**: **The crash does NOT disappear when the launch sweep is disabled.**
  - `v18_9_no_sweep` (`enrich=on`, `sweep=off`, verified by logcat `MetadataEnricher: Launch-time enrichment sweep disabled (debug switch)`) died **`4/5` times** at `37.1s–37.8s` (`SIGILL ILL_ILLOPC` × 3 at `#00 pc 0x03868b70`; `SIGSEGV SEGV_ACCERR` × 1 at `#00 pc 0x038689a4` in `libKindleAndroidNativeBundlerJNI.so`).
  - `v18_9_default` (`enrich=on`, `sweep=on`) died **`1/5` times** (`37.7s`, `SIGSEGV` at `#00 pc 0x0386a1c0`).
  - Earlier historical releases sharing the exact same unmodified native `libKindleAndroidNativeBundlerJNI.so` (`BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`) reproduce the exact same failure signature: `v18.5` died **`3/3` times** (`37.1s–37.5s`: `SIGSEGV`/`SIGSYS`/`SIGSEGV`), `v18.6` died **`2/3` times** (`36.7s`, `71.9s`), and `v18.8` died **`1/3` times** (`37.2s`).
  - Idle stock `com.amazon.kindle` control survived **`3/3` times** (`120.0s`) untouched (`lastUpdateTime=2026-11-19 21:59:23`).
- **Decision (Task 3 Branch B)**: The crash is **environmental / unmodified stock-native** (`libKindleAndroidNativeBundlerJNI.so` KSDK background `Thread-19`/`20`/`21`/`22` faults under unregistered `com.azath.reader`), independent of Java-side launch sweep or `v18.5`–`v18.9` changes. Sweep bisect `(a)/(b)/(c)` is **not applicable** (`N/A`), and native investigation stops here without modifying stock native libraries.

---

## 3. Round 168 Owed Evidence (Task 8 — Verbatim Measurements & Call Sites)

### 3.1 Size-Based Timeout on ~250 MB EPUB (`ExternalEpubActivity.computeConversionTimeoutSec`)
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

### 3.2 Standard Ebooks Link Rewrite (`FolioWebBrowserDialog.normalizeDownloadUrl`) & Live M30 Import
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

### 3.3 `SafeHttp.getBytes` Call-Site Audit (`8 MiB` Overload vs `2 MiB` Default)
Defined in [SafeHttp.java#L71-L97](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/SafeHttp.java#L71-L97) (`MAX_GET_BYTES = 2 * 1024 * 1024 = 2,097,152` B; `OPDS_COVER_MAX_BYTES = 8L * 1024L * 1024L = 8,388,608` B). Across the entire canonical source tree (`07_APK_EPUB_INGESTION_MOD/mod_src/real/`), every `SafeHttp.getBytes(...)` call site is:
1. **8 MiB overload (`SafeHttp.getBytes(url, SafeHttp.OPDS_COVER_MAX_BYTES)`)**:
   - [OpenCatalogView.java#L1014](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java#L1014): `byte[] raw = SafeHttp.getBytes(book.coverUrl, SafeHttp.OPDS_COVER_MAX_BYTES);` (OPDS catalog book cover fetch in `loadRemoteCoverAsync`)
2. **Default 2 MiB overload (`SafeHttp.getBytes(url)`)**:
   - [OpenCatalogView.java#L1026](file:///home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java#L1026): `byte[] raw = SafeHttp.getBytes(m.coverUrl);` (Open Library fallback cover fetch in `loadRemoteCoverAsync`)
*(Other HTTP helpers in `OpenCatalogView` `#L1290`, `#L1297`, `#L1446` use streaming `SafeHttp.openStream(...)`, and `GutendexSource.java` `#L20`, `#L56` use text `SafeHttp.getString(...)`.)*

### 3.4 Consecutive `TITLE_ORDER` Values (`FolioMetadataDb.planOrderInsertion`)
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

## 4. Mandatory Cover Architecture Directive (Verbatim — Preserved)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
