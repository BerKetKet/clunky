# Round 029 Instructions — Clean Offline "More" Tab & Non-Latin/Footnote/List KFX QC (Folio v2.6)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI improvements must be 100% general — never hardcode book titles or specific filenames.  
**Reply Archive Budget:** Package ONLY Round 029 files (`REPORT.md`, `INSTRUCTIONS_ROUND_029.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report029.zip` (< 15 MB) and verify the `.zip` preview chip before sending.

---

## Round 028 Visual QC Feedback

1. **PASS — Prose Image Aspect-Ratio Fix (`verify_r28_antigone_ornament_fixed.png`, `verify_r28_images_exam_aspect.png`)**: Great root-cause diagnosis on GIF decoding/transcoding + `$422/$423` + proportional column styles. The horizontal ornament under `ANTIGONE` renders crisply at its true proportion, and `900x150`, `2400x1600`, and `24x24` all preserve exact geometry on a single page.
2. **PASS — Default Spacing & Dark Mode Prose (`verify_r28_sophocles_contents_default_spacing.png`, `verify_r28_antigone_ornament_fixed.png`)**: Balanced Contents spacing, single-line blue hyperlinks, justified Bookerly prose with native Amazon hyphenation, and pure `#000000` AMOLED background.
3. **PASS — Library Search Branding (`verify_r28_library_branding.png`)**: `"Search Folio"` placeholder cleanly replaces `"Search Kindle"`.
4. **COVER ARCHITECTURE NOTE**: Keep the current Round 026–028 standalone `$270` KFX cover container (which already strips cover HTML from the spine at ingestion and renders edge-to-edge on `#000000` black with zero margins and zero `"0%"` footer).

---

## Task A: Repurpose Folio "More" Tab into a Clean Offline Control Center

Eliminate the dead Amazon account/store items audited in Round 028 (`Sign In with Amazon`, `Sync now`, Amazon support `Help`/`Info`) inside `com.amazon.kindle.epub`:

1. **Replace/Suppress Amazon Stubs**:
   - Hide or replace `"Sign In with Amazon"` so users are never prompted for Amazon credentials.
   - Repurpose the **More** tab into a clean, dark-themed Folio menu (matching Home/Library styling) with working offline actions:
     * **OPDS Catalogs** → opens Folio's existing OPDS catalog/feed manager.
     * **Scan Local Library** (replacing `"Sync now"`) → rescans local EPUB/KFX directories and shows a confirmation toast with book count.
     * **Backup & Restore** → launches Folio's existing SQLite backup/restore dialog (`/sdcard/kindle/backups/`).
     * **Settings** → retains native Kindle reader preferences (dark mode, reading options) without triggering Amazon sign-in prompts.
     * **About Folio** (replacing `"Help"`/`"Info"`) → opens a clean offline dialog displaying `Folio v2.6 KFX`, pure-Rust `libkfxconv.so` engine summary, and live local library / cache stats (total books + disk cache size).

---

## Task B: Pure-Rust `libkfxconv.so` Audit — Polytonic Greek, Footnotes & Lists/Blockquotes

1. **Polytonic Greek & Non-Latin Script Audit**:
   - Navigate to `The Greek Texts` in Sophocles (or another polytonic Greek / non-Latin EPUB in `/sdcard/epub/`) and verify whether `KRIFDocViewer` renders all Greek letters and diacritics crisply with zero tofu (`□`) glyphs. If any Unicode normalization, language tag (`<dc:language>`), or font-fallback fix is needed in `libkfxconv.so`, implement it and add a Rust unit test.
2. **Footnotes, Superscripts, Lists & Blockquotes Audit**:
   - Inspect how `libkfxconv.so` emits `<sup>`/`<sub>`, footnote links (`epub:type="noteref"` / `<a href="#fn...">`), `<ul>`/`<ol>`/`<li>` lists, and `<blockquote>` blocks (comparing against `jloutsch/kfxgen` and read-only stock Kindle KFX if any visual defects appear).
   - Fix any alignment, indentation, bullet/numbering, or link-return defects found, run `cargo test --all-targets`, cross-compile `libkfxconv.so`, and build/deploy **Folio v2.6**.

---

## Task C: On-Device Visual Verification (Folio v2.6)

Capture and verify on physical Galaxy M32:

1. `screenshots/verify_r29_more_tab_clean.png`: Redesigned Folio **More** tab with zero `"Sign In with Amazon"` / dead Amazon stubs, showing clean offline Folio items.
2. `screenshots/verify_r29_about_folio_dialog.png`: Tapping **About Folio** displaying version (`Folio v2.6 KFX`), engine info, and live local library/cache stats.
3. `screenshots/verify_r29_greek_script_render.png`: Sophocles `The Greek Texts` rendered in `KRIFDocViewer` proving crisp polytonic Greek glyphs with zero tofu boxes.
4. `screenshots/verify_r29_footnotes_lists_blockquotes.png`: A real page with footnotes/superscripts, lists, or blockquotes from a second distinct book (`kindle_exam_v3.epub` or Hesiod) proving clean indentation and typography.
