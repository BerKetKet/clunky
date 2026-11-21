# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v1.1") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 019 complete: Stock Kindle native cover page architecture reverse-engineered in `libKRF.so` (ARM64-v8a) and `classes*.dex`, confirming user architectural directive that displaying cover as part of book HTML flow is an architectural flaw and rendering the image standalone avoids WebCore margins and footers; verified on *Alice in Wonderland* and *Moby Dick* with `#000000` edge-to-edge presentation and smooth swipe transition into Page 1; Continue Reading direct book launch fixed via `kindle_library.db` schema correction (`JOIN LocalContent l ON k.ID = l.KEY`); fine-grained Open Library recommendations deployed with rich interactive info dialogs on non-Gutenberg books.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v1.1", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation + Home cover carousels with fine-grained Open Library recommendations + sanitized CSS + native fullscreen cover presentation + unlocked text spacing seekbars + direct Continue Reading reader launch)

## Cloned Package Architecture & Modifications
1. **Stock Kindle Native Cover Architecture (`logs/stock_kindle_cover_architecture.txt`)**:
   - Native disassembly of `libKRF.so`:
     * `DocumentInfo::hasCoverPage()` (`0x21f844`): checks `[x0, #0x60] != 0`.
     * `DocumentViewerWebCore::gotoCoverPageInternal()` (`0x246a58`): calls `reloadCover()` and `refreshCoverPageElements()`.
     * `DocumentPageWebCore::reloadCover()` (`0x2348d0`): creates standalone `ImageBuffer` via `CxImage` from PalmDB record EXTH 201.
     * `DocumentPageWebCore::refreshCoverPageElements()` (`0x233238`): calls `DocumentPage::clear()` (wipes HTML layout), sets `isCoverPage = 1` (`0x118`), centers image on raw viewport canvas `(viewport - img)/2` ignoring `z8.i` margins and footers.
     * `DocumentPageWebCore::leaveCoverPage()` (`0x2348c0`): resets `isCoverPage = 0`.
   - Dalvik layer: `MobiDocViewer.hasCoverPage()` (`0x2dfbb8`), `MobiDocViewer.navigateToCover()` (`0x2e2208`).
   - In Folio: `CoverOverlayView.java` replicates this native behavior, decoding high-res cover bitmaps and presenting them edge-to-edge on pure black `#000000` canvas with zero white margins and zero footer, dismissing smoothly on swipe left into Page 1 prose.
2. **Continue Reading Direct Book Launch (`LocalLibraryReader.java`, `OpenCatalogView.java`)**:
   - `kindle_library.db` schema: `LocalContent(KEY, FILE_PATH)` stores exact file path, while `UserContent(KEY, LAST_ACCESSED)` tracks access.
   - Fixed SQL query in `LocalLibraryReader.java`: joins `LocalContent l ON k.ID = l.KEY` to populate `lb.filePath`.
   - Card click listener in `OpenCatalogView.java`: routes to `ThirdPartyUpgradePage` (with MIME type `application/epub+zip` / `application/pdf`) or `StandAloneBookReaderActivity` (`book_id` + `asin`), eliminating toast fallback.
3. **Fine-Grained Open Library Recommendations (`OpenLibraryClient.java`, Round 019)**:
   - Multi-subject intersection, publication era proximity, and same-author bibliography queries.
   - Rich interactive metadata dialog on non-Gutenberg books: cover, author, first publication year, Open Library rating, topics, full synopsis, and "Search Catalog" button.

## Verification Evidence (Round 019)
1. **Native Fullscreen Cover Presentation**:
   - Book 1 (*Alice's Adventures in Wonderland*): `verify_r19_cover_book1.png` (36.3% `#000000` pixels, zero white margins, zero footer) and `verify_r19_cover_book1_page1.png` (smooth transition into Page 1 prose).
   - Book 2 (*Moby Dick*): `verify_r19_cover_book2.png` (29.8% `#000000` pixels, zero white margins, zero footer) and `verify_r19_cover_book2_page1.png` (smooth transition into Page 1 prose).
2. **Continue Reading Direct Launch**:
   - Verified on *Metro 2034* (`verify_r19_continue_reading_opened.png`) and *Alice in Wonderland*: direct reader launch without toast.
3. **Fine-Grained Recommendations & Rich Info Dialog**:
   - Verified on Home (`verify_r19_home_fine_grained_recs.png`): on-genre recommendations seeded from library books (*Alice* -> *Through the Looking Glass*).
   - Verified on tap (`verify_r19_rec_info_dialog.png`): rich dialog with rating, year (1865), topics, synopsis, and Search Catalog.
4. **Stock Safety Invariant**:
   - Confirmed `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23` untouched.

## Deliverables
- `antig_to_gemini_neon_clunky_report019.zip` (< 100 MB).
