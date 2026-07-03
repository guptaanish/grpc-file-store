package com.example.filestore.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;

/**
 * Interface for file metadata CRUD operations.
 */
public interface MetadataService {

    /**
     * Creates a new file record or retrieves an existing one by filename.
     *
     * @param filename    the filename.
     * @param contentType the MIME content type.
     * @return the file entity (existing or newly created).
     */
    FileEntity createOrGetFile(String filename, String contentType);

    /**
     * Creates a new version record for the given file.
     *
     * @param file        the parent file entity.
     * @param storagePath the storage path for this version.
     * @param size        the file size in bytes.
     * @param checksum    the SHA-256 checksum.
     * @return the created version entity.
     */
    FileVersionEntity createVersion(FileEntity file, String storagePath, long size, String checksum);

    /**
     * Finds a non-deleted file by ID.
     *
     * @param fileId the file ID.
     * @return an optional containing the file entity if found.
     */
    Optional<FileEntity> findById(UUID fileId);

    /**
     * Finds a specific version of a file.
     *
     * @param fileId  the file ID.
     * @param version the version number (0 for latest).
     * @return an optional containing the version entity.
     */
    Optional<FileVersionEntity> findVersion(UUID fileId, int version);

    /**
     * Gets all versions of a file ordered by version number.
     *
     * @param fileId the file ID.
     * @return list of version entities.
     */
    List<FileVersionEntity> getVersions(UUID fileId);

    /**
     * Searches files by filename substring or lists all if query is blank.
     *
     * @param query    the search query (may be empty).
     * @param pageable pagination parameters.
     * @return a page of matching file entities.
     */
    Page<FileEntity> search(String query, Pageable pageable);

    /**
     * Soft-deletes a file by ID.
     *
     * @param fileId the file ID.
     */
    void softDelete(UUID fileId);

    /**
     * Copies a file version to a new filename (zero-copy, shares storage path).
     *
     * @param sourceFileId      the source file ID.
     * @param sourceVersion     the version to copy (0 for latest).
     * @param destinationFilename the new filename.
     * @return the newly created file entity.
     */
    FileEntity copyFile(UUID sourceFileId, int sourceVersion, String destinationFilename);

    /**
     * Renames a file.
     *
     * @param fileId      the file ID.
     * @param newFilename the new filename.
     * @return the updated file entity.
     */
    FileEntity renameFile(UUID fileId, String newFilename);

    /**
     * Finds an existing storage path for a given checksum (content-addressable deduplication).
     *
     * @param checksum the SHA-256 checksum.
     * @return the storage path if a file with that checksum already exists.
     */
    Optional<String> findStoragePathByChecksum(String checksum);
}
