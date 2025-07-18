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
 *   - Extend API with "force add Le" parameter
 */

package com.samsung.openscp;

import java.io.IOException;

abstract class ApduFormatProcessor implements ApduProcessor {
    protected final SmartCardConnection connection;

    ApduFormatProcessor(SmartCardConnection connection) {
        this.connection = connection;
    }

    abstract byte[] formatApdu(byte cla,
                               byte ins,
                               byte p1,
                               byte p2,
                               byte[] data,
                               int offset,
                               int length,
                               int le,
                               boolean forceAddLe);

    @Override
    public ApduResponse sendApdu(Apdu apdu) throws IOException {
        byte[] data = apdu.getData();
        byte[] payload = formatApdu(apdu.getCla(),
                                    apdu.getIns(),
                                    apdu.getP1(),
                                    apdu.getP2(),
                                    data,
                                    0,
                                    data.length,
                                    apdu.getLe(),
                                    apdu.isForceAddLe());
        return new ApduResponse(connection.sendAndReceive(payload));
    }
}
