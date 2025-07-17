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

public final class SmartCardScp03Aes128S8ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030000806F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003709CE033FA78E6B10DDC2DBE8974C8B0DE00082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("8482330010B08D6CE26B6CB3CCB411CF0296EB7B1D");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F22000185230BA64388B4A40E0B4DA5CC1DF51C285E4020D99D5AED1");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("BD3292BFB1A23C4478E37292BA1EDF438770CE472FB7611FBDBD1C981A27FA4780A81A95D93C05F9C4C" +
                             "94839DED0363CFEA57CE2ECFB572B26F3474DAEEBBABC202942381F9755F59000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F24000181819D47B42BBE6B9449BBC2BD43A090DAC1F2F0A52D9F34B");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("BB82442BB5CC8C839620615D1F163D3DDBC9357D68EF4BAD997CFBB79A24C224A89488C44B25C3B23D4" +
                             "89E4E58A309D438FDD6E453D0E07216541FB142B977A3A7D4C4048BBE2BA068F04A0A4A9C50BAD232F8" +
                             "CA8EA1F40E9000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F280001809F07C3DF47956B1052951FA28211BA7BABC05C321D9B3BF");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("31EB08363026463BAD10AF29F24301F1D9B8532067F9313D97FDA39BBE6B6099BEFD623E1F79FB5D9000");

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

    public SmartCardScp03Aes128S8ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
