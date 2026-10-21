package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scheme_applications")
public class SchemeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_member_id")
    private HouseholdMember householdMember;

    @Column(nullable = false, length = 20)
    private String applicantType = "SELF"; // SELF or HOUSEHOLD_MEMBER

    @Column(nullable = false, length = 100)
    private String applicantName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.PLANNING;

    @Column(length = 100)
    private String applicationReference;

    private LocalDate appliedDate;

    @Column(nullable = false)
    private LocalDateTime lastStatusUpdate = LocalDateTime.now();

    private LocalDate nextActionDate;

    @Column(length = 500)
    private String nextActionNotes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<SchemeApplicationHistory> history = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public SchemeApplication() {}

    public SchemeApplication(Long id, User user, Scheme scheme, HouseholdMember householdMember, String applicantType, String applicantName, ApplicationStatus status, String applicationReference, LocalDate appliedDate, LocalDateTime lastStatusUpdate, LocalDate nextActionDate, String nextActionNotes, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.scheme = scheme;
        this.householdMember = householdMember;
        this.applicantType = applicantType != null ? applicantType : "SELF";
        this.applicantName = applicantName;
        this.status = status != null ? status : ApplicationStatus.PLANNING;
        this.applicationReference = applicationReference;
        this.appliedDate = appliedDate;
        this.lastStatusUpdate = lastStatusUpdate != null ? lastStatusUpdate : LocalDateTime.now();
        this.nextActionDate = nextActionDate;
        this.nextActionNotes = nextActionNotes;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private Scheme scheme;
        private HouseholdMember householdMember;
        private String applicantType = "SELF";
        private String applicantName;
        private ApplicationStatus status = ApplicationStatus.PLANNING;
        private String applicationReference;
        private LocalDate appliedDate;
        private LocalDateTime lastStatusUpdate = LocalDateTime.now();
        private LocalDate nextActionDate;
        private String nextActionNotes;
        private String notes;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder scheme(Scheme scheme) { this.scheme = scheme; return this; }
        public Builder householdMember(HouseholdMember householdMember) { this.householdMember = householdMember; return this; }
        public Builder applicantType(String applicantType) { this.applicantType = applicantType; return this; }
        public Builder applicantName(String applicantName) { this.applicantName = applicantName; return this; }
        public Builder status(ApplicationStatus status) { this.status = status; return this; }
        public Builder applicationReference(String applicationReference) { this.applicationReference = applicationReference; return this; }
        public Builder appliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; return this; }
        public Builder lastStatusUpdate(LocalDateTime lastStatusUpdate) { this.lastStatusUpdate = lastStatusUpdate; return this; }
        public Builder nextActionDate(LocalDate nextActionDate) { this.nextActionDate = nextActionDate; return this; }
        public Builder nextActionNotes(String nextActionNotes) { this.nextActionNotes = nextActionNotes; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public SchemeApplication build() {
            return new SchemeApplication(id, user, scheme, householdMember, applicantType, applicantName, status, applicationReference, appliedDate, lastStatusUpdate, nextActionDate, nextActionNotes, notes, createdAt, updatedAt);
        }
    }

    public void addHistory(ApplicationStatus fromStatus, ApplicationStatus toStatus, String notes, String initiatedBy) {
        SchemeApplicationHistory record = new SchemeApplicationHistory(this, fromStatus, toStatus, notes, initiatedBy);
        this.history.add(record);
        this.lastStatusUpdate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }

    public HouseholdMember getHouseholdMember() { return householdMember; }
    public void setHouseholdMember(HouseholdMember householdMember) { this.householdMember = householdMember; }

    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

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

    public List<SchemeApplicationHistory> getHistory() { return history; }
    public void setHistory(List<SchemeApplicationHistory> history) { this.history = history; }
}
