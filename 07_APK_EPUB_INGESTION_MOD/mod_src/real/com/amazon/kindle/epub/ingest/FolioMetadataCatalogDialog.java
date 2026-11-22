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

    private static final String[] SORT_MODES = new String[] { "Recent", "Title", "Author", "Year", "Progress" };

    private FolioMetadataCatalogDialog() {}

    public static void show(final Context ctx) {
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

        // Search box
        final EditText searchBox = new EditText(ctx);
        searchBox.setHint("Filter by title, author, genre, or ISBN...");
        searchBox.setHintTextColor(Color.parseColor("#64748B"));
        searchBox.setTextColor(Color.WHITE);
        searchBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        searchBox.setSingleLine(true);
        searchBox.setPadding(dp(ctx, 12), dp(ctx, 9), dp(ctx, 12), dp(ctx, 9));
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.parseColor("#1A1F2E"));
        searchBg.setCornerRadius(dp(ctx, 10));
        searchBg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
        searchBox.setBackground(searchBg);
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchLp.setMargins(0, 0, 0, dp(ctx, 10));
        searchBox.setLayoutParams(searchLp);
        root.addView(searchBox);

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

        final String[] selectedGenre = new String[] { "All" };
        final String[] queryText = new String[] { "" };
        final int[] sortIdx = new int[] { 0 };

        final Runnable[] renderRef = new Runnable[1];
        renderRef[0] = new Runnable() {
            @Override
            public void run() {
                heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
                btnSort.setText("Sort: " + SORT_MODES[sortIdx[0]]);

                chipRow.removeAllViews();
                for (final String g : genres) {
                    boolean active = g.equalsIgnoreCase(selectedGenre[0]);
                    int gCount = "All".equalsIgnoreCase(g) ? allBooks.size() : countBooksInGenre(allBooks, g);
                    TextView chip = new TextView(ctx);
                    chip.setText(g + " (" + gCount + ")");
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    chip.setTextColor(Color.parseColor(active ? "#111827" : "#CBD5E1"));
                    chip.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor(active ? "#F59E0B" : "#1E2433"));
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
                    if (!rec.hasGenre(selectedGenre[0])) continue;
                    if (!q.isEmpty() && !matchesQuery(rec, q)) continue;
                    filtered.add(rec);
                }

                sortBooks(filtered, sortIdx[0], progressMap);

                cardsContainer.removeAllViews();
                for (final FolioMetadataDb.BookRecord rec : filtered) {
                    cardsContainer.addView(buildBookCard(ctx, rec, progressMap, dialog, () -> {
                        loadFromDb(ctx, allBooks, genres);
                        renderRef[0].run();
                    }));
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

        btnSort.setOnClickListener(v -> {
            sortIdx[0] = (sortIdx[0] + 1) % SORT_MODES.length;
            renderRef[0].run();
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                queryText[0] = s != null ? s.toString() : "";
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

    private static void sortBooks(List<FolioMetadataDb.BookRecord> list, final int mode, final Map<String, Integer> progressMap) {
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
                        && !EpubMetadataExtractor.isNoiseTag(c)) {
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

    private static boolean matchesQuery(FolioMetadataDb.BookRecord r, String q) {
        return contains(r.title, q) || contains(r.author, q) || contains(r.genres, q)
                || contains(r.publisher, q) || contains(r.isbn, q) || contains(r.pubDate, q);
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
                                      final AlertDialog parentDialog, final Runnable onMetadataEdited) {
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
        title.setText(rec.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(title);

        StringBuilder byline = new StringBuilder(rec.author);
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

        card.addView(col);

        TextView infoBtn = new TextView(ctx);
        infoBtn.setText("ⓘ");
        infoBtn.setTextColor(Color.parseColor("#94A3B8"));
        infoBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        infoBtn.setPadding(dp(ctx, 8), dp(ctx, 4), dp(ctx, 2), dp(ctx, 4));
        infoBtn.setOnClickListener(v -> showBookDetailDialog(ctx, rec, parentDialog, onMetadataEdited));
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
                                             final AlertDialog parentDialog, final Runnable onMetadataEdited) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#161922"));
        box.setPadding(dp(ctx, 20), dp(ctx, 18), dp(ctx, 20), dp(ctx, 14));

        TextView title = new TextView(ctx);
        title.setText(rec.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(title);

        TextView sub = new TextView(ctx);
        sub.setText(rec.author);
        sub.setTextColor(Color.parseColor("#F59E0B"));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sub.setPadding(0, dp(ctx, 4), 0, dp(ctx, 10));
        box.addView(sub);

        StringBuilder details = new StringBuilder();
        String cleanDetailDate = EpubMetadataExtractor.normalizePubDate(rec.pubDate);
        if (!cleanDetailDate.isEmpty()) details.append("Published: ").append(cleanDetailDate).append("\n");
        if (!rec.publisher.isEmpty()) details.append("Publisher: ").append(rec.publisher).append("\n");
        if (!rec.isbn.isEmpty()) details.append("ISBN: ").append(rec.isbn).append("\n");
        String cleanDetGenres = cleanGenresForDisplay(rec.genres);
        if (!cleanDetGenres.isEmpty()) details.append("Genres: ").append(cleanDetGenres).append("\n");
        if (!rec.language.isEmpty() && !"und".equalsIgnoreCase(rec.language.trim())
                && !"undetermined".equalsIgnoreCase(rec.language.trim())) {
            details.append("Language: ").append(rec.language).append("\n");
        }
        if (!rec.openLibraryKey.isEmpty()) details.append("Open Library: ").append(rec.openLibraryKey).append("\n");

        TextView metaTv = new TextView(ctx);
        metaTv.setText(details.toString().trim());
        metaTv.setTextColor(Color.parseColor("#CBD5E1"));
        metaTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        box.addView(metaTv);

        if (!rec.description.isEmpty()) {
            TextView descTv = new TextView(ctx);
            descTv.setText("\n" + rec.description);
            descTv.setTextColor(Color.parseColor("#94A3B8"));
            descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            descTv.setMaxLines(6);
            descTv.setEllipsize(TextUtils.TruncateAt.END);
            box.addView(descTv);
        }

        final AlertDialog[] detailDlgRef = new AlertDialog[1];

        TextView removeBtn = new TextView(ctx);
        removeBtn.setText("Remove from Folio Library");
        removeBtn.setTextColor(Color.parseColor("#F87171"));
        removeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        removeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        removeBtn.setPadding(0, dp(ctx, 14), 0, dp(ctx, 4));
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
        box.addView(removeBtn);

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

    private static Map<String, Integer> loadProgressMap(Context ctx) {
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

    private static int getProgress(Map<String, Integer> map, String kfxPath) {
        if (map == null || kfxPath == null) return 0;
        Integer v = map.get(kfxPath);
        return v != null ? v : 0;
    }

    private static String cleanGenresForDisplay(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : raw.split(",")) {
            String c = part.trim();
            if (!c.isEmpty() && !EpubMetadataExtractor.isNoiseTag(c)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(c);
            }
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

    private static int dp(Context ctx, int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics()));
    }
}
