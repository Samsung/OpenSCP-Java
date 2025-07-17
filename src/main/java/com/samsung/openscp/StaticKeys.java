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
 *   - DEFAULT_KEY variable and getDefaultKeys() method removed
 *   - All AES keys sizes support added
 *   - Code refactored
 *   - Add missed JavaDocs
 */

package com.samsung.openscp;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * SCP03 static AES keys
 */
public class StaticKeys {
    final SecretKey enc;
    final SecretKey mac;
    @Nullable final SecretKey dek;

    /**
     * @param enc Static Secure Channel Encryption Key
     * @param mac Static Secure Channel Message Authentication Code Key
     * @param dek Data Encryption Key
     */
    public StaticKeys(byte[] enc, byte[] mac, @Nullable byte[] dek) {
        this.enc = new SecretKeySpec(enc, "AES");
        this.mac = new SecretKeySpec(mac, "AES");
        this.dek = dek != null ? new SecretKeySpec(dek, "AES") : null;
    }

    /**
     * Derive session AES keys
     *
     * @param context derivation data
     * @return session keys object
     */
    public SessionKeys derive(byte[] context) {
        final short keyLengthBits = (short) (this.enc.getEncoded().length * 8);
        return new SessionKeys(
            deriveKey(enc, (byte) 0x4, context, keyLengthBits),
            deriveKey(mac, (byte) 0x6, context, keyLengthBits),
            deriveKey(mac, (byte) 0x7, context, keyLengthBits),
            dek
        );
    }

    // Secure Channel Protocol '03' v1.2, "4.1.5 Data Derivation Scheme"
    // NIST SP 800-108 Rev. 1, "4.1 KDF in Counter Mode"
    static SecretKey deriveKey(final SecretKey key,
                               final byte dataDerivationConstant,
                               final byte[] context,
                               final short derivedDataLengthBits) {
        byte[] digest = null;
        try {
            byte iterationCounter = 0x01;
            final byte[] derivationInputData =
                buildDerivationInputData(dataDerivationConstant, derivedDataLengthBits, iterationCounter, context);
            digest = doAesCmac(key, derivationInputData);
            final int derivedDataLength = derivedDataLengthBits / 8;
            final boolean isSecondRoundNeeded = derivedDataLength > digest.length;
            if (isSecondRoundNeeded) {
                iterationCounter++;
                final byte[] derivationInputDataSecondRound =
                    buildDerivationInputData(dataDerivationConstant, derivedDataLengthBits, iterationCounter, context);
                final byte[] digestSecond = doAesCmac(key, derivationInputDataSecondRound);
                digest = ByteBuffer.allocate(digest.length + digestSecond.length)
                        .put(digest)
                        .put(digestSecond)
                        .array();
            }
            return new SecretKeySpec(digest, 0, derivedDataLength, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnsupportedOperationException("Cryptography provider does not support AESCMAC", e);
        } finally {
            if (digest != null) {
                Arrays.fill(digest, (byte) 0);
            }
        }
    }

    private static byte[] buildDerivationInputData(final byte dataDerivationConstant,
                                                   final short derivedDataLengthBits,
                                                   final byte counter,
                                                   final byte[] context) {
        final byte separationIndicator = 0x00;
        return ByteBuffer.allocate(16 + context.length)
                .put(new byte[11])
                .put(dataDerivationConstant)
                .put(separationIndicator)
                .putShort(derivedDataLengthBits)
                .put(counter)
                .put(context)
                .array();
    }

    private static byte[] doAesCmac(final SecretKey aesKey, final byte[] derivationInputData)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance("AESCMAC");
        mac.init(aesKey);
        return mac.doFinal(derivationInputData);
    }
}
