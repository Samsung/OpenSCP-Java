/*!
 * Copyright 2025 Samsung Electronics Co, Ltd
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
 */

package com.samsung.openscp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


/**
 * x509 certificate for usage in SCP11 v1.4
 */
public class X509ScpCertificate implements ScpCertificate {
    private final X509Certificate certificate;

    private X509ScpCertificate(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    @Override
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return certificate.getEncoded();
    }

    static X509ScpCertificate parse(final byte[] certificateBytes) throws CertificateException {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final InputStream stream = new ByteArrayInputStream(certificateBytes);
        final X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
        return new X509ScpCertificate(certificate);
    }
}