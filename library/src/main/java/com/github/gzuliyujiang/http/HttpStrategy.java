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
import androidx.annotation.WorkerThread;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/6/23
 */
@SuppressWarnings("unused")
public class HttpStrategy implements IHttpClient {
    private static final String MESSAGE = "Please add dependency `runtimeOnly 'com.lzy.net:okgo:xxx'`" +
            " or `runtimeOnly 'com.amitshekhar.android:android-networking:xxx'` in your app/build.gradle";
    private static final HttpStrategy INSTANCE = new HttpStrategy();
    private IHttpClient strategy;
    private ILogger logger;

    private HttpStrategy() {
        try {
            Class.forName(com.lzy.okgo.OkGo.class.getName());
            strategy = new OkGoImpl();
        } catch (ClassNotFoundException | NoClassDefFoundError e1) {
            try {
                Class.forName(com.androidnetworking.AndroidNetworking.class.getName());
                strategy = new FastNetworkingImpl();
            } catch (ClassNotFoundException | NoClassDefFoundError e2) {
                strategy = new UrlConnectionImpl();
            }
        }
    }

    @NonNull
    public static ILogger getLogger() {
        if (INSTANCE.logger == null) {
            INSTANCE.logger = new ILogger() {
                @Override
                public void printLog(Object log) {

                }
            };
        }
        return INSTANCE.logger;
    }

    public static void setLogger(ILogger logger) {
        INSTANCE.logger = logger;
    }

    public static IHttpClient getDefault() {
        if (INSTANCE.strategy == null) {
            throw new RuntimeException(MESSAGE);
        }
        return INSTANCE.strategy;
    }

    public static void setStrategy(IHttpClient strategy) {
        INSTANCE.strategy = strategy;
    }

    @Override
    public void setup(@NonNull Application application) {
        setup(application, true);
    }

    @Override
    public void setup(@NonNull Application application, boolean allowProxy) {
        getDefault().setup(application, allowProxy);
    }

    @WorkerThread
    @NonNull
    @Override
    public ResponseResult requestSync(@NonNull RequestApi api) {
        return getDefault().requestSync(api);
    }

}
