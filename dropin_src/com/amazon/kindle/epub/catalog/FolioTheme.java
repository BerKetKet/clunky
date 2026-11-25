package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Centralized Azath Stone Edition typography and dual-mode theme helper:
 * - Granite (Dark Mode): deep honed granite surfaces (#0E0F11 / #1C1E22) + warm limestone accents (#D4C9B8).
 * - Limestone (Light Mode): warm quarried limestone surfaces (#F4F0E8 / #E7E1D5) + deep granite ink (#1A1B1E).
 * Pairs Regular EB Garamond display headings (>= 17sp) with crisp Inter UI controls (< 17sp).
 */
public final class FolioTheme {
    public static final String PREFS_UI = "folio_ui_prefs";
    public static final String KEY_THEME_MODE = "azath_theme_mode"; // "granite", "limestone", or "auto"

    // Granite (Dark Mode) Tokens
    public static final int BG_CANVAS = 0xFF0E0F11;
    public static final int BG_MODAL = 0xFF15171A;
    public static final int BG_SURFACE = 0xFF1C1E22;
    public static final int BG_CONTROL = 0xFF25282D;
    public static final int BORDER_HAIRLINE = 0xFF32353B;
    public static final int TEXT_PRIMARY = 0xFFECE8E0;
    public static final int TEXT_SECONDARY = 0xFFBDB8AE;
    public static final int TEXT_MUTED = 0xFF8A857D;
    public static final int ACCENT_LIMESTONE = 0xFFD4C9B8;
    public static final int ACCENT_CRIMSON = ACCENT_LIMESTONE; // unified stone alias
    public static final int ACCENT_BLUE = ACCENT_LIMESTONE;    // unified stone alias
    public static final int ACCENT_FILL = 0xFF282520;
    public static final int ACCENT_BORDER = 0xFF524B41;
    public static final int ACCENT_SOFT_TEXT = 0xFFE6DDD0;

    // Limestone (Light Mode) Tokens
    public static final int LIGHT_BG_CANVAS = 0xFFF4F0E8;
    public static final int LIGHT_BG_MODAL = 0xFFEFEAE0;
    public static final int LIGHT_BG_SURFACE = 0xFFE7E1D5;
    public static final int LIGHT_BG_CONTROL = 0xFFDDD6C7;
    public static final int LIGHT_BORDER_HAIRLINE = 0xFFCBC3B3;
    public static final int LIGHT_TEXT_PRIMARY = 0xFF1A1B1E;
    public static final int LIGHT_TEXT_SECONDARY = 0xFF4A4C52;
    public static final int LIGHT_TEXT_MUTED = 0xFF73757B;
    public static final int LIGHT_ACCENT_GRANITE = 0xFF2D3138;
    public static final int LIGHT_ACCENT_FILL = 0xFFDFD8CA;
    public static final int LIGHT_ACCENT_BORDER = 0xFFAFA695;
    public static final int LIGHT_ACCENT_SOFT_TEXT = 0xFF25282E;

    private static boolean fontsLoaded = false;
    private static Typeface serifReg = null;
    private static Typeface serifBold = null;
    private static Typeface sansReg = null;
    private static Typeface sansBold = null;

    private FolioTheme() {}

    public static String getThemeMode(Context ctx) {
        if (ctx == null) return "granite";
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE);
            return sp.getString(KEY_THEME_MODE, "auto");
        } catch (Throwable ignored) {
            return "granite";
        }
    }

    private static final java.util.concurrent.CopyOnWriteArrayList<java.lang.ref.WeakReference<Runnable>> THEME_LISTENERS =
            new java.util.concurrent.CopyOnWriteArrayList<java.lang.ref.WeakReference<Runnable>>();

    public static void registerThemeListener(Runnable r) {
        if (r == null) return;
        THEME_LISTENERS.add(new java.lang.ref.WeakReference<Runnable>(r));
    }

    public static void notifyThemeChanged() {
        for (java.lang.ref.WeakReference<Runnable> ref : THEME_LISTENERS) {
            Runnable r = ref.get();
            if (r == null) {
                THEME_LISTENERS.remove(ref);
            } else {
                try { r.run(); } catch (Throwable ignored) {}
            }
        }
    }

    public static void setThemeMode(Context ctx, String mode) {
        if (ctx == null || mode == null) return;
        try {
            ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE)
                    .edit().putString(KEY_THEME_MODE, mode).apply();
        } catch (Throwable ignored) {}
    }

    private static volatile boolean sLastLight = false;

    public static boolean isLimestoneLight() {
        return sLastLight;
    }

    public static boolean isLimestoneLight(Context ctx) {
        String mode = getThemeMode(ctx);
        boolean res;
        if ("limestone".equalsIgnoreCase(mode)) {
            res = true;
        } else if ("granite".equalsIgnoreCase(mode)) {
            res = false;
        } else if (ctx == null) {
            res = sLastLight;
        } else {
            try {
                int uiMode = ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                res = (uiMode != Configuration.UI_MODE_NIGHT_YES);
            } catch (Throwable ignored) {
                res = false;
            }
        }
        sLastLight = res;
        return res;
    }

    public static int bgCanvas() { return sLastLight ? LIGHT_BG_CANVAS : BG_CANVAS; }
    public static int bgModal() { return sLastLight ? LIGHT_BG_MODAL : BG_MODAL; }
    public static int bgSurface() { return sLastLight ? LIGHT_BG_SURFACE : BG_SURFACE; }
    public static int bgControl() { return sLastLight ? LIGHT_BG_CONTROL : BG_CONTROL; }
    public static int border() { return sLastLight ? LIGHT_BORDER_HAIRLINE : BORDER_HAIRLINE; }
    public static int textPrimary() { return sLastLight ? LIGHT_TEXT_PRIMARY : TEXT_PRIMARY; }
    public static int textSecondary() { return sLastLight ? LIGHT_TEXT_SECONDARY : TEXT_SECONDARY; }
    public static int textMuted() { return sLastLight ? LIGHT_TEXT_MUTED : TEXT_MUTED; }
    public static int accent() { return sLastLight ? LIGHT_ACCENT_GRANITE : ACCENT_LIMESTONE; }
    public static int accentFill() { return sLastLight ? LIGHT_ACCENT_FILL : ACCENT_FILL; }
    public static int accentBorder() { return sLastLight ? LIGHT_ACCENT_BORDER : ACCENT_BORDER; }
    public static int accentSoftText() { return sLastLight ? LIGHT_ACCENT_SOFT_TEXT : ACCENT_SOFT_TEXT; }

    public static int bgCanvas(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_BG_CANVAS : BG_CANVAS; }
    public static int bgModal(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_BG_MODAL : BG_MODAL; }
    public static int bgSurface(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_BG_SURFACE : BG_SURFACE; }
    public static int bgControl(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_BG_CONTROL : BG_CONTROL; }
    public static int border(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_BORDER_HAIRLINE : BORDER_HAIRLINE; }
    public static int textPrimary(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_TEXT_PRIMARY : TEXT_PRIMARY; }
    public static int textSecondary(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_TEXT_SECONDARY : TEXT_SECONDARY; }
    public static int textMuted(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_TEXT_MUTED : TEXT_MUTED; }
    public static int accent(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_ACCENT_GRANITE : ACCENT_LIMESTONE; }
    public static int accentFill(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_ACCENT_FILL : ACCENT_FILL; }
    public static int accentBorder(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_ACCENT_BORDER : ACCENT_BORDER; }
    public static int accentSoftText(Context ctx) { return isLimestoneLight(ctx) ? LIGHT_ACCENT_SOFT_TEXT : ACCENT_SOFT_TEXT; }

    private static synchronized void ensureFonts(Context ctx) {
        if (fontsLoaded || ctx == null) return;
        fontsLoaded = true;
        try {
            AssetManager am = ctx.getAssets();
            try {
                String[] azFiles = am.list("azath_fonts");
                if (azFiles != null) {
                    for (String fn : azFiles) {
                        if (fn == null) continue;
                        String low = fn.toLowerCase(java.util.Locale.US);
                        if (!low.endsWith(".ttf") && !low.endsWith(".otf")) continue;
                        String path = "azath_fonts/" + fn;
                        try {
                            if (low.contains("garamond")) {
                                if ((low.contains("semibold") || low.contains("bold") || low.contains("medium"))
                                        && !low.contains("italic")) {
                                    if (serifBold == null) serifBold = Typeface.createFromAsset(am, path);
                                } else if (!low.contains("italic") && !low.contains("bold")) {
                                    if (serifReg == null) serifReg = Typeface.createFromAsset(am, path);
                                }
                            } else if (low.contains("inter")) {
                                if ((low.contains("semibold") || low.contains("bold") || low.contains("medium"))
                                        && !low.contains("italic")) {
                                    if (sansBold == null) sansBold = Typeface.createFromAsset(am, path);
                                } else if (!low.contains("italic") && !low.contains("bold") && !low.contains("light") && !low.contains("thin")) {
                                    if (sansReg == null) sansReg = Typeface.createFromAsset(am, path);
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}

            String[] dirs = new String[] { "fonts", "font", "" };
            for (String dir : dirs) {
                String[] files;
                try {
                    files = am.list(dir);
                } catch (Throwable ignored) {
                    continue;
                }
                if (files == null) continue;
                for (String fn : files) {
                    if (fn == null) continue;
                    String low = fn.toLowerCase(java.util.Locale.US);
                    if (!low.endsWith(".ttf") && !low.endsWith(".otf")) continue;
                    String path = dir.isEmpty() ? fn : (dir + "/" + fn);
                    try {
                        if (low.contains("bookerly") || low.contains("caecilia")) {
                            if (low.contains("bold") && !low.contains("italic")) {
                                if (serifBold == null) serifBold = Typeface.createFromAsset(am, path);
                            } else if (!low.contains("italic") && !low.contains("bold")) {
                                if (serifReg == null) serifReg = Typeface.createFromAsset(am, path);
                            }
                        } else if (low.contains("ember")) {
                            if ((low.contains("bold") || low.contains("_bd") || low.contains("medium") || low.contains("he"))
                                    && !low.contains("italic")) {
                                if (sansBold == null) sansBold = Typeface.createFromAsset(am, path);
                            } else if (!low.contains("italic") && !low.contains("bold") && !low.contains("light")) {
                                if (sansReg == null) sansReg = Typeface.createFromAsset(am, path);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        if (serifReg == null) serifReg = Typeface.SERIF;
        if (serifBold == null) serifBold = Typeface.create(serifReg, Typeface.BOLD);
        if (sansReg == null) sansReg = Typeface.SANS_SERIF;
        if (sansBold == null) sansBold = Typeface.create(sansReg, Typeface.BOLD);
    }

    public static Typeface serif(Context ctx) {
        ensureFonts(ctx);
        return serifReg;
    }

    public static Typeface serifBold(Context ctx) {
        ensureFonts(ctx);
        return serifBold;
    }

    public static Typeface sans(Context ctx) {
        ensureFonts(ctx);
        return sansReg;
    }

    public static Typeface sansBold(Context ctx) {
        ensureFonts(ctx);
        return sansBold;
    }

    public static ColorStateList checkboxTint() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        int[] colors = new int[] { ACCENT_LIMESTONE, 0xFF706D68 };
        return new ColorStateList(states, colors);
    }

    public static void applyToTree(Context ctx, View v) {
        if (ctx == null || v == null) return;
        ensureFonts(ctx);
        float scaledDensity = ctx.getResources().getDisplayMetrics().scaledDensity;
        if (scaledDensity <= 0f) scaledDensity = 1f;
        applyRecursive(v, scaledDensity);
    }

    private static void applyRecursive(View v, float scaledDensity) {
        if (v instanceof CompoundButton) {
            try {
                ((CompoundButton) v).setButtonTintList(checkboxTint());
            } catch (Throwable ignored) {}
        }
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            Typeface cur = tv.getTypeface();
            if (cur != Typeface.MONOSPACE) {
                boolean isBold = (cur != null && cur.isBold());
                float sp = tv.getTextSize() / scaledDensity;
                if (sp >= 17.0f) {
                    tv.setTypeface(serifReg);
                    if (sp >= 19.5f) {
                        try {
                            if (Math.abs(tv.getLetterSpacing()) < 0.005f) {
                                tv.setLetterSpacing(-0.015f);
                            }
                        } catch (Throwable ignored) {}
                    }
                } else {
                    tv.setTypeface(isBold ? sansBold : sansReg);
                }
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyRecursive(vg.getChildAt(i), scaledDensity);
            }
        }
    }

    public static void applyToDialog(Context ctx, AlertDialog dlg) {
        if (ctx == null || dlg == null) return;
        try {
            if (dlg.getWindow() != null) {
                float d = ctx.getResources().getDisplayMetrics().density;
                GradientDrawable frame = new GradientDrawable();
                frame.setColor(bgModal(ctx));
                frame.setCornerRadius(Math.round(18f * d));
                frame.setStroke(Math.max(1, Math.round(1f * d)), border(ctx));
                dlg.getWindow().setBackgroundDrawable(frame);
                if (dlg.getWindow().getDecorView() != null) {
                    applyToTree(ctx, dlg.getWindow().getDecorView());
                }
            }
        } catch (Throwable ignored) {}
        styleDialog(ctx, dlg);
    }

    private static long toComposeColorLong(int argb) {
        return (((long) argb) & 0xFFFFFFFFL) << 32;
    }

    private static void setStaticLongField(Class<?> cls, String name, long val) {
        try {
            java.lang.reflect.Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.setLong(null, val);
        } catch (Throwable ignored) {}
    }

    /**
     * Dynamically aligns Kindle's Compose dark/light palette tokens (ib.b) for the bottom navigation bar
     * and top chrome with Azath's active Stone theme (Granite Dark vs Limestone Light) without Activity.recreate().
     */
    public static void syncComposeBottomBarColors(Context ctx) {
        if (ctx == null) return;
        boolean light = isLimestoneLight(ctx);
        try {
            Class<?> cls = Class.forName("ib.b");
            long surface = toComposeColorLong(light ? LIGHT_BG_MODAL : BG_MODAL);
            long surfaceLow = toComposeColorLong(light ? LIGHT_BG_SURFACE : BG_SURFACE);
            long surfaceCont = toComposeColorLong(light ? LIGHT_BG_CONTROL : BG_CONTROL);
            long onSurface = toComposeColorLong(light ? LIGHT_TEXT_PRIMARY : TEXT_PRIMARY);
            long onSurfaceVar = toComposeColorLong(light ? LIGHT_TEXT_MUTED : TEXT_MUTED);
            long outline = toComposeColorLong(light ? LIGHT_BORDER_HAIRLINE : BORDER_HAIRLINE);
            long primary = toComposeColorLong(light ? LIGHT_ACCENT_GRANITE : ACCENT_LIMESTONE);

            setStaticLongField(cls, "functionalCorePrimary", primary);
            setStaticLongField(cls, "functionalCorePrimaryInteractive", primary);
            setStaticLongField(cls, "functionalSupplementalInteractionActive", primary);
            setStaticLongField(cls, "functionalSupplementalInteractionOutlineSelected", primary);
            setStaticLongField(cls, "functionalNeutralSurface", surface);
            setStaticLongField(cls, "functionalNeutralSurfaceContainerLowest", surface);
            setStaticLongField(cls, "functionalNeutralSurfaceContainerLow", surface);
            setStaticLongField(cls, "functionalNeutralSurfaceContainer", surfaceLow);
            setStaticLongField(cls, "functionalNeutralSurfaceContainerHigh", surfaceCont);
            setStaticLongField(cls, "functionalNeutralOnSurface", onSurface);
            setStaticLongField(cls, "functionalNeutralOnSurfaceVariant", onSurfaceVar);
            setStaticLongField(cls, "functionalNeutralOutline", onSurfaceVar);
            setStaticLongField(cls, "functionalNeutralOutlineVariant", outline);
        } catch (Throwable ignored) {}
    }

    public static void applyWindowBars(Context ctx) {
        syncComposeBottomBarColors(ctx);
        syncStoneBottomBar(ctx);
        if (!(ctx instanceof android.app.Activity)) return;
        try {
            android.view.Window win = ((android.app.Activity) ctx).getWindow();
            if (win == null) return;
            boolean light = isLimestoneLight(ctx);
            win.setStatusBarColor(bgCanvas(ctx));
            win.setNavigationBarColor(bgModal(ctx));
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.view.WindowInsetsController wic = win.getInsetsController();
                if (wic != null) {
                    int flags = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                            | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                    wic.setSystemBarsAppearance(light ? flags : 0, flags);
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void styleDialog(Context ctx, AlertDialog dlg) {
        if (ctx == null || dlg == null) return;
        ensureFonts(ctx);
        try {
            int modalBg = bgModal(ctx);
            Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
            Button neg = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button neu = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
            Button anchorBtn = pos != null ? pos : (neg != null ? neg : neu);
            if (anchorBtn != null) {
                android.view.ViewParent p1 = anchorBtn.getParent();
                if (p1 instanceof View) {
                    ((View) p1).setBackgroundColor(modalBg);
                    android.view.ViewParent p2 = p1.getParent();
                    if (p2 instanceof View) {
                        ((View) p2).setBackgroundColor(modalBg);
                    }
                }
            }
            if (pos != null) {
                pos.setTextColor(accent(ctx));
                pos.setTypeface(sansBold);
            }
            if (neg != null) {
                neg.setTextColor(textMuted(ctx));
                neg.setTypeface(sansReg);
            }
            if (neu != null) {
                neu.setTextColor(textSecondary(ctx));
                neu.setTypeface(sansReg);
            }
        } catch (Throwable ignored) {}
    }

    private static volatile int sActiveBottomTab = 0; // 0=Home, 1=Library, 2=More
    private static final String TAG_STONE_BOTTOM_BAR = "azath_stone_bottom_bar_v1";

    public static void setActiveBottomTab(Context ctx, int tabIdx) {
        sActiveBottomTab = tabIdx;
        syncStoneBottomBar(ctx);
    }

    public static void syncStoneBottomBar(Context ctx) {
        if (!(ctx instanceof android.app.Activity)) return;
        try {
            final android.app.Activity act = (android.app.Activity) ctx;
            String clsName = act.getClass().getName();
            // Only mount on main shell activity, never inside book reader or dialogs
            if (clsName.contains("Reader") || clsName.contains("ExternalEpub") || clsName.contains("Notification")) {
                return;
            }
            final android.view.ViewGroup content = act.findViewById(android.R.id.content);
            if (content == null) return;

            View existing = content.findViewWithTag(TAG_STONE_BOTTOM_BAR);
            StoneBottomBarView bar;
            if (existing instanceof StoneBottomBarView) {
                bar = (StoneBottomBarView) existing;
            } else {
                bar = new StoneBottomBarView(act);
                bar.setTag(TAG_STONE_BOTTOM_BAR);
                float d = act.getResources().getDisplayMetrics().density;
                android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Math.round(63f * d));
                lp.gravity = android.view.Gravity.BOTTOM;
                content.addView(bar, lp);
            }
            bar.refreshState(isLimestoneLight(ctx), sActiveBottomTab);
            bar.bringToFront();
        } catch (Throwable ignored) {}
    }

    private static final class StoneBottomBarView extends View {
        private boolean lightMode = false;
        private int activeTab = 0;
        private final android.graphics.Paint fillPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.Paint strokePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.Paint textPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        StoneBottomBarView(Context ctx) {
            super(ctx);
            setClickable(false);
            setFocusable(false);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            strokePaint.setStyle(android.graphics.Paint.Style.STROKE);
            strokePaint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            strokePaint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
            textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        }

        void refreshState(boolean light, int tab) {
            this.lightMode = light;
            this.activeTab = tab;
            // Only overlay in Limestone Light Mode so Granite Dark Mode uses native bar + floating cover untouched
            setVisibility(light ? VISIBLE : GONE);
            invalidate();
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            if (!lightMode) return;
            float d = getResources().getDisplayMetrics().density;
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;

            int bgCol = LIGHT_BG_MODAL;       // #EFEAE0 warm Limestone
            int seamCol = LIGHT_BORDER_HAIRLINE; // #CBC3B3
            int actCol = LIGHT_ACCENT_GRANITE; // #2D3138 deep Granite
            int inactCol = LIGHT_TEXT_MUTED;   // #73757B weathered Granite

            // Cutout around floating mini book cover on far left (26dp..76dp, 0..53dp)
            float cutL = 26f * d;
            float cutR = 76f * d;
            float cutB = 53f * d;

            fillPaint.setColor(bgCol);
            canvas.drawRect(0f, 0f, cutL, h, fillPaint);
            canvas.drawRect(cutR, 0f, w, h, fillPaint);
            canvas.drawRect(cutL, cutB, cutR, h, fillPaint);

            // Top 1dp Limestone hairline seam
            fillPaint.setColor(seamCol);
            float seamH = Math.max(1f, d);
            canvas.drawRect(0f, 0f, cutL, seamH, fillPaint);
            canvas.drawRect(cutR, 0f, w, seamH, fillPaint);

            // 3 tab centers matching Kindle's exact bottom bar geometry (37.3%, 62.4%, 87.5%)
            float[] centers = new float[] { w * 0.373f, w * 0.624f, w * 0.875f };
            String[] labels = new String[] { "Home", "Library", "More" };
            float iconCy = h * 0.38f;
            float iconR = 9.5f * d;
            float labelY = h * 0.79f;

            textPaint.setTextSize(11.5f * d);
            strokePaint.setStrokeWidth(Math.max(1.8f, 1.75f * d));

            for (int i = 0; i < 3; i++) {
                boolean isAct = (i == activeTab);
                int col = isAct ? actCol : inactCol;
                float cx = centers[i];

                // Draw crisp vector icon
                strokePaint.setColor(col);
                fillPaint.setColor(col);
                if (i == 0) {
                    // Home icon
                    android.graphics.Path hp = new android.graphics.Path();
                    hp.moveTo(cx - iconR, iconCy - 1f * d);
                    hp.lineTo(cx, iconCy - iconR);
                    hp.lineTo(cx + iconR, iconCy - 1f * d);
                    hp.lineTo(cx + iconR * 0.76f, iconCy - 1f * d);
                    hp.lineTo(cx + iconR * 0.76f, iconCy + iconR * 0.85f);
                    hp.lineTo(cx - iconR * 0.76f, iconCy + iconR * 0.85f);
                    hp.lineTo(cx - iconR * 0.76f, iconCy - 1f * d);
                    hp.close();
                    canvas.drawPath(hp, isAct ? fillPaint : strokePaint);
                } else if (i == 1) {
                    // Library 3-books icon
                    float bw = 3.2f * d;
                    float bh = iconR * 1.65f;
                    float top = iconCy - bh * 0.5f;
                    canvas.drawRect(cx - 8f * d, top, cx - 8f * d + bw, top + bh, isAct ? fillPaint : strokePaint);
                    canvas.drawRect(cx - 2f * d, top, cx - 2f * d + bw, top + bh, isAct ? fillPaint : strokePaint);
                    canvas.save();
                    canvas.rotate(-10f, cx + 6f * d, iconCy);
                    canvas.drawRect(cx + 4f * d, top, cx + 4f * d + bw, top + bh, isAct ? fillPaint : strokePaint);
                    canvas.restore();
                } else {
                    // More 3-lines icon
                    float xL = cx - 8.5f * d;
                    float xR = cx + 8.5f * d;
                    for (int li = -1; li <= 1; li++) {
                        float ly = iconCy + li * 5.2f * d;
                        canvas.drawCircle(xL, ly, 1.3f * d, fillPaint);
                        canvas.drawLine(xL + 4.2f * d, ly, xR, ly, strokePaint);
                    }
                }

                // Draw Inter label
                textPaint.setColor(col);
                textPaint.setTypeface(isAct ? sansBold(getContext()) : sans(getContext()));
                canvas.drawText(labels[i], cx, labelY, textPaint);
            }
        }
    }

}
