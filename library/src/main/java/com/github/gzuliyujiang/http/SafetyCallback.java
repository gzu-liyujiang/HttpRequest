package com.github.gzuliyujiang.http;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public abstract class SafetyCallback extends Callback implements LifecycleEventObserver {
    private Lifecycle.Event lifecycleEvent;

    public abstract void onErrorSafety(int i, Throwable th);

    public abstract void onSuccessSafety(String str);

    public SafetyCallback(FragmentActivity activity) {
        activity.getLifecycle().addObserver(this);
    }

    public SafetyCallback(Fragment fragment) {
        fragment.getLifecycle().addObserver(this);
    }

    @Override // com.github.gzuliyujiang.http.Callback
    public final void onSuccess(String result) {
        if (this.lifecycleEvent != Lifecycle.Event.ON_DESTROY) {
            try {
                onSuccessSafety(result);
            } catch (Throwable e) {
                Logger.print(e);
                onError(-1, e);
            }
        }
    }

    @Override // com.github.gzuliyujiang.http.Callback
    public final void onError(int code, Throwable throwable) {
        if (this.lifecycleEvent != Lifecycle.Event.ON_DESTROY) {
            try {
                onErrorSafety(code, throwable);
            } catch (Throwable e) {
                Logger.print(e);
            }
        }
    }

    @Override // androidx.lifecycle.LifecycleEventObserver
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        this.lifecycleEvent = event;
        if (event == Lifecycle.Event.ON_DESTROY) {
            source.getLifecycle().removeObserver(this);
        }
    }
}
