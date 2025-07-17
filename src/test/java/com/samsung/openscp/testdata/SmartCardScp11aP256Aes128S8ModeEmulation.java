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


public final class SmartCardScp11aP256Aes128S8ModeEmulation extends SmartCardEmulation {
    private static final byte[] MUTUAL_AUTHENTICATE_CAPDU_EXPECTED = BytesUtils.hexToBytes(
        "8082031153A60D9002110195013C8001888101105F49410470B0BD7863E90E32DA5401188354D1F41999442FDFDCBA7472B7F1E5" +
        "DBBF8A32F92D9D4F9D55C60D57D39BD6D7973306CEA55F7A86884096651A9CCAC8239C9200");
    private static final byte[] MUTUAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes(
        "5F49410492CAFD79D7E39EA433611E9AAD9742D85B3BD06F3D6055D7CDE28FC44D0AB556BC0F94D2100D87727FDFA2B49033C78F" +
        "07A30AA8D5A6A505E59F99FCDFB2526686100B63C42C2D5138936FF5894F10C1234F9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED = BytesUtils.hexToBytes(
        "84F2200018E1EE3A92EF45551E8464A252F997230DC4E84F41BA692405");
    private static final byte[] LIST_PACKAGES_RAPDU = BytesUtils.hexToBytes(
        "DC8D7D92F77BB20F4AEA2C2EB70A7D93C5F0EA0F27D193679C3D1C970BED8F73C394C63A201C8D367A9F0990146D1FF5CEC2" +
        "E5F90F6956EF241D2CC899BA25B964FFFB799F4C42469000");

    private static final byte[][] CAPDUS_EXPECTED = new byte[][] {
            Scp11Nist256TestData.GET_DATA_OPERATION_CAPDU_EXPECTED,
            Scp11Nist256TestData.GET_RESPONSE_OPERATION_CAPDU_EXPECTED,
            Scp11Nist256TestData.GET_RESPONSE_OPERATION_CAPDU_EXPECTED,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_CAPDU_CHUNK_0_EXPECTED,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_CAPDU_CHUNK_1_EXPECTED,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_CAPDU_CHUNK_2_EXPECTED,
            MUTUAL_AUTHENTICATE_CAPDU_EXPECTED,
            LIST_PACKAGES_CAPDU_EXPECTED
    };
    private static final byte[][] RAPDUS_TO_SEND = new byte[][] {
            Scp11Nist256TestData.GET_DATA_OPERATION_RAPDU_CHUNK_0,
            Scp11Nist256TestData.GET_DATA_OPERATION_RAPDU_CHUNK_1,
            Scp11Nist256TestData.GET_DATA_OPERATION_RAPDU_CHUNK_2,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_RAPDU,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_RAPDU,
            Scp11Nist256TestData.PERFORM_SECURITY_OPERATION_RAPDU,
            MUTUAL_AUTHENTICATE_RAPDU,
            LIST_PACKAGES_RAPDU
    };

    public SmartCardScp11aP256Aes128S8ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
