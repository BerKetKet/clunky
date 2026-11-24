package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.kindle.epub.catalog.FolioIcons;
import com.amazon.kindle.epub.catalog.FolioTheme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Kindle-native simplified WebView catalog browser with configurable URL,
 * monochrome hairline vector controls (FolioIcons), Quick Bookmarks, 1-tap EPUB
 * page scanner, and automatic EPUB -> KFX -> Folio Library ingestion.
 */
public final class FolioWebBrowserDialog {
    private static final String TAG = "FolioWebBrowser";
    private static final String PREFS = "folio_ui_prefs";
    private static final String KEY_HOME_URL = "web_browser_home_url";
    private static final String KEY_CLEAN_MODE = "web_browser_clean_mode";
    private static final String KEY_BOOKMARKS = "web_browser_bookmarks";
    public static final String DEFAULT_URL = "https://standardebooks.org/ebooks";

    private static final int BG_CANVAS = 0xFF0D0F12;
    private static final int BG_SURFACE = 0xFF141618;
    private static final int BG_CONTROL = 0xFF1C1F22;
    private static final int BORDER = 0xFF262A2E;
    private static final int TEXT_PRIMARY = 0xFFECEFF1;
    private static final int TEXT_MUTED = 0xFF8E959C;
    private static final int ACCENT_BLUE = 0xFF4DA6FF;

    private FolioWebBrowserDialog() {}

    public static String getHomeUrl(Context ctx) {
        try {
            String u = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY_HOME_URL, DEFAULT_URL);
            if (u != null && !u.trim().isEmpty()) return normalizeUrl(u.trim());
        } catch (Throwable ignored) {}
        return DEFAULT_URL;
    }

    public static void setHomeUrl(Context ctx, String url) {
        try {
            String clean = normalizeUrl(url != null ? url.trim() : "");
            if (clean.isEmpty()) clean = DEFAULT_URL;
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_HOME_URL, clean).apply();
        } catch (Throwable ignored) {}
    }

    public static boolean isCleanMode(Context ctx) {
        try {
            return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getBoolean(KEY_CLEAN_MODE, true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setCleanMode(Context ctx, boolean enabled) {
        try {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_CLEAN_MODE, enabled).apply();
        } catch (Throwable ignored) {}
    }

    public static String getHomeHostLabel(Context ctx) {
        try {
            Uri u = Uri.parse(getHomeUrl(ctx));
            String h = u.getHost();
            if (h != null && !h.isEmpty()) return h;
        } catch (Throwable ignored) {}
        return "standardebooks.org";
    }

    private static String normalizeUrl(String raw) {
        if (raw == null) return DEFAULT_URL;
        String s = raw.trim();
        if (s.isEmpty()) return DEFAULT_URL;
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            s = "https://" + s;
        }
        return s;
    }

    private static List<String[]> loadBookmarks(Context ctx) {
        List<String[]> out = new ArrayList<>();
        try {
            String raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_BOOKMARKS, null);
            if (raw == null) {
                out.add(new String[] { "Standard Ebooks", "https://standardebooks.org/ebooks" });
                out.add(new String[] { "Gutenberg", "https://www.gutenberg.org/ebooks/" });
                return out;
            }
            for (String line : raw.split("\n")) {
                int sep = line.indexOf('|');
                if (sep > 0 && sep + 1 < line.length()) {
                    out.add(new String[] { line.substring(0, sep).trim(), line.substring(sep + 1).trim() });
                }
            }
        } catch (Throwable ignored) {}
        return out;
    }

    private static void saveBookmarks(Context ctx, List<String[]> list) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append('\n');
                sb.append(list.get(i)[0].replace("|", " ").replace("\n", " "))
                        .append('|')
                        .append(list.get(i)[1].replace("\n", ""));
            }
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_BOOKMARKS, sb.toString()).apply();
        } catch (Throwable ignored) {}
    }

    public static void showUrlSettingsDialog(final Context ctx, final Runnable onSaved) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG_SURFACE);
        root.setPadding(dp(d, 20), dp(d, 18), dp(d, 20), dp(d, 12));

        TextView hdr = new TextView(ctx);
        hdr.setText("Web Browser Settings");
        hdr.setTextColor(TEXT_PRIMARY);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hdr.setTypeface(Typeface.SERIF, Typeface.BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 4));
        root.addView(hdr);

        TextView sub = new TextView(ctx);
        sub.setText("Configure your default catalog homepage URL and reading layout.");
        sub.setTextColor(TEXT_MUTED);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sub.setPadding(0, 0, 0, dp(d, 14));
        root.addView(sub);

        TextView lbl = new TextView(ctx);
        lbl.setText("HOMEPAGE URL");
        lbl.setTextColor(TEXT_MUTED);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lbl.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        lbl.setLetterSpacing(0.08f);
        lbl.setPadding(0, 0, 0, dp(d, 6));
        root.addView(lbl);

        final EditText urlEt = new EditText(ctx);
        urlEt.setText(getHomeUrl(ctx));
        urlEt.setHint("https://standardebooks.org/ebooks");
        urlEt.setHintTextColor(0xFF606870);
        urlEt.setTextColor(TEXT_PRIMARY);
        urlEt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        urlEt.setSingleLine(true);
        urlEt.setPadding(dp(d, 12), dp(d, 9), dp(d, 12), dp(d, 9));
        GradientDrawable uBg = new GradientDrawable();
        uBg.setColor(BG_CANVAS);
        uBg.setCornerRadius(dp(d, 8));
        uBg.setStroke(dp(d, 1), BORDER);
        urlEt.setBackground(uBg);
        LinearLayout.LayoutParams uLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uLp.bottomMargin = dp(d, 10);
        urlEt.setLayoutParams(uLp);
        root.addView(urlEt);

        HorizontalScrollView pHsv = new HorizontalScrollView(ctx);
        pHsv.setHorizontalScrollBarEnabled(false);
        LinearLayout pRow = new LinearLayout(ctx);
        pRow.setOrientation(LinearLayout.HORIZONTAL);
        pRow.setPadding(0, 0, 0, dp(d, 12));
        final String[][] presets = new String[][] {
                { "Standard Ebooks", "https://standardebooks.org/ebooks" },
                { "Project Gutenberg", "https://www.gutenberg.org/ebooks/" },
                { "Open Library", "https://openlibrary.org/" },
                { "Calibre LAN", "http://192.168.1.100:8080" }
        };
        for (final String[] pr : presets) {
            TextView chip = new TextView(ctx);
            chip.setText(pr[0]);
            chip.setTextColor(TEXT_PRIMARY);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            chip.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(BG_CONTROL);
            cBg.setCornerRadius(dp(d, 8));
            cBg.setStroke(dp(d, 1), BORDER);
            chip.setBackground(cBg);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cLp.rightMargin = dp(d, 6);
            chip.setLayoutParams(cLp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    urlEt.setText(pr[1]);
                    urlEt.setSelection(pr[1].length());
                }
            });
            pRow.addView(chip);
        }
        pHsv.addView(pRow);
        root.addView(pHsv);

        final boolean[] cleanState = new boolean[] { isCleanMode(ctx) };
        final TextView cleanToggle = new TextView(ctx);
        final Runnable styleClean = new Runnable() {
            @Override public void run() {
                boolean on = cleanState[0];
                cleanToggle.setText(on
                        ? "Simplified View: On  — hides banners & popups"
                        : "Simplified View: Off  — original page layout");
                cleanToggle.setTextColor(on ? ACCENT_BLUE : TEXT_MUTED);
                cleanToggle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                cleanToggle.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                cleanToggle.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
                GradientDrawable ctBg = new GradientDrawable();
                ctBg.setColor(BG_CONTROL);
                ctBg.setCornerRadius(dp(d, 8));
                ctBg.setStroke(dp(d, 1), on ? 0xFF2C5278 : BORDER);
                cleanToggle.setBackground(ctBg);
            }
        };
        styleClean.run();
        cleanToggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cleanState[0] = !cleanState[0];
                styleClean.run();
            }
        });
        root.addView(cleanToggle);
        FolioTheme.applyToTree(ctx, root);

        AlertDialog sDlg = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        setHomeUrl(ctx, urlEt.getText().toString());
                        setCleanMode(ctx, cleanState[0]);
                        Toast.makeText(ctx, "Saved browser URL: " + getHomeHostLabel(ctx), Toast.LENGTH_SHORT).show();
                        if (onSaved != null) onSaved.run();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        FolioTheme.styleDialog(ctx, sDlg);
    }

    public static void show(final Context ctx, final Runnable onImported) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG_CANVAS);

        // Top Kindle-native monochrome navigation bar
        LinearLayout topBar = new LinearLayout(ctx);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(BG_SURFACE);
        topBar.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));

        final FrameLayout btnBack = makeBarIcon(ctx, d, FolioIcons.BACK, FolioIcons.COLOR_ICON, BG_CONTROL, BORDER);
        final FrameLayout btnReload = makeBarIcon(ctx, d, FolioIcons.REFRESH, FolioIcons.COLOR_ICON, BG_CONTROL, BORDER);
        final FrameLayout btnHome = makeBarIcon(ctx, d, FolioIcons.HOME, FolioIcons.COLOR_ICON, BG_CONTROL, BORDER);

        final TextView urlChip = new TextView(ctx);
        urlChip.setText(getHomeHostLabel(ctx));
        urlChip.setTextColor(TEXT_PRIMARY);
        urlChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        urlChip.setSingleLine(true);
        urlChip.setEllipsize(TextUtils.TruncateAt.END);
        urlChip.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
        GradientDrawable ucBg = new GradientDrawable();
        ucBg.setColor(BG_CANVAS);
        ucBg.setCornerRadius(dp(d, 7));
        ucBg.setStroke(dp(d, 1), BORDER);
        urlChip.setBackground(ucBg);
        LinearLayout.LayoutParams ucLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ucLp.leftMargin = dp(d, 4);
        ucLp.rightMargin = dp(d, 6);
        urlChip.setLayoutParams(ucLp);

        final FrameLayout btnClean = makeBarIcon(ctx, d, FolioIcons.SPARKLE, ACCENT_BLUE, BG_CONTROL, BORDER);
        final FrameLayout btnSettings = makeBarIcon(ctx, d, FolioIcons.SETTINGS, FolioIcons.COLOR_ICON, BG_CONTROL, BORDER);
        final FrameLayout btnClose = makeBarIcon(ctx, d, FolioIcons.CLOSE, FolioIcons.COLOR_MUTED, BG_CONTROL, BORDER);

        topBar.addView(btnBack);
        topBar.addView(btnReload);
        topBar.addView(btnHome);
        topBar.addView(urlChip);
        topBar.addView(btnClean);
        topBar.addView(btnSettings);
        topBar.addView(btnClose);
        root.addView(topBar);

        View barDiv = new View(ctx);
        barDiv.setBackgroundColor(BORDER);
        root.addView(barDiv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Math.round(d * 0.8f))));

        // Restrained Bookmarks & EPUB Scanner Bar
        HorizontalScrollView bmScroll = new HorizontalScrollView(ctx);
        bmScroll.setHorizontalScrollBarEnabled(false);
        bmScroll.setBackgroundColor(BG_SURFACE);
        bmScroll.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));
        final LinearLayout bmRow = new LinearLayout(ctx);
        bmRow.setOrientation(LinearLayout.HORIZONTAL);
        bmRow.setGravity(Gravity.CENTER_VERTICAL);
        bmScroll.addView(bmRow);
        root.addView(bmScroll);

        View bmDiv = new View(ctx);
        bmDiv.setBackgroundColor(BORDER);
        root.addView(bmDiv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Math.round(d * 0.8f))));

        final ProgressBar progBar = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
        progBar.setMax(100);
        progBar.setProgress(0);
        progBar.setVisibility(View.GONE);
        progBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 2)));
        root.addView(progBar);

        // Live download / auto-ingest status banner
        final LinearLayout statusBanner = new LinearLayout(ctx);
        statusBanner.setOrientation(LinearLayout.HORIZONTAL);
        statusBanner.setGravity(Gravity.CENTER_VERTICAL);
        statusBanner.setBackgroundColor(BG_CONTROL);
        statusBanner.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        statusBanner.setVisibility(View.GONE);

        final TextView statusTv = new TextView(ctx);
        statusTv.setTextColor(TEXT_PRIMARY);
        statusTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        statusTv.setSingleLine(true);
        statusTv.setEllipsize(TextUtils.TruncateAt.END);
        statusTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        statusBanner.addView(statusTv);

        final TextView openNowBtn = new TextView(ctx);
        openNowBtn.setText("Open");
        openNowBtn.setTextColor(0xFF000000);
        openNowBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        openNowBtn.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        openNowBtn.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable onBg = new GradientDrawable();
        onBg.setColor(ACCENT_BLUE);
        onBg.setCornerRadius(dp(d, 6));
        openNowBtn.setBackground(onBg);
        openNowBtn.setVisibility(View.GONE);
        statusBanner.addView(openNowBtn);
        root.addView(statusBanner);

        final WebView webView = new WebView(ctx);
        int webH = (int) (ctx.getResources().getDisplayMetrics().heightPixels * 0.72f);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, webH));
        webView.setBackgroundColor(BG_CANVAS);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        try {
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } catch (Throwable ignored) {}

        final AlertDialog[] dlgRef = new AlertDialog[1];

        final Runnable updateCleanPill = new Runnable() {
            @Override public void run() {
                boolean on = isCleanMode(ctx);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(BG_CONTROL);
                bg.setCornerRadius(dp(d, 7));
                bg.setStroke(dp(d, 1), on ? 0xFF2C5278 : BORDER);
                btnClean.setBackground(bg);
                FolioIcons.setButtonIconColor(btnClean, on ? ACCENT_BLUE : FolioIcons.COLOR_MUTED);
            }
        };
        updateCleanPill.run();

        final Runnable injectCleanCss = new Runnable() {
            @Override public void run() {
                if (!isCleanMode(ctx)) return;
                String js = "(function(){try{"
                        + "var id='folio-clean-css';if(document.getElementById(id))return;"
                        + "var s=document.createElement('style');s.id=id;"
                        + "s.innerHTML='body{max-width:100vw!important;overflow-x:hidden!important;}"
                        + "[class*=\"cookie\"],[id*=\"cookie\"],[class*=\"consent\"],[id*=\"consent\"],"
                        + "[class*=\"banner-ad\"],[class*=\"newsletter\"],iframe[src*=\"ads\"]{display:none!important;}';"
                        + "document.head.appendChild(s);}catch(e){}})();";
                try { webView.evaluateJavascript(js, null); } catch (Throwable ignored) {}
            }
        };

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    progBar.setVisibility(View.GONE);
                } else {
                    progBar.setVisibility(View.VISIBLE);
                    progBar.setProgress(newProgress);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request != null && request.getUrl() != null ? request.getUrl().toString() : "";
                if (isLikelyEpubUrl(url)) {
                    downloadAndIngestEpub(ctx, url, view.getSettings().getUserAgentString(),
                            null, "application/epub+zip", mainHandler, statusBanner, statusTv, openNowBtn, dlgRef, onImported);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url != null) {
                    try {
                        Uri u = Uri.parse(url);
                        String host = u.getHost();
                        String path = u.getPath();
                        urlChip.setText((host != null ? host : url) + (path != null && path.length() > 1 ? path : ""));
                    } catch (Throwable ignored) {
                        urlChip.setText(url);
                    }
                }
                injectCleanCss.run();
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                downloadAndIngestEpub(ctx, url, userAgent, contentDisposition, mimetype,
                        mainHandler, statusBanner, statusTv, openNowBtn, dlgRef, onImported);
            }
        });

        root.addView(webView);

        final Runnable[] renderBookmarksRef = new Runnable[1];
        renderBookmarksRef[0] = new Runnable() {
            @Override public void run() {
                bmRow.removeAllViews();
                final List<String[]> bms = loadBookmarks(ctx);

                // Pin button (monochrome)
                LinearLayout pinBtn = makeChipWithIcon(ctx, d, FolioIcons.STAR, "Pin",
                        TEXT_PRIMARY, BG_CONTROL, BORDER);
                pinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String curUrl = webView.getUrl();
                        if (curUrl == null || curUrl.trim().isEmpty()) curUrl = getHomeUrl(ctx);
                        for (String[] existing : bms) {
                            if (curUrl.equalsIgnoreCase(existing[1])) {
                                Toast.makeText(ctx, "Already bookmarked", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        String lbl = webView.getTitle();
                        if (lbl == null || lbl.trim().isEmpty()) {
                            try { lbl = Uri.parse(curUrl).getHost(); } catch (Throwable ignored) {}
                        }
                        if (lbl == null || lbl.trim().isEmpty()) lbl = "Bookmark";
                        lbl = lbl.trim();
                        if (lbl.length() > 18) lbl = lbl.substring(0, 18).trim() + "…";
                        bms.add(new String[] { lbl, curUrl });
                        saveBookmarks(ctx, bms);
                        Toast.makeText(ctx, "Bookmarked " + lbl, Toast.LENGTH_SHORT).show();
                        renderBookmarksRef[0].run();
                    }
                });
                bmRow.addView(pinBtn);

                // EPUB Scanner button (single Kindle blue accent)
                LinearLayout scanBtn = makeChipWithIcon(ctx, d, FolioIcons.DOWNLOAD, "EPUBs",
                        ACCENT_BLUE, BG_CONTROL, 0xFF2C5278);
                scanBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        scanPageForEpubLinks(ctx, webView, mainHandler, statusBanner, statusTv, openNowBtn, dlgRef, onImported);
                    }
                });
                bmRow.addView(scanBtn);

                for (int i = 0; i < bms.size(); i++) {
                    final int idx = i;
                    final String[] item = bms.get(i);
                    TextView chip = new TextView(ctx);
                    chip.setText(item[0]);
                    chip.setTextColor(TEXT_MUTED);
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                    chip.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(BG_CANVAS);
                    cBg.setCornerRadius(dp(d, 7));
                    cBg.setStroke(dp(d, 1), BORDER);
                    chip.setBackground(cBg);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.rightMargin = dp(d, 6);
                    chip.setLayoutParams(cLp);
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) { webView.loadUrl(item[1]); }
                    });
                    chip.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override public boolean onLongClick(View v) {
                            bms.remove(idx);
                            saveBookmarks(ctx, bms);
                            Toast.makeText(ctx, "Removed bookmark", Toast.LENGTH_SHORT).show();
                            renderBookmarksRef[0].run();
                            return true;
                        }
                    });
                    bmRow.addView(chip);
                }
            }
        };
        renderBookmarksRef[0].run();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (webView.canGoBack()) webView.goBack();
            }
        });
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { webView.reload(); }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { webView.loadUrl(getHomeUrl(ctx)); }
        });
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean next = !isCleanMode(ctx);
                setCleanMode(ctx, next);
                updateCleanPill.run();
                Toast.makeText(ctx, next ? "Simplified View On" : "Simplified View Off", Toast.LENGTH_SHORT).show();
                webView.reload();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showUrlSettingsDialog(ctx, new Runnable() {
                    @Override public void run() {
                        updateCleanPill.run();
                        webView.loadUrl(getHomeUrl(ctx));
                    }
                });
            }
        });
        urlChip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showGoToUrlDialog(ctx, webView);
            }
        });

        final AlertDialog dlg = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(root)
                .create();
        dlgRef[0] = dlg;

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dlg.dismiss(); }
        });

        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialog) {
                try {
                    webView.stopLoading();
                    webView.destroy();
                } catch (Throwable ignored) {}
            }
        });

        FolioTheme.applyToTree(ctx, root);
        dlg.show();
        FolioTheme.styleDialog(ctx, dlg);
        Window win = dlg.getWindow();
        if (win != null) {
            win.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        webView.loadUrl(getHomeUrl(ctx));
    }

    private static void showGoToUrlDialog(final Context ctx, final WebView webView) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(BG_SURFACE);
        box.setPadding(dp(d, 20), dp(d, 18), dp(d, 20), dp(d, 10));

        TextView hdr = new TextView(ctx);
        hdr.setText("Go to URL");
        hdr.setTextColor(TEXT_PRIMARY);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        hdr.setTypeface(Typeface.SERIF, Typeface.BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 10));
        box.addView(hdr);

        final EditText et = new EditText(ctx);
        String cur = webView.getUrl();
        et.setText(cur != null && !cur.isEmpty() ? cur : getHomeUrl(ctx));
        et.setSelectAllOnFocus(true);
        et.setHint("https://...");
        et.setHintTextColor(0xFF606870);
        et.setTextColor(TEXT_PRIMARY);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        et.setSingleLine(true);
        et.setPadding(dp(d, 12), dp(d, 9), dp(d, 12), dp(d, 9));
        GradientDrawable eBg = new GradientDrawable();
        eBg.setColor(BG_CANVAS);
        eBg.setCornerRadius(dp(d, 8));
        eBg.setStroke(dp(d, 1), BORDER);
        et.setBackground(eBg);
        box.addView(et);

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setView(box)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        webView.loadUrl(normalizeUrl(et.getText().toString()));
                    }
                })
                .setNeutralButton("Set Home", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        String target = normalizeUrl(et.getText().toString());
                        setHomeUrl(ctx, target);
                        webView.loadUrl(target);
                        Toast.makeText(ctx, "Homepage updated to " + getHomeHostLabel(ctx), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void scanPageForEpubLinks(
            final Context ctx, final WebView webView, final Handler mainHandler,
            final LinearLayout statusBanner, final TextView statusTv,
            final TextView openNowBtn, final AlertDialog[] dlgRef, final Runnable onImported) {
        String js = "(function(){try{"
                + "var out=[];var seen={};var as=document.querySelectorAll('a[href]');"
                + "for(var i=0;i<as.length;i++){"
                + "var h=as[i].href||'';var hl=h.toLowerCase();"
                + "if((hl.indexOf('.epub')>=0||hl.indexOf('/epub')>=0)&&!seen[h]){"
                + "seen[h]=1;var t=(as[i].innerText||as[i].textContent||'').replace(/\\s+/g,' ').trim();"
                + "if(!t)t=h.substring(h.lastIndexOf('/')+1);"
                + "out.push(t.replace(/\\|/g,' ')+'|'+h);"
                + "if(out.length>=25)break;}}"
                + "return out.join('\\n');}catch(e){return '';}})();";
        webView.evaluateJavascript(js, new ValueCallback<String>() {
            @Override public void onReceiveValue(String raw) {
                if (raw == null || raw.equals("null") || raw.equals("\"\"")) {
                    Toast.makeText(ctx, "No EPUB links found on this page — open a book page first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String unq = raw;
                if (unq.startsWith("\"") && unq.endsWith("\"") && unq.length() >= 2) {
                    unq = unq.substring(1, unq.length() - 1);
                }
                unq = unq.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                final List<String[]> found = new ArrayList<>();
                for (String line : unq.split("\n")) {
                    int sep = line.indexOf('|');
                    if (sep > 0 && sep + 1 < line.length()) {
                        found.add(new String[] { line.substring(0, sep).trim(), line.substring(sep + 1).trim() });
                    }
                }
                if (found.isEmpty()) {
                    Toast.makeText(ctx, "No EPUB links found on this page — open a book page first", Toast.LENGTH_SHORT).show();
                    return;
                }
                final float d = ctx.getResources().getDisplayMetrics().density;
                LinearLayout wrap = new LinearLayout(ctx);
                wrap.setOrientation(LinearLayout.VERTICAL);
                wrap.setBackgroundColor(BG_SURFACE);
                wrap.setPadding(dp(d, 20), dp(d, 18), dp(d, 20), dp(d, 10));

                TextView hdr = new TextView(ctx);
                hdr.setText("EPUB Links (" + found.size() + ")");
                hdr.setTextColor(TEXT_PRIMARY);
                hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                hdr.setTypeface(Typeface.SERIF, Typeface.BOLD);
                hdr.setPadding(0, 0, 0, dp(d, 10));
                wrap.addView(hdr);

                ScrollView sv = new ScrollView(ctx);
                sv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(d, Math.min(300, Math.max(90, found.size() * 60)))));
                LinearLayout list = new LinearLayout(ctx);
                list.setOrientation(LinearLayout.VERTICAL);
                sv.addView(list);
                wrap.addView(sv);

                final AlertDialog[] pickDlg = new AlertDialog[1];
                for (final String[] item : found) {
                    LinearLayout row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
                    GradientDrawable rBg = new GradientDrawable();
                    rBg.setColor(BG_CONTROL);
                    rBg.setCornerRadius(dp(d, 8));
                    rBg.setStroke(dp(d, 1), BORDER);
                    row.setBackground(rBg);
                    LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rLp.bottomMargin = dp(d, 7);
                    row.setLayoutParams(rLp);

                    TextView tTv = new TextView(ctx);
                    tTv.setText(item[0]);
                    tTv.setTextColor(ACCENT_BLUE);
                    tTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    tTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                    tTv.setSingleLine(true);
                    tTv.setEllipsize(TextUtils.TruncateAt.END);
                    row.addView(tTv);

                    TextView uTv = new TextView(ctx);
                    uTv.setText(item[1]);
                    uTv.setTextColor(TEXT_MUTED);
                    uTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    uTv.setSingleLine(true);
                    uTv.setEllipsize(TextUtils.TruncateAt.END);
                    row.addView(uTv);

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            if (pickDlg[0] != null) pickDlg[0].dismiss();
                            downloadAndIngestEpub(ctx, item[1], webView.getSettings().getUserAgentString(),
                                    null, "application/epub+zip", mainHandler, statusBanner, statusTv, openNowBtn, dlgRef, onImported);
                        }
                    });
                    list.addView(row);
                }

                pickDlg[0] = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setView(wrap)
                        .setNegativeButton("Close", null)
                        .show();
            }
        });
    }

    private static boolean isLikelyEpubUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase(Locale.US);
        int q = lower.indexOf('?');
        String path = (q >= 0) ? lower.substring(0, q) : lower;
        return path.endsWith(".epub") || path.endsWith(".kepub.epub");
    }

    private static void downloadAndIngestEpub(
            final Context ctx, final String urlStr, final String userAgent,
            final String contentDisposition, final String mimeType,
            final Handler mainHandler, final LinearLayout statusBanner,
            final TextView statusTv, final TextView openNowBtn,
            final AlertDialog[] dlgRef, final Runnable onImported) {

        final String guessed = URLUtil.guessFileName(urlStr, contentDisposition, mimeType);
        final String displayFile = (guessed != null && !guessed.isEmpty()) ? guessed : "book.epub";

        mainHandler.post(new Runnable() {
            @Override public void run() {
                statusBanner.setVisibility(View.VISIBLE);
                statusBanner.setBackgroundColor(BG_CONTROL);
                statusTv.setTextColor(ACCENT_BLUE);
                statusTv.setText("Downloading " + displayFile + "...");
                openNowBtn.setVisibility(View.GONE);
            }
        });

        new Thread(new Runnable() {
            @Override public void run() {
                File tmpDir = new File(ctx.getCacheDir(), "folio_web_dl");
                if (!tmpDir.exists()) tmpDir.mkdirs();
                String safeName = displayFile.replaceAll("[^a-zA-Z0-9._-]", "_");
                if (!safeName.toLowerCase(Locale.US).endsWith(".epub")) safeName = safeName + ".epub";
                File dlFile = new File(tmpDir, safeName);

                try {
                    String currentUrl = urlStr;
                    HttpURLConnection conn = null;
                    for (int hop = 0; hop < 6; hop++) {
                        URL u = new URL(currentUrl);
                        conn = (HttpURLConnection) u.openConnection();
                        conn.setInstanceFollowRedirects(false);
                        conn.setConnectTimeout(20000);
                        conn.setReadTimeout(45000);
                        if (userAgent != null && !userAgent.isEmpty()) {
                            conn.setRequestProperty("User-Agent", userAgent);
                        }
                        try {
                            String homeRef = getHomeUrl(ctx);
                            if (homeRef != null && !homeRef.isEmpty()) {
                                conn.setRequestProperty("Referer", homeRef);
                            }
                        } catch (Throwable ignored) {}
                        try {
                            String cookie = CookieManager.getInstance().getCookie(currentUrl);
                            if (cookie != null && !cookie.isEmpty()) {
                                conn.setRequestProperty("Cookie", cookie);
                            }
                        } catch (Throwable ignored) {}
                        int code = conn.getResponseCode();
                        if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                            String loc = conn.getHeaderField("Location");
                            conn.disconnect();
                            if (loc == null || loc.isEmpty()) break;
                            currentUrl = new URL(u, loc).toString();
                            continue;
                        }
                        break;
                    }
                    if (conn == null || conn.getResponseCode() >= 400) {
                        throw new Exception("HTTP " + (conn != null ? conn.getResponseCode() : -1));
                    }

                    final long totalLen = conn.getContentLengthLong();
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    FileOutputStream fos = new FileOutputStream(dlFile);
                    byte[] buf = new byte[16384];
                    long readBytes = 0;
                    long lastUiMs = 0;
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        fos.write(buf, 0, r);
                        readBytes += r;
                        long now = System.currentTimeMillis();
                        if (now - lastUiMs > 220) {
                            lastUiMs = now;
                            final long kb = Math.max(1L, readBytes / 1024L);
                            final int pct = (totalLen > 0) ? (int) Math.min(100L, (readBytes * 100L) / totalLen) : -1;
                            mainHandler.post(new Runnable() {
                                @Override public void run() {
                                    statusTv.setText(pct >= 0
                                            ? ("Downloading " + displayFile + " (" + pct + "% · " + kb + " KB)...")
                                            : ("Downloading " + displayFile + " (" + kb + " KB)..."));
                                }
                            });
                        }
                    }
                    fos.close();
                    in.close();
                    conn.disconnect();

                    if (!isZipFile(dlFile)) {
                        dlFile.delete();
                        throw new Exception("Downloaded file is not a valid EPUB");
                    }

                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            statusTv.setText("Converting to KFX & adding to Library...");
                        }
                    });

                    File targetDir = new File(ctx.getFilesDir(), "folio_library");
                    if (!targetDir.exists()) targetDir.mkdirs();

                    EpubMetadataExtractor.BookMeta meta = EpubMetadataExtractor.extract(dlFile, safeName);
                    String bookKey = FolioMetadataDb.computeBookKey(meta.title, dlFile.length());
                    String safeBase = sanitizeFilename(meta.title) + "_" + bookKey;
                    final File outKfx = new File(targetDir, safeBase + ".kfx");
                    File outCover = new File(outKfx.getAbsolutePath() + ".cover.jpg");

                    boolean converted = ExternalEpubActivity.convertEpubToKfx(ctx, dlFile, outKfx);
                    if (!converted || !outKfx.exists() || outKfx.length() < 128) {
                        throw new Exception("KFX conversion failed");
                    }

                    try { MetadataEnricher.enrichInPlace(meta); } catch (Throwable ignored) {}

                    FolioMetadataDb db = new FolioMetadataDb(ctx);
                    try {
                        db.upsertBook(bookKey, outKfx, outCover, safeName, meta);
                    } finally {
                        db.close();
                    }
                    FolioMetadataDb.registerInKindleLibraryDb(ctx, bookKey, outKfx, outCover, meta);
                    dlFile.delete();

                    final String addedTitle = (meta.title != null && !meta.title.isEmpty()) ? meta.title : safeName;
                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            statusBanner.setBackgroundColor(BG_CONTROL);
                            statusTv.setTextColor(TEXT_PRIMARY);
                            statusTv.setText("Added \"" + addedTitle + "\" to Library");
                            openNowBtn.setVisibility(View.VISIBLE);
                            openNowBtn.setOnClickListener(new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    if (dlgRef[0] != null) dlgRef[0].dismiss();
                                    ExternalEpubActivity.launchKfxReader(ctx, outKfx);
                                }
                            });
                            if (onImported != null) onImported.run();
                            Toast.makeText(ctx, "Added \"" + addedTitle + "\" to Folio Library", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Throwable t) {
                    Log.e(TAG, "Web EPUB download/ingest failed", t);
                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            statusBanner.setBackgroundColor(BG_CONTROL);
                            statusTv.setTextColor(0xFFEF9A9A);
                            statusTv.setText(t.getMessage() != null ? t.getMessage() : "Download failed");
                            openNowBtn.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }, "folio-web-epub-dl").start();
    }

    private static boolean isZipFile(File f) {
        if (f == null || !f.exists() || f.length() < 4) return false;
        try {
            FileInputStream fis = new FileInputStream(f);
            int b0 = fis.read();
            int b1 = fis.read();
            fis.close();
            return b0 == 0x50 && b1 == 0x4B;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String sanitizeFilename(String s) {
        if (s == null || s.trim().isEmpty()) return "book";
        String clean = s.replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (clean.length() > 48) clean = clean.substring(0, 48);
        return clean;
    }

    private static FrameLayout makeBarIcon(Context ctx, float d, int iconId, int iconCol, int fillCol, int strokeCol) {
        FrameLayout fl = FolioIcons.makeIconButton(ctx, iconId, 16, 7, iconCol, fillCol, strokeCol, 7);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(d, 4);
        fl.setLayoutParams(lp);
        return fl;
    }

    private static LinearLayout makeChipWithIcon(
            Context ctx, float d, int iconId, String label, int textCol, int fillCol, int strokeCol) {
        LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.setPadding(dp(d, 9), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fillCol);
        bg.setCornerRadius(dp(d, 7));
        bg.setStroke(dp(d, 1), strokeCol);
        ll.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(d, 6);
        ll.setLayoutParams(lp);

        View ico = FolioIcons.makeIcon(ctx, iconId, 13, textCol);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(d, 13), dp(d, 13));
        ilp.rightMargin = dp(d, 5);
        ll.addView(ico, ilp);

        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(textCol);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        ll.addView(tv);
        return ll;
    }

    private static int dp(float d, int px) {
        return Math.round(px * d);
    }
}
