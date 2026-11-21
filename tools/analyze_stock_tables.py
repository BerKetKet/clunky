import sys
sys.path.insert(0, './kfxgen/plugin')
sys.path.insert(0, './kfxgen')
from tests._kfx_introspect import load_fragments, iter_entries

frags = load_fragments('logs/stock_kfx/CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx')
styles = {}
for f in frags:
    if getattr(f, 'ftype', None) == '$157':
        styles[f.fid] = f.value

print(f"Total fragments: {len(frags)}, total styles: {len(styles)}")

# Find storylines with tables
for f in frags:
    if getattr(f, 'ftype', None) == '$259':
        val = f.value
        content = val.get('$146') if hasattr(val, 'get') else []
        for entry in iter_entries(content):
            node_type = entry.get('$159')
            if node_type == '$278':
                print(f"\n=== Found Table in Storyline {f.fid} ===")
                print(f"Table entry keys: {list(entry.keys())}")
                for k, v in entry.items():
                    if k != '$146':
                        print(f"  {k}: {v}")
                table_style = entry.get('$157')
                if table_style in styles:
                    print(f"  Table style ({table_style}): {styles[table_style]}")
                
                # Check children of table (row groups / rows)
                kids = entry.get('$146', [])
                print(f"  Number of direct children in table $146: {len(kids)}")
                for kid_idx, kid in enumerate(kids[:5]):
                    print(f"    Child {kid_idx}: type={kid.get('$159')}, keys={list(kid.keys())}")
                    kid_style = kid.get('$157')
                    if kid_style in styles:
                        print(f"      style ({kid_style}): {styles[kid_style]}")
                    # If child is row group or row, check its kids
                    subkids = kid.get('$146', [])
                    print(f"      subkids count: {len(subkids)}")
                    for sub_idx, sub in enumerate(subkids[:5]):
                        print(f"        Subkid {sub_idx}: type={sub.get('$159')}, keys={list(sub.keys())}")
                        sub_style = sub.get('$157')
                        if sub_style in styles:
                            print(f"          style ({sub_style}): {styles[sub_style]}")
                        # If subkid has kids (e.g. cell holding entries)
                        cell_kids = sub.get('$146', [])
                        if cell_kids:
                            print(f"          cell kids count: {len(cell_kids)}")
                            for ck_idx, ck in enumerate(cell_kids[:2]):
                                print(f"            Cell kid {ck_idx}: type={ck.get('$159')}, style={ck.get('$157')}, text_ref={ck.get('$145')}, keys={list(ck.keys())}")
                break
