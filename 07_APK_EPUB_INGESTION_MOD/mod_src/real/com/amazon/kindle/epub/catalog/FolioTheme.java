package com.amazon.kindle.epub.catalog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Centralized Kindle-native typography, checkbox/button highlight, and dialog theme helper.
 * Dynamically loads Kindle's bundled Bookerly (serif headings) and Amazon Ember (UI sans)
 * fonts from APK assets with graceful fallback to Typeface.SERIF / Typeface.SANS_SERIF.
 */
public final class FolioTheme {
    public static final int BG_CANVAS = 0xFF000000;
    public static final int BG_MODAL = 0xFF0D0F12;
    public static final int BG_SURFACE = 0xFF141618;
    public static final int BG_CONTROL = 0xFF1C1F22;
    public static final int BORDER_HAIRLINE = 0xFF262A2E;
    public static final int TEXT_PRIMARY = 0xFFECEFF1;
    public static final int TEXT_MUTED = 0xFF8E959C;
    public static final int ACCENT_BLUE = 0xFF4DA6FF;
    public static final int ACCENT_FILL = 0xFF162330;
    public static final int ACCENT_BORDER = 0xFF2C5278;

    private static boolean fontsLoaded = false;
    private static Typeface bookerlyReg = null;
    private static Typeface bookerlyBold = null;
    private static Typeface emberReg = null;
    private static Typeface emberBold = null;

    private FolioTheme() {}

    private static synchronized void ensureFonts(Context ctx) {
        if (fontsLoaded || ctx == null) return;
        fontsLoaded = true;
        try {
            AssetManager am = ctx.getAssets();
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
                                if (bookerlyBold == null) bookerlyBold = Typeface.createFromAsset(am, path);
                            } else if (!low.contains("italic") && !low.contains("bold")) {
                                if (bookerlyReg == null) bookerlyReg = Typeface.createFromAsset(am, path);
                            }
                        } else if (low.contains("ember")) {
                            if ((low.contains("bold") || low.contains("_bd") || low.contains("medium") || low.contains("he"))
                                    && !low.contains("italic")) {
                                if (emberBold == null) emberBold = Typeface.createFromAsset(am, path);
                            } else if (!low.contains("italic") && !low.contains("bold") && !low.contains("light")) {
                                if (emberReg == null) emberReg = Typeface.createFromAsset(am, path);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        if (bookerlyReg == null) bookerlyReg = Typeface.SERIF;
        if (bookerlyBold == null) bookerlyBold = Typeface.create(bookerlyReg, Typeface.BOLD);
        if (emberReg == null) emberReg = Typeface.SANS_SERIF;
        if (emberBold == null) emberBold = Typeface.create(emberReg, Typeface.BOLD);
    }

    public static Typeface serif(Context ctx) {
        ensureFonts(ctx);
        return bookerlyReg;
    }

    public static Typeface serifBold(Context ctx) {
        ensureFonts(ctx);
        return bookerlyBold;
    }

    public static Typeface sans(Context ctx) {
        ensureFonts(ctx);
        return emberReg;
    }

    public static Typeface sansBold(Context ctx) {
        ensureFonts(ctx);
        return emberBold;
    }

    public static ColorStateList checkboxTint() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        int[] colors = new int[] { ACCENT_BLUE, 0xFF6E7680 };
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
                boolean isSerif = (cur == Typeface.SERIF
                        || (cur != null && cur.equals(Typeface.create(Typeface.SERIF, Typeface.BOLD)))
                        || (tv.getTextSize() / scaledDensity) >= 17.0f);
                if (isSerif) {
                    tv.setTypeface(isBold ? bookerlyBold : bookerlyReg);
                } else {
                    tv.setTypeface(isBold ? emberBold : emberReg);
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

    public static void styleDialog(Context ctx, AlertDialog dlg) {
        if (ctx == null || dlg == null) return;
        ensureFonts(ctx);
        try {
            Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
            if (pos != null) {
                pos.setTextColor(ACCENT_BLUE);
                pos.setTypeface(emberBold);
            }
            Button neg = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
            if (neg != null) {
                neg.setTextColor(TEXT_MUTED);
                neg.setTypeface(emberReg);
            }
            Button neu = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
            if (neu != null) {
                neu.setTextColor(0xFFC4CBD2);
                neu.setTypeface(emberReg);
            }
        } catch (Throwable ignored) {}
    }
}
