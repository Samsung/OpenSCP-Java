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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class TestUtils {
    private TestUtils() {
        throw new RuntimeException("Illegal attempt to instantiate an utility class");
    }

    static void executeGetStatusCmd(final SecurityDomainSession session, final byte elementId, final byte[] rapduExpected)
            throws ApduException, IOException {
        final Apdu capdu = TestUtils.constructGetStatusCapdu(elementId);
        final byte[] rapdu = session.sendAndReceive(capdu);
        assertNotNull(rapdu);
        assertArrayEquals(rapduExpected, rapdu);
    }

    static SecurityDomainSession initSecurityDomainSession(final SmartCardConnection connection) {
        return new SecurityDomainSession(connection, new BouncyCastleProvider());
    }

    // GlobalPlatform Card Specification v2.3.1, "11.4 GET STATUS Command"
    private static Apdu constructGetStatusCapdu(final byte elementId) {
        return new Apdu((byte) 0x80, (byte) 0xF2, elementId, (byte) 0x00, ByteUtils.hexToBytes("4F00"));
    }
}
