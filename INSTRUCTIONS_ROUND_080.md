# Round 080 — Milestone Folio v8.0 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds a persistent **`⊞` / `☰`** view-mode toggle pill in the catalog top bar (`SharedPreferences("folio_ui_prefs")` → `catalog_grid_mode`) switching cleanly between the rich List view and a **3-column Cover Art Grid** showcasing extracted `.cover.jpg` covers, 2-line titles, compact status/rating badges (`✓ Done`, `X%`, `★ 5/5`, `Unread`), and `ⓘ` details shortcuts.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Milestone Folio v8.0 KFX (Enhanced Typesetting)**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r80_catalog_cover_grid_view.png`: Open **All Metadata ›** and tap the **`⊞`** toggle pill in the top bar, verifying the crisp **3-column Cover Art Grid** displaying covers, titles, status/star badges, and `ⓘ` buttons cleanly aligned.
2. `screenshots/verify_r80_control_center_v80.png`: Open **More** tab (Control Center), verifying the **`Folio v8.0 KFX`** milestone badge.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report080.zip`.
