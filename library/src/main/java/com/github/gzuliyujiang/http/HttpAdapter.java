package com.github.gzuliyujiang.http;

public interface HttpAdapter {
    void cancel(Object obj);

    void cancelAll();

    void doGet(String str, Params params, Callback callback);

    void doPost(String str, Params params, Callback callback);

    void upload(String str, MultipartParams multipartParams, Callback callback);
}
