#!/usr/bin/env python3
"""
Binary DEX patcher for com.amazon.kindle v8.159.0.100 classes4.dex:
1. ThirdPartyUpgradePage.handleContentScheme (0x250140): NOP out EPUB -> SendToKindle check (units 0005..000c)
2. ThirdPartyUpgradePage.handleFileScheme (0x250248): bypass routeEpubToSendToKindle & AlertActivity (goto +0x3c)
3. ThirdPartyUpgradePage.routeEpubToSendToKindle (0x250450): redirect to openDocumentForContent(Uri)
4. ContentSchemeBookOpenHelperActivity.getContentTypeFromFilePath (0x24cd7c) & getContentTypeFromUri (0x24cdac):
   swap ContentType.b (0x249e, MOBI) and ContentType.a (0x249d, PDF) so .epub -> .prc -> dp.g
5. KRF4ContentModule.f() (0x3eebcc): register ".epub" instead of ".azw1" in LocalContentFactory
6. SendToKindleActivity.continueBuildActivity() (0x4737e4): open shared .epub locally via BookOpenManager
7. Remove Ldp/g; from class_defs (10561 -> 10560) so ART loads enhanced Ldp/g; from classes9.dex
"""
import os
import struct
import hashlib
import zlib

import sys

SRC = sys.argv[1] if len(sys.argv) > 1 else "/tmp/apk_chunks/chunk1/classes4.dex"
DST = sys.argv[2] if len(sys.argv) > 2 else "/tmp/patched_dex/classes4.dex"
os.makedirs(os.path.dirname(os.path.abspath(DST)), exist_ok=True)

with open(SRC, "rb") as f:
    dex = bytearray(f.read())

def read_uleb128(data, pos):
    res, shift = 0, 0
    while True:
        b = data[pos]
        pos += 1
        res |= (b & 0x7f) << shift
        if (b & 0x80) == 0:
            return res, pos
        shift += 7

def find_string_idx(data, target):
    s_size, s_off = struct.unpack_from("<II", data, 0x38)
    for i in range(s_size):
        off = struct.unpack_from("<I", data, s_off + i * 4)[0]
        _, pos = read_uleb128(data, off)
        end = data.find(b"\x00", pos)
        if data[pos:end].decode("utf-8", errors="replace") == target:
            return i
    raise RuntimeError(f"String not found: {target}")

def find_type_idx(data, target):
    s_idx = find_string_idx(data, target)
    t_size, t_off = struct.unpack_from("<II", data, 0x40)
    for i in range(t_size):
        if struct.unpack_from("<I", data, t_off + i * 4)[0] == s_idx:
            return i
    raise RuntimeError(f"Type not found: {target}")

def find_method_idx(data, cls_desc, mname, ret_desc=None, param_descs=None):
    c_idx = find_type_idx(data, cls_desc)
    n_idx = find_string_idx(data, mname)
    m_size, m_off = struct.unpack_from("<II", data, 0x58)
    p_size, p_off = struct.unpack_from("<II", data, 0x48)
    for i in range(m_size):
        mc, mp, mn = struct.unpack_from("<HHI", data, m_off + i * 8)
        if mc == c_idx and mn == n_idx:
            if ret_desc is None:
                return i
            _, r_idx, params_off = struct.unpack_from("<III", data, p_off + mp * 12)
            # check return type
            t_size, t_off = struct.unpack_from("<II", data, 0x40)
            s_size, s_off = struct.unpack_from("<II", data, 0x38)
            r_s_idx = struct.unpack_from("<I", data, t_off + r_idx * 4)[0]
            off = struct.unpack_from("<I", data, s_off + r_s_idx * 4)[0]
            _, pos = read_uleb128(data, off)
            end = data.find(b"\x00", pos)
            r_str = data[pos:end].decode("utf-8", errors="replace")
            if r_str != ret_desc:
                continue
            if param_descs is not None:
                ps = []
                if params_off != 0:
                    cnt = struct.unpack_from("<I", data, params_off)[0]
                    for j in range(cnt):
                        pt = struct.unpack_from("<H", data, params_off + 4 + j * 2)[0]
                        ps_idx = struct.unpack_from("<I", data, t_off + pt * 4)[0]
                        po = struct.unpack_from("<I", data, s_off + ps_idx * 4)[0]
                        _, ppos = read_uleb128(data, po)
                        pend = data.find(b"\x00", ppos)
                        ps.append(data[ppos:pend].decode("utf-8", errors="replace"))
                if tuple(ps) != tuple(param_descs):
                    continue
            return i
    raise RuntimeError(f"Method not found: {cls_desc}->{mname}")

# 1. ThirdPartyUpgradePage.handleContentScheme (code_off=0x250140): NOP units 0005..000c
for u in range(5, 13):
    struct.pack_into("<H", dex, 0x250140 + 16 + u * 2, 0x0000)

# 2. ThirdPartyUpgradePage.handleFileScheme (code_off=0x250248): replace unit 000c (2 units) with goto +0x3c; nop
struct.pack_into("<HH", dex, 0x250248 + 16 + 0x0c * 2, 0x3c28, 0x0000)

# 3. ThirdPartyUpgradePage.routeEpubToSendToKindle (code_off=0x250450):
#    invoke-virtual {v4, v5}, Lcom/amazon/kindle/BaseUpgradePage;->openDocumentForContent(Landroid/net/Uri;)V; return-void
m_open_doc = find_method_idx(dex, "Lcom/amazon/kindle/BaseUpgradePage;", "openDocumentForContent", "V", ("Landroid/net/Uri;",))
off = 0x250450 + 16
# 6e 20 <m_open_doc> 54 00  |  0e 00
struct.pack_into("<HHHH", dex, off, 0x206e, m_open_doc, 0x0054, 0x000e)

# 4. ContentSchemeBookOpenHelperActivity.getContentTypeFromFilePath (0x24cd7c) & getContentTypeFromUri (0x24cdac):
#    Swap sget-object field indices 0x249d (PDF) and 0x249e (MOBI)
for code_off in (0x24cd7c, 0x24cdac):
    insns_sz = struct.unpack_from("<I", dex, code_off + 12)[0]
    base = code_off + 16
    for u in range(insns_sz - 1):
        w0, w1 = struct.unpack_from("<HH", dex, base + u * 2)
        if (w0 & 0xff) == 0x62:
            if w1 == 0x249d:
                struct.pack_into("<H", dex, base + (u + 1) * 2, 0x249e)
            elif w1 == 0x249e:
                struct.pack_into("<H", dex, base + (u + 1) * 2, 0x249d)

# 5. KRF4ContentModule.f() (0x3eebcc): replace const-string ".azw1" with ".epub"
s_azw1 = find_string_idx(dex, ".azw1")
s_epub = find_string_idx(dex, ".epub")
insns_sz = struct.unpack_from("<I", dex, 0x3eebcc + 12)[0]
base = 0x3eebcc + 16
replaced_azw1 = False
for u in range(insns_sz - 1):
    w0, w1 = struct.unpack_from("<HH", dex, base + u * 2)
    if (w0 & 0xff) == 0x1a and w1 == s_azw1:
        struct.pack_into("<H", dex, base + (u + 1) * 2, s_epub)
        replaced_azw1 = True
        break
assert replaced_azw1, "Failed to locate .azw1 in KRF4ContentModule.f()"

# 6. SendToKindleActivity.continueBuildActivity() (0x4737e4):
#    Open shared .epub locally via BookOpenManagerBuilderFactory.a().build().f(m_file.getAbsolutePath(), this, EntryPoint.FILE_SCHEME) + finish()
#    Check registers: v4=this (registers_size=5, ins_size=1)
m_get_intent = find_method_idx(dex, "Landroid/app/Activity;", "getIntent", "Landroid/content/Intent;", ())
m_read_extras = find_method_idx(dex, "Lcom/amazon/kindle/s2k/SendToKindleActivity;", "readIntentExtras")
m_get_abs = find_method_idx(dex, "Ljava/io/File;", "getAbsolutePath", "Ljava/lang/String;", ())
# Locate BookOpenManagerBuilderFactory.a() -> build() -> f(String, Activity, EntryPoint)Z
t_bomf = find_type_idx(dex, "Lcom/amazon/kindle/bookopen/BookOpenManagerBuilderFactory;")
m_bom_inst = find_method_idx(dex, "Lcom/amazon/kindle/bookopen/BookOpenManagerBuilderFactory;", "a")
m_finish = find_method_idx(dex, "Landroid/app/Activity;", "finish", "V", ())
# Find f_m_file and EntryPoint.FILE_SCHEME from existing DEX references
s_mfile = find_string_idx(dex, "m_file")
t_s2k = find_type_idx(dex, "Lcom/amazon/kindle/s2k/SendToKindleActivity;")
f_size, f_off = struct.unpack_from("<II", dex, 0x50)
f_mfile = None
f_ep_file = None
t_ep = find_type_idx(dex, "Lcom/amazon/kindle/bookopen/configurations/EntryPoint;")
s_ep_file = find_string_idx(dex, "FILE_SCHEME")
for i in range(f_size):
    fc, ft, fn = struct.unpack_from("<HHI", dex, f_off + i * 8)
    if fc == t_s2k and fn == s_mfile:
        f_mfile = i
    if fc == t_ep and fn == s_ep_file:
        f_ep_file = i
assert f_mfile is not None

# Also find BookOpenManagerBuilderFactory.build()
m_bom_build = None
m_size, m_off = struct.unpack_from("<II", dex, 0x58)
t_bom_builder = None
for i in range(m_size):
    mc, mp, mn = struct.unpack_from("<HHI", dex, m_off + i * 8)
    if i == m_bom_inst:
        pass
# Use existing method lookup for build() on BookOpenManagerBuilderFactory$a or similar:
# Actually let's check ThirdPartyUpgradePage.handleFileScheme to copy the exact BookOpenManager call indices!
base_hfs = 0x250248 + 16
hfs_sz = struct.unpack_from("<I", dex, 0x250248 + 12)[0]
bom_calls = []
for u in range(hfs_sz):
    w0 = struct.unpack_from("<H", dex, base_hfs + u * 2)[0]
    op = w0 & 0xff
    if op in (0x6e, 0x71, 0x72):
        midx = struct.unpack_from("<H", dex, base_hfs + (u + 1) * 2)[0]
        bom_calls.append(midx)
# In handleFileScheme, the last 3 invokes before return are BookOpenManagerBuilderFactory.a(), build(), f()
m_bom_inst = bom_calls[-3]
m_bom_build = bom_calls[-2]
m_bom_f = bom_calls[-1]
for u in range(hfs_sz):
    w0 = struct.unpack_from("<H", dex, base_hfs + u * 2)[0]
    if (w0 & 0xff) == 0x62:
        f_ep_file = struct.unpack_from("<H", dex, base_hfs + (u + 1) * 2)[0]
assert f_ep_file is not None

# Assemble SendToKindleActivity.continueBuildActivity() at 0x4737e4 (has plenty of room)
# v4 = this
s2k_code = [
    0x106e, m_get_intent, 0x0004,    # invoke-virtual {v4}, getIntent()Intent
    0x000c,                           # move-result-object v0
    0x206e, m_read_extras, 0x0004,   # invoke-virtual {v4, v0}, readIntentExtras(Intent)V
    0x4054, f_mfile,                  # iget-object v0, v4, m_file:File
    0x0038, 0x0013,                   # if-eqz v0, +0x13 (to finish)
    0x106e, m_get_abs, 0x0000,       # invoke-virtual {v0}, File.getAbsolutePath()String
    0x000c,                           # move-result-object v0
    0x0071, m_bom_inst, 0x0000,      # invoke-static {}, BookOpenManagerBuilderFactory.a()
    0x010c,                           # move-result-object v1
    0x010c,                           # move-result-object v1
    0x0262, f_ep_file,                # sget-object v2, EntryPoint.FILE_SCHEME
    0x106e, m_finish, 0x0004,        # invoke-virtual {v4}, finish()V
    0x000e,                           # return-void
]
# Fix w2 for {v1, v0, v4, v2}: r0=1, r1=0, r2=4, r3=2 -> 0x2401
s2k_code[15] = 0x2401
# Fix branch offset: pc=6 (if-eqz v0), finish is at pc = 6 + 2 + 3 + 1 + 3 + 1 + 3 + 1 + 2 + 3 = 25 -> 25 - 6 = 19 (0x13)
base_s2k = 0x4737e4 + 16
for idx, val in enumerate(s2k_code):
    struct.pack_into("<H", dex, base_s2k + idx * 2, val)

# 7. Step 7 omitted: keep class_defs intact so ART verifier does not reject classes4 with
# "Could not find declaring class for non-empty class data item". Enhanced Ldp/g; is resolved
# from the injected classes4.dex, with original classes shifted 4..8 -> 5..9.

# Recompute SHA-1 and Adler-32
sha1 = hashlib.sha1(dex[32:]).digest()
dex[12:32] = sha1
adler = zlib.adler32(dex[12:]) & 0xffffffff
struct.pack_into("<I", dex, 8, adler)

with open(DST, "wb") as f:
    f.write(dex)
print(f"Patched {DST} ({len(dex):,} bytes)")
