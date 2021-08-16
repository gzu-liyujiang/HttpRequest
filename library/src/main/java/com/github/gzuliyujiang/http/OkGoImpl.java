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

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.request.base.BodyRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * 参见 https://github.com/jeasonlzy/okhttp-OkGo
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2018/9/14 13:03
 */
final class OkGoImpl implements IHttpClient, LifecycleEventObserver {
    private Context context;

    @Override
    public void setup(@NonNull Application application) {
        context = application;
        OkLogger.debug(false);
        // See https://github.com/jeasonlzy/okhttp-OkGo/wiki/Init#%E5%85%A8%E5%B1%80%E9%85%8D%E7%BD%AE
        OkGo okGo = OkGo.getInstance();
        okGo.init(application);
        okGo.setOkHttpClient(Utils.buildOkHttpClient(new CookieJarImpl(new SPCookieStore(application))));
        okGo.setRetryCount(1);
        okGo.setCacheMode(CacheMode.DEFAULT);
    }

    @NonNull
    @Override
    public ResponseResult requestSync(@NonNull RequestApi api) {
        ResponseResult result = new ResponseResult();
        Request<String, ?> request = buildRequest(api);
        try {
            okhttp3.Response okHttpResponse = request.execute();
            result.setHeaders(okHttpResponse.headers().toMultimap());
            result.setCode(okHttpResponse.code());
            if (okHttpResponse.isSuccessful()) {
                result.setBody(okHttpResponse.body().bytes());
            } else {
                result.setCause(HttpException.COMMON("服务器响应异常：" + okHttpResponse.code()));
            }
        } catch (UnknownHostException e) {
            result.setCause(HttpException.COMMON("网络不可用"));
        } catch (SocketTimeoutException e) {
            result.setCause(HttpException.COMMON("服务器连接超时"));
        } catch (ConnectException e) {
            result.setCause(HttpException.COMMON("服务器连接失败"));
        } catch (Throwable e) {
            result.setCause(e);
        }
        return result;
    }

    private Request<String, ?> buildRequest(@NonNull RequestApi api) {
        final LifecycleOwner lifecycleOwner = api.getLifecycleOwner();
        if (lifecycleOwner != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    lifecycleOwner.getLifecycle().addObserver(OkGoImpl.this);
                }
            });
        }
        String url = Utils.buildRequestUrl(api);
        Request<String, ?> request;
        if (MethodType.GET.equals(api.methodType()) ||
                MethodType.HEAD.equals(api.methodType()) ||
                MethodType.OPTIONS.equals(api.methodType())) {
            if (MethodType.HEAD.equals(api.methodType())) {
                request = OkGo.head(url);
            } else if (MethodType.OPTIONS.equals(api.methodType())) {
                request = OkGo.options(url);
            } else {
                request = OkGo.get(url);
            }
        } else {
            BodyRequest<String, ?> bodyRequest;
            if (MethodType.DELETE.equals(api.methodType())) {
                bodyRequest = OkGo.delete(url);
            } else if (MethodType.PUT.equals(api.methodType())) {
                bodyRequest = OkGo.put(url);
            } else if (MethodType.PATCH.equals(api.methodType())) {
                bodyRequest = OkGo.patch(url);
            } else {
                bodyRequest = OkGo.post(url);
            }
            List<File> files = api.files();
            int size = files == null ? 0 : files.size();
            if (size > 0) {
                if (size == 1) {
                    bodyRequest.params("file", files.get(0));
                } else {
                    bodyRequest.addFileParams("file", files);
                }
            }
            byte[] bodyToBytes = api.bodyToBytes();
            if (bodyToBytes != null) {
                bodyRequest.upBytes(bodyToBytes);
            } else {
                String body = Utils.buildRequestBody(api);
                if (ContentType.JSON.equals(api.contentType())) {
                    bodyRequest.upJson(body);
                } else {
                    bodyRequest.upString(body);
                }
            }
            request = bodyRequest;
        }
        request.tag(lifecycleOwner);
        Map<String, String> headers = api.headers();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.headers(entry.getKey(), entry.getValue());
            }
        }
        String ua = Utils.getDefaultUserAgent(context, "OkGo/" + com.lzy.okgo.BuildConfig.VERSION_NAME);
        String userAgentPart = api.userAgentPart();
        if (!TextUtils.isEmpty(userAgentPart)) {
            ua = ua + " " + userAgentPart;
        }
        request.headers("User-Agent", ua);
        return request;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            OkGo.getInstance().cancelTag(source);
            source.getLifecycle().removeObserver(this);
        }
    }

}
