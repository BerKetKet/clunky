# Folio Milestone Release Report: Inline Note Snippets, Sort by Favorites & Annotated Telemetry (Round 064)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v6.4 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v6.4_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 064, Folio advanced to **Folio v6.4 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 222,920 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Inline Note Snippet Previews on Catalog Cards**: Annotated catalog list cards display a crisp 1-line italic amber snippet preview (`“Essential dystopian classic - reread Part 3”`) directly beneath the bibliographic line, allowing instant review of personal thoughts without needing to tap into Book Details.
2. **`Sort: Favorites` Catalog Order**: The Sort control pill now cycles through `Recent → Title → Author → Year → Progress → Favorites`. Selecting `Sort: Favorites` pins starred books (e.g. `★ 📝 1984`) to the very top of the library, sorted by most recently added.
3. **Favorites & Notes Telemetry**: Added `countNotes(Context)` to dynamically tally annotated books across the library. Control Center About Folio displays `Version: Folio v6.4 KFX (Enhanced Typesetting)` and surfaces live telemetry: `• Favorites & Notes: 1 favorited · 1 annotated`.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v6.4 KFX

### A. Inline Card Note Snippet Preview (`FolioMetadataCatalogDialog.java`)
- Inspects `getNote(ctx, rec.kfxPath)` during card layout generation.
- When a personal note exists, appends an italicized TextView (`#F59E0B`, `11sp`) formatting the note as `“<note text>”`.
- Clamped to 1 line with `TextUtils.TruncateAt.END` to preserve compact card aesthetics while giving readers immediate visual recall.

### B. Sort by Favorites (`FolioMetadataCatalogDialog.java`)
- Appended `"Favorites"` to `SORT_MODES`, extending the cycle to 6 modes.
- Implemented mode 5 sorting in `sortBooks(Context ctx, List<BookRecord> list, int mode, Map progressMap)`:
  - Compares `isFavorite(ctx, a.kfxPath)` vs `isFavorite(ctx, b.kfxPath)`, pinning favorited titles first.
  - Sub-sorts favorited titles by `addedAt` descending.

### C. Favorites & Notes Combined Telemetry (`FolioMoreView.java`)
- Added `countNotes(Context)` public helper in `FolioMetadataCatalogDialog`.
- Updated About Folio dialog generation in `FolioMoreView.java` to fetch both `countFavorites(ctx)` and `countNotes(ctx)`.
- Renders: `• Favorites & Notes: <favTotal> favorited · <noteTotal> annotated`.
- Bumped Control Center badge and About Folio dialog string to **`Folio v6.4 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Sort by Favorites & Note Snippet Preview** | Catalog Dialog (`All Metadata ›`) | `screenshots/verify_r64_sort_favorites_note_snippet.png` | **PASS** — Cycling Sort pill to `Sort: Favorites` pins `★ 📝 1984` to position 0 at top of catalog, displaying italic amber note preview `“Essential dystopian classic - reread Part 3”`. |
| **About Folio v6.4 & Annotated Telemetry** | About Folio Dialog | `screenshots/verify_r64_about_v64_telemetry.png` | **PASS** — Tapping `Folio v6.4 KFX` badge displays `Version: Folio v6.4 KFX (Enhanced Typesetting)` and live telemetry `• Favorites & Notes: 1 favorited · 1 annotated`. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report064.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_064.md`
  - `protocol.json`
  - `screenshots/verify_r64_sort_favorites_note_snippet.png`
  - `screenshots/verify_r64_about_v64_telemetry.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v6.4_kfx/base.apk` (131 MB)
