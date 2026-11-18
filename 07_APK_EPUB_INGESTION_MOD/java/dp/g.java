package dp;

import com.amazon.kindle.content.a;
import com.amazon.kindle.epub.EpubBookBuilder;
import com.amazon.kindle.epub.EpubTranscoder;
import com.amazon.kindle.model.content.ContentState;
import hi.b;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import ji.j;

/**
 * Enhanced KRF4 local content loader (dp.g) for com.amazon.kindle v8.159.0.100.
 * Loaded from classes9.dex after removing the original class_def #8367 from classes4.dex.
 */
public class g extends b {
    private static final Set<String> c;

    static {
        HashSet<String> set = new HashSet<>();
        set.add(".epub");
        set.add(".azw1");
        c = set;
    }

    public g(j bookBuilder, b enumerator) {
        super(new EpubBookBuilder(bookBuilder), enumerator);
    }

    public static void h(Collection<String> extensions) {
        c.addAll(extensions);
        c.add(".epub");
        c.add(".azw1");
    }

    public static Collection<String> i() {
        return Collections.unmodifiableCollection(c);
    }

    @Override
    public a a(String userId, File file) {
        return super.a(userId, EpubTranscoder.ensureKindleFile(file));
    }

    @Override
    public Collection<String> d() {
        return i();
    }

    @Override
    public a e(String userId, File file, ContentState state, xt.a bookId) {
        return super.e(userId, EpubTranscoder.ensureKindleFile(file), state, bookId);
    }
}
