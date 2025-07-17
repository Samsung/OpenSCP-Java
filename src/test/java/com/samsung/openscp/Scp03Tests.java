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

import com.samsung.openscp.testdata.InputTestData;
import com.samsung.openscp.testdata.OutputTestData;
import com.samsung.openscp.testdata.SmartCardScp03Aes128S16ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp03Aes128S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp03Aes192S16ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp03Aes192S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp03Aes256S16ModeEmulation;
import com.samsung.openscp.testdata.SmartCardScp03Aes256S8ModeEmulation;
import com.samsung.openscp.testdata.SmartCardEmulation;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Scp03Tests {
    @Test
    void aes128S8ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes128,
                     ScpMode.S8,
                     InputTestData.hostChallengeS8,
                     new SmartCardScp03Aes128S8ModeEmulation());
    }

    @Test
    void aes192S8ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes192,
                     ScpMode.S8,
                     InputTestData.hostChallengeS8,
                     new SmartCardScp03Aes192S8ModeEmulation());
    }

    @Test
    void aes256S8ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes256,
                     ScpMode.S8,
                     InputTestData.hostChallengeS8,
                     new SmartCardScp03Aes256S8ModeEmulation());
    }

    @Test
    void aes128S16ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes128,
                     ScpMode.S16,
                     InputTestData.hostChallengeS16,
                     new SmartCardScp03Aes128S16ModeEmulation());
    }

    @Test
    void aes192S16ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes192,
                     ScpMode.S16,
                     InputTestData.hostChallengeS16,
                     new SmartCardScp03Aes192S16ModeEmulation());
    }

    @Test
    void aes256S16ModePositive() throws IOException, ApduException, BadResponseException {
        positiveTest(InputTestData.staticKeysAes256,
                     ScpMode.S16,
                     InputTestData.hostChallengeS16,
                     new SmartCardScp03Aes256S16ModeEmulation());
    }

    private void positiveTest(final StaticKeys staticKeys,
                              final ScpMode mode,
                              final byte[] hostChallenge,
                              final SmartCardEmulation connection)
            throws IOException, ApduException, BadResponseException {
        final KeyRef keyRef = new KeyRef((byte) 0x01, (byte) 0x30);
        final SecurityDomainSession session = TestUtils.initSecurityDomainSession(connection);
        final Scp03KeyParams keyParams = new Scp03KeyParams(keyRef, staticKeys);
        session.authenticate(keyParams, mode, hostChallenge);

        TestUtils.executeGetStatusCmd(
            session,
            InputTestData.LIST_PACKAGES_ID,
            OutputTestData.LIST_PACKAGES_RSP_PLAIN_DATA);
        TestUtils.executeGetStatusCmd(
            session,
            InputTestData.LIST_APPLETS_ID,
            OutputTestData.LIST_APPLETS_RSP_PLAIN_DATA);
        TestUtils.executeGetStatusCmd(
            session,
            InputTestData.LIST_ISSUER_DOMAIN_ID,
            OutputTestData.LIST_ISSUER_DOMAIN_RSP_PLAIN_DATA);

        assertTrue(connection.isAllExpectedCapdusReceived());
    }
}