package com.example.filestore.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.example.filestore.config.FileStoreProperties;

/**
 * Manages resumable upload sessions with temp file persistence.
 */
@Slf4j
@Component
public class ResumableUploadManager {

    /**
     * Active sessions indexed by session ID.
     */
    private final ConcurrentHashMap<String, ResumableUploadSession> sessions = new ConcurrentHashMap<>();

    /**
     * Directory for temporary upload files.
     */
    private final Path tempDir;

    /**
     * Constructs the manager and creates the temp directory.
     *
     * @param properties the file store properties.
     */
    public ResumableUploadManager(FileStoreProperties properties) {
        this.tempDir = Path.of(properties.storage().local().directory(), ".resumable-tmp");
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create resumable temp directory", e);
        }
    }

    /**
     * Initiates a new resumable upload session.
     *
     * @param filename    the filename.
     * @param contentType the content type.
     * @param totalSize   the expected total file size.
     * @return the created session.
     */
    public ResumableUploadSession initiate(String filename, String contentType, long totalSize) {
        final var sessionId = UUID.randomUUID().toString();
        final var tempFile = tempDir.resolve(sessionId + ".part");
        try {
            Files.createFile(tempFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create temp file for session", e);
        }
        final var session = ResumableUploadSession.create(sessionId, filename, contentType, totalSize, tempFile);
        sessions.put(sessionId, session);
        log.info("Initiated resumable upload session: {} for file: {}", sessionId, filename);
        return session;
    }

    /**
     * Gets a session by ID.
     *
     * @param sessionId the session ID.
     * @return the session if it exists.
     */
    public Optional<ResumableUploadSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Appends data to the session's temp file and updates bytes received.
     *
     * @param sessionId the session ID.
     * @param data      the chunk data.
     * @param length    the number of bytes to write.
     * @return the updated session.
     */
    public ResumableUploadSession appendData(String sessionId, byte[] data, int length) {
        final var session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        try {
            Files.write(session.tempFilePath(), data, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to append data to session file", e);
        }
        final var updated = session.withBytesReceived(session.bytesReceived() + length);
        sessions.put(sessionId, updated);
        return updated;
    }

    /**
     * Completes a session and returns the temp file path for finalization.
     *
     * @param sessionId the session ID.
     * @return the completed session.
     */
    public ResumableUploadSession complete(String sessionId) {
        final var session = sessions.remove(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        log.info("Completed resumable upload session: {} ({} bytes)", sessionId, session.bytesReceived());
        return session;
    }

    /**
     * Removes a session and cleans up the temp file.
     *
     * @param sessionId the session ID to remove.
     */
    public void cancel(String sessionId) {
        final var session = sessions.remove(sessionId);
        if (session != null) {
            try {
                Files.deleteIfExists(session.tempFilePath());
            } catch (IOException e) {
                log.warn("Failed to delete temp file for session {}: {}", sessionId, e.getMessage());
            }
        }
    }
}
