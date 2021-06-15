package com.github.gzuliyujiang.http;

import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/* access modifiers changed from: package-private */
public class Utils {
    Utils() {
    }

    public static OkHttpClient buildOkHttpClient(CookieJar cookieJar) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.readTimeout(8, TimeUnit.SECONDS);
        builder.writeTimeout(8, TimeUnit.SECONDS);
        builder.followRedirects(false);
        builder.followSslRedirects(true);
        builder.addInterceptor(new LoggingInterceptor());
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            UnsafeTrustManager unsafeTrustManager = new UnsafeTrustManager();
            sslContext.init(null, new TrustManager[]{unsafeTrustManager}, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), unsafeTrustManager);
        } catch (Exception e) {
        }
        builder.hostnameVerifier(new UnsafeHostnameVerifier());
        if (cookieJar != null) {
            builder.cookieJar(cookieJar);
        }
        return builder.build();
    }

    public static String getDefaultUserAgent(Context context, String customPart) {
        String ua;
        String customPart2;
        try {
            ua = WebSettings.getDefaultUserAgent(context);
        } catch (Throwable th) {
            ua = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.ID + "; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Safari/537.36";
        }
        if (customPart == null) {
            customPart2 = " HttpRequest/2.1.1";
        } else {
            customPart2 = " " + customPart.trim() + " HttpRequest/2.1.1";
        }
        return ua + customPart2;
    }
}
