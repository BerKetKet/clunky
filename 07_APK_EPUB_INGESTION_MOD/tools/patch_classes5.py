#!/usr/bin/env python3
"""
Binary DEX patcher for com.amazon.kindle v8.159.0.100 classes5.dex (shifted to classes6.dex in clone):
- Lly/c;->j(I)V (TrialEmptyLibraryController.updateLibraryCount):
  NOPs out unit 0x15 (`0312` -> `const/4 v3, 0`) so that when the user is not signed in
  (`!isAuthenticated() && !isDefaultContentSupported()`), the real local library item count
  from kindle_library.db (USER_ID='MASTER') is passed through to super.j(count) (d7.j),
  hiding EmptyLibraryView and displaying local EPUBs directly in the main Library tab grid.
"""
import sys
import struct
import hashlib
import zlib

if len(sys.argv) < 3:
    print("Usage: patch_classes5.py <input_classes5.dex> <output_classes6.dex>")
    sys.exit(1)

src, dst = sys.argv[1], sys.argv[2]
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

patched = False
for i in range(c_size):
    entry_off = c_off + i * 32
    class_idx, _, _, _, _, _, class_data_off, _ = struct.unpack_from("<IIIIIIII", dex, entry_off)
    if get_type(class_idx) == "Lly/c;" and class_data_off != 0:
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
            diff, pos = read_uleb128(dex, pos)
            _, pos = read_uleb128(dex, pos)
            code_off, pos = read_uleb128(dex, pos)
            m_idx += diff
            if get_method_name(m_idx) == "j":
                unit_off = code_off + 16 + 0x15 * 2
                old_word = struct.unpack_from("<H", dex, unit_off)[0]
                assert old_word == 0x0312, f"Unexpected opcode at Lly/c;->j+0x15: 0x{old_word:04x}"
                struct.pack_into("<H", dex, unit_off, 0x0000) # nop
                patched = True
                print(f"Patched Lly/c;->j(I)V at file offset 0x{unit_off:x}: 0x0312 -> 0x0000 (nop)")
                break
        break

assert patched, "Failed to locate Lly/c;->j(I)V in classes5.dex"

sha1 = hashlib.sha1(dex[32:]).digest()
dex[12:32] = sha1
adler = zlib.adler32(dex[12:]) & 0xFFFFFFFF
struct.pack_into("<I", dex, 8, adler)

with open(dst, "wb") as f:
    f.write(dex)
print(f"Wrote patched {dst} ({len(dex):,} bytes)")
