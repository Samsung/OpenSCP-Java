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

public final class SmartCardScp03Aes128S16ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030001006F85B77251BF79406F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003710617A2C34D50B562F7DECFC3DBF58A649EF733B7486D813C92E4CA61E5102" +
                             "F9100082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84823300201C6A43582F842A90D81CA4E970754664D8C0C6C340E007C5C6EBCDF4F7C065BE");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2200020EB30913C363A6C1309D13ADC765D9DF1C44F207C2941B898A290F627B05AE990");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("05717C3DED78C6BB7AEE55A68E66E0D7C90473C380A60755DD7B63ECF55FD0B4067612B43A0A05CDA95" +
                             "E9122C91D8527A21681805C1A25AAA38994E15A6379DF66EE234701C673C32DAF5C54B11085C49000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2400020F1EC5CF268D357692CD0FAF186A1E6C79F68B473C4672C0A08F0437B16314698");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("D7B658F9308D59779672CE4E5D1B8E6BD2A5A90F745788DAA817980BF84C2522B791382D5A095868C09" +
                             "6AC6A851A98A028CBBA7AC67D5F834F7969FE199B0DBBA12EF90E27CCF95C062D27BADAFC3B03CFB696" +
                             "4CCBE3FC04B54EFCD3B446B4469000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2800020A9DDD67660ECC75765C431CD69AD53A79202F01E13B24C15F4CFF4C9B335EEE5");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("5A866565D1D0F35427E010043F82E8ABCA25C79A3CAA41547838978F779563FFDEA329E3ABD1CBA719A" +
                             "6D9C13B7D45CC9000");

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

    public SmartCardScp03Aes128S16ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
