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
 * Configuration for a column family. Use {@link #builder()} to construct a
 * configuration with custom values, or {@link #defaultConfig()} to obtain a
 * configuration with default settings.
 *
 * <p>Instances are immutable once built. Unlike {@link Config.Builder}, the
 * {@link Builder#build()} method does not perform Java-side validation of the
 * field values.
 */
public class ColumnFamilyConfig {
    
    private long writeBufferSize;
    private long levelSizeRatio;
    private int minLevels;
    private int dividingLevelOffset;
    private long klogValueThreshold;
    private CompressionAlgorithm compressionAlgorithm;
    private boolean enableBloomFilter;
    private double bloomFPR;
    private boolean enableBlockIndexes;
    private int indexSampleRatio;
    private int blockIndexPrefixLen;
    private SyncMode syncMode;
    private long syncIntervalUs;
    private String comparatorName;
    private int skipListMaxLevel;
    private float skipListProbability;
    private IsolationLevel defaultIsolationLevel;
    private long minDiskSpace;
    private int l1FileCountTrigger;
    private int l0QueueStallThreshold;
    
    private ColumnFamilyConfig(Builder builder) {
        this.writeBufferSize = builder.writeBufferSize;
        this.levelSizeRatio = builder.levelSizeRatio;
        this.minLevels = builder.minLevels;
        this.dividingLevelOffset = builder.dividingLevelOffset;
        this.klogValueThreshold = builder.klogValueThreshold;
        this.compressionAlgorithm = builder.compressionAlgorithm;
        this.enableBloomFilter = builder.enableBloomFilter;
        this.bloomFPR = builder.bloomFPR;
        this.enableBlockIndexes = builder.enableBlockIndexes;
        this.indexSampleRatio = builder.indexSampleRatio;
        this.blockIndexPrefixLen = builder.blockIndexPrefixLen;
        this.syncMode = builder.syncMode;
        this.syncIntervalUs = builder.syncIntervalUs;
        this.comparatorName = builder.comparatorName;
        this.skipListMaxLevel = builder.skipListMaxLevel;
        this.skipListProbability = builder.skipListProbability;
        this.defaultIsolationLevel = builder.defaultIsolationLevel;
        this.minDiskSpace = builder.minDiskSpace;
        this.l1FileCountTrigger = builder.l1FileCountTrigger;
        this.l0QueueStallThreshold = builder.l0QueueStallThreshold;
    }
    
    /**
     * Creates a default column family configuration with the following values:
     * <ul>
     *   <li>{@code writeBufferSize}: 67108864 bytes (64 MiB)</li>
     *   <li>{@code levelSizeRatio}: 10</li>
     *   <li>{@code minLevels}: 4</li>
     *   <li>{@code dividingLevelOffset}: 2</li>
     *   <li>{@code klogValueThreshold}: 1024</li>
     *   <li>{@code compressionAlgorithm}: {@link CompressionAlgorithm#NO_COMPRESSION}</li>
     *   <li>{@code enableBloomFilter}: {@code false}</li>
     *   <li>{@code bloomFPR}: 0.01</li>
     *   <li>{@code enableBlockIndexes}: {@code false}</li>
     *   <li>{@code indexSampleRatio}: 16</li>
     *   <li>{@code blockIndexPrefixLen}: 4</li>
     *   <li>{@code syncMode}: {@link SyncMode#SYNC_NONE}</li>
     *   <li>{@code syncIntervalUs}: 0</li>
     *   <li>{@code comparatorName}: {@code ""} (default comparator)</li>
     *   <li>{@code skipListMaxLevel}: 12</li>
     *   <li>{@code skipListProbability}: 0.25</li>
     *   <li>{@code defaultIsolationLevel}: {@link IsolationLevel#READ_COMMITTED}</li>
     *   <li>{@code minDiskSpace}: 1073741824 bytes (1 GiB)</li>
     *   <li>{@code l1FileCountTrigger}: 4</li>
     *   <li>{@code l0QueueStallThreshold}: 8</li>
     * </ul>
     *
     * @return a new {@code ColumnFamilyConfig} with default values
     */
    public static ColumnFamilyConfig defaultConfig() {
        return new Builder()
            .writeBufferSize(64 * 1024 * 1024)
            .levelSizeRatio(10)
            .minLevels(4)
            .dividingLevelOffset(2)
            .klogValueThreshold(1024)
            .compressionAlgorithm(CompressionAlgorithm.NO_COMPRESSION)
            .enableBloomFilter(false)
            .bloomFPR(0.01)
            .enableBlockIndexes(false)
            .indexSampleRatio(16)
            .blockIndexPrefixLen(4)
            .syncMode(SyncMode.SYNC_NONE)
            .syncIntervalUs(0)
            .comparatorName("")
            .skipListMaxLevel(12)
            .skipListProbability(0.25f)
            .defaultIsolationLevel(IsolationLevel.READ_COMMITTED)
            .minDiskSpace(1024 * 1024 * 1024)
            .l1FileCountTrigger(4)
            .l0QueueStallThreshold(8)
            .build();
    }
    
    /**
     * Creates a new builder with default values.
     *
     * @return a new {@code Builder}
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Returns the write-buffer (memtable) size in bytes.
     *
     * @return the write-buffer size in bytes
     */
    public long getWriteBufferSize() { return writeBufferSize; }

    /**
     * Returns the size ratio between adjacent LSM levels.
     *
     * @return the level size ratio
     */
    public long getLevelSizeRatio() { return levelSizeRatio; }

    /**
     * Returns the minimum number of LSM levels.
     *
     * @return the minimum level count
     */
    public int getMinLevels() { return minLevels; }

    /**
     * Returns the dividing level offset used for tiering decisions.
     *
     * @return the dividing level offset
     */
    public int getDividingLevelOffset() { return dividingLevelOffset; }

    /**
     * Returns the key-log value threshold in bytes. Values at or below this
     * size are stored inline in the key log.
     *
     * @return the key-log value threshold in bytes
     */
    public long getKlogValueThreshold() { return klogValueThreshold; }

    /**
     * Returns the compression algorithm used for SSTables.
     *
     * @return the compression algorithm
     */
    public CompressionAlgorithm getCompressionAlgorithm() { return compressionAlgorithm; }

    /**
     * Returns whether Bloom filters are enabled for this column family.
     *
     * @return {@code true} if Bloom filters are enabled
     */
    public boolean isEnableBloomFilter() { return enableBloomFilter; }

    /**
     * Returns the Bloom filter false-positive rate.
     *
     * @return the false-positive rate
     */
    public double getBloomFPR() { return bloomFPR; }

    /**
     * Returns whether block indexes are enabled for this column family.
     *
     * @return {@code true} if block indexes are enabled
     */
    public boolean isEnableBlockIndexes() { return enableBlockIndexes; }

    /**
     * Returns the index sample ratio for block indexes.
     *
     * @return the index sample ratio
     */
    public int getIndexSampleRatio() { return indexSampleRatio; }

    /**
     * Returns the block index prefix length.
     *
     * @return the block index prefix length
     */
    public int getBlockIndexPrefixLen() { return blockIndexPrefixLen; }

    /**
     * Returns the sync mode for durability control.
     *
     * @return the sync mode
     */
    public SyncMode getSyncMode() { return syncMode; }

    /**
     * Returns the sync interval in microseconds. Only meaningful when
     * {@code syncMode} is {@link SyncMode#SYNC_INTERVAL}.
     *
     * @return the sync interval in microseconds
     */
    public long getSyncIntervalUs() { return syncIntervalUs; }

    /**
     * Returns the custom comparator name. An empty string indicates the
     * default lexicographic comparator.
     *
     * @return the comparator name, or an empty string for the default
     */
    public String getComparatorName() { return comparatorName; }

    /**
     * Returns the maximum level for the skip-list memtable.
     *
     * @return the skip-list maximum level
     */
    public int getSkipListMaxLevel() { return skipListMaxLevel; }

    /**
     * Returns the probability parameter for skip-list level promotion.
     *
     * @return the skip-list probability
     */
    public float getSkipListProbability() { return skipListProbability; }

    /**
     * Returns the default isolation level for transactions on this column
     * family.
     *
     * @return the default isolation level
     */
    public IsolationLevel getDefaultIsolationLevel() { return defaultIsolationLevel; }

    /**
     * Returns the minimum disk space in bytes required before writes are
     * rejected.
     *
     * @return the minimum disk space in bytes
     */
    public long getMinDiskSpace() { return minDiskSpace; }

    /**
     * Returns the L1 file-count trigger for compaction.
     *
     * @return the L1 file count trigger
     */
    public int getL1FileCountTrigger() { return l1FileCountTrigger; }

    /**
     * Returns the L0 queue stall threshold. When the L0 file count reaches
     * this threshold, writes are stalled until compaction reduces the count.
     *
     * @return the L0 queue stall threshold
     */
    public int getL0QueueStallThreshold() { return l0QueueStallThreshold; }
    
    /**
     * Builder for {@link ColumnFamilyConfig}. All fields have sensible
     * defaults matching {@link #defaultConfig()}. Call {@link #build()} to
     * create the immutable configuration.
     *
     * <p>Unlike {@link Config.Builder}, the {@link #build()} method does not
     * perform Java-side validation of field values.
     */
    public static class Builder {
        private long writeBufferSize = 64 * 1024 * 1024;

        /**
         * Creates a new builder with default values.
         */
        public Builder() {
        }
        private long levelSizeRatio = 10;
        private int minLevels = 4;
        private int dividingLevelOffset = 2;
        private long klogValueThreshold = 1024;
        private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.NO_COMPRESSION;
        private boolean enableBloomFilter = false;
        private double bloomFPR = 0.01;
        private boolean enableBlockIndexes = false;
        private int indexSampleRatio = 16;
        private int blockIndexPrefixLen = 4;
        private SyncMode syncMode = SyncMode.SYNC_NONE;
        private long syncIntervalUs = 0;
        private String comparatorName = "";
        private int skipListMaxLevel = 12;
        private float skipListProbability = 0.25f;
        private IsolationLevel defaultIsolationLevel = IsolationLevel.READ_COMMITTED;
        private long minDiskSpace = 1024 * 1024 * 1024;
        private int l1FileCountTrigger = 4;
        private int l0QueueStallThreshold = 8;
        
        /**
         * Sets the write-buffer (memtable) size in bytes.
         *
         * @param writeBufferSize the size in bytes
         * @return this builder
         */
        public Builder writeBufferSize(long writeBufferSize) {
            this.writeBufferSize = writeBufferSize;
            return this;
        }
        
        /**
         * Sets the size ratio between adjacent LSM levels.
         *
         * @param levelSizeRatio the level size ratio
         * @return this builder
         */
        public Builder levelSizeRatio(long levelSizeRatio) {
            this.levelSizeRatio = levelSizeRatio;
            return this;
        }
        
        /**
         * Sets the minimum number of LSM levels.
         *
         * @param minLevels the minimum level count
         * @return this builder
         */
        public Builder minLevels(int minLevels) {
            this.minLevels = minLevels;
            return this;
        }
        
        /**
         * Sets the dividing level offset for tiering decisions.
         *
         * @param dividingLevelOffset the offset
         * @return this builder
         */
        public Builder dividingLevelOffset(int dividingLevelOffset) {
            this.dividingLevelOffset = dividingLevelOffset;
            return this;
        }
        
        /**
         * Sets the key-log value threshold in bytes.
         *
         * @param klogValueThreshold the threshold in bytes
         * @return this builder
         */
        public Builder klogValueThreshold(long klogValueThreshold) {
            this.klogValueThreshold = klogValueThreshold;
            return this;
        }
        
        /**
         * Sets the compression algorithm for SSTables.
         *
         * @param compressionAlgorithm the compression algorithm; must not be
         *         {@code null}
         * @return this builder
         */
        public Builder compressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
            this.compressionAlgorithm = compressionAlgorithm;
            return this;
        }
        
        /**
         * Enables or disables Bloom filters.
         *
         * @param enableBloomFilter {@code true} to enable
         * @return this builder
         */
        public Builder enableBloomFilter(boolean enableBloomFilter) {
            this.enableBloomFilter = enableBloomFilter;
            return this;
        }
        
        /**
         * Sets the Bloom filter false-positive rate.
         *
         * @param bloomFPR the false-positive rate
         * @return this builder
         */
        public Builder bloomFPR(double bloomFPR) {
            this.bloomFPR = bloomFPR;
            return this;
        }
        
        /**
         * Enables or disables block indexes.
         *
         * @param enableBlockIndexes {@code true} to enable
         * @return this builder
         */
        public Builder enableBlockIndexes(boolean enableBlockIndexes) {
            this.enableBlockIndexes = enableBlockIndexes;
            return this;
        }
        
        /**
         * Sets the index sample ratio for block indexes.
         *
         * @param indexSampleRatio the sample ratio
         * @return this builder
         */
        public Builder indexSampleRatio(int indexSampleRatio) {
            this.indexSampleRatio = indexSampleRatio;
            return this;
        }
        
        /**
         * Sets the block index prefix length.
         *
         * @param blockIndexPrefixLen the prefix length
         * @return this builder
         */
        public Builder blockIndexPrefixLen(int blockIndexPrefixLen) {
            this.blockIndexPrefixLen = blockIndexPrefixLen;
            return this;
        }
        
        /**
         * Sets the sync mode for durability control.
         *
         * @param syncMode the sync mode; must not be {@code null}
         * @return this builder
         */
        public Builder syncMode(SyncMode syncMode) {
            this.syncMode = syncMode;
            return this;
        }
        
        /**
         * Sets the sync interval in microseconds. Only meaningful when
         * {@code syncMode} is {@link SyncMode#SYNC_INTERVAL}.
         *
         * @param syncIntervalUs the interval in microseconds
         * @return this builder
         */
        public Builder syncIntervalUs(long syncIntervalUs) {
            this.syncIntervalUs = syncIntervalUs;
            return this;
        }
        
        /**
         * Sets the custom comparator name. An empty string selects the
         * default lexicographic comparator.
         *
         * @param comparatorName the comparator name
         * @return this builder
         */
        public Builder comparatorName(String comparatorName) {
            this.comparatorName = comparatorName;
            return this;
        }
        
        /**
         * Sets the maximum level for the skip-list memtable.
         *
         * @param skipListMaxLevel the maximum level
         * @return this builder
         */
        public Builder skipListMaxLevel(int skipListMaxLevel) {
            this.skipListMaxLevel = skipListMaxLevel;
            return this;
        }
        
        /**
         * Sets the probability parameter for skip-list level promotion.
         *
         * @param skipListProbability the promotion probability
         * @return this builder
         */
        public Builder skipListProbability(float skipListProbability) {
            this.skipListProbability = skipListProbability;
            return this;
        }
        
        /**
         * Sets the default isolation level for transactions on this column
         * family.
         *
         * @param defaultIsolationLevel the isolation level; must not be
         *         {@code null}
         * @return this builder
         */
        public Builder defaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
            this.defaultIsolationLevel = defaultIsolationLevel;
            return this;
        }
        
        /**
         * Sets the minimum disk space in bytes required before writes are
         * rejected.
         *
         * @param minDiskSpace the minimum disk space in bytes
         * @return this builder
         */
        public Builder minDiskSpace(long minDiskSpace) {
            this.minDiskSpace = minDiskSpace;
            return this;
        }
        
        /**
         * Sets the L1 file-count trigger for compaction.
         *
         * @param l1FileCountTrigger the file count trigger
         * @return this builder
         */
        public Builder l1FileCountTrigger(int l1FileCountTrigger) {
            this.l1FileCountTrigger = l1FileCountTrigger;
            return this;
        }
        
        /**
         * Sets the L0 queue stall threshold. When the L0 file count reaches
         * this threshold, writes are stalled until compaction reduces the count.
         *
         * @param l0QueueStallThreshold the stall threshold
         * @return this builder
         */
        public Builder l0QueueStallThreshold(int l0QueueStallThreshold) {
            this.l0QueueStallThreshold = l0QueueStallThreshold;
            return this;
        }
        
        /**
         * Creates the immutable {@link ColumnFamilyConfig}. No Java-side
         * validation of field values is performed.
         *
         * @return a new {@code ColumnFamilyConfig}
         */
        public ColumnFamilyConfig build() {
            return new ColumnFamilyConfig(this);
        }
    }
}
