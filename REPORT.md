# Round 137 Deliverables Report — Azath v14.3 KFX (Continue Reading Archival Monograms, Bespoke Catalog Authors Icon, Web Browser Verification & GitHub Sync)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Azath Version**: `Azath v14.3 KFX` (`com.azath.reader`) installed on device at `2026-11-25 00:33:28`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Build, Deploy, Stage & GitHub Sync
1. **Drop-In Integration & Fixes Caught Before Build**:
   - **Missing Asset/Constant Resolved (`FolioIcons.AUTHORS` & `ic_authors.png`)**: `FolioMetadataCatalogDialog.java` referenced `FolioIcons.AUTHORS`, which was omitted along with `ic_authors.png` from `to_antig_clunky_round_137.zip`. Generated a matching 96x96 transparent `#EFECE6` + `#E05A47` editorial portrait+quill asset (`dropin_assets/azath_icons/ic_authors.png` and `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/ic_authors.png`) and wired `FolioIcons.AUTHORS = 28` -> `"azath_icons/ic_authors.png"` in `FolioIcons.java`.
   - **Continue Reading Title Hint (`FolioMoreView.java`)**: Added a `makeMiniCoverView(..., String titleHint, ...)` overload passing `rec.title` for `Continue Reading` cards so fallback initials match book titles (`B` for *Brave New World*) regardless of `.kfx` filename, and kept the `"Azath Library Insights\n"` snapshot header.
   - Copied both updated Java files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. **Compilation, Deployment & Staging**:
   - Recompiled `classes9.dex` (`461,852` bytes), ran `patch_classes2_azath.py` + XML bottom nav patches in `rebuild_fast.sh` (`azath_v14.3_kfx`).
   - Deployed `com.azath.reader` to **Samsung Galaxy M30** (`lastUpdateTime=2026-11-25 00:33:28`) with zero `FATAL EXCEPTION` entries in `logcat`.
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.3_kfx/base.apk` (`137,860,883` bytes ~132 MB, SHA256: `2e269a19b6a555aa7894fe49375532a0fada254bb7c040d596783efe7f3cf6be`).

---

## 2. Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
1. **`verify_r137_azath_control_center.png` (230,136 bytes)**:
   - Captured **Azath** Control Center showing **`v14.3 KFX`** pill badge, *Brave New World* displaying its Regular Bookerly **`B`** archival monogram with `#5C2D26` crimson spine rule in `Continue Reading`, and `#E05A47` Ember Crimson bottom navigation `More` tab.
2. **`verify_r137_azath_web_browser.png` (104,589 bytes)**:
   - Captured **Web Book Browser** opened from Control Center displaying the unified Obsidian-Ivory-Crimson toolbar (`standardebooks.org/ebo...`), `#E05A47` reader-mode chip, `Pin`, tonal crimson `EPUBs` chip, `Downloads` chip (`ic_import.png`), `Standard Ebooks`, and `Gutenberg`.
   - *(Note for Round 138 polish: When the phone has no active internet connection, stock Chromium WebView's built-in `net::ERR_INTERNET_DISCONNECTED` error page uses dark-on-dark default text inside `#121010`; injecting a dark-themed `onReceivedError` fallback HTML page or checking Settings/Catalog offline states would fix contrast.)*
3. **Safety Invariant**:
   - Stock `com.amazon.kindle` (`8.160.1.0`) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged in `logcat`.
