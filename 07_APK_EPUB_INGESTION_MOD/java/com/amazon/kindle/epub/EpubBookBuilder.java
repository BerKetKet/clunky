package com.amazon.kindle.epub;

import com.amazon.kindle.krf.KRF.Reader.IDocumentInfo;
import hi.b;
import ji.j;
import xt.d;

/**
 * Decorator around ji.p (LibraryCachedKRFBookBuilder) that transparently transcodes
 * any .epub file or ZIP-magic stream into a cached PalmDB BOOKMOBI (.prc) container
 * before native libKRF.so (EBookPDB / KindleDocumentBuilderMobi) opens it.
 */
public final class EpubBookBuilder implements j {
    private final j a;

    public EpubBookBuilder(j delegate) {
        this.a = delegate;
    }

    @Override
    public d a(b enumerator, String filePath, int flags) {
        return this.a.a(enumerator, EpubTranscoder.ensureKindlePath(filePath), flags);
    }

    @Override
    public IDocumentInfo b(String filePath) {
        return this.a.b(EpubTranscoder.ensureKindlePath(filePath));
    }

    @Override
    public d c(b enumerator, String filePath) {
        return this.a.c(enumerator, EpubTranscoder.ensureKindlePath(filePath));
    }
}
