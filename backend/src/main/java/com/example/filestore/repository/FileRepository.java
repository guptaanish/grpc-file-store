package com.example.filestore.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.filestore.entity.FileEntity;

/**
 * Spring Data JPA repository for {@link FileEntity}.
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    /**
     * Finds a non-deleted file by filename.
     *
     * @param filename the filename to search for.
     * @return an optional containing the file entity if found.
     */
    Optional<FileEntity> findByFilenameAndDeletedFalse(String filename);

    /**
     * Finds a non-deleted file by ID.
     *
     * @param id the file ID.
     * @return an optional containing the file entity if found.
     */
    Optional<FileEntity> findByIdAndDeletedFalse(UUID id);

    /**
     * Searches for non-deleted files with filenames containing the query (case-insensitive).
     *
     * @param query    the search query.
     * @param pageable pagination parameters.
     * @return a page of matching file entities.
     */
    Page<FileEntity> findByFilenameContainingIgnoreCaseAndDeletedFalse(String query, Pageable pageable);

    /**
     * Lists all non-deleted files with pagination.
     *
     * @param pageable pagination parameters.
     * @return a page of file entities.
     */
    Page<FileEntity> findByDeletedFalse(Pageable pageable);

    /**
     * Finds non-deleted files last updated before the given cutoff time.
     *
     * @param cutoff the cutoff timestamp.
     * @return list of expired file entities.
     */
    List<FileEntity> findByDeletedFalseAndUpdatedAtBefore(java.time.Instant cutoff);
}
