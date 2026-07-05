package com.example.filestore.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.filestore.entity.FileVersionEntity;

/**
 * Spring Data JPA repository for {@link FileVersionEntity}.
 */
@Repository
public interface FileVersionRepository extends JpaRepository<FileVersionEntity, UUID> {

    /**
     * Finds all versions of a file ordered by version number.
     *
     * @param fileId the file ID.
     * @return list of version entities ordered ascending.
     */
    List<FileVersionEntity> findByFileIdOrderByVersionAsc(UUID fileId);

    /**
     * Finds a specific version of a file.
     *
     * @param fileId  the file ID.
     * @param version the version number.
     * @return an optional containing the version entity if found.
     */
    Optional<FileVersionEntity> findByFileIdAndVersion(UUID fileId, int version);

    /**
     * Finds the latest version of a file.
     *
     * @param fileId the file ID.
     * @return an optional containing the latest version entity.
     */
    Optional<FileVersionEntity> findTopByFileIdOrderByVersionDesc(UUID fileId);

    /**
     * Finds storage paths of versions with the given checksum that belong to
     * non-deleted files (for content-addressable deduplication).
     *
     * <p>Deleted files are excluded so that a new upload only deduplicates onto a
     * storage path that is still live and therefore counted by {@link
     * #countActiveReferences(String)}.
     *
     * @param checksum the SHA-256 checksum.
     * @return storage paths of active versions sharing that checksum.
     */
    @Query("SELECT v.storagePath FROM FileVersionEntity v WHERE v.checksum = :checksum AND v.file.deleted = false")
    List<String> findActiveStoragePathsByChecksum(String checksum);

    /**
     * Counts versions referencing the given storage path that belong to
     * non-deleted files.
     *
     * <p>Used for reference-counted storage reclamation: a storage path is safe to
     * physically delete only when no active file version still references it.
     *
     * @param storagePath the storage path.
     * @return the number of active references to the storage path.
     */
    @Query("SELECT COUNT(v) FROM FileVersionEntity v WHERE v.storagePath = :storagePath AND v.file.deleted = false")
    long countActiveReferences(String storagePath);

    /**
     * Returns the total size of all stored file versions.
     *
     * @return the sum of all version sizes in bytes, or 0 if no versions exist.
     */
    @Query("SELECT COALESCE(SUM(v.size), 0) FROM FileVersionEntity v")
    long sumTotalSize();
}
