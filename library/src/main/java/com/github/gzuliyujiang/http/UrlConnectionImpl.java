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
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/7/6 11:16
 */
public class UrlConnectionImpl implements IHttpClient {
    private static final int TIMEOUT_IN_MILLIONS = 5000;
    private Context context;

    @Override
    public void setup(@NonNull Application application) {
        this.context = application;
    }

    @NonNull
    @Override
    public ResponseResult requestSync(@NonNull RequestApi api) {
        ResponseResult result = new ResponseResult();
        HttpURLConnection connection = null;
        try {
            long startMillis = System.currentTimeMillis();
            String url = Utils.buildRequestUrl(api);
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(TIMEOUT_IN_MILLIONS);
            connection.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(api.methodType());
            boolean hasBody = false;
            switch (api.methodType()) {
                case MethodType.GET:
                case MethodType.HEAD:
                case MethodType.OPTIONS:
                    connection.setUseCaches(true);
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    break;
                case MethodType.POST:
                case MethodType.PUT:
                case MethodType.DELETE:
                case MethodType.PATCH:
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    hasBody = true;
                    break;
                default:
                    break;
            }
            buildRequestHeadersAndBody(connection, api, hasBody);
            StringBuilder requestLog = new StringBuilder();
            try {
                requestLog.append(api.methodType()).append(" ");
                requestLog.append(url).append("\n");
                requestLog.append("Content-Type: ").append(connection.getRequestProperty("Content-Type")).append("\n");
                byte[] bytes = api.bodyToBytes();
                if (bytes != null) {
                    requestLog.append("Body: binary data, omitted!").append("\n");
                } else {
                    requestLog.append("Body: ").append(buildBodyString(api)).append("\n");
                }
            } catch (Exception e) {
                HttpStrategy.getLogger().printLog(e);
            } finally {
                HttpStrategy.getLogger().printLog(requestLog.toString());
            }
            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();
            }
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            long tookMs = System.currentTimeMillis() - startMillis;
            result.setCode(responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException(connection.getResponseMessage());
            }
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
            result.setHeaders(responseHeaders);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                int contentLength = connection.getContentLength();
                int len;
                byte[] buf = new byte[contentLength > 0 ? contentLength : 2048];
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
                byte[] data = outputStream.toByteArray();
                result.setBody(data);
                StringBuilder responseLog = new StringBuilder();
                try {
                    responseLog.append(responseCode).append(' ');
                    responseLog.append(responseMessage).append(' ');
                    responseLog.append(url);
                    responseLog.append(" (").append(tookMs).append("ms）");
                    // See RFC 2616
                    String statusLine = connection.getHeaderField(0);
                    if (statusLine != null) {
                        responseLog.append(" ").append(statusLine.split(" ")[0]);
                    }
                    responseLog.append("\n");
                    for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                        String key = entry.getKey();
                        if (key != null) {
                            responseLog.append(key).append(": ").append(entry.getValue().get(0)).append("\n");
                        }
                    }
                    String contentType = connection.getContentType().toLowerCase();
                    if (contentType.contains("text") || contentType.contains("form")
                            || contentType.contains("json") || contentType.contains("xml")
                            || contentType.contains("html")) {
                        String charset = connection.getRequestProperty("Charset");
                        if (TextUtils.isEmpty(charset)) {
                            if (contentType.contains("charset=")) {
                                charset = contentType.split("charset=")[1];
                            }
                        }
                        if (TextUtils.isEmpty(charset)) {
                            charset = "UTF-8";
                        }
                        String body = new String(data, charset);
                        responseLog.append("Body: ").append(body).append("\n");
                    } else {
                        responseLog.append("Body: maybe binary body, omitted!").append("\n");
                    }
                } catch (Exception e) {
                    HttpStrategy.getLogger().printLog(e);
                } finally {
                    HttpStrategy.getLogger().printLog(responseLog.toString());
                }
            } catch (IOException e) {
                result.setCause(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    HttpStrategy.getLogger().printLog(e);
                }
            }
        } catch (Throwable e) {
            HttpStrategy.getLogger().printLog(e);
            result.setCause(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private void buildRequestHeadersAndBody(URLConnection connection, RequestApi api, boolean hasBody) {
        List<File> files = api.files();
        if (files != null && files.size() > 0) {
            throw new UnsupportedOperationException("multipart/form-data not supported");
        }
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", api.contentType());
        connection.setRequestProperty("Charset", "UTF-8");
        String ua = Utils.getDefaultUserAgent(context, "HttpConnection/1.0");
        String userAgentPart = api.userAgentPart();
        if (!TextUtils.isEmpty(userAgentPart)) {
            ua = ua + " " + userAgentPart;
        }
        connection.setRequestProperty("User-Agent", ua);
        Map<String, String> headers = api.headers();
        if (headers == null) {
            headers = new ArrayMap<>();
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                continue;
            }
            String key = header.getKey().trim();
            String value = header.getValue().trim();
            connection.setRequestProperty(key, value);
        }
        if (!hasBody) {
            return;
        }
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            byte[] bytes = api.bodyToBytes();
            if (bytes == null) {
                bytes = buildBodyString(api).getBytes();
            }
            dos.write(bytes);
            dos.flush();
        } catch (Exception e) {
            HttpStrategy.getLogger().printLog(e);
        }
    }

    private String buildBodyString(RequestApi api) {
        String str = api.bodyToString();
        if (TextUtils.isEmpty(str)) {
            Map<String, String> bodyParameters = api.bodyParameters();
            if (bodyParameters != null && bodyParameters.size() > 0) {
                if (api.contentType().equals(ContentType.JSON)) {
                    str = new JSONObject(bodyParameters).toString();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> body : bodyParameters.entrySet()) {
                        if (body.getKey() == null || body.getValue() == null) {
                            continue;
                        }
                        sb.append(body.getKey()).append("=").append(body.getValue()).append("&");
                    }
                    sb.deleteCharAt(sb.lastIndexOf("&"));
                    str = sb.toString();
                }
            }
        }
        if (TextUtils.isEmpty(str)) {
            str = "";
        }
        return str;
    }

}
