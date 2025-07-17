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

package com.samsung.openscp.testdata;

import com.samsung.openscp.SmartCardConnection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmartCardEmulation implements SmartCardConnection {
    private final byte[][] capdusExpected;
    private final byte[][] rapdusToSend;
    private int apduIndex = 0;

    public SmartCardEmulation(final byte[][] capdusExpected, final byte[][] rapdusToSend) {
        assertEquals(capdusExpected.length, rapdusToSend.length);  // self-check
        this.capdusExpected = capdusExpected;
        this.rapdusToSend = rapdusToSend;
    }

    @Override
    public byte[] sendAndReceive(byte[] apdu) {
        assertTrue(apduIndex < capdusExpected.length);
        assertArrayEquals(capdusExpected[apduIndex], apdu);
        final byte[] rapdu = rapdusToSend[apduIndex];
        apduIndex++;
        return rapdu;
    }

    @Override
    public boolean isExtendedLengthApduSupported() {
        return true;
    }

    @Override
    public void close() {}

    public boolean isAllExpectedCapdusReceived() {
        return (apduIndex == capdusExpected.length);
    }
}
