# Round 055 — Folio v5.5 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`
     * Adds a compact **`Select New`** / **`Clear (N)`** toggle pill beside the folder match count: tapping `Select New` selects up to 25 currently visible *non-imported* EPUBs in 1 tap (automatically skipping already-imported books), updating `Import Selected (N)` and switching the pill to amber `Clear (N)`.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.5 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.5_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r55_select_new_orwell.png`: Tap **Import** on Home, browse `/sdcard/en/0`, search `Orwell`, and tap **`Select New`** — verifying all 12 new Orwell EPUBs check automatically (skipping already-imported *1984* and *Animal Farm*), `Clear (12)` appears in amber, and the bottom CTA updates to `Import Selected (12)`.
2. `screenshots/verify_r55_about_v55.png`: Open **More → About Folio**, verifying **Folio v5.5 KFX (Enhanced Typesetting)**.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report055.zip`.
