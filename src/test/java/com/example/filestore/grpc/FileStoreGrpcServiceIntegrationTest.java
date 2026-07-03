package com.example.filestore.grpc;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the FileStoreGrpcService.
 */
@SpringBootTest(properties = {
        "grpc.server.in-process-name=test",
        "grpc.server.port=-1"
})
class FileStoreGrpcServiceIntegrationTest {

    private ManagedChannel channel;
    private FileStoreServiceGrpc.FileStoreServiceBlockingStub blockingStub;
    private FileStoreServiceGrpc.FileStoreServiceStub asyncStub;

    @BeforeEach
    void setUp() {
        channel = InProcessChannelBuilder.forName("test")
                .directExecutor()
                .build();
        blockingStub = FileStoreServiceGrpc.newBlockingStub(channel);
        asyncStub = FileStoreServiceGrpc.newStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
    }

    @Test
    void shouldUploadAndDownloadFile() throws Exception {
        // Upload
        final var uploadResponse = uploadTestFile("test.txt", "text/plain", "Hello, gRPC!");

        assertNotNull(uploadResponse.getFileId());
        assertEquals("test.txt", uploadResponse.getFilename());
        assertEquals(12, uploadResponse.getSize());
        assertEquals(1, uploadResponse.getVersion());
        assertEquals(Status.STATUS_SUCCESS, uploadResponse.getStatus());
        assertFalse(uploadResponse.getChecksum().isEmpty());

        // Download
        final var downloadRequest = DownloadFileRequest.newBuilder()
                .setFileId(uploadResponse.getFileId())
                .setVersion(0)
                .build();

        final var responses = blockingStub.downloadFile(downloadRequest);
        assertTrue(responses.hasNext());

        final var firstResponse = responses.next();
        assertTrue(firstResponse.hasMetadata());
        assertEquals("test.txt", firstResponse.getMetadata().getFilename());

        final var content = new StringBuilder();
        while (responses.hasNext()) {
            content.append(responses.next().getChunkData().toStringUtf8());
        }
        assertEquals("Hello, gRPC!", content.toString());
    }

    @Test
    void shouldUploadMultiChunkFileAndVerifyIntegrity() throws Exception {
        // Create 256 KB of data (4 chunks at 64 KB) to exercise temp file streaming
        final var data = new byte[256 * 1024];
        java.util.Arrays.fill(data, (byte) 'A');
        final var content = new String(data, java.nio.charset.StandardCharsets.UTF_8);

        final var latch = new CountDownLatch(1);
        final var responseRef = new AtomicReference<UploadFileResponse>();

        final var requestObserver = asyncStub.uploadFile(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) { responseRef.set(response); }
            @Override
            public void onError(Throwable t) { latch.countDown(); }
            @Override
            public void onCompleted() { latch.countDown(); }
        });

        requestObserver.onNext(UploadFileRequest.newBuilder()
                .setFileInfo(FileInfo.newBuilder().setFilename("large.bin").setContentType("application/octet-stream").build())
                .build());

        // Send in 64 KB chunks
        for (int i = 0; i < 4; i++) {
            requestObserver.onNext(UploadFileRequest.newBuilder()
                    .setChunkData(ByteString.copyFrom(data, i * 65536, 65536))
                    .build());
        }
        requestObserver.onCompleted();
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertNotNull(responseRef.get());
        assertEquals(256 * 1024, responseRef.get().getSize());
        assertFalse(responseRef.get().getChecksum().isEmpty());

        // Download and verify size matches
        final var responses = blockingStub.downloadFile(DownloadFileRequest.newBuilder()
                .setFileId(responseRef.get().getFileId()).build());
        assertTrue(responses.hasNext());
        responses.next(); // metadata
        long downloaded = 0;
        while (responses.hasNext()) {
            downloaded += responses.next().getChunkData().size();
        }
        assertEquals(256 * 1024, downloaded);
    }

    @Test
    void shouldIncrementVersionOnReupload() throws Exception {
        uploadTestFile("versioned.txt", "text/plain", "version 1");
        final var response2 = uploadTestFile("versioned.txt", "text/plain", "version 2");

        assertEquals(2, response2.getVersion());
    }

    @Test
    void shouldListFiles() throws Exception {
        uploadTestFile("list-test-a.txt", "text/plain", "a");
        uploadTestFile("list-test-b.txt", "text/plain", "b");

        final var listResponse = blockingStub.listFiles(ListFilesRequest.newBuilder()
                .setSearchQuery("list-test")
                .setPageSize(10)
                .build());

        assertTrue(listResponse.getFilesCount() >= 2);
    }

    @Test
    void shouldDeleteFile() throws Exception {
        final var uploaded = uploadTestFile("to-delete.txt", "text/plain", "delete me");

        final var deleteResponse = blockingStub.deleteFile(DeleteFileRequest.newBuilder()
                .setFileId(uploaded.getFileId())
                .build());

        assertEquals(Status.STATUS_SUCCESS, deleteResponse.getStatus());

        // Verify file is no longer found
        assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getFileMetadata(GetFileMetadataRequest.newBuilder()
                        .setFileId(uploaded.getFileId())
                        .build()));
    }

    @Test
    void shouldReturnVersionHistory() throws Exception {
        uploadTestFile("history.txt", "text/plain", "v1");
        uploadTestFile("history.txt", "text/plain", "v2");

        // Get file ID by listing
        final var list = blockingStub.listFiles(ListFilesRequest.newBuilder()
                .setSearchQuery("history.txt")
                .setPageSize(1)
                .build());

        final var fileId = list.getFiles(0).getFileId();
        final var versions = blockingStub.getFileVersions(GetFileVersionsRequest.newBuilder()
                .setFileId(fileId)
                .build());

        assertEquals(2, versions.getVersionsCount());
        assertEquals(1, versions.getVersions(0).getVersion());
        assertEquals(2, versions.getVersions(1).getVersion());
    }

    @Test
    void shouldReturnNotFoundForNonExistentFile() {
        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getFileMetadata(GetFileMetadataRequest.newBuilder()
                        .setFileId("00000000-0000-0000-0000-000000000000")
                        .build()));

        assertEquals(io.grpc.Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    void shouldReturnInvalidArgumentForBadUuid() {
        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getFileMetadata(GetFileMetadataRequest.newBuilder()
                        .setFileId("not-a-uuid")
                        .build()));

        assertEquals(io.grpc.Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
    }

    /**
     * Uploads a test file using the async streaming stub.
     *
     * @param filename    the filename.
     * @param contentType the content type.
     * @param content     the file content.
     * @return the upload response.
     * @throws InterruptedException if the upload is interrupted.
     */
    private UploadFileResponse uploadTestFile(String filename, String contentType, String content) throws InterruptedException {
        final var latch = new CountDownLatch(1);
        final var responseRef = new AtomicReference<UploadFileResponse>();
        final var errorRef = new AtomicReference<Throwable>();

        final var requestObserver = asyncStub.uploadFile(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) {
                responseRef.set(response);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        // Send file info
        requestObserver.onNext(UploadFileRequest.newBuilder()
                .setFileInfo(FileInfo.newBuilder()
                        .setFilename(filename)
                        .setContentType(contentType)
                        .build())
                .build());

        // Send chunk
        requestObserver.onNext(UploadFileRequest.newBuilder()
                .setChunkData(ByteString.copyFrom(content, StandardCharsets.UTF_8))
                .build());

        requestObserver.onCompleted();
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        if (errorRef.get() != null) {
            fail("Upload failed: " + errorRef.get().getMessage());
        }

        return responseRef.get();
    }

    @Test
    void shouldCompleteResumableUpload() throws Exception {
        // Initiate session
        final var initResponse = blockingStub.initiateResumableUpload(
                InitiateResumableUploadRequest.newBuilder()
                        .setFilename("resumable.txt")
                        .setContentType("text/plain")
                        .setTotalSize(11)
                        .build());

        assertFalse(initResponse.getSessionId().isEmpty());
        assertEquals(Status.STATUS_SUCCESS, initResponse.getStatus());

        // Check status
        final var statusResponse = blockingStub.getUploadStatus(
                GetUploadStatusRequest.newBuilder()
                        .setSessionId(initResponse.getSessionId())
                        .build());
        assertEquals(0, statusResponse.getBytesReceived());
        assertEquals(11, statusResponse.getTotalSize());

        // Resume upload with data
        final var latch = new CountDownLatch(1);
        final var responseRef = new AtomicReference<UploadFileResponse>();

        final var requestObserver = asyncStub.resumeUpload(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) { responseRef.set(response); }
            @Override
            public void onError(Throwable t) { latch.countDown(); }
            @Override
            public void onCompleted() { latch.countDown(); }
        });

        // Send header
        requestObserver.onNext(ResumeUploadRequest.newBuilder()
                .setHeader(ResumeSessionHeader.newBuilder()
                        .setSessionId(initResponse.getSessionId())
                        .setOffset(0)
                        .build())
                .build());

        // Send data
        requestObserver.onNext(ResumeUploadRequest.newBuilder()
                .setChunkData(ByteString.copyFromUtf8("hello world"))
                .build());

        requestObserver.onCompleted();
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertNotNull(responseRef.get());
        assertEquals("resumable.txt", responseRef.get().getFilename());
        assertEquals(11, responseRef.get().getSize());
        assertEquals(1, responseRef.get().getVersion());
    }

    @Test
    void shouldRejectInvalidSessionId() {
        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getUploadStatus(GetUploadStatusRequest.newBuilder()
                        .setSessionId("nonexistent-session")
                        .build()));

        assertEquals(io.grpc.Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    void shouldResumeUploadFromOffset() throws Exception {
        // Initiate
        final var initResponse = blockingStub.initiateResumableUpload(
                InitiateResumableUploadRequest.newBuilder()
                        .setFilename("multi-chunk.txt")
                        .setContentType("text/plain")
                        .setTotalSize(10)
                        .build());

        // First partial upload (5 bytes)
        final var latch1 = new CountDownLatch(1);
        final var errorRef1 = new AtomicReference<Throwable>();
        final var observer1 = asyncStub.resumeUpload(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) {}
            @Override
            public void onError(Throwable t) { errorRef1.set(t); latch1.countDown(); }
            @Override
            public void onCompleted() { latch1.countDown(); }
        });
        observer1.onNext(ResumeUploadRequest.newBuilder()
                .setHeader(ResumeSessionHeader.newBuilder()
                        .setSessionId(initResponse.getSessionId())
                        .setOffset(0).build()).build());
        observer1.onNext(ResumeUploadRequest.newBuilder()
                .setChunkData(ByteString.copyFromUtf8("hello")).build());
        observer1.onError(new RuntimeException("simulated disconnect"));
        assertTrue(latch1.await(10, TimeUnit.SECONDS));

        // Check status shows 5 bytes received
        final var status = blockingStub.getUploadStatus(
                GetUploadStatusRequest.newBuilder()
                        .setSessionId(initResponse.getSessionId()).build());
        assertEquals(5, status.getBytesReceived());
        assertEquals("multi-chunk.txt", status.getFilename());

        // Resume from offset 5
        final var latch2 = new CountDownLatch(1);
        final var responseRef = new AtomicReference<UploadFileResponse>();
        final var observer2 = asyncStub.resumeUpload(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) { responseRef.set(response); }
            @Override
            public void onError(Throwable t) { latch2.countDown(); }
            @Override
            public void onCompleted() { latch2.countDown(); }
        });
        observer2.onNext(ResumeUploadRequest.newBuilder()
                .setHeader(ResumeSessionHeader.newBuilder()
                        .setSessionId(initResponse.getSessionId())
                        .setOffset(5).build()).build());
        observer2.onNext(ResumeUploadRequest.newBuilder()
                .setChunkData(ByteString.copyFromUtf8("world")).build());
        observer2.onCompleted();
        assertTrue(latch2.await(10, TimeUnit.SECONDS));

        assertNotNull(responseRef.get());
        assertEquals(10, responseRef.get().getSize());
        assertEquals(Status.STATUS_SUCCESS, responseRef.get().getStatus());
    }

    @Test
    void shouldRejectOffsetMismatch() throws Exception {
        final var initResponse = blockingStub.initiateResumableUpload(
                InitiateResumableUploadRequest.newBuilder()
                        .setFilename("offset-test.txt")
                        .setContentType("text/plain")
                        .setTotalSize(10)
                        .build());

        // Try to resume with wrong offset (server has 0 bytes, client says 5)
        final var latch = new CountDownLatch(1);
        final var errorRef = new AtomicReference<Throwable>();
        final var observer = asyncStub.resumeUpload(new StreamObserver<>() {
            @Override
            public void onNext(UploadFileResponse response) {}
            @Override
            public void onError(Throwable t) { errorRef.set(t); latch.countDown(); }
            @Override
            public void onCompleted() { latch.countDown(); }
        });
        observer.onNext(ResumeUploadRequest.newBuilder()
                .setHeader(ResumeSessionHeader.newBuilder()
                        .setSessionId(initResponse.getSessionId())
                        .setOffset(5).build()).build());
        observer.onCompleted();
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertNotNull(errorRef.get());
        assertTrue(errorRef.get().getMessage().contains("Offset mismatch"));
    }

    @Test
    void shouldRejectBlankFilenameForResumableUpload() {
        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.initiateResumableUpload(
                        InitiateResumableUploadRequest.newBuilder()
                                .setFilename("")
                                .setContentType("text/plain")
                                .setTotalSize(10)
                                .build()));

        assertEquals(io.grpc.Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
    }

    @Test
    void shouldCopyFileToNewName() throws Exception {
        final var uploaded = uploadTestFile("original.txt", "text/plain", "copy me");

        final var copyResponse = blockingStub.copyFile(CopyFileRequest.newBuilder()
                .setSourceFileId(uploaded.getFileId())
                .setDestinationFilename("copied.txt")
                .setSourceVersion(0)
                .build());

        assertEquals(Status.STATUS_SUCCESS, copyResponse.getStatus());
        assertEquals("copied.txt", copyResponse.getFile().getFilename());
        assertEquals(1, copyResponse.getFile().getCurrentVersion());
        assertNotEquals(uploaded.getFileId(), copyResponse.getFile().getFileId());
    }

    @Test
    void shouldMoveRenameFile() throws Exception {
        final var uploaded = uploadTestFile("before-rename.txt", "text/plain", "rename me");

        final var moveResponse = blockingStub.moveFile(MoveFileRequest.newBuilder()
                .setFileId(uploaded.getFileId())
                .setNewFilename("after-rename.txt")
                .build());

        assertEquals(Status.STATUS_SUCCESS, moveResponse.getStatus());
        assertEquals("after-rename.txt", moveResponse.getFile().getFilename());
        assertEquals(uploaded.getFileId(), moveResponse.getFile().getFileId());
    }

    @Test
    void shouldRejectCopyOfNonExistentFile() {
        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.copyFile(CopyFileRequest.newBuilder()
                        .setSourceFileId("00000000-0000-0000-0000-000000000000")
                        .setDestinationFilename("nope.txt")
                        .build()));

        assertEquals(io.grpc.Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    void shouldRejectMoveWithBlankFilename() throws Exception {
        final var uploaded = uploadTestFile("move-blank.txt", "text/plain", "data");

        final var exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.moveFile(MoveFileRequest.newBuilder()
                        .setFileId(uploaded.getFileId())
                        .setNewFilename("")
                        .build()));

        assertEquals(io.grpc.Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
    }

    @Test
    void shouldDeduplicateIdenticalContent() throws Exception {
        final var content = "identical content for dedup test";

        // Upload same content with two different filenames
        final var upload1 = uploadTestFile("dedup-a.txt", "text/plain", content);
        final var upload2 = uploadTestFile("dedup-b.txt", "text/plain", content);

        // Both should have the same checksum
        assertEquals(upload1.getChecksum(), upload2.getChecksum());

        // Both succeed
        assertEquals(Status.STATUS_SUCCESS, upload1.getStatus());
        assertEquals(Status.STATUS_SUCCESS, upload2.getStatus());

        // They are different files
        assertNotEquals(upload1.getFileId(), upload2.getFileId());

        // Both downloadable
        final var download1 = blockingStub.downloadFile(DownloadFileRequest.newBuilder()
                .setFileId(upload1.getFileId()).build());
        assertTrue(download1.hasNext());

        final var download2 = blockingStub.downloadFile(DownloadFileRequest.newBuilder()
                .setFileId(upload2.getFileId()).build());
        assertTrue(download2.hasNext());
    }
}
