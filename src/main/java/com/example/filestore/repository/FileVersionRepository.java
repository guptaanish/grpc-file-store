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
     * Finds any version with the given checksum (for deduplication).
     *
     * @param checksum the SHA-256 checksum.
     * @return an optional containing a version with that checksum.
     */
    Optional<FileVersionEntity> findFirstByChecksum(String checksum);

    /**
     * Returns the total size of all stored file versions.
     *
     * @return the sum of all version sizes in bytes, or 0 if no versions exist.
     */
    @Query("SELECT COALESCE(SUM(v.size), 0) FROM FileVersionEntity v")
    long sumTotalSize();
}
