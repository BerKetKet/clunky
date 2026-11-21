package com.amazon.kindle.epub.catalog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

/**
 * Persistent on-disk + in-memory cache for Open Library, Project Gutenberg (Gutendex),
 * OPDS feeds, and remote cover bitmaps. Supports TTL freshness and stale-on-error
 * fallback so Folio stays fast and resilient even during upstream maintenance outages.
 */
public final class DiskCache {
    public static final long TTL_FEED_MS = 24L * 60L * 60L * 1000L;      // 24 hours for search/feeds
    public static final long TTL_META_MS = 7L * 24L * 60L * 60L * 1000L; // 7 days for book metadata/ratings
    public static final long TTL_COVER_MS = 30L * 24L * 60L * 60L * 1000L; // 30 days for covers

    private static volatile File sMetaDir = null;
    private static volatile File sCoverDir = null;

    private static final LruCache<String, Bitmap> MEM_COVERS = new LruCache<String, Bitmap>(16 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return (value == null) ? 1 : Math.max(1, value.getByteCount() / 1024);
        }
    };

    public static void init(Context ctx) {
        if (ctx == null) return;
        try {
            File base = ctx.getCacheDir();
            if (base == null) return;
            File m = new File(base, "folio_meta_cache");
            File c = new File(base, "folio_cover_cache");
            m.mkdirs();
            c.mkdirs();
            sMetaDir = m;
            sCoverDir = c;
        } catch (Throwable ignored) {}
    }

    public static long ttlForUrl(String url) {
        if (url == null) return TTL_FEED_MS;
        if (url.contains("openlibrary.org/works/") || url.contains("/ratings.json") || url.contains("/authors/")) {
            return TTL_META_MS;
        }
        return TTL_FEED_MS;
    }

    public static String getText(String url, long maxAgeMs, boolean allowStale) {
        File dir = sMetaDir;
        if (dir == null || url == null || url.isEmpty()) return null;
        try {
            File f = new File(dir, hashKey(url) + ".txt");
            if (!f.exists() || f.length() == 0) return null;
            long age = System.currentTimeMillis() - f.lastModified();
            if (!allowStale && age > maxAgeMs) return null;
            byte[] bytes = readAll(f);
            return new String(bytes, "UTF-8");
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void putText(String url, String content) {
        File dir = sMetaDir;
        if (dir == null || url == null || content == null || content.isEmpty()) return;
        try {
            File f = new File(dir, hashKey(url) + ".txt");
            File tmp = new File(dir, f.getName() + ".tmp");
            FileOutputStream fos = new FileOutputStream(tmp);
            try {
                fos.write(content.getBytes("UTF-8"));
                fos.flush();
            } finally {
                fos.close();
            }
            tmp.renameTo(f);
        } catch (Throwable ignored) {}
    }

    public static Bitmap getCoverBitmap(String url) {
        if (url == null || url.isEmpty()) return null;
        Bitmap mem = MEM_COVERS.get(url);
        if (mem != null && !mem.isRecycled()) return mem;
        File dir = sCoverDir;
        if (dir == null) return null;
        try {
            File f = new File(dir, hashKey(url) + ".img");
            if (!f.exists() || f.length() < 128) return null;
            long age = System.currentTimeMillis() - f.lastModified();
            if (age > TTL_COVER_MS) return null;
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
            if (bmp != null) {
                MEM_COVERS.put(url, bmp);
            }
            return bmp;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void putCoverBytes(String url, byte[] data, Bitmap decoded) {
        if (url == null || url.isEmpty()) return;
        if (decoded != null) {
            MEM_COVERS.put(url, decoded);
        }
        File dir = sCoverDir;
        if (dir == null || data == null || data.length < 128) return;
        try {
            File f = new File(dir, hashKey(url) + ".img");
            File tmp = new File(dir, f.getName() + ".tmp");
            FileOutputStream fos = new FileOutputStream(tmp);
            try {
                fos.write(data);
                fos.flush();
            } finally {
                fos.close();
            }
            tmp.renameTo(f);
        } catch (Throwable ignored) {}
    }

    public static String getCacheStats() {
        int mCount = (sMetaDir != null && sMetaDir.list() != null) ? sMetaDir.list().length : 0;
        int cCount = (sCoverDir != null && sCoverDir.list() != null) ? sCoverDir.list().length : 0;
        return "meta=" + mCount + ", covers=" + cCount;
    }

    private static String hashKey(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 16 && i < dig.length; i++) {
                sb.append(String.format("%02x", dig[i] & 0xFF));
            }
            return sb.toString();
        } catch (Throwable ignored) {
            return String.valueOf(s.hashCode());
        }
    }

    private static byte[] readAll(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.min(f.length(), 65536));
        try {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) > 0) baos.write(buf, 0, n);
            return baos.toByteArray();
        } finally {
            fis.close();
        }
    }
}
