package com.example.filestore.grpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.util.UUID;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.event.FileDeletedEvent;
import com.example.filestore.event.FileUploadedEvent;
import com.example.filestore.interceptor.MdcKeys;
import com.example.filestore.mapper.FileProtoMapper;
import com.example.filestore.service.ChecksumLockManager;
import com.example.filestore.service.ChecksumService;
import com.example.filestore.service.FileLockManager;
import com.example.filestore.service.FileNotFoundException;
import com.example.filestore.service.FilenameLockManager;
import com.example.filestore.service.MetadataService;
import com.example.filestore.service.QuotaService;
import com.example.filestore.service.ResumableUploadManager;
import com.example.filestore.service.ResumableUploadSession;
import com.example.filestore.service.StorageReclamationService;
import com.example.filestore.service.StorageService;
import com.example.filestore.service.UploadSession;
import com.example.filestore.service.UploadTracker;

/**
 * gRPC service implementation for the FileStoreService.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FileStoreGrpcService extends FileStoreServiceGrpc.FileStoreServiceImplBase {

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
     * Upload tracker for active session monitoring.
     */
    private final UploadTracker uploadTracker;

    /**
     * Application event publisher for lifecycle events.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Proto mapper for entity to proto conversions.
     */
    private final FileProtoMapper fileProtoMapper;

    /**
     * Resumable upload session manager.
     */
    private final ResumableUploadManager resumableUploadManager;

    /**
     * Quota enforcement service.
     */
    private final QuotaService quotaService;

    /**
     * Reference-counted storage reclamation service.
     */
    private final StorageReclamationService storageReclamationService;

    /**
     * Application configuration properties.
     */
    private final FileStoreProperties properties;

    @Override
    public StreamObserver<UploadFileRequest> uploadFile(StreamObserver<UploadFileResponse> responseObserver) {
        return new StreamObserver<>() {
            private FileInfo fileInfo;
            private java.io.OutputStream tempOutput;
            private java.nio.file.Path tempFile;
            private MessageDigest digest;
            private UploadSession session;
            private long totalSize;

            @Override
            public void onNext(UploadFileRequest request) {
                if (request.hasFileInfo()) {
                    fileInfo = request.getFileInfo();
                    digest = checksumService.createDigest();
                    session = UploadSession.create(fileInfo.getFilename());
                    uploadTracker.register(session);
                    MDC.put(MdcKeys.SESSION_ID, session.sessionId().toString());
                    MDC.put(MdcKeys.FILENAME, fileInfo.getFilename());
                    MDC.put(MdcKeys.CONTENT_TYPE, fileInfo.getContentType());
                    try {
                        tempFile = java.nio.file.Files.createTempFile("upload-", ".tmp");
                        tempOutput = java.nio.file.Files.newOutputStream(tempFile);
                    } catch (IOException e) {
                        responseObserver.onError(Status.INTERNAL
                                .withDescription("Failed to create temp file")
                                .asRuntimeException());
                    }
                    log.debug("Upload started: {}", fileInfo.getFilename());
                } else if (request.hasChunkData()) {
                    if (fileInfo == null) {
                        responseObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("First message must contain file_info")
                                .asRuntimeException());
                        return;
                    }
                    final byte[] chunk = request.getChunkData().toByteArray();
                    totalSize += chunk.length;

                    if (totalSize > properties.maxFileSizeBytes()) {
                        responseObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("File exceeds maximum allowed size")
                                .asRuntimeException());
                        cleanup();
                        return;
                    }

                    checksumService.update(digest, chunk, chunk.length);
                    try {
                        tempOutput.write(chunk, 0, chunk.length);
                    } catch (IOException e) {
                        responseObserver.onError(Status.INTERNAL
                                .withDescription("Failed to write chunk to temp file")
                                .asRuntimeException());
                        cleanup();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Upload stream error: {}", t.getMessage());
                cleanup();
            }

            @Override
            public void onCompleted() {
                if (fileInfo == null) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("First message must contain file_info")
                            .asRuntimeException());
                    return;
                }

                try {
                    tempOutput.close();
                    final var checksum = checksumService.finish(digest);
                    quotaService.checkQuota(totalSize);
                    final var file = createOrGetFileLocked(fileInfo.getFilename(), fileInfo.getContentType());
                    MDC.put(MdcKeys.FILE_ID, file.getId().toString());
                    final var lock = fileLockManager.getLock(file.getId()).writeLock();
                    lock.lock();
                    try {
                        final var version = storeVersionDeduplicated(
                                file, checksum, totalSize, tempFile, fileInfo.getFilename());
                        MDC.put(MdcKeys.VERSION, String.valueOf(version.getVersion()));
                        MDC.put(MdcKeys.BYTES_TRANSFERRED, String.valueOf(totalSize));

                        final var response = UploadFileResponse.newBuilder()
                                .setFileId(file.getId().toString())
                                .setFilename(file.getFilename())
                                .setSize(totalSize)
                                .setChecksum(checksum)
                                .setVersion(version.getVersion())
                                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                                .build();

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                        eventPublisher.publishEvent(new FileUploadedEvent(file.getId(), file.getFilename(), version.getVersion()));
                        log.info("Upload completed: {} v{}", file.getFilename(), version.getVersion());
                    } finally {
                        lock.unlock();
                    }
                } catch (IOException e) {
                    log.error("Upload storage failed: {}", e.getMessage(), e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Failed to write file to storage")
                            .asRuntimeException());
                } catch (UncheckedIOException e) {
                    log.error("Upload storage failed: {}", e.getMessage(), e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Failed to write file to storage")
                            .asRuntimeException());
                } catch (RuntimeException e) {
                    log.error("Upload failed: {}", e.getMessage(), e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Internal error during upload")
                            .asRuntimeException());
                } finally {
                    cleanup();
                }
            }

            private void cleanup() {
                if (session != null) {
                    uploadTracker.remove(session.sessionId());
                }
                if (tempFile != null) {
                    try {
                        java.nio.file.Files.deleteIfExists(tempFile);
                    } catch (IOException ignored) {
                    }
                }
            }
        };
    }

    @Override
    public void downloadFile(DownloadFileRequest request, StreamObserver<DownloadFileResponse> responseObserver) {
        log.info("DownloadFile request received: fileId={}, version={}", request.getFileId(), request.getVersion());
        final var fileId = parseFileId(request.getFileId());
        MDC.put(MdcKeys.FILE_ID, fileId.toString());

        final var file = metadataService.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.forFile(fileId));
        MDC.put(MdcKeys.FILENAME, file.getFilename());
        MDC.put(MdcKeys.VERSION, String.valueOf(request.getVersion() > 0 ? request.getVersion() : file.getCurrentVersion()));

        final var version = metadataService.findVersion(fileId, request.getVersion())
                .orElseThrow(() -> FileNotFoundException.forVersion(fileId, request.getVersion()));

        final var lock = fileLockManager.getLock(fileId).readLock();
        lock.lock();
        try {
            final var metadata = fileProtoMapper.toFileMetadata(file, version);
            responseObserver.onNext(DownloadFileResponse.newBuilder().setMetadata(metadata).build());

            long bytesTransferred = 0;
            try (InputStream inputStream = storageService.load(version.getStoragePath())) {
                final var chunkBuffer = new byte[properties.chunkSizeBytes()];
                int bytesRead;
                while ((bytesRead = inputStream.read(chunkBuffer)) != -1) {
                    responseObserver.onNext(DownloadFileResponse.newBuilder()
                            .setChunkData(ByteString.copyFrom(chunkBuffer, 0, bytesRead))
                            .build());
                    bytesTransferred += bytesRead;
                }
            }
            MDC.put(MdcKeys.BYTES_TRANSFERRED, String.valueOf(bytesTransferred));
            responseObserver.onCompleted();
            log.info("DownloadFile completed: {} v{}, {} bytes", file.getFilename(), version.getVersion(), bytesTransferred);
        } catch (IOException e) {
            log.error("DownloadFile failed for file {}: {}", fileId, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to read file from storage")
                    .asRuntimeException());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void listFiles(ListFilesRequest request, StreamObserver<ListFilesResponse> responseObserver) {
        log.info("ListFiles request received: query='{}', pageSize={}", request.getSearchQuery(), request.getPageSize());
        final int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 50;
        final int page = parsePageToken(request.getPageToken());

        final var pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        final var results = metadataService.search(request.getSearchQuery(), pageable);

        final var builder = ListFilesResponse.newBuilder();
        for (var file : results.getContent()) {
            metadataService.findVersion(file.getId(), 0).ifPresent(version ->
                    builder.addFiles(fileProtoMapper.toFileMetadata(file, version))
            );
        }

        if (results.hasNext()) {
            builder.setNextPageToken(String.valueOf(page + 1));
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        log.info("ListFiles completed: returned {} files", results.getNumberOfElements());
    }

    @Override
    public void getFileMetadata(GetFileMetadataRequest request, StreamObserver<FileMetadata> responseObserver) {
        log.info("GetFileMetadata request received: fileId={}", request.getFileId());
        final var fileId = parseFileId(request.getFileId());
        MDC.put(MdcKeys.FILE_ID, fileId.toString());

        final var file = metadataService.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.forFile(fileId));
        MDC.put(MdcKeys.FILENAME, file.getFilename());

        final var version = metadataService.findVersion(fileId, 0)
                .orElseThrow(() -> FileNotFoundException.forVersion(fileId, 0));

        responseObserver.onNext(fileProtoMapper.toFileMetadata(file, version));
        responseObserver.onCompleted();
        log.info("GetFileMetadata completed: {}", file.getFilename());
    }

    @Override
    public void deleteFile(DeleteFileRequest request, StreamObserver<DeleteFileResponse> responseObserver) {
        log.info("DeleteFile request received: fileId={}", request.getFileId());
        final var fileId = parseFileId(request.getFileId());
        MDC.put(MdcKeys.FILE_ID, fileId.toString());

        final var file = metadataService.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.forFile(fileId));

        MDC.put(MdcKeys.FILENAME, file.getFilename());
        final var lock = fileLockManager.getLock(fileId).writeLock();
        lock.lock();
        try {
            metadataService.softDelete(fileId);
            storageReclamationService.reclaimOrphanedStorage(fileId);
        } finally {
            lock.unlock();
        }
        eventPublisher.publishEvent(new FileDeletedEvent(fileId, file.getFilename()));
        responseObserver.onNext(DeleteFileResponse.newBuilder()
                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                .build());
        responseObserver.onCompleted();
        log.info("DeleteFile completed: {}", file.getFilename());
    }

    @Override
    public void getFileVersions(GetFileVersionsRequest request, StreamObserver<GetFileVersionsResponse> responseObserver) {
        log.info("GetFileVersions request received: fileId={}", request.getFileId());
        final var fileId = parseFileId(request.getFileId());
        MDC.put(MdcKeys.FILE_ID, fileId.toString());

        final var file = metadataService.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.forFile(fileId));

        MDC.put(MdcKeys.FILENAME, file.getFilename());
        final var versions = metadataService.getVersions(fileId);
        final var builder = GetFileVersionsResponse.newBuilder();
        versions.forEach(v -> builder.addVersions(fileProtoMapper.toFileVersionInfo(v)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        log.info("GetFileVersions completed: {} has {} versions", file.getFilename(), versions.size());
    }

    @Override
    public void initiateResumableUpload(InitiateResumableUploadRequest request,
                                        StreamObserver<InitiateResumableUploadResponse> responseObserver) {
        log.info("InitiateResumableUpload request received: filename={}, totalSize={}", request.getFilename(), request.getTotalSize());
        if (request.getFilename().isBlank()) {
            throw new IllegalArgumentException("filename is required");
        }
        final var session = resumableUploadManager.initiate(
                request.getFilename(), request.getContentType(), request.getTotalSize());

        responseObserver.onNext(InitiateResumableUploadResponse.newBuilder()
                .setSessionId(session.sessionId())
                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                .build());
        responseObserver.onCompleted();
        log.info("InitiateResumableUpload completed: sessionId={}", session.sessionId());
    }

    @Override
    public StreamObserver<ResumeUploadRequest> resumeUpload(StreamObserver<UploadFileResponse> responseObserver) {
        log.info("ResumeUpload stream initiated");
        return new StreamObserver<>() {
            private ResumableUploadSession session;
            private java.security.MessageDigest digest;

            @Override
            public void onNext(ResumeUploadRequest request) {
                if (request.hasHeader()) {
                    final var header = request.getHeader();
                    session = resumableUploadManager.getSession(header.getSessionId())
                            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + header.getSessionId()));

                    if (header.getOffset() != session.bytesReceived()) {
                        responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                                .withDescription("Offset mismatch: expected %d, got %d"
                                        .formatted(session.bytesReceived(), header.getOffset()))
                                .asRuntimeException());
                        return;
                    }
                    digest = checksumService.createDigest();
                    MDC.put(MdcKeys.SESSION_ID, session.sessionId());
                    MDC.put(MdcKeys.FILENAME, session.filename());
                } else if (request.hasChunkData()) {
                    if (session == null) {
                        responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                                .withDescription("First message must contain session header")
                                .asRuntimeException());
                        return;
                    }
                    final byte[] chunk = request.getChunkData().toByteArray();
                    checksumService.update(digest, chunk, chunk.length);
                    session = resumableUploadManager.appendData(session.sessionId(), chunk, chunk.length);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Resume upload stream error: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                if (session == null) {
                    responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                            .withDescription("First message must contain session header")
                            .asRuntimeException());
                    return;
                }

                final var completed = resumableUploadManager.complete(session.sessionId());
                try {
                    final var checksum = checksumService.finish(digest);
                    final var file = createOrGetFileLocked(completed.filename(), completed.contentType());
                    MDC.put(MdcKeys.FILE_ID, file.getId().toString());

                    final var lock = fileLockManager.getLock(file.getId()).writeLock();
                    lock.lock();
                    try (var inputStream = java.nio.file.Files.newInputStream(completed.tempFilePath())) {
                        final var storagePath = storageService.store(
                                file.getId(), file.getCurrentVersion() + 1, completed.filename(), inputStream);
                        final var version = metadataService.createVersion(
                                file, storagePath, completed.bytesReceived(), checksum);
                        MDC.put(MdcKeys.VERSION, String.valueOf(version.getVersion()));
                        MDC.put(MdcKeys.BYTES_TRANSFERRED, String.valueOf(completed.bytesReceived()));

                        responseObserver.onNext(UploadFileResponse.newBuilder()
                                .setFileId(file.getId().toString())
                                .setFilename(file.getFilename())
                                .setSize(completed.bytesReceived())
                                .setChecksum(checksum)
                                .setVersion(version.getVersion())
                                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                                .build());
                        responseObserver.onCompleted();
                        eventPublisher.publishEvent(new FileUploadedEvent(file.getId(), file.getFilename(), version.getVersion()));
                        log.info("ResumeUpload completed: {} v{}, {} bytes", file.getFilename(), version.getVersion(), completed.bytesReceived());
                    } finally {
                        lock.unlock();
                    }
                    java.nio.file.Files.deleteIfExists(completed.tempFilePath());
                } catch (IOException e) {
                    log.error("ResumeUpload finalization failed: {}", e.getMessage(), e);
                    responseObserver.onError(io.grpc.Status.INTERNAL
                            .withDescription("Failed to finalize resumable upload")
                            .asRuntimeException());
                }
            }
        };
    }

    @Override
    public void getUploadStatus(GetUploadStatusRequest request,
                                StreamObserver<GetUploadStatusResponse> responseObserver) {
        log.info("GetUploadStatus request received: sessionId={}", request.getSessionId());
        if (request.getSessionId().isBlank()) {
            throw new IllegalArgumentException("session_id is required");
        }
        final var session = resumableUploadManager.getSession(request.getSessionId())
                .orElseThrow(() -> new FileNotFoundException("Upload session not found: " + request.getSessionId()));

        responseObserver.onNext(GetUploadStatusResponse.newBuilder()
                .setSessionId(session.sessionId())
                .setFilename(session.filename())
                .setBytesReceived(session.bytesReceived())
                .setTotalSize(session.totalSize())
                .setState(UploadState.UPLOAD_STATE_IN_PROGRESS)
                .build());
        responseObserver.onCompleted();
        log.info("GetUploadStatus completed: sessionId={}, bytesReceived={}", session.sessionId(), session.bytesReceived());
    }

    @Override
    public void copyFile(CopyFileRequest request, StreamObserver<CopyFileResponse> responseObserver) {
        log.info("CopyFile request received: sourceFileId={}, destination={}", request.getSourceFileId(), request.getDestinationFilename());
        final var sourceFileId = parseFileId(request.getSourceFileId());
        if (request.getDestinationFilename().isBlank()) {
            throw new IllegalArgumentException("destination_filename is required");
        }
        MDC.put(MdcKeys.FILE_ID, sourceFileId.toString());

        final var copiedFile = metadataService.copyFile(sourceFileId, request.getSourceVersion(), request.getDestinationFilename());
        final var version = metadataService.findVersion(copiedFile.getId(), 0)
                .orElseThrow(() -> FileNotFoundException.forFile(copiedFile.getId()));

        responseObserver.onNext(CopyFileResponse.newBuilder()
                .setFile(fileProtoMapper.toFileMetadata(copiedFile, version))
                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                .build());
        responseObserver.onCompleted();
        log.info("CopyFile completed: {} → {}", request.getSourceFileId(), request.getDestinationFilename());
    }

    @Override
    public void moveFile(MoveFileRequest request, StreamObserver<MoveFileResponse> responseObserver) {
        log.info("MoveFile request received: fileId={}, newFilename={}", request.getFileId(), request.getNewFilename());
        final var fileId = parseFileId(request.getFileId());
        if (request.getNewFilename().isBlank()) {
            throw new IllegalArgumentException("new_filename is required");
        }
        MDC.put(MdcKeys.FILE_ID, fileId.toString());

        final var renamedFile = metadataService.renameFile(fileId, request.getNewFilename());
        final var version = metadataService.findVersion(renamedFile.getId(), 0)
                .orElseThrow(() -> FileNotFoundException.forFile(renamedFile.getId()));

        responseObserver.onNext(MoveFileResponse.newBuilder()
                .setFile(fileProtoMapper.toFileMetadata(renamedFile, version))
                .setStatus(com.example.filestore.grpc.Status.STATUS_SUCCESS)
                .build());
        responseObserver.onCompleted();
        log.info("MoveFile completed: {} → {}", fileId, request.getNewFilename());
    }

    /**
     * Creates or retrieves a file record while holding the filename stripe lock.
     *
     * <p>Serializing on the filename prevents two concurrent uploads of the same new
     * filename from each inserting a duplicate file record.
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
     * Stores a new version, deduplicating storage by checksum, while holding the
     * checksum stripe lock.
     *
     * <p>The checksum lock is shared with {@code StorageReclamationService} so that a
     * concurrent delete cannot reclaim the storage path this upload is deduplicating
     * onto.
     *
     * @param file     the parent file entity.
     * @param checksum the content checksum.
     * @param size     the file size in bytes.
     * @param tempFile the temporary file holding the uploaded content.
     * @param filename the original filename (used for the storage path).
     * @return the created version entity.
     * @throws IOException if reading the temp file fails.
     */
    private FileVersionEntity storeVersionDeduplicated(
            FileEntity file, String checksum, long size, java.nio.file.Path tempFile, String filename)
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
                try (var input = java.nio.file.Files.newInputStream(tempFile)) {
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
     * Parses a file ID string to a UUID, throwing IllegalArgumentException if invalid.
     *
     * @param fileId the file ID string.
     * @return the parsed UUID.
     */
    private UUID parseFileId(String fileId) {
        return UUID.fromString(fileId);
    }

    /**
     * Parses a page token string to an integer page number.
     *
     * @param pageToken the page token string.
     * @return the page number (0 if null or invalid).
     */
    private int parsePageToken(String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(pageToken);
        } catch (NumberFormatException e) {
            log.warn("Invalid page token received: {}", pageToken);
            return 0;
        }
    }
}
