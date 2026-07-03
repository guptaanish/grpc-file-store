package com.example.filestore.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Tracks active upload sessions using a ConcurrentHashMap.
 */
@Component
public class UploadTracker {

    /**
     * Map of session IDs to active upload sessions.
     */
    private final ConcurrentHashMap<UUID, UploadSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Registers a new upload session.
     *
     * @param session the upload session to register.
     */
    public void register(UploadSession session) {
        activeSessions.put(session.sessionId(), session);
    }

    /**
     * Removes an upload session after completion or failure.
     *
     * @param sessionId the session ID to remove.
     */
    public void remove(UUID sessionId) {
        activeSessions.remove(sessionId);
    }

    /**
     * Returns the number of active upload sessions.
     *
     * @return the count of active sessions.
     */
    public int activeCount() {
        return activeSessions.size();
    }
}
