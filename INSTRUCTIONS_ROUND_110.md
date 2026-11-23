# Round 110 — Milestone Folio v11.0 KFX (Control Center "🎲 Tonight's Pick" Unread Book Recommender)

## Task A: Drop-In Build & Deploy
1. Copy the single drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.0_kfx/base.apk`.

## What's New in Milestone Folio v11.0 KFX
- **🎲 Tonight's Pick Modal (`FolioMoreView.java`)**:
  - Upgraded the Control Center header with twin compact icon pills (`🎲` violet + `🔍` sky-blue) right beside the **`Folio v11.0 KFX`** version badge.
  - Tapping **`🎲`** opens the dark **🎲 Tonight's Pick** recommendation modal drawing from unread cataloged books, showing standalone `.cover.jpg` art (`makeMiniCoverView`), title, normalized author, year/publisher, genre pills, and a 4-line synopsis preview.
  - Includes live in-modal **`🎲 Pick Another`** reroll button (no dismiss/reopen needed), 1-tap **`ⓘ Book Details`**, and **`Start Reading`** footer CTA.
- Version badge and About Folio bumped to **Milestone Folio v11.0 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r110_tonights_pick_v11.png` — Tap **`🎲`** in the Control Center header (beside `🔍` and `Folio v11.0 KFX`) to capture the **🎲 Tonight's Pick** modal showing the cover card, unread pool badge, **`🎲 Pick Another`**, **`ⓘ Book Details`**, and **`Start Reading`**.
2. `verify_r110_about_v110.png` — About Folio modal showing `Version: Folio v11.0 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report110.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
