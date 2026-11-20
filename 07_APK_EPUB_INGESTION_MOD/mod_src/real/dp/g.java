package dp;

import com.amazon.kindle.content.a;
import com.amazon.kindle.epub.EpubBookBuilder;
import com.amazon.kindle.epub.EpubTranscoder;
import com.amazon.kindle.model.content.ContentState;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import ji.j;

public class g extends b {
    private static final Set<String> c = new HashSet<String>();

    static {
        c.add(".epub");
        c.add(".azw1");
    }

    public g(j bookBuilder, hi.b enumerator) {
        super(new EpubBookBuilder(bookBuilder), enumerator);
    }

    public static void h(Collection<String> extensions) {
        if (extensions != null) {
            c.addAll(extensions);
        }
        c.add(".epub");
        c.add(".azw1");
    }

    public static Collection<String> i() {
        return Collections.unmodifiableCollection(c);
    }

    @Override
    public a a(String userId, File file) {
        File kindleFile = EpubTranscoder.ensureKindleFile(file);
        a meta = super.a(userId, kindleFile);
        EpubTranscoder.registerInLibrary(userId, file, meta);
        return meta;
    }

    public Collection<String> d() {
        return i();
    }

    @Override
    public a e(String userId, File file, ContentState state, xt.a bookId) {
        File kindleFile = EpubTranscoder.ensureKindleFile(file);
        a meta = super.e(userId, kindleFile, state, bookId);
        EpubTranscoder.registerInLibrary(userId, file, meta);
        return meta;
    }
}
