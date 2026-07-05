package com.example.filestore.service;

import java.io.InputStream;
import java.util.UUID;

/**
 * Interface for file I/O operations — backend-agnostic.
 */
public interface StorageService {

    /**
     * Stores file data and returns the storage path.
     *
     * @param fileId   the file identifier.
     * @param version  the version number.
     * @param filename the original filename.
     * @param data     the file content stream.
     * @return the storage path where the file was written.
     */
    String store(UUID fileId, int version, String filename, InputStream data);

    /**
     * Loads file data from the given storage path.
     *
     * @param storagePath the path to read from.
     * @return an input stream of the file content.
     */
    InputStream load(String storagePath);

    /**
     * Deletes the file at the given storage path.
     *
     * @param storagePath the path to delete.
     */
    void delete(String storagePath);
}
