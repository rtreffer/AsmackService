package com.googlecode.asmack.connection.impl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Easiest insecure trust manager. This manager does noth verify any
 * certificates, it will simply accept any possible certificate.
 */
public class UnTrustManager implements TrustManager, X509TrustManager {

    /**
     * <p>Check if a client certificate is trustworth.</p>
     * <p><b>Warning: This implementation will never reject a certificate!
     * </b></p>
     * @param chain X509Certificate[] The certificate chain.
     * @param authType String The type of authentification.
     * @throws CertificateException Never thrown.
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    /**
     * <p>Check if a server is trustworth based on a key exchange method and a
     * give certificate chain.</p>
     * <p><b>Warning: This implementation will never reject a certificate!
     * </b></p>
     * @param chain X509Certificate[]
     * @param authType String
     * @throws CertificateException
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    /**
     * <p>Get a list of accepted issuers.</p>
     * <p><b>Warning: This implementation will always return an empty array
     * </b></p>
     * @return X509Certificate[] An empty array.
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

}
