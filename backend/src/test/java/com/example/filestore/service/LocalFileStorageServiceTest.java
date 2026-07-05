package com.example.filestore.service;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.service.impl.LocalFileStorageService;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(new LocalStorageProperties(tempDir.toString()));
    }

    @Test
    void shouldStoreAndLoadFile() {
        final var fileId = UUID.randomUUID();
        final var content = "test content";
        final var input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        final var path = storageService.store(fileId, 1, "test.txt", input);

        assertNotNull(path);
        final var loaded = storageService.load(path);
        assertNotNull(loaded);
    }

    @Test
    void shouldDeleteFile() {
        final var fileId = UUID.randomUUID();
        final var input = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        final var path = storageService.store(fileId, 1, "del.txt", input);

        assertDoesNotThrow(() -> storageService.delete(path));
    }

    @Test
    void shouldThrowOnLoadNonExistentFile() {
        assertThrows(UncheckedIOException.class, () -> storageService.load("/nonexistent/path"));
    }

    @Test
    void shouldRejectPathTraversalInFilename() {
        final var fileId = UUID.randomUUID();
        final var input = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () ->
                storageService.store(fileId, 1, "../etc/passwd", input));
    }

    @Test
    void shouldRejectAbsolutePathInFilename() {
        final var fileId = UUID.randomUUID();
        final var input = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () ->
                storageService.store(fileId, 1, "/etc/passwd", input));
    }
}
