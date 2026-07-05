package com.example.filestore.service;

import org.springframework.stereotype.Component;

/**
 * Serializes deduplication and reclamation operations keyed by content checksum.
 *
 * <p>Shared between the upload path (which resolves/reuses a storage path for a
 * checksum and inserts a version referencing it) and the reclamation path (which
 * counts references to a storage path and physically deletes it when orphaned).
 * Holding the same stripe across both critical sections prevents a concurrent
 * upload from deduplicating onto a storage path that is being reclaimed.
 */
@Component
public class ChecksumLockManager extends StripedLockManager {

    /**
     * Number of lock stripes for checksum serialization.
     */
    private static final int STRIPES = 64;

    /**
     * Creates the checksum lock manager.
     */
    public ChecksumLockManager() {
        super(STRIPES);
    }
}
