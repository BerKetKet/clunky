# Round 115 (Milestone) — Folio v11.5 KFX (Reading Notes Studio: + Add Note, Share & Export .md)

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`** (`showNotesNotebookDialog`):
  1. **`+ Add Note` Header Pill & Searchable Book Picker (`showAddNoteBookPicker`)**: Added a violet `+ Add Note` pill right beside `📝 Reading Notes (N)` so you can pick any indexed book (with live title/author filtering and `✏️ Edit` / `+ Note` status badges) and write or update a reading note directly from the Notebook.
  2. **3-Pill Export & Share Toolbar**: Added `📋 Copy All` (amber), `↗ Share` (sky-blue `ACTION_SEND`), and `💾 Export .md` (emerald, writes `/sdcard/kindle/backups/folio_reading_notes.md`) right above the search bar, powered by `buildNotesMarkdown(...)` (includes star ratings alongside book titles, authors, and quote blocks).
  3. **Milestone Version Bump**: Updated Control Center header badge and `About Folio` dialog to `Folio v11.5 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.5_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r115_notes_studio.png`**: Open `📝 Reading Notes Notebook` from the Control Center, tap **`💾 Export .md`** (verify `/sdcard/kindle/backups/folio_reading_notes.md` is created and report its byte size in `REPORT.md`), and capture the upgraded Reading Notes Studio showing the `+ Add Note` header pill, the 3-pill toolbar (`📋 Copy All`, `↗ Share`, `💾 Export .md`), and the saved note card(s).
2. **`verify_r115_about_v115.png`**: Open `About Folio` and capture `Version: Folio v11.5 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report115.zip`.
