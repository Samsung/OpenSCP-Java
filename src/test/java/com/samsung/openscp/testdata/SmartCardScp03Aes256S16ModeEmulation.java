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

import com.samsung.openscp.BytesUtils;

public final class SmartCardScp03Aes256S16ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030001006F85B77251BF79406F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003710617A2C34D50B562F7DECFC3DBF58A6499D2966CF09C9201FDE8343E9" +
                             "3FB18B400082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("848233002071D1A091C5D24AF177B00E69B1A32B4E30A5168CF8C86B19730DF915EF82D8A7");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2200020DD3036DC4C636717702732ABBA185633873A933310002B25E386CF10A4E6B130");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("A5E7309DCA3EFFA13D1AA03FE0D3E2BA8D04DF15A6A26989150F512ED453F5DFFB2A7E4EE4E2FFACEFC" +
                             "0945435BCAF3A914A9F7BD66B4F82635B7757AE82B54262489B3B58224F99E846E2A8061B17329000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2400020EC2F4F2DDA358BF87C9A946D129D7C12F132C6A63CDAD60E80526C9A6487580C");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("160229863B7636672F094A2C2E62F845D3A4A38C0FB9171AF14FEE433CF4C2FA10A6E4F1EC14E8792FE" +
                             "0A009720C290A64C2FD9183167BFFF0AC4AF516838DE4E5A62B6AC4444AC81E5C73A6B7A57D6C55D268" +
                             "2B2F986C1F1FCD77DDAF69846C9000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F28000207C873E7BDCE82D67EC28783A3C3222C01E70D8C7A498CA3C7E348944F44A18B3");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("90AFB999D6499F47285B6319135F67F78D4CE73A3BD87F97DE623488BA9DBC7549F27DFF5B61E215A5C" +
                             "91F99D21682119000");

    private static final byte[][] CAPDUS_EXPECTED = new byte[][] {
        INITIALIZE_UPDATE_CAPDU_EXPECTED,
        EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED,
        LIST_PACKAGES_CAPDU_EXPECTED,
        LIST_APPLETS_CAPDU_EXPECTED,
        LIST_ISSUER_DOMAIN_CAPDU_EXPECTED
    };
    private static final byte[][] RAPDUS_TO_SEND = new byte[][] {
        INITIALIZE_UPDATE_RAPDU,
        EXTERNAL_AUTHENTICATE_RAPDU,
        LIST_PACKAGES_RAPDU,
        LIST_APPLETS_RAPDU,
        LIST_ISSUER_DOMAIN_RAPDU
    };

    public SmartCardScp03Aes256S16ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
