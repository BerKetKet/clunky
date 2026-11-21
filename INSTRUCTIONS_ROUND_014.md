# Round 014 Instructions — EPUB3 `nav.xhtml` TOC + Intra-File `#anchor` KF8 Links + OPDS Presets & Backup/Restore UI

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report014.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Changes Applied in Round 014

1. **`EpubTranscoder.java`**:
   - **EPUB3 `nav.xhtml` TOC Fallback**: Parses `<item properties="nav">` and `<nav epub:type="toc">` `<a href="...">` entries whenever `toc.ncx` is absent or empty, so EPUB3-only books get full native multi-chapter TOC drawers too.
   - **Intra-file `#anchor` KF8 Links (`kindle:pos:fid:XXXX:off:YYYYYYYYYY`)**: Rewrites same-file `<a href="#id">` and `<a href="file.xhtml#id">` links (footnotes, endnotes, in-book TOC links) using exact UTF-8 byte offsets encoded via `toBase32Ten()`.
2. **`OpenCatalogView.java`**:
   - **OPDS Quick Presets**: Upgraded the `+ OPDS Feed` dialog with one-tap preset buttons (**Feedbooks Public Domain**, **Project Gutenberg OPDS**, **Internet Archive OPDS**) plus custom URL input.
   - **Backup & Restore Dialog**: Tapping **Backup** now opens a clean dialog offering **Backup Now** and **Restore Latest** for `kindle_library.db` & `annotations.db` in `/sdcard/kindle/backups/`.

---

## Tasks

### Task A — Build & Deploy Round 014
1. Copy `EpubTranscoder.java` and `OpenCatalogView.java` into your project's `mod_src/real/com/amazon/kindle/epub/`.
2. Run `clone_and_deploy_epub_mod.sh` to compile & install **Folio** (`com.amazon.kindle.epub`).
3. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Verify Upgraded Home Catalog Dialogs (OPDS Presets + Backup/Restore)
1. Open **Folio** (`com.amazon.kindle.epub`) on the **Home** tab.
2. Tap **`+ OPDS Feed`** (or long-press `OPDS Feed` if already configured) to open the **OPDS Catalog Feed** dialog with Quick Presets. Capture **`screenshots/verify_r14_opds_presets.png`**.
3. Dismiss and tap **`Backup`** to open the **Folio Library & Notes Backup** dialog (`Backup Now` / `Restore Latest`). Capture **`screenshots/verify_r14_backup_restore.png`**, then tap **Backup Now** to verify a fresh timestamped backup is written.

### Task C — Verify Intra-File `#anchor` Link & Regression Check + Save Clean Release Bundle
1. Re-transcode and open `/sdcard/kindle/Moby_Dick__Or__The_Whale_2701.epub` or `/sdcard/kindle/kindle_exam_v3.epub` and verify zero regressions in KF8 rendering & native TOC drawer (`screenshots/verify_r14_kf8_regression.png`).
2. Save the signed Folio APK(s) cleanly into `<MOD_ROOT>/release/folio_v1.0/` and log their exact filenames and sizes in `REPORT.md`.

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report014.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
