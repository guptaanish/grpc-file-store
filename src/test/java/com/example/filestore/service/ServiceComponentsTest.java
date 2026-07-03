package com.example.filestore.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceComponentsTest {

    @Test
    void fileNotFoundExceptionShouldContainFileId() {
        final var id = UUID.randomUUID();
        final var ex = FileNotFoundException.forFile(id);
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void fileNotFoundExceptionShouldContainVersion() {
        final var id = UUID.randomUUID();
        final var ex = FileNotFoundException.forVersion(id, 3);
        assertTrue(ex.getMessage().contains("3"));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void uploadTrackerShouldTrackSessions() {
        final var tracker = new UploadTracker();
        final var session = UploadSession.create("test.txt");

        assertEquals(0, tracker.activeCount());
        tracker.register(session);
        assertEquals(1, tracker.activeCount());
        tracker.remove(session.sessionId());
        assertEquals(0, tracker.activeCount());
    }

    @Test
    void fileLockManagerShouldReturnSameLockForSameId() {
        final var manager = new FileLockManager();
        final var id = UUID.randomUUID();

        final var lock1 = manager.getLock(id);
        final var lock2 = manager.getLock(id);

        assertSame(lock1, lock2);
    }
}
