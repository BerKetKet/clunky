# Round 061 — Folio v6.1 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Expands the Control Center Live Stats Strip to a balanced **4-tile dashboard**: `BOOKS` (amber) · `READING` (teal) · `FINISHED` (emerald) · **`★ FAVS`** (gold `#FBBF24`), with 1-tap deep-link from `★ FAVS` directly into the `Favorites` catalog filter and live auto-refresh on dismiss.
     * Bumps badge and About Folio to **Folio v6.1 KFX**.
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
     * Upgrades portable JSON backup (`version: 2`) to export and restore each book's `is_favorite` state alongside metadata.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.1_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r61_control_center_4tile_favs.png`: Open the **More** tab (Control Center), verifying the **`Folio v6.1 KFX`** badge and the crisp 4-tile live stats strip (`29 BOOKS` · `5 READING` · `1 FINISHED` · `1 ★ FAVS`) cleanly aligned on one row.
2. `screenshots/verify_r61_favs_tile_deeplink.png`: Tap the gold **`1 ★ FAVS`** tile, confirming `Library Metadata & Genres (1 of 27)` opens immediately pre-filtered to the active gold `★ Favorites (1)` chip showing `★ 1984`.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report061.zip`.
