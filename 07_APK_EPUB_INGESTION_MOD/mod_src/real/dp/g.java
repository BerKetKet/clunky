package dp;

import com.amazon.kcp.application.j4;
import com.amazon.kcp.util.Utils;
import com.amazon.kindle.content.ILibraryService;
import com.amazon.kindle.content.a;
import com.amazon.kindle.epub.EpubBookBuilder;
import com.amazon.kindle.epub.EpubTranscoder;
import com.amazon.kindle.model.content.ContentState;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ji.j;

public class g extends b {
    private static final Set<String> c = new HashSet<String>();
    private static volatile g sInstance = null;
    private static volatile String sLastUserId = null;

    static {
        c.add(".epub");
        c.add(".azw1");
    }

    public g(j bookBuilder, hi.b enumerator) {
        super(new EpubBookBuilder(bookBuilder), enumerator);
        sInstance = this;
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
        if (userId != null && !userId.isEmpty()) sLastUserId = userId;
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
        if (userId != null && !userId.isEmpty()) sLastUserId = userId;
        File kindleFile = EpubTranscoder.ensureKindleFile(file);
        a meta = super.e(userId, kindleFile, state, bookId);
        EpubTranscoder.registerInLibrary(userId, file, meta);
        return meta;
    }

    /**
     * Immediately injects a newly converted .kfx file into Kindle's live in-memory ILibraryService
     * so the Library tab displays the book immediately without requiring an app relaunch.
     */
    public static boolean ingestKfxLive(File kfxFile) {
        if (kfxFile == null || !kfxFile.exists()) return false;
        try {
            g inst = sInstance;
            String uid = sLastUserId;
            if (inst == null) return false;
            a meta = inst.a(uid, kfxFile);
            if (meta == null) return false;
            j4 factory = Utils.getFactory();
            if (factory == null) return false;
            ILibraryService lib = factory.getLibraryService();
            if (lib == null) return false;
            List<a> single = Collections.singletonList(meta);
            lib.addContentMetadata(single);
            if (uid != null && !uid.isEmpty()) {
                String path = meta.e1();
                if (path == null || lib.W0(uid, path) == null) {
                    lib.W(uid, single, false);
                }
            }
            android.util.Log.i("AzathLiveIngest", "Live-registered KFX in ILibraryService: " + kfxFile.getName());
            return true;
        } catch (Throwable t) {
            android.util.Log.w("AzathLiveIngest", "ingestKfxLive error: " + t.getMessage());
            return false;
        }
    }
}
