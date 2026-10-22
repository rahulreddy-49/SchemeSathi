package com.schemesathi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminSchemeDto {
    private Long id;

    @NotBlank(message = "Scheme name is required")
    @Size(max = 300, message = "Scheme name cannot exceed 300 characters")
    private String name;

    @NotBlank(message = "Scheme description is required")
    private String description;

    private String objective;

    @Size(max = 200, message = "Government authority cannot exceed 200 characters")
    private String governmentAuthority;

    @NotBlank(message = "Scheme scope is required (CENTRAL or STATE)")
    private String scope = "CENTRAL";

    private String state;

    @NotBlank(message = "Benefit category is required")
    @Size(max = 50, message = "Benefit category cannot exceed 50 characters")
    private String benefitCategory;

    private String benefits;
    private String applicationProcess;
    private String officialUrl;
    private String applicationUrl;
    private String source;
    private String sourceUrl;
    private String publicationStatus = "DRAFT";
    private String verificationStatus = "UNVERIFIED";
    private String verificationNotes;
    private LocalDate verificationDate;
    private LocalDate deadline;
    private Boolean active = true;
    private String importantWarnings;
    private String commonRejectionReasons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Valid
    private List<AdminEligibilityRuleDto> eligibilityRules = new ArrayList<>();

    @Valid
    private List<AdminRequiredDocumentDto> requiredDocuments = new ArrayList<>();

    public AdminSchemeDto() {}

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

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

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

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getPublicationStatus() { return publicationStatus; }
    public void setPublicationStatus(String publicationStatus) { this.publicationStatus = publicationStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<AdminEligibilityRuleDto> getEligibilityRules() { return eligibilityRules; }
    public void setEligibilityRules(List<AdminEligibilityRuleDto> eligibilityRules) { this.eligibilityRules = eligibilityRules != null ? eligibilityRules : new ArrayList<>(); }

    public List<AdminRequiredDocumentDto> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<AdminRequiredDocumentDto> requiredDocuments) { this.requiredDocuments = requiredDocuments != null ? requiredDocuments : new ArrayList<>(); }

    public static class AdminEligibilityRuleDto {
        private Long id;

        @NotBlank(message = "Criterion field is required")
        private String criterionField;

        @NotBlank(message = "Operator is required")
        private String operator;

        private String value;
        private String displayLabel;
        private String description;
        private Boolean mandatory = true;
        private Integer priority = 0;

        public AdminEligibilityRuleDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCriterionField() { return criterionField; }
        public void setCriterionField(String criterionField) { this.criterionField = criterionField; }

        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getDisplayLabel() { return displayLabel; }
        public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Boolean getMandatory() { return mandatory; }
        public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    public static class AdminRequiredDocumentDto {
        private Long id;

        @NotBlank(message = "Document name is required")
        private String documentName;

        private String description;
        private Boolean mandatory = true;
        private String alternatives;

        public AdminRequiredDocumentDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Boolean getMandatory() { return mandatory; }
        public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

        public String getAlternatives() { return alternatives; }
        public void setAlternatives(String alternatives) { this.alternatives = alternatives; }
    }
}
