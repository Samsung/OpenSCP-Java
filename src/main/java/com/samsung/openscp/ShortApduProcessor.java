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
 *   - Add flag to force include 0x00 Le in formatApdu() method
 */

package com.samsung.openscp;

import java.io.IOException;
import java.nio.ByteBuffer;

class ShortApduProcessor extends ApduFormatProcessor {
    private static final int SHORT_APDU_MAX_CHUNK = 0xff;

    ShortApduProcessor(SmartCardConnection connection) {
        super(connection);
    }

    @Override
    byte[] formatApdu(byte cla,
                      byte ins,
                      byte p1,
                      byte p2,
                      byte[] data,
                      int offset,
                      int length,
                      int le,
                      boolean forceAddLe) {
        if (length > SHORT_APDU_MAX_CHUNK) {
            throw new IllegalArgumentException("Length must be no greater than " + SHORT_APDU_MAX_CHUNK);
        }
        if (le < 0 || le > SHORT_APDU_MAX_CHUNK) {
            throw new IllegalArgumentException("Le must be between 0 and " + SHORT_APDU_MAX_CHUNK);
        }

        boolean shouldAddLe = (le > 0) || forceAddLe;
        int bufSize = 4 + (length > 0 ? 1 : 0) + length;
        if (shouldAddLe) {
            bufSize++;
        }
        ByteBuffer buf = ByteBuffer.allocate(bufSize)
                .put(cla)
                .put(ins)
                .put(p1)
                .put(p2);
        if (length > 0) {
            buf.put((byte) length).put(data, offset, length);
        }
        if (shouldAddLe) {
            buf.put((byte) le);
        }
        return buf.array();
    }

    @Override
    public ApduResponse sendApdu(Apdu apdu) throws IOException {
        byte[] data = apdu.getData();
        int offset = 0;
        while (data.length - offset > SHORT_APDU_MAX_CHUNK) {
            ApduResponse response = new ApduResponse(
                connection.sendAndReceive(
                    formatApdu((byte) (apdu.getCla() | 0x10),
                                apdu.getIns(),
                                apdu.getP1(),
                                apdu.getP2(),
                                data,
                                offset,
                                SHORT_APDU_MAX_CHUNK,
                                apdu.getLe(),
                                apdu.isForceAddLe())));
            if (response.getSw() != SW.OK) {
                return response;
            }
            offset += SHORT_APDU_MAX_CHUNK;
        }
        return new ApduResponse(
            connection.sendAndReceive(
                formatApdu(apdu.getCla(),
                           apdu.getIns(),
                           apdu.getP1(),
                           apdu.getP2(),
                           data,
                           offset,
                           data.length - offset,
                           apdu.getLe(),
                           apdu.isForceAddLe())));
    }

    @Override
    public void close() throws IOException {
    }
}
