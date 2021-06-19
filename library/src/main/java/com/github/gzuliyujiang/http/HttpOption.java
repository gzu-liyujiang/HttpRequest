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

import androidx.lifecycle.LifecycleOwner;

import java.util.UUID;

/**
 * HTTP请求条件
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/3/7 1:41
 */
public class HttpOption {
    private final LifecycleOwner owner;
    private final Object tag;
    private final HttpApi api;
    private final HttpCallback callback;

    private HttpOption(HttpOption.Builder builder) {
        this.owner = builder.owner;
        this.tag = builder.tag;
        this.api = builder.api;
        this.callback = builder.callback;
    }

    public static Builder create(String url) {
        return create(new SimpleApi(url));
    }

    public static Builder create(HttpApi api) {
        return new Builder(api);
    }

    public LifecycleOwner getOwner() {
        return owner;
    }

    public Object getTag() {
        return tag;
    }

    public HttpApi getApi() {
        return api;
    }

    public HttpCallback getCallback() {
        return callback;
    }

    public final static class Builder {
        private final HttpApi api;
        private LifecycleOwner owner;
        private Object tag = UUID.randomUUID();
        private HttpCallback callback;

        public Builder(HttpApi api) {
            this.api = api;
        }

        /**
         * {@link androidx.fragment.app.FragmentActivity}或{@link androidx.fragment.app.Fragment}
         */
        public Builder lifecycleOwner(LifecycleOwner owner) {
            this.owner = owner;
            return this;
        }

        /**
         * 通常我们在{@link android.app.Activity}或{@link androidx.fragment.app.Fragment}中做网络请求，
         * 当销毁时要取消请求否则会发生内存泄露，可通过该标记取消该请求。
         */
        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder callback(HttpCallback callback) {
            this.callback = callback;
            return this;
        }

        public HttpOption build() {
            return new HttpOption(this);
        }

    }

}
