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


/**
 * AES algorithm, which defines AES session keys size
 *
 * @see "Secure Channel Protocol '03' v1.2"
 */
public enum AesAlg {
    /** 128 bit */
    AES_128(16),

    /** 192 bit */
    AES_192(24),

    /** 256 bit */
    AES_256(32);

    private final int keySizeInBytes;

    AesAlg(final int keySizeInBytes) {
        this.keySizeInBytes = keySizeInBytes;
    }

    final int getKeySizeInBytes() {
        return keySizeInBytes;
    }
}
