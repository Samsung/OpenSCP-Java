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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class to generate random data.
 */
class RandomUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    /**
     * Returns a byte array containing random values.
     */
    @SuppressWarnings("NewApi")
    static byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchMethodError | NoSuchAlgorithmException e) {
            // Fallback for older Android versions
            secureRandom.nextBytes(bytes);
        }
        return bytes;
    }

    private RandomUtils() {
        throw new IllegalStateException();
    }
}
