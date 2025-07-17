/*
 * Copyright (C) 2020-2022,2024 Yubico.
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
 *   - Removed redundant `public` access modifiers
 */
package com.samsung.openscp;

/**
 * Contains constants for APDU status codes (SW1, SW2).
 */
final class SW {
    static final short NO_INPUT_DATA = 0x6285;
    static final short VERIFY_FAIL_NO_RETRY = 0x63C0;
    static final short MEMORY_ERROR = 0x6581;
    static final short WRONG_LENGTH = 0x6700;
    static final short SECURITY_CONDITION_NOT_SATISFIED = 0x6982;
    static final short AUTH_METHOD_BLOCKED = 0x6983;
    static final short DATA_INVALID = 0x6984;
    static final short CONDITIONS_NOT_SATISFIED = 0x6985;
    static final short COMMAND_NOT_ALLOWED = 0x6986;
    static final short INCORRECT_PARAMETERS = 0x6A80;
    static final short FILE_NOT_FOUND = 0x6A82;
    static final short NO_SPACE = 0x6A84;
    static final short REFERENCED_DATA_NOT_FOUND = 0x6A88;
    static final short WRONG_PARAMETERS_P1P2 = 0x6B00;
    static final short INVALID_INSTRUCTION = 0x6D00;
    static final short CLASS_NOT_SUPPORTED = 0x6E00;
    static final short COMMAND_ABORTED = 0x6F00;
    static final short OK = (short) 0x9000;

    private SW() {
        throw new IllegalStateException();
    }
}
