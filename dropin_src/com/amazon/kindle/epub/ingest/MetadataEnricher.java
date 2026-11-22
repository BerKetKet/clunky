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
 * Tries ISBN first (when present), and automatically falls back to title+author
 * search if the ebook-specific ISBN returns 0 docs or no subjects — ensuring
 * modern ebook ISBN-13s still get rich genres, pub dates, and work keys.
 * Uses 15s timeouts for high-latency mobile TLS handshakes and fails gracefully offline.
 */
public final class MetadataEnricher {

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 15000;

    private MetadataEnricher() {}

    public static void enrichInPlace(EpubMetadataExtractor.BookMeta meta) {
        if (meta == null) return;
        boolean needsAuthor = EpubMetadataExtractor.isBlank(meta.author)
                || "Unknown Author".equalsIgnoreCase(meta.author.trim());
        boolean needsPubDate = EpubMetadataExtractor.isBlank(meta.pubDate);
        boolean needsGenres = meta.genres.isEmpty();
        boolean needsPublisher = EpubMetadataExtractor.isBlank(meta.publisher);

        if (!needsAuthor && !needsPubDate && !needsGenres && !needsPublisher) {
            return;
        }
        if (EpubMetadataExtractor.isBlank(meta.title) && EpubMetadataExtractor.isBlank(meta.isbn)) {
            return;
        }

        // Attempt 1: ISBN lookup if available
        if (!EpubMetadataExtractor.isBlank(meta.isbn)) {
            try {
                String q = "isbn=" + URLEncoder.encode(meta.isbn.trim(), "UTF-8");
                JSONObject doc = queryFirstDoc(q);
                if (doc != null) {
                    applyDoc(meta, doc);
                }
            } catch (Throwable ignored) {}
        }

        // Attempt 2: Fallback to title + author if genres or openLibraryKey still missing
        if ((meta.genres.isEmpty() || EpubMetadataExtractor.isBlank(meta.openLibraryKey)
                || EpubMetadataExtractor.isBlank(meta.pubDate))
                && !EpubMetadataExtractor.isBlank(meta.title)) {
            try {
                StringBuilder q = new StringBuilder();
                q.append("title=").append(URLEncoder.encode(meta.title.trim(), "UTF-8"));
                if (!EpubMetadataExtractor.isBlank(meta.author)
                        && !"Unknown Author".equalsIgnoreCase(meta.author.trim())) {
                    String primaryAuthor = meta.author.split(";")[0].trim();
                    if (!primaryAuthor.isEmpty()) {
                        q.append("&author=").append(URLEncoder.encode(primaryAuthor, "UTF-8"));
                    }
                }
                JSONObject doc = queryFirstDoc(q.toString());
                if (doc != null) {
                    applyDoc(meta, doc);
                }
            } catch (Throwable ignored) {}
        }
    }

    private static JSONObject queryFirstDoc(String queryParams) {
        HttpURLConnection conn = null;
        try {
            String urlStr = "https://openlibrary.org/search.json?" + queryParams
                    + "&fields=key,title,author_name,first_publish_year,subject,publisher,isbn&limit=1";
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "FolioReader/3.9 (Android; Local-First EPUB)");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) return null;

            StringBuilder body = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) body.append(line);
            br.close();

            JSONObject root = new JSONObject(body.toString());
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return null;
            return docs.getJSONObject(0);
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Throwable ignored) {}
            }
        }
    }

    private static void applyDoc(EpubMetadataExtractor.BookMeta meta, JSONObject doc) {
        boolean needsAuthor = EpubMetadataExtractor.isBlank(meta.author)
                || "Unknown Author".equalsIgnoreCase(meta.author.trim());
        if (needsAuthor) {
            JSONArray authors = doc.optJSONArray("author_name");
            if (authors != null && authors.length() > 0) {
                String a = authors.optString(0, "").trim();
                if (!a.isEmpty()) meta.author = a;
            }
        }

        if (EpubMetadataExtractor.isBlank(meta.pubDate)) {
            int year = doc.optInt("first_publish_year", 0);
            if (year > 0) meta.pubDate = String.valueOf(year);
        }

        if (meta.genres.isEmpty()) {
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

        if (EpubMetadataExtractor.isBlank(meta.publisher)) {
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

        if (EpubMetadataExtractor.isBlank(meta.openLibraryKey)) {
            String key = doc.optString("key", "").trim();
            if (!key.isEmpty()) meta.openLibraryKey = key;
        }
    }
}
