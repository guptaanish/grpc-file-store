package com.example.filestore.service;

/**
 * Thrown when a storage operation would exceed the configured quota.
 */
public class StorageQuotaExceededException extends RuntimeException {

    /**
     * Creates a quota exceeded exception.
     *
     * @param message the detail message.
     */
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
