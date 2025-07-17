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

import com.samsung.openscp.StaticKeys;
import com.samsung.openscp.BytesUtils;

public class InputTestData {
    public final static byte LIST_PACKAGES_ID = (byte) 0x20; // list packages
    public final static byte LIST_APPLETS_ID = (byte) 0x40; // list applets or security domains
    public final static byte LIST_ISSUER_DOMAIN_ID = (byte) 0x80; // list Card Manager / Security Issuer Domain

    private final static byte[] encKeyAes128 = BytesUtils.hexToBytes("1D72CD9283FD55162722C6BEAA4DC187");
    private final static byte[] macKeyAes128 = BytesUtils.hexToBytes("F4932BA02FFC3098D172790099D28382");
    private final static byte[] dekKeyAes128 = BytesUtils.hexToBytes("B4BDC610C3F6793708FF1132E2C5BF60");

    private final static byte[] encKeyAes192 = BytesUtils.hexToBytes("1D72CD9283FD55162722C6BEAA4DC1877F4C0CD0ECC15E05");
    private final static byte[] macKeyAes192 = BytesUtils.hexToBytes("F4932BA02FFC3098D172790099D2838236F2E61068D56F44");
    private final static byte[] dekKeyAes192 = BytesUtils.hexToBytes("B4BDC610C3F6793708FF1132E2C5BF60523AEAC06B32F204");

    private final static byte[] encKeyAes256 =
        BytesUtils.hexToBytes("1D72CD9283FD55162722C6BEAA4DC1877F4C0CD0ECC15E052AAC39A99AF9AD72");
    private final static byte[] macKeyAes256 =
        BytesUtils.hexToBytes("F4932BA02FFC3098D172790099D2838236F2E61068D56F4401CC0374C25AF8CB");
    private final static byte[] dekKeyAes256 =
        BytesUtils.hexToBytes("B4BDC610C3F6793708FF1132E2C5BF60523AEAC06B32F204B851B6CC007C8D3C");

    public final static StaticKeys staticKeysAes128 = new StaticKeys(encKeyAes128, macKeyAes128, dekKeyAes128);
    public final static StaticKeys staticKeysAes192 = new StaticKeys(encKeyAes192, macKeyAes192, dekKeyAes192);
    public final static StaticKeys staticKeysAes256 = new StaticKeys(encKeyAes256, macKeyAes256, dekKeyAes256);

    public final static byte[] hostChallengeS8 = BytesUtils.hexToBytes("06F85B77251BF794");
    public final static byte[] hostChallengeS16 = BytesUtils.hexToBytes("06F85B77251BF79406F85B77251BF794");
}
