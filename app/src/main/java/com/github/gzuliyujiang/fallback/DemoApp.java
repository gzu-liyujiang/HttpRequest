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

package com.github.gzuliyujiang.fallback;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.github.gzuliyujiang.http.HttpStrategy;
import com.github.gzuliyujiang.http.ILogger;
import com.github.gzuliyujiang.logger.Logger;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/5/15 16:01
 */
public class DemoApp extends Application implements ILogger {

    static {
        Logger.enableConsolePrinter("liyujiang");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HttpStrategy.setLogger(this);
        HttpStrategy.getDefault().setup(this, true);
    }

    @Override
    public void printLog(Object log) {
        Logger.print(log);
    }

}
