package com.example.filestore.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an active upload session being tracked.
 *
 * @param sessionId the unique session identifier.
 * @param filename  the filename being uploaded.
 * @param startedAt when the upload started.
 */
public record UploadSession(
        UUID sessionId,
        String filename,
        Instant startedAt
) {

    /**
     * Creates a new upload session for the given filename.
     *
     * @param filename the filename being uploaded.
     * @return a new UploadSession instance.
     */
    public static UploadSession create(String filename) {
        return new UploadSession(UUID.randomUUID(), filename, Instant.now());
    }
}
