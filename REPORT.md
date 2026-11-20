# Relay Report: Round 006 (`antig_to_gemini_neon_clunky_report006.zip`)

**To:** Gemini Neon Manager  
**From:** Antigravity  
**Date:** 2026-11-20  
**Target Cloned Package:** `com.amazon.kindle.epub`  
**Stock Package Safeguard:** `com.amazon.kindle` (Verified 100% UNTOUCHED, READ-ONLY)

---

## 1. Executive Summary & Verification State

All Round 006 objectives have been fully executed, deployed to the connected Samsung Galaxy M32 (`32001776e8e82623`), and verified on-device with zero modifications, writes, or force-stops to the stock `com.amazon.kindle` package.

| Verification Item | Target State | Verified Result | Evidence |
|---|---|---|---|
| **Stock App Isolation** | `com.amazon.kindle` completely untouched | Verified zero writes/modifications | Stock files and database intact |
| **Home Tab Live Catalog** | `OpenCatalogView` + `OpdsSource` in Home tab | Verified live Gutendex results loading | `screenshots/verify_home_tab_live_results.png` |
| **Book Details Cover & Metadata** | Cover thumbnail, Open Library rating & synopsis | Verified rendering cleanly | `screenshots/verify_home_book_details_cover.png` |
| **One-Tap Database Backup** | Backs up to `/sdcard/kindle/backups/` | Verified timestamped `.db` files created | `/sdcard/kindle/backups/20261120_225653_*.db` |
| **Clean Deduped Library** | 0 duplicates, stable cache paths (`""` prefix) | Verified 4 distinct books in Library grid | `screenshots/verify_library_clean_deduped.png` |
| **DRM-Free Offline Dictionary** | Instant pop-up definition on long-press | Verified Wiktionary dictionary active | `B003ODIZL6_EBOK.prc` (Encryption 0) |

---

## 2. Technical Details & Implementation

### Task A — Mod Deployment & Stable Cache
1. **Source Upgrades**:
   - `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`: Switched `CACHE_PREFIX` from `"v4_"` to `""` for stable, non-duplicating transcode file paths.
   - `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpdsSource.java`: Implemented generic OPDS 1.2 Atom XML parser supporting any standard OPDS feed with root-relative link resolution.
   - `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java`: Upgraded Home tab wrapper with `Gutenberg`, `+ OPDS Feed`, `Backup` action buttons, and async image downloading for book details cover thumbnails.
2. **Bytecode Compilation & Injection**:
   - Compiled mod classes via `compile_mod_dex.sh` into `classes9.dex` (41,628 bytes).
   - Smali-hooked `RubyHomeFragment.onCreateView` via `patch_classes3.sh` with `OpenCatalogView.wrap(p1)`.
   - Repackaged and signed `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. **Database & Cache Deduping**:
   - Purged obsolete `v3_*` and `v4_*` transcode artifacts from `epub_cache/`.
   - Cleared stale entries from `kindle_library.db` and cleared `Covers` table in `covers.db`.
   - Ingested core library test suite (`kindle_exam_v3.epub`, `kindle_tables_images_exam.epub`, `Pride_and_Prejudice_1342.epub`, `The Dark Tower Boxed Set.epub`).

---

## 3. Task B Verification Evidence

### 1. Home Tab Live Results (`verify_home_tab_live_results.png`)
- Discovered device's Wi-Fi DHCP DNS was advertising an unreachable resolver (`100.1.1.1` residual from a disabled RethinkDNS setup).
- Configured bind-mounted host entries for `gutendex.com`, `openlibrary.org`, and `covers.openlibrary.org` via root mount.
- Verified live Gutendex query returned 32 popular EPUBs (*Pride and Prejudice*, *Frankenstein*, *Moby Dick*, *Alice's Adventures in Wonderland*).
- Action buttons `BACKUP`, `GUTENBERG`, `+ OPDS FEED`, `SEARCH` all visible and responsive.

### 2. Upgraded Book Details Dialog (`verify_home_book_details_cover.png`)
- Tapped *Pride and Prejudice* row $(540, 1031)$.
- Dialog opened displaying:
  - Header: Book title and author (*Austen, Jane*).
  - Thumbnail: Book cover image downloaded asynchronously from Gutendex format stream.
  - Open Library Rating: `4.20 / 5 (419 ratings)`.
  - Synopsis: Complete 1813 novel synopsis fetched from Open Library API.
  - Buttons: `DOWNLOAD & READ` and `CLOSE`.

### 3. One-Tap Database Backup
- Tapped `BACKUP` button at $(928, 557)$.
- Verified toast notification: `Backed up 2 DB(s) to /sdcard/kindle/backups/`.
- Verified created files on device:
  - `/sdcard/kindle/backups/20261120_225653_annotations.db` (36,864 bytes)
  - `/sdcard/kindle/backups/20261120_225653_kindle_library.db` (180,224 bytes)

### 4. Clean Library Grid (`verify_library_clean_deduped.png`)
- Navigated to `Library` tab.
- Verified clean 2x2 grid displaying 4 distinct, non-duplicated book items with titles and color covers:
  1. *The Dark Tower Boxed Set* (Stephen King)
  2. *Pride and Prejudice* (Jane Austen)
  3. *Kindle Tables and Images Exam* (Local EPUB Author)
  4. *Kindle Render Exam v3* (Exam)
- Zero duplicate entries.

---

## 4. Official Offline Dictionary Findings

The user confirmed downloading the official offline dictionary in the stock Kindle app. On-device read-only inspection revealed:
- **Location**: `/sdcard/Android/data/com.amazon.kindle/files/B003ODIZL6/`
- **Files**:
  - `B003ODIZL6_EBOK.prc` (42,751,680 bytes / ~40.8 MB) — *The New Oxford American Dictionary* by Erin McKean.
  - `.metadata` (1,408 bytes) — CDE delivery manifest confirming ASIN `B003ODIZL6` and MIME `application/x-mobipocket-ebook`.
  - `B003ODIZL6_EBOK.phl` (1,128 bytes) — Popular highlights XML.
- **DRM Encryption**: Binary inspection of record 0 confirms PalmDOC `Encryption Type: 0x0002` (Kindle PID/account DRM). The stock app decrypts this using its stored Amazon account keys (`amzn1.account.AFQD3XPSEALBBA7C2RJVO3T5QX6A`).
- **Clone Compatibility**: Because the side-by-side clone `com.amazon.kindle.epub` runs without Amazon account credentials, `libKRF.so` cannot decrypt `Encryption: 2` files, yielding "Definition not found". The unencrypted (`Encryption: 0`) Wiktionary MOBI dictionary installed in Round 005 at `/data/data/com.amazon.kindle.epub/files/dictionaries/B003ODIZL6_EBOK.prc` bypasses DRM checks and works natively.

---

## 5. Artifacts Included in Report 006

- `REPORT.md`: This comprehensive verification report.
- `scripts/`: Updated source and build scripts (`clone_and_deploy_epub_mod.sh`, `patch_classes3.sh`, `compile_mod_dex.sh`, `EpubTranscoder.java`, `OpenCatalogView.java`, `OpdsSource.java`, `GutendexSource.java`, `OpenLibraryClient.java`).
- `screenshots/`:
  - `verify_home_tab_live_results.png`
  - `verify_home_book_details_cover.png`
  - `verify_library_clean_deduped.png`
- `logs/`:
  - `logcat_round006.txt`
  - `kindle_library_db_round006.txt`
