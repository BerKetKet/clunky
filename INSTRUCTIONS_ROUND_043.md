# Round 043 Instructions — Ultra-Light Full Source Code Snapshot for Gemini (No Build / No Screenshots)

**Priority:** Keep this round **ultra-fast and minimal** (single packaging step — NO compilation, NO APK rebuild, NO ADB deploy, NO screenshots).  
**Hard Safety Invariant:** Stock `com.amazon.kindle` remains **100% UNTOUCHED and READ-ONLY**.  
**Goal:** Transfer a clean, text-only snapshot of our entire custom codebase to Gemini so Gemini can do 100% of the heavy engineering, cross-file refactoring, and Rust/Java authoring directly on its end going forward.

---

## Round 042 Visual QC Feedback

- **PASS** on all 3 Round 042 screenshots (`verify_r42_folder_search_and_hide_imported.png`, `verify_r42_enriched_genres_card.png`, `verify_r42_about_v39.png`): 719-EPUB live filename search (`Orwell` → 14 matches), emerald `Hide Imported: ON` toggle, Animal Farm cover/genres, and `Folio v3.9 KFX` About dialog all verified!

---

## Task A (Single Step): Package Lean Text-Only Source Snapshot

Create `antig_to_gemini_neon_clunky_report043.zip` (< 5 MB, strictly text source files — **exclude** `target/`, `.git/`, `*.apk`, `*.so`, `*.dex`, `*.jar`, `*.png`, `*.epub`, `*.kfx`, `*.db`, `base_dec/` except `AndroidManifest.xml`) containing:

1. `REPORT.md` (brief manifest listing included files & sizes + stock Kindle `lastUpdateTime` check)
2. `INSTRUCTIONS_ROUND_043.md` and `protocol.json`
3. `snapshot/07_APK_EPUB_INGESTION_MOD/mod_src/` — ALL `.java` files
4. `snapshot/07_APK_EPUB_INGESTION_MOD/tools/` and `snapshot/07_APK_EPUB_INGESTION_MOD/scripts/` — ALL `.sh`, `.py`, `.smali` patch scripts
5. `snapshot/kfx_converter/` — `Cargo.toml`, `src/**/*.rs`, `tests/**/*.rs` (text `.rs`/`.toml` only; NO `target/` or binary test fixtures)
6. `snapshot/AndroidManifest.xml` — current patched manifest for `com.amazon.kindle.epub`
7. `snapshot/kindle_library_schema.sql` — read-only output of:
   `adb -s 32001776e8e82623 shell su -c "sqlite3 /data/data/com.amazon.kindle.epub/databases/kindle_library.db '.schema KindleContent LocalContent UserContent'"`

### Ready-to-Run Packaging Helper (Optional One-Liner)

```python
import os, zipfile

out_zip = "antig_to_gemini_neon_clunky_report043.zip"
TEXT_EXTS = {".java", ".rs", ".toml", ".sh", ".py", ".smali", ".xml", ".md", ".json", ".sql", ".txt"}
SKIP_DIRS = {"target", ".git", "build", "release", "screenshots", "logs", "__pycache__", "node_modules"}

def add_tree(zf, src_root, arc_prefix):
    if not os.path.exists(src_root):
        return 0
    count = 0
    for root, dirs, files in os.walk(src_root):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for fn in sorted(files):
            ext = os.path.splitext(fn)[1].lower()
            if ext not in TEXT_EXTS:
                continue
            full = os.path.join(root, fn)
            if os.path.getsize(full) > 512 * 1024:
                continue
            rel = os.path.relpath(full, src_root)
            zf.write(full, os.path.join(arc_prefix, rel))
            count += 1
    return count

with zipfile.ZipFile(out_zip, "w", zipfile.ZIP_DEFLATED) as zf:
    n = 0
    n += add_tree(zf, "07_APK_EPUB_INGESTION_MOD/mod_src", "snapshot/07_APK_EPUB_INGESTION_MOD/mod_src")
    n += add_tree(zf, "07_APK_EPUB_INGESTION_MOD/tools", "snapshot/07_APK_EPUB_INGESTION_MOD/tools")
    n += add_tree(zf, "07_APK_EPUB_INGESTION_MOD/scripts", "snapshot/07_APK_EPUB_INGESTION_MOD/scripts")
    n += add_tree(zf, "kfx_converter/src", "snapshot/kfx_converter/src")
    n += add_tree(zf, "kfx_converter/tests", "snapshot/kfx_converter/tests")
    for extra, arc in [
        ("kfx_converter/Cargo.toml", "snapshot/kfx_converter/Cargo.toml"),
        ("snapshot_manifest.xml", "snapshot/AndroidManifest.xml"),
        ("snapshot_schema.sql", "snapshot/kindle_library_schema.sql"),
        ("REPORT.md", "REPORT.md"),
        ("INSTRUCTIONS_ROUND_043.md", "INSTRUCTIONS_ROUND_043.md"),
        ("protocol.json", "protocol.json"),
    ]:
        if os.path.exists(extra):
            zf.write(extra, arc)
            n += 1
print(f"Packed {n} files into {out_zip} ({os.path.getsize(out_zip)/1024:.1f} KB)")
```

Verify a single `.zip` attachment chip in the composer and upload `antig_to_gemini_neon_clunky_report043.zip`.
