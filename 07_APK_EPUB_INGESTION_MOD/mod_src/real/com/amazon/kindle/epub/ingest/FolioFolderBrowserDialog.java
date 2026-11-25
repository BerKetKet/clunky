package com.amazon.kindle.epub.ingest;

import android.widget.FrameLayout;
import com.amazon.kindle.epub.catalog.FolioIcons;
import com.amazon.kindle.epub.catalog.FolioTheme;

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

        FolioTheme.isLimestoneLight(ctx);
        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(FolioTheme.bgModal());
        root.setPadding(dp(ctx, 18), dp(ctx, 16), dp(ctx, 18), dp(ctx, 12));

        // Top row: Eyebrow + Hide Imported toggle
        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView eyebrow = new TextView(ctx);
        eyebrow.setText("BATCH EPUB IMPORTER");
        eyebrow.setTextColor(FolioTheme.textMuted());
        eyebrow.setLetterSpacing(0.10f);
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(eyebrow);

        final String[] folderSortLabels = new String[] { "A–Z", "Newest", "Largest", "Smallest" };
        final int[] folderSortIdx = new int[] { 0 };
        final TextView btnSortFolder = new TextView(ctx);
        btnSortFolder.setText("Sort: A–Z");
        btnSortFolder.setTextColor(FolioTheme.textPrimary());
        btnSortFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSortFolder.setTypeface(Typeface.DEFAULT_BOLD);
        btnSortFolder.setPadding(dp(ctx, 9), dp(ctx, 5), dp(ctx, 9), dp(ctx, 5));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(FolioTheme.bgControl());
        sBg.setCornerRadius(dp(ctx, 999));
        sBg.setStroke(dp(ctx, 1), FolioTheme.border());
        btnSortFolder.setBackground(sBg);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sLp.setMargins(0, 0, dp(ctx, 6), 0);
        btnSortFolder.setLayoutParams(sLp);
        topRow.addView(btnSortFolder);

        final TextView btnHideImported = new TextView(ctx);
        btnHideImported.setText("Hide Imported: OFF");
        btnHideImported.setTextColor(FolioTheme.textSecondary());
        btnHideImported.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnHideImported.setTypeface(Typeface.DEFAULT_BOLD);
        btnHideImported.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
        topRow.addView(btnHideImported);
        root.addView(topRow);

        TextView heading = new TextView(ctx);
        heading.setText("Import EPUBs from Storage");
        heading.setTextColor(FolioTheme.textPrimary());
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        heading.setTypeface(FolioTheme.serif(ctx));
        heading.setLetterSpacing(-0.015f);
        heading.setPadding(0, dp(ctx, 4), 0, dp(ctx, 10));
        root.addView(heading);

        // Path bar + Open button
        LinearLayout pathRow = new LinearLayout(ctx);
        pathRow.setOrientation(LinearLayout.HORIZONTAL);
        pathRow.setGravity(Gravity.CENTER_VERTICAL);

        final EditText etPath = new EditText(ctx);
        String savedDir = null;
        try {
            savedDir = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getString("last_import_dir", null);
        } catch (Throwable ignored) {}
        String initDir = (savedDir != null && !savedDir.trim().isEmpty() && new File(savedDir).isDirectory())
                ? savedDir.trim() : pickDefaultDir();
        etPath.setText(initDir);
        etPath.setTextColor(FolioTheme.textPrimary());
        etPath.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        etPath.setSingleLine(true);
        etPath.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        etPath.setBackground(makeInputBg(ctx));
        etPath.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        final FrameLayout btnUpDir = FolioIcons.makeIconButton(
                ctx, FolioIcons.UP_ARROW, 16, 8, FolioIcons.COLOR_ICON, FolioTheme.bgControl(), FolioTheme.border(), 8);
        LinearLayout.LayoutParams upLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        upLp.setMargins(0, 0, dp(ctx, 6), 0);
        btnUpDir.setLayoutParams(upLp);
        pathRow.addView(btnUpDir);

        pathRow.addView(etPath);

        TextView btnGo = new TextView(ctx);
        btnGo.setText("Open");
        btnGo.setTextColor(FolioTheme.bgCanvas());
        btnGo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        btnGo.setTypeface(Typeface.DEFAULT_BOLD);
        btnGo.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        GradientDrawable goBg = new GradientDrawable();
        goBg.setColor(FolioTheme.accent());
        goBg.setCornerRadius(dp(ctx, 8));
        btnGo.setBackground(goBg);
        LinearLayout.LayoutParams goLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        goLp.setMargins(dp(ctx, 8), 0, 0, 0);
        btnGo.setLayoutParams(goLp);
        pathRow.addView(btnGo);

        final TextView btnPinDir = new TextView(ctx);
        btnPinDir.setText("☆");
        btnPinDir.setTextColor(FolioTheme.textMuted());
        btnPinDir.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btnPinDir.setTypeface(Typeface.DEFAULT_BOLD);
        btnPinDir.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
        final GradientDrawable pinBg = new GradientDrawable();
        pinBg.setColor(FolioTheme.bgControl());
        pinBg.setCornerRadius(dp(ctx, 8));
        pinBg.setStroke(dp(ctx, 1), FolioTheme.border());
        btnPinDir.setBackground(pinBg);
        LinearLayout.LayoutParams pinLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pinLp.setMargins(dp(ctx, 6), 0, 0, 0);
        btnPinDir.setLayoutParams(pinLp);
        pathRow.addView(btnPinDir);
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
        etFilter.setHintTextColor(FolioTheme.textMuted());
        etFilter.setTextColor(FolioTheme.textPrimary());
        etFilter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        etFilter.setSingleLine(true);
        etFilter.setBackgroundColor(Color.TRANSPARENT);
        etFilter.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 6), dp(ctx, 8));
        etFilter.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        filterRow.addView(etFilter);

        final TextView btnClearFilter = new TextView(ctx);
        btnClearFilter.setText("✕");
        btnClearFilter.setTextColor(FolioTheme.textMuted());
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
        statusTv.setTextColor(FolioTheme.textMuted());
        statusTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusTv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        statusRow.addView(statusTv);

        final TextView btnSelectBatch = new TextView(ctx);
        btnSelectBatch.setText("Select New");
        btnSelectBatch.setTextColor(FolioTheme.accent());
        btnSelectBatch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnSelectBatch.setTypeface(Typeface.DEFAULT_BOLD);
        btnSelectBatch.setPadding(dp(ctx, 10), dp(ctx, 4), dp(ctx, 10), dp(ctx, 4));
        GradientDrawable batchBg = new GradientDrawable();
        batchBg.setColor(FolioTheme.bgControl());
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
        btnImport.setTextColor(FolioTheme.bgCanvas());
        btnImport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnImport.setTypeface(Typeface.DEFAULT_BOLD);
        btnImport.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        GradientDrawable impBg = new GradientDrawable();
        impBg.setColor(FolioTheme.accent());
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
        final List<File> subDirsInDir = new ArrayList<>();
        final Runnable[] loadDirRef = new Runnable[1];
        final Runnable[] refreshChipsHolder = new Runnable[1];
        final Runnable updateImportLabel = () -> {
            int nSel = selectedFiles.size();
            if (nSel == 0) {
                btnImport.setText("Import Selected (0)");
            } else {
                long totalBytes = 0L;
                for (File sf : selectedFiles) totalBytes += sf.length();
                String szStr = totalBytes >= 1024L * 1024L
                        ? String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
                        : Math.max(1L, totalBytes / 1024L) + " KB";
                btnImport.setText("Import Selected (" + nSel + " · " + szStr + ")");
            }
            if (nSel > 0) {
                btnSelectBatch.setText("Clear (" + nSel + ")");
                btnSelectBatch.setTextColor(FolioTheme.accent());
            } else {
                btnSelectBatch.setText("Select New");
                btnSelectBatch.setTextColor(FolioTheme.accent());
            }
        };

        final Runnable styleHideToggle = () -> {
            btnHideImported.setText(hideImported[0] ? "Hide Imported: ON" : "Hide Imported: OFF");
            btnHideImported.setTextColor((hideImported[0] ? FolioTheme.accentSoftText() : FolioTheme.textSecondary()));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor((hideImported[0] ? FolioTheme.accentFill() : FolioTheme.bgControl()));
            bg.setCornerRadius(dp(ctx, 999));
            bg.setStroke(dp(ctx, 1), (hideImported[0] ? FolioTheme.accentBorder() : FolioTheme.border()));
            btnHideImported.setBackground(bg);
        };
        styleHideToggle.run();

        final int[] displayLimit = new int[] { 60 };
        final Runnable[] renderListRef = new Runnable[1];
        final Runnable renderList = () -> {
            listContainer.removeAllViews();
            String q = filterQuery[0].trim().toLowerCase(Locale.ROOT);

            // Render clickable 📁 Subfolder rows at top of list
            int dirShown = 0;
            for (final File sd : subDirsInDir) {
                String dName = sd.getName();
                if (!q.isEmpty() && !dName.toLowerCase(Locale.ROOT).contains(q)) continue;
                if (dirShown++ >= 20) break;
                LinearLayout dRow = new LinearLayout(ctx);
                dRow.setOrientation(LinearLayout.HORIZONTAL);
                dRow.setGravity(Gravity.CENTER_VERTICAL);
                dRow.setPadding(dp(ctx, 12), dp(ctx, 9), dp(ctx, 12), dp(ctx, 9));
                GradientDrawable dBg = new GradientDrawable();
                dBg.setColor(FolioTheme.bgSurface());
                dBg.setCornerRadius(dp(ctx, 8));
                dBg.setStroke(dp(ctx, 1), FolioTheme.border());
                dRow.setBackground(dBg);
                LinearLayout.LayoutParams drLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                drLp.setMargins(0, 0, 0, dp(ctx, 6));
                dRow.setLayoutParams(drLp);

                TextView dnTv = new TextView(ctx);
                dnTv.setText("📁 " + dName);
                dnTv.setTextColor(FolioTheme.textPrimary());
                dnTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                dnTv.setTypeface(Typeface.DEFAULT_BOLD);
                dnTv.setSingleLine(true);
                dnTv.setEllipsize(TextUtils.TruncateAt.END);
                dnTv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                dRow.addView(dnTv);

                TextView goTv = new TextView(ctx);
                goTv.setText("Open ›");
                goTv.setTextColor(FolioTheme.textMuted());
                goTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                dRow.addView(goTv);

                dRow.setOnClickListener(v -> {
                    etPath.setText(sd.getAbsolutePath());
                    if (loadDirRef[0] != null) loadDirRef[0].run();
                    if (refreshChipsHolder[0] != null) refreshChipsHolder[0].run();
                });
                listContainer.addView(dRow);
            }
            List<File> matched = new ArrayList<>();
            for (File f : allEpubsInDir) {
                String lower = f.getName().toLowerCase(Locale.ROOT);
                if (hideImported[0] && importedSourceNames.contains(lower)) continue;
                if (!q.isEmpty() && !lower.contains(q)) continue;
                matched.add(f);
            }

            final int sMode = folderSortIdx[0];
            java.util.Collections.sort(matched, (a, b) -> {
                if (sMode == 1) return Long.compare(b.lastModified(), a.lastModified());
                if (sMode == 2) return Long.compare(b.length(), a.length());
                if (sMode == 3) return Long.compare(a.length(), b.length());
                return a.getName().compareToIgnoreCase(b.getName());
            });

            int cap = Math.min(matched.size(), displayLimit[0]);
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
                rbg.setColor(FolioTheme.bgSurface());
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
                nameTv.setTextColor(FolioTheme.textPrimary());
                nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                nameTv.setMaxLines(1);
                nameTv.setEllipsize(TextUtils.TruncateAt.END);
                infoCol.addView(nameTv);

                String sizeStr = formatSize(epub.length()) + (already ? " · Already Imported" : "");
                TextView subTv = new TextView(ctx);
                subTv.setText(sizeStr);
                subTv.setTextColor((already ? FolioTheme.accent() : FolioTheme.textMuted()));
                subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                infoCol.addView(subTv);

                row.addView(infoCol);
                row.setOnClickListener(v -> cb.setChecked(!cb.isChecked()));
                listContainer.addView(row);
            }

            int remaining = matched.size() - cap;
            if (remaining > 0) {
                int step = Math.min(60, remaining);
                TextView btnMore = new TextView(ctx);
                btnMore.setText("Load More (+" + step + " of " + remaining + " remaining)");
                btnMore.setGravity(Gravity.CENTER);
                btnMore.setTextColor(FolioTheme.accent());
                btnMore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                btnMore.setTypeface(Typeface.DEFAULT_BOLD);
                btnMore.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
                GradientDrawable mBg = new GradientDrawable();
                mBg.setColor(FolioTheme.bgControl());
                mBg.setCornerRadius(dp(ctx, 8));
                mBg.setStroke(dp(ctx, 1), FolioTheme.border());
                btnMore.setBackground(mBg);
                LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mLp.setMargins(0, dp(ctx, 4), 0, dp(ctx, 6));
                btnMore.setLayoutParams(mLp);
                btnMore.setOnClickListener(v -> {
                    displayLimit[0] += 60;
                    if (renderListRef[0] != null) renderListRef[0].run();
                });
                listContainer.addView(btnMore);
            }
        };
        renderListRef[0] = renderList;

        final Runnable loadDir = () -> {
            displayLimit[0] = 60;
            selectedFiles.clear();
            allEpubsInDir.clear();
            subDirsInDir.clear();
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
            try {
                ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .edit().putString("last_import_dir", dirPath).apply();
            } catch (Throwable ignored) {}
            for (File f : children) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    subDirsInDir.add(f);
                } else if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".epub")) {
                    allEpubsInDir.add(f);
                }
            }
            subDirsInDir.sort(Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
            allEpubsInDir.sort(Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
            renderList.run();
        };
        loadDirRef[0] = loadDir;

        final Runnable[] refreshDirChipsRef = new Runnable[1];
        refreshDirChipsRef[0] = () -> {
            chipRow.removeAllViews();
            String curDir = etPath.getText() != null ? etPath.getText().toString().trim() : "";
            java.util.LinkedHashSet<String> pinned = loadPinnedDirs(ctx);
            boolean curIsPinned = !curDir.isEmpty() && pinned.contains(curDir);
            btnPinDir.setText(curIsPinned ? "★" : "☆");
            btnPinDir.setTextColor((curIsPinned ? FolioTheme.accent() : FolioTheme.textMuted()));
            pinBg.setColor((curIsPinned ? FolioTheme.accentFill() : FolioTheme.bgControl()));
            pinBg.setStroke(dp(ctx, 1), (curIsPinned ? FolioTheme.accentBorder() : FolioTheme.border()));

            java.util.LinkedHashMap<String, Boolean> allDirs = new java.util.LinkedHashMap<>();
            for (String pd : pinned) allDirs.put(pd, Boolean.TRUE);
            for (String qd : QUICK_DIRS) {
                if (!allDirs.containsKey(qd)) allDirs.put(qd, Boolean.FALSE);
            }
            for (java.util.Map.Entry<String, Boolean> ent : allDirs.entrySet()) {
                final String qd = ent.getKey();
                final boolean isPin = Boolean.TRUE.equals(ent.getValue());
                final boolean isCur = qd.equals(curDir);
                TextView chip = new TextView(ctx);
                String shortLbl = qd.replace("/sdcard/", "");
                if (shortLbl.isEmpty()) shortLbl = qd;
                chip.setText((isPin ? "★ " : "") + shortLbl);
                chip.setTextColor(Color.parseColor(isCur ? "#15171A" : (isPin ? "#D4C9B8" : "#BDB8AE")));
                chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                chip.setTypeface(isCur || isPin ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                chip.setPadding(dp(ctx, 10), dp(ctx, 5), dp(ctx, 10), dp(ctx, 5));
                GradientDrawable cbg = new GradientDrawable();
                cbg.setColor((isCur ? FolioTheme.accent() : FolioTheme.bgControl()));
                cbg.setCornerRadius(dp(ctx, 14));
                if (!isCur) cbg.setStroke(dp(ctx, 1), (isPin ? FolioTheme.accentBorder() : FolioTheme.border()));
                chip.setBackground(cbg);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.setMargins(0, 0, dp(ctx, 6), 0);
                chip.setLayoutParams(clp);
                chip.setOnClickListener(v -> {
                    etPath.setText(qd);
                    loadDir.run();
                    refreshDirChipsRef[0].run();
                });
                chipRow.addView(chip);
            }
        };
        refreshChipsHolder[0] = refreshDirChipsRef[0];
        btnUpDir.setOnClickListener(v -> {
            String cur = etPath.getText() != null ? etPath.getText().toString().trim() : "";
            File curF = new File(cur);
            File parent = curF.getParentFile();
            if (parent != null && parent.isDirectory() && parent.canRead()) {
                etPath.setText(parent.getAbsolutePath());
                loadDir.run();
                refreshDirChipsRef[0].run();
            } else {
                Toast.makeText(ctx, "Already at top readable folder", Toast.LENGTH_SHORT).show();
            }
        });
        refreshDirChipsRef[0].run();

        btnPinDir.setOnClickListener(v -> {
            String curDir = etPath.getText() != null ? etPath.getText().toString().trim() : "";
            if (curDir.isEmpty() || !new File(curDir).isDirectory()) {
                Toast.makeText(ctx, "Open a valid folder to pin it", Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.LinkedHashSet<String> pinned = loadPinnedDirs(ctx);
            boolean nowPinned;
            if (pinned.contains(curDir)) {
                pinned.remove(curDir);
                nowPinned = false;
            } else {
                pinned.add(curDir);
                nowPinned = true;
            }
            savePinnedDirs(ctx, pinned);
            refreshDirChipsRef[0].run();
            Toast.makeText(ctx,
                    (nowPinned ? "★ Pinned folder: " : "Removed pin: ") + curDir.replace("/sdcard/", ""),
                    Toast.LENGTH_SHORT).show();
        });

        refreshDirChipsRef[0].run();

        btnSortFolder.setOnClickListener(v -> {
            folderSortIdx[0] = (folderSortIdx[0] + 1) % folderSortLabels.length;
            btnSortFolder.setText("Sort: " + folderSortLabels[folderSortIdx[0]]);
            renderList.run();
        });

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

        btnGo.setOnClickListener(v -> { loadDir.run(); refreshDirChipsRef[0].run(); });

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
                File sdKindle = new File("/sdcard/kindle");
                if (!sdKindle.exists()) sdKindle.mkdirs();
                File targetDir = (sdKindle.exists() && sdKindle.canWrite())
                        ? sdKindle : new File(ctx.getFilesDir(), "folio_library");
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
                        if (!outKfx.getAbsolutePath().startsWith("/sdcard/kindle")) {
                            FolioMetadataDb.registerInKindleLibraryDb(ctx, bookKey, outKfx, outCover, meta);
                        }
                        dp.g.ingestKfxLive(outKfx);
                        com.amazon.kindle.epub.catalog.FolioTheme.triggerLibraryRescan(ctx, outKfx);
                        try { android.media.MediaScannerConnection.scanFile(ctx, new String[]{ outKfx.getAbsolutePath() }, null, null); } catch (Throwable ignored) {}
                        importedSourceNames.add(epub.getName().toLowerCase(Locale.ROOT));
                        okCount++;
                    } catch (Throwable t) {
                        Log.e(TAG, "Batch import error for " + epub.getName(), t);
                    }
                }

                try { FolioMetadataDb.cleanupLegacyDuplicateRows(ctx); } catch (Throwable ignored) {}
                final int finalOk = okCount;
                mainHandler.post(() -> {
                    btnImport.setEnabled(true);
                    selectedFiles.clear();
                    updateImportLabel.run();
                    renderList.run();
                    if (finalOk > 0) {
                        AlertDialog doneDlg = new AlertDialog.Builder(ctx)
                                .setTitle("Imported to Azath Library")
                                .setMessage("Converted " + finalOk + " of " + batch.size() + " EPUB" + (batch.size() == 1 ? "" : "s")
                                        + " into /sdcard/kindle/. Refresh now so your Library displays the new book" + (finalOk == 1 ? "" : "s") + " immediately.")
                                .setPositiveButton("Refresh Library Now", (d2, w2) -> {
                                    try {
                                        android.content.Intent li = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                                        if (li != null) {
                                            li.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            ctx.startActivity(li);
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                        }
                                    } catch (Throwable ignored) {}
                                })
                                .setNegativeButton("Keep Browsing", null)
                                .show();
                        FolioTheme.applyToDialog(ctx, doneDlg);
                    } else {
                        Toast.makeText(ctx,
                                "Imported 0 of " + batch.size() + " EPUBs",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }, "folio-batch-import").start();
        });

        loadDir.run();
        FolioTheme.applyToTree(ctx, root);
        dialog.show();
        FolioTheme.styleDialog(ctx, dialog);
    }

    private static GradientDrawable makeInputBg(Context ctx) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(FolioTheme.bgSurface());
        bg.setCornerRadius(dp(ctx, 8));
        bg.setStroke(dp(ctx, 1), FolioTheme.border());
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

    private static java.util.LinkedHashSet<String> loadPinnedDirs(Context ctx) {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        try {
            String raw = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getString("pinned_import_dirs", "");
            if (raw != null && !raw.isEmpty()) {
                for (String line : raw.split("\n")) {
                    String s = line.trim();
                    if (!s.isEmpty()) set.add(s);
                }
            }
        } catch (Throwable ignored) {}
        return set;
    }

    private static void savePinnedDirs(Context ctx, java.util.Set<String> dirs) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String d : dirs) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(d);
            }
            ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .edit().putString("pinned_import_dirs", sb.toString()).apply();
        } catch (Throwable ignored) {}
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
