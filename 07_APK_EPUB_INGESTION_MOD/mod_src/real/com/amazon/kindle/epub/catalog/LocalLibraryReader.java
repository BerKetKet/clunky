package com.amazon.kindle.epub.catalog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads Folio's local kindle_library.db and /sdcard/kindle/ to power the
 * "Continue Reading" carousel and Library-seeded Open Library recommendations.
 */
public final class LocalLibraryReader {
    public static final class LocalBook {
        public String id = "";
        public String asin = "";
        public String title = "";
        public String author = "";
        public String filePath = "";
        public String coverPath = "";
        public int percent = 0;
    }

    public static List<LocalBook> loadRecentBooks(Context ctx, int maxCount) {
        List<LocalBook> out = new ArrayList<LocalBook>();
        SQLiteDatabase db = null;
        try {
            File dbFile = new File(ctx.getApplicationInfo().dataDir, "databases/kindle_library.db");
            if (!dbFile.exists()) return out;
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            File coversSmall = new File(ctx.getApplicationInfo().dataDir, "files/coverssmall");
            File coversDir = new File(ctx.getApplicationInfo().dataDir, "files/covers");

            // Query KindleContent joined with UserContent if available, else fallback to KindleContent
            Cursor c = null;
            try {
                c = db.rawQuery(
                        "SELECT k.ID, k.TITLE, k.AUTHOR, k.LOCATION, COALESCE(u.LAST_ACCESSED, 0) AS LA " +
                        "FROM KindleContent k LEFT JOIN UserContent u ON k.ID = u.ID " +
                        "WHERE k.TITLE IS NOT NULL AND k.TITLE != '' " +
                        "ORDER BY LA DESC, k.ROWID DESC LIMIT ?",
                        new String[]{String.valueOf(maxCount * 3)}
                );
            } catch (Throwable ignored) {
                c = db.rawQuery(
                        "SELECT ID, TITLE, AUTHOR, '' AS LOCATION, ROWID AS LA FROM KindleContent " +
                        "WHERE TITLE IS NOT NULL AND TITLE != '' ORDER BY ROWID DESC LIMIT ?",
                        new String[]{String.valueOf(maxCount * 3)}
                );
            }

            if (c != null) {
                try {
                    while (c.moveToNext() && out.size() < maxCount) {
                        String id = safeStr(c.getString(0));
                        String title = safeStr(c.getString(1));
                        String author = safeStr(c.getString(2));
                        String loc = safeStr(c.getString(3));
                        String lowerT = title.toLowerCase(Locale.US);
                        if (id.contains("B003ODIZL6") || lowerT.contains("dictionary") || lowerT.contains("render exam") || lowerT.contains("tables and images exam")) {
                            continue;
                        }
                        LocalBook lb = new LocalBook();
                        lb.id = id;
                        lb.asin = extractAsin(id);
                        lb.title = title;
                        lb.author = author;
                        lb.filePath = resolveLocalFile(loc, title);
                        lb.coverPath = findCoverFile(coversSmall, coversDir, lb.asin, id);
                        out.add(lb);
                    }
                } finally {
                    c.close();
                }
            }
        } catch (Throwable t) {
            android.util.Log.e("LocalLibraryReader", "Error reading kindle_library.db", t);
        } finally {
            if (db != null) {
                try { db.close(); } catch (Throwable ignored) {}
            }
        }
        return out;
    }

    public static Bitmap decodeCover(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            File f = new File(path);
            if (!f.exists() || f.length() < 64) return null;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            return BitmapFactory.decodeFile(path, opts);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String extractAsin(String id) {
        if (id == null) return "";
        String[] parts = id.split("/");
        if (parts.length >= 2 && !parts[1].isEmpty()) return parts[1];
        return id;
    }

    private static String findCoverFile(File coversSmall, File coversDir, String asin, String id) {
        if (asin != null && !asin.isEmpty()) {
            File[] dirs = new File[]{coversDir, coversSmall};
            for (File d : dirs) {
                if (d == null || !d.isDirectory()) continue;
                File[] files = d.listFiles();
                if (files == null) continue;
                for (File f : files) {
                    if (f.isFile() && f.length() > 100 && f.getName().contains(asin)) {
                        return f.getAbsolutePath();
                    }
                }
            }
        }
        return "";
    }

    private static String resolveLocalFile(String loc, String title) {
        if (loc != null && !loc.isEmpty()) {
            String p = loc.startsWith("file://") ? loc.substring(7) : loc;
            if (new File(p).exists()) return p;
        }
        // Fallback scan in /sdcard/kindle/
        try {
            File dir = new File("/sdcard/kindle");
            File[] files = dir.listFiles();
            if (files != null && title != null && title.length() >= 4) {
                String norm = title.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
                if (norm.length() > 12) norm = norm.substring(0, 12);
                for (File f : files) {
                    if (!f.isFile()) continue;
                    String fn = f.getName().toLowerCase(Locale.US);
                    if (!fn.endsWith(".epub") && !fn.endsWith(".azw3") && !fn.endsWith(".mobi")) continue;
                    String fnNorm = fn.replaceAll("[^a-z0-9]", "");
                    if (!norm.isEmpty() && fnNorm.contains(norm)) {
                        return f.getAbsolutePath();
                    }
                }
            }
        } catch (Throwable ignored) {}
        return "";
    }

    private static String safeStr(String s) {
        return s == null ? "" : s.trim();
    }
}
