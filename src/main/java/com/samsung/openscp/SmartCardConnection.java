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
 *   - Remove extending the YubiKeyConnection class
 *   - Remove getTransport() & getAtr() methods
 *   - YubiKey/YubiKit/Yubico mentions removed from code and comments
 */

package com.samsung.openscp;

import java.io.Closeable;
import java.io.IOException;

/**
 * A connection capable of sending APDUs and receiving their responses.
 */
public interface SmartCardConnection extends Closeable {
    /**
     * Sends a command APDU to the smart card, and reads a response.
     *
     * @param apdu The binary APDU data to be sent.
     * @return The response back from the smart card
     * @throws IOException in case of communication error
     */
    byte[] sendAndReceive(byte[] apdu) throws IOException;

    /**
     * Standard APDUs have a 1-byte length field, allowing a maximum of 255 payload bytes,
     * which results in a maximum APDU length of 261 bytes. Extended length APDUs have a 3-byte length field,
     * allowing 65535 payload bytes.
     *
     * @return true if this connection object supports Extended length APDUs.
     */
    boolean isExtendedLengthApduSupported();
}