package com.amazon.kindle.epub.ingest;

import android.widget.FrameLayout;
import com.amazon.kindle.epub.catalog.FolioIcons;
import com.amazon.kindle.epub.catalog.FolioTheme;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

/**
 * Dark-themed "Library Metadata & Genres" browser surfacing all rich metadata
 * stored in folio_metadata.db with:
 *   - Live search bar (title, author, genre, ISBN, publisher)
 *   - Sort mode cycling (Recent / Title A-Z / Author / Year)
 *   - Title-cased genre filter chips
 *   - One-tap "Sync & Enrich" button (backfills new books + repairs missing covers)
 *   - Book Details (ⓘ) popup with "Edit Metadata" form & "Read Now" launch
 */
public final class FolioMetadataCatalogDialog {
    private interface AuthorFilterCallback {
        void onFilterByAuthor(String author);
    }

    private static final String[] SORT_MODES = new String[] { "Recent", "Title", "Author", "Year", "Progress", "Favorites", "Rating", "Size", "Up Next" };

    private FolioMetadataCatalogDialog() {}

    public static void show(final Context ctx) {
        show(ctx, "All", null);
    }

    public static void show(final Context ctx, final String initialFilter) {
        show(ctx, initialFilter, null);
    }

    public static void showWithSearch(final Context ctx, final String initialQuery) {
        show(ctx, "All", initialQuery, null);
    }

    public static void show(final Context ctx, final String initialFilter, final Runnable onDismiss) {
        show(ctx, initialFilter, "", onDismiss);
    }

    public static void show(final Context ctx, final String initialFilter, final String initialQuery, final Runnable onDismiss) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final List<FolioMetadataDb.BookRecord> allBooks = new ArrayList<>();
        final List<String> genres = new ArrayList<>();

        final Map<String, Integer> progressMap = loadProgressMap(ctx);
        loadFromDb(ctx, allBooks, genres);

        FolioTheme.isLimestoneLight(ctx);
        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(FolioTheme.bgModal());
        root.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 12));

        // Top row: Eyebrow + Sort pill + Sync pill
        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView eyebrow = new TextView(ctx);
        eyebrow.setText("AZATH");
        eyebrow.setTextColor(FolioTheme.textMuted());
        eyebrow.setLetterSpacing(0.10f);
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(eyebrow);

        boolean savedGrid = false;
        try {
            savedGrid = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getBoolean("catalog_grid_mode", false);
        } catch (Throwable ignored) {}
        final boolean[] gridMode = new boolean[] { savedGrid };

        final TextView btnViewMode = new TextView(ctx);
        btnViewMode.setText(gridMode[0] ? "☰" : "⊞");
        btnViewMode.setTextColor((gridMode[0] ? FolioTheme.accent() : FolioTheme.textPrimary()));
        btnViewMode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnViewMode.setTypeface(Typeface.DEFAULT_BOLD);
        btnViewMode.setPadding(dp(ctx, 7), dp(ctx, 4), dp(ctx, 7), dp(ctx, 4));
        GradientDrawable vmBg = new GradientDrawable();
        vmBg.setColor(FolioTheme.bgControl());
        vmBg.setCornerRadius(dp(ctx, 14));
        vmBg.setStroke(dp(ctx, 1), FolioTheme.border());
        btnViewMode.setBackground(vmBg);
        LinearLayout.LayoutParams vmLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        vmLp.setMargins(0, 0, dp(ctx, 5), 0);
        btnViewMode.setLayoutParams(vmLp);
        topRow.addView(btnViewMode);

        final FrameLayout btnAuthors = FolioIcons.makeIconButton(
                ctx, FolioIcons.AUTHORS, 15, 6, FolioIcons.COLOR_ICON, FolioTheme.bgControl(), FolioTheme.border(), 8);
        LinearLayout.LayoutParams authLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        authLp.setMargins(0, 0, dp(ctx, 5), 0);
        btnAuthors.setLayoutParams(authLp);
        topRow.addView(btnAuthors);

        final FrameLayout btnRandom = FolioIcons.makeIconButton(
                ctx, FolioIcons.SHUFFLE, 15, 6, FolioIcons.COLOR_ICON, FolioTheme.bgControl(), FolioTheme.border(), 8);
        LinearLayout.LayoutParams randLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        randLp.setMargins(0, 0, dp(ctx, 5), 0);
        btnRandom.setLayoutParams(randLp);
        topRow.addView(btnRandom);

        final TextView btnSort = new TextView(ctx);
        btnSort.setText("Sort: Recent");
        btnSort.setTextColor(FolioTheme.textPrimary());
        btnSort.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSort.setTypeface(Typeface.DEFAULT_BOLD);
        btnSort.setPadding(dp(ctx, 9), dp(ctx, 5), dp(ctx, 9), dp(ctx, 5));
        GradientDrawable sortBg = new GradientDrawable();
        sortBg.setColor(FolioTheme.bgControl());
        sortBg.setCornerRadius(dp(ctx, 14));
        sortBg.setStroke(dp(ctx, 1), FolioTheme.border());
        btnSort.setBackground(sortBg);
        LinearLayout.LayoutParams sortLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sortLp.setMargins(0, 0, dp(ctx, 6), 0);
        btnSort.setLayoutParams(sortLp);
        topRow.addView(btnSort);

        final TextView btnSync = new TextView(ctx);
        btnSync.setText("Sync & Enrich");
        btnSync.setTextColor(FolioTheme.bgCanvas());
        btnSync.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSync.setTypeface(Typeface.DEFAULT_BOLD);
        btnSync.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable syncBg = new GradientDrawable();
        syncBg.setColor(FolioTheme.accent());
        syncBg.setCornerRadius(dp(ctx, 14));
        btnSync.setBackground(syncBg);
        topRow.addView(btnSync);
        root.addView(topRow);

        LinearLayout headingRow = new LinearLayout(ctx);
        headingRow.setOrientation(LinearLayout.HORIZONTAL);
        headingRow.setGravity(Gravity.CENTER_VERTICAL);
        headingRow.setPadding(0, dp(ctx, 4), 0, dp(ctx, 10));

        final TextView heading = new TextView(ctx);
        heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
        heading.setTextColor(FolioTheme.textPrimary());
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        heading.setTypeface(Typeface.SERIF, Typeface.BOLD);
        heading.setSingleLine(true);
        heading.setEllipsize(TextUtils.TruncateAt.END);
        heading.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headingRow.addView(heading);

        final boolean[] selectMode = new boolean[] { false };
        final java.util.Set<String> selectedPaths = new java.util.LinkedHashSet<>();

        final TextView btnSelect = new TextView(ctx);
        btnSelect.setText("Select");
        btnSelect.setTextColor(FolioTheme.textSecondary());
        btnSelect.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSelect.setTypeface(Typeface.DEFAULT_BOLD);
        btnSelect.setPadding(dp(ctx, 9), dp(ctx, 4), dp(ctx, 9), dp(ctx, 4));
        final GradientDrawable selBtnBg = new GradientDrawable();
        selBtnBg.setColor(FolioTheme.bgControl());
        selBtnBg.setCornerRadius(dp(ctx, 12));
        selBtnBg.setStroke(dp(ctx, 1), FolioTheme.border());
        btnSelect.setBackground(selBtnBg);
        LinearLayout.LayoutParams selLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        selLp.setMargins(dp(ctx, 6), 0, 0, 0);
        btnSelect.setLayoutParams(selLp);
        headingRow.addView(btnSelect);
        root.addView(headingRow);

        // Search box + inline Clear (✕) button
        LinearLayout searchRow = new LinearLayout(ctx);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(FolioTheme.bgSurface());
        searchBg.setCornerRadius(dp(ctx, 10));
        searchBg.setStroke(dp(ctx, 1), FolioTheme.border());
        searchRow.setBackground(searchBg);
        LinearLayout.LayoutParams searchRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchRowLp.setMargins(0, 0, 0, dp(ctx, 10));
        searchRow.setLayoutParams(searchRowLp);

        final EditText searchBox = new EditText(ctx);
        searchBox.setHint("Filter by title, author, genre, or ISBN...");
        searchBox.setHintTextColor(FolioTheme.textMuted());
        searchBox.setTextColor(FolioTheme.textPrimary());
        searchBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        searchBox.setSingleLine(true);
        searchBox.setBackgroundColor(Color.TRANSPARENT);
        searchBox.setPadding(dp(ctx, 12), dp(ctx, 9), dp(ctx, 6), dp(ctx, 9));
        searchBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        searchRow.addView(searchBox);

        final TextView btnClearSearch = new TextView(ctx);
        btnClearSearch.setText("✕");
        btnClearSearch.setTextColor(FolioTheme.textMuted());
        btnClearSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnClearSearch.setTypeface(Typeface.DEFAULT_BOLD);
        btnClearSearch.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(v -> searchBox.setText(""));
        searchRow.addView(btnClearSearch);

        root.addView(searchRow);

        // Genre filter chip strip
        HorizontalScrollView chipScroll = new HorizontalScrollView(ctx);
        chipScroll.setHorizontalScrollBarEnabled(false);
        final LinearLayout chipRow = new LinearLayout(ctx);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipScroll.addView(chipRow);
        root.addView(chipScroll);

        View divider = new View(ctx);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 1));
        divLp.setMargins(0, dp(ctx, 10), 0, dp(ctx, 10));
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(FolioTheme.bgControl());
        root.addView(divider);

        // Book cards container
        final ScrollView listScroll = new ScrollView(ctx);
        listScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 390)));
        final LinearLayout cardsContainer = new LinearLayout(ctx);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(cardsContainer);
        root.addView(listScroll);

        // Bulk Action Bar (shown only when Multi-Select Mode is active)
        final LinearLayout bulkBar = new LinearLayout(ctx);
        bulkBar.setOrientation(LinearLayout.VERTICAL);
        bulkBar.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        GradientDrawable bulkBg = new GradientDrawable();
        bulkBg.setColor(FolioTheme.bgSurface());
        bulkBg.setCornerRadius(dp(ctx, 10));
        bulkBg.setStroke(dp(ctx, 1), FolioTheme.border());
        bulkBar.setBackground(bulkBg);
        LinearLayout.LayoutParams bulkLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bulkLp.setMargins(0, dp(ctx, 8), 0, 0);
        bulkBar.setLayoutParams(bulkLp);
        bulkBar.setVisibility(View.GONE);

        LinearLayout bulkTopRow = new LinearLayout(ctx);
        bulkTopRow.setOrientation(LinearLayout.HORIZONTAL);
        bulkTopRow.setGravity(Gravity.CENTER_VERTICAL);
        bulkTopRow.setPadding(0, 0, 0, dp(ctx, 7));

        final TextView bulkCountTv = new TextView(ctx);
        bulkCountTv.setText("0 selected — tap books to select");
        bulkCountTv.setTextColor(FolioTheme.textPrimary());
        bulkCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        bulkCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        bulkCountTv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        bulkTopRow.addView(bulkCountTv);

        final TextView btnSelectAll = new TextView(ctx);
        btnSelectAll.setText("Select All");
        btnSelectAll.setTextColor(FolioTheme.accent());
        btnSelectAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        btnSelectAll.setTypeface(Typeface.DEFAULT_BOLD);
        btnSelectAll.setPadding(dp(ctx, 8), dp(ctx, 3), dp(ctx, 4), dp(ctx, 3));
        bulkTopRow.addView(btnSelectAll);
        bulkBar.addView(bulkTopRow);

        LinearLayout bulkBtnsRow = new LinearLayout(ctx);
        bulkBtnsRow.setOrientation(LinearLayout.HORIZONTAL);
        bulkBtnsRow.setGravity(Gravity.CENTER_VERTICAL);

        final TextView btnBulkShelf = makeBulkActionPill(ctx, "Shelf", "#282520", "#ECE8E0", "#524B41");
        final TextView btnBulkGenre = makeBulkActionPill(ctx, "+Genre", "#282520", "#D4C9B8", "#524B41");
        final TextView btnBulkQueue = makeBulkActionPill(ctx, "+Queue", "#282520", "#D4C9B8", "#524B41");
        final TextView btnBulkFav = makeBulkActionPill(ctx, "★Fav", "#282520", "#D4C9B8", "#524B41");
        final TextView btnBulkDone = makeBulkActionPill(ctx, "✓Done", "#282520", "#D4C9B8", "#524B41");
        bulkBtnsRow.addView(btnBulkShelf);
        bulkBtnsRow.addView(btnBulkGenre);
        bulkBtnsRow.addView(btnBulkQueue);
        bulkBtnsRow.addView(btnBulkFav);
        bulkBtnsRow.addView(btnBulkDone);
        bulkBar.addView(bulkBtnsRow);
        root.addView(bulkBar);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .create();
        if (onDismiss != null) {
            dialog.setOnDismissListener(d -> onDismiss.run());
        }

        final String[] selectedGenre = new String[] { (initialFilter != null && !initialFilter.trim().isEmpty()) ? initialFilter.trim() : "All" };
        final String initQ = initialQuery != null ? initialQuery.trim() : "";
        final String[] queryText = new String[] { initQ };
        int savedSort = 0;
        boolean savedRev = false;
        try {
            android.content.SharedPreferences uiP = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE);
            savedSort = uiP.getInt("catalog_sort_idx", 0);
            if (savedSort < 0 || savedSort >= SORT_MODES.length) savedSort = 0;
            savedRev = uiP.getBoolean("catalog_sort_rev", false);
        } catch (Throwable ignored) {}
        final int[] sortIdx = new int[] { savedSort };
        final boolean[] sortRev = new boolean[] { savedRev };

        final List<FolioMetadataDb.BookRecord> lastFiltered = new ArrayList<>();
        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
                btnSort.setText("Sort: " + SORT_MODES[sortIdx[0]] + (sortRev[0] ? " ↑" : " ↓"));

                int readingCount = 0;
                int finishedCount = 0;
                int unreadCount = 0;
                int favCount = 0;
                int notesCount = 0;
                int queueCount = 0;
                final Map<String, Integer> shelfCounts = new LinkedHashMap<>();
                for (FolioMetadataDb.BookRecord rec : allBooks) {
                    int p = getProgress(progressMap, rec.kfxPath);
                    if (p >= 100) finishedCount++;
                    else if (p > 0) readingCount++;
                    else unreadCount++;
                    if (isFavorite(ctx, rec.kfxPath)) favCount++;
                    if (isInQueue(ctx, rec.kfxPath)) queueCount++;
                    if (!getNote(ctx, rec.kfxPath).isEmpty()) notesCount++;
                    String sh = getShelf(ctx, rec.kfxPath);
                    if (!sh.isEmpty()) {
                        shelfCounts.put(sh, (shelfCounts.containsKey(sh) ? shelfCounts.get(sh) : 0) + 1);
                    }
                }
                List<String> chipList = new ArrayList<>();
                chipList.add("All");
                if ("MISSING_COVER".equalsIgnoreCase(selectedGenre[0])
                        || "MISSING_GENRES".equalsIgnoreCase(selectedGenre[0])
                        || "MISSING_SYNOPSIS".equalsIgnoreCase(selectedGenre[0])
                        || selectedGenre[0].startsWith("ERA:")
                        || selectedGenre[0].startsWith("STAR:")) {
                    chipList.add(selectedGenre[0]);
                }
                if (queueCount > 0) chipList.add("Queue");
                if (favCount > 0) chipList.add("Favorites");
                if (notesCount > 0) chipList.add("Notes");
                for (String shName : shelfCounts.keySet()) chipList.add("SHELF:" + shName);
                if (readingCount > 0) chipList.add("Reading");
                if (finishedCount > 0) chipList.add("Finished");
                if (unreadCount > 0) chipList.add("Unread");
                for (String g : genres) {
                    if (!"All".equalsIgnoreCase(g)) chipList.add(g);
                }

                chipRow.removeAllViews();
                for (final String g : chipList) {
                    boolean active = g.equalsIgnoreCase(selectedGenre[0]);
                    boolean isQueueChip = "Queue".equalsIgnoreCase(g);
                    boolean isFavChip = "Favorites".equalsIgnoreCase(g);
                    boolean isNotesChip = "Notes".equalsIgnoreCase(g);
                    boolean isShelfChip = g.startsWith("SHELF:");
                    boolean isEraChip = g.startsWith("ERA:");
                    boolean isStarChip = g.startsWith("STAR:");
                    boolean isAuditChip = "MISSING_COVER".equalsIgnoreCase(g)
                            || "MISSING_GENRES".equalsIgnoreCase(g)
                            || "MISSING_SYNOPSIS".equalsIgnoreCase(g);
                    boolean isStatusChip = "Reading".equalsIgnoreCase(g)
                            || "Finished".equalsIgnoreCase(g)
                            || "Unread".equalsIgnoreCase(g);
                    String labelText = isQueueChip ? "Up Next" : (isStarChip ? (g.substring(5) + "-Star") : (isEraChip ? g.substring(4) : (isShelfChip ? g.substring(6)
                            : ("MISSING_COVER".equalsIgnoreCase(g) ? "Missing Cover"
                            : ("MISSING_GENRES".equalsIgnoreCase(g) ? "Missing Genres"
                            : ("MISSING_SYNOPSIS".equalsIgnoreCase(g) ? "Missing Synopsis" : g))))));
                    int gCount;
                    if ("All".equalsIgnoreCase(g)) gCount = allBooks.size();
                    else if (isQueueChip) gCount = queueCount;
                    else if (isFavChip) gCount = favCount;
                    else if (isNotesChip) gCount = notesCount;
                    else if (isShelfChip) gCount = shelfCounts.containsKey(labelText) ? shelfCounts.get(labelText) : 0;
                    else if (isAuditChip) gCount = countAuditMatches(allBooks, g);
                    else if (isEraChip) gCount = countEraMatches(allBooks, g.substring(4));
                    else if (isStarChip) gCount = countStarMatches(ctx, allBooks, g.substring(5));
                    else if ("Reading".equalsIgnoreCase(g)) gCount = readingCount;
                    else if ("Finished".equalsIgnoreCase(g)) gCount = finishedCount;
                    else if ("Unread".equalsIgnoreCase(g)) gCount = unreadCount;
                    else gCount = countBooksInGenre(allBooks, g);

                    TextView chip = new TextView(ctx);
                    String chipPrefix = isStarChip ? "★ " : "";
                    chip.setText(chipPrefix + labelText + " (" + gCount + ")");
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    chip.setTextColor((active ? FolioTheme.accentSoftText() : FolioTheme.textSecondary()));
                    chip.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor((active ? FolioTheme.accentFill() : FolioTheme.bgControl()));
                    bg.setCornerRadius(dp(ctx, 999));
                    bg.setStroke(dp(ctx, 1), (active ? FolioTheme.accentBorder() : FolioTheme.border()));
                    chip.setBackground(bg);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, dp(ctx, 8), 0);
                    chip.setLayoutParams(lp);
                    chip.setOnClickListener(v -> {
                        selectedGenre[0] = g;
                        renderRef[0].run();
                    });
                    chipRow.addView(chip);
                }

                List<FolioMetadataDb.BookRecord> filtered = new ArrayList<>();
                String q = queryText[0].trim().toLowerCase(Locale.ROOT);
                for (FolioMetadataDb.BookRecord rec : allBooks) {
                    if ("Queue".equalsIgnoreCase(selectedGenre[0])) {
                        if (!isInQueue(ctx, rec.kfxPath)) continue;
                    } else if ("Favorites".equalsIgnoreCase(selectedGenre[0])) {
                        if (!isFavorite(ctx, rec.kfxPath)) continue;
                    } else if ("Notes".equalsIgnoreCase(selectedGenre[0])) {
                        if (getNote(ctx, rec.kfxPath).isEmpty()) continue;
                    } else if (selectedGenre[0].startsWith("SHELF:")) {
                        String targetShelf = selectedGenre[0].substring(6);
                        if (!targetShelf.equalsIgnoreCase(getShelf(ctx, rec.kfxPath))) continue;
                    } else if ("Reading".equalsIgnoreCase(selectedGenre[0])) {
                        int p = getProgress(progressMap, rec.kfxPath);
                        if (p <= 0 || p >= 100) continue;
                    } else if ("Finished".equalsIgnoreCase(selectedGenre[0])) {
                        if (getProgress(progressMap, rec.kfxPath) < 100) continue;
                    } else if ("Unread".equalsIgnoreCase(selectedGenre[0])) {
                        if (getProgress(progressMap, rec.kfxPath) > 0) continue;
                    } else if (selectedGenre[0].startsWith("STAR:")) {
                        if (!matchesStarFilter(ctx, rec, selectedGenre[0].substring(5))) continue;
                    } else if (selectedGenre[0].startsWith("ERA:")) {
                        if (!matchesEraFilter(rec, selectedGenre[0].substring(4))) continue;
                    } else if ("MISSING_COVER".equalsIgnoreCase(selectedGenre[0])
                            || "MISSING_GENRES".equalsIgnoreCase(selectedGenre[0])
                            || "MISSING_SYNOPSIS".equalsIgnoreCase(selectedGenre[0])) {
                        if (!matchesAuditFilter(rec, selectedGenre[0])) continue;
                    } else {
                        if (!rec.hasGenre(selectedGenre[0])) continue;
                    }
                    if (!q.isEmpty() && !matchesQuery(ctx, rec, q)) continue;
                    filtered.add(rec);
                }

                boolean isFiltered = !"All".equalsIgnoreCase(selectedGenre[0]) || !q.isEmpty();
                heading.setText(isFiltered
                        ? "Library Metadata & Genres (" + filtered.size() + " of " + allBooks.size() + ")"
                        : "Library Metadata & Genres (" + allBooks.size() + ")");

                sortBooks(ctx, filtered, sortIdx[0], sortRev[0], progressMap);
                lastFiltered.clear();
                lastFiltered.addAll(filtered);

                final AuthorFilterCallback onAuthorFilter = author -> {
                    selectedGenre[0] = "All";
                    searchBox.setText(author);
                    searchBox.setSelection(author.length());
                };
                final Runnable onRefreshAll = () -> {
                    loadFromDb(ctx, allBooks, genres);
                    progressMap.clear();
                    progressMap.putAll(loadProgressMap(ctx));
                    renderRef[0].run();
                };
                cardsContainer.removeAllViews();
                if (gridMode[0]) {
                    for (int i = 0; i < filtered.size(); i += 3) {
                        LinearLayout gRow = new LinearLayout(ctx);
                        gRow.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams grLp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        grLp.setMargins(0, 0, 0, dp(ctx, 10));
                        gRow.setLayoutParams(grLp);
                        for (int c = 0; c < 3; c++) {
                            if (i + c < filtered.size()) {
                                final FolioMetadataDb.BookRecord gRec = filtered.get(i + c);
                                View gTile = buildGridTile(ctx, gRec, progressMap,
                                        dialog, onRefreshAll, onAuthorFilter, c < 2);
                                if (selectMode[0]) {
                                    applyBulkSelectionUi(ctx, gTile, gRec, selectedPaths, bulkCountTv, btnSelectAll, lastFiltered, renderRef[0]);
                                }
                                gRow.addView(gTile);
                            } else {
                                View spacer = new View(ctx);
                                LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(0, 1, 1f);
                                if (c < 2) spLp.setMargins(0, 0, dp(ctx, 8), 0);
                                spacer.setLayoutParams(spLp);
                                gRow.addView(spacer);
                            }
                        }
                        cardsContainer.addView(gRow);
                    }
                } else {
                    for (final FolioMetadataDb.BookRecord rec : filtered) {
                        View cardV = buildBookCard(ctx, rec, progressMap, dialog, onRefreshAll, onAuthorFilter);
                        if (selectMode[0]) {
                            applyBulkSelectionUi(ctx, cardV, rec, selectedPaths, bulkCountTv, btnSelectAll, lastFiltered, renderRef[0]);
                        }
                        cardsContainer.addView(cardV);
                    }
                }

                if (filtered.isEmpty()) {
                    TextView empty = new TextView(ctx);
                    empty.setText(allBooks.isEmpty()
                            ? "No books indexed yet. Tap \"Sync & Enrich\" above to index your Folio library."
                            : "No books match your current filter.");
                    empty.setTextColor(FolioTheme.textMuted());
                    empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    empty.setPadding(dp(ctx, 8), dp(ctx, 20), dp(ctx, 8), dp(ctx, 20));
                    cardsContainer.addView(empty);
                }
            }
        };

        btnViewMode.setOnClickListener(v -> {
            gridMode[0] = !gridMode[0];
            btnViewMode.setText(gridMode[0] ? "☰" : "⊞");
            btnViewMode.setTextColor((gridMode[0] ? FolioTheme.accent() : FolioTheme.textPrimary()));
            try {
                ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("catalog_grid_mode", gridMode[0]).apply();
            } catch (Throwable ignored) {}
            renderRef[0].run();
        });

        btnAuthors.setOnClickListener(v -> showAuthorsDirectoryDialog(ctx, allBooks, author -> {
            selectedGenre[0] = "All";
            searchBox.setText(author);
            searchBox.setSelection(author.length());
        }));

        btnRandom.setOnClickListener(v -> {
            List<FolioMetadataDb.BookRecord> pool = new ArrayList<>();
            for (FolioMetadataDb.BookRecord r : lastFiltered) {
                if (getProgress(progressMap, r.kfxPath) == 0) pool.add(r);
            }
            if (pool.isEmpty()) pool.addAll(lastFiltered);
            if (pool.isEmpty()) {
                Toast.makeText(ctx, "No books in current filter to pick from", Toast.LENGTH_SHORT).show();
                return;
            }
            FolioMetadataDb.BookRecord pick = pool.get(new java.util.Random().nextInt(pool.size()));
            Toast.makeText(ctx, "Picked for you: " + pick.title, Toast.LENGTH_SHORT).show();
            showBookDetailDialog(ctx, pick, getProgress(progressMap, pick.kfxPath), dialog,
                    () -> {
                        loadFromDb(ctx, allBooks, genres);
                        progressMap.clear();
                        progressMap.putAll(loadProgressMap(ctx));
                        renderRef[0].run();
                    },
                    author -> {
                        selectedGenre[0] = "All";
                        searchBox.setText(author);
                        searchBox.setSelection(author.length());
                    });
        });

        btnSort.setOnClickListener(v -> {
            showSortPickerDialog(ctx, sortIdx[0], sortRev[0], (newIdx, newRev) -> {
                sortIdx[0] = newIdx;
                sortRev[0] = newRev;
                try {
                    ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("catalog_sort_idx", sortIdx[0])
                            .putBoolean("catalog_sort_rev", sortRev[0])
                            .apply();
                } catch (Throwable ignored) {}
                renderRef[0].run();
            });
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                queryText[0] = s != null ? s.toString() : "";
                btnClearSearch.setVisibility(queryText[0].trim().isEmpty() ? View.GONE : View.VISIBLE);
                renderRef[0].run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSync.setOnClickListener(v -> {
            btnSync.setEnabled(false);
            btnSync.setText("Syncing...");
            LibraryMetadataBackfiller.syncAllAsync(ctx, new LibraryMetadataBackfiller.ProgressListener() {
                @Override
                public void onProgress(final int done, final int total, final String currentTitle) {
                    mainHandler.post(() -> btnSync.setText("Sync " + done + "/" + total));
                }

                @Override
                public void onFinished(final int newlyAdded, final int totalInDb) {
                    mainHandler.post(() -> {
                        btnSync.setEnabled(true);
                        btnSync.setText("Sync & Enrich");
                        loadFromDb(ctx, allBooks, genres);
                        renderRef[0].run();
                        Toast.makeText(ctx,
                                newlyAdded > 0
                                        ? "Updated " + newlyAdded + " books (" + totalInDb + " total)"
                                        : "All " + totalInDb + " library books are up to date",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        if (!initQ.isEmpty()) {
            searchBox.setText(initQ);
            searchBox.setSelection(initQ.length());
            btnClearSearch.setVisibility(View.VISIBLE);
        }
        final Runnable updateBulkBarHeader = () -> {
            int nSel = selectedPaths.size();
            bulkCountTv.setText(nSel == 0
                    ? "0 selected — tap books to select"
                    : nSel + (nSel == 1 ? " book selected" : " books selected"));
            boolean allVisSelected = !lastFiltered.isEmpty() && nSel >= lastFiltered.size();
            btnSelectAll.setText(allVisSelected ? "Clear All" : "Select All (" + lastFiltered.size() + ")");
        };

        btnSelect.setOnClickListener(v -> {
            selectMode[0] = !selectMode[0];
            selectedPaths.clear();
            if (selectMode[0]) {
                btnSelect.setText("✕ Done");
                btnSelect.setTextColor(FolioTheme.textPrimary());
                selBtnBg.setColor(FolioTheme.accentBorder());
                selBtnBg.setStroke(dp(ctx, 1), FolioTheme.accent());
                bulkBar.setVisibility(View.VISIBLE);
                listScroll.getLayoutParams().height = dp(ctx, 316);
                listScroll.requestLayout();
                updateBulkBarHeader.run();
            } else {
                btnSelect.setText("Select");
                btnSelect.setTextColor(FolioTheme.textSecondary());
                selBtnBg.setColor(FolioTheme.bgControl());
                selBtnBg.setStroke(dp(ctx, 1), FolioTheme.border());
                bulkBar.setVisibility(View.GONE);
                listScroll.getLayoutParams().height = dp(ctx, 390);
                listScroll.requestLayout();
            }
            renderRef[0].run();
        });

        btnSelectAll.setOnClickListener(v -> {
            if (!lastFiltered.isEmpty() && selectedPaths.size() >= lastFiltered.size()) {
                selectedPaths.clear();
            } else {
                for (FolioMetadataDb.BookRecord r : lastFiltered) {
                    if (r.kfxPath != null && !r.kfxPath.isEmpty()) selectedPaths.add(r.kfxPath);
                }
            }
            updateBulkBarHeader.run();
            renderRef[0].run();
        });

        btnBulkShelf.setOnClickListener(v -> {
            if (selectedPaths.isEmpty()) {
                Toast.makeText(ctx, "Select at least one book first", Toast.LENGTH_SHORT).show();
                return;
            }
            showBulkShelfPicker(ctx, new ArrayList<>(selectedPaths), () -> {
                selectedPaths.clear();
                updateBulkBarHeader.run();
                loadFromDb(ctx, allBooks, genres);
                progressMap.clear();
                progressMap.putAll(loadProgressMap(ctx));
                renderRef[0].run();
            });
        });

        btnBulkGenre.setOnClickListener(v -> {
            if (selectedPaths.isEmpty()) {
                Toast.makeText(ctx, "Select at least one book first", Toast.LENGTH_SHORT).show();
                return;
            }
            List<FolioMetadataDb.BookRecord> selRecs = new ArrayList<>();
            for (FolioMetadataDb.BookRecord r : allBooks) {
                if (r.kfxPath != null && selectedPaths.contains(r.kfxPath)) selRecs.add(r);
            }
            if (selRecs.isEmpty()) return;
            showBulkGenrePicker(ctx, selRecs, genres, () -> {
                selectedPaths.clear();
                updateBulkBarHeader.run();
                loadFromDb(ctx, allBooks, genres);
                progressMap.clear();
                progressMap.putAll(loadProgressMap(ctx));
                renderRef[0].run();
            });
        });

        btnBulkQueue.setOnClickListener(v -> {
            if (selectedPaths.isEmpty()) {
                Toast.makeText(ctx, "Select at least one book first", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allQueued = true;
            for (String p : selectedPaths) {
                if (!isInQueue(ctx, p)) { allQueued = false; break; }
            }
            boolean nextQ = !allQueued;
            int count = 0;
            for (String p : selectedPaths) {
                setInQueue(ctx, p, nextQ);
                count++;
            }
            Toast.makeText(ctx,
                    (nextQ ? "Queued " : "Removed ") + count
                            + (count == 1 ? " book" : " books")
                            + (nextQ ? " in Up Next" : " from Up Next"),
                    Toast.LENGTH_SHORT).show();
            selectedPaths.clear();
            updateBulkBarHeader.run();
            renderRef[0].run();
        });

        btnBulkFav.setOnClickListener(v -> {
            if (selectedPaths.isEmpty()) {
                Toast.makeText(ctx, "Select at least one book first", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allFav = true;
            for (String p : selectedPaths) {
                if (!isFavorite(ctx, p)) { allFav = false; break; }
            }
            boolean nextFav = !allFav;
            int count = 0;
            for (String p : selectedPaths) {
                setFavorite(ctx, p, nextFav);
                count++;
            }
            Toast.makeText(ctx,
                    (nextFav ? "★ Favorited " : "☆ Unfavorited ") + count + (count == 1 ? " book" : " books"),
                    Toast.LENGTH_SHORT).show();
            selectedPaths.clear();
            updateBulkBarHeader.run();
            renderRef[0].run();
        });

        btnBulkDone.setOnClickListener(v -> {
            if (selectedPaths.isEmpty()) {
                Toast.makeText(ctx, "Select at least one book first", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allDone = true;
            for (String p : selectedPaths) {
                if (getProgress(progressMap, p) < 100) { allDone = false; break; }
            }
            boolean nextDone = !allDone;
            int count = 0;
            for (String p : selectedPaths) {
                setBookReadState(ctx, p, nextDone);
                count++;
            }
            Toast.makeText(ctx,
                    (nextDone ? "✓ Marked " : "↺ Marked ") + count
                            + (count == 1 ? " book " : " books ") + (nextDone ? "Finished" : "Unread"),
                    Toast.LENGTH_SHORT).show();
            selectedPaths.clear();
            updateBulkBarHeader.run();
            progressMap.clear();
            progressMap.putAll(loadProgressMap(ctx));
            renderRef[0].run();
        });

        renderRef[0].run();
        FolioTheme.applyToTree(ctx, root);
        dialog.show();
        FolioTheme.styleDialog(ctx, dialog);
    }

    private interface SortPickCallback {
        void onPicked(int sortIndex, boolean reversed);
    }

    private static void showSortPickerDialog(
            final Context ctx, final int curIdx, final boolean curRev, final SortPickCallback cb) {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(FolioTheme.bgModal());
        root.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 12));

        LinearLayout hdrRow = new LinearLayout(ctx);
        hdrRow.setOrientation(LinearLayout.HORIZONTAL);
        hdrRow.setGravity(Gravity.CENTER_VERTICAL);
        hdrRow.setPadding(0, 0, 0, dp(ctx, 12));

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Sort Catalog");
        titleTv.setTextColor(FolioTheme.textPrimary());
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        hdrRow.addView(titleTv);

        final boolean[] revState = new boolean[] { curRev };
        final TextView dirPill = new TextView(ctx);
        dirPill.setText(revState[0] ? "↑ Reversed" : "↓ Standard");
        dirPill.setTextColor((revState[0] ? FolioTheme.accent() : FolioTheme.accent()));
        dirPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        dirPill.setTypeface(Typeface.DEFAULT_BOLD);
        dirPill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        final GradientDrawable dBg = new GradientDrawable();
        dBg.setColor((revState[0] ? FolioTheme.accentFill() : FolioTheme.accentFill()));
        dBg.setCornerRadius(dp(ctx, 12));
        dBg.setStroke(dp(ctx, 1), (revState[0] ? FolioTheme.accentBorder() : FolioTheme.accentBorder()));
        dirPill.setBackground(dBg);
        hdrRow.addView(dirPill);
        root.addView(hdrRow);

        final AlertDialog[] dlgRef = new AlertDialog[1];
        dirPill.setOnClickListener(v -> {
            revState[0] = !revState[0];
            dirPill.setText(revState[0] ? "↑ Reversed" : "↓ Standard");
            dirPill.setTextColor((revState[0] ? FolioTheme.accent() : FolioTheme.accent()));
            dBg.setColor((revState[0] ? FolioTheme.accentFill() : FolioTheme.accentFill()));
            dBg.setStroke(dp(ctx, 1), (revState[0] ? FolioTheme.accentBorder() : FolioTheme.accentBorder()));
            cb.onPicked(curIdx, revState[0]);
        });

        String[] descLabels = new String[] {
            "Recently Added", "Title (A–Z)", "Author (A–Z)", "Publication Year",
            "Reading Progress", "Favorites First", "Star Rating", "File Size (Largest)", "Up Next Queue First"
        };

        ScrollView sv = new ScrollView(ctx);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout listBox = new LinearLayout(ctx);
        listBox.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < SORT_MODES.length; i++) {
            final int idx = i;
            boolean active = (i == curIdx);
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
            GradientDrawable rBg = new GradientDrawable();
            rBg.setColor((active ? FolioTheme.bgControl() : FolioTheme.bgSurface()));
            rBg.setCornerRadius(dp(ctx, 9));
            rBg.setStroke(dp(ctx, 1), (active ? FolioTheme.accent() : FolioTheme.border()));
            row.setBackground(rBg);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rLp.setMargins(0, 0, 0, dp(ctx, 6));
            row.setLayoutParams(rLp);

            TextView lbl = new TextView(ctx);
            lbl.setText(descLabels[i]);
            lbl.setTextColor((active ? FolioTheme.accent() : FolioTheme.textPrimary()));
            lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            lbl.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            lbl.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(lbl);

            if (active) {
                TextView chk = new TextView(ctx);
                chk.setText("✓");
                chk.setTextColor(FolioTheme.accent());
                chk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                chk.setTypeface(Typeface.DEFAULT_BOLD);
                row.addView(chk);
            }

            row.setOnClickListener(v -> {
                if (dlgRef[0] != null) dlgRef[0].dismiss();
                cb.onPicked(idx, revState[0]);
            });
            listBox.addView(row);
        }
        sv.addView(listBox);
        root.addView(sv);

        dlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .show();
        FolioTheme.applyToDialog(ctx, dlgRef[0]);
    }

    private static void sortBooks(final Context ctx, List<FolioMetadataDb.BookRecord> list, final int mode, final boolean reversed, final Map<String, Integer> progressMap) {
        final Map<String, Long> sizeCache = new HashMap<>();
        if (mode == 7) {
            for (FolioMetadataDb.BookRecord r : list) {
                if (r.kfxPath != null && !r.kfxPath.isEmpty()) {
                    File f = new File(r.kfxPath);
                    sizeCache.put(r.kfxPath, f.exists() ? f.length() : 0L);
                }
            }
        }
        Collections.sort(list, new Comparator<FolioMetadataDb.BookRecord>() {
            @Override
            public int compare(FolioMetadataDb.BookRecord a, FolioMetadataDb.BookRecord b) {
                if (mode == 1) { // Title A-Z
                    return safeLower(a.title).compareTo(safeLower(b.title));
                } else if (mode == 2) { // Author A-Z
                    return safeLower(a.author).compareTo(safeLower(b.author));
                } else if (mode == 3) { // Year (oldest to newest, blank last)
                    int ya = parseYear(a.pubDate);
                    int yb = parseYear(b.pubDate);
                    if (ya == yb) return safeLower(a.title).compareTo(safeLower(b.title));
                    return Integer.compare(ya, yb);
                } else if (mode == 4) { // Progress (highest % first)
                    int pa = getProgress(progressMap, a.kfxPath);
                    int pb = getProgress(progressMap, b.kfxPath);
                    if (pa != pb) return Integer.compare(pb, pa);
                    return safeLower(a.title).compareTo(safeLower(b.title));
                } else if (mode == 5) { // Favorites first
                    boolean fa = isFavorite(ctx, a.kfxPath);
                    boolean fb = isFavorite(ctx, b.kfxPath);
                    if (fa != fb) return fa ? -1 : 1;
                    return Long.compare(b.addedAt, a.addedAt);
                } else if (mode == 6) { // Highest star rating first
                    int ra = getRating(ctx, a.kfxPath);
                    int rb = getRating(ctx, b.kfxPath);
                    if (ra != rb) return Integer.compare(rb, ra);
                    boolean fa = isFavorite(ctx, a.kfxPath);
                    boolean fb = isFavorite(ctx, b.kfxPath);
                    if (fa != fb) return fa ? -1 : 1;
                    return Long.compare(b.addedAt, a.addedAt);
                } else if (mode == 7) { // File Size (largest first)
                    long sa = (a.kfxPath != null && sizeCache.containsKey(a.kfxPath)) ? sizeCache.get(a.kfxPath) : 0L;
                    long sb = (b.kfxPath != null && sizeCache.containsKey(b.kfxPath)) ? sizeCache.get(b.kfxPath) : 0L;
                    if (sa != sb) return Long.compare(sb, sa);
                    return safeLower(a.title).compareTo(safeLower(b.title));
                } else if (mode == 8) { // Up Next Queue first (FIFO)
                    long qa = getQueueTimestamp(ctx, a.kfxPath);
                    long qb = getQueueTimestamp(ctx, b.kfxPath);
                    boolean ha = qa > 0L, hb = qb > 0L;
                    if (ha != hb) return ha ? -1 : 1;
                    if (ha) return Long.compare(qa, qb);
                    return Long.compare(b.addedAt, a.addedAt);
                }
                // Default 0: Recent (descending addedAt)
                return Long.compare(b.addedAt, a.addedAt);
            }
        });
        if (reversed) {
            Collections.reverse(list);
        }
    }

    private static int parseYear(String pubDate) {
        if (pubDate == null || pubDate.trim().isEmpty()) return 9999;
        String digits = pubDate.replaceAll("[^0-9]", "");
        if (digits.length() >= 4) {
            try { return Integer.parseInt(digits.substring(0, 4)); } catch (Throwable ignored) {}
        }
        return 9999;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static void loadFromDb(Context ctx, List<FolioMetadataDb.BookRecord> outBooks, List<String> outGenres) {
        outBooks.clear();
        outGenres.clear();
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            outBooks.addAll(db.getAllBooks());
        } finally {
            db.close();
        }
        final Map<String, Integer> freq = new HashMap<>();
        final Map<String, String> display = new HashMap<>();
        for (FolioMetadataDb.BookRecord r : outBooks) {
            List<String> gList = r.genreList();
            if (gList.isEmpty()) continue;
            Set<String> seenInBook = new LinkedHashSet<>();
            for (String g : gList) {
                String c = g.trim();
                if (c.length() >= 3 && c.length() <= 22
                        && !c.contains("--")
                        && !EpubMetadataExtractor.isNoiseTag(c)
                        && !isCatalogMetaTag(c)) {
                    String key = c.toLowerCase(Locale.ROOT);
                    if (seenInBook.add(key)) {
                        freq.put(key, (freq.containsKey(key) ? freq.get(key) : 0) + 1);
                        if (!display.containsKey(key)) display.put(key, toTitleCase(c));
                    }
                }
            }
        }
        List<String> sortedKeys = new ArrayList<>(freq.keySet());
        Collections.sort(sortedKeys, (a, b) -> {
            int ca = freq.get(a);
            int cb = freq.get(b);
            if (ca != cb) return Integer.compare(cb, ca);
            return a.compareTo(b);
        });
        outGenres.add("All");
        for (int i = 0; i < sortedKeys.size(); i++) {
            outGenres.add(display.get(sortedKeys.get(i)));
        }
    }

    private static boolean matchesQuery(Context ctx, FolioMetadataDb.BookRecord r, String q) {
        return contains(r.title, q) || contains(r.author, q)
                || contains(formatAuthorForDisplay(r.author), q)
                || contains(r.genres, q)
                || contains(r.publisher, q) || contains(r.isbn, q) || contains(r.pubDate, q)
                || contains(getNote(ctx, r.kfxPath), q)
                || contains(getShelf(ctx, r.kfxPath), q);
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return "";
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) sb.append(w.substring(1));
        }
        return sb.toString();
    }

    private static View buildGridTile(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                      final Map<String, Integer> progressMap,
                                      final AlertDialog parentDialog, final Runnable onMetadataEdited,
                                      final AuthorFilterCallback onAuthorFilter, boolean addRightGap) {
        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(ctx, 6), dp(ctx, 6), dp(ctx, 6), dp(ctx, 8));
        GradientDrawable tBg = new GradientDrawable();
        tBg.setColor(FolioTheme.bgSurface());
        tBg.setCornerRadius(dp(ctx, 10));
        tBg.setStroke(dp(ctx, 1), FolioTheme.border());
        tile.setBackground(tBg);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (addRightGap) tLp.setMargins(0, 0, dp(ctx, 8), 0);
        tile.setLayoutParams(tLp);

        ImageView cover = new ImageView(ctx);
        cover.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 132)));
        cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cover.setBackgroundColor(FolioTheme.bgModal());
        if (rec.coverPath != null && !rec.coverPath.isEmpty()) {
            File cf = new File(rec.coverPath);
            if (cf.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(cf.getAbsolutePath(), opts);
                if (bm != null) cover.setImageBitmap(bm);
            }
        }
        tile.addView(cover);

        TextView tTv = new TextView(ctx);
        String pref = isFavorite(ctx, rec.kfxPath) ? "★ " : "";
        tTv.setText(pref + rec.title);
        tTv.setTextColor(FolioTheme.textPrimary());
        tTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tTv.setTypeface(Typeface.DEFAULT_BOLD);
        tTv.setMinLines(2);
        tTv.setMaxLines(2);
        tTv.setEllipsize(TextUtils.TruncateAt.END);
        tTv.setPadding(0, dp(ctx, 6), 0, dp(ctx, 2));

        int pct = getProgress(progressMap, rec.kfxPath);
        LinearLayout miniTrack = new LinearLayout(ctx);
        miniTrack.setOrientation(LinearLayout.HORIZONTAL);
        miniTrack.setBackgroundColor(FolioTheme.bgControl());
        LinearLayout.LayoutParams mtLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 3));
        mtLp.setMargins(0, dp(ctx, 4), 0, 0);
        miniTrack.setLayoutParams(mtLp);
        if (pct > 0) {
            float ratio = Math.max(0.08f, Math.min(1f, pct / 100f));
            View mFill = new View(ctx);
            mFill.setBackgroundColor((pct >= 100 ? FolioTheme.accent() : FolioTheme.accent()));
            mFill.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, ratio));
            miniTrack.addView(mFill);
            View mRest = new View(ctx);
            mRest.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(0f, 1f - ratio)));
            miniTrack.addView(mRest);
        }
        tile.addView(miniTrack);
        tile.addView(tTv);
        int stars = getRating(ctx, rec.kfxPath);
        LinearLayout metaRow = new LinearLayout(ctx);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView badgeTv = new TextView(ctx);
        if (pct >= 100) {
            badgeTv.setText("✓ Done");
            badgeTv.setTextColor(FolioTheme.accent());
        } else if (pct > 0) {
            badgeTv.setText(pct + "%");
            badgeTv.setTextColor(FolioTheme.accent());
        } else if (stars > 0) {
            badgeTv.setText("★ " + stars + "/5");
            badgeTv.setTextColor(FolioTheme.accent());
        } else if (isInQueue(ctx, rec.kfxPath)) {
            badgeTv.setText("Up Next");
            badgeTv.setTextColor(FolioTheme.accent());
        } else {
            badgeTv.setText("Unread");
            badgeTv.setTextColor(FolioTheme.textMuted());
        }
        badgeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
        badgeTv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        metaRow.addView(badgeTv);

        TextView infoIco = new TextView(ctx);
        infoIco.setText("ⓘ");
        infoIco.setTextColor(FolioTheme.textMuted());
        infoIco.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        infoIco.setPadding(dp(ctx, 4), 0, 0, 0);
        infoIco.setOnClickListener(v -> showBookDetailDialog(ctx, rec, pct, parentDialog, onMetadataEdited, onAuthorFilter));
        metaRow.addView(infoIco);
        tile.addView(metaRow);

        tile.setOnClickListener(v -> {
            File f = new File(rec.kfxPath);
            if (!f.exists()) {
                Toast.makeText(ctx, "Book file not found in app storage", Toast.LENGTH_SHORT).show();
                return;
            }
            parentDialog.dismiss();
            ExternalEpubActivity.launchKfxReader(ctx, f);
        });
        tile.setOnLongClickListener(v -> {
            showBookDetailDialog(ctx, rec, pct, parentDialog, onMetadataEdited, onAuthorFilter);
            return true;
        });
        return tile;
    }

    private static View buildBookCard(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                      final Map<String, Integer> progressMap,
                                      final AlertDialog parentDialog, final Runnable onMetadataEdited,
                                      final AuthorFilterCallback onAuthorFilter) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(FolioTheme.bgSurface());
        bg.setCornerRadius(dp(ctx, 12));
        bg.setStroke(dp(ctx, 1), FolioTheme.border());
        card.setBackground(bg);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, 0, 0, dp(ctx, 10));
        card.setLayoutParams(cardLp);

        Bitmap coverBm = null;
        if (rec.coverPath != null && !rec.coverPath.isEmpty()) {
            File cf = new File(rec.coverPath);
            if (cf.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                coverBm = BitmapFactory.decodeFile(cf.getAbsolutePath(), opts);
            }
        }
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dp(ctx, 56), dp(ctx, 84));
        imgLp.setMargins(0, 0, dp(ctx, 12), 0);
        if (coverBm != null) {
            ImageView thumb = new ImageView(ctx);
            thumb.setLayoutParams(imgLp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setBackgroundColor(FolioTheme.bgControl());
            thumb.setImageBitmap(coverBm);
            card.addView(thumb);
        } else {
            FrameLayout monoBox = new FrameLayout(ctx);
            monoBox.setLayoutParams(imgLp);
            GradientDrawable monoBg = new GradientDrawable();
            monoBg.setColor(FolioTheme.bgControl());
            monoBg.setCornerRadius(dp(ctx, 6));
            monoBg.setStroke(dp(ctx, 1), FolioTheme.border());
            monoBox.setBackground(monoBg);

            String initial = "A";
            if (rec.title != null) {
                String trimmed = rec.title.trim();
                for (int ci = 0; ci < trimmed.length(); ci++) {
                    char ch = trimmed.charAt(ci);
                    if (Character.isLetterOrDigit(ch)) {
                        initial = String.valueOf(Character.toUpperCase(ch));
                        break;
                    }
                }
            }
            TextView monoTv = new TextView(ctx);
            monoTv.setText(initial);
            monoTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            monoTv.setTextColor(FolioTheme.textMuted());
            monoTv.setGravity(Gravity.CENTER);
            monoTv.setTypeface(com.amazon.kindle.epub.catalog.FolioTheme.serif(ctx));
            monoBox.addView(monoTv, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            View spineRule = new View(ctx);
            GradientDrawable ruleBg = new GradientDrawable();
            ruleBg.setColor(FolioTheme.accentBorder());
            ruleBg.setCornerRadius(dp(ctx, 2));
            spineRule.setBackground(ruleBg);
            FrameLayout.LayoutParams ruleLp = new FrameLayout.LayoutParams(dp(ctx, 22), dp(ctx, 2));
            ruleLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            ruleLp.bottomMargin = dp(ctx, 8);
            monoBox.addView(spineRule, ruleLp);

            card.addView(monoBox);
        }

        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        String prefix = (isInQueue(ctx, rec.kfxPath) ? "" : "") + (isFavorite(ctx, rec.kfxPath) ? "★ " : "") + (!getNote(ctx, rec.kfxPath).isEmpty() ? "" : "");
        title.setText(prefix + rec.title);
        title.setTextColor(FolioTheme.textPrimary());
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(title);

        StringBuilder byline = new StringBuilder(formatAuthorForDisplay(rec.author));
        String cleanDate = EpubMetadataExtractor.normalizePubDate(rec.pubDate);
        if (!cleanDate.isEmpty()) byline.append(" · ").append(cleanDate);
        if (!rec.publisher.isEmpty()) byline.append(" · ").append(rec.publisher);
        TextView metaLine = new TextView(ctx);
        metaLine.setText(byline.toString());
        metaLine.setTextColor(FolioTheme.textSecondary());
        metaLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        metaLine.setPadding(0, dp(ctx, 2), 0, dp(ctx, 4));
        col.addView(metaLine);

        String displayGenres = cleanGenresForDisplay(rec.genres);
        if (!displayGenres.isEmpty()) {
            TextView genresTv = new TextView(ctx);
            genresTv.setText(displayGenres);
            genresTv.setTextColor(FolioTheme.textMuted());
            genresTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            genresTv.setMaxLines(2);
            genresTv.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(genresTv);
        }

        int pct = getProgress(progressMap, rec.kfxPath);
        if (pct > 0) {
            TextView progTv = new TextView(ctx);
            progTv.setText(pct >= 100 ? "✓ Finished" : (pct + "% read"));
            progTv.setTextColor((pct >= 100 ? FolioTheme.accent() : FolioTheme.accent()));
            progTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            progTv.setTypeface(Typeface.DEFAULT_BOLD);
            progTv.setPadding(0, dp(ctx, 3), 0, 0);
            col.addView(progTv);
        }

        if (!rec.isbn.isEmpty()) {
            TextView isbnTv = new TextView(ctx);
            isbnTv.setText("ISBN: " + rec.isbn);
            isbnTv.setTextColor(FolioTheme.textMuted());
            isbnTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            isbnTv.setPadding(0, dp(ctx, 2), 0, 0);
            col.addView(isbnTv);
        }

        String cardShelf = getShelf(ctx, rec.kfxPath);
        if (!cardShelf.isEmpty()) {
            TextView shelfTv = new TextView(ctx);
            shelfTv.setText("" + cardShelf);
            shelfTv.setTextColor(FolioTheme.accent());
            shelfTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            shelfTv.setTypeface(Typeface.DEFAULT_BOLD);
            shelfTv.setPadding(0, dp(ctx, 2), 0, 0);
            col.addView(shelfTv);
        }

        int cardRating = getRating(ctx, rec.kfxPath);
        if (cardRating > 0) {
            StringBuilder stars = new StringBuilder();
            for (int s = 0; s < 5; s++) stars.append(s < cardRating ? "★" : "☆");
            TextView rateTv = new TextView(ctx);
            rateTv.setText(stars.toString() + "  " + cardRating + "/5");
            rateTv.setTextColor(FolioTheme.accent());
            rateTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            rateTv.setTypeface(Typeface.DEFAULT_BOLD);
            rateTv.setPadding(0, dp(ctx, 2), 0, 0);
            col.addView(rateTv);
        }

        String cardNote = getNote(ctx, rec.kfxPath);
        if (!cardNote.isEmpty()) {
            TextView notePreviewTv = new TextView(ctx);
            notePreviewTv.setText("“" + cardNote + "”");
            notePreviewTv.setTextColor(FolioTheme.accent());
            notePreviewTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            notePreviewTv.setTypeface(null, Typeface.ITALIC);
            notePreviewTv.setMaxLines(1);
            notePreviewTv.setEllipsize(TextUtils.TruncateAt.END);
            notePreviewTv.setPadding(0, dp(ctx, 3), 0, 0);
            col.addView(notePreviewTv);
        }

        card.addView(col);

        TextView infoBtn = new TextView(ctx);
        infoBtn.setText("ⓘ");
        infoBtn.setTextColor(FolioTheme.textMuted());
        infoBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        infoBtn.setPadding(dp(ctx, 8), dp(ctx, 4), dp(ctx, 2), dp(ctx, 4));
        final int currentProg = getProgress(progressMap, rec.kfxPath);
        infoBtn.setOnClickListener(v -> showBookDetailDialog(ctx, rec, currentProg, parentDialog, onMetadataEdited, onAuthorFilter));
        card.addView(infoBtn);

        card.setOnClickListener(v -> {
            File f = new File(rec.kfxPath);
            if (!f.exists()) {
                Toast.makeText(ctx, "Book file not found in app storage", Toast.LENGTH_SHORT).show();
                return;
            }
            parentDialog.dismiss();
            ExternalEpubActivity.launchKfxReader(ctx, f);
        });

        return card;
    }

    public static void showBookDetails(final Context ctx, final FolioMetadataDb.BookRecord rec, final int currentProgress) {
        if (ctx == null || rec == null) return;
        showBookDetailDialog(ctx, rec, currentProgress, null, null, author -> showWithSearch(ctx, author));
    }

    private static void showBookDetailDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                             final int currentProgress,
                                             final AlertDialog parentDialog, final Runnable onMetadataEdited,
                                             final AuthorFilterCallback onAuthorFilter) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(FolioTheme.bgSurface());
        box.setPadding(dp(ctx, 20), dp(ctx, 18), dp(ctx, 20), dp(ctx, 14));

        final AlertDialog[] detailDlgRef = new AlertDialog[1];

        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams hrLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hrLp.setMargins(0, 0, 0, dp(ctx, 12));
        headerRow.setLayoutParams(hrLp);

        if (rec.coverPath != null && !rec.coverPath.isEmpty()) {
            File cf = new File(rec.coverPath);
            if (cf.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(cf.getAbsolutePath(), opts);
                if (bm != null) {
                    ImageView coverIv = new ImageView(ctx);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(dp(ctx, 64), dp(ctx, 96));
                    cLp.setMargins(0, 0, dp(ctx, 14), 0);
                    coverIv.setLayoutParams(cLp);
                    coverIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    coverIv.setBackgroundColor(FolioTheme.border());
                    coverIv.setImageBitmap(bm);
                    coverIv.setOnClickListener(v -> showCoverLightbox(ctx, rec));
                    headerRow.addView(coverIv);
                }
            }
        }

        LinearLayout headerCol = new LinearLayout(ctx);
        headerCol.setOrientation(LinearLayout.VERTICAL);
        headerCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        title.setText(rec.title);
        title.setTextColor(FolioTheme.textPrimary());
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        headerCol.addView(title);

        final String displayAuthor = formatAuthorForDisplay(rec.author);
        TextView sub = new TextView(ctx);
        sub.setText(displayAuthor.isEmpty() ? "" : displayAuthor + "  ›");
        sub.setTextColor(FolioTheme.accent());
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sub.setTypeface(Typeface.DEFAULT_BOLD);
        sub.setPadding(0, dp(ctx, 4), 0, dp(ctx, 4));
        if (!displayAuthor.isEmpty() && onAuthorFilter != null) {
            sub.setOnClickListener(v -> {
                if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
                onAuthorFilter.onFilterByAuthor(displayAuthor);
            });
        }
        headerCol.addView(sub);

        if (currentProgress > 0) {
            TextView progTv = new TextView(ctx);
            if (currentProgress >= 100) {
                final String finDate = getFinishedDate(ctx, rec.kfxPath);
                progTv.setText(finDate.isEmpty() ? "✓ Finished · Set date ›" : "✓ Finished on " + finDate + " · Edit ›");
                progTv.setOnClickListener(v -> showEditFinishedDateDialog(ctx, rec, finDate, () -> {
                    if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
                    if (onMetadataEdited != null) onMetadataEdited.run();
                }));
            } else {
                progTv.setText(currentProgress + "% read");
            }
            progTv.setTextColor((currentProgress >= 100 ? FolioTheme.accent() : FolioTheme.accent()));
            progTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            progTv.setTypeface(Typeface.DEFAULT_BOLD);
            progTv.setPadding(0, dp(ctx, 2), 0, 0);
            headerCol.addView(progTv);
        }

        headerRow.addView(headerCol);
        box.addView(headerRow);

        StringBuilder details = new StringBuilder();
        String cleanDetailDate = EpubMetadataExtractor.normalizePubDate(rec.pubDate);
        if (!cleanDetailDate.isEmpty()) details.append("Published: ").append(cleanDetailDate).append("\n");
        if (!rec.publisher.isEmpty()) details.append("Publisher: ").append(rec.publisher).append("\n");
        if (!rec.isbn.isEmpty()) details.append("ISBN: ").append(rec.isbn).append("\n");
        String cleanDetGenres = cleanGenresForDisplay(rec.genres);
        if (!rec.language.isEmpty() && !"und".equalsIgnoreCase(rec.language.trim())
                && !"undetermined".equalsIgnoreCase(rec.language.trim())) {
            details.append("Language: ").append(rec.language).append("\n");
        }
        if (!rec.openLibraryKey.isEmpty()) details.append("Open Library: ").append(rec.openLibraryKey).append("\n");
        if (rec.kfxPath != null && !rec.kfxPath.isEmpty()) {
            File kf = new File(rec.kfxPath);
            if (kf.exists()) {
                long bytes = kf.length();
                String sizeStr = bytes >= 1024L * 1024L
                        ? String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                        : Math.max(1L, bytes / 1024L) + " KB";
                int wpm = 250;
                try {
                    wpm = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE).getInt("reading_speed_wpm", 250);
                    if (wpm < 100 || wpm > 800) wpm = 250;
                } catch (Throwable ignored) {}
                int estMins = Math.max(15, Math.min(2400, (int) Math.round((bytes / 1024.0) * 0.34 * (250.0 / wpm))));
                String readEst = estMins >= 60
                        ? ("~" + (estMins / 60) + "h " + (estMins % 60) + "m read")
                        : ("~" + estMins + "m read");
                details.append("Format: Enhanced KFX · ").append(sizeStr).append(" · ").append(readEst).append("\n");
            }
        }

        TextView metaTv = new TextView(ctx);
        metaTv.setText(details.toString().trim());
        metaTv.setTextColor(FolioTheme.textSecondary());
        metaTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        box.addView(metaTv);

        if (!cleanDetGenres.isEmpty()) {
            HorizontalScrollView gScroll = new HorizontalScrollView(ctx);
            gScroll.setHorizontalScrollBarEnabled(false);
            LinearLayout.LayoutParams gsLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            gsLp.setMargins(0, dp(ctx, 10), 0, dp(ctx, 2));
            gScroll.setLayoutParams(gsLp);

            LinearLayout gRow = new LinearLayout(ctx);
            gRow.setOrientation(LinearLayout.HORIZONTAL);
            for (String rawG : cleanDetGenres.split(",")) {
                final String gTag = rawG.trim();
                if (gTag.isEmpty()) continue;
                TextView pill = new TextView(ctx);
                pill.setText(gTag);
                pill.setTextColor(FolioTheme.accent());
                pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                pill.setTypeface(Typeface.DEFAULT_BOLD);
                pill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
                GradientDrawable pBg = new GradientDrawable();
                pBg.setColor(FolioTheme.bgControl());
                pBg.setCornerRadius(dp(ctx, 14));
                pBg.setStroke(dp(ctx, 1), FolioTheme.border());
                pill.setBackground(pBg);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                pLp.setMargins(0, 0, dp(ctx, 6), 0);
                pill.setLayoutParams(pLp);
                if (onAuthorFilter != null) {
                    pill.setOnClickListener(v -> {
                        if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
                        onAuthorFilter.onFilterByAuthor(gTag);
                    });
                }
                gRow.addView(pill);
            }
            gScroll.addView(gRow);
            box.addView(gScroll);
        }

        if (!rec.description.isEmpty()) {
            final boolean isLong = rec.description.length() > 180;
            final boolean[] expanded = new boolean[] { false };
            final TextView descTv = new TextView(ctx);
            descTv.setText("\n" + rec.description);
            descTv.setTextColor(FolioTheme.textMuted());
            descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            if (isLong) {
                descTv.setMaxLines(5);
                descTv.setEllipsize(TextUtils.TruncateAt.END);
            }
            box.addView(descTv);

            if (isLong) {
                final TextView moreTv = new TextView(ctx);
                moreTv.setText("Read full synopsis ›");
                moreTv.setTextColor(FolioTheme.accent());
                moreTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                moreTv.setTypeface(Typeface.DEFAULT_BOLD);
                moreTv.setPadding(0, dp(ctx, 4), 0, dp(ctx, 2));
                View.OnClickListener toggleDesc = v -> {
                    expanded[0] = !expanded[0];
                    if (expanded[0]) {
                        descTv.setMaxLines(Integer.MAX_VALUE);
                        descTv.setEllipsize(null);
                        moreTv.setText("Show less");
                    } else {
                        descTv.setMaxLines(5);
                        descTv.setEllipsize(TextUtils.TruncateAt.END);
                        moreTv.setText("Read full synopsis ›");
                    }
                };
                descTv.setOnClickListener(toggleDesc);
                moreTv.setOnClickListener(toggleDesc);
                box.addView(moreTv);
            }
        }

        LinearLayout actionPillRow = new LinearLayout(ctx);
        actionPillRow.setOrientation(LinearLayout.HORIZONTAL);
        actionPillRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams apLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        apLp.setMargins(0, dp(ctx, 14), 0, dp(ctx, 2));
        actionPillRow.setLayoutParams(apLp);

        final boolean isFinished = currentProgress >= 100;
        TextView readToggleBtn = new TextView(ctx);
        readToggleBtn.setText(isFinished ? "↺ Mark as Unread" : "✓ Mark as Finished");
        readToggleBtn.setTextColor((isFinished ? FolioTheme.accent() : FolioTheme.accent()));
        readToggleBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        readToggleBtn.setTypeface(Typeface.DEFAULT_BOLD);
        readToggleBtn.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable toggleBg = new GradientDrawable();
        toggleBg.setColor(FolioTheme.bgControl());
        toggleBg.setCornerRadius(dp(ctx, 10));
        readToggleBtn.setBackground(toggleBg);
        LinearLayout.LayoutParams toggleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        toggleLp.setMargins(0, 0, dp(ctx, 8), 0);
        readToggleBtn.setLayoutParams(toggleLp);
        readToggleBtn.setOnClickListener(v -> {
            boolean targetFinished = !isFinished;
            setBookReadState(ctx, rec.kfxPath, targetFinished);
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            Toast.makeText(ctx,
                    (targetFinished ? "Marked \"" : "Reset \"") + rec.title
                            + (targetFinished ? "\" as Finished" : "\" to Unread"),
                    Toast.LENGTH_SHORT).show();
            if (onMetadataEdited != null) onMetadataEdited.run();
        });
        actionPillRow.addView(readToggleBtn);

        final boolean isFav = isFavorite(ctx, rec.kfxPath);
        TextView favBtn = new TextView(ctx);
        favBtn.setText(isFav ? "★ Favorited" : "☆ Favorite");
        favBtn.setTextColor((isFav ? FolioTheme.accent() : FolioTheme.textSecondary()));
        favBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        favBtn.setTypeface(Typeface.DEFAULT_BOLD);
        favBtn.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable favBg = new GradientDrawable();
        favBg.setColor((isFav ? FolioTheme.accentFill() : FolioTheme.bgControl()));
        favBg.setCornerRadius(dp(ctx, 10));
        if (isFav) favBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
        favBtn.setBackground(favBg);
        favBtn.setOnClickListener(v -> {
            boolean nextFav = !isFav;
            setFavorite(ctx, rec.kfxPath, nextFav);
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            Toast.makeText(ctx,
                    (nextFav ? "Added \"" : "Removed \"") + rec.title
                            + (nextFav ? "\" to Favorites" : "\" from Favorites"),
                    Toast.LENGTH_SHORT).show();
            if (onMetadataEdited != null) onMetadataEdited.run();
        });
        actionPillRow.addView(favBtn);
        box.addView(actionPillRow);

        // Interactive 1-5 Star Rating Bar
        LinearLayout rateRow = new LinearLayout(ctx);
        rateRow.setOrientation(LinearLayout.HORIZONTAL);
        rateRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rrLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rrLp.setMargins(0, dp(ctx, 12), 0, dp(ctx, 0));
        rateRow.setLayoutParams(rrLp);

        TextView rateLbl = new TextView(ctx);
        rateLbl.setText("Your Rating:");
        rateLbl.setTextColor(FolioTheme.textMuted());
        rateLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rateLbl.setTypeface(Typeface.DEFAULT_BOLD);
        rateLbl.setPadding(0, 0, dp(ctx, 8), 0);
        rateRow.addView(rateLbl);

        final int[] curRating = new int[] { getRating(ctx, rec.kfxPath) };
        final TextView[] starViews = new TextView[5];
        final Runnable paintStars = () -> {
            for (int si = 0; si < 5; si++) {
                boolean filled = si < curRating[0];
                starViews[si].setText(filled ? "★" : "☆");
                starViews[si].setTextColor((filled ? FolioTheme.accent() : FolioTheme.border()));
            }
        };
        for (int si = 0; si < 5; si++) {
            final int starVal = si + 1;
            TextView st = new TextView(ctx);
            st.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            st.setPadding(dp(ctx, 4), dp(ctx, 2), dp(ctx, 4), dp(ctx, 2));
            st.setOnClickListener(v -> {
                curRating[0] = (curRating[0] == starVal) ? 0 : starVal;
                setRating(ctx, rec.kfxPath, curRating[0]);
                paintStars.run();
                if (onMetadataEdited != null) onMetadataEdited.run();
            });
            starViews[si] = st;
            rateRow.addView(st);
        }
        paintStars.run();
        box.addView(rateRow);

        // Custom Shelf / Collection Row
        final String curShelf = getShelf(ctx, rec.kfxPath);
        LinearLayout shelfRow = new LinearLayout(ctx);
        shelfRow.setOrientation(LinearLayout.HORIZONTAL);
        shelfRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shLp.setMargins(0, dp(ctx, 8), 0, dp(ctx, 0));
        shelfRow.setLayoutParams(shLp);

        TextView shelfLbl = new TextView(ctx);
        shelfLbl.setText("Shelf:");
        shelfLbl.setTextColor(FolioTheme.textMuted());
        shelfLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        shelfLbl.setTypeface(Typeface.DEFAULT_BOLD);
        shelfLbl.setPadding(0, 0, dp(ctx, 8), 0);
        shelfRow.addView(shelfLbl);

        TextView shelfPill = new TextView(ctx);
        shelfPill.setText(curShelf.isEmpty() ? "+ Assign Shelf ›" : "" + curShelf + "  ›");
        shelfPill.setTextColor((curShelf.isEmpty() ? FolioTheme.textMuted() : FolioTheme.accent()));
        shelfPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        shelfPill.setTypeface(Typeface.DEFAULT_BOLD);
        shelfPill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable shBg = new GradientDrawable();
        shBg.setColor((curShelf.isEmpty() ? FolioTheme.bgControl() : FolioTheme.accentFill()));
        shBg.setCornerRadius(dp(ctx, 12));
        if (!curShelf.isEmpty()) shBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
        shelfPill.setBackground(shBg);
        shelfPill.setOnClickListener(v -> showShelfPickerDialog(ctx, rec, curShelf, () -> {
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            if (onMetadataEdited != null) onMetadataEdited.run();
        }));
        shelfRow.addView(shelfPill);

        final boolean inQ = isInQueue(ctx, rec.kfxPath);
        TextView queuePill = new TextView(ctx);
        queuePill.setText(inQ ? "Queued" : "Up Next");
        queuePill.setTextColor((inQ ? FolioTheme.accentSoftText() : FolioTheme.textMuted()));
        queuePill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        queuePill.setTypeface(Typeface.DEFAULT_BOLD);
        queuePill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable qBg = new GradientDrawable();
        qBg.setColor((inQ ? FolioTheme.accentFill() : FolioTheme.bgControl()));
        qBg.setCornerRadius(dp(ctx, 12));
        if (inQ) qBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
        queuePill.setBackground(qBg);
        LinearLayout.LayoutParams qpLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        qpLp.setMargins(dp(ctx, 8), 0, 0, 0);
        queuePill.setLayoutParams(qpLp);
        queuePill.setOnClickListener(v -> {
            boolean nextQ = !inQ;
            setInQueue(ctx, rec.kfxPath, nextQ);
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            Toast.makeText(ctx,
                    (nextQ ? "Added \"" : "Removed \"") + rec.title
                            + (nextQ ? "\" to Up Next Queue" : "\" from Up Next Queue"),
                    Toast.LENGTH_SHORT).show();
            if (onMetadataEdited != null) onMetadataEdited.run();
        });
        shelfRow.addView(queuePill);
        box.addView(shelfRow);

        // Personal Reading Note card / "+ Add Note" button
        final String existingNote = getNote(ctx, rec.kfxPath);
        LinearLayout noteBox = new LinearLayout(ctx);
        noteBox.setOrientation(LinearLayout.VERTICAL);
        noteBox.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable noteBg = new GradientDrawable();
        noteBg.setColor(FolioTheme.bgSurface());
        noteBg.setCornerRadius(dp(ctx, 10));
        noteBg.setStroke(dp(ctx, 1), (existingNote.isEmpty() ? FolioTheme.border() : FolioTheme.accentBorder()));
        noteBox.setBackground(noteBg);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteLp.setMargins(0, dp(ctx, 12), 0, dp(ctx, 2));
        noteBox.setLayoutParams(noteLp);

        TextView noteHdr = new TextView(ctx);
        noteHdr.setText(existingNote.isEmpty() ? "Add a Personal Reading Note ›" : "Personal Note (tap to edit)");
        noteHdr.setTextColor((existingNote.isEmpty() ? FolioTheme.textMuted() : FolioTheme.accent()));
        noteHdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        noteHdr.setTypeface(Typeface.DEFAULT_BOLD);
        noteBox.addView(noteHdr);

        if (!existingNote.isEmpty()) {
            TextView noteBody = new TextView(ctx);
            noteBody.setText(existingNote);
            noteBody.setTextColor(FolioTheme.textPrimary());
            noteBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            noteBody.setPadding(0, dp(ctx, 4), 0, 0);
            noteBox.addView(noteBody);
        }

        noteBox.setOnClickListener(v -> showEditNoteDialog(ctx, rec, existingNote, () -> {
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            if (onMetadataEdited != null) onMetadataEdited.run();
        }));
        box.addView(noteBox);

        // "More by [Author]" interactive sibling book strip
        String curAuthorClean = formatAuthorForDisplay(rec.author);
        if (!curAuthorClean.isEmpty() && !"Unknown Author".equalsIgnoreCase(curAuthorClean)) {
            List<FolioMetadataDb.BookRecord> siblings = new ArrayList<>();
            FolioMetadataDb sibDb = new FolioMetadataDb(ctx);
            try {
                for (FolioMetadataDb.BookRecord cand : sibDb.getAllBooks()) {
                    if (cand.id == rec.id) continue;
                    if (curAuthorClean.equalsIgnoreCase(formatAuthorForDisplay(cand.author))) {
                        siblings.add(cand);
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                sibDb.close();
            }
            if (!siblings.isEmpty()) {
                Map<String, Integer> sibProgMap = loadProgressMap(ctx);
                TextView sibHdr = new TextView(ctx);
                sibHdr.setText("MORE BY " + curAuthorClean.toUpperCase(Locale.ROOT) + " (" + siblings.size() + ")");
                sibHdr.setTextColor(FolioTheme.accent());
                sibHdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                sibHdr.setTypeface(Typeface.DEFAULT_BOLD);
                sibHdr.setPadding(0, dp(ctx, 14), 0, dp(ctx, 6));
                box.addView(sibHdr);

                HorizontalScrollView sibScroll = new HorizontalScrollView(ctx);
                sibScroll.setHorizontalScrollBarEnabled(false);
                LinearLayout sibRow = new LinearLayout(ctx);
                sibRow.setOrientation(LinearLayout.HORIZONTAL);

                int sibLimit = Math.min(siblings.size(), 8);
                for (int si = 0; si < sibLimit; si++) {
                    final FolioMetadataDb.BookRecord sib = siblings.get(si);
                    final int sPct = getProgress(sibProgMap, sib.kfxPath);

                    LinearLayout sCard = new LinearLayout(ctx);
                    sCard.setOrientation(LinearLayout.HORIZONTAL);
                    sCard.setGravity(Gravity.CENTER_VERTICAL);
                    sCard.setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
                    GradientDrawable sBg = new GradientDrawable();
                    sBg.setColor(FolioTheme.bgSurface());
                    sBg.setCornerRadius(dp(ctx, 9));
                    sBg.setStroke(dp(ctx, 1), FolioTheme.border());
                    sCard.setBackground(sBg);
                    LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                            dp(ctx, 172), LinearLayout.LayoutParams.WRAP_CONTENT);
                    if (si < sibLimit - 1) scLp.setMargins(0, 0, dp(ctx, 8), 0);
                    sCard.setLayoutParams(scLp);

                    ImageView sCov = new ImageView(ctx);
                    sCov.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    sCov.setBackgroundColor(FolioTheme.bgModal());
                    LinearLayout.LayoutParams sivLp = new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 46));
                    sivLp.setMargins(0, 0, dp(ctx, 8), 0);
                    sCov.setLayoutParams(sivLp);
                    if (sib.kfxPath != null && !sib.kfxPath.isEmpty()) {
                        File scf = new File(sib.kfxPath + ".cover.jpg");
                        if (scf.exists() && scf.length() >= 64) {
                            BitmapFactory.Options sopts = new BitmapFactory.Options();
                            sopts.inSampleSize = 4;
                            Bitmap sbm = BitmapFactory.decodeFile(scf.getAbsolutePath(), sopts);
                            if (sbm != null) sCov.setImageBitmap(sbm);
                        }
                    }
                    sCard.addView(sCov);

                    LinearLayout sMeta = new LinearLayout(ctx);
                    sMeta.setOrientation(LinearLayout.VERTICAL);
                    sMeta.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView stTv = new TextView(ctx);
                    stTv.setText(sib.title != null && !sib.title.isEmpty() ? sib.title : "Untitled");
                    stTv.setTextColor(FolioTheme.textPrimary());
                    stTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                    stTv.setTypeface(Typeface.DEFAULT_BOLD);
                    stTv.setMaxLines(2);
                    stTv.setEllipsize(TextUtils.TruncateAt.END);
                    sMeta.addView(stTv);

                    TextView sbTv = new TextView(ctx);
                    if (sPct >= 100) {
                        sbTv.setText("✓ Done ›");
                        sbTv.setTextColor(FolioTheme.accent());
                    } else if (sPct > 0) {
                        sbTv.setText(sPct + "% read ›");
                        sbTv.setTextColor(FolioTheme.accent());
                    } else if (isInQueue(ctx, sib.kfxPath)) {
                        sbTv.setText("Up Next ›");
                        sbTv.setTextColor(FolioTheme.accent());
                    } else {
                        int sYr = parseBookYear(sib.pubDate);
                        sbTv.setText(sYr > 0 ? (sYr + " · View ›") : "View Details ›");
                        sbTv.setTextColor(FolioTheme.textMuted());
                    }
                    sbTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    sbTv.setTypeface(Typeface.DEFAULT_BOLD);
                    sbTv.setPadding(0, dp(ctx, 2), 0, 0);
                    sMeta.addView(sbTv);
                    sCard.addView(sMeta);

                    sCard.setOnClickListener(v -> {
                        if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
                        showBookDetailDialog(ctx, sib, sPct, parentDialog, onMetadataEdited, onAuthorFilter);
                    });
                    sibRow.addView(sCard);
                }
                sibScroll.addView(sibRow);
                box.addView(sibScroll);
            }
        }

        LinearLayout footerActionRow = new LinearLayout(ctx);
        footerActionRow.setOrientation(LinearLayout.HORIZONTAL);
        footerActionRow.setGravity(Gravity.CENTER_VERTICAL);
        footerActionRow.setPadding(0, dp(ctx, 14), 0, dp(ctx, 4));

        TextView removeBtn = new TextView(ctx);
        removeBtn.setText("Remove from Azath Library");
        removeBtn.setTextColor(Color.parseColor("#C99A7E"));
        removeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        removeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        removeBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        removeBtn.setOnClickListener(v -> {
            AlertDialog rmConfirm = new AlertDialog.Builder(ctx)
                    .setTitle("Remove Book")
                    .setMessage("Remove \"" + rec.title + "\" from Folio Library?\n\nOriginal EPUB files on external storage are never deleted.")
                    .setPositiveButton("Remove", (cd, cw) -> {
                        removeBookFromLibrary(ctx, rec);
                        if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
                        Toast.makeText(ctx, "Removed \"" + rec.title + "\" from library", Toast.LENGTH_SHORT).show();
                        if (onMetadataEdited != null) onMetadataEdited.run();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            FolioTheme.applyToDialog(ctx, rmConfirm);
        });
        footerActionRow.addView(removeBtn);

        final Runnable[] shareOrCopyBuilder = new Runnable[0];
        final java.util.concurrent.Callable<String> buildCardText = () -> {
            StringBuilder clip = new StringBuilder();
            clip.append(rec.title);
            if (!displayAuthor.isEmpty()) clip.append(" — ").append(displayAuthor);
            if (!cleanDetailDate.isEmpty()) clip.append(" (").append(cleanDetailDate).append(")");
            if (!rec.publisher.isEmpty()) clip.append("\nPublisher: ").append(rec.publisher);
            if (!rec.isbn.isEmpty()) clip.append("\nISBN: ").append(rec.isbn);
            if (currentProgress >= 100) {
                String finDt = getFinishedDate(ctx, rec.kfxPath);
                clip.append("\nStatus: ✓ Finished").append(finDt.isEmpty() ? "" : " (" + finDt + ")");
            } else if (currentProgress > 0) {
                clip.append("\nStatus: ").append(currentProgress).append("% read");
            }
            String shTxt = getShelf(ctx, rec.kfxPath);
            if (!shTxt.isEmpty()) clip.append("\nShelf: ").append(shTxt);
            int rStars = getRating(ctx, rec.kfxPath);
            if (rStars > 0) clip.append("\nRating: ").append(rStars).append("/5");
            String nTxt = getNote(ctx, rec.kfxPath);
            if (!nTxt.isEmpty()) clip.append("\nNote: ").append(nTxt);
            return clip.toString();
        };

        TextView copyBtn = new TextView(ctx);
        copyBtn.setText("Copy");
        copyBtn.setTextColor(FolioTheme.textMuted());
        copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        copyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        copyBtn.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable copyBg = new GradientDrawable();
        copyBg.setColor(FolioTheme.bgControl());
        copyBg.setCornerRadius(dp(ctx, 10));
        copyBtn.setBackground(copyBg);
        copyBtn.setOnClickListener(v -> {
            try {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Book Info", buildCardText.call()));
                    Toast.makeText(ctx, "Copied book info to clipboard", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable ignored) {}
        });
        footerActionRow.addView(copyBtn);

        TextView shareBtn = new TextView(ctx);
        shareBtn.setText("↗ Share");
        shareBtn.setTextColor(FolioTheme.accent());
        shareBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        shareBtn.setTypeface(Typeface.DEFAULT_BOLD);
        shareBtn.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable shareBg = new GradientDrawable();
        shareBg.setColor(FolioTheme.accentFill());
        shareBg.setCornerRadius(dp(ctx, 10));
        shareBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
        shareBtn.setBackground(shareBg);
        LinearLayout.LayoutParams shBtnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shBtnLp.setMargins(dp(ctx, 6), 0, 0, 0);
        shareBtn.setLayoutParams(shBtnLp);
        shareBtn.setOnClickListener(v -> {
            try {
                android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(android.content.Intent.EXTRA_SUBJECT, rec.title != null ? rec.title : "Folio Book");
                share.putExtra(android.content.Intent.EXTRA_TEXT, buildCardText.call());
                ctx.startActivity(android.content.Intent.createChooser(share, "Share Book Details"));
            } catch (Throwable ignored) {}
        });
        footerActionRow.addView(shareBtn);
        box.addView(footerActionRow);

        ScrollView detailSv = new ScrollView(ctx);
        detailSv.addView(box);

        detailDlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(detailSv)
                .setPositiveButton("Read Now", (d, w) -> {
                    File f = new File(rec.kfxPath);
                    if (f.exists()) {
                        if (parentDialog != null) parentDialog.dismiss();
                        ExternalEpubActivity.launchKfxReader(ctx, f);
                    }
                })
                .setNeutralButton("Edit Metadata", (d, w) ->
                        showEditMetadataDialog(ctx, rec, onMetadataEdited))
                .setNegativeButton("Close", null)
                .show();
        FolioTheme.applyToDialog(ctx, detailDlgRef[0]);
    }

    private static void showCoverLightbox(final Context ctx, final FolioMetadataDb.BookRecord rec) {
        if (rec == null || rec.coverPath == null || rec.coverPath.isEmpty()) return;
        File cf = new File(rec.coverPath);
        if (!cf.exists()) return;
        Bitmap bm = null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(cf.getAbsolutePath(), bounds);
            int sample = 1;
            int maxDim = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxDim / sample > 1600) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            bm = BitmapFactory.decodeFile(cf.getAbsolutePath(), opts);
        } catch (Throwable ignored) {}
        if (bm == null) return;

        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(Color.BLACK);
        wrap.setGravity(Gravity.CENTER);
        wrap.setPadding(dp(ctx, 16), dp(ctx, 20), dp(ctx, 16), dp(ctx, 16));

        ImageView fullIv = new ImageView(ctx);
        fullIv.setImageBitmap(bm);
        fullIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        fullIv.setAdjustViewBounds(true);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 440));
        fullIv.setLayoutParams(ivLp);
        wrap.addView(fullIv);

        TextView capTv = new TextView(ctx);
        String authorStr = formatAuthorForDisplay(rec.author);
        capTv.setText(authorStr.isEmpty() ? rec.title : rec.title + " · " + authorStr);
        capTv.setTextColor(FolioTheme.textSecondary());
        capTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        capTv.setGravity(Gravity.CENTER);
        capTv.setPadding(0, dp(ctx, 12), 0, dp(ctx, 4));
        wrap.addView(capTv);

        TextView hintTv = new TextView(ctx);
        hintTv.setText("Tap cover to close");
        hintTv.setTextColor(FolioTheme.textMuted());
        hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        hintTv.setGravity(Gravity.CENTER);
        wrap.addView(hintTv);

        final AlertDialog dlg = new AlertDialog.Builder(ctx)
                .setView(wrap)
                .create();
        wrap.setOnClickListener(v -> dlg.dismiss());
        fullIv.setOnClickListener(v -> dlg.dismiss());
        dlg.show();
        FolioTheme.applyToDialog(ctx, dlg);
    }

    private static void showEditMetadataDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                               final Runnable onSaved) {
        ScrollView sv = new ScrollView(ctx);
        LinearLayout form = new LinearLayout(ctx);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setBackgroundColor(FolioTheme.bgSurface());
        form.setPadding(dp(ctx, 20), dp(ctx, 18), dp(ctx, 20), dp(ctx, 14));
        sv.addView(form);

        TextView hdr = new TextView(ctx);
        hdr.setText("Edit Book Metadata");
        hdr.setTextColor(FolioTheme.textPrimary());
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 12));
        form.addView(hdr);

        final EditText etTitle = addField(ctx, form, "Title", rec.title);
        final EditText etAuthor = addField(ctx, form, "Author", rec.author);

        // Smart Quick-Fix Toolbar: ⇄ Swap · Aa Fix Author · Aa Title Case
        LinearLayout fixRow = new LinearLayout(ctx);
        fixRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams fixRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fixRowLp.topMargin = dp(ctx, 8);
        fixRowLp.bottomMargin = dp(ctx, 4);
        fixRow.setLayoutParams(fixRowLp);

        TextView swapPill = new TextView(ctx);
        swapPill.setText("⇄ Swap T/A");
        swapPill.setTextColor(FolioTheme.accent());
        swapPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        swapPill.setTypeface(Typeface.DEFAULT_BOLD);
        swapPill.setGravity(Gravity.CENTER);
        swapPill.setSingleLine(true);
        swapPill.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));
        swapPill.setBackground(pillBg(ctx, "#282520", "#524B41", 8));
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        spLp.rightMargin = dp(ctx, 6);
        swapPill.setLayoutParams(spLp);
        swapPill.setOnClickListener(v -> {
            String t = etTitle.getText().toString();
            String a = etAuthor.getText().toString();
            etTitle.setText(a);
            etAuthor.setText(t);
            Toast.makeText(ctx, "Swapped Title & Author", Toast.LENGTH_SHORT).show();
        });
        fixRow.addView(swapPill);

        TextView normAuthPill = new TextView(ctx);
        normAuthPill.setText("Aa Fix Author");
        normAuthPill.setTextColor(FolioTheme.accent());
        normAuthPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        normAuthPill.setTypeface(Typeface.DEFAULT_BOLD);
        normAuthPill.setGravity(Gravity.CENTER);
        normAuthPill.setSingleLine(true);
        normAuthPill.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));
        normAuthPill.setBackground(pillBg(ctx, "#282520", "#524B41", 8));
        LinearLayout.LayoutParams naLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        naLp.rightMargin = dp(ctx, 6);
        normAuthPill.setLayoutParams(naLp);
        normAuthPill.setOnClickListener(v -> {
            String fixed = formatAuthorForDisplay(etAuthor.getText().toString());
            etAuthor.setText(fixed);
            Toast.makeText(ctx, "Normalized Author", Toast.LENGTH_SHORT).show();
        });
        fixRow.addView(normAuthPill);

        TextView titleCasePill = new TextView(ctx);
        titleCasePill.setText("Aa Title Case");
        titleCasePill.setTextColor(FolioTheme.accent());
        titleCasePill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        titleCasePill.setTypeface(Typeface.DEFAULT_BOLD);
        titleCasePill.setGravity(Gravity.CENTER);
        titleCasePill.setSingleLine(true);
        titleCasePill.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));
        titleCasePill.setBackground(pillBg(ctx, "#282520", "#524B41", 8));
        titleCasePill.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        titleCasePill.setOnClickListener(v -> {
            String fixed = toSmartTitleCase(etTitle.getText().toString());
            etTitle.setText(fixed);
            Toast.makeText(ctx, "Formatted Title Case", Toast.LENGTH_SHORT).show();
        });
        fixRow.addView(titleCasePill);
        form.addView(fixRow);

        final EditText etPubDate = addField(ctx, form, "Published Date / Year", rec.pubDate);
        final EditText etPublisher = addField(ctx, form, "Publisher", rec.publisher);
        final EditText etGenres = addField(ctx, form, "Genres (comma-separated)", rec.genres);
        final EditText etIsbn = addField(ctx, form, "ISBN", rec.isbn);
        final EditText etDesc = addField(ctx, form, "Description / Synopsis", rec.description);
        etDesc.setSingleLine(false);
        etDesc.setMinLines(2);
        etDesc.setMaxLines(5);
        etDesc.setGravity(Gravity.TOP | Gravity.START);

        AlertDialog editDlg = new AlertDialog.Builder(ctx)
                .setView(sv)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newAuthor = etAuthor.getText().toString().trim();
                    FolioMetadataDb db = new FolioMetadataDb(ctx);
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put("title", newTitle);
                        cv.put("author", newAuthor);
                        cv.put("pub_date", etPubDate.getText().toString().trim());
                        cv.put("publisher", etPublisher.getText().toString().trim());
                        cv.put("genres", etGenres.getText().toString().trim());
                        cv.put("isbn", etIsbn.getText().toString().trim());
                        cv.put("description", etDesc.getText().toString().trim());
                        db.getWritableDatabase().update(
                                "books_metadata", cv, "id = ?", new String[] { String.valueOf(rec.id) });
                        rec.title = newTitle;
                        rec.author = newAuthor;
                        rec.pubDate = etPubDate.getText().toString().trim();
                        rec.publisher = etPublisher.getText().toString().trim();
                        rec.genres = etGenres.getText().toString().trim();
                        rec.isbn = etIsbn.getText().toString().trim();
                        rec.description = etDesc.getText().toString().trim();
                        syncTitleAuthorToKindleDb(ctx, rec.kfxPath, newTitle, newAuthor);
                        Toast.makeText(ctx, "Saved & synced metadata", Toast.LENGTH_SHORT).show();
                        if (onSaved != null) onSaved.run();
                    } finally {
                        db.close();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, editDlg);
    }

    private static GradientDrawable pillBg(Context ctx, String fillHex, String strokeHex, int radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(fillHex));
        bg.setCornerRadius(dp(ctx, radiusDp));
        bg.setStroke(dp(ctx, 1), Color.parseColor(strokeHex));
        return bg;
    }

    private static String toSmartTitleCase(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";
        String[] minor = { "a", "an", "the", "and", "or", "but", "nor", "of", "in", "on", "to", "for", "by", "at", "with", "from" };
        java.util.HashSet<String> minorSet = new java.util.HashSet<>(java.util.Arrays.asList(minor));
        String[] words = trimmed.split("\\s+");
        StringBuilder out = new StringBuilder();
        boolean forceCap = true;
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (i > 0) out.append(' ');
            String lower = w.toLowerCase(java.util.Locale.US);
            if (!forceCap && i < words.length - 1 && minorSet.contains(lower)) {
                out.append(lower);
            } else if (w.length() > 1) {
                out.append(Character.toUpperCase(w.charAt(0))).append(lower.substring(1));
            } else {
                out.append(w.toUpperCase(java.util.Locale.US));
            }
            forceCap = w.endsWith(":") || w.endsWith("-") || w.endsWith("—") || w.endsWith("?");
        }
        return out.toString();
    }

    private static void syncTitleAuthorToKindleDb(Context ctx, String kfxPath, String title, String author) {
        if (kfxPath == null || kfxPath.isEmpty()) return;
        try {
            File kdbFile = ctx.getDatabasePath("kindle_library.db");
            if (kdbFile == null || !kdbFile.exists()) return;
            android.database.sqlite.SQLiteDatabase kdb =
                    android.database.sqlite.SQLiteDatabase.openDatabase(
                            kdbFile.getAbsolutePath(), null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
            try {
                String key = null;
                android.database.Cursor c = kdb.rawQuery(
                        "SELECT KEY FROM LocalContent WHERE FILE_PATH = ?",
                        new String[] { kfxPath });
                try {
                    if (c.moveToFirst()) key = c.getString(0);
                } finally {
                    c.close();
                }
                if (key != null && !key.isEmpty()) {
                    if (title != null && !title.isEmpty()) {
                        kdb.execSQL("UPDATE KindleContent SET TITLE = ? WHERE ID = ?",
                                new Object[] { title, key });
                    }
                    if (author != null && !author.isEmpty()) {
                        kdb.execSQL("UPDATE KindleContent SET AUTHOR = ? WHERE ID = ?",
                                new Object[] { author, key });
                    }
                }
            } finally {
                kdb.close();
            }
        } catch (Throwable ignored) {}
    }

    private static EditText addField(Context ctx, LinearLayout parent, String label, String value) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(FolioTheme.textSecondary());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setPadding(0, dp(ctx, 6), 0, dp(ctx, 4));
        parent.addView(tv);

        EditText et = new EditText(ctx);
        et.setText(value != null ? value : "");
        et.setTextColor(FolioTheme.textPrimary());
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setSingleLine(true);
        et.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(FolioTheme.bgControl());
        bg.setCornerRadius(dp(ctx, 8));
        bg.setStroke(dp(ctx, 1), FolioTheme.border());
        et.setBackground(bg);
        parent.addView(et);
        return et;
    }

    private static void removeBookFromLibrary(Context ctx, FolioMetadataDb.BookRecord rec) {
        FolioMetadataDb mdb = new FolioMetadataDb(ctx);
        try {
            mdb.getWritableDatabase().delete("books_metadata", "id = ?",
                    new String[] { String.valueOf(rec.id) });
        } catch (Throwable ignored) {
        } finally {
            mdb.close();
        }

        try {
            File kdbFile = ctx.getDatabasePath("kindle_library.db");
            if (kdbFile != null && kdbFile.exists() && rec.kfxPath != null && !rec.kfxPath.isEmpty()) {
                android.database.sqlite.SQLiteDatabase kdb =
                        android.database.sqlite.SQLiteDatabase.openDatabase(
                                kdbFile.getAbsolutePath(), null,
                                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
                try {
                    android.database.Cursor c = kdb.rawQuery(
                            "SELECT KEY FROM LocalContent WHERE FILE_PATH = ?",
                            new String[] { rec.kfxPath });
                    List<String> keys = new ArrayList<>();
                    if (c != null) {
                        while (c.moveToNext()) keys.add(c.getString(0));
                        c.close();
                    }
                    kdb.beginTransaction();
                    try {
                        kdb.delete("LocalContent", "FILE_PATH = ?", new String[] { rec.kfxPath });
                        for (String key : keys) {
                            kdb.delete("KindleContent", "ID = ?", new String[] { key });
                            kdb.delete("UserContent", "KEY = ?", new String[] { key });
                        }
                        kdb.setTransactionSuccessful();
                    } finally {
                        kdb.endTransaction();
                    }
                } finally {
                    kdb.close();
                }
            }
        } catch (Throwable ignored) {}

        try {
            File appLib = new File(ctx.getFilesDir(), "folio_library").getCanonicalFile();
            if (rec.kfxPath != null && !rec.kfxPath.isEmpty()) {
                File kfx = new File(rec.kfxPath).getCanonicalFile();
                if (kfx.getPath().startsWith(appLib.getPath())) {
                    kfx.delete();
                    File cov = new File(kfx.getAbsolutePath() + ".cover.jpg");
                    if (cov.exists()) cov.delete();
                    String sdrPath = kfx.getAbsolutePath().replaceAll("\\.kfx$", ".sdr");
                    File sdr = new File(sdrPath);
                    if (sdr.exists() && sdr.isDirectory()) {
                        File[] kids = sdr.listFiles();
                        if (kids != null) {
                            for (File kf : kids) kf.delete();
                        }
                        sdr.delete();
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static Map<String, Integer> loadProgressMap(Context ctx) {
        Map<String, Integer> map = new HashMap<>();
        try {
            File kdbFile = ctx.getDatabasePath("kindle_library.db");
            if (kdbFile == null || !kdbFile.exists()) return map;
            android.database.sqlite.SQLiteDatabase kdb =
                    android.database.sqlite.SQLiteDatabase.openDatabase(
                            kdbFile.getAbsolutePath(), null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
            android.database.Cursor c = null;
            try {
                c = kdb.rawQuery(
                        "SELECT l.FILE_PATH, COALESCE(u.READING_PROGRESS, 0), COALESCE(k.READ_STATE, 'UNREAD') "
                        + "FROM LocalContent l "
                        + "LEFT JOIN UserContent u ON u.KEY = l.KEY "
                        + "LEFT JOIN KindleContent k ON k.ID = l.KEY "
                        + "WHERE l.FILE_PATH IS NOT NULL", null);
                while (c.moveToNext()) {
                    String path = c.getString(0);
                    int prog = c.getInt(1);
                    String state = c.getString(2);
                    if ("READ".equalsIgnoreCase(state)) prog = 100;
                    if (path != null && prog > 0) {
                        map.put(path, Math.min(100, prog));
                    }
                }
            } finally {
                if (c != null) c.close();
                kdb.close();
            }
        } catch (Throwable ignored) {}
        return map;
    }

    public static String getShelf(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return "";
        try {
            String s = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE).getString(kfxPath, "");
            return s != null ? s.trim() : "";
        } catch (Throwable ignored) { return ""; }
    }

    public static void setShelf(Context ctx, String kfxPath, String shelf) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE).edit();
            String clean = shelf != null ? shelf.trim() : "";
            if (clean.isEmpty()) ed.remove(kfxPath);
            else ed.putString(kfxPath, clean);
            ed.apply();
        } catch (Throwable ignored) {}
    }

    private static String authorMonogram(String name) {
        if (name == null) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "?";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        char a = parts[0].charAt(0);
        char b = parts[parts.length - 1].charAt(0);
        return ("" + Character.toUpperCase(a) + Character.toUpperCase(b));
    }

    private static void showAuthorsDirectoryDialog(final Context ctx,
                                                   final List<FolioMetadataDb.BookRecord> allBooks,
                                                   final AuthorFilterCallback onPick) {
        final Map<String, Integer> progMap = loadProgressMap(ctx);
        final Map<String, Integer> counts = new HashMap<>();
        final Map<String, Integer> finishedMap = new HashMap<>();
        final Map<String, Integer> readingMap = new HashMap<>();
        final Map<String, Integer> ratedCntMap = new HashMap<>();
        final Map<String, Integer> ratedSumMap = new HashMap<>();
        final Map<String, String> labels = new HashMap<>();

        for (FolioMetadataDb.BookRecord r : allBooks) {
            String disp = formatAuthorForDisplay(r.author);
            if (disp.isEmpty()) disp = "Unknown Author";
            String key = disp.toLowerCase(Locale.ROOT);
            counts.put(key, (counts.containsKey(key) ? counts.get(key) : 0) + 1);
            if (!labels.containsKey(key)) labels.put(key, disp);

            Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
            int pct = pv != null ? pv : 0;
            if (pct >= 100) {
                finishedMap.put(key, (finishedMap.containsKey(key) ? finishedMap.get(key) : 0) + 1);
            } else if (pct > 0) {
                readingMap.put(key, (readingMap.containsKey(key) ? readingMap.get(key) : 0) + 1);
            }
            int stars = getRating(ctx, r.kfxPath);
            if (stars > 0) {
                ratedCntMap.put(key, (ratedCntMap.containsKey(key) ? ratedCntMap.get(key) : 0) + 1);
                ratedSumMap.put(key, (ratedSumMap.containsKey(key) ? ratedSumMap.get(key) : 0) + stars);
            }
        }

        final List<String> keys = new ArrayList<>(counts.keySet());
        // Sort mode: 0 = Most Books, 1 = A-Z, 2 = Most Read
        final int[] sortMode = new int[] { 0 };

        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(FolioTheme.bgModal());
        wrap.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 10));

        final TextView hdr = new TextView(ctx);
        hdr.setText("Authors Directory (" + keys.size() + ")");
        hdr.setTextColor(FolioTheme.textPrimary());
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 10));
        wrap.addView(hdr);

        final LinearLayout sortRow = new LinearLayout(ctx);
        sortRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        srLp.setMargins(0, 0, 0, dp(ctx, 10));
        sortRow.setLayoutParams(srLp);
        wrap.addView(sortRow);

        final EditText authSearch = new EditText(ctx);
        authSearch.setHint("Search authors...");
        authSearch.setHintTextColor(FolioTheme.textMuted());
        authSearch.setTextColor(FolioTheme.textPrimary());
        authSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        authSearch.setSingleLine(true);
        authSearch.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable asBg = new GradientDrawable();
        asBg.setColor(FolioTheme.bgSurface());
        asBg.setCornerRadius(dp(ctx, 10));
        asBg.setStroke(dp(ctx, 1), FolioTheme.border());
        authSearch.setBackground(asBg);
        LinearLayout.LayoutParams asLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        asLp.setMargins(0, 0, 0, dp(ctx, 10));
        authSearch.setLayoutParams(asLp);
        wrap.addView(authSearch);

        ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 340)));
        final LinearLayout list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        wrap.addView(sv);

        final AlertDialog[] dlgRef = new AlertDialog[1];
        final String[] authQuery = new String[] { "" };
        final Runnable[] renderAuthorsRef = new Runnable[1];
        final Runnable[] renderSortRef = new Runnable[1];

        renderSortRef[0] = () -> {
            sortRow.removeAllViews();
            String[] sLabels = { "Most Books", "A–Z", "Most Read" };
            for (int i = 0; i < sLabels.length; i++) {
                final int target = i;
                boolean active = (sortMode[0] == i);
                TextView pill = new TextView(ctx);
                pill.setText(sLabels[i]);
                pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                pill.setTextColor((active ? FolioTheme.accent() : FolioTheme.textMuted()));
                pill.setGravity(Gravity.CENTER);
                pill.setSingleLine(true);
                pill.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
                pill.setBackground(pillBg(ctx, active ? "#282520" : "#1C1E22", active ? "#524B41" : "#32353B", 9));
                LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                if (i < sLabels.length - 1) plp.rightMargin = dp(ctx, 6);
                pill.setLayoutParams(plp);
                pill.setOnClickListener(v -> {
                    if (sortMode[0] != target) {
                        sortMode[0] = target;
                        renderSortRef[0].run();
                        renderAuthorsRef[0].run();
                    }
                });
                sortRow.addView(pill);
            }
        };

        renderAuthorsRef[0] = () -> {
            Collections.sort(keys, (a, b) -> {
                if (sortMode[0] == 1) {
                    return a.compareTo(b);
                } else if (sortMode[0] == 2) {
                    int fa = (finishedMap.containsKey(a) ? finishedMap.get(a) : 0) * 10
                            + (readingMap.containsKey(a) ? readingMap.get(a) : 0) * 3;
                    int fb = (finishedMap.containsKey(b) ? finishedMap.get(b) : 0) * 10
                            + (readingMap.containsKey(b) ? readingMap.get(b) : 0) * 3;
                    if (fa != fb) return Integer.compare(fb, fa);
                }
                int ca = counts.get(a);
                int cb = counts.get(b);
                if (ca != cb) return Integer.compare(cb, ca);
                return a.compareTo(b);
            });

            list.removeAllViews();
            String q = authQuery[0].trim().toLowerCase(Locale.ROOT);
            int shown = 0;
            for (String k : keys) {
                final String name = labels.get(k);
                if (!q.isEmpty() && !k.contains(q)) continue;
                shown++;
                int n = counts.get(k);
                int fin = finishedMap.containsKey(k) ? finishedMap.get(k) : 0;
                int rdg = readingMap.containsKey(k) ? readingMap.get(k) : 0;
                int rCnt = ratedCntMap.containsKey(k) ? ratedCntMap.get(k) : 0;
                int rSum = ratedSumMap.containsKey(k) ? ratedSumMap.get(k) : 0;

                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(ctx, 11), dp(ctx, 9), dp(ctx, 12), dp(ctx, 9));
                GradientDrawable rBg = new GradientDrawable();
                rBg.setColor(FolioTheme.bgSurface());
                rBg.setCornerRadius(dp(ctx, 10));
                rBg.setStroke(dp(ctx, 1), ((fin > 0 || rdg > 0) ? FolioTheme.accentBorder() : FolioTheme.border()));
                row.setBackground(rBg);
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rLp.setMargins(0, 0, 0, dp(ctx, 8));
                row.setLayoutParams(rLp);

                TextView avTv = new TextView(ctx);
                avTv.setText(authorMonogram(name));
                avTv.setTextColor(((fin > 0 || rdg > 0) ? FolioTheme.accent() : FolioTheme.accent()));
                avTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                avTv.setTypeface(Typeface.DEFAULT_BOLD);
                avTv.setGravity(Gravity.CENTER);
                GradientDrawable avBg = new GradientDrawable();
                avBg.setShape(GradientDrawable.OVAL);
                avBg.setColor(((fin > 0 || rdg > 0) ? FolioTheme.accentFill() : FolioTheme.bgControl()));
                avBg.setStroke(dp(ctx, 1), ((fin > 0 || rdg > 0) ? FolioTheme.accentBorder() : FolioTheme.border()));
                avTv.setBackground(avBg);
                LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 32));
                avLp.rightMargin = dp(ctx, 10);
                avTv.setLayoutParams(avLp);
                row.addView(avTv);

                LinearLayout txtCol = new LinearLayout(ctx);
                txtCol.setOrientation(LinearLayout.VERTICAL);
                txtCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView nameTv = new TextView(ctx);
                nameTv.setText(name);
                nameTv.setTextColor(FolioTheme.textPrimary());
                nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
                nameTv.setTypeface(Typeface.DEFAULT_BOLD);
                nameTv.setMaxLines(1);
                nameTv.setEllipsize(TextUtils.TruncateAt.END);
                txtCol.addView(nameTv);

                StringBuilder sub = new StringBuilder();
                if (fin > 0) sub.append("✓ ").append(fin).append(" finished");
                if (rdg > 0) {
                    if (sub.length() > 0) sub.append(" · ");
                    sub.append("").append(rdg).append(" reading");
                }
                if (rCnt > 0) {
                    if (sub.length() > 0) sub.append(" · ");
                    sub.append(String.format(Locale.US, "★ %.1f", (double) rSum / (double) rCnt));
                }
                if (sub.length() == 0) {
                    sub.append(n == 1 ? "Unread" : (n + " unread"));
                }
                TextView subTv = new TextView(ctx);
                subTv.setText(sub.toString());
                subTv.setTextColor(((fin > 0 || rdg > 0) ? FolioTheme.accent() : FolioTheme.textMuted()));
                subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                subTv.setSingleLine(true);
                subTv.setEllipsize(TextUtils.TruncateAt.END);
                txtCol.addView(subTv);
                row.addView(txtCol);

                TextView cntTv = new TextView(ctx);
                cntTv.setText(n + (n == 1 ? " book ›" : " books ›"));
                cntTv.setTextColor(FolioTheme.accent());
                cntTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                cntTv.setTypeface(Typeface.DEFAULT_BOLD);
                cntTv.setPadding(dp(ctx, 8), 0, 0, 0);
                row.addView(cntTv);

                row.setOnClickListener(v -> {
                    if (dlgRef[0] != null) dlgRef[0].dismiss();
                    if (onPick != null) onPick.onFilterByAuthor(name);
                });
                list.addView(row);
            }
            hdr.setText(q.isEmpty()
                    ? "Authors Directory (" + keys.size() + ")"
                    : "Authors Directory (" + shown + " of " + keys.size() + ")");
        };
        renderSortRef[0].run();
        renderAuthorsRef[0].run();

        authSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                authQuery[0] = s != null ? s.toString() : "";
                renderAuthorsRef[0].run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(wrap)
                .setPositiveButton("Close", null)
                .show();
        FolioTheme.applyToDialog(ctx, dlgRef[0]);
    }

    private static void showShelfPickerDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                              final String currentShelf, final Runnable onSaved) {
        final String[] presets = new String[] { "Classics", "Philosophy", "Sci-Fi", "History", "To Read", "Reference" };
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(FolioTheme.bgSurface());
        wrap.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Assign Shelf · " + rec.title);
        hdr.setTextColor(FolioTheme.textPrimary());
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 10));
        wrap.addView(hdr);

        final AlertDialog[] pickerRef = new AlertDialog[1];

        HorizontalScrollView pScroll = new HorizontalScrollView(ctx);
        pScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout pRow = new LinearLayout(ctx);
        pRow.setOrientation(LinearLayout.HORIZONTAL);
        for (final String preset : presets) {
            boolean isCur = preset.equalsIgnoreCase(currentShelf);
            TextView pill = new TextView(ctx);
            pill.setText("" + preset);
            pill.setTextColor((isCur ? FolioTheme.bgModal() : FolioTheme.accent()));
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor((isCur ? FolioTheme.accent() : FolioTheme.bgControl()));
            bg.setCornerRadius(dp(ctx, 14));
            pill.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(ctx, 6), 0);
            pill.setLayoutParams(lp);
            pill.setOnClickListener(v -> {
                setShelf(ctx, rec.kfxPath, preset);
                if (pickerRef[0] != null) pickerRef[0].dismiss();
                Toast.makeText(ctx, "Added to shelf: " + preset, Toast.LENGTH_SHORT).show();
                if (onSaved != null) onSaved.run();
            });
            pRow.addView(pill);
        }
        pScroll.addView(pRow);
        LinearLayout.LayoutParams psLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        psLp.setMargins(0, 0, 0, dp(ctx, 12));
        pScroll.setLayoutParams(psLp);
        wrap.addView(pScroll);

        final EditText et = new EditText(ctx);
        et.setText(currentShelf);
        et.setHint("Or type a custom shelf name...");
        et.setHintTextColor(FolioTheme.textMuted());
        et.setTextColor(FolioTheme.textPrimary());
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setSingleLine(true);
        et.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(FolioTheme.bgSurface());
        etBg.setCornerRadius(dp(ctx, 10));
        etBg.setStroke(dp(ctx, 1), FolioTheme.border());
        et.setBackground(etBg);
        wrap.addView(et);

        AlertDialog.Builder b = new AlertDialog.Builder(ctx)
                .setView(wrap)
                .setPositiveButton("Save Shelf", (d, w) -> {
                    String val = et.getText().toString().trim();
                    setShelf(ctx, rec.kfxPath, val);
                    Toast.makeText(ctx, val.isEmpty() ? "Removed from shelf" : "Saved shelf: " + val, Toast.LENGTH_SHORT).show();
                    if (onSaved != null) onSaved.run();
                })
                .setNegativeButton("Cancel", null);
        if (!currentShelf.isEmpty()) {
            b.setNeutralButton("Remove Shelf", (d, w) -> {
                setShelf(ctx, rec.kfxPath, "");
                Toast.makeText(ctx, "Removed from shelf", Toast.LENGTH_SHORT).show();
                if (onSaved != null) onSaved.run();
            });
        }
        pickerRef[0] = b.show();
        FolioTheme.applyToDialog(ctx, pickerRef[0]);
    }

    public static int getRating(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return 0;
        try {
            int r = ctx.getSharedPreferences("folio_ratings", Context.MODE_PRIVATE).getInt(kfxPath, 0);
            return (r >= 0 && r <= 5) ? r : 0;
        } catch (Throwable ignored) { return 0; }
    }

    public static void setRating(Context ctx, String kfxPath, int stars) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_ratings", Context.MODE_PRIVATE).edit();
            if (stars <= 0) ed.remove(kfxPath);
            else ed.putInt(kfxPath, Math.min(5, stars));
            ed.apply();
        } catch (Throwable ignored) {}
    }

    public static String getNote(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return "";
        try {
            String n = ctx.getSharedPreferences("folio_notes", Context.MODE_PRIVATE).getString(kfxPath, "");
            return n != null ? n.trim() : "";
        } catch (Throwable ignored) { return ""; }
    }

    public static void setNote(Context ctx, String kfxPath, String note) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_notes", Context.MODE_PRIVATE).edit();
            String clean = note != null ? note.trim() : "";
            if (clean.isEmpty()) ed.remove(kfxPath);
            else ed.putString(kfxPath, clean);
            ed.apply();
        } catch (Throwable ignored) {}
    }

    public static int countNotes(Context ctx) {
        if (ctx == null) return 0;
        try {
            int n = 0;
            for (Object v : ctx.getSharedPreferences("folio_notes", Context.MODE_PRIVATE).getAll().values()) {
                if (v instanceof String && !((String) v).trim().isEmpty()) n++;
            }
            return n;
        } catch (Throwable ignored) { return 0; }
    }

    private static void showEditNoteDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                           final String currentNote, final Runnable onSaved) {
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(FolioTheme.bgSurface());
        wrap.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Personal Note · " + rec.title);
        hdr.setTextColor(FolioTheme.textPrimary());
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 10));
        wrap.addView(hdr);

        final EditText et = new EditText(ctx);
        et.setText(currentNote);
        et.setHint("Write your thoughts, quotes, or reading reminders...");
        et.setHintTextColor(FolioTheme.textMuted());
        et.setTextColor(FolioTheme.textPrimary());
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setMinLines(3);
        et.setMaxLines(8);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(FolioTheme.bgSurface());
        etBg.setCornerRadius(dp(ctx, 10));
        etBg.setStroke(dp(ctx, 1), FolioTheme.border());
        et.setBackground(etBg);
        wrap.addView(et);

        AlertDialog.Builder b = new AlertDialog.Builder(ctx)
                .setView(wrap)
                .setPositiveButton("Save Note", (d, w) -> {
                    setNote(ctx, rec.kfxPath, et.getText().toString());
                    Toast.makeText(ctx, "Saved personal note", Toast.LENGTH_SHORT).show();
                    if (onSaved != null) onSaved.run();
                })
                .setNegativeButton("Cancel", null);
        if (!currentNote.isEmpty()) {
            b.setNeutralButton("Clear Note", (d, w) -> {
                setNote(ctx, rec.kfxPath, "");
                Toast.makeText(ctx, "Cleared personal note", Toast.LENGTH_SHORT).show();
                if (onSaved != null) onSaved.run();
            });
        }
        AlertDialog noteDlg = b.show();
        FolioTheme.applyToDialog(ctx, noteDlg);
    }

    public static boolean isFavorite(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return false;
        try {
            return ctx.getSharedPreferences("folio_favorites", Context.MODE_PRIVATE)
                    .getBoolean(kfxPath, false);
        } catch (Throwable ignored) { return false; }
    }

    public static boolean isInQueue(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return false;
        try {
            return ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).getLong(kfxPath, 0L) > 0L;
        } catch (Throwable ignored) { return false; }
    }

    public static long getQueueTimestamp(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return 0L;
        try {
            return ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).getLong(kfxPath, 0L);
        } catch (Throwable ignored) { return 0L; }
    }

    public static void setInQueue(Context ctx, String kfxPath, boolean queued) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).edit();
            if (queued) ed.putLong(kfxPath, System.currentTimeMillis());
            else ed.remove(kfxPath);
            ed.apply();
        } catch (Throwable ignored) {}
    }

    public static int countQueue(Context ctx) {
        if (ctx == null) return 0;
        try {
            Map<String, ?> all = ctx.getSharedPreferences("folio_queue", Context.MODE_PRIVATE).getAll();
            int n = 0;
            if (all != null) {
                for (Object v : all.values()) {
                    if (v instanceof Long && ((Long) v) > 0L) n++;
                }
            }
            return n;
        } catch (Throwable ignored) { return 0; }
    }

    public static void setFavorite(Context ctx, String kfxPath, boolean fav) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_favorites", Context.MODE_PRIVATE).edit();
            if (fav) ed.putBoolean(kfxPath, true);
            else ed.remove(kfxPath);
            ed.apply();
        } catch (Throwable ignored) {}
    }

    public static int countFavorites(Context ctx) {
        if (ctx == null) return 0;
        try {
            int n = 0;
            for (Object v : ctx.getSharedPreferences("folio_favorites", Context.MODE_PRIVATE).getAll().values()) {
                if (Boolean.TRUE.equals(v)) n++;
            }
            return n;
        } catch (Throwable ignored) { return 0; }
    }

    private static TextView makeBulkActionPill(Context ctx, String label, String bgHex, String textHex, String strokeHex) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(Color.parseColor(textHex));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setSingleLine(true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(ctx, 4), dp(ctx, 7), dp(ctx, 4), dp(ctx, 7));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgHex));
        bg.setCornerRadius(dp(ctx, 8));
        bg.setStroke(dp(ctx, 1), Color.parseColor(strokeHex));
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(ctx, 3), 0, dp(ctx, 3), 0);
        tv.setLayoutParams(lp);
        return tv;
    }

    private static void applyBulkSelectionUi(
            final Context ctx,
            final View cardView,
            final FolioMetadataDb.BookRecord rec,
            final java.util.Set<String> selectedPaths,
            final TextView bulkCountTv,
            final TextView btnSelectAll,
            final List<FolioMetadataDb.BookRecord> lastFiltered,
            final Runnable rerender) {
        final boolean isSel = rec.kfxPath != null && selectedPaths.contains(rec.kfxPath);
        GradientDrawable selBg = new GradientDrawable();
        selBg.setColor(isSel ? FolioTheme.bgControl() : FolioTheme.bgSurface());
        selBg.setCornerRadius(dp(ctx, 10));
        selBg.setStroke(dp(ctx, isSel ? 2 : 1), (isSel ? FolioTheme.accent() : FolioTheme.border()));
        cardView.setBackground(selBg);
        cardView.setOnClickListener(v -> {
            if (rec.kfxPath == null || rec.kfxPath.isEmpty()) return;
            if (selectedPaths.contains(rec.kfxPath)) {
                selectedPaths.remove(rec.kfxPath);
            } else {
                selectedPaths.add(rec.kfxPath);
            }
            int nSel = selectedPaths.size();
            bulkCountTv.setText(nSel == 0
                    ? "0 selected — tap books to select"
                    : nSel + (nSel == 1 ? " book selected" : " books selected"));
            boolean allVisSelected = !lastFiltered.isEmpty() && nSel >= lastFiltered.size();
            btnSelectAll.setText(allVisSelected ? "Clear All" : "Select All (" + lastFiltered.size() + ")");
            rerender.run();
        });
    }

    private static void showBulkShelfPicker(final Context ctx, final List<String> paths, final Runnable onDone) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 18), dp(ctx, 14), dp(ctx, 18), dp(ctx, 8));
        box.setBackgroundColor(FolioTheme.bgModal());

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Assign Shelf (" + paths.size() + (paths.size() == 1 ? " book)" : " books)"));
        titleTv.setTextColor(FolioTheme.textPrimary());
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(ctx, 10));
        box.addView(titleTv);

        final AlertDialog[] dlgRef = new AlertDialog[1];

        java.util.LinkedHashSet<String> choices = new java.util.LinkedHashSet<>();
        try {
            Map<String, ?> existing = ctx.getSharedPreferences("folio_shelves", Context.MODE_PRIVATE).getAll();
            if (existing != null) {
                for (Object val : existing.values()) {
                    if (val instanceof String && !((String) val).trim().isEmpty()) {
                        choices.add(((String) val).trim());
                    }
                }
            }
        } catch (Throwable ignored) {}
        String[] presets = new String[] { "Classics", "Sci-Fi", "Philosophy", "History", "To Read" };
        for (String p : presets) choices.add(p);

        HorizontalScrollView hsv = new HorizontalScrollView(ctx);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout presetRow = new LinearLayout(ctx);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        for (final String sh : choices) {
            TextView pill = new TextView(ctx);
            pill.setText("" + sh);
            pill.setTextColor(FolioTheme.textPrimary());
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setPadding(dp(ctx, 11), dp(ctx, 6), dp(ctx, 11), dp(ctx, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(FolioTheme.bgControl());
            pBg.setCornerRadius(dp(ctx, 14));
            pBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
            pill.setBackground(pBg);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pLp.setMargins(0, 0, dp(ctx, 6), 0);
            pill.setLayoutParams(pLp);
            pill.setOnClickListener(v -> {
                for (String kfx : paths) setShelf(ctx, kfx, sh);
                Toast.makeText(ctx, "Added " + paths.size() + " book(s) to shelf: " + sh, Toast.LENGTH_SHORT).show();
                if (dlgRef[0] != null) dlgRef[0].dismiss();
                onDone.run();
            });
            presetRow.addView(pill);
        }
        hsv.addView(presetRow);
        LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hsvLp.setMargins(0, 0, 0, dp(ctx, 12));
        hsv.setLayoutParams(hsvLp);
        box.addView(hsv);

        final EditText customInput = new EditText(ctx);
        customInput.setHint("Or type a custom shelf name...");
        customInput.setHintTextColor(FolioTheme.textMuted());
        customInput.setTextColor(FolioTheme.textPrimary());
        customInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        customInput.setSingleLine(true);
        box.addView(customInput);

        dlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(box)
                .setPositiveButton("Apply Custom", (d, w) -> {
                    String val = customInput.getText().toString().trim();
                    if (val.isEmpty()) return;
                    for (String kfx : paths) setShelf(ctx, kfx, val);
                    Toast.makeText(ctx, "Added " + paths.size() + " book(s) to shelf: " + val, Toast.LENGTH_SHORT).show();
                    onDone.run();
                })
                .setNeutralButton("Clear Shelf", (d, w) -> {
                    for (String kfx : paths) setShelf(ctx, kfx, "");
                    Toast.makeText(ctx, "Cleared shelf from " + paths.size() + " book(s)", Toast.LENGTH_SHORT).show();
                    onDone.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, dlgRef[0]);
    }

    private static void showBulkGenrePicker(
            final Context ctx,
            final List<FolioMetadataDb.BookRecord> recs,
            final List<String> existingGenres,
            final Runnable onDone) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 18), dp(ctx, 14), dp(ctx, 18), dp(ctx, 8));
        box.setBackgroundColor(FolioTheme.bgModal());

        TextView titleTv = new TextView(ctx);
        titleTv.setText("Add Genre (" + recs.size() + (recs.size() == 1 ? " book)" : " books)"));
        titleTv.setTextColor(FolioTheme.textPrimary());
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setPadding(0, 0, 0, dp(ctx, 4));
        box.addView(titleTv);

        TextView subTv = new TextView(ctx);
        subTv.setText("Merges cleanly with existing genres (duplicates skipped)");
        subTv.setTextColor(FolioTheme.textMuted());
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        subTv.setPadding(0, 0, 0, dp(ctx, 10));
        box.addView(subTv);

        final AlertDialog[] dlgRef = new AlertDialog[1];

        java.util.LinkedHashSet<String> choices = new java.util.LinkedHashSet<>();
        if (existingGenres != null) {
            for (String g : existingGenres) {
                if (g != null && !g.trim().isEmpty() && !"All".equalsIgnoreCase(g.trim())) {
                    choices.add(g.trim());
                }
            }
        }
        String[] defaults = new String[] { "Fiction", "History", "Philosophy", "Rome", "Biography", "Science Fiction", "Classics" };
        for (String d : defaults) choices.add(d);

        HorizontalScrollView hsv = new HorizontalScrollView(ctx);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout chipRow = new LinearLayout(ctx);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        int shown = 0;
        for (final String gName : choices) {
            if (shown++ >= 14) break;
            TextView pill = new TextView(ctx);
            pill.setText("+ " + gName);
            pill.setTextColor(FolioTheme.accent());
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setPadding(dp(ctx, 11), dp(ctx, 6), dp(ctx, 11), dp(ctx, 6));
            GradientDrawable pBg = new GradientDrawable();
            pBg.setColor(FolioTheme.accentFill());
            pBg.setCornerRadius(dp(ctx, 14));
            pBg.setStroke(dp(ctx, 1), FolioTheme.accentBorder());
            pill.setBackground(pBg);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pLp.setMargins(0, 0, dp(ctx, 6), 0);
            pill.setLayoutParams(pLp);
            pill.setOnClickListener(v -> {
                int updated = applyBulkGenreMerge(ctx, recs, gName);
                Toast.makeText(ctx, "Added \"" + gName + "\" to " + updated + (updated == 1 ? " book" : " books"), Toast.LENGTH_SHORT).show();
                if (dlgRef[0] != null) dlgRef[0].dismiss();
                onDone.run();
            });
            chipRow.addView(pill);
        }
        hsv.addView(chipRow);
        LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hsvLp.setMargins(0, 0, 0, dp(ctx, 12));
        hsv.setLayoutParams(hsvLp);
        box.addView(hsv);

        final EditText customInput = new EditText(ctx);
        customInput.setHint("Or type genre(s), comma-separated...");
        customInput.setHintTextColor(FolioTheme.textMuted());
        customInput.setTextColor(FolioTheme.textPrimary());
        customInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        customInput.setSingleLine(true);
        box.addView(customInput);

        dlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(box)
                .setPositiveButton("Apply Genre", (d, w) -> {
                    String val = customInput.getText().toString().trim();
                    if (val.isEmpty()) return;
                    int updated = applyBulkGenreMerge(ctx, recs, val);
                    Toast.makeText(ctx, "Added \"" + val + "\" to " + updated + (updated == 1 ? " book" : " books"), Toast.LENGTH_SHORT).show();
                    onDone.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, dlgRef[0]);
    }

    private static int applyBulkGenreMerge(Context ctx, List<FolioMetadataDb.BookRecord> recs, String toAddCsv) {
        if (recs == null || recs.isEmpty() || toAddCsv == null || toAddCsv.trim().isEmpty()) return 0;
        List<String> incoming = new ArrayList<>();
        for (String part : toAddCsv.split(",")) {
            String p = part.trim();
            if (!p.isEmpty()) incoming.add(p);
        }
        if (incoming.isEmpty()) return 0;
        int count = 0;
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            android.database.sqlite.SQLiteDatabase wdb = db.getWritableDatabase();
            for (FolioMetadataDb.BookRecord r : recs) {
                java.util.LinkedHashMap<String, String> merged = new java.util.LinkedHashMap<>();
                if (r.genres != null && !r.genres.trim().isEmpty()) {
                    for (String ex : r.genres.split(",")) {
                        String e = ex.trim();
                        if (!e.isEmpty()) merged.put(e.toLowerCase(Locale.ROOT), e);
                    }
                }
                for (String inc : incoming) {
                    String k = inc.toLowerCase(Locale.ROOT);
                    if (!merged.containsKey(k)) merged.put(k, inc);
                }
                StringBuilder sb = new StringBuilder();
                for (String val : merged.values()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(val);
                }
                ContentValues cv = new ContentValues();
                cv.put("genres", sb.toString());
                wdb.update("books_metadata", cv, "id = ?", new String[] { String.valueOf(r.id) });
                count++;
            }
        } finally {
            db.close();
        }
        return count;
    }

    public static int parseBookYear(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) return -1;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(1[5-9]\\d{2}|20\\d{2})").matcher(pubDate);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Throwable ignored) {}
        }
        return -1;
    }

    public static String eraBucketForYear(int yr) {
        if (yr < 0) return "";
        if (yr < 1900) return "Pre-1900";
        if (yr < 1950) return "1900–1949";
        if (yr < 2000) return "1950–1999";
        if (yr < 2010) return "2000–2009";
        if (yr < 2020) return "2010–2019";
        return "2020+";
    }

    private static boolean matchesEraFilter(FolioMetadataDb.BookRecord rec, String targetEra) {
        if (rec == null || targetEra == null) return false;
        int yr = parseBookYear(rec.pubDate);
        return targetEra.equals(eraBucketForYear(yr));
    }

    private static int countEraMatches(List<FolioMetadataDb.BookRecord> all, String targetEra) {
        int n = 0;
        for (FolioMetadataDb.BookRecord r : all) {
            if (matchesEraFilter(r, targetEra)) n++;
        }
        return n;
    }

    private static boolean matchesStarFilter(Context ctx, FolioMetadataDb.BookRecord rec, String starStr) {
        if (rec == null || starStr == null) return false;
        try {
            int target = Integer.parseInt(starStr.trim());
            return getRating(ctx, rec.kfxPath) == target;
        } catch (Throwable ignored) { return false; }
    }

    private static int countStarMatches(Context ctx, List<FolioMetadataDb.BookRecord> all, String starStr) {
        int n = 0;
        for (FolioMetadataDb.BookRecord r : all) {
            if (matchesStarFilter(ctx, r, starStr)) n++;
        }
        return n;
    }

    private static boolean matchesAuditFilter(FolioMetadataDb.BookRecord rec, String auditKey) {
        if ("MISSING_COVER".equalsIgnoreCase(auditKey)) {
            if (rec.kfxPath == null || rec.kfxPath.isEmpty()) return true;
            File cov = new File(rec.kfxPath + ".cover.jpg");
            return !cov.exists() || cov.length() < 64;
        }
        if ("MISSING_GENRES".equalsIgnoreCase(auditKey)) {
            if (rec.genres == null || rec.genres.trim().isEmpty()) return true;
            for (String g : rec.genres.split(",")) {
                if (!g.trim().isEmpty()) return false;
            }
            return true;
        }
        if ("MISSING_SYNOPSIS".equalsIgnoreCase(auditKey)) {
            return rec.description == null || rec.description.trim().isEmpty();
        }
        return false;
    }

    private static int countAuditMatches(List<FolioMetadataDb.BookRecord> all, String auditKey) {
        int n = 0;
        for (FolioMetadataDb.BookRecord r : all) {
            if (matchesAuditFilter(r, auditKey)) n++;
        }
        return n;
    }

    public static String getFinishedDate(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return "";
        try {
            String s = ctx.getSharedPreferences("folio_finished_dates", Context.MODE_PRIVATE).getString(kfxPath, "");
            return s != null ? s.trim() : "";
        } catch (Throwable ignored) { return ""; }
    }

    public static void setFinishedDate(Context ctx, String kfxPath, String dateStr) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return;
        try {
            android.content.SharedPreferences.Editor ed =
                    ctx.getSharedPreferences("folio_finished_dates", Context.MODE_PRIVATE).edit();
            if (dateStr == null || dateStr.trim().isEmpty()) ed.remove(kfxPath);
            else ed.putString(kfxPath, dateStr.trim());
            ed.apply();
        } catch (Throwable ignored) {}
    }

    private static void showEditFinishedDateDialog(
            final Context ctx, final FolioMetadataDb.BookRecord rec,
            final String curDate, final Runnable onSaved) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 20), dp(ctx, 12), dp(ctx, 20), dp(ctx, 6));

        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
        final EditText input = new EditText(ctx);
        input.setText(curDate.isEmpty() ? today : curDate);
        input.setSelection(input.getText().length());
        input.setSingleLine(true);
        input.setHint("YYYY-MM-DD (e.g. " + today + ")");
        input.setTextColor(FolioTheme.textPrimary());
        input.setHintTextColor(FolioTheme.textMuted());
        box.addView(input);

        AlertDialog dateDlg = new AlertDialog.Builder(ctx)
                .setTitle("Finished Date — " + (rec.title != null ? rec.title : "Book"))
                .setView(box)
                .setPositiveButton("Save Date", (d, w) -> {
                    String val = input.getText().toString().trim();
                    setFinishedDate(ctx, rec.kfxPath, val);
                    Toast.makeText(ctx, val.isEmpty() ? "Cleared finished date" : "Saved finished date: " + val, Toast.LENGTH_SHORT).show();
                    if (onSaved != null) onSaved.run();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    setFinishedDate(ctx, rec.kfxPath, "");
                    Toast.makeText(ctx, "Cleared finished date", Toast.LENGTH_SHORT).show();
                    if (onSaved != null) onSaved.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, dateDlg);
    }

    private static void setBookReadState(Context ctx, String kfxPath, boolean finished) {
        if (kfxPath == null || kfxPath.isEmpty()) return;
        try {
            File kdbFile = ctx.getDatabasePath("kindle_library.db");
            if (kdbFile == null || !kdbFile.exists()) return;
            android.database.sqlite.SQLiteDatabase kdb =
                    android.database.sqlite.SQLiteDatabase.openDatabase(
                            kdbFile.getAbsolutePath(), null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
            try {
                String key = null;
                android.database.Cursor c = kdb.rawQuery(
                        "SELECT KEY FROM LocalContent WHERE FILE_PATH = ?",
                        new String[] { kfxPath });
                try {
                    if (c.moveToFirst()) key = c.getString(0);
                } finally {
                    c.close();
                }
                if (key != null && !key.isEmpty()) {
                    kdb.execSQL("UPDATE KindleContent SET READ_STATE = ? WHERE ID = ?",
                            new Object[] { finished ? "READ" : "UNREAD", key });
                    kdb.execSQL("UPDATE UserContent SET READING_PROGRESS = ? WHERE KEY = ?",
                            new Object[] { finished ? 100 : 0, key });
                }
                if (finished) {
                    if (getFinishedDate(ctx, kfxPath).isEmpty()) {
                        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                .format(new java.util.Date());
                        setFinishedDate(ctx, kfxPath, today);
                    }
                } else {
                    setFinishedDate(ctx, kfxPath, "");
                }
            } finally {
                kdb.close();
            }
        } catch (Throwable ignored) {}
    }

    private static int getProgress(Map<String, Integer> map, String kfxPath) {
        if (map == null || kfxPath == null) return 0;
        Integer v = map.get(kfxPath);
        return v != null ? v : 0;
    }

    private static boolean isCatalogMetaTag(String tag) {
        if (tag == null) return true;
        String lower = tag.trim().toLowerCase(Locale.ROOT);
        return lower.isEmpty()
                || lower.startsWith("early works")
                || lower.startsWith("translations into")
                || lower.contains("textbooks for")
                || lower.contains("protected daisy")
                || lower.contains("accessible book")
                || lower.contains("in library")
                || lower.contains("internet archive")
                || lower.contains("lending library")
                || lower.equals("general");
    }

    private static String cleanGenresForDisplay(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        Set<String> seen = new LinkedHashSet<>();
        List<String> clean = new ArrayList<>();
        List<String> fallback = new ArrayList<>();
        for (String part : raw.split(",")) {
            String c = part.trim();
            if (c.isEmpty() || EpubMetadataExtractor.isNoiseTag(c) || isCatalogMetaTag(c)) continue;
            String key = c.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) continue;
            if (!c.contains("--")) {
                clean.add(toTitleCase(c));
            } else {
                fallback.add(c);
            }
        }
        List<String> chosen = !clean.isEmpty() ? clean : fallback;
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(chosen.size(), 6);
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(", ");
            sb.append(chosen.get(i));
        }
        return sb.toString();
    }

    private static int countBooksInGenre(List<FolioMetadataDb.BookRecord> books, String genre) {
        int n = 0;
        for (FolioMetadataDb.BookRecord r : books) {
            if (r.hasGenre(genre)) n++;
        }
        return n;
    }

    public static String formatAuthorForDisplay(String raw) {
        if (raw == null) return "";
        String a = raw.trim();
        if (!a.contains(";") && a.contains(",")) {
            String[] parts = a.split(",");
            if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()
                    && parts[0].trim().split("\\s+").length <= 2
                    && parts[1].trim().split("\\s+").length <= 2) {
                return parts[1].trim() + " " + parts[0].trim();
            }
        }
        return a;
    }

    private static int dp(Context ctx, int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics()));
    }
}
