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
 * Represents a column family in TidesDB. A column family is an isolated
 * key-value store within a database, with its own independent configuration.
 *
 * <p>A {@code ColumnFamily} is a handle returned by {@link TidesDB#getColumnFamily(String)}
 * and is not independently closeable. There is no Java-side guard against using
 * a column family after its owning database has been closed; callers must manage
 * the lifecycle externally.
 *
 * <p>This class is not guaranteed to be thread-safe.
 */
public class ColumnFamily {
    
    static {
        NativeLibrary.load();
    }
    
    private final long nativeHandle;
    private final String name;
    
    ColumnFamily(long nativeHandle, String name) {
        this.nativeHandle = nativeHandle;
        this.name = name;
    }
    
    /**
     * Gets the name of this column family.
     *
     * @return the column family name, never {@code null}
     */
    public String getName() {
        return name;
    }
    
    /**
     * Retrieves statistics about this column family.
     *
     * @return column family statistics, never {@code null}
     * @throws TidesDBException if the native stats retrieval fails
     */
    public Stats getStats() throws TidesDBException {
        return nativeGetStats(nativeHandle);
    }
    
    /**
     * Manually triggers compaction for this column family.
     *
     * @throws TidesDBException if the native compaction fails
     */
    public void compact() throws TidesDBException {
        nativeCompact(nativeHandle);
    }
    
    /**
     * Manually triggers a memtable flush for this column family.
     *
     * @throws TidesDBException if the native flush fails
     */
    public void flushMemtable() throws TidesDBException {
        nativeFlushMemtable(nativeHandle);
    }
    
    long getNativeHandle() {
        return nativeHandle;
    }
    
    private static native Stats nativeGetStats(long handle) throws TidesDBException;
    private static native void nativeCompact(long handle) throws TidesDBException;
    private static native void nativeFlushMemtable(long handle) throws TidesDBException;
}
