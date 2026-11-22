# Folio Milestone Release Report: Uncropped FIT_CENTER Grid Covers, Uniform Card Alignment & Mini Progress Bars (Round 081)

**Date:** 2026-11-23  
**Target Package:** `com.amazon.kindle.epub` (Folio v8.1 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Staged Release APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk` (132 MB)

---

## 1. Executive Summary

In Round 081, Folio advanced to **Folio v8.1 KFX**. Gemini pre-authored drop-in Java updates for `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`. Antigravity integrated both files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`, compiled Dalvik DEX (`classes9.dex`, 267,404 B), built, aligned, signed, and deployed the updated APK to physical Samsung Galaxy M32 hardware. Physical hardware verification confirmed:
1. **Uncropped `FIT_CENTER` Cover Art (`FolioMetadataCatalogDialog.java`)**: Switched cover image scaling in 3-column Cover Art Grid mode from `CENTER_CROP` to `ScaleType.FIT_CENTER` mounted on `#0D1017` dark matte container (height 132dp). Tall covers such as *Moby Dick* and *Moksha* display full cover art with zero cropped title lettering.
2. **Uniform 2-Line Title Card Alignment (`FolioMetadataCatalogDialog.java`)**: Enforced `setMinLines(2)` and `setMaxLines(2)` with `TextUtils.TruncateAt.END` on tile titles. All 3 cards in every row now share identical vertical heights and pixel-aligned bottom borders.
3. **Reading Progress Mini-Bars (`FolioMetadataCatalogDialog.java`)**: Added a sleek 3dp horizontal reading progress mini-bar directly beneath each cover image (emerald `#34D399` for `✓ Done`, sky blue `#38BDF8` proportional fill for in-progress `X%`).
4. **Control Center Version Bump (`FolioMoreView.java`)**: Bumped Control Center badge and About Folio dialog string to **`Folio v8.1 KFX (Enhanced Typesetting)`**.
5. **Stock Package Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% untouched, intact, and read-only (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 2. Integrated Features in Folio v8.1 KFX

### A. Fit-Center Covers & Uniform Height Alignment (`FolioMetadataCatalogDialog.java`)
- Cover image view configuration:
  ```java
  ImageView cover = new ImageView(ctx);
  cover.setLayoutParams(new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 132)));
  cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
  cover.setBackgroundColor(Color.parseColor("#0D1017"));
  ```
- Two-line uniform title bounds:
  ```java
  tTv.setMinLines(2);
  tTv.setMaxLines(2);
  tTv.setEllipsize(TextUtils.TruncateAt.END);
  ```

### B. Proportional Reading Progress Mini-Bars (`FolioMetadataCatalogDialog.java`)
- 3dp track rendered under cover:
  ```java
  int pct = getProgress(progressMap, rec.kfxPath);
  LinearLayout miniTrack = new LinearLayout(ctx);
  miniTrack.setOrientation(LinearLayout.HORIZONTAL);
  miniTrack.setBackgroundColor(Color.parseColor("#242938"));
  LinearLayout.LayoutParams mtLp = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 3));
  mtLp.setMargins(0, dp(ctx, 4), 0, 0);
  miniTrack.setLayoutParams(mtLp);
  if (pct > 0) {
      float ratio = Math.max(0.08f, Math.min(1f, pct / 100f));
      View mFill = new View(ctx);
      mFill.setBackgroundColor(Color.parseColor(pct >= 100 ? "#34D399" : "#38BDF8"));
      mFill.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, ratio));
      miniTrack.addView(mFill);
      View mRest = new View(ctx);
      mRest.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(0f, 1f - ratio)));
      miniTrack.addView(mRest);
  }
  ```

### C. Version Bump (`FolioMoreView.java`)
- Bumped Control Center badge and About Folio dialog string to **`Folio v8.1 KFX (Enhanced Typesetting)`**.

---

## 3. Physical Hardware Verification on Galaxy M32

| Verification Item | Target Screen | Evidence | Result |
| :--- | :--- | :--- | :--- |
| **Uncropped Grid Covers & Aligned Cards** | All Metadata › (Grid View) | `screenshots/verify_r81_grid_uncropped_aligned.png` | **VERIFIED**: Opened All Metadata in persistent Grid Mode; verified full uncropped covers for *Moby Dick* and *Moksha*, identical 2-line title alignment across all 3 columns, and emerald progress mini-bar under *Moby Dick*. |
| **Reading Mini-Bars** | All Metadata › (Reading Filter) | `screenshots/verify_r81_grid_reading_bars.png` | **VERIFIED**: Tapped `Reading (5)` chip; verified filtered view showing 5 in-progress covers (*The 210th Day* 4%, *Sophocles* 2%, *Aristotle* 8%, *Island*, *Brave New World*) with sky blue progress mini-bars directly under covers. |
| **Stock Kindle Invariant** | Android OS PackageManager | CLI `dumpsys package com.amazon.kindle` | **VERIFIED**: `lastUpdateTime=2026-11-19 21:59:23` completely untouched. |

---

## 4. Architectural Directive for Manager AI (Cover Rendering Path)

> **MANDATORY ARCHITECTURAL DIRECTIVE (Reiterated Verbatim):**  
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
