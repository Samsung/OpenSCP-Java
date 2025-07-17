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

import org.junit.jupiter.api.Test;

import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.*;


public class GlobalPlatformScpCertificateTests {
    private static final byte[] SERIAL_NUMBER = BytesUtils.hexToBytes("57F0A28A1F2ADCFF7D22239B8B644AE287422DD6");
    private static final byte[] CA_IDENTIFIER = BytesUtils.hexToBytes("851A6F60A6B45534647260877A357F3676C35686");
    private static final byte[] SUBJECT_IDENTIFIER = BytesUtils.hexToBytes("CD7B897E3C1BC6FFAC7F9595FB55AE66C3CE84AB");
    private static final byte[] KEY_USAGE = BytesUtils.hexToBytes("0080");
    private static final byte[] EFFECTIVE_DATE = BytesUtils.hexToBytes("00010203");
    private static final byte[] EXPIRATION_DATE = BytesUtils.hexToBytes("00010203");
    private static final byte[] DISCRETIONARY_DATA = BytesUtils.hexToBytes("00010203040506");
    private static final byte[] AUTHORIZATIONS = BytesUtils.hexToBytes("00010203040506070809");
    private static final byte[] PUBLIC_KEY = BytesUtils.hexToBytes(
        "3059301306072A8648CE3D020106082A8648CE3D030107034200044F92A07D168C309959EED99E288381DD192979CD452D8FBE1F" +
        "163447979207C5E6CD1F4DD11609E2100C033BBD723BE78B71477E64883EB41EC366713E44AF1E");
    private static final byte[] SIGNATURE = BytesUtils.hexToBytes(
        "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F30313233" +
        "3435363738393A3B3C3D3E3F40414243444546");

    @Test
    void positive() throws CertificateException {
        final byte[] certificateBytes = BytesUtils.hexToBytes(
            "7F2181FE931457F0A28A1F2ADCFF7D22239B8B644AE287422DD64214851A6F60A6B45534647260877A357F3676C356865F2014CD" +
            "7B897E3C1BC6FFAC7F9595FB55AE66C3CE84AB950200805F2504000102035F240400010203730700010203040506BF200A000102" +
            "030405060708097F4946B041044F92A07D168C309959EED99E288381DD192979CD452D8FBE1F163447979207C5E6CD1F4DD11609" +
            "E2100C033BBD723BE78B71477E64883EB41EC366713E44AF1EF001005F3747000102030405060708090A0B0C0D0E0F1011121314" +
            "15161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F40414243444546");
        final GlobalPlatformScpCertificate certificate = GlobalPlatformScpCertificate.parse(certificateBytes);
        assertArrayEquals(certificateBytes, certificate.getEncoded());
        assertArrayEquals(SERIAL_NUMBER, certificate.getSerialNumber());
        assertArrayEquals(CA_IDENTIFIER, certificate.getCaIdentifier());
        assertArrayEquals(SUBJECT_IDENTIFIER, certificate.getSubjectIdentifier());
        assertArrayEquals(KEY_USAGE, certificate.getKeyUsage());
        assertArrayEquals(EFFECTIVE_DATE, certificate.getEffectiveDate());
        assertArrayEquals(EXPIRATION_DATE, certificate.getExpirationDate());
        assertArrayEquals(DISCRETIONARY_DATA, certificate.getDiscretionaryData());
        assertArrayEquals(AUTHORIZATIONS, certificate.getAuthorizations());
        assertArrayEquals(PUBLIC_KEY, certificate.getPublicKey().getEncoded());
        assertArrayEquals(SIGNATURE, certificate.getSignature());
    }

    @Test
    void certificateOptionalFieldMissed() throws CertificateException {
        final byte[] certificateAuthorizationsMissedBytes = BytesUtils.hexToBytes(
            "7F2181F1931457F0A28A1F2ADCFF7D22239B8B644AE287422DD64214851A6F60A6B45534647260877A357F3676C356865F2014CD" +
            "7B897E3C1BC6FFAC7F9595FB55AE66C3CE84AB950200805F2504000102035F2404000102037307000102030405067F4946B04104" +
            "4F92A07D168C309959EED99E288381DD192979CD452D8FBE1F163447979207C5E6CD1F4DD11609E2100C033BBD723BE78B71477E" +
            "64883EB41EC366713E44AF1EF001005F3747000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F2021" +
            "22232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F40414243444546");
        final GlobalPlatformScpCertificate certificate = GlobalPlatformScpCertificate
            .parse(certificateAuthorizationsMissedBytes);
        assertArrayEquals(SERIAL_NUMBER, certificate.getSerialNumber());
        assertArrayEquals(CA_IDENTIFIER, certificate.getCaIdentifier());
        assertArrayEquals(SUBJECT_IDENTIFIER, certificate.getSubjectIdentifier());
        assertArrayEquals(KEY_USAGE, certificate.getKeyUsage());
        assertArrayEquals(EFFECTIVE_DATE, certificate.getEffectiveDate());
        assertArrayEquals(EXPIRATION_DATE, certificate.getExpirationDate());
        assertArrayEquals(DISCRETIONARY_DATA, certificate.getDiscretionaryData());
        assertNull(certificate.getAuthorizations());
        assertArrayEquals(PUBLIC_KEY, certificate.getPublicKey().getEncoded());
        assertArrayEquals(SIGNATURE, certificate.getSignature());
    }
}
