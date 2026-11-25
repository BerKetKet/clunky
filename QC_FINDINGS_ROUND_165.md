# Round 165 Unreviewed File Audit (`QC_FINDINGS_ROUND_165.md`)

Audited files in `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
1. `com/amazon/kindle/epub/EpubTranscoder.java`
2. `com/amazon/kindle/epub/ingest/FolioWebBrowserDialog.java`
3. `com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
4. `com/amazon/kindle/epub/catalog/OpenCatalogView.java`
5. `com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
6. `com/amazon/kindle/epub/catalog/FolioMoreView.java`
7. `com/amazon/kindle/epub/CoverOverlayView.java`

---

## P0 (Critical — Data Integrity / Pipeline Bypass)
1. **`FolioWebBrowserDialog.java` (`lines 1129–1159`) — Bypasses `AzathLibrarySync.ingestEpub(...)` Canonical Pipeline**
   - **Failure Mode**: Web Book Browser downloads performed inline EPUB->KFX conversion and DB registration using legacy `FolioMetadataDb.computeBookKey(meta.title, dlFile.length())`, synchronous `MetadataEnricher.enrichInPlace(meta)` (blocking the download thread), `sdKindle.exists() && sdKindle.canWrite()` (missing `isDirectory()`), and `db.upsertBook(...)` without `content_hash` or `source_size`.
   - **Fix Applied (`G-P0P1`)**: Routed Web Book Browser ingest through `AzathLibrarySync.ingestEpub(ctx, dlFile, safeName, true)` so SHA-256 `content_hash`, `source_size`, `SYNC_LOCK`, non-blocking `MetadataEnricher.enqueueAsync`, and `isDirectory()` fallback are enforced uniformly.

---

## P1 (High — Resource Leaks & Unbounded Download)
1. **`FolioWebBrowserDialog.java` (`lines 1054–1121`) — `HttpURLConnection`, `InputStream`, and `FileOutputStream` Not Closed in `finally` + Unbounded Download Size**
   - **Failure Mode**: If an `IOException` or validation failure occurred mid-stream in `in.read(buf)` / `fos.write(...)`, `in`, `fos`, and `conn` leaked and partial `dlFile` remained in `cacheDir/web_epubs/`. Additionally, no max download cap (`250 MB`) was enforced while streaming.
   - **Fix Applied (`G-P0P1`)**: Closed `in`, `fos`, and `conn.disconnect()` in `finally`, enforced `250 MB` (`250L * 1024L * 1024L`) streaming cap, and deleted `dlFile` on failure.

---

## P2 (Medium — Recommendations Only, Not Modified in Round 165)
1. **`FolioWebBrowserDialog.java` (`lines 580–586`)**: `WebSettings.setJavaScriptEnabled(true)` and `MIXED_CONTENT_COMPATIBILITY_MODE` are enabled without explicitly setting `ws.setAllowFileAccess(false)` and `ws.setAllowContentAccess(false)`. Recommend disabling `file://` and `content://` access in the WebView.
2. **`FolioWebBrowserDialog.java` (`lines 1105, 1123`)**: `mainHandler.post(...)` callbacks update `statusTv` without checking `FolioTheme.isContextDead(ctx)` / dialog dismissal.
3. **`CoverOverlayView.java` (`line 45`)**: `BitmapFactory.decodeFile(...)` decodes the cover image at full resolution without `inJustDecodeBounds` / `inSampleSize` capping, which could trigger `OutOfMemoryError` on malformed or ultra-high-resolution (>4K) cover images.

---

## P3 (Low — Recommendations Only, Not Modified in Round 165)
1. **`FolioMoreView.java` (`lines 443+`)**: `readQuickStats(ctx)` queries `FolioMetadataDb` synchronously on the UI thread when building Control Center. Recommend moving initial stats load to a background executor.
2. **`OpenCatalogView.java` (`lines 1418–1450`)**: OPDS download uses `tmpFile` (`.part`) -> `renameTo` with `validZip` magic check cleanly, but lacks an explicit `250 MB` streaming byte cap (`MAX_EPUB_BYTES`).
3. **`EpubTranscoder.java` (`lines 522–534, 648–657`) & `FolioMetadataBackup.java`**: Both properly close `FileOutputStream` in `finally` and use `.tmp -> renameTo` atomic writes (hardened in `E6`).
