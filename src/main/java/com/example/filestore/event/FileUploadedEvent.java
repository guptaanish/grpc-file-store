package com.example.filestore.event;

import java.util.UUID;

/**
 * Event published when a file is successfully uploaded.
 *
 * @param fileId   the file identifier.
 * @param filename the uploaded filename.
 * @param version  the version number created.
 */
public record FileUploadedEvent(UUID fileId, String filename, int version) {
}
