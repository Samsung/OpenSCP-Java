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

public final class SmartCardScp03Aes192S8ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030000806F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003709CE033FA78E6B10D6E7C64F962A822A400082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("848233001063B6CEFAC0EC098333860788C65220BA");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2200018D9EDCBCB7F69CB1EF0508E6EDE933A6D80091E7D99CB3E51");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("99077B167D43A4F313B59B63CC23EFD3B5158BDEF8F24D85E250570A4AAB81869A92307350267F0FBC2278F" +
                             "A3D34D2FD5D2B4E8C0362C01D082C76A17B80AEA4BA5FB9D7DA3BB3689000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2400018CDC4B0480CF151C1132655133115A8CA1A89964F2554551C");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("AB7A97B6C673DF3D95378D06B7B42E25D7C3B22D6D1A42299FFED17F5973950EC68C77700FC019470674701" +
                             "78A1D06152ED648E95E8C3510B61CF0036DFD8C9F6FA167D32FDEB3F81A0E6B2BB35BCD4CD104692D131D77" +
                             "769000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F280001896800333FA638A32DBCCBF4C7E52FBD5DA469A954E1D58F6");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("E5E5761FEFF5C0C078ADBC4E77B7290094C99183AC73CAB99A7412D0194DEFFDD0895DCCE662D9459000");

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

    public SmartCardScp03Aes192S8ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
