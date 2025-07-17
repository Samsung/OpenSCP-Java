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

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;


/**
 * GlobalPlatform certificate for usage in SCP11 v1.4
 */
public class GlobalPlatformScpCertificate implements ScpCertificate {
    private final byte[] encoded;
    private final byte[] serialNumber;
    private final byte[] caIdentifier;
    private final byte[] subjectIdentifier;
    private final byte[] keyUsage;
    private final byte[] effectiveDate; // Optional
    private final byte[] expirationDate;
    private final byte[] discretionaryData; // Optional
    private final byte[] authorizations; // Optional
    private final PublicKey publicKey;
    private final byte[] signature;

    private static final int CERTIFICATE_TAG = 0x7F21;

    private GlobalPlatformScpCertificate(final byte[] encoded,
                                         final byte[] serialNumber,
                                         final byte[] caIdentifier,
                                         final byte[] subjectIdentifier,
                                         final byte[] keyUsage,
                                         @Nullable final byte[] effectiveDate,
                                         final byte[] expirationDate,
                                         @Nullable final byte[] discretionaryData,
                                         @Nullable final byte[] authorizations,
                                         final PublicKey publicKey,
                                         final byte[] signature) {
        this.encoded = encoded;
        this.serialNumber = serialNumber;
        this.caIdentifier = caIdentifier;
        this.subjectIdentifier = subjectIdentifier;
        this.keyUsage = keyUsage;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.discretionaryData = discretionaryData;
        this.authorizations = authorizations;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    byte[] getSerialNumber() {
        return serialNumber;
    }

    byte[] getCaIdentifier() {
        return caIdentifier;
    }

    byte[] getSubjectIdentifier() {
        return subjectIdentifier;
    }

    byte[] getKeyUsage() {
        return keyUsage;
    }

    byte[] getEffectiveDate() {
        return effectiveDate;
    }

    byte[] getExpirationDate() {
        return expirationDate;
    }

    byte[] getDiscretionaryData() {
        return discretionaryData;
    }

    byte[] getAuthorizations() {
        return authorizations;
    }

    byte[] getSignature() {
        return signature;
    }

    static boolean isGlobalPlatformCertificate(final byte[] certificateBytes) throws CertificateException {
        if (certificateBytes == null) {
            throw new CertificateException("Certificate is absent");
        }
        final int certificateTagSize = 2;
        if (certificateBytes.length < certificateTagSize) {
            throw new CertificateException("Certificate is empty");
        }
        final byte[] certificateTagBytes = ByteUtils.intToLength(
            BigInteger.valueOf(CERTIFICATE_TAG), certificateTagSize);
        return Arrays.equals(Arrays.copyOfRange(certificateBytes, 0, certificateTagSize), certificateTagBytes);
    }

    static GlobalPlatformScpCertificate parse(final byte[] certificateBytes) throws CertificateException {
        if (certificateBytes == null) {
            throw new CertificateException("Certificate is missed");
        }
        final Map<Integer, byte[]> certificateTlvs = Tlvs.decodeMap(certificateBytes);
        final byte[] certificateFieldsBytes = certificateTlvs.get(CERTIFICATE_TAG);
        if (certificateFieldsBytes == null) {
            throw new CertificateException("Certificate tag is missed");
        }
        final Map<Integer, byte[]> certificateFields = Tlvs.decodeMap(certificateFieldsBytes);
        final byte[] serialNumber = certificateFields.get(0x93);
        final byte[] caIdentifier = certificateFields.get(0x42);
        final byte[] subjectIdentifier = certificateFields.get(0x5F20);
        final byte[] keyUsage = certificateFields.get(0x95);
        final byte[] effectiveDate = certificateFields.get(0x5F25);
        final byte[] expirationDate = certificateFields.get(0x5F24);
        final byte[] discretionaryData = parseDiscretionaryData(certificateFields);
        final byte[] authorizations = certificateFields.get(0xBF20);
        final PublicKey publicKey = parsePublicKeyFromTlv(certificateFields);
        final byte[] signature = certificateFields.get(0x5F37);

        return new GlobalPlatformScpCertificate(
            certificateBytes,
            serialNumber,
            caIdentifier,
            subjectIdentifier,
            keyUsage,
            effectiveDate,
            expirationDate,
            discretionaryData,
            authorizations,
            publicKey,
            signature);
    }

    private static byte[] parseDiscretionaryData(final Map<Integer, byte[]> certificateFields) {
        byte[] discretionaryData = certificateFields.get(0x53);
        if (discretionaryData == null) {
            discretionaryData = certificateFields.get(0x73);
        }
        return discretionaryData;
    }

    private static PublicKey parsePublicKeyFromTlv(final Map<Integer, byte[]> certificateFields)
            throws CertificateException {
        final byte[] publicKeyTlvBytes = certificateFields.get(0x7F49);
        if (publicKeyTlvBytes == null) {
            throw new CertificateException("Public key is absent");
        }

        final Map<Integer, byte[]> publicKeyFields = Tlvs.decodeMap(publicKeyTlvBytes);
        final byte[] publicKeyEncodedPoint = publicKeyFields.get(0xB0);
        if (publicKeyEncodedPoint == null) {
            throw new CertificateException("Public key Q value is absent");
        }

        final byte[] publicKeyReference = publicKeyFields.get(0xF0);
        if (publicKeyReference == null) {
            throw new CertificateException("Key Parameter Reference is absent");
        }
        try {
            final EllipticCurveValues ellipticCurveValues = KeyParameterReference
                .findEcValuesByKeyParameterReference(publicKeyReference);
            return PublicKeyValues.Ec
                .fromEncodedPoint(ellipticCurveValues, publicKeyEncodedPoint)
                .toPublicKey();
        } catch (BadResponseException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CertificateException(e.getMessage());
        }
    }

    /**
     * GP Card Specification v2.3.1, Table B-2: Key Parameter Reference Values
     */
    private enum KeyParameterReference {
        SECP256R1(ByteUtils.hexToBytes("00"), EllipticCurveValues.SECP256R1),
        SECP384R1(ByteUtils.hexToBytes("01"), EllipticCurveValues.SECP384R1),
        SECP521R1(ByteUtils.hexToBytes("02"), EllipticCurveValues.SECP521R1),
        BrainpoolP256R1(ByteUtils.hexToBytes("03"), EllipticCurveValues.BrainpoolP256R1),
        BrainpoolP384R1(ByteUtils.hexToBytes("05"), EllipticCurveValues.BrainpoolP384R1),
        BrainpoolP512R1(ByteUtils.hexToBytes("07"), EllipticCurveValues.BrainpoolP512R1);

        final byte[] value;
        final EllipticCurveValues ellipticCurveValues;

        KeyParameterReference(final byte[] value, final EllipticCurveValues ellipticCurveValues) {
            this.value = value;
            this.ellipticCurveValues = ellipticCurveValues;
        }

        static EllipticCurveValues findEcValuesByKeyParameterReference(byte[] value) throws BadResponseException {
            for(KeyParameterReference keyParameterReference : values()){
                if(Arrays.equals(keyParameterReference.value, value)){
                    return keyParameterReference.ellipticCurveValues;
                }
            }
            throw new BadResponseException("Key Parameter Reference is not supported: " + ByteUtils.bytesToHex(value));
        }
    }
}