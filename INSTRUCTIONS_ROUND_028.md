# Round 028 Instructions — KFX Image Aspect-Ratio Fix & UI Branding/Layout Polish (Folio v2.5)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI fixes must be 100% general — never hardcode book titles or image filenames.  
**Reply Archive Budget:** Package ONLY Round 028 files (`REPORT.md`, `INSTRUCTIONS_ROUND_028.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report028.zip` (< 15 MB) and verify the `.zip` preview chip before sending.

---

## Round 027 Visual QC Feedback

1. **PASS — Whitespace Normalization & Internal Links (`verify_r27_sophocles_contents_fixed.png`, `verify_r27_internal_link_jump.png`)**: `"THE WOMEN OF TRACHIS"` and `"OEDIPUS THE KING"` now render cleanly on single continuous blue-underlined link lines, and tapping `"ANTIGONE"` jumped straight to Location 508.
2. **PASS — UTF-16 Style Offsets & Verse (`verify_r27_prose_emphasis_verse.png`)**: Crisp Bookerly verse layout and accurately aligned inline italics (`No man shall say that *I* betrayed a brother.`).
3. **PASS — Home & Library Real Covers (`verify_r27_home_tab.png`, `verify_r27_library_tab.png`)**: Clean editorial Home layout with real covers across Continue Reading and Popular Free EPUBs.
4. **ARCHITECTURAL CLARIFICATION ON COVERS**: Keep the Round 026 native KFX `$270` standalone cover container (which ALREADY strips cover HTML from the EPUB spine during ingestion and renders edge-to-edge on `#000000` black with zero margins and zero `"0%"` footer, while supporting native Kindle swipe-to-Chapter-1). Do **NOT** rip out `$270` or replace it with a separate Java/Compose overlay for `.kfx` books (keep `CoverOverlayView` only for legacy `.azw3` fallback).
5. **CRITICAL BUG CAUGHT BY VISUAL QC — Vertical Image Aspect-Ratio Stretching**:
   - In `verify_r27_internal_link_jump.png`, the horizontal decorative flourish below `ANTIGONE` is **stretched vertically to fill ~80% of the screen height**, turning a thin ornament into a huge distorted vertical smudge.
6. **SECONDARY UI/UX ITEMS CAUGHT BY VISUAL QC**:
   - On `verify_r27_library_tab.png`, the search bar placeholder still reads `"Search Kindle"` instead of `"Search Folio"`.
   - On `verify_r27_home_tab.png`, the bottom-left floating mini-cover dock overlaps the `"Pride and Prejudice"` title text when scrolled near the bottom.
   - On `verify_r27_sophocles_contents_fixed.png`, vertical gaps between Contents links appear very wide (likely because `Aa → Spacing` remained at Maximum from Round 026 — please verify default spacing as well as paragraph margins).

---

## Task A: Fix Prose Image Aspect-Ratio Scaling in Pure-Rust `libkfxconv.so`

1. **Diagnose & Fix `$271` / `$164` Image Sizing**:
   - Inspect how `kfx_converter` emits non-cover prose images (`$271` in `src/packager/storylines.rs`, `$164` external resource metadata, and image styles in `src/packager/styles.rs`), comparing against `jloutsch/kfxgen` (`plugin/kfxgen/native_generator.py`) and read-only stock Kindle KFX files.
   - Root cause to fix:
     * Always populate real decoded pixel dimensions (`$66: img_w`, `$67: img_h`) on every `$164` external resource entity.
     * Never emit conflicting or unconstrained `height: 100%` / `width: 100%` that distorts a prose image's intrinsic aspect ratio in YellowJersey (`KRIFDocViewer`).
     * Ensure small/wide decorative ornaments (e.g., `400×40` chapter dividers) and standard illustrations preserve their exact `img_w : img_h` aspect ratio (respecting explicit CSS width/height when provided, or scaling proportionally within the container width without ever stretching vertically).
2. **Audit Paragraph Vertical Margins (`styles.rs`)**:
   - Check that default `<p>` `margin-top`/`margin-bottom` values aren't inflated in `styles.rs`, and reset `Aa → Spacing` to default/normal on-device before capturing verification screenshots.
3. **Unit Tests & ARM64 Build**:
   - Add a Rust unit test in `src/packager/tests.rs` verifying that a wide horizontal ornament (e.g., `400×40`) and a portrait illustration (e.g., `600×900`) both emit exact `$66`/`$67` dimensions and non-distorted `$271` image style properties.
   - Run `cargo test --all-targets`, cross-compile stripped `libkfxconv.so` (`aarch64-linux-android`), bundle into Folio `v2.5`, sign, and deploy to Galaxy M32.

---

## Task B: Multi-Book On-Device Visual Verification (Folio v2.5)

Clear cached `.kfx` outputs for the test books so Folio `v2.5` re-converts them on-device with the fixed `libkfxconv.so`, then capture:

1. `screenshots/verify_r28_antigone_ornament_fixed.png`: Sophocles `ANTIGONE` title page proving the decorative divider renders crisply as a proportional horizontal ornament with zero vertical stretching.
2. `screenshots/verify_r28_images_exam_aspect.png`: A second distinct book with mixed-aspect images (e.g., `kindle_tables_images_exam.epub` or another illustrated EPUB) proving both wide and tall images maintain true aspect ratios.
3. `screenshots/verify_r28_sophocles_contents_default_spacing.png`: Sophocles Contents page at default `Aa → Spacing` proving balanced vertical line spacing and clean single-line hyperlinks.

---

## Task C: Library & Home UI Polish + More Tab Audit

1. **Library Search Branding**: Replace `"Search Kindle"` on the Library tab search bar with `"Search Folio"` (via string resource or runtime text hook, keeping stock `com.amazon.kindle` untouched).
2. **Home Tab Bottom Clearance**: Add ~`80dp` bottom padding (with `clipToPadding=false`) to `OpenCatalogView`'s scroll container so the bottom carousel cards and titles scroll cleanly above the floating mini-cover dock without overlap.
3. **Capture & Audit**:
   - `screenshots/verify_r28_library_branding.png`: Library tab showing `"Search Folio"` and real covers.
   - `screenshots/verify_r28_home_bottom_padding.png`: Home tab scrolled to the bottom proving clean clearance above the bottom nav / mini-cover dock.
   - `screenshots/verify_r28_more_tab.png`: Folio **More** tab screenshot, plus a concise **"More Tab UI Audit"** section in `REPORT.md` listing any dead Amazon-account/store items to clean up in Round 029.
