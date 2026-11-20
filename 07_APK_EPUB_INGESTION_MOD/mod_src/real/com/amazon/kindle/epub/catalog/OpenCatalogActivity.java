package com.amazon.kindle.epub.catalog;

import android.app.Activity;
import android.os.Bundle;

public final class OpenCatalogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(OpenCatalogView.buildCatalogView(this));
    }
}
