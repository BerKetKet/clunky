package com.amazon.kindle.epub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Pure-Java on-device EPUB-to-PalmDB-MOBI (BOOKMOBI .prc) container transcoder
 * targeting libKRF.so's EBookPDB::get_book_info (0x3b7f10) and Record 0 / EXTH parser (0x3b9128).
 */
public final class EpubTranscoder {
    private static final String CACHE_DIR = "/data/data/com.amazon.kindle/files/epub_cache";

    public static String ensureKindlePath(String path) {
        if (path == null) {
            return null;
        }
        return ensureKindleFile(new File(path)).getAbsolutePath();
    }

    public static File ensureKindleFile(File file) {
        if (!isEpubFile(file)) {
            return file;
        }
        try {
            File cacheDir = new File(CACHE_DIR);
            cacheDir.mkdirs();
            File outFile = new File(cacheDir, file.getName() + ".prc");
            transcodeToPrc(file, outFile);
            if (outFile.exists()) {
                return outFile;
            }
        } catch (Throwable ignored) {
        }
        return file;
    }

    public static boolean isEpubFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.getName().toLowerCase(Locale.US).endsWith(".epub")) {
            return true;
        }
        byte[] magic = new byte[4];
        try {
            FileInputStream fis = new FileInputStream(file);
            int read = fis.read(magic);
            fis.close();
            return read == 4 && magic[0] == 0x50 && magic[1] == 0x4B;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void transcodeToPrc(File inFile, File outFile) {
        if (outFile.exists()) {
            return;
        }
        try {
            String title = inFile.getName();
            String author = "Local EPUB Author";
            ByteArrayOutputStream htmlBaos = new ByteArrayOutputStream();
            byte[] header = "<html><head></head><body>".getBytes("UTF-8");
            htmlBaos.write(header, 0, header.length);

            ZipInputStream zis = new ZipInputStream(new FileInputStream(inFile));
            byte[] buf = new byte[4096];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase(Locale.US);
                if (name.endsWith(".opf") || name.endsWith(".xhtml") || name.endsWith(".html") || name.endsWith(".htm")) {
                    ByteArrayOutputStream entryBaos = new ByteArrayOutputStream();
                    int n;
                    while ((n = zis.read(buf)) > 0) {
                        entryBaos.write(buf, 0, n);
                    }
                    byte[] entryBytes = entryBaos.toByteArray();
                    if (name.endsWith(".opf")) {
                        String xml = new String(entryBytes, "UTF-8");
                        title = extractXmlTag(xml, "<dc:title", "</dc:title>", title);
                        author = extractXmlTag(xml, "<dc:creator", "</dc:creator>", author);
                    } else {
                        byte[] bodyBytes = extractBodyBytes(entryBytes);
                        htmlBaos.write(bodyBytes, 0, bodyBytes.length);
                        byte[] pb = "<mbp:pagebreak/>".getBytes("UTF-8");
                        htmlBaos.write(pb, 0, pb.length);
                    }
                }
            }
            zis.close();

            byte[] footer = "</body></html>".getBytes("UTF-8");
            htmlBaos.write(footer, 0, footer.length);
            byte[] htmlBytes = htmlBaos.toByteArray();

            String asin = String.format(Locale.US, "B0%08X", Integer.valueOf(inFile.getName().hashCode()));
            byte[] prcBytes = buildPrcBytes(title, author, asin, htmlBytes);
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(prcBytes);
            fos.close();
        } catch (Throwable ignored) {
        }
    }

    public static byte[] extractBodyBytes(byte[] rawBytes) {
        try {
            String s = new String(rawBytes, "UTF-8");
            int bIdx = s.indexOf("<body");
            if (bIdx < 0) return rawBytes;
            int gtIdx = s.indexOf(">", bIdx);
            if (gtIdx < 0) return rawBytes;
            int start = gtIdx + 1;
            int end = s.indexOf("</body>", start);
            String body = (end >= 0) ? s.substring(start, end) : s.substring(start);
            return body.getBytes("UTF-8");
        } catch (Throwable ignored) {
            return rawBytes;
        }
    }

    public static String extractXmlTag(String xml, String openTag, String closeTag, String defaultVal) {
        try {
            int oIdx = xml.indexOf(openTag);
            if (oIdx < 0) return defaultVal;
            int gtIdx = xml.indexOf(">", oIdx);
            if (gtIdx < 0) return defaultVal;
            int start = gtIdx + 1;
            int end = xml.indexOf(closeTag, start);
            if (end < 0) return defaultVal;
            return xml.substring(start, end).trim();
        } catch (Throwable ignored) {
            return defaultVal;
        }
    }

    public static byte[] toFixed32Bytes(String s) {
        try {
            byte[] raw = (s + "                                ").getBytes("UTF-8");
            byte[] out = new byte[32];
            System.arraycopy(raw, 0, out, 0, 32);
            return out;
        } catch (Throwable ignored) {
            return new byte[32];
        }
    }

    public static byte[] buildPrcBytes(String title, String author, String asin, byte[] htmlBytes) {
        int htmlLen = htmlBytes.length;
        int numTextRecs = (htmlLen + 4095) >> 12;
        if (numTextRecs <= 0) numTextRecs = 1;
        int numRecords = numTextRecs + 2;
        int rec0Off = 80 + (numRecords << 3);
        int rec1Off = rec0Off + 448;
        byte[] prc = new byte[rec1Off + htmlLen + 4];

        byte[] title32 = toFixed32Bytes(title);
        System.arraycopy(title32, 0, prc, 0, 31);
        putIntBE(prc, 60, 0x424F4F4B); // "BOOK"
        putIntBE(prc, 64, 0x4D4F4249); // "MOBI"
        putShortBE(prc, 76, numRecords);
        putIntBE(prc, 78, rec0Off);

        for (int i = 0; i < numTextRecs; i++) {
            putIntBE(prc, 86 + (i << 3), rec1Off + (i << 12));
        }
        putIntBE(prc, 86 + (numTextRecs << 3), rec1Off + htmlLen);

        // Record 0: PalmDOC (16B) + MOBI (232B) + EXTH (144B) + FullName (32B) + Pad (24B) = 448B
        putShortBE(prc, rec0Off, 1);
        putIntBE(prc, rec0Off + 4, htmlLen);
        putShortBE(prc, rec0Off + 8, numTextRecs);
        putShortBE(prc, rec0Off + 10, 4096);

        putIntBE(prc, rec0Off + 16, 0x4D4F4249);
        putIntBE(prc, rec0Off + 20, 232);
        putIntBE(prc, rec0Off + 24, 2);
        putIntBE(prc, rec0Off + 28, 65001);
        putIntBE(prc, rec0Off + 32, 0x12345678);
        putIntBE(prc, rec0Off + 36, 6);
        for (int off = 40; off < 80; off += 4) {
            putIntBE(prc, rec0Off + off, -1);
        }
        putIntBE(prc, rec0Off + 80, numTextRecs + 1);
        putIntBE(prc, rec0Off + 84, 392);
        putIntBE(prc, rec0Off + 88, 32);
        putIntBE(prc, rec0Off + 92, 9);
        putIntBE(prc, rec0Off + 104, 6);
        putIntBE(prc, rec0Off + 108, numTextRecs + 1);
        putIntBE(prc, rec0Off + 128, 0x50);
        for (int off = 164; off < 192; off += 4) {
            putIntBE(prc, rec0Off + off, -1);
        }
        putShortBE(prc, rec0Off + 192, 1);
        putShortBE(prc, rec0Off + 194, numTextRecs);
        putIntBE(prc, rec0Off + 196, 1);
        putIntBE(prc, rec0Off + 200, -1);
        putIntBE(prc, rec0Off + 204, 1);
        putIntBE(prc, rec0Off + 208, -1);
        putIntBE(prc, rec0Off + 212, 1);

        // EXTH Header at rec0Off + 248 (144 bytes)
        putIntBE(prc, rec0Off + 248, 0x45585448);
        putIntBE(prc, rec0Off + 252, 144);
        putIntBE(prc, rec0Off + 256, 5);

        putIntBE(prc, rec0Off + 260, 100);
        putIntBE(prc, rec0Off + 264, 40);
        byte[] author32 = toFixed32Bytes(author);
        System.arraycopy(author32, 0, prc, rec0Off + 268, 32);

        try {
            putIntBE(prc, rec0Off + 300, 106);
            putIntBE(prc, rec0Off + 304, 18);
            System.arraycopy("2026-01-01".getBytes("UTF-8"), 0, prc, rec0Off + 308, 10);

            putIntBE(prc, rec0Off + 318, 113);
            putIntBE(prc, rec0Off + 322, 18);
            System.arraycopy(asin.getBytes("UTF-8"), 0, prc, rec0Off + 326, 10);

            putIntBE(prc, rec0Off + 336, 501);
            putIntBE(prc, rec0Off + 340, 12);
            System.arraycopy("EBOK".getBytes("UTF-8"), 0, prc, rec0Off + 344, 4);
        } catch (Throwable ignored) {
        }

        putIntBE(prc, rec0Off + 348, 503);
        putIntBE(prc, rec0Off + 352, 40);
        System.arraycopy(title32, 0, prc, rec0Off + 356, 32);

        System.arraycopy(title32, 0, prc, rec0Off + 392, 32);
        System.arraycopy(htmlBytes, 0, prc, rec1Off, htmlLen);
        putIntBE(prc, rec1Off + htmlLen, 0xE98E0D0A);
        return prc;
    }

    public static void putIntBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 24);
        b[off + 1] = (byte) (val >> 16);
        b[off + 2] = (byte) (val >> 8);
        b[off + 3] = (byte) val;
    }

    public static void putShortBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 8);
        b[off + 1] = (byte) val;
    }
}
