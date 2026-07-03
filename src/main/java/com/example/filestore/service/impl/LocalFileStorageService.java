package com.example.filestore.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.service.StorageService;

/**
 * Local filesystem implementation of {@link StorageService}.
 */
@Slf4j
public class LocalFileStorageService implements StorageService {

    /**
     * Root directory for file storage.
     */
    private final Path rootDir;

    /**
     * Constructs the local file storage service and ensures the root directory exists.
     *
     * @param properties the local storage properties.
     */
    public LocalFileStorageService(LocalStorageProperties properties) {
        this.rootDir = Path.of(properties.directory());
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create storage directory", e);
        }
    }

    @Override
    public String store(UUID fileId, int version, String filename, InputStream data) {
        final var fileDir = rootDir.resolve(fileId.toString());
        try {
            Files.createDirectories(fileDir);
            final var targetPath = fileDir.resolve("v%d_%s.dat".formatted(version, sanitize(filename)));
            Files.copy(data, targetPath);
            log.debug("Stored file at: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write file to storage", e);
        }
    }

    @Override
    public InputStream load(String storagePath) {
        try {
            return Files.newInputStream(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file from storage", e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file from storage", e);
        }
    }

    /**
     * Sanitizes a filename by removing dangerous characters and rejecting path traversal attempts.
     *
     * @param filename the original filename.
     * @return the sanitized filename.
     * @throws IllegalArgumentException if the filename contains path traversal sequences.
     */
    private String sanitize(String filename) {
        if (filename.contains("..") || filename.startsWith("/") || filename.contains(java.io.File.separator)) {
            throw new IllegalArgumentException("Invalid filename: path traversal detected");
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
