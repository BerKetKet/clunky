package com.amazon.kindle.epub.ingest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight, non-blocking metadata enricher using Open Library Search API.
 * Fills in missing author, publishing date, genre/subjects, publisher, and ISBN
 * using title/author/ISBN extracted from the EPUB. Fails gracefully when offline
 * (4s connect / 5s read timeout) so book opening NEVER hangs.
 */
public final class MetadataEnricher {

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 15000;

    private MetadataEnricher() {}

    public static void enrichInPlace(EpubMetadataExtractor.BookMeta meta) {
        if (meta == null) return;
        for (int i = meta.genres.size() - 1; i >= 0; i--) {
            String g = meta.genres.get(i);
            if (g == null || g.trim().isEmpty()) {
                meta.genres.remove(i);
            }
        }
        boolean needsAuthor = EpubMetadataExtractor.isBlank(meta.author)
                || "Unknown Author".equalsIgnoreCase(meta.author.trim());
        boolean needsPubDate = EpubMetadataExtractor.isBlank(meta.pubDate);
        boolean needsGenres = meta.genres.isEmpty();
        boolean needsPublisher = EpubMetadataExtractor.isBlank(meta.publisher);
        android.util.Log.i("MetadataEnricher", "enrichInPlace title=" + meta.title + " author=" + meta.author + " needsGenres=" + needsGenres + " genresCount=" + meta.genres.size());

        if (!needsAuthor && !needsPubDate && !needsGenres && !needsPublisher) {
            android.util.Log.i("MetadataEnricher", "All fields already populated, skipping Open Library");
            return;
        }
        if (EpubMetadataExtractor.isBlank(meta.title) && EpubMetadataExtractor.isBlank(meta.isbn)) {
            return;
        }

        HttpURLConnection conn = null;
        try {
            StringBuilder urlSb = new StringBuilder("https://openlibrary.org/search.json?");
            if (!EpubMetadataExtractor.isBlank(meta.title)) {
                urlSb.append("title=").append(URLEncoder.encode(meta.title, "UTF-8"));
                if (!EpubMetadataExtractor.isBlank(meta.author) && !"Unknown Author".equalsIgnoreCase(meta.author.trim())) {
                    String primaryAuthor = meta.author.split(";")[0].trim();
                    if (!primaryAuthor.isEmpty()) {
                        urlSb.append("&author=").append(URLEncoder.encode(primaryAuthor, "UTF-8"));
                    }
                }
            } else if (!EpubMetadataExtractor.isBlank(meta.isbn)) {
                urlSb.append("isbn=").append(URLEncoder.encode(meta.isbn, "UTF-8"));
            }
            urlSb.append("&fields=key,title,author_name,first_publish_year,subject,publisher,isbn&limit=1");
            android.util.Log.i("MetadataEnricher", "Requesting: " + urlSb.toString());

            URL url = new URL(urlSb.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "FolioReader/3.4 (Android; Local-First EPUB)");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            android.util.Log.i("MetadataEnricher", "Response code: " + code);
            if (code != 200) return;

            StringBuilder body = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }
            br.close();

            JSONObject root = new JSONObject(body.toString());
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return;

            JSONObject doc = docs.getJSONObject(0);

            if (needsAuthor) {
                JSONArray authors = doc.optJSONArray("author_name");
                if (authors != null && authors.length() > 0) {
                    String a = authors.optString(0, "").trim();
                    if (!a.isEmpty()) meta.author = a;
                }
            }

            if (needsPubDate) {
                int year = doc.optInt("first_publish_year", 0);
                if (year > 0) {
                    meta.pubDate = String.valueOf(year);
                }
            }

            if (needsGenres) {
                JSONArray subjects = doc.optJSONArray("subject");
                if (subjects != null) {
                    for (int i = 0; i < subjects.length() && meta.genres.size() < 5; i++) {
                        String s = subjects.optString(i, "").trim();
                        if (!s.isEmpty() && s.length() <= 45 && !meta.genres.contains(s)) {
                            meta.genres.add(s);
                        }
                    }
                }
            }

            if (needsPublisher) {
                JSONArray pubs = doc.optJSONArray("publisher");
                if (pubs != null && pubs.length() > 0) {
                    String p = pubs.optString(0, "").trim();
                    if (!p.isEmpty()) meta.publisher = p;
                }
            }

            if (EpubMetadataExtractor.isBlank(meta.isbn)) {
                JSONArray isbns = doc.optJSONArray("isbn");
                if (isbns != null && isbns.length() > 0) {
                    String id = isbns.optString(0, "").trim();
                    if (!id.isEmpty()) meta.isbn = id;
                }
            }

            String key = doc.optString("key", "").trim();
            if (!key.isEmpty()) {
                meta.openLibraryKey = key;
            }
        } catch (Throwable t) {
            android.util.Log.w("MetadataEnricher", "Open Library enrich error: " + t.getMessage(), t);
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Throwable ignored) {}
            }
        }
    }
}
