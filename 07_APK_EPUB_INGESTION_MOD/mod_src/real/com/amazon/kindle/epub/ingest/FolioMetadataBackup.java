package com.amazon.kindle.epub.ingest;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Portable JSON backup & restore for folio_metadata.db so enriched authors,
 * publishing dates, publishers, genres, ISBNs, and manual edits survive reinstalls.
 */
public final class FolioMetadataBackup {

    private static final String TAG = "FolioMetaBackup";
    private static final String BACKUP_DIR = "/sdcard/kindle/backups";
    private static final String BACKUP_FILE = "folio_metadata_backup.json";

    private FolioMetadataBackup() {}

    public static File getBackupFile(Context ctx) {
        File extDir = new File(BACKUP_DIR);
        if (extDir.exists() || extDir.mkdirs()) {
            return new File(extDir, BACKUP_FILE);
        }
        File fallback = new File(ctx.getFilesDir(), "backups");
        if (!fallback.exists()) fallback.mkdirs();
        return new File(fallback, BACKUP_FILE);
    }

    public static int exportToJson(Context ctx) {
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            JSONObject root = new JSONObject();
            root.put("version", 2);
            root.put("exported_at", System.currentTimeMillis());
            root.put("count", all.size());

            JSONArray arr = new JSONArray();
            for (FolioMetadataDb.BookRecord r : all) {
                JSONObject obj = new JSONObject();
                obj.put("book_key", r.bookKey);
                obj.put("kfx_path", r.kfxPath);
                obj.put("cover_path", r.coverPath);
                obj.put("source_name", r.sourceName);
                obj.put("title", r.title);
                obj.put("author", r.author);
                obj.put("publisher", r.publisher);
                obj.put("pub_date", r.pubDate);
                obj.put("genres", r.genres);
                obj.put("language", r.language);
                obj.put("isbn", r.isbn);
                obj.put("description", r.description);
                obj.put("open_library_key", r.openLibraryKey);
                obj.put("is_favorite", FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath));
                arr.put(obj);
            }
            root.put("books", arr);

            File out = getBackupFile(ctx);
            FileOutputStream fos = new FileOutputStream(out);
            try {
                fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                fos.flush();
            } finally {
                fos.close();
            }
            Log.i(TAG, "Exported " + all.size() + " books to " + out.getAbsolutePath());
            return all.size();
        } catch (Throwable t) {
            Log.e(TAG, "Metadata export failed", t);
            return -1;
        } finally {
            db.close();
        }
    }

    public static int restoreFromJson(Context ctx) {
        File inFile = getBackupFile(ctx);
        if (!inFile.exists()) return -1;

        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
            try {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            } finally {
                br.close();
            }

            JSONObject root = new JSONObject(sb.toString());
            JSONArray arr = root.optJSONArray("books");
            if (arr == null) return 0;

            int restored = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                String bookKey = obj.optString("book_key", "");
                String kfxPath = obj.optString("kfx_path", "");
                if (bookKey.isEmpty() || kfxPath.isEmpty()) continue;

                EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                meta.title = obj.optString("title", "");
                meta.author = obj.optString("author", "Unknown Author");
                meta.publisher = obj.optString("publisher", "");
                meta.pubDate = obj.optString("pub_date", "");
                meta.language = obj.optString("language", "");
                meta.isbn = obj.optString("isbn", "");
                meta.description = obj.optString("description", "");
                meta.openLibraryKey = obj.optString("open_library_key", "");
                String genresStr = obj.optString("genres", "");
                if (!genresStr.isEmpty()) {
                    for (String g : genresStr.split(",")) {
                        String c = g.trim();
                        if (!c.isEmpty()) meta.genres.add(c);
                    }
                }

                String coverPath = obj.optString("cover_path", "");
                String sourceName = obj.optString("source_name", "");
                db.upsertBook(
                        bookKey,
                        new File(kfxPath),
                        coverPath.isEmpty() ? null : new File(coverPath),
                        sourceName,
                        meta);
                if (obj.optBoolean("is_favorite", false)) {
                    FolioMetadataCatalogDialog.setFavorite(ctx, kfxPath, true);
                }
                restored++;
            }
            Log.i(TAG, "Restored " + restored + " metadata records from " + inFile.getAbsolutePath());
            return restored;
        } catch (Throwable t) {
            Log.e(TAG, "Metadata restore failed", t);
            return -1;
        } finally {
            db.close();
        }
    }
}
