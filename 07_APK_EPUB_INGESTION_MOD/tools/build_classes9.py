#!/usr/bin/env python3
"""
Direct DEX assembler for classes9.dex (com.amazon.kindle v8.159.0.100 EPUB Ingestion & Clone Mod).

Key enhancements:
1. Package-name-independent cache directory via ActivityThread.currentApplication().getFilesDir()
   (works under com.amazon.kindle.epub or any cloned package name without EACCES).
2. Automatic Kindle Library registration (kindle_library.db) via ILibraryService.W(...) whenever
   an EPUB is opened via ACTION_VIEW, ACTION_SEND, or local file path (deduplicated via W0).
3. Multi-record 4096-byte PalmDB BOOKMOBI container generation with OPF metadata & unique ASIN.
"""
import struct
import hashlib
import zlib
import os

def uleb128(val):
    out = bytearray()
    while True:
        b = val & 0x7f
        val >>= 7
        if val != 0:
            out.append(b | 0x80)
        else:
            out.append(b)
            break
    return bytes(out)

def mutf8_encode(s):
    b = s.encode('utf-8')
    return uleb128(len(s)) + b + b'\x00'

class DexBuilder:
    def __init__(self):
        self.strings = set()
        self.types = set()
        self.protos = set()
        self.fields = set()
        self.methods = set()

    def add_string(self, s):
        self.strings.add(s)
        return s

    def add_type(self, t):
        self.add_string(t)
        self.types.add(t)
        return t

    def make_shorty(self, ret, params):
        def st(t):
            if t.startswith('L') or t.startswith('['):
                return 'L'
            return t
        return st(ret) + ''.join(st(p) for p in params)

    def add_proto(self, ret, params=()):
        params = tuple(params)
        shorty = self.make_shorty(ret, params)
        self.add_string(shorty)
        self.add_type(ret)
        for p in params:
            self.add_type(p)
        p_tuple = (shorty, ret, params)
        self.protos.add(p_tuple)
        return p_tuple

    def add_field(self, cls, ftype, name):
        self.add_type(cls)
        self.add_type(ftype)
        self.add_string(name)
        f_tuple = (cls, ftype, name)
        self.fields.add(f_tuple)
        return f_tuple

    def add_method(self, cls, ret, params, name):
        self.add_type(cls)
        p_tuple = self.add_proto(ret, params)
        self.add_string(name)
        m_tuple = (cls, p_tuple, name)
        self.methods.add(m_tuple)
        return m_tuple

db = DexBuilder()

# Standard & Android types
T_OBJ = db.add_type("Ljava/lang/Object;")
T_STR = db.add_type("Ljava/lang/String;")
T_INT_CLS = db.add_type("Ljava/lang/Integer;")
T_FILE = db.add_type("Ljava/io/File;")
T_FIS = db.add_type("Ljava/io/FileInputStream;")
T_FOS = db.add_type("Ljava/io/FileOutputStream;")
T_BAOS = db.add_type("Ljava/io/ByteArrayOutputStream;")
T_IS = db.add_type("Ljava/io/InputStream;")
T_ZIS = db.add_type("Ljava/util/zip/ZipInputStream;")
T_ZE = db.add_type("Ljava/util/zip/ZipEntry;")
T_SB = db.add_type("Ljava/lang/StringBuilder;")
T_COLL = db.add_type("Ljava/util/Collection;")
T_LIST = db.add_type("Ljava/util/List;")
T_SET = db.add_type("Ljava/util/Set;")
T_HASHSET = db.add_type("Ljava/util/HashSet;")
T_COLLECTIONS = db.add_type("Ljava/util/Collections;")
T_LOCALE = db.add_type("Ljava/util/Locale;")
T_SYS = db.add_type("Ljava/lang/System;")
T_ACT_THREAD = db.add_type("Landroid/app/ActivityThread;")
T_APP = db.add_type("Landroid/app/Application;")

T_V = db.add_type("V")
T_Z = db.add_type("Z")
T_I = db.add_type("I")
T_BA = db.add_type("[B")
T_OA = db.add_type("[Ljava/lang/Object;")

# Kindle types
T_DP_B = db.add_type("Ldp/b;")
T_DP_G = db.add_type("Ldp/g;")
T_JI_J = db.add_type("Lji/j;")
T_HI_B = db.add_type("Lhi/b;")
T_CONTENT_A = db.add_type("Lcom/amazon/kindle/content/a;")
T_CONTENT_STATE = db.add_type("Lcom/amazon/kindle/model/content/ContentState;")
T_XT_A = db.add_type("Lxt/a;")
T_XT_D = db.add_type("Lxt/d;")
T_IDOC_INFO = db.add_type("Lcom/amazon/kindle/krf/KRF/Reader/IDocumentInfo;")
T_UTILS = db.add_type("Lcom/amazon/kcp/util/Utils;")
T_J4 = db.add_type("Lcom/amazon/kcp/application/j4;")
T_LIB_SVC = db.add_type("Lcom/amazon/kindle/content/ILibraryService;")

T_EPUB_BB = db.add_type("Lcom/amazon/kindle/epub/EpubBookBuilder;")
T_EPUB_TC = db.add_type("Lcom/amazon/kindle/epub/EpubTranscoder;")

# Strings
for s in [
    ".epub", ".azw1", ".prc", ".xhtml", ".html", ".htm", ".opf",
    "UTF-8", "EBOK", "B0%08X", "Kindle EPUB", "Local EPUB Author", "2026-01-01",
    "                                ",
    "<dc:title", "</dc:title>", "<dc:creator", "</dc:creator>",
    "<body", "</body>", ">",
    "<html><head></head><body>", "</body></html>", "<mbp:pagebreak/>",
    "epub_cache", "/data/local/tmp/epub_cache"
]:
    db.add_string(s)

# Fields
F_DP_G_C = db.add_field(T_DP_G, T_SET, "c")
F_EPUB_BB_DEL = db.add_field(T_EPUB_BB, T_JI_J, "a")
F_LOCALE_US = db.add_field(T_LOCALE, T_LOCALE, "US")

# Methods for Ldp/g; and Ldp/b;
M_DP_B_INIT = db.add_method(T_DP_B, T_V, (T_JI_J, T_HI_B), "<init>")
M_DP_B_A = db.add_method(T_DP_B, T_CONTENT_A, (T_STR, T_FILE), "a")
M_DP_B_E = db.add_method(T_DP_B, T_CONTENT_A, (T_STR, T_FILE, T_CONTENT_STATE, T_XT_A), "e")

M_DP_G_CLINIT = db.add_method(T_DP_G, T_V, (), "<clinit>")
M_DP_G_INIT = db.add_method(T_DP_G, T_V, (T_JI_J, T_HI_B), "<init>")
M_DP_G_H = db.add_method(T_DP_G, T_V, (T_COLL,), "h")
M_DP_G_I = db.add_method(T_DP_G, T_COLL, (), "i")
M_DP_G_A = db.add_method(T_DP_G, T_CONTENT_A, (T_STR, T_FILE), "a")
M_DP_G_D = db.add_method(T_DP_G, T_COLL, (), "d")
M_DP_G_E = db.add_method(T_DP_G, T_CONTENT_A, (T_STR, T_FILE, T_CONTENT_STATE, T_XT_A), "e")

# Methods for EpubBookBuilder & Lji/j;
M_OBJ_INIT = db.add_method(T_OBJ, T_V, (), "<init>")
M_JI_J_A = db.add_method(T_JI_J, T_XT_D, (T_HI_B, T_STR, T_I), "a")
M_JI_J_B = db.add_method(T_JI_J, T_IDOC_INFO, (T_STR,), "b")
M_JI_J_C = db.add_method(T_JI_J, T_XT_D, (T_HI_B, T_STR), "c")

M_EPUB_BB_INIT = db.add_method(T_EPUB_BB, T_V, (T_JI_J,), "<init>")
M_EPUB_BB_A = db.add_method(T_EPUB_BB, T_XT_D, (T_HI_B, T_STR, T_I), "a")
M_EPUB_BB_B = db.add_method(T_EPUB_BB, T_IDOC_INFO, (T_STR,), "b")
M_EPUB_BB_C = db.add_method(T_EPUB_BB, T_XT_D, (T_HI_B, T_STR), "c")

# Methods for EpubTranscoder
M_TC_INIT = db.add_method(T_EPUB_TC, T_V, (), "<init>")
M_TC_GET_CACHE = db.add_method(T_EPUB_TC, T_FILE, (), "getCacheDir")
M_TC_REG_LIB = db.add_method(T_EPUB_TC, T_V, (T_STR, T_FILE, T_CONTENT_A), "registerInLibrary")
M_TC_ENSURE_FILE = db.add_method(T_EPUB_TC, T_FILE, (T_FILE,), "ensureKindleFile")
M_TC_ENSURE_PATH = db.add_method(T_EPUB_TC, T_STR, (T_STR,), "ensureKindlePath")
M_TC_IS_EPUB = db.add_method(T_EPUB_TC, T_Z, (T_FILE,), "isEpubFile")
M_TC_TRANSCODE = db.add_method(T_EPUB_TC, T_V, (T_FILE, T_FILE), "transcodeToPrc")
M_TC_CLEAN_BODY = db.add_method(T_EPUB_TC, T_BA, (T_BA,), "extractBodyBytes")
M_TC_EXTRACT_TAG = db.add_method(T_EPUB_TC, T_STR, (T_STR, T_STR, T_STR, T_STR), "extractXmlTag")
M_TC_PAD32 = db.add_method(T_EPUB_TC, T_BA, (T_STR,), "toFixed32Bytes")
M_TC_BUILD_PRC = db.add_method(T_EPUB_TC, T_BA, (T_STR, T_STR, T_STR, T_BA), "buildPrcBytes")
M_TC_PUT_INT = db.add_method(T_EPUB_TC, T_V, (T_BA, T_I, T_I), "putIntBE")
M_TC_PUT_SHORT = db.add_method(T_EPUB_TC, T_V, (T_BA, T_I, T_I), "putShortBE")

# Android / Kindle Library methods
M_AT_CURR_APP = db.add_method(T_ACT_THREAD, T_APP, (), "currentApplication")
M_APP_FILES_DIR = db.add_method(T_APP, T_FILE, (), "getFilesDir")
M_UTILS_FACTORY = db.add_method(T_UTILS, T_J4, (), "getFactory")
M_J4_LIB_SVC = db.add_method(T_J4, T_LIB_SVC, (), "getLibraryService")
M_LIB_ADD_META = db.add_method(T_LIB_SVC, T_V, (T_COLL,), "addContentMetadata")
M_LIB_W0 = db.add_method(T_LIB_SVC, T_CONTENT_A, (T_STR, T_STR), "W0")
M_LIB_W = db.add_method(T_LIB_SVC, T_V, (T_STR, T_COLL, T_Z), "W")
M_CONTENT_A_E1 = db.add_method(T_CONTENT_A, T_STR, (), "e1")

# Standard Java library methods
M_HASHSET_INIT = db.add_method(T_HASHSET, T_V, (), "<init>")
M_SET_ADD = db.add_method(T_SET, T_Z, (T_OBJ,), "add")
M_SET_ADDALL = db.add_method(T_SET, T_Z, (T_COLL,), "addAll")
M_COLL_UNMOD = db.add_method(T_COLLECTIONS, T_COLL, (T_COLL,), "unmodifiableCollection")
M_COLL_SINGLETON = db.add_method(T_COLLECTIONS, T_LIST, (T_OBJ,), "singletonList")

M_FILE_INIT_STR = db.add_method(T_FILE, T_V, (T_STR,), "<init>")
M_FILE_INIT_PARENT = db.add_method(T_FILE, T_V, (T_FILE, T_STR), "<init>")
M_FILE_EXISTS = db.add_method(T_FILE, T_Z, (), "exists")
M_FILE_GETPATH = db.add_method(T_FILE, T_STR, (), "getAbsolutePath")
M_FILE_GETNAME = db.add_method(T_FILE, T_STR, (), "getName")
M_FILE_MKDIRS = db.add_method(T_FILE, T_Z, (), "mkdirs")

M_STR_TOLOWER = db.add_method(T_STR, T_STR, (T_LOCALE,), "toLowerCase")
M_STR_ENDSWITH = db.add_method(T_STR, T_Z, (T_STR,), "endsWith")
M_STR_GETBYTES = db.add_method(T_STR, T_BA, (T_STR,), "getBytes")
M_STR_INIT_BYTES = db.add_method(T_STR, T_V, (T_BA, T_STR), "<init>")
M_STR_INDEXOF = db.add_method(T_STR, T_I, (T_STR,), "indexOf")
M_STR_INDEXOF_FROM = db.add_method(T_STR, T_I, (T_STR, T_I), "indexOf")
M_STR_SUBSTR = db.add_method(T_STR, T_STR, (T_I,), "substring")
M_STR_SUBSTR2 = db.add_method(T_STR, T_STR, (T_I, T_I), "substring")
M_STR_TRIM = db.add_method(T_STR, T_STR, (), "trim")
M_STR_HASHCODE = db.add_method(T_STR, T_I, (), "hashCode")
M_STR_FORMAT = db.add_method(T_STR, T_STR, (T_LOCALE, T_STR, T_OA), "format")
M_INT_VALUEOF = db.add_method(T_INT_CLS, T_INT_CLS, (T_I,), "valueOf")

M_SB_INIT = db.add_method(T_SB, T_V, (), "<init>")
M_SB_APPEND_STR = db.add_method(T_SB, T_SB, (T_STR,), "append")
M_SB_TOSTR = db.add_method(T_SB, T_STR, (), "toString")

M_FIS_INIT = db.add_method(T_FIS, T_V, (T_FILE,), "<init>")
M_FIS_READ = db.add_method(T_FIS, T_I, (T_BA,), "read")
M_FIS_CLOSE = db.add_method(T_FIS, T_V, (), "close")

M_FOS_INIT = db.add_method(T_FOS, T_V, (T_FILE,), "<init>")
M_FOS_WRITE = db.add_method(T_FOS, T_V, (T_BA,), "write")
M_FOS_CLOSE = db.add_method(T_FOS, T_V, (), "close")

M_ZIS_INIT = db.add_method(T_ZIS, T_V, (T_IS,), "<init>")
M_ZIS_NEXT = db.add_method(T_ZIS, T_ZE, (), "getNextEntry")
M_ZIS_READ = db.add_method(T_ZIS, T_I, (T_BA,), "read")
M_ZIS_CLOSE = db.add_method(T_ZIS, T_V, (), "close")
M_ZE_GETNAME = db.add_method(T_ZE, T_STR, (), "getName")

M_BAOS_INIT = db.add_method(T_BAOS, T_V, (), "<init>")
M_BAOS_WRITE = db.add_method(T_BAOS, T_V, (T_BA, T_I, T_I), "write")
M_BAOS_TOBA = db.add_method(T_BAOS, T_BA, (), "toByteArray")

M_SYS_ARRAYCOPY = db.add_method(T_SYS, T_V, (T_OBJ, T_I, T_OBJ, T_I, T_I), "arraycopy")

sorted_strings = sorted(db.strings)
str_idx = {s: i for i, s in enumerate(sorted_strings)}

sorted_types = sorted(db.types, key=lambda t: str_idx[t])
type_idx = {t: i for i, t in enumerate(sorted_types)}

sorted_protos = sorted(db.protos, key=lambda p: (type_idx[p[1]], tuple(type_idx[x] for x in p[2])))
proto_idx = {p: i for i, p in enumerate(sorted_protos)}

sorted_fields = sorted(db.fields, key=lambda f: (type_idx[f[0]], str_idx[f[2]], type_idx[f[1]]))
field_idx = {f: i for i, f in enumerate(sorted_fields)}

sorted_methods = sorted(db.methods, key=lambda m: (type_idx[m[0]], str_idx[m[2]], proto_idx[m[1]]))
method_idx = {m: i for i, m in enumerate(sorted_methods)}

def insn_10x(op): return struct.pack('<H', op)
def insn_11x(op, a): return struct.pack('<H', op | (a << 8))
def insn_11n(op, a, b_signed4): return struct.pack('<H', op | (a << 8) | ((b_signed4 & 0xf) << 12))
def insn_12x(op, a, b): return struct.pack('<H', op | (a << 8) | (b << 12))
def insn_21s(op, a, imm16): return struct.pack('<HH', op | (a << 8), imm16 & 0xffff)
def insn_21c(op, a, idx16): return struct.pack('<HH', op | (a << 8), idx16 & 0xffff)
def insn_22b(op, a, b, imm8): return struct.pack('<HH', op | (a << 8), (b & 0xff) | ((imm8 & 0xff) << 8))
def insn_22s(op, a, b, imm16): return struct.pack('<HH', op | (a << 8) | (b << 12), imm16 & 0xffff)
def insn_22c(op, a, b, idx16): return struct.pack('<HH', op | (a << 8) | (b << 12), idx16 & 0xffff)
def insn_22t(op, a, b, off16): return struct.pack('<HH', op | (a << 8) | (b << 12), off16 & 0xffff)
def insn_21t(op, a, off16): return struct.pack('<HH', op | (a << 8), off16 & 0xffff)
def insn_10t(op, off8): return struct.pack('<H', op | ((off8 & 0xff) << 8))
def insn_23x(op, a, b, c): return struct.pack('<HH', op | (a << 8), (b & 0xff) | ((c & 0xff) << 8))
def insn_31i(op, a, imm32): return struct.pack('<HI', op | (a << 8), imm32 & 0xffffffff)
def insn_35c(op, regs, idx16):
    count = len(regs)
    r = list(regs) + [0] * (5 - count)
    w0 = op | (r[4] << 8) | (count << 12)
    w2 = r[0] | (r[1] << 4) | (r[2] << 8) | (r[3] << 12)
    return struct.pack('<HHH', w0, idx16 & 0xffff, w2)

class Asm:
    def __init__(self):
        self.items = []
        self.labels = {}
        self.pc = 0
    def label(self, name): self.labels[name] = self.pc
    def emit(self, b):
        assert len(b) % 2 == 0
        self.items.append((self.pc, b))
        self.pc += len(b) // 2
    def emit_branch21t(self, op, reg, target_label):
        pc = self.pc
        self.items.append((pc, lambda: insn_21t(op, reg, self.labels[target_label] - pc)))
        self.pc += 2
    def emit_branch22t(self, op, r1, r2, target_label):
        pc = self.pc
        self.items.append((pc, lambda: insn_22t(op, r1, r2, self.labels[target_label] - pc)))
        self.pc += 2
    def emit_branch10t(self, op, target_label):
        pc = self.pc
        self.items.append((pc, lambda: insn_10t(op, self.labels[target_label] - pc)))
        self.pc += 1
    def assemble(self):
        return b''.join(item() if callable(item) else item for _, item in self.items)

def make_code_item(registers_size, ins_size, outs_size, insns_bytes, tries=b'', handlers=b''):
    assert len(insns_bytes) % 2 == 0
    insns_count = len(insns_bytes) // 2
    tries_size = 0 if not tries else 1
    header = struct.pack('<HHHHII', registers_size, ins_size, outs_size, tries_size, 0, insns_count)
    body = header + insns_bytes
    if tries:
        if insns_count % 2 == 1:
            body += b'\x00\x00'
        body += tries + handlers
    return body

# ==============================================================================
# 1. Ldp/g;
# ==============================================================================
code_dp_g_clinit = make_code_item(2, 0, 2, b''.join([
    insn_21c(0x22, 0, type_idx[T_HASHSET]),
    insn_35c(0x70, [0], method_idx[M_HASHSET_INIT]),
    insn_21c(0x1a, 1, str_idx[".epub"]),
    insn_35c(0x72, [0, 1], method_idx[M_SET_ADD]),
    insn_21c(0x1a, 1, str_idx[".azw1"]),
    insn_35c(0x72, [0, 1], method_idx[M_SET_ADD]),
    insn_21c(0x69, 0, field_idx[F_DP_G_C]),
    insn_10x(0x0e),
]))

code_dp_g_init = make_code_item(4, 3, 3, b''.join([
    insn_21c(0x22, 0, type_idx[T_EPUB_BB]),
    insn_35c(0x70, [0, 2], method_idx[M_EPUB_BB_INIT]),
    insn_35c(0x70, [1, 0, 3], method_idx[M_DP_B_INIT]),
    insn_10x(0x0e),
]))

code_dp_g_h = make_code_item(3, 1, 2, b''.join([
    insn_21c(0x62, 0, field_idx[F_DP_G_C]),
    insn_35c(0x72, [0, 2], method_idx[M_SET_ADDALL]),
    insn_21c(0x1a, 1, str_idx[".epub"]),
    insn_35c(0x72, [0, 1], method_idx[M_SET_ADD]),
    insn_21c(0x1a, 1, str_idx[".azw1"]),
    insn_35c(0x72, [0, 1], method_idx[M_SET_ADD]),
    insn_10x(0x0e),
]))

code_dp_g_i = make_code_item(1, 0, 1, b''.join([
    insn_21c(0x62, 0, field_idx[F_DP_G_C]),
    insn_35c(0x71, [0], method_idx[M_COLL_UNMOD]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
]))

# Ldp/g;->a(String userId, File file)Lcom/amazon/kindle/content/a;
#   v0 = EpubTranscoder.ensureKindleFile(file)
#   v0 = super.a(userId, v0)
#   EpubTranscoder.registerInLibrary(userId, file, v0)
#   return v0
code_dp_g_a = make_code_item(5, 3, 3, b''.join([
    insn_35c(0x71, [4], method_idx[M_TC_ENSURE_FILE]),
    insn_11x(0x0c, 0),
    insn_35c(0x6f, [2, 3, 0], method_idx[M_DP_B_A]),
    insn_11x(0x0c, 0),
    insn_35c(0x71, [3, 4, 0], method_idx[M_TC_REG_LIB]),
    insn_11x(0x11, 0),
]))

code_dp_g_d = make_code_item(2, 1, 0, b''.join([
    insn_35c(0x71, [], method_idx[M_DP_G_I]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
]))

code_dp_g_e = make_code_item(7, 5, 5, b''.join([
    insn_35c(0x71, [4], method_idx[M_TC_ENSURE_FILE]),
    insn_11x(0x0c, 0),
    insn_35c(0x6f, [2, 3, 0, 5, 6], method_idx[M_DP_B_E]),
    insn_11x(0x0c, 1),
    insn_35c(0x71, [3, 4, 1], method_idx[M_TC_REG_LIB]),
    insn_11x(0x11, 1),
]))

# ==============================================================================
# 2. Lcom/amazon/kindle/epub/EpubBookBuilder;
# ==============================================================================
code_bb_init = make_code_item(2, 2, 1, b''.join([
    insn_35c(0x70, [0], method_idx[M_OBJ_INIT]),
    insn_22c(0x5b, 1, 0, field_idx[F_EPUB_BB_DEL]),
    insn_10x(0x0e),
]))

code_bb_a = make_code_item(5, 4, 4, b''.join([
    insn_35c(0x71, [3], method_idx[M_TC_ENSURE_PATH]),
    insn_11x(0x0c, 3),
    insn_22c(0x54, 0, 1, field_idx[F_EPUB_BB_DEL]),
    insn_35c(0x72, [0, 2, 3, 4], method_idx[M_JI_J_A]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
]))

code_bb_b = make_code_item(3, 2, 2, b''.join([
    insn_35c(0x71, [2], method_idx[M_TC_ENSURE_PATH]),
    insn_11x(0x0c, 2),
    insn_22c(0x54, 0, 1, field_idx[F_EPUB_BB_DEL]),
    insn_35c(0x72, [0, 2], method_idx[M_JI_J_B]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
]))

code_bb_c = make_code_item(4, 3, 3, b''.join([
    insn_35c(0x71, [3], method_idx[M_TC_ENSURE_PATH]),
    insn_11x(0x0c, 3),
    insn_22c(0x54, 0, 1, field_idx[F_EPUB_BB_DEL]),
    insn_35c(0x72, [0, 2, 3], method_idx[M_JI_J_C]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
]))

# ==============================================================================
# 3. Lcom/amazon/kindle/epub/EpubTranscoder;
# ==============================================================================
code_tc_init = make_code_item(1, 1, 1, b''.join([
    insn_35c(0x70, [0], method_idx[M_OBJ_INIT]),
    insn_10x(0x0e),
]))

# getCacheDir()Ljava/io/File;
# Resolves ActivityThread.currentApplication().getFilesDir() -> new File(filesDir, "epub_cache")
# Works under ANY cloned package name!
asm_gcd = Asm()
asm_gcd.label("try_start")
asm_gcd.emit(insn_35c(0x71, [], method_idx[M_AT_CURR_APP]))
asm_gcd.emit(insn_11x(0x0c, 0))
asm_gcd.emit_branch21t(0x38, 0, "fallback")
asm_gcd.emit(insn_35c(0x6e, [0], method_idx[M_APP_FILES_DIR]))
asm_gcd.emit(insn_11x(0x0c, 0))
asm_gcd.emit_branch21t(0x38, 0, "fallback")
asm_gcd.emit(insn_21c(0x22, 1, type_idx[T_FILE]))
asm_gcd.emit(insn_21c(0x1a, 2, str_idx["epub_cache"]))
asm_gcd.emit(insn_35c(0x70, [1, 0, 2], method_idx[M_FILE_INIT_PARENT]))
asm_gcd.emit(insn_35c(0x6e, [1], method_idx[M_FILE_MKDIRS]))
asm_gcd.label("try_end")
asm_gcd.emit(insn_11x(0x11, 1))
asm_gcd.label("fallback")
asm_gcd.emit(insn_21c(0x22, 0, type_idx[T_FILE]))
asm_gcd.emit(insn_21c(0x1a, 1, str_idx["/data/local/tmp/epub_cache"]))
asm_gcd.emit(insn_35c(0x70, [0, 1], method_idx[M_FILE_INIT_STR]))
asm_gcd.emit(insn_35c(0x6e, [0], method_idx[M_FILE_MKDIRS]))
asm_gcd.emit(insn_11x(0x11, 0))

b_gcd = asm_gcd.assemble()
tries_gcd = struct.pack('<IHH', asm_gcd.labels["try_start"], asm_gcd.labels["try_end"] - asm_gcd.labels["try_start"], 1)
handlers_gcd = uleb128(1) + bytes([0x00]) + uleb128(asm_gcd.labels["fallback"])
code_tc_get_cache = make_code_item(3, 0, 3, b_gcd, tries_gcd, handlers_gcd)

# registerInLibrary(Ljava/lang/String;Ljava/io/File;Lcom/amazon/kindle/content/a;)V
# Registers EPUB metadata in kindle_library.db via ILibraryService.addContentMetadata(singletonList(meta))
# and ILibraryService.W(userId, ...) if userId is present.
# Registers: 7 (v4=userId, v5=origFile, v6=metadata)
asm_reg = Asm()
asm_reg.emit_branch21t(0x38, 6, "ret")
asm_reg.emit_branch21t(0x38, 5, "ret")
asm_reg.emit(insn_35c(0x71, [5], method_idx[M_TC_IS_EPUB]))
asm_reg.emit(insn_11x(0x0a, 0))
asm_reg.emit_branch21t(0x38, 0, "ret")
asm_reg.label("try_start")
asm_reg.emit(insn_35c(0x71, [], method_idx[M_UTILS_FACTORY]))
asm_reg.emit(insn_11x(0x0c, 0))
asm_reg.emit_branch21t(0x38, 0, "ret")
asm_reg.emit(insn_35c(0x72, [0], method_idx[M_J4_LIB_SVC]))
asm_reg.emit(insn_11x(0x0c, 0)) # v0 = ILibraryService
asm_reg.emit_branch21t(0x38, 0, "ret")
asm_reg.emit(insn_35c(0x71, [6], method_idx[M_COLL_SINGLETON]))
asm_reg.emit(insn_11x(0x0c, 1)) # v1 = Collections.singletonList(metadata)
asm_reg.emit(insn_35c(0x72, [0, 1], method_idx[M_LIB_ADD_META])) # addContentMetadata(v1)
asm_reg.emit_branch21t(0x38, 4, "try_end") # skip user associations if userId is null
asm_reg.emit(insn_35c(0x6e, [6], method_idx[M_CONTENT_A_E1]))
asm_reg.emit(insn_11x(0x0c, 2)) # v2 = filePath
asm_reg.emit(insn_35c(0x72, [0, 4, 2], method_idx[M_LIB_W0]))
asm_reg.emit(insn_11x(0x0c, 2))
asm_reg.emit_branch21t(0x39, 2, "try_end") # already in user library
asm_reg.emit(insn_11n(0x12, 2, 0)) # false
asm_reg.emit(insn_35c(0x72, [0, 4, 1, 2], method_idx[M_LIB_W]))
asm_reg.label("try_end")
asm_reg.label("ret")
asm_reg.emit(insn_10x(0x0e))

b_reg = asm_reg.assemble()
tries_reg = struct.pack('<IHH', asm_reg.labels["try_start"], asm_reg.labels["try_end"] - asm_reg.labels["try_start"], 1)
handlers_reg = uleb128(1) + bytes([0x00]) + uleb128(asm_reg.labels["ret"])
code_tc_reg_lib = make_code_item(7, 3, 4, b_reg, tries_reg, handlers_reg)

code_tc_put_int = make_code_item(6, 3, 0, b''.join([
    insn_22b(0xe1, 0, 5, 24), insn_12x(0x8d, 0, 0), insn_23x(0x4f, 0, 3, 4),
    insn_22b(0xd8, 1, 4, 1), insn_22b(0xe1, 0, 5, 16), insn_12x(0x8d, 0, 0), insn_23x(0x4f, 0, 3, 1),
    insn_22b(0xd8, 1, 4, 2), insn_22b(0xe1, 0, 5, 8), insn_12x(0x8d, 0, 0), insn_23x(0x4f, 0, 3, 1),
    insn_22b(0xd8, 1, 4, 3), insn_12x(0x8d, 0, 5), insn_23x(0x4f, 0, 3, 1), insn_10x(0x0e),
]))

code_tc_put_short = make_code_item(6, 3, 0, b''.join([
    insn_22b(0xe1, 0, 5, 8), insn_12x(0x8d, 0, 0), insn_23x(0x4f, 0, 3, 4),
    insn_22b(0xd8, 1, 4, 1), insn_12x(0x8d, 0, 5), insn_23x(0x4f, 0, 3, 1), insn_10x(0x0e),
]))

code_tc_ensure_path = make_code_item(3, 1, 2, b''.join([
    insn_21t(0x38, 2, 16),
    insn_21c(0x22, 0, type_idx[T_FILE]),
    insn_35c(0x70, [0, 2], method_idx[M_FILE_INIT_STR]),
    insn_35c(0x71, [0], method_idx[M_TC_ENSURE_FILE]),
    insn_11x(0x0c, 0),
    insn_35c(0x6e, [0], method_idx[M_FILE_GETPATH]),
    insn_11x(0x0c, 0),
    insn_11x(0x11, 0),
    insn_11x(0x11, 2),
]))

asm_is_epub = Asm()
asm_is_epub.emit_branch21t(0x38, 5, "ret_false")
asm_is_epub.emit(insn_35c(0x6e, [5], method_idx[M_FILE_EXISTS]))
asm_is_epub.emit(insn_11x(0x0a, 0))
asm_is_epub.emit_branch21t(0x38, 0, "ret_false")
asm_is_epub.emit(insn_35c(0x6e, [5], method_idx[M_FILE_GETNAME]))
asm_is_epub.emit(insn_11x(0x0c, 0))
asm_is_epub.emit(insn_21c(0x62, 1, field_idx[F_LOCALE_US]))
asm_is_epub.emit(insn_35c(0x6e, [0, 1], method_idx[M_STR_TOLOWER]))
asm_is_epub.emit(insn_11x(0x0c, 0))
asm_is_epub.emit(insn_21c(0x1a, 1, str_idx[".epub"]))
asm_is_epub.emit(insn_35c(0x6e, [0, 1], method_idx[M_STR_ENDSWITH]))
asm_is_epub.emit(insn_11x(0x0a, 0))
asm_is_epub.emit_branch21t(0x39, 0, "ret_true")
asm_is_epub.emit(insn_11n(0x12, 0, 4))
asm_is_epub.emit(insn_22c(0x23, 1, 0, type_idx[T_BA]))
asm_is_epub.label("try_start")
asm_is_epub.emit(insn_21c(0x22, 2, type_idx[T_FIS]))
asm_is_epub.emit(insn_35c(0x70, [2, 5], method_idx[M_FIS_INIT]))
asm_is_epub.emit(insn_35c(0x6e, [2, 1], method_idx[M_FIS_READ]))
asm_is_epub.emit(insn_11x(0x0a, 3))
asm_is_epub.emit(insn_35c(0x6e, [2], method_idx[M_FIS_CLOSE]))
asm_is_epub.label("try_end")
asm_is_epub.emit_branch22t(0x33, 3, 0, "ret_false")
asm_is_epub.emit(insn_11n(0x12, 0, 0))
asm_is_epub.emit(insn_23x(0x48, 2, 1, 0))
asm_is_epub.emit(insn_21s(0x13, 3, 0x50))
asm_is_epub.emit_branch22t(0x33, 2, 3, "ret_false")
asm_is_epub.emit(insn_11n(0x12, 0, 1))
asm_is_epub.emit(insn_23x(0x48, 2, 1, 0))
asm_is_epub.emit(insn_21s(0x13, 3, 0x4b))
asm_is_epub.emit_branch22t(0x33, 2, 3, "ret_false")
asm_is_epub.label("ret_true")
asm_is_epub.emit(insn_11n(0x12, 0, 1))
asm_is_epub.emit(insn_11x(0x0f, 0))
asm_is_epub.label("ret_false")
asm_is_epub.emit(insn_11n(0x12, 0, 0))
asm_is_epub.emit(insn_11x(0x0f, 0))

b_is_epub = asm_is_epub.assemble()
tries_is_epub = struct.pack('<IHH', asm_is_epub.labels["try_start"], asm_is_epub.labels["try_end"] - asm_is_epub.labels["try_start"], 1)
handlers_is_epub = uleb128(1) + bytes([0x00]) + uleb128(asm_is_epub.labels["ret_false"])
code_tc_is_epub = make_code_item(6, 1, 2, b_is_epub, tries_is_epub, handlers_is_epub)

# ensureKindleFile(Ljava/io/File;)Ljava/io/File; (uses getCacheDir()!)
asm_ef = Asm()
asm_ef.emit(insn_35c(0x71, [4], method_idx[M_TC_IS_EPUB]))
asm_ef.emit(insn_11x(0x0a, 0))
asm_ef.emit_branch21t(0x38, 0, "ret_orig")
asm_ef.label("try_start")
asm_ef.emit(insn_35c(0x71, [], method_idx[M_TC_GET_CACHE]))
asm_ef.emit(insn_11x(0x0c, 0))
asm_ef.emit(insn_21c(0x22, 1, type_idx[T_SB]))
asm_ef.emit(insn_35c(0x70, [1], method_idx[M_SB_INIT]))
asm_ef.emit(insn_35c(0x6e, [4], method_idx[M_FILE_GETNAME]))
asm_ef.emit(insn_11x(0x0c, 2))
asm_ef.emit(insn_35c(0x6e, [1, 2], method_idx[M_SB_APPEND_STR]))
asm_ef.emit(insn_21c(0x1a, 2, str_idx[".prc"]))
asm_ef.emit(insn_35c(0x6e, [1, 2], method_idx[M_SB_APPEND_STR]))
asm_ef.emit(insn_35c(0x6e, [1], method_idx[M_SB_TOSTR]))
asm_ef.emit(insn_11x(0x0c, 1))
asm_ef.emit(insn_21c(0x22, 2, type_idx[T_FILE]))
asm_ef.emit(insn_35c(0x70, [2, 0, 1], method_idx[M_FILE_INIT_PARENT]))
asm_ef.emit(insn_35c(0x71, [4, 2], method_idx[M_TC_TRANSCODE]))
asm_ef.emit(insn_35c(0x6e, [2], method_idx[M_FILE_EXISTS]))
asm_ef.emit(insn_11x(0x0a, 0))
asm_ef.label("try_end")
asm_ef.emit_branch21t(0x38, 0, "ret_orig")
asm_ef.emit(insn_11x(0x11, 2))
asm_ef.label("ret_orig")
asm_ef.emit(insn_11x(0x11, 4))

b_ef = asm_ef.assemble()
tries_ef = struct.pack('<IHH', asm_ef.labels["try_start"], asm_ef.labels["try_end"] - asm_ef.labels["try_start"], 1)
handlers_ef = uleb128(1) + bytes([0x00]) + uleb128(asm_ef.labels["ret_orig"])
code_tc_ensure_file = make_code_item(5, 1, 3, b_ef, tries_ef, handlers_ef)

# extractBodyBytes([B)[B
asm_eb = Asm()
asm_eb.label("try_start")
asm_eb.emit(insn_21c(0x22, 0, type_idx[T_STR]))
asm_eb.emit(insn_21c(0x1a, 1, str_idx["UTF-8"]))
asm_eb.emit(insn_35c(0x70, [0, 4, 1], method_idx[M_STR_INIT_BYTES]))
asm_eb.emit(insn_21c(0x1a, 1, str_idx["<body"]))
asm_eb.emit(insn_35c(0x6e, [0, 1], method_idx[M_STR_INDEXOF]))
asm_eb.emit(insn_11x(0x0a, 1))
asm_eb.emit_branch21t(0x3a, 1, "ret_raw")
asm_eb.emit(insn_21c(0x1a, 2, str_idx[">"]))
asm_eb.emit(insn_35c(0x6e, [0, 2, 1], method_idx[M_STR_INDEXOF_FROM]))
asm_eb.emit(insn_11x(0x0a, 1))
asm_eb.emit_branch21t(0x3a, 1, "ret_raw")
asm_eb.emit(insn_22b(0xd8, 1, 1, 1))
asm_eb.emit(insn_21c(0x1a, 2, str_idx["</body>"]))
asm_eb.emit(insn_35c(0x6e, [0, 2, 1], method_idx[M_STR_INDEXOF_FROM]))
asm_eb.emit(insn_11x(0x0a, 2))
asm_eb.emit_branch21t(0x3a, 2, "no_close")
asm_eb.emit(insn_35c(0x6e, [0, 1, 2], method_idx[M_STR_SUBSTR2]))
asm_eb.emit(insn_11x(0x0c, 0))
asm_eb.emit_branch10t(0x28, "to_bytes")
asm_eb.label("no_close")
asm_eb.emit(insn_35c(0x6e, [0, 1], method_idx[M_STR_SUBSTR]))
asm_eb.emit(insn_11x(0x0c, 0))
asm_eb.label("to_bytes")
asm_eb.emit(insn_21c(0x1a, 1, str_idx["UTF-8"]))
asm_eb.emit(insn_35c(0x6e, [0, 1], method_idx[M_STR_GETBYTES]))
asm_eb.emit(insn_11x(0x0c, 0))
asm_eb.label("try_end")
asm_eb.emit(insn_11x(0x11, 0))
asm_eb.label("ret_raw")
asm_eb.emit(insn_11x(0x11, 4))

b_eb = asm_eb.assemble()
tries_eb = struct.pack('<IHH', asm_eb.labels["try_start"], asm_eb.labels["try_end"] - asm_eb.labels["try_start"], 1)
handlers_eb = uleb128(1) + bytes([0x00]) + uleb128(asm_eb.labels["ret_raw"])
code_tc_clean_body = make_code_item(5, 1, 3, b_eb, tries_eb, handlers_eb)

# extractXmlTag
asm_xt = Asm()
asm_xt.label("try_start")
asm_xt.emit(insn_35c(0x6e, [3, 4], method_idx[M_STR_INDEXOF]))
asm_xt.emit(insn_11x(0x0a, 0))
asm_xt.emit_branch21t(0x3a, 0, "ret_def")
asm_xt.emit(insn_21c(0x1a, 1, str_idx[">"]))
asm_xt.emit(insn_35c(0x6e, [3, 1, 0], method_idx[M_STR_INDEXOF_FROM]))
asm_xt.emit(insn_11x(0x0a, 0))
asm_xt.emit_branch21t(0x3a, 0, "ret_def")
asm_xt.emit(insn_22b(0xd8, 0, 0, 1))
asm_xt.emit(insn_35c(0x6e, [3, 5, 0], method_idx[M_STR_INDEXOF_FROM]))
asm_xt.emit(insn_11x(0x0a, 1))
asm_xt.emit_branch21t(0x3a, 1, "ret_def")
asm_xt.emit(insn_35c(0x6e, [3, 0, 1], method_idx[M_STR_SUBSTR2]))
asm_xt.emit(insn_11x(0x0c, 0))
asm_xt.emit(insn_35c(0x6e, [0], method_idx[M_STR_TRIM]))
asm_xt.emit(insn_11x(0x0c, 0))
asm_xt.label("try_end")
asm_xt.emit(insn_11x(0x11, 0))
asm_xt.label("ret_def")
asm_xt.emit(insn_11x(0x11, 6))

b_xt = asm_xt.assemble()
tries_xt = struct.pack('<IHH', asm_xt.labels["try_start"], asm_xt.labels["try_end"] - asm_xt.labels["try_start"], 1)
handlers_xt = uleb128(1) + bytes([0x00]) + uleb128(asm_xt.labels["ret_def"])
code_tc_extract_tag = make_code_item(7, 4, 3, b_xt, tries_xt, handlers_xt)

code_tc_pad32 = make_code_item(4, 1, 5, b''.join([
    insn_21c(0x22, 0, type_idx[T_SB]),
    insn_35c(0x70, [0], method_idx[M_SB_INIT]),
    insn_35c(0x6e, [0, 3], method_idx[M_SB_APPEND_STR]),
    insn_21c(0x1a, 1, str_idx["                                "]),
    insn_35c(0x6e, [0, 1], method_idx[M_SB_APPEND_STR]),
    insn_35c(0x6e, [0], method_idx[M_SB_TOSTR]),
    insn_11x(0x0c, 0),
    insn_21c(0x1a, 1, str_idx["UTF-8"]),
    insn_35c(0x6e, [0, 1], method_idx[M_STR_GETBYTES]),
    insn_11x(0x0c, 0),
    insn_21s(0x13, 1, 32),
    insn_22c(0x23, 2, 1, type_idx[T_BA]),
    insn_11n(0x12, 3, 0),
    insn_35c(0x71, [0, 3, 2, 3, 1], method_idx[M_SYS_ARRAYCOPY]),
    insn_11x(0x11, 2),
]))

# buildPrcBytes
asm_prc = Asm()
asm_prc.emit(insn_12x(0x21, 0, 11))
asm_prc.emit(insn_22s(0xd0, 1, 0, 4095))
asm_prc.emit(insn_22b(0xe1, 1, 1, 12))
asm_prc.emit_branch21t(0x3c, 1, "have_recs")
asm_prc.emit(insn_11n(0x12, 1, 1))
asm_prc.label("have_recs")
asm_prc.emit(insn_22b(0xd8, 2, 1, 2))
asm_prc.emit(insn_22b(0xe0, 3, 2, 3))
asm_prc.emit(insn_22b(0xd8, 3, 3, 80))
asm_prc.emit(insn_22s(0xd0, 4, 3, 448))
asm_prc.emit(insn_23x(0x90, 6, 4, 0))
asm_prc.emit(insn_22b(0xd8, 6, 6, 4))
asm_prc.emit(insn_22c(0x23, 5, 6, type_idx[T_BA]))

asm_prc.emit(insn_35c(0x71, [8], method_idx[M_TC_PAD32]))
asm_prc.emit(insn_11x(0x0c, 8))
asm_prc.emit(insn_11n(0x12, 6, 0))
asm_prc.emit(insn_21s(0x13, 7, 31))
asm_prc.emit(insn_35c(0x71, [8, 6, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))
asm_prc.emit(insn_21s(0x13, 6, 60))
asm_prc.emit(insn_31i(0x14, 7, 0x424f4f4b))
asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))
asm_prc.emit(insn_21s(0x13, 6, 64))
asm_prc.emit(insn_31i(0x14, 7, 0x4d4f4249))
asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))
asm_prc.emit(insn_21s(0x13, 6, 76))
asm_prc.emit(insn_35c(0x71, [5, 6, 2], method_idx[M_TC_PUT_SHORT]))
asm_prc.emit(insn_21s(0x13, 6, 78))
asm_prc.emit(insn_35c(0x71, [5, 6, 3], method_idx[M_TC_PUT_INT]))

asm_prc.emit(insn_11n(0x12, 6, 0))
asm_prc.label("rec_loop")
asm_prc.emit_branch22t(0x35, 6, 1, "rec_loop_done")
asm_prc.emit(insn_22b(0xe0, 2, 6, 3))
asm_prc.emit(insn_22b(0xd8, 2, 2, 86))
asm_prc.emit(insn_22b(0xe0, 7, 6, 12))
asm_prc.emit(insn_23x(0x90, 7, 4, 7))
asm_prc.emit(insn_35c(0x71, [5, 2, 7], method_idx[M_TC_PUT_INT]))
asm_prc.emit(insn_22b(0xd8, 6, 6, 1))
asm_prc.emit_branch10t(0x28, "rec_loop")
asm_prc.label("rec_loop_done")

asm_prc.emit(insn_22b(0xe0, 2, 1, 3))
asm_prc.emit(insn_22b(0xd8, 2, 2, 86))
asm_prc.emit(insn_23x(0x90, 7, 4, 0))
asm_prc.emit(insn_35c(0x71, [5, 2, 7], method_idx[M_TC_PUT_INT]))

def emit_rec0_int(rel_off, val_int):
    asm_prc.emit(insn_22s(0xd0, 6, 3, rel_off))
    asm_prc.emit(insn_31i(0x14, 7, val_int))
    asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))

def emit_rec0_short(rel_off, val_short):
    asm_prc.emit(insn_22s(0xd0, 6, 3, rel_off))
    asm_prc.emit(insn_21s(0x13, 7, val_short))
    asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_SHORT]))

emit_rec0_short(0, 1)
asm_prc.emit(insn_22s(0xd0, 6, 3, 4))
asm_prc.emit(insn_35c(0x71, [5, 6, 0], method_idx[M_TC_PUT_INT]))
asm_prc.emit(insn_22s(0xd0, 6, 3, 8))
asm_prc.emit(insn_35c(0x71, [5, 6, 1], method_idx[M_TC_PUT_SHORT]))
emit_rec0_short(10, 4096)

emit_rec0_int(16, 0x4d4f4249)
emit_rec0_int(20, 232)
emit_rec0_int(24, 2)
emit_rec0_int(28, 65001)
emit_rec0_int(32, 0x12345678)
emit_rec0_int(36, 6)
for off in range(40, 80, 4):
    emit_rec0_int(off, -1)
asm_prc.emit(insn_22b(0xd8, 7, 1, 1))
asm_prc.emit(insn_22s(0xd0, 6, 3, 80))
asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))
emit_rec0_int(84, 392)
emit_rec0_int(88, 32)
emit_rec0_int(92, 9)
emit_rec0_int(104, 6)
asm_prc.emit(insn_22s(0xd0, 6, 3, 108))
asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))
emit_rec0_int(128, 0x50)
for off in range(164, 192, 4):
    emit_rec0_int(off, -1)
emit_rec0_short(192, 1)
asm_prc.emit(insn_22s(0xd0, 6, 3, 194))
asm_prc.emit(insn_35c(0x71, [5, 6, 1], method_idx[M_TC_PUT_SHORT]))
emit_rec0_int(196, 1)
emit_rec0_int(200, -1)
emit_rec0_int(204, 1)
emit_rec0_int(208, -1)
emit_rec0_int(212, 1)

emit_rec0_int(248, 0x45585448)
emit_rec0_int(252, 144)
emit_rec0_int(256, 5)
emit_rec0_int(260, 100)
emit_rec0_int(264, 40)
asm_prc.emit(insn_35c(0x71, [9], method_idx[M_TC_PAD32]))
asm_prc.emit(insn_11x(0x0c, 9))
asm_prc.emit(insn_11n(0x12, 2, 0))
asm_prc.emit(insn_22s(0xd0, 6, 3, 268))
asm_prc.emit(insn_21s(0x13, 7, 32))
asm_prc.emit(insn_35c(0x71, [9, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))

emit_rec0_int(300, 106)
emit_rec0_int(304, 18)
asm_prc.emit(insn_21c(0x1a, 6, str_idx["2026-01-01"]))
asm_prc.emit(insn_21c(0x1a, 7, str_idx["UTF-8"]))
asm_prc.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_GETBYTES]))
asm_prc.emit(insn_11x(0x0c, 9))
asm_prc.emit(insn_22s(0xd0, 6, 3, 308))
asm_prc.emit(insn_21s(0x13, 7, 10))
asm_prc.emit(insn_35c(0x71, [9, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))

emit_rec0_int(318, 113)
emit_rec0_int(322, 18)
asm_prc.emit(insn_21c(0x1a, 7, str_idx["UTF-8"]))
asm_prc.emit(insn_35c(0x6e, [10, 7], method_idx[M_STR_GETBYTES]))
asm_prc.emit(insn_11x(0x0c, 9))
asm_prc.emit(insn_22s(0xd0, 6, 3, 326))
asm_prc.emit(insn_21s(0x13, 7, 10))
asm_prc.emit(insn_35c(0x71, [9, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))

emit_rec0_int(336, 501)
emit_rec0_int(340, 12)
asm_prc.emit(insn_21c(0x1a, 6, str_idx["EBOK"]))
asm_prc.emit(insn_21c(0x1a, 7, str_idx["UTF-8"]))
asm_prc.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_GETBYTES]))
asm_prc.emit(insn_11x(0x0c, 9))
asm_prc.emit(insn_22s(0xd0, 6, 3, 344))
asm_prc.emit(insn_11n(0x12, 7, 4))
asm_prc.emit(insn_35c(0x71, [9, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))

emit_rec0_int(348, 503)
emit_rec0_int(352, 40)
asm_prc.emit(insn_22s(0xd0, 6, 3, 356))
asm_prc.emit(insn_21s(0x13, 7, 32))
asm_prc.emit(insn_35c(0x71, [8, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))

asm_prc.emit(insn_22s(0xd0, 6, 3, 392))
asm_prc.emit(insn_35c(0x71, [8, 2, 5, 6, 7], method_idx[M_SYS_ARRAYCOPY]))
asm_prc.emit(insn_35c(0x71, [11, 2, 5, 4, 0], method_idx[M_SYS_ARRAYCOPY]))

asm_prc.emit(insn_23x(0x90, 6, 4, 0))
asm_prc.emit(insn_31i(0x14, 7, 0xe98e0d0a))
asm_prc.emit(insn_35c(0x71, [5, 6, 7], method_idx[M_TC_PUT_INT]))
asm_prc.emit(insn_11x(0x11, 5))

code_tc_build_prc = make_code_item(12, 4, 5, asm_prc.assemble())

# transcodeToPrc
asm_tr = Asm()
asm_tr.emit(insn_35c(0x6e, [11], method_idx[M_FILE_EXISTS]))
asm_tr.emit(insn_11x(0x0a, 0))
asm_tr.emit_branch21t(0x39, 0, "ret")
asm_tr.label("try_start")
asm_tr.emit(insn_35c(0x6e, [10], method_idx[M_FILE_GETNAME]))
asm_tr.emit(insn_11x(0x0c, 3))
asm_tr.emit(insn_21c(0x1a, 4, str_idx["Local EPUB Author"]))
asm_tr.emit(insn_21c(0x22, 0, type_idx[T_BAOS]))
asm_tr.emit(insn_35c(0x70, [0], method_idx[M_BAOS_INIT]))
asm_tr.emit(insn_21c(0x1a, 7, str_idx["<html><head></head><body>"]))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["UTF-8"]))
asm_tr.emit(insn_35c(0x6e, [7, 8], method_idx[M_STR_GETBYTES]))
asm_tr.emit(insn_11x(0x0c, 7))
asm_tr.emit(insn_11n(0x12, 8, 0))
asm_tr.emit(insn_12x(0x21, 9, 7))
asm_tr.emit(insn_35c(0x6e, [0, 7, 8, 9], method_idx[M_BAOS_WRITE]))

asm_tr.emit(insn_21c(0x22, 1, type_idx[T_ZIS]))
asm_tr.emit(insn_21c(0x22, 7, type_idx[T_FIS]))
asm_tr.emit(insn_35c(0x70, [7, 10], method_idx[M_FIS_INIT]))
asm_tr.emit(insn_35c(0x70, [1, 7], method_idx[M_ZIS_INIT]))
asm_tr.emit(insn_21s(0x13, 2, 4096))
asm_tr.emit(insn_22c(0x23, 2, 2, type_idx[T_BA]))

asm_tr.label("loop_entry")
asm_tr.emit(insn_35c(0x6e, [1], method_idx[M_ZIS_NEXT]))
asm_tr.emit(insn_11x(0x0c, 6))
asm_tr.emit_branch21t(0x38, 6, "done_zip")
asm_tr.emit(insn_35c(0x6e, [6], method_idx[M_ZE_GETNAME]))
asm_tr.emit(insn_11x(0x0c, 6))
asm_tr.emit(insn_21c(0x62, 7, field_idx[F_LOCALE_US]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_TOLOWER]))
asm_tr.emit(insn_11x(0x0c, 6))

asm_tr.emit(insn_21c(0x1a, 7, str_idx[".opf"]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_ENDSWITH]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x39, 7, "read_entry_bytes")

asm_tr.emit(insn_21c(0x1a, 7, str_idx[".xhtml"]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_ENDSWITH]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x39, 7, "read_entry_bytes")
asm_tr.emit(insn_21c(0x1a, 7, str_idx[".html"]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_ENDSWITH]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x39, 7, "read_entry_bytes")
asm_tr.emit(insn_21c(0x1a, 7, str_idx[".htm"]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_ENDSWITH]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x38, 7, "loop_entry")

asm_tr.label("read_entry_bytes")
asm_tr.emit(insn_21c(0x22, 5, type_idx[T_BAOS]))
asm_tr.emit(insn_35c(0x70, [5], method_idx[M_BAOS_INIT]))
asm_tr.label("read_chunk")
asm_tr.emit(insn_35c(0x6e, [1, 2], method_idx[M_ZIS_READ]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x3d, 7, "entry_read_done")
asm_tr.emit(insn_11n(0x12, 8, 0))
asm_tr.emit(insn_35c(0x6e, [5, 2, 8, 7], method_idx[M_BAOS_WRITE]))
asm_tr.emit_branch10t(0x28, "read_chunk")

asm_tr.label("entry_read_done")
asm_tr.emit(insn_35c(0x6e, [5], method_idx[M_BAOS_TOBA]))
asm_tr.emit(insn_11x(0x0c, 5))
asm_tr.emit(insn_21c(0x1a, 7, str_idx[".opf"]))
asm_tr.emit(insn_35c(0x6e, [6, 7], method_idx[M_STR_ENDSWITH]))
asm_tr.emit(insn_11x(0x0a, 7))
asm_tr.emit_branch21t(0x38, 7, "handle_html_entry")

asm_tr.emit(insn_21c(0x22, 7, type_idx[T_STR]))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["UTF-8"]))
asm_tr.emit(insn_35c(0x70, [7, 5, 8], method_idx[M_STR_INIT_BYTES]))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["<dc:title"]))
asm_tr.emit(insn_21c(0x1a, 9, str_idx["</dc:title>"]))
asm_tr.emit(insn_35c(0x71, [7, 8, 9, 3], method_idx[M_TC_EXTRACT_TAG]))
asm_tr.emit(insn_11x(0x0c, 3))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["<dc:creator"]))
asm_tr.emit(insn_21c(0x1a, 9, str_idx["</dc:creator>"]))
asm_tr.emit(insn_35c(0x71, [7, 8, 9, 4], method_idx[M_TC_EXTRACT_TAG]))
asm_tr.emit(insn_11x(0x0c, 4))
asm_tr.emit_branch10t(0x28, "loop_entry")

asm_tr.label("handle_html_entry")
asm_tr.emit(insn_35c(0x71, [5], method_idx[M_TC_CLEAN_BODY]))
asm_tr.emit(insn_11x(0x0c, 5))
asm_tr.emit(insn_11n(0x12, 8, 0))
asm_tr.emit(insn_12x(0x21, 9, 5))
asm_tr.emit(insn_35c(0x6e, [0, 5, 8, 9], method_idx[M_BAOS_WRITE]))
asm_tr.emit(insn_21c(0x1a, 7, str_idx["<mbp:pagebreak/>"]))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["UTF-8"]))
asm_tr.emit(insn_35c(0x6e, [7, 8], method_idx[M_STR_GETBYTES]))
asm_tr.emit(insn_11x(0x0c, 7))
asm_tr.emit(insn_11n(0x12, 8, 0))
asm_tr.emit(insn_12x(0x21, 9, 7))
asm_tr.emit(insn_35c(0x6e, [0, 7, 8, 9], method_idx[M_BAOS_WRITE]))
asm_tr.emit_branch10t(0x28, "loop_entry")

asm_tr.label("done_zip")
asm_tr.emit(insn_35c(0x6e, [1], method_idx[M_ZIS_CLOSE]))
asm_tr.emit(insn_21c(0x1a, 7, str_idx["</body></html>"]))
asm_tr.emit(insn_21c(0x1a, 8, str_idx["UTF-8"]))
asm_tr.emit(insn_35c(0x6e, [7, 8], method_idx[M_STR_GETBYTES]))
asm_tr.emit(insn_11x(0x0c, 7))
asm_tr.emit(insn_11n(0x12, 8, 0))
asm_tr.emit(insn_12x(0x21, 9, 7))
asm_tr.emit(insn_35c(0x6e, [0, 7, 8, 9], method_idx[M_BAOS_WRITE]))
asm_tr.emit(insn_35c(0x6e, [0], method_idx[M_BAOS_TOBA]))
asm_tr.emit(insn_11x(0x0c, 0))

asm_tr.emit(insn_35c(0x6e, [10], method_idx[M_FILE_GETNAME]))
asm_tr.emit(insn_11x(0x0c, 5))
asm_tr.emit(insn_35c(0x6e, [5], method_idx[M_STR_HASHCODE]))
asm_tr.emit(insn_11x(0x0a, 5))
asm_tr.emit(insn_35c(0x71, [5], method_idx[M_INT_VALUEOF]))
asm_tr.emit(insn_11x(0x0c, 5))
asm_tr.emit(insn_11n(0x12, 6, 1))
asm_tr.emit(insn_22c(0x23, 6, 6, type_idx[T_OA]))
asm_tr.emit(insn_11n(0x12, 7, 0))
asm_tr.emit(insn_23x(0x4d, 5, 6, 7))
asm_tr.emit(insn_21c(0x62, 5, field_idx[F_LOCALE_US]))
asm_tr.emit(insn_21c(0x1a, 7, str_idx["B0%08X"]))
asm_tr.emit(insn_35c(0x71, [5, 7, 6], method_idx[M_STR_FORMAT]))
asm_tr.emit(insn_11x(0x0c, 5))

asm_tr.emit(insn_35c(0x71, [3, 4, 5, 0], method_idx[M_TC_BUILD_PRC]))
asm_tr.emit(insn_11x(0x0c, 0))
asm_tr.emit(insn_21c(0x22, 1, type_idx[T_FOS]))
asm_tr.emit(insn_35c(0x70, [1, 11], method_idx[M_FOS_INIT]))
asm_tr.emit(insn_35c(0x6e, [1, 0], method_idx[M_FOS_WRITE]))
asm_tr.emit(insn_35c(0x6e, [1], method_idx[M_FOS_CLOSE]))
asm_tr.label("try_end")
asm_tr.label("ret")
asm_tr.emit(insn_10x(0x0e))

b_tr = asm_tr.assemble()
tries_tr = struct.pack('<IHH', asm_tr.labels["try_start"], asm_tr.labels["try_end"] - asm_tr.labels["try_start"], 1)
handlers_tr = uleb128(1) + bytes([0x00]) + uleb128(asm_tr.labels["ret"])
code_tc_transcode = make_code_item(12, 2, 4, b_tr, tries_tr, handlers_tr)

# ==============================================================================
# Build Full DEX
# ==============================================================================
type_list_tuples = set(params for _, _, params in sorted_protos if params)
type_list_tuples.add((T_JI_J,))
sorted_type_lists = sorted(type_list_tuples)

code_items_map = {
    M_DP_G_CLINIT: code_dp_g_clinit,
    M_DP_G_INIT: code_dp_g_init,
    M_DP_G_H: code_dp_g_h,
    M_DP_G_I: code_dp_g_i,
    M_DP_G_A: code_dp_g_a,
    M_DP_G_D: code_dp_g_d,
    M_DP_G_E: code_dp_g_e,
    M_EPUB_BB_INIT: code_bb_init,
    M_EPUB_BB_A: code_bb_a,
    M_EPUB_BB_B: code_bb_b,
    M_EPUB_BB_C: code_bb_c,
    M_TC_INIT: code_tc_init,
    M_TC_GET_CACHE: code_tc_get_cache,
    M_TC_REG_LIB: code_tc_reg_lib,
    M_TC_ENSURE_FILE: code_tc_ensure_file,
    M_TC_ENSURE_PATH: code_tc_ensure_path,
    M_TC_IS_EPUB: code_tc_is_epub,
    M_TC_TRANSCODE: code_tc_transcode,
    M_TC_CLEAN_BODY: code_tc_clean_body,
    M_TC_EXTRACT_TAG: code_tc_extract_tag,
    M_TC_PAD32: code_tc_pad32,
    M_TC_BUILD_PRC: code_tc_build_prc,
    M_TC_PUT_INT: code_tc_put_int,
    M_TC_PUT_SHORT: code_tc_put_short,
}

header_size = 0x70
string_ids_off = header_size
string_ids_size = len(sorted_strings)
type_ids_off = string_ids_off + string_ids_size * 4
type_ids_size = len(sorted_types)
proto_ids_off = type_ids_off + type_ids_size * 4
proto_ids_size = len(sorted_protos)
field_ids_off = proto_ids_off + proto_ids_size * 12
field_ids_size = len(sorted_fields)
method_ids_off = field_ids_off + field_ids_size * 8
method_ids_size = len(sorted_methods)
class_defs_off = method_ids_off + method_ids_size * 8
class_defs_size = 3
data_off = class_defs_off + class_defs_size * 32

data_buf = bytearray()
def cur_off(): return data_off + len(data_buf)
def align4():
    while cur_off() % 4 != 0:
        data_buf.append(0)

align4()
type_list_Start = cur_off()
type_list_offs = {}
for tl in sorted_type_lists:
    align4()
    type_list_offs[tl] = cur_off()
    data_buf.extend(struct.pack('<I', len(tl)))
    for t in tl:
        data_buf.extend(struct.pack('<H', type_idx[t]))

align4()
code_item_start = cur_off()
code_item_offs = {}
for m, cbytes in code_items_map.items():
    align4()
    code_item_offs[m] = cur_off()
    data_buf.extend(cbytes)

string_data_start = cur_off()
string_data_offs = {}
for s in sorted_strings:
    string_data_offs[s] = cur_off()
    data_buf.extend(mutf8_encode(s))

def encode_class_data(static_fields, instance_fields, direct_methods, virtual_methods):
    out = bytearray()
    out.extend(uleb128(len(static_fields)))
    out.extend(uleb128(len(instance_fields)))
    out.extend(uleb128(len(direct_methods)))
    out.extend(uleb128(len(virtual_methods)))
    prev = 0
    for f, acc in sorted(static_fields, key=lambda x: field_idx[x[0]]):
        idx = field_idx[f]
        out.extend(uleb128(idx - prev))
        out.extend(uleb128(acc))
        prev = idx
    prev = 0
    for f, acc in sorted(instance_fields, key=lambda x: field_idx[x[0]]):
        idx = field_idx[f]
        out.extend(uleb128(idx - prev))
        out.extend(uleb128(acc))
        prev = idx
    prev = 0
    for m, acc in sorted(direct_methods, key=lambda x: method_idx[x[0]]):
        idx = method_idx[m]
        out.extend(uleb128(idx - prev))
        out.extend(uleb128(acc))
        out.extend(uleb128(code_item_offs[m]))
        prev = idx
    prev = 0
    for m, acc in sorted(virtual_methods, key=lambda x: method_idx[x[0]]):
        idx = method_idx[m]
        out.extend(uleb128(idx - prev))
        out.extend(uleb128(acc))
        out.extend(uleb128(code_item_offs[m]))
        prev = idx
    return bytes(out)

ACC_PUBLIC, ACC_PRIVATE, ACC_STATIC, ACC_FINAL, ACC_CONSTRUCTOR = 0x0001, 0x0002, 0x0008, 0x0010, 0x10000

class_data_start = cur_off()
cd_dp_g_off = cur_off()
data_buf.extend(encode_class_data(
    static_fields=[(F_DP_G_C, ACC_PRIVATE | ACC_STATIC | ACC_FINAL)],
    instance_fields=[],
    direct_methods=[
        (M_DP_G_CLINIT, ACC_STATIC | ACC_CONSTRUCTOR),
        (M_DP_G_INIT, ACC_PUBLIC | ACC_CONSTRUCTOR),
        (M_DP_G_H, ACC_PUBLIC | ACC_STATIC),
        (M_DP_G_I, ACC_PUBLIC | ACC_STATIC),
    ],
    virtual_methods=[(M_DP_G_A, ACC_PUBLIC), (M_DP_G_D, ACC_PUBLIC), (M_DP_G_E, ACC_PUBLIC)]
))

cd_bb_off = cur_off()
data_buf.extend(encode_class_data(
    static_fields=[],
    instance_fields=[(F_EPUB_BB_DEL, ACC_PRIVATE | ACC_FINAL)],
    direct_methods=[(M_EPUB_BB_INIT, ACC_PUBLIC | ACC_CONSTRUCTOR)],
    virtual_methods=[(M_EPUB_BB_A, ACC_PUBLIC), (M_EPUB_BB_B, ACC_PUBLIC), (M_EPUB_BB_C, ACC_PUBLIC)]
))

cd_tc_off = cur_off()
data_buf.extend(encode_class_data(
    static_fields=[],
    instance_fields=[],
    direct_methods=[
        (M_TC_INIT, ACC_PUBLIC | ACC_CONSTRUCTOR),
        (M_TC_GET_CACHE, ACC_PUBLIC | ACC_STATIC),
        (M_TC_REG_LIB, ACC_PUBLIC | ACC_STATIC),
        (M_TC_ENSURE_FILE, ACC_PUBLIC | ACC_STATIC),
        (M_TC_ENSURE_PATH, ACC_PUBLIC | ACC_STATIC),
        (M_TC_IS_EPUB, ACC_PUBLIC | ACC_STATIC),
        (M_TC_TRANSCODE, ACC_PUBLIC | ACC_STATIC),
        (M_TC_CLEAN_BODY, ACC_PUBLIC | ACC_STATIC),
        (M_TC_EXTRACT_TAG, ACC_PUBLIC | ACC_STATIC),
        (M_TC_PAD32, ACC_PUBLIC | ACC_STATIC),
        (M_TC_BUILD_PRC, ACC_PUBLIC | ACC_STATIC),
        (M_TC_PUT_INT, ACC_PUBLIC | ACC_STATIC),
        (M_TC_PUT_SHORT, ACC_PUBLIC | ACC_STATIC),
    ],
    virtual_methods=[]
))

align4()
map_off = cur_off()
map_items = [
    (0x0000, 1, 0),
    (0x0001, string_ids_size, string_ids_off),
    (0x0002, type_ids_size, type_ids_off),
    (0x0003, proto_ids_size, proto_ids_off),
    (0x0004, field_ids_size, field_ids_off),
    (0x0005, method_ids_size, method_ids_off),
    (0x0006, class_defs_size, class_defs_off),
    (0x1001, len(sorted_type_lists), type_list_Start),
    (0x2001, len(code_items_map), code_item_start),
    (0x2002, string_ids_size, string_data_start),
    (0x2000, 3, class_data_start),
    (0x1000, 1, map_off),
]
data_buf.extend(struct.pack('<I', len(map_items)))
for mtype, msize, moff in map_items:
    data_buf.extend(struct.pack('<HHII', mtype, 0, msize, moff))

file_size = cur_off()
data_size = len(data_buf)

dex = bytearray()
dex.extend(struct.pack(
    '<8sI20sIIIIIIIIIIIIIIIIIIII',
    b'dex\n035\x00', 0, b'\x00' * 20, file_size, header_size, 0x12345678,
    0, 0, map_off,
    string_ids_size, string_ids_off,
    type_ids_size, type_ids_off,
    proto_ids_size, proto_ids_off,
    field_ids_size, field_ids_off,
    method_ids_size, method_ids_off,
    class_defs_size, class_defs_off,
    data_size, data_off
))

for s in sorted_strings: dex.extend(struct.pack('<I', string_data_offs[s]))
for t in sorted_types: dex.extend(struct.pack('<I', str_idx[t]))
for shorty, ret, params in sorted_protos:
    p_off = type_list_offs[params] if params else 0
    dex.extend(struct.pack('<III', str_idx[shorty], type_idx[ret], p_off))
for cls, ftype, name in sorted_fields: dex.extend(struct.pack('<HHI', type_idx[cls], type_idx[ftype], str_idx[name]))
for cls, p_tuple, name in sorted_methods: dex.extend(struct.pack('<HHI', type_idx[cls], proto_idx[p_tuple], str_idx[name]))

for ctype, acc, stype, ioff, sidx, aoff, cdoff, svoff in [
    (T_DP_G, ACC_PUBLIC, T_DP_B, 0, 0xFFFFFFFF, 0, cd_dp_g_off, 0),
    (T_EPUB_BB, ACC_PUBLIC | ACC_FINAL, T_OBJ, type_list_offs[(T_JI_J,)], 0xFFFFFFFF, 0, cd_bb_off, 0),
    (T_EPUB_TC, ACC_PUBLIC | ACC_FINAL, T_OBJ, 0, 0xFFFFFFFF, 0, cd_tc_off, 0),
]:
    dex.extend(struct.pack('<IIIIIIII', type_idx[ctype], acc, type_idx[stype], ioff, sidx, aoff, cdoff, svoff))

dex.extend(data_buf)
assert len(dex) == file_size

sha1 = hashlib.sha1(dex[32:]).digest()
dex[12:32] = sha1
adler = zlib.adler32(dex[12:]) & 0xFFFFFFFF
struct.pack_into('<I', dex, 8, adler)

import sys
out_path = sys.argv[1] if len(sys.argv) > 1 else '07_APK_EPUB_INGESTION_MOD/dex/classes9.dex'
os.makedirs(os.path.dirname(os.path.abspath(out_path)), exist_ok=True)
with open(out_path, 'wb') as f:
    f.write(dex)

print(f"Successfully assembled {out_path} ({len(dex):,} bytes)")
