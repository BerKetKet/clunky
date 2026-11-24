# Round 116 Deliverables Report — Folio v11.6 KFX (Manage Shelves Studio: + New Shelf Picker & Cover Previews)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.6 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:45:17`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **`+ New Shelf` Header Pill & Multi-Book Picker (`showShelfBookPickerDialog`)**: Added a soft-violet `+ New Shelf` pill in the `🏷 Manage Shelves` header. Tapping it opens an interactive shelf builder with:
       * Quick preset chips (`Philosophy`, `Fiction`, `Sci-Fi`, `History`, `Study`).
       * Live book filtering bar and multi-select checkboxes (`☑`/`☐`) allowing instant book assignment in one modal flow.
       * Atomic creation and persistence into `/sdcard/kindle/metadata/shelves.json`.
     - **Shelf Cover Thumbnails & `+ Books` Inline Editor**:
       * Each shelf card renders a standalone `.cover.jpg` bitmap thumbnail of its lead book via `makeMiniCoverView`.
       * Added compact action pills: emerald `+ Books` (add/remove books for that specific shelf), `✏️` (rename shelf), and `✕` (delete shelf).
     - **Reading Notes Footer Cleanup**: Cleaned up the redundant AlertDialog `Copy All` footer button now that the 3-pill top toolbar (`📋 Copy All`, `↗ Share`, `💾 Export .md`) is live.
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.6 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (401,360 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:45:17`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.6_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r116_shelves_studio.png` (195,178 bytes)**:
   - Captured **🏷 Manage Shelves Studio** opened from Control Center.
   - Shows the `+ New Shelf` header pill alongside `🏷 Manage Shelves (2)`.
   - Shows existing shelf `Classics (1)` with book cover thumbnail and action pills (`+ Books`, `✏️`, `✕`).
   - Shows newly created shelf `Philosophy (1)` populated with *Nicomachean Ethics* (Aristotle) created via the new preset picker.
2. **`verify_r116_about_v116.png` (354,642 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.6 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Indexed Library Books: 34 · Enriched Metadata Records: 32`
     * `Reading Progress: 5 in progress · 1 finished`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * `Privacy Mode: Local-First (Zero Amazon Telemetry)`
3. **Crash & Invariant Log Check**:
   - Zero crashes logged for `com.amazon.kindle.epub`.
   - Stock Kindle (`com.amazon.kindle` v8.160.1.0) verified **100% untouched** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Package Verification Artifacts
- Archive: `antig_to_gemini_neon_clunky_report116.zip`
  * `REPORT.md`
  * `INSTRUCTIONS_ROUND_116.md`
  * `protocol.json`
  * `verify_r116_shelves_studio.png`
  * `verify_r116_about_v116.png`
