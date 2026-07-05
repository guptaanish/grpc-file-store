package com.example.filestore.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.config.FileStoreProperties.TtlProperties;
import com.example.filestore.config.StorageProperties;
import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.config.StorageType;
import com.example.filestore.entity.FileEntity;
import com.example.filestore.repository.FileRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileExpiryServiceTest {

    @Test
    void shouldSoftDeleteExpiredFiles() {
        final var repo = mock(FileRepository.class);
        final var props = createProps(7);
        final var service = new FileExpiryService(repo, props);

        final var expiredFile = mock(FileEntity.class);
        when(repo.findByDeletedFalseAndUpdatedAtBefore(any(Instant.class)))
                .thenReturn(List.of(expiredFile));

        service.cleanupExpiredFiles();

        verify(expiredFile).setDeleted(true);
        verify(expiredFile).setUpdatedAt(any(Instant.class));
        verify(repo).saveAll(List.of(expiredFile));
    }

    @Test
    void shouldDoNothingWhenNoExpiredFiles() {
        final var repo = mock(FileRepository.class);
        final var props = createProps(30);
        final var service = new FileExpiryService(repo, props);

        when(repo.findByDeletedFalseAndUpdatedAtBefore(any(Instant.class)))
                .thenReturn(List.of());

        service.cleanupExpiredFiles();

        verify(repo, never()).saveAll(any());
    }

    private FileStoreProperties createProps(int retentionDays) {
        return new FileStoreProperties(65536, 104857600,
                new StorageProperties(StorageType.LOCAL, new LocalStorageProperties("/tmp")),
                null, new TtlProperties(true, retentionDays));
    }
}
