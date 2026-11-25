# Round 161 Report — Azath v18.0 KFX (2.9 GB Background EPUB Auto-Crawl Stopped & Reclaimed)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v18.0 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 18:19:36`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`). `/sdcard/kindle/` and `/sdcard/en/0/` untouched; zero `ct.reader` references.

## 2. Task A: Verbatim Drop-In Build, Deploy, Purge & GitHub Push (`Azath v18.0 KFX`)
- Copied Round 161 `dp/g.java` (`shouldBlockAutoEpubCrawl` + `.epub` removed from background sideload enumerator) and `FolioMoreView.java` (`v18.0 KFX`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v18.0_kfx`.
- Built `classes9.dex` (`493,524` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.0_kfx/base.apk` (`138,852,455` bytes, SHA256 `80ba7bda7ec795c2547fc05bbdf875d3bfe40cb127a3e7cbdd0af62049cc5ee4`).
- Force-stopped `com.azath.reader` and purged ONLY `/data/user/0/com.azath.reader/files/epub_cache/*`.

## 3. Task B: Verification (`2.9 GB -> 30 MB`, Zero Re-Crawl)
- Launched `com.azath.reader`, opened **Library** (`verify_r161_library.png`) and **Control Center (`v18.0 KFX`)** (`verify_r161_control_center_v18.png`), and waited **15 seconds**:
```text
30M	/data/user/0/com.azath.reader
4.0K	/data/user/0/com.azath.reader/files/epub_cache
4.3M	/data/user/0/com.azath.reader/files/folio_library
```
- `epub_cache` remained **4.0K (0 files)** with zero background `.epub` re-crawl.
- All `/sdcard/kindle/` books remain intact in `Library`, and `logcat` shows **zero `FATAL EXCEPTION` and zero `SIGSYS`**.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
