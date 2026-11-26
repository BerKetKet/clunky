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

    private static final int BG_DARK = 0xFF0E0F11;
    private static final int BG_CARD = 0xFF1C1E22;
    private static final int BG_CARD_HOVER = 0xFF25282D;
    private static final int BORDER_HAIRLINE = 0xFF32353B;
    private static final int ACCENT_AMBER = 0xFFD4C9B8;
    private static final int ACCENT_TEAL = 0xFFD4C9B8;
    private static final int TEXT_PRIMARY = 0xFFECE8E0;
    private static final int TEXT_MUTED = 0xFF8A857D;

    public static View wrap(View originalView) {
        try {
            if (originalView == null) return null;
            final Context ctx = originalView.getContext();
            final FrameLayout host = new FrameLayout(ctx);
            host.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            host.addView(build(ctx), new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            final Runnable rebuilder = new Runnable() {
                @Override public void run() {
                    host.post(new Runnable() {
                        @Override public void run() {
                            try {
                                host.removeAllViews();
                                host.addView(build(ctx), new FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            } catch (Throwable ignored) {}
                        }
                    });
                }
            };
            host.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override public void onViewAttachedToWindow(View v) { FolioTheme.setActiveBottomTab(ctx, 2); }
                @Override public void onViewDetachedFromWindow(View v) {}
            });
            host.setTag(rebuilder); // keep strong ref tied to host lifecycle
            FolioTheme.registerThemeListener(rebuilder);
            return host;
        } catch (Throwable t) {
            android.util.Log.e("FolioMoreView", "Failed to build Azath Control Center", t);
            return originalView;
        }
    }

    public static View build(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;

        final int dynBg = FolioTheme.bgCanvas(ctx);
        final int dynCard = FolioTheme.bgSurface(ctx);
        final int dynBorder = FolioTheme.border(ctx);
        final int dynText = FolioTheme.textPrimary(ctx);
        final int dynMuted = FolioTheme.textMuted(ctx);
        final int dynAccent = FolioTheme.accent(ctx);
        final int dynAccentFill = FolioTheme.accentFill(ctx);
        final int dynAccentBorder = FolioTheme.accentBorder(ctx);
        final int dynAccentSoft = FolioTheme.accentSoftText(ctx);

        FolioTheme.setActiveBottomTab(ctx, 2);
        FolioTheme.applyWindowBars(ctx);
        ScrollView scroll = new ScrollView(ctx);
        scroll.setBackgroundColor(dynBg);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(d, 96));

        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(d, 16), dp(d, 14), dp(d, 16), dp(d, 24));
        scroll.addView(container, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // 1. Header Row: Serif Title + Monochrome Vector Actions + Subtle Version Tag
        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(d, 4));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Control Center");
        titleTv.setTextSize(22);
        titleTv.setTextColor(dynText);
        titleTv.setTypeface(FolioTheme.serif(ctx));
        titleTv.setLetterSpacing(-0.02f);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        headerRow.addView(titleTv, tlp);

        FrameLayout diceBtn = FolioIcons.makeIconButton(
                ctx, FolioIcons.SHUFFLE, 19, 7, dynText, dynCard, dynBorder, 12);
        LinearLayout.LayoutParams diceLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        diceLp.rightMargin = dp(d, 7);
        diceBtn.setLayoutParams(diceLp);
        headerRow.addView(diceBtn);

        FrameLayout quickSearchBtn = FolioIcons.makeIconButton(
                ctx, FolioIcons.SEARCH, 19, 7, dynText, dynCard, dynBorder, 12);
        LinearLayout.LayoutParams qsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qsLp.rightMargin = dp(d, 7);
        quickSearchBtn.setLayoutParams(qsLp);
        quickSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showGlobalSearchDialog(ctx); }
        });
        headerRow.addView(quickSearchBtn);

        TextView badgeTv = new TextView(ctx);
        badgeTv.setText("v18.7 KFX");
        badgeTv.setTextSize(11);
        badgeTv.setTextColor(dynAccentSoft);
        badgeTv.setTypeface(FolioTheme.sansBold(ctx));
        badgeTv.setPadding(dp(d, 11), dp(d, 5), dp(d, 11), dp(d, 5));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(dynAccentFill);
        badgeBg.setCornerRadius(dp(d, 999));
        badgeBg.setStroke(dp(d, 1), dynAccentBorder);
        badgeTv.setBackground(badgeBg);
        badgeTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAboutDialog(ctx); }
        });
        headerRow.addView(badgeTv);
        container.addView(headerRow);

        LinearLayout subRow = new LinearLayout(ctx);
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(Gravity.CENTER_VERTICAL);
        subRow.setPadding(0, dp(d, 2), 0, dp(d, 16));

        TextView subTv = new TextView(ctx);
        subTv.setText("Offline EPUB & KFX Library");
        subTv.setTextSize(12.5f);
        subTv.setTextColor(dynMuted);
        subRow.addView(subTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        final TextView themeTogglePill = new TextView(ctx);
        boolean isLightNow = FolioTheme.isLimestoneLight(ctx);
        themeTogglePill.setText(isLightNow ? "Limestone" : "Granite");
        themeTogglePill.setTextSize(11);
        themeTogglePill.setTextColor(dynAccentSoft);
        themeTogglePill.setTypeface(FolioTheme.sansBold(ctx));
        themeTogglePill.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable ttBg = new GradientDrawable();
        ttBg.setColor(dynAccentFill);
        ttBg.setCornerRadius(dp(d, 999));
        ttBg.setStroke(dp(d, 1), dynAccentBorder);
        themeTogglePill.setBackground(ttBg);
        themeTogglePill.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean nextLight = !FolioTheme.isLimestoneLight(ctx);
                FolioTheme.setThemeMode(ctx, nextLight ? "limestone" : "granite");
                themeTogglePill.setText(nextLight ? "Limestone" : "Granite");
                FolioTheme.notifyThemeChanged();
            }
        });
        subRow.addView(themeTogglePill);
        container.addView(subRow);

        // 1b. Monochrome 4-Tile Reading Stats Strip + Annual Reading Goal Card
        final TextView[] statNums = new TextView[4];
        final TextView[] goalStatusRef = new TextView[1];
        final View[] goalFillRef = new View[1];
        final View[] goalTrackRef = new View[1];
        final LinearLayout[] continueHostRef = new LinearLayout[1];
        final LinearLayout[] upNextHostRef = new LinearLayout[1];
        final Runnable refreshStats = new Runnable() {
            @Override public void run() {
                final Runnable selfRefresh = this;
                loadQuickStatsAsync(ctx, new StatsCallback() {
                    @Override public void onStats(int[] s) {
                        applyQuickStatsUi(ctx, s, statNums, goalStatusRef[0], goalFillRef[0], goalTrackRef[0]);
                    }
                });
                if (continueHostRef[0] != null) {
                    populateContinueReadingStrip(ctx, d, continueHostRef[0], selfRefresh);
                }
                if (upNextHostRef[0] != null) {
                    populateUpNextStrip(ctx, d, upNextHostRef[0], selfRefresh);
                }
            }
        };

        diceBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTonightsPickDialog(ctx, refreshStats); }
        });

        com.amazon.kindle.epub.ingest.AzathLibrarySync.reconcileOnLaunchAsync(ctx);
        int[] stats = new int[] { 0, 0, 0, 0 };
        LinearLayout statsRow = new LinearLayout(ctx);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srLp.bottomMargin = dp(d, 14);
        statsRow.setLayoutParams(srLp);

        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[0]), "BOOKS", FolioTheme.textPrimary(ctx), true,
                statNums, 0,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[1]), "READING", FolioTheme.textPrimary(ctx), true,
                statNums, 1,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Reading", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[2]), "FINISHED", FolioTheme.textPrimary(ctx), true,
                statNums, 2,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Finished", refreshStats);
                    }
                }));
        statsRow.addView(makeStatTile(ctx, d, String.valueOf(stats[3]), "FAVORITES", FolioTheme.textPrimary(ctx), false,
                statNums, 3,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Favorites", refreshStats);
                    }
                }));
        container.addView(statsRow);

        // 1c. Annual Reading Goal Card (Kindle-native hairline surface + single blue progress bar)
        LinearLayout goalCard = new LinearLayout(ctx);
        goalCard.setOrientation(LinearLayout.VERTICAL);
        goalCard.setPadding(dp(d, 14), dp(d, 12), dp(d, 14), dp(d, 12));
        GradientDrawable gcBg = new GradientDrawable();
        gcBg.setColor(FolioTheme.bgSurface(ctx));
        gcBg.setCornerRadius(dp(d, 10));
        gcBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        goalCard.setBackground(gcBg);
        LinearLayout.LayoutParams gcLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gcLp.bottomMargin = dp(d, 14);
        goalCard.setLayoutParams(gcLp);

        LinearLayout goalTop = new LinearLayout(ctx);
        goalTop.setOrientation(LinearLayout.HORIZONTAL);
        goalTop.setGravity(Gravity.CENTER_VERTICAL);

        View goalIco = FolioIcons.makeIcon(ctx, FolioIcons.TARGET, 16, FolioIcons.COLOR_ICON);
        LinearLayout.LayoutParams giLp = new LinearLayout.LayoutParams(dp(d, 16), dp(d, 16));
        giLp.rightMargin = dp(d, 8);
        goalTop.addView(goalIco, giLp);

        int yrNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        TextView goalTitleTv = new TextView(ctx);
        goalTitleTv.setText(yrNow + " Reading Goal");
        goalTitleTv.setTextColor(FolioTheme.textPrimary(ctx));
        goalTitleTv.setTextSize(13.5f);
        goalTitleTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        goalTitleTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        goalTop.addView(goalTitleTv);

        TextView goalEditTv = new TextView(ctx);
        goalEditTv.setText("Edit");
        goalEditTv.setTextColor(FolioTheme.accent(ctx));
        goalEditTv.setTextSize(12);
        goalEditTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        goalTop.addView(goalEditTv);
        goalCard.addView(goalTop);

        TextView goalSubTv = new TextView(ctx);
        goalSubTv.setTextColor(FolioTheme.textMuted(ctx));
        goalSubTv.setTextSize(12);
        goalSubTv.setPadding(0, dp(d, 4), 0, dp(d, 8));
        goalStatusRef[0] = goalSubTv;
        goalCard.addView(goalSubTv);

        LinearLayout barTrack = new LinearLayout(ctx);
        barTrack.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable trBg = new GradientDrawable();
        trBg.setColor(FolioTheme.border(ctx));
        trBg.setCornerRadius(dp(d, 3));
        barTrack.setBackground(trBg);
        barTrack.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 4)));

        View barFill = new View(ctx);
        GradientDrawable flBg = new GradientDrawable();
        flBg.setColor(FolioTheme.accent(ctx));
        flBg.setCornerRadius(dp(d, 3));
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
        loadQuickStatsAsync(ctx, new StatsCallback() {
            @Override public void onStats(int[] s) {
                applyQuickStatsUi(ctx, s, statNums, goalStatusRef[0], goalFillRef[0], goalTrackRef[0]);
            }
        });
        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showReadingGoalDialog(ctx, refreshStats);
            }
        });
        container.addView(goalCard);

        // 1d. Continue Reading Quick-Resume Carousel
        LinearLayout continueHost = new LinearLayout(ctx);
        continueHost.setOrientation(LinearLayout.VERTICAL);
        continueHostRef[0] = continueHost;
        populateContinueReadingStrip(ctx, d, continueHost, refreshStats);
        container.addView(continueHost);

        // 1e. Up Next Reading Queue Carousel
        LinearLayout upNextHost = new LinearLayout(ctx);
        upNextHost.setOrientation(LinearLayout.VERTICAL);
        upNextHostRef[0] = upNextHost;
        populateUpNextStrip(ctx, d, upNextHost, refreshStats);
        container.addView(upNextHost);

        // 2. Grouped Kindle-Style Hairline Tool Lists (Zero Emojis)
        container.addView(makeSectionLabel(ctx, d, "LIBRARY & IMPORT"));
        LinearLayout libGroup = makeGroupCard(ctx, d);
        libGroup.addView(makeKindleRow(ctx, d, FolioIcons.LIBRARY, "Library Metadata & Genres",
                "Browse authors, dates, ISBNs & genres", false, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All", refreshStats);
                    }
                }));
        libGroup.addView(makeKindleRow(ctx, d, FolioIcons.IMPORT, "Import EPUBs from Storage",
                "Batch-select EPUB folders into app library", true, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioFolderBrowserDialog.show(ctx);
                    }
                }));
        libGroup.addView(makeKindleRow(ctx, d, FolioIcons.GLOBE, "Web Book Browser",
                "Browse " + com.amazon.kindle.epub.ingest.FolioWebBrowserDialog.getHomeHostLabel(ctx)
                        + " & auto-import EPUBs", true, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioWebBrowserDialog.show(ctx, refreshStats);
                    }
                }));
        libGroup.addView(makeKindleRow(ctx, d, FolioIcons.FEED, "OPDS Catalogs",
                "Gutenberg, Standard Ebooks & custom feeds", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showOpdsDialog(ctx); }
                }));
        libGroup.addView(makeKindleRow(ctx, d, FolioIcons.REFRESH, "Scan Local Library",
                "Rescan /sdcard/epub/ & update KFX catalog", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { scanLocalLibrary(ctx); }
                }));
        container.addView(libGroup);

        container.addView(makeSectionLabel(ctx, d, "READING & CURATION"));
        LinearLayout curGroup = makeGroupCard(ctx, d);
        curGroup.addView(makeKindleRow(ctx, d, FolioIcons.NOTES, "Reading Notes Notebook",
                "Search, edit, share & export personal notes", false, new View.OnClickListener() {
                    @Override public void onClick(View v) { showNotesNotebookDialog(ctx); }
                }));
        curGroup.addView(makeKindleRow(ctx, d, FolioIcons.SHELVES, "Manage Shelves",
                "Create, rename, or browse custom shelves", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showManageShelvesDialog(ctx); }
                }));
        curGroup.addView(makeKindleRow(ctx, d, FolioIcons.INSIGHTS, "Library Insights",
                "Authors, genres, ratings & publication eras", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showLibraryInsightsDialog(ctx); }
                }));
        curGroup.addView(makeKindleRow(ctx, d, FolioIcons.LOG, "Reading Log & Export",
                "Export reading log to Markdown, CSV or HTML", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showReadingLogDialog(ctx); }
                }));
        container.addView(curGroup);

        container.addView(makeSectionLabel(ctx, d, "SYSTEM & SETTINGS"));
        LinearLayout sysGroup = makeGroupCard(ctx, d);
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.BACKUP, "Backup & Restore",
                "Back up metadata, notes & reading progress", false, new View.OnClickListener() {
                    @Override public void onClick(View v) { OpenCatalogView.showBackupRestoreDialog(ctx); }
                }));
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.STORAGE, "Storage & Duplicates",
                "Largest KFX files, saved backups & cache", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showStorageDuplicatesDialog(ctx); }
                }));
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.REFRESH, "Force Reconcile",
                "Preview & prune missing DB rows and orphan cache", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showForceReconcileDialog(ctx, refreshStats); }
                }));
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.LINK, "Web Browser URL",
                "Homepage & Simplified View ("
                        + com.amazon.kindle.epub.ingest.FolioWebBrowserDialog.getHomeHostLabel(ctx) + ")",
                true, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        com.amazon.kindle.epub.ingest.FolioWebBrowserDialog.showUrlSettingsDialog(ctx, refreshStats);
                    }
                }));
        final boolean enrichOn = com.amazon.kindle.epub.ingest.MetadataEnricher.isOnlineEnrichmentEnabled(ctx);
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.SETTINGS, "Online Metadata Enrichment",
                enrichOn ? "ON — Fetch genres & dates from Open Library" : "OFF — Strictly offline metadata only",
                true, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        boolean next = !com.amazon.kindle.epub.ingest.MetadataEnricher.isOnlineEnrichmentEnabled(ctx);
                        com.amazon.kindle.epub.ingest.MetadataEnricher.setOnlineEnrichmentEnabled(ctx, next);
                        refreshStats.run();
                    }
                }));
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.SETTINGS, "Reader & App Settings",
                "Reading preferences, dark mode & dictionary", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { openSettings(ctx); }
                }));
        sysGroup.addView(makeKindleRow(ctx, d, FolioIcons.INFO, "About Azath",
                "Engine capabilities, version & library stats", true, new View.OnClickListener() {
                    @Override public void onClick(View v) { showAboutDialog(ctx); }
                }));
        container.addView(sysGroup);

        FolioTheme.applyToTree(ctx, container);
        return scroll;
    }

    private interface StatsCallback {
        void onStats(int[] stats);
    }

    private static final java.util.concurrent.ExecutorService STATS_EXECUTOR =
            java.util.concurrent.Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "folio-more-stats");
                    t.setDaemon(true);
                    return t;
                }
            });

    private static void loadQuickStatsAsync(final Context ctx, final StatsCallback cb) {
        final Context appCtx = (ctx != null) ? ctx.getApplicationContext() : null;
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        STATS_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final int[] s = readQuickStats(appCtx != null ? appCtx : ctx);
                mainHandler.post(new Runnable() {
                    @Override public void run() {
                        if (FolioTheme.isContextDead(ctx)) return;
                        cb.onStats(s);
                    }
                });
            }
        });
    }

    private static void applyQuickStatsUi(Context ctx, int[] s, TextView[] statNums,
                                          TextView goalStatus, View goalFill, View goalTrack) {
        if (s == null || s.length < 4) return;
        if (statNums != null) {
            for (int i = 0; i < 4 && i < statNums.length; i++) {
                if (statNums[i] != null) statNums[i].setText(String.valueOf(s[i]));
            }
        }
        updateGoalCard(ctx, s[2], goalStatus, goalFill, goalTrack);
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
        bg.setColor(FolioTheme.bgSurface(ctx));
        bg.setCornerRadius(dp(d, 10));
        bg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        tile.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        if (addRightGap) lp.rightMargin = dp(d, 8);
        tile.setLayoutParams(lp);

        TextView numTv = new TextView(ctx);
        numTv.setText(count);
        numTv.setTextSize(19);
        numTv.setTextColor(accentColor);
        numTv.setTypeface(Typeface.SERIF, Typeface.BOLD);
        numTv.setGravity(Gravity.CENTER);
        if (numSlot != null && slotIdx >= 0 && slotIdx < numSlot.length) numSlot[slotIdx] = numTv;
        tile.addView(numTv);

        TextView lblTv = new TextView(ctx);
        lblTv.setText(label);
        lblTv.setTextSize(10);
        lblTv.setTextColor(FolioTheme.textMuted(ctx));
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

    private static int getReadingSpeedWpm(Context ctx) {
        try {
            int w = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE).getInt("reading_speed_wpm", 250);
            if (w >= 100 && w <= 800) return w;
        } catch (Throwable ignored) {}
        return 250;
    }

    private static void setReadingSpeedWpm(Context ctx, int wpm) {
        try {
            ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .edit().putInt("reading_speed_wpm", wpm).apply();
        } catch (Throwable ignored) {}
    }

    private static void showReadingGoalDialog(final Context ctx, final Runnable onSaved) {
        loadQuickStatsAsync(ctx, new StatsCallback() {
            @Override public void onStats(int[] qs) {
                showReadingGoalDialogLoaded(ctx, onSaved, qs);
            }
        });
    }

    private static void showReadingGoalDialogLoaded(final Context ctx, final Runnable onSaved, int[] qs) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        int cur = getReadingGoalTarget(ctx);
        int yrNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int finCount = (qs != null && qs.length >= 3) ? qs[2] : 0;
        int pct = Math.min(100, Math.round((finCount * 100f) / Math.max(1, cur)));

        // Gather completed books for Trophy Shelf
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> finishedBooks = new java.util.ArrayList<>();
        final java.util.Map<String, Integer> progMap =
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.loadProgressMap(ctx);
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : mdb.getAllBooks()) {
                if (r.kfxPath == null) continue;
                Integer p = progMap.get(r.kfxPath);
                if (p != null && p >= 100) finishedBooks.add(r);
            }
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }
        java.util.Collections.sort(finishedBooks, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                String da = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getFinishedDate(ctx, a.kfxPath);
                String db = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getFinishedDate(ctx, b.kfxPath);
                int cmp = db.compareTo(da);
                if (cmp != 0) return cmp;
                return (a.title != null ? a.title : "").compareToIgnoreCase(b.title != null ? b.title : "");
            }
        });

        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(FolioTheme.bgModal(ctx));
        wrap.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        java.util.Calendar calNow = java.util.Calendar.getInstance();
        int dayOfYr = calNow.get(java.util.Calendar.DAY_OF_YEAR);
        int daysInYr = calNow.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
        final int daysLeft = Math.max(0, daysInYr - dayOfYr);
        final int booksLeft = Math.max(0, cur - finCount);
        final String paceVal;
        if (booksLeft == 0) {
            paceVal = "Met!";
        } else if (daysLeft == 0) {
            paceVal = booksLeft + " today";
        } else if (daysLeft >= booksLeft) {
            int dPerBook = Math.max(1, Math.round((float) daysLeft / (float) booksLeft));
            paceVal = "1 / " + dPerBook + "d";
        } else {
            paceVal = String.format(java.util.Locale.US, "%.1f/wk", (booksLeft * 7.0) / Math.max(1, daysLeft));
        }

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(d, 3));

        TextView hdr = new TextView(ctx);
        hdr.setText("" + yrNow + " Reading Goal");
        hdr.setTextColor(FolioTheme.textPrimary(ctx));
        hdr.setTextSize(17);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(hdr);

        final int shareFin = finCount;
        final int shareCur = cur;
        final int sharePct = pct;
        final int shareYr = yrNow;
        TextView shareGoalBtn = new TextView(ctx);
        shareGoalBtn.setText("↗ Share");
        shareGoalBtn.setTextColor(FolioTheme.accent(ctx));
        shareGoalBtn.setTextSize(11.5f);
        shareGoalBtn.setTypeface(Typeface.DEFAULT_BOLD);
        shareGoalBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
        GradientDrawable sgBg = new GradientDrawable();
        sgBg.setColor(FolioTheme.accentFill(ctx));
        sgBg.setCornerRadius(dp(d, 9));
        sgBg.setStroke(dp(d, 1), FolioTheme.accentBorder(ctx));
        shareGoalBtn.setBackground(sgBg);
        shareGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append("").append(shareYr).append(" Reading Goal (Folio)\n");
                sb.append("Progress: ").append(shareFin).append(" / ").append(shareCur)
                        .append(" books finished (").append(sharePct).append("%)\n");
                sb.append("Pace: ").append(daysLeft).append(" days left · ")
                        .append(booksLeft == 0 ? "Goal reached!" : (booksLeft + " to go (" + paceVal + ")"))
                        .append("\n");
                if (!finishedBooks.isEmpty()) {
                    sb.append("\nCompleted:\n");
                    for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : finishedBooks) {
                        String t = (r.title != null && !r.title.isEmpty()) ? r.title : "Untitled";
                        String a = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                        String dt = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getFinishedDate(ctx, r.kfxPath);
                        sb.append("• ").append(t);
                        if (!a.isEmpty()) sb.append(" — ").append(a);
                        if (!dt.isEmpty()) sb.append(" (").append(dt).append(")");
                        sb.append("\n");
                    }
                }
                try {
                    android.content.Intent si = new android.content.Intent(android.content.Intent.ACTION_SEND);
                    si.setType("text/plain");
                    si.putExtra(android.content.Intent.EXTRA_SUBJECT, shareYr + " Reading Goal");
                    si.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString().trim());
                    ctx.startActivity(android.content.Intent.createChooser(si, "Share Reading Goal"));
                } catch (Throwable t) {
                    Toast.makeText(ctx, "Unable to open share sheet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        hdrRow.addView(shareGoalBtn);
        wrap.addView(hdrRow);

        TextView progSub = new TextView(ctx);
        progSub.setText(finCount + " of " + cur + " books finished (" + pct + "%)");
        progSub.setTextColor(FolioTheme.accent(ctx));
        progSub.setTextSize(12.5f);
        progSub.setTypeface(Typeface.DEFAULT_BOLD);
        progSub.setPadding(0, 0, 0, dp(d, 8));
        wrap.addView(progSub);

        LinearLayout pTrack = new LinearLayout(ctx);
        pTrack.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable ptBg = new GradientDrawable();
        ptBg.setColor(FolioTheme.bgControl(ctx));
        ptBg.setCornerRadius(dp(d, 4));
        pTrack.setBackground(ptBg);
        LinearLayout.LayoutParams ptLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 6));
        ptLp.bottomMargin = dp(d, 14);
        pTrack.setLayoutParams(ptLp);

        float frac = Math.max(0.03f, Math.min(1f, finCount / (float) Math.max(1, cur)));
        View pFill = new View(ctx);
        GradientDrawable pfBg = new GradientDrawable();
        pfBg.setColor(FolioTheme.accent(ctx));
        pfBg.setCornerRadius(dp(d, 4));
        pFill.setBackground(pfBg);
        pFill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, frac));
        pTrack.addView(pFill);
        View pRest = new View(ctx);
        pRest.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f - frac));
        pTrack.addView(pRest);
        wrap.addView(pTrack);

        // 3-Tile Reading Pace Telemetry Strip
        LinearLayout paceRow = new LinearLayout(ctx);
        paceRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams prLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prLp.bottomMargin = dp(d, 14);
        paceRow.setLayoutParams(prLp);

        String[] pVals = new String[] {
                String.valueOf(daysLeft),
                booksLeft == 0 ? "✓ 0" : String.valueOf(booksLeft),
                paceVal
        };
        String[] pLbls = new String[] { "DAYS LEFT", "TO GO", "NEEDED PACE" };
        int[] pCols = new int[] { FolioTheme.accent(ctx), booksLeft == 0 ? FolioTheme.accent(ctx) : FolioTheme.accent(ctx), FolioTheme.accent(ctx) };
        int[] pBgs = new int[] { FolioTheme.accentFill(ctx), booksLeft == 0 ? FolioTheme.accentFill(ctx) : FolioTheme.accentFill(ctx), FolioTheme.accentFill(ctx) };
        int[] pBrd = new int[] { FolioTheme.accentBorder(ctx), booksLeft == 0 ? FolioTheme.accentBorder(ctx) : FolioTheme.accentBorder(ctx), FolioTheme.accentBorder(ctx) };

        for (int ti = 0; ti < 3; ti++) {
            LinearLayout tile = new LinearLayout(ctx);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));
            GradientDrawable tBg = new GradientDrawable();
            tBg.setColor(pBgs[ti]);
            tBg.setCornerRadius(dp(d, 9));
            tBg.setStroke(dp(d, 1), pBrd[ti]);
            tile.setBackground(tBg);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (ti < 2) tLp.rightMargin = dp(d, 7);
            tile.setLayoutParams(tLp);

            TextView vTv = new TextView(ctx);
            vTv.setText(pVals[ti]);
            vTv.setTextColor(pCols[ti]);
            vTv.setTextSize(15);
            vTv.setTypeface(Typeface.DEFAULT_BOLD);
            vTv.setSingleLine(true);
            tile.addView(vTv);

            TextView lTv = new TextView(ctx);
            lTv.setText(pLbls[ti]);
            lTv.setTextColor(FolioTheme.textMuted(ctx));
            lTv.setTextSize(9.5f);
            lTv.setTypeface(Typeface.DEFAULT_BOLD);
            lTv.setPadding(0, dp(d, 2), 0, 0);
            tile.addView(lTv);

            paceRow.addView(tile);
        }
        wrap.addView(paceRow);

        final AlertDialog[] goalDlgRef = new AlertDialog[1];

        // Completed Books Trophy Shelf
        if (!finishedBooks.isEmpty()) {
            TextView trHdr = new TextView(ctx);
            trHdr.setText("COMPLETED BOOKS (" + finishedBooks.size() + ")");
            trHdr.setTextColor(FolioTheme.accent(ctx));
            trHdr.setTextSize(10.5f);
            trHdr.setTypeface(Typeface.DEFAULT_BOLD);
            trHdr.setPadding(0, 0, 0, dp(d, 7));
            wrap.addView(trHdr);

            android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(ctx);
            hsv.setHorizontalScrollBarEnabled(false);
            LinearLayout strip = new LinearLayout(ctx);
            strip.setOrientation(LinearLayout.HORIZONTAL);
            int limit = Math.min(finishedBooks.size(), 10);
            for (int i = 0; i < limit; i++) {
                final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = finishedBooks.get(i);
                String finDt = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getFinishedDate(ctx, rec.kfxPath);
                String aDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(d, 9), dp(d, 8), dp(d, 10), dp(d, 8));
                GradientDrawable cBg = new GradientDrawable();
                cBg.setColor(FolioTheme.bgSurface(ctx));
                cBg.setCornerRadius(dp(d, 9));
                cBg.setStroke(dp(d, 1), FolioTheme.accentBorder(ctx));
                card.setBackground(cBg);
                LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                        dp(d, 196), ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i < limit - 1) cLp.rightMargin = dp(d, 8);
                card.setLayoutParams(cLp);

                card.addView(makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 34, 50, 9));

                LinearLayout mc = new LinearLayout(ctx);
                mc.setOrientation(LinearLayout.VERTICAL);
                mc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView tTv = new TextView(ctx);
                tTv.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
                tTv.setTextColor(FolioTheme.textPrimary(ctx));
                tTv.setTextSize(12);
                tTv.setTypeface(Typeface.DEFAULT_BOLD);
                tTv.setMaxLines(2);
                tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                mc.addView(tTv);

                TextView aTv = new TextView(ctx);
                aTv.setText(!aDisp.isEmpty() ? aDisp : "Unknown Author");
                aTv.setTextColor(FolioTheme.textMuted(ctx));
                aTv.setTextSize(10.5f);
                aTv.setSingleLine(true);
                aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                mc.addView(aTv);

                TextView dTv = new TextView(ctx);
                dTv.setText(finDt.isEmpty() ? "✓ Finished" : ("✓ " + finDt));
                dTv.setTextColor(FolioTheme.accent(ctx));
                dTv.setTextSize(10.5f);
                dTv.setTypeface(Typeface.DEFAULT_BOLD);
                dTv.setPadding(0, dp(d, 2), 0, 0);
                mc.addView(dTv);

                card.addView(mc);
                card.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (goalDlgRef[0] != null) goalDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "Finished", onSaved);
                    }
                });
                strip.addView(card);
            }
            hsv.addView(strip);
            LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hsvLp.bottomMargin = dp(d, 14);
            hsv.setLayoutParams(hsvLp);
            wrap.addView(hsv);
        }

        TextView tgtLbl = new TextView(ctx);
        tgtLbl.setText("ANNUAL TARGET (BOOKS)");
        tgtLbl.setTextColor(FolioTheme.textMuted(ctx));
        tgtLbl.setTextSize(10.5f);
        tgtLbl.setTypeface(Typeface.DEFAULT_BOLD);
        tgtLbl.setPadding(0, 0, 0, dp(d, 7));
        wrap.addView(tgtLbl);

        final android.widget.EditText et = new android.widget.EditText(ctx);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(cur));
        et.setTextColor(FolioTheme.textPrimary(ctx));
        et.setHint("Number of books (e.g. 12, 24, 52)");
        et.setHintTextColor(FolioTheme.textMuted(ctx));
        et.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(FolioTheme.bgSurface(ctx));
        etBg.setCornerRadius(dp(d, 10));
        etBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        et.setBackground(etBg);

        LinearLayout presetRow = new LinearLayout(ctx);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams presetLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        presetLp.bottomMargin = dp(d, 10);
        presetRow.setLayoutParams(presetLp);

        int[] presets = new int[] { 5, 12, 24, 36, 52 };
        for (final int pv : presets) {
            TextView pill = new TextView(ctx);
            pill.setText(String.valueOf(pv));
            pill.setTextColor(pv == cur ? FolioTheme.bgModal(ctx) : FolioTheme.accent(ctx));
            pill.setTextSize(12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setGravity(Gravity.CENTER);
            pill.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(pv == cur ? FolioTheme.accent(ctx) : FolioTheme.bgControl(ctx));
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

        TextView wpmHdr = new TextView(ctx);
        wpmHdr.setText("READING SPEED (WPM)");
        wpmHdr.setTextColor(FolioTheme.textMuted(ctx));
        wpmHdr.setTextSize(10.5f);
        wpmHdr.setTypeface(Typeface.DEFAULT_BOLD);
        wpmHdr.setPadding(0, dp(d, 12), 0, dp(d, 6));
        wrap.addView(wpmHdr);

        final int[] chosenWpm = new int[] { getReadingSpeedWpm(ctx) };
        final LinearLayout wpmRow = new LinearLayout(ctx);
        wpmRow.setOrientation(LinearLayout.HORIZONTAL);
        final Runnable[] renderWpmRef = new Runnable[1];
        renderWpmRef[0] = new Runnable() {
            @Override public void run() {
                wpmRow.removeAllViews();
                int[] speeds = new int[] { 180, 250, 330, 420 };
                String[] lbls = new String[] { "180", "250", "330", "420" };
                for (int si = 0; si < speeds.length; si++) {
                    final int spVal = speeds[si];
                    boolean active = (chosenWpm[0] == spVal);
                    TextView spBtn = new TextView(ctx);
                    spBtn.setText(lbls[si] + " wpm");
                    spBtn.setTextSize(11.5f);
                    spBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    spBtn.setGravity(Gravity.CENTER);
                    spBtn.setTextColor(active ? 0xFF0E0F11 : FolioTheme.textPrimary(ctx));
                    spBtn.setPadding(dp(d, 6), dp(d, 7), dp(d, 6), dp(d, 7));
                    GradientDrawable sBg = new GradientDrawable();
                    sBg.setColor(active ? FolioTheme.accent(ctx) : FolioTheme.bgControl(ctx));
                    sBg.setCornerRadius(dp(d, 8));
                    if (!active) sBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
                    spBtn.setBackground(sBg);
                    LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (si < speeds.length - 1) sLp.rightMargin = dp(d, 6);
                    spBtn.setLayoutParams(sLp);
                    spBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            chosenWpm[0] = spVal;
                            setReadingSpeedWpm(ctx, spVal);
                            renderWpmRef[0].run();
                            FolioTheme.applyToTree(ctx, wpmRow);
                        }
                    });
                    wpmRow.addView(spBtn);
                }
            }
        };
        renderWpmRef[0].run();
        wrap.addView(wpmRow);

        goalDlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
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
        FolioTheme.applyToDialog(ctx, goalDlgRef[0]);
    }

    private static TextView makeSectionLabel(Context ctx, float d, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(FolioTheme.textMuted(ctx));
        tv.setTextSize(11);
        tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        tv.setLetterSpacing(0.08f);
        tv.setPadding(dp(d, 2), dp(d, 6), dp(d, 2), dp(d, 8));
        return tv;
    }

    private static LinearLayout makeGroupCard(Context ctx, float d) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(FolioTheme.bgSurface(ctx));
        bg.setCornerRadius(dp(d, 10));
        bg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(d, 14);
        card.setLayoutParams(lp);
        return card;
    }

    private static View makeKindleRow(
            Context ctx, float d, int iconId, String title, String subtitle,
            boolean topDivider, View.OnClickListener listener) {
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);

        if (topDivider) {
            View div = new View(ctx);
            div.setBackgroundColor(FolioTheme.bgControl(ctx));
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Math.round(d * 0.8f)));
            dlp.leftMargin = dp(d, 48);
            wrap.addView(div, dlp);
        }

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(d, 14), dp(d, 13), dp(d, 14), dp(d, 13));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(listener);

        View ico = FolioIcons.makeIcon(ctx, iconId, 24, FolioIcons.COLOR_ICON);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(d, 24), dp(d, 24));
        ilp.rightMargin = dp(d, 14);
        row.addView(ico, ilp);

        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tTv = new TextView(ctx);
        tTv.setText(title);
        tTv.setTextColor(FolioTheme.textPrimary(ctx));
        tTv.setTextSize(14.5f);
        tTv.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        col.addView(tTv);

        TextView sTv = new TextView(ctx);
        sTv.setText(subtitle);
        sTv.setTextColor(FolioTheme.textMuted(ctx));
        sTv.setTextSize(12);
        sTv.setPadding(0, dp(d, 2), 0, 0);
        col.addView(sTv);
        row.addView(col);

        View chev = FolioIcons.makeIcon(ctx, FolioIcons.CHEVRON_RIGHT, 16, FolioTheme.textMuted(ctx));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(d, 16), dp(d, 16));
        clp.leftMargin = dp(d, 8);
        row.addView(chev, clp);

        wrap.addView(row);
        return wrap;
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
        bg.setColor(FolioTheme.bgSurface(ctx));
        bg.setCornerRadius(dp(d, 12));
        bg.setStroke(dp(d, 1), FolioTheme.border(ctx));
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
        titleTv.setTextColor(FolioTheme.textPrimary(ctx));
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(titleTv);

        TextView subTv = new TextView(ctx);
        subTv.setText(subtitle);
        subTv.setTextSize(12);
        subTv.setTextColor(FolioTheme.textMuted(ctx));
        subTv.setPadding(0, dp(d, 2), 0, 0);
        textCol.addView(subTv);

        card.addView(textCol, tclp);

        TextView arrowTv = new TextView(ctx);
        arrowTv.setText("›");
        arrowTv.setTextSize(20);
        arrowTv.setTextColor(FolioTheme.textMuted(ctx));
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
        label.setTextColor(FolioTheme.textSecondary(ctx));
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
            btn.setTextColor(FolioTheme.accent(ctx));
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
        customLabel.setTextColor(FolioTheme.textSecondary(ctx));
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
        wBg.setColor(FolioTheme.bgSurface(ctx));
        wBg.setCornerRadius(dp(d, 12));
        wBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
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
        hdrTitle.setText("Continue Reading");
        hdrTitle.setTextColor(FolioTheme.textPrimary(ctx));
        hdrTitle.setTextSize(13);
        hdrTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hdrTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdr.addView(hdrTitle);

        TextView seeAll = new TextView(ctx);
        seeAll.setText("See All (" + inProgress.size() + ") ›");
        seeAll.setTextColor(FolioTheme.accent(ctx));
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
            cBg.setColor(FolioTheme.bgModal(ctx));
            cBg.setCornerRadius(dp(d, 10));
            cBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
            card.setBackground(cBg);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    dp(d, 196), ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < limit - 1) cLp.rightMargin = dp(d, 10);
            card.setLayoutParams(cLp);

            LinearLayout topRow = new LinearLayout(ctx);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            // Dedicated standalone cover thumbnail (FIT_CENTER on dark matte or placeholder)
            topRow.addView(makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 38, 54, 9));

            LinearLayout metaCol = new LinearLayout(ctx);
            metaCol.setOrientation(LinearLayout.VERTICAL);
            metaCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tTv = new TextView(ctx);
            tTv.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
            tTv.setTextColor(FolioTheme.textPrimary(ctx));
            tTv.setTextSize(12);
            tTv.setTypeface(Typeface.DEFAULT_BOLD);
            tTv.setMinLines(2);
            tTv.setMaxLines(2);
            tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(tTv);

            String authorDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
            TextView aTv = new TextView(ctx);
            aTv.setText(!authorDisp.isEmpty() ? authorDisp : "Unknown Author");
            aTv.setTextColor(FolioTheme.textMuted(ctx));
            aTv.setTextSize(10.5f);
            aTv.setSingleLine(true);
            aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            aTv.setPadding(0, dp(d, 2), 0, dp(d, 2));
            metaCol.addView(aTv);

            TextView pTv = new TextView(ctx);
            pTv.setText(pct + "% read · Resume ›");
            pTv.setTextColor(FolioTheme.accent(ctx));
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
            trBg.setColor(FolioTheme.bgControl(ctx));
            trBg.setCornerRadius(dp(d, 2));
            track.setBackground(trBg);
            LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 3));
            trLp.topMargin = dp(d, 8);
            track.setLayoutParams(trLp);

            float frac = Math.max(0.04f, Math.min(1f, pct / 100f));
            View fill = new View(ctx);
            GradientDrawable flBg = new GradientDrawable();
            flBg.setColor(FolioTheme.accent(ctx));
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
                    File f = com.amazon.kindle.epub.ingest.ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(rec.kfxPath));
                    if (f != null && f.exists()) {
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
        wBg.setColor(FolioTheme.bgSurface(ctx));
        wBg.setCornerRadius(dp(d, 12));
        wBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
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
            t1.setText("Up Next Queue");
            t1.setTextColor(FolioTheme.textPrimary(ctx));
            t1.setTextSize(13);
            t1.setTypeface(Typeface.DEFAULT_BOLD);
            txtCol.addView(t1);

            TextView t2 = new TextView(ctx);
            t2.setText("Pin unread books in Details (ⓘ) to plan your reading queue");
            t2.setTextColor(FolioTheme.textMuted(ctx));
            t2.setTextSize(11.5f);
            txtCol.addView(t2);
            row.addView(txtCol);

            TextView pickBtn = new TextView(ctx);
            pickBtn.setText("+ Pick ›");
            pickBtn.setTextColor(FolioTheme.accent(ctx));
            pickBtn.setTextSize(12);
            pickBtn.setTypeface(Typeface.DEFAULT_BOLD);
            pickBtn.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(FolioTheme.accentFill(ctx));
            pBg.setCornerRadius(dp(d, 8));
            pBg.setStroke(dp(d, 1), FolioTheme.accentBorder(ctx));
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
        hdrTitle.setText("Up Next Queue");
        hdrTitle.setTextColor(FolioTheme.textPrimary(ctx));
        hdrTitle.setTextSize(13);
        hdrTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hdrTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdr.addView(hdrTitle);

        TextView reorderBtn = new TextView(ctx);
        reorderBtn.setText("Reorder");
        reorderBtn.setTextColor(FolioTheme.textPrimary(ctx));
        reorderBtn.setTextSize(11);
        reorderBtn.setTypeface(Typeface.DEFAULT_BOLD);
        reorderBtn.setPadding(dp(d, 8), dp(d, 3), dp(d, 8), dp(d, 3));
        GradientDrawable roBg = new GradientDrawable();
        roBg.setColor(FolioTheme.bgControl(ctx));
        roBg.setCornerRadius(dp(d, 8));
        roBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        reorderBtn.setBackground(roBg);
        LinearLayout.LayoutParams roLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        roLp.rightMargin = dp(d, 8);
        reorderBtn.setLayoutParams(roLp);
        reorderBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showQueueManagerDialog(ctx, onRefresh);
            }
        });
        hdr.addView(reorderBtn);

        TextView seeAll = new TextView(ctx);
        seeAll.setText("See All (" + queued.size() + ") ›");
        seeAll.setTextColor(FolioTheme.accent(ctx));
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
            cBg.setColor(FolioTheme.bgControl(ctx));
            cBg.setCornerRadius(dp(d, 10));
            cBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
            card.setBackground(cBg);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    dp(d, 196), ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < limit - 1) cLp.rightMargin = dp(d, 10);
            card.setLayoutParams(cLp);

            card.addView(makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 36, 52, 9));

            LinearLayout metaCol = new LinearLayout(ctx);
            metaCol.setOrientation(LinearLayout.VERTICAL);
            metaCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tTv = new TextView(ctx);
            tTv.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
            tTv.setTextColor(FolioTheme.textPrimary(ctx));
            tTv.setTextSize(12);
            tTv.setTypeface(Typeface.DEFAULT_BOLD);
            tTv.setMaxLines(2);
            tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(tTv);

            String authorDisp = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
            TextView aTv = new TextView(ctx);
            aTv.setText(!authorDisp.isEmpty() ? authorDisp : "Unknown Author");
            aTv.setTextColor(FolioTheme.textMuted(ctx));
            aTv.setTextSize(10.5f);
            aTv.setSingleLine(true);
            aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            metaCol.addView(aTv);

            TextView goTv = new TextView(ctx);
            goTv.setText("Start Reading ›");
            goTv.setTextColor(FolioTheme.accent(ctx));
            goTv.setTextSize(10.5f);
            goTv.setTypeface(Typeface.DEFAULT_BOLD);
            goTv.setPadding(0, dp(d, 2), 0, 0);
            metaCol.addView(goTv);
            card.addView(metaCol);

            TextView rmBtn = new TextView(ctx);
            rmBtn.setText("✕");
            rmBtn.setTextColor(FolioTheme.textMuted(ctx));
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
                    File f = com.amazon.kindle.epub.ingest.ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(rec.kfxPath));
                    if (f != null && f.exists()) {
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

    private static String extractCoverInitial(String text) {
        if (text != null) {
            String baseName = text.trim();
            int sl = baseName.lastIndexOf('/');
            if (sl >= 0 && sl + 1 < baseName.length()) baseName = baseName.substring(sl + 1);
            for (int ci = 0; ci < baseName.length(); ci++) {
                char ch = baseName.charAt(ci);
                if (Character.isLetterOrDigit(ch)) {
                    return String.valueOf(Character.toUpperCase(ch));
                }
            }
        }
        return "A";
    }

    private static View makeMiniCoverView(Context ctx, float d, String kfxPath, int wDp, int hDp, int rightMarginDp) {
        return makeMiniCoverView(ctx, d, kfxPath, null, wDp, hDp, rightMarginDp);
    }

    private static View makeMiniCoverView(Context ctx, float d, String kfxPath, String titleHint, int wDp, int hDp, int rightMarginDp) {
        FrameLayout covFrame = new FrameLayout(ctx);
        GradientDrawable cfBg = new GradientDrawable();
        cfBg.setColor(FolioTheme.bgControl(ctx));
        cfBg.setCornerRadius(dp(d, 6));
        cfBg.setStroke(dp(d, 1), FolioTheme.border(ctx));
        covFrame.setBackground(cfBg);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(d, wDp), dp(d, hDp));
        ivLp.rightMargin = dp(d, rightMarginDp);
        covFrame.setLayoutParams(ivLp);
        android.graphics.Bitmap bmp = decodeMiniCover(kfxPath, dp(d, wDp + 8));
        if (bmp != null) {
            android.widget.ImageView covIv = new android.widget.ImageView(ctx);
            covIv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            covIv.setImageBitmap(bmp);
            covFrame.addView(covIv, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            TextView phTv = new TextView(ctx);
            phTv.setText(extractCoverInitial(titleHint != null && !titleHint.trim().isEmpty() ? titleHint : kfxPath));
            phTv.setTextColor(FolioTheme.textMuted(ctx));
            phTv.setTextSize(wDp >= 44 ? 19 : 16);
            phTv.setTypeface(FolioTheme.serif(ctx));
            phTv.setGravity(Gravity.CENTER);
            covFrame.addView(phTv, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            View spineRule = new View(ctx);
            GradientDrawable ruleBg = new GradientDrawable();
            ruleBg.setColor(FolioTheme.accentBorder(ctx));
            ruleBg.setCornerRadius(dp(d, 2));
            spineRule.setBackground(ruleBg);
            FrameLayout.LayoutParams ruleLp = new FrameLayout.LayoutParams(dp(d, Math.max(12, wDp / 2 - 4)), dp(d, 2));
            ruleLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            ruleLp.bottomMargin = dp(d, 6);
            covFrame.addView(spineRule, ruleLp);
        }
        return covFrame;
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

    private static void persistQueueOrder(Context ctx,
            java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> ordered) {
        if (ctx == null) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).edit();
            ed.clear();
            long base = System.currentTimeMillis() - (ordered.size() * 1000L);
            for (int i = 0; i < ordered.size(); i++) {
                com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r = ordered.get(i);
                if (r.kfxPath != null && !r.kfxPath.isEmpty()) {
                    ed.putLong(r.kfxPath, base + (i * 1000L));
                }
            }
            ed.apply();
        } catch (Throwable ignored) {}
    }

    private static void showQueueManagerDialog(final Context ctx, final Runnable onRefresh) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> queued = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : mdb.getAllBooks()) {
                if (com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
                    queued.add(r);
                }
            }
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }
        java.util.Collections.sort(queued, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                long ta = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getQueueTimestamp(ctx, a.kfxPath);
                long tb = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getQueueTimestamp(ctx, b.kfxPath);
                return Long.compare(ta, tb);
            }
        });

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 10));

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleHdr = new TextView(ctx);
        titleHdr.setText("Up Next Queue");
        titleHdr.setTextColor(TEXT_PRIMARY);
        titleHdr.setTextSize(17);
        titleHdr.setTypeface(Typeface.DEFAULT_BOLD);
        titleHdr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleHdr);

        final TextView countBadge = new TextView(ctx);
        countBadge.setTextColor(0xFFD4C9B8);
        countBadge.setTextSize(11);
        countBadge.setTypeface(Typeface.DEFAULT_BOLD);
        countBadge.setPadding(dp(d, 9), dp(d, 4), dp(d, 9), dp(d, 4));
        GradientDrawable cbBg = new GradientDrawable();
        cbBg.setColor(0xFF282520);
        cbBg.setCornerRadius(dp(d, 10));
        cbBg.setStroke(dp(d, 1), 0x664DA6FF);
        countBadge.setBackground(cbBg);
        hdrRow.addView(countBadge);
        root.addView(hdrRow);

        TextView subHdr = new TextView(ctx);
        subHdr.setText("Tap ↑ / ↓ to prioritize your reading order");
        subHdr.setTextColor(TEXT_MUTED);
        subHdr.setTextSize(12);
        subHdr.setPadding(0, dp(d, 4), 0, dp(d, 12));
        root.addView(subHdr);

        ScrollView sv = new ScrollView(ctx);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sv.setLayoutParams(svLp);
        final LinearLayout listHost = new LinearLayout(ctx);
        listHost.setOrientation(LinearLayout.VERTICAL);
        sv.addView(listHost);
        root.addView(sv);

        final AlertDialog[] dlgRef = new AlertDialog[1];
        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                listHost.removeAllViews();
                countBadge.setText(queued.size() + " queued");
                if (queued.isEmpty()) {
                    TextView emptyTv = new TextView(ctx);
                    emptyTv.setText("Your Up Next queue is empty.");
                    emptyTv.setTextColor(TEXT_MUTED);
                    emptyTv.setTextSize(13);
                    emptyTv.setGravity(Gravity.CENTER);
                    emptyTv.setPadding(dp(d, 12), dp(d, 22), dp(d, 12), dp(d, 22));
                    listHost.addView(emptyTv);
                    return;
                }
                for (int i = 0; i < queued.size(); i++) {
                    final int idx = i;
                    final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = queued.get(i);

                    LinearLayout row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(d, 10), dp(d, 10), dp(d, 8), dp(d, 10));
                    GradientDrawable rBg = new GradientDrawable();
                    rBg.setColor(BG_CARD);
                    rBg.setCornerRadius(dp(d, 10));
                    rBg.setStroke(dp(d, 1), i == 0 ? 0xFFD4C9B8 : 0xFF32353B);
                    row.setBackground(rBg);
                    LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rLp.bottomMargin = dp(d, 8);
                    row.setLayoutParams(rLp);

                    TextView rankTv = new TextView(ctx);
                    rankTv.setText("#" + (i + 1));
                    rankTv.setTextColor(i == 0 ? 0xFFD4C9B8 : 0xFF8A857D);
                    rankTv.setTextSize(12);
                    rankTv.setTypeface(Typeface.DEFAULT_BOLD);
                    rankTv.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams rkLp = new LinearLayout.LayoutParams(
                            dp(d, 26), ViewGroup.LayoutParams.WRAP_CONTENT);
                    rkLp.rightMargin = dp(d, 6);
                    rankTv.setLayoutParams(rkLp);
                    row.addView(rankTv);

                    row.addView(makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 34, 48, 10));

                    LinearLayout infoCol = new LinearLayout(ctx);
                    infoCol.setOrientation(LinearLayout.VERTICAL);
                    infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView bTitle = new TextView(ctx);
                    bTitle.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
                    bTitle.setTextColor(TEXT_PRIMARY);
                    bTitle.setTextSize(13);
                    bTitle.setTypeface(Typeface.DEFAULT_BOLD);
                    bTitle.setMaxLines(2);
                    bTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    infoCol.addView(bTitle);

                    String aStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                    TextView bAuth = new TextView(ctx);
                    bAuth.setText(!aStr.isEmpty() ? aStr : "Unknown Author");
                    bAuth.setTextColor(ACCENT_AMBER);
                    bAuth.setTextSize(11);
                    bAuth.setSingleLine(true);
                    bAuth.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    infoCol.addView(bAuth);

                    infoCol.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (dlgRef[0] != null) dlgRef[0].dismiss();
                            File f = com.amazon.kindle.epub.ingest.ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(rec.kfxPath));
                            if (f != null && f.exists()) {
                                com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                            }
                        }
                    });
                    row.addView(infoCol);

                    // Up / Down / Remove compact controls
                    boolean canUp = idx > 0;
                    TextView upBtn = new TextView(ctx);
                    upBtn.setText("↑");
                    upBtn.setTextColor(canUp ? 0xFFD4C9B8 : 0xFF32353B);
                    upBtn.setTextSize(14);
                    upBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    upBtn.setGravity(Gravity.CENTER);
                    upBtn.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
                    if (canUp) {
                        GradientDrawable uBg = new GradientDrawable();
                        uBg.setColor(0xFF282520);
                        uBg.setCornerRadius(dp(d, 7));
                        upBtn.setBackground(uBg);
                        upBtn.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                java.util.Collections.swap(queued, idx, idx - 1);
                                persistQueueOrder(ctx, queued);
                                if (onRefresh != null) onRefresh.run();
                                renderRef[0].run();
                            }
                        });
                    }
                    LinearLayout.LayoutParams btnLp1 = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnLp1.leftMargin = dp(d, 6);
                    upBtn.setLayoutParams(btnLp1);
                    row.addView(upBtn);

                    boolean canDown = idx < queued.size() - 1;
                    TextView dnBtn = new TextView(ctx);
                    dnBtn.setText("↓");
                    dnBtn.setTextColor(canDown ? 0xFFD4C9B8 : 0xFF32353B);
                    dnBtn.setTextSize(14);
                    dnBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    dnBtn.setGravity(Gravity.CENTER);
                    dnBtn.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
                    if (canDown) {
                        GradientDrawable dBg = new GradientDrawable();
                        dBg.setColor(0xFF282520);
                        dBg.setCornerRadius(dp(d, 7));
                        dnBtn.setBackground(dBg);
                        dnBtn.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                java.util.Collections.swap(queued, idx, idx + 1);
                                persistQueueOrder(ctx, queued);
                                if (onRefresh != null) onRefresh.run();
                                renderRef[0].run();
                            }
                        });
                    }
                    LinearLayout.LayoutParams btnLp2 = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnLp2.leftMargin = dp(d, 4);
                    dnBtn.setLayoutParams(btnLp2);
                    row.addView(dnBtn);

                    TextView rmBtn = new TextView(ctx);
                    rmBtn.setText("✕");
                    rmBtn.setTextColor(0xFF8A857D);
                    rmBtn.setTextSize(12);
                    rmBtn.setGravity(Gravity.CENTER);
                    rmBtn.setPadding(dp(d, 7), dp(d, 6), dp(d, 6), dp(d, 6));
                    rmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            queued.remove(idx);
                            persistQueueOrder(ctx, queued);
                            if (onRefresh != null) onRefresh.run();
                            renderRef[0].run();
                        }
                    });
                    LinearLayout.LayoutParams btnLp3 = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnLp3.leftMargin = dp(d, 2);
                    rmBtn.setLayoutParams(btnLp3);
                    row.addView(rmBtn);

                    listHost.addView(row);
                }
            }
        };
        renderRef[0].run();

        dlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton("Done", null)
                .setNeutralButton("Clear Queue", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        queued.clear();
                        persistQueueOrder(ctx, queued);
                        if (onRefresh != null) onRefresh.run();
                        Toast.makeText(ctx, "Up Next queue cleared", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
        FolioTheme.applyToDialog(ctx, dlgRef[0]);
    }

    private static void showTonightsPickDialog(final Context ctx, final Runnable onChanged) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }
        if (all.isEmpty()) {
            Toast.makeText(ctx, "No books indexed yet", Toast.LENGTH_SHORT).show();
            return;
        }
        final java.util.Map<String, Integer> progMap =
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.loadProgressMap(ctx);

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> unreadPool = new java.util.ArrayList<>();
        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
            Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
            int p = pv != null ? pv : 0;
            if (p == 0) unreadPool.add(r);
        }

        // Scope: 0 = Unread, 1 = Up Next Queue, 2 = All Books
        final int[] scopeIdx = new int[] { unreadPool.isEmpty() ? 2 : 0 };
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> activePool = new java.util.ArrayList<>();
        if (scopeIdx[0] == 0) activePool.addAll(unreadPool);
        else activePool.addAll(all);

        final java.util.Random rng = new java.util.Random();
        final int[] curIdx = new int[] { rng.nextInt(activePool.size()) };

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(d, 10));

        TextView titleHdr = new TextView(ctx);
        titleHdr.setText("Tonight's Pick");
        titleHdr.setTextColor(TEXT_PRIMARY);
        titleHdr.setTextSize(18);
        titleHdr.setTypeface(Typeface.DEFAULT_BOLD);
        titleHdr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleHdr);

        final TextView poolTag = new TextView(ctx);
        poolTag.setTextColor(0xFFD4C9B8);
        poolTag.setTextSize(11);
        poolTag.setTypeface(Typeface.DEFAULT_BOLD);
        poolTag.setPadding(dp(d, 9), dp(d, 4), dp(d, 9), dp(d, 4));
        GradientDrawable ptBg = new GradientDrawable();
        ptBg.setColor(0xFF282520);
        ptBg.setCornerRadius(dp(d, 10));
        ptBg.setStroke(dp(d, 1), 0x664DA6FF);
        poolTag.setBackground(ptBg);
        hdrRow.addView(poolTag);
        root.addView(hdrRow);

        // Pool Scope Selector Row (Unread / Queue / All)
        final LinearLayout scopeRow = new LinearLayout(ctx);
        scopeRow.setOrientation(LinearLayout.HORIZONTAL);
        scopeRow.setPadding(0, 0, 0, dp(d, 10));
        root.addView(scopeRow);

        final LinearLayout cardHost = new LinearLayout(ctx);
        cardHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(cardHost);

        final AlertDialog[] pickDlgRef = new AlertDialog[1];
        final Runnable[] renderScopeRef = new Runnable[1];
        final Runnable[] renderPickRef = new Runnable[1];

        renderScopeRef[0] = new Runnable() {
            @Override
            public void run() {
                scopeRow.removeAllViews();
                final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> queuePool = new java.util.ArrayList<>();
                for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
                    if (com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
                        queuePool.add(r);
                    }
                }
                String[] labels = new String[] {
                        "Unread (" + unreadPool.size() + ")",
                        "Queue (" + queuePool.size() + ")",
                        "All (" + all.size() + ")"
                };
                if (scopeIdx[0] == 0) poolTag.setText(unreadPool.size() + " unread");
                else if (scopeIdx[0] == 1) poolTag.setText(queuePool.size() + " queued");
                else poolTag.setText(all.size() + " books");

                for (int i = 0; i < labels.length; i++) {
                    final int targetScope = i;
                    boolean active = (scopeIdx[0] == i);
                    TextView pill = new TextView(ctx);
                    pill.setText(labels[i]);
                    pill.setTextSize(11.5f);
                    pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    pill.setTextColor(active ? 0xFFD4C9B8 : 0xFF8A857D);
                    pill.setGravity(Gravity.CENTER);
                    pill.setSingleLine(true);
                    pill.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
                    GradientDrawable pBg = new GradientDrawable();
                    pBg.setColor(active ? 0xFF282520 : 0xFF1C1E22);
                    pBg.setCornerRadius(dp(d, 9));
                    pBg.setStroke(dp(d, 1), active ? 0xFFD4C9B8 : 0xFF32353B);
                    pill.setBackground(pBg);
                    LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (i < labels.length - 1) plp.rightMargin = dp(d, 6);
                    pill.setLayoutParams(plp);
                    pill.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> nextPool = new java.util.ArrayList<>();
                            if (targetScope == 0) {
                                nextPool.addAll(unreadPool);
                            } else if (targetScope == 1) {
                                for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
                                    if (com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
                                        nextPool.add(r);
                                    }
                                }
                            } else {
                                nextPool.addAll(all);
                            }
                            if (nextPool.isEmpty()) {
                                String msg = (targetScope == 1) ? "Up Next queue is empty" : "No books in this pool";
                                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            scopeIdx[0] = targetScope;
                            activePool.clear();
                            activePool.addAll(nextPool);
                            curIdx[0] = rng.nextInt(activePool.size());
                            renderScopeRef[0].run();
                            renderPickRef[0].run();
                        }
                    });
                    scopeRow.addView(pill);
                }
            }
        };

        renderPickRef[0] = new Runnable() {
            @Override
            public void run() {
                cardHost.removeAllViews();
                if (activePool.isEmpty()) return;
                if (curIdx[0] >= activePool.size()) curIdx[0] = 0;
                final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = activePool.get(curIdx[0]);
                Integer pv = (rec.kfxPath != null) ? progMap.get(rec.kfxPath) : null;
                final int pct = pv != null ? pv : 0;

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(d, 14), dp(d, 14), dp(d, 14), dp(d, 14));
                GradientDrawable cBg = new GradientDrawable();
                cBg.setColor(BG_CARD);
                cBg.setCornerRadius(dp(d, 12));
                cBg.setStroke(dp(d, 1), BORDER_HAIRLINE);
                card.setBackground(cBg);

                LinearLayout topRow = new LinearLayout(ctx);
                topRow.setOrientation(LinearLayout.HORIZONTAL);
                topRow.setGravity(Gravity.CENTER_VERTICAL);

                topRow.addView(makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 68, 98, 12));

                LinearLayout mc = new LinearLayout(ctx);
                mc.setOrientation(LinearLayout.VERTICAL);
                mc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView bTitle = new TextView(ctx);
                bTitle.setText(rec.title != null && !rec.title.isEmpty() ? rec.title : "Untitled");
                bTitle.setTextColor(TEXT_PRIMARY);
                bTitle.setTextSize(15);
                bTitle.setTypeface(Typeface.DEFAULT_BOLD);
                bTitle.setMaxLines(3);
                bTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                mc.addView(bTitle);

                String aStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                TextView bAuthor = new TextView(ctx);
                bAuthor.setText(!aStr.isEmpty() ? aStr : "Unknown Author");
                bAuthor.setTextColor(ACCENT_AMBER);
                bAuthor.setTextSize(12.5f);
                bAuthor.setTypeface(Typeface.DEFAULT_BOLD);
                bAuthor.setPadding(0, dp(d, 4), 0, dp(d, 4));
                mc.addView(bAuthor);

                int yr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.parseBookYear(rec.pubDate);
                StringBuilder metaLine = new StringBuilder();
                if (yr > 0) metaLine.append(yr);
                if (rec.publisher != null && !rec.publisher.trim().isEmpty()) {
                    if (metaLine.length() > 0) metaLine.append(" · ");
                    metaLine.append(rec.publisher.trim());
                }
                if (pct > 0) {
                    if (metaLine.length() > 0) metaLine.append(" · ");
                    metaLine.append(pct >= 100 ? "Finished" : (pct + "% read"));
                }
                if (metaLine.length() > 0) {
                    TextView bMeta = new TextView(ctx);
                    bMeta.setText(metaLine.toString());
                    bMeta.setTextColor(0xFF8A857D);
                    bMeta.setTextSize(11.5f);
                    bMeta.setSingleLine(true);
                    bMeta.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    mc.addView(bMeta);
                }

                // Up to 2 clean genre pills
                if (rec.genres != null && !rec.genres.trim().isEmpty()) {
                    LinearLayout gRow = new LinearLayout(ctx);
                    gRow.setOrientation(LinearLayout.HORIZONTAL);
                    gRow.setPadding(0, dp(d, 6), 0, 0);
                    int gShown = 0;
                    for (String gPart : rec.genres.split(",")) {
                        String g = gPart.trim();
                        if (g.isEmpty() || g.length() > 22) continue;
                        if (gShown++ >= 2) break;
                        TextView gp = new TextView(ctx);
                        gp.setText(g);
                        gp.setTextColor(0xFFE6DDD0);
                        gp.setTextSize(10.5f);
                        gp.setPadding(dp(d, 8), dp(d, 3), dp(d, 8), dp(d, 3));
                        GradientDrawable gBg = new GradientDrawable();
                        gBg.setColor(0xFF282520);
                        gBg.setCornerRadius(dp(d, 8));
                        gp.setBackground(gBg);
                        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        glp.rightMargin = dp(d, 5);
                        gp.setLayoutParams(glp);
                        gRow.addView(gp);
                    }
                    if (gShown > 0) mc.addView(gRow);
                }
                topRow.addView(mc);
                card.addView(topRow);

                if (rec.description != null && !rec.description.trim().isEmpty()) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(rec.description.trim());
                    descTv.setTextColor(0xFFBDB8AE);
                    descTv.setTextSize(12.5f);
                    descTv.setMaxLines(4);
                    descTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    descTv.setPadding(0, dp(d, 10), 0, 0);
                    card.addView(descTv);
                }

                // Inline 3-Button Action Row: Another · Queue/Queued · ⓘ Details
                LinearLayout actRow = new LinearLayout(ctx);
                actRow.setOrientation(LinearLayout.HORIZONTAL);
                actRow.setPadding(0, dp(d, 12), 0, 0);

                TextView rerollBtn = new TextView(ctx);
                rerollBtn.setText("Another");
                rerollBtn.setTextColor(0xFFD4C9B8);
                rerollBtn.setTextSize(11.5f);
                rerollBtn.setTypeface(Typeface.DEFAULT_BOLD);
                rerollBtn.setGravity(Gravity.CENTER);
                rerollBtn.setSingleLine(true);
                rerollBtn.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));
                GradientDrawable rrBg = new GradientDrawable();
                rrBg.setColor(0xFF282520);
                rrBg.setCornerRadius(dp(d, 9));
                rrBg.setStroke(dp(d, 1), 0xFF524B41);
                rerollBtn.setBackground(rrBg);
                LinearLayout.LayoutParams rrLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                rrLp.rightMargin = dp(d, 6);
                rerollBtn.setLayoutParams(rrLp);
                rerollBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (activePool.size() > 1) {
                            int next = rng.nextInt(activePool.size());
                            if (next == curIdx[0]) next = (next + 1) % activePool.size();
                            curIdx[0] = next;
                            renderPickRef[0].run();
                        } else {
                            Toast.makeText(ctx, "Only 1 book in this pool", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                actRow.addView(rerollBtn);

                final boolean inQueue = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, rec.kfxPath);
                TextView queueBtn = new TextView(ctx);
                queueBtn.setText(inQueue ? "Queued" : "Queue");
                queueBtn.setTextColor(inQueue ? 0xFFECE8E0 : 0xFFBDB8AE);
                queueBtn.setTextSize(11.5f);
                queueBtn.setTypeface(Typeface.DEFAULT_BOLD);
                queueBtn.setGravity(Gravity.CENTER);
                queueBtn.setSingleLine(true);
                queueBtn.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));
                GradientDrawable qBg = new GradientDrawable();
                qBg.setColor(inQueue ? 0xFF282520 : 0xFF1C1E22);
                qBg.setCornerRadius(dp(d, 9));
                qBg.setStroke(dp(d, 1), inQueue ? 0xFF524B41 : 0xFF32353B);
                queueBtn.setBackground(qBg);
                LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                qLp.rightMargin = dp(d, 6);
                queueBtn.setLayoutParams(qLp);
                queueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        boolean nextQ = !inQueue;
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.setInQueue(ctx, rec.kfxPath, nextQ);
                        Toast.makeText(ctx, nextQ ? "Added to Up Next" : "Removed from Up Next", Toast.LENGTH_SHORT).show();
                        if (onChanged != null) onChanged.run();
                        renderScopeRef[0].run();
                        renderPickRef[0].run();
                    }
                });
                actRow.addView(queueBtn);

                TextView detBtn = new TextView(ctx);
                detBtn.setText("ⓘ Details");
                detBtn.setTextColor(ACCENT_TEAL);
                detBtn.setTextSize(11.5f);
                detBtn.setTypeface(Typeface.DEFAULT_BOLD);
                detBtn.setGravity(Gravity.CENTER);
                detBtn.setSingleLine(true);
                detBtn.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));
                GradientDrawable dtBg = new GradientDrawable();
                dtBg.setColor(0xFF282520);
                dtBg.setCornerRadius(dp(d, 9));
                dtBg.setStroke(dp(d, 1), 0xFF524B41);
                detBtn.setBackground(dtBg);
                detBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                detBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (pickDlgRef[0] != null) pickDlgRef[0].dismiss();
                        com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showBookDetails(ctx, rec, pct);
                    }
                });
                actRow.addView(detBtn);
                card.addView(actRow);

                cardHost.addView(card);
            }
        };

        renderScopeRef[0].run();
        renderPickRef[0].run();

        pickDlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton("Start Reading", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (activePool.isEmpty()) return;
                        com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec = activePool.get(curIdx[0]);
                        File f = new File(rec.kfxPath);
                        if (f.exists()) {
                            com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .show();
        FolioTheme.applyToDialog(ctx, pickDlgRef[0]);
    }

    private static java.util.List<String> loadRecentSearches(Context ctx) {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            String raw = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getString("recent_global_searches", "");
            if (raw != null && !raw.isEmpty()) {
                for (String s : raw.split("\n")) {
                    String t = s.trim();
                    if (!t.isEmpty()) out.add(t);
                }
            }
        } catch (Throwable ignored) {}
        return out;
    }

    private static void saveRecentSearch(Context ctx, String query) {
        if (query == null) return;
        String q = query.trim();
        if (q.length() < 2) return;
        try {
            java.util.List<String> cur = loadRecentSearches(ctx);
            java.util.List<String> next = new java.util.ArrayList<>();
            next.add(q);
            for (String item : cur) {
                if (!item.equalsIgnoreCase(q) && next.size() < 5) next.add(item);
            }
            StringBuilder sb = new StringBuilder();
            for (String item : next) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(item);
            }
            ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .edit().putString("recent_global_searches", sb.toString()).apply();
        } catch (Throwable ignored) {}
    }

    private static void showGlobalSearchDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }
        final java.util.Map<String, Integer> progMap =
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.loadProgressMap(ctx);

        // Pre-collect active custom shelves
        final java.util.Map<String, Integer> shelfCounts = new java.util.LinkedHashMap<>();
        try {
            java.util.Map<String, ?> shAll = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE).getAll();
            if (shAll != null) {
                for (Object v : shAll.values()) {
                    if (v instanceof String) {
                        String s = ((String) v).trim();
                        if (!s.isEmpty()) shelfCounts.put(s, (shelfCounts.containsKey(s) ? shelfCounts.get(s) : 0) + 1);
                    }
                }
            }
        } catch (Throwable ignored) {}

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        final TextView titleTv = new TextView(ctx);
        titleTv.setText("Quick Library Search");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        final TextView subTv = new TextView(ctx);
        subTv.setText("Search across " + all.size() + " books, authors, personal notes & shelves");
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 10));
        root.addView(subTv);

        final AlertDialog[] gsDlgRef = new AlertDialog[1];

        LinearLayout searchWrap = new LinearLayout(ctx);
        searchWrap.setOrientation(LinearLayout.HORIZONTAL);
        searchWrap.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(0xFF1C1E22);
        sBg.setCornerRadius(dp(d, 10));
        sBg.setStroke(dp(d, 1), 0xFF32353B);
        searchWrap.setBackground(sBg);
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        swLp.bottomMargin = dp(d, 10);
        searchWrap.setLayoutParams(swLp);

        final EditText searchEt = new EditText(ctx);
        searchEt.setHint("Type title, author, genre, note, or shelf...");
        searchEt.setHintTextColor(0xFF8A857D);
        searchEt.setTextColor(TEXT_PRIMARY);
        searchEt.setTextSize(13.5f);
        searchEt.setSingleLine(true);
        searchEt.setBackgroundColor(Color.TRANSPARENT);
        searchEt.setPadding(dp(d, 12), dp(d, 9), dp(d, 6), dp(d, 9));
        searchEt.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        searchWrap.addView(searchEt);

        final TextView clearBtn = new TextView(ctx);
        clearBtn.setText("✕");
        clearBtn.setTextColor(0xFF8A857D);
        clearBtn.setTextSize(14);
        clearBtn.setTypeface(Typeface.DEFAULT_BOLD);
        clearBtn.setPadding(dp(d, 10), dp(d, 8), dp(d, 12), dp(d, 8));
        clearBtn.setVisibility(View.GONE);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { searchEt.setText(""); }
        });
        searchWrap.addView(clearBtn);
        root.addView(searchWrap);

        // Quick-Filter Scope Pills (All, Reading, Notes, Queue, Shelves)
        final String[] scopeKeys = new String[] { "ALL", "READING", "RATED", "NOTES", "QUEUE", "SHELVES" };
        final String[] scopeLabels = new String[] { "All", "Reading", "★ Rated", "Notes", "Queue", "Shelves" };
        final String[] activeScope = new String[] { "ALL" };

        android.widget.HorizontalScrollView scopeScroll = new android.widget.HorizontalScrollView(ctx);
        scopeScroll.setHorizontalScrollBarEnabled(false);
        final LinearLayout scopeRow = new LinearLayout(ctx);
        scopeRow.setOrientation(LinearLayout.HORIZONTAL);
        scopeScroll.addView(scopeRow);
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scLp.bottomMargin = dp(d, 8);
        scopeScroll.setLayoutParams(scLp);
        root.addView(scopeScroll);

        // Recent Search Chips Row (shown when query is empty & history exists)
        final LinearLayout recentHost = new LinearLayout(ctx);
        recentHost.setOrientation(LinearLayout.HORIZONTAL);
        recentHost.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rhLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rhLp.bottomMargin = dp(d, 8);
        recentHost.setLayoutParams(rhLp);
        root.addView(recentHost);

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 316)));
        final LinearLayout resultsBox = new LinearLayout(ctx);
        resultsBox.setOrientation(LinearLayout.VERTICAL);
        sv.addView(resultsBox);
        root.addView(sv);

        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                // Repaint scope pills
                scopeRow.removeAllViews();
                for (int si = 0; si < scopeKeys.length; si++) {
                    final String sk = scopeKeys[si];
                    boolean act = sk.equals(activeScope[0]);
                    TextView sp = new TextView(ctx);
                    sp.setText(scopeLabels[si]);
                    sp.setTextColor(act ? 0xFF15171A : 0xFFBDB8AE);
                    sp.setTextSize(11.5f);
                    sp.setTypeface(act ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    sp.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
                    GradientDrawable spBg = new GradientDrawable();
                    spBg.setColor(act ? 0xFFD4C9B8 : 0xFF25282D);
                    spBg.setCornerRadius(dp(d, 13));
                    sp.setBackground(spBg);
                    LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    spLp.rightMargin = dp(d, 6);
                    sp.setLayoutParams(spLp);
                    sp.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            activeScope[0] = sk;
                            renderRef[0].run();
                        }
                    });
                    scopeRow.addView(sp);
                }

                resultsBox.removeAllViews();
                String q = searchEt.getText() != null
                        ? searchEt.getText().toString().trim().toLowerCase(java.util.Locale.ROOT)
                        : "";
                clearBtn.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);

                // Render Recent Searches strip when query is empty
                recentHost.removeAllViews();
                java.util.List<String> recents = loadRecentSearches(ctx);
                if (q.isEmpty() && !recents.isEmpty()) {
                    recentHost.setVisibility(View.VISIBLE);
                    android.widget.HorizontalScrollView rhsv = new android.widget.HorizontalScrollView(ctx);
                    rhsv.setHorizontalScrollBarEnabled(false);
                    LinearLayout rRow = new LinearLayout(ctx);
                    rRow.setOrientation(LinearLayout.HORIZONTAL);
                    rRow.setGravity(Gravity.CENTER_VERTICAL);
                    for (final String rq : recents) {
                        TextView chip = new TextView(ctx);
                        chip.setText("" + rq);
                        chip.setTextColor(0xFF8A857D);
                        chip.setTextSize(11);
                        chip.setPadding(dp(d, 9), dp(d, 4), dp(d, 9), dp(d, 4));
                        GradientDrawable rcBg = new GradientDrawable();
                        rcBg.setColor(0xFF1C1E22);
                        rcBg.setCornerRadius(dp(d, 12));
                        rcBg.setStroke(dp(d, 1), 0xFF32353B);
                        chip.setBackground(rcBg);
                        LinearLayout.LayoutParams rcLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        rcLp.rightMargin = dp(d, 6);
                        chip.setLayoutParams(rcLp);
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                searchEt.setText(rq);
                                searchEt.setSelection(rq.length());
                            }
                        });
                        rRow.addView(chip);
                    }
                    TextView clrH = new TextView(ctx);
                    clrH.setText("Clear");
                    clrH.setTextColor(0xFFC99A7E);
                    clrH.setTextSize(11);
                    clrH.setPadding(dp(d, 6), dp(d, 4), dp(d, 4), dp(d, 4));
                    clrH.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                                    .edit().remove("recent_global_searches").apply();
                            renderRef[0].run();
                        }
                    });
                    rRow.addView(clrH);
                    rhsv.addView(rRow);
                    recentHost.addView(rhsv);
                } else {
                    recentHost.setVisibility(View.GONE);
                }

                // 1. Matching Custom Shelves (shown for ALL or SHELVES scope)
                int shelfHits = 0;
                boolean showShelves = "ALL".equals(activeScope[0]) || "SHELVES".equals(activeScope[0]);
                for (java.util.Map.Entry<String, Integer> se : shelfCounts.entrySet()) {
                    if (!showShelves) break;
                    final String shName = se.getKey();
                    if (!q.isEmpty() && !shName.toLowerCase(java.util.Locale.ROOT).contains(q)) continue;
                    if (q.isEmpty() && shelfHits >= 3) break;
                    shelfHits++;

                    LinearLayout shRow = new LinearLayout(ctx);
                    shRow.setOrientation(LinearLayout.HORIZONTAL);
                    shRow.setGravity(Gravity.CENTER_VERTICAL);
                    shRow.setPadding(dp(d, 11), dp(d, 8), dp(d, 11), dp(d, 8));
                    GradientDrawable shBg = new GradientDrawable();
                    shBg.setColor(0xFF1C1E22);
                    shBg.setCornerRadius(dp(d, 9));
                    shBg.setStroke(dp(d, 1), 0xFF32353B);
                    shRow.setBackground(shBg);
                    LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    shLp.bottomMargin = dp(d, 7);
                    shRow.setLayoutParams(shLp);

                    TextView shTv = new TextView(ctx);
                    shTv.setText("Shelf: " + shName);
                    shTv.setTextColor(0xFFECE8E0);
                    shTv.setTextSize(12.5f);
                    shTv.setTypeface(Typeface.DEFAULT_BOLD);
                    shTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    shRow.addView(shTv);

                    TextView shCnt = new TextView(ctx);
                    int sc = se.getValue();
                    shCnt.setText(sc + (sc == 1 ? " book ›" : " books ›"));
                    shCnt.setTextColor(0xFF8A857D);
                    shCnt.setTextSize(11.5f);
                    shRow.addView(shCnt);

                    shRow.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (gsDlgRef[0] != null) gsDlgRef[0].dismiss();
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "SHELF:" + shName);
                        }
                    });
                    resultsBox.addView(shRow);
                }

                // 2. Matching Books & Personal Notes
                int bookHits = 0;
                int shown = 0;
                boolean showBooks = !"SHELVES".equals(activeScope[0]);
                for (final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec : all) {
                    if (!showBooks) break;
                    if ("RATED".equals(activeScope[0])
                            && com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getRating(ctx, rec.kfxPath) <= 0) {
                        continue;
                    }
                    if ("NOTES".equals(activeScope[0])
                            && com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getNote(ctx, rec.kfxPath).isEmpty()) {
                        continue;
                    }
                    if ("QUEUE".equals(activeScope[0])
                            && !com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, rec.kfxPath)) {
                        continue;
                    }
                    if ("READING".equals(activeScope[0])) {
                        Integer pChk = (rec.kfxPath != null) ? progMap.get(rec.kfxPath) : null;
                        int pvChk = pChk != null ? pChk : 0;
                        if (pvChk <= 0 || pvChk >= 100) continue;
                    }
                    String tStr = (rec.title != null && !rec.title.isEmpty()) ? rec.title : "Untitled";
                    String aStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                    String nStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getNote(ctx, rec.kfxPath);
                    String gStr = rec.genres != null ? rec.genres : "";
                    String iStr = rec.isbn != null ? rec.isbn : "";

                    boolean matchedNote = false;
                    if (!q.isEmpty()) {
                        String hay = (tStr + " " + aStr + " " + gStr + " " + iStr).toLowerCase(java.util.Locale.ROOT);
                        boolean inMeta = hay.contains(q);
                        matchedNote = !nStr.isEmpty() && nStr.toLowerCase(java.util.Locale.ROOT).contains(q);
                        if (!inMeta && !matchedNote) continue;
                    }
                    bookHits++;
                    if (shown >= 12) continue;
                    shown++;

                    Integer pv = (rec.kfxPath != null) ? progMap.get(rec.kfxPath) : null;
                    int pct = pv != null ? pv : 0;

                    LinearLayout card = new LinearLayout(ctx);
                    card.setOrientation(LinearLayout.HORIZONTAL);
                    card.setGravity(Gravity.CENTER_VERTICAL);
                    card.setPadding(dp(d, 10), dp(d, 8), dp(d, 10), dp(d, 8));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(BG_CARD);
                    cBg.setCornerRadius(dp(d, 9));
                    cBg.setStroke(dp(d, 1), matchedNote ? 0xFF524B41 : 0xFF32353B);
                    card.setBackground(cBg);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.bottomMargin = dp(d, 7);
                    card.setLayoutParams(cLp);

                    FrameLayout covFrame = new FrameLayout(ctx);
                    GradientDrawable cfBg = new GradientDrawable();
                    cfBg.setColor(0xFF15171A);
                    cfBg.setCornerRadius(dp(d, 4));
                    cfBg.setStroke(dp(d, 1), 0xFF25282D);
                    covFrame.setBackground(cfBg);
                    LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(d, 34), dp(d, 48));
                    ivLp.rightMargin = dp(d, 10);
                    covFrame.setLayoutParams(ivLp);
                    android.graphics.Bitmap bmp = decodeMiniCover(rec.kfxPath, dp(d, 42));
                    if (bmp != null) {
                        android.widget.ImageView covIv = new android.widget.ImageView(ctx);
                        covIv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                        covIv.setImageBitmap(bmp);
                        covFrame.addView(covIv, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    } else {
                        TextView phTv = new TextView(ctx);
                        phTv.setText(extractCoverInitial(rec.title != null && !rec.title.isEmpty() ? rec.title : rec.kfxPath));
                        phTv.setTextColor(0xFF8A857D);
                        phTv.setTextSize(16);
                        phTv.setTypeface(FolioTheme.serif(ctx));
                        phTv.setGravity(Gravity.CENTER);
                        covFrame.addView(phTv, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        View spineRule = new View(ctx);
                        GradientDrawable ruleBg = new GradientDrawable();
                        ruleBg.setColor(0xFF524B41);
                        ruleBg.setCornerRadius(dp(d, 2));
                        spineRule.setBackground(ruleBg);
                        FrameLayout.LayoutParams ruleLp = new FrameLayout.LayoutParams(dp(d, 14), dp(d, 2));
                        ruleLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        ruleLp.bottomMargin = dp(d, 5);
                        covFrame.addView(spineRule, ruleLp);
                    }
                    card.addView(covFrame);

                    LinearLayout mc = new LinearLayout(ctx);
                    mc.setOrientation(LinearLayout.VERTICAL);
                    mc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView bt = new TextView(ctx);
                    bt.setText(tStr);
                    bt.setTextColor(TEXT_PRIMARY);
                    bt.setTextSize(13);
                    bt.setTypeface(Typeface.DEFAULT_BOLD);
                    bt.setSingleLine(true);
                    bt.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    mc.addView(bt);

                    String statusTag = pct >= 100 ? "✓ Finished"
                            : (pct > 0 ? (pct + "% read")
                            : (com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.isInQueue(ctx, rec.kfxPath) ? "Up Next" : "Unread"));
                    TextView ba = new TextView(ctx);
                    ba.setText((!aStr.isEmpty() ? aStr : "Unknown Author") + " · " + statusTag);
                    ba.setTextColor(0xFF8A857D);
                    ba.setTextSize(11);
                    ba.setSingleLine(true);
                    ba.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    mc.addView(ba);

                    if (!nStr.isEmpty() && (matchedNote || q.isEmpty())) {
                        TextView bn = new TextView(ctx);
                        bn.setText("" + nStr);
                        bn.setTextColor(0xFFD4C9B8);
                        bn.setTextSize(10.5f);
                        bn.setSingleLine(true);
                        bn.setEllipsize(android.text.TextUtils.TruncateAt.END);
                        mc.addView(bn);
                    }
                    card.addView(mc);

                    final int cardPct = pct;
                    TextView infoPill = new TextView(ctx);
                    infoPill.setText("ⓘ");
                    infoPill.setTextColor(0xFF8A857D);
                    infoPill.setTextSize(14);
                    infoPill.setPadding(dp(d, 8), dp(d, 6), dp(d, 6), dp(d, 6));
                    infoPill.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            String curQ = searchEt.getText() != null ? searchEt.getText().toString().trim() : "";
                            if (!curQ.isEmpty()) saveRecentSearch(ctx, curQ);
                            if (gsDlgRef[0] != null) gsDlgRef[0].dismiss();
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showBookDetails(ctx, rec, cardPct);
                        }
                    });
                    card.addView(infoPill);

                    TextView readPill = new TextView(ctx);
                    readPill.setText("Read ›");
                    readPill.setTextColor(ACCENT_TEAL);
                    readPill.setTextSize(11);
                    readPill.setTypeface(Typeface.DEFAULT_BOLD);
                    readPill.setPadding(dp(d, 6), dp(d, 4), dp(d, 2), dp(d, 4));
                    card.addView(readPill);

                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            String curQ = searchEt.getText() != null ? searchEt.getText().toString().trim() : "";
                            if (!curQ.isEmpty()) saveRecentSearch(ctx, curQ);
                            File f = new File(rec.kfxPath);
                            if (f.exists()) {
                                if (gsDlgRef[0] != null) gsDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.ExternalEpubActivity.launchKfxReader(ctx, f);
                            }
                        }
                    });
                    resultsBox.addView(card);
                }

                if (q.isEmpty()) {
                    subTv.setText("Search across " + all.size() + " books, authors, personal notes & shelves");
                } else {
                    subTv.setText("Found " + bookHits + (bookHits == 1 ? " book" : " books")
                            + (shelfHits > 0 ? (" · " + shelfHits + (shelfHits == 1 ? " shelf" : " shelves")) : "")
                            + " matching \"" + q + "\"");
                }

                if (bookHits == 0 && shelfHits == 0) {
                    TextView noneTv = new TextView(ctx);
                    noneTv.setText("No books, notes, or shelves match \"" + q + "\".");
                    noneTv.setTextColor(TEXT_MUTED);
                    noneTv.setTextSize(13);
                    noneTv.setPadding(dp(d, 6), dp(d, 16), dp(d, 6), dp(d, 16));
                    resultsBox.addView(noneTv);
                }
            }
        };

        searchEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { renderRef[0].run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        renderRef[0].run();

        gsDlgRef[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setNeutralButton("Open in Catalog", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        String q = searchEt.getText() != null ? searchEt.getText().toString().trim() : "";
                        if (!q.isEmpty()) {
                            saveRecentSearch(ctx, q);
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, q);
                        } else {
                            com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All");
                        }
                    }
                })
                .setPositiveButton("Close", null)
                .show();
        FolioTheme.applyToDialog(ctx, gsDlgRef[0]);
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
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        final AlertDialog[] nbDlgRef = new AlertDialog[1];

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(d, 4));

        final TextView titleTv = new TextView(ctx);
        titleTv.setText("Reading Notes (" + annotated.size() + ")");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(17.5f);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleTv);

        TextView addNoteBtn = new TextView(ctx);
        addNoteBtn.setText("+ Add Note");
        addNoteBtn.setTextColor(0xFFD4C9B8);
        addNoteBtn.setTextSize(11.5f);
        addNoteBtn.setTypeface(Typeface.DEFAULT_BOLD);
        addNoteBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
        GradientDrawable anBg = new GradientDrawable();
        anBg.setColor(0xFF282520);
        anBg.setCornerRadius(dp(d, 9));
        anBg.setStroke(dp(d, 1), 0xFF524B41);
        addNoteBtn.setBackground(anBg);
        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showAddNoteBookPicker(ctx, all, nbDlgRef[0]);
            }
        });
        hdrRow.addView(addNoteBtn);
        root.addView(hdrRow);

        TextView subTv = new TextView(ctx);
        subTv.setText(annotated.isEmpty()
                ? "Tap + Add Note to save a quote or thought for any book"
                : "Tap any book header to read, or export your notebook below");
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 10));
        root.addView(subTv);

        if (!annotated.isEmpty()) {
            LinearLayout actBar = new LinearLayout(ctx);
            actBar.setOrientation(LinearLayout.HORIZONTAL);
            actBar.setPadding(0, 0, 0, dp(d, 10));

            TextView copyAllPill = new TextView(ctx);
            copyAllPill.setText("Copy All");
            copyAllPill.setTextColor(ACCENT_AMBER);
            copyAllPill.setTextSize(11.5f);
            copyAllPill.setTypeface(Typeface.DEFAULT_BOLD);
            copyAllPill.setGravity(Gravity.CENTER);
            copyAllPill.setSingleLine(true);
            copyAllPill.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
            GradientDrawable caBg = new GradientDrawable();
            caBg.setColor(0xFF282520);
            caBg.setCornerRadius(dp(d, 8));
            caBg.setStroke(dp(d, 1), 0xFF524B41);
            copyAllPill.setBackground(caBg);
            LinearLayout.LayoutParams caLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            caLp.rightMargin = dp(d, 6);
            copyAllPill.setLayoutParams(caLp);
            copyAllPill.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String md = buildNotesMarkdown(ctx, annotated, noteMap, rateMap);
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Azath Reading Notes", md));
                        Toast.makeText(ctx, "Copied " + annotated.size() + " note(s) as Markdown", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            actBar.addView(copyAllPill);

            TextView shareAllPill = new TextView(ctx);
            shareAllPill.setText("↗ Share");
            shareAllPill.setTextColor(ACCENT_TEAL);
            shareAllPill.setTextSize(11.5f);
            shareAllPill.setTypeface(Typeface.DEFAULT_BOLD);
            shareAllPill.setGravity(Gravity.CENTER);
            shareAllPill.setSingleLine(true);
            shareAllPill.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
            GradientDrawable saBg = new GradientDrawable();
            saBg.setColor(0xFF282520);
            saBg.setCornerRadius(dp(d, 8));
            saBg.setStroke(dp(d, 1), 0xFF524B41);
            shareAllPill.setBackground(saBg);
            LinearLayout.LayoutParams saLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            saLp.rightMargin = dp(d, 6);
            shareAllPill.setLayoutParams(saLp);
            shareAllPill.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String md = buildNotesMarkdown(ctx, annotated, noteMap, rateMap);
                    try {
                        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Azath Reading Notes");
                        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, md);
                        ctx.startActivity(android.content.Intent.createChooser(shareIntent, "Share Reading Notes"));
                    } catch (Throwable t) {
                        Toast.makeText(ctx, "Unable to open share sheet", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            actBar.addView(shareAllPill);

            TextView exportMdPill = new TextView(ctx);
            exportMdPill.setText("Export .md");
            exportMdPill.setTextColor(0xFFD4C9B8);
            exportMdPill.setTextSize(11.5f);
            exportMdPill.setTypeface(Typeface.DEFAULT_BOLD);
            exportMdPill.setGravity(Gravity.CENTER);
            exportMdPill.setSingleLine(true);
            exportMdPill.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
            GradientDrawable emBg = new GradientDrawable();
            emBg.setColor(0xFF282520);
            emBg.setCornerRadius(dp(d, 8));
            emBg.setStroke(dp(d, 1), 0xFF524B41);
            exportMdPill.setBackground(emBg);
            exportMdPill.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            exportMdPill.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String md = buildNotesMarkdown(ctx, annotated, noteMap, rateMap);
                    try {
                        File dir = new File("/sdcard/kindle/backups");
                        if (!dir.exists()) dir.mkdirs();
                        File outFile = new File(dir, "folio_reading_notes.md");
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                        byte[] bytes = md.getBytes("UTF-8");
                        fos.write(bytes);
                        fos.close();
                        Toast.makeText(ctx, "Saved folio_reading_notes.md (" + bytes.length + " B)", Toast.LENGTH_LONG).show();
                    } catch (Throwable t) {
                        Toast.makeText(ctx, "Export failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            actBar.addView(exportMdPill);
            root.addView(actBar);
        }

        final EditText searchEt = new EditText(ctx);
        searchEt.setHint("Search notes, titles, or authors...");
        searchEt.setHintTextColor(0xFF8A857D);
        searchEt.setTextColor(TEXT_PRIMARY);
        searchEt.setTextSize(13);
        searchEt.setSingleLine(true);
        searchEt.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(0xFF1C1E22);
        sBg.setCornerRadius(dp(d, 8));
        sBg.setStroke(dp(d, 1), 0xFF32353B);
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
                    emptyTv.setTextColor(0xFF8A857D);
                    emptyTv.setTextSize(13);
                    emptyTv.setPadding(dp(d, 14), dp(d, 14), dp(d, 14), dp(d, 14));
                    GradientDrawable eBg = new GradientDrawable();
                    eBg.setColor(BG_CARD);
                    eBg.setCornerRadius(dp(d, 10));
                    eBg.setStroke(dp(d, 1), 0xFF32353B);
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
                    cBg.setStroke(dp(d, 1), 0xFF32353B);
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
                    covIv.setBackgroundColor(0xFF15171A);
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
                    if (sh != null && !sh.isEmpty()) subMeta.append(" · ").append(sh);
                    TextView bSub = new TextView(ctx);
                    bSub.setText(subMeta.toString());
                    bSub.setTextColor(0xFF8A857D);
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
                    qBg.setColor(0xFF282520);
                    qBg.setCornerRadius(dp(d, 7));
                    qBg.setStroke(dp(d, 1), 0xFF524B41);
                    quoteWrap.setBackground(qBg);
                    LinearLayout.LayoutParams qwLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    qwLp.topMargin = dp(d, 8);
                    qwLp.bottomMargin = dp(d, 8);
                    quoteWrap.setLayoutParams(qwLp);

                    View accentBar = new View(ctx);
                    accentBar.setBackgroundColor(0xFFD4C9B8);
                    accentBar.setLayoutParams(new LinearLayout.LayoutParams(dp(d, 3), ViewGroup.LayoutParams.MATCH_PARENT));
                    quoteWrap.addView(accentBar);

                    TextView noteTv = new TextView(ctx);
                    noteTv.setText(noteTxt);
                    noteTv.setTextColor(0xFFECE8E0);
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
                    copyBtn.setText("Copy");
                    copyBtn.setTextColor(0xFF8A857D);
                    copyBtn.setTextSize(11);
                    copyBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    copyBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
                    GradientDrawable cpBg = new GradientDrawable();
                    cpBg.setColor(0xFF25282D);
                    cpBg.setCornerRadius(dp(d, 6));
                    cpBg.setStroke(dp(d, 1), 0xFF32353B);
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
                    editBtn.setText("Edit");
                    editBtn.setTextColor(ACCENT_TEAL);
                    editBtn.setTextSize(11);
                    editBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    editBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
                    GradientDrawable edBg = new GradientDrawable();
                    edBg.setColor(0xFF282520);
                    edBg.setCornerRadius(dp(d, 6));
                    edBg.setStroke(dp(d, 1), 0xFF524B41);
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
                        ? "Reading Notes (" + annotated.size() + ")"
                        : "Reading Notes (" + shown + " of " + annotated.size() + ")");
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
        nbDlgRef[0] = b.show();
        FolioTheme.applyToDialog(ctx, nbDlgRef[0]);
    }

    private static String buildNotesMarkdown(
            Context ctx,
            java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> annotated,
            java.util.Map<String, String> noteMap,
            java.util.Map<String, Integer> rateMap) {
        StringBuilder sb = new StringBuilder("# Azath Reading Notes\n\n");
        for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : annotated) {
            String t = (r.title != null && !r.title.isEmpty()) ? r.title : "Untitled";
            String a = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
            int stars = rateMap.containsKey(r.kfxPath) ? rateMap.get(r.kfxPath) : 0;
            sb.append("## ").append(t);
            if (!a.isEmpty()) sb.append(" — ").append(a);
            if (stars > 0) {
                sb.append(" (");
                for (int s = 0; s < stars; s++) sb.append("★");
                sb.append(")");
            }
            sb.append("\n> ").append(noteMap.get(r.kfxPath)).append("\n\n");
        }
        return sb.toString();
    }

    private static void showAddNoteBookPicker(
            final Context ctx,
            final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> allBooks,
            final AlertDialog parentDlg) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> sorted =
                new java.util.ArrayList<>(allBooks);
        java.util.Collections.sort(sorted, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                String ta = a.title != null ? a.title : "";
                String tb = b.title != null ? b.title : "";
                return ta.compareToIgnoreCase(tb);
            }
        });

        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(0xFF15171A);
        wrap.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Add / Edit Book Note");
        hdr.setTextColor(TEXT_PRIMARY);
        hdr.setTextSize(17);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 8));
        wrap.addView(hdr);

        final EditText filterEt = new EditText(ctx);
        filterEt.setHint("Filter books by title or author...");
        filterEt.setHintTextColor(0xFF8A857D);
        filterEt.setTextColor(TEXT_PRIMARY);
        filterEt.setTextSize(13);
        filterEt.setSingleLine(true);
        filterEt.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        GradientDrawable fBg = new GradientDrawable();
        fBg.setColor(0xFF1C1E22);
        fBg.setCornerRadius(dp(d, 8));
        fBg.setStroke(dp(d, 1), 0xFF32353B);
        filterEt.setBackground(fBg);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.bottomMargin = dp(d, 10);
        filterEt.setLayoutParams(fLp);
        wrap.addView(filterEt);

        ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 320)));
        final LinearLayout list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        wrap.addView(sv);

        final AlertDialog[] pickerDlg = new AlertDialog[1];
        final Runnable renderPicker = new Runnable() {
            @Override
            public void run() {
                list.removeAllViews();
                String q = filterEt.getText() != null
                        ? filterEt.getText().toString().trim().toLowerCase(java.util.Locale.ROOT)
                        : "";
                for (final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec : sorted) {
                    if (rec.kfxPath == null || rec.kfxPath.isEmpty()) continue;
                    final String titleStr = (rec.title != null && !rec.title.isEmpty()) ? rec.title : "Untitled";
                    final String authStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                    if (!q.isEmpty() && !(titleStr + " " + authStr).toLowerCase(java.util.Locale.ROOT).contains(q)) {
                        continue;
                    }
                    final String existing = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.getNote(ctx, rec.kfxPath);
                    final boolean hasNote = (existing != null && !existing.trim().isEmpty());

                    LinearLayout row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(d, 10), dp(d, 9), dp(d, 10), dp(d, 9));
                    GradientDrawable rBg = new GradientDrawable();
                    rBg.setColor(BG_CARD);
                    rBg.setCornerRadius(dp(d, 9));
                    rBg.setStroke(dp(d, 1), hasNote ? 0xFF524B41 : 0xFF32353B);
                    row.setBackground(rBg);
                    LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rLp.bottomMargin = dp(d, 7);
                    row.setLayoutParams(rLp);

                    LinearLayout tc = new LinearLayout(ctx);
                    tc.setOrientation(LinearLayout.VERTICAL);
                    tc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView tTv = new TextView(ctx);
                    tTv.setText(titleStr);
                    tTv.setTextColor(TEXT_PRIMARY);
                    tTv.setTextSize(13);
                    tTv.setTypeface(Typeface.DEFAULT_BOLD);
                    tTv.setMaxLines(1);
                    tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    tc.addView(tTv);

                    TextView aTv = new TextView(ctx);
                    aTv.setText(!authStr.isEmpty() ? authStr : "Unknown Author");
                    aTv.setTextColor(0xFF8A857D);
                    aTv.setTextSize(11);
                    aTv.setMaxLines(1);
                    aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    tc.addView(aTv);
                    row.addView(tc);

                    TextView tagTv = new TextView(ctx);
                    tagTv.setText(hasNote ? "Edit" : "+ Note");
                    tagTv.setTextColor(hasNote ? ACCENT_AMBER : 0xFFD4C9B8);
                    tagTv.setTextSize(11.5f);
                    tagTv.setTypeface(Typeface.DEFAULT_BOLD);
                    tagTv.setPadding(dp(d, 8), 0, 0, 0);
                    row.addView(tagTv);

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (pickerDlg[0] != null) pickerDlg[0].dismiss();
                            showInlineNoteEditor(ctx, rec.kfxPath, titleStr, hasNote ? existing.trim() : "", parentDlg);
                        }
                    });
                    list.addView(row);
                }
            }
        };
        renderPicker.run();
        filterEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { renderPicker.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        pickerDlg[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(wrap)
                .setNegativeButton("Cancel", null)
                .show();
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
        input.setHintTextColor(0xFF8A857D);
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

    private static void showSavedBackupsDialog(final Context ctx, final AlertDialog parentStorDlg) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final File backupDir = new File("/sdcard/kindle/backups");

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 10));

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(d, 4));

        final TextView titleTv = new TextView(ctx);
        titleTv.setText("Saved Backups & Exports");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleTv);

        final TextView countTag = new TextView(ctx);
        countTag.setTextColor(0xFFD4C9B8);
        countTag.setTextSize(11);
        countTag.setTypeface(Typeface.DEFAULT_BOLD);
        countTag.setPadding(dp(d, 9), dp(d, 4), dp(d, 9), dp(d, 4));
        GradientDrawable ctBg = new GradientDrawable();
        ctBg.setColor(0xFF282520);
        ctBg.setCornerRadius(dp(d, 10));
        ctBg.setStroke(dp(d, 1), 0xFF524B41);
        countTag.setBackground(ctBg);
        hdrRow.addView(countTag);
        root.addView(hdrRow);

        TextView pathSub = new TextView(ctx);
        pathSub.setText("/sdcard/kindle/backups");
        pathSub.setTextColor(TEXT_MUTED);
        pathSub.setTextSize(11.5f);
        pathSub.setTypeface(Typeface.MONOSPACE);
        pathSub.setPadding(0, 0, 0, dp(d, 12));
        root.addView(pathSub);

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 340)));
        final LinearLayout listHost = new LinearLayout(ctx);
        listHost.setOrientation(LinearLayout.VERTICAL);
        sv.addView(listHost);
        root.addView(sv);

        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                listHost.removeAllViews();
                java.util.List<File> files = new java.util.ArrayList<>();
                if (backupDir.exists() && backupDir.isDirectory()) {
                    File[] listed = backupDir.listFiles();
                    if (listed != null) {
                        for (File f : listed) {
                            if (f.isFile()) files.add(f);
                        }
                    }
                }
                java.util.Collections.sort(files, new java.util.Comparator<File>() {
                    @Override public int compare(File a, File b) {
                        return Long.compare(b.lastModified(), a.lastModified());
                    }
                });
                countTag.setText(files.size() + (files.size() == 1 ? " file" : " files"));

                if (files.isEmpty()) {
                    TextView emp = new TextView(ctx);
                    emp.setText("No saved backups or exports yet. Use Backup/Restore, Reading Log, or Reading Notes to export files.");
                    emp.setTextColor(0xFF8A857D);
                    emp.setTextSize(12.5f);
                    emp.setPadding(dp(d, 14), dp(d, 16), dp(d, 14), dp(d, 16));
                    GradientDrawable eBg = new GradientDrawable();
                    eBg.setColor(BG_CARD);
                    eBg.setCornerRadius(dp(d, 10));
                    eBg.setStroke(dp(d, 1), 0xFF32353B);
                    emp.setBackground(eBg);
                    listHost.addView(emp);
                    return;
                }

                for (final File f : files) {
                    String fn = f.getName();
                    String ext = "FILE";
                    int dot = fn.lastIndexOf('.');
                    if (dot >= 0 && dot + 1 < fn.length()) {
                        ext = fn.substring(dot + 1).toUpperCase(java.util.Locale.US);
                    }
                    int extCol = 0xFF8A857D;
                    int extFill = 0xFF25282D;
                    if ("HTML".equals(ext)) { extCol = 0xFFD4C9B8; extFill = 0xFF282520; }
                    else if ("MD".equals(ext)) { extCol = 0xFFD4C9B8; extFill = 0xFF282520; }
                    else if ("JSON".equals(ext)) { extCol = 0xFFD4C9B8; extFill = 0xFF282520; }
                    else if ("CSV".equals(ext)) { extCol = 0xFFD4C9B8; extFill = 0xFF282520; }

                    LinearLayout card = new LinearLayout(ctx);
                    card.setOrientation(LinearLayout.HORIZONTAL);
                    card.setGravity(Gravity.CENTER_VERTICAL);
                    card.setPadding(dp(d, 10), dp(d, 10), dp(d, 10), dp(d, 10));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(BG_CARD);
                    cBg.setCornerRadius(dp(d, 10));
                    cBg.setStroke(dp(d, 1), 0xFF32353B);
                    card.setBackground(cBg);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.bottomMargin = dp(d, 8);
                    card.setLayoutParams(cLp);

                    TextView badge = new TextView(ctx);
                    badge.setText(ext);
                    badge.setTextColor(extCol);
                    badge.setTextSize(10);
                    badge.setTypeface(Typeface.DEFAULT_BOLD);
                    badge.setGravity(Gravity.CENTER);
                    badge.setPadding(dp(d, 7), dp(d, 5), dp(d, 7), dp(d, 5));
                    GradientDrawable bBg = new GradientDrawable();
                    bBg.setColor(extFill);
                    bBg.setCornerRadius(dp(d, 7));
                    badge.setBackground(bBg);
                    LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                            dp(d, 44), ViewGroup.LayoutParams.WRAP_CONTENT);
                    bLp.rightMargin = dp(d, 10);
                    badge.setLayoutParams(bLp);
                    card.addView(badge);

                    LinearLayout mc = new LinearLayout(ctx);
                    mc.setOrientation(LinearLayout.VERTICAL);
                    mc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView nameTv = new TextView(ctx);
                    nameTv.setText(fn);
                    nameTv.setTextColor(TEXT_PRIMARY);
                    nameTv.setTextSize(12.5f);
                    nameTv.setTypeface(Typeface.DEFAULT_BOLD);
                    nameTv.setSingleLine(true);
                    nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    mc.addView(nameTv);

                    TextView metaTv = new TextView(ctx);
                    metaTv.setText(formatBytesShort(f.length()) + " · " + sdf.format(new java.util.Date(f.lastModified())));
                    metaTv.setTextColor(0xFF8A857D);
                    metaTv.setTextSize(10.5f);
                    mc.addView(metaTv);
                    card.addView(mc);

                    TextView viewBtn = new TextView(ctx);
                    viewBtn.setText("View");
                    viewBtn.setTextColor(ACCENT_TEAL);
                    viewBtn.setTextSize(13);
                    viewBtn.setPadding(dp(d, 8), dp(d, 5), dp(d, 8), dp(d, 5));
                    GradientDrawable vBg = new GradientDrawable();
                    vBg.setColor(0xFF282520);
                    vBg.setCornerRadius(dp(d, 7));
                    vBg.setStroke(dp(d, 1), 0xFF524B41);
                    viewBtn.setBackground(vBg);
                    LinearLayout.LayoutParams vLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    vLp.leftMargin = dp(d, 6);
                    viewBtn.setLayoutParams(vLp);
                    viewBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            showBackupFilePreviewDialog(ctx, f);
                        }
                    });
                    card.addView(viewBtn);

                    TextView delBtn = new TextView(ctx);
                    delBtn.setText("✕");
                    delBtn.setTextColor(0xFFC99A7E);
                    delBtn.setTextSize(12);
                    delBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    delBtn.setPadding(dp(d, 8), dp(d, 5), dp(d, 8), dp(d, 5));
                    GradientDrawable dBg = new GradientDrawable();
                    dBg.setColor(0xFF282520);
                    dBg.setCornerRadius(dp(d, 7));
                    dBg.setStroke(dp(d, 1), 0xFF524B41);
                    delBtn.setBackground(dBg);
                    LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dLp.leftMargin = dp(d, 5);
                    delBtn.setLayoutParams(dLp);
                    delBtn.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (f.delete()) {
                                Toast.makeText(ctx, "Deleted " + f.getName(), Toast.LENGTH_SHORT).show();
                                renderRef[0].run();
                            }
                        }
                    });
                    card.addView(delBtn);

                    listHost.addView(card);
                }
            }
        };
        renderRef[0].run();

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (parentStorDlg != null) {
                            parentStorDlg.dismiss();
                            showStorageDuplicatesDialog(ctx);
                        }
                    }
                })
                .show();
    }

    private static void showBackupFilePreviewDialog(final Context ctx, final File file) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        StringBuilder sb = new StringBuilder();
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"));
            char[] buf = new char[4096];
            int r = br.read(buf);
            if (r > 0) sb.append(buf, 0, r);
            if (br.read() != -1) sb.append("\n\n... (truncated preview)");
            br.close();
        } catch (Throwable t) {
            sb.append("Unable to preview file: ").append(t.getMessage());
        }
        final String previewText = sb.toString();

        ScrollView sv = new ScrollView(ctx);
        sv.setBackgroundColor(0xFF15171A);
        sv.setPadding(dp(d, 14), dp(d, 12), dp(d, 14), dp(d, 12));
        TextView tv = new TextView(ctx);
        tv.setText(previewText);
        tv.setTextColor(0xFFBDB8AE);
        tv.setTextSize(11.5f);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextIsSelectable(true);
        sv.addView(tv);

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(file.getName() + " (" + formatBytesShort(file.length()) + ")")
                .setView(sv)
                .setPositiveButton("Close", null)
                .setNeutralButton("Copy", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            cm.setPrimaryClip(android.content.ClipData.newPlainText(file.getName(), previewText));
                            Toast.makeText(ctx, "Copied preview to clipboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private static void showForceReconcileDialog(final Context ctx, final Runnable onDone) {
        Toast.makeText(ctx, "Scanning library & cache...", Toast.LENGTH_SHORT).show();
        final Handler h = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override public void run() {
                final com.amazon.kindle.epub.ingest.AzathLibrarySync.ReconcilePreview prev =
                        com.amazon.kindle.epub.ingest.AzathLibrarySync.previewReconcile(ctx);
                h.post(new Runnable() {
                    @Override public void run() {
                        if (FolioTheme.isContextDead(ctx)) return;
                        final float d = ctx.getResources().getDisplayMetrics().density;
                        LinearLayout box = new LinearLayout(ctx);
                        box.setOrientation(LinearLayout.VERTICAL);
                        box.setBackgroundColor(FolioTheme.bgSurface(ctx));
                        box.setPadding(dp(d, 20), dp(d, 18), dp(d, 20), dp(d, 12));

                        TextView hdr = new TextView(ctx);
                        hdr.setText("Force Reconcile Preview");
                        hdr.setTextColor(FolioTheme.textPrimary(ctx));
                        hdr.setTextSize(17);
                        hdr.setTypeface(Typeface.DEFAULT_BOLD);
                        hdr.setPadding(0, 0, 0, dp(d, 6));
                        box.addView(hdr);

                        int dbDelCount = prev.dbDeleteDetails.size();
                        int cacheDelCount = prev.cacheDeletePaths.size();
                        int totalDel = dbDelCount + cacheDelCount;

                        StringBuilder body = new StringBuilder();
                        body.append("Active KFX on disk: ").append(prev.activeKfxCount).append("\n");
                        body.append("/sdcard/kindle healthy: ").append(prev.sdKindleHealthy ? "Yes" : "No").append("\n");
                        body.append("Private folio_library healthy: ").append(prev.privateDirHealthy ? "Yes" : "No").append("\n\n");

                        if (prev.forcedPlan.skipDestructive) {
                            body.append("Blocked: ").append(prev.forcedPlan.warningReason).append("\n");
                        } else if (totalDel == 0) {
                            body.append("Library & cache are clean:\n");
                            body.append("• Missing DB rows to delete: 0\n");
                            body.append("• Orphan epub_cache files to delete: 0\n");
                            if (!prev.forcedPlan.backfillPaths.isEmpty()) {
                                body.append("• Unindexed KFX files to backfill: ").append(prev.forcedPlan.backfillPaths.size()).append("\n");
                            }
                        } else {
                            if (prev.circuitBreakerWouldTrip) {
                                body.append("Circuit breaker tripped for automatic reconcile; manual confirmation required.\n\n");
                            }
                            body.append("DB rows to delete (").append(dbDelCount).append("):\n");
                            if (dbDelCount == 0) {
                                body.append("  (none)\n");
                            } else {
                                for (String line : prev.dbDeleteDetails) {
                                    body.append("  • ").append(line).append("\n");
                                }
                            }
                            body.append("\nOrphan epub_cache files to delete (").append(cacheDelCount).append("):\n");
                            if (cacheDelCount == 0) {
                                body.append("  (none)\n");
                            } else {
                                for (String path : prev.cacheDeletePaths) {
                                    body.append("  • ").append(path).append("\n");
                                }
                            }
                        }

                        ScrollView sv = new ScrollView(ctx);
                        sv.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 260)));
                        TextView msg = new TextView(ctx);
                        msg.setText(body.toString());
                        msg.setTextColor(FolioTheme.textMuted(ctx));
                        msg.setTextSize(12.5f);
                        msg.setPadding(0, dp(d, 4), 0, dp(d, 4));
                        sv.addView(msg);
                        box.addView(sv);

                        AlertDialog.Builder b = new AlertDialog.Builder(ctx)
                                .setView(box)
                                .setNegativeButton("Cancel", null);
                        if (!prev.forcedPlan.skipDestructive && totalDel > 0) {
                            b.setPositiveButton("Confirm Delete (" + totalDel + ")", new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override public void run() {
                                            final int changes = com.amazon.kindle.epub.ingest.AzathLibrarySync.reconcileLibrary(ctx, true);
                                            h.post(new Runnable() {
                                                @Override public void run() {
                                                    if (FolioTheme.isContextDead(ctx)) return;
                                                    Toast.makeText(ctx,
                                                            "Force Reconcile completed (" + changes + " change(s))",
                                                            Toast.LENGTH_LONG).show();
                                                    if (onDone != null) onDone.run();
                                                }
                                            });
                                        }
                                    }, "azath-force-reconcile").start();
                                }
                            });
                        } else if (!prev.forcedPlan.skipDestructive && !prev.forcedPlan.backfillPaths.isEmpty()) {
                            b.setPositiveButton("Backfill (" + prev.forcedPlan.backfillPaths.size() + ")", new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override public void run() {
                                            final int changes = com.amazon.kindle.epub.ingest.AzathLibrarySync.reconcileLibrary(ctx, true);
                                            h.post(new Runnable() {
                                                @Override public void run() {
                                                    if (FolioTheme.isContextDead(ctx)) return;
                                                    Toast.makeText(ctx,
                                                            "Reconcile completed (" + changes + " change(s))",
                                                            Toast.LENGTH_SHORT).show();
                                                    if (onDone != null) onDone.run();
                                                }
                                            });
                                        }
                                    }, "azath-force-reconcile").start();
                                }
                            });
                        }
                        AlertDialog dlg = b.show();
                        FolioTheme.applyToDialog(ctx, dlg);
                    }
                });
            }
        }, "azath-preview-reconcile").start();
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
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Storage & Duplicates");
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
        sHdr.setTextColor(0xFFD4C9B8);
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
                0xFFD4C9B8, 0xFF282520, true, null));
        tileRow.addView(makeHealthBadge(ctx, d, "Backups ›", formatBytesShort(backupBytes),
                0xFFD4C9B8, 0xFF282520, true, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        showSavedBackupsDialog(ctx, storDlgRef[0]);
                    }
                }));
        tileRow.addView(makeHealthBadge(ctx, d, "Temp Cache", formatBytesShort(tempCacheBytes),
                0xFFD4C9B8, 0xFF25282D, false, null));
        content.addView(tileRow);

        // Section 2: Top 5 Largest Books (tap to locate in catalog)
        TextView lHdr = new TextView(ctx);
        lHdr.setText("LARGEST BOOKS (TAP TO LOCATE)");
        lHdr.setTextColor(0xFFD4C9B8);
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
                szTv.setTextColor(0xFFD4C9B8);
                szTv.setTextSize(12);
                szTv.setTypeface(Typeface.DEFAULT_BOLD);
                topR.addView(szTv);
                row.addView(topR);

                LinearLayout barTrack = new LinearLayout(ctx);
                barTrack.setOrientation(LinearLayout.HORIZONTAL);
                GradientDrawable tbBg = new GradientDrawable();
                tbBg.setColor(0xFF25282D);
                tbBg.setCornerRadius(dp(d, 3));
                barTrack.setBackground(tbBg);
                LinearLayout.LayoutParams btLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 5));
                btLp.topMargin = dp(d, 4);
                barTrack.setLayoutParams(btLp);

                View barFill = new View(ctx);
                GradientDrawable bfBg = new GradientDrawable();
                bfBg.setColor(0xFFD4C9B8);
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
        dHdr.setTextColor(dupKeys.isEmpty() ? 0xFFD4C9B8 : 0xFFC99A7E);
        dHdr.setTextSize(11);
        dHdr.setTypeface(Typeface.DEFAULT_BOLD);
        dHdr.setPadding(0, dp(d, 12), 0, dp(d, 8));
        content.addView(dHdr);

        if (dupKeys.isEmpty()) {
            TextView okTv = new TextView(ctx);
            okTv.setText("✓ No duplicate titles found across " + all.size() + " cataloged books");
            okTv.setTextColor(0xFFD4C9B8);
            okTv.setTextSize(12.5f);
            okTv.setTypeface(Typeface.DEFAULT_BOLD);
            okTv.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
            GradientDrawable okBg = new GradientDrawable();
            okBg.setColor(0xFF282520);
            okBg.setCornerRadius(dp(d, 8));
            okBg.setStroke(dp(d, 1), 0xFF524B41);
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
                dBg.setColor(0xFF282520);
                dBg.setCornerRadius(dp(d, 8));
                dBg.setStroke(dp(d, 1), 0xFF524B41);
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
                dcTv.setTextColor(0xFFC99A7E);
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
        FolioTheme.applyToDialog(ctx, storDlgRef[0]);
    }

    private static void showManageShelvesDialog(final Context ctx) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final SharedPreferences shelfPrefs = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);
        final java.util.Map<String, ?> rawAll = shelfPrefs.getAll();
        final java.util.Map<String, Integer> shelfCounts = new java.util.HashMap<>();
        final java.util.Map<String, String> shelfSamplePath = new java.util.HashMap<>();
        int totalShelved = 0;
        if (rawAll != null) {
            for (java.util.Map.Entry<String, ?> e : rawAll.entrySet()) {
                Object v = e.getValue();
                if (v instanceof String) {
                    String s = ((String) v).trim();
                    if (!s.isEmpty()) {
                        shelfCounts.put(s, (shelfCounts.containsKey(s) ? shelfCounts.get(s) : 0) + 1);
                        if (!shelfSamplePath.containsKey(s)) shelfSamplePath.put(s, e.getKey());
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
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        final AlertDialog[] mgrDlgRef = new AlertDialog[1];

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(d, 4));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Manage Shelves");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(17.5f);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleTv);

        TextView newShelfBtn = new TextView(ctx);
        newShelfBtn.setText("+ New Shelf");
        newShelfBtn.setTextColor(0xFFD4C9B8);
        newShelfBtn.setTextSize(11.5f);
        newShelfBtn.setTypeface(Typeface.DEFAULT_BOLD);
        newShelfBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
        GradientDrawable nsBg = new GradientDrawable();
        nsBg.setColor(0xFF282520);
        nsBg.setCornerRadius(dp(d, 9));
        nsBg.setStroke(dp(d, 1), 0xFF524B41);
        newShelfBtn.setBackground(nsBg);
        newShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showShelfBookPickerDialog(ctx, "", mgrDlgRef[0]);
            }
        });
        hdrRow.addView(newShelfBtn);
        root.addView(hdrRow);

        String subText = shelves.isEmpty()
                ? "Tap + New Shelf to organize books into collections"
                : shelves.size() + (shelves.size() == 1 ? " custom shelf · " : " custom shelves · ")
                        + totalShelved + (totalShelved == 1 ? " shelved book" : " shelved books")
                        + " — tap a shelf to browse";
        TextView subTv = new TextView(ctx);
        subTv.setText(subText);
        subTv.setTextColor(TEXT_MUTED);
        subTv.setTextSize(12);
        subTv.setPadding(0, 0, 0, dp(d, 14));
        root.addView(subTv);

        if (shelves.isEmpty()) {
            TextView emptyTv = new TextView(ctx);
            emptyTv.setText("Open any book's Details (ⓘ) in the Metadata Catalog to assign a custom reading shelf (e.g. Classics, Sci-Fi, Study).");
            emptyTv.setTextColor(0xFF8A857D);
            emptyTv.setTextSize(13);
            emptyTv.setPadding(dp(d, 14), dp(d, 16), dp(d, 14), dp(d, 16));
            GradientDrawable emptyBg = new GradientDrawable();
            emptyBg.setColor(BG_CARD);
            emptyBg.setCornerRadius(dp(d, 10));
            emptyBg.setStroke(dp(d, 1), 0xFF32353B);
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
                rowBg.setStroke(dp(d, 1), 0xFF32353B);
                row.setBackground(rowBg);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowLp.bottomMargin = dp(d, 8);
                row.setLayoutParams(rowLp);

                String samplePath = shelfSamplePath.get(shelfName);
                row.addView(makeMiniCoverView(ctx, d, samplePath, 30, 42, 10));

                LinearLayout infoCol = new LinearLayout(ctx);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                infoCol.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                infoCol.setPadding(0, 0, dp(d, 6), 0);

                TextView nameTv = new TextView(ctx);
                nameTv.setText(shelfName);
                nameTv.setTextColor(0xFFECE8E0);
                nameTv.setTextSize(13.5f);
                nameTv.setTypeface(Typeface.DEFAULT_BOLD);
                nameTv.setSingleLine(true);
                nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                infoCol.addView(nameTv);

                TextView cntTv = new TextView(ctx);
                cntTv.setText(cnt + (cnt == 1 ? " book ›" : " books ›"));
                cntTv.setTextColor(0xFF8A857D);
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

                // + Books pill
                TextView addBBtn = new TextView(ctx);
                addBBtn.setText("+ Books");
                addBBtn.setTextColor(0xFFD4C9B8);
                addBBtn.setTextSize(11);
                addBBtn.setTypeface(Typeface.DEFAULT_BOLD);
                addBBtn.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
                GradientDrawable abBg = new GradientDrawable();
                abBg.setColor(0xFF282520);
                abBg.setCornerRadius(dp(d, 7));
                abBg.setStroke(dp(d, 1), 0xFF524B41);
                addBBtn.setBackground(abBg);
                LinearLayout.LayoutParams abLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                abLp.rightMargin = dp(d, 5);
                addBBtn.setLayoutParams(abLp);
                addBBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        showShelfBookPickerDialog(ctx, shelfName, mgrDlgRef[0]);
                    }
                });
                row.addView(addBBtn);

                // Rename pill
                TextView renBtn = new TextView(ctx);
                renBtn.setText("Edit");
                renBtn.setTextColor(ACCENT_TEAL);
                renBtn.setTextSize(12);
                renBtn.setPadding(dp(d, 8), dp(d, 5), dp(d, 8), dp(d, 5));
                GradientDrawable renBg = new GradientDrawable();
                renBg.setColor(0xFF282520);
                renBg.setCornerRadius(dp(d, 7));
                renBg.setStroke(dp(d, 1), 0xFF524B41);
                renBtn.setBackground(renBg);
                LinearLayout.LayoutParams renLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                renLp.rightMargin = dp(d, 5);
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
                delBtn.setText("✕");
                delBtn.setTextColor(0xFFC99A7E);
                delBtn.setTextSize(12);
                delBtn.setTypeface(Typeface.DEFAULT_BOLD);
                delBtn.setPadding(dp(d, 8), dp(d, 5), dp(d, 8), dp(d, 5));
                GradientDrawable delBg = new GradientDrawable();
                delBg.setColor(0xFF282520);
                delBg.setCornerRadius(dp(d, 7));
                delBg.setStroke(dp(d, 1), 0xFF524B41);
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
        FolioTheme.applyToDialog(ctx, mgrDlgRef[0]);
    }

    private static void showShelfBookPickerDialog(
            final Context ctx, final String initialShelfName, final AlertDialog parentDlg) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final boolean isEditExisting = initialShelfName != null && !initialShelfName.trim().isEmpty();
        final android.content.SharedPreferences shelfPrefs =
                ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE);

        final java.util.List<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord> all = new java.util.ArrayList<>();
        com.amazon.kindle.epub.ingest.FolioMetadataDb mdb = new com.amazon.kindle.epub.ingest.FolioMetadataDb(ctx);
        try {
            all.addAll(mdb.getAllBooks());
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }
        java.util.Collections.sort(all, new java.util.Comparator<com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord a,
                               com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord b) {
                String ta = a.title != null ? a.title : "";
                String tb = b.title != null ? b.title : "";
                return ta.compareToIgnoreCase(tb);
            }
        });

        final java.util.Set<String> selected = new java.util.HashSet<>();
        if (isEditExisting) {
            for (com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord r : all) {
                if (r.kfxPath != null && initialShelfName.equals(shelfPrefs.getString(r.kfxPath, ""))) {
                    selected.add(r.kfxPath);
                }
            }
        }

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 10));

        final TextView hdr = new TextView(ctx);
        hdr.setText(isEditExisting ? ("Books in " + initialShelfName) : "Create New Shelf");
        hdr.setTextColor(TEXT_PRIMARY);
        hdr.setTextSize(17);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 8));
        root.addView(hdr);

        final EditText nameEt = new EditText(ctx);
        nameEt.setHint("Shelf name (e.g. Philosophy, Sci-Fi, Study)...");
        nameEt.setHintTextColor(0xFF8A857D);
        nameEt.setTextColor(TEXT_PRIMARY);
        nameEt.setTextSize(13.5f);
        nameEt.setSingleLine(true);
        if (isEditExisting) nameEt.setText(initialShelfName);
        nameEt.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        GradientDrawable nBg = new GradientDrawable();
        nBg.setColor(0xFF1C1E22);
        nBg.setCornerRadius(dp(d, 8));
        nBg.setStroke(dp(d, 1), 0xFF524B41);
        nameEt.setBackground(nBg);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nLp.bottomMargin = dp(d, 8);
        nameEt.setLayoutParams(nLp);
        if (!isEditExisting) {
            root.addView(nameEt);
            // Quick preset shelf chips
            android.widget.HorizontalScrollView pHsv = new android.widget.HorizontalScrollView(ctx);
            pHsv.setHorizontalScrollBarEnabled(false);
            LinearLayout pRow = new LinearLayout(ctx);
            pRow.setOrientation(LinearLayout.HORIZONTAL);
            pRow.setPadding(0, 0, 0, dp(d, 8));
            String[] presets = { "Philosophy", "Fiction", "Sci-Fi", "History", "Study" };
            for (final String pr : presets) {
                TextView pc = new TextView(ctx);
                pc.setText(pr);
                pc.setTextColor(0xFFD4C9B8);
                pc.setTextSize(11);
                pc.setPadding(dp(d, 9), dp(d, 4), dp(d, 9), dp(d, 4));
                GradientDrawable pBg = new GradientDrawable();
                pBg.setColor(0xFF282520);
                pBg.setCornerRadius(dp(d, 8));
                pBg.setStroke(dp(d, 1), 0x664DA6FF);
                pc.setBackground(pBg);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                pLp.rightMargin = dp(d, 6);
                pc.setLayoutParams(pLp);
                pc.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        nameEt.setText(pr);
                        nameEt.setSelection(pr.length());
                    }
                });
                pRow.addView(pc);
            }
            pHsv.addView(pRow);
            root.addView(pHsv);
        }

        final EditText filterEt = new EditText(ctx);
        filterEt.setHint("Filter books to include...");
        filterEt.setHintTextColor(0xFF8A857D);
        filterEt.setTextColor(TEXT_PRIMARY);
        filterEt.setTextSize(12.5f);
        filterEt.setSingleLine(true);
        filterEt.setPadding(dp(d, 12), dp(d, 7), dp(d, 12), dp(d, 7));
        GradientDrawable fBg = new GradientDrawable();
        fBg.setColor(0xFF1C1E22);
        fBg.setCornerRadius(dp(d, 8));
        fBg.setStroke(dp(d, 1), 0xFF32353B);
        filterEt.setBackground(fBg);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.bottomMargin = dp(d, 8);
        filterEt.setLayoutParams(fLp);
        root.addView(filterEt);

        final TextView selCountTv = new TextView(ctx);
        selCountTv.setTextColor(0xFFD4C9B8);
        selCountTv.setTextSize(11.5f);
        selCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        selCountTv.setPadding(0, 0, 0, dp(d, 6));
        root.addView(selCountTv);

        ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 260)));
        final LinearLayout list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv);

        final Runnable[] renderListRef = new Runnable[1];
        renderListRef[0] = new Runnable() {
            @Override
            public void run() {
                list.removeAllViews();
                selCountTv.setText(selected.size() + (selected.size() == 1 ? " book selected" : " books selected"));
                String q = filterEt.getText() != null
                        ? filterEt.getText().toString().trim().toLowerCase(java.util.Locale.ROOT)
                        : "";
                for (final com.amazon.kindle.epub.ingest.FolioMetadataDb.BookRecord rec : all) {
                    if (rec.kfxPath == null || rec.kfxPath.isEmpty()) continue;
                    String titleStr = (rec.title != null && !rec.title.isEmpty()) ? rec.title : "Untitled";
                    String authStr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.formatAuthorForDisplay(rec.author);
                    if (!q.isEmpty() && !(titleStr + " " + authStr).toLowerCase(java.util.Locale.ROOT).contains(q)) {
                        continue;
                    }
                    final boolean isSel = selected.contains(rec.kfxPath);
                    LinearLayout row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(d, 10), dp(d, 8), dp(d, 10), dp(d, 8));
                    GradientDrawable rBg = new GradientDrawable();
                    rBg.setColor(isSel ? 0xFF282520 : BG_CARD);
                    rBg.setCornerRadius(dp(d, 8));
                    rBg.setStroke(dp(d, 1), isSel ? 0xFFD4C9B8 : 0xFF32353B);
                    row.setBackground(rBg);
                    LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rLp.bottomMargin = dp(d, 6);
                    row.setLayoutParams(rLp);

                    TextView chk = new TextView(ctx);
                    chk.setText(isSel ? "☑" : "☐");
                    chk.setTextColor(isSel ? 0xFFD4C9B8 : 0xFF8A857D);
                    chk.setTextSize(16);
                    chk.setPadding(0, 0, dp(d, 10), 0);
                    row.addView(chk);

                    LinearLayout tc = new LinearLayout(ctx);
                    tc.setOrientation(LinearLayout.VERTICAL);
                    tc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView tTv = new TextView(ctx);
                    tTv.setText(titleStr);
                    tTv.setTextColor(TEXT_PRIMARY);
                    tTv.setTextSize(12.5f);
                    tTv.setTypeface(Typeface.DEFAULT_BOLD);
                    tTv.setMaxLines(1);
                    tTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    tc.addView(tTv);

                    TextView aTv = new TextView(ctx);
                    aTv.setText(!authStr.isEmpty() ? authStr : "Unknown Author");
                    aTv.setTextColor(0xFF8A857D);
                    aTv.setTextSize(11);
                    aTv.setMaxLines(1);
                    aTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    tc.addView(aTv);
                    row.addView(tc);

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (selected.contains(rec.kfxPath)) selected.remove(rec.kfxPath);
                            else selected.add(rec.kfxPath);
                            renderListRef[0].run();
                        }
                    });
                    list.addView(row);
                }
            }
        };
        renderListRef[0].run();
        filterEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { renderListRef[0].run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton(isEditExisting ? "Save Shelf" : "Create Shelf", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        String shelfName = isEditExisting ? initialShelfName : nameEt.getText().toString().trim();
                        if (shelfName.isEmpty()) {
                            Toast.makeText(ctx, "Enter a shelf name", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!isEditExisting && selected.isEmpty()) {
                            Toast.makeText(ctx, "Select at least 1 book for the shelf", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        android.content.SharedPreferences.Editor ed = shelfPrefs.edit();
                        if (isEditExisting) {
                            java.util.Map<String, ?> curAll = shelfPrefs.getAll();
                            if (curAll != null) {
                                for (java.util.Map.Entry<String, ?> e : curAll.entrySet()) {
                                    if (shelfName.equals(e.getValue()) && !selected.contains(e.getKey())) {
                                        ed.remove(e.getKey());
                                    }
                                }
                            }
                        }
                        for (String p : selected) {
                            ed.putString(p, shelfName);
                        }
                        ed.apply();
                        Toast.makeText(ctx, "Saved " + selected.size() + " book(s) to " + shelfName, Toast.LENGTH_SHORT).show();
                        if (parentDlg != null) parentDlg.dismiss();
                        showManageShelvesDialog(ctx);
                    }
                })
                .setNegativeButton("Cancel", null)
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
        input.setHintTextColor(0xFF8A857D);
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
        loadQuickStatsAsync(ctx, new StatsCallback() {
            @Override public void onStats(int[] qs) {
                showLibraryInsightsDialogLoaded(ctx, qs);
            }
        });
    }

    private static void showLibraryInsightsDialogLoaded(final Context ctx, final int[] qs) {
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
        final int[] starBuckets = new int[6];
        int missingCover = 0, missingGenre = 0, missingDesc = 0;
        final java.util.LinkedHashMap<String, Integer> eraFreq = new java.util.LinkedHashMap<>();
        String[] eraOrder = new String[] { "Pre-1900", "1900–1949", "1950–1999", "2000–2009", "2010–2019", "2020+" };
        for (String eo : eraOrder) eraFreq.put(eo, 0);
        int minYear = Integer.MAX_VALUE, maxYear = Integer.MIN_VALUE, datedCount = 0;
        final int userWpm = getReadingSpeedWpm(ctx);
        final java.util.Map<String, Integer> insProgMap =
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.loadProgressMap(ctx);
        double totalLibMins = 0.0;
        double readMinsSoFar = 0.0;

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
                File kfxF = new File(r.kfxPath);
                if (kfxF.exists() && kfxF.length() > 0) {
                    double bMins = Math.max(15.0, Math.min(2400.0, (kfxF.length() / 1024.0) * 0.34 * (250.0 / userWpm)));
                    totalLibMins += bMins;
                    Integer pv = insProgMap.get(r.kfxPath);
                    int p = (pv != null) ? Math.max(0, Math.min(100, pv)) : 0;
                    readMinsSoFar += bMins * (p / 100.0);
                }
            }
            if (!hasCov) missingCover++;
            int bYr = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.parseBookYear(r.pubDate);
            if (bYr > 0) {
                datedCount++;
                if (bYr < minYear) minYear = bYr;
                if (bYr > maxYear) maxYear = bYr;
                String eb = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.eraBucketForYear(bYr);
                if (eraFreq.containsKey(eb)) eraFreq.put(eb, eraFreq.get(eb) + 1);
            }
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
            if (st >= 1 && st <= 5) { ratedCount++; ratingSum += st; starBuckets[st]++; }
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

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15171A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Library Insights");
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(d, 4));
        root.addView(titleTv);

        String avgStr = ratedCount > 0
                ? String.format(java.util.Locale.US, "%.1f★ avg (%d rated)", ratingSum / (float) ratedCount, ratedCount)
                : "No ratings yet";
        final int finCnt = (qs != null && qs.length >= 3) ? qs[2] : 0;
        final int readCnt = (qs != null && qs.length >= 2) ? qs[1] : 0;
        TextView summaryTv = new TextView(ctx);
        summaryTv.setText(all.size() + " cataloged · " + readCnt + " reading · " + finCnt + " finished\n"
                + favs + " fav · " + noted + " noted · " + shelved + " shelved · "
                + com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countQueue(ctx) + " queued · " + avgStr);
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

        // Section -1: Reading Time Portfolio (calibrated to user WPM)
        double remMins = Math.max(0.0, totalLibMins - readMinsSoFar);
        String totHrStr = totalLibMins >= 60.0 ? (Math.round(totalLibMins / 60.0) + "h") : (Math.round(totalLibMins) + "m");
        String readHrStr = readMinsSoFar >= 60.0
                ? String.format(java.util.Locale.US, "%.1fh", readMinsSoFar / 60.0)
                : (Math.round(readMinsSoFar) + "m");
        String remHrStr = remMins >= 60.0 ? (Math.round(remMins / 60.0) + "h") : (Math.round(remMins) + "m");

        TextView rtHdr = new TextView(ctx);
        rtHdr.setText("READING TIME PORTFOLIO (" + userWpm + " WPM · TAP TO CALIBRATE)");
        rtHdr.setTextColor(0xFFD4C9B8);
        rtHdr.setTextSize(11);
        rtHdr.setTypeface(Typeface.DEFAULT_BOLD);
        rtHdr.setPadding(0, 0, 0, dp(d, 7));
        View.OnClickListener calibrateClick = new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                showReadingGoalDialog(ctx, new Runnable() {
                    @Override public void run() { showLibraryInsightsDialog(ctx); }
                });
            }
        };
        rtHdr.setOnClickListener(calibrateClick);
        content.addView(rtHdr);

        LinearLayout rtRow = new LinearLayout(ctx);
        rtRow.setOnClickListener(calibrateClick);
        rtRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rtRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rtRowLp.bottomMargin = dp(d, 14);
        rtRow.setLayoutParams(rtRowLp);

        String[] rtVals = new String[] { totHrStr, readHrStr, remHrStr };
        String[] rtLbls = new String[] { "TOTAL LIBRARY", "TIME READ", "REMAINING" };
        for (int ri = 0; ri < 3; ri++) {
            LinearLayout rTile = new LinearLayout(ctx);
            rTile.setOrientation(LinearLayout.VERTICAL);
            rTile.setGravity(Gravity.CENTER);
            rTile.setPadding(dp(d, 8), dp(d, 9), dp(d, 8), dp(d, 9));
            GradientDrawable rtBg = new GradientDrawable();
            rtBg.setColor(ri == 1 ? 0xFF282520 : BG_CARD);
            rtBg.setCornerRadius(dp(d, 9));
            rtBg.setStroke(dp(d, 1), ri == 1 ? 0xFF524B41 : BORDER_HAIRLINE);
            rTile.setBackground(rtBg);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (ri < 2) rLp.rightMargin = dp(d, 7);
            rTile.setLayoutParams(rLp);

            TextView rv = new TextView(ctx);
            rv.setText(rtVals[ri]);
            rv.setTextColor(ri == 1 ? 0xFFD4C9B8 : TEXT_PRIMARY);
            rv.setTextSize(16);
            rv.setTypeface(Typeface.DEFAULT_BOLD);
            rTile.addView(rv);

            TextView rl = new TextView(ctx);
            rl.setText(rtLbls[ri]);
            rl.setTextColor(TEXT_MUTED);
            rl.setTextSize(9.5f);
            rl.setTypeface(Typeface.DEFAULT_BOLD);
            rl.setPadding(0, dp(d, 2), 0, 0);
            rTile.addView(rl);
            rtRow.addView(rTile);
        }
        rtRow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                showReadingGoalDialog(ctx, new Runnable() {
                    @Override public void run() { showLibraryInsightsDialog(ctx); }
                });
            }
        });
        content.addView(rtRow);

        // Section 0: Interactive Reading Status Stacked Bar (Finished / Reading / Unread)
        int unrdCnt = Math.max(0, all.size() - finCnt - readCnt);
        int totalStat = Math.max(1, finCnt + readCnt + unrdCnt);

        TextView stHdr = new TextView(ctx);
        stHdr.setText("READING STATUS (TAP TO FILTER)");
        stHdr.setTextColor(0xFFD4C9B8);
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
            sfBg.setColor(0xFFD4C9B8);
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
            srBg.setColor(0xFFD4C9B8);
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
            suBg.setColor(0xFF32353B);
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
            { "● Finished (" + finCnt + ")", "Finished", "#D4C9B8" },
            { "● Reading (" + readCnt + ")", "Reading", "#D4C9B8" },
            { "● Unread (" + unrdCnt + ")", "Unread", "#8A857D" }
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
        hHdr.setTextColor(healthPct >= 85 ? 0xFFD4C9B8 : 0xFFD4C9B8);
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
                fMissCov == 0 ? 0xFFD4C9B8 : 0xFFC99A7E,
                fMissCov == 0 ? 0xFF282520 : 0xFF282520,
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
                fMissGen == 0 ? 0xFFD4C9B8 : 0xFFD4C9B8,
                fMissGen == 0 ? 0xFF282520 : 0xFF282520,
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
                fMissDesc == 0 ? 0xFFD4C9B8 : 0xFFD4C9B8,
                fMissDesc == 0 ? 0xFF282520 : 0xFF25282D,
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
        gHdr.setTextColor(0xFFD4C9B8);
        gHdr.setTextSize(11);
        gHdr.setTypeface(Typeface.DEFAULT_BOLD);
        gHdr.setPadding(0, 0, 0, dp(d, 8));
        content.addView(gHdr);

        int maxG = topGenres.isEmpty() ? 1 : Math.max(1, genreFreq.get(topGenres.get(0)));
        for (int i = 0; i < Math.min(5, topGenres.size()); i++) {
            String k = topGenres.get(i);
            final String gName = genreLabel.get(k);
            content.addView(makeInsightBarRow(ctx, d, gName, genreFreq.get(k), maxG, 0xFFD4C9B8,
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
        aHdr.setTextColor(0xFFD4C9B8);
        aHdr.setTextSize(11);
        aHdr.setTypeface(Typeface.DEFAULT_BOLD);
        aHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
        content.addView(aHdr);

        int maxA = topAuthors.isEmpty() ? 1 : Math.max(1, authorFreq.get(topAuthors.get(0)));
        for (int i = 0; i < Math.min(5, topAuthors.size()); i++) {
            String k = topAuthors.get(i);
            final String aName = authorLabel.get(k);
            content.addView(makeInsightBarRow(ctx, d, aName, authorFreq.get(k), maxA, 0xFFD4C9B8,
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
            sHdr.setTextColor(0xFFD4C9B8);
            sHdr.setTextSize(11);
            sHdr.setTypeface(Typeface.DEFAULT_BOLD);
            sHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(sHdr);

            int maxS = Math.max(1, shelfFreq.get(topShelves.get(0)));
            for (final String sh : topShelves) {
                content.addView(makeInsightBarRow(ctx, d, "" + sh, shelfFreq.get(sh), maxS, 0xFFD4C9B8,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "SHELF:" + sh);
                            }
                        }));
            }
        }

        // Section 3b: Publication Eras Bar Chart (tap any era to filter catalog)
        if (datedCount > 0) {
            TextView eHdr = new TextView(ctx);
            eHdr.setText("PUBLICATION ERAS · " + minYear + "–" + maxYear + " (TAP TO FILTER)");
            eHdr.setTextColor(0xFFD4C9B8);
            eHdr.setTextSize(11);
            eHdr.setTypeface(Typeface.DEFAULT_BOLD);
            eHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(eHdr);

            int maxE = 1;
            for (Integer ec : eraFreq.values()) {
                if (ec > maxE) maxE = ec;
            }
            for (java.util.Map.Entry<String, Integer> ee : eraFreq.entrySet()) {
                final String eraName = ee.getKey();
                int eCnt = ee.getValue();
                if (eCnt <= 0) continue;
                content.addView(makeInsightBarRow(ctx, d, "" + eraName, eCnt, maxE, 0xFFD4C9B8,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "ERA:" + eraName);
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
            pHdr.setTextColor(0xFFD4C9B8);
            pHdr.setTextSize(11);
            pHdr.setTypeface(Typeface.DEFAULT_BOLD);
            pHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(pHdr);

            int maxP = Math.max(1, pubFreq.get(topPubs.get(0)));
            for (int i = 0; i < Math.min(4, topPubs.size()); i++) {
                String k = topPubs.get(i);
                final String pName = pubLabel.get(k);
                content.addView(makeInsightBarRow(ctx, d, pName, pubFreq.get(k), maxP, 0xFFD4C9B8,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.showWithSearch(ctx, pName);
                            }
                        }));
            }
        }

        // Section 5: Star Ratings Distribution (when rated books exist)
        if (ratedCount > 0) {
            TextView rHdr = new TextView(ctx);
            rHdr.setText("STAR RATINGS · " + avgStr.toUpperCase(java.util.Locale.US) + " (TAP TO FILTER)");
            rHdr.setTextColor(0xFFD4C9B8);
            rHdr.setTextSize(11);
            rHdr.setTypeface(Typeface.DEFAULT_BOLD);
            rHdr.setPadding(0, dp(d, 14), 0, dp(d, 8));
            content.addView(rHdr);

            int maxR = 1;
            for (int s = 1; s <= 5; s++) if (starBuckets[s] > maxR) maxR = starBuckets[s];
            for (int s = 5; s >= 1; s--) {
                if (starBuckets[s] <= 0) continue;
                final int starTarget = s;
                StringBuilder stLbl = new StringBuilder();
                for (int k = 0; k < s; k++) stLbl.append("★");
                stLbl.append(" (").append(s).append("★)");
                content.addView(makeInsightBarRow(ctx, d, stLbl.toString(), starBuckets[s], maxR, 0xFFD4C9B8,
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                if (insightDlgRef[0] != null) insightDlgRef[0].dismiss();
                                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "STAR:" + starTarget);
                            }
                        }));
            }
        }

        sv.addView(content);
        root.addView(sv);

        // Build shareable Library Insights Snapshot text
        int goalTgt = getReadingGoalTarget(ctx);
        int goalPct = Math.min(100, Math.round((finCnt * 100f) / Math.max(1, goalTgt)));
        int yrNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        StringBuilder snap = new StringBuilder();
        snap.append("Azath Library Insights\n");
        snap.append("Books: ").append(all.size()).append(" cataloged · ")
            .append(readCnt).append(" reading · ").append(finCnt).append(" finished · ")
            .append(unrdCnt).append(" unread\n");
        snap.append(yrNow).append(" Goal: ").append(finCnt).append("/").append(goalTgt)
            .append(" (").append(goalPct).append("%)\n");
        snap.append("Curation: ").append(favs).append(" fav · ").append(noted).append(" noted · ")
            .append(shelved).append(" shelved · ")
            .append(com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countQueue(ctx)).append(" queued · ")
            .append(avgStr).append("\n");
        snap.append("Metadata Health: ").append(healthPct).append("% complete\n");
        if (datedCount > 0) {
            snap.append("Publication Span: ").append(minYear).append("–").append(maxYear).append("\n");
        }
        if (!topGenres.isEmpty()) {
            snap.append("Top Genres: ");
            for (int i = 0; i < Math.min(3, topGenres.size()); i++) {
                if (i > 0) snap.append(", ");
                String gk = topGenres.get(i);
                snap.append(genreLabel.get(gk)).append(" (").append(genreFreq.get(gk)).append(")");
            }
            snap.append("\n");
        }
        if (!topAuthors.isEmpty()) {
            snap.append("Top Authors: ");
            for (int i = 0; i < Math.min(3, topAuthors.size()); i++) {
                if (i > 0) snap.append(", ");
                String ak = topAuthors.get(i);
                snap.append(authorLabel.get(ak)).append(" (").append(authorFreq.get(ak)).append(")");
            }
            snap.append("\n");
        }
        snap.append("Reading Time (").append(userWpm).append(" WPM): ").append(totHrStr).append(" total · ").append(readHrStr).append(" read · ").append(remHrStr).append(" remaining\n");
        final String snapshotText = snap.toString().trim();

        insightDlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .setNeutralButton("Copy Summary", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                    ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Azath Library Insights", snapshotText));
                                Toast.makeText(ctx, "Copied Library Insights summary", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Throwable ignored) {}
                    }
                })
                .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_SUBJECT, "Azath Library Insights");
                            share.putExtra(Intent.EXTRA_TEXT, snapshotText);
                            ctx.startActivity(Intent.createChooser(share, "Share Library Insights"));
                        } catch (Throwable ignored) {}
                    }
                })
                .show();
        FolioTheme.applyToDialog(ctx, insightDlgRef[0]);
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
        lblTv.setTextColor(0xFFBDB8AE);
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
        trBg.setColor(0xFF25282D);
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
        final boolean[] curatedOnly = new boolean[] { true };
        final File initFile = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportReadingLogMarkdown(ctx, true);
        final String[] currentMd = new String[] {
                com.amazon.kindle.epub.ingest.FolioMetadataBackup.buildReadingLogMarkdown(ctx, true)
        };
        final float d = ctx.getResources().getDisplayMetrics().density;

        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(0xFF1C1E22);
        box.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        final TextView pathTv = new TextView(ctx);
        pathTv.setText(initFile != null
                ? "Saved: " + initFile.getAbsolutePath()
                : "Markdown preview ready");
        pathTv.setTextColor(0xFFD4C9B8);
        pathTv.setTextSize(11);
        pathTv.setTypeface(Typeface.DEFAULT_BOLD);
        pathTv.setPadding(0, 0, 0, dp(d, 8));
        box.addView(pathTv);

        LinearLayout topExportRow = new LinearLayout(ctx);
        topExportRow.setOrientation(LinearLayout.HORIZONTAL);
        topExportRow.setGravity(Gravity.CENTER_VERTICAL);
        topExportRow.setPadding(0, 0, 0, dp(d, 10));

        final TextView scopeBtn = new TextView(ctx);
        scopeBtn.setText("Scope: Curated Only");
        scopeBtn.setTextColor(0xFFD4C9B8);
        scopeBtn.setTextSize(11);
        scopeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        scopeBtn.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
        final GradientDrawable scopeBg = new GradientDrawable();
        scopeBg.setColor(0xFF282520);
        scopeBg.setCornerRadius(dp(d, 10));
        scopeBg.setStroke(dp(d, 1), 0x884DA6FF);
        scopeBtn.setBackground(scopeBg);
        topExportRow.addView(scopeBtn);

        View spacer = new View(ctx);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        topExportRow.addView(spacer);

        TextView csvBtn = new TextView(ctx);
        csvBtn.setText("CSV");
        csvBtn.setTextColor(0xFFD4C9B8);
        csvBtn.setTextSize(11);
        csvBtn.setTypeface(Typeface.DEFAULT_BOLD);
        csvBtn.setPadding(dp(d, 9), dp(d, 5), dp(d, 9), dp(d, 5));
        GradientDrawable csvBg = new GradientDrawable();
        csvBg.setColor(0xFF282520);
        csvBg.setCornerRadius(dp(d, 10));
        csvBg.setStroke(dp(d, 1), 0x664DA6FF);
        csvBtn.setBackground(csvBg);
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

        TextView htmlBtn = new TextView(ctx);
        htmlBtn.setText("HTML");
        htmlBtn.setTextColor(0xFFD4C9B8);
        htmlBtn.setTextSize(11);
        htmlBtn.setTypeface(Typeface.DEFAULT_BOLD);
        htmlBtn.setPadding(dp(d, 9), dp(d, 5), dp(d, 9), dp(d, 5));
        GradientDrawable htmlBg = new GradientDrawable();
        htmlBg.setColor(0xFF282520);
        htmlBg.setCornerRadius(dp(d, 10));
        htmlBg.setStroke(dp(d, 1), 0x664DA6FF);
        htmlBtn.setBackground(htmlBg);
        LinearLayout.LayoutParams htmlLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        htmlLp.leftMargin = dp(d, 6);
        htmlBtn.setLayoutParams(htmlLp);
        htmlBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                File htmlFile = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportLibraryHtml(ctx);
                if (htmlFile != null) {
                    pathTv.setText("Saved HTML: " + htmlFile.getAbsolutePath());
                    Toast.makeText(ctx, "Exported offline HTML catalog (" + htmlFile.getName() + ")", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "HTML export failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        topExportRow.addView(htmlBtn);
        box.addView(topExportRow);

        ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 330)));
        final TextView mdTv = new TextView(ctx);
        mdTv.setText(currentMd[0]);
        mdTv.setTextColor(0xFFECE8E0);
        mdTv.setTextSize(12);
        mdTv.setTypeface(Typeface.MONOSPACE);
        mdTv.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
        GradientDrawable mdBg = new GradientDrawable();
        mdBg.setColor(0xFF15171A);
        mdBg.setCornerRadius(dp(d, 10));
        mdBg.setStroke(dp(d, 1), 0xFF32353B);
        mdTv.setBackground(mdBg);
        sv.addView(mdTv);
        box.addView(sv);

        scopeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                curatedOnly[0] = !curatedOnly[0];
                currentMd[0] = com.amazon.kindle.epub.ingest.FolioMetadataBackup.buildReadingLogMarkdown(ctx, curatedOnly[0]);
                File saved = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportReadingLogMarkdown(ctx, curatedOnly[0]);
                mdTv.setText(currentMd[0]);
                if (saved != null) pathTv.setText("Saved: " + saved.getAbsolutePath());
                if (curatedOnly[0]) {
                    scopeBtn.setText("Scope: Curated Only");
                    scopeBtn.setTextColor(0xFFD4C9B8);
                    scopeBg.setColor(0xFF282520);
                    scopeBg.setStroke(dp(d, 1), 0x884DA6FF);
                } else {
                    scopeBtn.setText("Scope: All Books");
                    scopeBtn.setTextColor(0xFFD4C9B8);
                    scopeBg.setColor(0xFF282520);
                    scopeBg.setStroke(dp(d, 1), 0x884DA6FF);
                }
            }
        });

        AlertDialog logDlg = new AlertDialog.Builder(ctx)
                .setTitle("Reading Log (.md)")
                .setView(box)
                .setPositiveButton("Close", null)
                .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_SUBJECT, "Folio Reading Log");
                            share.putExtra(Intent.EXTRA_TEXT, currentMd[0]);
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
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Reading Log", currentMd[0]));
                                Toast.makeText(ctx, "Copied Reading Log Markdown to clipboard", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Throwable ignored) {}
                    }
                })
                .show();
        FolioTheme.applyToDialog(ctx, logDlg);
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
        sb.append("Azath — Offline EPUB & KFX Reader\n");
        sb.append("Version: Azath v18.7 KFX (Enhanced Typesetting)\n");
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
        sb.append("• Standalone $270 Cover Container (#0E0F11, 0% margin)\n");
        sb.append("• Native YellowJersey Tables ($278/$279/$269)\n");
        sb.append("• Proportional Column-Width Image Scaling ($422/$423)\n");
        sb.append("• Unicode NFC Polytonic Greek & Non-Latin Normalization\n");
        sb.append("• Native YellowJersey Lists ($277) with Hanging Indents\n");
        sb.append("• Enhanced Blockquote Spacing & Italic Typography\n");
        sb.append("• Native Pop-up Footnotes ($616/$617, $615/$281)\n");
        sb.append("• Rich Code Blocks (<pre>/<code>), <hr/> Rules & Inlines\n");
        sb.append("• Native $266 Anchor Links & $179 Hyperlink Navigation\n• Publisher CSS Fidelity (sans-serif, margins, border-bottom & tracking)\n");
        sb.append("• Simplified Web Book Browser & Auto-Ingest EPUB Pipeline\n\n");

        sb.append("Live Library & Storage Stats:\n");
        sb.append("• Indexed Library Books: ").append(indexedBooks).append("\n");
        sb.append("• Enriched Metadata Records: ").append(metaRecords).append("\n");
        sb.append("• Reading Progress: ").append(booksInProgress).append(" in progress · ").append(booksFinished).append(" finished\n");
        int favTotal = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countFavorites(ctx);
        int noteTotal = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countNotes(ctx);
        int queueTotal = com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.countQueue(ctx);
        int goalTarget = getReadingGoalTarget(ctx);
        sb.append("• Favorites & Notes: ").append(favTotal).append(" favorited · ").append(noteTotal).append(" annotated\n");
        sb.append("• Up Next & Goal: ").append(queueTotal).append(" queued · ").append(booksFinished).append("/").append(goalTarget).append(" annual goal\n");
        sb.append("• App Library Storage: ").append(String.format("%.1f", appLibMb)).append(" MB\n");
        sb.append("• Legacy /sdcard/kindle: ").append(String.format("%.1f", sdKindleMb)).append(" MB\n");
        sb.append("• Temp Open-Once Cache: ").append(String.format("%.1f", tempCacheMb)).append(" MB\n");
        sb.append("• Privacy Mode: Local-First (Zero Amazon Telemetry)\n");

        AlertDialog aboutDlg = new AlertDialog.Builder(ctx)
                .setTitle("About Azath")
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
        FolioTheme.applyToDialog(ctx, aboutDlg);
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
