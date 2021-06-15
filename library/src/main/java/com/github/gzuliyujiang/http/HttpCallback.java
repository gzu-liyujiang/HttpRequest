/*
 * Copyright (c) 2013-present, 贵州纳雍穿青人李裕江<1032694760@qq.com>, All Rights Reserved.
 */

package com.github.gzuliyujiang.http;

/**
 * HTTP请求回调
 * Created by liyujiang on 2020/7/14.
 */
public abstract class HttpCallback {

    public abstract void onSuccess(String result);

    public abstract void onError(int code, Throwable throwable);

}
