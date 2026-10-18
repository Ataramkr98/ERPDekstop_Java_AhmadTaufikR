package com.erp.dms;

import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One revision of a document (M-DMS-002). History is append-only so the audit log can be
 * replayed.
 */
@Entity(name = "DmsDocumentVersion")
@Table(name = "dms_document_version")
public class DmsDocumentVersion extends BaseEntity {

    /**
     * documentId
     */
    @Column(name = "document_id", nullable = false)
    public Long documentId;

    /**
     * versionNumber
     */
    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    /**
     * fileName
     */
    @Column(name = "file_name", length = 300)
    public String fileName;

    /**
     * storagePath
     */
    @Column(name = "storage_path", length = 500)
    public String storagePath;

    /**
     * fileSizeBytes
     */
    @Column(name = "file_size_bytes")
    public Long fileSizeBytes;

    /**
     * checksum
     */
    @Column(name = "checksum", length = 128)
    public String checksum;

    /**
     * uploadedBy
     */
    @Column(name = "uploaded_by", length = 120)
    public String uploadedBy;

    /**
     * uploadedAt
     */
    @Column(name = "uploaded_at")
    public Instant uploadedAt;

    /**
     * changeNotes
     */
    @Column(name = "change_notes")
    public String changeNotes;

}
