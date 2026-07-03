package com.example.filestore.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

/**
 * Manages per-file ReadWriteLocks for concurrent access control.
 */
@Component
public class FileLockManager {

    /**
     * Map of file IDs to their associated ReadWriteLocks.
     */
    private final ConcurrentHashMap<UUID, ReadWriteLock> locks = new ConcurrentHashMap<>();

    /**
     * Gets or creates a ReadWriteLock for the given file ID.
     *
     * @param fileId the file identifier.
     * @return the ReadWriteLock associated with this file.
     */
    public ReadWriteLock getLock(UUID fileId) {
        return locks.computeIfAbsent(fileId, k -> new ReentrantReadWriteLock());
    }
}
