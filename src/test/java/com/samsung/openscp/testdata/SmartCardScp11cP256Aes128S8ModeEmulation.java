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


public final class SmartCardScp11cP256Aes128S8ModeEmulation extends SmartCardEmulation {
    static final byte[] GET_DATA_OPERATION_CAPDU_EXPECTED = BytesUtils.hexToBytes(
        "00CABF2106A6048302150300");

    private static final byte[] MUTUAL_AUTHENTICATE_CAPDU_EXPECTED = BytesUtils.hexToBytes(
        "8082031553A60D9002110395013C8001888101105F49410470B0BD7863E90E32DA5401188354D1F41999442FDFDCBA7472B7F1E5" +
        "DBBF8A32F92D9D4F9D55C60D57D39BD6D7973306CEA55F7A86884096651A9CCAC8239C9200");
    private static final byte[] MUTUAL_AUTHENTICATE_RAPDU = BytesUtils.hexToBytes(
        "5F4941044F92A07D168C309959EED99E288381DD192979CD452D8FBE1F163447979207C5E6CD1F4DD11609E2100C033BBD723BE7" +
        "8B71477E64883EB41EC366713E44AF1E86100B9394B90AEC0BC62F1F024E6D748ADD9000");

    private static final byte[] LIST_PACKAGES_CAPDU_EXPECTED = BytesUtils.hexToBytes(
        "84F2200018D2AAA03B2F7B80150EEB1A62CE525FA9B98055B95E2E35A4");
    private static final byte[] LIST_PACKAGES_RAPDU = BytesUtils.hexToBytes(
        "A05ADFADAF85CD3E9FCBBC05597FFA61B6328C799934B35A92AA3ABC3B7596FD6E77F30C5A7D6758A2636A26FF81C1A9643C189D" +
        "834565C153D34A1901B1D706411CE2DC076B51779000");

    private static final byte[][] CAPDUS_EXPECTED = new byte[][] {
            GET_DATA_OPERATION_CAPDU_EXPECTED,
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

    public SmartCardScp11cP256Aes128S8ModeEmulation() {
        super(CAPDUS_EXPECTED, RAPDUS_TO_SEND);
    }
}
