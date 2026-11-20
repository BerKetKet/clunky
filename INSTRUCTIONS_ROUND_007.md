# Gemini -> Antigravity Relay: Round 007 (`to_antig_clunky_round_007.zip`)

**Incoming:** `to_antig_clunky_round_007.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report007.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Never modify, write, force-stop, or clear stock `com.amazon.kindle`.  
**Autonomous Mode:** Continue autonomously via Chrome Dev Bridge without pausing unless a fatal blocker/auth prompt occurs.

---

## 1. Round 007 Objectives
1. **In-App DoH DNS Fallback (`SafeHttp.java`)**:
   - Resolves hostnames via Cloudflare `1.1.1.1/dns-query` and Google `8.8.8.8/resolve` directly by IP with TLS SNI whenever Android's system DNS fails (`UnknownHostException`).
   - Wired into `GutendexSource`, `OpdsSource`, `OpenLibraryClient`, and `OpenCatalogView` (including cover thumbnails and `.epub` downloads across redirects!).
2. **Binary `INDX` (NCX) Table of Contents Drawer (`EpubTranscoder.java`)**:
   - Parses `toc.ncx` / headings and emits PalmDB Master `INDX` + Secondary `INDX` + `CNCX` records referenced by MOBI header offset `244` (`ncx_index`), enabling Kindle's native slide-out **Go To → Table of Contents** drawer!
3. **KF8 (`file_version = 8`, `libWebCoreKRF.so`) Support (`EpubTranscoder.java`)**:
   - Emits `header_length = 264`, `FDST` (HTML Flow 0 + CSS Flow 1), `SKEL`, `FRAG`, `NCX`, and `kindle:embed:` image URIs when `/sdcard/kindle/.use_kf8` is present (keeping MOBI7 as the safe default during A/B testing).

---

## 2. Round 007 Tasks

### Task A — Deploy & Verify `SafeHttp` DoH DNS Fallback (WITHOUT `/etc/hosts`!)
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` and `mod_src/real/com/amazon/kindle/epub/catalog/{SafeHttp,GutendexSource,OpenCatalogView}.java` into `07_APK_EPUB_INGESTION_MOD/`.
2. Rebuild and install `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. **Unmount any temporary `/etc/hosts` bind-mount** so the phone uses its real network state, then launch `com.amazon.kindle.epub`:
   - Verify the **Home** tab loads live Project Gutenberg results via `SafeHttp` DoH fallback.
   - Tap *Pride and Prejudice* and verify BOTH the **cover thumbnail from `www.gutenberg.org`** AND the **Open Library rating/synopsis** render inside the Book Details dialog!
   - Capture `screenshots/verify_doh_home_and_cover_dialog.png`.

### Task B — Verify Native "Go To → Table of Contents" Drawer (Binary `INDX` NCX in MOBI7)
1. Ensure `/sdcard/kindle/.use_kf8` does NOT exist yet, clear `/data/data/com.amazon.kindle.epub/files/epub_cache/kindle_exam_v3.epub.prc`, and open `file:///sdcard/kindle/kindle_exam_v3.epub` in `com.amazon.kindle.epub`.
2. Tap center of screen to show reader chrome, then tap the **Table of Contents (hamburger/list)** icon in the top-left toolbar to open Kindle's native slide-out TOC drawer!
3. Capture `screenshots/verify_native_toc_drawer.png` showing the populated chapter list (`E01 The Ferryman's Almanac`, `E02 Dialogue and Quotation`, ...), and tap a chapter (e.g. `E06` or `E09`) to confirm native chapter jumping works!

### Task C — A/B Test KF8 (`libWebCoreKRF.so` CSS3 Engine)
1. Enable KF8 mode: `adb shell "touch /sdcard/kindle/.use_kf8"` and clear `epub_cache/*.azw3`.
2. Open `file:///sdcard/kindle/kindle_tables_images_exam.epub` and `file:///sdcard/kindle/kindle_exam_v3.epub` in `com.amazon.kindle.epub`.
3. Check `logcat` for `KindleDocumentBuilderWebCore` / `libWebCoreKRF` / `EBookPDB` / `MobiDocViewer`!
   - If KF8 opens and renders CSS3 + tables + images cleanly, capture `screenshots/verify_kf8_rendering.png`!
   - If `libWebCoreKRF.so` logs any `SKEL`/`FRAG`/`FDST` rejection, capture the exact logcat error lines in `logs/logcat_kf8_test.txt` and remove `/sdcard/kindle/.use_kf8` so the phone stays on the rock-solid MOBI7+NCX mode while we refine KF8 in Round 008.

---

## 3. Package & Relay `~/projects/clunky/antig_to_gemini_neon_clunky_report007.zip` (`< 100 MB`)
Include `REPORT.md`, updated `scripts/`, `screenshots/` (`verify_doh_home_and_cover_dialog.png`, `verify_native_toc_drawer.png`, `verify_kf8_rendering.png` if KF8 renders), and `logs/` (`logcat_round007.txt`, `logcat_kf8_test.txt`).
Relay back via Chrome Dev Bridge immediately upon completion!
