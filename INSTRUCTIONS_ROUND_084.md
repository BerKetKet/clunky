# Round 084 — Folio v8.4 KFX (Continue Reading Quick-Resume Carousel)

## Task A: Drop-In Build & Deploy
1. Copy the single drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk`.

## What's New in Folio v8.4 KFX
- **📖 Continue Reading Quick-Resume Carousel** added to the top of Control Center right below `🎯 2026 Reading Goal`:
  - Dynamically lists all in-progress books (`0 < progress < 100%`) sorted by highest % read first, with a `See All (5) ›` deep-link into the `Reading` filter.
  - Each horizontal card displays the standalone `.cover.jpg` thumbnail (`FIT_CENTER` on `#0D1017` matte), 2-line title, normalized author, `X% read · Resume ›` label, and a sleek 3dp sky-blue progress bar.
  - Tapping any card launches the book straight into the KFX reader via `ExternalEpubActivity.launchKfxReader(...)`.
  - Auto-refreshes live alongside the 4 stat tiles and Reading Goal card.
- Version badge and About Folio bumped to **Folio v8.4 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r84_continue_reading.png` — Top of Control Center showing the **Folio v8.4 KFX** badge, 4-tile stats strip, `🎯 2026 Reading Goal`, and the new **📖 Continue Reading** horizontal card carousel with cover thumbnails and progress bars.
2. `verify_r84_about_v84.png` — Tap the **Folio v8.4 KFX** badge (or About Folio) to verify `Version: Folio v8.4 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report084.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
