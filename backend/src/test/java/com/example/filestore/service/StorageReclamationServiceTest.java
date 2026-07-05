package com.example.filestore.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.repository.FileVersionRepository;
import com.example.filestore.service.impl.LocalFileStorageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StorageReclamationService} reference-counted reclamation.
 */
class StorageReclamationServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;
    private FileVersionRepository versionRepository;
    private StorageReclamationService reclamationService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(new LocalStorageProperties(tempDir.toString()));
        versionRepository = mock(FileVersionRepository.class);
        reclamationService = new StorageReclamationService(versionRepository, storageService, new ChecksumLockManager());
    }

    @Test
    void shouldReclaimStorageWhenNoActiveReferencesRemain() {
        final var fileId = UUID.randomUUID();
        final var path = storageService.store(fileId, 1, "orphan.txt",
                new ByteArrayInputStream("orphan".getBytes(StandardCharsets.UTF_8)));
        final var version = version(path, "checksum-a");

        when(versionRepository.findByFileIdOrderByVersionAsc(fileId)).thenReturn(List.of(version));
        when(versionRepository.countActiveReferences(path)).thenReturn(0L);

        reclamationService.reclaimOrphanedStorage(fileId);

        assertTrue(Files.notExists(Path.of(path)), "orphaned storage should be physically deleted");
    }

    @Test
    void shouldRetainStorageWhenActiveReferencesRemain() {
        final var fileId = UUID.randomUUID();
        final var path = storageService.store(fileId, 1, "shared.txt",
                new ByteArrayInputStream("shared".getBytes(StandardCharsets.UTF_8)));
        final var version = version(path, "checksum-b");

        when(versionRepository.findByFileIdOrderByVersionAsc(fileId)).thenReturn(List.of(version));
        when(versionRepository.countActiveReferences(path)).thenReturn(1L);

        reclamationService.reclaimOrphanedStorage(fileId);

        assertTrue(Files.exists(Path.of(path)), "storage referenced by another active file must be retained");
    }

    @Test
    void shouldEvaluateEachDistinctPathOnlyOnce() {
        final var fileId = UUID.randomUUID();
        final var storage = mock(StorageService.class);
        final var service = new StorageReclamationService(versionRepository, storage, new ChecksumLockManager());

        // Two versions of the same file deduplicated onto a single storage path.
        final var v1 = version("/data/shared.dat", "same-checksum");
        final var v2 = version("/data/shared.dat", "same-checksum");
        when(versionRepository.findByFileIdOrderByVersionAsc(fileId)).thenReturn(List.of(v1, v2));
        when(versionRepository.countActiveReferences("/data/shared.dat")).thenReturn(0L);

        service.reclaimOrphanedStorage(fileId);

        verify(versionRepository, times(1)).countActiveReferences("/data/shared.dat");
        verify(storage, times(1)).delete("/data/shared.dat");
    }

    private FileVersionEntity version(String storagePath, String checksum) {
        final var file = FileEntity.create("file.txt", "text/plain");
        return FileVersionEntity.create(file, 1, storagePath, 10L, checksum);
    }
}
