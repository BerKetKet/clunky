# Round 071 — Folio v7.1 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/` (including `import java.util.LinkedHashMap;` synced in `FolioMetadataCatalogDialog.java`). Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Adds a sleek **🎯 2026 Reading Goal** progress card directly beneath the 4-tile Live Stats Strip (`1 of 12 books finished (8%)` with emerald progress bar and `Edit ›` tap target).
     * Tapping the card opens a dark goal-setter dialog with quick presets (`5`, `12`, `24`, `36`, `52`) and custom number input, live-updating the progress bar and percentage.
     * Bumps badge and About Folio to **Folio v7.1 KFX**.
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Includes `import java.util.LinkedHashMap;` and adds `Shelf: <shelf>` to the 1-tap `📋 Copy Info` clipboard summary.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v7.1_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r71_control_center_reading_goal.png`: Open **More** tab (Control Center), showing **`Folio v7.1 KFX`** badge, the 4-tile live stats strip, and the new **🎯 2026 Reading Goal** progress card (`1 of 12 books finished (8%)` with emerald progress bar).
2. `screenshots/verify_r71_reading_goal_updated.png`: Tap the Reading Goal card, select **`5`** (or `10`) and tap **SAVE GOAL**, verifying the card updates live to `1 of 5 books finished (20%)` with the wider emerald progress bar.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report071.zip`.
