# Round 125 (Milestone) — Folio v12.5 KFX (New Crimson Launcher Icon, Native Kindle Fonts & Zero-Neon Highlight Sweep)

## Summary of Changes (Drop-In Ready)
1. **New Crimson Boy-Under-Tree Launcher Icon (`dropin_res/`)**:
   - Pre-cropped, clean full-bleed PNG assets generated across all Android densities (`mipmap-mdpi` through `mipmap-xxxhdpi`: `ic_launcher.png`, `ic_launcher_round.png`, `ic_launcher_foreground.png`, plus `ic_launcher_full_512.png` and `ic_launcher_foreground.png`).
   - Replace the decoded APK's launcher icon PNGs/WebPs (e.g., `res/mipmap-*/ic_launcher*`, `res/drawable-*/ic_launcher*`, or whatever icon resource `AndroidManifest.xml` `android:icon` / `android:roundIcon` points to) with the matching density assets from `dropin_res/`. If the manifest uses an adaptive `<adaptive-icon>` XML pointing to a foreground drawable + background color, point the foreground to `ic_launcher_foreground.png` and set the background color to `#1A0808`.
2. **NEW `FolioTheme.java`** (`com.amazon.kindle.epub.catalog.FolioTheme`):
   - **Native Kindle Font Loader**: Loads Kindle's bundled **Bookerly** (headings/display) and **Amazon Ember** (body, lists, buttons, chips) fonts directly from the APK's `assets/` at runtime with clean `SERIF` / `SANS_SERIF` fallback.
   - **Recursive Tree Typography & Checkbox Tinter (`FolioTheme.applyToTree`)**: Applies Bookerly/Amazon Ember across every view hierarchy and tints every `CheckBox` with Kindle blue `#4DA6FF` (checked) / neutral `#6E7680` (unchecked).
   - **Dialog Footer Styler (`FolioTheme.styleDialog`)**: Unifies AlertDialog Positive (`#4DA6FF` Ember bold), Negative (`#8E959C`), and Neutral (`#C4CBD2`) buttons.
3. **Thorough Zero-Neon Highlight Sweep (0 Leftover Neon Colors Across All 4 UI Files)**:
   - Audited and replaced all 200 remaining neon green, purple, amber/orange, and cyan highlight hexes across `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioFolderBrowserDialog.java`, and `FolioWebBrowserDialog.java` with Kindle's unified highlight palette (`#4DA6FF` active/checked, `#162330` subtle selected fill, `#2C5278` active border, `#1C1F22` neutral controls, `#262A2E` hairlines).
   - Bumped Control Center badge and `About Folio` dialog to **Milestone `Folio v12.5 KFX`**.

## Task A: Build, Wire Launcher Icon, Sign, Deploy & Stage
1. Copy all 5 Java files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Wire the new crimson launcher icon assets from `dropin_res/` into the decoded APK's `res/` launcher icon resources (matching `AndroidManifest.xml`'s `android:icon` and `android:roundIcon`).
3. Recompile `classes9.dex`, rebuild APK with `apktool b`, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
4. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.5_kfx/base.apk`.
5. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r125_launcher_icon.png`**: Capture the Android app drawer or home screen on the Galaxy M32 showing the new **Folio** crimson boy-under-tree launcher icon.
2. **`verify_r125_kindle_fonts_and_highlights.png`**: Open **Import EPUBs from Storage** (check 1–2 EPUB checkboxes so the Kindle blue `#4DA6FF` checked tint, Amazon Ember/Bookerly typography, and zero-neon `Select New` / `Already Imported` / `★` pin button are all visible) and capture the modal.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report125.zip`.
