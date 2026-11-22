# Round 064 — Folio v6.4 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds a crisp 1-line italic amber **Note Snippet Preview** (`“Essential dystopian classic - reread Part 3”`) directly on annotated catalog cards.
     * Adds **`Sort: Favorites`** to the Sort pill cycle (`Recent → Title → Author → Year → Progress → Favorites`) pinning starred books at the top of the catalog.
     * Adds `countNotes(Context)` helper for About Folio telemetry.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v6.4 KFX** and shows `• Favorites & Notes: 1 favorited · 1 annotated`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.4_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r64_sort_favorites_note_snippet.png`: Open **All Metadata ›** and cycle the Sort pill to **`Sort: Favorites`**, verifying starred `★ 📝 1984` pins cleanly to the very top of the catalog with its italic amber inline note snippet preview (`“Essential dystopian classic - reread Part 3”`).
2. `screenshots/verify_r64_about_v64_telemetry.png`: Tap the **`Folio v6.4 KFX`** badge on Control Center to open About Folio, verifying `Version: Folio v6.4 KFX (Enhanced Typesetting)` and `• Favorites & Notes: 1 favorited · 1 annotated`.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report064.zip`.
