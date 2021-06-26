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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * 绑定{@link FragmentActivity}及{@link Fragment}生命周期，防止界面销毁后异常
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2020/7/14
 */
@SuppressWarnings("unused")
public abstract class SafetyCallback extends Callback implements LifecycleEventObserver {
    private Lifecycle.Event lifecycleEvent;

    public SafetyCallback(FragmentActivity activity) {
        activity.getLifecycle().addObserver(this);
    }

    public SafetyCallback(Fragment fragment) {
        fragment.getLifecycle().addObserver(this);
    }

    public abstract void onSuccessSafety(String result);

    public abstract void onErrorSafety(int code, Throwable throwable);

    @Override
    public void onResult(@NonNull ResponseResult result) {
        if (lifecycleEvent == Lifecycle.Event.ON_DESTROY) {
            return;
        }
        if (result.isSuccessful()) {
            try {
                onSuccessSafety(result.getBody());
            } catch (Exception e) {
                HttpStrategy.getLogger().printLog(e);
                try {
                    onErrorSafety(-1, e);
                } catch (Exception e2) {
                    HttpStrategy.getLogger().printLog(e2);
                }
            }
            return;
        }
        try {
            onErrorSafety(result.getCode(), result.getCause());
        } catch (Exception e) {
            HttpStrategy.getLogger().printLog(e);
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        lifecycleEvent = event;
        if (event == Lifecycle.Event.ON_DESTROY) {
            source.getLifecycle().removeObserver(this);
        }
    }

}

