package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schemes")
public class Scheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String objective;

    @Column(length = 200)
    private String governmentAuthority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemeScope scope = SchemeScope.CENTRAL;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String benefitCategory;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String applicationProcess;

    @Column(length = 500)
    private String officialUrl;

    @Column(length = 500)
    private String applicationUrl;

    @Column(length = 200)
    private String source;

    private LocalDate verificationDate;

    private LocalDate deadline;

    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String importantWarnings;

    @Column(columnDefinition = "TEXT")
    private String commonRejectionReasons;

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EligibilityRule> eligibilityRules = new ArrayList<>();

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RequiredDocument> requiredDocuments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SchemeScope {
        CENTRAL, STATE
    }

    public Scheme() {}

    public Scheme(Long id, String name, String description, String objective, String governmentAuthority, SchemeScope scope, String state, String benefitCategory, String benefits, String applicationProcess, String officialUrl, String applicationUrl, String source, LocalDate verificationDate, LocalDate deadline, Boolean active, String importantWarnings, String commonRejectionReasons, List<EligibilityRule> eligibilityRules, List<RequiredDocument> requiredDocuments, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objective = objective;
        this.governmentAuthority = governmentAuthority;
        this.scope = scope != null ? scope : SchemeScope.CENTRAL;
        this.state = state;
        this.benefitCategory = benefitCategory;
        this.benefits = benefits;
        this.applicationProcess = applicationProcess;
        this.officialUrl = officialUrl;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.verificationDate = verificationDate;
        this.deadline = deadline;
        this.active = active != null ? active : true;
        this.importantWarnings = importantWarnings;
        this.commonRejectionReasons = commonRejectionReasons;
        if (eligibilityRules != null) this.eligibilityRules = eligibilityRules;
        if (requiredDocuments != null) this.requiredDocuments = requiredDocuments;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private String objective;
        private String governmentAuthority;
        private SchemeScope scope = SchemeScope.CENTRAL;
        private String state;
        private String benefitCategory;
        private String benefits;
        private String applicationProcess;
        private String officialUrl;
        private String applicationUrl;
        private String source;
        private LocalDate verificationDate;
        private LocalDate deadline;
        private Boolean active = true;
        private String importantWarnings;
        private String commonRejectionReasons;
        private List<EligibilityRule> eligibilityRules = new ArrayList<>();
        private List<RequiredDocument> requiredDocuments = new ArrayList<>();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder objective(String objective) { this.objective = objective; return this; }
        public Builder governmentAuthority(String governmentAuthority) { this.governmentAuthority = governmentAuthority; return this; }
        public Builder scope(SchemeScope scope) { this.scope = scope; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder benefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; return this; }
        public Builder benefits(String benefits) { this.benefits = benefits; return this; }
        public Builder applicationProcess(String applicationProcess) { this.applicationProcess = applicationProcess; return this; }
        public Builder officialUrl(String officialUrl) { this.officialUrl = officialUrl; return this; }
        public Builder applicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder verificationDate(LocalDate verificationDate) { this.verificationDate = verificationDate; return this; }
        public Builder deadline(LocalDate deadline) { this.deadline = deadline; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder importantWarnings(String importantWarnings) { this.importantWarnings = importantWarnings; return this; }
        public Builder commonRejectionReasons(String commonRejectionReasons) { this.commonRejectionReasons = commonRejectionReasons; return this; }
        public Builder eligibilityRules(List<EligibilityRule> eligibilityRules) { this.eligibilityRules = eligibilityRules; return this; }
        public Builder requiredDocuments(List<RequiredDocument> requiredDocuments) { this.requiredDocuments = requiredDocuments; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Scheme build() {
            return new Scheme(id, name, description, objective, governmentAuthority, scope, state, benefitCategory, benefits, applicationProcess, officialUrl, applicationUrl, source, verificationDate, deadline, active, importantWarnings, commonRejectionReasons, eligibilityRules, requiredDocuments, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public String getGovernmentAuthority() { return governmentAuthority; }
    public void setGovernmentAuthority(String governmentAuthority) { this.governmentAuthority = governmentAuthority; }

    public SchemeScope getScope() { return scope; }
    public void setScope(SchemeScope scope) { this.scope = scope; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getBenefitCategory() { return benefitCategory; }
    public void setBenefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getApplicationProcess() { return applicationProcess; }
    public void setApplicationProcess(String applicationProcess) { this.applicationProcess = applicationProcess; }

    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDate getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDate verificationDate) { this.verificationDate = verificationDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getImportantWarnings() { return importantWarnings; }
    public void setImportantWarnings(String importantWarnings) { this.importantWarnings = importantWarnings; }

    public String getCommonRejectionReasons() { return commonRejectionReasons; }
    public void setCommonRejectionReasons(String commonRejectionReasons) { this.commonRejectionReasons = commonRejectionReasons; }

    public List<EligibilityRule> getEligibilityRules() { return eligibilityRules; }
    public void setEligibilityRules(List<EligibilityRule> eligibilityRules) { this.eligibilityRules = eligibilityRules; }

    public List<RequiredDocument> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<RequiredDocument> requiredDocuments) { this.requiredDocuments = requiredDocuments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
