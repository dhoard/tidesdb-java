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
 *     https://www.mozilla.org.org/en-US/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tidesdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pure-Java POJOs, enums, builders, and exception classes.
 * Covers branches and constructors not exercised by the integration tests
 * in {@link TidesDBTest}.
 */
class PojoAndEnumTest {

    // -----------------------------------------------------------------------
    // TidesDBException
    // -----------------------------------------------------------------------

    @Test
    void tidesDBException_messageConstructor_defaultsToUnknownErrorCode() {
        TidesDBException ex = new TidesDBException("boom");
        assertEquals("boom", ex.getMessage());
        assertEquals(TidesDBException.ERR_UNKNOWN, ex.getErrorCode());
    }

    @Test
    void tidesDBException_messageAndCodeConstructor() {
        TidesDBException ex = new TidesDBException("not found", TidesDBException.ERR_NOT_FOUND);
        assertEquals("not found", ex.getMessage());
        assertEquals(TidesDBException.ERR_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void tidesDBException_messageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        TidesDBException ex = new TidesDBException("wrapper", cause);
        assertEquals("wrapper", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals(TidesDBException.ERR_UNKNOWN, ex.getErrorCode());
    }

    @Test
    void tidesDBException_messageCodeAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        TidesDBException ex = new TidesDBException("io err", TidesDBException.ERR_IO, cause);
        assertEquals("io err", ex.getMessage());
        assertEquals(TidesDBException.ERR_IO, ex.getErrorCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    void tidesDBException_getErrorMessage_coversAllKnownCodes() {
        assertErrorMessage(TidesDBException.ERR_SUCCESS, "success");
        assertErrorMessage(TidesDBException.ERR_MEMORY, "memory allocation failed");
        assertErrorMessage(TidesDBException.ERR_INVALID_ARGS, "invalid arguments");
        assertErrorMessage(TidesDBException.ERR_NOT_FOUND, "not found");
        assertErrorMessage(TidesDBException.ERR_IO, "I/O error");
        assertErrorMessage(TidesDBException.ERR_CORRUPTION, "data corruption");
        assertErrorMessage(TidesDBException.ERR_EXISTS, "already exists");
        assertErrorMessage(TidesDBException.ERR_CONFLICT, "transaction conflict");
        assertErrorMessage(TidesDBException.ERR_TOO_LARGE, "key or value too large");
        assertErrorMessage(TidesDBException.ERR_MEMORY_LIMIT, "memory limit exceeded");
        assertErrorMessage(TidesDBException.ERR_INVALID_DB, "invalid database handle");
        assertErrorMessage(TidesDBException.ERR_LOCKED, "database is locked");
    }

    @Test
    void tidesDBException_getErrorMessage_unknownCodeReturnsUnknownError() {
        TidesDBException ex = new TidesDBException("msg", 9999);
        assertEquals("unknown error", ex.getErrorMessage());
    }

    @Test
    void tidesDBException_errorCodeConstants_areDistinct() {
        int[] codes = {
            TidesDBException.ERR_SUCCESS,
            TidesDBException.ERR_MEMORY,
            TidesDBException.ERR_INVALID_ARGS,
            TidesDBException.ERR_NOT_FOUND,
            TidesDBException.ERR_IO,
            TidesDBException.ERR_CORRUPTION,
            TidesDBException.ERR_EXISTS,
            TidesDBException.ERR_CONFLICT,
            TidesDBException.ERR_TOO_LARGE,
            TidesDBException.ERR_MEMORY_LIMIT,
            TidesDBException.ERR_INVALID_DB,
            TidesDBException.ERR_UNKNOWN,
            TidesDBException.ERR_LOCKED,
        };
        assertThat(codes).doesNotHaveDuplicates();
    }

    private void assertErrorMessage(int code, String expectedMessage) {
        TidesDBException ex = new TidesDBException("irrelevant", code);
        assertEquals(expectedMessage, ex.getErrorMessage(),
            "Error code " + code + " should map to '" + expectedMessage + "'");
    }

    // -----------------------------------------------------------------------
    // KeyValue
    // -----------------------------------------------------------------------

    @Test
    void keyValue_constructorAndGetters() {
        byte[] k = {1, 2, 3};
        byte[] v = {4, 5};
        KeyValue kv = new KeyValue(k, v);
        assertSame(k, kv.getKey());
        assertSame(v, kv.getValue());
    }

    @Test
    void keyValue_nullKeyAndValue() {
        KeyValue kv = new KeyValue(null, null);
        assertNull(kv.getKey());
        assertNull(kv.getValue());
    }

    // -----------------------------------------------------------------------
    // CommitOp
    // -----------------------------------------------------------------------

    @Test
    void commitOp_putOperation() {
        byte[] key = {10};
        byte[] val = {20};
        CommitOp op = new CommitOp(key, val, 3600L, false);
        assertSame(key, op.getKey());
        assertSame(val, op.getValue());
        assertEquals(3600L, op.getTtl());
        assertFalse(op.isDelete());
    }

    @Test
    void commitOp_deleteOperation() {
        byte[] key = {10};
        CommitOp op = new CommitOp(key, null, -1, true);
        assertSame(key, op.getKey());
        assertNull(op.getValue());
        assertEquals(-1, op.getTtl());
        assertTrue(op.isDelete());
    }

    // -----------------------------------------------------------------------
    // CacheStats
    // -----------------------------------------------------------------------

    @Test
    void cacheStats_constructorAndGetters() {
        CacheStats cs = new CacheStats(true, 100, 4096, 80, 20, 0.8, 4);
        assertTrue(cs.isEnabled());
        assertEquals(100, cs.getTotalEntries());
        assertEquals(4096, cs.getTotalBytes());
        assertEquals(80, cs.getHits());
        assertEquals(20, cs.getMisses());
        assertEquals(0.8, cs.getHitRate(), 1e-9);
        assertEquals(4, cs.getNumPartitions());
    }

    @Test
    void cacheStats_toString() {
        CacheStats cs = new CacheStats(false, 0, 0, 0, 0, 0.0, 1);
        String str = cs.toString();
        assertThat(str).contains("enabled=false");
        assertThat(str).contains("totalEntries=0");
        assertThat(str).contains("totalBytes=0");
        assertThat(str).contains("hits=0");
        assertThat(str).contains("misses=0");
        assertThat(str).contains("hitRate=0.0");
        assertThat(str).contains("numPartitions=1");
    }

    // -----------------------------------------------------------------------
    // DbStats
    // -----------------------------------------------------------------------

    @Test
    void dbStats_constructorAndGetters() {
        DbStats stats = new DbStats(
            5, 1000000, 500000, 2000000, 1, 3,
            8000, 2, 10, 50000, 8, 42,
            100, 0, 0,
            false, 0, 0, false, 0, 0,
            false, null, 0, 0, 0, 0, 0, 0, 0,
            false, 0, 0,
            0, 5000, 3000, 4000, 2000, 6000, 10, 20);

        assertEquals(5, stats.getNumColumnFamilies());
        assertEquals(1000000, stats.getTotalMemory());
        assertEquals(500000, stats.getAvailableMemory());
        assertEquals(2000000, stats.getResolvedMemoryLimit());
        assertEquals(1, stats.getMemoryPressureLevel());
        assertEquals(3, stats.getFlushPendingCount());
        assertEquals(8000, stats.getTotalMemtableBytes());
        assertEquals(2, stats.getTotalImmutableCount());
        assertEquals(10, stats.getTotalSstableCount());
        assertEquals(50000, stats.getTotalDataSizeBytes());
        assertEquals(8, stats.getNumOpenSstables());
        assertEquals(42, stats.getGlobalSeq());
        assertEquals(100, stats.getTxnMemoryBytes());
        assertEquals(0, stats.getCompactionQueueSize());
        assertEquals(0, stats.getFlushQueueSize());
        assertFalse(stats.isUnifiedMemtableEnabled());
        assertEquals(0, stats.getUnifiedMemtableBytes());
        assertEquals(0, stats.getUnifiedImmutableCount());
        assertFalse(stats.isUnifiedIsFlushing());
        assertEquals(0, stats.getUnifiedNextCfIndex());
        assertEquals(0, stats.getUnifiedWalGeneration());
        assertFalse(stats.isObjectStoreEnabled());
        assertNull(stats.getObjectStoreConnector());
        assertEquals(0, stats.getLocalCacheBytesUsed());
        assertEquals(0, stats.getLocalCacheBytesMax());
        assertEquals(0, stats.getLocalCacheNumFiles());
        assertEquals(0, stats.getLastUploadedGeneration());
        assertEquals(0, stats.getUploadQueueDepth());
        assertEquals(0, stats.getTotalUploads());
        assertEquals(0, stats.getTotalUploadFailures());
        assertFalse(stats.isReplicaMode());
        assertEquals(0, stats.getPrimaryEpoch());
        assertEquals(0, stats.getSeenEpoch());
        assertEquals(0, stats.getUwalBytesWritten());
        assertEquals(5000, stats.getWalBytesWritten());
        assertEquals(3000, stats.getFlushBytesWritten());
        assertEquals(4000, stats.getCompactionBytesWritten());
        assertEquals(2000, stats.getCompactionBytesRead());
        assertEquals(6000, stats.getUserBytesWritten());
        assertEquals(10, stats.getFlushCount());
        assertEquals(20, stats.getCompactionCount());
    }

    @Test
    void dbStats_toString() {
        DbStats stats = new DbStats(
            2, 100, 50, 200, 0, 0,
            30, 0, 5, 100, 3, 1,
            0, 0, 0,
            false, 0, 0, false, 0, 0,
            false, null, 0, 0, 0, 0, 0, 0, 0,
            false, 0, 0,
            0, 100, 200, 300, 400, 500, 1, 2);

        String str = stats.toString();
        assertThat(str).contains("numColumnFamilies=2");
        assertThat(str).contains("totalMemory=100");
        assertThat(str).contains("replicaMode=false");
    }

    // -----------------------------------------------------------------------
    // CompressionAlgorithm enum
    // -----------------------------------------------------------------------

    @Test
    void compressionAlgorithm_values() {
        assertEquals(0, CompressionAlgorithm.NO_COMPRESSION.getValue());
        assertEquals(1, CompressionAlgorithm.SNAPPY_COMPRESSION.getValue());
        assertEquals(2, CompressionAlgorithm.LZ4_COMPRESSION.getValue());
        assertEquals(3, CompressionAlgorithm.ZSTD_COMPRESSION.getValue());
        assertEquals(4, CompressionAlgorithm.LZ4_FAST_COMPRESSION.getValue());
    }

    @Test
    void compressionAlgorithm_fromValue_roundTripsAllValues() {
        for (CompressionAlgorithm algo : CompressionAlgorithm.values()) {
            assertEquals(algo, CompressionAlgorithm.fromValue(algo.getValue()),
                "fromValue should round-trip for " + algo.name());
        }
    }

    @Test
    void compressionAlgorithm_fromValue_invalidValueThrows() {
        assertThatThrownBy(() -> CompressionAlgorithm.fromValue(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // IsolationLevel enum
    // -----------------------------------------------------------------------

    @Test
    void isolationLevel_values() {
        assertEquals(0, IsolationLevel.READ_UNCOMMITTED.getValue());
        assertEquals(1, IsolationLevel.READ_COMMITTED.getValue());
        assertEquals(2, IsolationLevel.REPEATABLE_READ.getValue());
        assertEquals(3, IsolationLevel.SNAPSHOT.getValue());
        assertEquals(4, IsolationLevel.SERIALIZABLE.getValue());
    }

    @Test
    void isolationLevel_fromValue_roundTripsAllValues() {
        for (IsolationLevel level : IsolationLevel.values()) {
            assertEquals(level, IsolationLevel.fromValue(level.getValue()),
                "fromValue should round-trip for " + level.name());
        }
    }

    @Test
    void isolationLevel_fromValue_invalidValueThrows() {
        assertThatThrownBy(() -> IsolationLevel.fromValue(-99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("-99");
    }

    // -----------------------------------------------------------------------
    // LogLevel enum
    // -----------------------------------------------------------------------

    @Test
    void logLevel_values() {
        assertEquals(0, LogLevel.DEBUG.getValue());
        assertEquals(1, LogLevel.INFO.getValue());
        assertEquals(2, LogLevel.WARN.getValue());
        assertEquals(3, LogLevel.ERROR.getValue());
        assertEquals(4, LogLevel.FATAL.getValue());
        assertEquals(99, LogLevel.NONE.getValue());
    }

    @Test
    void logLevel_fromValue_roundTripsAllValues() {
        for (LogLevel level : LogLevel.values()) {
            assertEquals(level, LogLevel.fromValue(level.getValue()),
                "fromValue should round-trip for " + level.name());
        }
    }

    @Test
    void logLevel_fromValue_invalidValueThrows() {
        assertThatThrownBy(() -> LogLevel.fromValue(42))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("42");
    }

    // -----------------------------------------------------------------------
    // SyncMode enum
    // -----------------------------------------------------------------------

    @Test
    void syncMode_values() {
        assertEquals(0, SyncMode.SYNC_NONE.getValue());
        assertEquals(1, SyncMode.SYNC_FULL.getValue());
        assertEquals(2, SyncMode.SYNC_INTERVAL.getValue());
    }

    @Test
    void syncMode_fromValue_roundTripsAllValues() {
        for (SyncMode mode : SyncMode.values()) {
            assertEquals(mode, SyncMode.fromValue(mode.getValue()),
                "fromValue should round-trip for " + mode.name());
        }
    }

    @Test
    void syncMode_fromValue_invalidValueThrows() {
        assertThatThrownBy(() -> SyncMode.fromValue(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("-1");
    }

    // -----------------------------------------------------------------------
    // Config.Builder validation
    // -----------------------------------------------------------------------

    @Test
    void configBuilder_nullDbPathThrows() {
        assertThatThrownBy(() -> Config.builder(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Database path");
    }

    @Test
    void configBuilder_numFlushThreadsZeroOrNegativeThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).numFlushThreads(0).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("flush threads");
        assertThatThrownBy(() -> Config.builder(dir.toString()).numFlushThreads(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("flush threads");
    }

    @Test
    void configBuilder_numCompactionThreadsZeroOrNegativeThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).numCompactionThreads(0).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("compaction threads");
        assertThatThrownBy(() -> Config.builder(dir.toString()).numCompactionThreads(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("compaction threads");
    }

    @Test
    void configBuilder_nullLogLevelThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).logLevel(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Log level");
    }

    @Test
    void configBuilder_negativeBlockCacheSizeThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).blockCacheSize(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Block cache size");
    }

    @Test
    void configBuilder_zeroOrNegativeMaxOpenSSTablesThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).maxOpenSSTables(0).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max open SSTables");
        assertThatThrownBy(() -> Config.builder(dir.toString()).maxOpenSSTables(-5).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max open SSTables");
    }

    @Test
    void configBuilder_negativeMaxConcurrentFlushesThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).maxConcurrentFlushes(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maxConcurrentFlushes");
    }

    @Test
    void configBuilder_negativeUnifiedMemtableSkipListMaxLevelThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSkipListMaxLevel(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSkipListMaxLevel");
    }

    @Test
    void configBuilder_negativeUnifiedMemtableSyncModeThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSyncMode(-1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSyncMode");
    }

    @Test
    void configBuilder_unifiedMemtableSkipListProbabilityOutOfRangeThrows(@TempDir Path dir) {
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSkipListProbability(-0.1f).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSkipListProbability");
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSkipListProbability(1.1f).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSkipListProbability");
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSkipListProbability(Float.NaN).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSkipListProbability");
        assertThatThrownBy(() -> Config.builder(dir.toString()).unifiedMemtableSkipListProbability(Float.POSITIVE_INFINITY).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unifiedMemtableSkipListProbability");
    }

    @Test
    void configBuilder_validBoundaryValuesAccepted(@TempDir Path dir) {
        assertDoesNotThrow(() -> Config.builder(dir.toString())
            .numFlushThreads(1)
            .numCompactionThreads(1)
            .maxOpenSSTables(1)
            .blockCacheSize(0)
            .logTruncationAt(0)
            .maxMemoryUsage(0)
            .maxConcurrentFlushes(0)
            .unifiedMemtableSkipListProbability(0.0f)
            .build());

        assertDoesNotThrow(() -> Config.builder(dir.toString())
            .unifiedMemtableSkipListProbability(1.0f)
            .build());
    }

    @Test
    void configBuilder_settersPreserveValues(@TempDir Path dir) {
        Config config = Config.builder(dir.toString())
            .numFlushThreads(8)
            .numCompactionThreads(4)
            .logLevel(LogLevel.ERROR)
            .blockCacheSize(1024)
            .maxOpenSSTables(128)
            .logToFile(true)
            .logTruncationAt(1234)
            .maxMemoryUsage(9999)
            .unifiedMemtable(true)
            .unifiedMemtableWriteBufferSize(512)
            .unifiedMemtableSkipListMaxLevel(6)
            .unifiedMemtableSkipListProbability(0.5f)
            .unifiedMemtableSyncMode(1)
            .unifiedMemtableSyncIntervalUs(500)
            .objectStoreFsPath("/some/path")
            .maxConcurrentFlushes(3)
            .finishCompactionsOnClose(true)
            .build();

        assertEquals(8, config.getNumFlushThreads());
        assertEquals(4, config.getNumCompactionThreads());
        assertEquals(LogLevel.ERROR, config.getLogLevel());
        assertEquals(1024, config.getBlockCacheSize());
        assertEquals(128, config.getMaxOpenSSTables());
        assertTrue(config.isLogToFile());
        assertEquals(1234, config.getLogTruncationAt());
        assertEquals(9999, config.getMaxMemoryUsage());
        assertTrue(config.isUnifiedMemtable());
        assertEquals(512, config.getUnifiedMemtableWriteBufferSize());
        assertEquals(6, config.getUnifiedMemtableSkipListMaxLevel());
        assertEquals(0.5f, config.getUnifiedMemtableSkipListProbability(), 1e-6);
        assertEquals(1, config.getUnifiedMemtableSyncMode());
        assertEquals(500, config.getUnifiedMemtableSyncIntervalUs());
        assertEquals("/some/path", config.getObjectStoreFsPath());
        assertEquals(3, config.getMaxConcurrentFlushes());
        assertTrue(config.isFinishCompactionsOnClose());
    }

    // -----------------------------------------------------------------------
    // ColumnFamilyConfig.Builder validation
    // -----------------------------------------------------------------------

    @Test
    void columnFamilyConfigBuilder_settersPreserveValues() {
        ColumnFamilyConfig config = ColumnFamilyConfig.builder()
            .writeBufferSize(64 * 1024)
            .levelSizeRatio(5)
            .minLevels(3)
            .dividingLevelOffset(1)
            .klogValueThreshold(256)
            .compressionAlgorithm(CompressionAlgorithm.ZSTD_COMPRESSION)
            .enableBloomFilter(false)
            .bloomFPR(0.05)
            .enableBlockIndexes(false)
            .indexSampleRatio(4)
            .blockIndexPrefixLen(32)
            .syncMode(SyncMode.SYNC_NONE)
            .syncIntervalUs(5000)
            .comparatorName("my_cmp")
            .skipListMaxLevel(8)
            .skipListProbability(0.5f)
            .defaultIsolationLevel(IsolationLevel.SNAPSHOT)
            .minDiskSpace(1024)
            .l1FileCountTrigger(8)
            .l0QueueStallThreshold(10)
            .tombstoneDensityTrigger(0.3)
            .tombstoneDensityMinEntries(512)
            .useBtree(true)
            .objectLazyCompaction(true)
            .objectPrefetchCompaction(false)
            .build();

        assertEquals(64 * 1024, config.getWriteBufferSize());
        assertEquals(5, config.getLevelSizeRatio());
        assertEquals(3, config.getMinLevels());
        assertEquals(1, config.getDividingLevelOffset());
        assertEquals(256, config.getKlogValueThreshold());
        assertEquals(CompressionAlgorithm.ZSTD_COMPRESSION, config.getCompressionAlgorithm());
        assertFalse(config.isEnableBloomFilter());
        assertEquals(0.05, config.getBloomFPR(), 1e-9);
        assertFalse(config.isEnableBlockIndexes());
        assertEquals(4, config.getIndexSampleRatio());
        assertEquals(32, config.getBlockIndexPrefixLen());
        assertEquals(SyncMode.SYNC_NONE, config.getSyncMode());
        assertEquals(5000, config.getSyncIntervalUs());
        assertEquals("my_cmp", config.getComparatorName());
        assertEquals(8, config.getSkipListMaxLevel());
        assertEquals(0.5f, config.getSkipListProbability(), 1e-6);
        assertEquals(IsolationLevel.SNAPSHOT, config.getDefaultIsolationLevel());
        assertEquals(1024, config.getMinDiskSpace());
        assertEquals(8, config.getL1FileCountTrigger());
        assertEquals(10, config.getL0QueueStallThreshold());
        assertEquals(0.3, config.getTombstoneDensityTrigger(), 1e-9);
        assertEquals(512, config.getTombstoneDensityMinEntries());
        assertTrue(config.isUseBtree());
        assertTrue(config.isObjectLazyCompaction());
        assertFalse(config.isObjectPrefetchCompaction());
    }

    // -----------------------------------------------------------------------
    // ObjectStoreConfig.Builder
    // -----------------------------------------------------------------------

    @Test
    void objectStoreConfigBuilder_settersPreserveValues() {
        ObjectStoreConfig config = ObjectStoreConfig.builder()
            .localCachePath("/cache")
            .localCacheMaxBytes(1024)
            .cacheOnRead(false)
            .cacheOnWrite(false)
            .maxConcurrentUploads(2)
            .maxConcurrentDownloads(16)
            .multipartThreshold(1024 * 1024)
            .multipartPartSize(256 * 1024)
            .syncManifestToObject(false)
            .replicateWal(false)
            .walUploadSync(true)
            .walSyncThresholdBytes(2048)
            .walSyncOnCommit(true)
            .replicaMode(true)
            .replicaSyncIntervalUs(1000)
            .replicaReplayWal(false)
            .build();

        assertEquals("/cache", config.getLocalCachePath());
        assertEquals(1024, config.getLocalCacheMaxBytes());
        assertFalse(config.isCacheOnRead());
        assertFalse(config.isCacheOnWrite());
        assertEquals(2, config.getMaxConcurrentUploads());
        assertEquals(16, config.getMaxConcurrentDownloads());
        assertEquals(1024 * 1024, config.getMultipartThreshold());
        assertEquals(256 * 1024, config.getMultipartPartSize());
        assertFalse(config.isSyncManifestToObject());
        assertFalse(config.isReplicateWal());
        assertTrue(config.isWalUploadSync());
        assertEquals(2048, config.getWalSyncThresholdBytes());
        assertTrue(config.isWalSyncOnCommit());
        assertTrue(config.isReplicaMode());
        assertEquals(1000, config.getReplicaSyncIntervalUs());
        assertFalse(config.isReplicaReplayWal());
    }

    @Test
    void objectStoreConfigBuilder_defaultValues() {
        ObjectStoreConfig config = ObjectStoreConfig.builder().build();
        assertNull(config.getLocalCachePath());
        assertEquals(0, config.getLocalCacheMaxBytes());
        assertTrue(config.isCacheOnRead());
        assertTrue(config.isCacheOnWrite());
        assertEquals(4, config.getMaxConcurrentUploads());
        assertEquals(8, config.getMaxConcurrentDownloads());
        assertEquals(64 * 1024 * 1024, config.getMultipartThreshold());
        assertEquals(8 * 1024 * 1024, config.getMultipartPartSize());
        assertTrue(config.isSyncManifestToObject());
        assertTrue(config.isReplicateWal());
        assertFalse(config.isWalUploadSync());
        assertEquals(1048576, config.getWalSyncThresholdBytes());
        assertFalse(config.isWalSyncOnCommit());
        assertFalse(config.isReplicaMode());
        assertEquals(5000000, config.getReplicaSyncIntervalUs());
        assertTrue(config.isReplicaReplayWal());
    }

    // -----------------------------------------------------------------------
    // S3Config.Builder
    // -----------------------------------------------------------------------

    @Test
    void s3ConfigBuilder_emptyRequiredFieldsThrow() {
        assertThatThrownBy(() -> S3Config.builder().endpoint("").bucket("b").accessKey("ak").secretKey("sk").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("endpoint");
        assertThatThrownBy(() -> S3Config.builder().endpoint("e").bucket("").accessKey("ak").secretKey("sk").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("bucket");
        assertThatThrownBy(() -> S3Config.builder().endpoint("e").bucket("b").accessKey("").secretKey("sk").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("access key");
        assertThatThrownBy(() -> S3Config.builder().endpoint("e").bucket("b").accessKey("ak").secretKey("").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("secret key");
    }

    @Test
    void s3ConfigBuilder_defaultOptionalFields() {
        S3Config config = S3Config.builder()
            .endpoint("s3.example.com")
            .bucket("my-bucket")
            .accessKey("AK")
            .secretKey("SK")
            .build();

        assertNull(config.getPrefix());
        assertNull(config.getRegion());
        assertTrue(config.isUseSsl());
        assertFalse(config.isUsePathStyle());
        assertNull(config.getTlsCaPath());
        assertFalse(config.isTlsInsecureSkipVerify());
        assertEquals(0, config.getMultipartThreshold());
        assertEquals(0, config.getMultipartPartSize());
    }

    @Test
    void s3ConfigBuilder_allFieldsSet() {
        S3Config config = S3Config.builder()
            .endpoint("minio.local:9000")
            .bucket("test-bucket")
            .prefix("prefix/")
            .accessKey("access")
            .secretKey("secret")
            .region("eu-west-1")
            .useSsl(false)
            .usePathStyle(true)
            .tlsCaPath("/etc/ssl/ca.pem")
            .tlsInsecureSkipVerify(true)
            .multipartThreshold(5 * 1024 * 1024)
            .multipartPartSize(1024 * 1024)
            .build();

        assertEquals("minio.local:9000", config.getEndpoint());
        assertEquals("test-bucket", config.getBucket());
        assertEquals("prefix/", config.getPrefix());
        assertEquals("access", config.getAccessKey());
        assertEquals("secret", config.getSecretKey());
        assertEquals("eu-west-1", config.getRegion());
        assertFalse(config.isUseSsl());
        assertTrue(config.isUsePathStyle());
        assertEquals("/etc/ssl/ca.pem", config.getTlsCaPath());
        assertTrue(config.isTlsInsecureSkipVerify());
        assertEquals(5 * 1024 * 1024, config.getMultipartThreshold());
        assertEquals(1024 * 1024, config.getMultipartPartSize());
    }

    // -----------------------------------------------------------------------
    // Stats
    // -----------------------------------------------------------------------

    @Test
    void stats_constructorAndGetters() {
        ColumnFamilyConfig cfConfig = ColumnFamilyConfig.defaultConfig();
        Stats stats = new Stats(
            3, 1024,
            new long[]{100, 200, 300}, new int[]{1, 2, 3},
            cfConfig,
            500, 600,
            10.5, 20.5, new long[]{100, 200, 200},
            1.5, 0.95,
            false, 0, 0, 0.0,
            10, 0.02, new long[]{5, 3, 2},
            0.15, 2,
            1000, 2000, 3000, 4000, 5000, 5, 10);

        assertEquals(3, stats.getNumLevels());
        assertEquals(1024, stats.getMemtableSize());
        assertArrayEquals(new long[]{100, 200, 300}, stats.getLevelSizes());
        assertArrayEquals(new int[]{1, 2, 3}, stats.getLevelNumSSTables());
        assertNotNull(stats.getConfig());
        assertEquals(500, stats.getTotalKeys());
        assertEquals(600, stats.getTotalDataSize());
        assertEquals(10.5, stats.getAvgKeySize(), 1e-9);
        assertEquals(20.5, stats.getAvgValueSize(), 1e-9);
        assertArrayEquals(new long[]{100, 200, 200}, stats.getLevelKeyCounts());
        assertEquals(1.5, stats.getReadAmp(), 1e-9);
        assertEquals(0.95, stats.getHitRate(), 1e-9);
        assertFalse(stats.isUseBtree());
        assertEquals(0, stats.getBtreeTotalNodes());
        assertEquals(0, stats.getBtreeMaxHeight());
        assertEquals(0.0, stats.getBtreeAvgHeight(), 1e-9);
        assertEquals(10, stats.getTotalTombstones());
        assertEquals(0.02, stats.getTombstoneRatio(), 1e-9);
        assertArrayEquals(new long[]{5, 3, 2}, stats.getLevelTombstoneCounts());
        assertEquals(0.15, stats.getMaxSstDensity(), 1e-9);
        assertEquals(2, stats.getMaxSstDensityLevel());
        assertEquals(1000, stats.getWalBytesWritten());
        assertEquals(2000, stats.getFlushBytesWritten());
        assertEquals(3000, stats.getCompactionBytesWritten());
        assertEquals(4000, stats.getCompactionBytesRead());
        assertEquals(5000, stats.getUserBytesWritten());
        assertEquals(5, stats.getFlushCount());
        assertEquals(10, stats.getCompactionCount());
    }

    @Test
    void stats_toString_containsAllFields() {
        Stats stats = new Stats(
            2, 512,
            new long[]{100, 200}, new int[]{1, 2},
            ColumnFamilyConfig.defaultConfig(),
            300, 400,
            8.0, 16.0, new long[]{150, 150},
            1.0, 0.9,
            false, 0, 0, 0.0,
            5, 0.01, new long[]{3, 2},
            0.1, 1,
            500, 1000, 1500, 2000, 2500, 3, 7);

        String str = stats.toString();
        assertThat(str).contains("numLevels=2");
        assertThat(str).contains("memtableSize=512");
        assertThat(str).contains("totalKeys=300");
        assertThat(str).contains("totalDataSize=400");
        assertThat(str).contains("readAmp=");
        assertThat(str).contains("hitRate=");
        assertThat(str).contains("useBtree=false");
        assertThat(str).contains("totalTombstones=5");
        assertThat(str).contains("tombstoneRatio=");
        assertThat(str).contains("levelSizes=[");
        assertThat(str).contains("levelNumSSTables=[");
        assertThat(str).contains("levelKeyCounts=[");
        assertThat(str).contains("levelTombstoneCounts=[");
    }

    @Test
    void stats_toString_nullArraysOmitted() {
        Stats stats = new Stats(
            0, 0, null, null, ColumnFamilyConfig.defaultConfig(),
            0, 0, 0, 0, null, 0, 0,
            false, 0, 0, 0.0,
            0, 0, null, 0, 0,
            0, 0, 0, 0, 0, 0, 0);

        String str = stats.toString();
        assertThat(str).doesNotContain("levelSizes=");
        assertThat(str).doesNotContain("levelNumSSTables=");
        assertThat(str).doesNotContain("levelKeyCounts=");
        assertThat(str).doesNotContain("levelTombstoneCounts=");
    }

    @Test
    void stats_toString_btreeFieldsIncludedWhenEnabled() {
        Stats stats = new Stats(
            1, 0,
            new long[0], new int[0],
            ColumnFamilyConfig.defaultConfig(),
            0, 0,
            0, 0, new long[0],
            0, 0,
            true, 50, 5, 3.5,
            0, 0, new long[0],
            0, 0,
            0, 0, 0, 0, 0, 0, 0);

        String str = stats.toString();
        assertThat(str).contains("useBtree=true");
        assertThat(str).contains("btreeTotalNodes=50");
        assertThat(str).contains("btreeMaxHeight=5");
        assertThat(str).contains("btreeAvgHeight=3.5");
    }
}
