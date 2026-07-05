package com.example.filestore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.repository.FileVersionRepository;

/**
 * Enforces global storage quota limits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaService {

    /**
     * File store configuration.
     */
    private final FileStoreProperties properties;

    /**
     * Version repository for computing total storage used.
     */
    private final FileVersionRepository fileVersionRepository;

    /**
     * Checks if adding the given bytes would exceed the quota.
     *
     * @param additionalBytes the number of bytes to be added.
     * @throws StorageQuotaExceededException if quota would be exceeded.
     */
    public void checkQuota(long additionalBytes) {
        final var quota = properties.quota();
        if (quota == null || !quota.enabled()) {
            return;
        }
        final long totalUsed = fileVersionRepository.sumTotalSize();
        if (totalUsed + additionalBytes > quota.maxTotalBytes()) {
            log.warn("Storage quota exceeded: used={}, additional={}, max={}",
                    totalUsed, additionalBytes, quota.maxTotalBytes());
            throw new StorageQuotaExceededException(
                    "Storage quota exceeded: %d/%d bytes used".formatted(totalUsed, quota.maxTotalBytes()));
        }
    }
}
