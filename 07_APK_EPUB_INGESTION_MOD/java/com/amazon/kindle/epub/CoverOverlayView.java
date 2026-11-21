package com.amazon.kindle.epub;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.io.File;

/**
 * Native fullscreen #000000 black cover overlay attached directly to the reader
 * Activity's decor/content FrameLayout — completely outside WebCore/MobiDocViewer
 * reflow margins and footer overlays!
 *
 * Matches stock Kindle's native cover presentation: full-width high-res cover centered
 * vertically on pure #000000 black, zero margins, zero 0% footer.
 * Tap or swipe left to smoothly dismiss straight into Page 1 of the book prose.
 */
public final class CoverOverlayView {
    private static final String OVERLAY_TAG = "folio_fullscreen_cover_overlay";
    public static volatile boolean sDismissedThisSession = false;
    private static volatile String sLastActiveBookId = null;

    /**
     * Attaches a fullscreen #000000 black cover overlay onto the given Activity
     * if a valid high-res cover file exists. Returns true if shown.
     */
    public static boolean showIfAvailable(final Activity activity, final String coverImagePath) {
        android.util.Log.i("CoverOverlayView", "showIfAvailable called: activity=" + activity + " coverPath=" + coverImagePath);
        if (activity == null || coverImagePath == null || coverImagePath.isEmpty()) {
            android.util.Log.w("CoverOverlayView", "showIfAvailable rejected: null activity or empty path");
            return false;
        }
        final File coverFile = new File(coverImagePath);
        if (!coverFile.exists() || coverFile.length() < 256) {
            android.util.Log.w("CoverOverlayView", "showIfAvailable rejected: file missing or too small (" + coverFile.length() + ")");
            return false;
        }

        final Bitmap bmp = BitmapFactory.decodeFile(coverFile.getAbsolutePath());
        if (bmp == null) {
            android.util.Log.w("CoverOverlayView", "showIfAvailable rejected: decodeFile returned null for " + coverImagePath);
            return false;
        }
        android.util.Log.i("CoverOverlayView", "Decoded cover bitmap: " + bmp.getWidth() + "x" + bmp.getHeight());

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
                    View existing = decor.findViewWithTag(OVERLAY_TAG);
                    if (existing != null) {
                        decor.removeView(existing);
                    }

                    final FrameLayout overlay = new FrameLayout(activity);
                    overlay.setTag(OVERLAY_TAG);
                    overlay.setBackgroundColor(Color.BLACK);
                    overlay.setClickable(true);
                    overlay.setFocusable(true);
                    overlay.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );

                    ImageView iv = new ImageView(activity);
                    iv.setImageBitmap(bmp);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    iv.setAdjustViewBounds(true);
                    iv.setBackgroundColor(Color.BLACK);

                    FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER
                    );
                    overlay.addView(iv, ivLp);

                    final float[] downX = new float[]{0f};
                    overlay.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent ev) {
                            int act = ev.getActionMasked();
                            if (act == MotionEvent.ACTION_DOWN) {
                                downX[0] = ev.getX();
                                return true;
                            } else if (act == MotionEvent.ACTION_UP) {
                                float dx = ev.getX() - downX[0];
                                android.util.Log.i("CoverOverlayView", "Touch UP: dx=" + dx);
                                // Tap or swipe left dismisses smoothly into Page 1 of the book
                                if (Math.abs(dx) < 60f || dx < -60f) {
                                    sDismissedThisSession = true;
                                    dismissOverlay(decor, overlay);
                                }
                                return true;
                            }
                            return true;
                        }
                    });

                    decor.addView(overlay, new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
                    android.util.Log.i("CoverOverlayView", "Attached fullscreen cover overlay to decorView successfully!");
                } catch (Throwable t) {
                    android.util.Log.e("CoverOverlayView", "Failed to show fullscreen cover", t);
                }
            }
        });
        return true;
    }

    /**
     * Convenience helper: locates <azw3Path>.cover.jpg (written by EpubTranscoder)
     * and shows the fullscreen #000000 cover overlay.
     */
    public static boolean showForBookFile(Activity activity, String bookFilePath) {
        if (bookFilePath == null || bookFilePath.isEmpty()) return false;
        String candidate = bookFilePath + ".cover.jpg";
        if (new File(candidate).exists()) {
            return showIfAvailable(activity, candidate);
        }
        return false;
    }

    /**
     * Shows cover overlay for the active book if user is at position 0 / Cover.
     */
    public static boolean showForCurrentBookForced(final Activity activity) {
        sDismissedThisSession = false;
        return showForCurrentBookInternal(activity, true);
    }

    public static boolean showForCurrentBook(final Activity activity) {
        return showForCurrentBookInternal(activity, false);
    }

    private static boolean showForCurrentBookInternal(final Activity activity, final boolean force) {
        android.util.Log.i("CoverOverlayView", "showForCurrentBook called: activity=" + activity + " sDismissedThisSession=" + sDismissedThisSession);
        if (activity == null) {
            return false;
        }

        String asin = null;
        int pos = 0;
        try {
            Object factory = Class.forName("com.amazon.kcp.util.Utils").getMethod("getFactory").invoke(null);
            Object readerCtrl = factory.getClass().getMethod("getReaderController").invoke(factory);
            if (readerCtrl != null) {
                Object dv = readerCtrl.getClass().getMethod("getDocViewer").invoke(readerCtrl);
                if (dv != null) {
                    String dvName = dv.getClass().getName();
                    android.util.Log.i("CoverOverlayView", "Current DocViewer: " + dvName);
                    if (dvName.contains("KRIF") || dvName.contains("kfx") || dvName.contains("KRF")) {
                        android.util.Log.i("CoverOverlayView", "Skipping CoverOverlayView for native KFX book (KRIFDocViewer active)");
                        return false;
                    }
                    pos = ((Integer) dv.getClass().getMethod("getPageStartPosition").invoke(dv)).intValue();
                    android.util.Log.i("CoverOverlayView", "getPageStartPosition: " + pos);
                    Object bookInfo = dv.getClass().getMethod("getBookInfo").invoke(dv);
                    if (bookInfo != null) {
                        Object asinObj = bookInfo.getClass().getMethod("getAsin").invoke(bookInfo);
                        if (asinObj != null) {
                            asin = asinObj.toString();
                            android.util.Log.i("CoverOverlayView", "Active book ASIN: " + asin);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            android.util.Log.w("CoverOverlayView", "Failed to query getPageStartPosition / ASIN: " + t);
        }

        // If a different book is opened, reset dismissed session flag
        if (asin != null && !asin.equals(sLastActiveBookId)) {
            sLastActiveBookId = asin;
            sDismissedThisSession = false;
        }

        if (sDismissedThisSession) {
            android.util.Log.w("CoverOverlayView", "showForCurrentBook aborted: dismissed=" + sDismissedThisSession);
            return false;
        }

        // Only auto-show cover overlay when at the beginning of the book (pos <= 500)
        // Note: In Kindle DocViewer, getPageStartPosition() returns raw text byte offset,
        // which starts at ~100-200 bytes (header/fragment 0) on the very first page of the book.
        if (!force && pos > 500) {
            android.util.Log.i("CoverOverlayView", "Skipping auto-cover overlay at mid-book pos=" + pos);
            return false;
        }

        // 1. Check real high-res .cover.jpg sidecars FIRST (preventing gray placeholders from overriding!)
        // 1a. Try matching the exact book opened via Activity intent URI -> <path>.cover.jpg / epub_cache/<name>.azw3.cover.jpg
        try {
            android.content.Intent intent = activity.getIntent();
            if (intent != null && intent.getData() != null) {
                String dataPath = intent.getData().getPath();
                if (dataPath != null && dataPath.toLowerCase(java.util.Locale.US).endsWith(".kfx")) {
                    android.util.Log.i("CoverOverlayView", "Skipping CoverOverlayView for native KFX book: " + dataPath);
                    return false;
                }
                if (dataPath != null && !dataPath.isEmpty()) {
                    if (dataPath.startsWith("file://")) dataPath = dataPath.substring(7);
                    if (new File(dataPath + ".cover.jpg").exists() && new File(dataPath + ".cover.jpg").length() > 256) {
                        return showIfAvailable(activity, dataPath + ".cover.jpg");
                    }
                    String baseName = new File(dataPath).getName();
                    File cacheCover = new File(activity.getFilesDir(), "epub_cache/" + baseName + ".azw3.cover.jpg");
                    if (cacheCover.exists() && cacheCover.length() > 256) {
                        return showIfAvailable(activity, cacheCover.getAbsolutePath());
                    }
                    File cacheCoverDirect = new File(activity.getFilesDir(), "epub_cache/" + baseName + ".cover.jpg");
                    if (cacheCoverDirect.exists() && cacheCoverDirect.length() > 256) {
                        return showIfAvailable(activity, cacheCoverDirect.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 1b. If ASIN is known, check kindle_library.db LocalContent for real file path -> <path>.cover.jpg
        if (asin != null && !asin.isEmpty()) {
            try {
                File dbFile = new File(activity.getFilesDir().getParentFile(), "databases/kindle_library.db");
                if (dbFile.exists()) {
                    android.database.sqlite.SQLiteDatabase db = android.database.sqlite.SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
                    try {
                        android.database.Cursor c = db.rawQuery("SELECT FILE_PATH FROM LocalContent WHERE KEY LIKE ? LIMIT 1", new String[]{"%" + asin + "%"});
                        if (c != null) {
                            try {
                                if (c.moveToFirst()) {
                                    String fp = c.getString(0);
                                    if (fp != null && !fp.isEmpty()) {
                                        if (fp.startsWith("file://")) fp = fp.substring(7);
                                        File sc = new File(fp + ".cover.jpg");
                                        if (sc.exists() && sc.length() > 256) {
                                            return showIfAvailable(activity, sc.getAbsolutePath());
                                        }
                                        String base = new File(fp).getName();
                                        File cc = new File(activity.getFilesDir(), "epub_cache/" + base + ".cover.jpg");
                                        if (cc.exists() && cc.length() > 256) {
                                            return showIfAvailable(activity, cc.getAbsolutePath());
                                        }
                                        File ccAzw = new File(activity.getFilesDir(), "epub_cache/" + base + ".azw3.cover.jpg");
                                        if (ccAzw.exists() && ccAzw.length() > 256) {
                                            return showIfAvailable(activity, ccAzw.getAbsolutePath());
                                        }
                                    }
                                }
                            } finally {
                                c.close();
                            }
                        }
                    } finally {
                        db.close();
                    }
                }
            } catch (Throwable ignored) {}

            // 1c. Check epub_cache/ directly for any .cover.jpg matching ASIN
            File cacheDir = new File(activity.getFilesDir(), "epub_cache");
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] cfs = cacheDir.listFiles();
                if (cfs != null) {
                    for (File f : cfs) {
                        if (f.getName().endsWith(".cover.jpg") && f.length() > 256 && f.getName().contains(asin)) {
                            return showIfAvailable(activity, f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        // 1d. Fallback to last transcoded cover path
        String cp = EpubTranscoder.lastCoverPath;
        android.util.Log.i("CoverOverlayView", "lastCoverPath=" + cp);
        if (cp != null && new File(cp).exists() && new File(cp).length() > 256) {
            return showIfAvailable(activity, cp);
        }



        // 2. Only if NO .cover.jpg exists anywhere, fallback to Kindle's native cover directories
        if (asin != null && !asin.isEmpty()) {
            File[] coverDirs = new File[]{
                    new File(activity.getFilesDir(), "coverslarge"),
                    new File(activity.getFilesDir(), "coversmedium"),
                    new File(activity.getFilesDir(), "covers"),
                    new File(activity.getFilesDir(), "coverssmall")
            };
            for (File cd : coverDirs) {
                if (cd.exists() && cd.isDirectory()) {
                    File cf = new File(cd, asin);
                    if (cf.exists() && cf.length() > 256) {
                        android.util.Log.i("CoverOverlayView", "Found cover for ASIN " + asin + " in " + cd.getName() + ": " + cf.getAbsolutePath());
                        return showIfAvailable(activity, cf.getAbsolutePath());
                    }
                }
            }
        }

        android.util.Log.w("CoverOverlayView", "No cover image found to display");
        return false;
    }

    public static void resetSession() {
        sDismissedThisSession = false;
    }

    private static void dismissOverlay(final ViewGroup parent, final View overlay) {
        overlay.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            parent.removeView(overlay);
                        } catch (Throwable ignored) {}
                    }
                })
                .start();
    }
}
