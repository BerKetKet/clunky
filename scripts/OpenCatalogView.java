package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Embedded Open Book Catalog View that replaces the Amazon Store / Kindle+ upsell feed
 * inside RubyHomeFragment (the Home tab of com.amazon.kindle.epub).
 * Preserves the bottom navigation bar (Home | Library | More) and mini-cover resume bar.
 */
public final class OpenCatalogView {
    public static View wrap(View originalHomeView) {
        if (originalHomeView == null) return null;
        try {
            Context ctx = originalHomeView.getContext();
            FrameLayout container = new FrameLayout(ctx);
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            originalHomeView.setVisibility(View.GONE);
            container.addView(originalHomeView);

            View catalogRoot = buildCatalogView(ctx);
            container.addView(catalogRoot, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return container;
        } catch (Throwable t) {
            return originalHomeView;
        }
    }

    public static View buildCatalogView(final Context ctx) {
        final List<BookSource> sources = new ArrayList<BookSource>();
        sources.add(new GutendexSource());
        final List<BookResult> currentResults = new ArrayList<BookResult>();
        final List<String> displayItems = new ArrayList<String>();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(36, 96, 36, 160);

        TextView header = new TextView(ctx);
        header.setText("Discover Free EPUBs");
        header.setTextColor(Color.WHITE);
        header.setTextSize(22f);
        header.setTypeface(Typeface.SERIF, Typeface.BOLD);
        header.setPadding(0, 0, 0, 16);
        root.addView(header);

        LinearLayout searchRow = new LinearLayout(ctx);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        final EditText searchInput = new EditText(ctx);
        searchInput.setHint("Search Project Gutenberg...");
        searchInput.setHintTextColor(Color.parseColor("#999999"));
        searchInput.setTextColor(Color.WHITE);
        searchInput.setSingleLine(true);
        searchRow.addView(searchInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button searchBtn = new Button(ctx);
        searchBtn.setText("Search");
        searchRow.addView(searchBtn);
        root.addView(searchRow);

        final TextView statusView = new TextView(ctx);
        statusView.setText("Loading popular public-domain books...");
        statusView.setTextColor(Color.parseColor("#BBBBBB"));
        statusView.setPadding(0, 12, 0, 12);
        root.addView(statusView);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, displayItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setPadding(12, 20, 12, 20);
                return tv;
            }
        };

        ListView listView = new ListView(ctx);
        listView.setAdapter(adapter);
        root.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final Runnable doSearch = new Runnable() {
            @Override
            public void run() {
                final String q = searchInput.getText().toString();
                statusView.setText(q.trim().isEmpty() ? "Loading popular Project Gutenberg books..." : "Searching for \"" + q + "\"...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final List<BookResult> found = sources.get(0).search(q);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentResults.clear();
                                    displayItems.clear();
                                    currentResults.addAll(found);
                                    for (BookResult b : found) {
                                        displayItems.add(b.title + "\n" + b.author + "  •  " + b.downloadCount + " downloads");
                                    }
                                    adapter.notifyDataSetChanged();
                                    statusView.setText("Found " + found.size() + " EPUBs via " + sources.get(0).getName());
                                }
                            });
                        } catch (final Throwable t) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    statusView.setText("Search error: " + t.getMessage());
                                }
                            });
                        }
                    }
                }).start();
            }
        };

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch.run();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentResults.size()) {
                    showBookDetails(ctx, mainHandler, currentResults.get(position));
                }
            }
        });

        doSearch.run();
        return root;
    }

    private static void showBookDetails(final Context ctx, final Handler mainHandler, final BookResult book) {
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(book.title)
                .setMessage("Author: " + book.author + "\nSource: " + book.sourceName + "\n\nFetching Open Library ratings, description & similar books...")
                .setPositiveButton("Download & Read", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        downloadAndOpen(ctx, mainHandler, book);
                    }
                })
                .setNegativeButton("Close", null)
                .create();
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final OpenLibraryClient.BookMeta meta = OpenLibraryClient.fetchMeta(book.title, book.author);
                final StringBuilder msg = new StringBuilder();
                msg.append("Author: ").append(book.author).append("\n");
                msg.append("Source: ").append(book.sourceName).append("\n");
                if (meta.ratingsCount > 0) {
                    msg.append(String.format(Locale.US, "Open Library Rating: %.2f / 5 (%d ratings)\n", meta.avgRating, meta.ratingsCount));
                }
                if (!meta.description.isEmpty()) {
                    String desc = meta.description.length() > 350 ? meta.description.substring(0, 350) + "..." : meta.description;
                    msg.append("\n").append(desc).append("\n");
                }
                if (!meta.similarTitles.isEmpty()) {
                    msg.append("\nSimilar Books:\n");
                    for (int i = 0; i < Math.min(4, meta.similarTitles.size()); i++) {
                        msg.append("• ").append(meta.similarTitles.get(i)).append("\n");
                    }
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog.isShowing()) {
                            dialog.setMessage(msg.toString());
                        }
                    }
                });
            }
        }).start();
    }

    private static void downloadAndOpen(final Context ctx, final Handler mainHandler, final BookResult book) {
        Toast.makeText(ctx, "Downloading " + book.title + "...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File outDir = new File("/sdcard/kindle");
                    outDir.mkdirs();
                    String safeName = book.title.replaceAll("[^A-Za-z0-9._-]", "_");
                    if (safeName.length() > 50) safeName = safeName.substring(0, 50);
                    final File epubFile = new File(outDir, safeName + "_" + book.id + ".epub");

                    HttpURLConnection conn = (HttpURLConnection) new URL(book.epubUrl).openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    InputStream is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(epubFile);
                    try {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = is.read(buf)) > 0) {
                            fos.write(buf, 0, n);
                        }
                    } finally {
                        fos.close();
                        is.close();
                        conn.disconnect();
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, "Opening in Kindle EPUB...", Toast.LENGTH_SHORT).show();
                            Intent openIntent = new Intent(Intent.ACTION_VIEW);
                            openIntent.setClassName(ctx.getPackageName(), "com.amazon.kindle.ThirdPartyUpgradePage");
                            openIntent.setDataAndType(Uri.fromFile(epubFile), "application/epub+zip");
                            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ctx.startActivity(openIntent);
                        }
                    });
                } catch (final Throwable t) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, "Download failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
