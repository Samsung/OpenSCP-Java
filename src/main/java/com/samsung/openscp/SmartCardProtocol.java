/*
 * Copyright (C) 2019-2024 Yubico.
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
 *   - Updated package and import statements during the code move from the original project
 *   - Parametrized initScp() & initScp03() methods with the host challenge
 *   - Parametrized initScp() & initScp11() methods with the ephemeral keys
 *   - Parametrized with ScpMode variable, add S16 mode support
 *   - Removed YubiKey/YubiKit/Yubico mentions from code and comments
 *   - Removed configure(), enableWorkarounds(), setEnableTouchWorkaround() & setApduFormat() methods
 *   - Removed redundant `public` access modifiers
 *   - Removed check for extended APDU usage for SCP, used max APDU based on the connection settings
 *   - Added sendAndReceiveApdu() method to receive raw RAPDU bytes
 */

package com.samsung.openscp;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.security.KeyPair;

/**
 * Support class for communication over a SmartCardConnection.
 * <p>
 * This class handles APDU encoding and chaining, and implements workarounds for known issues.
 */
class SmartCardProtocol implements Closeable {
    private static final byte INS_SELECT = (byte) 0xa4;
    private static final byte P1_SELECT = (byte) 0x04;
    private static final byte P2_SELECT = (byte) 0x00;

    private static final byte INS_SEND_REMAINING = (byte) 0xc0;

    private final byte insSendRemaining;

    private final SmartCardConnection connection;

    private boolean extendedApdus = false;

    private int maxApduSize = MaxApduSize.SHORT;

    private ApduProcessor processor;

    /**
     * Create new instance of {@link SmartCardProtocol}
     * and selects the application for use
     *
     * @param connection connection to the smart card
     */
    SmartCardProtocol(SmartCardConnection connection) {
        this(connection, INS_SEND_REMAINING);
    }

    SmartCardProtocol(SmartCardConnection connection, byte insSendRemaining) {
        this.connection = connection;
        this.insSendRemaining = insSendRemaining;
        processor = new ChainedResponseProcessor(connection, false, maxApduSize, insSendRemaining);
    }

    private void resetProcessor(@Nullable ApduProcessor processor) throws IOException {
        this.processor.close();
        if (processor != null) {
            this.processor = processor;
        } else {
            this.processor = new ChainedResponseProcessor(connection, extendedApdus, maxApduSize, insSendRemaining);
        }
    }

    @Override
    public void close() throws IOException {
        processor.close();
        connection.close();
    }

    /**
     * @return the underlying connection
     */
    SmartCardConnection getConnection() {
        return connection;
    }

    /**
     * Sends an APDU to SELECT an Application.
     *
     * @param aid the AID to select.
     * @return the response data from selecting the Application
     * @throws IOException                      in case of connection or communication error
     * @throws ApplicationNotAvailableException in case the AID doesn't match an available application
     */
    byte[] select(byte[] aid) throws IOException, ApplicationNotAvailableException {
        resetProcessor(null);
        try {
            return sendAndReceive(new Apdu(0, INS_SELECT, P1_SELECT, P2_SELECT, aid));
        } catch (ApduException e) {
            // NEO sometimes returns INVALID_INSTRUCTION instead of FILE_NOT_FOUND
            if (e.getSw() == SW.FILE_NOT_FOUND || e.getSw() == SW.INVALID_INSTRUCTION) {
                throw new ApplicationNotAvailableException("The application couldn't be selected", e);
            }
            throw new IOException("Unexpected SW", e);
        }
    }

    /**
     * Sends APDU command and receives byte array from connection
     * <p>
     * In case if output has status code that it has remaining info sends another APDU command to receive what's remaining
     *
     * @param command well-structured command that needs to be sent
     * @return data blob concatenated from all APDU commands that were sent *set of output commands and send remaining commands)
     * @throws IOException   in case of connection and communication error
     * @throws ApduException in case if received error in APDU response
     */
    byte[] sendAndReceive(Apdu command) throws IOException, ApduException {
         return sendAndReceiveResponse(command).getData();
    }

    /**
     * Sends APDU command and receives byte array from connection
     * <p>
     * In case if output has status code that it has remaining info sends another APDU command to receive what's remaining
     *
     * @param command well-structured command that needs to be sent
     * @return data blob raw RAPDU bytes
     * @throws IOException   in case of connection and communication error
     * @throws ApduException in case if received error in APDU response
     */
    byte[] sendAndReceiveApdu(Apdu command) throws IOException, ApduException {
        return sendAndReceiveResponse(command).getBytes();
    }

    @Nullable DataEncryptor initScp(ScpKeyParams keyParams, ScpMode mode)
            throws IOException, ApduException, BadResponseException {
        return initScp(keyParams, mode, null, null);
    }

    @Nullable DataEncryptor initScp(ScpKeyParams keyParams, ScpMode mode, @Nullable byte[] hostChallenge)
            throws IOException, ApduException, BadResponseException {
        return initScp(keyParams, mode, hostChallenge, null);
    }

    @Nullable DataEncryptor initScp(ScpKeyParams keyParams, ScpMode mode, @Nullable KeyPair ephemeralKeys)
            throws IOException, ApduException, BadResponseException {
        return initScp(keyParams, mode, null, ephemeralKeys);
    }

    private ApduResponse sendAndReceiveResponse(Apdu command) throws IOException, ApduException {
        try {
            ApduResponse response = processor.sendApdu(command);
            if (response.getSw() != SW.OK) {
                throw new ApduException(response.getSw());
            }
            return response;
        } catch (BadResponseException e) {
            throw new IOException(e);
        }
    }

    private @Nullable DataEncryptor initScp(ScpKeyParams keyParams,
                                            ScpMode mode,
                                            @Nullable byte[] hostChallenge,
                                            @Nullable KeyPair ephemeralKeys)
            throws IOException, ApduException, BadResponseException {
        try {
            ScpState state;
            if (keyParams instanceof Scp03KeyParams) {
                state = initScp03((Scp03KeyParams) keyParams, mode, hostChallenge);
            } else if (keyParams instanceof Scp11KeyParams) {
                state = initScp11((Scp11KeyParams) keyParams, ephemeralKeys, mode);
            } else {
                throw new IllegalArgumentException("Unsupported ScpKeyParams");
            }
            extendedApdus = connection.isExtendedLengthApduSupported();
            maxApduSize = extendedApdus ? MaxApduSize.EXTENDED : MaxApduSize.SHORT;
            return state.getDataEncryptor();
        } catch (ApduException e) {
            if (e.getSw() == SW.CLASS_NOT_SUPPORTED) {
                throw new UnsupportedOperationException("This smart card does not support secure messaging");
            }
            throw e;
        }
    }

    private ScpState initScp03(Scp03KeyParams keyParams, ScpMode mode, @Nullable byte[] hostChallenge) throws IOException, ApduException, BadResponseException {
        Pair<ScpState, byte[]> pair = ScpState.scp03Init(processor, keyParams, mode, hostChallenge);
        ScpProcessor processor = new ScpProcessor(connection, pair.first, maxApduSize, insSendRemaining, mode);

        // Send EXTERNAL AUTHENTICATE
        // P1 = C-DECRYPTION, R-ENCRYPTION, C-MAC, and R-MAC
        ApduResponse resp = processor.sendApdu(new Apdu(0x84, 0x82, 0x33, 0, pair.second), false);
        if (resp.getSw() != SW.OK) {
            throw new ApduException(resp.getSw());
        }
        resetProcessor(processor);
        return pair.first;
    }

    private ScpState initScp11(Scp11KeyParams keyParams, @Nullable KeyPair ephemeralKeys, ScpMode mode)
            throws IOException, ApduException, BadResponseException {
        ScpState scp = ScpState.scp11Init(processor, keyParams, ephemeralKeys);
        resetProcessor(new ScpProcessor(connection, scp, maxApduSize, insSendRemaining, mode));
        return scp;
    }
}
