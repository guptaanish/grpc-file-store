package com.example.filestore.service;

import org.junit.jupiter.api.Test;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.config.FileStoreProperties.QuotaProperties;
import com.example.filestore.config.StorageProperties;
import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.config.StorageType;
import com.example.filestore.repository.FileVersionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuotaServiceTest {

    @Test
    void shouldAllowUploadWhenUnderQuota() {
        final var repo = mock(FileVersionRepository.class);
        final var props = createProps(true, 1000);
        final var service = new QuotaService(props, repo);

        when(repo.sumTotalSize()).thenReturn(100L);

        assertDoesNotThrow(() -> service.checkQuota(500));
    }

    @Test
    void shouldThrowWhenQuotaExceeded() {
        final var repo = mock(FileVersionRepository.class);
        final var props = createProps(true, 1000);
        final var service = new QuotaService(props, repo);

        when(repo.sumTotalSize()).thenReturn(900L);

        final var ex = assertThrows(StorageQuotaExceededException.class,
                () -> service.checkQuota(200));
        assertTrue(ex.getMessage().contains("quota exceeded"));
    }

    @Test
    void shouldSkipCheckWhenQuotaDisabled() {
        final var repo = mock(FileVersionRepository.class);
        final var props = createProps(false, 100);
        final var service = new QuotaService(props, repo);

        assertDoesNotThrow(() -> service.checkQuota(99999));
        verifyNoInteractions(repo);
    }

    @Test
    void shouldSkipCheckWhenQuotaNull() {
        final var props = new FileStoreProperties(65536, 104857600,
                new StorageProperties(StorageType.LOCAL, new LocalStorageProperties("/tmp")), null, null);
        final var repo = mock(FileVersionRepository.class);
        final var service = new QuotaService(props, repo);

        assertDoesNotThrow(() -> service.checkQuota(99999));
        verifyNoInteractions(repo);
    }

    private FileStoreProperties createProps(boolean enabled, long maxBytes) {
        return new FileStoreProperties(65536, 104857600,
                new StorageProperties(StorageType.LOCAL, new LocalStorageProperties("/tmp")),
                new QuotaProperties(enabled, maxBytes), null);
    }
}
