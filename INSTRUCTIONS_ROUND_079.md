# Round 079 — Folio v7.9 KFX (Ultra-Lean Single-File Drop-In)

All Java code is pre-authored and strictly lex-verified in `dropin_src/` (**1 single file**). Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In File, Build & Deploy (~2 min)
1. Copy the 1 drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Adds an interactive **READING STATUS (TAP TO FILTER)** stacked proportional bar (`● Finished (1)` emerald · `● Reading (5)` sky blue · `● Unread (21)` slate) at the top of Library Insights — tapping any segment or legend pill jumps straight into that catalog filter.
     * Adds a **TOP PUBLISHERS (TAP TO FILTER)** teal bar chart at the bottom of Library Insights (tapping any publisher launches the catalog filtered to that publisher).
     * Bumps badge and About Folio to **Folio v7.9 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r79_insights_stacked_status.png`: Open **More → 📊 Library Insights**, verifying the new stacked proportional **READING STATUS** bar (`● Finished (1)` · `● Reading (5)` · `● Unread (21)`) above `TOP GENRES`.
2. `screenshots/verify_r79_insights_shelves_publishers.png`: Scroll down inside Library Insights to show the **CUSTOM SHELVES** indigo bar (`🏷 Classics 1`) and the new **TOP PUBLISHERS** teal bar chart.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report079.zip`.
