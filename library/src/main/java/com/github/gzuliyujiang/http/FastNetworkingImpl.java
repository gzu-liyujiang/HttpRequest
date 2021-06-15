package com.github.gzuliyujiang.http;

import android.app.Application;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.RequestBuilder;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import java.util.Map;

class FastNetworkingImpl implements HttpAdapter {
    private final Application application;

    public FastNetworkingImpl(Application application2) {
        AndroidNetworking.initialize(application2, Utils.buildOkHttpClient(null));
        this.application = application2;
    }

    @Override
    public void doGet(String url, Params params, Callback callback) {
        query(0, url, params, callback);
    }

    @Override
    public void doPost(String url, Params params, Callback callback) {
        query(1, url, params, callback);
    }

    @Override
    public void upload(String url, MultipartParams params, Callback callback) {
        query(1, url, params, callback);
    }

    private void query(int method, String url, Params params, Callback callback) {
        if (params instanceof MultipartParams) {
            ANRequest.MultiPartBuilder<?> builder = AndroidNetworking.upload(url);
            addHeaderAndQuery(builder, params);
            builder.addMultipartFileList("file", ((MultipartParams) params).toFiles());
            getAsString(builder.build(), callback);
            return;
        }
        ANRequest.DynamicRequestBuilder builder2 = AndroidNetworking.request(url, method);
        if (params != null) {
            addHeaderAndQuery(builder2, params);
            if (params instanceof FormParams) {
                builder2.setContentType("application/x-www-form-urlencoded");
                builder2.addStringBody(((FormParams) params).toBodyString());
            } else if (params instanceof JsonParams) {
                builder2.setContentType("application/json");
                builder2.addStringBody(((JsonParams) params).toBodyJson());
            }
        }
        getAsString(builder2.build(), callback);
    }

    private void getAsString(ANRequest<?> request, final Callback callback) {
        request.getAsString(new StringRequestListener() {
            public void onResponse(String response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            public void onError(ANError anError) {
                if (callback != null) {
                    callback.onError(anError.getErrorCode(), anError);
                }
            }
        });
    }

    private void addHeaderAndQuery(RequestBuilder builder, Params params) {
        builder.setTag(params.getTag());
        builder.setUserAgent(Utils.getDefaultUserAgent(this.application, "FastNetworking/1.0"));
        for (Map.Entry<String, String> entry : params.toHeaderMap().entrySet()) {
            builder.addHeaders(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry2 : params.toBodyMap().entrySet()) {
            builder.addQueryParameter(entry2.getKey(), entry2.getValue());
        }
    }

    @Override
    public void cancel(Object tag) {
        AndroidNetworking.cancel(tag);
    }

    @Override
    public void cancelAll() {
        AndroidNetworking.cancelAll();
    }
}
