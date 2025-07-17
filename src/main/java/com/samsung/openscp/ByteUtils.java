/*
 * Copyright (C) 2023 Yubico.
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
 *   - bytesToHex() & hexToBytes() methods added
 *   - YubiKey/YubiKit/Yubico mentions removed from code and comments
 *   - Removed redundant `public` access modifiers
 */

package com.samsung.openscp;

import java.math.BigInteger;
import java.util.Arrays;

final class ByteUtils {
    /**
     * Serializes a BigInteger as an unsigned integer of the given length.
     *
     * @param value the integer to serialize
     * @param length the length of the byte[] to return
     * @return the value as an unsigned integer
     */
    static byte[] intToLength(BigInteger value, int length) {
        byte[] data = value.toByteArray();
        if (data.length == length) {
            return data;
        } else if (data.length < length) {
            byte[] padded = new byte[length];
            System.arraycopy(data, 0, padded, length - data.length, data.length);
            return padded;
        } else if (data.length == length + 1 && data[0] == 0) {
            // BigInteger may have a leading zero, since it's signed.
            return Arrays.copyOfRange(data, 1, data.length);
        } else {
            throw new IllegalArgumentException("value is too large to be represented in " + length + " bytes");
        }
    }

    /**
     * Constructs a hex String from a byte array
     *
     * @param bytes byte array to construct hex
     * @return hex value of a byte array
     */
    static String bytesToHex(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Constructs a byte array from a hex String
     *
     * @param hex String value to construct byte array
     * @return byte array constructed from hex
     */
    static byte[] hexToBytes(final String hex) {
        if (hex == null) {
            return null;
        }

        final int len = hex.length();
        final int byteInHexSize = 2;
        byte[] data = new byte[len / byteInHexSize];
        final int radixHex = 16;
        final int bytesShift = 4;
        for (int i = 0; i < len; i += byteInHexSize) {
            data[i / byteInHexSize] = (byte) ((Character.digit(hex.charAt(i), radixHex) << bytesShift)
                + Character.digit(hex.charAt(i + 1), radixHex));
        }
        return data;
    }
}
