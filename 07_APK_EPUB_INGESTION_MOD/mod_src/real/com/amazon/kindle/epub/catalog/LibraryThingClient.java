package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;

/**
 * Optional LibraryThing reviews fetcher using user-provided API token.
 * Fails gracefully if the endpoint is unavailable or restricted.
 */
public final class LibraryThingClient {
    private static final String TOKEN = "fb426ff52551f01a51b93022056d4b99";

    public static String fetchReviewsSnippet(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return "";
        }
        try {
            String url = "https://www.librarything.com/services/rest/1.1/?method=librarything.ck.getwork&isbn="
                    + URLEncoder.encode(isbn.trim(), "UTF-8") + "&apikey=" + TOKEN;
            String resp = GutendexSource.httpGet(url);
            if (resp != null && resp.contains("<ltml")) {
                return "LibraryThing Common Knowledge matched ISBN " + isbn;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}
