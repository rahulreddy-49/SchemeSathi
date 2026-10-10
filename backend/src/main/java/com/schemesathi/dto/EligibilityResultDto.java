package com.schemesathi.dto;

import java.util.List;

public class EligibilityResultDto {
    private Long schemeId;
    private String schemeName;
    private EligibilityStatus overallStatus;
    private double matchScore;
    private List<CriterionResult> criteriaResults;
    private List<String> missingInformation;
    private List<String> requiredDocuments;
    private List<String> availableDocuments;
    private List<String> missingDocuments;
    private String explanation;

    public enum EligibilityStatus {
        ELIGIBLE,
        PROBABLY_ELIGIBLE,
        NOT_ELIGIBLE,
        INSUFFICIENT_INFORMATION,
        REQUIRES_OFFICIAL_VERIFICATION
    }

    public static class CriterionResult {
        private String criterionName;
        private String displayLabel;
        private String citizenValue;
        private String requiredValue;
        private CriterionStatus status;
        private String explanation;

        public enum CriterionStatus {
            PASS, FAIL, MISSING, NEEDS_VERIFICATION
        }

        public CriterionResult() {}

        public CriterionResult(String criterionName, String displayLabel, String citizenValue, String requiredValue, CriterionStatus status, String explanation) {
            this.criterionName = criterionName;
            this.displayLabel = displayLabel;
            this.citizenValue = citizenValue;
            this.requiredValue = requiredValue;
            this.status = status;
            this.explanation = explanation;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String criterionName;
            private String displayLabel;
            private String citizenValue;
            private String requiredValue;
            private CriterionStatus status;
            private String explanation;

            public Builder criterionName(String criterionName) { this.criterionName = criterionName; return this; }
            public Builder displayLabel(String displayLabel) { this.displayLabel = displayLabel; return this; }
            public Builder citizenValue(String citizenValue) { this.citizenValue = citizenValue; return this; }
            public Builder requiredValue(String requiredValue) { this.requiredValue = requiredValue; return this; }
            public Builder status(CriterionStatus status) { this.status = status; return this; }
            public Builder explanation(String explanation) { this.explanation = explanation; return this; }

            public CriterionResult build() {
                return new CriterionResult(criterionName, displayLabel, citizenValue, requiredValue, status, explanation);
            }
        }

        public String getCriterionName() { return criterionName; }
        public void setCriterionName(String criterionName) { this.criterionName = criterionName; }

        public String getDisplayLabel() { return displayLabel; }
        public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

        public String getCitizenValue() { return citizenValue; }
        public void setCitizenValue(String citizenValue) { this.citizenValue = citizenValue; }

        public String getRequiredValue() { return requiredValue; }
        public void setRequiredValue(String requiredValue) { this.requiredValue = requiredValue; }

        public CriterionStatus getStatus() { return status; }
        public void setStatus(CriterionStatus status) { this.status = status; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public EligibilityResultDto() {}

    public EligibilityResultDto(Long schemeId, String schemeName, EligibilityStatus overallStatus, double matchScore, List<CriterionResult> criteriaResults, List<String> missingInformation, List<String> requiredDocuments, List<String> availableDocuments, List<String> missingDocuments, String explanation) {
        this.schemeId = schemeId;
        this.schemeName = schemeName;
        this.overallStatus = overallStatus;
        this.matchScore = matchScore;
        this.criteriaResults = criteriaResults;
        this.missingInformation = missingInformation;
        this.requiredDocuments = requiredDocuments;
        this.availableDocuments = availableDocuments;
        this.missingDocuments = missingDocuments;
        this.explanation = explanation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long schemeId;
        private String schemeName;
        private EligibilityStatus overallStatus;
        private double matchScore;
        private List<CriterionResult> criteriaResults;
        private List<String> missingInformation;
        private List<String> requiredDocuments;
        private List<String> availableDocuments;
        private List<String> missingDocuments;
        private String explanation;

        public Builder schemeId(Long schemeId) { this.schemeId = schemeId; return this; }
        public Builder schemeName(String schemeName) { this.schemeName = schemeName; return this; }
        public Builder overallStatus(EligibilityStatus overallStatus) { this.overallStatus = overallStatus; return this; }
        public Builder matchScore(double matchScore) { this.matchScore = matchScore; return this; }
        public Builder criteriaResults(List<CriterionResult> criteriaResults) { this.criteriaResults = criteriaResults; return this; }
        public Builder missingInformation(List<String> missingInformation) { this.missingInformation = missingInformation; return this; }
        public Builder requiredDocuments(List<String> requiredDocuments) { this.requiredDocuments = requiredDocuments; return this; }
        public Builder availableDocuments(List<String> availableDocuments) { this.availableDocuments = availableDocuments; return this; }
        public Builder missingDocuments(List<String> missingDocuments) { this.missingDocuments = missingDocuments; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }

        public EligibilityResultDto build() {
            return new EligibilityResultDto(schemeId, schemeName, overallStatus, matchScore, criteriaResults, missingInformation, requiredDocuments, availableDocuments, missingDocuments, explanation);
        }
    }

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public EligibilityStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(EligibilityStatus overallStatus) { this.overallStatus = overallStatus; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public List<CriterionResult> getCriteriaResults() { return criteriaResults; }
    public void setCriteriaResults(List<CriterionResult> criteriaResults) { this.criteriaResults = criteriaResults; }

    public List<String> getMissingInformation() { return missingInformation; }
    public void setMissingInformation(List<String> missingInformation) { this.missingInformation = missingInformation; }

    public List<String> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<String> requiredDocuments) { this.requiredDocuments = requiredDocuments; }

    public List<String> getAvailableDocuments() { return availableDocuments; }
    public void setAvailableDocuments(List<String> availableDocuments) { this.availableDocuments = availableDocuments; }

    public List<String> getMissingDocuments() { return missingDocuments; }
    public void setMissingDocuments(List<String> missingDocuments) { this.missingDocuments = missingDocuments; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
