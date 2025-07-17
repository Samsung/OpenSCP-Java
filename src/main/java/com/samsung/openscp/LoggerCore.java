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
 *   - File renamed: Logger -> LoggerCore
 *   - YubiKey/YubiKit/Yubico mentions removed from code and comments
 *   - Removed redundant `public` access modifiers
 */

package com.samsung.openscp;

import javax.annotation.Nullable;

/**
 * Helper class allows to customize logs within the SDK
 * SDK has only 2 levels of logging: debug information and error
 * If a Logger implementation is not provided the SDK won't produce any logs
 */
@Deprecated
abstract class LoggerCore {

    /**
     * Specifies how debug messages are logged.
     * <p>
     * If this method is not overridden, then debug messages will not be logged.
     *
     * @param message the message can to be logged
     */
    protected void logDebug(String message) {
    }

    /**
     * Specifies how error messages (with exceptions) are logged.
     * <p>
     * If this method is not overridden, then error messages will not be logged.
     *
     * @param message   the message can to be logged
     * @param throwable the exception that can to be logged or counted
     */
    protected void logError(String message, Throwable throwable) {
    }

    @Nullable
    static LoggerCore instance = null;

    /**
     * Set the Logger implementation to use. Override the logDebug and logError methods to produce
     * logs. Call with null to disable logging.
     *
     * @param logger the Logger implementation to use
     */
    static void setLogger(@Nullable LoggerCore logger) {
        instance = logger;
        Logger.setLogger(instance);
    }

    /**
     * Log a debug message.
     */
    static void d(String message) {
        if (instance != null) {
            instance.logDebug(message);
        }
    }

    /**
     * Log an error message, together with an exception.
     */
    static void e(String message, Throwable throwable) {
        if (instance != null) {
            instance.logError(message, throwable);
        }
    }
}
