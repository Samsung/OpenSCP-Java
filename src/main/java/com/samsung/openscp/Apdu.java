/*
 * Copyright (C) 2019-2022 Yubico.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -----------------------
 * Modifications copyright:
 *
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
 *
 * Modifications include:
 *   - Package and import statements updated during code move from the original project
 *   - Extend API with "force add Le" parameter
 *   - Add missed JavaDocs
 */

package com.samsung.openscp;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Data model for encapsulating an APDU command, as defined by ISO/IEC 7816-4 standard.
 */
public class Apdu {
    private final byte cla;
    private final byte ins;
    private final byte p1;
    private final byte p2;
    private final byte[] data;
    private final int le;
    private final boolean forceAddLe;

    /**
     * Creates a new command APDU from a list of parameters specified by the ISO/IEC 7816-4 standard.
     *
     * @param cla        the instruction class
     * @param ins        the instruction number
     * @param p1         the first instruction parameter byte
     * @param p2         the second instruction parameter byte
     * @param data       the command data
     * @param le         the length of expected data in the response
     * @param forceAddLe should 0x00 Le be forcibly added to the encoded APDU form or not
     */
    public Apdu(int cla, int ins, int p1, int p2, @Nullable byte[] data, int le, boolean forceAddLe) {
        this(validateByte(cla, "CLA"),
             validateByte(ins, "INS"),
             validateByte(p1, "P1"),
             validateByte(p2, "P2"),
             data,
             le,
             forceAddLe
        );
    }

    /**
     * {@link #Apdu(int, int, int, int, byte[], int, boolean)} wrapper for Le=0 and forceAddLe=false
     */
    public Apdu(int cla, int ins, int p1, int p2, @Nullable byte[] data) {
        this(cla, ins, p1, p2, data, 0 /*Le*/, false /*forceAddLe*/);
    }

    /**
     * {@link #Apdu(int, int, int, int, byte[], int, boolean)} wrapper for forceAddLe=false
     */
    public Apdu(int cla, int ins, int p1, int p2, @Nullable byte[] data, int le) {
        this(cla, ins, p1, p2, data, le, false /*forceAddLe*/);
    }

    private Apdu(byte cla, byte ins, byte p1, byte p2, @Nullable byte[] data, int le, boolean forceAddLe) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.data = data == null ? new byte[0] : data;
        this.le = le;
        this.forceAddLe = forceAddLe;
    }

    private Apdu(byte cla, byte ins, byte p1, byte p2, @Nullable byte[] data) {
        this(cla, ins, p1, p2, data, 0 /*Le*/, false /*forceAddLe*/);
    }

    /**
     * Returns the data payload of the APDU.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the CLA of the APDU.
     */
    public byte getCla() {
        return cla;
    }

    /**
     * Returns the INS of the APDU.
     */
    public byte getIns() {
        return ins;
    }

    /**
     * Returns the parameter P1 of the APDU.
     */
    public byte getP1() {
        return p1;
    }

    /**
     * Returns the parameter P2 of the APDU.
     */
    public byte getP2() {
        return p2;
    }

    /**
     * Returns the Le of the APDU.
     */
    public int getLe() {
        return le;
    }

    /**
     * Returns the flag if 0x00 Le should be added to encoded APDU or not
     */
    public boolean isForceAddLe() {
        return forceAddLe;
    }

    /**
     * Validates that integer passed fits into byte and converts to byte
     */
    private static byte validateByte(int byteInt, String name) {
        if (byteInt > 255 || byteInt < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid value for " + name + ", must fit in a byte");
        }
        return (byte) byteInt;
    }
}
