# Folio Milestone Release Report: Sort by Rating & Random Unread Book Picker (Round 066)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v6.6 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v6.6_kfx/base.apk` (131 MB)

---

## 1. Executive Summary

In Round 066, Folio advanced to **Folio v6.6 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 227,464 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **`Sort: Rating` Library Ordering**: The catalog Sort control pill now cycles through 7 sorting modes (`Recent → Title → Author → Year → Progress → Favorites → Rating`). Selecting `Sort: Rating` orders books by star rating descending (then by favorites and recency). Five-star title `★ 📝 1984` (`★★★★★  5/5`) ranks cleanly at position 0 at the very top of the library.
2. **Random Unread Book Picker (`🎲`)**: A compact `🎲` button is situated beside `Sort:` in the catalog top bar. Tapping it automatically filters the current view for unread books (`progress == 0`), selects a random title from the candidate pool, displays a confirmation Toast (`Picked for you: <Title>`), and immediately launches its rich Book Details popup (verified with *Delphi Complete Works of Pliny the Younger*).
3. **Control Center Version Bump**: Header badge and About Folio dialog bumped to **`Folio v6.6 KFX (Enhanced Typesetting)`**.
4. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v6.6 KFX

### A. Sort by Rating (`FolioMetadataCatalogDialog.java`)
- Appended `"Rating"` to `SORT_MODES` (mode index 6).
- Implemented mode 6 sorting logic in `sortBooks`:
  - Compares `getRating(ctx, a.kfxPath)` vs `getRating(ctx, b.kfxPath)` in descending order (`Integer.compare(rb, ra)`).
  - Uses `isFavorite` and `addedAt` as secondary and tertiary sort keys.

### B. Random Unread Book Picker (`FolioMetadataCatalogDialog.java`)
- Created `btnRandom` ("🎲") TextView styled with `#1E2433` background, `#334155` border stroke, and `#E2E8F0` foreground.
- Situated directly between the `FOLIO METADATA` eyebrow and the `Sort:` pill in `topRow`.
- Tracks `lastFiltered` during list rendering.
- On click:
  - Filters `lastFiltered` for records where `getProgress == 0`.
  - Falls back to `lastFiltered` if all filtered books have been started/finished.
  - Selects random index via `java.util.Random`.
  - Shows Toast (`Picked for you: ` + pick.title) and opens `showBookDetailDialog`.

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v6.6 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Sort by Rating & Dice Pill** | Catalog Dialog (`All Metadata ›`) | `screenshots/verify_r66_sort_rating_and_dice_pill.png` | **PASS** — `Sort: Rating` ranks 5-star `★ 📝 1984` (`★★★★★ 5/5`) at position 0 at top; `🎲` pill is positioned cleanly beside Sort. |
| **Random Unread Book Picker Action** | Book Details Popup | `screenshots/verify_r66_random_book_picked.png` | **PASS** — Tapping `🎲` selects random unread book (*Delphi Complete Works of Pliny the Younger*) and opens its rich Book Details popup. |
| **Stock Package Safety Invariant** | Android Package Manager | `lastUpdateTime=2026-11-19 21:59:23` | **PASS** — Stock `com.amazon.kindle` remains 100% untouched, intact, and read-only. |

---

## 4. Project Lead Architectural Directive on Cover Handling

As instructed by the Project Lead, the following architectural directive is reaffirmed:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 5. Artifacts and Deliverables

- `antig_to_gemini_neon_clunky_report066.zip`:
  - `REPORT.md`
  - `INSTRUCTIONS_ROUND_066.md`
  - `protocol.json`
  - `screenshots/verify_r66_sort_rating_and_dice_pill.png`
  - `screenshots/verify_r66_random_book_picked.png`
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v6.6_kfx/base.apk` (131 MB)
