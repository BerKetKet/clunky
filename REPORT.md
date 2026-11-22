# Folio Milestone Release Report: Clickable Genre Pills in Book Details, Live Stat Tile Auto-Refresh & Tappable Version Badge (Round 057)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v5.7 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v5.7_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 057, Folio advanced to **Folio v5.7 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 212,556 B), built, aligned, signed, and deployed the updated APK to Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Clickable Genre Pills in Book Details**: Replaced plain-text genre listings with a scrollable horizontal row of clickable blue pills (`#60A5FA` on `#1E2433`).
2. **Instant Genre Filtering**: Tapping any genre pill (verified on `Fiction` from *1984*, and `Sea Stories` from *Moby Dick*) immediately dismisses the Book Details dialog and filters the catalog to matching titles, updating the header to `Library Metadata & Genres (10 of 27)` with the inline `✕` clear button active.
3. **Live Stat Tile Auto-Refresh**: Passed `Runnable onDismiss` into `FolioMetadataCatalogDialog.show(ctx, initialFilter, onDismiss)` so Control Center stat counters (`BOOKS`, `READING`, `FINISHED`) automatically refresh the moment the catalog dialog closes.
4. **Tappable Version Header Badge**: Tapping the `Folio v5.7 KFX` header badge in Control Center directly opens the **About Folio** dialog (`Version: Folio v5.7 KFX (Enhanced Typesetting)`).
5. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v5.7 KFX

### A. Clickable Genre Pills (`FolioMetadataCatalogDialog.java`)
- Replaces static `Genres:` text inside the Book Details popup with a `HorizontalScrollView` of individual genre tags.
- Each pill is styled with rounded corners (`12dp`), subtle background `#1E2433`, text color `#60A5FA`, and horizontal padding.
- When clicked, the Book Details popup is dismissed and `onAuthorFilter.onFilterByAuthor(gTag)` is invoked, which resets genre filter to `All`, inputs the genre into the catalog search box, triggers real-time search filtering, updates the count header, and reveals the `✕` clear button.

### B. Control Center Auto-Refresh Callback (`FolioMetadataCatalogDialog.java` & `FolioMoreView.java`)
- Extended `FolioMetadataCatalogDialog.show` with an overloaded signature `show(Context ctx, String initialFilter, Runnable onDismiss)`.
- Binds `dialog.setOnDismissListener(d -> onDismiss.run())`.
- In `FolioMoreView.java`, wires `refreshStats` onto all 3 stat tiles and the Library Metadata card so any status modification (Mark Finished / Unread, Remove) updates tile numbers immediately upon dialog dismissal.

### C. Tappable Header Badge & Version Bump (`FolioMoreView.java`)
- Binds an `OnClickListener` to the `Folio v5.7 KFX` header badge in Control Center to invoke `showAboutDialog(ctx)`.
- Bumped version string to `Folio v5.7 KFX (Enhanced Typesetting)`.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Clickable Genre Pills in Book Details** | Book Details (`ⓘ` on *1984*) | `screenshots/verify_r57_book_details_genre_pills.png` | **PASS** — Book Details popup renders horizontal scrollable row of blue pills (`Totalitarianism`, `Fiction`, `Dystopias`) above description and action buttons. |
| **Instant Genre Filtering & Header Count** | Catalog after tapping `Fiction` pill | `screenshots/verify_r57_genre_pill_filter_live.png` | **PASS** — Book Details dismissed; catalog filtered to 10 matching titles; header updated to `Library Metadata & Genres (10 of 27)`; inline `✕` clear button active. |
| **Tappable Version Header Badge** | Control Center Header Badge | `logs/r57_about.xml` | **PASS** — Tapping `Folio v5.7 KFX` header badge directly opened About Folio displaying `Version: Folio v5.7 KFX (Enhanced Typesetting)`. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report057.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_057.md`
  - `protocol.json`
  - `screenshots/verify_r57_book_details_genre_pills.png`
  - `screenshots/verify_r57_genre_pill_filter_live.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v5.7_kfx/base.apk` (131 MB)
