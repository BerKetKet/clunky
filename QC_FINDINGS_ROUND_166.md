# Round 166 Deep Security & Stability Audit (`QC_FINDINGS_ROUND_166.md`)

Deep line-by-line security and stability audit of:
1. `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`
2. `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioWebBrowserDialog.java`
3. `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java`
4. `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
5. Supporting review of `CoverOverlayView.java` and `SafeHttp.java`

---

## P0 (Critical — Zip-Bomb DoS / Path Traversal / Restore Validation)

### P0-1: `EpubTranscoder.java` — Unbounded ZIP Decompression (Zip-Bomb OOM / DoS) & Zip-Slip Path Segments (`lines 350–371`, `1559–1564`)
- **Failure Mode**: `transcode(File, File, boolean)` read every entry from `ZipInputStream` into heap (`Map<String, byte[]> zipEntries`) with **no entry count limit**, **no per-entry uncompressed byte cap**, and **no total uncompressed byte cap**. `normalizeZipPath(String)` only stripped leading `/` and normalized `\\` -> `/`, without rejecting `..` path traversal segments or NUL/control characters. A crafted EPUB (zip bomb or traversal archive) could crash the process with `OutOfMemoryError` or propagate unsafe entry names.
- **Fix Applied (`Task 8`)**:
  - Added `isSafeZipEntryName(String)` rejecting NUL/control chars (`< 0x20` / `0x7F`), `%2e%2e`, and `..` path segments; enforced in both `transcode` and `normalizeZipPath`.
  - Added hard streaming decompression limits in `transcode`: `MAX_ZIP_ENTRIES = 10000`, `MAX_ZIP_ENTRY_BYTES = 32 MB`, and `MAX_ZIP_TOTAL_BYTES = 150 MB`.

### P0-2: `FolioMetadataBackup.java` — Unbounded Restore Read & Unvalidated `kfx_path` / `cover_path` / Field Bounds (`lines 94–200`)
- **Failure Mode**: `restoreFromJson` loaded the entire JSON file into `StringBuilder` with no byte cap (`OutOfMemoryError` risk), did not validate schema version bounds, trusted arbitrary `kfx_path`/`cover_path` values (allowing `..`, relative paths, or paths inside `/databases/` or `/shared_prefs/`) into `FolioMetadataDb.upsertBook` and `SharedPreferences`, and applied uncapped `reading_goal_target` / `star_rating` / string lengths.
- **Fix Applied (`Task 8`)**:
  - Enforced `MAX_BACKUP_FILE_BYTES = 16 MB` (both pre-read file length and streaming char counter) and `MAX_BACKUP_BOOKS = 10000`.
  - Added `isValidBackupBookKey`, `isValidBackupBookPath`, `isValidBackupCoverPath`, `clampReadingGoal` (`1..1000`), `clampStarRating` (`1..5`), and per-field length bounds (`MAX_TEXT_FIELD_LEN = 2000`, `MAX_DESC_FIELD_LEN = 4000`, plus `normalizePubDate` / `normalizeIsbn`).

---

## P1 (High — WebView TLS / File Access, Filename Sanitization, OPDS Cap & Bitmap Heap)

### P1-1: `FolioWebBrowserDialog.java` — Explicit `onReceivedSslError` Cancel Guard, File/Content Access Lockdown, `Content-Disposition` Sanitization & Scheme Check (`lines 579–660`, `1024–1100`)
- **Failure Mode**:
  - `WebViewClient` did not override `onReceivedSslError` explicitly with `handler.cancel()`.
  - `WebSettings` enabled JS without explicitly calling `ws.setAllowFileAccess(false)` and `ws.setAllowContentAccess(false)`.
  - `downloadAndIngestEpub` only stripped punctuation from `URLUtil.guessFileName`, leaving `..` sequences uncapped/unstripped and without enforcing canonical containment inside `cacheDir/folio_web_dl`, and without explicitly blocking non-`http(s)` download URLs or HTML-attribute quote escaping (`'`, `"`, `&`) in `onReceivedError`.
- **Audit Note (`addJavascriptInterface`)**: Full codebase grep confirms **0** `addJavascriptInterface` calls anywhere in `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
- **Fix Applied (`Task 8`)**:
  - Added explicit `onReceivedSslError` overriding to ALWAYS call `handler.cancel()` (never `handler.proceed()`), log a warning, and toast `"Blocked insecure TLS certificate"`.
  - Set `ws.setAllowFileAccess(false)` and `ws.setAllowContentAccess(false)`.
  - Added pure `sanitizeDownloadFilename(contentDisposition, urlOrFallback)` stripping NUL/control chars, directory separators, `..`, normalizing to `[a-zA-Z0-9._-]`, enforcing `.epub` extension, and capping length `<= 80` chars; enforced canonical destination containment under `folio_web_dl/` and `http://`/`https://` URL + redirect validation.
  - Added `escapeHtmlAttr` for `onReceivedError` error template.

### P1-2: `OpenCatalogView.java` — Missing `250 MB` Streaming Cap on OPDS/Gutendex Download (`lines 1420–1456`)
- **Failure Mode**: `downloadAndOpen` streamed HTTP responses directly into `tmpFile` (`while ((n = is.read(buf)) > 0)`) without checking total bytes downloaded against `250 MB`, risking storage exhaustion on oversized/malicious OPDS feeds.
- **Fix Applied (`Task 8`)**: Enforced `maxDownloadBytes = 250L * 1024L * 1024L` inside the download loop; aborts immediately, deletes `tmpFile`, and reports `"OPDS download exceeds 250 MB limit"`.

### P1-3: `EpubTranscoder.java` — Unsampled Full-Resolution Image Decoding in Fallback Transcoder (`ensureMobiCompatibleImage`, `lines 1388–1416`)
- **Failure Mode**: `BitmapFactory.decodeByteArray(raw, 0, raw.length)` decoded multi-megapixel images at full `ARGB_8888` resolution before downscaling, risking OOM on large EPUB images during fallback KF8/MOBI transcoding.
- **Fix Applied (`Task 8`)**: Added two-pass `inJustDecodeBounds` + power-of-2 `inSampleSize` (target max `1080x1600` for cover, `1440x2160` for inline images) and `RGB_565` decode preference prior to JPEG re-encoding.

---

## P2 (Medium — Observed, Deferred / Non-Blocking)

1. **`OpenCatalogView.java` (`lines 1472–1492`)**:
   - Retains the legacy fallback path calling `registerDownloadedBook` after `AzathLibrarySync.ingestEpub` (also carries a `StrictMode` reflection call at line `1482`). Since `AzathLibrarySync.ingestEpub` already handles conversion + `FolioMetadataDb` + `kindle_library.db` registration, `downloadAndOpen` should rely cleanly on `AzathLibrarySync.ingestEpub` and `ExternalEpubActivity.launchKfxReader` in a follow-up cleanup.
2. **`SafeHttp.java` (`lines 71–84`, `86–120`)**:
   - `getBytes(String)` reads responses into `ByteArrayOutputStream` without a byte cap (currently used for thumbnails/JSON covers, while OPDS downloads go through `openStream` which is now capped in `OpenCatalogView`), and `openStream` relies on caller-closed stream (`HttpURLConnection` reference not explicitly disconnected on stream close).
3. **`CoverOverlayView.java` (`line 45`)**:
   - `BitmapFactory.decodeFile(...)` decodes the standalone `.cover.jpg` sidecar without `inJustDecodeBounds`/`inSampleSize` sampling (`libkfxconv.so` normalizes covers, so risk is bounded on converted books, but sampling should be added defensively in a follow-up).
4. **`EpubTranscoder.java` (`transcode`) Memory Architecture**:
   - Pure-Java KF8/MOBI fallback still buffers HTML/CSS/image entries in `zipEntries` in memory rather than streaming spine items to temp files; now bounded safely by the `150 MB` total / `32 MB` per-entry limits, and primary ingestion uses native Rust `libkfxconv.so`.

---

## Verification Summary (`Task 8`)
- Verified via `compile_mod_dex.sh` (`d8` DEX build) and pure-JVM unit tests in `AzathUnitTests.java` (Task 10 adds dedicated assertions for `EpubTranscoder.isSafeZipEntryName`, `FolioWebBrowserDialog.sanitizeDownloadFilename`, and `FolioMetadataBackup` path/bounds validators).
