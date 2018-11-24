package org.varys.common.service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

final class SSLUtils {

    private SSLUtils() {
        super();
    }

    static X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // if the queried APIs certificates are not up to date, we don't check them
                    Log.warn("Unsecure client trust check (auth type: {})", authType);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // if the queried APIs certificates are not up to date, we don't check them
                    Log.warn("Unsecure server trust check (auth type: {})", authType);
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

            };

    static SSLSocketFactory createUnsecuredSocketFactory() throws NoSuchAlgorithmException {
        final SSLContext sslContext = SSLContext.getDefault();
        return sslContext.getSocketFactory();
    }
}
