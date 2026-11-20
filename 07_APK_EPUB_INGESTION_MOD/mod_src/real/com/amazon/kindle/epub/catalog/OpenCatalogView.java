package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Embedded Open Book Catalog View replacing Amazon Store / Kindle+ upsells
 * inside RubyHomeFragment (Home tab of com.amazon.kindle.epub).
 * Supports pluggable sources (Project Gutenberg via Gutendex + Custom OPDS feeds),
 * Open Library ratings/descriptions/similar books, cover thumbnails, and local DB backups.
 */
public final class OpenCatalogView {
    private static final String PREFS = "OpenCatalogPrefs";
    private static final String KEY_CUSTOM_OPDS = "custom_opds_url";

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
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String savedOpds = prefs.getString(KEY_CUSTOM_OPDS, "");
        if (!savedOpds.isEmpty()) {
            sources.add(new OpdsSource("Custom OPDS", savedOpds, ""));
        }

        final int[] activeSourceIdx = new int[]{0};
        final List<BookResult> currentResults = new ArrayList<BookResult>();
        final List<String> displayItems = new ArrayList<String>();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(36, 96, 36, 160);

        // Header row: Title + Backup button
        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView header = new TextView(ctx);
        header.setText("Discover Free EPUBs");
        header.setTextColor(Color.WHITE);
        header.setTextSize(21f);
        header.setTypeface(Typeface.SERIF, Typeface.BOLD);
        headerRow.addView(header, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button backupBtn = new Button(ctx);
        backupBtn.setText("Backup");
        backupBtn.setTextSize(12f);
        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupLocalDatabases(ctx);
            }
        });
        headerRow.addView(backupBtn);
        root.addView(headerRow);

        // Source Switcher row
        LinearLayout sourceRow = new LinearLayout(ctx);
        sourceRow.setOrientation(LinearLayout.HORIZONTAL);
        sourceRow.setPadding(0, 8, 0, 8);

        final Button gutenBtn = new Button(ctx);
        gutenBtn.setText("Gutenberg");
        gutenBtn.setTextSize(12f);
        sourceRow.addView(gutenBtn);

        final Button opdsBtn = new Button(ctx);
        opdsBtn.setText(savedOpds.isEmpty() ? "+ OPDS Feed" : "Custom OPDS");
        opdsBtn.setTextSize(12f);
        sourceRow.addView(opdsBtn);

        root.addView(sourceRow);

        // Search row
        LinearLayout searchRow = new LinearLayout(ctx);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        final EditText searchInput = new EditText(ctx);
        searchInput.setHint("Search title or author...");
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
                final BookSource src = sources.get(activeSourceIdx[0]);
                final String q = searchInput.getText().toString();
                statusView.setText(q.trim().isEmpty() ? "Loading " + src.getName() + "..." : "Searching " + src.getName() + " for \"" + q + "\"...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final List<BookResult> found = src.search(q);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentResults.clear();
                                    displayItems.clear();
                                    currentResults.addAll(found);
                                    for (BookResult b : found) {
                                        String sub = (b.downloadCount > 0)
                                                ? (b.author + "  •  " + b.downloadCount + " downloads")
                                                : b.author;
                                        displayItems.add(b.title + "\n" + sub);
                                    }
                                    adapter.notifyDataSetChanged();
                                    statusView.setText("Found " + found.size() + " EPUBs via " + src.getName());
                                }
                            });
                        } catch (final Throwable t) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    statusView.setText("Network error (" + t.getMessage() + "). Tap Search to retry.");
                                }
                            });
                        }
                    }
                }).start();
            }
        };

        gutenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeSourceIdx[0] = 0;
                doSearch.run();
            }
        });

        opdsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sources.size() > 1) {
                    activeSourceIdx[0] = 1;
                    doSearch.run();
                } else {
                    promptCustomOpds(ctx, prefs, sources, activeSourceIdx, opdsBtn, doSearch);
                }
            }
        });

        opdsBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                promptCustomOpds(ctx, prefs, sources, activeSourceIdx, opdsBtn, doSearch);
                return true;
            }
        });

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

    private static void promptCustomOpds(
            final Context ctx,
            final SharedPreferences prefs,
            final List<BookSource> sources,
            final int[] activeIdx,
            final Button opdsBtn,
            final Runnable doSearch
    ) {
        final EditText input = new EditText(ctx);
        input.setHint("https://example.org/opds.xml");
        input.setText(prefs.getString(KEY_CUSTOM_OPDS, ""));
        new AlertDialog.Builder(ctx)
                .setTitle("Add OPDS Feed URL")
                .setView(input)
                .setPositiveButton("Save & Browse", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String url = input.getText().toString().trim();
                        if (!url.isEmpty()) {
                            prefs.edit().putString(KEY_CUSTOM_OPDS, url).apply();
                            OpdsSource opds = new OpdsSource("Custom OPDS", url, "");
                            if (sources.size() > 1) {
                                sources.set(1, opds);
                            } else {
                                sources.add(opds);
                            }
                            opdsBtn.setText("Custom OPDS");
                            activeIdx[0] = 1;
                            doSearch.run();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void showBookDetails(final Context ctx, final Handler mainHandler, final BookResult book) {
        ScrollView scroll = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 32, 48, 16);

        final ImageView coverView = new ImageView(ctx);
        coverView.setVisibility(View.GONE);
        LinearLayout.LayoutParams covLp = new LinearLayout.LayoutParams(260, 390);
        covLp.gravity = Gravity.CENTER_HORIZONTAL;
        covLp.bottomMargin = 24;
        box.addView(coverView, covLp);

        final TextView msgView = new TextView(ctx);
        msgView.setText("Author: " + book.author + "\nSource: " + book.sourceName + "\n\nFetching Open Library ratings, synopsis & similar books...");
        msgView.setTextSize(15f);
        box.addView(msgView);
        scroll.addView(box);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(book.title)
                .setView(scroll)
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

                // Fetch cover bitmap: try book.coverUrl first, fallback to Open Library if Gutenberg is 503
                Bitmap bmp = null;
                if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                    try {
                        InputStream is = SafeHttp.openStream(book.coverUrl);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Throwable t) { android.util.Log.e("OpenCatalogView", "Primary cover error", t); }
                }
                if (bmp == null && meta.coverUrl != null && !meta.coverUrl.isEmpty()) {
                    try {
                        InputStream is = SafeHttp.openStream(meta.coverUrl);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Throwable t) { android.util.Log.e("OpenCatalogView", "Fallback cover error", t); }
                }
                if (bmp != null) {
                    final Bitmap finalBmp = bmp;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                coverView.setImageBitmap(finalBmp);
                                coverView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                final StringBuilder msg = new StringBuilder();
                msg.append("Author: ").append(book.author).append("\n");
                msg.append("Source: ").append(book.sourceName).append("\n");
                if (meta.ratingsCount > 0) {
                    msg.append(String.format(Locale.US, "Open Library Rating: %.2f / 5 (%d ratings)\n", meta.avgRating, meta.ratingsCount));
                }
                if (!meta.description.isEmpty()) {
                    String desc = meta.description.length() > 380 ? meta.description.substring(0, 380) + "..." : meta.description;
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
                            msgView.setText(msg.toString());
                        }
                    }
                });
            }
        }).start();
    }

    private static void backupLocalDatabases(Context ctx) {
        try {
            File backupDir = new File("/sdcard/kindle/backups");
            backupDir.mkdirs();
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File dbDir = new File(ctx.getApplicationInfo().dataDir, "databases");
            int copied = 0;
            for (String dbName : new String[]{"kindle_library.db", "annotations.db"}) {
                File src = new File(dbDir, dbName);
                if (src.exists()) {
                    copyFile(src, new File(backupDir, stamp + "_" + dbName));
                    copied++;
                }
            }
            Toast.makeText(ctx, "Backed up " + copied + " DB(s) to /sdcard/kindle/backups/", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            Toast.makeText(ctx, "Backup error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void copyFile(File src, File dst) throws Exception {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
        } finally {
            fos.close();
            fis.close();
        }
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

                    InputStream is = SafeHttp.openStream(book.epubUrl);
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
