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

import java.io.Closeable;

/**
 * Main entry point for a TidesDB database. This class wraps the native TidesDB
 * library through JNI and owns the underlying database handle.
 *
 * <p>{@code TidesDB} implements {@link java.io.Closeable} and should be used with
 * try-with-resources. Callers must close any {@link TidesDBIterator} and
 * {@link Transaction} instances before closing the database. Closing an already-closed
 * instance is a no-op.
 *
 * <p>Operations on a closed instance throw {@link IllegalStateException}.
 *
 * <p>This class is not guaranteed to be thread-safe.
 */
public class TidesDB implements Closeable {
    
    static {
        NativeLibrary.load();
    }
    
    private long nativeHandle;
    private boolean closed = false;
    
    private TidesDB(long nativeHandle) {
        this.nativeHandle = nativeHandle;
    }
    
    /**
     * Opens a TidesDB instance with the given configuration.
     *
     * @param config the database configuration; must not be {@code null}
     * @return a new TidesDB instance
     * @throws IllegalArgumentException if {@code config} is {@code null} or its
     *         {@code dbPath} is {@code null} or empty
     * @throws TidesDBException if the native database cannot be opened
     */
    public static TidesDB open(Config config) throws TidesDBException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (config.getDbPath() == null || config.getDbPath().isEmpty()) {
            throw new IllegalArgumentException("Database path cannot be null or empty");
        }
        
        long handle = nativeOpen(
            config.getDbPath(),
            config.getNumFlushThreads(),
            config.getNumCompactionThreads(),
            config.getLogLevel().getValue(),
            config.getBlockCacheSize(),
            config.getMaxOpenSSTables()
        );
        
        return new TidesDB(handle);
    }
    
    /**
     * Closes the database and releases all native resources.
     *
     * <p>This method is idempotent; subsequent calls are no-ops. After closing,
     * all other operations on this instance throw {@link IllegalStateException}.
     *
     * <p>Callers should close all {@link TidesDBIterator} and {@link Transaction}
     * instances before calling this method.
     */
    @Override
    public void close() {
        if (!closed && nativeHandle != 0) {
            nativeClose(nativeHandle);
            nativeHandle = 0;
            closed = true;
        }
    }
    
    /**
     * Creates a new column family with the given configuration.
     *
     * @param name the column family name; must not be {@code null} or empty
     * @param config the column family configuration; must not be {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty,
     *         or {@code config} is {@code null}
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native column family cannot be created
     */
    public void createColumnFamily(String name, ColumnFamilyConfig config) throws TidesDBException {
        checkNotClosed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Column family name cannot be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Column family config cannot be null");
        }
        
        nativeCreateColumnFamily(nativeHandle, name,
            config.getWriteBufferSize(),
            config.getLevelSizeRatio(),
            config.getMinLevels(),
            config.getDividingLevelOffset(),
            config.getKlogValueThreshold(),
            config.getCompressionAlgorithm().getValue(),
            config.isEnableBloomFilter(),
            config.getBloomFPR(),
            config.isEnableBlockIndexes(),
            config.getIndexSampleRatio(),
            config.getBlockIndexPrefixLen(),
            config.getSyncMode().getValue(),
            config.getSyncIntervalUs(),
            config.getComparatorName(),
            config.getSkipListMaxLevel(),
            config.getSkipListProbability(),
            config.getDefaultIsolationLevel().getValue(),
            config.getMinDiskSpace(),
            config.getL1FileCountTrigger(),
            config.getL0QueueStallThreshold()
        );
    }
    
    /**
     * Drops a column family and all associated data.
     *
     * @param name the column family name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native column family cannot be dropped
     */
    public void dropColumnFamily(String name) throws TidesDBException {
        checkNotClosed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Column family name cannot be null or empty");
        }
        nativeDropColumnFamily(nativeHandle, name);
    }
    
    /**
     * Retrieves a column family by name.
     *
     * @param name the column family name; must not be {@code null} or empty
     * @return the column family handle
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the column family is not found or a native
     *         error occurs
     */
    public ColumnFamily getColumnFamily(String name) throws TidesDBException {
        checkNotClosed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Column family name cannot be null or empty");
        }
        long cfHandle = nativeGetColumnFamily(nativeHandle, name);
        return new ColumnFamily(cfHandle, name);
    }
    
    /**
     * Lists all column families in the database.
     *
     * @return array of column family names, never {@code null}
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native list operation fails
     */
    public String[] listColumnFamilies() throws TidesDBException {
        checkNotClosed();
        return nativeListColumnFamilies(nativeHandle);
    }
    
    /**
     * Begins a new transaction with the default isolation level.
     *
     * <p>Close the returned transaction before closing this database.
     *
     * @return a new transaction
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native transaction cannot be started
     */
    public Transaction beginTransaction() throws TidesDBException {
        checkNotClosed();
        long txnHandle = nativeBeginTransaction(nativeHandle);
        return new Transaction(txnHandle);
    }
    
    /**
     * Begins a new transaction with the specified isolation level.
     *
     * <p>Close the returned transaction before closing this database.
     *
     * @param isolationLevel the isolation level; must not be {@code null}
     * @return a new transaction
     * @throws IllegalArgumentException if {@code isolationLevel} is {@code null}
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native transaction cannot be started
     */
    public Transaction beginTransaction(IsolationLevel isolationLevel) throws TidesDBException {
        checkNotClosed();
        if (isolationLevel == null) {
            throw new IllegalArgumentException("Isolation level cannot be null");
        }
        long txnHandle = nativeBeginTransactionWithIsolation(nativeHandle, isolationLevel.getValue());
        return new Transaction(txnHandle);
    }
    
    /**
     * Retrieves statistics about the block cache.
     *
     * @return cache statistics, never {@code null}
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native stats retrieval fails
     */
    public CacheStats getCacheStats() throws TidesDBException {
        checkNotClosed();
        return nativeGetCacheStats(nativeHandle);
    }
    
    /**
     * Registers a custom comparator with the database.
     *
     * @param name the comparator name; must not be {@code null} or empty
     * @param context optional context string, may be {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this database is closed
     * @throws TidesDBException if the native comparator registration fails
     */
    public void registerComparator(String name, String context) throws TidesDBException {
        checkNotClosed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Comparator name cannot be null or empty");
        }
        nativeRegisterComparator(nativeHandle, name, context);
    }
    
    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("TidesDB instance is closed");
        }
    }
    
    long getNativeHandle() {
        return nativeHandle;
    }
    
    private static native long nativeOpen(String dbPath, int numFlushThreads, int numCompactionThreads,
                                          int logLevel, long blockCacheSize, long maxOpenSSTables) throws TidesDBException;
    
    private static native void nativeClose(long handle);
    
    private static native void nativeCreateColumnFamily(long handle, String name,
        long writeBufferSize, long levelSizeRatio, int minLevels, int dividingLevelOffset,
        long klogValueThreshold, int compressionAlgorithm, boolean enableBloomFilter,
        double bloomFPR, boolean enableBlockIndexes, int indexSampleRatio, int blockIndexPrefixLen,
        int syncMode, long syncIntervalUs, String comparatorName, int skipListMaxLevel,
        float skipListProbability, int defaultIsolationLevel, long minDiskSpace,
        int l1FileCountTrigger, int l0QueueStallThreshold) throws TidesDBException;
    
    private static native void nativeDropColumnFamily(long handle, String name) throws TidesDBException;
    
    private static native long nativeGetColumnFamily(long handle, String name) throws TidesDBException;
    
    private static native String[] nativeListColumnFamilies(long handle) throws TidesDBException;
    
    private static native long nativeBeginTransaction(long handle) throws TidesDBException;
    
    private static native long nativeBeginTransactionWithIsolation(long handle, int isolationLevel) throws TidesDBException;
    
    private static native CacheStats nativeGetCacheStats(long handle) throws TidesDBException;
    
    private static native void nativeRegisterComparator(long handle, String name, String context) throws TidesDBException;
}
