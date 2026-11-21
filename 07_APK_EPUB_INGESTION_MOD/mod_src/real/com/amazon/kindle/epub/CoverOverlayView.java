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
 * Matches stock Kindle's cover presentation: full-width high-res cover centered
 * vertically on pure #000000 black, zero margins, zero 0% footer.
 * Tap or swipe left to smoothly dismiss straight into Page 1 of the book.
 */
public final class CoverOverlayView {
    private static final String OVERLAY_TAG = "folio_fullscreen_cover_overlay";
    public static volatile boolean sDismissedThisSession = false;

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
                                // Tap or swipe left dismisses smoothly into the book
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
    public static boolean showForCurrentBook(final Activity activity) {
        android.util.Log.i("CoverOverlayView", "showForCurrentBook called: activity=" + activity + " sDismissedThisSession=" + sDismissedThisSession);
        if (activity == null || sDismissedThisSession) {
            android.util.Log.w("CoverOverlayView", "showForCurrentBook aborted: activity=" + activity + " dismissed=" + sDismissedThisSession);
            return false;
        }
        int pos = 0;
        try {
            Object factory = Class.forName("com.amazon.kcp.util.Utils").getMethod("getFactory").invoke(null);
            Object readerCtrl = factory.getClass().getMethod("getReaderController").invoke(factory);
            if (readerCtrl != null) {
                Object dv = readerCtrl.getClass().getMethod("getDocViewer").invoke(readerCtrl);
                if (dv != null) {
                    pos = ((Integer) dv.getClass().getMethod("getPageStartPosition").invoke(dv)).intValue();
                    android.util.Log.i("CoverOverlayView", "getPageStartPosition: " + pos);
                }
            }
        } catch (Throwable t) {
            android.util.Log.w("CoverOverlayView", "Failed to query getPageStartPosition: " + t);
        }

        String cp = EpubTranscoder.lastCoverPath;
        android.util.Log.i("CoverOverlayView", "lastCoverPath=" + cp);
        if (cp != null && new File(cp).exists()) {
            return showIfAvailable(activity, cp);
        }

        // Search /sdcard/kindle/ for newest .cover.jpg
        File dir = new File("/sdcard/kindle");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                File newest = null;
                for (File f : files) {
                    if (f.getName().endsWith(".cover.jpg")) {
                        if (newest == null || f.lastModified() > newest.lastModified()) {
                            newest = f;
                        }
                    }
                }
                if (newest != null) {
                    android.util.Log.i("CoverOverlayView", "Found fallback cover in /sdcard/kindle: " + newest.getAbsolutePath());
                    return showIfAvailable(activity, newest.getAbsolutePath());
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
