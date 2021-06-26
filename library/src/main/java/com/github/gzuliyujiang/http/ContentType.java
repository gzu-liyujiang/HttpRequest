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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/6/27 2:13
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ContentType {
    String FORM = "application/x-www-form-urlencoded";
    String MULTIPART = "multipart/form-data";
    String JSON = "application/json;charset=utf-8";
    String TEXT = "text/plain;charset=utf-8";
    String STREAM = "application/octet-stream";
}
