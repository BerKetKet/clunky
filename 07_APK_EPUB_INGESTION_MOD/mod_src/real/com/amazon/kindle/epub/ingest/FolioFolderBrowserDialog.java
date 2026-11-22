package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Dark-themed multi-select EPUB folder importer with:
 *   - Quick folder chips (/sdcard/en/0, /sdcard/epub, /sdcard/Download, /sdcard/Books)
 *   - Instant in-memory filename search across large 700+ EPUB folders
 *   - "Hide Imported" toggle pill to focus only on new books
 *   - Batch KFX conversion into app-private files/folio_library/ + Open Library enrichment
 */
public final class FolioFolderBrowserDialog {

    private static final String TAG = "FolioFolderImport";
    private static final String[] QUICK_DIRS = new String[] {
        "/sdcard/en/0",
        "/sdcard/epub",
        "/sdcard/Download",
        "/sdcard/Books"
    };

    private FolioFolderBrowserDialog() {}

    public static void show(final Context ctx) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Set<String> importedSourceNames = loadImportedSources(ctx);
        final List<File> allEpubsInDir = new ArrayList<>();
        final List<File> selectedFiles = new ArrayList<>();
        final boolean[] hideImported = new boolean[] { false };
        final String[] filterQuery = new String[] { "" };

        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#11131A"));
        root.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 12));

        // Top row: Eyebrow + Hide Imported toggle
        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView eyebrow = new TextView(ctx);
        eyebrow.setText("BATCH EPUB IMPORTER");
        eyebrow.setTextColor(Color.parseColor("#F59E0B"));
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(eyebrow);

        final TextView btnHideImported = new TextView(ctx);
        btnHideImported.setText("Hide Imported: OFF");
        btnHideImported.setTextColor(Color.parseColor("#CBD5E1"));
        btnHideImported.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnHideImported.setTypeface(Typeface.DEFAULT_BOLD);
        btnHideImported.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        topRow.addView(btnHideImported);
        root.addView(topRow);

        TextView heading = new TextView(ctx);
        heading.setText("Import EPUBs from Storage");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setPadding(0, dp(ctx, 4), 0, dp(ctx, 10));
        root.addView(heading);

        // Path bar + Open button
        LinearLayout pathRow = new LinearLayout(ctx);
        pathRow.setOrientation(LinearLayout.HORIZONTAL);
        pathRow.setGravity(Gravity.CENTER_VERTICAL);

        final EditText etPath = new EditText(ctx);
        etPath.setText(pickDefaultDir());
        etPath.setTextColor(Color.WHITE);
        etPath.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        etPath.setSingleLine(true);
        etPath.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        etPath.setBackground(makeInputBg(ctx));
        etPath.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        pathRow.addView(etPath);

        TextView btnGo = new TextView(ctx);
        btnGo.setText("Open");
        btnGo.setTextColor(Color.parseColor("#111827"));
        btnGo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        btnGo.setTypeface(Typeface.DEFAULT_BOLD);
        btnGo.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable goBg = new GradientDrawable();
        goBg.setColor(Color.parseColor("#F59E0B"));
        goBg.setCornerRadius(dp(ctx, 8));
        btnGo.setBackground(goBg);
        LinearLayout.LayoutParams goLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        goLp.setMargins(dp(ctx, 8), 0, 0, 0);
        btnGo.setLayoutParams(goLp);
        pathRow.addView(btnGo);
        root.addView(pathRow);

        // Quick directory chips
        HorizontalScrollView chipScroll = new HorizontalScrollView(ctx);
        chipScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        csLp.setMargins(0, dp(ctx, 8), 0, dp(ctx, 8));
        chipScroll.setLayoutParams(csLp);

        LinearLayout chipRow = new LinearLayout(ctx);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipScroll.addView(chipRow);
        root.addView(chipScroll);

        // Inline filename search bar + Clear (✕) button (filters ALL EPUBs in directory, e.g. 719+)
        LinearLayout filterRow = new LinearLayout(ctx);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER_VERTICAL);
        filterRow.setBackground(makeInputBg(ctx));
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fLp.setMargins(0, 0, 0, dp(ctx, 8));
        filterRow.setLayoutParams(fLp);

        final EditText etFilter = new EditText(ctx);
        etFilter.setHint("Search EPUB filenames in this folder...");
        etFilter.setHintTextColor(Color.parseColor("#64748B"));
        etFilter.setTextColor(Color.WHITE);
        etFilter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        etFilter.setSingleLine(true);
        etFilter.setBackgroundColor(Color.TRANSPARENT);
        etFilter.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 6), dp(ctx, 8));
        etFilter.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        filterRow.addView(etFilter);

        final TextView btnClearFilter = new TextView(ctx);
        btnClearFilter.setText("✕");
        btnClearFilter.setTextColor(Color.parseColor("#94A3B8"));
        btnClearFilter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnClearFilter.setTypeface(Typeface.DEFAULT_BOLD);
        btnClearFilter.setPadding(dp(ctx, 10), dp(ctx, 7), dp(ctx, 12), dp(ctx, 7));
        btnClearFilter.setVisibility(View.GONE);
        btnClearFilter.setOnClickListener(v -> etFilter.setText(""));
        filterRow.addView(btnClearFilter);

        root.addView(filterRow);

        LinearLayout statusRow = new LinearLayout(ctx);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        statusRow.setPadding(0, 0, 0, dp(ctx, 6));

        final TextView statusTv = new TextView(ctx);
        statusTv.setTextColor(Color.parseColor("#94A3B8"));
        statusTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusTv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        statusRow.addView(statusTv);

        final TextView btnSelectBatch = new TextView(ctx);
        btnSelectBatch.setText("Select New");
        btnSelectBatch.setTextColor(Color.parseColor("#5EEAD4"));
        btnSelectBatch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSelectBatch.setTypeface(Typeface.DEFAULT_BOLD);
        btnSelectBatch.setPadding(dp(ctx, 10), dp(ctx, 4), dp(ctx, 10), dp(ctx, 4));
        GradientDrawable batchBg = new GradientDrawable();
        batchBg.setColor(Color.parseColor("#1E2433"));
        batchBg.setCornerRadius(dp(ctx, 12));
        btnSelectBatch.setBackground(batchBg);
        LinearLayout.LayoutParams batchLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        batchLp.setMargins(dp(ctx, 8), 0, 0, 0);
        btnSelectBatch.setLayoutParams(batchLp);
        statusRow.addView(btnSelectBatch);

        root.addView(statusRow);

        // File list container
        ScrollView listScroll = new ScrollView(ctx);
        listScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 290)));
        final LinearLayout listContainer = new LinearLayout(ctx);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(listContainer);
        root.addView(listScroll);

        // Import Selected button
        final TextView btnImport = new TextView(ctx);
        btnImport.setText("Import Selected (0)");
        btnImport.setGravity(Gravity.CENTER);
        btnImport.setTextColor(Color.parseColor("#111827"));
        btnImport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnImport.setTypeface(Typeface.DEFAULT_BOLD);
        btnImport.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        GradientDrawable impBg = new GradientDrawable();
        impBg.setColor(Color.parseColor("#34D399"));
        impBg.setCornerRadius(dp(ctx, 10));
        btnImport.setBackground(impBg);
        LinearLayout.LayoutParams impLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        impLp.setMargins(0, dp(ctx, 10), 0, 0);
        btnImport.setLayoutParams(impLp);
        root.addView(btnImport);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(root)
                .setNegativeButton("Close", null)
                .create();

        final List<File> currentVisible = new ArrayList<>();
        final Runnable updateImportLabel = () -> {
            int nSel = selectedFiles.size();
            btnImport.setText("Import Selected (" + nSel + ")");
            if (nSel > 0) {
                btnSelectBatch.setText("Clear (" + nSel + ")");
                btnSelectBatch.setTextColor(Color.parseColor("#F59E0B"));
            } else {
                btnSelectBatch.setText("Select New");
                btnSelectBatch.setTextColor(Color.parseColor("#5EEAD4"));
            }
        };

        final Runnable styleHideToggle = () -> {
            btnHideImported.setText(hideImported[0] ? "Hide Imported: ON" : "Hide Imported: OFF");
            btnHideImported.setTextColor(Color.parseColor(hideImported[0] ? "#111827" : "#CBD5E1"));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(hideImported[0] ? "#34D399" : "#1E2433"));
            bg.setCornerRadius(dp(ctx, 14));
            btnHideImported.setBackground(bg);
        };
        styleHideToggle.run();

        final Runnable renderList = () -> {
            listContainer.removeAllViews();
            String q = filterQuery[0].trim().toLowerCase(Locale.ROOT);
            List<File> matched = new ArrayList<>();
            for (File f : allEpubsInDir) {
                String lower = f.getName().toLowerCase(Locale.ROOT);
                if (hideImported[0] && importedSourceNames.contains(lower)) continue;
                if (!q.isEmpty() && !lower.contains(q)) continue;
                matched.add(f);
            }

            int cap = Math.min(matched.size(), 60);
            currentVisible.clear();
            for (int k = 0; k < cap; k++) currentVisible.add(matched.get(k));
            statusTv.setText("Showing " + cap + " of " + matched.size() + " matching EPUBs ("
                    + allEpubsInDir.size() + " total in folder)");

            for (int i = 0; i < cap; i++) {
                final File epub = matched.get(i);
                final boolean already = importedSourceNames.contains(epub.getName().toLowerCase(Locale.ROOT));

                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
                GradientDrawable rbg = new GradientDrawable();
                rbg.setColor(Color.parseColor("#1A1F2E"));
                rbg.setCornerRadius(dp(ctx, 8));
                row.setBackground(rbg);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(0, 0, 0, dp(ctx, 6));
                row.setLayoutParams(rlp);

                final CheckBox cb = new CheckBox(ctx);
                cb.setChecked(selectedFiles.contains(epub));
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedFiles.contains(epub)) selectedFiles.add(epub);
                    } else {
                        selectedFiles.remove(epub);
                    }
                    updateImportLabel.run();
                });
                row.addView(cb);

                LinearLayout infoCol = new LinearLayout(ctx);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                infoCol.setPadding(dp(ctx, 6), 0, 0, 0);
                infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView nameTv = new TextView(ctx);
                nameTv.setText(epub.getName());
                nameTv.setTextColor(Color.WHITE);
                nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                nameTv.setMaxLines(1);
                nameTv.setEllipsize(TextUtils.TruncateAt.END);
                infoCol.addView(nameTv);

                String sizeStr = formatSize(epub.length()) + (already ? " · Already Imported" : "");
                TextView subTv = new TextView(ctx);
                subTv.setText(sizeStr);
                subTv.setTextColor(Color.parseColor(already ? "#34D399" : "#94A3B8"));
                subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                infoCol.addView(subTv);

                row.addView(infoCol);
                row.setOnClickListener(v -> cb.setChecked(!cb.isChecked()));
                listContainer.addView(row);
            }
        };

        final Runnable loadDir = () -> {
            selectedFiles.clear();
            allEpubsInDir.clear();
            updateImportLabel.run();

            String dirPath = etPath.getText().toString().trim();
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                listContainer.removeAllViews();
                statusTv.setText("Folder not found: " + dirPath);
                return;
            }
            File[] children = dir.listFiles();
            if (children == null) {
                listContainer.removeAllViews();
                statusTv.setText("Cannot read folder: " + dirPath);
                return;
            }
            for (File f : children) {
                if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".epub")) {
                    allEpubsInDir.add(f);
                }
            }
            allEpubsInDir.sort(Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
            renderList.run();
        };

        for (final String qd : QUICK_DIRS) {
            TextView chip = new TextView(ctx);
            chip.setText(qd.replace("/sdcard/", ""));
            chip.setTextColor(Color.parseColor("#CBD5E1"));
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            chip.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
            GradientDrawable cbg = new GradientDrawable();
            cbg.setColor(Color.parseColor("#1E2433"));
            cbg.setCornerRadius(dp(ctx, 14));
            chip.setBackground(cbg);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, dp(ctx, 6), 0);
            chip.setLayoutParams(clp);
            chip.setOnClickListener(v -> {
                etPath.setText(qd);
                loadDir.run();
            });
            chipRow.addView(chip);
        }

        btnHideImported.setOnClickListener(v -> {
            hideImported[0] = !hideImported[0];
            styleHideToggle.run();
            renderList.run();
        });

        btnSelectBatch.setOnClickListener(v -> {
            if (!selectedFiles.isEmpty()) {
                selectedFiles.clear();
            } else {
                int added = 0;
                for (File f : currentVisible) {
                    if (added >= 25) break;
                    if (!importedSourceNames.contains(f.getName().toLowerCase(Locale.ROOT))) {
                        if (!selectedFiles.contains(f)) {
                            selectedFiles.add(f);
                            added++;
                        }
                    }
                }
                if (added == 0) {
                    Toast.makeText(ctx, "All visible EPUBs are already imported", Toast.LENGTH_SHORT).show();
                }
            }
            updateImportLabel.run();
            renderList.run();
        });

        etFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuery[0] = s != null ? s.toString() : "";
                btnClearFilter.setVisibility(filterQuery[0].trim().isEmpty() ? View.GONE : View.VISIBLE);
                renderList.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGo.setOnClickListener(v -> loadDir.run());

        btnImport.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Toast.makeText(ctx, "Select at least one EPUB first", Toast.LENGTH_SHORT).show();
                return;
            }
            final List<File> batch = new ArrayList<>(selectedFiles);
            btnImport.setEnabled(false);
            btnImport.setText("Importing 0/" + batch.size() + "...");

            new Thread(() -> {
                int okCount = 0;
                File targetDir = new File(ctx.getFilesDir(), "folio_library");
                if (!targetDir.exists()) targetDir.mkdirs();

                for (int i = 0; i < batch.size(); i++) {
                    final int idx = i + 1;
                    final File epub = batch.get(i);
                    mainHandler.post(() -> btnImport.setText("Importing " + idx + "/" + batch.size() + "..."));
                    try {
                        EpubMetadataExtractor.BookMeta meta =
                                EpubMetadataExtractor.extract(epub, epub.getName());
                        String bookKey = FolioMetadataDb.computeBookKey(meta.title, epub.length());
                        String safeBase = sanitizeFilename(meta.title) + "_" + bookKey;
                        File outKfx = new File(targetDir, safeBase + ".kfx");
                        File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

                        boolean converted = ExternalEpubActivity.convertEpubToKfx(ctx, epub, outKfx);
                        if (!converted || !outKfx.exists() || outKfx.length() < 128) {
                            Log.w(TAG, "Conversion failed for " + epub.getName());
                            continue;
                        }

                        MetadataEnricher.enrichInPlace(meta);

                        FolioMetadataDb db = new FolioMetadataDb(ctx);
                        try {
                            db.upsertBook(bookKey, outKfx, outCover, epub.getName(), meta);
                        } finally {
                            db.close();
                        }
                        FolioMetadataDb.registerInKindleLibraryDb(ctx, bookKey, outKfx, outCover, meta);
                        importedSourceNames.add(epub.getName().toLowerCase(Locale.ROOT));
                        okCount++;
                    } catch (Throwable t) {
                        Log.e(TAG, "Batch import error for " + epub.getName(), t);
                    }
                }

                final int finalOk = okCount;
                mainHandler.post(() -> {
                    btnImport.setEnabled(true);
                    selectedFiles.clear();
                    updateImportLabel.run();
                    Toast.makeText(ctx,
                            "Imported " + finalOk + " of " + batch.size() + " EPUBs to Folio Library",
                            Toast.LENGTH_LONG).show();
                    renderList.run();
                });
            }, "folio-batch-import").start();
        });

        loadDir.run();
        dialog.show();
    }

    private static GradientDrawable makeInputBg(Context ctx) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A1F2E"));
        bg.setCornerRadius(dp(ctx, 8));
        bg.setStroke(dp(ctx, 1), Color.parseColor("#2A3142"));
        return bg;
    }

    private static Set<String> loadImportedSources(Context ctx) {
        Set<String> set = new HashSet<>();
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            for (FolioMetadataDb.BookRecord r : db.getAllBooks()) {
                if (r.sourceName != null && !r.sourceName.isEmpty()) {
                    set.add(r.sourceName.toLowerCase(Locale.ROOT));
                }
            }
        } finally {
            db.close();
        }
        return set;
    }

    private static String pickDefaultDir() {
        for (String d : QUICK_DIRS) {
            File f = new File(d);
            if (f.exists() && f.isDirectory()) return d;
        }
        return "/sdcard/Download";
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return Math.max(1, bytes / 1024) + " KB";
    }

    private static String sanitizeFilename(String s) {
        if (s == null || s.trim().isEmpty()) return "book";
        String clean = s.replaceAll("[^a-zA-Z0-9._-]+", "_").replaceAll("_+", "_");
        if (clean.length() > 48) clean = clean.substring(0, 48);
        return clean.replaceAll("^_+|_+$", "");
    }

    private static int dp(Context ctx, int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics()));
    }
}
