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

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 拦截器打印日志
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @see com.lzy.okgo.interceptor.HttpLoggingInterceptor
 * @see com.androidnetworking.interceptors.HttpLoggingInterceptor
 * @since 2018/10/17 18:07
 */
final class LoggingInterceptor implements Interceptor {
    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final ILogger logger;

    public LoggingInterceptor(@Nullable ILogger logger) {
        if (logger == null) {
            logger = new ILogger() {
                @Override
                public void printLog(Object log) {

                }
            };
        }
        this.logger = logger;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //请求日志拦截
        logForRequest(request, chain.connection());
        //执行请求，计算请求时间
        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        //响应日志拦截
        return logForResponse(response, tookMs);
    }

    private void logForRequest(Request request, Connection connection) {
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        StringBuilder requestMessage = new StringBuilder();
        try {
            requestMessage.append(request.method()).append(' ');
            requestMessage.append(request.url()).append(' ');
            requestMessage.append(protocol);
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestMessage.append("\nContent-Type: ").append(requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    requestMessage.append("\nContent-Length: ").append(requestBody.contentLength());
                }
            }
            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    requestMessage.append("\n").append(name).append(": ").append(headers.value(i));
                }
            }
            requestMessage.append(" ");
            if (hasRequestBody) {
                MediaType contentType = requestBody.contentType();
                if (isPlaintext(contentType)) {
                    requestMessage.append("\nBody: ").append(bodyToString(request));
                } else {
                    requestMessage.append("\nBody: ");
                    if (contentType != null) {
                        requestMessage.append(contentType.subtype()).append(", ");
                    }
                    requestMessage.append("maybe binary data, omitted!");
                }
            }
        } catch (Exception e) {
            logger.printLog(e);
        } finally {
            logger.printLog(requestMessage.toString());
        }
    }

    private Response logForResponse(Response response, long tookMs) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        StringBuilder responseMessage = new StringBuilder();
        try {
            responseMessage.append(clone.code()).append(' ');
            responseMessage.append(clone.message()).append(' ');
            responseMessage.append(clone.request().url());
            responseMessage.append(" (").append(tookMs).append("ms）");
            Headers headers = clone.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                responseMessage.append("\n").append(headers.name(i)).append(": ").append(headers.value(i));
            }
            responseMessage.append(" ");
            ResponseBody responseBody = clone.body();
            if (responseBody == null) {
                return response;
            }
            MediaType contentType = responseBody.contentType();
            if (isPlaintext(contentType)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                InputStream inputStream = responseBody.byteStream();
                int len;
                byte[] buffer = new byte[4096];
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                byte[] bytes = outputStream.toByteArray();
                String body = new String(bytes, getCharset(contentType));
                responseMessage.append("\nBody: ").append(body);
                responseBody = ResponseBody.create(responseBody.contentType(), bytes);
                return response.newBuilder().body(responseBody).build();
            } else {
                responseMessage.append("\nBody: content type is ");
                responseMessage.append(contentType);
                responseMessage.append(", maybe binary body, omitted!");
            }
        } catch (Exception e) {
            logger.printLog(e);
        } finally {
            logger.printLog(responseMessage.toString());
        }
        return response;
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;
        if (charset == null) {
            charset = UTF8;
        }
        return charset;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (TextUtils.equals(mediaType.type(), "text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            return subtype.contains("x-www-form-urlencoded") || subtype.contains("json")
                    || subtype.contains("xml") || subtype.contains("html");
        }
        return false;
    }

    private String bodyToString(Request request) {
        try {
            Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
            if (body == null) {
                return "";
            }
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            Charset charset = getCharset(body.contentType());
            return buffer.readString(charset);
        } catch (Exception e) {
            logger.printLog(e);
            return "";
        }
    }

}
