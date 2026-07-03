package com.example.filestore.service;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Tracks the state of a resumable upload session.
 *
 * @param sessionId     unique session identifier.
 * @param filename      original filename.
 * @param contentType   MIME content type.
 * @param totalSize     expected total file size in bytes.
 * @param bytesReceived number of bytes received so far.
 * @param tempFilePath  path to the temporary file on disk.
 * @param createdAt     when the session was initiated.
 */
public record ResumableUploadSession(
        String sessionId,
        String filename,
        String contentType,
        long totalSize,
        long bytesReceived,
        Path tempFilePath,
        Instant createdAt
) {

    /**
     * Creates a new session with zero bytes received.
     *
     * @param sessionId    the session ID.
     * @param filename     the filename.
     * @param contentType  the content type.
     * @param totalSize    the expected total size.
     * @param tempFilePath the temp file path.
     * @return a new session instance.
     */
    public static ResumableUploadSession create(String sessionId, String filename, String contentType, long totalSize, Path tempFilePath) {
        return new ResumableUploadSession(sessionId, filename, contentType, totalSize, 0, tempFilePath, Instant.now());
    }

    /**
     * Returns a copy with updated bytes received.
     *
     * @param newBytesReceived the updated byte count.
     * @return a new session with the updated count.
     */
    public ResumableUploadSession withBytesReceived(long newBytesReceived) {
        return new ResumableUploadSession(sessionId, filename, contentType, totalSize, newBytesReceived, tempFilePath, createdAt);
    }
}
