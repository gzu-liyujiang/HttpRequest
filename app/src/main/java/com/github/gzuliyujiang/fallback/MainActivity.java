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

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.gzuliyujiang.http.HttpCallback;
import com.github.gzuliyujiang.http.HttpOption;
import com.github.gzuliyujiang.http.HttpStrategy;
import com.github.gzuliyujiang.http.SimpleApi;
import com.github.gzuliyujiang.logger.Logger;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                SimpleApi api = new SimpleApi("http://ip-api.com/json/?lang=zh-CN");
                HttpStrategy.getDefault().request(HttpOption.create(api).callback(new HttpCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Logger.print(result);
                    }

                    @Override
                    public void onError(int code, Throwable throwable) {
                        Logger.print(throwable);
                    }
                }).build());
            }
        });
    }

}
