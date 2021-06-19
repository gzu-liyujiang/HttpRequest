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

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/7/6
 */
final class Utils {
    private static final String HTTP_STRATEGY_UA_PART = "LiYuJiang(2021; HttpStrategy/7.0)";

    public static OkHttpClient buildOkHttpClient(@Nullable CookieJar cookieJar, @Nullable ILogger logger) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.readTimeout(8, TimeUnit.SECONDS);
        builder.writeTimeout(8, TimeUnit.SECONDS);
        builder.followRedirects(false);
        builder.followSslRedirects(true);
        builder.addInterceptor(new LoggingInterceptor(logger));
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new UnsafeTrustManager()}, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory());
        } catch (Exception ignore) {
        }
        builder.hostnameVerifier(new UnsafeHostnameVerifier());
        if (cookieJar != null) {
            builder.cookieJar(cookieJar);
        }
        return builder.build();
    }

    public static String getDefaultUserAgent(Context context, String customPart) {
        String ua = "";
        try {
            Class.forName("android.webkit.TracingController");
            // Mozilla/5.0 (Linux; Android 10; V1838A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Safari/537.36
            ua = WebSettings.getDefaultUserAgent(context);
        } catch (Throwable ignore) {
            // ClassNotFoundException: Didn't find class "android.webkit.TracingController"
        }
        if (TextUtils.isEmpty(ua)) {
            // Dalvik/2.1.0 (Linux; U; Android 10; V1838A Build/QP1A.190711.020)
            ua = System.getProperty("http.agent");
        }
        if (customPart == null) {
            customPart = " " + HTTP_STRATEGY_UA_PART;
        } else {
            customPart = " " + customPart.trim() + " " + HTTP_STRATEGY_UA_PART;
        }
        return ua + customPart;
    }

}
