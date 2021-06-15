package com.github.gzuliyujiang.http;

import android.text.TextUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Params {
    private ConcurrentHashMap<String, String> body = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> header = new ConcurrentHashMap<>();
    private Object tag = UUID.randomUUID();

    public void setTag(Object tag2) {
        this.tag = tag2;
    }

    public final Object getTag() {
        return this.tag;
    }

    public void putHeader(String key, String value) {
        if (value == null) {
            value = "";
        }
        if (!TextUtils.isEmpty(key)) {
            this.header.put(key, value);
        }
    }

    public void putBody(String key, String value) {
        if (value == null) {
            value = "";
        }
        if (!TextUtils.isEmpty(key)) {
            this.body.put(key, value);
        }
    }

    public Map<String, String> toHeaderMap() {
        for (Map.Entry<String, String> entry : this.header.entrySet()) {
            if (entry.getValue() == null) {
                this.header.remove(entry.getKey());
            }
        }
        return this.header;
    }

    public Map<String, String> toBodyMap() {
        for (Map.Entry<String, String> entry : this.body.entrySet()) {
            if (entry.getValue() == null) {
                this.body.remove(entry.getKey());
            }
        }
        return this.body;
    }

    public void clearHeader() {
        this.header.clear();
    }

    public void clearBody() {
        this.body.clear();
    }

    public String toString() {
        return "{header=" + this.header + ", body=" + this.body + "}";
    }
}
