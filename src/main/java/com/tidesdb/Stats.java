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
 * Statistics about a column family, including level structure, memtable size,
 * and the active configuration. Created by the native library and returned
 * from {@link ColumnFamily#getStats()}.
 *
 * <p>The arrays returned by {@link #getLevelSizes()} and
 * {@link #getLevelNumSSTables()} are the internal stored references, not
 * defensive copies. Callers must not modify them.
 */
public class Stats {
    
    private final int numLevels;
    private final long memtableSize;
    private final long[] levelSizes;
    private final int[] levelNumSSTables;
    private final ColumnFamilyConfig config;
    
    /**
     * Creates a new {@code Stats} instance. Typically called by the JNI
     * bridge rather than application code.
     *
     * @param numLevels the number of LSM levels
     * @param memtableSize the current memtable size in bytes
     * @param levelSizes the size in bytes for each level (stored by reference)
     * @param levelNumSSTables the SSTable count for each level (stored by reference)
     * @param config the column family configuration
     */
    public Stats(int numLevels, long memtableSize, long[] levelSizes, int[] levelNumSSTables, ColumnFamilyConfig config) {
        this.numLevels = numLevels;
        this.memtableSize = memtableSize;
        this.levelSizes = levelSizes;
        this.levelNumSSTables = levelNumSSTables;
        this.config = config;
    }
    
    /**
     * Returns the number of LSM levels.
     *
     * @return the number of levels
     */
    public int getNumLevels() {
        return numLevels;
    }
    
    /**
     * Returns the current memtable size in bytes.
     *
     * @return the memtable size in bytes
     */
    public long getMemtableSize() {
        return memtableSize;
    }
    
    /**
     * Returns the sizes of each LSM level in bytes.
     *
     * <p>The returned array is the stored internal reference, not a
     * defensive copy. Callers must not modify it.
     *
     * @return the level sizes in bytes (internal reference)
     */
    public long[] getLevelSizes() {
        return levelSizes;
    }
    
    /**
     * Returns the number of SSTables at each LSM level.
     *
     * <p>The returned array is the stored internal reference, not a
     * defensive copy. Callers must not modify it.
     *
     * @return the SSTable counts per level (internal reference)
     */
    public int[] getLevelNumSSTables() {
        return levelNumSSTables;
    }
    
    /**
     * Returns the column family configuration active when these statistics
     * were captured.
     *
     * @return the configuration, never {@code null}
     */
    public ColumnFamilyConfig getConfig() {
        return config;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stats{numLevels=").append(numLevels);
        sb.append(", memtableSize=").append(memtableSize);
        if (levelSizes != null) {
            sb.append(", levelSizes=[");
            for (int i = 0; i < levelSizes.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(levelSizes[i]);
            }
            sb.append("]");
        }
        if (levelNumSSTables != null) {
            sb.append(", levelNumSSTables=[");
            for (int i = 0; i < levelNumSSTables.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(levelNumSSTables[i]);
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
