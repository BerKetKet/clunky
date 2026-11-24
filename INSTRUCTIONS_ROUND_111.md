# Round 111 — Folio v11.1 KFX (Tonight's Pick Pool Switcher & 1-Tap Queue)

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`** (`showTonightsPickDialog`):
  1. **Interactive Pool Scope Switcher**: Added 3 live-counted scope pills (`Unread (N)`, `📌 Queue (N)`, `All (N)`) right below the `🎲 Tonight's Pick` header so you can roll strictly from unread books, pick from your `📌 Up Next` queue, or roll from the entire library (displaying reading progress like `25% read` or `Finished` when applicable).
  2. **1-Tap `📌 Queue` / `📌 Queued` Toggle**: Added a third inline action button right beside `🎲 Another` and `ⓘ Details` so you can bookmark interesting picks into your `📌 Up Next` queue while rolling, dynamically updating both the button state (warm amber `📌 Queued`) and the live `📌 Queue (N)` pool counter.
  3. **Version Bump**: Updated Control Center header badge and `About Folio` dialog to `Folio v11.1 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.1_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r111_tonights_pick_pools.png`**: Tap `🎲` in the Control Center header (`Folio v11.1 KFX`), tap `📌 Queue` on the picked book to confirm it toggles to amber `📌 Queued` and increments the `📌 Queue (N)` scope pill, then capture the modal showing all 3 scope pills and all 3 inline action buttons (`🎲 Another`, `📌 Queued`, `ⓘ Details`).
2. **`verify_r111_about_v111.png`**: Open `About Folio` and capture `Version: Folio v11.1 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report111.zip`.
