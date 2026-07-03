package com.example.filestore.service;

import java.util.UUID;

/**
 * Thrown when a requested file or version is not found.
 */
public class FileNotFoundException extends RuntimeException {

    /**
     * Creates a file not found exception.
     *
     * @param message the detail message.
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception for a missing file by ID.
     *
     * @param fileId the file ID that was not found.
     * @return a new FileNotFoundException.
     */
    public static FileNotFoundException forFile(UUID fileId) {
        return new FileNotFoundException("File with id %s not found".formatted(fileId));
    }

    /**
     * Creates an exception for a missing file version.
     *
     * @param fileId  the file ID.
     * @param version the version number.
     * @return a new FileNotFoundException.
     */
    public static FileNotFoundException forVersion(UUID fileId, int version) {
        return new FileNotFoundException("Version %d not found for file %s".formatted(version, fileId));
    }
}
