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
 * Transaction isolation level. Each constant maps to an integer used by the
 * JNI bridge.
 */
public enum IsolationLevel {

    /**
     * Reads may see uncommitted changes from other transactions.
     */
    READ_UNCOMMITTED(0),

    /**
     * Reads see only committed data (default).
     */
    READ_COMMITTED(1),

    /**
     * Reads within a transaction see a consistent snapshot from its start.
     */
    REPEATABLE_READ(2),

    /**
     * Reads operate against a named snapshot.
     */
    SNAPSHOT(3),

    /**
     * Full serializable isolation.
     */
    SERIALIZABLE(4);
    
    private final int value;
    
    IsolationLevel(int value) {
        this.value = value;
    }
    
    /**
     * Returns the JNI numeric mapping for this isolation level.
     *
     * @return the integer value passed to the native library
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Returns the {@link IsolationLevel} constant matching the given JNI
     * integer value.
     *
     * @param value the JNI integer value
     * @return the matching constant
     * @throws IllegalArgumentException if {@code value} does not map to any
     *         known constant
     */
    public static IsolationLevel fromValue(int value) {
        for (IsolationLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown isolation level value: " + value);
    }
}
