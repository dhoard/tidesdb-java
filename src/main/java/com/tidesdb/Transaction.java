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
 * Represents a transaction in TidesDB. Transactions provide atomic
 * operations on the database and implement {@link java.io.Closeable} for
 * use with try-with-resources.
 *
 * <p>Close transactions before closing the owning {@link TidesDB} instance.
 * After this transaction is freed, all operations except {@code close()} throw
 * {@link IllegalStateException}.
 *
 * <p>This class is not guaranteed to be thread-safe.
 */
public class Transaction implements Closeable {
    
    static {
        NativeLibrary.load();
    }
    
    private long nativeHandle;
    private boolean freed = false;
    
    Transaction(long nativeHandle) {
        this.nativeHandle = nativeHandle;
    }
    
    /**
     * Adds a key-value pair to the transaction.
     *
     * @param cf the column family; must not be {@code null}
     * @param key the key; must not be {@code null} or empty
     * @param value the value; must not be {@code null}
     * @param ttl expiration as seconds since the Unix epoch, or {@code -1} for
     *            no expiration
     * @throws IllegalArgumentException if {@code cf} is {@code null}, {@code key}
     *         is {@code null} or empty, or {@code value} is {@code null}
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native put fails
     */
    public void put(ColumnFamily cf, byte[] key, byte[] value, long ttl) throws TidesDBException {
        checkNotFreed();
        if (cf == null) {
            throw new IllegalArgumentException("Column family cannot be null");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        nativePut(nativeHandle, cf.getNativeHandle(), key, value, ttl);
    }
    
    /**
     * Adds a key-value pair to the transaction with no expiration.
     *
     * <p>Equivalent to {@code put(cf, key, value, -1)}.
     *
     * @param cf the column family; must not be {@code null}
     * @param key the key; must not be {@code null} or empty
     * @param value the value; must not be {@code null}
     * @throws IllegalArgumentException if {@code cf} is {@code null}, {@code key}
     *         is {@code null} or empty, or {@code value} is {@code null}
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native put fails
     */
    public void put(ColumnFamily cf, byte[] key, byte[] value) throws TidesDBException {
        put(cf, key, value, -1);
    }
    
    /**
     * Retrieves a value from the transaction.
     *
     * @param cf the column family; must not be {@code null}
     * @param key the key; must not be {@code null} or empty
     * @return the value, or {@code null} if not found
     * @throws IllegalArgumentException if {@code cf} is {@code null}, or
     *         {@code key} is {@code null} or empty
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native get fails
     */
    public byte[] get(ColumnFamily cf, byte[] key) throws TidesDBException {
        checkNotFreed();
        if (cf == null) {
            throw new IllegalArgumentException("Column family cannot be null");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        return nativeGet(nativeHandle, cf.getNativeHandle(), key);
    }
    
    /**
     * Removes a key-value pair from the transaction.
     *
     * @param cf the column family; must not be {@code null}
     * @param key the key; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code cf} is {@code null}, or
     *         {@code key} is {@code null} or empty
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native delete fails
     */
    public void delete(ColumnFamily cf, byte[] key) throws TidesDBException {
        checkNotFreed();
        if (cf == null) {
            throw new IllegalArgumentException("Column family cannot be null");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        nativeDelete(nativeHandle, cf.getNativeHandle(), key);
    }
    
    /**
     * Commits the transaction, making all pending operations durable.
     *
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native commit fails
     */
    public void commit() throws TidesDBException {
        checkNotFreed();
        nativeCommit(nativeHandle);
    }
    
    /**
     * Rolls back all operations in the transaction.
     *
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native rollback fails
     */
    public void rollback() throws TidesDBException {
        checkNotFreed();
        nativeRollback(nativeHandle);
    }
    
    /**
     * Creates a named savepoint within the transaction.
     *
     * @param name the savepoint name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native savepoint creation fails
     */
    public void savepoint(String name) throws TidesDBException {
        checkNotFreed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Savepoint name cannot be null or empty");
        }
        nativeSavepoint(nativeHandle, name);
    }
    
    /**
     * Rolls back the transaction to a named savepoint.
     *
     * @param name the savepoint name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native rollback fails
     */
    public void rollbackToSavepoint(String name) throws TidesDBException {
        checkNotFreed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Savepoint name cannot be null or empty");
        }
        nativeRollbackToSavepoint(nativeHandle, name);
    }
    
    /**
     * Releases a named savepoint without rolling back.
     *
     * @param name the savepoint name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native release fails
     */
    public void releaseSavepoint(String name) throws TidesDBException {
        checkNotFreed();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Savepoint name cannot be null or empty");
        }
        nativeReleaseSavepoint(nativeHandle, name);
    }
    
    /**
     * Creates a new iterator for the given column family within this transaction.
     *
     * <p>Close the returned iterator before freeing this transaction.
     *
     * @param cf the column family; must not be {@code null}
     * @return a new iterator
     * @throws IllegalArgumentException if {@code cf} is {@code null}
     * @throws IllegalStateException if this transaction is freed
     * @throws TidesDBException if the native iterator cannot be created
     */
    public TidesDBIterator newIterator(ColumnFamily cf) throws TidesDBException {
        checkNotFreed();
        if (cf == null) {
            throw new IllegalArgumentException("Column family cannot be null");
        }
        long iterHandle = nativeNewIterator(nativeHandle, cf.getNativeHandle());
        return new TidesDBIterator(iterHandle);
    }
    
    /**
     * Frees the transaction and releases all native resources.
     *
     * <p>This method is idempotent; subsequent calls are no-ops. After freeing,
     * all other operations on this instance throw {@link IllegalStateException}.
     */
    public void free() {
        if (!freed && nativeHandle != 0) {
            nativeFree(nativeHandle);
            nativeHandle = 0;
            freed = true;
        }
    }
    
    /**
     * Closes this transaction. Equivalent to {@link #free()}.
     */
    @Override
    public void close() {
        free();
    }
    
    private void checkNotFreed() {
        if (freed) {
            throw new IllegalStateException("Transaction has been freed");
        }
    }
    
    long getNativeHandle() {
        return nativeHandle;
    }
    
    private static native void nativePut(long handle, long cfHandle, byte[] key, byte[] value, long ttl) throws TidesDBException;
    private static native byte[] nativeGet(long handle, long cfHandle, byte[] key) throws TidesDBException;
    private static native void nativeDelete(long handle, long cfHandle, byte[] key) throws TidesDBException;
    private static native void nativeCommit(long handle) throws TidesDBException;
    private static native void nativeRollback(long handle) throws TidesDBException;
    private static native void nativeSavepoint(long handle, String name) throws TidesDBException;
    private static native void nativeRollbackToSavepoint(long handle, String name) throws TidesDBException;
    private static native void nativeReleaseSavepoint(long handle, String name) throws TidesDBException;
    private static native long nativeNewIterator(long handle, long cfHandle) throws TidesDBException;
    private static native void nativeFree(long handle);
}
