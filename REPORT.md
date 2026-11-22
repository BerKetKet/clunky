# Folio Full Source Code Snapshot Report (Round 043)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v3.9 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

As requested in Round 043 instructions, this round delivers an **ultra-light, text-only full source code snapshot** of the entire Folio custom codebase to Gemini. This provides complete context for full-stack engineering, cross-file refactoring, and Rust/Java authoring directly on Gemini's end for subsequent rounds.

No compilation, APK rebuild, or hardware screenshots were required for this round.

---

## 2. Snapshot Manifest & Architecture Inventory

The snapshot archive `antig_to_gemini_neon_clunky_report043.zip` packages the following text assets:

1. **Custom Java Runtime Layer (`snapshot/07_APK_EPUB_INGESTION_MOD/mod_src/`)**:
   - `com.amazon.kindle.epub.ingest.FolioFolderBrowserDialog.java`: Dark multi-select EPUB storage importer with 719-EPUB in-memory search and `Hide Imported` toggle pill.
   - `com.amazon.kindle.epub.ingest.FolioMetadataBackup.java`: Portable JSON metadata backup & restore engine (`/sdcard/kindle/backups/folio_metadata_backup.json`).
   - `com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.java`: In-app Library Metadata & Genres browser dialog with sorting, real-time search filtering, and editable metadata forms.
   - `com.amazon.kindle.epub.ingest.FolioMetadataDb.java`: SQLite database manager for `folio_metadata.db` and double-registration bridge into `kindle_library.db`.
   - `com.amazon.kindle.epub.ingest.MetadataEnricher.java`: Open Library Search API enrichment engine with ISBN-to-title+author fallback.
   - `com.amazon.kindle.epub.ingest.LibraryMetadataBackfiller.java`: Full-library background backfiller and cover auto-repair sweep with HTTP 302 redirect following.
   - `com.amazon.kindle.epub.ingest.EpubMetadataExtractor.java`: Zero-dependency pure-Java EPUB container & OPF metadata extractor.
   - `com.amazon.kindle.epub.ingest.ExternalEpubActivity.java`: File intent ingestion handler with duplicate import guard (`Open Existing Copy` vs `Re-Import & Replace`).
   - `com.amazon.kindle.epub.catalog.FolioMoreView.java`: Folio Control Center custom UI screenlet with action cards and live engine telemetry.
   - `com.amazon.kindle.epub.catalog.OpenCatalogView.java`: OPDS feed catalog browser, search, and database backup hooks.
   - Additional custom stubs and injection helper classes.

2. **Build, Patch & Smali Tooling (`snapshot/07_APK_EPUB_INGESTION_MOD/tools/` & `scripts/`)**:
   - `compile_mod_dex.sh`: Standalone stub + mod Java compiler producing `classes9.dex` via Android SDK `d8`.
   - `rebuild_fast.sh`: Fast packaging script using Apktool, zipalign, apksigner, and ADB install.
   - Patch scripts, smali injector scripts, and Dex merger tools.

3. **Pure-Rust Typesetting & KFX Converter Engine (`snapshot/kfx_converter/`)**:
   - `Cargo.toml`: Complete package manifest, dependencies, and feature flags.
   - `src/**/*.rs`: Pure-Rust EPUB-to-KFX conversion engine including `$270` cover container, native tables (`$278/$279/$269`), proportional image scaling (`$422/$423`), polytonic Greek NFC normalization, hanging indent lists (`$277`), pop-up footnotes (`$616/$617`), and anchor hyperlink routing (`$266/$179`).
   - `tests/**/*.rs`: Integration and pipeline test suites.

4. **Patched AndroidManifest.xml (`snapshot/AndroidManifest.xml`)**:
   - Full cloned package manifest for `com.amazon.kindle.epub` (v8.159.0.100), including intent filters for `.epub` file viewing, storage permissions, and custom component declarations.

5. **Kindle Library SQLite Database Schema (`snapshot/kindle_library_schema.sql`)**:
   - Read-only schema dump of `kindle_library.db` capturing all tables (`KindleContent`, `LocalContent`, `UserContent`, `DictionaryInfo`, `DictionaryLocale`, `RecommendedContent`), triggers, and indexes.

---

## 3. Architectural Directive on Cover Handling

As instructed by the Project Lead, we pass the following architectural directive to the Manager AI:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 4. Safety Invariant Status

- `com.amazon.kindle` (v8.160.1.0): `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED).
- Hardware: Samsung Galaxy M32 (`32001776e8e82623`).
