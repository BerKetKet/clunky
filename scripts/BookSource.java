package com.amazon.kindle.epub.catalog;

import java.util.List;

/**
 * Pluggable catalog source interface. Add new free ebook sources (OPDS, Standard Ebooks,
 * Internet Archive, local Calibre-Web) by implementing this interface and registering it in OpenCatalogActivity.
 */
public interface BookSource {
    String getName();
    List<BookResult> search(String query) throws Exception;
}
