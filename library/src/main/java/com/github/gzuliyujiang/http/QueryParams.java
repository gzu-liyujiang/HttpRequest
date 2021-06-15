package com.github.gzuliyujiang.http;

import java.util.Map;

public class QueryParams extends Params {
    public String toBodyString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : toBodyMap().entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
    }

    @Override // com.github.gzuliyujiang.http.Params
    public String toString() {
        return toBodyString();
    }
}
