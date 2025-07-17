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
 *   - Parametrize with ScpMode variable, add S16 mode support
 *   - Parametrize with AesAlg variable, add AES-192, AES-256 mode support to SCP11
 *   - Added 8th bit modification for P1 under PERFORM_SECURITY CMD constructing
 *   - Add 0x00 Le to INITIALIZE UPDATE & PERFORM SECURITY & INTERNAL AUTHENTICATE & MUTUAL AUTHENTICATE CAPDU
 *   - Add check that card supports maximum security level
 *   - Parametrize scp11Init() method with the ephemeral keys
 *   - Removed redundant `public` access modifiers
 *   - Skip zero size payload encryption to meet GP SCP03 specification
 */

package com.samsung.openscp;

import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Internal SCP state class for managing SCP state, handling encryption/decryption and MAC.
 */
class ScpState {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ScpState.class);

    private final SessionKeys keys;
    private byte[] macChain;
    private int encCounter = 1;

    ScpState(SessionKeys keys, byte[] macChain) {
        this.keys = keys;
        this.macChain = macChain;
    }

    @Nullable DataEncryptor getDataEncryptor() {
        if (keys.dek == null) {
            return null;
        }
        return data -> cbcEncrypt(keys.dek, data);
    }

    byte[] encrypt(byte[] data) {
        if (data.length == 0) {
            encCounter++;
            return data;
        }
        // Pad the data
        Logger.trace(logger, "Plaintext data: {}", ByteUtils.bytesToHex(data));
        int padLen = 16 - (data.length % 16);
        byte[] padded = Arrays.copyOf(data, data.length + padLen);
        padded[data.length] = (byte) 0x80;

        // Encrypt
        try {
            @SuppressWarnings("GetInstance") Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keys.senc);
            byte[] ivData = ByteBuffer.allocate(16).put(new byte[12]).putInt(encCounter++).array();
            byte[] iv = cipher.doFinal(ivData);

            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keys.senc, new IvParameterSpec(iv));
            return cipher.doFinal(padded);
        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | BadPaddingException |
                 InvalidAlgorithmParameterException e) {
            //This should never happen
            throw new RuntimeException(e);
        } finally {
            Arrays.fill(padded, (byte) 0);
        }
    }

    byte[] decrypt(byte[] encrypted) throws BadResponseException {
        // Decrypt
        byte[] decrypted = null;
        try {
            @SuppressWarnings("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keys.senc);
            byte[] ivData = ByteBuffer.allocate(16).put((byte) 0x80).put(new byte[11])
                    .putInt(encCounter - 1).array();
            byte[] iv = cipher.doFinal(ivData);

            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keys.senc, new IvParameterSpec(iv));
            decrypted = cipher.doFinal(encrypted);
            for (int i = decrypted.length - 1; i > 0; i--) {
                if (decrypted[i] == (byte) 0x80) {
                    Logger.trace(logger, "Plaintext resp: {}", ByteUtils.bytesToHex(decrypted));
                    return Arrays.copyOf(decrypted, i);
                } else if (decrypted[i] != 0x00) {
                    break;
                }
            }
            throw new BadResponseException("Bad padding");
        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | BadPaddingException |
                 InvalidAlgorithmParameterException e) {
            //This should never happen
            throw new RuntimeException(e);
        } finally {
            if (decrypted != null) {
                Arrays.fill(decrypted, (byte) 0);
            }
        }
    }

    byte[] mac(byte[] data, ScpMode mode) {
        try {
            final Mac mac = Mac.getInstance("AESCMAC");
            mac.init(keys.smac);
            mac.update(macChain);
            macChain = mac.doFinal(data);
            final int macSize = ScpMode.getBlobSizeByMode(mode);
            return Arrays.copyOf(macChain, macSize);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnsupportedOperationException("Cryptography provider does not support AESCMAC", e);
        }
    }

    byte[] unmac(byte[] data, short sw, ScpMode mode) throws BadResponseException {
        final int macSize = ScpMode.getBlobSizeByMode(mode);
        byte[] msg = ByteBuffer.allocate(data.length - macSize + 2).put(data, 0, data.length - macSize)
                .putShort(sw).array();

        try {
            final Mac mac = Mac.getInstance("AESCMAC");
            mac.init(keys.srmac);
            mac.update(macChain);

            byte[] rmac = Arrays.copyOf(mac.doFinal(msg), macSize);
            if (MessageDigest.isEqual(rmac, Arrays.copyOfRange(data, data.length - macSize, data.length))) {
                return Arrays.copyOf(msg, msg.length - 2);
            }
            throw new BadResponseException("Wrong MAC");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnsupportedOperationException("Cryptography provider does not support AESCMAC", e);
        }
    }

    static Pair<ScpState, byte[]> scp03Init(ApduProcessor processor, Scp03KeyParams keyParams, ScpMode mode, @Nullable byte[] hostChallenge) throws BadResponseException, IOException, ApduException {
        final int cryptoBlobSize = ScpMode.getBlobSizeByMode(mode);
        if (hostChallenge == null) {
            hostChallenge = RandomUtils.getRandomBytes(cryptoBlobSize);
        }

        ApduResponse resp = processor.sendApdu(
            new Apdu(0x80,
                     SecurityDomainSession.INS_INITIALIZE_UPDATE,
                     keyParams.getKeyRef().getKvn(),
                     0x00,
                     hostChallenge,
                     0x00 /*Le*/,
                     true /*forceAddLe*/));
        if (resp.getSw() != SW.OK) {
            throw new ApduException(resp.getSw());
        }

        byte[] diversificationData = new byte[10];
        byte[] keyInfo = new byte[3];
        byte[] cardChallenge = new byte[cryptoBlobSize];
        byte[] cardCryptogram = new byte[cryptoBlobSize];
        ByteBuffer.wrap(resp.getData())
                .get(diversificationData)
                .get(keyInfo)
                .get(cardChallenge)
                .get(cardCryptogram);

        final byte iParameter = keyInfo[2];
        checkCardSecurityLevel(iParameter);

        final int contextSize = cryptoBlobSize * 2;
        byte[] context = ByteBuffer.allocate(contextSize).put(hostChallenge).put(cardChallenge).array();
        SessionKeys sessionKeys = keyParams.keys.derive(context);

        final short derivedDataLength = ScpMode.getDerivedDataBitsSizeByMode(mode);
        byte[] genCardCryptogram = StaticKeys.deriveKey(sessionKeys.smac, (byte) 0x00, context, derivedDataLength)
                .getEncoded();
        if (!MessageDigest.isEqual(genCardCryptogram, cardCryptogram)) {
            throw new BadResponseException("Wrong SCP03 key set");
        }

        byte[] hostCryptogram = StaticKeys.deriveKey(sessionKeys.smac, (byte) 0x01, context, derivedDataLength)
                .getEncoded();
        return new Pair<>(new ScpState(sessionKeys, new byte[16]), hostCryptogram);
    }

    static ScpState scp11Init(ApduProcessor processor,
                              Scp11KeyParams keyParams,
                              @Nullable KeyPair ephemeralKeyPair)
            throws BadResponseException, IOException, ApduException {
        // GPC v2.3 Amendment F (SCP11) v1.4 §7.1.1
        byte params;
        byte kid = keyParams.getKeyRef().getKid();
        switch (kid) {
            case ScpKid.SCP11a:
                params = 0b01;
                break;
            case ScpKid.SCP11b:
                params = 0b00;
                break;
            case ScpKid.SCP11c:
                params = 0b11;
                break;
            default:
                throw new IllegalArgumentException("Invalid SCP11 KID");
        }

        if (kid == ScpKid.SCP11a || kid == ScpKid.SCP11c) {
            // GPC v2.3 Amendment F (SCP11) v1.4 §7.5
            Objects.requireNonNull(keyParams.skOceEcka);
            int n = keyParams.certificates.size() - 1;
            if (n < 0) {
                throw new IllegalArgumentException("SCP11a and SCP11c require a certificate chain");
            }
            KeyRef oceRef = keyParams.oceKeyRef != null ? keyParams.oceKeyRef : new KeyRef((byte) 0, (byte) 0);
            for (int i = 0; i <= n; i++) {
                byte[] data = keyParams.certificates.get(i);
                byte p2 = (byte) (oceRef.getKid() | (i < n ? 0x80 : 0x00));
                ApduResponse resp = sendPerformSecurityApdu(
                    processor,
                    new Apdu(
                        0x80 /*CLA*/,
                        SecurityDomainSession.INS_PERFORM_SECURITY_OPERATION,
                        oceRef.getKvn(),
                        p2,
                        data,
                        0x00 /*Le*/,
                        true /*forceAddLe*/));
                if (resp.getSw() != SW.OK) {
                    throw new ApduException(resp.getSw());
                }
            }
        }

        byte[] keyUsage = new byte[]{0x3C}; // AUTHENTICATED | C_MAC | C_DECRYPTION | R_MAC | R_ENCRYPTION
        byte[] keyType = new byte[]{(byte) 0x88}; // AES
        final int keySizeInBytes = keyParams.sessionKeysAlg.getKeySizeInBytes();
        final int keyLenSize = 1;
        byte[] keyLen = ByteUtils.intToLength(BigInteger.valueOf(keySizeInBytes), keyLenSize);

        // Host ephemeral key
        try {
            ECPublicKey pkSdEcka = (ECPublicKey) keyParams.pkSdEcka;
            ECPrivateKey eskOceEcka;
            ECPublicKey epkOceEcka;
            if (ephemeralKeyPair == null) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(pkSdEcka.getParams());
                KeyPair generatedEphemeralKeyPair = kpg.generateKeyPair();
                eskOceEcka = (ECPrivateKey) generatedEphemeralKeyPair.getPrivate();
                epkOceEcka = (ECPublicKey) generatedEphemeralKeyPair.getPublic();
            } else {
                eskOceEcka = (ECPrivateKey) ephemeralKeyPair.getPrivate();
                epkOceEcka = (ECPublicKey) ephemeralKeyPair.getPublic();
            }


            PublicKeyValues.Ec epkOceEckaValues = (PublicKeyValues.Ec) PublicKeyValues.fromPublicKey(epkOceEcka);

            // GPC v2.3 Amendment F (SCP11) v1.4 §7.6.2.3
            byte[] data = Tlvs.encodeList(
                    Arrays.asList(
                            new Tlv(
                                    0xA6,
                                    Tlvs.encodeList(
                                            Arrays.asList(
                                                    new Tlv(0x90, new byte[]{0x11, params}),
                                                    new Tlv(0x95, keyUsage),
                                                    new Tlv(0x80, keyType),
                                                    new Tlv(0x81, keyLen)
                                            )
                                    )
                            ),
                            new Tlv(
                                    0x5F49,
                                    epkOceEckaValues.getEncodedPoint()
                            )
                    )
            );

            // Static host key (SCP11a/c), or ephemeral key again (SCP11b)
            PrivateKey skOceEcka = keyParams.skOceEcka != null ? keyParams.skOceEcka : eskOceEcka;
            int ins = keyParams.getKeyRef()
                    .getKid() == ScpKid.SCP11b ? SecurityDomainSession.INS_INTERNAL_AUTHENTICATE : SecurityDomainSession.INS_EXTERNAL_AUTHENTICATE;
            ApduResponse resp = processor.sendApdu(
                new Apdu(
                    0x80 /*CLA*/,
                    ins,
                    keyParams.getKeyRef().getKvn(),
                    keyParams.getKeyRef().getKid(),
                    data,
                    0x00 /*Le*/,
                    true /*forceAddLe*/));
            if (resp.getSw() != SW.OK) {
                throw new ApduException(resp.getSw());
            }
            List<Tlv> tlvs = Tlvs.decodeList(resp.getData());
            Tlv epkSdEckaTlv = tlvs.get(0);
            byte[] epkSdEckaEncodedPoint = Tlvs.unpackValue(0x5F49, epkSdEckaTlv.getBytes());
            byte[] receipt = Tlvs.unpackValue(0x86, tlvs.get(1).getBytes());

            // GPC v2.3 Amendment F (SCP11) v1.3 §3.1.2 Key Derivation
            byte[] keyAgreementData = ByteBuffer.allocate(data.length + epkSdEckaTlv.getBytes().length)
                    .put(data)
                    .put(epkSdEckaTlv.getBytes())
                    .array();
            byte[] sharedInfo = ByteBuffer.allocate(keyUsage.length + keyType.length + keyLen.length)
                    .put(keyUsage)
                    .put(keyType)
                    .put(keyLen)
                    .array();

            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");

            keyAgreement.init(eskOceEcka);
            keyAgreement.doPhase(PublicKeyValues.Ec.fromEncodedPoint(epkOceEckaValues.getCurveParams(), epkSdEckaEncodedPoint)
                    .toPublicKey(), true);
            byte[] ka1 = keyAgreement.generateSecret();

            keyAgreement.init(skOceEcka);
            keyAgreement.doPhase(pkSdEcka, true);
            byte[] ka2 = keyAgreement.generateSecret();

            byte[] keyMaterial = ByteBuffer.allocate(ka1.length + ka2.length).put(ka1).put(ka2)
                    .array();

            final int keysNum = 5;
            final int allKeysSize = keySizeInBytes * keysNum;
            final int hashSize = 32;
            final int requiredIterationsNum = (int) Math.ceil((double) allKeysSize / hashSize);
            int counter = 1;
            byte[] keyData = new byte[hashSize * requiredIterationsNum];
            for (int i = 0; i < requiredIterationsNum; i++) {
                final MessageDigest hash = MessageDigest.getInstance("SHA256");
                hash.update(keyMaterial);
                hash.update(ByteBuffer.allocate(4).putInt(counter++).array());
                hash.update(sharedInfo);
                byte[] digest = hash.digest();
                System.arraycopy(digest, 0, keyData, i * hashSize, digest.length);
                Arrays.fill(digest, (byte) 0);
            }
            List<SecretKey> keys = new ArrayList<>();
            for (int i = 0; i < keysNum; i++) {
                keys.add(new SecretKeySpec(keyData, i * keySizeInBytes, keySizeInBytes, "AES"));
            }
            Arrays.fill(keyData, (byte) 0);

            // 5 keys were derived. One for verification of receipt, 4 keys to use
            SecretKey key = keys.get(0);
            final Mac mac = Mac.getInstance("AESCMAC");
            mac.init(key);
            byte[] genReceipt = mac.doFinal(keyAgreementData);
            if (!MessageDigest.isEqual(receipt, genReceipt)) {
                throw new BadResponseException("Receipt does not match");
            }
            return new ScpState(new SessionKeys(
                    keys.get(1),
                    keys.get(2),
                    keys.get(3),
                    keys.get(4)
            ), receipt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] cbcEncrypt(SecretKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Investigate if separate ApduProcessor class is convenient
    private static ApduResponse sendPerformSecurityApdu(final ApduProcessor processor,
                                                        final Apdu apdu) throws BadResponseException, IOException {
        final byte[] data = apdu.getData();
        int offset = 0;
        int shortApduMaxChunk = 0xFF;
        byte[] chunk;
        while (data.length - offset > shortApduMaxChunk) {
            chunk = Arrays.copyOfRange(data, offset, (offset + shortApduMaxChunk));
            ApduResponse response = processor.sendApdu(new Apdu(
                    (byte) (apdu.getCla() | 0x10),
                    apdu.getIns(),
                    (byte) (apdu.getP1() | 0x80),
                    apdu.getP2(),
                    chunk,
                    apdu.getLe(),
                    apdu.isForceAddLe()));
            if (response.getSw() != SW.OK) {
                return response;
            }
            offset += shortApduMaxChunk;
        }
        chunk = Arrays.copyOfRange(data, offset, data.length);
        return processor.sendApdu(
            new Apdu(
                apdu.getCla(),
                apdu.getIns(),
                apdu.getP1(),
                apdu.getP2(),
                chunk,
                apdu.getLe(),
                apdu.isForceAddLe()));
    }

    // Secure Channel Protocol '03' v1.2, "Table 5-1: Values of Parameter “i”"
    private static void checkCardSecurityLevel(final byte iParameter) {
        final byte rMacMask = 0b01000000;
        final boolean isRMacSupported = ((iParameter & rMacMask) != 0);
        final byte rEncryptionMask = 0b00100000;
        final boolean isREncryptionSupported = ((iParameter & rEncryptionMask) != 0);
        if (!isRMacSupported || !isREncryptionSupported) {
            throw new UnsupportedOperationException("Only maximum security level is supported: " +
                "C-DECRYPTION, R-ENCRYPTION, C-MAC, and R-MAC. Card doesn't support R-MAC or R-ENCRYPTION");
        }
    }
}
