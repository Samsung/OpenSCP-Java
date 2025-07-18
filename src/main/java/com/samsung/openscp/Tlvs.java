/*
 * Copyright (C) 2019-2022 Yubico.
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
 *   - Removed redundant `public` access modifiers
 */

package com.samsung.openscp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to encode and decode BER-TLV data.
 */
class Tlvs {

    /**
     * Decodes a sequence of BER-TLV encoded data into a list of Tlvs.
     *
     * @param data sequence of TLV encoded data
     * @return list of Tlvs
     */
    static List<Tlv> decodeList(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<Tlv> tlvs = new ArrayList<>();
        while (buffer.hasRemaining()) {
            Tlv tlv = Tlv.parseFrom(buffer);
            tlvs.add(tlv);
        }
        return tlvs;
    }

    /**
     * Decodes a sequence of BER-TLV encoded data into a mapping of Tag-Value pairs.
     * <p>
     * Iteration order is preserved. If the same tag occurs more than once only the latest will be kept.
     *
     * @param data sequence of TLV encoded data
     * @return map of Tag-Value pairs
     */
    static Map<Integer, byte[]> decodeMap(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Map<Integer, byte[]> tlvs = new LinkedHashMap<>();
        while (buffer.hasRemaining()) {
            Tlv tlv = Tlv.parseFrom(buffer);
            tlvs.put(tlv.getTag(), tlv.getValue());
        }
        return tlvs;
    }

    /**
     * Encodes a List of Tlvs into an array of bytes.
     *
     * @param list list of Tlvs
     * @return the data encoded as a sequence of TLV values
     */
    static byte[] encodeList(Iterable<? extends Tlv> list) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Tlv tlv : list) {
            byte[] tlvBytes = tlv.getBytes();
            stream.write(tlvBytes, 0, tlvBytes.length);
        }
        return stream.toByteArray();
    }

    /**
     * Encodes a Map of Tag-Value pairs into an array of bytes.
     * <p>
     * NOTE: If order is important use a Map implementation that preserves order, such as LinkedHashMap.
     *
     * @param map the tag-value mappings
     * @return the data encoded as a sequence of TLV values
     */
    static byte[] encodeMap(Map<Integer, byte[]> map) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Map.Entry<Integer, byte[]> entry : map.entrySet()) {
            Tlv tlv = new Tlv(entry.getKey(), entry.getValue());
            byte[] tlvBytes = tlv.getBytes();
            stream.write(tlvBytes, 0, tlvBytes.length);
        }
        return stream.toByteArray();
    }

    /**
     * Decode a single TLV encoded object, returning only the value.
     *
     * @param expectedTag the expected tag value of the given TLV data
     * @param tlvData     the TLV data
     * @return the value of the TLV
     * @throws BadResponseException if the TLV tag differs from expectedTag
     */
    static byte[] unpackValue(int expectedTag, byte[] tlvData) throws BadResponseException {
        Tlv tlv = Tlv.parse(tlvData, 0, tlvData.length);
        if (tlv.getTag() != expectedTag) {
            throw new BadResponseException(String.format("Expected tag: %02x, got %02x", expectedTag, tlv.getTag()));
        }
        return tlv.getValue();
    }
}
