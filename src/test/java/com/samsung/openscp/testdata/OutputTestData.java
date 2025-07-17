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


public class OutputTestData {
    public static final byte[] LIST_PACKAGES_RSP_PLAIN_DATA = BytesUtils.hexToBytes(
        "08A00000015141434C010010A00000022020030101010000000000060100" +
        "10A0000002202003010101000000000011010005A0000002480100");
    public static final byte[] LIST_APPLETS_RSP_PLAIN_DATA = BytesUtils.hexToBytes(
        "0AA9A8A7A6A5A4A3A2A1A00F800AA0A1A2A3A4A5A6A7A8A9070009A00000015141434C00070010A000" +
        "0002202003010301000000000011070007A00000024804000700");
    public static final byte[] LIST_ISSUER_DOMAIN_RSP_PLAIN_DATA = BytesUtils.hexToBytes("08A0000001510000000F9E");
}
