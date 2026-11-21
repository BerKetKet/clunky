#!/usr/bin/env python3
"""
Binary DEX patcher for com.amazon.kindle classes2.dex:
- Lcom/amazon/android/docviewer/KindleDocViewerImpl;:
  Patches hasLineSpacingV2Support, hasLetterSpacingSupport, hasParagraphSpacingSupport,
  and hasWordSpacingSupport from returning false (12 00 0f 00) to returning true (12 10 0f 00).
"""
import sys
import struct
import hashlib
import zlib

if len(sys.argv) < 2:
    print("Usage: patch_classes2.py <input_classes2.dex> [output_classes2.dex]")
    sys.exit(1)

src = sys.argv[1]
dst = sys.argv[2] if len(sys.argv) > 2 else src

with open(src, "rb") as f:
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

s_size, s_off = struct.unpack_from("<II", dex, 0x38)
t_size, t_off = struct.unpack_from("<II", dex, 0x40)
m_size, m_off = struct.unpack_from("<II", dex, 0x58)
c_size, c_off = struct.unpack_from("<II", dex, 0x60)

def get_str(idx):
    off = struct.unpack_from("<I", dex, s_off + idx * 4)[0]
    _, pos = read_uleb128(dex, off)
    end = dex.find(b"\x00", pos)
    return dex[pos:end].decode("utf-8", errors="replace")

def get_type(idx):
    return get_str(struct.unpack_from("<I", dex, t_off + idx * 4)[0])

def get_method_name(idx):
    _, _, n_idx = struct.unpack_from("<HHI", dex, m_off + idx * 8)
    return get_str(n_idx)

targets = {"hasLineSpacingV2Support", "hasLetterSpacingSupport", "hasParagraphSpacingSupport", "hasWordSpacingSupport"}
found_targets = set()

for i in range(c_size):
    entry_off = c_off + i * 32
    class_idx, _, _, _, _, _, class_data_off, _ = struct.unpack_from("<IIIIIIII", dex, entry_off)
    if get_type(class_idx) == "Lcom/amazon/android/docviewer/KindleDocViewerImpl;" and class_data_off != 0:
        pos = class_data_off
        sf, pos = read_uleb128(dex, pos)
        inf, pos = read_uleb128(dex, pos)
        dm, pos = read_uleb128(dex, pos)
        vm, pos = read_uleb128(dex, pos)
        for _ in range(sf + inf):
            _, pos = read_uleb128(dex, pos); _, pos = read_uleb128(dex, pos)
        for _ in range(dm):
            _, pos = read_uleb128(dex, pos); _, pos = read_uleb128(dex, pos); _, pos = read_uleb128(dex, pos)
        m_idx = 0
        for _ in range(vm):
            diff, pos = read_uleb128(dex, pos); _, pos = read_uleb128(dex, pos); code_off, pos = read_uleb128(dex, pos)
            m_idx += diff
            mname = get_method_name(m_idx)
            if mname in targets:
                unit_off = code_off + 16
                old_bytes = dex[unit_off:unit_off+4]
                assert old_bytes == b"\x12\x00\x0f\x00", f"Unexpected bytes for {mname}: {old_bytes.hex()}"
                dex[unit_off:unit_off+4] = b"\x12\x10\x0f\x00"
                found_targets.add(mname)
                print(f"Patched KindleDocViewerImpl.{mname} at 0x{unit_off:x}: 12000f00 -> 12100f00 (return true)")

assert found_targets == targets, f"Failed to patch all targets: missing {targets - found_targets}"

sha1 = hashlib.sha1(dex[32:]).digest()
dex[12:32] = sha1
adler = zlib.adler32(dex[12:]) & 0xFFFFFFFF
struct.pack_into("<I", dex, 8, adler)

with open(dst, "wb") as f:
    f.write(dex)
print(f"Wrote patched {dst} ({len(dex):,} bytes)")
