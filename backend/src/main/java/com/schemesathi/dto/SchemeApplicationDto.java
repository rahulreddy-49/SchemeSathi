package com.schemesathi.dto;

import com.schemesathi.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SchemeApplicationDto {

    private Long id;
    private Long schemeId;
    private String schemeName;
    private String schemeCategory;
    private String governmentAuthority;
    private Long householdMemberId;
    private String applicantType;
    private String applicantName;
    private ApplicationStatus status;
    private String statusDisplayName;
    private String applicationReference;
    private LocalDate appliedDate;
    private LocalDateTime lastStatusUpdate;
    private LocalDate nextActionDate;
    private String nextActionNotes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SchemeApplicationHistoryDto> history = new ArrayList<>();

    public SchemeApplicationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public String getSchemeCategory() { return schemeCategory; }
    public void setSchemeCategory(String schemeCategory) { this.schemeCategory = schemeCategory; }

    public String getGovernmentAuthority() { return governmentAuthority; }
    public void setGovernmentAuthority(String governmentAuthority) { this.governmentAuthority = governmentAuthority; }

    public Long getHouseholdMemberId() { return householdMemberId; }
    public void setHouseholdMemberId(Long householdMemberId) { this.householdMemberId = householdMemberId; }

    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.statusDisplayName = status != null ? status.getDisplayName() : null;
    }

    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }

    public String getApplicationReference() { return applicationReference; }
    public void setApplicationReference(String applicationReference) { this.applicationReference = applicationReference; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public LocalDateTime getLastStatusUpdate() { return lastStatusUpdate; }
    public void setLastStatusUpdate(LocalDateTime lastStatusUpdate) { this.lastStatusUpdate = lastStatusUpdate; }

    public LocalDate getNextActionDate() { return nextActionDate; }
    public void setNextActionDate(LocalDate nextActionDate) { this.nextActionDate = nextActionDate; }

    public String getNextActionNotes() { return nextActionNotes; }
    public void setNextActionNotes(String nextActionNotes) { this.nextActionNotes = nextActionNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SchemeApplicationHistoryDto> getHistory() { return history; }
    public void setHistory(List<SchemeApplicationHistoryDto> history) { this.history = history; }

    // DTO for Application History entry
    public static class SchemeApplicationHistoryDto {
        private Long id;
        private ApplicationStatus fromStatus;
        private String fromStatusDisplayName;
        private ApplicationStatus toStatus;
        private String toStatusDisplayName;
        private String notes;
        private String initiatedBy;
        private LocalDateTime createdAt;

        public SchemeApplicationHistoryDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public ApplicationStatus getFromStatus() { return fromStatus; }
        public void setFromStatus(ApplicationStatus fromStatus) {
            this.fromStatus = fromStatus;
            this.fromStatusDisplayName = fromStatus != null ? fromStatus.getDisplayName() : null;
        }

        public String getFromStatusDisplayName() { return fromStatusDisplayName; }
        public void setFromStatusDisplayName(String fromStatusDisplayName) { this.fromStatusDisplayName = fromStatusDisplayName; }

        public ApplicationStatus getToStatus() { return toStatus; }
        public void setToStatus(ApplicationStatus toStatus) {
            this.toStatus = toStatus;
            this.toStatusDisplayName = toStatus != null ? toStatus.getDisplayName() : null;
        }

        public String getToStatusDisplayName() { return toStatusDisplayName; }
        public void setToStatusDisplayName(String toStatusDisplayName) { this.toStatusDisplayName = toStatusDisplayName; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getInitiatedBy() { return initiatedBy; }
        public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // Request DTO to create a new tracking record
    public static class CreateApplicationRequest {
        @NotNull(message = "Scheme ID is required")
        private Long schemeId;

        private Long householdMemberId;

        private ApplicationStatus status = ApplicationStatus.PLANNING;

        @Size(max = 100, message = "Application reference number cannot exceed 100 characters")
        private String applicationReference;

        private LocalDate appliedDate;

        private LocalDate nextActionDate;

        @Size(max = 500, message = "Next action notes cannot exceed 500 characters")
        private String nextActionNotes;

        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        private String notes;

        public CreateApplicationRequest() {}

        public Long getSchemeId() { return schemeId; }
        public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

        public Long getHouseholdMemberId() { return householdMemberId; }
        public void setHouseholdMemberId(Long householdMemberId) { this.householdMemberId = householdMemberId; }

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }

        public String getApplicationReference() { return applicationReference; }
        public void setApplicationReference(String applicationReference) { this.applicationReference = applicationReference; }

        public LocalDate getAppliedDate() { return appliedDate; }
        public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

        public LocalDate getNextActionDate() { return nextActionDate; }
        public void setNextActionDate(LocalDate nextActionDate) { this.nextActionDate = nextActionDate; }

        public String getNextActionNotes() { return nextActionNotes; }
        public void setNextActionNotes(String nextActionNotes) { this.nextActionNotes = nextActionNotes; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    // Request DTO to update tracking details and/or progress
    public static class UpdateApplicationRequest {
        private ApplicationStatus status;

        @Size(max = 100, message = "Application reference number cannot exceed 100 characters")
        private String applicationReference;

        private LocalDate appliedDate;

        private LocalDate nextActionDate;

        @Size(max = 500, message = "Next action notes cannot exceed 500 characters")
        private String nextActionNotes;

        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        private String notes;

        @Size(max = 500, message = "Status change note cannot exceed 500 characters")
        private String statusChangeNote;

        public UpdateApplicationRequest() {}

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }

        public String getApplicationReference() { return applicationReference; }
        public void setApplicationReference(String applicationReference) { this.applicationReference = applicationReference; }

        public LocalDate getAppliedDate() { return appliedDate; }
        public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

        public LocalDate getNextActionDate() { return nextActionDate; }
        public void setNextActionDate(LocalDate nextActionDate) { this.nextActionDate = nextActionDate; }

        public String getNextActionNotes() { return nextActionNotes; }
        public void setNextActionNotes(String nextActionNotes) { this.nextActionNotes = nextActionNotes; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getStatusChangeNote() { return statusChangeNote; }
        public void setStatusChangeNote(String statusChangeNote) { this.statusChangeNote = statusChangeNote; }
    }

    // Summary DTO for tracking dashboard counts
    public static class ApplicationSummaryDto {
        private long totalApplications;
        private long planningCount;
        private long readyToApplyCount;
        private long submittedCount;
        private long underReviewCount;
        private long actionRequiredCount;
        private long approvedCount;
        private long rejectedCount;
        private long completedCount;
        private long cancelledCount;

        public ApplicationSummaryDto() {}

        public long getTotalApplications() { return totalApplications; }
        public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

        public long getPlanningCount() { return planningCount; }
        public void setPlanningCount(long planningCount) { this.planningCount = planningCount; }

        public long getReadyToApplyCount() { return readyToApplyCount; }
        public void setReadyToApplyCount(long readyToApplyCount) { this.readyToApplyCount = readyToApplyCount; }

        public long getSubmittedCount() { return submittedCount; }
        public void setSubmittedCount(long submittedCount) { this.submittedCount = submittedCount; }

        public long getUnderReviewCount() { return underReviewCount; }
        public void setUnderReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; }

        public long getActionRequiredCount() { return actionRequiredCount; }
        public void setActionRequiredCount(long actionRequiredCount) { this.actionRequiredCount = actionRequiredCount; }

        public long getApprovedCount() { return approvedCount; }
        public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }

        public long getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }

        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

        public long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }
    }
}
