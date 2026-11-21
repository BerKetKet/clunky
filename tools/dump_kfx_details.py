import sys
sys.path.insert(0, './kfxgen/plugin')
sys.path.insert(0, './kfxgen')
from tests._kfx_introspect import load_fragments, iter_entries

# 1. Analyze Kindle Tables
frags = load_fragments('logs/stock_kfx/CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx')
styles = {f.fid: f.value for f in frags if f.ftype == '$157'}

print("=== Table Styles ===")
for s in ['s1H', 's2F', 'sY', 's13', 'sBV', 'sCN']:
    if s in styles:
        print(f"Style {s}: {dict(styles[s])}")

# 2. Analyze Cover Structure in Homer's Odyssey (CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx)
odyssey = load_fragments('logs/stock_kfx/CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx')
print("\n=== Odyssey Fragments Summary ===")
od_types = {}
for f in odyssey:
    t = str(f.ftype)
    od_types[t] = od_types.get(t, 0) + 1
print(od_types)

print("\n=== Odyssey Section ($260) Fragments ===")
for f in odyssey:
    if f.ftype == '$260':
        print(f"Section {f.fid}: {dict(f.value) if hasattr(f.value, 'items') else f.value}")

print("\n=== Odyssey Storyline ($259) for Cover ===")
for f in odyssey:
    if f.ftype == '$259':
        val = f.value
        content = val.get('$146', []) if hasattr(val, 'get') else []
        for entry in iter_entries(content):
            if entry.get('$159') == '$270' or entry.get('$159') == '$271' or 'cover' in str(entry):
                print(f"Storyline {f.fid} entry: {dict(entry)}")
                style_id = entry.get('$157')
                print(f"  Style for entry ({style_id})")

print("\n=== Odyssey Metadata ($490 / $258 / $495 / $593) ===")
for f in odyssey:
    if f.ftype in ['$490', '$258', '$495', '$593', '$389', '$270']:
        print(f"Fragment {f.ftype} ({f.fid}): {dict(f.value) if hasattr(f.value, 'items') else f.value}")

