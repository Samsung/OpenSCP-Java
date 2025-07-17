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
 *   - YubiKey/YubiKit/Yubico mentions removed from code and comments
 */

package com.samsung.openscp;

import java.io.IOException;
import java.nio.ByteBuffer;

class ExtendedApduProcessor extends ApduFormatProcessor {
    private final int maxApduSize;

    ExtendedApduProcessor(SmartCardConnection connection, int maxApduSize) {
        super(connection);
        this.maxApduSize = maxApduSize;
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
        boolean shouldAddLe = (le > 0) || forceAddLe;
        int bufSize = 5 + (data.length > 0 ? 2 : 0) + data.length;
        if (shouldAddLe) {
            bufSize += 2;
        }
        ByteBuffer buf = ByteBuffer.allocate(bufSize)
                .put(cla)
                .put(ins)
                .put(p1)
                .put(p2)
                .put((byte) 0x00);
        if (data.length > 0) {
            buf.putShort((short) data.length).put(data);
        }
        if (shouldAddLe) {
            buf.putShort((short) le);
        }
        if (buf.limit() > maxApduSize) {
            throw new UnsupportedOperationException("APDU length exceeds smart card capability");
        }
        return buf.array();
    }

    @Override
    public void close() throws IOException {
    }
}
