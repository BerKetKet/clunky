# Round 013 Instructions — True Android Adaptive Icon (Zero White Frame) + Full Multi-Chapter `#anchor` TOC Support (135+ Chapters)

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report013.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Visual Inspection Findings from Round 012

Round 012's Folio rebrand, 4-tier download validation, and Library grid look awesome! Inspecting the screenshots revealed two high-value polish items we can nail right now:
1. **White border around Folio icon on Smart Launcher 6**: Smart Launcher wraps legacy non-adaptive `@drawable` icons in a white squircle. Round 013 upgrades Folio to a true Android `<adaptive-icon>` (`res/mipmap-anydpi-v26/folio_launcher_icon.xml` with `@drawable/folio_icon_bg` + `@drawable/folio_icon_fg`) so the deep navy-teal icon fills the ENTIRE squircle edge-to-edge with zero white border!
2. **Multi-chapter-per-file EPUB TOC (e.g., *Moby Dick*)**: In `verify_r12_downloaded_book2.png`, *Moby Dick* showed Chapters 1, 4, 10, 17… because Project Gutenberg packs multiple chapters into each spine HTML file using `#id` anchors, and `EpubTranscoder` previously created only 1 TOC entry per spine file (with 2-digit `"%02d"` INDX keys). Round 013 parses **all `<navLabel>/<content src="...#id">` entries**, resolves each `#id` to its exact UTF-8 byte offset inside the KF8 fragment, and upgrades NCX INDX keys to 4-digit `"%04d"` (supporting up to 10,000 chapters!).

---

## Tasks

### Task A — Deploy Adaptive Icon & Full Multi-Chapter TOC Transcoder
1. Copy from this package into your project:
   - `assets/folio_launcher_icon.png`, `assets/folio_icon_bg.png`, `assets/folio_icon_fg.png` → `<MOD_ROOT>/assets/`
   - `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` → `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`
   - `scripts/clone_and_deploy_epub_mod.sh` → `<MOD_ROOT>/tools/clone_and_deploy_epub_mod.sh`
2. Run `clone_and_deploy_epub_mod.sh` to rebuild & install **Folio** (`com.amazon.kindle.epub`).
3. Confirm stock `com.amazon.kindle` is 100% untouched.
4. Capture **`screenshots/verify_r13_adaptive_icon.png`** in Smart Launcher 6's app drawer confirming the Folio icon fills the squircle cleanly with **zero white border**!

### Task B — Verify Full 135-Chapter Native TOC on *Moby Dick*
1. Delete the cached `/data/data/com.amazon.kindle.epub/files/epub_cache/Moby_Dick__Or__The_Whale_2701.epub.azw3` so *Moby Dick* re-transcodes with Round 013.
2. Open `/sdcard/kindle/Moby_Dick__Or__The_Whale_2701.epub` in **Folio** (`com.amazon.kindle.epub`).
3. Open the native slide-out TOC drawer and verify **consecutive chapters** (`CHAPTER 1. Loomings.`, `CHAPTER 2. The Carpet-Bag.`, `CHAPTER 3. The Spouter-Inn.`, `CHAPTER 4. The Counterpane.`, `CHAPTER 5. Breakfast.`, etc.) are ALL present! Capture **`screenshots/verify_r13_mobydick_full_toc.png`**.
4. Tap **`CHAPTER 2. The Carpet-Bag.`** (an intra-file `#anchor` chapter!) and confirm the reader jumps cleanly to Chapter 2! Capture **`screenshots/verify_r13_mobydick_ch2_jump.png`**.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report013.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
