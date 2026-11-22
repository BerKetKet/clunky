# Round 099 — Folio v9.9 KFX (Reading Goal Completed Books Trophy Shelf)

## Task A: Drop-In Build & Deploy
1. Copy the single drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.9_kfx/base.apk`.

## What's New in Folio v9.9 KFX
- **🏆 Completed Books Trophy Shelf inside Reading Goal Modal (`FolioMoreView.java`)**:
  - Upgraded `showReadingGoalDialog(...)` into a rich dark **🎯 2026 Reading Goal** modal featuring live emerald progress bar, a **`🏆 COMPLETED BOOKS (N)`** horizontal cover-art trophy strip (displaying standalone `.cover.jpg` thumbnails, normalized authors, and emerald `✓ YYYY-MM-DD` completion dates sorted most-recently-finished first, with 1-tap jump to the `Finished` filter), and quick annual target presets (`5`, `12`, `24`, `36`, `52`) + custom target input.
- Version badge and About Folio bumped to **Folio v9.9 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r99_goal_trophy_shelf.png` — Tap **`🎯 2026 Reading Goal`** on Control Center to capture the upgraded dark modal showing the progress bar, **`🏆 COMPLETED BOOKS (1)`** trophy card for *Moby Dick; Or, The Whale* (`✓ 2026-11-23`), and target presets.
2. `verify_r99_about_v99.png` — About Folio modal showing `Version: Folio v9.9 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report099.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
