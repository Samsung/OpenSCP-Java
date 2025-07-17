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
 *   - Use short APDU for SCP
 *   - Parametrize with ScpMode variable, add S16 mode support
 *   - Minor updates according to other classes' API changes
 *   - Removed redundant `public` access modifiers
 */

package com.samsung.openscp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

class ScpProcessor extends ChainedResponseProcessor {
    private final ScpState state;
    private final ScpMode mode;

    ScpProcessor(SmartCardConnection connection, ScpState state, int maxApduSize, byte insSendRemaining, ScpMode mode) {
        super(connection, false /*isExtendedApduUsed*/, maxApduSize, insSendRemaining);
        this.state = state;
        this.mode = mode;
    }

    @Override
    public ApduResponse sendApdu(Apdu apdu) throws IOException, BadResponseException {
        return sendApdu(apdu, true);
    }

    ApduResponse sendApdu(Apdu apdu, boolean encrypt) throws IOException, BadResponseException {
        byte[] data = apdu.getData();
        if (encrypt) {
            data = state.encrypt(data);
        }
        byte cla = (byte) (apdu.getCla() | 0x04);

        // Calculate and add MAC to data
        final int macSize = ScpMode.getBlobSizeByMode(mode);
        byte[] macedData = new byte[data.length + macSize];
        System.arraycopy(data, 0, macedData, 0, data.length);
        byte[] apduData = processor.formatApdu(
            cla,
            apdu.getIns(),
            apdu.getP1(),
            apdu.getP2(),
            macedData,
            0,
            macedData.length,
            0,
            /* Mandatory for correct MAC calculation over whole APDU blob without Le byte */
            false );

        byte[] mac = state.mac(Arrays.copyOf(apduData, apduData.length - macSize), mode);
        System.arraycopy(mac, 0, macedData, macedData.length - macSize, macSize);

        ApduResponse resp = super.sendApdu(new Apdu(cla, apdu.getIns(), apdu.getP1(), apdu.getP2(), macedData, apdu.getLe()));
        byte[] respData = resp.getData();

        // Un-MAC and decrypt, if needed
        if (respData.length > 0) {
            respData = state.unmac(respData, resp.getSw(), mode);
        }
        if (respData.length > 0) {
            respData = state.decrypt(respData);
        }

        return new ApduResponse(ByteBuffer.allocate(respData.length + 2).put(respData).putShort(resp.getSw()).array());
    }
}
