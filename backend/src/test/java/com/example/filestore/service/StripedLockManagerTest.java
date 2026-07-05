package com.example.filestore.service;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StripedLockManager} and its concrete stripe managers.
 */
class StripedLockManagerTest {

    @Test
    void shouldReturnSameLockInstanceForSameKey() {
        final var manager = new FilenameLockManager();

        final ReentrantLock first = manager.getLock("report.pdf");
        final ReentrantLock second = manager.getLock("report.pdf");

        assertSame(first, second, "same key must map to the same stripe");
    }

    @Test
    void shouldSerializeSameKeyAndBeReentrant() {
        final var manager = new ChecksumLockManager();
        final var lock = manager.getLock("abc123");

        lock.lock();
        try {
            assertTrue(lock.isHeldByCurrentThread());
            // Reentrant acquisition of the same stripe must not deadlock.
            assertSame(lock, manager.getLock("abc123"));
        } finally {
            lock.unlock();
        }
        assertFalse(lock.isLocked());
    }

    @Test
    void shouldRejectNonPositiveStripeCount() {
        assertThrows(IllegalArgumentException.class, () -> new StripedLockManager(0) {
        });
    }
}
