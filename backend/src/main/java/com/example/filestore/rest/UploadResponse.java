package com.example.filestore.rest;

/**
 * Response DTO returned after a successful file upload.
 *
 * @param fileId   the unique file identifier.
 * @param filename the original filename.
 * @param size     the file size in bytes.
 * @param checksum the SHA-256 hex digest.
 * @param version  the version number assigned to this upload.
 */
public record UploadResponse(
        String fileId,
        String filename,
        long size,
        String checksum,
        int version
) {
}
