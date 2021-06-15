package com.github.gzuliyujiang.http;

import org.json.JSONObject;

public class JsonParams extends Params {
    private final String json;

    public JsonParams(String json2) {
        this.json = json2;
    }

    public String toBodyJson() {
        String str = this.json;
        if (str != null) {
            return str;
        }
        return new JSONObject(toBodyMap()).toString();
    }

    @Override
    public String toString() {
        return toBodyJson();
    }
}
