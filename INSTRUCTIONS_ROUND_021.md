# Round 021 Instructions — Persistent Metadata/Cover Disk Cache + `/sdcard/epub/` Side-by-Side Kindle Parity Suite + Stock Send-to-Kindle KFX Inspection & Local KFX Proof-of-Concept

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (you may launch/view/screenshot books in stock Kindle for visual comparison and read-only `ls`/`head` inspection, NEVER write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report021.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Project Lead Directives for Round 021

1. **Project Gutenberg Maintenance**: `gutenberg.org` is currently down for maintenance—do NOT fail or stress over Gutenberg 503 responses. Rely on cached responses, Open Library, OPDS, and local `/sdcard/epub/` books.
2. **Persistent Metadata & Cover Cache (`DiskCache.java`)**: Cache Open Library, Gutendex, OPDS responses, and remote cover bitmaps on disk (+ memory `LruCache`) with TTL and stale-on-error fallback to avoid repeated network fetches and slowdowns.
3. **`/sdcard/epub/` Side-by-Side Parity Benchmark**: The user has placed real EPUB books in `/sdcard/epub/` and uploaded them to their stock Kindle too. Compare **every single book in `/sdcard/epub/`** side-by-side between **stock Kindle** (read-only viewing) and **Folio**—**identical appearance** is the goal!
4. **Local KFX (Send-to-Kindle Server Parity) Investigation**: Inspect (read-only) what stock Kindle downloaded from Amazon's Send-to-Kindle servers (KFX `CONT` / Ion fragments) and test whether Folio's native `KRIFDocViewer` can open a **local unencrypted `.kfx` file** directly!

---

## Tasks

### Task A — Deploy Persistent `DiskCache` (`DiskCache.java` + `SafeHttp.java` + `OpenCatalogView.java`)
1. Copy `DiskCache.java` (new!), `SafeHttp.java`, and `OpenCatalogView.java` into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Also check `OpenCatalogView.java`'s `showBookDetails` and remove any synthetic fallback placeholder text (`First Published: 1900`, `4.00 / 5 (1200 ratings)`) if present—always show real fetched/cached Open Library data!
3. Compile, rebuild, and deploy **Folio** (`com.amazon.kindle.epub`). Confirm stock `com.amazon.kindle` is 100% untouched.
4. Open Folio's **Home** tab, let Open Library recommendations & covers populate, then verify files were cached on disk under `/data/data/com.amazon.kindle.epub/cache/folio_meta_cache/` and `folio_cover_cache/` (log exact file counts & byte sizes in `logs/disk_cache_verify.txt`).

### Task B — `/sdcard/epub/` Side-by-Side Visual Parity Suite (Folio vs Stock Kindle Read-Only)
1. List all `.epub` files in `/sdcard/epub/` (`ls -la /sdcard/epub/`).
2. For **EACH** EPUB in `/sdcard/epub/` (up to all books present, keeping report zip `< 100 MB` by resizing screenshots to ~540px wide if > 10 screenshots):
   - **In Folio (`com.amazon.kindle.epub`)**: Open the EPUB directly (`ThirdPartyUpgradePage`), and capture:
     * `screenshots/parity/<slug>_folio_cover.png` (Cover presentation)
     * `screenshots/parity/<slug>_folio_prose.png` (Chapter 1 / first prose page under default theme)
     * `screenshots/parity/<slug>_folio_toc.png` (Native slide-out TOC drawer)
   - **In Stock Kindle (`com.amazon.kindle`, STRICTLY READ-ONLY — view & screenshot only!)**: If the matching book is already synced/downloaded in stock Kindle's Library, open it to the matching Cover and Chapter 1 prose page and capture:
     * `screenshots/parity/<slug>_stock_cover.png`
     * `screenshots/parity/<slug>_stock_prose.png`
3. Write a detailed per-book visual diff matrix in **`logs/epub_parity_matrix.md`** listing every observable difference between Stock Kindle and Folio (margins, line spacing, paragraph spacing, hyphenation, font weight/size, cover crop/margins, headings, images, tables, TOC entries).

### Task C — Stock Send-to-Kindle KFX Inspection & Local KFX Proof-of-Concept in Folio
1. **Read-Only Inspection of Stock Kindle's Downloaded Send-to-Kindle Files**:
   - Inspect `/sdcard/Android/data/com.amazon.kindle/files/` (and `/data/data/com.amazon.kindle/files/` read-only via `ls` + `xxd -l 64`) to document the exact files stock Kindle received from Amazon's Send-to-Kindle server for uploaded EPUBs (e.g., `.kfx` / `.azw` / `.res` / `.md` / `CONT` Ion header). Save findings to **`logs/stock_stk_kfx_inspection.txt`**.
2. **Local KFX Proof-of-Concept in Folio (`KRIFDocViewer`)**:
   - Check in `classes*.dex` how `KRIFDocViewer` / `KRF4ContentModule` / `ThirdPartyUpgradePage` opens `.kfx` files locally (which extension/MIME/directory/DB columns route a local unencrypted `.kfx` file into `KRIFDocViewer` instead of `MobiDocViewer`).
   - If you can generate a tiny unencrypted test `.kfx` on the PC (e.g. via Calibre KFX Output or Rust `kfx_converter` if installed, or check what's needed), test opening it in **Folio** (`com.amazon.kindle.epub`) and log whether `KRIFDocViewer` opens it natively with Enhanced Typesetting, hyphenation, live `Aa → Font → Spacing`, and Continuous Scrolling! Save full findings to **`logs/local_kfx_poc.txt`**.

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report021.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
