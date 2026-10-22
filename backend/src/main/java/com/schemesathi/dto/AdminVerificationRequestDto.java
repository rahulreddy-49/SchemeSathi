package com.schemesathi.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class AdminVerificationRequestDto {

    @NotBlank(message = "Official source URL is required")
    private String sourceUrl;

    @NotBlank(message = "Verification status is required (VERIFIED, NEEDS_REVIEW, UNVERIFIED)")
    private String verificationStatus;

    private String verificationNotes;

    private LocalDate verificationDate;

    public AdminVerificationRequestDto() {}

    public AdminVerificationRequestDto(String sourceUrl, String verificationStatus, String verificationNotes, LocalDate verificationDate) {
        this.sourceUrl = sourceUrl;
        this.verificationStatus = verificationStatus;
        this.verificationNotes = verificationNotes;
        this.verificationDate = verificationDate != null ? verificationDate : LocalDate.now();
    }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public LocalDate getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDate verificationDate) { this.verificationDate = verificationDate; }
}
