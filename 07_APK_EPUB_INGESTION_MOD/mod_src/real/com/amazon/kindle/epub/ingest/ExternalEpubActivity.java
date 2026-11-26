package com.amazon.kindle.epub.ingest;

import com.amazon.kindle.epub.catalog.FolioTheme;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cleanly intercepts external EPUB open intents (file:// or content:// from ANY
 * storage directory), eliminating Kindle's hanging external-source loading wheel.
 * Includes duplicate library detection: if already imported, offers instant
 * "Open Existing Copy" alongside "Re-Import & Replace" and "Open Once".
 */
public class ExternalEpubActivity extends Activity {

    private static final String TAG = "FolioExternalEpub";
    private static final long MAX_EPUB_BYTES = 250L * 1024L * 1024L;
    private static final long KFX_CONV_TIMEOUT_SEC = 120L;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog activeDialog;

    public static boolean interceptIfExternalEpub(Activity caller, Intent intent) {
        if (caller == null || intent == null) return false;
        if (intent.getBooleanExtra("folio_bypass_intercept", false)) return false;
        Uri uri = extractUri(intent);
        if (uri == null) return false;
        if (!isEpubIntent(caller, intent, uri)) return false;

        Intent fwd = new Intent(caller, ExternalEpubActivity.class);
        fwd.setAction(Intent.ACTION_VIEW);
        fwd.setDataAndType(uri, "application/epub+zip");
        fwd.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        caller.startActivity(fwd);
        caller.finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = extractUri(getIntent());
        if (uri == null || !isSupportedScheme(uri)) {
            Toast.makeText(this, "Unsupported or missing EPUB URI", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (isBlockedPrivateFileUri(this, uri)) {
            Log.w(TAG, "Blocked file:// URI targeting private app storage: " + uri);
            Toast.makeText(this, "Blocked: cannot open app-private storage file", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        startStagingAndPrompt(uri);
    }

    private static boolean isSupportedScheme(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        return "content".equalsIgnoreCase(scheme) || "file".equalsIgnoreCase(scheme);
    }

    private void startStagingAndPrompt(final Uri uri) {
        showProgressDialog("Inspecting EPUB...", "Reading book metadata from storage");
        new Thread(() -> {
            File stagedEpub = null;
            boolean handedOff = false;
            try {
                final String displayName = resolveDisplayName(this, uri);
                File stagingDir = new File(getCacheDir(), "epub_staging");
                if (!stagingDir.exists()) stagingDir.mkdirs();
                stagedEpub = new File(stagingDir, "incoming_" + System.currentTimeMillis() + "_" + System.nanoTime() + ".epub");

                copyUriToFile(this, uri, stagedEpub);
                if (!stagedEpub.exists() || stagedEpub.length() < 64) {
                    throw new IllegalStateException("Could not read EPUB file from storage");
                }

                final EpubMetadataExtractor.BookMeta meta =
                        EpubMetadataExtractor.extract(stagedEpub, displayName);
                final String contentHash = FolioMetadataDb.computeContentHash(stagedEpub);
                final String bookKey = !contentHash.isEmpty()
                        ? FolioMetadataDb.computeBookKeyFromHash(contentHash)
                        : FolioMetadataDb.computeBookKey(meta.title, stagedEpub.length());

                FolioMetadataDb.BookRecord existing = null;
                FolioMetadataDb.BookRecord possibleDup = null;
                FolioMetadataDb db = new FolioMetadataDb(this);
                try {
                    existing = db.findExisting(bookKey, contentHash);
                    if (existing == null) {
                        possibleDup = db.findPossibleDuplicate(meta.title, meta.author);
                    }
                } finally {
                    db.close();
                }

                final File finalStaged = stagedEpub;
                final FolioMetadataDb.BookRecord finalExisting = existing;
                final FolioMetadataDb.BookRecord finalPossibleDup = possibleDup;
                handedOff = true;
                mainHandler.post(() -> {
                    if (com.amazon.kindle.epub.catalog.FolioTheme.isContextDead(this)) {
                        if (finalStaged.exists()) finalStaged.delete();
                        return;
                    }
                    showChoiceDialog(finalStaged, displayName, meta, bookKey, contentHash, finalExisting, finalPossibleDup);
                });
            } catch (Throwable t) {
                Log.e(TAG, "Failed to inspect incoming EPUB", t);
                mainHandler.post(() -> {
                    if (com.amazon.kindle.epub.catalog.FolioTheme.isContextDead(this)) return;
                    dismissActiveDialog();
                    Toast.makeText(this, "Could not open EPUB: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            } finally {
                if (!handedOff && stagedEpub != null && stagedEpub.exists()) {
                    stagedEpub.delete();
                }
            }
        }, "folio-epub-stage").start();
    }

    private void showChoiceDialog(final File stagedEpub, final String displayName,
                                  final EpubMetadataExtractor.BookMeta meta,
                                  final String bookKey, final String contentHash,
                                  final FolioMetadataDb.BookRecord existing,
                                  final FolioMetadataDb.BookRecord possibleDup) {
        dismissActiveDialog();
        if (isFinishing()) return;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, dp(16));
        root.setBackgroundColor(FolioTheme.bgModal(this));

        TextView badge = new TextView(this);
        badge.setText(existing != null
                ? "ALREADY IN AZATH LIBRARY"
                : (possibleDup != null ? "POSSIBLE DUPLICATE IN LIBRARY" : "AZATH EPUB IMPORT"));
        badge.setTextColor(FolioTheme.accent(this));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(badge);

        TextView titleView = new TextView(this);
        titleView.setText(meta.title);
        titleView.setTextColor(FolioTheme.textPrimary(this));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setPadding(0, dp(6), 0, dp(4));
        root.addView(titleView);

        StringBuilder sub = new StringBuilder();
        sub.append(meta.author);
        if (!EpubMetadataExtractor.isBlank(meta.pubDate)) {
            sub.append(" · ").append(meta.pubDate);
        }
        TextView authorView = new TextView(this);
        authorView.setText(sub.toString());
        authorView.setTextColor(FolioTheme.textSecondary(this));
        authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        authorView.setPadding(0, 0, 0, dp(12));
        root.addView(authorView);

        TextView desc = new TextView(this);
        if (existing != null) {
            desc.setText("This book is already saved in Azath's app storage. Open your existing copy instantly, or re-import and replace it.");
        } else if (possibleDup != null) {
            desc.setText("Advisory: another file with the same title and author (\"" + possibleDup.title
                    + "\") is already in your library. Importing will keep both as separate editions.");
        } else {
            desc.setText("Add to your Azath library (saves converted book & metadata in app storage) or open once without saving.");
        }
        desc.setTextColor(FolioTheme.textMuted(this));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        desc.setPadding(0, 0, 0, dp(18));
        root.addView(desc);

        if (existing != null) {
            TextView btnOpenExisting = makeButton("Open Existing Copy", FolioTheme.accent(this), FolioTheme.bgCanvas(this));
            btnOpenExisting.setOnClickListener(v -> {
                stagedEpub.delete();
                dismissActiveDialog();
                launchKfxReader(this, new File(existing.kfxPath));
                finish();
            });
            root.addView(btnOpenExisting);

            View sp0 = new View(this);
            sp0.setLayoutParams(new LinearLayout.LayoutParams(1, dp(10)));
            root.addView(sp0);

            TextView btnReimport = makeButton("Re-Import & Replace", FolioTheme.bgControl(this), FolioTheme.textPrimary(this));
            btnReimport.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, bookKey, contentHash, true));
            root.addView(btnReimport);
        } else {
            TextView btnAdd = makeButton("Add to Library & Read", FolioTheme.accent(this), FolioTheme.bgCanvas(this));
            btnAdd.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, bookKey, contentHash, true));
            root.addView(btnAdd);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(1, dp(10)));
            root.addView(spacer);

            TextView btnOnce = makeButton("Open Once (Don't Add)", FolioTheme.bgControl(this), FolioTheme.textPrimary(this));
            btnOnce.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, bookKey, contentHash, false));
            root.addView(btnOnce);
        }

        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(1, dp(8)));
        root.addView(spacer2);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setTextColor(FolioTheme.textMuted(this));
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnCancel.setPadding(0, dp(10), 0, dp(6));
        btnCancel.setOnClickListener(v -> {
            stagedEpub.delete();
            dismissActiveDialog();
            finish();
        });
        root.addView(btnCancel);

        activeDialog = new AlertDialog.Builder(this)
                .setView(root)
                .setCancelable(true)
                .setOnCancelListener(d -> {
                    stagedEpub.delete();
                    finish();
                })
                .create();
        activeDialog.show();
        com.amazon.kindle.epub.catalog.FolioTheme.applyToDialog(this, activeDialog);
    }

    private void processAndOpen(final File stagedEpub, final String displayName,
                                final EpubMetadataExtractor.BookMeta meta,
                                final String bookKey, final String contentHash,
                                final boolean addToLibrary) {
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        showProgressDialog(
                addToLibrary ? "Adding to Azath Library..." : "Preparing Book...",
                addToLibrary ? "Converting to KFX & enriching book metadata"
                             : "Converting EPUB to native KFX",
                () -> {
                    cancelFlag.set(true);
                    if (stagedEpub != null && stagedEpub.exists()) {
                        stagedEpub.delete();
                    }
                    finish();
                });

        new Thread(() -> {
            AzathLibrarySync.SYNC_LOCK.lock();
            try {
                final String safeBase = AzathLibrarySync.sanitizeFilenameBase(meta.title, bookKey);

                File sdKindle = new File("/sdcard/kindle");
                if (addToLibrary && !sdKindle.exists()) sdKindle.mkdirs();
                File targetDir = addToLibrary
                        ? ((sdKindle.isDirectory() && sdKindle.canWrite()) ? sdKindle : new File(getFilesDir(), "folio_library"))
                        : new File(getCacheDir(), "folio_once");
                if (!targetDir.exists()) targetDir.mkdirs();

                File outKfx = new File(targetDir, safeBase + ".kfx");
                File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

                boolean ok = convertEpubToKfx(this, stagedEpub, outKfx, KFX_CONV_TIMEOUT_SEC, cancelFlag);
                if (cancelFlag.get()) {
                    deleteAttemptArtifacts(buildAttemptPartFile(outKfx, "inproc"));
                    deleteAttemptArtifacts(buildAttemptPartFile(outKfx, "native1"));
                    deleteAttemptArtifacts(buildAttemptPartFile(outKfx, "native2"));
                    deleteAttemptArtifacts(new File(outKfx.getParentFile(), outKfx.getName() + ".part"));
                    if (stagedEpub.exists()) stagedEpub.delete();
                    return;
                }
                if (!ok || !outKfx.exists() || outKfx.length() < 128) {
                    throw new IllegalStateException("KFX conversion failed");
                }

                boolean registeredOk = true;
                if (addToLibrary) {
                    long sourceSize = stagedEpub.length();
                    FolioMetadataDb metaDb = new FolioMetadataDb(this);
                    try {
                        metaDb.upsertBook(bookKey, outKfx, outCover, displayName, meta, contentHash, sourceSize);
                    } finally {
                        metaDb.close();
                    }
                    if (!outKfx.getAbsolutePath().startsWith("/sdcard/kindle")) {
                        registeredOk = FolioMetadataDb.registerInKindleLibraryDb(this, bookKey, outKfx, outCover, meta);
                    }
                    boolean liveOk = false;
                    try {
                        liveOk = dp.g.ingestKfxLive(outKfx);
                    } catch (Throwable liveErr) {
                        Log.w(TAG, "dp.g.ingestKfxLive threw for " + outKfx.getName() + ": " + liveErr.getMessage(), liveErr);
                    }
                    if (!liveOk) {
                        Log.w(TAG, "dp.g.ingestKfxLive returned false for " + outKfx.getName());
                    }
                    com.amazon.kindle.epub.catalog.FolioTheme.triggerLibraryRescan(this, outKfx);
                    try { android.media.MediaScannerConnection.scanFile(this, new String[]{ outKfx.getAbsolutePath() }, null, null); } catch (Throwable ignored) {}
                    MetadataEnricher.enqueueAsync(this, bookKey, outKfx, outCover, displayName, meta, contentHash, sourceSize);
                }

                stagedEpub.delete();

                final boolean finalRegisteredOk = registeredOk;
                mainHandler.post(() -> {
                    if (com.amazon.kindle.epub.catalog.FolioTheme.isContextDead(this)) return;
                    dismissActiveDialog();
                    if (addToLibrary) {
                        if (finalRegisteredOk) {
                            Toast.makeText(this, "Added \"" + meta.title + "\" to Azath Library", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Converted \"" + meta.title + "\", but Library DB registration failed", Toast.LENGTH_LONG).show();
                        }
                    }
                    launchKfxReader(this, outKfx);
                    finish();
                });
            } catch (Throwable t) {
                Log.e(TAG, "Failed to process external EPUB", t);
                stagedEpub.delete();
                mainHandler.post(() -> {
                    if (com.amazon.kindle.epub.catalog.FolioTheme.isContextDead(this)) return;
                    dismissActiveDialog();
                    Toast.makeText(this, "Import error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            } finally {
                AzathLibrarySync.SYNC_LOCK.unlock();
            }
        }, "folio-epub-convert").start();
    }

    public static boolean convertEpubToKfx(Context ctx, File epubIn, File kfxOut) {
        return convertEpubToKfx(ctx, epubIn, kfxOut, KFX_CONV_TIMEOUT_SEC, null);
    }

    public static File buildAttemptPartFile(File kfxOut, String attemptTag) {
        if (kfxOut == null) return null;
        File parent = kfxOut.getParentFile();
        String tag = (attemptTag == null) ? "" : attemptTag.replaceAll("[^A-Za-z0-9_-]+", "");
        if (tag.isEmpty()) tag = "attempt";
        return new File(parent != null ? parent : new File("."), kfxOut.getName() + ".part." + tag);
    }

    public static long computeDeadlineNanos(long nowNanos, long totalTimeoutSec) {
        return nowNanos + Math.max(1L, totalTimeoutSec) * 1_000_000_000L;
    }

    public static long remainingTimeoutMs(long nowNanos, long deadlineNanos) {
        long rem = (deadlineNanos - nowNanos) / 1_000_000L;
        return rem > 0L ? rem : 0L;
    }

    public static boolean shouldRunFallback(
            boolean primaryOk, boolean primaryTimedOut, boolean cancelled, long nowNanos, long deadlineNanos) {
        return !primaryOk && !primaryTimedOut && !cancelled && remainingTimeoutMs(nowNanos, deadlineNanos) > 0L;
    }

    private static void deleteAttemptArtifacts(File partFile) {
        if (partFile == null) return;
        if (partFile.exists()) partFile.delete();
        File partCover = new File(partFile.getAbsolutePath() + ".cover.jpg");
        if (partCover.exists()) partCover.delete();
    }

    public static boolean convertEpubToKfx(Context ctx, File epubIn, File kfxOut,
                                           long timeoutSec, AtomicBoolean cancelFlag) {
        if (kfxOut == null) return false;
        File parent = kfxOut.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        final File partInproc = buildAttemptPartFile(kfxOut, "inproc");
        final File partNative1 = buildAttemptPartFile(kfxOut, "native1");
        final File partNative2 = buildAttemptPartFile(kfxOut, "native2");
        final File legacyPart = new File(parent != null ? parent : new File("."), kfxOut.getName() + ".part");
        final File finalCover = new File(kfxOut.getAbsolutePath() + ".cover.jpg");
        deleteAttemptArtifacts(partInproc);
        deleteAttemptArtifacts(partNative1);
        deleteAttemptArtifacts(partNative2);
        deleteAttemptArtifacts(legacyPart);

        final long deadlineNanos = computeDeadlineNanos(System.nanoTime(), timeoutSec);
        boolean ok = false;
        boolean inProcCompleted = false;
        boolean inProcTimedOut = false;
        File winningPart = null;

        long inProcRemSec = Math.max(1L, (remainingTimeoutMs(System.nanoTime(), deadlineNanos) + 999L) / 1000L);
        final java.util.concurrent.FutureTask<Boolean> fut = new java.util.concurrent.FutureTask<>(
                (Callable<Boolean>) () -> com.amazon.kindle.epub.EpubTranscoder.transcodeToKfx(
                        epubIn, partInproc, inProcRemSec, cancelFlag));
        final Thread inProcThread = new Thread(fut, "azath-inproc-kfx");
        inProcThread.setDaemon(true);
        try {
            inProcThread.start();
            while (true) {
                if (cancelFlag != null && cancelFlag.get()) {
                    fut.cancel(true);
                    inProcThread.interrupt();
                    break;
                }
                long remMs = remainingTimeoutMs(System.nanoTime(), deadlineNanos);
                if (remMs <= 0L) {
                    inProcTimedOut = true;
                    break;
                }
                try {
                    long sliceMs = Math.min(250L, remMs);
                    Boolean res = fut.get(Math.max(1L, sliceMs), TimeUnit.MILLISECONDS);
                    inProcCompleted = true;
                    if (Boolean.TRUE.equals(res) && isValidKfxPart(partInproc)) {
                        ok = true;
                        winningPart = partInproc;
                    }
                    break;
                } catch (java.util.concurrent.TimeoutException te) {
                    // continue until shared deadline or cancelFlag
                }
            }
            if (!inProcCompleted && (cancelFlag == null || !cancelFlag.get())) {
                inProcTimedOut = true;
                Log.w(TAG, "EpubTranscoder.transcodeToKfx timed out at shared deadline (" + timeoutSec + "s)");
            }
        } catch (Throwable t) {
            Log.w(TAG, "EpubTranscoder direct call failed: " + t.getMessage());
        } finally {
            if (!fut.isDone()) {
                fut.cancel(true);
                inProcThread.interrupt();
            }
            if (!ok) {
                try {
                    inProcThread.join(750L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if (inProcThread.isAlive()) {
                    Log.w(TAG, "In-process converter thread still running after 750ms join");
                }
            }
        }

        try {
            boolean cancelled = cancelFlag != null && cancelFlag.get();
            if (shouldRunFallback(ok, inProcTimedOut, cancelled, System.nanoTime(), deadlineNanos)) {
                File bin = new File(ctx.getApplicationInfo().nativeLibraryDir, "libkfxconv.so");
                if (!bin.exists() && isDebuggable(ctx)) {
                    bin = new File("/data/local/tmp/kfx-conv");
                }
                if (!bin.exists()) {
                    Log.e(TAG, "libkfxconv.so missing at " + bin.getAbsolutePath());
                    return false;
                }
                File[] attemptParts = new File[] { partNative1, partNative2 };
                String[][] argVariants = new String[][] {
                    { bin.getAbsolutePath(), epubIn.getAbsolutePath(), "-o", partNative1.getAbsolutePath() },
                    { bin.getAbsolutePath(), epubIn.getAbsolutePath(), partNative2.getAbsolutePath() }
                };
                for (int i = 0; i < argVariants.length; i++) {
                    cancelled = cancelFlag != null && cancelFlag.get();
                    if (cancelled || remainingTimeoutMs(System.nanoTime(), deadlineNanos) <= 0L) {
                        break;
                    }
                    File attemptPart = attemptParts[i];
                    deleteAttemptArtifacts(attemptPart);
                    StringBuilder outLog = new StringBuilder();
                    int code = runProcessWithTimeoutUntil(argVariants[i], deadlineNanos, cancelFlag, outLog);
                    Log.i(TAG, "libkfxconv attempt=" + attemptPart.getName() + " exit=" + code + " size=" + attemptPart.length() + "B");
                    if (code == 0 && isValidKfxPart(attemptPart)) {
                        ok = true;
                        winningPart = attemptPart;
                        break;
                    }
                    Log.w(TAG, "libkfxconv output: " + outLog);
                    if (code == -1 || code == -2) {
                        break;
                    }
                }
            }

            cancelled = cancelFlag != null && cancelFlag.get();
            if (ok && winningPart != null && !cancelled) {
                if (kfxOut.exists()) kfxOut.delete();
                if (!winningPart.renameTo(kfxOut)) {
                    Log.e(TAG, "Atomic rename failed: " + winningPart.getAbsolutePath() + " -> " + kfxOut.getAbsolutePath());
                    return false;
                }
                File winCover = new File(winningPart.getAbsolutePath() + ".cover.jpg");
                if (winCover.exists() && winCover.length() > 0) {
                    if (finalCover.exists()) finalCover.delete();
                    winCover.renameTo(finalCover);
                }
                return true;
            }
            return false;
        } finally {
            deleteAttemptArtifacts(partInproc);
            deleteAttemptArtifacts(partNative1);
            deleteAttemptArtifacts(partNative2);
            deleteAttemptArtifacts(legacyPart);
        }
    }

    /**
     * Runs an external process with stdout/stderr drained on a separate daemon thread BEFORE waitFor,
     * enforcing timeoutSec and optional cancelFlag with destroyForcibly() cleanup.
     */
    public static int runProcessWithTimeout(String[] cmd, long timeoutSec,
                                            AtomicBoolean cancelFlag, StringBuilder outLog) {
        return runProcessWithTimeoutUntil(cmd, computeDeadlineNanos(System.nanoTime(), timeoutSec), cancelFlag, outLog);
    }

    public static int runProcessWithTimeoutUntil(String[] cmd, long deadlineNanos,
                                                 AtomicBoolean cancelFlag, StringBuilder outLog) {
        Process proc = null;
        try {
            if (remainingTimeoutMs(System.nanoTime(), deadlineNanos) <= 0L) {
                return -1;
            }
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            proc = pb.start();
            final InputStream pis = proc.getInputStream();
            Thread drainer = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(pis))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (outLog != null && outLog.length() < 16384) {
                            outLog.append(line).append('\n');
                        }
                    }
                } catch (Throwable ignored) {}
            }, "azath-proc-drain");
            drainer.setDaemon(true);
            drainer.start();

            boolean finished = false;
            while (remainingTimeoutMs(System.nanoTime(), deadlineNanos) > 0L) {
                if ((cancelFlag != null && cancelFlag.get()) || Thread.currentThread().isInterrupted()) {
                    proc.destroyForcibly();
                    try { proc.waitFor(2, TimeUnit.SECONDS); } catch (Throwable ignored) {}
                    return -2;
                }
                long remMs = remainingTimeoutMs(System.nanoTime(), deadlineNanos);
                if (proc.waitFor(Math.min(200L, Math.max(1L, remMs)), TimeUnit.MILLISECONDS)) {
                    finished = true;
                    break;
                }
            }
            if (!finished) {
                proc.destroyForcibly();
                try { proc.waitFor(2, TimeUnit.SECONDS); } catch (Throwable ignored) {}
                Log.w(TAG, "Process timed out at shared deadline; destroyed forcibly");
                return -1;
            }
            try { drainer.join(500); } catch (Throwable ignored) {}
            return proc.exitValue();
        } catch (Throwable t) {
            if (proc != null) {
                try { proc.destroyForcibly(); } catch (Throwable ignored) {}
            }
            Log.e(TAG, "runProcessWithTimeout error", t);
            return -1;
        }
    }

    public static boolean isValidKfxPart(File f) {
        if (f == null || !f.isFile() || f.length() < 128) return false;
        try (FileInputStream fis = new FileInputStream(f)) {
            byte[] hdr = new byte[4];
            int n = fis.read(hdr);
            if (n < 4) return false;
            return hdr[0] == 'C' && hdr[1] == 'O' && hdr[2] == 'N' && hdr[3] == 'T';
        } catch (Throwable t) {
            return false;
        }
    }

    public static File resolveExistingKfxFile(Context ctx, File rawFile) {
        if (rawFile == null) return null;
        if (rawFile.exists() && rawFile.length() > 0) return rawFile;
        String rawPath = rawFile.getAbsolutePath();
        if (ctx != null && rawPath.contains("/com.amazon.kindle.epub/")) {
            File migrated = new File(rawPath.replace("/com.amazon.kindle.epub/", "/" + ctx.getPackageName() + "/"));
            if (migrated.exists() && migrated.length() > 0) return migrated;
        }
        String name = rawFile.getName();
        if (name == null || name.isEmpty()) return rawFile;
        File[] candidates = new File[] {
                ctx != null ? new File(new File(ctx.getFilesDir(), "folio_library"), name) : null,
                new File("/sdcard/kindle", name),
                new File("/sdcard/kindle/imported", name)
        };
        for (File c : candidates) {
            if (c != null && c.exists() && c.length() > 0) return c;
        }
        // Case-insensitive scan fallback
        File[] dirs = new File[] {
                ctx != null ? new File(ctx.getFilesDir(), "folio_library") : null,
                new File("/sdcard/kindle")
        };
        for (File dir : dirs) {
            if (dir == null || !dir.isDirectory()) continue;
            File[] files = dir.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (f.isFile() && f.getName().equalsIgnoreCase(name)) return f;
            }
        }
        return rawFile;
    }

    public static void launchKfxReader(Context ctx, File kfxFile) {
        File resolved = resolveExistingKfxFile(ctx, kfxFile);
        if (resolved == null || !resolved.exists()) {
            Log.w(TAG, "launchKfxReader: file not found: " + (kfxFile != null ? kfxFile.getAbsolutePath() : "null"));
            return;
        }
        kfxFile = resolved;
        com.amazon.kindle.epub.catalog.FolioTheme.markFloatingCoverActive();

        try {
            Intent reader = new Intent(Intent.ACTION_VIEW);
            reader.setClassName(ctx.getPackageName(), "com.amazon.kindle.ThirdPartyUpgradePage");
            reader.setDataAndType(Uri.fromFile(kfxFile), "application/x-kfx");
            reader.putExtra("folio_bypass_intercept", true);
            reader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(reader);
            return;
        } catch (Throwable t) {
            Log.w(TAG, "ThirdPartyUpgradePage launch failed, trying StandAloneBookReaderActivity", t);
        }

        try {
            Intent reader = new Intent(Intent.ACTION_VIEW);
            reader.setClassName(ctx.getPackageName(), "com.amazon.kcp.reader.StandAloneBookReaderActivity");
            reader.setDataAndType(Uri.fromFile(kfxFile), "application/x-kfx");
            reader.putExtra("folio_bypass_intercept", true);
            reader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(reader);
        } catch (Throwable t2) {
            Log.e(TAG, "Failed to launch KFX reader", t2);
        }
    }

    private void showProgressDialog(String title, String subtitle) {
        showProgressDialog(title, subtitle, null);
    }

    private void showProgressDialog(String title, String subtitle, final Runnable onCancel) {
        dismissActiveDialog();
        if (isFinishing()) return;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(20));
        box.setBackgroundColor(FolioTheme.bgModal(this));

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        box.addView(pb);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setPadding(dp(16), 0, 0, 0);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(FolioTheme.textPrimary(this));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(FolioTheme.textMuted(this));
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textCol.addView(tvSub);

        box.addView(textCol);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setView(box)
                .setCancelable(onCancel != null);
        if (onCancel != null) {
            b.setNegativeButton("Cancel", (d, w) -> onCancel.run());
            b.setOnCancelListener(d -> onCancel.run());
        }
        activeDialog = b.create();
        activeDialog.show();
        com.amazon.kindle.epub.catalog.FolioTheme.applyToDialog(this, activeDialog);
    }

    private void dismissActiveDialog() {
        if (activeDialog != null) {
            try { activeDialog.dismiss(); } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissActiveDialog();
        super.onDestroy();
    }

    private TextView makeButton(String label, int bgColor, int fgColor) {
        TextView btn = new TextView(this);
        btn.setText(label);
        btn.setTextColor(fgColor);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(16), dp(13), dp(16), dp(13));
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(10));
        btn.setBackground(gd);
        return btn;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    private static Uri extractUri(Intent intent) {
        if (intent == null) return null;
        Uri data = intent.getData();
        if (data != null) return data;
        Object extra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (extra instanceof Uri) return (Uri) extra;
        return null;
    }

    private static boolean isEpubIntent(Context ctx, Intent intent, Uri uri) {
        String type = intent.getType();
        if (type != null && type.toLowerCase(Locale.ROOT).contains("epub")) return true;
        String s = uri.toString().toLowerCase(Locale.ROOT);
        if (s.endsWith(".epub") || s.contains(".epub?")) return true;
        String name = resolveDisplayName(ctx, uri);
        return name != null && name.toLowerCase(Locale.ROOT).endsWith(".epub");
    }

    private static String resolveDisplayName(Context ctx, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor c = null;
            try {
                c = ctx.getContentResolver().query(uri,
                        new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
                if (c != null && c.moveToFirst()) {
                    String n = c.getString(0);
                    if (n != null && !n.trim().isEmpty()) return n.trim();
                }
            } catch (Throwable ignored) {
            } finally {
                if (c != null) c.close();
            }
        }
        String last = uri.getLastPathSegment();
        if (last != null && !last.trim().isEmpty()) {
            int slash = last.lastIndexOf('/');
            return slash >= 0 ? last.substring(slash + 1) : last;
        }
        return "external_book.epub";
    }

    private static boolean isDebuggable(Context ctx) {
        if (ctx == null) return false;
        try {
            ApplicationInfo ai = ctx.getApplicationInfo();
            return ai != null && (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isPathInsideOrEqual(String candidatePath, String rootPath) {
        if (candidatePath == null || rootPath == null || rootPath.isEmpty()) return false;
        String normRoot = (rootPath.endsWith(File.separator) && rootPath.length() > 1)
                ? rootPath.substring(0, rootPath.length() - 1)
                : rootPath;
        return candidatePath.equals(normRoot) || candidatePath.startsWith(normRoot + File.separator);
    }

    public static boolean isBlockedPrivateFile(Context ctx, File src) {
        if (ctx == null || src == null) return false;
        try {
            String canon = src.getCanonicalFile().getPath();
            String abs = src.getAbsolutePath();
            java.util.List<File> roots = new java.util.ArrayList<>();
            try { if (ctx.getDataDir() != null) roots.add(ctx.getDataDir()); } catch (Throwable ignored) {}
            try {
                File f = ctx.getFilesDir();
                if (f != null && f.getParentFile() != null) roots.add(f.getParentFile());
            } catch (Throwable ignored) {}
            try {
                File c = ctx.getCacheDir();
                if (c != null && c.getParentFile() != null) roots.add(c.getParentFile());
            } catch (Throwable ignored) {}
            String pkg = ctx.getPackageName();
            if (pkg != null && !pkg.isEmpty()) {
                roots.add(new File("/data/data/" + pkg));
                roots.add(new File("/data/user/0/" + pkg));
            }
            for (File r : roots) {
                if (r == null) continue;
                String rAbs = r.getAbsolutePath();
                String rCanon;
                try {
                    rCanon = r.getCanonicalFile().getPath();
                } catch (Throwable t) {
                    rCanon = rAbs;
                }
                if (isPathInsideOrEqual(canon, rCanon) || isPathInsideOrEqual(abs, rAbs)
                        || isPathInsideOrEqual(canon, rAbs) || isPathInsideOrEqual(abs, rCanon)) {
                    return true;
                }
            }
        } catch (Throwable t) {
            return true;
        }
        return false;
    }

    private static boolean isBlockedPrivateFileUri(Context ctx, Uri uri) {
        if (uri == null || !"file".equalsIgnoreCase(uri.getScheme())) return false;
        String rawPath = uri.getPath();
        if (rawPath == null || rawPath.isEmpty()) return true;
        return isBlockedPrivateFile(ctx, new File(rawPath));
    }

    private static void copyUriToFile(Context ctx, Uri uri, File dest) throws Exception {
        if (!isSupportedScheme(uri)) {
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri.getScheme());
        }
        InputStream in = null;
        OutputStream out = null;
        boolean ok = false;
        try {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                String rawPath = uri.getPath();
                if (rawPath == null || rawPath.isEmpty() || rawPath.contains("..")) {
                    throw new IllegalArgumentException("Invalid file URI path");
                }
                File src = new File(rawPath).getCanonicalFile();
                if (isBlockedPrivateFile(ctx, src)) {
                    throw new SecurityException("Access to private app storage is not permitted");
                }
                if (!src.isFile() || !src.canRead()) {
                    throw new IllegalStateException("File not readable: " + src.getAbsolutePath());
                }
                if (src.length() > MAX_EPUB_BYTES) {
                    throw new IllegalStateException("EPUB exceeds 250 MB limit");
                }
                in = new FileInputStream(src);
            } else {
                Cursor c = null;
                try {
                    c = ctx.getContentResolver().query(uri,
                            new String[] { OpenableColumns.SIZE }, null, null, null);
                    if (c != null && c.moveToFirst() && !c.isNull(0)) {
                        long reported = c.getLong(0);
                        if (reported > MAX_EPUB_BYTES) {
                            throw new IllegalStateException("EPUB exceeds 250 MB limit");
                        }
                    }
                } finally {
                    if (c != null) try { c.close(); } catch (Throwable ignored) {}
                }
                in = ctx.getContentResolver().openInputStream(uri);
            }
            if (in == null) throw new IllegalStateException("Cannot open URI stream: " + uri);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[16384];
            long total = 0L;
            int n;
            while ((n = in.read(buf)) > 0) {
                total += n;
                if (total > MAX_EPUB_BYTES) {
                    throw new IllegalStateException("EPUB exceeds 250 MB limit");
                }
                out.write(buf, 0, n);
            }
            out.flush();
            ok = true;
        } finally {
            if (in != null) try { in.close(); } catch (Throwable ignored) {}
            if (out != null) try { out.close(); } catch (Throwable ignored) {}
            if (!ok && dest != null && dest.exists()) {
                dest.delete();
            }
        }
    }

    private static String sanitizeFilename(String s) {
        if (s == null || s.trim().isEmpty()) return "book";
        String clean = s.replaceAll("[^a-zA-Z0-9._-]+", "_").replaceAll("_+", "_");
        if (clean.length() > 48) clean = clean.substring(0, 48);
        return clean.replaceAll("^_+|_+$", "");
    }
}
