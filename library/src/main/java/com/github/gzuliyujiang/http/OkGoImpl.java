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

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.BodyRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 参见 https://github.com/jeasonlzy/okhttp-OkGo
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2018/9/14 13:03
 */
final class OkGoImpl implements IHttp, LifecycleEventObserver {
    private Context context;

    @Override
    public void setup(@NonNull Application application, @Nullable ILogger logger) {
        context = application;
        OkLogger.debug(false);
        // See https://github.com/jeasonlzy/okhttp-OkGo/wiki/Init#%E5%85%A8%E5%B1%80%E9%85%8D%E7%BD%AE
        OkGo okGo = OkGo.getInstance();
        okGo.init(application);
        okGo.setOkHttpClient(Utils.buildOkHttpClient(new CookieJarImpl(new SPCookieStore(application)), logger));
        okGo.setRetryCount(1);
        okGo.setCacheMode(CacheMode.DEFAULT);
    }

    @Override
    public void request(@NonNull final HttpOption option) {
        Request<String, ?> request = buildRequest(option);
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                HttpCallback callback = option.getCallback();
                if (callback == null) {
                    return;
                }
                HttpResult result = new HttpResult();
                try {
                    result.setCode(response.code());
                    result.setBody(response.body());
                    result.setHeaders(response.headers().toMultimap());
                    result.setCause(response.getException());
                } catch (Exception e) {
                    result.setCause(e);
                }
                callback.onResult(result);
            }

            @Override
            public void onError(Response<String> response) {
                onSuccess(response);
            }
        });
    }

    @NonNull
    @Override
    public HttpResult requestSync(@NonNull HttpOption option) {
        HttpResult result = new HttpResult();
        Request<String, ?> request = buildRequest(option);
        try {
            okhttp3.Response okHttpResponse = request.execute();
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

    private Request<String, ?> buildRequest(HttpOption option) {
        final LifecycleOwner lifecycleOwner = option.getOwner();
        if (lifecycleOwner != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    lifecycleOwner.getLifecycle().addObserver(OkGoImpl.this);
                }
            });
        }
        HttpApi api = option.getApi();
        Request<String, ?> request;
        if (HttpMethod.GET.equals(api.method())) {
            request = OkGo.get(api.url());
        } else {
            BodyRequest<String, ?> bodyRequest = OkGo.post(api.url());
            List<File> files = api.files();
            int size = files == null ? 0 : files.size();
            if (size > 0) {
                if (size == 1) {
                    bodyRequest.params("file", files.get(0));
                } else {
                    bodyRequest.addFileParams("file", files);
                }
            }
            if (api.contentType().toLowerCase().contains("json")) {
                // 注意使用该方法上传数据会清空实体中其他所有的参数(头信息不清除)
                bodyRequest.upJson(api.bodyToJson());
            }
            request = bodyRequest;
        }
        request.tag(option.getTag());
        Map<String, String> headers = api.headers();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.headers(entry.getKey(), entry.getValue());
            }
        }
        Map<String, String> body = api.bodyToMap();
        if (body.size() > 0) {
            for (Map.Entry<String, String> entry : body.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
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
    public void cancel(@NonNull Object tag) {
        OkGo.getInstance().cancelTag(tag);
    }

    @Override
    public void cancelAll() {
        OkGo.getInstance().cancelAll();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            cancelAll();
            source.getLifecycle().removeObserver(this);
        }
    }

}
