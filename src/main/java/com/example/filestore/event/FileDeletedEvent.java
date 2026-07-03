package com.example.filestore.event;

import java.util.UUID;

/**
 * Event published when a file is deleted.
 *
 * @param fileId   the file identifier.
 * @param filename the deleted filename.
 */
public record FileDeletedEvent(UUID fileId, String filename) {
}
