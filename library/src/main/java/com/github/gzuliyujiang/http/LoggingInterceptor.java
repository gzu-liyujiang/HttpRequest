package com.github.gzuliyujiang.http;

import com.lzy.okgo.model.HttpHeaders;

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

public class LoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        logForRequest(request, chain.connection());
        return logForResponse(chain.proceed(request), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - System.nanoTime()));
    }

    private void logForRequest(Request request, Connection connection) {
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        StringBuilder requestMessage = new StringBuilder();
        try {
            requestMessage.append(request.method());
            requestMessage.append(' ');
            requestMessage.append(request.url());
            requestMessage.append(' ');
            requestMessage.append(protocol);
            if (hasRequestBody) {
                if (requestBody.contentType() != null) {
                    requestMessage.append("\nContent-Type: ");
                    requestMessage.append(requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    requestMessage.append("\nContent-Length: ");
                    requestMessage.append(requestBody.contentLength());
                }
            }
            Headers headers = request.headers();
            int count = headers.size();
            for (int i = 0; i < count; i++) {
                String name = headers.name(i);
                if (!HttpHeaders.HEAD_KEY_CONTENT_TYPE.equalsIgnoreCase(name) && !HttpHeaders.HEAD_KEY_CONTENT_LENGTH.equalsIgnoreCase(name)) {
                    requestMessage.append("\n");
                    requestMessage.append(name);
                    requestMessage.append(": ");
                    requestMessage.append(headers.value(i));
                }
            }
            requestMessage.append(" ");
            if (hasRequestBody) {
                MediaType contentType = requestBody.contentType();
                if (isPlaintext(contentType)) {
                    requestMessage.append("\nBody: ");
                    requestMessage.append(bodyToString(request));
                } else {
                    requestMessage.append("\nBody: ");
                    if (contentType != null) {
                        requestMessage.append(contentType.subtype());
                        requestMessage.append(", ");
                    }
                    requestMessage.append("maybe binary data, omitted!");
                }
            }
        } catch (Exception e) {
            Logger.print(e);
        } catch (Throwable th) {
            Logger.print(requestMessage.toString());
            throw th;
        }
        Logger.print(requestMessage.toString());
    }

    private Response logForResponse(Response response, long tookMs) {
        Response clone = response.newBuilder().build();
        ResponseBody responseBody = clone.body();
        StringBuilder responseMessage = new StringBuilder();
        try {
            responseMessage.append(clone.code());
            responseMessage.append(' ');
            responseMessage.append(clone.message());
            responseMessage.append(' ');
            responseMessage.append(clone.request().url());
            responseMessage.append(" (");
            responseMessage.append(tookMs);
            responseMessage.append("ms）");
            Headers headers = clone.headers();
            int count = headers.size();
            for (int i = 0; i < count; i++) {
                responseMessage.append("\n");
                responseMessage.append(headers.name(i));
                responseMessage.append(": ");
                responseMessage.append(headers.value(i));
            }
            responseMessage.append(" ");
            if (okhttp3.internal.http.HttpHeaders.hasBody(clone)) {
                if (responseBody == null) {
                    Logger.print(responseMessage.toString());
                    return response;
                }
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    responseMessage.append("\nContent-Type: ");
                    responseMessage.append(contentType.toString());
                }
                if (isPlaintext(contentType)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream inputStream = responseBody.byteStream();
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int len = inputStream.read(buffer);
                        if (len != -1) {
                            outputStream.write(buffer, 0, len);
                        } else {
                            outputStream.close();
                            byte[] bytes = outputStream.toByteArray();
                            String body = new String(bytes, getCharset(contentType));
                            responseMessage.append("\nBody: ");
                            responseMessage.append(body);
                            Response build = response.newBuilder().body(ResponseBody.create(responseBody.contentType(), bytes)).build();
                            Logger.print(responseMessage.toString());
                            return build;
                        }
                    }
                } else {
                    responseMessage.append("\nBody: maybe [binary body], omitted!");
                }
            }
            Logger.print(responseMessage.toString());
        } catch (Exception e) {
            Logger.print(e);
        }
        return response;
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = UTF8;
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if (charset == null) {
            return UTF8;
        }
        return charset;
    }

    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype == null) {
            return false;
        }
        String subtype2 = subtype.toLowerCase();
        if (subtype2.contains("x-www-form-urlencoded") || subtype2.contains("json") || subtype2.contains("xml") || subtype2.contains("html")) {
            return true;
        }
        return false;
    }

    private String bodyToString(Request request) {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null) {
                return "";
            }
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readString(getCharset(body.contentType()));
        } catch (Exception e) {
            Logger.print(e);
            return "";
        }
    }
}
