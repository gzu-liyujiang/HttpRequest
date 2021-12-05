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

import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
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
    public static final String HTTP_STRATEGY_UA_PART = "LiYuJiang(2021; HttpStrategy/7.5)";

    public static OkHttpClient buildOkHttpClient(@Nullable CookieJar cookieJar) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.readTimeout(8, TimeUnit.SECONDS);
        builder.writeTimeout(8, TimeUnit.SECONDS);
        builder.followRedirects(false);
        builder.followSslRedirects(true);
        builder.addInterceptor(new LoggingInterceptor());
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

    public static String buildRequestUrl(RequestApi api) {
        String url = api.url();
        Map<String, Object> queryParameters = api.queryParameters();
        if (queryParameters != null && queryParameters.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> query : queryParameters.entrySet()) {
                String key = query.getKey();
                Object value = query.getValue();
                if (key == null || value == null) {
                    continue;
                }
                if (value instanceof Collection) {
                    Collection<?> collection = (Collection<?>) value;
                    for (Object object : collection) {
                        sb.append(key).append("=").append(object).append("&");
                    }
                } else {
                    sb.append(key).append("=").append(value).append("&");
                }
            }
            sb.deleteCharAt(sb.lastIndexOf("&"));
            if (url.contains("?")) {
                url = url + "&" + sb.toString();
            } else {
                url = url + "?" + sb.toString();
            }
        }
        return url;
    }

    public static String buildRequestBody(RequestApi api) {
        String str = api.bodyToString();
        if (TextUtils.isEmpty(str)) {
            Map<String, Object> bodyParameters = api.bodyParameters();
            if (bodyParameters != null && bodyParameters.size() > 0) {
                if (api.contentType().equals(ContentType.JSON)) {
                    str = new JSONObject(bodyParameters).toString();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Object> body : bodyParameters.entrySet()) {
                        if (body.getKey() == null || body.getValue() == null) {
                            continue;
                        }
                        sb.append(body.getKey()).append("=").append(body.getValue()).append("&");
                    }
                    sb.deleteCharAt(sb.lastIndexOf("&"));
                    str = sb.toString();
                }
            }
        }
        if (TextUtils.isEmpty(str)) {
            str = "";
        }
        return str;
    }

}
