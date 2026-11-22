# Round 081 — Folio v8.1 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Upgrades 3-column Cover Grid tiles:
       - Switches cover rendering to `ScaleType.FIT_CENTER` on `#0D1017` dark mat so tall covers (*Moksha*, *Moby Dick*) never crop their top title lettering.
       - Sets `minLines(2)` on tile titles so all 3 cards in every row share a pixel-aligned bottom edge.
       - Adds a sleek 3dp **Reading Progress Mini-Bar** directly under each cover (emerald for `✓ Done`, sky blue for in-progress `X%`).
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v8.1 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r81_grid_uncropped_aligned.png`: Open **All Metadata ›** (already in persistent Grid Mode!), verifying uncropped *Moby Dick* and *Moksha* covers, pixel-aligned bottom edges across all 3 columns, and the emerald progress bar under *Moby Dick*.
2. `screenshots/verify_r81_grid_reading_bars.png`: Tap the teal **`Reading (5)`** chip while in Grid Mode, verifying the sky-blue progress mini-bars under your 5 in-progress covers!

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report081.zip`.
