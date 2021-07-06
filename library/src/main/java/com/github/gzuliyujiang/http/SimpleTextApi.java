/*
 * Copyright (c) 2016-present 贵州纳雍穿青人李裕江<1032694760@qq.com>
 *
 * The software is licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.github.gzuliyujiang.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/6/15 20:46
 */
public class SimpleTextApi extends RequestApi {
    private final String url;

    public SimpleTextApi(String url) {
        this.url = url;
    }

    @NonNull
    @Override
    public String contentType() {
        return ContentType.TEXT;
    }

    @NonNull
    @Override
    public String methodType() {
        return MethodType.GET;
    }

    @NonNull
    @Override
    public String url() {
        return url;
    }

    @Nullable
    @Override
    public Map<String, String> headers() {
        return null;
    }

    @Nullable
    @Override
    public List<File> files() {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> queryParameters() {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> bodyParameters() {
        return null;
    }

}
