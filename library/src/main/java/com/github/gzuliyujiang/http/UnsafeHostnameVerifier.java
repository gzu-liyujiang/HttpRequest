package com.github.gzuliyujiang.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class UnsafeHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
