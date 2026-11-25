package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Folio Home Screen — Rich dark editorial discovery & reading hub:
 * - Sleek rounded search bar + source pills (Gutenberg / OPDS / Backup) + Genre chips
 * - "Continue Reading" horizontal cover carousel from local kindle_library.db
 * - "Because You Read ..." personalized horizontal cover carousel via Open Library API
 * - "Popular Free EPUBs" horizontal cover carousel with live cover thumbnails
 * - Cover-illustrated search results & Book Details dialog with 4-tier ZIP-validated downloads
 */
public final class OpenCatalogView {
    private static final String PREFS_NAME = "folio_catalog_prefs";
    private static final String KEY_CUSTOM_OPDS = "custom_opds_url";

    private static final int BG_DARK = 0xFF0E0F11;
    private static final int BG_SURFACE = 0xFF1C1E22;
    private static final int BG_CARD = 0xFF25282D;
    private static final int ACCENT_AMBER = 0xFFD4C9B8;
    private static final int ACCENT_TEAL = 0xFFE6DDD0;
    private static final int TEXT_PRIMARY = 0xFFECE8E0;
    private static final int TEXT_MUTED = 0xFF8A857D;

    private static final ExecutorService IMG_POOL = Executors.newFixedThreadPool(4);

    public static View wrap(View originalView) {
        try {
            if (originalView == null) return null;
            final Context ctx = originalView.getContext();
            final FrameLayout host = new FrameLayout(ctx);
            host.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            host.addView(build(ctx), new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            final Runnable hideTask = new Runnable() {
                @Override
                public void run() {
                    hideRedundantKindleSearchBar(host);
                }
            };
            host.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    FolioTheme.setActiveBottomTab(ctx, 0);
                    view.post(hideTask);
                    view.postDelayed(hideTask, 250);
                    view.postDelayed(hideTask, 800);
                }
                @Override
                public void onViewDetachedFromWindow(View view) {}
            });
            host.post(hideTask);
            host.postDelayed(hideTask, 400);

            final Runnable rebuilder = new Runnable() {
                @Override public void run() {
                    host.post(new Runnable() {
                        @Override public void run() {
                            try {
                                host.removeAllViews();
                                host.addView(build(ctx), new FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                host.post(hideTask);
                            } catch (Throwable ignored) {}
                        }
                    });
                }
            };
            host.setTag(rebuilder); // keep strong ref tied to host lifecycle
            FolioTheme.registerThemeListener(rebuilder);
            return host;
        } catch (Throwable t) {
            android.util.Log.e("OpenCatalogView", "Failed to build Azath Home", t);
            return originalView;
        }
    }

    private static java.lang.ref.WeakReference<View> sStockBellRef = null;

    private static boolean isAncestorOf(View ancestor, View child) {
        if (ancestor == null || child == null) return false;
        View cur = child;
        while (cur != null) {
            if (cur == ancestor) return true;
            if (cur.getParent() instanceof View) {
                cur = (View) cur.getParent();
            } else {
                break;
            }
        }
        return false;
    }

    private static View findClickableBellInSubtree(View v) {
        if (v == null) return null;
        CharSequence cd = v.getContentDescription();
        String cds = cd != null ? cd.toString().toLowerCase(Locale.US) : "";
        if (cds.contains("notif") || cds.contains("bell") || cds.contains("alert") || cds.contains("inbox")) {
            return v;
        }
        if (v instanceof ImageView && v.isClickable()) {
            return v;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                View found = findClickableBellInSubtree(vg.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void hideRedundantKindleSearchBar(View anchor) {
        try {
            View root = anchor.getRootView();
            if (!(root instanceof ViewGroup)) return;
            float d = anchor.getResources().getDisplayMetrics().density;
            int screenH = anchor.getResources().getDisplayMetrics().heightPixels;
            hideSearchKindleRecursive((ViewGroup) root, anchor, d, screenH);
        } catch (Throwable ignored) {}
    }

    private static void hideSearchKindleRecursive(ViewGroup vg, View skipSubtree, float d, int screenH) {
        if (vg == skipSubtree) return;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View c = vg.getChildAt(i);
            if (c == skipSubtree) continue;
            if (c instanceof TextView) {
                CharSequence txt = ((TextView) c).getText();
                CharSequence hint = ((TextView) c).getHint();
                String s1 = txt != null ? txt.toString().trim() : "";
                String s2 = hint != null ? hint.toString().trim() : "";
                if (s1.contains("Search Kindle") || s2.contains("Search Kindle")
                        || s1.contains("Search Folio") || s2.contains("Search Folio")
                        || s1.contains("Search Azath") || s2.contains("Search Azath")) {
                    View target = c;
                    if (c.getParent() instanceof View && c.getParent() != vg.getRootView()
                            && !isAncestorOf((View) c.getParent(), skipSubtree)) {
                        target = (View) c.getParent();
                    }
                    target.setVisibility(View.GONE);
                    continue;
                }
                // Collapse the redundant stock top "Home" bar (strictly in top third of screen so bottom nav "Home" is untouched)
                if ("Home".equals(s1) && c.getVisibility() == View.VISIBLE) {
                    int[] loc = new int[2];
                    c.getLocationOnScreen(loc);
                    if (loc[1] >= 0 && loc[1] < screenH / 3) {
                        View bar = c;
                        while (bar.getParent() instanceof View) {
                            View p = (View) bar.getParent();
                            if (p == vg.getRootView() || isAncestorOf(p, skipSubtree)) break;
                            int ph = p.getHeight();
                            if (ph > 0 && ph <= dp(d, 136)) {
                                bar = p;
                            } else {
                                break;
                            }
                        }
                        if (!isAncestorOf(bar, skipSubtree)) {
                            View bell = findClickableBellInSubtree(bar);
                            if (bell != null) {
                                sStockBellRef = new java.lang.ref.WeakReference<View>(bell);
                            }
                            bar.setVisibility(View.GONE);
                            continue;
                        }
                    }
                }
            }
            if (c instanceof ViewGroup) {
                hideSearchKindleRecursive((ViewGroup) c, skipSubtree, d, screenH);
            }
        }
    }

    private static View makeBellIconButton(final Context ctx, final float d) {
        FrameLayout btn = new FrameLayout(ctx);
        btn.setBackground(roundRect(FolioTheme.bgSurface(ctx), dp(d, 999), FolioTheme.border(ctx), dp(d, 1)));
        int size = dp(d, 34);
        btn.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        View icon = new View(ctx) {
            @Override
            protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);
                android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                p.setColor(FolioTheme.textPrimary(ctx));
                p.setStyle(android.graphics.Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(1.8f, 1.55f * d));
                p.setStrokeCap(android.graphics.Paint.Cap.ROUND);
                p.setStrokeJoin(android.graphics.Paint.Join.ROUND);

                float w = getWidth();
                float h = getHeight();
                float cx = w * 0.5f;
                // Bell dome + flared skirt
                android.graphics.Path path = new android.graphics.Path();
                path.moveTo(w * 0.27f, h * 0.68f);
                path.lineTo(w * 0.31f, h * 0.44f);
                path.cubicTo(w * 0.31f, h * 0.25f, w * 0.69f, h * 0.25f, w * 0.69f, h * 0.44f);
                path.lineTo(w * 0.73f, h * 0.68f);
                path.close();
                canvas.drawPath(path, p);
                // Top crown dot
                canvas.drawLine(cx, h * 0.20f, cx, h * 0.26f, p);
                // Clapper arc at bottom
                android.graphics.RectF clap = new android.graphics.RectF(cx - w * 0.08f, h * 0.67f, cx + w * 0.08f, h * 0.79f);
                canvas.drawArc(clap, 15f, 150f, false, p);
            }
        };
        int icSz = dp(d, 20);
        FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(icSz, icSz);
        ilp.gravity = Gravity.CENTER;
        btn.addView(icon, ilp);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent ni = new Intent();
                    ni.setClassName(ctx.getPackageName(), "com.amazon.kindle.inapp.notifications.InAppNotificationsActivity");
                    ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(ni);
                    return;
                } catch (Throwable ignored) {}
                View stockBell = sStockBellRef != null ? sStockBellRef.get() : null;
                if (stockBell != null) {
                    try {
                        stockBell.performClick();
                        return;
                    } catch (Throwable ignored) {}
                }
                Toast.makeText(ctx, "No new notifications", Toast.LENGTH_SHORT).show();
            }
        });
        return btn;
    }

    public static View buildCatalogView(final Context ctx) {
        return build(ctx);
    }

    public static View build(final Context ctx) {
        DiskCache.init(ctx);
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final float d = ctx.getResources().getDisplayMetrics().density;

        final List<BookSource> sources = new ArrayList<BookSource>();
        sources.add(new GutendexSource());
        String savedOpds = prefs.getString(KEY_CUSTOM_OPDS, "");
        if (savedOpds != null && !savedOpds.isEmpty()) {
            sources.add(new OpdsSource("OPDS Feed", savedOpds, ""));
        }
        final int[] activeSourceIdx = new int[]{0};

        final int cBgDark = FolioTheme.bgCanvas(ctx);
        final int cBgSurface = FolioTheme.bgSurface(ctx);
        final int cBgCard = FolioTheme.bgControl(ctx);
        final int cBorder = FolioTheme.border(ctx);
        final int cTextPrimary = FolioTheme.textPrimary(ctx);
        final int cTextMuted = FolioTheme.textMuted(ctx);
        final int cAccent = FolioTheme.accent(ctx);
        final int cAccentFill = FolioTheme.accentFill(ctx);
        final int cAccentBorder = FolioTheme.accentBorder(ctx);
        final int cAccentSoftText = FolioTheme.accentSoftText(ctx);
        final int cBtnTextOnAccent = FolioTheme.isLimestoneLight(ctx) ? 0xFFF4F0E8 : 0xFF0E0F11;

        ScrollView mainScroll = new ScrollView(ctx);
        mainScroll.setBackgroundColor(cBgDark);
        mainScroll.setFillViewport(true);
        mainScroll.setClipToPadding(false);
        mainScroll.setPadding(0, 0, 0, dp(d, 96));
        mainScroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBgDark);
        root.setPadding(dp(d, 16), dp(d, 14), dp(d, 16), dp(d, 24));
        mainScroll.addView(root);

        // --- Top Brand Header ---
        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(d, 12));

        TextView brand = new TextView(ctx);
        brand.setText("Azath");
        brand.setTextColor(cTextPrimary);
        brand.setTextSize(22f);
        brand.setTypeface(FolioTheme.serif(ctx));
        brand.setLetterSpacing(-0.02f);
        headerRow.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView opdsPill = makePill(ctx, d, sources.size() > 1 ? "OPDS" : "+ OPDS", cBgSurface, cTextPrimary);
        headerRow.addView(opdsPill);

        View gap = new View(ctx);
        headerRow.addView(gap, new LinearLayout.LayoutParams(dp(d, 8), 1));

        TextView backupPill = makePill(ctx, d, "Backup", cAccentFill, cAccentSoftText);
        backupPill.setBackground(roundRect(cAccentFill, dp(d, 999), cAccentBorder, dp(d, 1)));
        headerRow.addView(backupPill);

        View gapBell = new View(ctx);
        headerRow.addView(gapBell, new LinearLayout.LayoutParams(dp(d, 8), 1));

        View bellBtn = makeBellIconButton(ctx, d);
        headerRow.addView(bellBtn);
        root.addView(headerRow);

        // --- Rounded Search Bar ---
        LinearLayout searchBar = new LinearLayout(ctx);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setBackground(roundRect(cBgSurface, dp(d, 24), cBorder, dp(d, 1)));
        searchBar.setPadding(dp(d, 16), dp(d, 4), dp(d, 8), dp(d, 4));
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbLp.bottomMargin = dp(d, 12);

        final EditText searchInput = new EditText(ctx);
        searchInput.setHint("Search books, authors, or topics...");
        searchInput.setHintTextColor(cTextMuted);
        searchInput.setTextColor(cTextPrimary);
        searchInput.setTextSize(15f);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setSingleLine(true);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.addView(searchInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final TextView searchGo = makePill(ctx, d, "Search", cAccent, cBtnTextOnAccent);
        searchBar.addView(searchGo);
        root.addView(searchBar, sbLp);

        // --- Genre Quick Chips ---
        HorizontalScrollView chipScroll = new HorizontalScrollView(ctx);
        chipScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chipRow = new LinearLayout(ctx);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipRow.setPadding(0, 0, 0, dp(d, 14));
        chipScroll.addView(chipRow);
        root.addView(chipScroll);

        // --- Search Results Container (hidden until user searches) ---
        final LinearLayout searchSection = new LinearLayout(ctx);
        searchSection.setOrientation(LinearLayout.VERTICAL);
        searchSection.setVisibility(View.GONE);
        root.addView(searchSection);

        // --- Section 1: Continue Reading (Local Library Carousel) ---
        final LinearLayout libSection = new LinearLayout(ctx);
        libSection.setOrientation(LinearLayout.VERTICAL);
        libSection.setVisibility(View.GONE);
        root.addView(libSection);

        // --- Section 2: Because You Read ... (Open Library Personalized Carousel) ---
        final LinearLayout recSection = new LinearLayout(ctx);
        recSection.setOrientation(LinearLayout.VERTICAL);
        recSection.setVisibility(View.GONE);
        root.addView(recSection);

        // --- Section 3: Popular Free EPUBs (Gutendex Carousel) ---
        final LinearLayout popSection = new LinearLayout(ctx);
        popSection.setOrientation(LinearLayout.VERTICAL);
        root.addView(popSection);

        final TextView popTitle = makeSectionTitle(ctx, d, "Popular Free EPUBs");
        popSection.addView(popTitle);
        final TextView popStatus = new TextView(ctx);
        popStatus.setText("Loading Project Gutenberg catalog...");
        popStatus.setTextColor(TEXT_MUTED);
        popStatus.setTextSize(13f);
        popStatus.setPadding(0, dp(d, 6), 0, dp(d, 12));
        popSection.addView(popStatus);

        final HorizontalScrollView popScroll = new HorizontalScrollView(ctx);
        popScroll.setHorizontalScrollBarEnabled(false);
        final LinearLayout popCarousel = new LinearLayout(ctx);
        popCarousel.setOrientation(LinearLayout.HORIZONTAL);
        popScroll.addView(popCarousel);
        popSection.addView(popScroll);

        // --- Search Action ---
        final Runnable doSearch = new Runnable() {
            @Override
            public void run() {
                final String q = searchInput.getText().toString().trim();
                final BookSource src = sources.get(activeSourceIdx[0]);
                if (q.isEmpty() && activeSourceIdx[0] == 0) {
                    searchSection.setVisibility(View.GONE);
                    return;
                }
                searchSection.setVisibility(View.VISIBLE);
                searchSection.removeAllViews();

                LinearLayout srHdr = new LinearLayout(ctx);
                srHdr.setOrientation(LinearLayout.HORIZONTAL);
                srHdr.setGravity(Gravity.CENTER_VERTICAL);
                TextView srTitle = makeSectionTitle(ctx, d, q.isEmpty() ? src.getName() : ("Results for \"" + q + "\""));
                srHdr.addView(srTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView clearBtn = makePill(ctx, d, "Clear", BG_SURFACE, TEXT_MUTED);
                clearBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchInput.setText("");
                        activeSourceIdx[0] = 0;
                        searchSection.setVisibility(View.GONE);
                    }
                });
                srHdr.addView(clearBtn);
                searchSection.addView(srHdr);

                final TextView loadingTv = new TextView(ctx);
                loadingTv.setText("Searching " + src.getName() + "...");
                loadingTv.setTextColor(TEXT_MUTED);
                loadingTv.setTextSize(13f);
                loadingTv.setPadding(0, dp(d, 6), 0, dp(d, 12));
                searchSection.addView(loadingTv);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final List<BookResult> results = src.search(q);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    searchSection.removeView(loadingTv);
                                    if (results.isEmpty()) {
                                        TextView emptyTv = new TextView(ctx);
                                        emptyTv.setText("No books found. Try another keyword or OPDS feed.");
                                        emptyTv.setTextColor(TEXT_MUTED);
                                        emptyTv.setPadding(0, dp(d, 8), 0, dp(d, 16));
                                        searchSection.addView(emptyTv);
                                        return;
                                    }
                                    for (int i = 0; i < Math.min(20, results.size()); i++) {
                                        final BookResult br = results.get(i);
                                        searchSection.addView(buildSearchRow(ctx, d, mainHandler, br, searchInput, this));
                                    }
                                }
                            });
                        } catch (final Throwable t) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadingTv.setText("Error: " + t.getMessage());
                                }
                            });
                        }
                    }
                }).start();
            }
        };

        // Populate Genre Chips
        String[] genres = new String[]{"Sci-Fi", "Fantasy", "Mystery", "Horror", "Classic", "History", "Philosophy", "Adventure"};
        for (final String g : genres) {
            TextView chip = makePill(ctx, d, g, cBgSurface, cTextPrimary);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.rightMargin = dp(d, 8);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activeSourceIdx[0] = 0;
                    searchInput.setText(g);
                    doSearch.run();
                }
            });
            chipRow.addView(chip, clp);
        }

        // Wire Buttons
        searchGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch.run();
            }
        });
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    doSearch.run();
                    return true;
                }
                return false;
            }
        });
        backupPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupRestoreDialog(ctx);
            }
        });
        opdsPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptCustomOpds(ctx, prefs, sources, activeSourceIdx, opdsPill, doSearch);
            }
        });

        // --- Async Load 1: Local Library Carousel + Open Library Personalized Recommendations ---
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<LocalLibraryReader.LocalBook> localBooks = LocalLibraryReader.loadRecentBooks(ctx, 10);
                if (!localBooks.isEmpty()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            libSection.setVisibility(View.VISIBLE);
        FolioTheme.applyToTree(ctx, libSection);
                            libSection.removeAllViews();
                            libSection.addView(makeSectionTitle(ctx, d, "Continue Reading"));
                            HorizontalScrollView hs = new HorizontalScrollView(ctx);
                            hs.setHorizontalScrollBarEnabled(false);
                            LinearLayout row = new LinearLayout(ctx);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setPadding(0, dp(d, 8), 0, dp(d, 18));
                            for (final LocalLibraryReader.LocalBook lb : localBooks) {
                                row.addView(buildLocalCoverCard(ctx, d, mainHandler, lb));
                            }
                            hs.addView(row);
                            libSection.addView(hs);
                        }
                    });

                    // Seed personalized Open Library recommendations from the top book in Library
                    final LocalLibraryReader.LocalBook seed = localBooks.get(0);
                    final List<BookResult> recs = OpenLibraryClient.fetchRecommendations(seed.title, seed.author, 10);
                    if (!recs.isEmpty()) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                recSection.setVisibility(View.VISIBLE);
        FolioTheme.applyToTree(ctx, recSection);
                                recSection.removeAllViews();
                                String shortSeed = seed.title.length() > 26 ? seed.title.substring(0, 26) + "..." : seed.title;
                                recSection.addView(makeSectionTitle(ctx, d, "Because You Read " + shortSeed));
                                HorizontalScrollView hs = new HorizontalScrollView(ctx);
                                hs.setHorizontalScrollBarEnabled(false);
                                LinearLayout row = new LinearLayout(ctx);
                                row.setOrientation(LinearLayout.HORIZONTAL);
                                row.setPadding(0, dp(d, 8), 0, dp(d, 18));
                                for (BookResult rec : recs) {
                                    row.addView(buildRemoteCoverCard(ctx, d, mainHandler, rec, searchInput, doSearch));
                                }
                                hs.addView(row);
                                recSection.addView(hs);
                            }
                        });
                    }
                }
            }
        }).start();

        // --- Async Load 2: Popular Free EPUBs Carousel (Gutendex) ---
        final LinearLayout offlineCard = new LinearLayout(ctx);
        offlineCard.setOrientation(LinearLayout.VERTICAL);
        offlineCard.setBackground(roundRect(cBgSurface, dp(d, 16), cBorder, dp(d, 1)));
        offlineCard.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 16));
        LinearLayout.LayoutParams ocLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ocLp.topMargin = dp(d, 8);
        ocLp.bottomMargin = dp(d, 16);
        offlineCard.setLayoutParams(ocLp);
        offlineCard.setVisibility(View.GONE);

        TextView offTitle = new TextView(ctx);
        offTitle.setText("Offline Mode");
        offTitle.setTextColor(cTextPrimary);
        offTitle.setTextSize(18f);
        offTitle.setTypeface(FolioTheme.serif(ctx));
        offlineCard.addView(offTitle);

        TextView offMsg = new TextView(ctx);
        offMsg.setText("Connect to Wi-Fi to browse Project Gutenberg & OPDS catalogs, or continue reading from your local library.");
        offMsg.setTextColor(cTextMuted);
        offMsg.setTextSize(12.5f);
        offMsg.setPadding(0, dp(d, 4), 0, dp(d, 14));
        offlineCard.addView(offMsg);

        LinearLayout offBtns = new LinearLayout(ctx);
        offBtns.setOrientation(LinearLayout.HORIZONTAL);
        TextView retryPill = makePill(ctx, d, "Retry", cAccentFill, cAccentSoftText);
        retryPill.setBackground(roundRect(cAccentFill, dp(d, 999), cAccentBorder, dp(d, 1)));
        offBtns.addView(retryPill);

        View offGap = new View(ctx);
        offBtns.addView(offGap, new LinearLayout.LayoutParams(dp(d, 8), 1));

        TextView localCatPill = makePill(ctx, d, "Local Catalog", cBgCard, cTextPrimary);
        localCatPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(ctx, "All", null);
            }
        });
        offBtns.addView(localCatPill);
        offlineCard.addView(offBtns);
        popSection.addView(offlineCard);

        final Runnable[] loadPopularRef = new Runnable[1];
        loadPopularRef[0] = new Runnable() {
            @Override
            public void run() {
                offlineCard.setVisibility(View.GONE);
                popStatus.setVisibility(View.VISIBLE);
                popStatus.setText("Loading Project Gutenberg catalog...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final List<BookResult> popular = sources.get(0).search("");
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    popStatus.setVisibility(View.GONE);
                                    offlineCard.setVisibility(View.GONE);
                                    popCarousel.removeAllViews();
                                    popCarousel.setPadding(0, dp(d, 8), 0, dp(d, 18));
                                    for (int i = 0; i < Math.min(14, popular.size()); i++) {
                                        popCarousel.addView(buildRemoteCoverCard(ctx, d, mainHandler, popular.get(i), searchInput, doSearch));
                                    }
                                    FolioTheme.applyToTree(ctx, popCarousel);
                                }
                            });
                        } catch (final Throwable t) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    popStatus.setVisibility(View.GONE);
                                    offlineCard.setVisibility(View.VISIBLE);
                                    FolioTheme.applyToTree(ctx, offlineCard);
                                }
                            });
                        }
                    }
                }).start();
            }
        };
        retryPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPopularRef[0].run();
            }
        });
        loadPopularRef[0].run();

        FolioTheme.applyToTree(ctx, mainScroll);
        FolioTheme.setActiveBottomTab(ctx, 0);
        FolioTheme.applyWindowBars(ctx);
        return mainScroll;
    }

    private static String formatHomeAuthor(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        int semi = s.indexOf(';');
        if (semi > 0) s = s.substring(0, semi).trim();
        int colon = s.indexOf(':');
        if (colon > 0 && s.length() - colon > 2 && s.substring(0, colon).toLowerCase(Locale.US).contains("by")) {
            s = s.substring(colon + 1).trim();
        }
        int comma = s.indexOf(',');
        if (comma > 0 && comma == s.lastIndexOf(',')) {
            String last = s.substring(0, comma).trim();
            String first = s.substring(comma + 1).trim();
            if (!first.isEmpty() && !last.isEmpty() && !first.matches(".*[0-9].*")) {
                s = first + " " + last;
            }
        }
        return s;
    }

    private static View buildLocalCoverCard(final Context ctx, float d, final Handler mainHandler, final LocalLibraryReader.LocalBook lb) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(d, 114), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(d, 12);
        card.setLayoutParams(lp);

        FrameLayout coverFrame = new FrameLayout(ctx);
        coverFrame.setBackground(roundRect(FolioTheme.bgControl(ctx), dp(d, 8), FolioTheme.border(ctx), dp(d, 1)));
        coverFrame.setClipToOutline(true);
        LinearLayout.LayoutParams cfLp = new LinearLayout.LayoutParams(dp(d, 114), dp(d, 168));
        cfLp.bottomMargin = dp(d, 6);

        final ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverFrame.addView(iv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final LinearLayout fallback = new LinearLayout(ctx);
        fallback.setOrientation(LinearLayout.VERTICAL);
        fallback.setGravity(Gravity.CENTER);
        fallback.setPadding(dp(d, 10), dp(d, 12), dp(d, 10), dp(d, 12));

        String initial = "A";
        if (lb.title != null) {
            String tr = lb.title.trim();
            for (int ci = 0; ci < tr.length(); ci++) {
                char ch = tr.charAt(ci);
                if (Character.isLetterOrDigit(ch)) {
                    initial = String.valueOf(Character.toUpperCase(ch));
                    break;
                }
            }
        }
        TextView dropCap = new TextView(ctx);
        dropCap.setText(initial);
        dropCap.setTextColor(FolioTheme.textMuted(ctx));
        dropCap.setTextSize(34f);
        dropCap.setTypeface(FolioTheme.serif(ctx));
        dropCap.setGravity(Gravity.CENTER);
        fallback.addView(dropCap);

        View spineRule = new View(ctx);
        spineRule.setBackground(roundRect(FolioTheme.accentBorder(ctx), dp(d, 2), 0, 0));
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(dp(d, 26), dp(d, 2));
        srLp.topMargin = dp(d, 4);
        srLp.bottomMargin = dp(d, 10);
        fallback.addView(spineRule, srLp);

        TextView fbTitle = new TextView(ctx);
        fbTitle.setText(lb.title);
        fbTitle.setTextColor(FolioTheme.textSecondary(ctx));
        fbTitle.setTextSize(11.5f);
        fbTitle.setTypeface(FolioTheme.serif(ctx));
        fbTitle.setGravity(Gravity.CENTER);
        fbTitle.setMaxLines(3);
        fbTitle.setEllipsize(TextUtils.TruncateAt.END);
        fallback.addView(fbTitle);

        coverFrame.addView(fallback, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        card.addView(coverFrame, cfLp);

        TextView titleTv = new TextView(ctx);
        titleTv.setText(lb.title);
        titleTv.setTextColor(FolioTheme.textPrimary(ctx));
        titleTv.setTextSize(12.5f);
        titleTv.setTypeface(null, Typeface.BOLD);
        titleTv.setMaxLines(2);
        titleTv.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(titleTv);

        String cleanAuth = formatHomeAuthor(lb.author);
        if (!cleanAuth.isEmpty()) {
            TextView authTv = new TextView(ctx);
            authTv.setText(cleanAuth);
            authTv.setTextColor(FolioTheme.textMuted(ctx));
            authTv.setTextSize(11f);
            authTv.setMaxLines(1);
            authTv.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(authTv);
        }

        IMG_POOL.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap dec = LocalLibraryReader.decodeCover(lb.coverPath);
                if (dec == null && lb.filePath != null && !lb.filePath.isEmpty()) {
                    File alt = new File(lb.filePath + ".cover.jpg");
                    if (alt.exists() && alt.length() > 64) {
                        dec = LocalLibraryReader.decodeCover(alt.getAbsolutePath());
                    }
                }
                final Bitmap bmp = dec;
                if (bmp != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(bmp);
                            fallback.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetPath = lb.filePath;
                if (targetPath == null || targetPath.isEmpty() || !new File(targetPath).exists()) {
                    targetPath = LocalLibraryReader.resolveLocalFile(targetPath, lb.title);
                }
                if (targetPath != null && !targetPath.isEmpty() && new File(targetPath).exists()) {
                    Intent openIntent = new Intent(Intent.ACTION_VIEW);
                    openIntent.setClassName(ctx.getPackageName(), "com.amazon.kindle.ThirdPartyUpgradePage");
                    String mime = "application/epub+zip";
                    if (targetPath.toLowerCase(Locale.US).endsWith(".pdf")) {
                        mime = "application/pdf";
                    } else if (targetPath.toLowerCase(Locale.US).endsWith(".kfx")) {
                        mime = "application/x-kfx";
                    }
                    openIntent.setDataAndType(Uri.fromFile(new File(targetPath)), mime);
                    openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(openIntent);
                } else {
                    Intent readerIntent = new Intent();
                    readerIntent.setClassName(ctx.getPackageName(), "com.amazon.kcp.reader.StandAloneBookReaderActivity");
                    readerIntent.putExtra("book_id", lb.id);
                    readerIntent.putExtra("asin", lb.asin);
                    readerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        ctx.startActivity(readerIntent);
                    } catch (Throwable t) {
                        android.util.Log.e("OpenCatalogView", "Direct reader launch failed for " + lb.title, t);
                    }
                }
            }
        });
        return card;
    }

    private static View buildRemoteCoverCard(
            final Context ctx, float d, final Handler mainHandler, final BookResult book,
            final EditText searchInput, final Runnable doSearch
    ) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(d, 114), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(d, 12);
        card.setLayoutParams(lp);

        FrameLayout coverFrame = new FrameLayout(ctx);
        coverFrame.setBackground(roundRect(FolioTheme.bgControl(ctx), dp(d, 8), FolioTheme.border(ctx), dp(d, 1)));
        coverFrame.setClipToOutline(true);
        LinearLayout.LayoutParams cfLp = new LinearLayout.LayoutParams(dp(d, 114), dp(d, 168));
        cfLp.bottomMargin = dp(d, 6);

        final ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverFrame.addView(iv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final TextView fallback = new TextView(ctx);
        fallback.setText(book.title);
        fallback.setTextColor(FolioTheme.textPrimary(ctx));
        fallback.setTextSize(12f);
        fallback.setGravity(Gravity.CENTER);
        fallback.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));
        coverFrame.addView(fallback);
        card.addView(coverFrame, cfLp);

        TextView titleTv = new TextView(ctx);
        titleTv.setText(book.title);
        titleTv.setTextColor(FolioTheme.textPrimary(ctx));
        titleTv.setTextSize(12.5f);
        titleTv.setTypeface(null, Typeface.BOLD);
        titleTv.setMaxLines(2);
        titleTv.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(titleTv);

        TextView authTv = new TextView(ctx);
        authTv.setText(book.author);
        authTv.setTextColor(FolioTheme.textMuted(ctx));
        authTv.setTextSize(11f);
        authTv.setMaxLines(1);
        authTv.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(authTv);

        loadRemoteCoverAsync(book, iv, fallback, mainHandler);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookDetails(ctx, mainHandler, book, searchInput, doSearch);
            }
        });
        return card;
    }

    private static View buildSearchRow(
            final Context ctx, float d, final Handler mainHandler, final BookResult book,
            final EditText searchInput, final Runnable doSearch
    ) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(roundRect(BG_SURFACE, dp(d, 10), 0xFF25282D, dp(d, 1)));
        row.setPadding(dp(d, 10), dp(d, 10), dp(d, 12), dp(d, 10));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.bottomMargin = dp(d, 10);
        row.setLayoutParams(rlp);

        FrameLayout thumbBox = new FrameLayout(ctx);
        thumbBox.setBackground(roundRect(BG_CARD, dp(d, 6), 0, 0));
        final ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbBox.addView(iv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final TextView fb = new TextView(ctx);
        fb.setText("EPUB");
        fb.setTextColor(TEXT_MUTED);
        fb.setTextSize(10f);
        fb.setGravity(Gravity.CENTER);
        thumbBox.addView(fb);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(d, 52), dp(d, 76));
        tlp.rightMargin = dp(d, 12);
        row.addView(thumbBox, tlp);

        LinearLayout info = new LinearLayout(ctx);
        info.setOrientation(LinearLayout.VERTICAL);
        TextView tvTitle = new TextView(ctx);
        tvTitle.setText(book.title);
        tvTitle.setTextColor(TEXT_PRIMARY);
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setMaxLines(2);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(tvTitle);

        TextView tvMeta = new TextView(ctx);
        String metaLine = book.author + (book.downloadCount > 0 ? (" • " + book.downloadCount + " downloads") : (" • " + book.sourceName));
        tvMeta.setText(metaLine);
        tvMeta.setTextColor(TEXT_MUTED);
        tvMeta.setTextSize(12.5f);
        tvMeta.setPadding(0, dp(d, 3), 0, 0);
        info.addView(tvMeta);
        row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        loadRemoteCoverAsync(book, iv, fb, mainHandler);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookDetails(ctx, mainHandler, book, searchInput, doSearch);
            }
        });
        return row;
    }

    private static void loadRemoteCoverAsync(final BookResult book, final ImageView iv, final View fallback, final Handler mainHandler) {
        IMG_POOL.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = null;
                if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                    bmp = DiskCache.getCoverBitmap(book.coverUrl);
                    if (bmp == null) {
                        try {
                            byte[] raw = SafeHttp.getBytes(book.coverUrl);
                            bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length);
                            if (bmp != null) DiskCache.putCoverBytes(book.coverUrl, raw, bmp);
                        } catch (Throwable ignored) {}
                    }
                }
                if (bmp == null) {
                    try {
                        OpenLibraryClient.BookMeta m = OpenLibraryClient.fetchMeta(book.title, book.author);
                        if (m.coverUrl != null && !m.coverUrl.isEmpty()) {
                            bmp = DiskCache.getCoverBitmap(m.coverUrl);
                            if (bmp == null) {
                                byte[] raw = SafeHttp.getBytes(m.coverUrl);
                                bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length);
                                if (bmp != null) DiskCache.putCoverBytes(m.coverUrl, raw, bmp);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
                if (bmp != null) {
                    final Bitmap finalBmp = bmp;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(finalBmp);
                            if (fallback != null) fallback.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private static TextView makeSectionTitle(Context ctx, float d, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(FolioTheme.textPrimary(ctx));
        tv.setTextSize(20f);
        tv.setTypeface(FolioTheme.serif(ctx));
        tv.setLetterSpacing(-0.015f);
        tv.setPadding(0, dp(d, 6), 0, dp(d, 4));
        return tv;
    }

    private static TextView makePill(Context ctx, float d, String text, int bgColor, int textColor) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(12.5f);
        tv.setTypeface(FolioTheme.sansBold(ctx));
        tv.setBackground(roundRect(bgColor, dp(d, 999), FolioTheme.border(ctx), dp(d, 1)));
        tv.setPadding(dp(d, 14), dp(d, 7), dp(d, 14), dp(d, 7));
        return tv;
    }

    private static GradientDrawable roundRect(int fillColor, int radiusPx, int strokeColor, int strokeWidth) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        gd.setCornerRadius(radiusPx);
        if (strokeWidth > 0) gd.setStroke(strokeWidth, strokeColor);
        return gd;
    }

    private static int dp(float density, int value) {
        return (int) (value * density + 0.5f);
    }

    private static void promptCustomOpds(
            final Context ctx,
            final SharedPreferences prefs,
            final List<BookSource> sources,
            final int[] activeIdx,
            final TextView opdsBtn,
            final Runnable doSearch
    ) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 24, 40, 12);

        TextView presetLabel = new TextView(ctx);
        presetLabel.setText("Quick OPDS Catalogs:");
        presetLabel.setTextSize(14f);
        presetLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(presetLabel);

        final EditText input = new EditText(ctx);
        input.setHint("https://example.org/opds.xml");
        input.setText(prefs.getString(KEY_CUSTOM_OPDS, ""));

        final String[][] presets = new String[][]{
            {"Standard Ebooks", "https://standardebooks.org/feeds/atom/new-releases"},
            {"Project Gutenberg OPDS", "https://m.gutenberg.org/ebooks.opds/"},
            {"Internet Archive OPDS", "https://bookserver.archive.org/catalog/"}
        };
        for (final String[] p : presets) {
            Button b = new Button(ctx);
            b.setText(p[0]);
            b.setAllCaps(false);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    input.setText(p[1]);
                }
            });
            layout.addView(b);
        }

        TextView customLabel = new TextView(ctx);
        customLabel.setText("\nOr enter any OPDS 1.2 feed URL:");
        customLabel.setTextSize(13f);
        layout.addView(customLabel);
        layout.addView(input);

        AlertDialog opdsDlg = new AlertDialog.Builder(ctx)
                .setTitle("OPDS Catalog Feed")
                .setView(layout)
                .setPositiveButton("Save & Browse", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String url = input.getText().toString().trim();
                        if (!url.isEmpty()) {
                            prefs.edit().putString(KEY_CUSTOM_OPDS, url).apply();
                            OpdsSource opds = new OpdsSource("OPDS Feed", url, "");
                            if (sources.size() > 1) {
                                sources.set(1, opds);
                            } else {
                                sources.add(opds);
                            }
                            opdsBtn.setText("OPDS");
                            activeIdx[0] = 1;
                            doSearch.run();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, opdsDlg);
    }

    private static String formatMetaMessage(BookResult book, OpenLibraryClient.BookMeta meta, boolean isOpenLibRec) {
        StringBuilder msg = new StringBuilder();
        msg.append("Author: ").append(book.author).append("\n");
        if (meta.firstPublishYear > 0) {
            msg.append("First Published: ").append(meta.firstPublishYear).append("\n");
        }
        if (meta.ratingsCount > 0) {
            msg.append(String.format(Locale.US, "Open Library Rating: %.2f / 5 (%d ratings)\n", meta.avgRating, meta.ratingsCount));
        }
        if (!meta.subjects.isEmpty()) {
            StringBuilder sbSub = new StringBuilder();
            for (int i = 0; i < Math.min(4, meta.subjects.size()); i++) {
                if (i > 0) sbSub.append(" • ");
                sbSub.append(meta.subjects.get(i));
            }
            msg.append("Topics: ").append(sbSub.toString()).append("\n");
        }
        if (isOpenLibRec) {
            msg.append("Availability: Book info via Open Library (tap 'Search Catalog' to check OPDS / Gutenberg)\n");
        } else {
            msg.append("Source: ").append(book.sourceName).append("\n");
        }
        if (!meta.description.isEmpty()) {
            String desc = meta.description.length() > 520 ? meta.description.substring(0, 520) + "..." : meta.description;
            msg.append("\n").append(desc).append("\n");
        }
        return msg.toString();
    }

    private static void populateSimilarContainer(final Context ctx, final LinearLayout container, final List<String> similarTitles, final EditText searchInput, final Runnable doSearch, final AlertDialog dialog) {
        if (container == null) return;
        container.removeAllViews();
        if (similarTitles == null || similarTitles.isEmpty()) return;
        TextView simHdr = new TextView(ctx);
        simHdr.setText("\nSimilar Books (tap to search):");
        simHdr.setTextSize(14f);
        simHdr.setTypeface(null, Typeface.BOLD);
        container.addView(simHdr);
        for (int i = 0; i < Math.min(4, similarTitles.size()); i++) {
            final String simTitle = similarTitles.get(i);
            Button simBtn = new Button(ctx);
            simBtn.setText(simTitle);
            simBtn.setAllCaps(false);
            simBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (searchInput != null && doSearch != null) {
                        searchInput.setText(simTitle);
                        doSearch.run();
                    }
                }
            });
            container.addView(simBtn);
        }
    }

    private static void showBookDetails(final Context ctx, final Handler mainHandler, final BookResult book, final EditText searchInput, final Runnable doSearch) {
        ScrollView scroll = new ScrollView(ctx);
        final LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 32, 48, 16);

        final ImageView coverView = new ImageView(ctx);
        coverView.setVisibility(View.GONE);
        LinearLayout.LayoutParams covLp = new LinearLayout.LayoutParams(260, 390);
        covLp.gravity = Gravity.CENTER_HORIZONTAL;
        covLp.bottomMargin = 24;
        box.addView(coverView, covLp);

        final boolean isOpenLibRec = (book.epubUrl == null || book.epubUrl.isEmpty());
        final OpenLibraryClient.BookMeta quickMeta = OpenLibraryClient.getQuickMeta(book.title, book.author);

        final TextView msgView = new TextView(ctx);
        msgView.setTextSize(15f);
        if (quickMeta != null && (quickMeta.firstPublishYear > 0 || !quickMeta.description.isEmpty())) {
            msgView.setText(formatMetaMessage(book, quickMeta, isOpenLibRec));
        } else {
            msgView.setText("Author: " + book.author + "\nSource: " + book.sourceName + "\n\nFetching Open Library ratings, synopsis & similar books...");
        }
        box.addView(msgView);

        final LinearLayout simContainer = new LinearLayout(ctx);
        simContainer.setOrientation(LinearLayout.VERTICAL);
        box.addView(simContainer);

        if (quickMeta != null && !quickMeta.similarTitles.isEmpty()) {
            populateSimilarContainer(ctx, simContainer, quickMeta.similarTitles, searchInput, doSearch, null);
        }
        scroll.addView(box);

        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(book.title)
                .setView(scroll)
                .setPositiveButton(isOpenLibRec ? "Search Catalog" : "Download & Read", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (isOpenLibRec) {
                            if (searchInput != null && doSearch != null) {
                                searchInput.setText(book.title);
                                doSearch.run();
                            }
                        } else {
                            downloadAndOpen(ctx, mainHandler, book);
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .create();
        dialog.show();
        FolioTheme.applyToDialog(ctx, dialog);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final OpenLibraryClient.BookMeta meta = OpenLibraryClient.fetchMeta(book.title, book.author);

                // 1. Update text metadata immediately so ratings, year, topics & synopsis appear without delay!
                final String msg = formatMetaMessage(book, meta, isOpenLibRec);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog.isShowing()) {
                            msgView.setText(msg);
                            if (!meta.similarTitles.isEmpty()) {
                                populateSimilarContainer(ctx, simContainer, meta.similarTitles, searchInput, doSearch, dialog);
                            }
                        }
                    }
                });

                // 2. Fetch cover image asynchronously so slow networks don't block text metadata
                Bitmap bmp = null;
                if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                    try {
                        InputStream is = SafeHttp.openStream(book.coverUrl);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Throwable ignored) {}
                }
                if (bmp == null && meta.coverUrl != null && !meta.coverUrl.isEmpty()) {
                    try {
                        InputStream is = SafeHttp.openStream(meta.coverUrl);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Throwable ignored) {}
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
            }
        }).start();
    }

    public static void showBackupRestoreDialog(final Context ctx) {
        AlertDialog bakDlg = new AlertDialog.Builder(ctx)
                .setTitle("Azath Library & Notes Backup")
                .setMessage("Back up or restore your local Azath library metadata, reading progress, and highlights/notes (/sdcard/kindle/backups/).")
                .setPositiveButton("Backup Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        backupLocalDatabases(ctx);
                    }
                })
                .setNeutralButton("Restore Latest", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        restoreLatestDatabases(ctx);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.applyToDialog(ctx, bakDlg);
    }

    private static void restoreLatestDatabases(Context ctx) {
        try {
            File backupDir = new File("/sdcard/kindle/backups");
            File[] files = backupDir.listFiles();
            if (files == null || files.length == 0) {
                Toast.makeText(ctx, "No backups found in /sdcard/kindle/backups/", Toast.LENGTH_LONG).show();
                return;
            }
            File dbDir = new File(ctx.getApplicationInfo().dataDir, "databases");
            int restored = 0;
            for (String dbName : new String[]{"kindle_library.db", "annotations.db"}) {
                File latest = null;
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith("_" + dbName)) {
                        if (latest == null || f.getName().compareTo(latest.getName()) > 0) {
                            latest = f;
                        }
                    }
                }
                if (latest != null) {
                    copyFile(latest, new File(dbDir, dbName));
                    restored++;
                }
            }
            int restoredMeta = com.amazon.kindle.epub.ingest.FolioMetadataBackup.restoreFromJson(ctx);
            String msg = "Restored " + restored + " DB(s)";
            if (restoredMeta >= 0) {
                msg += " & " + restoredMeta + " metadata record(s)";
            }
            msg += ". Restart Azath to apply.";
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            Toast.makeText(ctx, "Restore error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            int metaCount = com.amazon.kindle.epub.ingest.FolioMetadataBackup.exportToJson(ctx);
            String msg = "Backed up " + copied + " DB(s)";
            if (metaCount >= 0) {
                msg += " & " + metaCount + " metadata record(s)";
            }
            msg += " to /sdcard/kindle/backups/";
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
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

                    List<String> candidateUrls = new ArrayList<String>();
                    if (book.epubUrl != null && !book.epubUrl.isEmpty()) {
                        candidateUrls.add(book.epubUrl);
                    }
                    if (book.id != null && !book.id.isEmpty() && !book.id.startsWith("opds") && !book.id.startsWith("ol_")) {
                        candidateUrls.add("https://www.gutenberg.org/cache/epub/" + book.id + "/pg" + book.id + "-images.epub");
                        candidateUrls.add("http://aleph.gutenberg.org/cache/epub/" + book.id + "/pg" + book.id + "-images.epub");
                        candidateUrls.add("http://aleph.gutenberg.org/cache/epub/" + book.id + "/pg" + book.id + ".epub");
                    }

                    Throwable lastErr = null;
                    boolean downloaded = false;
                    File tmpFile = new File(outDir, epubFile.getName() + ".part");
                    for (String candUrl : candidateUrls) {
                        InputStream is = null;
                        FileOutputStream fos = null;
                        try {
                            is = SafeHttp.openStream(candUrl);
                            fos = new FileOutputStream(tmpFile);
                            byte[] buf = new byte[8192];
                            int n;
                            long total = 0;
                            byte[] hdr = new byte[4];
                            int hdrLen = 0;
                            while ((n = is.read(buf)) > 0) {
                                if (hdrLen < 4) {
                                    int take = Math.min(4 - hdrLen, n);
                                    System.arraycopy(buf, 0, hdr, hdrLen, take);
                                    hdrLen += take;
                                }
                                fos.write(buf, 0, n);
                                total += n;
                            }
                            fos.flush();
                            fos.close();
                            fos = null;
                            is.close();
                            is = null;
                            boolean validZip = (hdrLen == 4 && hdr[0] == 0x50 && hdr[1] == 0x4B && hdr[2] == 0x03 && hdr[3] == 0x04 && total >= 1024);
                            if (validZip) {
                                if (epubFile.exists()) epubFile.delete();
                                if (!tmpFile.renameTo(epubFile)) {
                                    copyFile(tmpFile, epubFile);
                                    tmpFile.delete();
                                }
                                downloaded = true;
                                break;
                            } else {
                                tmpFile.delete();
                                lastErr = new java.io.IOException("Invalid EPUB response (" + total + " bytes) from " + candUrl);
                            }
                        } catch (Throwable t) {
                            lastErr = t;
                            try { if (fos != null) fos.close(); } catch (Throwable ignored) {}
                            try { if (is != null) is.close(); } catch (Throwable ignored) {}
                            tmpFile.delete();
                        }
                    }
                    if (!downloaded) {
                        throw (lastErr != null) ? lastErr : new java.io.IOException("All download mirrors failed");
                    }

                    final File kfxFile = new File(outDir, safeName + "_" + book.id + ".kfx");
                    boolean kfxOk = com.amazon.kindle.epub.EpubTranscoder.transcodeToKfx(epubFile, kfxFile);
                    if (kfxOk) {
                        registerDownloadedBook(ctx, book.title, book.author, kfxFile);
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                android.os.StrictMode.class.getMethod("disableDeathOnFileUriExposure").invoke(null);
                            } catch (Throwable ignored) {}
                            Toast.makeText(ctx, "Opening in Azath...", Toast.LENGTH_SHORT).show();
                            Intent openIntent = new Intent(Intent.ACTION_VIEW);
                            openIntent.setClassName(ctx.getPackageName(), "com.amazon.kindle.ThirdPartyUpgradePage");
                            File target = kfxFile.exists() ? kfxFile : epubFile;
                            String mime = kfxFile.exists() ? "application/x-kfx" : "application/epub+zip";
                            openIntent.setDataAndType(Uri.fromFile(target), mime);
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

    public static void registerDownloadedBook(Context ctx, String title, String author, File kfxFile) {
        try {
            File dbFile = new File(ctx.getApplicationInfo().dataDir, "databases/kindle_library.db");
            if (!dbFile.exists()) return;
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            String asin = "CR!" + Math.abs(title.hashCode()) + "KFX";
            String key = "AMZNID0/" + asin + "/0/";
            long now = System.currentTimeMillis();
            long size = kfxFile.exists() ? kfxFile.length() : 1048576;

            db.beginTransaction();
            try {
                db.execSQL(
                    "INSERT OR REPLACE INTO KindleContent (" +
                    "ID, STATE, ARCHIVABLE, HAS_READ_ALONG, TYPE, KEPT, CONTENT_TYPE, TITLE, AUTHOR, HAS_XRAY, DICTIONARY_TYPE, TITLE_ORDER, AUTHOR_ORDER, AUTHOR_ORDER_REVERSED, READ_STATE, READ_STATE_ORIGIN, HAS_LOADED_COVER, IS_HIDDEN, IS_HIDDEN_BY_USER" +
                    ") VALUES (?, 'LOCAL', 0, 0, 'BT_EBOOK', 0, 'application/x-kfx-ebook', ?, ?, 0, 'NONE', -1, -1, -1, 'UNREAD', 'OTHER', 1, 0, 0)",
                    new Object[]{key, title, author}
                );
                db.execSQL(
                    "INSERT OR REPLACE INTO LocalContent (" +
                    "KEY, FILE_PATH, GUID, LAST_MODIFIED, OWNERSHIP_TYPE, FILE_SIZE" +
                    ") VALUES (?, ?, ?, ?, 'Unknown', ?)",
                    new Object[]{key, kfxFile.getAbsolutePath(), asin, now, size}
                );
                db.execSQL(
                    "INSERT OR REPLACE INTO UserContent (" +
                    "KEY, USER_ID, LAST_ACCESSED, LPR, MRPR, FPR, MLT_LASTUPDATED, READING_PROGRESS, IS_IN_CAROUSEL, CLIPPING_BALANCE, HAS_UNMIGRATED_ANNOTATION" +
                    ") VALUES (?, 'MASTER', ?, 0, 0, 0, -1, 0, 1, 0, 0)",
                    new Object[]{key, now}
                );
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
        } catch (Throwable t) {
            android.util.Log.w("OpenCatalogView", "registerDownloadedBook failed: " + t.getMessage());
        }
    }
}
