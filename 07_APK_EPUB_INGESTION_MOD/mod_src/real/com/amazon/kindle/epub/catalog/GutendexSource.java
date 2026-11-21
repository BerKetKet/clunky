package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class GutendexSource implements BookSource {
    @Override
    public String getName() {
        return "Project Gutenberg (Gutendex)";
    }

    @Override
    public List<BookResult> search(String query) throws Exception {
        String urlStr = (query == null || query.trim().isEmpty())
                ? "https://gutendex.com/books/?sort=popular"
                : "https://gutendex.com/books/?search=" + URLEncoder.encode(query.trim(), "UTF-8");
        String json = SafeHttp.getString(urlStr);
        JSONObject root = new JSONObject(json);
        JSONArray results = root.optJSONArray("results");
        List<BookResult> out = new ArrayList<BookResult>();
        if (results == null) return out;
        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.optJSONObject(i);
            if (item == null) continue;
            int id = item.optInt("id");
            JSONObject formats = item.optJSONObject("formats");
            if (formats == null) continue;
            String epubUrl = formats.optString("application/epub+zip", "");
            if (id > 0 && (epubUrl.isEmpty() || epubUrl.contains("gutenberg.org"))) {
                epubUrl = "http://aleph.gutenberg.org/cache/epub/" + id + "/pg" + id + ".epub";
            }
            if (epubUrl.isEmpty()) continue;
            String coverUrl = formats.optString("image/jpeg", "");
            if (id > 0 && (coverUrl.isEmpty() || coverUrl.contains("gutenberg.org"))) {
                coverUrl = "http://aleph.gutenberg.org/cache/epub/" + id + "/pg" + id + ".cover.medium.jpg";
            }
            String title = item.optString("title", "Untitled");
            String author = "Unknown Author";
            JSONArray authors = item.optJSONArray("authors");
            if (authors != null && authors.length() > 0) {
                JSONObject a0 = authors.optJSONObject(0);
                if (a0 != null) {
                    author = a0.optString("name", author);
                }
            }
            int downloads = item.optInt("download_count", 0);
            out.add(new BookResult(String.valueOf(item.optInt("id")), getName(), title, author, epubUrl, coverUrl, "", downloads));
        }
        return out;
    }

    static String httpGet(String urlStr) throws Exception {
        return SafeHttp.getString(urlStr);
    }
}
