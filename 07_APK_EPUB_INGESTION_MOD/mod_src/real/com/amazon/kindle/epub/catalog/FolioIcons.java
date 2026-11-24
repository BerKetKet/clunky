package com.amazon.kindle.epub.catalog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.widget.ImageView;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Kindle-native monochrome hairline vector icon library.
 * Drawn on a 24x24 normalized canvas with 1.65dp round-cap strokes to match
 * the native Kindle Android app's Home / Library / More / Settings chrome.
 */
public final class FolioIcons {
    public static final int SEARCH = 1;
    public static final int SHUFFLE = 2;
    public static final int LIBRARY = 3;
    public static final int IMPORT = 4;
    public static final int GLOBE = 5;
    public static final int FEED = 6;
    public static final int REFRESH = 7;
    public static final int BACKUP = 8;
    public static final int LOG = 9;
    public static final int NOTES = 10;
    public static final int INSIGHTS = 11;
    public static final int SHELVES = 12;
    public static final int STORAGE = 13;
    public static final int SETTINGS = 14;
    public static final int LINK = 15;
    public static final int INFO = 16;
    public static final int BACK = 17;
    public static final int HOME = 18;
    public static final int SPARKLE = 19;
    public static final int DOWNLOAD = 20;
    public static final int STAR = 21;
    public static final int CLOSE = 22;
    public static final int BOOKMARK = 23;
    public static final int TARGET = 24;
    public static final int CHEVRON_RIGHT = 25;
    public static final int USER = 26;
    public static final int UP_ARROW = 27;
    public static final int AUTHORS = 28;

    public static final int COLOR_PRIMARY = 0xFFECE8E0;
    public static final int COLOR_ICON = 0xFFBDB8AE;
    public static final int COLOR_MUTED = 0xFF8A857D;
    public static final int COLOR_ACCENT = 0xFFD4C9B8;

    private FolioIcons() {}

    private static final Map<Integer, Bitmap> ASSET_CACHE = new HashMap<>();

    private static String assetFileForIcon(int iconId) {
        switch (iconId) {
            case LIBRARY: return "azath_icons/ic_library.png";
            case IMPORT: return "azath_icons/ic_import.png";
            case GLOBE: return "azath_icons/ic_globe.png";
            case FEED: return "azath_icons/ic_feed.png";
            case REFRESH: return "azath_icons/ic_refresh.png";
            case NOTES: return "azath_icons/ic_notes.png";
            case SHELVES: return "azath_icons/ic_shelves.png";
            case INSIGHTS: return "azath_icons/ic_insights.png";
            case LOG: return "azath_icons/ic_log.png";
            case BACKUP: return "azath_icons/ic_backup.png";
            case STORAGE: return "azath_icons/ic_storage.png";
            case LINK: return "azath_icons/ic_link.png";
            case SETTINGS: return "azath_icons/ic_settings.png";
            case INFO: return "azath_icons/ic_info.png";
            case SHUFFLE: return "azath_icons/ic_compass.png";
            case SEARCH: return "azath_icons/ic_search.png";
            case AUTHORS: return "azath_icons/ic_authors.png";
            default: return null;
        }
    }

    private static Bitmap loadAssetIcon(Context ctx, int iconId) {
        if (ctx == null) return null;
        synchronized (ASSET_CACHE) {
            if (ASSET_CACHE.containsKey(iconId)) return ASSET_CACHE.get(iconId);
        }
        String path = assetFileForIcon(iconId);
        if (path == null) return null;
        Bitmap bmp = null;
        try {
            InputStream is = ctx.getAssets().open(path);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Throwable ignored) {}
        synchronized (ASSET_CACHE) {
            ASSET_CACHE.put(iconId, bmp);
        }
        return bmp;
    }

    public static View makeIcon(Context ctx, final int iconId, int sizeDp, final int color) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        final int px = Math.round(sizeDp * d);
        Bitmap bmp = loadAssetIcon(ctx, iconId);
        if (bmp != null) {
            ImageView img = new ImageView(ctx);
            img.setImageBitmap(bmp);
            if (FolioTheme.isLimestoneLight(ctx)) {
                img.setColorFilter(FolioTheme.textPrimary(ctx), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setLayoutParams(new LinearLayout.LayoutParams(px, px));
            return img;
        }
        IconView iv = new IconView(ctx, iconId, color, d);
        iv.setLayoutParams(new LinearLayout.LayoutParams(px, px));
        return iv;
    }

    public static FrameLayout makeIconButton(
            Context ctx, int iconId, int iconSizeDp, int padDp,
            int iconColor, int bgColor, int strokeColor, int radiusDp) {
        final float d = ctx.getResources().getDisplayMetrics().density;
        FrameLayout fl = new FrameLayout(ctx);
        int pad = Math.round(padDp * d);
        fl.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(Math.round(radiusDp * d));
        if (strokeColor != 0) bg.setStroke(Math.max(1, Math.round(d)), strokeColor);
        fl.setBackground(bg);

        int sz = Math.round(iconSizeDp * d);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sz, sz);
        lp.gravity = Gravity.CENTER;
        Bitmap bmp = loadAssetIcon(ctx, iconId);
        if (bmp != null) {
            ImageView img = new ImageView(ctx);
            img.setImageBitmap(bmp);
            if (FolioTheme.isLimestoneLight(ctx)) {
                img.setColorFilter(FolioTheme.textPrimary(ctx), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fl.addView(img, lp);
            fl.setTag(img);
        } else {
            IconView iv = new IconView(ctx, iconId, iconColor, d);
            fl.addView(iv, lp);
            fl.setTag(iv);
        }
        return fl;
    }

    public static void setButtonIconColor(FrameLayout btn, int newColor) {
        if (btn == null) return;
        Object tag = btn.getTag();
        if (tag instanceof IconView) {
            ((IconView) tag).setIconColor(newColor);
        }
    }

    private static final class IconView extends View {
        private final int iconId;
        private final Paint paint;
        private final Paint fillPaint;
        private final RectF rf = new RectF();
        private final Path path = new Path();

        IconView(Context ctx, int iconId, int color, float density) {
            super(ctx);
            this.iconId = iconId;
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeCap(Paint.Cap.ROUND);
            this.paint.setStrokeJoin(Paint.Join.ROUND);
            this.paint.setColor(color);
            this.paint.setStrokeWidth(1.65f * density);

            this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.fillPaint.setStyle(Paint.Style.FILL);
            this.fillPaint.setColor(color);
        }

        void setIconColor(int color) {
            paint.setColor(color);
            fillPaint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;
            float s = Math.min(w, h) / 24f;
            float ox = (w - 24f * s) * 0.5f;
            float oy = (h - 24f * s) * 0.5f;
            c.save();
            c.translate(ox, oy);
            path.reset();

            switch (iconId) {
                case SEARCH:
                    c.drawCircle(10.5f * s, 10.5f * s, 6.2f * s, paint);
                    c.drawLine(15.2f * s, 15.2f * s, 20.5f * s, 20.5f * s, paint);
                    break;

                case SHUFFLE:
                    path.moveTo(3f * s, 7f * s);
                    path.lineTo(8f * s, 7f * s);
                    path.lineTo(15f * s, 17f * s);
                    path.lineTo(20.5f * s, 17f * s);
                    path.moveTo(3f * s, 17f * s);
                    path.lineTo(8f * s, 17f * s);
                    path.lineTo(15f * s, 7f * s);
                    path.lineTo(20.5f * s, 7f * s);
                    // Arrowheads
                    path.moveTo(18f * s, 4.5f * s);
                    path.lineTo(20.5f * s, 7f * s);
                    path.lineTo(18f * s, 9.5f * s);
                    path.moveTo(18f * s, 14.5f * s);
                    path.lineTo(20.5f * s, 17f * s);
                    path.lineTo(18f * s, 19.5f * s);
                    c.drawPath(path, paint);
                    break;

                case LIBRARY:
                    rf.set(4f * s, 4f * s, 8.5f * s, 20f * s);
                    c.drawRoundRect(rf, 1f * s, 1f * s, paint);
                    rf.set(10.2f * s, 4f * s, 14.7f * s, 20f * s);
                    c.drawRoundRect(rf, 1f * s, 1f * s, paint);
                    c.drawLine(16.8f * s, 5f * s, 20.2f * s, 19.8f * s, paint);
                    break;

                case IMPORT:
                    path.moveTo(12f * s, 3.5f * s);
                    path.lineTo(12f * s, 14.5f * s);
                    path.moveTo(8f * s, 10.8f * s);
                    path.lineTo(12f * s, 14.8f * s);
                    path.lineTo(16f * s, 10.8f * s);
                    path.moveTo(4f * s, 15.5f * s);
                    path.lineTo(4f * s, 19f * s);
                    path.lineTo(20f * s, 19f * s);
                    path.lineTo(20f * s, 15.5f * s);
                    c.drawPath(path, paint);
                    break;

                case GLOBE:
                    c.drawCircle(12f * s, 12f * s, 8.5f * s, paint);
                    rf.set(8.2f * s, 3.5f * s, 15.8f * s, 20.5f * s);
                    c.drawOval(rf, paint);
                    c.drawLine(3.8f * s, 12f * s, 20.2f * s, 12f * s, paint);
                    break;

                case FEED:
                    c.drawCircle(6f * s, 18f * s, 1.4f * s, fillPaint);
                    rf.set(-2f * s, 10f * s, 14f * s, 26f * s);
                    c.drawArc(rf, 270f, 90f, false, paint);
                    rf.set(-8f * s, 4f * s, 20f * s, 32f * s);
                    c.drawArc(rf, 270f, 90f, false, paint);
                    break;

                case REFRESH:
                    rf.set(4.5f * s, 4.5f * s, 19.5f * s, 19.5f * s);
                    c.drawArc(rf, -55f, 295f, false, paint);
                    path.moveTo(16.2f * s, 4.2f * s);
                    path.lineTo(18.6f * s, 7.6f * s);
                    path.lineTo(14.5f * s, 8.5f * s);
                    c.drawPath(path, paint);
                    break;

                case BACKUP:
                    rf.set(4f * s, 4f * s, 20f * s, 9f * s);
                    c.drawRoundRect(rf, 1.5f * s, 1.5f * s, paint);
                    path.moveTo(5.5f * s, 9f * s);
                    path.lineTo(5.5f * s, 19.5f * s);
                    path.lineTo(18.5f * s, 19.5f * s);
                    path.lineTo(18.5f * s, 9f * s);
                    path.moveTo(9.5f * s, 13.5f * s);
                    path.lineTo(14.5f * s, 13.5f * s);
                    c.drawPath(path, paint);
                    break;

                case LOG:
                    rf.set(5.5f * s, 3.5f * s, 18.5f * s, 20.5f * s);
                    c.drawRoundRect(rf, 2f * s, 2f * s, paint);
                    c.drawLine(8.5f * s, 8.5f * s, 15.5f * s, 8.5f * s, paint);
                    c.drawLine(8.5f * s, 12.2f * s, 15.5f * s, 12.2f * s, paint);
                    c.drawLine(8.5f * s, 15.8f * s, 13f * s, 15.8f * s, paint);
                    break;

                case NOTES:
                    rf.set(4.5f * s, 4f * s, 19.5f * s, 20f * s);
                    c.drawRoundRect(rf, 2f * s, 2f * s, paint);
                    c.drawLine(8.5f * s, 4f * s, 8.5f * s, 20f * s, paint);
                    c.drawLine(11.5f * s, 9.5f * s, 16.5f * s, 9.5f * s, paint);
                    c.drawLine(11.5f * s, 13.5f * s, 16.5f * s, 13.5f * s, paint);
                    break;

                case INSIGHTS:
                    c.drawLine(5.5f * s, 19f * s, 5.5f * s, 13f * s, paint);
                    c.drawLine(12f * s, 19f * s, 12f * s, 6f * s, paint);
                    c.drawLine(18.5f * s, 19f * s, 18.5f * s, 10f * s, paint);
                    c.drawLine(3.5f * s, 19.5f * s, 20.5f * s, 19.5f * s, paint);
                    break;

                case SHELVES:
                    path.moveTo(4f * s, 5.5f * s);
                    path.lineTo(12.2f * s, 5.5f * s);
                    path.lineTo(20f * s, 13.3f * s);
                    path.lineTo(13.3f * s, 20f * s);
                    path.lineTo(5.5f * s, 12.2f * s);
                    path.close();
                    c.drawPath(path, paint);
                    c.drawCircle(8.5f * s, 9.5f * s, 1.1f * s, fillPaint);
                    break;

                case STORAGE:
                    rf.set(4f * s, 5f * s, 20f * s, 11f * s);
                    c.drawRoundRect(rf, 2f * s, 2f * s, paint);
                    rf.set(4f * s, 13f * s, 20f * s, 19f * s);
                    c.drawRoundRect(rf, 2f * s, 2f * s, paint);
                    c.drawCircle(7.5f * s, 8f * s, 0.9f * s, fillPaint);
                    c.drawCircle(7.5f * s, 16f * s, 0.9f * s, fillPaint);
                    break;

                case SETTINGS:
                    c.drawCircle(12f * s, 12f * s, 3.2f * s, paint);
                    c.drawCircle(12f * s, 12f * s, 7.2f * s, paint);
                    for (int a = 0; a < 360; a += 45) {
                        double rad = Math.toRadians(a);
                        float x1 = (float) (12f + Math.cos(rad) * 7.2f) * s;
                        float y1 = (float) (12f + Math.sin(rad) * 7.2f) * s;
                        float x2 = (float) (12f + Math.cos(rad) * 9.5f) * s;
                        float y2 = (float) (12f + Math.sin(rad) * 9.5f) * s;
                        c.drawLine(x1, y1, x2, y2, paint);
                    }
                    break;

                case LINK:
                    rf.set(3.8f * s, 8.5f * s, 12.5f * s, 15.5f * s);
                    c.drawRoundRect(rf, 3.5f * s, 3.5f * s, paint);
                    rf.set(11.5f * s, 8.5f * s, 20.2f * s, 15.5f * s);
                    c.drawRoundRect(rf, 3.5f * s, 3.5f * s, paint);
                    break;

                case INFO:
                    c.drawCircle(12f * s, 12f * s, 8.5f * s, paint);
                    c.drawCircle(12f * s, 8.2f * s, 1f * s, fillPaint);
                    c.drawLine(12f * s, 11.2f * s, 12f * s, 16.5f * s, paint);
                    break;

                case BACK:
                    path.moveTo(14.5f * s, 6f * s);
                    path.lineTo(8.5f * s, 12f * s);
                    path.lineTo(14.5f * s, 18f * s);
                    c.drawPath(path, paint);
                    break;

                case HOME:
                    path.moveTo(4f * s, 11.2f * s);
                    path.lineTo(12f * s, 4.5f * s);
                    path.lineTo(20f * s, 11.2f * s);
                    path.moveTo(6.5f * s, 9.8f * s);
                    path.lineTo(6.5f * s, 19.5f * s);
                    path.lineTo(17.5f * s, 19.5f * s);
                    path.lineTo(17.5f * s, 9.8f * s);
                    c.drawPath(path, paint);
                    break;

                case SPARKLE:
                    // Clean reader-mode document lines icon
                    c.drawLine(5f * s, 7f * s, 19f * s, 7f * s, paint);
                    c.drawLine(5f * s, 12f * s, 19f * s, 12f * s, paint);
                    c.drawLine(5f * s, 17f * s, 14f * s, 17f * s, paint);
                    break;

                case DOWNLOAD:
                    path.moveTo(12f * s, 4f * s);
                    path.lineTo(12f * s, 15.5f * s);
                    path.moveTo(7.5f * s, 11.5f * s);
                    path.lineTo(12f * s, 16f * s);
                    path.lineTo(16.5f * s, 11.5f * s);
                    path.moveTo(5f * s, 19.5f * s);
                    path.lineTo(19f * s, 19.5f * s);
                    c.drawPath(path, paint);
                    break;

                case STAR:
                    for (int i = 0; i < 10; i++) {
                        double ang = Math.toRadians(-90 + i * 36);
                        float r = (i % 2 == 0) ? 8.5f : 3.8f;
                        float px = (float) (12f + Math.cos(ang) * r) * s;
                        float py = (float) (12f + Math.sin(ang) * r) * s;
                        if (i == 0) path.moveTo(px, py);
                        else path.lineTo(px, py);
                    }
                    path.close();
                    c.drawPath(path, paint);
                    break;

                case CLOSE:
                    c.drawLine(6.5f * s, 6.5f * s, 17.5f * s, 17.5f * s, paint);
                    c.drawLine(17.5f * s, 6.5f * s, 6.5f * s, 17.5f * s, paint);
                    break;

                case BOOKMARK:
                    path.moveTo(6.5f * s, 4f * s);
                    path.lineTo(17.5f * s, 4f * s);
                    path.lineTo(17.5f * s, 20f * s);
                    path.lineTo(12f * s, 15.8f * s);
                    path.lineTo(6.5f * s, 20f * s);
                    path.close();
                    c.drawPath(path, paint);
                    break;

                case TARGET:
                    c.drawCircle(12f * s, 12f * s, 8.2f * s, paint);
                    c.drawCircle(12f * s, 12f * s, 4.4f * s, paint);
                    c.drawCircle(12f * s, 12f * s, 1.3f * s, fillPaint);
                    break;

                case CHEVRON_RIGHT:
                    path.moveTo(9.5f * s, 6.5f * s);
                    path.lineTo(15f * s, 12f * s);
                    path.lineTo(9.5f * s, 17.5f * s);
                    c.drawPath(path, paint);
                    break;

                case USER:
                    c.drawCircle(12f * s, 8.2f * s, 3.6f * s, paint);
                    rf.set(5f * s, 14.2f * s, 19f * s, 25f * s);
                    c.drawArc(rf, 180f, 180f, false, paint);
                    break;

                case UP_ARROW:
                    path.moveTo(12f * s, 18.5f * s);
                    path.lineTo(12f * s, 5.5f * s);
                    path.moveTo(6.8f * s, 10.7f * s);
                    path.lineTo(12f * s, 5.5f * s);
                    path.lineTo(17.2f * s, 10.7f * s);
                    c.drawPath(path, paint);
                    break;

                default:
                    c.drawCircle(12f * s, 12f * s, 6f * s, paint);
                    break;
            }
            c.restore();
        }
    }
}
