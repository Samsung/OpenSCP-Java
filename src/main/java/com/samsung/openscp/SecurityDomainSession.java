/*
 * Copyright (C) 2024 Yubico.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -----------------------
 * Modifications copyright:
 *
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
 *
 * Modifications include:
 *   - Package and import statements updated during code move from the original project
 *   - Removed all except protocol setup from constructor
 *   - Removed ApplicationSession dependency
 *   - Parametrized with ScpMode variable, add S16 mode support
 *   - Added select() & sendAndReceive() functions
 *   - Added 0x00 Le to PUT KEY & GET DATA CAPDU
 *   - Parametrized authenticate() method with the host challenge
 *   - Added additional constructor with security provider parametrization
 *   - Added missed JavaDocs
 *   - Added storeAllowlist() method variant for SCP11c mode with allowlist counter
 *   - Added BF21 tag parsing in GET DATA (Certificate Store) response processing
 */

package com.samsung.openscp;

import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Closeable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;

/**
 * Off-card entity communication (OCE) session with the security domain (SD, smart card)
 */
public class SecurityDomainSession implements Closeable {
    private static final byte[] DEFAULT_KCV_IV = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    private static final byte INS_GET_DATA = (byte) 0xCA;
    private static final byte INS_PUT_KEY = (byte) 0xD8;
    private static final byte INS_STORE_DATA = (byte) 0xE2;
    private static final byte INS_DELETE = (byte) 0xE4;
    private static final byte INS_GENERATE_KEY = (byte) 0xF1;

    static final byte INS_INITIALIZE_UPDATE = (byte) 0x50;
    static final byte INS_EXTERNAL_AUTHENTICATE = (byte) 0x82;
    static final byte INS_INTERNAL_AUTHENTICATE = (byte) 0x88;
    static final byte INS_PERFORM_SECURITY_OPERATION = (byte) 0x2A;

    private static final short TAG_KEY_INFORMATION = 0xE0;
    private static final short TAG_CARD_RECOGNITION_DATA = 0x66;
    private static final short TAG_CA_KLOC_IDENTIFIERS = (short) 0xFF33;
    private static final short TAG_CA_KLCC_IDENTIFIERS = (short) 0xFF34;
    private static final short TAG_CERTIFICATE_STORE = (short) 0xBF21;

    private static final int KEY_TYPE_AES = 0x88;
    private static final int KEY_TYPE_ECC_PUBLIC_KEY = 0xB0;
    private static final int KEY_TYPE_ECC_PRIVATE_KEY = 0xB1;
    private static final int KEY_TYPE_ECC_KEY_PARAMS = 0xF0;

    private final SmartCardProtocol protocol;
    @Nullable
    private DataEncryptor dataEncryptor;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecurityDomainSession.class);

    /**
     * @param connection implementation of connection to the smart card
     * @param securityProvider external security provider if your default JCA security provider doesn't support crypto
     *                         algorithms required by the SCP protocol
     */
    public SecurityDomainSession(final SmartCardConnection connection, final Provider securityProvider) {
        this(connection);
        Security.insertProviderAt(securityProvider, 1);
    }

    /**
     * @param connection implementation of connection to the smart card
     */
    public SecurityDomainSession(final SmartCardConnection connection) {
        protocol = new SmartCardProtocol(connection);
        Logger.debug(logger, "Security Domain session initialized");
    }

    @Override
    public void close() throws IOException {
        protocol.close();
    }

    /**
     * Select Security Domain
     */
    public void select(byte[] aid) throws IOException, ApplicationNotAvailableException {
        protocol.select(aid);
    }

    /**
     * Initialize SCP and authenticate the session.
     * SCP11b does not authenticate the off-card entity, and will not allow the usage of commands which require such authentication.
     */
    public void authenticate(ScpKeyParams keyParams, ScpMode mode)
            throws BadResponseException, ApduException, IOException {
        final byte[] hostChallenge = null;
        authenticate(keyParams, mode, hostChallenge);
    }

    /**
     * Execute GET DATA command
     *
     * @param tag data object tag to read
     * @param data CAPDU data
     * @return RAPDU data
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     */
    public byte[] getData(short tag, @Nullable byte[] data) throws ApduException, IOException {
        return protocol.sendAndReceive(
            new Apdu(
                0 /*CLA*/,
                INS_GET_DATA,
                tag >> 8,
                tag & 0xff,
                data,
                0x00 /*Le*/,
                true /*forceAddLe*/));
    }

    /**
     * Retrieve card recognition data from the smart card
     *
     * @return card recognition data bytes
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     * @throws BadResponseException if the TLV tag differs from expectedTag
     */
    public byte[] getCardRecognitionData() throws ApduException, IOException, BadResponseException {
        return Tlvs.unpackValue(0x73, getData(TAG_CARD_RECOGNITION_DATA, null));
    }

    /**
     * Retrieve keys information from the smart card
     *
     * @return parsed keys information
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     * @throws BadResponseException if the TLV tag differs from expectedTag
     */
    public Map<KeyRef, Map<Byte, Byte>> getKeyInformation() throws ApduException, IOException, BadResponseException {
        Map<KeyRef, Map<Byte, Byte>> keys = new HashMap<>();
        for (Tlv tlv : Tlvs.decodeList(getData(TAG_KEY_INFORMATION, null))) {
            ByteBuffer data = ByteBuffer.wrap(Tlvs.unpackValue(0xC0, tlv.getBytes()));
            KeyRef keyRef = new KeyRef(data.get(), data.get());
            Map<Byte, Byte> components = new HashMap<>();
            while (data.hasRemaining()) {
                components.put(data.get(), data.get());
            }
            keys.put(keyRef, components);
        }
        return keys;
    }

    /**
     * Retrieve an SCP11 Certificate Store from smart card using the GET DATA (Certificate Store) command
     *
     * @param keyRef key reference to the SK.SD.ECKA
     * @return parsed certificates list
     *
     * @throws ApduException        in case if received error in APDU response
     * @throws IOException          in case of connection and communication error
     * @throws CertificateException in case of certificate parsing failure
     * @throws BadResponseException in case of TLV parsing failure
     */
    public List<ScpCertificate> getCertificateBundle(KeyRef keyRef)
            throws ApduException, IOException, CertificateException, BadResponseException {
        Logger.debug(logger, "Getting certificate bundle for key={}", keyRef);
        List<ScpCertificate> certificates = new ArrayList<>();
        try {
            byte[] resp = getData(TAG_CERTIFICATE_STORE, new Tlv(0xA6, new Tlv(0x83, keyRef.getBytes()).getBytes()).getBytes());
            byte[] certificatesListEncoded = Tlvs.unpackValue(Short.toUnsignedInt(TAG_CERTIFICATE_STORE), resp);
            for (Tlv der : Tlvs.decodeList(certificatesListEncoded)) {
                final byte[] certificateBytes = der.getBytes();
                final boolean isGlobalPlatformCertificate = GlobalPlatformScpCertificate
                    .isGlobalPlatformCertificate(certificateBytes);
                final ScpCertificate certificate = isGlobalPlatformCertificate ?
                    GlobalPlatformScpCertificate.parse(certificateBytes) :
                    X509ScpCertificate.parse(certificateBytes);
                certificates.add(certificate);
            }
        } catch (ApduException e) {
            // On REFERENCED_DATA_NOT_FOUND return empty list
            if (e.getSw() != SW.REFERENCED_DATA_NOT_FOUND) {
                throw e;
            }
        }
        return certificates;
    }

    /**
     * Retrieve supported CA identifiers from smart card using the GET DATA (Supported CA Identifiers) command
     *
     * @param kloc retrieve supported Key Loading OCE Certificates
     * @param klcc retrieve supported Key Loading Card Certificates
     * @return parsed CA identifiers
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     */
    public Map<KeyRef, byte[]> getSupportedCaIdentifiers(boolean kloc, boolean klcc) throws ApduException, IOException {
        if (!kloc && !klcc) {
            throw new IllegalArgumentException("At least one of kloc and klcc must be true");
        }
        Logger.debug(logger, "Getting CA identifiers KLOC={}, KLCC={}", kloc, klcc);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        if (kloc) {
            try {
                data.write(getData(TAG_CA_KLOC_IDENTIFIERS, null));
            } catch (ApduException e) {
                if (e.getSw() != SW.REFERENCED_DATA_NOT_FOUND) {
                    throw e;
                }
            }
        }
        if (klcc) {
            try {
                data.write(getData(TAG_CA_KLCC_IDENTIFIERS, null));
            } catch (ApduException e) {
                if (e.getSw() != SW.REFERENCED_DATA_NOT_FOUND) {
                    throw e;
                }
            }
        }
        List<Tlv> tlvs = Tlvs.decodeList(data.toByteArray());
        Map<KeyRef, byte[]> identifiers = new HashMap<>();
        for (int i = 0; i < tlvs.size(); i += 2) {
            ByteBuffer ref = ByteBuffer.wrap(tlvs.get(i + 1).getValue());
            identifiers.put(new KeyRef(ref.get(), ref.get()), tlvs.get(i).getValue());
        }
        return identifiers;
    }

    /**
     * Execute the STORE DATA command
     *
     * @param data data to store on the smart card
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     */
    public void storeData(byte[] data) throws ApduException, IOException {
        protocol.sendAndReceive(new Apdu(0, INS_STORE_DATA, 0x90, 0x00, data));
    }

    /**
     * Store the certificate chain for a given key using the STORE DATA (Certificate Store) command
     * <p>
     * Requires off-card entity verification.
     * <p>
     * Certificates should be in order, with the leaf certificate last.
     *
     * @param keyRef       a reference to the key for which to store the certificates
     * @param certificates the certificates to store
     */
    public void storeCertificateBundle(KeyRef keyRef, List<X509Certificate> certificates) throws ApduException, IOException {
        Logger.debug(logger, "Storing certificate bundle for {}", keyRef);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (X509Certificate cert : certificates) {
            try {
                data.write(cert.getEncoded());
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException("Failed to get encoded version of certificate", e);
            }
        }
        storeData(Tlvs.encodeList(Arrays.asList(
                new Tlv(0xA6, new Tlv(0x83, keyRef.getBytes()).getBytes()),
                new Tlv(TAG_CERTIFICATE_STORE, data.toByteArray())
        )));
        Logger.info(logger, "Certificate bundle stored");
    }

    /**
     * Store which certificate serial numbers that can be used for a given key using the STORE DATA (Allowlist) command
     * <p>
     * Requires off-card entity verification.
     * <p>
     * If no allowlist is stored, any certificate signed by the CA can be used.
     * <p>
     * NOTE: Use for SCP11a
     *
     * @param keyRef  a reference to the key for which to store the allowlist
     * @param serials the list of serial numbers to store
     */
    public void storeAllowlist(KeyRef keyRef, List<BigInteger> serials) throws ApduException, IOException {
        final Integer allowlistCounter = null;
        storeAllowlist(keyRef, serials, allowlistCounter);
    }

    /**
     * Store which certificate serial numbers that can be used for a given key using the STORE DATA (Allowlist) command
     * <p>
     * Requires off-card entity verification.
     * <p>
     * If no allowlist is stored, any certificate signed by the CA can be used.
     * <p>
     * NOTE: Use for SCP11c
     *
     * @param keyRef  a reference to the key for which to store the allowlist
     * @param serials the list of serial numbers to store
     * @param allowlistCounter SD allowlist counter - positive integer up to 32767
     */
    public void storeAllowlist(KeyRef keyRef, List<BigInteger> serials, Integer allowlistCounter)
            throws ApduException, IOException {
        Logger.debug(logger, "Storing serial allowlist for {}", keyRef);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (BigInteger serial : serials) {
            data.write(new Tlv(0x93, serial.toByteArray()).getBytes());
        }
        final List<Tlv> cmdData = Arrays.asList(new Tlv(0xA6, new Tlv(0x83, keyRef.getBytes()).getBytes()));
        if (allowlistCounter != null) {
            final int counterSizeBytes = 2;
            byte[] counterBytes = ByteUtils.intToLength(BigInteger.valueOf(allowlistCounter), counterSizeBytes);
            cmdData.add(new Tlv(0x92, counterBytes));
        }
        cmdData.add(new Tlv(0x70, data.toByteArray()));
        storeData(Tlvs.encodeList(cmdData));
        Logger.info(logger, "Serial allowlist stored");
    }

    /**
     * Store the SKI (Subject Key Identifier) for the CA of a given key using the STORE DATA (CA Identifier) command
     * <p>
     * Requires off-card entity verification.
     *
     * @param keyRef a reference to the key for which to store the CA issuer
     * @param ski    the Subject Key Identifier to store
     */
    public void storeCaIssuer(KeyRef keyRef, byte[] ski) throws ApduException, IOException {
        Logger.debug(logger, "Storing CA issuer SKI for {}: {}", keyRef, ByteUtils.bytesToHex(ski));
        byte klcc = 0;
        switch (keyRef.getKid()) {
            case ScpKid.SCP11a:
            case ScpKid.SCP11b:
            case ScpKid.SCP11c:
                klcc = 1;
        }
        storeData(new Tlv(0xA6, Tlvs.encodeList(Arrays.asList(
                new Tlv(0x80, new byte[]{klcc}),
                new Tlv(0x42, ski),
                new Tlv(0x83, keyRef.getBytes())
        ))).getBytes());
        Logger.info(logger, "CA issuer SKI stored");
    }

    /**
     * Delete one (or more) keys.
     * <p>
     * Requires off-card entity verification.
     * <p>
     * All keys matching the given KID and/or KVN will be deleted (0 is treated as a wildcard).
     * <p>
     * To delete the final key you must set deleteLast = true.
     *
     * @param keyRef     a reference to the key to delete
     * @param deleteLast must be true if deleting the final key, false otherwise
     */
    public void deleteKey(KeyRef keyRef, boolean deleteLast) throws ApduException, IOException {
        byte kid = keyRef.getKid();
        byte kvn = keyRef.getKvn();
        if (kid == 0 && kvn == 0) {
            throw new IllegalArgumentException("At least one of KID, KVN must be nonzero");
        }
        if (kid == 1 || kid == 2 || kid == 3) {
            if (kvn != 0) {
                kid = 0;
            } else {
                throw new IllegalArgumentException("SCP03 keys can only be deleted by KVN");
            }
        }
        Logger.debug(logger, "Deleting keys matching {}", keyRef);
        List<Tlv> tlvs = new ArrayList<>();
        if (kid != 0) {
            tlvs.add(new Tlv(0xD0, new byte[]{kid}));
        }
        if (kvn != 0) {
            tlvs.add(new Tlv(0xD2, new byte[]{kvn}));
        }
        protocol.sendAndReceive(new Apdu(0x80, INS_DELETE, 0, deleteLast ? 1 : 0, Tlvs.encodeList(tlvs)));
        Logger.info(logger, "Keys deleted");
    }

    /**
     * Generate a new SCP11 key.
     * <p>
     * Requires off-card entity verification.
     *
     * @param keyRef     the KID-KVN pair to assign the new key
     * @param replaceKvn 0 to generate a new keypair, non-zero to replace an existing KVN
     * @return the public key from the generated key pair
     */
    public PublicKeyValues.Ec generateEcKey(KeyRef keyRef, int replaceKvn) throws ApduException, IOException, BadResponseException {
        Logger.debug(logger, "Generating new key for {}" +
                (replaceKvn == 0 ? "" : String.format(Locale.ROOT, ", replacing KVN=0x%02x", replaceKvn)), keyRef);

        byte[] params = new Tlv(KEY_TYPE_ECC_KEY_PARAMS, new byte[]{0}).getBytes();
        byte[] data = ByteBuffer
                .allocate(params.length + 1)
                .put(keyRef.getKvn())
                .put(params)
                .array();
        byte[] resp = protocol.sendAndReceive(new Apdu(0x80, INS_GENERATE_KEY, replaceKvn, keyRef.getKid(), data));
        byte[] encodedPoint = Tlvs.unpackValue(KEY_TYPE_ECC_PUBLIC_KEY, resp);
        return PublicKeyValues.Ec.fromEncodedPoint(EllipticCurveValues.SECP256R1, encodedPoint);
    }

    /**
     * Imports an SCP03 key set.
     * <p>
     * Requires off-card entity verification.
     *
     * @param keyRef     the KID-KVN pair to assign the new key set, KID must be 1
     * @param keys       the key material to import
     * @param replaceKvn 0 to generate a new keypair, non-zero to replace an existing KVN
     */
    public void putKey(KeyRef keyRef, StaticKeys keys, int replaceKvn) throws ApduException, IOException, BadResponseException {
        Logger.debug(logger, "Importing SCP03 key set into {}", keyRef);
        if (keyRef.getKid() != ScpKid.SCP03) {
            throw new IllegalArgumentException("KID must be 0x01 for SCP03 key sets");
        }
        if (keys.dek == null) {
            throw new IllegalArgumentException("New DEK must be set in static keys");
        }
        if (dataEncryptor == null) {
            throw new IllegalStateException("No session DEK key available");
        }

        ByteBuffer data = ByteBuffer.allocate(1 + 3 * (18 + 4)).put(keyRef.getKvn());
        ByteBuffer expected = ByteBuffer.allocate(1 + 3 * 3).put(keyRef.getKvn());
        for (SecretKey key : Arrays.asList(keys.enc, keys.mac, keys.dek)) {
            byte[] kcv = Arrays.copyOf(ScpState.cbcEncrypt(key, DEFAULT_KCV_IV), 3);
            byte[] keyBytes = key.getEncoded();
            try {
                data.put(new Tlv(KEY_TYPE_AES, dataEncryptor.encrypt(keyBytes)).getBytes())
                        .put((byte) kcv.length)
                        .put(kcv);
            } finally {
                Arrays.fill(keyBytes, (byte) 0);
            }
            expected.put(kcv);
        }

        byte[] resp = protocol.sendAndReceive(
            new Apdu(0x80,
                     INS_PUT_KEY,
                     replaceKvn,
                     0x80 | keyRef.getKid(),
                     data.array(),
                     0x00 /*Le*/,
                     true /*forceAddLe*/));
        if (!MessageDigest.isEqual(resp, expected.array())) {
            throw new BadResponseException("Incorrect key check value");
        }
        Logger.info(logger, "SCP03 Key set imported");
    }

    /**
     * Imports a secret key for SCP11.
     * <p>
     * Requires off-card entity verification.
     *
     * @param keyRef     the KID-KVN pair to assign the new secret key, KID must be 0x11, 0x13, or 0x15
     * @param secretKey  a private EC key used to authenticate the SD
     * @param replaceKvn 0 to generate a new keypair, non-zero to replace an existing KVN
     */
    public void putKey(KeyRef keyRef, PrivateKeyValues secretKey, int replaceKvn) throws ApduException, IOException, BadResponseException {
        Logger.debug(logger, "Importing SCP11 private key into {}", keyRef);
        if (!(secretKey instanceof PrivateKeyValues.Ec) ||
                !((PrivateKeyValues.Ec) secretKey).getCurveParams()
                        .equals(EllipticCurveValues.SECP256R1)) {
            throw new IllegalArgumentException("Private key must be of type SECP256R1");
        }
        if (dataEncryptor == null) {
            throw new IllegalStateException("No session DEK key available");
        }

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(keyRef.getKvn());
        byte[] expected = new byte[]{keyRef.getKvn()};

        byte[] keyBytes = ((PrivateKeyValues.Ec) secretKey).getSecret();
        try {
            data.write(new Tlv(KEY_TYPE_ECC_PRIVATE_KEY, dataEncryptor.encrypt(keyBytes)).getBytes());
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
        data.write(new Tlv(KEY_TYPE_ECC_KEY_PARAMS, new byte[]{0x00}).getBytes());
        data.write((byte) 0);

        byte[] resp = protocol.sendAndReceive(new Apdu(0x80, INS_PUT_KEY, replaceKvn, keyRef.getKid(), data.toByteArray()));
        if (!MessageDigest.isEqual(resp, expected)) {
            throw new BadResponseException("Incorrect key check value");
        }
        Logger.info(logger, "SCP11 private key imported");
    }

    /**
     * Imports a public key for authentication of the off-card entity for SCP11a/c.
     * <p>
     * Requires off-card entity verification.
     *
     * @param keyRef     the KID-KVN pair to assign the new public key
     * @param publicKey  a public EC key used as CA to authenticate the off-card entity
     * @param replaceKvn 0 to generate a new keypair, non-zero to replace an existing KVN
     */
    public void putKey(KeyRef keyRef, PublicKeyValues publicKey, int replaceKvn) throws ApduException, IOException, BadResponseException {
        Logger.debug(logger, "Importing SCP11 public key into {}", keyRef);
        if (!(publicKey instanceof PublicKeyValues.Ec) ||
                !((PublicKeyValues.Ec) publicKey).getCurveParams()
                        .equals(EllipticCurveValues.SECP256R1)) {
            throw new IllegalArgumentException("Public key must be of type SECP256R1");
        }

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(keyRef.getKvn());
        byte[] expected = new byte[]{keyRef.getKvn()};

        data.write(new Tlv(KEY_TYPE_ECC_PUBLIC_KEY, ((PublicKeyValues.Ec) publicKey).getEncodedPoint()).getBytes());
        data.write(new Tlv(KEY_TYPE_ECC_KEY_PARAMS, new byte[]{0x00}).getBytes());
        data.write((byte) 0);

        byte[] resp = protocol.sendAndReceive(new Apdu(0x80, INS_PUT_KEY, replaceKvn, keyRef.getKid(), data.toByteArray()));
        if (!MessageDigest.isEqual(resp, expected)) {
            throw new BadResponseException("Incorrect key check value");
        }
        Logger.info(logger, "SCP11 public key imported");
    }

    /**
     * Perform a factory reset of the Security Domain.
     * <p>
     * This will remove all keys and associated data, as well as restore the default SCP03 static keys,
     * and generate a new (attestable) SCP11b key.
     */
    public void reset() throws BadResponseException, ApduException, IOException {
        Logger.debug(logger, "Resetting all SCP keys");
        // Reset is done by blocking all available keys
        byte[] data = new byte[8];
        for (KeyRef keyRef : getKeyInformation().keySet()) {
            byte ins;
            switch (keyRef.getKid()) {
                case ScpKid.SCP03:
                    // SCP03 uses KID=0, we use KVN=0 to allow deleting the default keys
                    // which have an invalid KVN (0xff).
                    keyRef = new KeyRef((byte) 0, (byte) 0);
                    ins = INS_INITIALIZE_UPDATE;
                    break;
                case 0x02:
                case 0x03:
                    continue;  // Skip these as they are deleted by 0x01
                case ScpKid.SCP11a:
                case ScpKid.SCP11c:
                    ins = INS_EXTERNAL_AUTHENTICATE;
                    break;
                case ScpKid.SCP11b:
                    ins = INS_INTERNAL_AUTHENTICATE;
                    break;
                default:  // 0x10, 0x20-0x2F
                    ins = INS_PERFORM_SECURITY_OPERATION;
            }

            // Keys have 65 attempts before blocking (and thus removal)
            for (int i = 0; i < 65; i++) {
                try {
                    protocol.sendAndReceive(new Apdu(0x80, ins, keyRef.getKvn(), keyRef.getKid(), data));
                } catch (ApduException e) {
                    switch (e.getSw()) {
                        case SW.AUTH_METHOD_BLOCKED:
                        case SW.SECURITY_CONDITION_NOT_SATISFIED:
                            i = 65;
                            break;
                        case SW.INCORRECT_PARAMETERS:
                            continue;
                        default:
                            throw e;
                    }
                }
            }
        }
        Logger.info(logger, "SCP keys reset");
    }

    /**
     * Send Command APDU to the smart card, receives the Response APDU payload.
     * <p>
     * NOTE: SW is not included to returned data
     *
     * @param apdu Command APDU to send
     * @return Response APDU data payload
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     */
    public byte[] sendAndReceive(Apdu apdu) throws ApduException, IOException {
        return protocol.sendAndReceive(apdu);
    }

    /**
     * Send Command APDU to the smart card, receives the raw Response APDU
     *
     * @param apdu Command APDU to send
     * @return Response APDU raw data including SW bytes
     *
     * @throws ApduException in case if received error in APDU response
     * @throws IOException   in case of connection and communication error
     */
    public byte[] sendAndReceiveApdu(Apdu apdu) throws ApduException, IOException {
        return protocol.sendAndReceiveApdu(apdu);
    }

    void authenticate(ScpKeyParams keyParams, ScpMode mode, @Nullable KeyPair ephemeralKeys)
            throws BadResponseException, ApduException, IOException {
        dataEncryptor = protocol.initScp(keyParams, mode, ephemeralKeys);
    }

    void authenticate(ScpKeyParams keyParams, ScpMode mode, @Nullable byte[] hostChallenge)
            throws BadResponseException, ApduException, IOException {
        dataEncryptor = protocol.initScp(keyParams, mode, hostChallenge);
    }
}