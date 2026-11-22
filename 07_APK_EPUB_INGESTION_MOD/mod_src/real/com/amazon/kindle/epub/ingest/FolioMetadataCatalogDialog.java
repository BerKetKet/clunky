package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * Dark-themed "Library Metadata & Genres" browser surfacing all rich metadata
 * stored in folio_metadata.db (cover thumbnail, title, author, pub date,
 * publisher, ISBN, genres, and description) with live genre filter chips and
 * 1-tap launch into KRIFDocViewer.
 */
public final class FolioMetadataCatalogDialog {

    private FolioMetadataCatalogDialog() {}

    public static void show(final Context ctx) {
        final FolioMetadataDb db = new FolioMetadataDb(ctx);
        final List<FolioMetadataDb.BookRecord> allBooks;
        final List<String> genres;
        try {
            allBooks = db.getAllBooks();
            genres = db.getDistinctGenres(allBooks);
        } finally {
            db.close();
        }

        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#11131A"));
        root.setPadding(dp(ctx, 18), dp(ctx, 18), dp(ctx, 18), dp(ctx, 14));

        // Header
        TextView eyebrow = new TextView(ctx);
        eyebrow.setText("FOLIO METADATA CATALOG");
        eyebrow.setTextColor(Color.parseColor("#F59E0B"));
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(eyebrow);

        TextView heading = new TextView(ctx);
        heading.setText("Library Metadata & Genres (" + allBooks.size() + ")");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setPadding(0, dp(ctx, 4), 0, dp(ctx, 12));
        root.addView(heading);

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
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 420)));
        final LinearLayout cardsContainer = new LinearLayout(ctx);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(cardsContainer);
        root.addView(listScroll);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(root)
                .setPositiveButton("Close", null)
                .create();

        final String[] selectedGenre = new String[] { "All" };

        final Runnable render = new Runnable() {
            @Override
            public void run() {
                // Rebuild chips
                chipRow.removeAllViews();
                for (final String g : genres) {
                    boolean active = g.equals(selectedGenre[0]);
                    TextView chip = new TextView(ctx);
                    chip.setText(g);
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
                        run();
                    });
                    chipRow.addView(chip);
                }

                // Rebuild book cards
                cardsContainer.removeAllViews();
                int shown = 0;
                for (final FolioMetadataDb.BookRecord rec : allBooks) {
                    if (!rec.hasGenre(selectedGenre[0])) continue;
                    shown++;
                    cardsContainer.addView(buildBookCard(ctx, rec, dialog));
                }

                if (shown == 0) {
                    TextView empty = new TextView(ctx);
                    empty.setText("No imported books in folio_metadata.db yet.\nOpen any EPUB from storage and choose \"Add to Library & Read\".");
                    empty.setTextColor(Color.parseColor("#94A3B8"));
                    empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    empty.setPadding(dp(ctx, 8), dp(ctx, 20), dp(ctx, 8), dp(ctx, 20));
                    cardsContainer.addView(empty);
                }
            }
        };

        render.run();
        dialog.show();
    }

    private static View buildBookCard(final Context ctx, final FolioMetadataDb.BookRecord rec,
                                      final AlertDialog dialog) {
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

        // Cover thumbnail
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

        // Text metadata column
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
        if (!rec.pubDate.isEmpty()) byline.append(" · ").append(rec.pubDate);
        if (!rec.publisher.isEmpty()) byline.append(" · ").append(rec.publisher);
        TextView metaLine = new TextView(ctx);
        metaLine.setText(byline.toString());
        metaLine.setTextColor(Color.parseColor("#CBD5E1"));
        metaLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        metaLine.setPadding(0, dp(ctx, 2), 0, dp(ctx, 4));
        col.addView(metaLine);

        if (!rec.genres.isEmpty()) {
            TextView genresTv = new TextView(ctx);
            genresTv.setText(rec.genres);
            genresTv.setTextColor(Color.parseColor("#60A5FA"));
            genresTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            genresTv.setMaxLines(2);
            genresTv.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(genresTv);
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

        card.setOnClickListener(v -> {
            File f = new File(rec.kfxPath);
            if (!f.exists()) {
                Toast.makeText(ctx, "Book file not found in app storage", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            ExternalEpubActivity.launchKfxReader(ctx, f);
        });

        return card;
    }

    private static int dp(Context ctx, int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics()));
    }
}
