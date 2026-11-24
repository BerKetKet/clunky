package com.amazon.kindle.epub.ingest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Simplified in-app WebView catalog browser with user-configurable URL in settings,
 * optional Clean View CSS simplification, and automatic EPUB download -> KFX conversion
 * -> Folio Library ingestion.
 */
public final class FolioWebBrowserDialog {
    private static final String TAG = "FolioWebBrowser";
    private static final String PREFS = "folio_ui_prefs";
    private static final String KEY_HOME_URL = "web_browser_home_url";
    private static final String KEY_CLEAN_MODE = "web_browser_clean_mode";
    public static final String DEFAULT_URL = "https://standardebooks.org/ebooks";

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

    public static void showUrlSettingsDialog(final Context ctx, final Runnable onSaved) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF11131A);
        root.setPadding(dp(d, 18), dp(d, 16), dp(d, 18), dp(d, 12));

        TextView hdr = new TextView(ctx);
        hdr.setText("🌐 Web Browser Settings");
        hdr.setTextColor(0xFFFFFFFF);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        hdr.setTypeface(Typeface.DEFAULT_BOLD);
        hdr.setPadding(0, 0, 0, dp(d, 4));
        root.addView(hdr);

        TextView sub = new TextView(ctx);
        sub.setText("Set your catalog homepage URL (e.g. Standard Ebooks, Project Gutenberg, or your local Calibre server).");
        sub.setTextColor(0xFF94A3B8);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sub.setPadding(0, 0, 0, dp(d, 12));
        root.addView(sub);

        TextView lbl = new TextView(ctx);
        lbl.setText("HOMEPAGE URL");
        lbl.setTextColor(0xFF38BDF8);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setPadding(0, 0, 0, dp(d, 5));
        root.addView(lbl);

        final EditText urlEt = new EditText(ctx);
        urlEt.setText(getHomeUrl(ctx));
        urlEt.setHint("https://standardebooks.org/ebooks");
        urlEt.setHintTextColor(0xFF64748B);
        urlEt.setTextColor(0xFFFFFFFF);
        urlEt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        urlEt.setSingleLine(true);
        urlEt.setPadding(dp(d, 12), dp(d, 9), dp(d, 12), dp(d, 9));
        GradientDrawable uBg = new GradientDrawable();
        uBg.setColor(0xFF1A1F2E);
        uBg.setCornerRadius(dp(d, 9));
        uBg.setStroke(dp(d, 1), 0xFF334155);
        urlEt.setBackground(uBg);
        LinearLayout.LayoutParams uLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uLp.bottomMargin = dp(d, 10);
        urlEt.setLayoutParams(uLp);
        root.addView(urlEt);

        // Quick legal catalog presets
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
            chip.setTextColor(0xFF38BDF8);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setPadding(dp(d, 10), dp(d, 5), dp(d, 10), dp(d, 5));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(0xFF152638);
            cBg.setCornerRadius(dp(d, 8));
            cBg.setStroke(dp(d, 1), 0xFF1E4E75);
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

        // Clean Mode toggle row
        final boolean[] cleanState = new boolean[] { isCleanMode(ctx) };
        final TextView cleanToggle = new TextView(ctx);
        final Runnable styleClean = new Runnable() {
            @Override public void run() {
                boolean on = cleanState[0];
                cleanToggle.setText(on
                        ? "✨ Simplified View: ON  (hides clutter & popups)"
                        : "✨ Simplified View: OFF  (full original page)");
                cleanToggle.setTextColor(on ? 0xFF34D399 : 0xFF94A3B8);
                cleanToggle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                cleanToggle.setTypeface(Typeface.DEFAULT_BOLD);
                cleanToggle.setPadding(dp(d, 12), dp(d, 10), dp(d, 12), dp(d, 10));
                GradientDrawable ctBg = new GradientDrawable();
                ctBg.setColor(on ? 0xFF0F2A22 : 0xFF161923);
                ctBg.setCornerRadius(dp(d, 9));
                ctBg.setStroke(dp(d, 1), on ? 0xFF1E6B52 : 0xFF2A3142);
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

        new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert)
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
    }

    public static void show(final Context ctx, final Runnable onImported) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0E1017);

        // Top compact navigation bar
        LinearLayout topBar = new LinearLayout(ctx);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(0xFF141722);
        topBar.setPadding(dp(d, 8), dp(d, 8), dp(d, 8), dp(d, 8));

        final TextView btnBack = makeNavPill(ctx, d, "←", 0xFFE2E8F0, 0xFF1E2433, 0xFF334155);
        final TextView btnReload = makeNavPill(ctx, d, "↻", 0xFFE2E8F0, 0xFF1E2433, 0xFF334155);
        final TextView btnHome = makeNavPill(ctx, d, "🏠", 0xFF38BDF8, 0xFF152638, 0xFF1E4E75);

        final TextView urlChip = new TextView(ctx);
        urlChip.setText(getHomeHostLabel(ctx));
        urlChip.setTextColor(0xFFCBD5E1);
        urlChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        urlChip.setSingleLine(true);
        urlChip.setEllipsize(TextUtils.TruncateAt.END);
        urlChip.setPadding(dp(d, 10), dp(d, 6), dp(d, 10), dp(d, 6));
        GradientDrawable ucBg = new GradientDrawable();
        ucBg.setColor(0xFF0E111A);
        ucBg.setCornerRadius(dp(d, 8));
        ucBg.setStroke(dp(d, 1), 0xFF2A3142);
        urlChip.setBackground(ucBg);
        LinearLayout.LayoutParams ucLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ucLp.leftMargin = dp(d, 5);
        ucLp.rightMargin = dp(d, 5);
        urlChip.setLayoutParams(ucLp);

        final TextView btnClean = makeNavPill(ctx, d, "✨", 0xFF34D399, 0xFF0F2A22, 0xFF1E6B52);
        final TextView btnSettings = makeNavPill(ctx, d, "⚙", 0xFFC084FC, 0xFF2B1B3D, 0xFF6B21A8);
        final TextView btnClose = makeNavPill(ctx, d, "✕", 0xFFF87171, 0xFF2A181C, 0xFF5C262E);

        topBar.addView(btnBack);
        topBar.addView(btnReload);
        topBar.addView(btnHome);
        topBar.addView(urlChip);
        topBar.addView(btnClean);
        topBar.addView(btnSettings);
        topBar.addView(btnClose);
        root.addView(topBar);

        final ProgressBar progBar = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
        progBar.setMax(100);
        progBar.setProgress(0);
        progBar.setVisibility(View.GONE);
        progBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 3)));
        root.addView(progBar);

        // Live download / auto-ingest status banner (hidden until download starts)
        final LinearLayout statusBanner = new LinearLayout(ctx);
        statusBanner.setOrientation(LinearLayout.HORIZONTAL);
        statusBanner.setGravity(Gravity.CENTER_VERTICAL);
        statusBanner.setBackgroundColor(0xFF13261F);
        statusBanner.setPadding(dp(d, 12), dp(d, 8), dp(d, 12), dp(d, 8));
        statusBanner.setVisibility(View.GONE);

        final TextView statusTv = new TextView(ctx);
        statusTv.setTextColor(0xFF6EE7B7);
        statusTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusTv.setTypeface(Typeface.DEFAULT_BOLD);
        statusTv.setSingleLine(true);
        statusTv.setEllipsize(TextUtils.TruncateAt.END);
        statusTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        statusBanner.addView(statusTv);

        final TextView openNowBtn = new TextView(ctx);
        openNowBtn.setText("Open ›");
        openNowBtn.setTextColor(0xFF0E1017);
        openNowBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        openNowBtn.setTypeface(Typeface.DEFAULT_BOLD);
        openNowBtn.setPadding(dp(d, 10), dp(d, 4), dp(d, 10), dp(d, 4));
        GradientDrawable onBg = new GradientDrawable();
        onBg.setColor(0xFF34D399);
        onBg.setCornerRadius(dp(d, 7));
        openNowBtn.setBackground(onBg);
        openNowBtn.setVisibility(View.GONE);
        statusBanner.addView(openNowBtn);
        root.addView(statusBanner);

        final WebView webView = new WebView(ctx);
        int webH = (int) (ctx.getResources().getDisplayMetrics().heightPixels * 0.72f);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, webH));
        webView.setBackgroundColor(0xFF0E1017);

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
                bg.setColor(on ? 0xFF0F2A22 : 0xFF1E2433);
                bg.setCornerRadius(dp(d, 7));
                bg.setStroke(dp(d, 1), on ? 0xFF1E6B52 : 0xFF334155);
                btnClean.setBackground(bg);
                btnClean.setTextColor(on ? 0xFF34D399 : 0xFF64748B);
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
                Toast.makeText(ctx, next ? "Simplified View ON" : "Simplified View OFF", Toast.LENGTH_SHORT).show();
                webView.reload();
            }
        });
        View.OnClickListener openSettingsClick = new View.OnClickListener() {
            @Override public void onClick(View v) {
                showUrlSettingsDialog(ctx, new Runnable() {
                    @Override public void run() {
                        updateCleanPill.run();
                        webView.loadUrl(getHomeUrl(ctx));
                    }
                });
            }
        };
        btnSettings.setOnClickListener(openSettingsClick);
        urlChip.setOnClickListener(openSettingsClick);

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

        dlg.show();
        Window win = dlg.getWindow();
        if (win != null) {
            win.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        webView.loadUrl(getHomeUrl(ctx));
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
                statusBanner.setBackgroundColor(0xFF152638);
                statusTv.setTextColor(0xFF7DD3FC);
                statusTv.setText("⬇ Downloading " + displayFile + "...");
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

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    FileOutputStream fos = new FileOutputStream(dlFile);
                    byte[] buf = new byte[16384];
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        fos.write(buf, 0, r);
                    }
                    fos.close();
                    in.close();
                    conn.disconnect();

                    // Verify ZIP/EPUB magic bytes ('PK')
                    if (!isZipFile(dlFile)) {
                        dlFile.delete();
                        throw new Exception("Downloaded file is not a valid EPUB");
                    }

                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            statusTv.setText("⚙ Converting to KFX & adding to Library...");
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
                            statusBanner.setBackgroundColor(0xFF122B24);
                            statusTv.setTextColor(0xFF34D399);
                            statusTv.setText("✓ Added \"" + addedTitle + "\" to Library");
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
                            statusBanner.setBackgroundColor(0xFF2A181C);
                            statusTv.setTextColor(0xFFF87171);
                            statusTv.setText("✕ " + (t.getMessage() != null ? t.getMessage() : "Download failed"));
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

    private static TextView makeNavPill(Context ctx, float d, String label, int textCol, int fillCol, int strokeCol) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(textCol);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(d, 8), dp(d, 5), dp(d, 8), dp(d, 5));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fillCol);
        bg.setCornerRadius(dp(d, 7));
        bg.setStroke(dp(d, 1), strokeCol);
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(d, 4);
        tv.setLayoutParams(lp);
        return tv;
    }

    private static int dp(float d, int px) {
        return Math.round(px * d);
    }
}
