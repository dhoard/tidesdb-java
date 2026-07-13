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
 * Statistics about the block cache in a {@link TidesDB} instance. Created by
 * the native library and returned from {@link TidesDB#getCacheStats()}.
 */
public class CacheStats {
    
    private final boolean enabled;
    private final long totalEntries;
    private final long totalBytes;
    private final long hits;
    private final long misses;
    private final double hitRate;
    private final long numPartitions;
    
    /**
     * Creates a new {@code CacheStats} instance. Typically called by the JNI
     * bridge rather than application code.
     *
     * @param enabled whether the cache is enabled
     * @param totalEntries the total number of cache entries
     * @param totalBytes the total bytes used by the cache
     * @param hits the number of cache hits
     * @param misses the number of cache misses
     * @param hitRate the cache hit rate (0.0 to 1.0)
     * @param numPartitions the number of cache partitions
     */
    public CacheStats(boolean enabled, long totalEntries, long totalBytes, long hits, long misses, double hitRate, long numPartitions) {
        this.enabled = enabled;
        this.totalEntries = totalEntries;
        this.totalBytes = totalBytes;
        this.hits = hits;
        this.misses = misses;
        this.hitRate = hitRate;
        this.numPartitions = numPartitions;
    }
    
    /**
     * Returns whether the block cache is enabled.
     *
     * @return {@code true} if the cache is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Returns the total number of entries currently in the cache.
     *
     * @return the total entry count
     */
    public long getTotalEntries() {
        return totalEntries;
    }
    
    /**
     * Returns the total bytes used by the cache.
     *
     * @return the total bytes
     */
    public long getTotalBytes() {
        return totalBytes;
    }
    
    /**
     * Returns the cumulative number of cache hits.
     *
     * @return the cache hit count
     */
    public long getHits() {
        return hits;
    }
    
    /**
     * Returns the cumulative number of cache misses.
     *
     * @return the cache miss count
     */
    public long getMisses() {
        return misses;
    }
    
    /**
     * Returns the cache hit rate.
     *
     * @return the hit rate, typically in the range 0.0 to 1.0
     */
    public double getHitRate() {
        return hitRate;
    }
    
    /**
     * Returns the number of cache partitions.
     *
     * @return the partition count
     */
    public long getNumPartitions() {
        return numPartitions;
    }
    
    @Override
    public String toString() {
        return "CacheStats{" +
            "enabled=" + enabled +
            ", totalEntries=" + totalEntries +
            ", totalBytes=" + totalBytes +
            ", hits=" + hits +
            ", misses=" + misses +
            ", hitRate=" + hitRate +
            ", numPartitions=" + numPartitions +
            '}';
    }
}
