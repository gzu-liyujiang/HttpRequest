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
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.RequestBuilder;
import com.androidnetworking.error.ANError;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * 参见 https://github.com/amitshekhariitbhu/Fast-Android-Networking
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/6/23 13:50
 */
final class FastNetworkingImpl implements IHttpClient, LifecycleEventObserver {
    private Context context;
    private boolean allowProxy;

    @Override
    public void setup(@NonNull Application application) {
        setup(application, true);
    }

    @Override
    public void setup(@NonNull Application application, boolean allowProxy) {
        this.context = application;
        this.allowProxy = allowProxy;
        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application));
        AndroidNetworking.initialize(application, Utils.buildOkHttpClient(cookieJar));
    }

    @NonNull
    @Override
    public ResponseResult requestSync(@NonNull RequestApi api) {
        ResponseResult result = new ResponseResult();
        try {
            Utils.checkHttpProxy(allowProxy);
            ANRequest<?> request = buildRequest(api);
            ANResponse<?> response = request.executeForOkHttpResponse();
            if (response.isSuccess()) {
                Response okHttpResponse = response.getOkHttpResponse();
                result.setHeaders(okHttpResponse.headers().toMultimap());
                if (okHttpResponse.isSuccessful()) {
                    result.setCode(okHttpResponse.code());
                    result.setBody(okHttpResponse.body().bytes());
                } else {
                    throw new ANError("服务器响应异常：" + okHttpResponse.code());
                }
            } else {
                throw response.getError();
            }
        } catch (Throwable e) {
            if (e instanceof ANError) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    result.setCause(new ANError(Utils.wrapErrorMessage(cause)));
                } else {
                    result.setCause(e);
                }
            } else {
                result.setCause(new ANError(Utils.wrapErrorMessage(e)));
            }
        }
        return result;
    }

    private ANRequest<?> buildRequest(@NonNull RequestApi api) {
        final LifecycleOwner lifecycleOwner = api.getLifecycleOwner();
        if (lifecycleOwner != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    lifecycleOwner.getLifecycle().addObserver(FastNetworkingImpl.this);
                }
            });
        }
        String url = Utils.buildRequestUrl(api);
        ANRequest<?> request;
        if (MethodType.GET.equals(api.methodType()) ||
                MethodType.HEAD.equals(api.methodType()) ||
                MethodType.OPTIONS.equals(api.methodType())) {
            ANRequest.GetRequestBuilder<?> getRequestBuilder;
            if (MethodType.HEAD.equals(api.methodType())) {
                getRequestBuilder = AndroidNetworking.head(url);
            } else if (MethodType.OPTIONS.equals(api.methodType())) {
                getRequestBuilder = AndroidNetworking.options(url);
            } else {
                getRequestBuilder = AndroidNetworking.get(url);
            }
            getRequestBuilder.setTag(lifecycleOwner);
            setHeaders(getRequestBuilder, api);
            request = getRequestBuilder.build();
        } else {
            String body = Utils.buildRequestBody(api);
            List<File> files = api.files();
            int size = files == null ? 0 : files.size();
            if (size > 0) {
                ANRequest.MultiPartBuilder<?> multiPartBuilder = AndroidNetworking.upload(url);
                multiPartBuilder.setTag(lifecycleOwner);
                if (size == 1) {
                    multiPartBuilder.addMultipartFile("file", files.get(0));
                } else {
                    multiPartBuilder.addMultipartFileList("file", files);
                }
                setHeaders(multiPartBuilder, api);
                multiPartBuilder.addMultipartParameter(body);
                request = multiPartBuilder.build();
            } else {
                ANRequest.PostRequestBuilder<?> postRequestBuilder;
                if (MethodType.DELETE.equals(api.methodType())) {
                    postRequestBuilder = AndroidNetworking.delete(url);
                } else if (MethodType.PUT.equals(api.methodType())) {
                    postRequestBuilder = AndroidNetworking.put(url);
                } else if (MethodType.PATCH.equals(api.methodType())) {
                    postRequestBuilder = AndroidNetworking.patch(url);
                } else {
                    postRequestBuilder = AndroidNetworking.post(url);
                }
                postRequestBuilder.setTag(lifecycleOwner);
                setHeaders(postRequestBuilder, api);
                postRequestBuilder.setContentType(api.contentType());
                byte[] bodyToBytes = api.bodyToBytes();
                if (bodyToBytes != null) {
                    postRequestBuilder.addByteBody(bodyToBytes);
                } else {
                    postRequestBuilder.addStringBody(body);
                }
                request = postRequestBuilder.build();
            }
        }
        String ua = Utils.getDefaultUserAgent(context, "FastNetworking/" + BuildConfig.VERSION_NAME);
        String userAgentPart = api.userAgentPart();
        if (!TextUtils.isEmpty(userAgentPart)) {
            ua = ua + " " + userAgentPart;
        }
        request.setUserAgent(ua);
        return request;
    }

    private void setHeaders(RequestBuilder builder, RequestApi api) {
        Map<String, String> headers = api.headers();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeaders(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            AndroidNetworking.cancel(source);
            source.getLifecycle().removeObserver(this);
        }
    }

}
