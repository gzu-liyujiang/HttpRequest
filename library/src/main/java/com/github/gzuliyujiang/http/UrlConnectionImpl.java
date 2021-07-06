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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
        HttpStrategy.getLogger().printLog("request: " + api);
        ResponseResult result = new ResponseResult();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(buildRequestUrl(api)).openConnection();
            connection.setReadTimeout(TIMEOUT_IN_MILLIONS);
            connection.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(api.methodType());
            buildRequestHeaders(connection, api);
            switch (api.methodType()) {
                case MethodType.GET:
                    connection.setUseCaches(true);
                    break;
                case MethodType.POST:
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    buildRequestBody(connection, api);
                    break;
                default:
                    break;
            }
            int responseCode = connection.getResponseCode();
            result.setCode(responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException(connection.getResponseMessage());
            }
            Map<String, List<String>> headers = connection.getHeaderFields();
            result.setHeaders(headers);
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                int contentLength = connection.getContentLength();
                int len;
                byte[] buf = new byte[contentLength > 0 ? contentLength : 2048];
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
                byte[] data = outputStream.toByteArray();
                outputStream.close();
                HttpStrategy.getLogger().printLog("response: " + headers);
                result.setBody(data);
            } catch (IOException e) {
                result.setCause(e);
            }
        } catch (Exception e) {
            HttpStrategy.getLogger().printLog("response: " + e);
            result.setCause(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String buildRequestUrl(RequestApi api) {
        String url = api.url();
        Map<String, String> queryParameters = api.queryParameters();
        if (queryParameters != null && queryParameters.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> query : queryParameters.entrySet()) {
                if (query.getKey() == null || query.getValue() == null) {
                    continue;
                }
                sb.append(query.getKey()).append("=").append(query.getValue()).append("&");
            }
            sb.deleteCharAt(sb.lastIndexOf("&"));
            if (url.contains("?")) {
                url = url + "&" + sb.toString();
            } else {
                url = url + "?" + sb.toString();
            }
        }
        return url;
    }

    private void buildRequestHeaders(HttpURLConnection connection, RequestApi api) {
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
    }

    private void buildRequestBody(HttpURLConnection connection, RequestApi api) {
        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream)) {
            Map<String, String> bodyParameters = api.bodyParameters();
            if (bodyParameters != null && bodyParameters.size() > 0) {
                if (api.contentType().equals(ContentType.JSON)) {
                    writer.print(new JSONObject(bodyParameters).toString());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> body : bodyParameters.entrySet()) {
                        if (body.getKey() == null || body.getValue() == null) {
                            continue;
                        }
                        sb.append(body.getKey()).append("=").append(body.getValue()).append("&");
                    }
                    sb.deleteCharAt(sb.lastIndexOf("&"));
                    writer.print(sb.toString());
                }
            }
            byte[] bytes = api.bodyToBytes();
            if (bytes != null) {
                outputStream.write(bytes);
            }
            String str = api.bodyToString();
            if (!TextUtils.isEmpty(str)) {
                writer.print(str);
            }
            outputStream.flush();
            writer.flush();
        } catch (Exception e) {
            HttpStrategy.getLogger().printLog(e);
        }
    }

}
