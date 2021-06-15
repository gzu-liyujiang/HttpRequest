package com.github.gzuliyujiang.http;

public class FormParams extends QueryParams {
    private final String bodyString;

    public FormParams(String body) {
        this.bodyString = body;
    }

    @Override
    public String toBodyString() {
        String str = this.bodyString;
        if (str != null) {
            return str;
        }
        return super.toBodyString();
    }
}
