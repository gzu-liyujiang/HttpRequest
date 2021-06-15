package com.github.gzuliyujiang.http;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipartParams extends QueryParams {
    private List<File> files;

    public MultipartParams(File file) {
        this(Collections.singletonList(file));
    }

    public MultipartParams(List<File> files2) {
        ArrayList arrayList = new ArrayList();
        this.files = arrayList;
        arrayList.addAll(files2);
    }

    public List<File> toFiles() {
        return this.files;
    }
}
