# Round 082 — Folio v8.2 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds a multiline **`Description / Synopsis`** field to the **Edit Book Metadata** form.
     * Adds `syncTitleAuthorToKindleDb(...)` on Save so edited Title and Author immediately propagate into `kindle_library.db` (`KindleContent`) in addition to `folio_metadata.db`.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v8.2 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v8.2_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r82_edit_metadata_synopsis.png`: Open **All Metadata ›**, tap `ⓘ` on *1984* (or *Animal farm: a fairy story*) → **EDIT METADATA**, scrolling slightly if needed to verify the new multiline **`Description / Synopsis`** field at the bottom of the form.
2. `screenshots/verify_r82_about_v82.png`: Tap the **`Folio v8.2 KFX`** badge on Control Center to open About Folio confirming `Version: Folio v8.2 KFX (Enhanced Typesetting)`.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report082.zip`.
