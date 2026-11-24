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

    public static boolean isLimestoneLight(Context ctx) {
        String mode = getThemeMode(ctx);
        if ("limestone".equalsIgnoreCase(mode)) return true;
        if ("granite".equalsIgnoreCase(mode)) return false;
        if (ctx == null) return false;
        try {
            int uiMode = ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return uiMode != Configuration.UI_MODE_NIGHT_YES;
        } catch (Throwable ignored) {
            return false;
        }
    }

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

    public static void styleDialog(Context ctx, AlertDialog dlg) {
        if (ctx == null || dlg == null) return;
        ensureFonts(ctx);
        try {
            Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
            if (pos != null) {
                pos.setTextColor(accent(ctx));
                pos.setTypeface(sansBold);
            }
            Button neg = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
            if (neg != null) {
                neg.setTextColor(textMuted(ctx));
                neg.setTypeface(sansReg);
            }
            Button neu = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
            if (neu != null) {
                neu.setTextColor(textSecondary(ctx));
                neu.setTypeface(sansReg);
            }
        } catch (Throwable ignored) {}
    }
}
