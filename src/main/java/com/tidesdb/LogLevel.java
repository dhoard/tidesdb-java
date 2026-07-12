/**
 *
 * Copyright (C) TidesDB
 *
 * Original Author: Alex Gaetano Padula
 *
 * Licensed under the Mozilla Public License, v. 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tidesdb;

/**
 * Logging level for the native TidesDB library. Each constant maps to an
 * integer used by the JNI bridge.
 */
public enum LogLevel {

    /**
     * Verbose debug output.
     */
    DEBUG(0),

    /**
     * Informational messages (default).
     */
    INFO(1),

    /**
     * Warning messages.
     */
    WARN(2),

    /**
     * Error messages.
     */
    ERROR(3),

    /**
     * Fatal messages.
     */
    FATAL(4),

    /**
     * Logging disabled.
     */
    NONE(5);
    
    private final int value;
    
    LogLevel(int value) {
        this.value = value;
    }
    
    /**
     * Returns the JNI numeric mapping for this log level.
     *
     * @return the integer value passed to the native library
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Returns the {@link LogLevel} constant matching the given JNI integer
     * value.
     *
     * @param value the JNI integer value
     * @return the matching constant
     * @throws IllegalArgumentException if {@code value} does not map to any
     *         known constant
     */
    public static LogLevel fromValue(int value) {
        for (LogLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown log level value: " + value);
    }
}
