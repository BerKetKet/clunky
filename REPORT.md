# Round 012 Report — Custom App Name & Launcher Icon ("Folio") + Multi-Book Real Download Stress Test

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary

In Round 012, all tasks have been completed and verified on-device:
1. **Task A (Branding & Deployment)**: Rebranded cloned standalone package `com.amazon.kindle.epub` to **Folio** with custom midnight-navy/amber launcher icon (`folio_launcher_icon.png`). Rebuilt split APKs and installed without impacting stock `com.amazon.kindle`. Confirmed custom launcher icon and "Folio" title visible in Android launcher.
2. **Task B (Multi-Book Real Download Stress Test)**: Downloaded two fresh books from Open Catalog (*Alice's Adventures in Wonderland* #11 and *Moby Dick; Or, The Whale* #2701). Verified `PK\x03\x04` magic bytes, complete ZIP archive integrity via `unzip -t`, auto-transcoding to KF8 (`BOOKMOBI`), reader launch, and native TOC drawers. Verified both appear at the top of the Folio Library grid with covers.
3. **Task C (Packaging & Autonomous Delivery)**: Assembled verified artifacts into `antig_to_gemini_neon_clunky_report012.zip` (< 100 MB).

---

## 2. Task A — Folio Rebrand & Stock Invariant Verification

### A.1 Manifest & Resource Rewriting
- Updated `tools/clone_and_deploy_epub_mod.sh` to bundle `assets/folio_launcher_icon.png` into `res/drawable/folio_launcher_icon.png`.
- Replaced app label with `android:label="Folio"` across all localized label entries.
- Added `android:icon="@drawable/folio_launcher_icon"` and `android:roundIcon="@drawable/folio_launcher_icon"` to `<application>` and all `CATEGORY_LAUNCHER` activity declarations.
- Re-signed and deployed via `adb install-multiple`.

### A.2 Stock vs Mod Verification
```
# Mod Package:
Package: com.amazon.kindle.epub
Label: Folio (application-label: 'Folio')
Icon: res/drawable/folio_launcher_icon.png
Version: 8.159.0.100

# Stock Package (Strictly Untouched):
Package: com.amazon.kindle
Label: Kindle
Version: 8.160.1.0(2.0.124621.0)
State: 100% untouched and separate.
```

- **Evidence Screenshot:** `screenshots/verify_r12_launcher_icon_name.png` (displays "Folio" in the launcher app drawer alongside other apps).

---

## 3. Task B — Multi-Book Download Stress Test Results

### B.1 Book 1: *Alice's Adventures in Wonderland* (ID: 11, Lewis Carroll)
- **Saved EPUB Path:** `/sdcard/kindle/Alice_s_Adventures_in_Wonderland_11.epub`
- **Byte Size:** `136,759` bytes
- **Header Magic Bytes:** `50 4b 03 04` (`PK\x03\x04`)
- **ZIP Archive Test (`unzip -t`):**
  ```
  Archive: /tmp/Alice.epub
      testing: mimetype                 OK
      testing: META-INF/container.xml   OK
      testing: OEBPS/7402354833213361015_cover.jpg   OK
      testing: OEBPS/8620333367427747180_11-h-0.htm.html   OK
      ...
      testing: OEBPS/toc.ncx            OK
  No errors detected in compressed data of /tmp/Alice.epub.
  ```
- **Auto-Transcode KF8 Cache:**
  - Path: `/data/data/com.amazon.kindle.epub/files/epub_cache/Alice_s_Adventures_in_Wonderland_11.epub.azw3`
  - Byte Size: `246,381` bytes
  - PalmDB Format: `BOOKMOBI` (KF8)
- **Native Reader & TOC Drawer:**
  - Opens directly into `StandAloneBookReaderActivity`.
  - Native Table of Contents drawer verified with 12 chapters ("CHAPTER I. Down the Rabbit-Hole", "CHAPTER II. The Pool of Tears", etc.).
- **Evidence Screenshot:** `screenshots/verify_r12_downloaded_book1.png`

---

### B.2 Book 2: *Moby Dick; Or, The Whale* (ID: 2701, Herman Melville)
- **Saved EPUB Path:** `/sdcard/kindle/Moby_Dick__Or__The_Whale_2701.epub`
- **Byte Size:** `722,877` bytes
- **Header Magic Bytes:** `50 4b 03 04` (`PK\x03\x04`)
- **ZIP Archive Test (`unzip -t`):**
  ```
  Archive: /tmp/Moby.epub
      testing: mimetype                 OK
      testing: META-INF/container.xml   OK
      testing: OEBPS/2296071534275781326_cover.jpg   OK
      testing: OEBPS/8951984864809045623_2701-h-0.htm.html   OK
      ...
      testing: OEBPS/8951984864809045623_2701-h-26.htm.html  OK
      testing: OEBPS/toc.ncx            OK
  No errors detected in compressed data of /tmp/Moby.epub.
  ```
- **Auto-Transcode KF8 Cache:**
  - Path: `/data/data/com.amazon.kindle.epub/files/epub_cache/Moby_Dick__Or__The_Whale_2701.epub.azw3`
  - Byte Size: `1,523,748` bytes
  - PalmDB Format: `BOOKMOBI` (KF8)
- **Native Reader & TOC Drawer:**
  - Opens cleanly into `StandAloneBookReaderActivity`.
  - Native Table of Contents drawer verified with 27+ sections ("MOBY-DICK; or, THE WHALE.", "CHAPTER 1. Loomings.", "CHAPTER 4. The Counterpane.", etc.).
- **Evidence Screenshot:** `screenshots/verify_r12_downloaded_book2.png`

---

### B.3 Library Tab Grid Verification
- SQLite database `kindle_library.db` verified:
  - Both books registered in `KindleContent` as `LOCAL` and `BT_EBOOK`.
  - Both books registered in `UserContent` with valid `LAST_ACCESSED` timestamps.
  - Cover images generated in `covers.db` and stored in `/data/data/com.amazon.kindle.epub/files/coverssmall/`.
- UI Grid verification:
  - Both *Moby Dick* and *Alice's Adventures in Wonderland* appear at the top row of the Library grid.
- **Evidence Screenshot:** `screenshots/verify_r12_library_with_downloads.png`

---

## 4. Verification Checkpoint Table

| Checkpoint | Target / Value | Status |
|---|---|---|
| Stock `com.amazon.kindle` | Unmodified, v8.160.1.0 | **PASS (Untouched)** |
| Mod Package Label | "Folio" | **PASS** |
| Mod Package Icon | `folio_launcher_icon.png` (Navy/Amber) | **PASS** |
| Launcher App Drawer | "Folio" visible with custom icon | **PASS** (`verify_r12_launcher_icon_name.png`) |
| Book 1 (Alice) Download | 136,759 bytes, valid ZIP (`PK\x03\x04`) | **PASS** |
| Book 1 Transcode | 246,381 bytes AZW3, `BOOKMOBI` | **PASS** |
| Book 1 Reader & TOC | 12 chapters native TOC drawer | **PASS** (`verify_r12_downloaded_book1.png`) |
| Book 2 (Moby Dick) Download | 722,877 bytes, valid ZIP (`PK\x03\x04`) | **PASS** |
| Book 2 Transcode | 1,523,748 bytes AZW3, `BOOKMOBI` | **PASS** |
| Book 2 Reader & TOC | 27+ chapters native TOC drawer | **PASS** (`verify_r12_downloaded_book2.png`) |
| Library Grid with Covers | Both books visible at top of grid | **PASS** (`verify_r12_library_with_downloads.png`) |
