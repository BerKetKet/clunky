# Round 018 Instructions — Final Polish: Guaranteed On-Genre Recommendations + Smart Per-Book Cover Overlay + Folio v1.1 Release

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report018.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Round 017 Visual Review & Final Polish Items

Round 017's screenshots are awesome:
- `verify_r17_cover_fullscreen_native.png`: Edge-to-edge `#000000` black full-width cover, zero white borders, zero `"0%"` footer!
- `verify_r17_aa_font_spacing.png` & `verify_r17_spacing_sliders.png`: Native **`Aa → Font → Spacing`** unlocked with live Line Spacing & Paragraph Spacing sliders!
- `verify_r17_home_polished.png`: Clean single search bar, **Continue Reading** & **Popular Free EPUBs** cover carousels!

Two quick final-polish tweaks in Round 018 make **Folio v1.1** ready for the user's spin:
1. **`OpenLibraryClient.java`**: Added 3-tier fallback in `fetchRecommendations()` (specific subjects → same-author books from Open Library → genre shelf fallback) so **"Because You Read Metro 2034"** ALWAYS populates with on-genre covers (e.g. *Metro 2033*, *Metro 2035*, sci-fi classics) instead of hiding when specific subject queries return empty!
2. **`CoverOverlayView.java`**: Checks `pos <= 5` so returning to a mid-book reading position isn't interrupted by the cover overlay, and resolves the exact active book's `.cover.jpg` from the Activity's intent URI before falling back to `EpubTranscoder.lastCoverPath`.

---

## Tasks

### Task A — Build & Deploy Round 018
1. Copy `OpenLibraryClient.java` and `CoverOverlayView.java` into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/`.
2. Rebuild & deploy **Folio** (`com.amazon.kindle.epub`) via `clone_and_deploy_epub_mod.sh`.
3. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Verify On-Genre Recommendations & Per-Book Cover Overlay + Stage Folio v1.1 Release
1. Open **Folio** on the **Home** tab, wait ~5s for all three carousels to populate, and capture **`screenshots/verify_r18_home_all_carousels.png`** confirming **Continue Reading**, **Because You Read Metro 2034** (with real on-genre covers!), and **Popular Free EPUBs** all appear cleanly!
2. Open *Metro 2034* (or *Moby Dick*) at the beginning of the book to confirm its own fullscreen `#000000` black cover renders edge-to-edge (`screenshots/verify_r18_cover_verified.png`).
3. Stage the signed **`Folio v1.1`** release APK(s) into `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/` and record exact file sizes in `REPORT.md`.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report018.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
