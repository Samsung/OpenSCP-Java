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

import com.samsung.openscp.testdata.InputTestData;
import com.samsung.openscp.testdata.OutputTestData;
import com.samsung.openscp.testdata.Scp11GetDataCmdTestData;
import com.samsung.openscp.testdata.Scp11TestData;
import com.samsung.openscp.testdata.SmartCardEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aGpP256Aes128S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aP256Aes128S16ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aP256Aes128S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aP256Aes192S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aP256Aes256S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11cP256Aes128S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aBP256Aes128S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp11aP384Aes128S8ModeEmulation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Scp11Tests {
    @Nested
    class GetCertificateBundle {
        @Test
        void singleGpCertificate() throws BadResponseException, ApduException, CertificateException, IOException {
            final byte[][] expectedCertificatesEncoded = new byte[][] {
                Scp11GetDataCmdTestData.GP_LAST_CERTIFICATE_BYTES
            };
            positiveTest(Scp11GetDataCmdTestData.GET_DATA_SINGLE_GP_RAPDUS, expectedCertificatesEncoded);
        }

        @Test
        void singleX509Certificate() throws BadResponseException, ApduException, CertificateException, IOException {
            final byte[][] expectedCertificatesEncoded = new byte[][] {
                Scp11GetDataCmdTestData.X509_LAST_CERTIFICATE_BYTES
            };
            positiveTest(Scp11GetDataCmdTestData.GET_DATA_SINGLE_X509_RAPDUS, expectedCertificatesEncoded);
        }

        @Test
        void gpChainCertificates() throws BadResponseException, ApduException, CertificateException, IOException {
            final byte[][] expectedCertificatesEncoded = new byte[][] {
                Scp11GetDataCmdTestData.GP_FIRST_CERTIFICATE_BYTES,
                Scp11GetDataCmdTestData.GP_LAST_CERTIFICATE_BYTES
            };
            positiveTest(Scp11GetDataCmdTestData.GET_DATA_GP_CHAIN_RAPDUS, expectedCertificatesEncoded);
        }

        @Test
        void x509ChainCertificates() throws BadResponseException, ApduException, CertificateException, IOException {
            final byte[][] expectedCertificatesEncoded = new byte[][] {
                Scp11GetDataCmdTestData.X509_FIRST_CERTIFICATE_BYTES,
                Scp11GetDataCmdTestData.X509_LAST_CERTIFICATE_BYTES
            };
            positiveTest(Scp11GetDataCmdTestData.GET_DATA_X509_CHAIN_RAPDUS, expectedCertificatesEncoded);
        }

        void positiveTest(final byte[][] rapdusToSend, final byte[][] expectedCertificatesEncoded)
                throws BadResponseException, ApduException, CertificateException, IOException {
            final byte[][] capdusExpected = Scp11GetDataCmdTestData.constructCapdus(rapdusToSend.length);
            final SmartCardConnection connection = new SmartCardEmulation(capdusExpected, rapdusToSend);
            final SecurityDomainSession session = TestUtils.initSecurityDomainSession(connection);
            final KeyRef keyRef = new KeyRef((byte) 0x11, (byte) 0x03);
            final List<ScpCertificate> sdCertChain = session.getCertificateBundle(keyRef);
            assertEquals(expectedCertificatesEncoded.length, sdCertChain.size());
            for (int i = 0; i < expectedCertificatesEncoded.length; i++) {
                final byte[] expectedCertificateEncoded = expectedCertificatesEncoded[i];
                final byte[] certificateEncoded = sdCertChain.get(i).getEncoded();
                assertArrayEquals(expectedCertificateEncoded, certificateEncoded);
            }
        }
    }

    @Nested
    class Scp11a {
        private static final byte KID = (byte) 0x11;

        @Test
        void p256Aes128S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            p256PositiveTest(ScpMode.S8, AesAlg.AES_128, new SmartCardScp11aP256Aes128S8ModeEmulation());
        }

        @Test
        void p256Aes192S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            p256PositiveTest(ScpMode.S8, AesAlg.AES_192, new SmartCardScp11aP256Aes192S8ModeEmulation());
        }

        @Test
        void p256Aes256S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            p256PositiveTest(ScpMode.S8, AesAlg.AES_256, new SmartCardScp11aP256Aes256S8ModeEmulation());
        }

        @Test
        void p256Aes128S16ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            p256PositiveTest(ScpMode.S16, AesAlg.AES_128, new SmartCardScp11aP256Aes128S16ModeEmulation());
        }

        @Test
        void p384Aes128S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            positiveTest(
                KID,
                ScpMode.S8,
                AesAlg.AES_128,
                new SmartCardScp11aP384Aes128S8ModeEmulation(),
                Scp11TestData.SK_OCE_ECKA_P384,
                Scp11TestData.CERT_OCE_ECKA_P384,
                Scp11TestData.EPK_OCE_ECKA_P384,
                Scp11TestData.ESK_OCE_ECKA_P384);
        }

        @Test
        void bp256Aes128S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            positiveTest(
                KID,
                ScpMode.S8,
                AesAlg.AES_128,
                new SmartCardScp11aBP256Aes128S8ModeEmulation(),
                Scp11TestData.SK_OCE_ECKA_BP256,
                Scp11TestData.CERT_OCE_ECKA_BP256,
                Scp11TestData.EPK_OCE_ECKA_BP256,
                Scp11TestData.ESK_OCE_ECKA_BP256);
        }

        @Test
        void globalPlatformP256Aes128S8ModePositive() throws ApduException, CertificateException, IOException,
                NoSuchAlgorithmException, InvalidKeySpecException, BadResponseException {
            positiveTest(
                KID,
                ScpMode.S8,
                AesAlg.AES_128,
                new SmartCardScp11aGpP256Aes128S8ModeEmulation(),
                Scp11TestData.SK_OCE_ECKA_P256,
                Scp11TestData.GP_CERT_OCE_ECKA_P256,
                Scp11TestData.EPK_OCE_ECKA_P256,
                Scp11TestData.ESK_OCE_ECKA_P256);
        }

        private void p256PositiveTest(final ScpMode mode,
                                      final AesAlg sessionKeysAlg,
                                      final SmartCardEmulation connection)
                throws BadResponseException, ApduException, CertificateException, IOException, NoSuchAlgorithmException,
                    InvalidKeySpecException {
            Scp11Tests.this.p256PositiveTest(KID, mode, sessionKeysAlg, connection);
        }
    }

    @Nested
    class Scp11c {
        @Test
        void aes128S8ModePositive() throws ApduException, CertificateException, IOException, NoSuchAlgorithmException,
                InvalidKeySpecException, BadResponseException {
            final byte scp11cKid = (byte) 0x15;
            p256PositiveTest(scp11cKid, ScpMode.S8, AesAlg.AES_128, new SmartCardScp11cP256Aes128S8ModeEmulation());
        }
    }

    private void p256PositiveTest(final byte kid,
                                  final ScpMode mode,
                                  final AesAlg sessionKeysAlg,
                                  final SmartCardEmulation connection)
            throws ApduException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException,
                BadResponseException {
        positiveTest(
            kid,
            mode,
            sessionKeysAlg,
            connection,
            Scp11TestData.SK_OCE_ECKA_P256,
            Scp11TestData.X509_CERT_OCE_ECKA_P256,
            Scp11TestData.EPK_OCE_ECKA_P256,
            Scp11TestData.ESK_OCE_ECKA_P256);
    }

    private void positiveTest(final byte kid,
                              final ScpMode mode,
                              final AesAlg sessionKeysAlg,
                              final SmartCardEmulation connection,
                              final byte[] skOceEcka,
                              final byte[] certOceEcka,
                              final byte[] epkOceEcka,
                              final byte[] eskOceEcka)
            throws ApduException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException,
                BadResponseException {
        final KeyRef keyRef = new KeyRef(kid, (byte) 0x03);

        final SecurityDomainSession session = TestUtils.initSecurityDomainSession(connection);
        final List<ScpCertificate> sdCertChain = session.getCertificateBundle(keyRef);
        assertFalse(sdCertChain.isEmpty());

        final int eckaCertPosition = sdCertChain.size() - 1;
        final PublicKey pkSdEcka = sdCertChain.get(eckaCertPosition).getPublicKey();
        final Scp11KeyParams keyParams = createKeyParams(keyRef, pkSdEcka, skOceEcka, certOceEcka, sessionKeysAlg);
        final KeyPair ephemeralKeyPair = createKeyPair(epkOceEcka, eskOceEcka);
        session.authenticate(keyParams, mode, ephemeralKeyPair);

        TestUtils.executeGetStatusCmd(
            session,
            InputTestData.LIST_PACKAGES_ID,
            OutputTestData.LIST_PACKAGES_RSP_PLAIN_DATA);

        assertTrue(connection.isAllExpectedCapdusReceived());
    }

    private static Scp11KeyParams createKeyParams(final KeyRef sessionRef,
                                                  final PublicKey pkSdEcka,
                                                  final byte[] skOceEckaBytes,
                                                  final byte[] certOceEckaBytes,
                                                  final AesAlg sessionKeysAlg)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyRef oceKeyRef = new KeyRef(Scp11TestData.OCE_KID, sessionRef.getKvn());
        final PrivateKey skOceEcka = createEcPrivateKey(skOceEckaBytes);
        final List<byte[]> certChain = Collections.singletonList(certOceEckaBytes);
        return new Scp11KeyParams(
                sessionRef,
                pkSdEcka,
                oceKeyRef,
                skOceEcka,
                certChain,
                sessionKeysAlg
        );
    }

    private static KeyPair createKeyPair(final byte[] publicKeyBytes, final byte[] privateKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final PublicKey publicKey = createEcPublicKey(publicKeyBytes);
        final PrivateKey privateKey = createEcPrivateKey(privateKeyBytes);
        return new KeyPair(publicKey, privateKey);
    }

    private static PublicKey createEcPublicKey(final byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance("EC");
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    private static PrivateKey createEcPrivateKey(final byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance("EC");
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
