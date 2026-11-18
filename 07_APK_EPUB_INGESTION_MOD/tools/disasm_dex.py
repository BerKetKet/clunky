import struct, os

def read_uleb128(data, pos):
    result = 0
    shift = 0
    while True:
        b = data[pos]
        pos += 1
        result |= (b & 0x7f) << shift
        if (b & 0x80) == 0:
            break
        shift += 7
    return result, pos

class DexFile:
    def __init__(self, path):
        with open(path, 'rb') as f:
            self.data = f.read()
        self.string_ids_size, self.string_ids_off = struct.unpack_from('<II', self.data, 0x38)
        self.type_ids_size, self.type_ids_off = struct.unpack_from('<II', self.data, 0x40)
        self.proto_ids_size, self.proto_ids_off = struct.unpack_from('<II', self.data, 0x48)
        self.field_ids_size, self.field_ids_off = struct.unpack_from('<II', self.data, 0x50)
        self.method_ids_size, self.method_ids_off = struct.unpack_from('<II', self.data, 0x58)
        self.class_defs_size, self.class_defs_off = struct.unpack_from('<II', self.data, 0x60)

    def get_string(self, idx):
        if idx >= self.string_ids_size: return f"<str#{idx}>"
        off = struct.unpack_from('<I', self.data, self.string_ids_off + idx * 4)[0]
        _, pos = read_uleb128(self.data, off)
        end = self.data.find(b'\x00', pos)
        return self.data[pos:end].decode('utf-8', errors='replace')

    def get_type(self, idx):
        if idx >= self.type_ids_size: return f"<type#{idx}>"
        s_idx = struct.unpack_from('<I', self.data, self.type_ids_off + idx * 4)[0]
        return self.get_string(s_idx)

    def get_proto(self, idx):
        shorty_idx, return_type_idx, params_off = struct.unpack_from('<III', self.data, self.proto_ids_off + idx * 12)
        ret = self.get_type(return_type_idx)
        params = []
        if params_off != 0:
            sz = struct.unpack_from('<I', self.data, params_off)[0]
            for i in range(sz):
                t_idx = struct.unpack_from('<H', self.data, params_off + 4 + i * 2)[0]
                params.append(self.get_type(t_idx))
        return f"({''.join(params)}){ret}"

    def get_method(self, idx):
        if idx >= self.method_ids_size: return ("", f"<meth#{idx}>", "")
        class_idx, proto_idx, name_idx = struct.unpack_from('<HHI', self.data, self.method_ids_off + idx * 8)
        return self.get_type(class_idx), self.get_string(name_idx), self.get_proto(proto_idx)

    def get_field(self, idx):
        if idx >= self.field_ids_size: return ("", f"<field#{idx}>", "")
        class_idx, type_idx, name_idx = struct.unpack_from('<HHI', self.data, self.field_ids_off + idx * 8)
        return self.get_type(class_idx), self.get_string(name_idx), self.get_type(type_idx)

    def dump_code(self, code_off):
        if code_off == 0:
            return
        registers_size, ins_size, outs_size, tries_size, debug_info_off, insns_size = struct.unpack_from('<HHHHII', self.data, code_off)
        insns = struct.unpack_from(f'<{insns_size}H', self.data, code_off + 16)
        pc = 0
        while pc < insns_size:
            w0 = insns[pc]
            op = w0 & 0xff
            hi = (w0 >> 8) & 0xff
            # decode key opcodes
            if op == 0x00 and w0 >= 0x0100: # payload
                if w0 == 0x0100: # packed-switch
                    sz = insns[pc+1]
                    pc += (sz * 2) + 4
                    continue
                elif w0 == 0x0200: # sparse-switch
                    sz = insns[pc+1]
                    pc += (sz * 4) + 2
                    continue
                elif w0 == 0x0300: # fill-array-data
                    ew = insns[pc+1]
                    sz = insns[pc+2] | (insns[pc+3] << 16)
                    pc += ((sz * ew + 1) // 2) + 4
                    continue
            if op in (0x1a,): # const-string vAA, string@BBBB
                sidx = insns[pc+1]
                print(f"      {pc:04x}: const-string v{hi}, {self.get_string(sidx)!r}")
                pc += 2
            elif op in (0x1b,): # const-string/jumbo
                sidx = insns[pc+1] | (insns[pc+2] << 16)
                print(f"      {pc:04x}: const-string/jumbo v{hi}, {self.get_string(sidx)!r}")
                pc += 3
            elif op in (0x1c, 0x1f, 0x22): # const-class, check-cast, new-instance
                tidx = insns[pc+1]
                oname = {0x1c:"const-class", 0x1f:"check-cast", 0x22:"new-instance"}[op]
                print(f"      {pc:04x}: {oname} v{hi}, {self.get_type(tidx)}")
                pc += 2
            elif op in (0x20, 0x23): # instance-of, new-array
                tidx = insns[pc+1]
                oname = {0x20:"instance-of", 0x23:"new-array"}[op]
                print(f"      {pc:04x}: {oname} (0x{hi:02x}), {self.get_type(tidx)}")
                pc += 2
            elif 0x52 <= op <= 0x6d: # iget/iput/sget/sput
                fidx = insns[pc+1]
                fc, fn, ft = self.get_field(fidx)
                kind = "iget/iput" if op < 0x60 else "sget/sput"
                print(f"      {pc:04x}: {kind}(0x{op:02x}) {fc}->{fn}:{ft}")
                pc += 2
            elif 0x6e <= op <= 0x72: # invoke-kind
                midx = insns[pc+1]
                mc, mn, mp = self.get_method(midx)
                kname = {0x6e:"invoke-virtual", 0x6f:"invoke-super", 0x70:"invoke-direct", 0x71:"invoke-static", 0x72:"invoke-interface"}[op]
                print(f"      {pc:04x}: {kname} {mc}->{mn}{mp}")
                pc += 3
            elif 0x74 <= op <= 0x78: # invoke-kind/range
                midx = insns[pc+1]
                mc, mn, mp = self.get_method(midx)
                kname = {0x74:"invoke-virtual/range", 0x75:"invoke-super/range", 0x76:"invoke-direct/range", 0x77:"invoke-static/range", 0x78:"invoke-interface/range"}[op]
                print(f"      {pc:04x}: {kname} {mc}->{mn}{mp}")
                pc += 3
            else:
                # step size by opcode
                sz1 = {0x00,0x01,0x04,0x07,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,0x10,0x11,0x12,0x1d,0x1e,0x21,0x27,0x28} | set(range(0x7b,0x90)) | set(range(0xb0,0xd0))
                sz2 = {0x02,0x05,0x08,0x13,0x15,0x16,0x19,0x29} | set(range(0x2d,0x3e)) | set(range(0x44,0x52)) | set(range(0x90,0xb0)) | set(range(0xd0,0xe0))
                sz3 = {0x03,0x06,0x09,0x14,0x17,0x24,0x25,0x26,0x2a,0x2b,0x2c}
                sz5 = {0x18}
                if op in sz1: pc += 1
                elif op in sz2: pc += 2
                elif op in sz3: pc += 3
                elif op in sz5: pc += 5
                else: pc += 1

    def dump_classes(self, targets):
        for i in range(self.class_defs_size):
            c_off = self.class_defs_off + i * 32
            class_idx, access_flags, superclass_idx, interfaces_off, source_file_idx, annotations_off, class_data_off, static_values_off = struct.unpack_from('<IIIIIIII', self.data, c_off)
            cname = self.get_type(class_idx)
            if cname in targets and class_data_off != 0:
                print(f"================ {cname} ================")
                pos = class_data_off
                sf_sz, pos = read_uleb128(self.data, pos)
                if_sz, pos = read_uleb128(self.data, pos)
                dm_sz, pos = read_uleb128(self.data, pos)
                vm_sz, pos = read_uleb128(self.data, pos)
                for _ in range(sf_sz + if_sz):
                    _, pos = read_uleb128(self.data, pos)
                    _, pos = read_uleb128(self.data, pos)
                m_idx = 0
                for _ in range(dm_sz):
                    diff, pos = read_uleb128(self.data, pos)
                    acc, pos = read_uleb128(self.data, pos)
                    code_off, pos = read_uleb128(self.data, pos)
                    m_idx += diff
                    _, mn, mp = self.get_method(m_idx)
                    print(f"  [DIRECT] {mn}{mp}")
                    self.dump_code(code_off)
                m_idx = 0
                for _ in range(vm_sz):
                    diff, pos = read_uleb128(self.data, pos)
                    acc, pos = read_uleb128(self.data, pos)
                    code_off, pos = read_uleb128(self.data, pos)
                    m_idx += diff
                    _, mn, mp = self.get_method(m_idx)
                    print(f"  [VIRTUAL] {mn}{mp}")
                    self.dump_code(code_off)

c1 = "/tmp/apk_chunks/kindle_apk_chunk1_dex_and_manifest"
d4 = DexFile(f"{c1}/classes4.dex")
d4.dump_classes({"Ldp/b;", "Ldp/f;", "Ldp/g;", "Ldp/h;", "Ldp/i;"})
d5 = DexFile(f"{c1}/classes5.dex")
d5.dump_classes({"Lxy/s;"})
