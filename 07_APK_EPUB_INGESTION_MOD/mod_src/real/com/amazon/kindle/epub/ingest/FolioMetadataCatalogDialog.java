package com.amazon.kindle.epub.ingest;

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

    private static final String[] SORT_MODES = new String[] { "Recent", "Title", "Author", "Year", "Progress", "Favorites", "Rating" };

    private FolioMetadataCatalogDialog() {}

    public static void show(final Context ctx) {
        show(ctx, "All", null);
    }

    public static void show(final Context ctx, final String initialFilter) {
        show(ctx, initialFilter, null);
    }

    public static void show(final Context ctx, final String initialFilter, final Runnable onDismiss) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final List<FolioMetadataDb.BookRecord> allBooks = new ArrayList<>();
        final List<String> genres = new ArrayList<>();

        final Map<String, Integer> progressMap = loadProgressMap(ctx);
        loadFromDb(ctx, allBooks, genres);

        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#11131A"));
        root.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 12));

        // Top row: Eyebrow + Sort pill + Sync pill
        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView eyebrow = new TextView(ctx);
        eyebrow.setText("FOLIO METADATA");
        eyebrow.setTextColor(Color.parseColor("#F59E0B"));
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(eyebrow);

        final TextView btnRandom = new TextView(ctx);
        btnRandom.setText("🎲");
        btnRandom.setTextColor(Color.parseColor("#E2E8F0"));
        btnRandom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        btnRandom.setPadding(dp(ctx, 8), dp(ctx, 4), dp(ctx, 8), dp(ctx, 4));
        GradientDrawable randBg = new GradientDrawable();
        randBg.setColor(Color.parseColor("#1E2433"));
        randBg.setCornerRadius(dp(ctx, 14));
        randBg.setStroke(dp(ctx, 1), Color.parseColor("#334155"));
        btnRandom.setBackground(randBg);
        LinearLayout.LayoutParams randLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        randLp.setMargins(0, 0, dp(ctx, 6), 0);
        btnRandom.setLayoutParams(randLp);
        topRow.addView(btnRandom);

        final TextView btnSort = new TextView(ctx);
        btnSort.setText("Sort: Recent");
        btnSort.setTextColor(Color.parseColor("#E2E8F0"));
        btnSort.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSort.setTypeface(Typeface.DEFAULT_BOLD);
        btnSort.setPadding(dp(ctx, 9), dp(ctx, 5), dp(ctx, 9), dp(ctx, 5));
        GradientDrawable sortBg = new GradientDrawable();
        sortBg.setColor(Color.parseColor("#1E2433"));
        sortBg.setCornerRadius(dp(ctx, 14));
        sortBg.setStroke(dp(ctx, 1), Color.parseColor("#334155"));
        btnSort.setBackground(sortBg);
        LinearLayout.LayoutParams sortLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sortLp.setMargins(0, 0, dp(ctx, 6), 0);
        btnSort.setLayoutParams(sortLp);
        topRow.addView(btnSort);

        final TextView btnSync = new TextView(ctx);
        btnSync.setText("Sync & Enrich");
        btnSync.setTextColor(Color.parseColor("#111827"));
        btnSync.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSync.setTypeface(Typeface.DEFAULT_BOLD);
        btnSync.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable syncBg = new GradientDrawable();
        syncBg.setColor(Color.parseColor("#34D399"));
        syncBg.setCornerRadius(dp(ctx, 14));
        btnSync.setBackground(syncBg);
        topRow.addView(btnSync);
        root.addView(topRow);

        final TextView heading = new TextView(ctx);
        heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setPadding(0, dp(ctx, 4), 0, dp(ctx, 10));
        root.addView(heading);

        // Search box + inline Clear (✕) button
        LinearLayout searchRow = new LinearLayout(ctx);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.parseColor("#1A1F2E"));
        searchBg.setCornerRadius(dp(ctx, 10));
        searchBg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
        searchRow.setBackground(searchBg);
        LinearLayout.LayoutParams searchRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchRowLp.setMargins(0, 0, 0, dp(ctx, 10));
        searchRow.setLayoutParams(searchRowLp);

        final EditText searchBox = new EditText(ctx);
        searchBox.setHint("Filter by title, author, genre, or ISBN...");
        searchBox.setHintTextColor(Color.parseColor("#64748B"));
        searchBox.setTextColor(Color.WHITE);
        searchBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        searchBox.setSingleLine(true);
        searchBox.setBackgroundColor(Color.TRANSPARENT);
        searchBox.setPadding(dp(ctx, 12), dp(ctx, 9), dp(ctx, 6), dp(ctx, 9));
        searchBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        searchRow.addView(searchBox);

        final TextView btnClearSearch = new TextView(ctx);
        btnClearSearch.setText("✕");
        btnClearSearch.setTextColor(Color.parseColor("#94A3B8"));
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
        divider.setBackgroundColor(Color.parseColor("#242938"));
        root.addView(divider);

        // Book cards container
        ScrollView listScroll = new ScrollView(ctx);
        listScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 390)));
        final LinearLayout cardsContainer = new LinearLayout(ctx);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(cardsContainer);
        root.addView(listScroll);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .create();
        if (onDismiss != null) {
            dialog.setOnDismissListener(d -> onDismiss.run());
        }

        final String[] selectedGenre = new String[] { (initialFilter != null && !initialFilter.trim().isEmpty()) ? initialFilter.trim() : "All" };
        final String[] queryText = new String[] { "" };
        int savedSort = 0;
        try {
            savedSort = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getInt("catalog_sort_idx", 0);
            if (savedSort < 0 || savedSort >= SORT_MODES.length) savedSort = 0;
        } catch (Throwable ignored) {}
        final int[] sortIdx = new int[] { savedSort };

        final List<FolioMetadataDb.BookRecord> lastFiltered = new ArrayList<>();
        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
                btnSort.setText("Sort: " + SORT_MODES[sortIdx[0]]);

                int readingCount = 0;
                int finishedCount = 0;
                int unreadCount = 0;
                int favCount = 0;
                int notesCount = 0;
                final Map<String, Integer> shelfCounts = new LinkedHashMap<>();
                for (FolioMetadataDb.BookRecord rec : allBooks) {
                    int p = getProgress(progressMap, rec.kfxPath);
                    if (p >= 100) finishedCount++;
                    else if (p > 0) readingCount++;
                    else unreadCount++;
                    if (isFavorite(ctx, rec.kfxPath)) favCount++;
                    if (!getNote(ctx, rec.kfxPath).isEmpty()) notesCount++;
                    String sh = getShelf(ctx, rec.kfxPath);
                    if (!sh.isEmpty()) {
                        shelfCounts.put(sh, (shelfCounts.containsKey(sh) ? shelfCounts.get(sh) : 0) + 1);
                    }
                }
                List<String> chipList = new ArrayList<>();
                chipList.add("All");
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
                    boolean isFavChip = "Favorites".equalsIgnoreCase(g);
                    boolean isNotesChip = "Notes".equalsIgnoreCase(g);
                    boolean isShelfChip = g.startsWith("SHELF:");
                    boolean isStatusChip = "Reading".equalsIgnoreCase(g)
                            || "Finished".equalsIgnoreCase(g)
                            || "Unread".equalsIgnoreCase(g);
                    String labelText = isShelfChip ? g.substring(6) : g;
                    int gCount;
                    if ("All".equalsIgnoreCase(g)) gCount = allBooks.size();
                    else if (isFavChip) gCount = favCount;
                    else if (isNotesChip) gCount = notesCount;
                    else if (isShelfChip) gCount = shelfCounts.containsKey(labelText) ? shelfCounts.get(labelText) : 0;
                    else if ("Reading".equalsIgnoreCase(g)) gCount = readingCount;
                    else if ("Finished".equalsIgnoreCase(g)) gCount = finishedCount;
                    else if ("Unread".equalsIgnoreCase(g)) gCount = unreadCount;
                    else gCount = countBooksInGenre(allBooks, g);

                    TextView chip = new TextView(ctx);
                    String chipPrefix = isFavChip ? "★ " : (isNotesChip ? "📝 " : (isShelfChip ? "🏷 " : ""));
                    chip.setText(chipPrefix + labelText + " (" + gCount + ")");
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    chip.setTextColor(Color.parseColor(active ? "#111827"
                            : ((isFavChip || isNotesChip) ? "#FBBF24"
                            : (isShelfChip ? "#A5B4FC"
                            : (isStatusChip ? "#5EEAD4" : "#CBD5E1")))));
                    chip.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor(active
                            ? ((isFavChip || isNotesChip) ? "#FBBF24"
                            : (isShelfChip ? "#818CF8"
                            : (isStatusChip ? "#14B8A6" : "#F59E0B")))
                            : "#1E2433"));
                    bg.setCornerRadius(dp(ctx, 16));
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
                    if ("Favorites".equalsIgnoreCase(selectedGenre[0])) {
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

                sortBooks(ctx, filtered, sortIdx[0], progressMap);
                lastFiltered.clear();
                lastFiltered.addAll(filtered);

                final AuthorFilterCallback onAuthorFilter = author -> {
                    selectedGenre[0] = "All";
                    searchBox.setText(author);
                    searchBox.setSelection(author.length());
                };
                cardsContainer.removeAllViews();
                for (final FolioMetadataDb.BookRecord rec : filtered) {
                    cardsContainer.addView(buildBookCard(ctx, rec, progressMap, dialog, () -> {
                        loadFromDb(ctx, allBooks, genres);
                        progressMap.clear();
                        progressMap.putAll(loadProgressMap(ctx));
                        renderRef[0].run();
                    }, onAuthorFilter));
                }

                if (filtered.isEmpty()) {
                    TextView empty = new TextView(ctx);
                    empty.setText(allBooks.isEmpty()
                            ? "No books indexed yet. Tap \"Sync & Enrich\" above to index your Folio library."
                            : "No books match your current filter.");
                    empty.setTextColor(Color.parseColor("#94A3B8"));
                    empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    empty.setPadding(dp(ctx, 8), dp(ctx, 20), dp(ctx, 8), dp(ctx, 20));
                    cardsContainer.addView(empty);
                }
            }
        };

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
            sortIdx[0] = (sortIdx[0] + 1) % SORT_MODES.length;
            try {
                ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("catalog_sort_idx", sortIdx[0]).apply();
            } catch (Throwable ignored) {}
            renderRef[0].run();
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

        renderRef[0].run();
        dialog.show();
    }

    private static void sortBooks(final Context ctx, List<FolioMetadataDb.BookRecord> list, final int mode, final Map<String, Integer> progressMap) {
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
                }
                // Default 0: Recent (descending addedAt)
                return Long.compare(b.addedAt, a.addedAt);
            }
        });
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
            if (r.genres == null || r.genres.isEmpty()) continue;
            Set<String> seenInBook = new LinkedHashSet<>();
            for (String g : r.genres.split(",")) {
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
        for (int i = 0; i < sortedKeys.size() && outGenres.size() < 12; i++) {
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

    private static View buildBookCard(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                      final Map<String, Integer> progressMap,
                                      final AlertDialog parentDialog, final Runnable onMetadataEdited,
                                      final AuthorFilterCallback onAuthorFilter) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A1F2E"));
        bg.setCornerRadius(dp(ctx, 12));
        bg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
        card.setBackground(bg);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, 0, 0, dp(ctx, 10));
        card.setLayoutParams(cardLp);

        ImageView thumb = new ImageView(ctx);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dp(ctx, 56), dp(ctx, 84));
        imgLp.setMargins(0, 0, dp(ctx, 12), 0);
        thumb.setLayoutParams(imgLp);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundColor(Color.parseColor("#262B38"));
        if (rec.coverPath != null && !rec.coverPath.isEmpty()) {
            File cf = new File(rec.coverPath);
            if (cf.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(cf.getAbsolutePath(), opts);
                if (bm != null) thumb.setImageBitmap(bm);
            }
        }
        card.addView(thumb);

        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        String prefix = (isFavorite(ctx, rec.kfxPath) ? "★ " : "") + (!getNote(ctx, rec.kfxPath).isEmpty() ? "📝 " : "");
        title.setText(prefix + rec.title);
        title.setTextColor(Color.WHITE);
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
        metaLine.setTextColor(Color.parseColor("#CBD5E1"));
        metaLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        metaLine.setPadding(0, dp(ctx, 2), 0, dp(ctx, 4));
        col.addView(metaLine);

        String displayGenres = cleanGenresForDisplay(rec.genres);
        if (!displayGenres.isEmpty()) {
            TextView genresTv = new TextView(ctx);
            genresTv.setText(displayGenres);
            genresTv.setTextColor(Color.parseColor("#60A5FA"));
            genresTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            genresTv.setMaxLines(2);
            genresTv.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(genresTv);
        }

        int pct = getProgress(progressMap, rec.kfxPath);
        if (pct > 0) {
            TextView progTv = new TextView(ctx);
            progTv.setText(pct >= 100 ? "✓ Finished" : (pct + "% read"));
            progTv.setTextColor(Color.parseColor(pct >= 100 ? "#34D399" : "#38BDF8"));
            progTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            progTv.setTypeface(Typeface.DEFAULT_BOLD);
            progTv.setPadding(0, dp(ctx, 3), 0, 0);
            col.addView(progTv);
        }

        if (!rec.isbn.isEmpty()) {
            TextView isbnTv = new TextView(ctx);
            isbnTv.setText("ISBN: " + rec.isbn);
            isbnTv.setTextColor(Color.parseColor("#94A3B8"));
            isbnTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            isbnTv.setPadding(0, dp(ctx, 2), 0, 0);
            col.addView(isbnTv);
        }

        String cardShelf = getShelf(ctx, rec.kfxPath);
        if (!cardShelf.isEmpty()) {
            TextView shelfTv = new TextView(ctx);
            shelfTv.setText("🏷 " + cardShelf);
            shelfTv.setTextColor(Color.parseColor("#A5B4FC"));
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
            rateTv.setTextColor(Color.parseColor("#FBBF24"));
            rateTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            rateTv.setTypeface(Typeface.DEFAULT_BOLD);
            rateTv.setPadding(0, dp(ctx, 2), 0, 0);
            col.addView(rateTv);
        }

        String cardNote = getNote(ctx, rec.kfxPath);
        if (!cardNote.isEmpty()) {
            TextView notePreviewTv = new TextView(ctx);
            notePreviewTv.setText("“" + cardNote + "”");
            notePreviewTv.setTextColor(Color.parseColor("#F59E0B"));
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
        infoBtn.setTextColor(Color.parseColor("#94A3B8"));
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

    private static void showBookDetailDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                             final int currentProgress,
                                             final AlertDialog parentDialog, final Runnable onMetadataEdited,
                                             final AuthorFilterCallback onAuthorFilter) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#161922"));
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
                    coverIv.setBackgroundColor(Color.parseColor("#262B38"));
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
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        headerCol.addView(title);

        final String displayAuthor = formatAuthorForDisplay(rec.author);
        TextView sub = new TextView(ctx);
        sub.setText(displayAuthor.isEmpty() ? "" : displayAuthor + "  ›");
        sub.setTextColor(Color.parseColor("#F59E0B"));
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
            progTv.setText(currentProgress >= 100 ? "✓ Finished" : currentProgress + "% read");
            progTv.setTextColor(Color.parseColor(currentProgress >= 100 ? "#34D399" : "#38BDF8"));
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
                details.append("Format: Enhanced KFX · ").append(sizeStr).append("\n");
            }
        }

        TextView metaTv = new TextView(ctx);
        metaTv.setText(details.toString().trim());
        metaTv.setTextColor(Color.parseColor("#CBD5E1"));
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
                pill.setTextColor(Color.parseColor("#60A5FA"));
                pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                pill.setTypeface(Typeface.DEFAULT_BOLD);
                pill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
                GradientDrawable pBg = new GradientDrawable();
                pBg.setColor(Color.parseColor("#1E2433"));
                pBg.setCornerRadius(dp(ctx, 14));
                pBg.setStroke(dp(ctx, 1), Color.parseColor("#2A354D"));
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
            descTv.setTextColor(Color.parseColor("#94A3B8"));
            descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            if (isLong) {
                descTv.setMaxLines(5);
                descTv.setEllipsize(TextUtils.TruncateAt.END);
            }
            box.addView(descTv);

            if (isLong) {
                final TextView moreTv = new TextView(ctx);
                moreTv.setText("Read full synopsis ›");
                moreTv.setTextColor(Color.parseColor("#5EEAD4"));
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
        readToggleBtn.setTextColor(Color.parseColor(isFinished ? "#F59E0B" : "#34D399"));
        readToggleBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        readToggleBtn.setTypeface(Typeface.DEFAULT_BOLD);
        readToggleBtn.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable toggleBg = new GradientDrawable();
        toggleBg.setColor(Color.parseColor("#1E2433"));
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
        favBtn.setTextColor(Color.parseColor(isFav ? "#FBBF24" : "#CBD5E1"));
        favBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        favBtn.setTypeface(Typeface.DEFAULT_BOLD);
        favBtn.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable favBg = new GradientDrawable();
        favBg.setColor(Color.parseColor(isFav ? "#282216" : "#1E2433"));
        favBg.setCornerRadius(dp(ctx, 10));
        if (isFav) favBg.setStroke(dp(ctx, 1), Color.parseColor("#66FBBF24"));
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
        rateLbl.setTextColor(Color.parseColor("#94A3B8"));
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
                starViews[si].setTextColor(Color.parseColor(filled ? "#FBBF24" : "#475569"));
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
        shelfLbl.setTextColor(Color.parseColor("#94A3B8"));
        shelfLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        shelfLbl.setTypeface(Typeface.DEFAULT_BOLD);
        shelfLbl.setPadding(0, 0, dp(ctx, 8), 0);
        shelfRow.addView(shelfLbl);

        TextView shelfPill = new TextView(ctx);
        shelfPill.setText(curShelf.isEmpty() ? "+ Assign Shelf ›" : "🏷 " + curShelf + "  ›");
        shelfPill.setTextColor(Color.parseColor(curShelf.isEmpty() ? "#94A3B8" : "#A5B4FC"));
        shelfPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        shelfPill.setTypeface(Typeface.DEFAULT_BOLD);
        shelfPill.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable shBg = new GradientDrawable();
        shBg.setColor(Color.parseColor(curShelf.isEmpty() ? "#1E2433" : "#1E223B"));
        shBg.setCornerRadius(dp(ctx, 12));
        if (!curShelf.isEmpty()) shBg.setStroke(dp(ctx, 1), Color.parseColor("#66818CF8"));
        shelfPill.setBackground(shBg);
        shelfPill.setOnClickListener(v -> showShelfPickerDialog(ctx, rec, curShelf, () -> {
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            if (onMetadataEdited != null) onMetadataEdited.run();
        }));
        shelfRow.addView(shelfPill);
        box.addView(shelfRow);

        // Personal Reading Note card / "+ Add Note" button
        final String existingNote = getNote(ctx, rec.kfxPath);
        LinearLayout noteBox = new LinearLayout(ctx);
        noteBox.setOrientation(LinearLayout.VERTICAL);
        noteBox.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable noteBg = new GradientDrawable();
        noteBg.setColor(Color.parseColor("#1A1F2E"));
        noteBg.setCornerRadius(dp(ctx, 10));
        noteBg.setStroke(dp(ctx, 1), Color.parseColor(existingNote.isEmpty() ? "#2A3142" : "#66F59E0B"));
        noteBox.setBackground(noteBg);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteLp.setMargins(0, dp(ctx, 12), 0, dp(ctx, 2));
        noteBox.setLayoutParams(noteLp);

        TextView noteHdr = new TextView(ctx);
        noteHdr.setText(existingNote.isEmpty() ? "📝 Add a Personal Reading Note ›" : "📝 Personal Note (tap to edit)");
        noteHdr.setTextColor(Color.parseColor(existingNote.isEmpty() ? "#94A3B8" : "#F59E0B"));
        noteHdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        noteHdr.setTypeface(Typeface.DEFAULT_BOLD);
        noteBox.addView(noteHdr);

        if (!existingNote.isEmpty()) {
            TextView noteBody = new TextView(ctx);
            noteBody.setText(existingNote);
            noteBody.setTextColor(Color.parseColor("#E2E8F0"));
            noteBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            noteBody.setPadding(0, dp(ctx, 4), 0, 0);
            noteBox.addView(noteBody);
        }

        noteBox.setOnClickListener(v -> showEditNoteDialog(ctx, rec, existingNote, () -> {
            if (detailDlgRef[0] != null) detailDlgRef[0].dismiss();
            if (onMetadataEdited != null) onMetadataEdited.run();
        }));
        box.addView(noteBox);

        LinearLayout footerActionRow = new LinearLayout(ctx);
        footerActionRow.setOrientation(LinearLayout.HORIZONTAL);
        footerActionRow.setGravity(Gravity.CENTER_VERTICAL);
        footerActionRow.setPadding(0, dp(ctx, 14), 0, dp(ctx, 4));

        TextView removeBtn = new TextView(ctx);
        removeBtn.setText("Remove from Folio Library");
        removeBtn.setTextColor(Color.parseColor("#F87171"));
        removeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        removeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        removeBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        removeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(ctx)
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
        });
        footerActionRow.addView(removeBtn);

        TextView copyBtn = new TextView(ctx);
        copyBtn.setText("📋 Copy Info");
        copyBtn.setTextColor(Color.parseColor("#94A3B8"));
        copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        copyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        copyBtn.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        GradientDrawable copyBg = new GradientDrawable();
        copyBg.setColor(Color.parseColor("#1E2433"));
        copyBg.setCornerRadius(dp(ctx, 10));
        copyBtn.setBackground(copyBg);
        copyBtn.setOnClickListener(v -> {
            StringBuilder clip = new StringBuilder();
            clip.append(rec.title);
            if (!displayAuthor.isEmpty()) clip.append(" — ").append(displayAuthor);
            if (!cleanDetailDate.isEmpty()) clip.append(" (").append(cleanDetailDate).append(")");
            if (!rec.publisher.isEmpty()) clip.append("\nPublisher: ").append(rec.publisher);
            if (!rec.isbn.isEmpty()) clip.append("\nISBN: ").append(rec.isbn);
            int rStars = getRating(ctx, rec.kfxPath);
            if (rStars > 0) clip.append("\nRating: ").append(rStars).append("/5");
            String nTxt = getNote(ctx, rec.kfxPath);
            if (!nTxt.isEmpty()) clip.append("\nNote: ").append(nTxt);
            try {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("Folio Book Info", clip.toString()));
                    Toast.makeText(ctx, "Copied book info to clipboard", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable ignored) {}
        });
        footerActionRow.addView(copyBtn);
        box.addView(footerActionRow);

        ScrollView detailSv = new ScrollView(ctx);
        detailSv.addView(box);

        detailDlgRef[0] = new AlertDialog.Builder(ctx)
                .setView(detailSv)
                .setPositiveButton("Read Now", (d, w) -> {
                    File f = new File(rec.kfxPath);
                    if (f.exists()) {
                        parentDialog.dismiss();
                        ExternalEpubActivity.launchKfxReader(ctx, f);
                    }
                })
                .setNeutralButton("Edit Metadata", (d, w) ->
                        showEditMetadataDialog(ctx, rec, onMetadataEdited))
                .setNegativeButton("Close", null)
                .show();
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
        capTv.setTextColor(Color.parseColor("#CBD5E1"));
        capTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        capTv.setGravity(Gravity.CENTER);
        capTv.setPadding(0, dp(ctx, 12), 0, dp(ctx, 4));
        wrap.addView(capTv);

        TextView hintTv = new TextView(ctx);
        hintTv.setText("Tap cover to close");
        hintTv.setTextColor(Color.parseColor("#64748B"));
        hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        hintTv.setGravity(Gravity.CENTER);
        wrap.addView(hintTv);

        final AlertDialog dlg = new AlertDialog.Builder(ctx)
                .setView(wrap)
                .create();
        wrap.setOnClickListener(v -> dlg.dismiss());
        fullIv.setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }

    private static void showEditMetadataDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                               final Runnable onSaved) {
        ScrollView sv = new ScrollView(ctx);
        LinearLayout form = new LinearLayout(ctx);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setBackgroundColor(Color.parseColor("#161922"));
        form.setPadding(dp(ctx, 20), dp(ctx, 18), dp(ctx, 20), dp(ctx, 14));
        sv.addView(form);

        TextView hdr = new TextView(ctx);
        hdr.setText("Edit Book Metadata");
        hdr.setTextColor(Color.WHITE);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 12));
        form.addView(hdr);

        final EditText etTitle = addField(ctx, form, "Title", rec.title);
        final EditText etAuthor = addField(ctx, form, "Author", rec.author);
        final EditText etPubDate = addField(ctx, form, "Published Date / Year", rec.pubDate);
        final EditText etPublisher = addField(ctx, form, "Publisher", rec.publisher);
        final EditText etGenres = addField(ctx, form, "Genres (comma-separated)", rec.genres);
        final EditText etIsbn = addField(ctx, form, "ISBN", rec.isbn);

        new AlertDialog.Builder(ctx)
                .setView(sv)
                .setPositiveButton("Save", (d, w) -> {
                    FolioMetadataDb db = new FolioMetadataDb(ctx);
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put("title", etTitle.getText().toString().trim());
                        cv.put("author", etAuthor.getText().toString().trim());
                        cv.put("pub_date", etPubDate.getText().toString().trim());
                        cv.put("publisher", etPublisher.getText().toString().trim());
                        cv.put("genres", etGenres.getText().toString().trim());
                        cv.put("isbn", etIsbn.getText().toString().trim());
                        db.getWritableDatabase().update(
                                "books_metadata", cv, "id = ?", new String[] { String.valueOf(rec.id) });
                        Toast.makeText(ctx, "Saved metadata changes", Toast.LENGTH_SHORT).show();
                        if (onSaved != null) onSaved.run();
                    } finally {
                        db.close();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static EditText addField(Context ctx, LinearLayout parent, String label, String value) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#CBD5E1"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setPadding(0, dp(ctx, 6), 0, dp(ctx, 4));
        parent.addView(tv);

        EditText et = new EditText(ctx);
        et.setText(value != null ? value : "");
        et.setTextColor(Color.WHITE);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setSingleLine(true);
        et.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1E2433"));
        bg.setCornerRadius(dp(ctx, 8));
        bg.setStroke(dp(ctx, 1), Color.parseColor("#334155"));
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

    private static void showShelfPickerDialog(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                              final String currentShelf, final Runnable onSaved) {
        final String[] presets = new String[] { "Classics", "Philosophy", "Sci-Fi", "History", "To Read", "Reference" };
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(Color.parseColor("#161922"));
        wrap.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Assign Shelf · " + rec.title);
        hdr.setTextColor(Color.WHITE);
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
            pill.setText("🏷 " + preset);
            pill.setTextColor(Color.parseColor(isCur ? "#111827" : "#A5B4FC"));
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pill.setTypeface(Typeface.DEFAULT_BOLD);
            pill.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(isCur ? "#818CF8" : "#1E2433"));
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
        et.setHintTextColor(Color.parseColor("#64748B"));
        et.setTextColor(Color.WHITE);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setSingleLine(true);
        et.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(Color.parseColor("#1A1F2E"));
        etBg.setCornerRadius(dp(ctx, 10));
        etBg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
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
        wrap.setBackgroundColor(Color.parseColor("#161922"));
        wrap.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Personal Note · " + rec.title);
        hdr.setTextColor(Color.WHITE);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(ctx, 10));
        wrap.addView(hdr);

        final EditText et = new EditText(ctx);
        et.setText(currentNote);
        et.setHint("Write your thoughts, quotes, or reading reminders...");
        et.setHintTextColor(Color.parseColor("#64748B"));
        et.setTextColor(Color.WHITE);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setMinLines(3);
        et.setMaxLines(8);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(Color.parseColor("#1A1F2E"));
        etBg.setCornerRadius(dp(ctx, 10));
        etBg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
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
        b.show();
    }

    public static boolean isFavorite(Context ctx, String kfxPath) {
        if (ctx == null || kfxPath == null || kfxPath.isEmpty()) return false;
        try {
            return ctx.getSharedPreferences("folio_favorites", Context.MODE_PRIVATE)
                    .getBoolean(kfxPath, false);
        } catch (Throwable ignored) { return false; }
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
