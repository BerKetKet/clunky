# Round 011 Instructions — KF8 Internal Hyperlinks (`kindle:pos:fid:`), Embedded Images Verification & Interactive Similar Books

**Target Package:** `com.amazon.kindle.epub`  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report011.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Changes Applied in Round 011

1. **`EpubTranscoder.java`**:
   - Added internal cross-chapter `<a href="...xhtml">` → **`kindle:pos:fid:XXXX:off:0000000000`** rewriting in KF8 mode so tapping links inside an in-book Table of Contents or footnote/endnote jumps natively inside Kindle KF8 (`libWebCoreKRF.so`).
2. **`OpenCatalogView.java`**:
   - Upgraded Open Library **Similar Books** in the Book Details dialog from plain text into **tappable recommendation buttons** that dismiss the dialog, populate the search bar, and run a live catalog search immediately.

---

## Tasks

### Task A — Build & Deploy Round 011
1. Copy both files from `mod_src/real/com/amazon/kindle/epub/` into your project:
   - `EpubTranscoder.java`
   - `catalog/OpenCatalogView.java`
2. Compile `classes4.dex` via `compile_mod_dex.sh` and deploy `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Verify KF8 Embedded Images & Internal `<a href>` Hyperlinks
1. Clear cached transcoded files in `com.amazon.kindle.epub` so test books re-transcode with Round 011.
2. Open **`file:///sdcard/kindle/kindle_exam_v3.epub`**:
   - On the in-book **Contents** page, tap **`6. E06 Headings and Blocks`** directly on the page (not the slide-out drawer!) to verify native `kindle:pos:fid:` inline link jumping works! Capture **`screenshots/verify_kf8_r11_inline_link.png`**.
   - Jump to **`E09 Pagination and Images`** (via TOC drawer or link) to verify embedded images render cleanly under KF8! Capture **`screenshots/verify_kf8_r11_images.png`**.

### Task C — Verify Interactive Similar Books & End-to-End Gutenberg → KF8 Flow
1. Open the **Home** tab in `com.amazon.kindle.epub`, tap *Pride and Prejudice* (or another classic), scroll the Book Details dialog to show the tappable **Similar Books** buttons, and capture **`screenshots/verify_r11_similar_books_dialog.png`**.
2. Tap one of the **Similar Books** buttons (or tap **Download & Read** on a fresh Project Gutenberg book not yet in Library) to verify the full end-to-end flow: live DoH catalog → Download EPUB → auto-transcode to KF8 (`MOBI8`) → opens cleanly in reader with native TOC! Capture **`screenshots/verify_r11_gutenberg_kf8_reader.png`**.

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report011.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
