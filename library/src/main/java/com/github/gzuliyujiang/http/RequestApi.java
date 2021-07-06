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
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求接口
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/3/22 10:09
 */
@SuppressWarnings("unused")
public abstract class RequestApi implements Serializable {
    private LifecycleOwner lifecycleOwner;

    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @Nullable
    public String userAgentPart() {
        return null;
    }

    @NonNull
    @ContentType
    public abstract String contentType();

    @NonNull
    @MethodType
    public abstract String methodType();

    @NonNull
    public abstract String url();

    @Nullable
    public abstract Map<String, String> headers();

    /**
     * @see MethodType#POST
     * @see ContentType#MULTIPART
     */
    @Nullable
    public abstract List<File> files();

    /**
     * @see MethodType#GET
     */
    @Nullable
    public abstract Map<String, String> queryParameters();

    /**
     * @see MethodType#POST
     * @see ContentType#FORM
     * @see ContentType#MULTIPART
     */
    @Nullable
    public abstract Map<String, String> bodyParameters();

    /**
     * 注意：使用该方法提交数据会清空实体中其他所有的参数(头信息不清除)
     *
     * @see MethodType#POST
     * @see ContentType#JSON
     * @see ContentType#TEXT
     */
    @Nullable
    public String bodyToString() {
        return null;
    }

    /**
     * 注意：使用该方法提交数据会清空实体中其他所有的参数(头信息不清除)
     *
     * @see MethodType#POST
     * @see ContentType#STREAM
     */
    @Nullable
    public byte[] bodyToBytes() {
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "url=" + url() + ", contentType=" + contentType() + ", methodType=" + methodType();
    }

}
