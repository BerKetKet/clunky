package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Queries Open Library API for book ratings, ISBN, description, and similar books by subject.
 */
public final class OpenLibraryClient {
    public static final class BookMeta {
        public String workKey = "";
        public String isbn = "";
        public double avgRating = 0.0;
        public int ratingsCount = 0;
        public String description = "";
        public List<String> similarTitles = new ArrayList<String>();
        public String coverUrl = "";
    }

    public static BookMeta fetchMeta(String title, String author) {
        BookMeta meta = new BookMeta();
        try {
            String q = URLEncoder.encode(title + " " + author, "UTF-8");
            String searchJson = GutendexSource.httpGet("https://openlibrary.org/search.json?q=" + q + "&fields=key,title,author_name,cover_i,isbn,subject&limit=1");
            JSONObject root = new JSONObject(searchJson);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return meta;
            JSONObject doc = docs.optJSONObject(0);
            if (doc == null) return meta;
            meta.workKey = doc.optString("key", "");
            int coverI = doc.optInt("cover_i", 0);
            if (coverI > 0) {
                meta.coverUrl = "https://covers.openlibrary.org/b/id/" + coverI + "-M.jpg";
            }
            JSONArray isbns = doc.optJSONArray("isbn");
            if (isbns != null && isbns.length() > 0) {
                meta.isbn = isbns.optString(0, "");
            }

            if (!meta.workKey.isEmpty()) {
                try {
                    String rJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + "/ratings.json");
                    JSONObject rRoot = new JSONObject(rJson);
                    JSONObject summary = rRoot.optJSONObject("summary");
                    if (summary != null) {
                        meta.avgRating = summary.optDouble("average", 0.0);
                        meta.ratingsCount = summary.optInt("count", 0);
                    }
                } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Error", t); }

                try {
                    String wJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + ".json");
                    JSONObject wRoot = new JSONObject(wJson);
                    Object descObj = wRoot.opt("description");
                    if (descObj instanceof String) {
                        meta.description = (String) descObj;
                    } else if (descObj instanceof JSONObject) {
                        meta.description = ((JSONObject) descObj).optString("value", "");
                    }
                } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Error", t); }
            }

            // Similar books via subject
            JSONArray subjects = doc.optJSONArray("subject");
            if (subjects != null && subjects.length() > 0) {
                for (int si = 0; si < subjects.length() && meta.similarTitles.size() < 4; si++) {
                    String subj = subjects.optString(si, "").toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "");
                    if (subj.isEmpty()) continue;
                    try {
                        String sJson = GutendexSource.httpGet("https://openlibrary.org/subjects/" + subj + ".json?limit=6");
                        JSONObject sRoot = new JSONObject(sJson);
                        JSONArray works = sRoot.optJSONArray("works");
                        if (works != null) {
                            for (int i = 0; i < works.length() && meta.similarTitles.size() < 4; i++) {
                                JSONObject w = works.optJSONObject(i);
                                if (w == null) continue;
                                String wt = w.optString("title", "");
                                if (!wt.isEmpty() && !wt.equalsIgnoreCase(title) && !meta.similarTitles.contains(wt)) {
                                    meta.similarTitles.add(wt);
                                }
                            }
                        }
                    } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Error", t); }
                }
            }
        } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Outer error", t);
        }
        return meta;
    }
}
