package com.example.filestore.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a stored file's metadata.
 */
@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor
public class FileEntity {

    /**
     * Unique identifier for the file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Original filename.
     */
    @Setter
    @Column(nullable = false, length = 512)
    private String filename;

    /**
     * MIME content type.
     */
    @Column(name = "content_type", length = 255)
    private String contentType;

    /**
     * Latest version number.
     */
    @Setter
    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    /**
     * Timestamp of first upload.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of last modification.
     */
    @Setter
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Soft-delete flag.
     */
    @Setter
    @Column(nullable = false)
    private boolean deleted;

    /**
     * Creates a new FileEntity with the given filename and content type.
     *
     * @param filename    the original filename.
     * @param contentType the MIME content type.
     * @return a new FileEntity instance.
     */
    public static FileEntity create(String filename, String contentType) {
        final var entity = new FileEntity();
        entity.filename = filename;
        entity.contentType = contentType;
        entity.currentVersion = 0;
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        entity.deleted = false;
        return entity;
    }
}
