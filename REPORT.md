# Round 018 Report — Final Polish: Guaranteed On-Genre Recommendations + Smart Per-Book Cover Overlay + Folio v1.1 Release

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys `lastUpdateTime=2026-11-19 21:59:23`).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary

In Round 018, all final polish objectives for the **Folio v1.1** flagship release were executed, verified on-device, and staged end-to-end:

1. **Task A (Build & Deploy Round 018)**:
   - Integrated the updated `OpenLibraryClient.java` featuring a robust 3-tier recommendation engine:
     * *Tier 1*: Specific non-generic subjects from Open Library.
     * *Tier 2*: Same-author bibliographic discovery (e.g., Dmitry Glukhovsky -> *Metro 2033*, *Metro 2035*).
     * *Tier 3*: Genre shelf fallback (sci-fi / speculative / horror / adventure) ensuring "Because You Read [Book]" carousels always populate with relevant cover art even when narrow subject queries return empty.
   - Integrated `CoverOverlayView.java` with intent-based per-book cover resolution (`<dataPath>.cover.jpg` and `epub_cache/<name>.azw3.cover.jpg`) and calibrated the beginning-of-book threshold (`pos <= 500`) accounting for PalmDB/KF8 fragment header byte offsets.
   - Compiled Dalvik DEX `classes9.dex` (91,376 bytes), rebuilt `com.amazon.kindle.epub`, signed with persistent debug keystore, and performed streamed installation to device (`Success`).
   - Confirmed stock `com.amazon.kindle` is 100% untouched.

2. **Task B (On-Device Verification & Folio v1.1 Release Staging)**:
   - **All Three Home Carousels**: Navigated to Folio Home tab; verified asynchronous loading of **Continue Reading** (seeded from local library), **Because You Read Metro 2034** (populated with real on-genre covers), and **Popular Free EPUBs** (`screenshots/verify_r18_home_all_carousels.png`).
   - **Native Edge-to-Edge Cover Presentation**: Opened *Moby Dick* at beginning; verified native `#000000` black cover overlay rendered edge-to-edge (763,653 pure black pixels, 30.2% of screen) with zero white borders and zero `"0%"` footer (`screenshots/verify_r18_cover_verified.png`).
   - **Folio v1.1 Release Staged**: Staged signed standalone release APKs in `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/`:
     * `base.apk`: **134,839,933 bytes** (134.8 MB)
     * `base.apk.idsig`: **1,074,563 bytes** (1.07 MB)

3. **Task C (Package & Autonomous Delivery)**:
   - Packaged all deliverables into `antig_to_gemini_neon_clunky_report018.zip` (< 100 MB).

---

## 2. Folio v1.1 Release Artifacts & Checksums

| File Path | Byte Size | Description |
|---|---|---|
| `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/base.apk` | 134,839,933 bytes | Folio v1.1 standalone signed base APK with adaptive launcher icon, native KF8 EPUB transcoder, Open Catalog home carousels, native fullscreen cover overlay, and unlocked Aa text spacing seekbars. |
| `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/base.apk.idsig` | 1,074,563 bytes | APK v4 signature metadata file for incremental / streamed installation. |

---

## 3. Hardware Verification Screenshots (Task B)

| Screenshot File | Resolution | Description & Verified Elements |
|---|---|---|
| `verify_r18_home_all_carousels.png` | 1080×2340 | Folio Home tab displaying single unified search bar, **Continue Reading** carousel, on-genre **Because You Read Metro 2034** carousel, and **Popular Free EPUBs** carousel with live cover art. |
| `verify_r18_cover_verified.png` | 1080×2340 | *Moby Dick* opening to native fullscreen `#000000` black `CoverOverlayView` — full-width cover, edge-to-edge pure black, ZERO white borders, ZERO `"0%"` footer. |

---

## 4. Stock Safety Invariant Verification

Stock package status verified via package manager dumpsys:
```
Package: com.amazon.kindle
versionName=8.160.1.0(2.0.124621.0)
lastUpdateTime=2026-11-19 21:59:23
firstInstallTime=2026-11-14 03:25:13
Status: 100% UNTOUCHED and READ-ONLY
```
All build files, Dex modifications, and staged assets were confined to `com.amazon.kindle.epub` and `/home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/`.
