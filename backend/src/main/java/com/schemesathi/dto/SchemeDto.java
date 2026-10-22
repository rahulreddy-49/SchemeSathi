package com.schemesathi.dto;

import java.time.LocalDate;
import java.util.List;

public class SchemeDto {
    private Long id;
    private String name;
    private String description;
    private String objective;
    private String governmentAuthority;
    private String scope;
    private String state;
    private String benefitCategory;
    private String benefits;
    private String applicationProcess;
    private String officialUrl;
    private String applicationUrl;
    private String source;
    private String publicationStatus;
    private String verificationStatus;
    private String sourceUrl;
    private String verificationNotes;
    private LocalDate verificationDate;
    private LocalDate deadline;
    private Boolean active;
    private String importantWarnings;
    private String commonRejectionReasons;
    private List<EligibilityRuleDto> eligibilityRules;
    private List<RequiredDocumentDto> requiredDocuments;
    private Boolean saved;

    public static class EligibilityRuleDto {
        private Long id;
        private String criterionField;
        private String operator;
        private String value;
        private String displayLabel;
        private String description;
        private Boolean mandatory;

        public EligibilityRuleDto() {}

        public EligibilityRuleDto(Long id, String criterionField, String operator, String value, String displayLabel, String description, Boolean mandatory) {
            this.id = id;
            this.criterionField = criterionField;
            this.operator = operator;
            this.value = value;
            this.displayLabel = displayLabel;
            this.description = description;
            this.mandatory = mandatory;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String criterionField;
            private String operator;
            private String value;
            private String displayLabel;
            private String description;
            private Boolean mandatory;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder criterionField(String criterionField) { this.criterionField = criterionField; return this; }
            public Builder operator(String operator) { this.operator = operator; return this; }
            public Builder value(String value) { this.value = value; return this; }
            public Builder displayLabel(String displayLabel) { this.displayLabel = displayLabel; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder mandatory(Boolean mandatory) { this.mandatory = mandatory; return this; }

            public EligibilityRuleDto build() {
                return new EligibilityRuleDto(id, criterionField, operator, value, displayLabel, description, mandatory);
            }
        }

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
    }

    public static class RequiredDocumentDto {
        private Long id;
        private String documentName;
        private String description;
        private Boolean mandatory;
        private String alternatives;

        public RequiredDocumentDto() {}

        public RequiredDocumentDto(Long id, String documentName, String description, Boolean mandatory, String alternatives) {
            this.id = id;
            this.documentName = documentName;
            this.description = description;
            this.mandatory = mandatory;
            this.alternatives = alternatives;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String documentName;
            private String description;
            private Boolean mandatory;
            private String alternatives;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder documentName(String documentName) { this.documentName = documentName; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder mandatory(Boolean mandatory) { this.mandatory = mandatory; return this; }
            public Builder alternatives(String alternatives) { this.alternatives = alternatives; return this; }

            public RequiredDocumentDto build() {
                return new RequiredDocumentDto(id, documentName, description, mandatory, alternatives);
            }
        }

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

    public SchemeDto() {}

    public SchemeDto(Long id, String name, String description, String objective, String governmentAuthority,
                     String scope, String state, String benefitCategory, String benefits,
                     String applicationProcess, String officialUrl, String applicationUrl, String source,
                     String publicationStatus, String verificationStatus, String sourceUrl, String verificationNotes,
                     LocalDate verificationDate, LocalDate deadline, Boolean active,
                     String importantWarnings, String commonRejectionReasons,
                     List<EligibilityRuleDto> eligibilityRules, List<RequiredDocumentDto> requiredDocuments, Boolean saved) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objective = objective;
        this.governmentAuthority = governmentAuthority;
        this.scope = scope;
        this.state = state;
        this.benefitCategory = benefitCategory;
        this.benefits = benefits;
        this.applicationProcess = applicationProcess;
        this.officialUrl = officialUrl;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.publicationStatus = publicationStatus;
        this.verificationStatus = verificationStatus;
        this.sourceUrl = sourceUrl;
        this.verificationNotes = verificationNotes;
        this.verificationDate = verificationDate;
        this.deadline = deadline;
        this.active = active;
        this.importantWarnings = importantWarnings;
        this.commonRejectionReasons = commonRejectionReasons;
        this.eligibilityRules = eligibilityRules;
        this.requiredDocuments = requiredDocuments;
        this.saved = saved;
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
        private String scope;
        private String state;
        private String benefitCategory;
        private String benefits;
        private String applicationProcess;
        private String officialUrl;
        private String applicationUrl;
        private String source;
        private String publicationStatus;
        private String verificationStatus;
        private String sourceUrl;
        private String verificationNotes;
        private LocalDate verificationDate;
        private LocalDate deadline;
        private Boolean active;
        private String importantWarnings;
        private String commonRejectionReasons;
        private List<EligibilityRuleDto> eligibilityRules;
        private List<RequiredDocumentDto> requiredDocuments;
        private Boolean saved;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder objective(String objective) { this.objective = objective; return this; }
        public Builder governmentAuthority(String governmentAuthority) { this.governmentAuthority = governmentAuthority; return this; }
        public Builder scope(String scope) { this.scope = scope; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder benefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; return this; }
        public Builder benefits(String benefits) { this.benefits = benefits; return this; }
        public Builder applicationProcess(String applicationProcess) { this.applicationProcess = applicationProcess; return this; }
        public Builder officialUrl(String officialUrl) { this.officialUrl = officialUrl; return this; }
        public Builder applicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder publicationStatus(String publicationStatus) { this.publicationStatus = publicationStatus; return this; }
        public Builder verificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public Builder sourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }
        public Builder verificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; return this; }
        public Builder verificationDate(LocalDate verificationDate) { this.verificationDate = verificationDate; return this; }
        public Builder deadline(LocalDate deadline) { this.deadline = deadline; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder importantWarnings(String importantWarnings) { this.importantWarnings = importantWarnings; return this; }
        public Builder commonRejectionReasons(String commonRejectionReasons) { this.commonRejectionReasons = commonRejectionReasons; return this; }
        public Builder eligibilityRules(List<EligibilityRuleDto> eligibilityRules) { this.eligibilityRules = eligibilityRules; return this; }
        public Builder requiredDocuments(List<RequiredDocumentDto> requiredDocuments) { this.requiredDocuments = requiredDocuments; return this; }
        public Builder saved(Boolean saved) { this.saved = saved; return this; }

        public SchemeDto build() {
            return new SchemeDto(id, name, description, objective, governmentAuthority, scope, state, benefitCategory, benefits,
                    applicationProcess, officialUrl, applicationUrl, source, publicationStatus, verificationStatus,
                    sourceUrl, verificationNotes, verificationDate, deadline, active, importantWarnings,
                    commonRejectionReasons, eligibilityRules, requiredDocuments, saved);
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
    public String getPublicationStatus() { return publicationStatus; }
    public void setPublicationStatus(String publicationStatus) { this.publicationStatus = publicationStatus; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
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
    public List<EligibilityRuleDto> getEligibilityRules() { return eligibilityRules; }
    public void setEligibilityRules(List<EligibilityRuleDto> eligibilityRules) { this.eligibilityRules = eligibilityRules; }
    public List<RequiredDocumentDto> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<RequiredDocumentDto> requiredDocuments) { this.requiredDocuments = requiredDocuments; }
    public Boolean getSaved() { return saved; }
    public void setSaved(Boolean saved) { this.saved = saved; }
}
