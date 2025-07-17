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
 * SCP mode, which defines size of challenges, cryptograms and MACs
 *
 * @see "Secure Channel Protocol '03' v1.2"
 */
public enum ScpMode {
    /**
     * 8 bytes
     */
    S8,

    /**
     * 16 bytes
     */
    S16;

    /**
     * Get size of challenge, cryptogram or MAC depending on the SCP mode
     *
     * @param mode SCP mode
     * @return Challenge, cryptogram or MAC size
     */
    static int getBlobSizeByMode(final ScpMode mode) {
        return (mode == S8) ? 8 : 16;
    }

    /**
     * Get derived data size in bits depending on the SCP mode
     * @see "Secure Channel Protocol '03' v1.2, "4.1.5 Data Derivation Scheme, 'L' parameter"
     *
     * @param mode SCP mode
     * @return Derived data size in bits
     */
    static short getDerivedDataBitsSizeByMode(final ScpMode mode) {
        return (mode == S8) ? (short) 0x40 : (short) 0x80;
    }
}
