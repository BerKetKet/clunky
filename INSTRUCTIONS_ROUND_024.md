# Round 024 Instructions — Port `jloutsch/kfxgen` Native Tables, Cover Stripping, `override_kindle_font: false` & Image Fidelity into Rust `libkfxconv.so` (Folio v2.1)

**Target Package:** `com.amazon.kindle.epub` ("Folio v2.1 KFX")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report024.zip` (`< 15 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Important Fix for Chrome Dev Bridge Uploader
Neither `antig_to_gemini_neon_clunky_report022.zip` nor `antig_to_gemini_neon_clunky_report023.zip` actually attached to your messages (the chat received only the text summary). Before clicking Send in `tools/autonomous_relay_runner.mjs`, please verify that the uploaded `.zip` filename chip is visibly attached in the composer DOM (wait for upload progress to finish!) so I can inspect your screenshots and logs directly in Report 024!

---

## Tasks — Port `jloutsch/kfxgen` Reference Improvements into Rust `libkfxconv.so`

Using your cloned `jloutsch/kfxgen/plugin/kfxgen/native_generator.py` strictly as a **specification & code reference** (do NOT bundle Python into the APK!), port the following 4 high-impact KFX improvements directly into our pure-Rust `kfx_converter` (`libkfxconv.so`):

### Task A — Port Native Kindle KFX Tables (`storylines.rs` + `$585` Content Features)
1. Inspect `jloutsch/kfxgen/plugin/kfxgen/native_generator.py` for how it emits native Kindle tables:
   - Table Ion entity types (`$278` table, `$279` table_row, table_cell, column/header styles in `$157`, multi-paragraph cells).
   - Required `$585` (`content_features`) declarations (`yj_table`, `yj_table_viewer`).
2. Implement native table emission in `kfx_converter`'s `src/.../storylines.rs` (replacing the current drop of `<table>` nodes!) so HTML tables in `kindle_tables_images_exam.epub` and commercial EPUBs render as real native Kindle tables (with horizontal pan chevron on wide 6-column tables!) inside `KRIFDocViewer`.

### Task B — Strip Redundant In-Spine Cover HTML + Preserve Native KFX Cover Resource
1. Per the Project Lead's architectural directive: in `kfx_converter`'s EPUB parser, detect and **strip any cover-only HTML spine item** (`cover.xhtml`, `titlepage.xhtml`, `wrap0000.html` containing only an image with minimal text) from the reading spine so the cover is NEVER rendered as a white-margined reflowable HTML page!
2. Keep the high-res cover image cleanly in the dedicated KFX cover resource (`$417` + `$164` + `$495 cover_image`, which `KRIFDocViewer` renders natively on `#000000` black) AND ensure `EpubTranscoder.java` writes `<outKfx>.cover.jpg` for Library/Continue Reading thumbnails.

### Task C — Typography, Language & Image Parity (`override_kindle_font: false`, `<dc:language>`, Transparent PNGs)
1. Ensure `kfx_converter` emits `override_kindle_font: false` (unless the book embeds `@font-face`) so Kindle's selected font (`Bookerly`, `Caecilia`, etc.) and OpenType ligatures are always honored.
2. Ensure the EPUB's `<dc:language>` metadata is propagated cleanly into KFX metadata (`$490` / `$495`) so Amazon's native hyphenation dictionaries and offline Wiktionary activate automatically.
3. Ensure inline & block images (including transparent PNGs composited onto white, like `pic12.png` in `kindle_exam_v3.epub` and cell images in `kindle_tables_images_exam.epub`) are emitted cleanly as `$164`/`$417` resources.

### Task D — Cross-Compile `libkfxconv.so`, Deploy Folio v2.1 & Hardware-Verify
1. Cross-compile updated `kfx_converter` for `aarch64-linux-android` (`lib/arm64-v8a/libkfxconv.so`, stripped), rebuild & deploy **Folio v2.1** (`com.amazon.kindle.epub`), and confirm stock `com.amazon.kindle` is 100% untouched.
2. Clear `epub_cache/` and verify on-device across **multiple distinct books** (zero overfitting!):
   - **`kindle_tables_images_exam.epub`**: Verify native KFX tables (including 6-column table!) AND embedded images render cleanly inside `KRIFDocViewer` (`screenshots/verify_r24_kfx_native_tables.png`, `screenshots/verify_r24_kfx_images.png`).
   - **`kindle_exam_v3.epub`**: Verify `E09 Pagination and Images` (`pic12.png`), native hyphenation, and zero duplicate cover page (`screenshots/verify_r24_exam_v3_images_hyphen.png`).
   - **2 distinct books from `/sdcard/epub/`**: Verify clean native cover (zero white-margined HTML duplicate), crisp prose, and live `Aa → Font → Spacing` before/after (`screenshots/verify_r24_classic_prose_spacing.png`, `screenshots/verify_r24_classic_cover.png`).
3. Stage signed **Folio v2.1** APK in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/`.

### Task E — Package Lean Report (`< 15 MB`) & Verify Attachment Before Sending
Build `antig_to_gemini_neon_clunky_report024.zip` (`< 15 MB`, containing ONLY Round 024 `REPORT.md`, modified `scripts/`/`rust_patches/`, Round 024 `screenshots/`, and Round 024 `logs/`), verify the `.zip` attachment chip is visibly present in the Gemini composer before clicking Send, and upload via Chrome Dev Bridge!
