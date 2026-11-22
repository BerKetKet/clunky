# Folio Milestone Release Report: Persistent Favorites System, Gold Quick-Filter Chip & Milestone v6.0 KFX (Round 060)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Milestone Folio v6.0 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v6.0_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 060, Folio achieved a major milestone release: **Folio v6.0 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 217,936 B), built, aligned, signed, and deployed the updated APK to Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Persistent Favorites Action Pill**: Added a dedicated **`☆ Favorite`** / **`★ Favorited`** toggle pill side-by-side with `✓ Mark as Finished` inside Book Details (`ⓘ`), backed cleanly by `SharedPreferences("folio_favorites")` with zero DB schema migration risk.
2. **Dynamic Gold Favorites Quick-Filter Chip**: Whenever 1+ books are favorited, the catalog header strip surfaces a prominent gold **`★ Favorites (N)`** chip (`#FBBF24` on `#282216`) positioned directly alongside `All (27)`. Tapping the chip instantly filters the catalog (verified on *1984*, updating heading to `Library Metadata & Genres (1 of 27)`).
3. **Starred Title Card Badges**: Favorited books display a prominent gold `★` prefix on their catalog list card (e.g., `★ 1984`).
4. **Live About Folio Favorites Telemetry**: About Folio dialog and Control Center badge bumped to **`Folio v6.0 KFX (Enhanced Typesetting)`** and display live **`• Favorited Books: 1`** alongside reading and storage metrics.
5. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Milestone Folio v6.0 KFX

### A. Persistent Favorites Subsystem (`FolioMetadataCatalogDialog.java`)
- Implemented static helpers `isFavorite(Context, String kfxPath)`, `setFavorite(Context, String kfxPath, boolean)`, and `countFavorites(Context)`.
- Storage is localized to app-private `folio_favorites.xml` preferences, ensuring non-destructive zero-migration persistence across app upgrades.
- In Book Details popup, an `actionPillRow` hosts both the read-state toggle and the favorite toggle:
  - Unfavorited: `☆ Favorite` with `#1E2433` background and `#CBD5E1` text.
  - Favorited: `★ Favorited` with `#282216` background, `#FBBF24` gold text, and subtle `#66FBBF24` stroke.
  - Tapping toggles state, shows confirmation Toast, dismisses Book Details, and refreshes catalog cards.

### B. Gold Favorites Chip & Filter Filtering (`FolioMetadataCatalogDialog.java`)
- Evaluates `favCount` across `allBooks`: if $N \ge 1$, inserts `"Favorites"` into top filter chip list.
- Renders `★ Favorites (N)` in gold accent (`#FBBF24`), styling active state with solid gold background and dark slate text.
- Catalog filter loop evaluates `isFavorite(ctx, rec.kfxPath)` when `selectedGenre[0] == "Favorites"`.
- Prefixes book card title with `★ ` when favorited.

### C. Milestone Version Bump & About Telemetry (`FolioMoreView.java`)
- Header badge and About Folio dialog updated to **`Folio v6.0 KFX (Enhanced Typesetting)`**.
- Reads `FolioMetadataCatalogDialog.countFavorites(ctx)` and renders `• Favorited Books: N`.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Side-by-Side Favorite & Finished Pills** | Book Details (`ⓘ` on *1984*) | `screenshots/verify_r60_book_details_favorite_pill.png` | **PASS** — Book Details popup renders side-by-side `✓ Mark as Finished` and `☆ Favorite` action pills above Remove action. |
| **Gold Favorites Chip & Starred Filter** | Catalog after tapping `☆ Favorite` | `screenshots/verify_r60_favorites_chip_live.png` | **PASS** — Gold `★ Favorites (1)` chip appeared in strip; tapping it filtered catalog to `Library Metadata & Genres (1 of 27)` with starred `★ 1984` card. |
| **Live About Folio Favorites Telemetry** | Control Center About Folio | `logs/r60_about.xml` | **PASS** — About Folio displays `Version: Folio v6.0 KFX (Enhanced Typesetting)` and `• Favorited Books: 1`. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report060.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_060.md`
  - `protocol.json`
  - `screenshots/verify_r60_book_details_favorite_pill.png`
  - `screenshots/verify_r60_favorites_chip_live.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v6.0_kfx/base.apk` (131 MB)
