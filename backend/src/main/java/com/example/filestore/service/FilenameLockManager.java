package com.example.filestore.service;

import org.springframework.stereotype.Component;

/**
 * Serializes metadata operations keyed by filename.
 *
 * <p>Used to close the race where two concurrent uploads of the same new filename
 * both fail the "does it exist?" check and each insert a duplicate {@link
 * com.example.filestore.entity.FileEntity}.
 */
@Component
public class FilenameLockManager extends StripedLockManager {

    /**
     * Number of lock stripes for filename serialization.
     */
    private static final int STRIPES = 64;

    /**
     * Creates the filename lock manager.
     */
    public FilenameLockManager() {
        super(STRIPES);
    }
}
