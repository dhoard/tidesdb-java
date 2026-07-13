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
 * Sync mode for durability control. Each constant maps to an integer used by
 * the JNI bridge.
 */
public enum SyncMode {

    /**
     * No synchronous writes; fastest, but uncommitted data may be lost on
     * crash.
     */
    SYNC_NONE(0),

    /**
     * Synchronous writes on every operation; slowest, but most durable.
     */
    SYNC_FULL(1),

    /**
     * Synchronous writes at a configured interval (see
     * {@code syncIntervalUs} in {@link ColumnFamilyConfig}).
     */
    SYNC_INTERVAL(2);
    
    private final int value;
    
    SyncMode(int value) {
        this.value = value;
    }
    
    /**
     * Returns the JNI numeric mapping for this sync mode.
     *
     * @return the integer value passed to the native library
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Returns the {@link SyncMode} constant matching the given JNI integer
     * value.
     *
     * @param value the JNI integer value
     * @return the matching constant
     * @throws IllegalArgumentException if {@code value} does not map to any
     *         known constant
     */
    public static SyncMode fromValue(int value) {
        for (SyncMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown sync mode value: " + value);
    }
}
