package com.example.filestore.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.repository.FileRepository;

/**
 * Scheduled job that soft-deletes files exceeding the configured retention period.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "filestore.ttl.enabled", havingValue = "true")
public class FileExpiryService {

    /**
     * File repository for querying expired files.
     */
    private final FileRepository fileRepository;

    /**
     * Configuration properties.
     */
    private final FileStoreProperties properties;

    /**
     * Runs the expiry cleanup at a fixed rate (default: every hour).
     */
    @Scheduled(fixedRateString = "${filestore.ttl.cleanup-interval:3600000}")
    @Transactional
    public void cleanupExpiredFiles() {
        final var cutoff = Instant.now().minus(properties.ttl().retentionDays(), ChronoUnit.DAYS);
        final var expired = fileRepository.findByDeletedFalseAndUpdatedAtBefore(cutoff);

        if (expired.isEmpty()) {
            return;
        }

        expired.forEach(file -> {
            file.setDeleted(true);
            file.setUpdatedAt(Instant.now());
        });
        fileRepository.saveAll(expired);
        log.info("TTL cleanup: soft-deleted {} expired files (retention: {} days)",
                expired.size(), properties.ttl().retentionDays());
    }
}
