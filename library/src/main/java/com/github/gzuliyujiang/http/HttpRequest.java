package com.github.gzuliyujiang.http;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.lzy.okgo.OkGo;

import java.io.File;
import java.util.List;

public class HttpRequest {
    private static final String MESSAGE = "Please add dependency `runtimeOnly 'com.lzy.net:okgo:3.0.4'` or `runtimeOnly 'com.amitshekhar.android:android-networking:1.0.2'` in your app/build.gradle";
    private static volatile HttpRequest instance;
    private HttpAdapter adapter;

    private HttpRequest() {
    }

    private static HttpRequest getInstance() {
        if (instance == null) {
            synchronized (HttpRequest.class) {
                if (instance == null) {
                    instance = new HttpRequest();
                }
            }
        }
        return instance;
    }

    public static void enableLog() {
        Logger.enable();
    }

    public static void initInApplication(Application application) {
        try {
            Class.forName(OkGo.class.getName());
            setAdapter(new OkGoImpl(application));
        } catch (Throwable th) {
            try {
                Class.forName(AndroidNetworking.class.getName());
                setAdapter(new FastNetworkingImpl(application));
            } catch (Throwable th2) {
            }
        }
    }

    public static void setAdapter(HttpAdapter adapter2) {
        getInstance().adapter = adapter2;
    }

    public static void doGet(String url, Params params, Callback callback) {
        getAdapter().doGet(url, params, callback);
    }

    public static void doPost(String url, Params params, Callback callback) {
        getAdapter().doPost(url, params, callback);
    }

    public static void upload(String url, List<File> files, Callback callback) {
        doPost(url, new MultipartParams(files), callback);
    }

    public static void cancel(Object tag) {
        getAdapter().cancel(tag);
    }

    public static void cancelAll() {
        getAdapter().cancelAll();
    }

    private static HttpAdapter getAdapter() {
        HttpAdapter adapter2 = getInstance().adapter;
        if (adapter2 != null) {
            return adapter2;
        }
        throw new RuntimeException(MESSAGE);
    }
}
