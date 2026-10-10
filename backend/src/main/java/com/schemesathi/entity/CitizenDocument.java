package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_documents")
public class CitizenDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String documentType;

    @Column(length = 200)
    private String fileName;

    @Column(length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(columnDefinition = "TEXT")
    private String extractedData;

    private Double confidence;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private LocalDateTime verifiedAt;

    private LocalDateTime expiryDate;

    public enum DocumentStatus {
        UPLOADED, PROCESSING, EXTRACTED, VERIFIED, EXPIRED, INVALID, NEEDS_VERIFICATION
    }

    public CitizenDocument() {}

    public CitizenDocument(Long id, User user, String documentType, String fileName, String filePath, DocumentStatus status, String extractedData, Double confidence, String notes, LocalDateTime uploadedAt, LocalDateTime verifiedAt, LocalDateTime expiryDate) {
        this.id = id;
        this.user = user;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status != null ? status : DocumentStatus.UPLOADED;
        this.extractedData = extractedData;
        this.confidence = confidence;
        this.notes = notes;
        this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now();
        this.verifiedAt = verifiedAt;
        this.expiryDate = expiryDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String documentType;
        private String fileName;
        private String filePath;
        private DocumentStatus status = DocumentStatus.UPLOADED;
        private String extractedData;
        private Double confidence;
        private String notes;
        private LocalDateTime uploadedAt = LocalDateTime.now();
        private LocalDateTime verifiedAt;
        private LocalDateTime expiryDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder documentType(String documentType) { this.documentType = documentType; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder status(DocumentStatus status) { this.status = status; return this; }
        public Builder extractedData(String extractedData) { this.extractedData = extractedData; return this; }
        public Builder confidence(Double confidence) { this.confidence = confidence; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }
        public Builder verifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }

        public CitizenDocument build() {
            return new CitizenDocument(id, user, documentType, fileName, filePath, status, extractedData, confidence, notes, uploadedAt, verifiedAt, expiryDate);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public String getExtractedData() { return extractedData; }
    public void setExtractedData(String extractedData) { this.extractedData = extractedData; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}
