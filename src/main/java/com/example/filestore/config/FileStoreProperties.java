package com.example.filestore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the file store application.
 *
 * @param chunkSizeBytes   chunk size for streaming in bytes.
 * @param maxFileSizeBytes maximum allowed file size in bytes.
 * @param storage          storage backend configuration.
 * @param quota            global storage quota configuration.
 * @param ttl              TTL / auto-expiry configuration.
 */
@ConfigurationProperties(prefix = "filestore")
public record FileStoreProperties(
        int chunkSizeBytes,
        long maxFileSizeBytes,
        StorageProperties storage,
        QuotaProperties quota,
        TtlProperties ttl
) {

    /**
     * Storage quota configuration.
     *
     * @param enabled          whether quota enforcement is enabled.
     * @param maxTotalBytes    maximum total storage in bytes (default 1 GB).
     */
    public record QuotaProperties(
            boolean enabled,
            long maxTotalBytes
    ) {
    }

    /**
     * TTL / auto-expiry configuration.
     *
     * @param enabled       whether TTL cleanup is enabled.
     * @param retentionDays number of days to retain files before auto-deletion.
     */
    public record TtlProperties(
            boolean enabled,
            int retentionDays
    ) {
    }
}
