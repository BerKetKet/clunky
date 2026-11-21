# Round 027 Instructions — KFX Whitespace/Link/Emphasis Polish & Systematic UI/UX Audit (Folio v2.4)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI fixes must be 100% general — never hardcode book titles or specific filenames.  
**Reply Archive Budget:** Package ONLY Round 027 files (`REPORT.md`, `INSTRUCTIONS_ROUND_027.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report027.zip` (< 15 MB) and verify the `.zip` preview chip in the composer before sending.

---

## Round 026 Visual QC Feedback

1. **PASS — Native KFX `$270` Covers (`verify_r26_kfx_cover_book1.png`, `verify_r26_kfx_cover_book2.png`)**: Both Hesiod and Sophocles render edge-to-edge full-width centered on `#000000` black with zero white HTML margins and zero `"0%"` footer.
2. **PASS — Live `Aa → Font → Spacing` Reflow (`verify_r26_kfx_spacing_before.png` vs `after.png`)**: Clear real-time vertical reflow confirmed in `KRIFDocViewer` (note: those screenshots were captured on Sophocles's Title/Contents page rather than prose, so please capture real prose/verse in Round 027).
3. **BUG CAUGHT BY VISUAL QC — Stray Line Breaks in Contents / Text Nodes**:
   - In `verify_r26_kfx_spacing_before.png`, look at the Contents list:
     ```
     THE
     WOMEN OF TRACHIS
     OEDIPUS
     THE KING
     ```
     `"THE"` and `"OEDIPUS"` break onto their own indented lines while `"WOMEN OF TRACHIS"` and `"THE KING"` wrap flush-left!
   - Root cause to verify: In EPUB XHTML, source files often contain raw `\n`/`\r`/`\t` inside `<p>` or `<a>` tags, or inline elements split across lines. HTML (`white-space: normal`) collapses all ASCII whitespace (`\r`, `\n`, `\t`, multiple spaces) into a single space `' '`, whereas KFX `$145` content strings treat literal `\n` as a hard line break (or splitting an inline `<a>`/`<span>` into separate blocks causes a forced break).

---

## Task A: Pure-Rust `libkfxconv.so` Typography, Whitespace & Internal Hyperlink Fixes

Reference `jloutsch/kfxgen` (`plugin/kfxgen/native_generator.py`) and stock Kindle KFX structure (read-only) as needed to port clean Ion structures into our Rust `kfx_converter`:

1. **HTML Whitespace Normalization (Fix Stray Line Breaks)**:
   - Inspect Sophocles's EPUB HTML around `"THE WOMEN OF TRACHIS"` and `"OEDIPUS THE KING"` and inspect `kfx_converter`'s HTML/text parser (`src/epub/` and `src/packager/`).
   - Collapse all `\r`, `\n`, `\t`, and consecutive spaces into a single `' '` inside normal text nodes (preserving explicit `<br/>` and `<pre>` / `white-space: pre` blocks), trim leading/trailing whitespace at block boundaries, and ensure inline tags (`<a>`, `<span>`, `<i>`, `<em>`, `<b>`, `<strong>`, `<sup>`, `<sub>`) never spuriously split a parent `<p>` into multiple blocks.
2. **Inline Emphasis & Multi-Byte UTF-8 Offset Audit**:
   - Verify `$142` (`style_events`) emits accurate `$143` (`offset`) and `$144` (`length`) in **Unicode code points / UTF-16 code units** (NOT UTF-8 byte offsets!) so italics, bold, superscripts, and links stay aligned after curly quotes (`“”`), em-dashes (`—`), and Greek/accented characters.
   - Verify `<blockquote>`, poetry/verse indentation (`margin-left`/`padding-left` and `<br/>` inside `<p>`), and `<ul>`/`<ol>` lists render cleanly.
3. **Internal `#anchor` Hyperlinks & Footnotes**:
   - Check how `kfx_converter` emits internal `<a href="file.xhtml#id">` links vs `jloutsch/kfxgen` (`$266` anchor entities + `$179` `link_to` in `$142` `style_events`).
   - Fix or wire up internal hyperlink resolution so tapping an in-book Contents entry (e.g., `"ANTIGONE"` / `"AJAX"` in Sophocles) or footnote link jumps directly to the target section/anchor in `KRIFDocViewer`.
4. **Unit Tests & ARM64 Build**:
   - Add Rust unit tests (`cargo test`) covering: (a) HTML newline/whitespace collapsing, (b) multi-byte UTF-8 inline style offsets, and (c) internal `#anchor` link emission.
   - Cross-compile stripped `libkfxconv.so` (`aarch64-linux-android`), bundle into Folio `v2.4`, sign, and deploy to Galaxy M32.

---

## Task B: On-Device Multi-Book Visual QC (Folio v2.4)

Clear cached `.kfx` outputs for the test books so Folio re-converts them on-device with the updated `libkfxconv.so`, then capture and verify:

1. `screenshots/verify_r27_sophocles_contents_fixed.png`: Sophocles Title/Contents page proving `"THE WOMEN OF TRACHIS"` and `"OEDIPUS THE KING"` render cleanly on single lines with zero stray breaks.
2. `screenshots/verify_r27_internal_link_jump.png`: Tap an in-book Contents hyperlink (e.g., `"ANTIGONE"` or `"AJAX"` in Sophocles, or a footnote/TOC link in `kindle_exam_v3.epub`) and capture the destination page proving live internal link navigation.
3. `screenshots/verify_r27_prose_emphasis_verse.png`: Capture a real prose/verse page (from Sophocles or a second distinct book such as `kindle_exam_v3.epub` or Herodotus/Aeschylus) with italics/bold, curly quotes/em-dashes, and verse/blockquote formatting to verify typography and emphasis alignment.

---

## Task C: Systematic UI/UX Audit Sweep (Dark Theme, Library, Home)

Inspect the remaining core Folio UI surfaces on-device and capture:

1. `screenshots/verify_r27_reader_dark_theme.png`: Switch an open KFX book to **Black (Dark)** page color in `Aa → Layout → Page Color` and verify crisp white-on-black text, zero white borders/flashes, and clean footer.
2. `screenshots/verify_r27_library_tab.png`: Folio **Library** tab showing local KFX books with real covers, clean titles/authors, and zero broken/placeholder thumbnails.
3. `screenshots/verify_r27_home_tab.png`: Folio **Home** tab showing single unified search bar, Continue Reading real covers, and Open Library recommendations.
4. In `REPORT.md`, include a concise **"UI/UX Audit Findings"** list noting any real visual glitches, clipped labels, broken covers, or leftover Kindle-store stubs spotted on Library, Home, or More tabs so we can fix them in Round 028.
