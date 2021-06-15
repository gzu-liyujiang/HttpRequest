package com.github.gzuliyujiang.http;

import android.app.Application;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.BodyRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;
import java.io.File;
import java.util.List;
import java.util.Map;

class OkGoImpl implements HttpAdapter {
    public OkGoImpl(Application application) {
        OkLogger.debug(false);
        OkGo okGo = OkGo.getInstance();
        okGo.init(application);
        okGo.setOkHttpClient(Utils.buildOkHttpClient(new CookieJarImpl(new SPCookieStore(application))));
        okGo.setRetryCount(1);
        okGo.setCacheMode(CacheMode.DEFAULT);
        okGo.addCommonHeaders(new HttpHeaders(HttpHeaders.HEAD_KEY_USER_AGENT, Utils.getDefaultUserAgent(application, "OkGo/3.0.4")));
    }

    @Override // com.github.gzuliyujiang.http.HttpAdapter
    public void doGet(String url, Params params, Callback callback) {
        query(OkGo.get(url), params, callback);
    }

    @Override // com.github.gzuliyujiang.http.HttpAdapter
    public void doPost(String url, Params params, Callback callback) {
        query(OkGo.post(url), params, callback);
    }

    @Override // com.github.gzuliyujiang.http.HttpAdapter
    public void upload(String url, MultipartParams params, Callback callback) {
        query(OkGo.post(url), params, callback);
    }

    private void query(Request<String, ?> request, Params params, final Callback callback) {
        if (params != null) {
            request.tag(params.getTag());
            for (Map.Entry<String, String> entry : params.toHeaderMap().entrySet()) {
                request.headers(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry2 : params.toBodyMap().entrySet()) {
                request.params(entry2.getKey(), entry2.getValue(), new boolean[0]);
            }
        }
        if (request instanceof BodyRequest) {
            BodyRequest<?, ?> bodyRequest = (BodyRequest) request;
            if (params instanceof MultipartParams) {
                List<File> files = ((MultipartParams) params).toFiles();
                if (files != null && files.size() > 0) {
                    if (files.size() == 1) {
                        bodyRequest.params("file", files.get(0));
                    } else {
                        bodyRequest.addFileParams("file", files);
                    }
                }
            } else if (params instanceof JsonParams) {
                bodyRequest.upJson(((JsonParams) params).toBodyJson());
            }
        }
        if (callback == null) {
            request.execute(new StringCallback() {
                @Override // com.lzy.okgo.callback.Callback
                public void onSuccess(Response<String> response) {
                }
            });
        } else {
            request.execute(new StringCallback() {
                /* class com.github.gzuliyujiang.http.OkGoImpl.C03932 */

                @Override // com.lzy.okgo.callback.Callback
                public void onSuccess(Response<String> response) {
                    try {
                        callback.onSuccess(response.body());
                    } catch (Exception e) {
                        Logger.print(e);
                        callback.onError(-1, e);
                    }
                }

                @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                public void onError(Response<String> response) {
                    Throwable throwable = response.getException();
                    try {
                        callback.onError(response.code(), throwable);
                    } catch (Exception e) {
                        Logger.print(e);
                        callback.onError(-1, e);
                    }
                }
            });
        }
    }

    @Override // com.github.gzuliyujiang.http.HttpAdapter
    public void cancel(Object tag) {
        OkGo.getInstance().cancelTag(tag);
    }

    @Override // com.github.gzuliyujiang.http.HttpAdapter
    public void cancelAll() {
        OkGo.getInstance().cancelAll();
    }
}
