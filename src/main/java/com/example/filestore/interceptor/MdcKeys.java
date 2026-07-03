package com.example.filestore.interceptor;

/**
 * Constants for MDC (Mapped Diagnostic Context) keys used throughout the application.
 */
public final class MdcKeys {

    private MdcKeys() {
    }

    /**
     * Unique identifier for the gRPC request, generated per RPC call.
     */
    public static final String REQUEST_ID = "request-id";

    /**
     * The bare gRPC method name (e.g., UploadFile, DownloadFile).
     */
    public static final String METHOD = "method";

    /**
     * Remote address of the gRPC client.
     */
    public static final String CLIENT_IP = "client-ip";

    /**
     * User-agent header from the gRPC client metadata.
     */
    public static final String USER_AGENT = "user-agent";

    /**
     * gRPC status code at call completion (e.g., OK, NOT_FOUND, INTERNAL).
     */
    public static final String STATUS = "status";

    /**
     * Time taken to complete the request in milliseconds.
     */
    public static final String DURATION_MS = "duration-ms";

    /**
     * UUID of the file being operated on.
     */
    public static final String FILE_ID = "file-id";

    /**
     * Original filename of the file being processed.
     */
    public static final String FILENAME = "filename";

    /**
     * MIME content type of the file (set during upload).
     */
    public static final String CONTENT_TYPE = "content-type";

    /**
     * File version number being accessed or created.
     */
    public static final String VERSION = "version";

    /**
     * Upload session identifier for tracking active streaming uploads.
     */
    public static final String SESSION_ID = "session-id";

    /**
     * Total bytes transferred during an upload or download operation.
     */
    public static final String BYTES_TRANSFERRED = "bytes-transferred";
}
