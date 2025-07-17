/*
 * Copyright (C) 2023 Yubico.
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
 *   - YubiKey/YubiKit/Yubico mentions removed from code and comments
 *   - Removed redundant `public` access modifiers
 */

package com.samsung.openscp;

import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;

@SuppressWarnings({"unused", "deprecation"})
final class Logger {

    @Nullable
    private static LoggerCore instance = null;

    static void setLogger(@Nullable LoggerCore logger) {
        instance = logger;
    }

    static void trace(org.slf4j.Logger logger, String message) {
        log(Level.TRACE, logger, message);
    }

    static void trace(org.slf4j.Logger logger, String format, Object arg) {
        log(Level.TRACE, logger, format, arg);
    }

    static void trace(org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        log(Level.TRACE, logger, format, arg1, arg2);
    }

    static void trace(org.slf4j.Logger logger, String format, Object... args) {
        log(Level.TRACE, logger, format, args);
    }

    static void debug(org.slf4j.Logger logger, String message) {
        log(Level.DEBUG, logger, message);
    }

    static void debug(org.slf4j.Logger logger, String format, Object arg) {
        log(Level.DEBUG, logger, format, arg);
    }

    static void debug(org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        log(Level.DEBUG, logger, format, arg1, arg2);
    }

    static void debug(org.slf4j.Logger logger, String format, Object... args) {
        log(Level.DEBUG, logger, format, args);
    }

    static void info(org.slf4j.Logger logger, String message) {
        log(Level.INFO, logger, message);
    }

    static void info(org.slf4j.Logger logger, String format, Object arg) {
        log(Level.INFO, logger, format, arg);
    }

    static void info(org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        log(Level.INFO, logger, format, arg1, arg2);
    }

    static void info(org.slf4j.Logger logger, String format, Object... args) {
        log(Level.INFO, logger, format, args);
    }

    static void warn(org.slf4j.Logger logger, String message) {
        log(Level.WARN, logger, message);
    }

    static void warn(org.slf4j.Logger logger, String format, Object arg) {
        log(Level.WARN, logger, format, arg);
    }

    static void warn(org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        log(Level.WARN, logger, format, arg1, arg2);
    }

    static void warn(org.slf4j.Logger logger, String format, Object... args) {
        log(Level.WARN, logger, format, args);
    }

    static void error(org.slf4j.Logger logger, String message) {
        log(Level.ERROR, logger, message);
    }

    static void error(org.slf4j.Logger logger, String format, Object arg) {
        Logger.log(Level.ERROR, logger, format, arg);
    }

    static void error(org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        Logger.log(Level.ERROR, logger, format, arg1, arg2);
    }

    static void error(org.slf4j.Logger logger, String format, Object... args) {
        Logger.log(Level.ERROR, logger, format, args);
    }

    private static void log(Level level, org.slf4j.Logger logger, String message) {
        if (instance != null) {
            if (Level.ERROR == level) {
                LoggerCore.e(message, new Exception("Throwable missing in logger.error"));
            } else {
                LoggerCore.d(message);
            }
        } else {
            switch (level) {
                case TRACE:
                    logger.trace(message);
                    break;
                case DEBUG:
                    logger.debug(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
            }
        }
    }

    private static void log(Level level, org.slf4j.Logger logger, String format, Object arg) {
        if (instance != null) {
            logToInstance(level, MessageFormatter.format(format, arg));
        } else {
            switch (level) {
                case TRACE:
                    logger.trace(format, arg);
                    break;
                case DEBUG:
                    logger.debug(format, arg);
                    break;
                case INFO:
                    logger.info(format, arg);
                    break;
                case WARN:
                    logger.warn(format, arg);
                    break;
                case ERROR:
                    logger.error(format, arg);
                    break;
            }
        }
    }

    private static void log(Level level, org.slf4j.Logger logger, String format, Object arg1, Object arg2) {
        if (instance != null) {
            logToInstance(level, MessageFormatter.format(format, arg1, arg2));
        } else {
            switch (level) {
                case TRACE:
                    logger.trace(format, arg1, arg2);
                    break;
                case DEBUG:
                    logger.debug(format, arg1, arg2);
                    break;
                case INFO:
                    logger.info(format, arg1, arg2);
                    break;
                case WARN:
                    logger.warn(format, arg1, arg2);
                    break;
                case ERROR:
                    logger.error(format, arg1, arg2);
                    break;
            }
        }
    }

    private static void log(Level level, org.slf4j.Logger logger, String format, Object... args) {
        if (instance != null) {
            logToInstance(level, MessageFormatter.arrayFormat(format, args));
        } else {
            switch (level) {
                case TRACE:
                    logger.trace(format, args);
                    break;
                case DEBUG:
                    logger.debug(format, args);
                    break;
                case INFO:
                    logger.info(format, args);
                    break;
                case WARN:
                    logger.warn(format, args);
                    break;
                case ERROR:
                    logger.error(format, args);
                    break;
            }
        }
    }

    private static void logToInstance(Level level, FormattingTuple formattingTuple) {
        if (instance != null) {

            Throwable throwable = formattingTuple.getThrowable();
            String message = formattingTuple.getMessage();

            if (Level.ERROR == level) {
                if (throwable != null) {
                    LoggerCore.e(message, throwable);
                } else {
                    LoggerCore.e(message, new Throwable("Throwable missing in logger.error"));
                }
            } else {
                if (throwable != null) {
                    LoggerCore.d(message + " Throwable: " + throwable.getMessage());
                } else {
                    LoggerCore.d(message);
                }
            }
        }
    }


}
