package com.amazon.kindle.epub.ingest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
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

/**
 * Cleanly intercepts external EPUB open intents (file:// or content:// from ANY
 * storage directory), eliminating Kindle's hanging external-source loading wheel.
 * Includes duplicate library detection: if already imported, offers instant
 * "Open Existing Copy" alongside "Re-Import & Replace" and "Open Once".
 */
public class ExternalEpubActivity extends Activity {

    private static final String TAG = "FolioExternalEpub";
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
        if (uri == null) {
            Toast.makeText(this, "No EPUB file provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        startStagingAndPrompt(uri);
    }

    private void startStagingAndPrompt(final Uri uri) {
        showProgressDialog("Inspecting EPUB...", "Reading book metadata from storage");
        new Thread(() -> {
            try {
                final String displayName = resolveDisplayName(this, uri);
                File stagingDir = new File(getCacheDir(), "folio_staging");
                if (!stagingDir.exists()) stagingDir.mkdirs();
                final File stagedEpub = new File(stagingDir, "incoming_" + System.currentTimeMillis() + ".epub");

                copyUriToFile(this, uri, stagedEpub);
                if (!stagedEpub.exists() || stagedEpub.length() < 64) {
                    throw new IllegalStateException("Could not read EPUB file from storage");
                }

                final EpubMetadataExtractor.BookMeta meta =
                        EpubMetadataExtractor.extract(stagedEpub, displayName);
                final String bookKey = FolioMetadataDb.computeBookKey(meta.title, stagedEpub.length());

                FolioMetadataDb.BookRecord existing = null;
                FolioMetadataDb db = new FolioMetadataDb(this);
                try {
                    existing = db.findExisting(bookKey, meta.title, meta.author);
                } finally {
                    db.close();
                }

                final FolioMetadataDb.BookRecord finalExisting = existing;
                mainHandler.post(() -> showChoiceDialog(stagedEpub, displayName, meta, finalExisting));
            } catch (Throwable t) {
                Log.e(TAG, "Failed to inspect incoming EPUB", t);
                mainHandler.post(() -> {
                    dismissActiveDialog();
                    Toast.makeText(this, "Could not open EPUB: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }, "folio-epub-stage").start();
    }

    private void showChoiceDialog(final File stagedEpub, final String displayName,
                                  final EpubMetadataExtractor.BookMeta meta,
                                  final FolioMetadataDb.BookRecord existing) {
        dismissActiveDialog();
        if (isFinishing()) return;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, dp(16));
        root.setBackgroundColor(Color.parseColor("#161922"));

        TextView badge = new TextView(this);
        badge.setText(existing != null ? "ALREADY IN FOLIO LIBRARY" : "FOLIO EPUB IMPORT");
        badge.setTextColor(Color.parseColor(existing != null ? "#34D399" : "#F59E0B"));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(badge);

        TextView titleView = new TextView(this);
        titleView.setText(meta.title);
        titleView.setTextColor(Color.WHITE);
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
        authorView.setTextColor(Color.parseColor("#CBD5E1"));
        authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        authorView.setPadding(0, 0, 0, dp(12));
        root.addView(authorView);

        TextView desc = new TextView(this);
        if (existing != null) {
            desc.setText("This book is already saved in Folio's app storage. Open your existing copy instantly, or re-import and replace it.");
        } else {
            desc.setText("Add to your Folio library (saves converted book & metadata in app storage) or open once without saving.");
        }
        desc.setTextColor(Color.parseColor("#94A3B8"));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        desc.setPadding(0, 0, 0, dp(18));
        root.addView(desc);

        if (existing != null) {
            TextView btnOpenExisting = makeButton("Open Existing Copy", "#34D399", "#0F172A");
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

            TextView btnReimport = makeButton("Re-Import & Replace", "#262B38", "#F8FAFC");
            btnReimport.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, true));
            root.addView(btnReimport);
        } else {
            TextView btnAdd = makeButton("Add to Library & Read", "#F59E0B", "#111827");
            btnAdd.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, true));
            root.addView(btnAdd);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(1, dp(10)));
            root.addView(spacer);

            TextView btnOnce = makeButton("Open Once (Don't Add)", "#262B38", "#F8FAFC");
            btnOnce.setOnClickListener(v -> processAndOpen(stagedEpub, displayName, meta, false));
            root.addView(btnOnce);
        }

        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(1, dp(8)));
        root.addView(spacer2);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setTextColor(Color.parseColor("#94A3B8"));
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
    }

    private void processAndOpen(final File stagedEpub, final String displayName,
                                final EpubMetadataExtractor.BookMeta meta, final boolean addToLibrary) {
        showProgressDialog(
                addToLibrary ? "Adding to Folio Library..." : "Preparing Book...",
                addToLibrary ? "Converting to KFX & enriching book metadata"
                             : "Converting EPUB to native KFX");

        new Thread(() -> {
            try {
                final String bookKey = FolioMetadataDb.computeBookKey(meta.title, stagedEpub.length());
                final String safeBase = sanitizeFilename(meta.title) + "_" + bookKey;

                File targetDir = addToLibrary
                        ? new File(getFilesDir(), "folio_library")
                        : new File(getCacheDir(), "folio_once");
                if (!targetDir.exists()) targetDir.mkdirs();

                File outKfx = new File(targetDir, safeBase + ".kfx");
                File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

                boolean ok = convertEpubToKfx(this, stagedEpub, outKfx);
                if (!ok || !outKfx.exists() || outKfx.length() < 128) {
                    throw new IllegalStateException("KFX conversion failed");
                }

                if (addToLibrary) {
                    MetadataEnricher.enrichInPlace(meta);
                    FolioMetadataDb metaDb = new FolioMetadataDb(this);
                    try {
                        metaDb.upsertBook(bookKey, outKfx, outCover, displayName, meta);
                    } finally {
                        metaDb.close();
                    }
                    FolioMetadataDb.registerInKindleLibraryDb(this, bookKey, outKfx, outCover, meta);
                }

                stagedEpub.delete();

                mainHandler.post(() -> {
                    dismissActiveDialog();
                    if (addToLibrary) {
                        Toast.makeText(this, "Added \"" + meta.title + "\" to Folio Library", Toast.LENGTH_SHORT).show();
                    }
                    launchKfxReader(this, outKfx);
                    finish();
                });
            } catch (Throwable t) {
                Log.e(TAG, "Failed to process external EPUB", t);
                stagedEpub.delete();
                mainHandler.post(() -> {
                    dismissActiveDialog();
                    Toast.makeText(this, "Import error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }, "folio-epub-convert").start();
    }

    public static boolean convertEpubToKfx(Context ctx, File epubIn, File kfxOut) {
        try {
            if (com.amazon.kindle.epub.EpubTranscoder.transcodeToKfx(epubIn, kfxOut)) {
                return true;
            }
        } catch (Throwable t) {
            Log.w(TAG, "EpubTranscoder direct call failed: " + t.getMessage());
        }

        File bin = new File(ctx.getApplicationInfo().nativeLibraryDir, "libkfxconv.so");
        if (!bin.exists()) {
            bin = new File("/data/local/tmp/kfx-conv");
        }
        if (!bin.exists()) {
            Log.e(TAG, "libkfxconv.so missing at " + bin.getAbsolutePath());
            return false;
        }
        String[][] argVariants = new String[][] {
            { bin.getAbsolutePath(), epubIn.getAbsolutePath(), "-o", kfxOut.getAbsolutePath() },
            { bin.getAbsolutePath(), epubIn.getAbsolutePath(), kfxOut.getAbsolutePath() }
        };
        for (String[] cmd : argVariants) {
            try {
                if (kfxOut.exists()) kfxOut.delete();
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process proc = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                StringBuilder outLog = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    outLog.append(line).append('\n');
                }
                int code = proc.waitFor();
                Log.i(TAG, "libkfxconv exit=" + code + " out=" + kfxOut.length() + "B");
                if (code == 0 && kfxOut.exists() && kfxOut.length() > 128) {
                    return true;
                }
                Log.w(TAG, "libkfxconv output: " + outLog);
            } catch (Throwable t) {
                Log.e(TAG, "libkfxconv exec error", t);
            }
        }
        return false;
    }

    public static void launchKfxReader(Context ctx, File kfxFile) {
        try {
            android.os.StrictMode.class.getMethod("disableDeathOnFileUriExposure").invoke(null);
        } catch (Throwable ignored) {}

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
        dismissActiveDialog();
        if (isFinishing()) return;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(20));
        box.setBackgroundColor(Color.parseColor("#161922"));

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        box.addView(pb);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setPadding(dp(16), 0, 0, 0);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(Color.parseColor("#94A3B8"));
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textCol.addView(tvSub);

        box.addView(textCol);

        activeDialog = new AlertDialog.Builder(this)
                .setView(box)
                .setCancelable(false)
                .create();
        activeDialog.show();
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

    private TextView makeButton(String label, String bgHex, String fgHex) {
        TextView btn = new TextView(this);
        btn.setText(label);
        btn.setTextColor(Color.parseColor(fgHex));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(16), dp(13), dp(16), dp(13));
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(bgHex));
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

    private static void copyUriToFile(Context ctx, Uri uri, File dest) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            if ("file".equalsIgnoreCase(uri.getScheme()) && uri.getPath() != null) {
                in = new FileInputStream(new File(uri.getPath()));
            } else {
                in = ctx.getContentResolver().openInputStream(uri);
            }
            if (in == null) throw new IllegalStateException("Cannot open URI stream: " + uri);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
        } finally {
            if (in != null) try { in.close(); } catch (Throwable ignored) {}
            if (out != null) try { out.close(); } catch (Throwable ignored) {}
        }
    }

    private static String sanitizeFilename(String s) {
        if (s == null || s.trim().isEmpty()) return "book";
        String clean = s.replaceAll("[^a-zA-Z0-9._-]+", "_").replaceAll("_+", "_");
        if (clean.length() > 48) clean = clean.substring(0, 48);
        return clean.replaceAll("^_+|_+$", "");
    }
}
