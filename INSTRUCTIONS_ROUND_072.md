# Round 072 — Folio v7.2 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
     * Adds `exportLibraryCsv(Context)` generating an RFC-4180 CSV export (`/sdcard/kindle/backups/folio_library.csv`) with columns `Title,Author,Year,Publisher,ISBN,Status,Shelf,Rating,Favorite,Note`.
     * Includes `reading_goal_target` in portable JSON backup/restore and renders `**2026 Reading Goal:** 1 / 5 books finished (20%)` inside the Markdown Reading Log header.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Adds a 1-tap amber **`📊 Export CSV`** pill inside the Reading Log dialog header bar.
     * Bumps badge and About Folio to **Folio v7.2 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v7.2_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r72_reading_log_goal_and_csv_btn.png`: Open **More → Reading Log & Export**, verifying the `**2026 Reading Goal:** 1 / 5 books finished (20%)` header line, `- **Shelf:** Classics` under *1984*, and the amber **`📊 Export CSV`** pill in the top bar.
2. `screenshots/verify_r72_csv_exported_banner.png`: Tap **`📊 Export CSV`**, verifying the banner updates live to `Saved CSV: /sdcard/kindle/backups/folio_library.csv` (and verify the CSV file exists on `/sdcard/kindle/backups/folio_library.csv`).

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report072.zip`.
