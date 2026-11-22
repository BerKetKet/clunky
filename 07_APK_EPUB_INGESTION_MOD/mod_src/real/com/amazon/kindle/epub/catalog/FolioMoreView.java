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
        badgeTv.setText("Folio v9.0 KFX");
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
        final LinearLayout[] continueHostRef = new LinearLayout[1];
        final LinearLayout[] upNextHostRef = new LinearLayout[1];
        final Runnable refreshStats = new Runnable() {
            @Override public void run() {
                int[] s = readQuickStats(ctx);
                if (statNums[0] != null) statNums[0].setText(String.valueOf(s[0]));
                if (statNums[1] != null) statNums[1].setText(String.valueOf(s[1]));
                if (statNums[2] != null) statNums[2].setText(String.valueOf(s[2]));
                if (statNums[3] != null) statNums[3].setText(String.valueOf(s[3]));
                updateGoalCard(ctx, s[2], goalStatusRef[0], goalFillRef[0], goalTrackRef[0]);
                if (continueHostRef[0] != null) {
                    populateContinueReadingStrip(ctx, d, continueHostRef[0], this);
                }
                if (upNextHostRef[0] != null) {
                    populateUpNextStrip(ctx, d, upNextHostRef[0], this);
                }
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

        // 1d. Continue Reading Quick-Resume Carousel (in-progress books with standalone cover thumbnails)
        LinearLayout continueHost = new LinearLayout(ctx);
        continueHost.setOrientation(LinearLayout.VERTICAL);
        continueHostRef[0] = continueHost;
        populateContinueReadingStrip(ctx, d, continueHost, refreshStats);
        container.addView(continueHost);

        // 1e. Up Next Reading Queue Carousel / Quick-Pick Bar
        LinearLayout upNextHost = new LinearLayout(ctx);
        upNextHost.setOrientation(LinearLayout.VERTICAL);
        upNextHostRef[0] = upNextHost;
        populateUpNextStrip(ctx, d, upNextHost, refreshStats);
        container.addView(upNextHost);

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

        // Card C2b: Reading Notes & Quotes Notebook
        container.addView(makeActionCard(
                ctx, d,
                "📝", "Reading Notes Notebook",
                "Browse, search, copy & edit personal book notes in one place",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNotesNotebookDialog(ctx);
                    }
                }
        ));

        // Card C3: Library Insights & Visual Analytics
        container.addView(makeActionCard(
                ctx, d,
                "📊", "Library Insights",
                "Top authors, top genres, average rating & shelf stats",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLibraryInsightsDialog(ctx);
                    }
                }
        ));

        // Card C4: Custom Shelves Manager
        container.addView(makeActionCard(
                ctx, d,
                "🏷", "Manage Shelves",
                "Rename, remove, or open your custom reading shelves",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showManageShelvesDialog(ctx);
                    }
                }
        ));

        // Card C5: Storage & Duplicates Manager
        container.addView(makeActionCard(
                ctx, d,
                "💾", "Storage & Duplicates",
                "Inspect largest KFX books, cache size & duplicate titles",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showStorageDuplicatesDialog(ctx);
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

    private static void populateContinueReadingStrip(
            final Context ctx, final float d, final LinearLayout host, final Runnable onCatalogDismiss) {
        host.removeAllViews();
        final java.util.Map<String, Integer> progMap =
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.loadProgressMap(ctx);
        if (progMap == null || progMap.isEmpty()) {
            host.setVisibility(View.GONE);
            return;
        }

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> inProgress = new java.util.ArrayList<>();
        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            if (r.kfxPath == null || r.kfxPath.isEmpty()) continue;
            Integer p = progMap.get(r.kfxPath);
            if (p != null && p > 0 && p < 100) {
                inProgress.add(r);
            }
        }

        if (inProgress.isEmpty()) {
            host.setVisibility(View.GONE);
            return;
        }
        host.setVisibility(View.VISIBLE);

        java.util.Collections.sort(inProgress, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                int pa = progMap.containsKey(a.kfxPath) ? progMap.get(a.kfxPath) : 0;
                int pb = progMap.containsKey(b.kfxPath) ? progMap.get(b.kfxPath) : 0;
                int cmp = Integer.compare(pb, pa);
                if (cmp != 0) return cmp;
                String ta = a.title != null ? a.title : "";
                String tb = b.title != null ? b.title : "";
                return ta.compareToIgnoreCase(tb);
            }
        });

        LinearLayout wrapper = new LinearLayout(ctx);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(d, 14), dp(d, 12), dp(d, 14), dp(d, 12));
        GradientDrawable wBg = new GradientDrawable();
        wBg.setColor(0xFF1A1F2E);
        wBg.setCornerRadius(dp(d, 12));
        wBg.setStroke(dp(d, 1), 0xFF2A3142);
        wrapper.setBackground(wBg);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.bottomMargin = dp(d, 16);
        wrapper.setLayoutParams(wLp);

        LinearLayout hdr = new LinearLayout(ctx);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        hdr.setPadding(0, 0, 0, dp(d, 10));

        TextView hdrTitle = new TextView(ctx);
        hdrTitle.setText("📖 Continue Reading");
        hdrTitle.setTextColor(TEXT_PRIMARY);
        hdrTitle.setTextSize(13);
        hdrTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hdrTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdr.addView(hdrTitle);

        TextView seeAll = new TextView(ctx);
        seeAll.setText("See All (" + inProgress.size() + ") ›");
        seeAll.setTextColor(0xFF5EEAD4);
        seeAll.setTextSize(12);
        seeAll.setTypeface(Typeface.DEFAULT_BOLD);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Reading", onCatalogDismiss);
            }
        });
        hdr.addView(seeAll);
        wrapper.addView(hdr);

        android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(ctx);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout strip = new LinearLayout(ctx);
        strip.setOrientation(LinearLayout.HORIZONTAL);

        int limit = Math.min(inProgress.size(), 8);
        for (int i = 0; i < limit; i++) {
            final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = inProgress.get(i);
            final int pct = progMap.containsKey(rec.kfxPath) ? progMap.get(rec.kfxPath) : 0;

            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(d, 10), dp(d, 10), dp(d, 10), dp(d, 9));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(0xFF121620);
            cBg.setCornerRadius(dp(d, 10));
            cBg.setStroke(dp(d, 1), 0xFF273043);
            card.setBackground(cBg);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    dp(d, 196), ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < limit - 1) cLp.rightMargin = dp(d, 10);
            card.setLayoutParams(cLp);

            LinearLayout topRow = new LinearLayout(ctx);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            // Dedicated standalone cover thumbnail (FIT_CENTER on dark matte)
            android.widget.ImageView covIv = new android.widget.ImageView(ctx);
            covIv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            covIv.setBackgroundColor(0xFF0D1017);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(d, 38), dp(d, 54));
            ivLp.rightMargin = dp(d, 9);
            covIv.setLayoutParams(ivLp);
            android.graphics.Bitmap bmp = decodeMiniCover(rec.kfxPath, dp(d, 48));
            if (bmp != null) {
                covIv.setImageBitmap(bmp);
            } else {
                covIv.setBackgroundColor(0xFF1F2636);
            }
            topRow.addView(covIv);

            LinearLayout metaCol = new LinearLayout(ctx);
            metaCol.setOrientation(LinearLayout.VERTICAL);
            metaCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tTv = new TextView(ctx);
            tTv.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
            tTv.setTextColor(TEXT_PRIMARY);
            tTv.setTextSize(12);
            tTv.setTypeface(Typeface.DEFAULT_BOLD);
            tTv.setMinLines(2);
            tTv.setMaxLines(2);
            tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(tTv);

            String authorDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
            TextView aTv = new TextView(ctx);
            aTv.setText(!authorDisp.isEmpty() ? authorDisp : "Unknown Author");
            aTv.setTextColor(0xFF94A3B8);
            aTv.setTextSize(10.5f);
            aTv.setSingleLine(true);
            aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            aTv.setPadding(0, dp(d, 2), 0, dp(d, 2));
            metaCol.addView(aTv);

            TextView pTv = new TextView(ctx);
            pTv.setText(pct + "% read · Resume ›");
            pTv.setTextColor(ACCENT_TEAL);
            pTv.setTextSize(10.5f);
            pTv.setTypeface(Typeface.DEFAULT_BOLD);
            pTv.setSingleLine(true);
            metaCol.addView(pTv);

            topRow.addView(metaCol);
            card.addView(topRow);

            // Sleek 3dp progress mini-bar
            LinearLayout track = new LinearLayout(ctx);
            track.setOrientation(LinearLayout.HORIZONTAL);
            GradientDrawable trBg = new GradientDrawable();
            trBg.setColor(0xFF232A3A);
            trBg.setCornerRadius(dp(d, 2));
            track.setBackground(trBg);
            LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 3));
            trLp.topMargin = dp(d, 8);
            track.setLayoutParams(trLp);

            float frac = Math.max(0.04f, Math.min(1f, pct / 100f));
            View fill = new View(ctx);
            GradientDrawable flBg = new GradientDrawable();
            flBg.setColor(ACCENT_TEAL);
            flBg.setCornerRadius(dp(d, 2));
            fill.setBackground(flBg);
            fill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, frac));
            track.addView(fill);

            View rest = new View(ctx);
            rest.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f - frac));
            track.addView(rest);
            card.addView(track);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File f = new File(rec.kfxPath);
                    if (f.exists()) {
                        com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                    } else {
                        Toast.makeText(ctx, "Book file not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            strip.addView(card);
        }

        hsv.addView(strip);
        wrapper.addView(hsv);
        host.addView(wrapper);
    }

    private static void populateUpNextStrip(
            final Context ctx, final float d, final LinearLayout host, final Runnable onRefresh) {
        host.removeAllViews();
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> queued = new java.util.ArrayList<>();
        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            if (r.kfxPath != null && com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
                queued.add(r);
            }
        }

        LinearLayout wrapper = new LinearLayout(ctx);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(d, 14), dp(d, 11), dp(d, 14), dp(d, 11));
        GradientDrawable wBg = new GradientDrawable();
        wBg.setColor(0xFF1A1F2E);
        wBg.setCornerRadius(dp(d, 12));
        wBg.setStroke(dp(d, 1), queued.isEmpty() ? 0xFF2A3142 : 0xFF4C3575);
        wrapper.setBackground(wBg);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.bottomMargin = dp(d, 16);
        wrapper.setLayoutParams(wLp);

        if (queued.isEmpty()) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout txtCol = new LinearLayout(ctx);
            txtCol.setOrientation(LinearLayout.VERTICAL);
            txtCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView t1 = new TextView(ctx);
            t1.setText("📌 Up Next Queue");
            t1.setTextColor(TEXT_PRIMARY);
            t1.setTextSize(13);
            t1.setTypeface(Typeface.DEFAULT_BOLD);
            txtCol.addView(t1);

            TextView t2 = new TextView(ctx);
            t2.setText("Pin unread books in Details (ⓘ) to plan your reading queue");
            t2.setTextColor(TEXT_MUTED);
            t2.setTextSize(11.5f);
            txtCol.addView(t2);
            row.addView(txtCol);

            TextView pickBtn = new TextView(ctx);
            pickBtn.setText("+ Pick ›");
            pickBtn.setTextColor(0xFFC084FC);
            pickBtn.setTextSize(12);
            pickBtn.setTypeface(Typeface.DEFAULT_BOLD);
            pickBtn.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(0xFF2B1B3D);
            pBg.setCornerRadius(dp(d, 8));
            pBg.setStroke(dp(d, 1), 0xFF6B21A8);
            pickBtn.setBackground(pBg);
            row.addView(pickBtn);

            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Unread", onRefresh);
                }
            });
            wrapper.addView(row);
            host.addView(wrapper);
            return;
        }

        // Sort FIFO by queue insertion timestamp ascending
        java.util.Collections.sort(queued, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                long ta = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getQueueTimestamp(ctx, a.kfxPath);
                long tb = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getQueueTimestamp(ctx, b.kfxPath);
                return Long.compare(ta, tb);
            }
        });

        LinearLayout hdr = new LinearLayout(ctx);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        hdr.setPadding(0, 0, 0, dp(d, 10));

        TextView hdrTitle = new TextView(ctx);
        hdrTitle.setText("📌 Up Next Queue");
        hdrTitle.setTextColor(TEXT_PRIMARY);
        hdrTitle.setTextSize(13);
        hdrTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hdrTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdr.addView(hdrTitle);

        TextView seeAll = new TextView(ctx);
        seeAll.setText("See All (" + queued.size() + ") ›");
        seeAll.setTextColor(0xFFC084FC);
        seeAll.setTextSize(12);
        seeAll.setTypeface(Typeface.DEFAULT_BOLD);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Queue", onRefresh);
            }
        });
        hdr.addView(seeAll);
        wrapper.addView(hdr);

        android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(ctx);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout strip = new LinearLayout(ctx);
        strip.setOrientation(LinearLayout.HORIZONTAL);

        int limit = Math.min(queued.size(), 8);
        for (int i = 0; i < limit; i++) {
            final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = queued.get(i);

            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(d, 10), dp(d, 9), dp(d, 8), dp(d, 9));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(0xFF141622);
            cBg.setCornerRadius(dp(d, 10));
            cBg.setStroke(dp(d, 1), 0xFF3A2B54);
            card.setBackground(cBg);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    dp(d, 196), ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < limit - 1) cLp.rightMargin = dp(d, 10);
            card.setLayoutParams(cLp);

            android.widget.ImageView covIv = new android.widget.ImageView(ctx);
            covIv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            covIv.setBackgroundColor(0xFF0D1017);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(d, 36), dp(d, 52));
            ivLp.rightMargin = dp(d, 9);
            covIv.setLayoutParams(ivLp);
            android.graphics.Bitmap bmp = decodeMiniCover(rec.kfxPath, dp(d, 44));
            if (bmp != null) covIv.setImageBitmap(bmp);
            card.addView(covIv);

            LinearLayout metaCol = new LinearLayout(ctx);
            metaCol.setOrientation(LinearLayout.VERTICAL);
            metaCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tTv = new TextView(ctx);
            tTv.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
            tTv.setTextColor(TEXT_PRIMARY);
            tTv.setTextSize(12);
            tTv.setTypeface(Typeface.DEFAULT_BOLD);
            tTv.setMaxLines(2);
            tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(tTv);

            String authorDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
            TextView aTv = new TextView(ctx);
            aTv.setText(!authorDisp.isEmpty() ? authorDisp : "Unknown Author");
            aTv.setTextColor(0xFF94A3B8);
            aTv.setTextSize(10.5f);
            aTv.setSingleLine(true);
            aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(aTv);

            TextView goTv = new TextView(ctx);
            goTv.setText("Start Reading ›");
            goTv.setTextColor(0xFFC084FC);
            goTv.setTextSize(10.5f);
            goTv.setTypeface(Typeface.DEFAULT_BOLD);
            goTv.setPadding(0, dp(d, 2), 0, 0);
            metaCol.addView(goTv);
            card.addView(metaCol);

            TextView rmBtn = new TextView(ctx);
            rmBtn.setText("✕");
            rmBtn.setTextColor(0xFF64748B);
            rmBtn.setTextSize(12);
            rmBtn.setPadding(dp(d, 6), dp(d, 4), dp(d, 4), dp(d, 4));
            rmBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.setInQueue(ctx, rec.kfxPath, false);
                    Toast.makeText(ctx, "Removed from Up Next Queue", Toast.LENGTH_SHORT).show();
                    if (onRefresh != null) onRefresh.run();
                }
            });
            card.addView(rmBtn);

            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    File f = new File(rec.kfxPath);
                    if (f.exists()) {
                        com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                    }
                }
            });
            strip.addView(card);
        }

        hsv.addView(strip);
        wrapper.addView(hsv);
        host.addView(wrapper);
    }

    private static android.graphics.Bitmap decodeMiniCover(String kfxPath, int targetPx) {
        if (kfxPath == null || kfxPath.isEmpty()) return null;
        try {
            File cov = new File(kfxPath + ".cover.jpg");
            if (!cov.exists() || cov.length() < 64) return null;
            android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(cov.getAbsolutePath(), opts);
            int sample = 1;
            int minDim = Math.min(opts.outWidth, opts.outHeight);
            while (minDim / (sample * 2) >= Math.max(48, targetPx)) {
                sample *= 2;
            }
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sample;
            return android.graphics.BitmapFactory.decodeFile(cov.getAbsolutePath(), opts);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void showNotesNotebookDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> annotated = new java.util.ArrayList<>();
        final java.util.Map<String, String> noteMap = new java.util.HashMap<>();
        final java.util.Map<String, Integer> rateMap = new java.util.HashMap<>();
        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            if (r.kfxPath == null || r.kfxPath.isEmpty()) continue;
            String n = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);
            if (n != null && !n.trim().isEmpty()) {
                annotated.add(r);
                noteMap.put(r.kfxPath, n.trim());
                rateMap.put(r.kfxPath, com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath));
            }
        }

        java.util.Collections.sort(annotated, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                int ra = rateMap.containsKey(a.kfxPath) ? rateMap.get(a.kfxPath) : 0;
                int rb = rateMap.containsKey(b.kfxPath) ? rateMap.get(b.kfxPath) : 0;
                int cmp = Integer.compare(rb, ra);
                if (cmp != 0) return cmp;
                String ta = a.title != null ? a.title : "";
                String tb = b.title != null ? b.title : "";
                return ta.compareToIgnoreCase(tb);
            }
        });

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF11131A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        final TextView titleTv = new TextView(ctx);
        titleTv.setText("📝 Reading Notes (" + annotated.size() + ")");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        TextView subTv = new TextView(ctx);
        subTv.setText(annotated.isEmpty()
                ? "No personal reading notes saved yet"
                : "Tap any book header to read, or copy & edit notes directly");
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 10));
        root.addView(subTv);

        final AlertDialog[] nbDlgRef = new AlertDialog[1];

        final EditText searchEt = new EditText(ctx);
        searchEt.setHint("Search notes, titles, or authors...");
        searchEt.setHintTextColor(0xFF64748B);
        searchEt.setTextColor(TEXT_PRIMARY);
        searchEt.setTextSize(13);
        searchEt.setSingleLine(true);
        searchEt.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(0xFF1A1F2E);
        sBg.setCornerRadius(dp(d, 8));
        sBg.setStroke(dp(d, 1), 0xFF2A3142);
        searchEt.setBackground(sBg);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.bottomMargin = dp(d, 10);
        searchEt.setLayoutParams(sLp);
        if (!annotated.isEmpty()) {
            root.addView(searchEt);
        }

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, annotated.isEmpty() ? 110 : 340)));
        final LinearLayout listBox = new LinearLayout(ctx);
        listBox.setOrientation(LinearLayout.VERTICAL);
        sv.addView(listBox);
        root.addView(sv);

        final Runnable renderNotes = new Runnable() {
            @Override
            public void run() {
                listBox.removeAllViews();
                String q = searchEt.getText() != null
                        ? searchEt.getText().toString().trim().toLowerCase(java.util.Locale.ROOT)
                        : "";
                if (annotated.isEmpty()) {
                    TextView emptyTv = new TextView(ctx);
                    emptyTv.setText("Open any book's Details (ⓘ) in the Metadata Catalog and tap \"Add Note\" to save personal thoughts or quotes.");
                    emptyTv.setTextColor(0xFF94A3B8);
                    emptyTv.setTextSize(13);
                    emptyTv.setPadding(dp(d, 14), dp(d, 14), dp(d, 14), dp(d, 14));
                    GradientDrawable eBg = new GradientDrawable();
                    eBg.setColor(BG_CARD);
                    eBg.setCornerRadius(dp(d, 10));
                    eBg.setStroke(dp(d, 1), 0xFF2A3140);
                    emptyTv.setBackground(eBg);
                    listBox.addView(emptyTv);
                    return;
                }
                int shown = 0;
                for (final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec : annotated) {
                    final String noteTxt = noteMap.get(rec.kfxPath);
                    final String titleStr = (rec.title != null && !rec.title.isEmpty()) ? rec.title : "Untitled";
                    final String authorStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                    if (!q.isEmpty()) {
                        String hay = (titleStr + " " + authorStr + " " + noteTxt).toLowerCase(java.util.Locale.ROOT);
                        if (!hay.contains(q)) continue;
                    }
                    shown++;

                    LinearLayout card = new LinearLayout(ctx);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(BG_CARD);
                    cBg.setCornerRadius(dp(d, 10));
                    cBg.setStroke(dp(d, 1), 0xFF2A3140);
                    card.setBackground(cBg);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.bottomMargin = dp(d, 10);
                    card.setLayoutParams(cLp);

                    // Top book header row (cover + title/author/rating)
                    LinearLayout hdrRow = new LinearLayout(ctx);
                    hdrRow.setOrientation(LinearLayout.HORIZONTAL);
                    hdrRow.setGravity(Gravity.CENTER_VERTICAL);

                    android.widget.ImageView covIv = new android.widget.ImageView(ctx);
                    covIv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                    covIv.setBackgroundColor(0xFF0D1017);
                    LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(d, 32), dp(d, 46));
                    ivLp.rightMargin = dp(d, 10);
                    covIv.setLayoutParams(ivLp);
                    android.graphics.Bitmap bmp = decodeMiniCover(rec.kfxPath, dp(d, 42));
                    if (bmp != null) covIv.setImageBitmap(bmp);
                    hdrRow.addView(covIv);

                    LinearLayout metaCol = new LinearLayout(ctx);
                    metaCol.setOrientation(LinearLayout.VERTICAL);
                    metaCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView bTitle = new TextView(ctx);
                    bTitle.setText(titleStr);
                    bTitle.setTextColor(TEXT_PRIMARY);
                    bTitle.setTextSize(13.5f);
                    bTitle.setTypeface(Typeface.DEFAULT_BOLD);
                    bTitle.setSingleLine(true);
                    bTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    metaCol.addView(bTitle);

                    int st = rateMap.containsKey(rec.kfxPath) ? rateMap.get(rec.kfxPath) : 0;
                    String sh = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getShelf(ctx, rec.kfxPath);
                    StringBuilder subMeta = new StringBuilder();
                    subMeta.append(!authorStr.isEmpty() ? authorStr : "Unknown Author");
                    if (st > 0) subMeta.append(" · ★ ").append(st).append("/5");
                    if (sh != null && !sh.isEmpty()) subMeta.append(" · 🏷 ").append(sh);
                    TextView bSub = new TextView(ctx);
                    bSub.setText(subMeta.toString());
                    bSub.setTextColor(0xFF94A3B8);
                    bSub.setTextSize(11);
                    bSub.setSingleLine(true);
                    bSub.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    metaCol.addView(bSub);
                    hdrRow.addView(metaCol);

                    hdrRow.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            File f = new File(rec.kfxPath);
                            if (f.exists()) {
                                if (nbDlgRef[0] != null) nbDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                            }
                        }
                    });
                    card.addView(hdrRow);

                    // Warm amber-accented quote block
                    LinearLayout quoteWrap = new LinearLayout(ctx);
                    quoteWrap.setOrientation(LinearLayout.HORIZONTAL);
                    GradientDrawable qBg = new GradientDrawable();
                    qBg.setColor(0xFF1F1B14);
                    qBg.setCornerRadius(dp(d, 7));
                    qBg.setStroke(dp(d, 1), 0xFF45361A);
                    quoteWrap.setBackground(qBg);
                    LinearLayout.LayoutParams qwLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    qwLp.topMargin = dp(d, 8);
                    qwLp.bottomMargin = dp(d, 8);
                    quoteWrap.setLayoutParams(qwLp);

                    View accentBar = new View(ctx);
                    accentBar.setBackgroundColor(0xFFF59E0B);
                    accentBar.setLayoutParams(new LinearLayout.LayoutParams(dp(d, 3), ViewGroup.LayoutParams.MATCH_PARENT));
                    quoteWrap.addView(accentBar);

                    TextView noteTv = new TextView(ctx);
                    noteTv.setText(noteTxt);
                    noteTv.setTextColor(0xFFFDE68A);
                    noteTv.setTextSize(12.5f);
                    noteTv.setPadding(dp(d, 10), dp(d, 8), dp(d, 10), dp(d, 8));
                    noteTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    quoteWrap.addView(noteTv);
                    card.addView(quoteWrap);

                    // Action pills row: Copy & Edit
                    LinearLayout actRow = new LinearLayout(ctx);
                    actRow.setOrientation(LinearLayout.HORIZONTAL);
                    actRow.setGravity(Gravity.END);

                    TextView copyBtn = new TextView(ctx);
                    copyBtn.setText("📋 Copy");
                    copyBtn.setTextColor(0xFF94A3B8);
                    copyBtn.setTextSize(11);
                    copyBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    copyBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
                    GradientDrawable cpBg = new GradientDrawable();
                    cpBg.setColor(0xFF1E2433);
                    cpBg.setCornerRadius(dp(d, 6));
                    cpBg.setStroke(dp(d, 1), 0xFF334155);
                    copyBtn.setBackground(cpBg);
                    LinearLayout.LayoutParams cpLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cpLp.rightMargin = dp(d, 6);
                    copyBtn.setLayoutParams(cpLp);
                    copyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                    ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                String clip = titleStr + (authorStr.isEmpty() ? "" : " — " + authorStr) + "\n" + noteTxt;
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Note", clip));
                                Toast.makeText(ctx, "Copied note to clipboard", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    actRow.addView(copyBtn);

                    TextView editBtn = new TextView(ctx);
                    editBtn.setText("✏️ Edit");
                    editBtn.setTextColor(ACCENT_TEAL);
                    editBtn.setTextSize(11);
                    editBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    editBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
                    GradientDrawable edBg = new GradientDrawable();
                    edBg.setColor(0xFF172536);
                    edBg.setCornerRadius(dp(d, 6));
                    edBg.setStroke(dp(d, 1), 0xFF1E3A5F);
                    editBtn.setBackground(edBg);
                    editBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            showInlineNoteEditor(ctx, rec.kfxPath, titleStr, noteTxt, nbDlgRef[0]);
                        }
                    });
                    actRow.addView(editBtn);
                    card.addView(actRow);

                    listBox.addView(card);
                }
                titleTv.setText(q.isEmpty()
                        ? "📝 Reading Notes (" + annotated.size() + ")"
                        : "📝 Reading Notes (" + shown + " of " + annotated.size() + ")");
                if (shown == 0) {
                    TextView noMatch = new TextView(ctx);
                    noMatch.setText("No notes match \"" + q + "\".");
                    noMatch.setTextColor(TEXT_MUTED);
                    noMatch.setTextSize(12.5f);
                    noMatch.setPadding(dp(d, 6), dp(d, 12), dp(d, 6), dp(d, 12));
                    listBox.addView(noMatch);
                }
            }
        };

        searchEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { renderNotes.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        renderNotes.run();

        AlertDialog.Builder b = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton("Close", null);
        if (!annotated.isEmpty()) {
            b.setNeutralButton("Copy All", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    StringBuilder sb = new StringBuilder("# Folio Reading Notes\n\n");
                    for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : annotated) {
                        String t = (r.title != null && !r.title.isEmpty()) ? r.title : "Untitled";
                        String a = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                        sb.append("## ").append(t);
                        if (!a.isEmpty()) sb.append(" — ").append(a);
                        sb.append("\n> ").append(noteMap.get(r.kfxPath)).append("\n\n");
                    }
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Reading Notes", sb.toString()));
                        Toast.makeText(ctx, "Copied " + annotated.size() + " note(s) as Markdown", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        nbDlgRef[0] = b.show();
    }

    private static void showInlineNoteEditor(
            final Context ctx, final String kfxPath, final String titleStr,
            final String currentNote, final AlertDialog parentDlg) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(d, 20), dp(d, 12), dp(d, 20), dp(d, 6));

        final EditText input = new EditText(ctx);
        input.setText(currentNote);
        input.setSelection(currentNote.length());
        input.setMinLines(3);
        input.setMaxLines(6);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setHint("Write your reading note (leave empty to delete)...");
        input.setTextColor(TEXT_PRIMARY);
        input.setHintTextColor(0xFF64748B);
        box.addView(input);

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Edit Note — " + titleStr)
                .setView(box)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        String val = input.getText().toString().trim();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.setNote(ctx, kfxPath, val);
                        Toast.makeText(ctx, val.isEmpty() ? "Note cleared" : "Saved note", Toast.LENGTH_SHORT).show();
                        if (parentDlg != null) parentDlg.dismiss();
                        showNotesNotebookDialog(ctx);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static String formatBytesShort(long bytes) {
        if (bytes < 1024L) return bytes + " B";
        if (bytes < 1024L * 1024L) return String.format(java.util.Locale.US, "%.0f KB", bytes / 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static void showStorageDuplicatesDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        long appLibBytes = dirSizeBytes(new File(ctx.getFilesDir(), "folio_library"))
                + dirSizeBytes(new File(ctx.getFilesDir(), "folio_covers"));
        long backupBytes = dirSizeBytes(new File("/sdcard/kindle/backups"));
        long tempCacheBytes = dirSizeBytes(new File(ctx.getCacheDir(), "folio_once"))
                + dirSizeBytes(new File(ctx.getCacheDir(), "folio_staging"));

        java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        // Measure each book's KFX + cover size and check normalized title duplicates
        final java.util.Map<String, Long> sizeMap = new java.util.HashMap<>();
        java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> sizedBooks = new java.util.ArrayList<>();
        final java.util.Map<String, Integer> normCounts = new java.util.HashMap<>();
        final java.util.Map<String, String> normLabel = new java.util.HashMap<>();
        long totalBookBytes = 0L;

        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            if (r.kfxPath != null && !r.kfxPath.isEmpty()) {
                File f = new File(r.kfxPath);
                long sz = f.exists() ? f.length() : 0L;
                File cov = new File(r.kfxPath + ".cover.jpg");
                if (cov.exists()) sz += cov.length();
                if (sz > 0L) {
                    sizeMap.put(r.kfxPath, sz);
                    sizedBooks.add(r);
                    totalBookBytes += sz;
                }
            }
            if (r.title != null && !r.title.trim().isEmpty()) {
                String norm = r.title.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
                if (!norm.isEmpty()) {
                    normCounts.put(norm, (normCounts.containsKey(norm) ? normCounts.get(norm) : 0) + 1);
                    if (!normLabel.containsKey(norm)) normLabel.put(norm, r.title.trim());
                }
            }
        }

        java.util.Collections.sort(sizedBooks, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                long sa = sizeMap.containsKey(a.kfxPath) ? sizeMap.get(a.kfxPath) : 0L;
                long sb = sizeMap.containsKey(b.kfxPath) ? sizeMap.get(b.kfxPath) : 0L;
                return Long.compare(sb, sa);
            }
        });

        java.util.List<String> dupKeys = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : normCounts.entrySet()) {
            if (e.getValue() > 1) dupKeys.add(e.getKey());
        }
        java.util.Collections.sort(dupKeys, new java.util.Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(normCounts.get(b), normCounts.get(a));
            }
        });

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF11131A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("💾 Storage & Duplicates");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        TextView subTv = new TextView(ctx);
        subTv.setText(all.size() + " cataloged books (" + formatBytesShort(totalBookBytes)
                + " indexed) · " + dupKeys.size() + (dupKeys.size() == 1 ? " duplicate group" : " duplicate groups"));
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 14));
        root.addView(subTv);

        final AlertDialog[] storDlgRef = new AlertDialog[1];

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 388)));
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, dp(d, 4), dp(d, 10));

        // Section 1: 3-Tile Storage Footprint Breakdown
        TextView sHdr = new TextView(ctx);
        sHdr.setText("STORAGE FOOTPRINT");
        sHdr.setTextColor(0xFF38BDF8);
        sHdr.setTextSize(11);
        sHdr.setTypeface(Typeface.DEFAULT_BOLD);
        sHdr.setPadding(0, 0, 0, dp(d, 8));
        content.addView(sHdr);

        LinearLayout tileRow = new LinearLayout(ctx);
        tileRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trLp.bottomMargin = dp(d, 16);
        tileRow.setLayoutParams(trLp);

        tileRow.addView(makeHealthBadge(ctx, d, "App Library", formatBytesShort(appLibBytes),
                0xFF38BDF8, 0xFF152638, true, null));
        tileRow.addView(makeHealthBadge(ctx, d, "Backups", formatBytesShort(backupBytes),
                0xFF34D399, 0xFF122B24, true, null));
        tileRow.addView(makeHealthBadge(ctx, d, "Temp Cache", formatBytesShort(tempCacheBytes),
                0xFFFBBF24, 0xFF2E2414, false, null));
        content.addView(tileRow);

        // Section 2: Top 5 Largest Books (tap to locate in catalog)
        TextView lHdr = new TextView(ctx);
        lHdr.setText("LARGEST BOOKS (TAP TO LOCATE)");
        lHdr.setTextColor(0xFFF59E0B);
        lHdr.setTextSize(11);
        lHdr.setTypeface(Typeface.DEFAULT_BOLD);
        lHdr.setPadding(0, 0, 0, dp(d, 8));
        content.addView(lHdr);

        if (sizedBooks.isEmpty()) {
            TextView noneTv = new TextView(ctx);
            noneTv.setText("No local KFX files measured yet.");
            noneTv.setTextColor(TEXT_MUTED);
            noneTv.setTextSize(12);
            noneTv.setPadding(0, 0, 0, dp(d, 12));
            content.addView(noneTv);
        } else {
            long maxBytes = Math.max(1L, sizeMap.get(sizedBooks.get(0).kfxPath));
            int topN = Math.min(5, sizedBooks.size());
            for (int i = 0; i < topN; i++) {
                final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = sizedBooks.get(i);
                long sz = sizeMap.get(rec.kfxPath);
                String tLabel = (rec.title != null && !rec.title.isEmpty()) ? rec.title : "Untitled";
                String szLabel = formatBytesShort(sz);
                float frac = Math.max(0.06f, (float) sz / (float) maxBytes);

                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, dp(d, 3), 0, dp(d, 7));

                LinearLayout topR = new LinearLayout(ctx);
                topR.setOrientation(LinearLayout.HORIZONTAL);
                TextView nameTv = new TextView(ctx);
                nameTv.setText(tLabel);
                nameTv.setTextColor(TEXT_PRIMARY);
                nameTv.setTextSize(12.5f);
                nameTv.setSingleLine(true);
                nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                nameTv.setPadding(0, 0, dp(d, 8), 0);
                nameTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                topR.addView(nameTv);

                TextView szTv = new TextView(ctx);
                szTv.setText(szLabel);
                szTv.setTextColor(0xFFF59E0B);
                szTv.setTextSize(12);
                szTv.setTypeface(Typeface.DEFAULT_BOLD);
                topR.addView(szTv);
                row.addView(topR);

                LinearLayout barTrack = new LinearLayout(ctx);
                barTrack.setOrientation(LinearLayout.HORIZONTAL);
                GradientDrawable tbBg = new GradientDrawable();
                tbBg.setColor(0xFF1F2433);
                tbBg.setCornerRadius(dp(d, 3));
                barTrack.setBackground(tbBg);
                LinearLayout.LayoutParams btLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 5));
                btLp.topMargin = dp(d, 4);
                barTrack.setLayoutParams(btLp);

                View barFill = new View(ctx);
                GradientDrawable bfBg = new GradientDrawable();
                bfBg.setColor(0xFFF59E0B);
                bfBg.setCornerRadius(dp(d, 3));
                barFill.setBackground(bfBg);
                barFill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, frac));
                barTrack.addView(barFill);

                View barRest = new View(ctx);
                barRest.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f - frac));
                barTrack.addView(barRest);
                row.addView(barTrack);

                final String searchTarget = tLabel;
                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (storDlgRef[0] != null) storDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, searchTarget);
                    }
                });
                content.addView(row);
            }
        }

        // Section 3: Duplicate Titles Audit
        TextView dHdr = new TextView(ctx);
        dHdr.setText("DUPLICATE TITLES AUDIT");
        dHdr.setTextColor(dupKeys.isEmpty() ? 0xFF34D399 : 0xFFF87171);
        dHdr.setTextSize(11);
        dHdr.setTypeface(Typeface.DEFAULT_BOLD);
        dHdr.setPadding(0, dp(d, 12), 0, dp(d, 8));
        content.addView(dHdr);

        if (dupKeys.isEmpty()) {
            TextView okTv = new TextView(ctx);
            okTv.setText("✓ No duplicate titles found across " + all.size() + " cataloged books");
            okTv.setTextColor(0xFF34D399);
            okTv.setTextSize(12.5f);
            okTv.setTypeface(Typeface.DEFAULT_BOLD);
            okTv.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
            GradientDrawable okBg = new GradientDrawable();
            okBg.setColor(0xFF122B24);
            okBg.setCornerRadius(dp(d, 8));
            okBg.setStroke(dp(d, 1), 0xFF1E5C47);
            okTv.setBackground(okBg);
            content.addView(okTv);
        } else {
            for (String dk : dupKeys) {
                final String dTitle = normLabel.get(dk);
                int cnt = normCounts.get(dk);
                LinearLayout dRow = new LinearLayout(ctx);
                dRow.setOrientation(LinearLayout.HORIZONTAL);
                dRow.setGravity(Gravity.CENTER_VERTICAL);
                dRow.setPadding(dp(d, 12), dp(d, 9), dp(d, 12), dp(d, 9));
                GradientDrawable dBg = new GradientDrawable();
                dBg.setColor(0xFF2B181E);
                dBg.setCornerRadius(dp(d, 8));
                dBg.setStroke(dp(d, 1), 0xFF5C262E);
                dRow.setBackground(dBg);
                LinearLayout.LayoutParams drLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                drLp.bottomMargin = dp(d, 6);
                dRow.setLayoutParams(drLp);

                TextView dtTv = new TextView(ctx);
                dtTv.setText(dTitle);
                dtTv.setTextColor(TEXT_PRIMARY);
                dtTv.setTextSize(12.5f);
                dtTv.setSingleLine(true);
                dtTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                dtTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                dRow.addView(dtTv);

                TextView dcTv = new TextView(ctx);
                dcTv.setText(cnt + " copies ›");
                dcTv.setTextColor(0xFFF87171);
                dcTv.setTextSize(12);
                dcTv.setTypeface(Typeface.DEFAULT_BOLD);
                dRow.addView(dcTv);

                dRow.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (storDlgRef[0] != null) storDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, dTitle);
                    }
                });
                content.addView(dRow);
            }
        }

        sv.addView(content);
        root.addView(sv);

        storDlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setNeutralButton("Clear Temp Cache", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long freed = clearDirContents(new File(ctx.getCacheDir(), "folio_once"))
                                + clearDirContents(new File(ctx.getCacheDir(), "folio_staging"));
                        Toast.makeText(ctx,
                                "Cleared " + formatBytesShort(freed) + " of temporary cache",
                                Toast.LENGTH_SHORT).show();
                        showStorageDuplicatesDialog(ctx);
                    }
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private static void showManageShelvesDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final SharedPreferences shelfPrefs = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);
        final java.util.Map<String, ?> rawAll = shelfPrefs.getAll();
        final java.util.Map<String, Integer> shelfCounts = new java.util.HashMap<>();
        int totalShelved = 0;
        if (rawAll != null) {
            for (java.util.Map.Entry<String, ?> e : rawAll.entrySet()) {
                Object v = e.getValue();
                if (v instanceof String) {
                    String s = ((String) v).trim();
                    if (!s.isEmpty()) {
                        shelfCounts.put(s, (shelfCounts.containsKey(s) ? shelfCounts.get(s) : 0) + 1);
                        totalShelved++;
                    }
                }
            }
        }

        final java.util.List<String> shelves = new java.util.ArrayList<>(shelfCounts.keySet());
        java.util.Collections.sort(shelves, new java.util.Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                int cmp = Integer.compare(shelfCounts.get(b), shelfCounts.get(a));
                return cmp != 0 ? cmp : a.compareToIgnoreCase(b);
            }
        });

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF11131A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("🏷 Manage Shelves");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        String subText = shelves.isEmpty()
                ? "No custom shelves yet"
                : shelves.size() + (shelves.size() == 1 ? " custom shelf · " : " custom shelves · ")
                        + totalShelved + (totalShelved == 1 ? " shelved book" : " shelved books")
                        + " — tap a shelf to browse";
        TextView subTv = new TextView(ctx);
        subTv.setText(subText);
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 14));
        root.addView(subTv);

        final AlertDialog[] mgrDlgRef = new AlertDialog[1];

        if (shelves.isEmpty()) {
            TextView emptyTv = new TextView(ctx);
            emptyTv.setText("Open any book's Details (ⓘ) in the Metadata Catalog to assign a custom reading shelf (e.g. Classics, Sci-Fi, Study).");
            emptyTv.setTextColor(0xFF94A3B8);
            emptyTv.setTextSize(13);
            emptyTv.setPadding(dp(d, 14), dp(d, 16), dp(d, 14), dp(d, 16));
            GradientDrawable emptyBg = new GradientDrawable();
            emptyBg.setColor(BG_CARD);
            emptyBg.setCornerRadius(dp(d, 10));
            emptyBg.setStroke(dp(d, 1), 0xFF2A3140);
            emptyTv.setBackground(emptyBg);
            root.addView(emptyTv);
        } else {
            ScrollView sv = new ScrollView(ctx);
            sv.setVerticalScrollBarEnabled(false);
            LinearLayout listBox = new LinearLayout(ctx);
            listBox.setOrientation(LinearLayout.VERTICAL);

            for (final String shelfName : shelves) {
                final int cnt = shelfCounts.get(shelfName);

                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(d, 12), dp(d, 10), dp(d, 10), dp(d, 10));
                GradientDrawable rowBg = new GradientDrawable();
                rowBg.setColor(BG_CARD);
                rowBg.setCornerRadius(dp(d, 10));
                rowBg.setStroke(dp(d, 1), 0xFF2A3140);
                row.setBackground(rowBg);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowLp.bottomMargin = dp(d, 8);
                row.setLayoutParams(rowLp);

                LinearLayout infoCol = new LinearLayout(ctx);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                infoCol.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                infoCol.setPadding(0, 0, dp(d, 8), 0);

                TextView nameTv = new TextView(ctx);
                nameTv.setText("🏷 " + shelfName);
                nameTv.setTextColor(0xFFC7D2FE);
                nameTv.setTextSize(14);
                nameTv.setTypeface(Typeface.DEFAULT_BOLD);
                nameTv.setSingleLine(true);
                nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                infoCol.addView(nameTv);

                TextView cntTv = new TextView(ctx);
                cntTv.setText(cnt + (cnt == 1 ? " book ›" : " books ›"));
                cntTv.setTextColor(0xFF94A3B8);
                cntTv.setTextSize(11.5f);
                infoCol.addView(cntTv);

                infoCol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mgrDlgRef[0] != null) mgrDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "SHELF:" + shelfName);
                    }
                });
                row.addView(infoCol);

                // Rename pill
                TextView renBtn = new TextView(ctx);
                renBtn.setText("Rename");
                renBtn.setTextColor(ACCENT_TEAL);
                renBtn.setTextSize(11.5f);
                renBtn.setTypeface(Typeface.DEFAULT_BOLD);
                renBtn.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
                GradientDrawable renBg = new GradientDrawable();
                renBg.setColor(0xFF172536);
                renBg.setCornerRadius(dp(d, 7));
                renBg.setStroke(dp(d, 1), 0xFF1E3A5F);
                renBtn.setBackground(renBg);
                LinearLayout.LayoutParams renLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                renLp.rightMargin = dp(d, 6);
                renBtn.setLayoutParams(renLp);
                renBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRenameShelfDialog(ctx, shelfName, cnt, mgrDlgRef[0]);
                    }
                });
                row.addView(renBtn);

                // Remove pill
                TextView delBtn = new TextView(ctx);
                delBtn.setText("Remove");
                delBtn.setTextColor(0xFFF87171);
                delBtn.setTextSize(11.5f);
                delBtn.setTypeface(Typeface.DEFAULT_BOLD);
                delBtn.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
                GradientDrawable delBg = new GradientDrawable();
                delBg.setColor(0xFF2A181C);
                delBg.setCornerRadius(dp(d, 7));
                delBg.setStroke(dp(d, 1), 0xFF5C262E);
                delBtn.setBackground(delBg);
                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRemoveShelfDialog(ctx, shelfName, cnt, mgrDlgRef[0]);
                    }
                });
                row.addView(delBtn);

                listBox.addView(row);
            }

            sv.addView(listBox);
            int maxH = dp(d, Math.min(340, Math.max(96, shelves.size() * 68)));
            sv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, maxH));
            root.addView(sv);
        }

        mgrDlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setNeutralButton("Browse Catalog", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All");
                    }
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private static void showRenameShelfDialog(final Context ctx, final String oldName, final int count, final AlertDialog parentDlg) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(d, 20), dp(d, 12), dp(d, 20), dp(d, 6));

        final EditText input = new EditText(ctx);
        input.setText(oldName);
        input.setSelection(oldName.length());
        input.setSingleLine(true);
        input.setHint("New shelf name");
        input.setTextColor(TEXT_PRIMARY);
        input.setHintTextColor(0xFF64748B);
        box.addView(input);

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Rename Shelf \"" + oldName + "\"")
                .setView(box)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty() || newName.equals(oldName)) return;
                        SharedPreferences sp = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);
                        SharedPreferences.Editor ed = sp.edit();
                        java.util.Map<String, ?> all = sp.getAll();
                        int updated = 0;
                        if (all != null) {
                            for (java.util.Map.Entry<String, ?> e : all.entrySet()) {
                                Object v = e.getValue();
                                if (v instanceof String && oldName.equals(((String) v).trim())) {
                                    ed.putString(e.getKey(), newName);
                                    updated++;
                                }
                            }
                        }
                        ed.apply();
                        Toast.makeText(ctx,
                                "Renamed \"" + oldName + "\" to \"" + newName + "\" (" + updated + (updated == 1 ? " book)" : " books)"),
                                Toast.LENGTH_SHORT).show();
                        if (parentDlg != null) parentDlg.dismiss();
                        showManageShelvesDialog(ctx);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void showRemoveShelfDialog(final Context ctx, final String shelfName, final int count, final AlertDialog parentDlg) {
        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Remove Shelf")
                .setMessage("Remove shelf \"" + shelfName + "\" from " + count + (count == 1 ? " book" : " books")
                        + "? Your books and metadata will stay untouched.")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sp = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);
                        SharedPreferences.Editor ed = sp.edit();
                        java.util.Map<String, ?> all = sp.getAll();
                        if (all != null) {
                            for (java.util.Map.Entry<String, ?> e : all.entrySet()) {
                                Object v = e.getValue();
                                if (v instanceof String && shelfName.equals(((String) v).trim())) {
                                    ed.remove(e.getKey());
                                }
                            }
                        }
                        ed.apply();
                        Toast.makeText(ctx, "Removed shelf \"" + shelfName + "\"", Toast.LENGTH_SHORT).show();
                        if (parentDlg != null) parentDlg.dismiss();
                        showManageShelvesDialog(ctx);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void showLibraryInsightsDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        final java.util.Map<String, Integer> authorFreq = new java.util.HashMap<>();
        final java.util.Map<String, String> authorLabel = new java.util.HashMap<>();
        final java.util.Map<String, Integer> genreFreq = new java.util.HashMap<>();
        final java.util.Map<String, String> genreLabel = new java.util.HashMap<>();
        final java.util.Map<String, Integer> shelfFreq = new java.util.HashMap<>();
        final java.util.Map<String, Integer> pubFreq = new java.util.HashMap<>();
        final java.util.Map<String, String> pubLabel = new java.util.HashMap<>();
        int favs = 0, noted = 0, shelved = 0, ratedCount = 0, ratingSum = 0;
        int missingCover = 0, missingGenre = 0, missingDesc = 0;

        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            String aDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
            if (!aDisp.isEmpty() && !"Unknown Author".equalsIgnoreCase(aDisp)) {
                String ak = aDisp.toLowerCase(java.util.Locale.ROOT);
                authorFreq.put(ak, (authorFreq.containsKey(ak) ? authorFreq.get(ak) : 0) + 1);
                if (!authorLabel.containsKey(ak)) authorLabel.put(ak, aDisp);
            }
            boolean hasCov = false;
            if (r.kfxPath != null && !r.kfxPath.isEmpty()) {
                File covF = new File(r.kfxPath + ".cover.jpg");
                hasCov = covF.exists() && covF.length() >= 64;
            }
            if (!hasCov) missingCover++;
            if (r.description == null || r.description.trim().isEmpty()) missingDesc++;
            boolean hasCleanG = false;
            if (r.genres != null && !r.genres.isEmpty()) {
                for (String gCheck : r.genres.split(",")) {
                    if (!gCheck.trim().isEmpty()) { hasCleanG = true; break; }
                }
            }
            if (!hasCleanG) missingGenre++;
            if (r.genres != null && !r.genres.isEmpty()) {
                java.util.Set<String> seenG = new java.util.HashSet<>();
                for (String g : r.genres.split(",")) {
                    String c = g.trim();
                    if (c.length() >= 3 && c.length() <= 22 && !c.contains("--")
                            && !com.amazon.kindle.epub.ingest.EpubMetadataExtractor.isNoiseTag(c)) {
                        String gk = c.toLowerCase(java.util.Locale.ROOT);
                        if (gk.startsWith("early works") || gk.startsWith("translations into")
                                || gk.contains("textbooks for") || gk.equals("general")) continue;
                        if (seenG.add(gk)) {
                            genreFreq.put(gk, (genreFreq.containsKey(gk) ? genreFreq.get(gk) : 0) + 1);
                            if (!genreLabel.containsKey(gk)) {
                                genreLabel.put(gk, Character.toUpperCase(c.charAt(0)) + c.substring(1));
                            }
                        }
                    }
                }
            }
            if (com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath)) favs++;
            if (!com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath).isEmpty()) noted++;
            String shName = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
            if (!shName.isEmpty()) {
                shelved++;
                shelfFreq.put(shName, (shelfFreq.containsKey(shName) ? shelfFreq.get(shName) : 0) + 1);
            }
            if (r.publisher != null && !r.publisher.trim().isEmpty()) {
                String pClean = r.publisher.trim();
                String pk = pClean.toLowerCase(java.util.Locale.ROOT);
                pubFreq.put(pk, (pubFreq.containsKey(pk) ? pubFreq.get(pk) : 0) + 1);
                if (!pubLabel.containsKey(pk)) pubLabel.put(pk, pClean);
            }
            int st = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
            if (st > 0) { ratedCount++; ratingSum += st; }
        }

        java.util.List<String> topAuthors = new java.util.ArrayList<>(authorFreq.keySet());
        java.util.Collections.sort(topAuthors, (a, b) -> {
            int cmp = Integer.compare(authorFreq.get(b), authorFreq.get(a));
            return cmp != 0 ? cmp : a.compareTo(b);
        });
        java.util.List<String> topGenres = new java.util.ArrayList<>(genreFreq.keySet());
        java.util.Collections.sort(topGenres, (a, b) -> {
            int cmp = Integer.compare(genreFreq.get(b), genreFreq.get(a));
            return cmp != 0 ? cmp : a.compareTo(b);
        });

        int[] qs = readQuickStats(ctx);
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF11131A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("📊 Library Insights");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        String avgStr = ratedCount > 0
                ? String.format(java.util.Locale.US, "%.1f★ avg (%d rated)", ratingSum / (float) ratedCount, ratedCount)
                : "No ratings yet";
        TextView summaryTv = new TextView(ctx);
        summaryTv.setText(all.size() + " cataloged · " + qs[1] + " reading · " + qs[2] + " finished\n"
                + favs + " favorited · " + noted + " annotated · " + shelved + " shelved · " + avgStr);
        summaryTv.setTextColor(TEXT_MUTED);
        summaryTv.setTextSize(12);
        summaryTv.setPadding(0, 0, 0, dp(d, 14));
        root.addView(summaryTv);

        final AlertDialog[] insightDlgRef = new AlertDialog[1];

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 360)));
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, dp(d, 4), 0);

        // Section 0: Interactive Reading Status Stacked Bar (Finished / Reading / Unread)
        int finCnt = qs[2];
        int readCnt = qs[1];
        int unrdCnt = Math.max(0, all.size() - finCnt - readCnt);
        int totalStat = Math.max(1, finCnt + readCnt + unrdCnt);

        TextView stHdr = new TextView(ctx);
        stHdr.setText("READING STATUS (TAP TO FILTER)");
        stHdr.setTextColor(0xFF34D399);
        stHdr.setTextSize(11);
        stHdr.setTypeface(Typeface.DEFAULT_BOLD);
        stHdr.setPadding(0, 0, 0, dp(d, 6));
        content.addView(stHdr);

        LinearLayout stackedBar = new LinearLayout(ctx);
        stackedBar.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 10));
        sbLp.bottomMargin = dp(d, 8);
        stackedBar.setLayoutParams(sbLp);

        if (finCnt > 0) {
            View segF = new View(ctx);
            GradientDrawable sfBg = new GradientDrawable();
            sfBg.setColor(0xFF34D399);
            sfBg.setCornerRadius(dp(d, 4));
            segF.setBackground(sfBg);
            LinearLayout.LayoutParams lpF = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(0.06f, finCnt / (float) totalStat));
            lpF.rightMargin = dp(d, 3);
            segF.setLayoutParams(lpF);
            segF.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Finished");
                }
            });
            stackedBar.addView(segF);
        }
        if (readCnt > 0) {
            View segR = new View(ctx);
            GradientDrawable srBg = new GradientDrawable();
            srBg.setColor(0xFF38BDF8);
            srBg.setCornerRadius(dp(d, 4));
            segR.setBackground(srBg);
            LinearLayout.LayoutParams lpR = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(0.06f, readCnt / (float) totalStat));
            lpR.rightMargin = dp(d, 3);
            segR.setLayoutParams(lpR);
            segR.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Reading");
                }
            });
            stackedBar.addView(segR);
        }
        if (unrdCnt > 0) {
            View segU = new View(ctx);
            GradientDrawable suBg = new GradientDrawable();
            suBg.setColor(0xFF475569);
            suBg.setCornerRadius(dp(d, 4));
            segU.setBackground(suBg);
            segU.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(0.06f, unrdCnt / (float) totalStat)));
            segU.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Unread");
                }
            });
            stackedBar.addView(segU);
        }
        content.addView(stackedBar);

        LinearLayout legendRow = new LinearLayout(ctx);
        legendRow.setOrientation(LinearLayout.HORIZONTAL);
        legendRow.setPadding(0, 0, 0, dp(d, 14));
        String[][] legItems = new String[][] {
            { "● Finished (" + finCnt + ")", "Finished", "#34D399" },
            { "● Reading (" + readCnt + ")", "Reading", "#38BDF8" },
            { "● Unread (" + unrdCnt + ")", "Unread", "#94A3B8" }
        };
        for (final String[] item : legItems) {
            TextView lt = new TextView(ctx);
            lt.setText(item[0]);
            lt.setTextColor(android.graphics.Color.parseColor(item[2]));
            lt.setTextSize(11);
            lt.setTypeface(Typeface.DEFAULT_BOLD);
            lt.setPadding(0, 0, dp(d, 12), 0);
            lt.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                    com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, item[1]);
                }
            });
            legendRow.addView(lt);
        }
        content.addView(legendRow);

        // Section 0b: Metadata Health & Quick-Fix Audit (1-tap drill-down into incomplete books)
        int totalFields = Math.max(1, all.size() * 3);
        int completeFields = Math.max(0, totalFields - missingCover - missingGenre - missingDesc);
        int healthPct = Math.round(100f * completeFields / totalFields);

        TextView hHdr = new TextView(ctx);
        hHdr.setText("METADATA HEALTH — " + healthPct + "% COMPLETE"
                + ((missingCover + missingGenre + missingDesc > 0) ? " (TAP TO FIX)" : ""));
        hHdr.setTextColor(healthPct >= 85 ? 0xFF34D399 : 0xFFFBBF24);
        hHdr.setTextSize(11);
        hHdr.setTypeface(Typeface.DEFAULT_BOLD);
        hHdr.setPadding(0, 0, 0, dp(d, 8));
        content.addView(hHdr);

        LinearLayout healthRow = new LinearLayout(ctx);
        healthRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams hrLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hrLp.bottomMargin = dp(d, 14);
        healthRow.setLayoutParams(hrLp);

        final int fMissCov = missingCover;
        final int fMissGen = missingGenre;
        final int fMissDesc = missingDesc;

        healthRow.addView(makeHealthBadge(ctx, d,
                fMissCov == 0 ? "✓ Covers" : "No Cover",
                fMissCov == 0 ? "100%" : (fMissCov + " ›"),
                fMissCov == 0 ? 0xFF34D399 : 0xFFF87171,
                fMissCov == 0 ? 0xFF122B24 : 0xFF2B181E,
                true,
                fMissCov > 0 ? new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "MISSING_COVER");
                    }
                } : null));

        healthRow.addView(makeHealthBadge(ctx, d,
                fMissGen == 0 ? "✓ Genres" : "No Genre",
                fMissGen == 0 ? "100%" : (fMissGen + " ›"),
                fMissGen == 0 ? 0xFF34D399 : 0xFF38BDF8,
                fMissGen == 0 ? 0xFF122B24 : 0xFF152638,
                true,
                fMissGen > 0 ? new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "MISSING_GENRES");
                    }
                } : null));

        healthRow.addView(makeHealthBadge(ctx, d,
                fMissDesc == 0 ? "✓ Synopsis" : "No Synopsis",
                fMissDesc == 0 ? "100%" : (fMissDesc + " ›"),
                fMissDesc == 0 ? 0xFF34D399 : 0xFFFBBF24,
                fMissDesc == 0 ? 0xFF122B24 : 0xFF2E2414,
                false,
                fMissDesc > 0 ? new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "MISSING_SYNOPSIS");
                    }
                } : null));
        content.addView(healthRow);

        // Section 1: Top Genres Bar Chart (tap any bar to filter catalog)
        TextView gHdr = new TextView(ctx);
        gHdr.setText("TOP GENRES (TAP TO FILTER)");
        gHdr.setTextColor(0xFF60A5FA);
        gHdr.setTextSize(11);
        gHdr.setTypeface(Typeface.DEFAULT_BOLD);
        gHdr.setPadding(0, 0, 0, dp(d, 8));
        content.addView(gHdr);

        int maxG = topGenres.isEmpty() ? 1 : Math.max(1, genreFreq.get(topGenres.get(0)));
        for (int i = 0; i < Math.min(5, topGenres.size()); i++) {
            String k = topGenres.get(i);
            final String gName = genreLabel.get(k);
            content.addView(makeInsightBarRow(ctx, d, gName, genreFreq.get(k), maxG, 0xFF60A5FA,
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, gName);
                        }
                    }));
        }

        // Section 2: Top Authors Bar Chart (tap any bar to filter catalog)
        TextView aHdr = new TextView(ctx);
        aHdr.setText("TOP AUTHORS (TAP TO FILTER)");
        aHdr.setTextColor(0xFFF59E0B);
        aHdr.setTextSize(11);
        aHdr.setTypeface(Typeface.DEFAULT_BOLD);
        aHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
        content.addView(aHdr);

        int maxA = topAuthors.isEmpty() ? 1 : Math.max(1, authorFreq.get(topAuthors.get(0)));
        for (int i = 0; i < Math.min(5, topAuthors.size()); i++) {
            String k = topAuthors.get(i);
            final String aName = authorLabel.get(k);
            content.addView(makeInsightBarRow(ctx, d, aName, authorFreq.get(k), maxA, 0xFFF59E0B,
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, aName);
                        }
                    }));
        }

        // Section 3: Custom Shelves Bar Chart (when shelves exist)
        if (!shelfFreq.isEmpty()) {
            java.util.List<String> topShelves = new java.util.ArrayList<>(shelfFreq.keySet());
            java.util.Collections.sort(topShelves, (a, b) -> {
                int cmp = Integer.compare(shelfFreq.get(b), shelfFreq.get(a));
                return cmp != 0 ? cmp : a.compareTo(b);
            });
            TextView sHdr = new TextView(ctx);
            sHdr.setText("CUSTOM SHELVES (TAP TO FILTER)");
            sHdr.setTextColor(0xFFA5B4FC);
            sHdr.setTextSize(11);
            sHdr.setTypeface(Typeface.DEFAULT_BOLD);
            sHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(sHdr);

            int maxS = Math.max(1, shelfFreq.get(topShelves.get(0)));
            for (final String sh : topShelves) {
                content.addView(makeInsightBarRow(ctx, d, "🏷 " + sh, shelfFreq.get(sh), maxS, 0xFF818CF8,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "SHELF:" + sh);
                            }
                        }));
            }
        }

        // Section 4: Top Publishers Bar Chart
        if (!pubFreq.isEmpty()) {
            java.util.List<String> topPubs = new java.util.ArrayList<>(pubFreq.keySet());
            java.util.Collections.sort(topPubs, (a, b) -> {
                int cmp = Integer.compare(pubFreq.get(b), pubFreq.get(a));
                return cmp != 0 ? cmp : a.compareTo(b);
            });
            TextView pHdr = new TextView(ctx);
            pHdr.setText("TOP PUBLISHERS (TAP TO FILTER)");
            pHdr.setTextColor(0xFF5EEAD4);
            pHdr.setTextSize(11);
            pHdr.setTypeface(Typeface.DEFAULT_BOLD);
            pHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(pHdr);

            int maxP = Math.max(1, pubFreq.get(topPubs.get(0)));
            for (int i = 0; i < Math.min(4, topPubs.size()); i++) {
                String k = topPubs.get(i);
                final String pName = pubLabel.get(k);
                content.addView(makeInsightBarRow(ctx, d, pName, pubFreq.get(k), maxP, 0xFF5EEAD4,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, pName);
                            }
                        }));
            }
        }

        sv.addView(content);
        root.addView(sv);

        insightDlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .show();
    }

    private static View makeHealthBadge(
            Context ctx, float d, String label, String value, int accentColor, int bgColor,
            boolean rightGap, View.OnClickListener listener) {
        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(d, 6), dp(d, 8), dp(d, 6), dp(d, 8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(d, 8));
        bg.setStroke(dp(d, 1), accentColor & 0x66FFFFFF | 0x44000000);
        tile.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (rightGap) lp.rightMargin = dp(d, 6);
        tile.setLayoutParams(lp);

        TextView valTv = new TextView(ctx);
        valTv.setText(value);
        valTv.setTextColor(accentColor);
        valTv.setTextSize(13.5f);
        valTv.setTypeface(Typeface.DEFAULT_BOLD);
        valTv.setGravity(Gravity.CENTER);
        tile.addView(valTv);

        TextView lblTv = new TextView(ctx);
        lblTv.setText(label);
        lblTv.setTextColor(0xFFCBD5E1);
        lblTv.setTextSize(10.5f);
        lblTv.setSingleLine(true);
        lblTv.setGravity(Gravity.CENTER);
        tile.addView(lblTv);

        if (listener != null) tile.setOnClickListener(listener);
        return tile;
    }

    private static View makeInsightBarRow(Context ctx, float d, String label, int count, int maxCount,
                                          int barColor, View.OnClickListener onClick) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, dp(d, 9));

        LinearLayout top = new LinearLayout(ctx);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView lbl = new TextView(ctx);
        lbl.setText(label);
        lbl.setTextColor(TEXT_PRIMARY);
        lbl.setTextSize(13);
        lbl.setSingleLine(true);
        lbl.setEllipsize(android.text.TextUtils.TruncateAt.END);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(lbl);

        TextView cnt = new TextView(ctx);
        cnt.setText(String.valueOf(count));
        cnt.setTextColor(barColor);
        cnt.setTextSize(12);
        cnt.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(cnt);
        box.addView(top);

        LinearLayout track = new LinearLayout(ctx);
        track.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable trBg = new GradientDrawable();
        trBg.setColor(0xFF1E2433);
        trBg.setCornerRadius(dp(d, 4));
        track.setBackground(trBg);
        LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 6));
        trLp.topMargin = dp(d, 4);
        track.setLayoutParams(trLp);

        float ratio = Math.max(0.06f, Math.min(1f, count / (float) Math.max(1, maxCount)));
        View fill = new View(ctx);
        GradientDrawable fBg = new GradientDrawable();
        fBg.setColor(barColor);
        fBg.setCornerRadius(dp(d, 4));
        fill.setBackground(fBg);
        fill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, ratio));
        track.addView(fill);

        View rest = new View(ctx);
        rest.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(0f, 1f - ratio)));
        track.addView(rest);

        box.addView(track);
        if (onClick != null) box.setOnClickListener(onClick);
        return box;
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
        sb.append("Version: Folio v9.0 KFX (Enhanced Typesetting)\n");
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
