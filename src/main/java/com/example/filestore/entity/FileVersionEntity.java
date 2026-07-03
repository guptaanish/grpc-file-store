package com.example.filestore.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a specific version of a file.
 */
@Entity
@Table(name = "file_versions")
@Getter
@NoArgsConstructor
public class FileVersionEntity {

    /**
     * Unique identifier for this version record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The parent file entity.
     */
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    /**
     * Version number for this file version.
     */
    @Column(nullable = false)
    private int version;

    /**
     * Path where this version is stored on disk.
     */
    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    /**
     * File size in bytes.
     */
    @Column(nullable = false)
    private long size;

    /**
     * SHA-256 hex digest of the file content.
     */
    @Column(nullable = false, length = 64)
    private String checksum;

    /**
     * Timestamp when this version was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates a new FileVersionEntity.
     *
     * @param file        the parent file entity.
     * @param version     the version number.
     * @param storagePath the storage path.
     * @param size        the file size in bytes.
     * @param checksum    the SHA-256 checksum.
     * @return a new FileVersionEntity instance.
     */
    public static FileVersionEntity create(FileEntity file, int version, String storagePath, long size, String checksum) {
        final var entity = new FileVersionEntity();
        entity.file = file;
        entity.version = version;
        entity.storagePath = storagePath;
        entity.size = size;
        entity.checksum = checksum;
        entity.createdAt = Instant.now();
        return entity;
    }
}
