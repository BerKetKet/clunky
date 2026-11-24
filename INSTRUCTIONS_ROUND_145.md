# Round 145 Instructions — Azath v15.1 KFX (Stale Package-Migration KFX Path Resolver + Control Center Continue Reading Launch Fix + ExternalEpubActivity Azath Unification)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v15.1 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Bug Fix & Polish in Round 145
1. **Fix Control Center `Continue Reading` Launch for Migrated Books (`ExternalEpubActivity.java` & `FolioMoreView.java`)**:
   - In Round 144, tapping *Brave New World* inside Control Center's `Continue Reading` row stayed on `MainActivity` because restored metadata entries retained legacy `/data/user/0/com.amazon.kindle.epub/files/folio_library/...` paths from before the `com.azath.reader` rebrand.
   - Added `ExternalEpubActivity.resolveExistingKfxFile(ctx, rawFile)` (called inside `launchKfxReader` AND `FolioMoreView` click handlers): transparently remaps `/com.amazon.kindle.epub/` -> `/com.azath.reader/` and falls back to `files/folio_library/<name>`, `/sdcard/kindle/<name>`, and `/sdcard/kindle/imported/<name>` (with case-insensitive scan).
2. **`ExternalEpubActivity.java` Azath Editorial Unification**:
   - Swept all legacy `#34D399`/`#F59E0B`/`#111827`/`#262B38`/`#94A3B8` hexes and `"Folio"` strings into Azath's Obsidian-Ivory-Crimson palette (`#0A0909`, `#121010`, `#211C1B`, `#E05A47`, `#F2EFE9`, `#C4BDB5`, `#8E867E`) and wired `FolioTheme.applyToDialog(this, activeDialog)`.
3. **Version Bump**: **`Azath v15.1 KFX`**.

## Task A: Build, Deploy, Stage & GitHub Sync
1. Copy `ExternalEpubActivity.java` and `FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
2. Rebuild via `rebuild_fast.sh` (`azath_v15.1_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v15.1_kfx/base.apk` with SHA256, and push `Azath v15.1 KFX` to GitHub (`origin master`).

## Task B: Hardware Verification (Samsung Galaxy M30)
Capture and include in `antig_to_gemini_neon_clunky_report145.zip`:
1. `verify_r145_azath_continue_reading_launch.png`: Tap **Brave New World** directly from Control Center's `Continue Reading` row and verify it now launches `StandAloneBookReaderActivity` cleanly!
2. Confirm `com.amazon.kindle` remains untouched and zero fatal crashes in `logcat`.
