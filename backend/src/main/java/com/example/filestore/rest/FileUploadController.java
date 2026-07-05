package com.example.filestore.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.event.FileUploadedEvent;
import com.example.filestore.service.ChecksumLockManager;
import com.example.filestore.service.ChecksumService;
import com.example.filestore.service.FileLockManager;
import com.example.filestore.service.FilenameLockManager;
import com.example.filestore.service.MetadataService;
import com.example.filestore.service.QuotaService;
import com.example.filestore.service.ResumableUploadManager;
import com.example.filestore.service.StorageService;

/**
 * REST controller for file uploads (multipart and resumable).
 *
 * <p>This controller exists because gRPC-Web does not support client-streaming RPCs.
 * The browser frontend uses these REST endpoints for uploads while using gRPC-Web
 * for all other (unary and server-streaming) RPCs.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files/upload")
@RequiredArgsConstructor
public class FileUploadController {

    /**
     * Storage service for file I/O.
     */
    private final StorageService storageService;

    /**
     * Metadata service for file records.
     */
    private final MetadataService metadataService;

    /**
     * Checksum service for integrity hashing.
     */
    private final ChecksumService checksumService;

    /**
     * File lock manager for concurrent access control.
     */
    private final FileLockManager fileLockManager;

    /**
     * Filename lock manager to serialize file-record creation per filename.
     */
    private final FilenameLockManager filenameLockManager;

    /**
     * Checksum lock manager to serialize deduplication against reclamation.
     */
    private final ChecksumLockManager checksumLockManager;

    /**
     * Resumable upload session manager.
     */
    private final ResumableUploadManager resumableUploadManager;

    /**
     * Quota enforcement service.
     */
    private final QuotaService quotaService;

    /**
     * Application event publisher for lifecycle events.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Application configuration properties.
     */
    private final FileStoreProperties properties;

    /**
     * Uploads a file via multipart form data.
     *
     * <p>The file is streamed to a temp file, checksummed, deduplicated, and stored.
     * This mirrors the logic of the gRPC {@code UploadFile} RPC.
     *
     * @param file the multipart file from the browser.
     * @return the upload response with file ID, checksum, version, and size.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("REST upload received: filename={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        validateUploadRequest(file);

        final var filename = file.getOriginalFilename();
        final var contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        Path tempFile = null;
        try {
            // Stream to temp file and compute checksum incrementally
            tempFile = Files.createTempFile("rest-upload-", ".tmp");
            final MessageDigest digest = checksumService.createDigest();
            long totalSize = 0;

            try (InputStream input = file.getInputStream();
                 var output = Files.newOutputStream(tempFile)) {
                final var buffer = new byte[properties.chunkSizeBytes()];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    checksumService.update(digest, buffer, bytesRead);
                    output.write(buffer, 0, bytesRead);
                    totalSize += bytesRead;
                }
            }

            final var checksum = checksumService.finish(digest);
            quotaService.checkQuota(totalSize);

            // Create or get file record (serialized by filename)
            final var fileEntity = createOrGetFileLocked(filename, contentType);
            final var lock = fileLockManager.getLock(fileEntity.getId()).writeLock();
            lock.lock();
            try {
                final var version = storeVersionDeduplicated(fileEntity, checksum, totalSize, tempFile, filename);

                final var response = new UploadResponse(
                        fileEntity.getId().toString(),
                        fileEntity.getFilename(),
                        totalSize,
                        checksum,
                        version.getVersion()
                );

                eventPublisher.publishEvent(
                        new FileUploadedEvent(fileEntity.getId(), fileEntity.getFilename(), version.getVersion()));
                log.info("REST upload completed: {} v{}, {} bytes", filename, version.getVersion(), totalSize);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            log.error("REST upload failed for {}: {}", filename, e.getMessage(), e);
            throw new UncheckedIOException("Failed to process upload", e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * Initiates a resumable upload session.
     *
     * @param filename    the original filename.
     * @param contentType the MIME content type.
     * @param totalSize   the expected total file size in bytes.
     * @return a map containing the session ID.
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateResumableUpload(
            @RequestParam("filename") String filename,
            @RequestParam("contentType") String contentType,
            @RequestParam("totalSize") long totalSize) {

        log.info("REST initiate resumable upload: filename={}, totalSize={}", filename, totalSize);

        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "filename is required"));
        }
        if (totalSize <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "totalSize must be positive"));
        }

        final var session = resumableUploadManager.initiate(filename, contentType, totalSize);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "sessionId", session.sessionId(),
                "filename", session.filename(),
                "bytesReceived", session.bytesReceived(),
                "totalSize", session.totalSize()
        ));
    }

    /**
     * Resumes an upload by appending chunk data to the session.
     *
     * <p>The chunk is sent as a multipart file. When all bytes have been received
     * (bytesReceived == totalSize), the upload is finalized automatically.
     *
     * @param sessionId the resumable upload session ID.
     * @param chunk     the chunk data as a multipart file.
     * @param offset    the expected byte offset (for consistency validation).
     * @return the current upload status, or the final upload response if complete.
     */
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<?> resumeUpload(
            @PathVariable("sessionId") String sessionId,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("offset") long offset) {

        log.info("REST resume upload: sessionId={}, offset={}, chunkSize={}", sessionId, offset, chunk.getSize());

        final var session = resumableUploadManager.getSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (offset != session.bytesReceived()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Offset mismatch: expected %d, got %d".formatted(session.bytesReceived(), offset)
            ));
        }

        try {
            final byte[] data = chunk.getBytes();
            final var updated = resumableUploadManager.appendData(sessionId, data, data.length);

            // Check if upload is complete
            if (updated.bytesReceived() >= updated.totalSize()) {
                return finalizeResumableUpload(sessionId);
            }

            return ResponseEntity.ok(Map.of(
                    "sessionId", updated.sessionId(),
                    "bytesReceived", updated.bytesReceived(),
                    "totalSize", updated.totalSize(),
                    "state", "IN_PROGRESS"
            ));
        } catch (IOException e) {
            log.error("Resume upload chunk failed for session {}: {}", sessionId, e.getMessage(), e);
            throw new UncheckedIOException("Failed to process upload chunk", e);
        }
    }

    /**
     * Finalizes a completed resumable upload session.
     *
     * @param sessionId the session ID to finalize.
     * @return the upload response with file details.
     */
    private ResponseEntity<?> finalizeResumableUpload(String sessionId) {
        final var completed = resumableUploadManager.complete(sessionId);

        try {
            // Compute checksum of the full file
            final MessageDigest digest = checksumService.createDigest();
            try (var input = Files.newInputStream(completed.tempFilePath())) {
                final var buffer = new byte[properties.chunkSizeBytes()];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    checksumService.update(digest, buffer, bytesRead);
                }
            }
            final var checksum = checksumService.finish(digest);
            quotaService.checkQuota(completed.bytesReceived());

            final var fileEntity = createOrGetFileLocked(completed.filename(), completed.contentType());
            final var lock = fileLockManager.getLock(fileEntity.getId()).writeLock();
            lock.lock();
            try (var inputStream = Files.newInputStream(completed.tempFilePath())) {
                final var storagePath = storageService.store(
                        fileEntity.getId(), fileEntity.getCurrentVersion() + 1, completed.filename(), inputStream);
                final var version = metadataService.createVersion(
                        fileEntity, storagePath, completed.bytesReceived(), checksum);

                final var response = new UploadResponse(
                        fileEntity.getId().toString(),
                        fileEntity.getFilename(),
                        completed.bytesReceived(),
                        checksum,
                        version.getVersion()
                );

                eventPublisher.publishEvent(
                        new FileUploadedEvent(fileEntity.getId(), fileEntity.getFilename(), version.getVersion()));
                log.info("Resumable upload finalized: {} v{}, {} bytes",
                        completed.filename(), version.getVersion(), completed.bytesReceived());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            log.error("Resumable upload finalization failed for session {}: {}", sessionId, e.getMessage(), e);
            throw new UncheckedIOException("Failed to finalize resumable upload", e);
        } finally {
            deleteTempFile(completed.tempFilePath());
        }
    }

    /**
     * Validates the upload request parameters.
     *
     * @param file the multipart file to validate.
     */
    private void validateUploadRequest(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("Filename must not be blank");
        }
        if (file.getSize() > properties.maxFileSizeBytes()) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of %d bytes"
                    .formatted(properties.maxFileSizeBytes()));
        }
    }

    /**
     * Creates or retrieves a file record while holding the filename stripe lock.
     *
     * @param filename    the filename.
     * @param contentType the MIME content type.
     * @return the existing or newly created file entity.
     */
    private FileEntity createOrGetFileLocked(String filename, String contentType) {
        final var nameLock = filenameLockManager.getLock(filename);
        nameLock.lock();
        try {
            return metadataService.createOrGetFile(filename, contentType);
        } finally {
            nameLock.unlock();
        }
    }

    /**
     * Stores a new version, deduplicating storage by checksum.
     *
     * @param file     the parent file entity.
     * @param checksum the content checksum.
     * @param size     the file size in bytes.
     * @param tempFile the temporary file holding the uploaded content.
     * @param filename the original filename.
     * @return the created version entity.
     * @throws IOException if reading the temp file fails.
     */
    private FileVersionEntity storeVersionDeduplicated(
            FileEntity file, String checksum, long size, Path tempFile, String filename)
            throws IOException {
        final var checksumLock = checksumLockManager.getLock(checksum);
        checksumLock.lock();
        try {
            final var existingPath = metadataService.findStoragePathByChecksum(checksum);
            final String storagePath;
            if (existingPath.isPresent()) {
                storagePath = existingPath.get();
                log.info("Deduplication: reusing storage for checksum {}", checksum);
            } else {
                try (var input = Files.newInputStream(tempFile)) {
                    storagePath = storageService.store(
                            file.getId(), file.getCurrentVersion() + 1, filename, input);
                }
            }
            return metadataService.createVersion(file, storagePath, size, checksum);
        } finally {
            checksumLock.unlock();
        }
    }

    /**
     * Safely deletes a temporary file, logging any failure.
     *
     * @param tempFile the path to delete, or null.
     */
    private void deleteTempFile(Path tempFile) {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temp file {}: {}", tempFile, e.getMessage());
            }
        }
    }
}
