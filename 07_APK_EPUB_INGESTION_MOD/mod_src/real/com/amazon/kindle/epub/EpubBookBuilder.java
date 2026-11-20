package com.amazon.kindle.epub;

import com.amazon.kindle.krf.KRF.Reader.IDocumentInfo;
import hi.b;
import ji.j;
import xt.d;

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
