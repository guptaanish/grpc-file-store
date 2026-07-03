package com.example.filestore.service;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides a fixed pool of {@link ReentrantLock} stripes keyed by a string.
 *
 * <p>Keys are mapped to a stripe by their hash, so distinct keys can proceed in
 * parallel while operations on the same key are serialized. Because the number of
 * stripes is fixed at construction time, this manager uses bounded memory and does
 * not leak locks as new keys are introduced (unlike an unbounded per-key map).
 */
public abstract class StripedLockManager {

    /**
     * Fixed array of lock stripes.
     */
    private final ReentrantLock[] stripes;

    /**
     * Creates a striped lock manager with the given number of stripes.
     *
     * @param stripeCount the number of lock stripes (must be positive).
     */
    protected StripedLockManager(int stripeCount) {
        if (stripeCount <= 0) {
            throw new IllegalArgumentException("stripeCount must be positive");
        }
        this.stripes = new ReentrantLock[stripeCount];
        for (int i = 0; i < stripeCount; i++) {
            this.stripes[i] = new ReentrantLock();
        }
    }

    /**
     * Returns the lock stripe associated with the given key.
     *
     * @param key the key to resolve to a stripe (must not be null).
     * @return the lock guarding all keys that hash to the same stripe.
     */
    public ReentrantLock getLock(String key) {
        final int index = Math.floorMod(key.hashCode(), stripes.length);
        return stripes[index];
    }
}
