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
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.RequestBuilder;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.lzy.okgo.exception.HttpException;

import org.json.JSONObject;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

/**
 * 参见 https://github.com/amitshekhariitbhu/Fast-Android-Networking
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/6/23 13:50
 */
final class FastNetworkingImpl implements IHttp, LifecycleEventObserver {
    private Context context;

    @Override
    public void setup(@NonNull Application application) {
        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application));
        AndroidNetworking.initialize(application, Utils.buildOkHttpClient(cookieJar));
        this.context = application;
    }

    @Override
    public void request(@NonNull HttpApi api, final @Nullable HttpCallback callback) {
        ANRequest<?> request = buildRequest(api);
        request.getAsOkHttpResponse(new OkHttpResponseListener() {
            @Override
            public void onResponse(Response response) {
                if (callback == null) {
                    return;
                }
                HttpResult result = new HttpResult();
                try {
                    result.setCode(response.code());
                    result.setHeaders(response.headers().toMultimap());
                    result.setBody(Objects.requireNonNull(response.body()).string());
                } catch (Exception e) {
                    result.setCause(e);
                }
                callback.onResult(result);
            }

            @Override
            public void onError(ANError anError) {
                if (callback == null) {
                    return;
                }
                HttpResult result = new HttpResult();
                try {
                    result.setCode(anError.getErrorCode());
                    Response response = anError.getResponse();
                    result.setHeaders(response.headers().toMultimap());
                    result.setBody(Objects.requireNonNull(response.body()).string());
                    result.setCause(anError.getCause());
                } catch (Exception e) {
                    result.setCause(e);
                }
                callback.onResult(result);
            }
        });
    }

    @NonNull
    @Override
    public HttpResult requestSync(@NonNull HttpApi api) {
        HttpResult result = new HttpResult();
        try {
            ANRequest<?> request = buildRequest(api);
            ANResponse<?> response = request.executeForOkHttpResponse();
            Response okHttpResponse = response.getOkHttpResponse();
            result.setHeaders(okHttpResponse.headers().toMultimap());
            result.setCode(okHttpResponse.code());
            if (okHttpResponse.isSuccessful()) {
                result.setBody(Objects.requireNonNull(okHttpResponse.body()).string());
            } else {
                result.setCause(HttpException.COMMON("服务器响应异常：" + okHttpResponse.code()));
            }
        } catch (SocketTimeoutException e) {
            result.setCause(HttpException.COMMON("服务器连接超时"));
        } catch (ConnectException e) {
            result.setCause(HttpException.COMMON("服务器连接失败"));
        } catch (Exception e) {
            result.setCause(e);
        }
        return result;
    }

    private ANRequest<?> buildRequest(@NonNull HttpApi api) {
        final LifecycleOwner lifecycleOwner = api.getLifecycleOwner();
        if (lifecycleOwner != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    lifecycleOwner.getLifecycle().addObserver(FastNetworkingImpl.this);
                }
            });
        }
        ANRequest<?> request;
        List<File> files = api.files();
        if (files != null && files.size() > 0) {
            ANRequest.MultiPartBuilder<?> builder = AndroidNetworking.upload(api.url());
            builder.setTag(api.getRequestTag());
            builder.addMultipartFileList("file", files);
            setHeaderAndBody(builder, api);
            request = builder.build();
        } else if (HttpMethod.GET.equals(api.method())) {
            ANRequest.GetRequestBuilder<?> builder = AndroidNetworking.get(api.url());
            builder.setTag(api.getRequestTag());
            setHeaderAndBody(builder, api);
            request = builder.build();
        } else {
            ANRequest.PostRequestBuilder<?> builder = AndroidNetworking.post(api.url());
            builder.setTag(api.getRequestTag());
            setHeaderAndBody(builder, api);
            if (api.contentType().toLowerCase().contains("json")) {
                // 注意使用该方法上传数据会清空实体中其他所有的参数(头信息不清除)
                builder.setContentType("application/json");
                builder.addStringBody(new JSONObject(api.bodyToMap()).toString());
            }
            request = builder.build();
        }
        String ua = Utils.getDefaultUserAgent(context, "FastNetworking/" + BuildConfig.VERSION_NAME);
        String userAgentPart = api.userAgentPart();
        if (!TextUtils.isEmpty(userAgentPart)) {
            ua = ua + " " + userAgentPart;
        }
        request.setUserAgent(ua);
        return request;
    }

    private void setHeaderAndBody(RequestBuilder builder, HttpApi api) {
        Map<String, String> headers = api.headers();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeaders(entry.getKey(), entry.getValue());
            }
        }
        Map<String, String> map = api.bodyToMap();
        if (map.size() > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void cancel(@NonNull Object tag) {
        AndroidNetworking.cancel(tag);
    }

    @Override
    public void cancelAll() {
        AndroidNetworking.cancelAll();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            cancelAll();
            source.getLifecycle().removeObserver(this);
        }
    }

}
