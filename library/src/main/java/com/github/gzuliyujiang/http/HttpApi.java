/*
 * Copyright (c) 2013-present, 贵州纳雍穿青人李裕江<1032694760@qq.com>, All Rights Reserved.
 */

package com.github.gzuliyujiang.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.github.gzuliyujiang.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP接口
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/3/22 10:09
 */
public abstract class HttpApi implements Serializable {

    @Nullable
    public String userAgentPart() {
        return null;
    }

    @NonNull
    public abstract String contentType();

    @NonNull
    @HttpMethod
    public abstract String method();

    @NonNull
    public abstract String url();

    @Nullable
    public Map<String, String> headers() {
        return null;
    }

    @Nullable
    public List<File> files() {
        return null;
    }

    @NonNull
    public abstract String bodyToJson();

    @NonNull
    public Map<String, String> bodyToMap() {
        Map<String, String> map = new ArrayMap<>();
        try {
            JSONObject jsonObject = new JSONObject(bodyToJson());
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                map.put(key, jsonObject.optString(key));
            }
        } catch (JSONException e) {
            Logger.print(e);
        }
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return bodyToJson();
    }

}
