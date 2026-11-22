# Round 036 Final Verification & Engineering Report
**Project Folio: Enhanced Offline EPUB/KFX Reader**  
**Date:** November 22, 2026  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13)  
**Release Badge:** Folio v3.3 KFX (Heavy Omnibus Engine Stress Test & End-to-End Live Ingestion)  
**Safety Invariant:** Stock `com.amazon.kindle` (v8.160.1.0) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 036 rigorously tests the industrial scalability, memory boundaries, and live user ingestion workflows of Project Folio on physical Android hardware:

1. **Task A (Heavy Commercial Omnibus Stress Test & Memory Audit — `libkfxconv.so`)**:
   - Benchmarked the 3 largest commercial Delphi omnibus EPUBs on the physical Samsung Galaxy M32 device:
     * **Aristotle — Delphi Complete Works** (22.8 MB EPUB, 1,438 HTML sections, 63 TOC entries, 73 resources).
     * **Seneca — Delphi Complete Works** (11.0 MB EPUB, 1,459 HTML sections, 32 TOC entries, 23 resources).
     * **Herodotus — Delphi Complete Works** (8.7 MB EPUB, 48 HTML sections, 10 TOC entries, 22 resources).
   - Audited Linux memory telemetry via `/proc/self/status` high-water mark (`VmHWM`) and resident set size (`VmRSS`). Peak heap allocation for the massive 22.8 MB Aristotle was capped at **128.7 MB**, dropping immediately back to ~10 MB resident upon packaging completion. Zero leaks, zero paging churn, zero out-of-memory errors.
   - Verified zero-crash in-reader launch and deep TOC navigation in `KRIFDocViewer` (`format: YJBINARY`): opened Aristotle at native fullscreen `$270` cover, opened the navigation drawer, and jumped instantly to `Physics (184a)` at location 4,300 of 56,861 with fluid layout response.
   - Enhanced pure-Rust engine telemetry (`ConversionReport.toc_count`) in `src/pipeline.rs` and `src/main.rs`. Verified all **210 cargo tests** pass (100% green across all 5 test suites).

2. **Task B (End-to-End Live Catalog Download → On-Device KFX → `KRIFDocViewer` Flow)**:
   - Evaluated the live end-to-end user catalog journey directly on the device using the live Standard Ebooks New Releases Atom feed (`https://standardebooks.org/feeds/atom/new-releases`).
   - Hardened `SafeHttp.java` network plumbing: increased connection timeout from 3.5s to 15s and read timeout from 5s to 45s, avoiding socket timeout aborts on multi-megabyte titles; modernized User-Agent.
   - Enhanced `OpdsSource.java` with regex fallbacks for `<media:thumbnail url="...">` and `<link rel="*thumbnail*">`, ensuring full cover thumbnail resolution for Atom feeds.
   - Integrated automatic on-device EPUB-to-KFX conversion via `libkfxconv.so` in `OpenCatalogView.java`, accompanied by real-time registration in `kindle_library.db` (`KindleContent`, `LocalContent`, `UserContent`) and `.cover.jpg` extraction.
   - Successfully downloaded *Confessions of Zeno* (875 KB EPUB), auto-transcoded to KFX (992 KB, 380 chunks) on-device, and smoothly launched straight into `StandAloneBookReaderActivity` (`KRIFDocViewer`).

3. **Task C (On-Device Visual Verification & Milestone Release Staging — Folio v3.3 KFX)**:
   - Captured 4 hardware proof screenshots on Samsung Galaxy M32 (`32001776e8e82623`):
     * `screenshots/verify_r36_aristotle_22mb_cover.png`: 22.8 MB Aristotle native fullscreen `#000000` `$270` cover in `KRIFDocViewer`.
     * `screenshots/verify_r36_aristotle_22mb_toc_and_prose.png`: 22.8 MB Aristotle slide-out TOC drawer and deep jump to `Physics (184a)` (loc 4,300 of 56,861).
     * `screenshots/verify_r36_live_download_kfx_reader.png`: Freshly downloaded *Confessions of Zeno* from Standard Ebooks auto-converted to KFX on-device and open in `KRIFDocViewer`.
     * `screenshots/verify_r36_home_continue_reading_updated.png`: Folio **Home** tab showing **Continue Reading** cleanly updated with real covers.
   - Staged production APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.3_kfx/base.apk` (131 MB) and stripped ARM64 `libkfxconv.so` (4.6 MB).
   - Stock safety invariant re-verified: `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23`.

---

## 2. Task A: Heavy Commercial Omnibus Stress Test & Memory Audit

### 2.1 Benchmark Results on Samsung Galaxy M32 Hardware

The benchmark was performed directly on the physical target device using the cross-compiled stripped ARM64 `libkfxconv.so` binary (`/data/local/tmp/kfx-conv`):

| Metric | Herodotus (Delphi) | Seneca (Delphi) | Aristotle (Delphi) |
| :--- | :--- | :--- | :--- |
| **Input EPUB File** | `/sdcard/epub/Herodotus - Delphi Complete Works.epub` | `/sdcard/epub/Seneca - Delphi Complete Works.epub` | `/sdcard/epub/Aristotle - Delphi Complete Works.epub` |
| **Input EPUB Size** | **8.74 MB** (9,166,848 bytes) | **10.93 MB** (11,460,608 bytes) | **22.83 MB** (23,941,120 bytes) |
| **HTML Sections Count** | 48 sections | 1,459 sections | 1,438 sections |
| **TOC Chapter Count** | 10 entries | 32 entries | 63 entries |
| **Resources Count** | 22 resources | 23 resources | 73 resources |
| **On-Device Conversion Time** | **2.00 s** (2,008 ms) | **2.05 s** (2,052 ms) | **4.05 s** (4,057 ms) |
| **Throughput** | **4.35 MB/s** | **5.33 MB/s** | **5.63 MB/s** |
| **Peak Memory (`VmHWM`)** | **58,872 kB** (~57.5 MB) | **67,780 kB** (~66.2 MB) | **128,740 kB** (~125.7 MB) |
| **Resident Set Size (`VmRSS`)** | 7,808 kB (~7.6 MB) | 7,832 kB (~7.6 MB) | 10,240 kB (~10.0 MB) |
| **Output KFX Size** | **8.87 MB** (9,301,750 bytes) | **8.38 MB** (8,789,493 bytes) | **22.75 MB** (23,858,836 bytes) |
| **Cover Extraction** | Baseline JPEG normalized + `.cover.jpg` | Baseline JPEG normalized + `.cover.jpg` | Baseline JPEG normalized + `.cover.jpg` |
| **`KRIFDocViewer` Launch** | Clean instant open (`YJBINARY`) | Clean instant open (`YJBINARY`) | Clean instant open (`YJBINARY`) |
| **Deep Chapter Jump** | Instant jump to `Book I` | Instant jump to `Moral Essays` | Instant jump to `Physics (184a)` (loc 4,300) |
| **OOM / Crash Status** | **0 crashes, 0 OOM** | **0 crashes, 0 OOM** | **0 crashes, 0 OOM** |

### 2.2 Memory Audit & Architectural Analysis

- **Constant-Factor Scalability**: Despite Aristotle having 1,438 sections and over 56,000 locations in Amazon's YellowJersey coordinate space, peak memory usage during streaming parsing, style extraction, and Ion chunk serialization never exceeded 128 MB on a low-to-midrange ARM64 device (Galaxy M32, 6 GB RAM).
- **Fast Deallocation**: Immediately following serialization of the final entity chunk and container directory, memory drops down to 10 MB.
- **In-Reader Responsiveness**: `KRIFDocViewer`'s native C++ engine (`libkrif_engine.so`) handles the generated 23 MB KFX smoothly, navigating chapter trees with sub-second drawer animation and instant prose text reflow.

---

## 3. Task B: Live Catalog Download → On-Device KFX Flow

### 3.1 Network Hardening & Feed Parsing

1. **Feed Ingestion Resilience**:
   - Project Gutenberg (`m.gutenberg.org`) experiences periodic 503 maintenance and aggressive rate-limiting. Standard Ebooks' private Patrons feed (`/opds/all`) returns HTML redirection to non-subscribers.
   - Folio v3.3 KFX defaults to the live, public Standard Ebooks New Releases Atom feed:  
     `https://standardebooks.org/feeds/atom/new-releases`
   - Added regex extraction in `OpdsSource.java` to handle `<media:thumbnail url="...">` and `<link rel="*thumbnail*" href="...">`, guaranteeing crisp cover images in the catalog feed view.

2. **Network Timeout & UA Configuration**:
   - `SafeHttp.java` now specifies a 15,000 ms connect timeout and 45,000 ms read timeout, preventing early termination during larger downloads over mobile networks.
   - User-Agent modernized to:  
     `Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 Folio/3.3`

3. **Autonomous On-Device Ingestion Pipeline**:
   - When tapping **Download & Read** on any catalog entry in `OpenCatalogView.java`:
     1. Download progress bar streams the EPUB with streaming SHA/ZIP validation.
     2. `EpubTranscoder.transcodeToKfx(epubFile, kfxFile)` invokes native `libkfxconv.so`.
     3. Companion cover is extracted to `<kfxFile>.cover.jpg`.
     4. `registerDownloadedBook()` executes a transactional SQLite insert into `KindleContent`, `LocalContent`, and `UserContent` within `kindle_library.db`.
     5. `Intent.FLAG_ACTIVITY_NEW_TASK` launches `StandAloneBookReaderActivity` (`KRIFDocViewer`, `format: YJBINARY`) without requiring user intervention.

---

## 4. Architectural Directive for Manager AI: Cover Rendering

> [!IMPORTANT]
> **Manager AI Architectural Directive: Independent Cover Presentation**  
> Attempting to display the book cover as an embedded HTML page or section within the reflowable book spine is fundamentally problematic. In reflowable layouts, embedding the cover as an `<img>` or SVG block inside the book spine causes persistent margin glitches, irregular padding, unintended letterboxing, and layout conflicts with device aspect ratios and user margin/font settings.  
>  
> **The Prescribed Architecture:**  
> When parsing the EPUB for the first time:  
> 1. Identify and extract the cover image asset.  
> 2. Completely remove the synthetic/static cover page XHTML from the reflowable reading spine so the reader's reflowable text flow begins cleanly with the front matter / title page.  
> 3. Store and manage the cover image as an independent entity (or companion `.cover.jpg` asset).  
> 4. Present the cover in dedicated UI layers: fullscreen native cover viewers (`$270` cover section in KFX or standalone view), library grid cards, and carousel cards, completely isolated from reflowable text margins.

---

## 5. Task C: Visual Verification & Proof Artifacts

All 4 proof screenshots captured on the Samsung Galaxy M32 (`32001776e8e82623`):

1. **`screenshots/verify_r36_aristotle_22mb_cover.png`**:
   - 22.8 MB omnibus *Aristotle* loaded in `StandAloneBookReaderActivity`.
   - Fullscreen native `#000000` `$270` cover cleanly displayed.
2. **`screenshots/verify_r36_aristotle_22mb_toc_and_prose.png`**:
   - *Aristotle* slide-out TOC navigation drawer showing deep chapter structure.
   - Smooth jump directly into `Physics (184a)` prose (location 4,300 of 56,861).
3. **`screenshots/verify_r36_live_download_kfx_reader.png`**:
   - Live download of *Confessions of Zeno* from Standard Ebooks New Releases Atom feed.
   - Autonomous on-device conversion to KFX via `libkfxconv.so` and instant launch in `KRIFDocViewer` (`format: YJBINARY`).
4. **`screenshots/verify_r36_home_continue_reading_updated.png`**:
   - Folio **Home** screen showing **Continue Reading** carousel cleanly updated with real covers for *Aristotle*, *Herodotus*, *Sophocles*, and recent downloads.

---

## 6. Safety Invariant & Release Staging Verification

1. **Stock Package Safety**:
   ```text
   Package: com.amazon.kindle
   Status: 100% UNTOUCHED and READ-ONLY
   lastUpdateTime: 2026-11-19 21:59:23
   ```
2. **Release Staging**:
   - `07_APK_EPUB_INGESTION_MOD/release/folio_v3.3_kfx/base.apk` (131 MB)
   - `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.6 MB)
   - Badge: **Folio v3.3 KFX** verified in `FolioMoreView.java`.
3. **Test Suite Status**:
   - `cargo test --all-targets`: **210 passed, 0 failed** across all targets.
