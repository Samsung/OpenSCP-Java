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

public final class SmartCardScp03Aes256S8ModeEmulation extends SmartCardEmulation {
    private static final byte[] INITIALIZE_UPDATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("805030000806F85B77251BF79400");
    private static final byte[] INITIALIZE_UPDATE_RAPDU =
        BytesUtils.hexToBytes("A1A012430585513120853003709CE033FA78E6B10D8AFA7267CB63740E00082A9000");

    private static final byte[] EXTERNAL_AUTHENTICATE_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("848233001050E003735F92228269A094FFC07429FD");
    private static final byte[] EXTERNAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes("9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F2200018BD57D1382AE8F66F7EB5F5991B92D1399044157C7DFD2761");
    private static final byte[] LIST_PACKAGES_RAPDU =
        BytesUtils.hexToBytes("47D81041004B7E9208E3BEF1372E7CDE8CD995AEF207F138C80D45156F2D36F2B15BDC9C4D6FDB977434" +
                             "4495CCC83AE7BA0B39C734BF9CEBD07204AA5A67DF2D7E663D55FC4944C09000");

    private static final byte[] LIST_APPLETS_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F240001845D2B475C3EFF8BBB254D0B6A8E6CA97CF5265D907A76070");
    private static final byte[] LIST_APPLETS_RAPDU =
        BytesUtils.hexToBytes("59E8DCFDC22D436336552128F790E1B383D6942ED4025F30FE8D95541E634E238BFD963D88DF822D8EB" +
                             "CC1272A9D56C7D1CBC306039647FC4977EFF562C0B8C01314B1C8B2D168A581A98C65B676B3EE4032E9" +
                             "1A9C0858EC9000");

    private static final byte[] LIST_ISSUER_DOMAIN_CAPDU_EXPECTED =
        BytesUtils.hexToBytes("84F28000187083CA3ADA0F76CF7B3FFAC60ABE13599D521EE7B9224C49");
    private static final byte[] LIST_ISSUER_DOMAIN_RAPDU =
        BytesUtils.hexToBytes("E58CB33A46F76909ADDDFF0C2821F4F25F22B5553C534DC69000");

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

    public SmartCardScp03Aes256S8ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
