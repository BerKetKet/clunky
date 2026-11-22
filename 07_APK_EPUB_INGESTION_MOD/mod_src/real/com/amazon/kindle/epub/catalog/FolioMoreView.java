package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.amazon.kindle.epub.EpubTranscoder;
import java.io.File;

/**
 * Folio Control Center — Replaces the dead stock Amazon More tab with a clean,
 * 100% offline control center:
 * - OPDS Catalogs (Project Gutenberg, Standard Ebooks, Internet Archive & Custom Feeds)
 * - Scan Local Library (/sdcard/epub/ & /sdcard/kindle/ native KFX conversion)
 * - Backup & Restore (/sdcard/kindle/backups/)
 * - Reader & App Settings (native Kindle preferences without Amazon auth prompts)
 * - About Folio (engine architecture, active features & live library/cache stats)
 */
public final class FolioMoreView {
    private static final String PREFS_NAME = "folio_catalog_prefs";
    private static final String KEY_CUSTOM_OPDS = "custom_opds_url";

    private static final int BG_DARK = 0xFF101216;
    private static final int BG_CARD = 0xFF1B1F27;
    private static final int BG_CARD_HOVER = 0xFF242A35;
    private static final int ACCENT_AMBER = 0xFFE6A23C;
    private static final int ACCENT_TEAL = 0xFF38BDF8;
    private static final int TEXT_PRIMARY = 0xFFF3F4F6;
    private static final int TEXT_MUTED = 0xFF9CA3AF;

    public static View wrap(View originalView) {
        try {
            if (originalView == null) return null;
            return build(originalView.getContext());
        } catch (Throwable t) {
            android.util.Log.e("FolioMoreView", "Failed to build Folio Control Center", t);
            return originalView;
        }
    }

    public static View build(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;

        ScrollView scroll = new ScrollView(ctx);
        scroll.setBackgroundColor(BG_DARK);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(d, 80));

        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(d, 20), dp(d, 24), dp(d, 20), dp(d, 24));
        scroll.addView(container, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // 1. Header Row: Title + Version Badge
        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(d, 6));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Control Center");
        titleTv.setTextSize(22);
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        headerRow.addView(titleTv, tlp);

        TextView badgeTv = new TextView(ctx);
        badgeTv.setText("Folio v3.8 KFX");
        badgeTv.setTextSize(11);
        badgeTv.setTextColor(ACCENT_AMBER);
        badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
        badgeTv.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xFF282216);
        badgeBg.setCornerRadius(dp(d, 12));
        badgeBg.setStroke(dp(d, 1), 0x66E6A23C);
        badgeTv.setBackground(badgeBg);
        headerRow.addView(badgeTv);
        container.addView(headerRow);

        TextView subTv = new TextView(ctx);
        subTv.setText("Offline Reader & Library Tools");
        subTv.setTextSize(13);
        subTv.setTextColor(TEXT_MUTED);
        subTv.setPadding(0, 0, 0, dp(d, 22));
        container.addView(subTv);

        // 2. Action Cards
        // Card: Library Metadata & Genres
        container.addView(makeActionCard(
                ctx, d,
                "📚", "Library Metadata & Genres",
                "Browse saved authors, pub dates, ISBNs & genres",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx);
                    }
                }
        ));

        // Card: Import EPUBs from Storage
        container.addView(makeActionCard(
                ctx, d,
                "📥", "Import EPUBs from Storage",
                "Batch-select EPUB folders into app library",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioFolderBrowserDialog.show(ctx);
                    }
                }
        ));

        // Card A: OPDS Catalogs
        container.addView(makeActionCard(
                ctx, d,
                "🌐", "OPDS Catalogs",
                "Browse Gutenberg, Standard Ebooks & custom feeds",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOpdsDialog(ctx);
                    }
                }
        ));

        // Card B: Scan Local Library
        container.addView(makeActionCard(
                ctx, d,
                "🔄", "Scan Local Library",
                "Rescan /sdcard/epub/ & update native KFX catalog",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scanLocalLibrary(ctx);
                    }
                }
        ));

        // Card C: Backup & Restore
        container.addView(makeActionCard(
                ctx, d,
                "💾", "Backup & Restore",
                "Back up library metadata, notes & reading progress",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OpenCatalogView.showBackupRestoreDialog(ctx);
                    }
                }
        ));

        // Card D: Reader Settings
        container.addView(makeActionCard(
                ctx, d,
                "⚙️", "Reader & App Settings",
                "Reading preferences, dark mode, dictionary & storage",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSettings(ctx);
                    }
                }
        ));

        // Card E: About Folio
        container.addView(makeActionCard(
                ctx, d,
                "ℹ️", "About Folio",
                "Engine capabilities, active features & cache stats",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAboutDialog(ctx);
                    }
                }
        ));

        return scroll;
    }

    private static View makeActionCard(
            Context ctx, float d,
            String icon, String title, String subtitle,
            View.OnClickListener listener
    ) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(d, 16), dp(d, 14), dp(d, 16), dp(d, 14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BG_CARD);
        bg.setCornerRadius(dp(d, 12));
        bg.setStroke(dp(d, 1), 0xFF2A313E);
        card.setBackground(bg);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(listener);

        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        clp.setMargins(0, 0, 0, dp(d, 12));
        card.setLayoutParams(clp);

        TextView iconTv = new TextView(ctx);
        iconTv.setText(icon);
        iconTv.setTextSize(22);
        iconTv.setPadding(0, 0, dp(d, 16), 0);
        card.addView(iconTv);

        LinearLayout textCol = new LinearLayout(ctx);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tclp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        TextView titleTv = new TextView(ctx);
        titleTv.setText(title);
        titleTv.setTextSize(15);
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(titleTv);

        TextView subTv = new TextView(ctx);
        subTv.setText(subtitle);
        subTv.setTextSize(12);
        subTv.setTextColor(TEXT_MUTED);
        subTv.setPadding(0, dp(d, 2), 0, 0);
        textCol.addView(subTv);

        card.addView(textCol, tclp);

        TextView arrowTv = new TextView(ctx);
        arrowTv.setText("›");
        arrowTv.setTextSize(20);
        arrowTv.setTextColor(0xFF6B7280);
        arrowTv.setPadding(dp(d, 8), 0, 0, 0);
        card.addView(arrowTv);

        return card;
    }

    private static void openSettings(Context ctx) {
        try {
            Intent intent = new Intent();
            intent.setClassName(ctx.getPackageName(), "com.amazon.kcp.library.SettingsComposeActivity");
            intent.putExtra(Intent.EXTRA_TITLE, "Settings");
            ctx.startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent fallback = new Intent();
                fallback.setClassName(ctx.getPackageName(), "com.amazon.kcp.library.SettingsActivity");
                fallback.putExtra(Intent.EXTRA_TITLE, "Settings");
                ctx.startActivity(fallback);
            } catch (Throwable t2) {
                Toast.makeText(ctx, "Could not launch Settings: " + t2.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void showOpdsDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(d, 22), dp(d, 14), dp(d, 22), dp(d, 8));

        TextView label = new TextView(ctx);
        label.setText("Quick OPDS Catalogs:");
        label.setTextColor(0xFFCBD5E1);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setPadding(0, 0, 0, dp(d, 8));
        layout.addView(label);

        final String[][] presets = {
                {"Project Gutenberg", "https://m.gutenberg.org/ebooks.opds/"},
                {"Standard Ebooks", "https://standardebooks.org/feeds/atom/new-releases"},
                {"Internet Archive", "https://bookserver.archive.org/catalog/"}
        };

        final EditText input = new EditText(ctx);
        input.setHint("https://example.org/opds.xml");
        input.setText(prefs.getString(KEY_CUSTOM_OPDS, ""));
        input.setSingleLine(true);
        input.setTextSize(13);

        for (final String[] p : presets) {
            TextView btn = new TextView(ctx);
            btn.setText("• " + p[0]);
            btn.setTextColor(0xFF1E88E5);
            btn.setPadding(dp(d, 4), dp(d, 6), dp(d, 4), dp(d, 6));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    input.setText(p[1]);
                }
            });
            layout.addView(btn);
        }

        TextView customLabel = new TextView(ctx);
        customLabel.setText("\nOr enter custom OPDS feed URL:");
        customLabel.setTextColor(0xFFCBD5E1);
        customLabel.setTypeface(Typeface.DEFAULT_BOLD);
        layout.addView(customLabel);
        layout.addView(input);

        new AlertDialog.Builder(ctx)
                .setTitle("OPDS Catalog Feeds")
                .setView(layout)
                .setPositiveButton("Save Feed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String url = input.getText().toString().trim();
                        if (!url.isEmpty()) {
                            prefs.edit().putString(KEY_CUSTOM_OPDS, url).apply();
                            Toast.makeText(ctx, "OPDS feed saved! Search or browse from the Home tab.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void scanLocalLibrary(final Context ctx) {
        Toast.makeText(ctx, "Scanning /sdcard/epub/ & /sdcard/kindle/...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                int epubCount = 0;
                int kfxConverted = 0;
                int totalKindle = 0;

                File epubDir = new File("/sdcard/epub");
                File kindleDir = new File("/sdcard/kindle");

                if (epubDir.exists() && epubDir.isDirectory()) {
                    File[] epubs = epubDir.listFiles();
                    if (epubs != null) {
                        for (File f : epubs) {
                            if (f.getName().toLowerCase().endsWith(".epub")) {
                                epubCount++;
                                String baseName = f.getName().substring(0, f.getName().lastIndexOf('.'));
                                File targetKfx = new File(kindleDir, baseName + ".kfx");
                                if (!targetKfx.exists()) {
                                    if (EpubTranscoder.transcodeToKfx(f, targetKfx)) {
                                        kfxConverted++;
                                    }
                                }
                            }
                        }
                    }
                }

                if (kindleDir.exists() && kindleDir.isDirectory()) {
                    File[] kf = kindleDir.listFiles();
                    if (kf != null) {
                        for (File f : kf) {
                            String name = f.getName().toLowerCase();
                            if (name.endsWith(".kfx") || name.endsWith(".azw3") || name.endsWith(".mobi") || name.endsWith(".prc")) {
                                totalKindle++;
                            }
                        }
                    }
                }

                final int finalEpubs = epubCount;
                final int finalConverted = kfxConverted;
                final int finalKindle = totalKindle;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Scan complete: " + finalKindle + " Kindle book(s) found";
                        if (finalConverted > 0) {
                            msg += " (" + finalConverted + " new EPUBs converted to KFX)";
                        }
                        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private static void showAboutDialog(final Context ctx) {
        // Collect live stats
        int indexedBooks = 0;
        try {
            File dbFile = new File(ctx.getApplicationInfo().dataDir, "databases/kindle_library.db");
            if (dbFile.exists()) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                Cursor c = db.rawQuery("SELECT COUNT(*) FROM KindleContent WHERE TITLE IS NOT NULL AND TITLE != ''", null);
                if (c != null && c.moveToFirst()) {
                    indexedBooks = c.getInt(0);
                    c.close();
                }
                db.close();
            }
        } catch (Throwable ignored) {}

        long cacheBytes = 0;
        try {
            File kindleDir = new File("/sdcard/kindle");
            if (kindleDir.exists()) {
                File[] files = kindleDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        cacheBytes += f.length();
                    }
                }
            }
        } catch (Throwable ignored) {}

        double cacheMb = cacheBytes / (1024.0 * 1024.0);

        StringBuilder sb = new StringBuilder();
        sb.append("Folio — Offline EPUB & KFX Reader\n");
        sb.append("Version: Folio v3.8 KFX (Enhanced Typesetting)\n");
        String arch = (android.os.Build.SUPPORTED_ABIS != null && android.os.Build.SUPPORTED_ABIS.length > 0)
                ? android.os.Build.SUPPORTED_ABIS[0] : "ARM64";
        String mfg = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String rel = android.os.Build.VERSION.RELEASE;
        boolean hasMfg = mfg != null && !mfg.trim().isEmpty() && !mfg.equalsIgnoreCase("unknown");
        boolean modelHasMfg = hasMfg && model != null && model.toLowerCase().contains(mfg.toLowerCase());
        StringBuilder hw = new StringBuilder();
        if (hasMfg && !modelHasMfg) {
            hw.append(mfg).append(" ");
        }
        if (model != null) {
            hw.append(model);
        }
        hw.append(" · Android ").append(rel != null ? rel : "?").append(" (").append(arch).append(")");
        sb.append("Hardware: ").append(hw.toString()).append("\n\n");

        sb.append("Pure-Rust Engine (libkfxconv.so):\n");
        sb.append("• Standalone $270 Cover Container (#000000, 0% margin)\n");
        sb.append("• Native YellowJersey Tables ($278/$279/$269)\n");
        sb.append("• Proportional Column-Width Image Scaling ($422/$423)\n");
        sb.append("• Unicode NFC Polytonic Greek & Non-Latin Normalization\n");
        sb.append("• Native YellowJersey Lists ($277) with Hanging Indents\n");
        sb.append("• Enhanced Blockquote Spacing & Italic Typography\n");
        sb.append("• Native Pop-up Footnotes ($616/$617, $615/$281)\n");
        sb.append("• Rich Code Blocks (<pre>/<code>), <hr/> Rules & Inlines\n");
        sb.append("• Native $266 Anchor Links & $179 Hyperlink Navigation\n\n");

        sb.append("Live Library & Cache Stats:\n");
        sb.append("• Indexed Library Books: ").append(indexedBooks).append("\n");
        sb.append("• Local Storage Cache: ").append(String.format("%.1f", cacheMb)).append(" MB\n");
        sb.append("• Privacy Mode: Local-First (Zero Amazon Telemetry)\n");

        new AlertDialog.Builder(ctx)
                .setTitle("About Folio")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private static int dp(float density, int value) {
        return (int) (value * density + 0.5f);
    }
}
