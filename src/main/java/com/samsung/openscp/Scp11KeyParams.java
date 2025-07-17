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
 *   - Added AES algorithm variable
 *   - Add missed JavaDocs
 */

package com.samsung.openscp;

import javax.annotation.Nullable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SCP key parameters for performing SCP11 authentication.
 * <p>
 * For SCP11b only keyRef and pkSdEcka are required. NOTE: this does not authenticate the off-card entity.
 * <p>
 * For SCP11a and SCP11c the off-card entity CA key reference must be provided, as well as the off-card entity secret key and certificate chain.
 */
public class Scp11KeyParams implements ScpKeyParams {
    private final KeyRef keyRef;
    final PublicKey pkSdEcka;
    @Nullable
    final KeyRef oceKeyRef;
    @Nullable
    final PrivateKey skOceEcka;
    final List<byte[]> certificates;
    final AesAlg sessionKeysAlg;

    /**
     * SCP11a- & SCP11c-specific constructor
     *
     * @param keyRef the reference to the key set for associated SK.SD.ECKA
     * @param pkSdEcka public key of the SD used for key agreement (PK.SD.ECKA)
     * @param oceKeyRef the reference to the key set for associated SK.OCE.ECKA
     * @param skOceEcka private key of the OCE used for key agreement (SK.OCE.ECKA)
     * @param certificates OCE certificates in encoded form
     * @param sessionKeysAlg AES algorithm of session keys that will be generated
     */
    public Scp11KeyParams(KeyRef keyRef, PublicKey pkSdEcka,
                          @Nullable KeyRef oceKeyRef,
                          @Nullable PrivateKey skOceEcka,
                          List<byte[]> certificates,
                          AesAlg sessionKeysAlg) {
        this.keyRef = keyRef;
        this.pkSdEcka = pkSdEcka;
        this.oceKeyRef = oceKeyRef;
        this.skOceEcka = skOceEcka;
        this.certificates = Collections.unmodifiableList(new ArrayList<>(certificates));
        this.sessionKeysAlg = sessionKeysAlg;
        switch (keyRef.getKid()) {
            case ScpKid.SCP11b:
                if (oceKeyRef != null || skOceEcka != null || !certificates.isEmpty()) {
                    throw new IllegalArgumentException("Cannot provide oceKeyRef, skOceEcka or certificates for SCP11b");
                }
                break;
            case ScpKid.SCP11a:
            case ScpKid.SCP11c:
                if (oceKeyRef == null || skOceEcka == null || certificates.isEmpty()) {
                    throw new IllegalArgumentException("Must provide oceKeyRef, skOceEcka or certificates for SCP11a/c");
                }
                break;
            default:
                throw new IllegalArgumentException("KID must be 0x11, 0x13, or 0x15 for SCP11");
        }
    }

    /**
     * SCP11b-specific constructor
     *
     * @param keyRef the reference to the key set for associated SK.SD.ECKA
     * @param pkSdEcka public key of the SD used for key agreement (PK.SD.ECKA)
     * @param sessionKeysAlg AES algorithm of session keys that will be generated
     */
    public Scp11KeyParams(KeyRef keyRef, PublicKey pkSdEcka, AesAlg sessionKeysAlg) {
        this(keyRef, pkSdEcka, null, null, Collections.emptyList(), sessionKeysAlg);
    }

    @Override
    public KeyRef getKeyRef() {
        return keyRef;
    }
}
