package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.util.Log;
import com.amazon.kindle.epub.catalog.FolioTheme;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unified Library Accounting & Ingestion Engine for Azath.
 * Single canonical pipeline for:
 *  - Project Gutenberg / OPDS downloads (OpenCatalogView)
 *  - Batch Storage EPUB imports (FolioFolderBrowserDialog)
 *  - Web Book Browser downloads (FolioWebBrowserDialog)
 *  - External "Open With Azath" imports (ExternalEpubActivity)
 *  - Launch / Post-Ingest / Post-Delete / Pull-to-Refresh reconciliation
 */
public final class AzathLibrarySync {
    private static final String TAG = "AzathLibrarySync";
    private static final AtomicBoolean sLaunchReconciled = new AtomicBoolean(false);

    public static final class IngestResult {
        public final boolean ok;
        public final String bookKey;
        public final File kfxFile;
        public final File coverFile;
        public final EpubMetadataExtractor.BookMeta meta;
        public final String error;

        IngestResult(boolean ok, String bookKey, File kfxFile, File coverFile,
                     EpubMetadataExtractor.BookMeta meta, String error) {
            this.ok = ok;
            this.bookKey = bookKey;
            this.kfxFile = kfxFile;
            this.coverFile = coverFile;
            this.meta = meta;
            this.error = error;
        }
    }

    private AzathLibrarySync() {}

    public static File getCanonicalLibraryDir(Context ctx) {
        File sdKindle = new File("/sdcard/kindle");
        if (!sdKindle.exists()) {
            try { sdKindle.mkdirs(); } catch (Throwable ignored) {}
        }
        if (sdKindle.exists() && sdKindle.canWrite()) {
            return sdKindle;
        }
        File priv = new File(ctx.getFilesDir(), "folio_library");
        if (!priv.exists()) priv.mkdirs();
        return priv;
    }

    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Azath_Book";
        String s = raw.trim().replaceAll("[^A-Za-z0-9._-]+", "_");
        s = s.replaceAll("^_+|_+$", "");
        if (s.isEmpty()) s = "Azath_Book";
        if (s.length() > 48) s = s.substring(0, 48);
        return s;
    }

    /**
     * Canonical EPUB -> KFX ingestion pipeline. Always writes into /sdcard/kindle/ (or private fallback),
     * enriches metadata, updates FolioMetadataDb, cleans legacy duplicate rows, and triggers MediaScanner.
     */
    public static IngestResult ingestEpub(Context ctx, File epubFile, String sourceLabel, boolean deleteSourceEpubAfter) {
        if (ctx == null || epubFile == null || !epubFile.exists() || epubFile.length() < 128) {
            return new IngestResult(false, "", null, null, null, "Source EPUB missing or empty");
        }
        try {
            String dispName = (sourceLabel != null && !sourceLabel.trim().isEmpty())
                    ? sourceLabel.trim() : epubFile.getName();
            EpubMetadataExtractor.BookMeta meta = EpubMetadataExtractor.extract(epubFile, dispName);
            String bookKey = FolioMetadataDb.computeBookKey(meta.title, epubFile.length());
            File targetDir = getCanonicalLibraryDir(ctx);
            String safeBase = sanitizeFilename(meta.title) + "_" + bookKey;
            File outKfx = new File(targetDir, safeBase + ".kfx");
            File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

            boolean converted = ExternalEpubActivity.convertEpubToKfx(ctx, epubFile, outKfx);
            if (!converted || !outKfx.exists() || outKfx.length() < 128) {
                return new IngestResult(false, bookKey, outKfx, outCover, meta, "KFX conversion failed");
            }

            if (deleteSourceEpubAfter) {
                try { epubFile.delete(); } catch (Throwable ignored) {}
            }

            try { MetadataEnricher.enrichInPlace(meta); } catch (Throwable ignored) {}

            FolioMetadataDb db = new FolioMetadataDb(ctx);
            try {
                db.upsertBook(bookKey, outKfx, outCover, dispName, meta);
            } finally {
                db.close();
            }

            if (!outKfx.getAbsolutePath().startsWith("/sdcard/kindle")) {
                FolioMetadataDb.registerInKindleLibraryDb(ctx, bookKey, outKfx, outCover, meta);
            } else {
                FolioMetadataDb.cleanupLegacyDuplicateRows(ctx);
            }

            try {
                MediaScannerConnection.scanFile(ctx, new String[] { outKfx.getAbsolutePath() }, null, null);
            } catch (Throwable ignored) {}
            try { dp.g.ingestKfxLive(outKfx); } catch (Throwable ignored) {}

            Log.i(TAG, "Ingested OK: " + outKfx.getAbsolutePath() + " (" + outKfx.length() + " B)");
            return new IngestResult(true, bookKey, outKfx, outCover, meta, null);
        } catch (Throwable t) {
            Log.e(TAG, "ingestEpub failed for " + epubFile.getName(), t);
            return new IngestResult(false, "", null, null, null, t.getMessage());
        }
    }

    /**
     * Runs once per process on app launch to keep FolioMetadataDb, kindle_library.db, and cache cleanly reconciled.
     */
    public static void reconcileOnLaunchAsync(final Context ctx) {
        if (ctx == null || !sLaunchReconciled.compareAndSet(false, true)) return;
        new Thread(new Runnable() {
            @Override public void run() {
                reconcileLibrary(ctx.getApplicationContext());
            }
        }, "azath-launch-reconcile").start();
    }

    /**
     * Authoritative library accounting pass:
     *  1. Cleans legacy duplicate synthetic AMZNID0/CR!...KFX/0/ rows in kindle_library.db.
     *  2. Removes dead FolioMetadataDb rows whose .kfx file was deleted from disk.
     *  3. Indexes any /sdcard/kindle/*.kfx files missing from FolioMetadataDb.
     *  4. Prunes ONLY orphan files in files/epub_cache/ that do not belong to any active Library book.
     */
    public static int reconcileLibrary(Context ctx) {
        if (ctx == null) return 0;
        int changes = 0;
        try {
            changes += FolioMetadataDb.cleanupLegacyDuplicateRows(ctx);

            Set<String> activeNames = new HashSet<>();
            Set<String> activePaths = new HashSet<>();
            File[] scanDirs = new File[] {
                    new File("/sdcard/kindle"),
                    new File(ctx.getFilesDir(), "folio_library")
            };
            for (File dir : scanDirs) {
                if (dir == null || !dir.isDirectory()) continue;
                File[] files = dir.listFiles();
                if (files == null) continue;
                for (File f : files) {
                    if (f.isFile() && f.getName().toLowerCase(Locale.US).endsWith(".kfx") && f.length() >= 128) {
                        activePaths.add(f.getAbsolutePath());
                        activeNames.add(f.getName().toLowerCase(Locale.US));
                    }
                }
            }

            // Reconcile FolioMetadataDb against disk ground truth
            FolioMetadataDb db = new FolioMetadataDb(ctx);
            try {
                List<FolioMetadataDb.BookRecord> recs = db.getAllBooks();
                Set<String> indexedPaths = new HashSet<>();
                for (FolioMetadataDb.BookRecord r : recs) {
                    if (r.kfxPath == null || r.kfxPath.isEmpty()) continue;
                    File f = ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(r.kfxPath));
                    if (f == null || !f.exists() || f.length() < 128) {
                        try {
                            db.deleteBook(r.bookKey);
                            changes++;
                        } catch (Throwable ignored) {}
                    } else {
                        indexedPaths.add(f.getAbsolutePath());
                    }
                }

                // Backfill any on-disk /sdcard/kindle/*.kfx missing from FolioMetadataDb
                for (String path : activePaths) {
                    if (indexedPaths.contains(path)) continue;
                    File kfx = new File(path);
                    String baseTitle = kfx.getName();
                    if (baseTitle.toLowerCase(Locale.US).endsWith(".kfx")) {
                        baseTitle = baseTitle.substring(0, baseTitle.length() - 4);
                    }
                    int fi = baseTitle.indexOf("_FOLIO_");
                    if (fi > 0) baseTitle = baseTitle.substring(0, fi);
                    baseTitle = baseTitle.replace('_', ' ').trim();
                    if (baseTitle.isEmpty()) baseTitle = kfx.getName();

                    EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                    meta.title = baseTitle;
                    meta.author = "Unknown Author";
                    String key = FolioMetadataDb.computeBookKey(baseTitle, kfx.length());
                    File cov = new File(kfx.getAbsolutePath() + ".cover.jpg");
                    db.upsertBook(key, kfx, cov.exists() ? cov : null, kfx.getName(), meta);
                    changes++;
                }
            } finally {
                db.close();
            }

            // Prune ONLY unreferenced orphan files in files/epub_cache/ (preserving any file matching an active book)
            File epubCache = new File(ctx.getFilesDir(), "epub_cache");
            if (epubCache.isDirectory()) {
                File[] cached = epubCache.listFiles();
                if (cached != null) {
                    for (File cf : cached) {
                        String cn = cf.getName().toLowerCase(Locale.US);
                        boolean referenced = false;
                        for (String an : activeNames) {
                            if (cn.startsWith(an.replace(".kfx", ""))) {
                                referenced = true;
                                break;
                            }
                        }
                        if (!referenced) {
                            deleteRecursive(cf);
                            changes++;
                        }
                    }
                }
            }

            Log.i(TAG, "reconcileLibrary complete: activeKfx=" + activePaths.size() + " changes=" + changes);
        } catch (Throwable t) {
            Log.w(TAG, "reconcileLibrary warning: " + t.getMessage());
        }
        return changes;
    }

    private static void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) {
                for (File k : kids) deleteRecursive(k);
            }
        }
        try { f.delete(); } catch (Throwable ignored) {}
    }

    public static void restartToRefreshLibrary(final Context ctx) {
        if (ctx == null) return;
        try {
            Intent li = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
            if (li != null && li.getComponent() != null) {
                Intent restart = Intent.makeRestartActivityTask(li.getComponent());
                ctx.startActivity(restart);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 220);
            }
        } catch (Throwable ignored) {}
    }

    public static void promptLibraryRefresh(final Context ctx, String title, String message) {
        if (ctx == null) return;
        try {
            AlertDialog dlg = new AlertDialog.Builder(ctx)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Refresh Library Now", (d, w) -> restartToRefreshLibrary(ctx))
                    .setNegativeButton("Keep Browsing", null)
                    .show();
            FolioTheme.applyToDialog(ctx, dlg);
        } catch (Throwable ignored) {}
    }
}
