package com.example.filestore.health;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.example.filestore.config.FileStoreProperties;

/**
 * Health indicator that checks storage directory writability and disk space.
 */
@Component
@RequiredArgsConstructor
public class StorageHealthIndicator implements HealthIndicator {

    /**
     * Minimum free space threshold in bytes (100 MB).
     */
    private static final long MIN_FREE_SPACE = 100 * 1024 * 1024L;

    /**
     * File store configuration properties.
     */
    private final FileStoreProperties properties;

    @Override
    public Health health() {
        final var storageDir = Path.of(properties.storage().local().directory());

        if (!Files.exists(storageDir)) {
            return Health.down().withDetail("error", "Storage directory does not exist").build();
        }

        if (!Files.isWritable(storageDir)) {
            return Health.down().withDetail("error", "Storage directory is not writable").build();
        }

        try {
            final long freeSpace = Files.getFileStore(storageDir).getUsableSpace();
            if (freeSpace < MIN_FREE_SPACE) {
                return Health.down()
                        .withDetail("freeSpace", freeSpace)
                        .withDetail("error", "Insufficient disk space")
                        .build();
            }
            return Health.up()
                    .withDetail("directory", storageDir.toString())
                    .withDetail("freeSpaceMB", freeSpace / (1024 * 1024))
                    .build();
        } catch (IOException e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
