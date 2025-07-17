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

public final class SmartCardScp03Aes192S16ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030001006F85B77251BF79406F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003710617A2C34D50B562F7DECFC3DBF58A64A378671D41E7F6E3F5254CA9D" +
                             "4C481A400082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("848233002099033B5BB5246C859B9748E497EDAE8D9EBB7BD196CE4E3EBFC92343D69478AB");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F220002032319E399171DAF20FD8E8ECD1CE462569B7D5645AAD98E537593EF48816DB4B");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("B6AB2FDB15B2D1A895B8329736C5AF47D4B6B6AECBB73E327A91D300D4BF0F36AD710F7FF5505481F69" +
                             "0EFE3561E1C5335EF510B018D56D21D5F1CA9557AC9E225D04413BAA39828273DBC4B00B0BF309000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2400020161D4B6C1E2D790D66EA1E890ADC3F45D59E2416D6DC553E8738C2ADA182E583");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("3C24F511B4923F55DE51925292BEED9456359F54F01B3D0C6C30C5666030788FE6BD1345C802AE66E8C" +
                             "B06C93E64CFE74590D83E85E8B1E06A7A750A48E8554B1DB948BFD041432E824BCDD24AACD87F65A986" +
                             "15076CAD3418BD67BFC1B4FC4C9000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F28000208BCC8139988A0F51441F6F8FF3C20A526CD783E30BEC1D8CC9D7E3D43AE779A5");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("B2332173A07F9EBE27EDEA0DB5849B6CACC0F28A77DA52AB9A1E151399ABD07115DDB34C92C8DA0FFE6" +
                             "79FD8D7DF0F369000");

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

    public SmartCardScp03Aes192S16ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
