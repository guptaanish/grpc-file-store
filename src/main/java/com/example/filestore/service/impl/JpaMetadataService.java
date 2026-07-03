package com.example.filestore.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.repository.FileRepository;
import com.example.filestore.repository.FileVersionRepository;
import com.example.filestore.service.MetadataService;

/**
 * JPA-backed implementation of {@link MetadataService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaMetadataService implements MetadataService {

    /**
     * Repository for file entities.
     */
    private final FileRepository fileRepository;

    /**
     * Repository for file version entities.
     */
    private final FileVersionRepository fileVersionRepository;

    @Override
    @Transactional
    public FileEntity createOrGetFile(String filename, String contentType) {
        return fileRepository.findByFilenameAndDeletedFalse(filename)
                .orElseGet(() -> fileRepository.save(FileEntity.create(filename, contentType)));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public FileVersionEntity createVersion(FileEntity file, String storagePath, long size, String checksum) {
        final int nextVersion = file.getCurrentVersion() + 1;
        file.setCurrentVersion(nextVersion);
        file.setUpdatedAt(Instant.now());
        fileRepository.save(file);

        final var version = FileVersionEntity.create(file, nextVersion, storagePath, size, checksum);
        log.debug("Created version {} for file {}", nextVersion, file.getId());
        return fileVersionRepository.save(version);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileEntity> findById(UUID fileId) {
        return fileRepository.findByIdAndDeletedFalse(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileVersionEntity> findVersion(UUID fileId, int version) {
        if (version <= 0) {
            return fileVersionRepository.findTopByFileIdOrderByVersionDesc(fileId);
        }
        return fileVersionRepository.findByFileIdAndVersion(fileId, version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileVersionEntity> getVersions(UUID fileId) {
        return fileVersionRepository.findByFileIdOrderByVersionAsc(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileEntity> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return fileRepository.findByDeletedFalse(pageable);
        }
        return fileRepository.findByFilenameContainingIgnoreCaseAndDeletedFalse(query, pageable);
    }

    @Override
    @Transactional
    public void softDelete(UUID fileId) {
        fileRepository.findByIdAndDeletedFalse(fileId).ifPresent(file -> {
            file.setDeleted(true);
            file.setUpdatedAt(Instant.now());
            fileRepository.save(file);
            log.info("Soft-deleted file: {}", fileId);
        });
    }

    @Override
    @Transactional
    public FileEntity copyFile(UUID sourceFileId, int sourceVersion, String destinationFilename) {
        final var sourceFile = fileRepository.findByIdAndDeletedFalse(sourceFileId)
                .orElseThrow(() -> new com.example.filestore.service.FileNotFoundException("Source file not found: " + sourceFileId));

        final var sourceVer = sourceVersion <= 0
                ? fileVersionRepository.findTopByFileIdOrderByVersionDesc(sourceFileId)
                        .orElseThrow(() -> new com.example.filestore.service.FileNotFoundException("No version found for source file"))
                : fileVersionRepository.findByFileIdAndVersion(sourceFileId, sourceVersion)
                        .orElseThrow(() -> new com.example.filestore.service.FileNotFoundException("Version %d not found".formatted(sourceVersion)));

        final var destFile = fileRepository.save(FileEntity.create(destinationFilename, sourceFile.getContentType()));
        destFile.setCurrentVersion(1);
        destFile.setUpdatedAt(Instant.now());
        fileRepository.save(destFile);

        fileVersionRepository.save(FileVersionEntity.create(destFile, 1, sourceVer.getStoragePath(), sourceVer.getSize(), sourceVer.getChecksum()));
        log.info("Copied file {} v{} → {}", sourceFileId, sourceVer.getVersion(), destinationFilename);
        return destFile;
    }

    @Override
    @Transactional
    public FileEntity renameFile(UUID fileId, String newFilename) {
        final var file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new com.example.filestore.service.FileNotFoundException("File not found: " + fileId));

        file.setFilename(newFilename);
        file.setUpdatedAt(Instant.now());
        log.info("Renamed file {} → {}", fileId, newFilename);
        return fileRepository.save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findStoragePathByChecksum(String checksum) {
        return fileVersionRepository.findActiveStoragePathsByChecksum(checksum).stream().findFirst();
    }
}
