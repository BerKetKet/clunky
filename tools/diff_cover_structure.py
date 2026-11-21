import sys
sys.path.insert(0, './kfxgen/plugin')
sys.path.insert(0, './kfxgen')
from tests._kfx_introspect import load_fragments

odyssey = {f.fid: f for f in load_fragments('logs/stock_kfx/CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx')}
hesiod = {f.fid: f for f in load_fragments('logs/hesiod_on_device.kfx')}

print("=== Section 0 Comparison ===")
print("Odyssey Section 0 ($260):", odyssey.get('c0').value)
print("Hesiod  Section 0 ($260):", hesiod.get('c_cover').value)

print("\n=== Storyline 0 Comparison ===")
print("Odyssey Storyline 0 ($259):", odyssey.get('l4').value)
print("Hesiod  Storyline 0 ($259):", hesiod.get('c_cover-story').value)

print("\n=== Reading Order ($258) ===")
print("Odyssey $258:", [f.value for f in odyssey.values() if f.ftype == '$258'][0])
print("Hesiod  $258:", [f.value for f in hesiod.values() if f.ftype == '$258'][0]['$169'][0]['$170'][:5])

print("\n=== Document Data ($538) ===")
print("Odyssey $538:", [f.value for f in odyssey.values() if f.ftype == '$538'][0])
print("Hesiod  $538:", [f.value for f in hesiod.values() if f.ftype == '$538'][0])

print("\n=== Position Map ($264 / $265) for Section 0 ===")
print("Odyssey $264:", [f.value for f in odyssey.values() if f.ftype == '$264'][0])
print("Hesiod  $264:", [f.value for f in hesiod.values() if f.ftype == '$264'][0])

print("Odyssey $265:", [f.value for f in odyssey.values() if f.ftype == '$265'][0])
print("Hesiod  $265:", [f.value for f in hesiod.values() if f.ftype == '$265'][0])

print("\n=== Location Map ($550) ===")
print("Odyssey $550:", [f.value for f in odyssey.values() if f.ftype == '$550'][0])
print("Hesiod  $550:", [f.value for f in hesiod.values() if f.ftype == '$550'][0])

print("\n=== Section Position Id Map ($609) ===")
print("Odyssey $609 for c0:", odyssey.get('c0') if 'c0' in odyssey else None)
for f in odyssey.values():
    if f.ftype == '$609' and f.fid == 'c0':
        print("  Odyssey c0 $609:", f.value)
for f in hesiod.values():
    if f.ftype == '$609' and f.fid == 'c_cover':
        print("  Hesiod c_cover $609:", f.value)
