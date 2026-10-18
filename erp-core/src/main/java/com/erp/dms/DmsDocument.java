package com.erp.dms;

import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Document attached to any record (M-DMS-001, M-DMS-002). Uploading a new file creates a new
 * version; the previous revision is retained.
 */
@Entity(name = "DmsDocument")
@Table(name = "dms_document")
public class DmsDocument extends BaseEntity {

    /**
     * documentNumber
     */
    @Column(name = "document_number", length = 40, nullable = false)
    public String documentNumber;

    /**
     * name
     */
    @Column(name = "name", length = 300, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * folderId
     */
    @Column(name = "folder_id")
    public Long folderId;

    /**
     * INVOICE, CONTRACT, DRAWING, PHOTO, REPORT, OTHER.
     */
    @Column(name = "document_type", length = 60)
    public String documentType;

    /**
     * entityType
     */
    @Column(name = "entity_type", length = 120)
    public String entityType;

    /**
     * entityId
     */
    @Column(name = "entity_id")
    public Long entityId;

    /**
     * fileName
     */
    @Column(name = "file_name", length = 300)
    public String fileName;

    /**
     * mimeType
     */
    @Column(name = "mime_type", length = 120)
    public String mimeType;

    /**
     * fileSizeBytes
     */
    @Column(name = "file_size_bytes")
    public Long fileSizeBytes;

    /**
     * Path relative to the configured document store root.
     */
    @Column(name = "storage_path", length = 500)
    public String storagePath;

    /**
     * SHA-256 of the current version's content.
     */
    @Column(name = "checksum", length = 128)
    public String checksum;

    /**
     * currentVersion
     */
    @Column(name = "current_version")
    public Integer currentVersion;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public DocumentStatus status;

    /**
     * Set while a user holds the edit lock (M-DMS-006).
     */
    @Column(name = "checked_out_by", length = 120)
    public String checkedOutBy;

    /**
     * checkedOutAt
     */
    @Column(name = "checked_out_at")
    public Instant checkedOutAt;

    /**
     * ownerUsername
     */
    @Column(name = "owner_username", length = 120)
    public String ownerUsername;

    /**
     * Retention expiry; see NFR-DR-01 and NFR-DR-02.
     */
    @Column(name = "retention_until")
    public LocalDate retentionUntil;

    /**
     * accessLevel
     */
    @Column(name = "access_level", length = 20)
    public String accessLevel;

    /**
     * Extracted text used by full-text search (M-DMS-004).
     */
    @Column(name = "search_text")
    public String searchText;

    @Override
    public String displayLabel() {
        return documentNumber + " - " + name;
    }

}
