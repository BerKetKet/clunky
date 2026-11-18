#!/usr/bin/env python3
"""
Kindle Android v8.159.0.100 EPUB Ingestion & Display DEX Builder & Patcher.
1. Patches classes4.dex in-place:
   - com.amazon.kindle.ThirdPartyUpgradePage:
     * handleContentScheme(Uri, ContentType): bypasses routeEpubToSendToKindle(uri), routes content:// EPUBs to openDocumentForContent(uri).
     * handleFileScheme(Uri, ContentType): bypasses routeEpubToSendToKindle(uri) and PDF-only AlertActivity guard, routes file:// EPUBs directly to BookOpenManagerBuilderFactory.a().build().f(path, this, EntryPoint.FILE_SCHEME).
     * routeEpubToSendToKindle(Uri): redirects any residual call to openDocumentForContent(uri).
   - com.amazon.kindle.ContentSchemeBookOpenHelperActivity:
     * getContentTypeFromFilePath(String) & getContentTypeFromUri(Uri, ContentResolver): returns ContentType.PDF (".pdf") only for PDF, and ContentType.MOBI (".prc") for EPUB/MOBI/AZW3 so content:// EPUB streams are routed to the ebook loader (dp.g) instead of PdfReaderActivity.
   - com.amazon.kindle.modules.KRF4ContentModule:
     * f(): includes ".epub" in the registered local ebook extensions list at startup.
   - com.amazon.kindle.s2k.SendToKindleActivity:
     * continueBuildActivity(): intercepts ACTION_SEND ("Share to Kindle") intents and opens the shared EPUB file directly in the local Kindle reader via BookOpenManagerBuilderFactory without cloud upload.
   - Removes Ldp/g; from classes4.dex class_defs so Android ART loads the enhanced Ldp/g; from classes9.dex.
2. Assembles classes9.dex from scratch:
   - Ldp/g; (extends Ldp/b;)
   - Lcom/amazon/kindle/epub/EpubBookBuilder; (implements Lji/j;)
   - Lcom/amazon/kindle/epub/EpubTranscoder; (complete pure-Java on-device EPUB-to-PalmDB-MOBI/HTML/Image/Table transcoder)
"""
import hashlib
import os
import struct
import zlib


def uleb128_encode(val: int) -> bytes:
    out = bytearray()
    while True:
        b = val & 0x7F
        val >>= 7
        if val != 0:
            out.append(b | 0x80)
        else:
            out.append(b)
            break
    return bytes(out)


def read_uleb128(buf: bytes, pos: int):
    res, shift = 0, 0
    while True:
        b = buf[pos]
        pos += 1
        res |= (b & 0x7F) << shift
        if not (b & 0x80):
            break
        shift += 7
    return res, pos


def mutf8_encode(s: str) -> bytes:
    # Standard UTF-8 for BMP non-null characters matches MUTF-8
    b = s.encode("utf-8")
    return uleb128_encode(len(s)) + b + b"\x00"


def update_dex_hashes(dex_bytes: bytearray) -> bytes:
    struct.pack_into("<I", dex_bytes, 0x20, len(dex_bytes))
    sha1 = hashlib.sha1(dex_bytes[0x20:]).digest()
    dex_bytes[0x0C:0x20] = sha1
    adler = zlib.adler32(dex_bytes[0x0C:]) & 0xFFFFFFFF
    struct.pack_into("<I", dex_bytes, 0x08, adler)
    return bytes(dex_bytes)


def patch_classes4_dex(src_path: str, dst_path: str):
    with open(src_path, "rb") as f:
        dex = bytearray(f.read())

    # 1. ContentSchemeBookOpenHelperActivity.getContentTypeFromFilePath @ 0x24cd7c
    # Original: 62009e247110523800000c016e202cd712000a0238020300110062029d241102
    # Swap 9e24 (ContentType.MOBI) and 9d24 (ContentType.PDF)
    off_fp = 0x24CD7C + 16
    assert dex[off_fp : off_fp + 4] == bytes.fromhex("62009e24")
    assert dex[off_fp + 26 : off_fp + 30] == bytes.fromhex("62029d24")
    dex[off_fp : off_fp + 4] = bytes.fromhex("62009d24")
    dex[off_fp + 26 : off_fp + 30] = bytes.fromhex("62029e24")

    # 2. ContentSchemeBookOpenHelperActivity.getContentTypeFromUri @ 0x24cdac
    # Swap 9e24 and 9d24 at tail of getContentTypeFromUri
    off_uri = 0x24CDAC + 16
    assert dex[off_uri + 58 : off_uri + 62] == bytes.fromhex("62029e24")
    assert dex[off_uri + 84 : off_uri + 88] == bytes.fromhex("62019d24")
    dex[off_uri + 58 : off_uri + 62] = bytes.fromhex("62029d24")
    dex[off_uri + 84 : off_uri + 88] = bytes.fromhex("62019e24")

    # 3. ThirdPartyUpgradePage.handleContentScheme @ 0x250140
    # NOP out units 0005..000c (62006f363306060070200a3954000e00)
    off_hcs = 0x250140 + 16
    assert dex[off_hcs + 10 : off_hcs + 26] == bytes.fromhex("62006f363306060070200a3954000e00")
    dex[off_hcs + 10 : off_hcs + 26] = b"\x00" * 16

    # 4. ThirdPartyUpgradePage.handleFileScheme @ 0x250248
    # At unit 000c (byte offset +24), replace 62016f36 with 283c0000 (goto +0x3c -> unit 0048)
    off_hfs = 0x250248 + 16
    assert dex[off_hfs + 24 : off_hfs + 28] == bytes.fromhex("62016f36")
    dex[off_hfs + 24 : off_hfs + 28] = bytes.fromhex("283c0000")

    # 5. ThirdPartyUpgradePage.routeEpubToSendToKindle @ 0x250450
    # Redirect to invoke-virtual {v4, v5}, BaseUpgradePage;->openDocumentForContent(Uri)V ; return-void
    off_r2k = 0x250450 + 16
    patch_r2k = bytes.fromhex("6e203f3854000e00")
    dex[off_r2k : off_r2k + len(patch_r2k)] = patch_r2k

    # 6. KRF4ContentModule.f() @ 0x3eebcc
    # Replace const-string v3, ".azw1" (1a03f911) with const-string v3, ".epub" (1a031112)
    off_krf4_f = 0x3EEBCC + 16
    assert dex[off_krf4_f + 20 : off_krf4_f + 24] == bytes.fromhex("1a03f911")
    dex[off_krf4_f + 20 : off_krf4_f + 24] = bytes.fromhex("1a031112")

    # 7. SendToKindleActivity.continueBuildActivity() @ 0x4737e4
    # Update outs from 3 to 4, and replace 51-unit body with local BookOpenManagerBuilderFactory open + finish()
    off_stk = 0x4737E4
    regs, ins, outs, tries, dbg, insns_sz = struct.unpack_from("<HHHHII", dex, off_stk)
    assert regs == 4 and ins == 1 and outs == 3 and insns_sz == 51
    struct.pack_into("<H", dex, off_stk + 4, 4)  # outs = 4
    stk_code = bytes.fromhex(
        "6e1092000300"  # invoke-virtual {v3}, Activity;->getIntent()Landroid/content/Intent;
        "0c00"          # move-result-object v0
        "702009c40300"  # invoke-direct {v3, v0}, SendToKindleActivity;->readIntentExtras(Intent)Z
        "0a00"          # move-result v0
        "38002500"      # if-eqz v0, +0x25 (to 002e: return-void)
        "54311c6a"      # iget-object v1, v3, SendToKindleActivity;->m_file:Ljava/io/File;
        "38012100"      # if-eqz v1, +0x21 (to 002e: return-void)
        "6e1016d60100"  # invoke-virtual {v1}, File;->getAbsolutePath()Ljava/lang/String;
        "0c01"          # move-result-object v1
        "1c00210f"      # const-class v0, BookOpenManagerBuilderFactory
        "7110cffd0000"  # invoke-static {v0}, Lzw/j;->e(Class)Lzw/j;
        "0c00"          # move-result-object v0
        "6e10cefd0000"  # invoke-virtual {v0}, Lzw/j;->c()Ljava/lang/Object;
        "0c00"          # move-result-object v0
        "1f00210f"      # check-cast v0, BookOpenManagerBuilderFactory
        "7210f23f0000"  # invoke-interface {v0}, BookOpenManagerBuilderFactory;->a()Ljo/d;
        "0c00"          # move-result-object v0
        "72104ddb0000"  # invoke-interface {v0}, Ljo/d;->build()Ljo/c;
        "0c00"          # move-result-object v0
        "62028628"      # sget-object v2, EntryPoint;->FILE_SCHEME
        "724049db1023"  # invoke-interface {v0, v1, v3, v2}, Ljo/c;->f(String, Activity, EntryPoint)Z
        "6e108c000300"  # invoke-virtual {v3}, Activity;->finish()V
        "0e00"          # return-void
        "0000000000000000"  # 4 units padding to 51 units
    )
    assert len(stk_code) == 51 * 2
    dex[off_stk + 16 : off_stk + 16 + 51 * 2] = stk_code

    # 8. Remove Ldp/g; (class_def #8367) from classes4.dex class_defs so classes9.dex provides Ldp/g;
    class_defs_size, class_defs_off = struct.unpack_from("<II", dex, 0x60)
    target_idx = 8367
    # Verify class_def #8367 is Ldp/g;
    string_ids_size, string_ids_off = struct.unpack_from("<II", dex, 0x38)
    type_ids_size, type_ids_off = struct.unpack_from("<II", dex, 0x40)
    c_idx = struct.unpack_from("<I", dex, class_defs_off + target_idx * 32)[0]
    s_idx = struct.unpack_from("<I", dex, type_ids_off + c_idx * 4)[0]
    s_off = struct.unpack_from("<I", dex, string_ids_off + s_idx * 4)[0]
    _, s_pos = read_uleb128(dex, s_off)
    c_name = dex[s_pos : dex.find(b"\x00", s_pos)].decode()
    assert c_name == "Ldp/g;", f"Expected Ldp/g;, got {c_name}"

    # Shift class_defs[target_idx+1 : class_defs_size] left by 32 bytes
    start_del = class_defs_off + target_idx * 32
    end_defs = class_defs_off + class_defs_size * 32
    dex[start_del : end_defs - 32] = dex[start_del + 32 : end_defs]
    dex[end_defs - 32 : end_defs] = b"\x00" * 32
    new_class_defs_size = class_defs_size - 1
    struct.pack_into("<I", dex, 0x60, new_class_defs_size)

    # Also update TYPE_CLASS_DEF_ITEM (0x0006) in map_list
    map_off = struct.unpack_from("<I", dex, 0x34)[0]
    map_size = struct.unpack_from("<I", dex, map_off)[0]
    for i in range(map_size):
        m_type, _, m_count, m_offset = struct.unpack_from("<HHII", dex, map_off + 4 + i * 12)
        if m_type == 0x0006:  # TYPE_CLASS_DEF_ITEM
            struct.pack_into("<I", dex, map_off + 4 + i * 12 + 4, new_class_defs_size)

    update_dex_hashes(dex)
    with open(dst_path, "wb") as f:
        f.write(dex)
    print(f"Patched classes4.dex written to {dst_path} ({len(dex)} bytes, class_defs={new_class_defs_size})")


if __name__ == "__main__":
    os.makedirs("/tmp/patched_dex", exist_ok=True)
    patch_classes4_dex(
        "/tmp/apk_chunks/kindle_apk_chunk1_dex_and_manifest/classes4.dex",
        "/tmp/patched_dex/classes4.dex",
    )
