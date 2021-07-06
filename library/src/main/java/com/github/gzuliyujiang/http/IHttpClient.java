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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * 面向接口编程，使用接口对各模块进行解耦，增强对第三方库的管控，不强依赖某些三方库，使得三方库可自由搭配组装。
 * <p>
 * 集成第三方HTTP框架（如：Kalle、EasyHttp、okhttp-OkGo、Fast-Android-Networking、android-async-http、Volley），
 * <p>
 * https://github.com/jeasonlzy/okhttp-OkGo
 * https://github.com/yanzhenjie/Kalle
 * https://github.com/getActivity/EasyHttp
 * https://github.com/amitshekhariitbhu/Fast-Android-Networking
 * https://github.com/yanzhenjie/NoHttp
 * https://github.com/loopj/android-async-http
 * https://github.com/litesuits/android-lite-http
 * https://android.googlesource.com/platform/frameworks/volley
 * https://github.com/Konloch/HTTPRequest
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2016/12/31 15:37
 * @since 2020/5/14
 */
public interface IHttpClient {

    @MainThread
    void setup(@NonNull Application application);

    @WorkerThread
    @NonNull
    ResponseResult requestSync(@NonNull RequestApi api);

}
