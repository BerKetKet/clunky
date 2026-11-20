package com.amazon.kindle.epub.catalog;

public final class BookResult {
    public final String id;
    public final String sourceName;
    public final String title;
    public final String author;
    public final String epubUrl;
    public final String coverUrl;
    public final String isbn;
    public final int downloadCount;

    public BookResult(String id, String sourceName, String title, String author, String epubUrl, String coverUrl, String isbn, int downloadCount) {
        this.id = id;
        this.sourceName = sourceName;
        this.title = title;
        this.author = author;
        this.epubUrl = epubUrl;
        this.coverUrl = coverUrl;
        this.isbn = isbn;
        this.downloadCount = downloadCount;
    }
}
