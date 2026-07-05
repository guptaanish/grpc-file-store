package com.example.filestore.service;

import java.security.MessageDigest;

/**
 * Interface for computing file checksums.
 */
public interface ChecksumService {

    /**
     * Creates a new digest instance for incremental hashing.
     *
     * @return a fresh MessageDigest.
     */
    MessageDigest createDigest();

    /**
     * Updates the digest with additional data.
     *
     * @param digest the digest to update.
     * @param data   the byte array containing data.
     * @param length the number of bytes to process.
     */
    void update(MessageDigest digest, byte[] data, int length);

    /**
     * Finalizes the digest and returns the hex string.
     *
     * @param digest the digest to finalize.
     * @return the hex-encoded checksum string.
     */
    String finish(MessageDigest digest);
}
