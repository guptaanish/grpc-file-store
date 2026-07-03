# gRPC File Store

A Spring Boot application exposing a gRPC API for file storage with streaming upload/download, versioning, and search.

## Prerequisites

- **Java 21** (Corretto, Temurin, or any OpenJDK distribution)
- **Gradle 9.6.0** (wrapper included)

## Build & Run

```bash
./gradlew build
./gradlew bootRun
```

| Endpoint | Port | Purpose |
|----------|------|---------|
| gRPC server | `9090` | File store API |
| HTTP actuator | `8080/actuator/health` | Health checks |
| H2 console | `8080/h2-console` | Database browser |

## Testing

```bash
./gradlew test
```

47 tests (20 integration + 27 unit) covering upload/download round-trip, multi-chunk streaming, versioning, search, delete, version history, resumable uploads, file copy/move, content-addressable deduplication, reference-counted storage reclamation, concurrent-upload de-duplication of file records, quota enforcement, TTL auto-expiry, error cases, health indicators, checksums, storage, and concurrency components (per-file, filename, and checksum striped locks).

## Code Formatting

```bash
./gradlew format          # Auto-fix all formatting violations
./gradlew spotlessCheck   # Check formatting (runs with ./gradlew check)
```

## gRPC API

| RPC | Type | Description |
|-----|------|-------------|
| `UploadFile` | Client streaming | Upload file in 64 KB chunks with auto-versioning and SHA-256 checksum |
| `DownloadFile` | Server streaming | Download file chunks by ID and optional version |
| `ListFiles` | Unary | Search/list files with pagination (case-insensitive filename search) |
| `GetFileMetadata` | Unary | Get full metadata for a file |
| `DeleteFile` | Unary | Soft-delete a file |
| `GetFileVersions` | Unary | Get version history for a file |
| `InitiateResumableUpload` | Unary | Start a resumable upload session (returns session ID) |
| `ResumeUpload` | Client streaming | Resume upload from offset with session ID |
| `GetUploadStatus` | Unary | Check bytes received for a resumable session |
| `CopyFile` | Unary | Server-side copy to new filename (zero-copy, shares storage) |
| `MoveFile` | Unary | Rename a file without re-uploading |
| `Health/Check` | Unary | gRPC standard health check (grpc.health.v1) |

## Testing with grpcurl

```bash
# List services (reflection enabled)
grpcurl -plaintext localhost:9090 list

# List files
grpcurl -plaintext -d '{"search_query": "", "page_size": 10}' \
  localhost:9090 filestore.v1.FileStoreService/ListFiles

# Get file metadata
grpcurl -plaintext -d '{"file_id": "<uuid>"}' \
  localhost:9090 filestore.v1.FileStoreService/GetFileMetadata

# Download a file
grpcurl -plaintext -d '{"file_id": "<uuid>", "version": 0}' \
  localhost:9090 filestore.v1.FileStoreService/DownloadFile

# Delete a file
grpcurl -plaintext -d '{"file_id": "<uuid>"}' \
  localhost:9090 filestore.v1.FileStoreService/DeleteFile

# Get file versions
grpcurl -plaintext -d '{"file_id": "<uuid>"}' \
  localhost:9090 filestore.v1.FileStoreService/GetFileVersions

# Health check
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check
```

## Configuration

Key properties in `application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `filestore.chunk-size-bytes` | `65536` | Streaming chunk size (64 KB) |
| `filestore.max-file-size-bytes` | `104857600` | Max upload size (100 MB) |
| `filestore.storage.type` | `LOCAL` | Storage backend (LOCAL, S3, GCS) |
| `filestore.storage.local.directory` | `./file-store-data` | Local storage directory |
| `grpc.server.port` | `9090` | gRPC server port |
| `spring.threads.virtual.enabled` | `true` | Virtual threads (Project Loom) |
| `heartbeat.enabled` | `true` | Enable periodic heartbeat logging |
| `heartbeat.interval` | `300000` | Heartbeat interval in milliseconds (5 min) |
| `filestore.quota.enabled` | `false` | Enable global storage quota enforcement |
| `filestore.quota.max-total-bytes` | `1073741824` | Maximum total storage (1 GB) |
| `filestore.ttl.enabled` | `false` | Enable TTL auto-expiry of old files |
| `filestore.ttl.retention-days` | `30` | Days to retain files before auto-deletion |

## Project Structure

```
src/main/java/com/example/filestore/
├── GrpcFileStoreApplication.java            # Application entry point
├── config/
│   ├── FileStoreProperties.java             # @ConfigurationProperties (filestore.*)
│   ├── StorageProperties.java               # Storage backend config record
│   ├── StorageType.java                     # Enum: LOCAL, S3, GCS
│   └── StorageAutoConfiguration.java        # @ConditionalOnProperty bean factory
├── entity/
│   ├── FileEntity.java                      # JPA entity: file metadata
│   └── FileVersionEntity.java              # JPA entity: file version
├── repository/
│   ├── FileRepository.java                  # Spring Data JPA repository
│   └── FileVersionRepository.java          # Spring Data JPA repository
├── service/
│   ├── StorageService.java                  # Interface: file I/O
│   ├── MetadataService.java                 # Interface: metadata CRUD
│   ├── ChecksumService.java                 # Interface: hashing
│   ├── FileLockManager.java                 # Per-file ReadWriteLock manager
│   ├── StripedLockManager.java              # Base: fixed-pool striped locks
│   ├── FilenameLockManager.java             # Striped locks keyed by filename
│   ├── ChecksumLockManager.java             # Striped locks keyed by checksum
│   ├── FileNotFoundException.java           # Domain exception
│   ├── StorageQuotaExceededException.java   # Domain exception (quota)
│   ├── QuotaService.java                    # Global storage quota enforcement
│   ├── FileExpiryService.java               # Scheduled TTL soft-delete job
│   ├── StorageReclamationService.java       # Ref-counted storage reclamation
│   ├── ResumableUploadManager.java          # Resumable upload session manager
│   ├── ResumableUploadSession.java          # Resumable session state record
│   ├── UploadSession.java                   # Upload session record
│   ├── UploadTracker.java                   # Active upload tracking
│   └── impl/
│       ├── LocalFileStorageService.java     # StorageService: local filesystem
│       ├── JpaMetadataService.java          # MetadataService: JPA implementation
│       └── Sha256ChecksumService.java       # ChecksumService: SHA-256
├── mapper/
│   └── FileProtoMapper.java                 # MapStruct: Entity ↔ Proto
├── event/
│   ├── FileUploadedEvent.java               # Upload lifecycle event
│   ├── FileDeletedEvent.java                # Delete lifecycle event
│   └── FileEventListener.java              # Event logger
├── interceptor/
│   ├── MdcKeys.java                         # MDC field constants
│   ├── LoggingInterceptor.java              # Order 1: MDC + call logging
│   ├── ValidationInterceptor.java           # Order 2: request validation hook
│   ├── MetricsInterceptor.java              # Order 3: Micrometer metrics
│   └── ExceptionHandlerInterceptor.java     # Order 4: exception → gRPC Status
├── health/
│   ├── StorageHealthIndicator.java          # Disk space + writability check
│   ├── DatabaseHealthIndicator.java         # DB connectivity check
│   └── HeartbeatService.java               # Periodic heartbeat logger
└── grpc/
    ├── FileStoreGrpcService.java            # gRPC service implementation
    └── HealthCheckService.java              # grpc.health.v1.Health
```
