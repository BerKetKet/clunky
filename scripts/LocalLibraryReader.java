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
            File coversLarge = new File(ctx.getApplicationInfo().dataDir, "files/coverslarge");
            File coversMedium = new File(ctx.getApplicationInfo().dataDir, "files/coversmedium");
            File coversSmall = new File(ctx.getApplicationInfo().dataDir, "files/coverssmall");
            File coversDir = new File(ctx.getApplicationInfo().dataDir, "files/covers");

            // Query KindleContent joined with LocalContent for exact file path, and UserContent for last accessed time
            Cursor c = null;
            try {
                c = db.rawQuery(
                        "SELECT k.ID, k.TITLE, k.AUTHOR, l.FILE_PATH, COALESCE(u.LAST_ACCESSED, 0) AS LA " +
                        "FROM KindleContent k " +
                        "JOIN LocalContent l ON k.ID = l.KEY " +
                        "LEFT JOIN UserContent u ON k.ID = u.KEY " +
                        "WHERE k.TITLE IS NOT NULL AND k.TITLE != '' " +
                        "ORDER BY LA DESC, k.ROWID DESC LIMIT ?",
                        new String[]{String.valueOf(maxCount * 3)}
                );
            } catch (Throwable t1) {
                try {
                    c = db.rawQuery(
                            "SELECT k.ID, k.TITLE, k.AUTHOR, COALESCE(l.FILE_PATH, '') AS FILE_PATH, k.ROWID AS LA " +
                            "FROM KindleContent k LEFT JOIN LocalContent l ON k.ID = l.KEY " +
                            "WHERE k.TITLE IS NOT NULL AND k.TITLE != '' ORDER BY k.ROWID DESC LIMIT ?",
                            new String[]{String.valueOf(maxCount * 3)}
                    );
                } catch (Throwable t2) {
                    c = db.rawQuery(
                            "SELECT ID, TITLE, AUTHOR, '' AS FILE_PATH, ROWID AS LA FROM KindleContent " +
                            "WHERE TITLE IS NOT NULL AND TITLE != '' ORDER BY ROWID DESC LIMIT ?",
                            new String[]{String.valueOf(maxCount * 3)}
                    );
                }
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
                        lb.coverPath = findCoverFile(ctx, lb.filePath, coversLarge, coversMedium, coversSmall, coversDir, lb.asin, id);
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
        if (id.startsWith("AMZNID0/")) {
            String[] parts = id.split("/");
            if (parts.length >= 2) return parts[1];
        }
        return id;
    }

    public static String findCoverFile(Context ctx, String filePath, File coversLarge, File coversMedium, File coversSmall, File coversDir, String asin, String id) {
        // 1. Check real .cover.jpg sidecar FIRST before Kindle's native directories
        if (filePath != null && !filePath.isEmpty()) {
            String p = filePath.startsWith("file://") ? filePath.substring(7) : filePath;
            File sidecar = new File(p + ".cover.jpg");
            if (sidecar.exists() && sidecar.length() > 256) {
                return sidecar.getAbsolutePath();
            }
            if (ctx != null) {
                String baseName = new File(p).getName();
                File cacheCover = new File(ctx.getApplicationInfo().dataDir, "files/epub_cache/" + baseName + ".cover.jpg");
                if (cacheCover.exists() && cacheCover.length() > 256) {
                    return cacheCover.getAbsolutePath();
                }
                File cacheCoverAzw3 = new File(ctx.getApplicationInfo().dataDir, "files/epub_cache/" + baseName + ".azw3.cover.jpg");
                if (cacheCoverAzw3.exists() && cacheCoverAzw3.length() > 256) {
                    return cacheCoverAzw3.getAbsolutePath();
                }
            }
            String baseName = new File(p).getName();
            File sdCover = new File("/sdcard/kindle/" + baseName + ".cover.jpg");
            if (sdCover.exists() && sdCover.length() > 256) {
                return sdCover.getAbsolutePath();
            }
        }

        // Check if epub_cache has any file matching asin
        if (ctx != null && asin != null && !asin.isEmpty()) {
            File cacheDir = new File(ctx.getApplicationInfo().dataDir, "files/epub_cache");
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] cFiles = cacheDir.listFiles();
                if (cFiles != null) {
                    for (File f : cFiles) {
                        if (f.getName().endsWith(".cover.jpg") && f.length() > 256 && f.getName().contains(asin)) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            }
        }

        // 2. Fallback to Kindle's native cover dirs (coverslarge, coversmedium, covers, coverssmall)
        if (asin != null && !asin.isEmpty()) {
            File[] dirs = new File[]{coversLarge, coversMedium, coversDir, coversSmall};
            for (File d : dirs) {
                if (d == null || !d.isDirectory()) continue;
                File target = new File(d, asin);
                if (target.exists() && target.length() > 100) {
                    return target.getAbsolutePath();
                }
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

    public static String findCoverFile(File coversLarge, File coversMedium, File coversSmall, File coversDir, String asin, String id) {
        return findCoverFile(null, null, coversLarge, coversMedium, coversSmall, coversDir, asin, id);
    }

    public static String resolveLocalFile(String loc, String title) {
        if (loc != null && !loc.isEmpty()) {
            String p = loc.startsWith("file://") ? loc.substring(7) : loc;
            if (new File(p).exists()) return p;
        }
        // Fallback scan in files/epub_cache/ and /sdcard/kindle/
        try {
            if (title != null && title.length() >= 4) {
                String norm = title.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
                if (norm.length() > 10) norm = norm.substring(0, 10);
                File[] cacheDirs = new File[]{
                        new File("/data/user/0/com.amazon.kindle.epub/files/epub_cache"),
                        new File("/sdcard/kindle")
                };
                for (File dir : cacheDirs) {
                    if (dir.exists() && dir.isDirectory()) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (!f.isFile()) continue;
                                String fn = f.getName().toLowerCase(Locale.US);
                                if (!fn.endsWith(".epub") && !fn.endsWith(".azw3") && !fn.endsWith(".mobi") && !fn.endsWith(".prc")) continue;
                                String fnNorm = fn.replaceAll("[^a-z0-9]", "");
                                if (!norm.isEmpty() && fnNorm.contains(norm)) {
                                    return f.getAbsolutePath();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return (loc != null && !loc.isEmpty()) ? loc : "";
    }

    private static String safeStr(String s) {
        return s == null ? "" : s.trim();
    }
}
