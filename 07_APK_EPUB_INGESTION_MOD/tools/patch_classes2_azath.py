#!/usr/bin/env python3
"""
Binary DEX patcher for com.azath.reader classes2.dex:
Patches Compose Dark Color Palette (Lib/b;.<clinit>) primary accent token
from Kindle Blue (0xff2491ffL) to Azath Ember Crimson (0xffe05a47L).
Idempotent: safe to run repeatedly on already-patched classes2.dex.
"""
import sys
import struct
import hashlib
import zlib

if len(sys.argv) < 2:
    print("Usage: patch_classes2_azath.py <input_classes2.dex> [output_classes2.dex]")
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

OLD_BLUE = bytes([0xff, 0x91, 0x24, 0xff, 0x00, 0x00, 0x00, 0x00])     # 0xff2491ffL LE
OLD_CRIMSON = bytes([0x47, 0x5a, 0xe0, 0xff, 0x00, 0x00, 0x00, 0x00])  # 0xffe05a47L LE
NEW_LIT = bytes([0xb8, 0xc9, 0xd4, 0xff, 0x00, 0x00, 0x00, 0x00])       # 0xffd4c9b8L LE (Limestone)

patched = False
for i in range(c_size):
    entry_off = c_off + i * 32
    class_idx, _, _, _, _, _, class_data_off, _ = struct.unpack_from("<IIIIIIII", dex, entry_off)
    if get_type(class_idx) == "Lib/b;" and class_data_off != 0:
        pos = class_data_off
        sf, pos = read_uleb128(dex, pos)
        inf, pos = read_uleb128(dex, pos)
        dm, pos = read_uleb128(dex, pos)
        vm, pos = read_uleb128(dex, pos)
        for _ in range(sf + inf):
            _, pos = read_uleb128(dex, pos)
            _, pos = read_uleb128(dex, pos)
        m_idx = 0
        for _ in range(dm):
            diff, pos = read_uleb128(dex, pos)
            _, pos = read_uleb128(dex, pos)
            code_off, pos = read_uleb128(dex, pos)
            m_idx += diff
            if get_method_name(m_idx) == "<clinit>" and code_off != 0:
                insns_size = struct.unpack_from("<I", dex, code_off + 12)[0]
                insns_start = code_off + 16
                insns_end = insns_start + insns_size * 2
                slice_bytes = bytes(dex[insns_start:insns_end])
                idx = slice_bytes.find(OLD_BLUE)
                if idx == -1:
                    idx = slice_bytes.find(OLD_CRIMSON)
                if idx != -1:
                    dex[insns_start + idx : insns_start + idx + 8] = NEW_LIT
                    patched = True
                    print(f"Patched Lib/b.<clinit> at 0x{insns_start + idx:x} -> 0xffd4c9b8L (Limestone)")
                elif NEW_LIT in slice_bytes:
                    print("Lib/b.<clinit> already patched to 0xffd4c9b8L.")
                else:
                    print("WARNING: Neither old nor new literal found in Lib/b.<clinit>!", file=sys.stderr)
                    sys.exit(1)
        break

if patched:
    sha1 = hashlib.sha1(dex[32:]).digest()
    dex[12:32] = sha1
    adler = zlib.adler32(dex[12:]) & 0xFFFFFFFF
    struct.pack_into("<I", dex, 8, adler)
    with open(dst, "wb") as f:
        f.write(dex)
    print(f"Wrote patched {dst} ({len(dex):,} bytes)")
