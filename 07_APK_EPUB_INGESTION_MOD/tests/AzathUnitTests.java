package com.amazon.kindle.epub.tests;

import com.amazon.kindle.epub.CoverOverlayView;
import com.amazon.kindle.epub.EpubTranscoder;
import com.amazon.kindle.epub.catalog.SafeHttp;
import com.amazon.kindle.epub.ingest.AzathLibrarySync;
import com.amazon.kindle.epub.ingest.EpubMetadataExtractor;
import com.amazon.kindle.epub.ingest.ExternalEpubActivity;
import com.amazon.kindle.epub.ingest.FolioMetadataBackup;
import com.amazon.kindle.epub.ingest.FolioMetadataDb;
import com.amazon.kindle.epub.ingest.FolioWebBrowserDialog;
import com.amazon.kindle.epub.ingest.MetadataEnricher;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        testAuthorDeduplication();
        testNormalizePubDate();
        testSanitizeFilenameBase();
        testExtractBookKeyFromFilename();
        testKfxMagicAndPathGuards();
        testSecurityHelpers();
        testRound167Helpers();

        System.out.println("AzathUnitTests: passed=" + passed + " failed=" + failed + " total=" + (passed + failed));
        if (failed > 0) {
            System.err.println("TOTAL FAILED: " + failed);
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

        // 5. Task 3: missing folio_library (!exists) treated as healthy-empty allowing epub_cache prune
        java.io.File nonExistentPriv = new java.io.File("/tmp/azath_unit_nonexistent_folio_library_dir_12345");
        check("missing folio_library treated as healthy-empty",
                AzathLibrarySync.isPrivateLibraryDirHealthy(nonExistentPriv));
        AzathLibrarySync.ScanResult healthyWithEmptyPriv = new AzathLibrarySync.ScanResult(
                true,
                AzathLibrarySync.isPrivateLibraryDirHealthy(nonExistentPriv),
                paths,
                names);
        AzathLibrarySync.ReconcilePlan p5 = AzathLibrarySync.planReconcile(
                healthyWithEmptyPriv,
                recs.subList(0, 4),
                Arrays.asList("b1.epub.kfx", "stale_orphan.epub.kfx"));
        check("missing folio_library still allows epub_cache prune",
                p5.cacheNamesToPrune.equals(Collections.singletonList("stale_orphan.epub.kfx")));

        // 6. Task 3: force=true bypasses circuit breaker while force=false still aborts
        AzathLibrarySync.ReconcilePlan p6NoForce = AzathLibrarySync.planReconcile(
                healthyScan, thirtyRows, Arrays.asList("orphan_cb.epub.kfx"), false);
        check("force=false circuit breaker aborts deletes", p6NoForce.circuitBreakerTripped && p6NoForce.dbKeysToDelete.isEmpty());
        check("force=false circuit breaker skips cache prune", p6NoForce.cacheNamesToPrune.isEmpty());
        AzathLibrarySync.ReconcilePlan p6Force = AzathLibrarySync.planReconcile(
                healthyScan, thirtyRows, Arrays.asList("orphan_cb.epub.kfx"), true);
        check("force=true bypasses circuit breaker", !p6Force.circuitBreakerTripped && p6Force.dbKeysToDelete.size() == 11);
        check("force=true allows cache prune", p6Force.cacheNamesToPrune.equals(Collections.singletonList("orphan_cb.epub.kfx")));
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

        // Task 5: Unknown/blank author rejected single-doc without ISBN; requires ISBN OR limit=3 agreeing first author
        check("Unknown Author without ISBN single-doc rejected", !MetadataEnricher.isAcceptableMatch(
                "The Odyssey", "Unknown Author", "",
                "Odyssey", "Homer", ""));
        check("Blank Author with exact ISBN -> true", MetadataEnricher.isAcceptableMatch(
                "The Odyssey", "", "9780140268867",
                "Odyssey", "Homer", "978-0-14-026886-7"));
        check("Unknown Author limit=3 all agree first author -> accepted",
                MetadataEnricher.selectAcceptedCandidateIndex(
                        "The Odyssey", "Unknown Author", "",
                        Arrays.asList("Odyssey", "The Odyssey", "Odyssey"),
                        Arrays.asList("Homer", "Homer", "Homer"),
                        Arrays.asList("", "", "")) == 0);
        check("Unknown Author limit=3 disagreeing authors -> rejected",
                MetadataEnricher.selectAcceptedCandidateIndex(
                        "The Odyssey", "Unknown Author", "",
                        Arrays.asList("Odyssey", "2001: A Space Odyssey", "Odyssey"),
                        Arrays.asList("Homer", "Arthur C. Clarke", "Homer"),
                        Arrays.asList("", "", "")) == -1);
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

    private static void testAuthorDeduplication() {
        // Exact case-insensitive equality per '; '-separated author; never substring .contains()
        checkEq("Do not drop 'Li' when 'Alice' is present", "Alice; Li",
                EpubMetadataExtractor.appendDistinctAuthor("Alice", "Li"));
        checkEq("Deduplicate 'li' when 'Li; Alice' already present", "Li; Alice",
                EpubMetadataExtractor.appendDistinctAuthor("Li; Alice", "li"));
        checkEq("normalizeAuthor deduplicates duplicate semicolon entries case-insensitively",
                "Aldous Huxley; Li; Alice",
                EpubMetadataExtractor.normalizeAuthor("Huxley, Aldous; Li; Alice; li; huxley, aldous"));

        StringBuilder descSb = new StringBuilder();
        for (int i = 0; i < 4500; i++) descSb.append('x');
        check("truncateDescription caps at 4000 chars",
                EpubMetadataExtractor.truncateDescription(descSb.toString()).length() == 4000);
    }

    private static void testNormalizePubDate() {
        checkEq("Human readable date extracts year", "2010", EpubMetadataExtractor.normalizePubDate("July 5, 2010"));
        checkEq("ISO timestamp T", "2014-06-06", EpubMetadataExtractor.normalizePubDate("2014-06-06T00:00:00Z"));
        checkEq("ISO timestamp space", "2000-07-15", EpubMetadataExtractor.normalizePubDate("2000-07-15 00:00:00+01:00"));
        checkEq("Year only", "2010", EpubMetadataExtractor.normalizePubDate("2010"));
        checkEq("Historical year", "1932", EpubMetadataExtractor.normalizePubDate("1932"));
        checkEq("Garbage string returns empty", "", EpubMetadataExtractor.normalizePubDate("unknown / undated"));
        checkEq("Out-of-range year returns empty", "", EpubMetadataExtractor.normalizePubDate("99999"));
        checkEq("Time-only ISO garbage returns empty", "", EpubMetadataExtractor.normalizePubDate("T12:00:00Z"));
        checkEq("Blank returns empty", "", EpubMetadataExtractor.normalizePubDate("   "));
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

    private static void testExtractBookKeyFromFilename() {
        checkEq("Standard _FOLIO_<hex>.kfx", "FOLIO_DB72217519E6",
                FolioMetadataDb.extractBookKeyFromFilename("Blindsight_FOLIO_DB72217519E6.kfx"));
        checkEq("Multiple underscores before _FOLIO_", "FOLIO_16116B0F7724",
                FolioMetadataDb.extractBookKeyFromFilename("A_Spy_in_the_House_of_Love_FOLIO_16116B0F7724.kfx"));
        checkEq("Missing _FOLIO_ suffix -> null", null,
                FolioMetadataDb.extractBookKeyFromFilename("hesiod.kfx"));
        checkEq("Malformed non-hex suffix -> null", null,
                FolioMetadataDb.extractBookKeyFromFilename("Bad_Book_FOLIO_ZZZZZZZZZZZZ.kfx"));
        checkEq("Malformed short suffix -> null", null,
                FolioMetadataDb.extractBookKeyFromFilename("Bad_Book_FOLIO_123.kfx"));
        checkEq("Malformed trailing token after hex -> null", null,
                FolioMetadataDb.extractBookKeyFromFilename("Bad_FOLIO_DB72217519E6_copy.kfx"));
        checkEq("Null filename -> null", null,
                FolioMetadataDb.extractBookKeyFromFilename(null));
    }

    private static void testKfxMagicAndPathGuards() {
        try {
            File tmpValid = File.createTempFile("azath_kfx_valid_", ".kfx.part");
            try (FileOutputStream fos = new FileOutputStream(tmpValid)) {
                byte[] b = new byte[256];
                b[0] = 'C'; b[1] = 'O'; b[2] = 'N'; b[3] = 'T';
                fos.write(b);
            }
            check("isValidKfxPart accepts 256B CONT header", ExternalEpubActivity.isValidKfxPart(tmpValid));

            File tmpBadMagic = File.createTempFile("azath_kfx_bad_", ".kfx.part");
            try (FileOutputStream fos = new FileOutputStream(tmpBadMagic)) {
                byte[] b = new byte[256];
                b[0] = (byte) 0xFF; b[1] = (byte) 0xFF; b[2] = 'X'; b[3] = 'Y';
                fos.write(b);
            }
            check("isValidKfxPart rejects non-CONT header", !ExternalEpubActivity.isValidKfxPart(tmpBadMagic));

            File tmpTooSmall = File.createTempFile("azath_kfx_tiny_", ".kfx.part");
            try (FileOutputStream fos = new FileOutputStream(tmpTooSmall)) {
                fos.write(new byte[] { 'C', 'O', 'N', 'T', 1, 2 });
            }
            check("isValidKfxPart rejects <128B CONT header", !ExternalEpubActivity.isValidKfxPart(tmpTooSmall));

            tmpValid.delete();
            tmpBadMagic.delete();
            tmpTooSmall.delete();
        } catch (Exception e) {
            check("KFX temp file magic test threw unexpected exception: " + e, false);
        }

        check("Private path inside /data/data/<pkg> blocked",
                ExternalEpubActivity.isPathInsideOrEqual("/data/data/com.azath.reader/databases/kindle_library.db", "/data/data/com.azath.reader"));
        check("Private path exact root blocked",
                ExternalEpubActivity.isPathInsideOrEqual("/data/user/0/com.azath.reader", "/data/user/0/com.azath.reader"));
        check("Sibling pkg prefix NOT falsely blocked",
                !ExternalEpubActivity.isPathInsideOrEqual("/data/user/0/com.azath.reader2/book.epub", "/data/user/0/com.azath.reader"));
    }

    private static void testSecurityHelpers() {
        // Content-Disposition filename sanitization
        checkEq("Content-Disposition traversal stripped", "passwd.epub",
                FolioWebBrowserDialog.sanitizeDownloadFilename("attachment; filename=\"../../etc/passwd\"", null));
        checkEq("Content-Disposition control/NUL stripped", "My_Book_1.epub",
                FolioWebBrowserDialog.sanitizeDownloadFilename("attachment; filename=\"My Book\u0000\n 1.epub\"", null));
        checkEq("Content-Disposition UTF-8 filename* parsed", "Blindsight.epub",
                FolioWebBrowserDialog.sanitizeDownloadFilename("attachment; filename*=UTF-8''/evil/dir/Blindsight.epub", null));
        StringBuilder longCd = new StringBuilder("attachment; filename=\"");
        for (int i = 0; i < 140; i++) longCd.append('Z');
        longCd.append(".epub\"");
        String sanLongCd = FolioWebBrowserDialog.sanitizeDownloadFilename(longCd.toString(), null);
        check("Content-Disposition capped <= 80 chars and ends .epub",
                sanLongCd.length() <= 80 && sanLongCd.endsWith(".epub"));

        // ZIP slip entry guard
        check("Normal EPUB entry safe", EpubTranscoder.isSafeZipEntryName("OEBPS/Text/chapter_01.xhtml"));
        check("Zip-slip .. segment rejected", !EpubTranscoder.isSafeZipEntryName("OEBPS/../../etc/passwd"));
        check("Percent-encoded %2e%2e rejected", !EpubTranscoder.isSafeZipEntryName("%2e%2e/secret"));
        check("NUL char in ZIP entry rejected", !EpubTranscoder.isSafeZipEntryName("OEBPS/ch1.xhtml\u0000.png"));

        // Backup validation helpers
        check("Valid /sdcard/kindle backup kfx path accepted",
                FolioMetadataBackup.isValidBackupBookPath("/sdcard/kindle/Blindsight_FOLIO_DB72217519E6.kfx"));
        check("Backup path with .. rejected",
                !FolioMetadataBackup.isValidBackupBookPath("/sdcard/kindle/../../etc/evil.kfx"));
        check("Backup path targeting databases rejected",
                !FolioMetadataBackup.isValidBackupBookPath("/data/data/com.azath.reader/databases/kindle_library.db"));
        check("Backup non-.kfx path rejected",
                !FolioMetadataBackup.isValidBackupBookPath("/sdcard/kindle/note.txt"));
        check("Backup goal clamped <= 1000", FolioMetadataBackup.clampReadingGoal(50000) == 1000);
        check("Backup rating clamped <= 5", FolioMetadataBackup.clampStarRating(99) == 5);
    }

    private static void testRound167Helpers() {
        // Task 4: parsePubDateToEpochMs + toSortableText
        Long dIso = FolioMetadataDb.parsePubDateToEpochMs("2022-03-15");
        check("parsePubDateToEpochMs YYYY-MM-DD exact UTC", dIso != null && dIso == 1647302400000L);
        Long dTs = FolioMetadataDb.parsePubDateToEpochMs("2021-10-21T14:30:00Z");
        check("parsePubDateToEpochMs ISO timestamp UTC day", dTs != null && dTs == 1634774400000L);
        Long dYr = FolioMetadataDb.parsePubDateToEpochMs("1981");
        check("parsePubDateToEpochMs YYYY exact UTC",
                dYr != null && dYr == java.time.Instant.parse("1981-01-01T00:00:00Z").toEpochMilli());
        check("parsePubDateToEpochMs null -> null", FolioMetadataDb.parsePubDateToEpochMs(null) == null);
        check("parsePubDateToEpochMs empty -> null", FolioMetadataDb.parsePubDateToEpochMs("   ") == null);
        check("parsePubDateToEpochMs text -> null", FolioMetadataDb.parsePubDateToEpochMs("19th century") == null);
        check("parsePubDateToEpochMs bad month -> null", FolioMetadataDb.parsePubDateToEpochMs("2022-13-40") == null);
        check("parsePubDateToEpochMs out-of-range year -> null", FolioMetadataDb.parsePubDateToEpochMs("3000") == null);
        checkEq("toSortableText trims & lowercases", "george eliot", FolioMetadataDb.toSortableText("  George Eliot "));
        checkEq("toSortableText null -> empty", "", FolioMetadataDb.toSortableText(null));

        // Task 2: Attempt-isolated .part naming + shared deadline math
        File baseKfx = new File("/sdcard/kindle/Book_FOLIO_1234.kfx");
        File pInproc = ExternalEpubActivity.buildAttemptPartFile(baseKfx, "inproc");
        File pNative1 = ExternalEpubActivity.buildAttemptPartFile(baseKfx, "native1");
        check("Attempt .part files are unique per attempt", !pInproc.getName().equals(pNative1.getName()));
        check("Attempt inproc .part name", pInproc.getName().equals("Book_FOLIO_1234.kfx.part.inproc"));
        check("isPartArtifactName recognizes attempt .part.<tag>", AzathLibrarySync.isPartArtifactName("book.kfx.part.inproc"));
        check("isPartArtifactName recognizes .part.cover.jpg", AzathLibrarySync.isPartArtifactName("book.kfx.part.cover.jpg"));
        check("isPartArtifactName rejects final .kfx", !AzathLibrarySync.isPartArtifactName("book.kfx"));

        long deadline = ExternalEpubActivity.computeDeadlineNanos(1_000_000_000L, 10L);
        check("computeDeadlineNanos adds timeout ns", deadline == 11_000_000_000L);
        check("remainingTimeoutMs mid-budget", ExternalEpubActivity.remainingTimeoutMs(6_000_000_000L, deadline) == 5000L);
        check("remainingTimeoutMs expired -> 0", ExternalEpubActivity.remainingTimeoutMs(12_000_000_000L, deadline) == 0L);
        check("shouldRunFallback true when primary failed cleanly within budget",
                ExternalEpubActivity.shouldRunFallback(false, false, false, 6_000_000_000L, deadline));
        check("shouldRunFallback false when primary succeeded",
                !ExternalEpubActivity.shouldRunFallback(true, false, false, 6_000_000_000L, deadline));
        check("shouldRunFallback false on primary timeout",
                !ExternalEpubActivity.shouldRunFallback(false, true, false, 6_000_000_000L, deadline));
        check("shouldRunFallback false on cancel",
                !ExternalEpubActivity.shouldRunFallback(false, false, true, 6_000_000_000L, deadline));
        check("shouldRunFallback false after deadline",
                !ExternalEpubActivity.shouldRunFallback(false, false, false, 12_000_000_000L, deadline));

        // Task 3: Cancelable lock wait loop
        ReentrantLock freeLock = new ReentrantLock();
        AtomicInteger freeWaits = new AtomicInteger(0);
        boolean gotFree = ExternalEpubActivity.acquireLockCancelable(
                freeLock, new AtomicBoolean(false), freeWaits::incrementAndGet, 20L);
        check("acquireLockCancelable immediate acquire when unlocked", gotFree && freeWaits.get() == 0);
        if (gotFree && freeLock.isHeldByCurrentThread()) freeLock.unlock();

        final ReentrantLock contendedLock = new ReentrantLock();
        final CountDownLatch lockedLatch = new CountDownLatch(1);
        final CountDownLatch releaseWaiter = new CountDownLatch(1);
        Thread holder1 = new Thread(() -> {
            contendedLock.lock();
            lockedLatch.countDown();
            try { releaseWaiter.await(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
            contendedLock.unlock();
        });
        holder1.start();
        try {
            lockedLatch.await(2, TimeUnit.SECONDS);
            final AtomicBoolean cancelFlag = new AtomicBoolean(false);
            final AtomicInteger waitCalls = new AtomicInteger(0);
            boolean acquiredWhileCancelled = ExternalEpubActivity.acquireLockCancelable(
                    contendedLock, cancelFlag, () -> {
                        waitCalls.incrementAndGet();
                        cancelFlag.set(true);
                    }, 20L);
            check("acquireLockCancelable aborts on cancel while waiting",
                    !acquiredWhileCancelled && waitCalls.get() >= 1 && !contendedLock.isHeldByCurrentThread());
        } catch (InterruptedException ie) {
            check("Contended cancel test interrupted", false);
        } finally {
            releaseWaiter.countDown();
            try { holder1.join(2000L); } catch (InterruptedException ignored) {}
        }

        final ReentrantLock releaseLock = new ReentrantLock();
        final CountDownLatch lockedLatch2 = new CountDownLatch(1);
        final CountDownLatch waitingSeen = new CountDownLatch(1);
        Thread holder2 = new Thread(() -> {
            releaseLock.lock();
            lockedLatch2.countDown();
            try { waitingSeen.await(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
            try { Thread.sleep(30L); } catch (InterruptedException ignored) {}
            releaseLock.unlock();
        });
        holder2.start();
        try {
            lockedLatch2.await(2, TimeUnit.SECONDS);
            final AtomicInteger waits2 = new AtomicInteger(0);
            boolean acquiredAfterRelease = ExternalEpubActivity.acquireLockCancelable(
                    releaseLock, new AtomicBoolean(false), () -> {
                        waits2.incrementAndGet();
                        waitingSeen.countDown();
                    }, 20L);
            check("acquireLockCancelable acquires once contention clears", acquiredAfterRelease && waits2.get() >= 1);
            if (acquiredAfterRelease && releaseLock.isHeldByCurrentThread()) releaseLock.unlock();
        } catch (InterruptedException ie) {
            check("Contended release test interrupted", false);
        } finally {
            waitingSeen.countDown();
            try { holder2.join(2000L); } catch (InterruptedException ignored) {}
        }

        // Task 1: Raised caps + streaming spill EPUB transcode (>256KB binary entry)
        check("EpubTranscoder entry cap is 128MB", EpubTranscoder.MAX_ZIP_ENTRY_BYTES == 128L * 1024L * 1024L);
        check("EpubTranscoder total cap is 512MB", EpubTranscoder.MAX_ZIP_TOTAL_BYTES == 512L * 1024L * 1024L);
        try {
            File tmpEpub = File.createTempFile("azath_spill_", ".epub");
            File tmpOut = File.createTempFile("azath_spill_", ".mobi");
            tmpOut.delete();
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpEpub))) {
                zos.putNextEntry(new ZipEntry("mimetype"));
                zos.write("application/epub+zip".getBytes(StandardCharsets.US_ASCII));
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry("META-INF/container.xml"));
                zos.write(("<?xml version=\"1.0\"?><container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">"
                        + "<rootfiles><rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/></rootfiles></container>").getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry("OEBPS/content.opf"));
                zos.write(("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                        + "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"2.0\" unique-identifier=\"uid\">"
                        + "<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                        + "<dc:title>Spill Test Book</dc:title><dc:creator>Test</dc:creator>"
                        + "<meta name=\"cover\" content=\"cover-img\"/></metadata>"
                        + "<manifest>"
                        + "<item id=\"ch1\" href=\"ch1.xhtml\" media-type=\"application/xhtml+xml\"/>"
                        + "<item id=\"cover-img\" href=\"big.png\" media-type=\"image/png\"/>"
                        + "</manifest><spine><itemref idref=\"ch1\"/></spine></package>").getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry("OEBPS/ch1.xhtml"));
                zos.write(("<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><h1>Chapter 1</h1><p>Spill streaming test prose.</p></body></html>").getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry("OEBPS/big.png"));
                byte[] bigImg = new byte[400 * 1024];
                Arrays.fill(bigImg, (byte) 0x7A);
                bigImg[0] = (byte) 0x89; bigImg[1] = 'P'; bigImg[2] = 'N'; bigImg[3] = 'G';
                zos.write(bigImg);
                zos.closeEntry();
            }
            EpubTranscoder.transcode(tmpEpub, tmpOut, true);
            byte[] magic = new byte[8];
            try (RandomAccessFile raf = new RandomAccessFile(tmpOut, "r")) {
                raf.seek(60);
                raf.readFully(magic);
            }
            check("Streaming spill EPUB produced valid BOOKMOBI",
                    tmpOut.exists() && tmpOut.length() > 100_000 && "BOOKMOBI".equals(new String(magic, StandardCharsets.US_ASCII)));
            tmpEpub.delete();
            tmpOut.delete();
        } catch (Throwable t) {
            check("Streaming spill EPUB transcode threw: " + t, false);
        }

        // Task 9.1: CoverOverlayView 2048px sample sizing
        check("Cover sample 1200x800 -> 1", CoverOverlayView.computeSampleSize(1200, 800, 2048) == 1);
        check("Cover sample 2048x1024 -> 1", CoverOverlayView.computeSampleSize(2048, 1024, 2048) == 1);
        check("Cover sample 4096x2000 -> 2", CoverOverlayView.computeSampleSize(4096, 2000, 2048) == 2);
        check("Cover sample 5000x3000 -> 4", CoverOverlayView.computeSampleSize(5000, 3000, 2048) == 4);

        // Task 8b + Task 9.2: MetadataEnricher 429/503 Retry-After + SafeHttp 2MB cap & close wrapper
        check("Retry-After header 2s -> 2000ms", MetadataEnricher.parseRetryAfterMs("2", 0) == 2000L);
        check("Retry-After header clamped 999s -> 30000ms", MetadataEnricher.parseRetryAfterMs("999", 0) == 30000L);
        check("Retry-After non-numeric falls back to 1000<<1", MetadataEnricher.parseRetryAfterMs("bad", 1) == 2000L);
        check("SafeHttp MAX_GET_BYTES == 2MB", SafeHttp.MAX_GET_BYTES == 2 * 1024 * 1024);

        try {
            HttpServer srv = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            final AtomicInteger hits429 = new AtomicInteger(0);
            final AtomicInteger hits503 = new AtomicInteger(0);
            srv.createContext("/r429", ex -> {
                if (hits429.incrementAndGet() == 1) {
                    ex.getResponseHeaders().add("Retry-After", "1");
                    byte[] b = "rate-limit".getBytes(StandardCharsets.UTF_8);
                    ex.sendResponseHeaders(429, b.length);
                    try (OutputStream os = ex.getResponseBody()) { os.write(b); }
                } else {
                    byte[] b = "{\"docs\":[{\"key\":\"/works/OL1W\"}]}".getBytes(StandardCharsets.UTF_8);
                    ex.sendResponseHeaders(200, b.length);
                    try (OutputStream os = ex.getResponseBody()) { os.write(b); }
                }
            });
            srv.createContext("/r503", ex -> {
                hits503.incrementAndGet();
                byte[] b = "unavailable".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(503, b.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(b); }
            });
            srv.createContext("/ok", ex -> {
                byte[] b = new byte[1024];
                Arrays.fill(b, (byte) 'K');
                ex.sendResponseHeaders(200, b.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(b); }
            });
            srv.createContext("/over2mb", ex -> {
                byte[] b = new byte[SafeHttp.MAX_GET_BYTES + 1024];
                ex.sendResponseHeaders(200, b.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(b); }
            });
            srv.start();
            try {
                int port = srv.getAddress().getPort();
                String r1 = MetadataEnricher.queryDocsRawJsonUrl("http://127.0.0.1:" + port + "/r429", "AzathTest/1.0");
                check("HTTP 429 + Retry-After retries and succeeds", r1 != null && r1.contains("OL1W") && hits429.get() == 2);
                String r2 = MetadataEnricher.queryDocsRawJsonUrl("http://127.0.0.1:" + port + "/r503", "AzathTest/1.0");
                check("HTTP 503 x3 returns null cleanly", r2 == null && hits503.get() == 3);

                byte[] small = SafeHttp.getBytes("http://127.0.0.1:" + port + "/ok");
                check("SafeHttp.getBytes <= 2MB returns full payload", small.length == 1024);
                boolean blocked = false;
                try {
                    SafeHttp.getBytes("http://127.0.0.1:" + port + "/over2mb");
                } catch (java.io.IOException ioe) {
                    blocked = ioe.getMessage() != null && ioe.getMessage().contains("exceeds");
                }
                check("SafeHttp.getBytes > 2MB throws IOException", blocked);
                try (InputStream is = SafeHttp.openStream("http://127.0.0.1:" + port + "/ok")) {
                    check("SafeHttp.openStream returns FilterInputStream wrapper",
                            is.getClass().getName().startsWith(SafeHttp.class.getName()) && is.read() == 'K');
                }
            } finally {
                srv.stop(0);
            }
        } catch (Throwable t) {
            check("Mock HttpServer suite threw: " + t, false);
        }
    }
}
