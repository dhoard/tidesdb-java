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
 * Configuration for opening a {@link TidesDB} instance. Use {@link #builder(String)}
 * to construct a configuration with custom values, or {@link #defaultConfig()} to
 * obtain a configuration with default settings.
 *
 * <p>Instances are immutable once built. {@link Builder#build()} validates all fields
 * and throws {@link IllegalArgumentException} for invalid values.
 */
public class Config {

    static {
        NativeLibrary.load();
    }

    private String dbPath;
    private int numFlushThreads;
    private int numCompactionThreads;
    private LogLevel logLevel;
    private long blockCacheSize;
    private long maxOpenSSTables;
    private boolean logToFile;
    private long logTruncationAt;
    private long maxMemoryUsage;
    private boolean unifiedMemtable;
    private long unifiedMemtableWriteBufferSize;
    private int unifiedMemtableSkipListMaxLevel;
    private float unifiedMemtableSkipListProbability;
    private int unifiedMemtableSyncMode;
    private long unifiedMemtableSyncIntervalUs;
    private String objectStoreFsPath;
    private ObjectStoreConfig objectStoreConfig;
    private S3Config objectStoreS3Config;
    private int maxConcurrentFlushes;
    private boolean finishCompactionsOnClose;

    private Config(Builder builder) {
        this.dbPath = builder.dbPath;
        this.numFlushThreads = builder.numFlushThreads;
        this.numCompactionThreads = builder.numCompactionThreads;
        this.logLevel = builder.logLevel;
        this.blockCacheSize = builder.blockCacheSize;
        this.maxOpenSSTables = builder.maxOpenSSTables;
        this.logToFile = builder.logToFile;
        this.logTruncationAt = builder.logTruncationAt;
        this.maxMemoryUsage = builder.maxMemoryUsage;
        this.unifiedMemtable = builder.unifiedMemtable;
        this.unifiedMemtableWriteBufferSize = builder.unifiedMemtableWriteBufferSize;
        this.unifiedMemtableSkipListMaxLevel = builder.unifiedMemtableSkipListMaxLevel;
        this.unifiedMemtableSkipListProbability = builder.unifiedMemtableSkipListProbability;
        this.unifiedMemtableSyncMode = builder.unifiedMemtableSyncMode;
        this.unifiedMemtableSyncIntervalUs = builder.unifiedMemtableSyncIntervalUs;
        this.objectStoreFsPath = builder.objectStoreFsPath;
        this.objectStoreConfig = builder.objectStoreConfig;
        this.objectStoreS3Config = builder.objectStoreS3Config;
        this.maxConcurrentFlushes = builder.maxConcurrentFlushes;
        this.finishCompactionsOnClose = builder.finishCompactionsOnClose;
    }

    /**
     * Creates a default configuration with the following values:
     * <ul>
     *   <li>{@code numFlushThreads}: 2</li>
     *   <li>{@code numCompactionThreads}: 2</li>
     *   <li>{@code logLevel}: {@link LogLevel#INFO}</li>
     *   <li>{@code blockCacheSize}: 67108864 bytes (64 MiB)</li>
     *   <li>{@code maxOpenSSTables}: 256</li>
     *   <li>{@code maxConcurrentFlushes}: sourced from the native C library via
     *       {@code tidesdb_default_config()}</li>
     * </ul>
     *
     * @return a new {@code Config} with default values
     */
    public static Config defaultConfig() {
        return new Builder()
            .numFlushThreads(2)
            .numCompactionThreads(2)
            .logLevel(LogLevel.INFO)
            .blockCacheSize(64 * 1024 * 1024)
            .maxOpenSSTables(256)
            .logToFile(false)
            .logTruncationAt(24 * 1024 * 1024)
            .maxMemoryUsage(0)
            .maxConcurrentFlushes(nativeDefaultMaxConcurrentFlushes())
            .build();
    }

    private static native int nativeDefaultMaxConcurrentFlushes();
    
    /**
     * Creates a new builder with the given database path.
     *
     * @param dbPath the database file-system path; must not be {@code null}
     * @return a new {@code Builder}
     */
    public static Builder builder(String dbPath) {
        return new Builder().dbPath(dbPath);
    }
    
    /**
     * Returns the database file-system path.
     *
     * @return the database path
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * Returns the number of flush threads.
     *
     * @return the flush thread count
     */
    public int getNumFlushThreads() {
        return numFlushThreads;
    }

    /**
     * Returns the number of compaction threads.
     *
     * @return the compaction thread count
     */
    public int getNumCompactionThreads() {
        return numCompactionThreads;
    }

    /**
     * Returns the log level.
     *
     * @return the log level
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Returns the block cache size in bytes.
     *
     * @return the block cache size in bytes
     */
    public long getBlockCacheSize() {
        return blockCacheSize;
    }

    /**
     * Returns the maximum number of open SSTables.
     *
     * @return the maximum open SSTable count
     */
    public long getMaxOpenSSTables() {
        return maxOpenSSTables;
    }
    
    public boolean isLogToFile() {
        return logToFile;
    }
    
    public long getLogTruncationAt() {
        return logTruncationAt;
    }
    
    public long getMaxMemoryUsage() {
        return maxMemoryUsage;
    }

    public boolean isUnifiedMemtable() {
        return unifiedMemtable;
    }

    public long getUnifiedMemtableWriteBufferSize() {
        return unifiedMemtableWriteBufferSize;
    }

    public int getUnifiedMemtableSkipListMaxLevel() {
        return unifiedMemtableSkipListMaxLevel;
    }

    public float getUnifiedMemtableSkipListProbability() {
        return unifiedMemtableSkipListProbability;
    }

    public int getUnifiedMemtableSyncMode() {
        return unifiedMemtableSyncMode;
    }

    public long getUnifiedMemtableSyncIntervalUs() {
        return unifiedMemtableSyncIntervalUs;
    }

    public String getObjectStoreFsPath() {
        return objectStoreFsPath;
    }

    public ObjectStoreConfig getObjectStoreConfig() {
        return objectStoreConfig;
    }

    /**
     * Returns the S3-compatible object store connector configuration, or null if the database
     * is not backed by S3.
     *
     * @return the S3 connector config, or null
     */
    public S3Config getObjectStoreS3Config() {
        return objectStoreS3Config;
    }

    public int getMaxConcurrentFlushes() {
        return maxConcurrentFlushes;
    }

    /**
     * Returns the close behavior for in-flight compactions.
     *
     * @return true if {@code close()} waits for in-flight compactions to finish;
     *         false (default) cancels them at their next checkpoint for a fast shutdown
     */
    public boolean isFinishCompactionsOnClose() {
        return finishCompactionsOnClose;
    }

    /**
     * Builder for {@link Config}. All fields have sensible defaults. Call
     * {@link #build()} to create the immutable configuration; {@code build()}
     * validates all fields.
     */
    public static class Builder {
        private String dbPath = "";

        /**
         * Creates a new builder with default values.
         */
        public Builder() {
        }
        private int numFlushThreads = 2;
        private int numCompactionThreads = 2;
        private LogLevel logLevel = LogLevel.INFO;
        private long blockCacheSize = 64 * 1024 * 1024;
        private long maxOpenSSTables = 256;
        private boolean logToFile = false;
        private long logTruncationAt = 24 * 1024 * 1024;
        private long maxMemoryUsage = 0;
        private boolean unifiedMemtable = false;
        private long unifiedMemtableWriteBufferSize = 0;
        private int unifiedMemtableSkipListMaxLevel = 0;
        private float unifiedMemtableSkipListProbability = 0;
        private int unifiedMemtableSyncMode = 0;
        private long unifiedMemtableSyncIntervalUs = 0;
        private String objectStoreFsPath = null;
        private ObjectStoreConfig objectStoreConfig = null;
        private S3Config objectStoreS3Config = null;
        private int maxConcurrentFlushes = 0;
        private boolean finishCompactionsOnClose = false;

        /**
         * Sets the database file-system path.
         *
         * @param dbPath the path; must not be {@code null}
         * @return this builder
         */
        public Builder dbPath(String dbPath) {
            this.dbPath = dbPath;
            return this;
        }
        
        /**
         * Sets the number of flush threads.
         *
         * @param numFlushThreads the thread count; must be positive
         * @return this builder
         */
        public Builder numFlushThreads(int numFlushThreads) {
            this.numFlushThreads = numFlushThreads;
            return this;
        }
        
        /**
         * Sets the number of compaction threads.
         *
         * @param numCompactionThreads the thread count; must be positive
         * @return this builder
         */
        public Builder numCompactionThreads(int numCompactionThreads) {
            this.numCompactionThreads = numCompactionThreads;
            return this;
        }
        
        /**
         * Sets the log level.
         *
         * @param logLevel the log level; must not be {@code null}
         * @return this builder
         */
        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }
        
        /**
         * Sets the block cache size in bytes.
         *
         * @param blockCacheSize the size in bytes; must not be negative
         * @return this builder
         */
        public Builder blockCacheSize(long blockCacheSize) {
            this.blockCacheSize = blockCacheSize;
            return this;
        }
        
        /**
         * Sets the maximum number of open SSTables.
         *
         * @param maxOpenSSTables the maximum count; must be positive
         * @return this builder
         */
        public Builder maxOpenSSTables(long maxOpenSSTables) {
            this.maxOpenSSTables = maxOpenSSTables;
            return this;
        }
        
        public Builder logToFile(boolean logToFile) {
            this.logToFile = logToFile;
            return this;
        }
        
        public Builder logTruncationAt(long logTruncationAt) {
            this.logTruncationAt = logTruncationAt;
            return this;
        }
        
        public Builder maxMemoryUsage(long maxMemoryUsage) {
            this.maxMemoryUsage = maxMemoryUsage;
            return this;
        }

        public Builder unifiedMemtable(boolean unifiedMemtable) {
            this.unifiedMemtable = unifiedMemtable;
            return this;
        }

        public Builder unifiedMemtableWriteBufferSize(long unifiedMemtableWriteBufferSize) {
            this.unifiedMemtableWriteBufferSize = unifiedMemtableWriteBufferSize;
            return this;
        }

        public Builder unifiedMemtableSkipListMaxLevel(int unifiedMemtableSkipListMaxLevel) {
            this.unifiedMemtableSkipListMaxLevel = unifiedMemtableSkipListMaxLevel;
            return this;
        }

        public Builder unifiedMemtableSkipListProbability(float unifiedMemtableSkipListProbability) {
            this.unifiedMemtableSkipListProbability = unifiedMemtableSkipListProbability;
            return this;
        }

        public Builder unifiedMemtableSyncMode(int unifiedMemtableSyncMode) {
            this.unifiedMemtableSyncMode = unifiedMemtableSyncMode;
            return this;
        }

        public Builder unifiedMemtableSyncIntervalUs(long unifiedMemtableSyncIntervalUs) {
            this.unifiedMemtableSyncIntervalUs = unifiedMemtableSyncIntervalUs;
            return this;
        }

        public Builder objectStoreFsPath(String objectStoreFsPath) {
            this.objectStoreFsPath = objectStoreFsPath;
            return this;
        }

        public Builder objectStoreConfig(ObjectStoreConfig objectStoreConfig) {
            this.objectStoreConfig = objectStoreConfig;
            return this;
        }

        /**
         * Backs the database with an S3-compatible object store connector (AWS S3, MinIO, etc.).
         * Takes precedence over {@link #objectStoreFsPath(String)} when both are set. Pair with
         * {@link #objectStoreConfig(ObjectStoreConfig)} to tune cache, multipart, and replication
         * behavior. Requires the native library to be built with {@code TIDESDB_WITH_S3=ON}.
         *
         * @param objectStoreS3Config the S3 connector configuration, or null for none
         * @return this builder
         */
        public Builder objectStoreS3Config(S3Config objectStoreS3Config) {
            this.objectStoreS3Config = objectStoreS3Config;
            return this;
        }

        public Builder maxConcurrentFlushes(int maxConcurrentFlushes) {
            this.maxConcurrentFlushes = maxConcurrentFlushes;
            return this;
        }

        /**
         * Sets the close behavior for in-flight compactions.
         *
         * @param finishCompactionsOnClose false (default) cancels in-flight compactions at their
         *        next checkpoint for a fast shutdown (no data is lost; recovery handles a mid-merge
         *        state). true lets in-flight compactions run to completion before {@code close()}
         *        returns.
         * @return this builder
         */
        public Builder finishCompactionsOnClose(boolean finishCompactionsOnClose) {
            this.finishCompactionsOnClose = finishCompactionsOnClose;
            return this;
        }

        /**
         * Validates all fields and creates the immutable {@link Config}.
         *
         * @return a new {@code Config}
         * @throws IllegalArgumentException if any field is invalid
         */
        public Config build() {
            validate();
            return new Config(this);
        }

        private void validate() throws IllegalArgumentException {
            if (dbPath == null) {
                throw new IllegalArgumentException("Database path cannot be null");
            }
            if (dbPath.isEmpty()) {
                dbPath = "";
            }
            if (numFlushThreads <= 0) {
                throw new IllegalArgumentException("Number of flush threads must be positive");
            }
            if (numCompactionThreads <= 0) {
                throw new IllegalArgumentException("Number of compaction threads must be positive");
            }
            if (logLevel == null) {
                throw new IllegalArgumentException("Log level cannot be null");
            }
            if (blockCacheSize < 0) {
                throw new IllegalArgumentException("Block cache size cannot be negative");
            }
            if (maxOpenSSTables <= 0) {
                throw new IllegalArgumentException("Max open SSTables must be positive");
            }
        }
    }
}
