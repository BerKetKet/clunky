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
        badgeTv.setText("Folio v7.4 KFX");
        badgeTv.setTextSize(11);
        badgeTv.setTextColor(ACCENT_AMBER);
        badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
        badgeTv.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xFF282216);
        badgeBg.setCornerRadius(dp(d, 12));
        badgeBg.setStroke(dp(d, 1), 0x66E6A23C);
        badgeTv.setBackground(badgeBg);
        badgeTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAboutDialog(ctx); }
        });
        headerRow.addView(badgeTv);
        container.addView(headerRow);

        TextView subTv = new TextView(ctx);
        subTv.setText("Offline Reader & Library Tools");
        subTv.setTextSize(13);
        subTv.setTextColor(TEXT_MUTED);
        subTv.setPadding(0, 0, 0, dp(d, 14));
        container.addView(subTv);

        // 1b. Live 4-Tile Reading Stats Strip + Annual Reading Goal Card
        final TextView[] statNums = new TextView[4];
        final TextView[] goalStatusRef = new TextView[1];
        final View[] goalFillRef = new View[1];
        final View[] goalTrackRef = new View[1];
        final Runnable refreshStats = new Runnable() {
            @Override public void run() {
                int[] s = readQuickStats(ctx);
                if (statNums[0] != null) statNums[0].setText(String.valueOf(s[0]));
                if (statNums[1] != null) statNums[1].setText(String.valueOf(s[1]));
                if (statNums[2] != null) statNums[2].setText(String.valueOf(s[2]));
                if (statNums[3] != null) statNums[3].setText(String.valueOf(s[3]));
                updateGoalCard(ctx, s[2], goalStatusRef[0], goalFillRef[0], goalTrackRef[0]);
            }
        };

        int[] stats = readQuickStats(ctx);
        LinearLayout statsRow = new LinearLayout(ctx);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srLp.bottomMargin = dp(d, 16);
        statsRow.setLayoutParams(srLp);

        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[0]), "BOOKS", 0xFFF59E0B, true,
                statNums, 0,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[1]), "READING", 0xFF5EEAD4, true,
                statNums, 1,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Reading", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[2]), "FINISHED", 0xFF34D399, true,
                statNums, 2,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Finished", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[3]), "★ FAVS", 0xFFFBBF24, false,
                statNums, 3,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Favorites", refreshStats);
                    }
                }));
        container.addView(statsRow);

        // 1c. Annual Reading Goal Progress Card (tap to customize target)
        LinearLayout goalCard = new LinearLayout(ctx);
        goalCard.setOrientation(LinearLayout.VERTICAL);
        goalCard.setPadding(dp(d, 14), dp(d, 12), dp(d, 14), dp(d, 12));
        GradientDrawable gcBg = new GradientDrawable();
        gcBg.setColor(0xFF1A1F2E);
        gcBg.setCornerRadius(dp(d, 12));
        gcBg.setStroke(dp(d, 1), 0xFF2A3142);
        goalCard.setBackground(gcBg);
        LinearLayout.LayoutParams gcLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gcLp.bottomMargin = dp(d, 16);
        goalCard.setLayoutParams(gcLp);

        LinearLayout goalTop = new LinearLayout(ctx);
        goalTop.setOrientation(LinearLayout.HORIZONTAL);
        goalTop.setGravity(Gravity.CENTER_VERTICAL);

        int yrNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        TextView goalTitleTv = new TextView(ctx);
        goalTitleTv.setText("🎯 " + yrNow + " Reading Goal");
        goalTitleTv.setTextColor(TEXT_PRIMARY);
        goalTitleTv.setTextSize(13);
        goalTitleTv.setTypeface(Typeface.DEFAULT_BOLD);
        goalTitleTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        goalTop.addView(goalTitleTv);

        TextView goalEditTv = new TextView(ctx);
        goalEditTv.setText("Edit ›");
        goalEditTv.setTextColor(0xFF34D399);
        goalEditTv.setTextSize(12);
        goalEditTv.setTypeface(Typeface.DEFAULT_BOLD);
        goalTop.addView(goalEditTv);
        goalCard.addView(goalTop);

        TextView goalSubTv = new TextView(ctx);
        goalSubTv.setTextColor(TEXT_MUTED);
        goalSubTv.setTextSize(12);
        goalSubTv.setPadding(0, dp(d, 3), 0, dp(d, 8));
        goalStatusRef[0] = goalSubTv;
        goalCard.addView(goalSubTv);

        LinearLayout barTrack = new LinearLayout(ctx);
        barTrack.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable trBg = new GradientDrawable();
        trBg.setColor(0xFF242938);
        trBg.setCornerRadius(dp(d, 4));
        barTrack.setBackground(trBg);
        barTrack.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 6)));

        View barFill = new View(ctx);
        GradientDrawable flBg = new GradientDrawable();
        flBg.setColor(0xFF34D399);
        flBg.setCornerRadius(dp(d, 4));
        barFill.setBackground(flBg);
        barFill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f));
        barTrack.addView(barFill);

        View barRest = new View(ctx);
        barRest.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        barTrack.addView(barRest);

        goalFillRef[0] = barFill;
        goalTrackRef[0] = barRest;
        goalCard.addView(barTrack);

        updateGoalCard(ctx, stats[2], goalSubTv, barFill, barRest);
        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showReadingGoalDialog(ctx, refreshStats);
            }
        });
        container.addView(goalCard);

        // 2. Action Cards
        // Card: Library Metadata & Genres
        container.addView(makeActionCard(
                ctx, d,
                "📚", "Library Metadata & Genres",
                "Browse saved authors, pub dates, ISBNs & genres",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All", refreshStats);
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

        // Card C2: Reading Log & Markdown Export
        container.addView(makeActionCard(
                ctx, d,
                "📄", "Reading Log & Export",
                "Export rated, favorited & annotated books to Markdown",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReadingLogDialog(ctx);
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

    private static int[] readQuickStats(Context ctx) {
        int indexed = 0, reading = 0, finished = 0;
        try {
            File dbFile = new File(ctx.getApplicationInfo().dataDir, "databases/kindle_library.db");
            if (dbFile.exists()) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                try {
                    Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM KindleContent WHERE TITLE IS NOT NULL AND TITLE != ''", null);
                    if (c1 != null) { if (c1.moveToFirst()) indexed = c1.getInt(0); c1.close(); }
                    try {
                        Cursor c2 = db.rawQuery(
                                "SELECT COUNT(*) FROM UserContent u JOIN KindleContent k ON u.KEY = k.ID "
                                + "WHERE COALESCE(u.READING_PROGRESS, 0) > 0 AND COALESCE(u.READING_PROGRESS, 0) < 100 "
                                + "AND UPPER(COALESCE(k.READ_STATE, '')) != 'READ'", null);
                        if (c2 != null) { if (c2.moveToFirst()) reading = c2.getInt(0); c2.close(); }
                    } catch (Throwable ignored) {}
                    try {
                        Cursor c3 = db.rawQuery(
                                "SELECT COUNT(*) FROM KindleContent k LEFT JOIN UserContent u ON u.KEY = k.ID "
                                + "WHERE UPPER(COALESCE(k.READ_STATE, '')) = 'READ' OR COALESCE(u.READING_PROGRESS, 0) >= 100", null);
                        if (c3 != null) { if (c3.moveToFirst()) finished = c3.getInt(0); c3.close(); }
                    } catch (Throwable ignored) {}
                } finally {
                    db.close();
                }
            }
        } catch (Throwable ignored) {}
        int favs = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countFavorites(ctx);
        return new int[] { indexed, reading, finished, favs };
    }

    private static View makeStatTile(Context ctx, float d, String count, String label,
                                     int accentColor, boolean addRightGap,
                                     TextView[] numSlot, int slotIdx, View.OnClickListener listener) {
        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(d, 6), dp(d, 11), dp(d, 6), dp(d, 11));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1A1F2E);
        bg.setCornerRadius(dp(d, 12));
        bg.setStroke(dp(d, 1), 0xFF2A3142);
        tile.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        if (addRightGap) lp.rightMargin = dp(d, 8);
        tile.setLayoutParams(lp);

        TextView numTv = new TextView(ctx);
        numTv.setText(count);
        numTv.setTextSize(20);
        numTv.setTextColor(accentColor);
        numTv.setTypeface(Typeface.DEFAULT_BOLD);
        numTv.setGravity(Gravity.CENTER);
        if (numSlot != null && slotIdx >= 0 && slotIdx < numSlot.length) numSlot[slotIdx] = numTv;
        tile.addView(numTv);

        TextView lblTv = new TextView(ctx);
        lblTv.setText(label);
        lblTv.setTextSize(10);
        lblTv.setTextColor(TEXT_MUTED);
        lblTv.setTypeface(Typeface.DEFAULT_BOLD);
        lblTv.setGravity(Gravity.CENTER);
        lblTv.setPadding(0, dp(d, 2), 0, 0);
        tile.addView(lblTv);

        tile.setOnClickListener(listener);
        return tile;
    }

    private static int getReadingGoalTarget(Context ctx) {
        try {
            int g = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getInt("reading_goal_target", 12);
            return g > 0 ? g : 12;
        } catch (Throwable ignored) { return 12; }
    }

    private static void setReadingGoalTarget(Context ctx, int target) {
        try {
            ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .edit().putInt("reading_goal_target", Math.max(1, target)).apply();
        } catch (Throwable ignored) {}
    }

    private static void updateGoalCard(Context ctx, int finishedCount, TextView statusTv, View fillV, View restV) {
        if (statusTv == null || fillV == null || restV == null) return;
        int target = getReadingGoalTarget(ctx);
        int pct = Math.min(100, Math.round((finishedCount * 100f) / Math.max(1, target)));
        statusTv.setText(finishedCount + " of " + target + " books finished (" + pct + "%)");
        float ratio = Math.min(1f, finishedCount / (float) Math.max(1, target));
        if (ratio > 0f && ratio < 0.04f) ratio = 0.04f;
        fillV.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, ratio));
        restV.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(0f, 1f - ratio)));
    }

    private static void showReadingGoalDialog(final Context ctx, final Runnable onSaved) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        int cur = getReadingGoalTarget(ctx);
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(0xFF161922);
        wrap.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Set Annual Reading Goal");
        hdr.setTextColor(TEXT_PRIMARY);
        hdr.setTextSize(16);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 10));
        wrap.addView(hdr);

        final android.widget.EditText et = new android.widget.EditText(ctx);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(cur));
        et.setTextColor(TEXT_PRIMARY);
        et.setHint("Number of books (e.g. 12, 24, 52)");
        et.setHintTextColor(0xFF64748B);
        et.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(0xFF1A1F2E);
        etBg.setCornerRadius(dp(d, 10));
        etBg.setStroke(dp(d, 1), 0xFF2A3142);
        et.setBackground(etBg);

        LinearLayout presetRow = new LinearLayout(ctx);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams prLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prLp.bottomMargin = dp(d, 12);
        presetRow.setLayoutParams(prLp);

        int[] presets = new int[] { 5, 12, 24, 36, 52 };
        for (final int pv : presets) {
            TextView pill = new TextView(ctx);
            pill.setText(String.valueOf(pv));
            pill.setTextColor(pv == cur ? 0xFF111827 : 0xFF34D399);
            pill.setTextSize(12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setGravity(Gravity.CENTER);
            pill.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(pv == cur ? 0xFF34D399 : 0xFF1E2433);
            pBg.setCornerRadius(dp(d, 12));
            pill.setBackground(pBg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.rightMargin = dp(d, 6);
            pill.setLayoutParams(lp);
            pill.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    et.setText(String.valueOf(pv));
                    et.setSelection(et.getText().length());
                }
            });
            presetRow.addView(pill);
        }
        wrap.addView(presetRow);
        wrap.addView(et);

        new AlertDialog.Builder(ctx)
                .setView(wrap)
                .setPositiveButton("Save Goal", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            int val = Integer.parseInt(et.getText().toString().trim());
                            if (val > 0 && val <= 999) {
                                setReadingGoalTarget(ctx, val);
                                Toast.makeText(ctx, "Reading goal set to " + val + " books", Toast.LENGTH_SHORT).show();
                                if (onSaved != null) onSaved.run();
                            }
                        } catch (Throwable ignored) {}
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                File appLibDir = new File(ctx.getFilesDir(), "folio_library");
                if (!appLibDir.exists()) appLibDir.mkdirs();

                if (epubDir.exists() && epubDir.isDirectory()) {
                    File[] epubs = epubDir.listFiles();
                    if (epubs != null) {
                        for (File f : epubs) {
                            if (f.getName().toLowerCase().endsWith(".epub")) {
                                epubCount++;
                                String baseName = f.getName().substring(0, f.getName().lastIndexOf('.'));
                                File legacyKfx = new File(kindleDir, baseName + ".kfx");
                                File appKfx = new File(appLibDir, baseName + ".kfx");
                                if (!legacyKfx.exists() && !appKfx.exists()) {
                                    if (EpubTranscoder.transcodeToKfx(f, appKfx)) {
                                        kfxConverted++;
                                        try {
                                            com.amazon.kindle.epub.ingest.EpubMetadataExtractor.BookMeta meta =
                                                    com.amazon.kindle.epub.ingest.EpubMetadataExtractor.extract(f, f.getName());
                                            com.amazon.kindle.epub.ingest.MetadataEnricher.enrichInPlace(meta);
                                            String bKey = com.amazon.kindle.epub.ingest.FolioMetadataDb.computeBookKey(meta.title, f.length());
                                            File cov = new File(appKfx.getAbsolutePath() + ".cover.jpg");
                                            com.amazon.kindle.epub.ingest.FolioMetadataDb mdb =
                                                    new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
                                            try {
                                                mdb.upsertBook(bKey, appKfx, cov, f.getName(), meta);
                                            } finally {
                                                mdb.close();
                                            }
                                            com.amazon.kindle.epub.ingest.FolioMetadataDb.registerInKindleLibraryDb(
                                                    ctx, bKey, appKfx, cov, meta);
                                        } catch (Throwable ignored) {}
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

    private static void showReadingLogDialog(final Context ctx) {
        final File outFile = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportReadingLogMarkdown(ctx);
        final String md = com.amazon.kindle.epub.ingest.FolioMetadataBackup.buildReadingLogMarkdown(ctx);
        float d = ctx.getResources().getDisplayMetrics().density;

        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(0xFF161922);
        box.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        LinearLayout topExportRow = new LinearLayout(ctx);
        topExportRow.setOrientation(LinearLayout.HORIZONTAL);
        topExportRow.setGravity(Gravity.CENTER_VERTICAL);
        topExportRow.setPadding(0, 0, 0, dp(d, 10));

        final TextView pathTv = new TextView(ctx);
        pathTv.setText(outFile != null
                ? "Saved: " + outFile.getAbsolutePath()
                : "Markdown preview ready");
        pathTv.setTextColor(0xFF34D399);
        pathTv.setTextSize(11);
        pathTv.setTypeface(Typeface.DEFAULT_BOLD);
        pathTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        topExportRow.addView(pathTv);

        TextView csvBtn = new TextView(ctx);
        csvBtn.setText("📊 Export CSV");
        csvBtn.setTextColor(0xFFF59E0B);
        csvBtn.setTextSize(11);
        csvBtn.setTypeface(Typeface.DEFAULT_BOLD);
        csvBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
        GradientDrawable csvBg = new GradientDrawable();
        csvBg.setColor(0xFF282216);
        csvBg.setCornerRadius(dp(d, 10));
        csvBg.setStroke(dp(d, 1), 0x66F59E0B);
        csvBtn.setBackground(csvBg);
        LinearLayout.LayoutParams csvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        csvLp.leftMargin = dp(d, 8);
        csvBtn.setLayoutParams(csvLp);
        csvBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                File csvFile = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportLibraryCsv(ctx);
                if (csvFile != null) {
                    pathTv.setText("Saved CSV: " + csvFile.getAbsolutePath());
                    Toast.makeText(ctx, "Exported CSV to " + csvFile.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "CSV export failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        topExportRow.addView(csvBtn);
        box.addView(topExportRow);

        ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 340)));
        TextView mdTv = new TextView(ctx);
        mdTv.setText(md);
        mdTv.setTextColor(0xFFE2E8F0);
        mdTv.setTextSize(12);
        mdTv.setTypeface(Typeface.MONOSPACE);
        mdTv.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
        GradientDrawable mdBg = new GradientDrawable();
        mdBg.setColor(0xFF11131A);
        mdBg.setCornerRadius(dp(d, 10));
        mdBg.setStroke(dp(d, 1), 0xFF2A3142);
        mdTv.setBackground(mdBg);
        sv.addView(mdTv);
        box.addView(sv);

        new AlertDialog.Builder(ctx)
                .setTitle("Reading Log (.md)")
                .setView(box)
                .setPositiveButton("Close", null)
                .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_SUBJECT, "Folio Reading Log");
                            share.putExtra(Intent.EXTRA_TEXT, md);
                            ctx.startActivity(Intent.createChooser(share, "Share Folio Reading Log"));
                        } catch (Throwable ignored) {}
                    }
                })
                .setNeutralButton("Copy Markdown", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                    ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Reading Log", md));
                                Toast.makeText(ctx, "Copied Reading Log Markdown to clipboard", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Throwable ignored) {}
                    }
                })
                .show();
    }

    private static void showAboutDialog(final Context ctx) {
        // Collect live stats
        int indexedBooks = 0;
        int booksInProgress = 0;
        int booksFinished = 0;
        try {
            File dbFile = new File(ctx.getApplicationInfo().dataDir, "databases/kindle_library.db");
            if (dbFile.exists()) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                try {
                    Cursor c = db.rawQuery("SELECT COUNT(*) FROM KindleContent WHERE TITLE IS NOT NULL AND TITLE != ''", null);
                    if (c != null) {
                        if (c.moveToFirst()) indexedBooks = c.getInt(0);
                        c.close();
                    }
                    try {
                        Cursor cFin = db.rawQuery(
                                "SELECT COUNT(*) FROM KindleContent k LEFT JOIN UserContent u ON u.KEY = k.ID "
                                + "WHERE UPPER(COALESCE(k.READ_STATE, '')) = 'READ' OR COALESCE(u.READING_PROGRESS, 0) >= 100", null);
                        if (cFin != null) {
                            if (cFin.moveToFirst()) booksFinished = cFin.getInt(0);
                            cFin.close();
                        }
                    } catch (Throwable ignored) {}
                    try {
                        Cursor cProg = db.rawQuery(
                                "SELECT COUNT(*) FROM UserContent u JOIN KindleContent k ON u.KEY = k.ID "
                                + "WHERE COALESCE(u.READING_PROGRESS, 0) > 0 AND COALESCE(u.READING_PROGRESS, 0) < 100 "
                                + "AND UPPER(COALESCE(k.READ_STATE, '')) != 'READ'", null);
                        if (cProg != null) {
                            if (cProg.moveToFirst()) booksInProgress = cProg.getInt(0);
                            cProg.close();
                        }
                    } catch (Throwable ignored) {}
                } finally {
                    db.close();
                }
            }
        } catch (Throwable ignored) {}

        int metaRecords = 0;
        try {
            com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
            try {
                metaRecords = mdb.getAllBooks().size();
            } finally {
                mdb.close();
            }
        } catch (Throwable ignored) {}

        long appLibBytes = dirSizeBytes(new File(ctx.getFilesDir(), "folio_library"))
                + dirSizeBytes(new File(ctx.getFilesDir(), "folio_covers"));
        long tempCacheBytes = dirSizeBytes(new File(ctx.getCacheDir(), "folio_once"))
                + dirSizeBytes(new File(ctx.getCacheDir(), "folio_staging"));
        long sdKindleBytes = dirSizeBytes(new File("/sdcard/kindle"));

        double appLibMb = appLibBytes / (1024.0 * 1024.0);
        double tempCacheMb = tempCacheBytes / (1024.0 * 1024.0);
        double sdKindleMb = sdKindleBytes / (1024.0 * 1024.0);

        StringBuilder sb = new StringBuilder();
        sb.append("Folio — Offline EPUB & KFX Reader\n");
        sb.append("Version: Folio v7.4 KFX (Enhanced Typesetting)\n");
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

        sb.append("Live Library & Storage Stats:\n");
        sb.append("• Indexed Library Books: ").append(indexedBooks).append("\n");
        sb.append("• Enriched Metadata Records: ").append(metaRecords).append("\n");
        sb.append("• Reading Progress: ").append(booksInProgress).append(" in progress · ").append(booksFinished).append(" finished\n");
        int favTotal = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countFavorites(ctx);
        int noteTotal = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countNotes(ctx);
        sb.append("• Favorites & Notes: ").append(favTotal).append(" favorited · ").append(noteTotal).append(" annotated\n");
        sb.append("• App Library Storage: ").append(String.format("%.1f", appLibMb)).append(" MB\n");
        sb.append("• Legacy /sdcard/kindle: ").append(String.format("%.1f", sdKindleMb)).append(" MB\n");
        sb.append("• Temp Open-Once Cache: ").append(String.format("%.1f", tempCacheMb)).append(" MB\n");
        sb.append("• Privacy Mode: Local-First (Zero Amazon Telemetry)\n");

        new AlertDialog.Builder(ctx)
                .setTitle("About Folio")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .setNeutralButton("Clear Temp Cache", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        long freed = clearDirContents(new File(ctx.getCacheDir(), "folio_once"))
                                + clearDirContents(new File(ctx.getCacheDir(), "folio_staging"));
                        double mb = freed / (1024.0 * 1024.0);
                        Toast.makeText(ctx,
                                String.format("Cleared %.1f MB of temporary cache", mb),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private static long dirSizeBytes(File dir) {
        if (dir == null || !dir.exists()) return 0L;
        if (dir.isFile()) return dir.length();
        long sum = 0L;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File f : children) {
                sum += f.isDirectory() ? dirSizeBytes(f) : f.length();
            }
        }
        return sum;
    }

    private static long clearDirContents(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return 0L;
        long freed = 0L;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File f : children) {
                if (f.isDirectory()) {
                    freed += clearDirContents(f);
                    f.delete();
                } else {
                    freed += f.length();
                    f.delete();
                }
            }
        }
        return freed;
    }

    private static int dp(float density, int value) {
        return (int) (value * density + 0.5f);
    }
}
