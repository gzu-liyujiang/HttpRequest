/*
 * Copyright (c) 2013-present, 贵州纳雍穿青人李裕江<1032694760@qq.com>, All Rights Reserved.
 */

package com.github.gzuliyujiang.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 类说明
 *
 * @author 李玉江[QQ:1023694760]
 * @since 2021/3/7 1:47
 */
@Retention(RetentionPolicy.SOURCE)
public @interface HttpMethod {
    String GET = "GET";
    String POST = "POST";
}
