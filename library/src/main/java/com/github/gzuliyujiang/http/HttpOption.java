/*
 * Copyright (c) 2013-present, 贵州纳雍穿青人李裕江<1032694760@qq.com>, All Rights Reserved.
 */

package com.github.gzuliyujiang.http;

import androidx.lifecycle.LifecycleOwner;

import java.util.UUID;

/**
 * 类说明
 *
 * @author 李玉江[QQ:1023694760]
 * @since 2021/3/7 1:41
 */
public class HttpOption {
    private final LifecycleOwner owner;
    private final Object tag;
    private final HttpApi api;
    private final HttpCallback callback;

    private HttpOption(Builder builder) {
        this.owner = builder.owner;
        this.tag = builder.tag;
        this.api = builder.api;
        this.callback = builder.callback;
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
