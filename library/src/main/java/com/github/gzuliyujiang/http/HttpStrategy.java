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

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/6/23
 */
public class HttpStrategy implements IHttp, LifecycleEventObserver {
    private static final String MESSAGE = "Please add dependency `runtimeOnly 'com.lzy.net:okgo:xxx'`" +
            " or `runtimeOnly 'com.amitshekhar.android:android-networking:xxx'` in your app/build.gradle";
    private static final HttpStrategy INSTANCE = new HttpStrategy();
    private IHttp strategy;

    private HttpStrategy() {
        try {
            Class.forName(com.lzy.okgo.OkGo.class.getName());
            strategy = new OkGoImpl();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            try {
                Class.forName(com.androidnetworking.AndroidNetworking.class.getName());
                strategy = new FastNetworkingImpl();
            } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
            }
        }
    }

    public static IHttp getDefault() {
        if (INSTANCE.strategy == null) {
            throw new RuntimeException(MESSAGE);
        }
        return INSTANCE.strategy;
    }

    public static void setStrategy(IHttp strategy) {
        INSTANCE.strategy = strategy;
    }

    @Override
    public void setup(@NonNull Application application, @Nullable ILogger logger) {
        getDefault().setup(application, logger);
    }

    @UiThread
    @Override
    public void request(@NonNull HttpOption option) {
        getDefault().request(option);
    }

    @WorkerThread
    @NonNull
    @Override
    public HttpResult requestSync(@NonNull HttpOption option) {
        return getDefault().requestSync(option);
    }

    @Override
    public void cancel(@NonNull Object tag) {
        getDefault().cancel(tag);
    }

    @Override
    public void cancelAll() {
        getDefault().cancelAll();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            cancelAll();
            source.getLifecycle().removeObserver(this);
        }
    }

}
