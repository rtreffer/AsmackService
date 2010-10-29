/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

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
