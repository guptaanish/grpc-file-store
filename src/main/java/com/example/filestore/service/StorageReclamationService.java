package com.example.filestore.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.repository.FileVersionRepository;

/**
 * Reclaims physical storage for a soft-deleted file, honouring content-addressable
 * deduplication via reference counting.
 *
 * <p>Because uploads may deduplicate identical content onto a single storage path
 * that is shared across multiple versions and files, a storage path may only be
 * physically deleted once no <em>active</em> (non-deleted) file version references
 * it. This service must be invoked <em>after</em> the file has been soft-deleted so
 * that the file's own versions are excluded from the active reference count.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageReclamationService {

    /**
     * Repository for querying versions and reference counts.
     */
    private final FileVersionRepository fileVersionRepository;

    /**
     * Storage backend for physical deletion.
     */
    private final StorageService storageService;

    /**
     * Lock manager shared with the upload/deduplication path.
     */
    private final ChecksumLockManager checksumLockManager;

    /**
     * Physically deletes any storage paths of the given (already soft-deleted) file
     * that are no longer referenced by an active file version.
     *
     * <p>Each distinct storage path is evaluated under the checksum stripe lock, so a
     * concurrent upload cannot deduplicate onto the same content while the reference
     * count is being checked and the file removed.
     *
     * @param fileId the ID of the file that was just soft-deleted.
     */
    public void reclaimOrphanedStorage(UUID fileId) {
        final var versions = fileVersionRepository.findByFileIdOrderByVersionAsc(fileId);
        final Set<String> processedPaths = new HashSet<>();

        for (final FileVersionEntity version : versions) {
            final var storagePath = version.getStoragePath();
            if (!processedPaths.add(storagePath)) {
                continue;
            }

            final var lock = checksumLockManager.getLock(version.getChecksum());
            lock.lock();
            try {
                final long activeReferences = fileVersionRepository.countActiveReferences(storagePath);
                if (activeReferences == 0) {
                    storageService.delete(storagePath);
                    log.info("Reclaimed orphaned storage for file {}: {}", fileId, storagePath);
                } else {
                    log.debug("Retaining shared storage for file {}: {} ({} active references)",
                            fileId, storagePath, activeReferences);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
