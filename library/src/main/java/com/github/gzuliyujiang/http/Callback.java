package com.github.gzuliyujiang.http;

public abstract class Callback {
    public abstract void onError(int i, Throwable th);

    public abstract void onSuccess(String str);
}
