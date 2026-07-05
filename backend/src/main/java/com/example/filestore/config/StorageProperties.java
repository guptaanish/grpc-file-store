package com.example.filestore.config;

/**
 * Storage backend configuration properties.
 *
 * @param type  the storage backend type.
 * @param local local filesystem configuration.
 */
public record StorageProperties(
        StorageType type,
        LocalStorageProperties local
) {

    /**
     * Local filesystem storage properties.
     *
     * @param directory the directory for file storage.
     */
    public record LocalStorageProperties(String directory) {
    }
}
