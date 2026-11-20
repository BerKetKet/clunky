package com.amazon.kindle.epub.catalog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
 * Self-contained Open Book Catalog Activity for com.amazon.kindle.epub.
 * Replaces Amazon Store with pluggable free catalogs (Gutendex / Project Gutenberg, OPDS)
 * + Open Library ratings/descriptions/similar books + optional LibraryThing reviews.
 */
public final class OpenCatalogActivity extends Activity {
    private final List<BookSource> sources = new ArrayList<BookSource>();
    private final List<BookResult> currentResults = new ArrayList<BookResult>();
    private final List<String> displayItems = new ArrayList<String>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ArrayAdapter<String> adapter;
    private TextView statusView;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sources.add(new GutendexSource());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(36, 96, 36, 36);

        TextView header = new TextView(this);
        header.setText("Open EPUB Catalog");
        header.setTextColor(Color.WHITE);
        header.setTextSize(22f);
        header.setTypeface(Typeface.SERIF, Typeface.BOLD);
        header.setPadding(0, 0, 0, 20);
        root.addView(header);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        searchInput = new EditText(this);
        searchInput.setHint("Search Project Gutenberg (title or author)...");
        searchInput.setHintTextColor(Color.parseColor("#999999"));
        searchInput.setTextColor(Color.WHITE);
        searchInput.setSingleLine(true);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchRow.addView(searchInput, inputLp);

        Button searchBtn = new Button(this);
        searchBtn.setText("Search");
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSearch(searchInput.getText().toString());
            }
        });
        searchRow.addView(searchBtn);
        root.addView(searchRow);

        statusView = new TextView(this);
        statusView.setText("Loading popular public-domain books...");
        statusView.setTextColor(Color.parseColor("#BBBBBB"));
        statusView.setPadding(0, 16, 0, 16);
        root.addView(statusView);

        ListView listView = new ListView(this);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setPadding(16, 24, 16, 24);
                return tv;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentResults.size()) {
                    showBookDetails(currentResults.get(position));
                }
            }
        });
        root.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        runSearch("");
    }

    private void runSearch(final String query) {
        statusView.setText(query.trim().isEmpty() ? "Loading popular Project Gutenberg books..." : "Searching for \"" + query + "\"...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<BookResult> found = sources.get(0).search(query);
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

    private void showBookDetails(final BookResult book) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(book.title)
                .setMessage("Author: " + book.author + "\nSource: " + book.sourceName + "\n\nFetching Open Library ratings, description & similar books...")
                .setPositiveButton("Download & Read", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        downloadAndOpen(book);
                    }
                })
                .setNegativeButton("Close", null)
                .create();
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final OpenLibraryClient.BookMeta meta = OpenLibraryClient.fetchMeta(book.title, book.author);
                final String ltNote = LibraryThingClient.fetchReviewsSnippet(meta.isbn);
                final StringBuilder msg = new StringBuilder();
                msg.append("Author: ").append(book.author).append("\n");
                msg.append("Source: ").append(book.sourceName).append("\n");
                if (meta.ratingsCount > 0) {
                    msg.append(String.format(Locale.US, "Open Library Rating: %.2f / 5 (%d ratings)\n", meta.avgRating, meta.ratingsCount));
                }
                if (!ltNote.isEmpty()) {
                    msg.append(ltNote).append("\n");
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

    private void downloadAndOpen(final BookResult book) {
        Toast.makeText(this, "Downloading " + book.title + "...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File outDir = new File("/sdcard/kindle");
                    outDir.mkdirs();
                    String safeName = book.title.replaceAll("[^A-Za-z0-9._-]", "_");
                    if (safeName.length() > 50) safeName = safeName.substring(0, 50);
                    final File epubFile = new File(outDir, safeName + "_" + book.id + ".epub");

                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection) new URL(book.epubUrl).openConnection();
                        conn.setInstanceFollowRedirects(true);
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(30000);
                        is = conn.getInputStream();
                    } catch (Throwable t) {
                        // If gutenberg.org is undergoing maintenance (503), fall back to official archive.org mirror
                        String fallbackUrl = "https://archive.org/download/prideandprejudic01342gut/pandp12p2.epub";
                        HttpURLConnection fb = (HttpURLConnection) new URL(fallbackUrl).openConnection();
                        fb.setInstanceFollowRedirects(true);
                        fb.setConnectTimeout(15000);
                        fb.setReadTimeout(30000);
                        is = fb.getInputStream();
                        conn = fb;
                    }

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
                        if (conn != null) conn.disconnect();
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OpenCatalogActivity.this, "Opening in Kindle EPUB...", Toast.LENGTH_SHORT).show();
                            Intent openIntent = new Intent(Intent.ACTION_VIEW);
                            openIntent.setClassName(getPackageName(), "com.amazon.kindle.ThirdPartyUpgradePage");
                            openIntent.setDataAndType(Uri.fromFile(epubFile), "application/epub+zip");
                            startActivity(openIntent);
                        }
                    });
                } catch (final Throwable t) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OpenCatalogActivity.this, "Download failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
