# Azath (`com.azath.reader`)

Offline-first, Dual Stone Edition EPUB-to-KFX reader and library manager built on Kindle Android (`v8.159.0.100`).

## Architecture Overview
- **Canonical Mod Source**: `07_APK_EPUB_INGESTION_MOD/mod_src/real/`
- **EPUB -> KFX Pipeline**:
  1. EPUBs imported via Storage (`FolioFolderBrowserDialog`), Web Browser (`FolioWebBrowserDialog`), OPDS (`OpenCatalogView`), or External Intent (`ExternalEpubActivity`) are converted via `EpubTranscoder` / native `libkfxconv.so` using atomic `.kfx.part` -> `.kfx` writes into `/sdcard/kindle/` (or `files/folio_library/` fallback).
  2. Metadata is extracted locally (`EpubMetadataExtractor`), persisted transactionally in `folio_metadata.db` (`DB_VERSION = 2` with `content_hash`, `source_size`, `genres_json`), and optionally enriched asynchronously (`MetadataEnricher`) when `online_enrichment_enabled` is `true`.
  3. `AzathLibrarySync` reconciles `/sdcard/kindle/`, `folio_metadata.db`, `kindle_library.db`, and `files/epub_cache/` with scan-health guards, a delete circuit breaker, and `ReentrantLock SYNC_LOCK`.
- **Dual Stone Edition UI**:
  - **Granite (Dark Mode)** (`#0E0F11` / `#1C1E22` / `#D4C9B8`) and **Limestone (Light Mode)** (`#F4F0E8` / `#E7E1D5` / `#2D3138`), hot-swappable without `Activity.recreate()` or `Process.killProcess`.
  - Typography: `EB Garamond` display headings + `Inter` UI controls.
- **Cover Architecture Directive**:
  > When parsing an EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is processed separately in its own class/structure and inserted when displaying the book separately (`CoverOverlayView`) rather than as a reflowable spine page.

## Build & Deploy
- **Fast Rebuild & Deploy** (requires `/tmp/kindle_clone_build/base_dec/` unpacked from base APK and connected target device):
  ```bash
  bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh
  ```
- **Compile Mod DEX (`classes9.dex` -> `classes4.dex` inside APK)**:
  ```bash
  bash 07_APK_EPUB_INGESTION_MOD/tools/compile_mod_dex.sh 07_APK_EPUB_INGESTION_MOD/dex/classes9.dex
  ```
- **Pure-JVM Unit Tests**:
  ```bash
  ./scripts/run_unit_tests.sh
  ```

## Hardware & Safety Invariants
- **Target Device**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Stock Kindle Isolation**: `com.amazon.kindle` (`v8.160.1.0`) MUST remain **100% untouched and read-only**.
- **No `ct.reader`**: Play Store Folio must never be launched, inspected, or referenced.

## Submodule / Gitlink Notes (`kfx_converter`)
- `git ls-tree HEAD kfx_converter`: `160000 commit 61f664c4207ba2eef9d58037f5f4c92dba52230e kfx_converter`.
- No `.gitmodules` file exists (`git submodule status` reports `fatal: no submodule mapping found in .gitmodules for path 'kfx_converter'`), so `kfx_converter` is a dangling gitlink with no configured remote URL.

