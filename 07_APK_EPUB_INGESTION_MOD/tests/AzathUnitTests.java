package com.amazon.kindle.epub.tests;

import com.amazon.kindle.epub.ingest.AzathLibrarySync;
import com.amazon.kindle.epub.ingest.EpubMetadataExtractor;
import com.amazon.kindle.epub.ingest.MetadataEnricher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure-JVM unit tests for Round 163-165 Android-free helpers:
 *   - AzathLibrarySync.planReconcile / isCacheEntryReferenced / sanitizeFilenameBase
 *   - MetadataEnricher.isAcceptableMatch
 *   - EpubMetadataExtractor.normalizeIsbn / normalizeAuthor / normalizePubDate
 */
public final class AzathUnitTests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) {
            passed++;
        } else {
            failed++;
            System.err.println("FAIL: " + name);
        }
    }

    private static void checkEq(String name, String expected, String actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            passed++;
        } else {
            failed++;
            System.err.println("FAIL: " + name + " | expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        testPlanReconcile();
        testCacheBoundaryMatching();
        testIsAcceptableMatch();
        testNormalizeIsbn();
        testNormalizeAuthor();
        testNormalizePubDate();
        testSanitizeFilenameBase();

        System.out.println("AzathUnitTests: passed=" + passed + " failed=" + failed + " total=" + (passed + failed));
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testPlanReconcile() {
        // 1. Healthy scan with 1 missing file out of 5 -> deletes expected row
        List<AzathLibrarySync.BookEntry> recs = new ArrayList<>();
        recs.add(new AzathLibrarySync.BookEntry("FOLIO_1", "/sdcard/kindle/b1.kfx", true));
        recs.add(new AzathLibrarySync.BookEntry("FOLIO_2", "/sdcard/kindle/b2.kfx", true));
        recs.add(new AzathLibrarySync.BookEntry("FOLIO_3", "/sdcard/kindle/b3.kfx", true));
        recs.add(new AzathLibrarySync.BookEntry("FOLIO_4", "/sdcard/kindle/b4.kfx", true));
        recs.add(new AzathLibrarySync.BookEntry("FOLIO_5", "/sdcard/kindle/b5.kfx", false));
        Set<String> paths = new HashSet<>(Arrays.asList(
                "/sdcard/kindle/b1.kfx", "/sdcard/kindle/b2.kfx", "/sdcard/kindle/b3.kfx", "/sdcard/kindle/b4.kfx"));
        Set<String> names = new HashSet<>(Arrays.asList("b1.kfx", "b2.kfx", "b3.kfx", "b4.kfx"));
        AzathLibrarySync.ScanResult healthyScan = new AzathLibrarySync.ScanResult(true, true, paths, names);

        AzathLibrarySync.ReconcilePlan p1 = AzathLibrarySync.planReconcile(
                healthyScan, recs, Arrays.asList("b1.epub.kfx", "orphan.epub.kfx"));
        check("planReconcile healthy skipDestructive=false", !p1.skipDestructive);
        check("planReconcile healthy circuitBreaker=false", !p1.circuitBreakerTripped);
        check("planReconcile healthy deletes FOLIO_5", p1.dbKeysToDelete.equals(Collections.singletonList("FOLIO_5")));
        check("planReconcile healthy prunes orphan.epub.kfx", p1.cacheNamesToPrune.equals(Collections.singletonList("orphan.epub.kfx")));

        // 2. Unhealthy /sdcard/kindle scan when DB has /sdcard/kindle rows -> aborts ALL deletes & cache prune
        AzathLibrarySync.ScanResult unhealthySd = new AzathLibrarySync.ScanResult(
                false, true, Collections.<String>emptySet(), Collections.<String>emptySet());
        AzathLibrarySync.ReconcilePlan p2 = AzathLibrarySync.planReconcile(
                unhealthySd, recs, Arrays.asList("b1.epub.kfx", "orphan.epub.kfx"));
        check("planReconcile unhealthy skipDestructive=true", p2.skipDestructive);
        check("planReconcile unhealthy 0 deletes", p2.dbKeysToDelete.isEmpty());
        check("planReconcile unhealthy 0 cache prunes", p2.cacheNamesToPrune.isEmpty());

        // 3. Circuit breaker: >50% missing -> aborts ALL deletes
        List<AzathLibrarySync.BookEntry> majorityMissing = Arrays.asList(
                new AzathLibrarySync.BookEntry("FOLIO_1", "/sdcard/kindle/b1.kfx", true),
                new AzathLibrarySync.BookEntry("FOLIO_2", "/sdcard/kindle/b2.kfx", false),
                new AzathLibrarySync.BookEntry("FOLIO_3", "/sdcard/kindle/b3.kfx", false));
        AzathLibrarySync.ReconcilePlan p3 = AzathLibrarySync.planReconcile(
                healthyScan, majorityMissing, Collections.<String>emptyList());
        check("planReconcile circuit breaker >50% tripped", p3.circuitBreakerTripped);
        check("planReconcile circuit breaker >50% 0 deletes", p3.dbKeysToDelete.isEmpty());

        // 4. Circuit breaker: >10 missing out of 30 -> aborts ALL deletes
        List<AzathLibrarySync.BookEntry> thirtyRows = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            thirtyRows.add(new AzathLibrarySync.BookEntry("FOLIO_" + i, "/sdcard/kindle/b" + i + ".kfx", i >= 11));
        }
        AzathLibrarySync.ReconcilePlan p4 = AzathLibrarySync.planReconcile(
                healthyScan, thirtyRows, Collections.<String>emptyList());
        check("planReconcile circuit breaker >10 tripped", p4.circuitBreakerTripped);
        check("planReconcile circuit breaker >10 0 deletes", p4.dbKeysToDelete.isEmpty());
    }

    private static void testCacheBoundaryMatching() {
        Set<String> active = new HashSet<>(Collections.singletonList("book.kfx"));
        check("cache exact .epub.kfx matches", AzathLibrarySync.isCacheEntryReferenced("book.epub.kfx", active));
        check("cache exact .epub.sdr matches", AzathLibrarySync.isCacheEntryReferenced("book.epub.sdr", active));
        check("cache exact .epub.kfx.cover.jpg matches", AzathLibrarySync.isCacheEntryReferenced("book.epub.kfx.cover.jpg", active));
        check("cache Book2 does NOT match Book", !AzathLibrarySync.isCacheEntryReferenced("book2.epub.kfx", active));
        check("cache Book_Extra does NOT match Book", !AzathLibrarySync.isCacheEntryReferenced("book_extra.epub.kfx", active));
    }

    private static void testIsAcceptableMatch() {
        // Exact ISBN match -> true even if title/author formatting differs
        check("match exact ISBN", MetadataEnricher.isAcceptableMatch(
                "Dune", "Frank Herbert", "978-0-441-17271-9",
                "Dune Messiah", "Other", "9780441172719"));

        // Matching title + matching author (case/punctuation/article differences) -> true
        check("match title+author normalized", MetadataEnricher.isAcceptableMatch(
                "The Great Gatsby", "F. Scott Fitzgerald", "",
                "Great Gatsby!", "Fitzgerald, Francis Scott", ""));

        // Matching title + mismatched author -> false (closes R164 T8)
        check("reject matching title + mismatched author", !MetadataEnricher.isAcceptableMatch(
                "The Great Gatsby", "Completely Wrong Author XYZ", "",
                "The Great Gatsby", "F. Scott Fitzgerald", ""));

        // Mismatched title + matching author -> false
        check("reject mismatched title + matching author", !MetadataEnricher.isAcceptableMatch(
                "Brave New World", "Aldous Huxley", "",
                "Island", "Aldous Huxley", ""));

        // Unknown Author requires strict title match
        check("Unknown Author exact title -> true", MetadataEnricher.isAcceptableMatch(
                "The Odyssey", "Unknown Author", "",
                "Odyssey", "Homer", ""));
        check("Unknown Author unrelated title -> false", !MetadataEnricher.isAcceptableMatch(
                "The Odyssey", "Unknown Author", "",
                "Iliad", "Homer", ""));
    }

    private static void testNormalizeIsbn() {
        checkEq("ISBN-13 valid", "9780441172719", EpubMetadataExtractor.normalizeIsbn("urn:isbn:978-0-441-17271-9"));
        checkEq("ISBN-10 X valid", "080442957X", EpubMetadataExtractor.normalizeIsbn("ISBN: 0-8044-2957-x"));
        checkEq("ISBN-13 bad checksum rejected", "", EpubMetadataExtractor.normalizeIsbn("9780441172710"));
        checkEq("UUID rejected", "", EpubMetadataExtractor.normalizeIsbn("urn:uuid:550e8400-e29b-41d4-a716-446655440000"));
        checkEq("Calibre ID rejected", "", EpubMetadataExtractor.normalizeIsbn("calibre:1234567890"));
        checkEq("URL rejected", "", EpubMetadataExtractor.normalizeIsbn("https://example.com/9780441172719"));
    }

    private static void testNormalizeAuthor() {
        checkEq("Last, First", "Aldous Huxley", EpubMetadataExtractor.normalizeAuthor("Huxley, Aldous"));
        checkEq("Last, First, Jr.", "John Smith Jr.", EpubMetadataExtractor.normalizeAuthor("Smith, John, Jr."));
        checkEq("Multi-author semicolon", "Aldous Huxley; George Orwell",
                EpubMetadataExtractor.normalizeAuthor("Huxley, Aldous; Orwell, George"));
        checkEq("Plain First Last untouched", "Peter Watts", EpubMetadataExtractor.normalizeAuthor("Peter Watts"));
    }

    private static void testNormalizePubDate() {
        checkEq("ISO timestamp T", "2014-06-06", EpubMetadataExtractor.normalizePubDate("2014-06-06T00:00:00Z"));
        checkEq("ISO timestamp space", "2000-07-15", EpubMetadataExtractor.normalizePubDate("2000-07-15 00:00:00+01:00"));
        checkEq("Year only", "1932", EpubMetadataExtractor.normalizePubDate("1932"));
    }

    private static void testSanitizeFilenameBase() {
        checkEq("Path traversal stripped", "etc_passwd_FOLIO_1234",
                AzathLibrarySync.sanitizeFilenameBase("../../../etc/passwd", "FOLIO_1234"));
        StringBuilder lb = new StringBuilder();
        for (int i = 0; i < 140; i++) lb.append('A');
        String longTitle = lb.toString();
        String sanitizedLong = AzathLibrarySync.sanitizeFilenameBase(longTitle, "FOLIO_ABCDEF123456");
        check("Long title capped <= 80 chars", sanitizedLong.length() <= 80 && sanitizedLong.endsWith("_FOLIO_ABCDEF123456"));
        checkEq("Emoji/blank fallback", "Book_FOLIO_1234",
                AzathLibrarySync.sanitizeFilenameBase("📚✨", "FOLIO_1234"));
    }
}
