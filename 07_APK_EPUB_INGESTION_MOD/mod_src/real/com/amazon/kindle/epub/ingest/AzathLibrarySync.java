package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.util.Log;
import com.amazon.kindle.epub.catalog.FolioTheme;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
    public static final ReentrantLock SYNC_LOCK = new ReentrantLock();
    private static final long RECENT_KFX_WINDOW_MS = 30_000L;
    private static final long STALE_PART_AGE_MS = 3_600_000L;

    public static final class IngestResult {
        public final boolean ok;
        public final String bookKey;
        public final File kfxFile;
        public final File coverFile;
        public final EpubMetadataExtractor.BookMeta meta;
        public final String error;
        public final boolean registered;
        public final boolean liveIndexed;

        IngestResult(boolean ok, String bookKey, File kfxFile, File coverFile,
                     EpubMetadataExtractor.BookMeta meta, String error) {
            this(ok, bookKey, kfxFile, coverFile, meta, error, false, false);
        }

        IngestResult(boolean ok, String bookKey, File kfxFile, File coverFile,
                     EpubMetadataExtractor.BookMeta meta, String error,
                     boolean registered, boolean liveIndexed) {
            this.ok = ok;
            this.bookKey = bookKey;
            this.kfxFile = kfxFile;
            this.coverFile = coverFile;
            this.meta = meta;
            this.error = error;
            this.registered = registered;
            this.liveIndexed = liveIndexed;
        }
    }

    private AzathLibrarySync() {}

    public static File getCanonicalLibraryDir(Context ctx) {
        File sdKindle = new File("/sdcard/kindle");
        if (!sdKindle.exists()) {
            try { sdKindle.mkdirs(); } catch (Throwable ignored) {}
        }
        if (sdKindle.isDirectory() && sdKindle.canWrite()) {
            return sdKindle;
        }
        File priv = new File(ctx.getFilesDir(), "folio_library");
        if (!priv.exists()) priv.mkdirs();
        return priv;
    }

    /**
     * Pure, Android-free KFX filename base sanitizer.
     * Strips path separators, NUL/control chars, and filesystem-hostile chars (:*?"<>|),
     * collapses whitespace/underscores, caps total length <= 80 chars, and falls back to
     * "Book" + "_" + bookKey when title is empty or non-ASCII-only.
     */
    public static String sanitizeFilenameBase(String title, String bookKey) {
        String s = title == null ? "" : title.trim();
        // Strip NUL/control chars, path separators, and filesystem-hostile chars
        s = s.replaceAll("[\\u0000-\\u001F\\u007F/\\\\:*?\"<>|]+", "_");
        s = s.replaceAll("[^A-Za-z0-9._-]+", "_");
        s = s.replaceAll("_+", "_").replaceAll("^[._]+|[._]+$", "");
        if (s.isEmpty()) s = "Book";
        String key = (bookKey != null) ? bookKey.replaceAll("[^A-Za-z0-9_-]+", "") : "";
        int maxTitleLen = key.isEmpty() ? 80 : Math.max(12, 80 - 1 - key.length());
        if (s.length() > maxTitleLen) {
            s = s.substring(0, maxTitleLen).replaceAll("[._]+$", "");
            if (s.isEmpty()) s = "Book";
        }
        return key.isEmpty() ? s : (s + "_" + key);
    }

    /**
     * Canonical EPUB -> KFX ingestion pipeline. Always writes into /sdcard/kindle/ (or private fallback),
     * enriches metadata, updates FolioMetadataDb, cleans legacy duplicate rows, and triggers MediaScanner.
     */
    public static IngestResult ingestEpub(Context ctx, File epubFile, String sourceLabel, boolean deleteSourceEpubAfter) {
        if (ctx == null || epubFile == null || !epubFile.exists() || epubFile.length() < 128) {
            return new IngestResult(false, "", null, null, null, "Source EPUB missing or empty");
        }
        SYNC_LOCK.lock();
        try {
            String dispName = (sourceLabel != null && !sourceLabel.trim().isEmpty())
                    ? sourceLabel.trim() : epubFile.getName();
            EpubMetadataExtractor.BookMeta meta = EpubMetadataExtractor.extract(epubFile, dispName);
            String contentHash = FolioMetadataDb.computeContentHash(epubFile);
            String bookKey = !contentHash.isEmpty()
                    ? FolioMetadataDb.computeBookKeyFromHash(contentHash)
                    : FolioMetadataDb.computeBookKey(meta.title, epubFile.length());
            File targetDir = getCanonicalLibraryDir(ctx);
            String safeBase = sanitizeFilenameBase(meta.title, bookKey);
            File outKfx = new File(targetDir, safeBase + ".kfx");
            File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

            boolean converted = ExternalEpubActivity.convertEpubToKfx(ctx, epubFile, outKfx);
            if (!converted || !outKfx.exists() || outKfx.length() < 128) {
                return new IngestResult(false, bookKey, outKfx, outCover, meta, "KFX conversion failed");
            }

            long sourceSize = epubFile.length();
            FolioMetadataDb db = new FolioMetadataDb(ctx);
            try {
                db.upsertBook(bookKey, outKfx, outCover, dispName, meta, contentHash, sourceSize);
            } catch (Throwable dbErr) {
                Log.e(TAG, "FolioMetadataDb upsert failed; keeping source EPUB", dbErr);
                return new IngestResult(false, bookKey, outKfx, outCover, meta,
                        "Metadata DB upsert failed: " + dbErr.getMessage());
            } finally {
                db.close();
            }

            boolean registered = true;
            if (!outKfx.getAbsolutePath().startsWith("/sdcard/kindle")) {
                registered = FolioMetadataDb.registerInKindleLibraryDb(ctx, bookKey, outKfx, outCover, meta);
                if (!registered) {
                    Log.e(TAG, "kindle_library.db registration failed on private-dir path; keeping source EPUB");
                    return new IngestResult(false, bookKey, outKfx, outCover, meta,
                            "kindle_library.db registration failed", false, false);
                }
            } else {
                FolioMetadataDb.cleanupLegacyDuplicateRows(ctx);
            }

            if (deleteSourceEpubAfter) {
                try {
                    if (!epubFile.delete()) {
                        Log.w(TAG, "Failed to delete source EPUB after durable ingest: " + epubFile.getAbsolutePath());
                    }
                } catch (Throwable delErr) {
                    Log.w(TAG, "Exception deleting source EPUB after durable ingest", delErr);
                }
            }

            try {
                MediaScannerConnection.scanFile(ctx, new String[] { outKfx.getAbsolutePath() }, null, null);
            } catch (Throwable ignored) {}
            boolean liveIndexed = false;
            try {
                liveIndexed = dp.g.ingestKfxLive(outKfx);
            } catch (Throwable t) {
                Log.w(TAG, "ingestKfxLive exception: " + t.getClass().getName() + ": " + t.getMessage(), t);
            }

            MetadataEnricher.enqueueAsync(ctx, bookKey, outKfx, outCover, dispName, meta, contentHash, sourceSize);

            Log.i(TAG, "Ingested OK: " + outKfx.getAbsolutePath() + " (" + outKfx.length() + " B, liveIndexed=" + liveIndexed + ")");
            return new IngestResult(true, bookKey, outKfx, outCover, meta, null, registered, liveIndexed);
        } catch (Throwable t) {
            Log.e(TAG, "ingestEpub failed for " + epubFile.getName(), t);
            return new IngestResult(false, "", null, null, null, t.getMessage());
        } finally {
            SYNC_LOCK.unlock();
        }
    }

    /**
     * Runs once per process on app launch to keep FolioMetadataDb, kindle_library.db, and cache cleanly reconciled,
     * then sweeps unenriched books for asynchronous Open Library enrichment when enabled and online.
     */
    public static void reconcileOnLaunchAsync(final Context ctx) {
        if (ctx == null || !sLaunchReconciled.compareAndSet(false, true)) return;
        startThreadSnapshotDiagIfEnabled(ctx.getApplicationContext());
        new Thread(new Runnable() {
            @Override public void run() {
                Context appCtx = ctx.getApplicationContext();
                reconcileLibrary(appCtx);
                MetadataEnricher.sweepUnenrichedAsync(appCtx);
            }
        }, "azath-launch-reconcile").start();
    }

    private static volatile boolean sThreadDiagStarted = false;

    public static void startThreadSnapshotDiagIfEnabled(final Context ctx) {
        if (ctx == null || sThreadDiagStarted) return;
        boolean enabled = false;
        try {
            if (new File("/sdcard/kindle/.diag_threads").exists()) {
                enabled = true;
            } else {
                enabled = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .getBoolean("diag_thread_snap_enabled", false);
            }
        } catch (Throwable ignored) {}
        if (!enabled) return;
        sThreadDiagStarted = true;
        Thread diag = new Thread(new Runnable() {
            @Override public void run() {
                long startMs = System.currentTimeMillis();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        long sec = (System.currentTimeMillis() - startMs) / 1000L;
                        java.util.Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
                        StringBuilder az = new StringBuilder();
                        StringBuilder oth = new StringBuilder();
                        int azCount = 0;
                        int total = all.size();
                        for (Thread t : all.keySet()) {
                            String n = t.getName();
                            if (n == null) continue;
                            if (n.contains("azath") || n.contains("folio") || n.contains("Thread-20") || n.contains("Thread-22")) {
                                if (az.length() > 0) az.append(',');
                                az.append(n).append(':').append(t.getState());
                                azCount++;
                            } else if (n.startsWith("Thread-") || n.contains("pool-")) {
                                if (oth.length() > 0 && oth.length() < 240) oth.append(',');
                                if (oth.length() < 240) oth.append(n);
                            }
                        }
                        Log.i(TAG, "ThreadSnap t=" + sec + "s total=" + total
                                + " syncLocked=" + SYNC_LOCK.isLocked()
                                + " azath=[" + az + "] workers=[" + oth + "]");
                        Thread.sleep(5000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Throwable ignored) {}
                }
            }
        }, "azath-diag-threads");
        diag.setDaemon(true);
        diag.start();
    }

    public static final class BookEntry {
        public final String bookKey;
        public final String kfxPath;
        public final boolean existsOnDisk;

        public BookEntry(String bookKey, String kfxPath, boolean existsOnDisk) {
            this.bookKey = bookKey;
            this.kfxPath = kfxPath;
            this.existsOnDisk = existsOnDisk;
        }
    }

    public static final class ScanResult {
        public final boolean sdKindleHealthy;
        public final boolean privateDirHealthy;
        public final Set<String> activePaths;
        public final Set<String> activeNamesLower;

        public ScanResult(boolean sdKindleHealthy, boolean privateDirHealthy,
                          Set<String> activePaths, Set<String> activeNamesLower) {
            this.sdKindleHealthy = sdKindleHealthy;
            this.privateDirHealthy = privateDirHealthy;
            this.activePaths = activePaths != null ? activePaths : Collections.<String>emptySet();
            this.activeNamesLower = activeNamesLower != null ? activeNamesLower : Collections.<String>emptySet();
        }
    }

    public static final class ReconcilePlan {
        public final boolean skipDestructive;
        public final boolean circuitBreakerTripped;
        public final String warningReason;
        public final List<String> dbKeysToDelete;
        public final List<String> backfillPaths;
        public final List<String> cacheNamesToPrune;

        public ReconcilePlan(boolean skipDestructive, boolean circuitBreakerTripped,
                             String warningReason, List<String> dbKeysToDelete,
                             List<String> backfillPaths, List<String> cacheNamesToPrune) {
            this.skipDestructive = skipDestructive;
            this.circuitBreakerTripped = circuitBreakerTripped;
            this.warningReason = warningReason;
            this.dbKeysToDelete = dbKeysToDelete;
            this.backfillPaths = backfillPaths;
            this.cacheNamesToPrune = cacheNamesToPrune;
        }
    }

    /**
     * Exact base-name boundary matcher for files/epub_cache/ entries.
     * Cache entries observed on device are <base>.epub.kfx, <base>.epub.kfx.cover.jpg,
     * <base>.epub.sdr (as well as <base>.kfx, <base>.kfx.cover.jpg, <base>.sdr, <base>.prc, <base>.azw3).
     * Never uses loose startsWith() so "book1" cannot falsely match "book10".
     */
    public static boolean isCacheEntryReferenced(String cacheFileNameLower, Set<String> activeNamesLower) {
        if (cacheFileNameLower == null || cacheFileNameLower.isEmpty() || activeNamesLower == null) {
            return false;
        }
        for (String an : activeNamesLower) {
            if (an == null || !an.endsWith(".kfx")) continue;
            String base = an.substring(0, an.length() - 4);
            if (base.isEmpty()) continue;
            if (cacheFileNameLower.equals(base + ".kfx")
                    || cacheFileNameLower.equals(base + ".kfx.cover.jpg")
                    || cacheFileNameLower.equals(base + ".sdr")
                    || cacheFileNameLower.equals(base + ".epub.kfx")
                    || cacheFileNameLower.equals(base + ".epub.kfx.cover.jpg")
                    || cacheFileNameLower.equals(base + ".epub.sdr")
                    || cacheFileNameLower.equals(base + ".prc")
                    || cacheFileNameLower.equals(base + ".azw3")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrivateLibraryDirHealthy(File privDir) {
        if (privDir == null) return false;
        if (!privDir.exists()) return true; // healthy-empty before first private-dir write
        return privDir.isDirectory() && privDir.canRead() && privDir.listFiles() != null;
    }

    public static boolean isPartArtifactName(String lowerName) {
        if (lowerName == null || lowerName.isEmpty()) return false;
        return lowerName.endsWith(".part") || lowerName.contains(".kfx.part.");
    }

    public static final class ReconcilePreview {
        public final ReconcilePlan forcedPlan;
        public final boolean circuitBreakerWouldTrip;
        public final boolean sdKindleHealthy;
        public final boolean privateDirHealthy;
        public final int activeKfxCount;
        public final List<String> dbDeleteDetails;
        public final List<String> cacheDeletePaths;

        public ReconcilePreview(ReconcilePlan forcedPlan, boolean circuitBreakerWouldTrip,
                                boolean sdKindleHealthy, boolean privateDirHealthy,
                                int activeKfxCount, List<String> dbDeleteDetails,
                                List<String> cacheDeletePaths) {
            this.forcedPlan = forcedPlan;
            this.circuitBreakerWouldTrip = circuitBreakerWouldTrip;
            this.sdKindleHealthy = sdKindleHealthy;
            this.privateDirHealthy = privateDirHealthy;
            this.activeKfxCount = activeKfxCount;
            this.dbDeleteDetails = dbDeleteDetails != null ? dbDeleteDetails : Collections.<String>emptyList();
            this.cacheDeletePaths = cacheDeletePaths != null ? cacheDeletePaths : Collections.<String>emptyList();
        }
    }

    /**
     * Pure, Android-free reconcile planner for JVM unit testing and runtime execution.
     */
    public static ReconcilePlan planReconcile(
            ScanResult scan,
            List<BookEntry> existingRecords,
            List<String> cacheEntryNames) {
        return planReconcile(scan, existingRecords, cacheEntryNames, false);
    }

    public static ReconcilePlan planReconcile(
            ScanResult scan,
            List<BookEntry> existingRecords,
            List<String> cacheEntryNames,
            boolean force) {
        List<BookEntry> recs = existingRecords != null ? existingRecords : Collections.<BookEntry>emptyList();
        boolean sdKindleInUse = false;
        for (BookEntry r : recs) {
            if (r.kfxPath != null && r.kfxPath.startsWith("/sdcard/kindle")) {
                sdKindleInUse = true;
                break;
            }
        }

        if (sdKindleInUse && !scan.sdKindleHealthy) {
            return new ReconcilePlan(
                    true,
                    false,
                    "Skipping destructive reconcile: /sdcard/kindle scan unhealthy while DB rows reference /sdcard/kindle",
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList());
        }

        List<String> candidateDeletes = new ArrayList<>();
        Set<String> indexedPaths = new HashSet<>();
        for (BookEntry r : recs) {
            if (r.kfxPath == null || r.kfxPath.isEmpty()) continue;
            if (!r.existsOnDisk) {
                if (r.bookKey != null && !r.bookKey.isEmpty()) {
                    candidateDeletes.add(r.bookKey);
                }
            } else {
                indexedPaths.add(r.kfxPath);
            }
        }

        int totalRows = recs.size();
        boolean rawBreaker = candidateDeletes.size() > 10
                || (totalRows > 0 && candidateDeletes.size() * 2 > totalRows);
        boolean circuitTripped = !force && rawBreaker;
        String warn = null;
        if (circuitTripped) {
            warn = "Circuit breaker tripped: would delete " + candidateDeletes.size()
                    + " of " + totalRows + " DB rows; aborting all deletes";
            candidateDeletes = Collections.<String>emptyList();
        } else if (force && rawBreaker) {
            warn = "Circuit breaker bypassed (force=true): scheduling " + candidateDeletes.size()
                    + " of " + totalRows + " missing DB row deletes";
        }

        List<String> backfill = new ArrayList<>();
        for (String path : scan.activePaths) {
            if (!indexedPaths.contains(path)) {
                backfill.add(path);
            }
        }

        List<String> cachePrune = new ArrayList<>();
        boolean allScansHealthy = scan.sdKindleHealthy && scan.privateDirHealthy;
        if (!circuitTripped && allScansHealthy && !scan.activeNamesLower.isEmpty() && cacheEntryNames != null) {
            for (String cn : cacheEntryNames) {
                if (cn == null) continue;
                if (!isCacheEntryReferenced(cn.toLowerCase(Locale.US), scan.activeNamesLower)) {
                    cachePrune.add(cn);
                }
            }
        }

        return new ReconcilePlan(false, circuitTripped, warn, candidateDeletes, backfill, cachePrune);
    }

    public static ReconcilePreview previewReconcile(Context ctx) {
        if (ctx == null) {
            return new ReconcilePreview(
                    new ReconcilePlan(true, false, "Context is null",
                            Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList()),
                    false, false, false, 0, Collections.<String>emptyList(), Collections.<String>emptyList());
        }
        SYNC_LOCK.lock();
        try {
            long now = System.currentTimeMillis();
            Set<String> activeNames = new HashSet<>();
            Set<String> activePaths = new HashSet<>();
            File sdDir = new File("/sdcard/kindle");
            File privDir = new File(ctx.getFilesDir(), "folio_library");

            File[] sdFiles = (sdDir.isDirectory() && sdDir.canRead()) ? sdDir.listFiles() : null;
            boolean sdHealthy = sdDir.isDirectory() && sdDir.canRead() && sdFiles != null;
            if (sdHealthy) {
                for (File f : sdFiles) {
                    String lower = f.getName().toLowerCase(Locale.US);
                    if (f.isFile() && lower.endsWith(".kfx") && f.length() >= 128) {
                        if (now - f.lastModified() >= RECENT_KFX_WINDOW_MS) {
                            activePaths.add(f.getAbsolutePath());
                        }
                        activeNames.add(lower);
                    }
                }
            }

            boolean privHealthy = isPrivateLibraryDirHealthy(privDir);
            File[] privFiles = (privDir.isDirectory() && privDir.canRead()) ? privDir.listFiles() : null;
            if (privHealthy && privFiles != null) {
                for (File f : privFiles) {
                    String lower = f.getName().toLowerCase(Locale.US);
                    if (f.isFile() && lower.endsWith(".kfx") && f.length() >= 128) {
                        if (now - f.lastModified() >= RECENT_KFX_WINDOW_MS) {
                            activePaths.add(f.getAbsolutePath());
                        }
                        activeNames.add(lower);
                    }
                }
            }

            ScanResult scan = new ScanResult(sdHealthy, privHealthy, activePaths, activeNames);
            File epubCache = new File(ctx.getFilesDir(), "epub_cache");
            List<String> cacheNames = new ArrayList<>();
            if (epubCache.isDirectory()) {
                File[] cached = epubCache.listFiles();
                if (cached != null) {
                    for (File cf : cached) cacheNames.add(cf.getName());
                }
            }

            FolioMetadataDb db = new FolioMetadataDb(ctx);
            ReconcilePlan safePlan;
            ReconcilePlan forcedPlan;
            List<String> deleteDetails = new ArrayList<>();
            try {
                List<FolioMetadataDb.BookRecord> recs = db.getAllBooks();
                List<BookEntry> entries = new ArrayList<>(recs.size());
                java.util.Map<String, FolioMetadataDb.BookRecord> byKey = new java.util.HashMap<>();
                for (FolioMetadataDb.BookRecord r : recs) {
                    File resolved = (r.kfxPath != null && !r.kfxPath.isEmpty())
                            ? ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(r.kfxPath))
                            : null;
                    boolean onDisk = resolved != null && resolved.exists() && resolved.length() >= 128;
                    entries.add(new BookEntry(r.bookKey, onDisk ? resolved.getAbsolutePath() : r.kfxPath, onDisk));
                    if (r.bookKey != null) byKey.put(r.bookKey, r);
                }
                safePlan = planReconcile(scan, entries, cacheNames, false);
                forcedPlan = planReconcile(scan, entries, cacheNames, true);
                for (String key : forcedPlan.dbKeysToDelete) {
                    FolioMetadataDb.BookRecord rec = byKey.get(key);
                    String title = (rec != null && rec.title != null && !rec.title.isEmpty()) ? rec.title : "(untitled)";
                    String path = (rec != null && rec.kfxPath != null) ? rec.kfxPath : "";
                    deleteDetails.add(title + " [" + key + "] -> " + path);
                }
            } finally {
                db.close();
            }

            List<String> cachePaths = new ArrayList<>();
            for (String cn : forcedPlan.cacheNamesToPrune) {
                cachePaths.add(new File(epubCache, cn).getAbsolutePath());
            }
            return new ReconcilePreview(forcedPlan, safePlan.circuitBreakerTripped,
                    sdHealthy, privHealthy, activePaths.size(), deleteDetails, cachePaths);
        } finally {
            SYNC_LOCK.unlock();
        }
    }

    /**
     * Authoritative library accounting pass:
     *  1. Cleans legacy duplicate synthetic AMZNID0/CR!...KFX/0/ rows in kindle_library.db.
     *  2. Removes dead FolioMetadataDb rows whose .kfx file was deleted from disk (guarded by scan health + circuit breaker).
     *  3. Indexes any /sdcard/kindle/*.kfx files missing from FolioMetadataDb.
     *  4. Prunes ONLY orphan files in files/epub_cache/ that do not belong to any active Library book.
     */
    public static int reconcileLibrary(Context ctx) {
        return reconcileLibrary(ctx, false);
    }

    public static int reconcileLibrary(Context ctx, boolean force) {
        if (ctx == null) return 0;
        int changes = 0;
        SYNC_LOCK.lock();
        try {
            long now = System.currentTimeMillis();
            changes += FolioMetadataDb.cleanupLegacyDuplicateRows(ctx);

            Set<String> activeNames = new HashSet<>();
            Set<String> activePaths = new HashSet<>();
            File sdDir = new File("/sdcard/kindle");
            File privDir = new File(ctx.getFilesDir(), "folio_library");

            File[] sdFiles = (sdDir.isDirectory() && sdDir.canRead()) ? sdDir.listFiles() : null;
            boolean sdHealthy = sdDir.isDirectory() && sdDir.canRead() && sdFiles != null;
            if (!sdHealthy) {
                Log.w(TAG, "Scan unhealthy for /sdcard/kindle (exists=" + sdDir.exists()
                        + " isDir=" + sdDir.isDirectory() + " canRead=" + sdDir.canRead() + ")");
            } else {
                for (File f : sdFiles) {
                    String lower = f.getName().toLowerCase(Locale.US);
                    int staleHandled = handleStalePartEntry(f, lower, now);
                    if (staleHandled >= 0) {
                        changes += staleHandled;
                        continue;
                    }
                    if (f.isFile() && lower.endsWith(".kfx") && f.length() >= 128) {
                        if (now - f.lastModified() < RECENT_KFX_WINDOW_MS) {
                            Log.i(TAG, "Skipping recently modified .kfx (<30s): " + f.getAbsolutePath());
                            activeNames.add(lower);
                            continue;
                        }
                        activePaths.add(f.getAbsolutePath());
                        activeNames.add(lower);
                    }
                }
            }

            boolean privHealthy = isPrivateLibraryDirHealthy(privDir);
            File[] privFiles = (privDir.isDirectory() && privDir.canRead()) ? privDir.listFiles() : null;
            if (!privHealthy) {
                Log.w(TAG, "Scan unhealthy for private library dir (exists=" + privDir.exists()
                        + " isDir=" + privDir.isDirectory() + " canRead=" + privDir.canRead() + ")");
            } else if (privFiles != null) {
                for (File f : privFiles) {
                    String lower = f.getName().toLowerCase(Locale.US);
                    int staleHandled = handleStalePartEntry(f, lower, now);
                    if (staleHandled >= 0) {
                        changes += staleHandled;
                        continue;
                    }
                    if (f.isFile() && lower.endsWith(".kfx") && f.length() >= 128) {
                        if (now - f.lastModified() < RECENT_KFX_WINDOW_MS) {
                            Log.i(TAG, "Skipping recently modified .kfx (<30s): " + f.getAbsolutePath());
                            activeNames.add(lower);
                            continue;
                        }
                        activePaths.add(f.getAbsolutePath());
                        activeNames.add(lower);
                    }
                }
            }

            ScanResult scan = new ScanResult(sdHealthy, privHealthy, activePaths, activeNames);

            File epubCache = new File(ctx.getFilesDir(), "epub_cache");
            List<String> cacheNames = new ArrayList<>();
            if (epubCache.isDirectory()) {
                File[] cached = epubCache.listFiles();
                if (cached != null) {
                    for (File cf : cached) cacheNames.add(cf.getName());
                }
            }

            // Reconcile FolioMetadataDb against disk ground truth
            FolioMetadataDb db = new FolioMetadataDb(ctx);
            ReconcilePlan plan;
            try {
                db.ensureGenresMigrated();
                List<FolioMetadataDb.BookRecord> recs = db.getAllBooks();
                List<BookEntry> entries = new ArrayList<>(recs.size());
                for (FolioMetadataDb.BookRecord r : recs) {
                    File resolved = (r.kfxPath != null && !r.kfxPath.isEmpty())
                            ? ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(r.kfxPath))
                            : null;
                    boolean onDisk = resolved != null && resolved.exists() && resolved.length() >= 128;
                    entries.add(new BookEntry(r.bookKey, onDisk ? resolved.getAbsolutePath() : r.kfxPath, onDisk));
                }

                plan = planReconcile(scan, entries, cacheNames, force);
                if (plan.warningReason != null) {
                    Log.w(TAG, plan.warningReason);
                }

                if (!plan.skipDestructive && !plan.circuitBreakerTripped) {
                    for (String delKey : plan.dbKeysToDelete) {
                        try {
                            if (db.deleteBook(delKey)) {
                                Log.i(TAG, "Deleted stale DB row book_key=" + delKey + " reason=kfx_missing_on_disk");
                                changes++;
                            } else {
                                Log.w(TAG, "deleteBook returned false for book_key=" + delKey);
                            }
                        } catch (Throwable t) {
                            Log.w(TAG, "Failed deleting DB row " + delKey, t);
                        }
                    }
                }

                // Backfill any on-disk /sdcard/kindle/*.kfx missing from FolioMetadataDb
                for (String path : plan.backfillPaths) {
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
                    String key = FolioMetadataDb.extractBookKeyFromFilename(kfx.getName());
                    if (key == null || key.isEmpty()) {
                        key = FolioMetadataDb.computeBookKey(baseTitle, kfx.length());
                    }
                    File cov = new File(kfx.getAbsolutePath() + ".cover.jpg");
                    db.upsertBook(key, kfx, cov.exists() ? cov : null, kfx.getName(), meta);
                    changes++;
                }
            } finally {
                db.close();
            }

            // Prune ONLY unreferenced orphan files in files/epub_cache/ when scan is healthy & activeNames non-empty
            if (!plan.skipDestructive && !plan.circuitBreakerTripped && !plan.cacheNamesToPrune.isEmpty()) {
                for (String cn : plan.cacheNamesToPrune) {
                    File cf = new File(epubCache, cn);
                    if (cf.exists()) {
                        deleteRecursive(cf);
                        Log.i(TAG, "Pruned orphan epub_cache entry: " + cf.getAbsolutePath() + " reason=unreferenced_by_active_kfx");
                        changes++;
                    }
                }
            }

            // Sweep stale cache/epub_staging/* and cache/folio_once/* files older than 1 hour
            changes += sweepStaleCacheDir(new File(ctx.getCacheDir(), "epub_staging"), now, "epub_staging");
            changes += sweepStaleCacheDir(new File(ctx.getCacheDir(), "folio_once"), now, "folio_once");

            Log.i(TAG, "reconcileLibrary complete: activeKfx=" + activePaths.size() + " changes=" + changes);
        } catch (Throwable t) {
            Log.w(TAG, "reconcileLibrary warning: " + t.getMessage());
        } finally {
            SYNC_LOCK.unlock();
        }
        return changes;
    }

    private static int sweepStaleCacheDir(File dir, long nowMs, String label) {
        if (dir == null || !dir.isDirectory()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int removed = 0;
        for (File sf : files) {
            if (nowMs - sf.lastModified() > STALE_PART_AGE_MS) {
                deleteRecursive(sf);
                if (!sf.exists()) {
                    Log.i(TAG, "Deleted stale " + label + " entry: " + sf.getAbsolutePath());
                    removed++;
                }
            }
        }
        return removed;
    }

    /** Returns -1 if not a stale/part entry; 1 if deleted; 0 if retained. */
    private static int handleStalePartEntry(File f, String lower, long now) {
        boolean isStaleCandidate = (f.isFile() && isPartArtifactName(lower))
                || (f.isDirectory() && lower.endsWith(".part.sdr"));
        if (!isStaleCandidate) return -1;
        if (now - f.lastModified() > STALE_PART_AGE_MS) {
            deleteRecursive(f);
            if (!f.exists()) {
                Log.i(TAG, "Deleted stale .part artifact: " + f.getAbsolutePath());
                return 1;
            }
        }
        return 0;
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

    /**
     * Opens a newly imported .kfx book directly via ThirdPartyUpgradePage -> StandAloneBookReaderActivity.
     * This natively indexes the book into Kindle's live in-memory Library without MainActivity restart or SIGSYS.
     */
    public static void openImportedBookLive(final Context ctx, final File kfxFile) {
        if (ctx == null || kfxFile == null || !kfxFile.exists()) return;
        try {
            ExternalEpubActivity.launchKfxReader(ctx, kfxFile);
        } catch (Throwable ignored) {}
    }
}
