package com.schemesathi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CitizenProfileDto {
    private Long id;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 130, message = "Age must be valid (130 or below)")
    private Integer age;

    @Size(max = 20, message = "Gender length must not exceed 20 characters")
    private String gender;

    @Size(max = 50, message = "State length must not exceed 50 characters")
    private String state;

    @Size(max = 100, message = "District length must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "Occupation length must not exceed 100 characters")
    private String occupation;

    @PositiveOrZero(message = "Annual income cannot be negative")
    private Double annualIncome;

    @Size(max = 50, message = "Education level length must not exceed 50 characters")
    private String educationLevel;

    @Size(max = 30, message = "Category length must not exceed 30 characters")
    private String category;

    private Boolean disabilityStatus;

    @Size(max = 50, message = "Disability type length must not exceed 50 characters")
    private String disabilityType;

    @Min(value = 0, message = "Disability percentage cannot be negative")
    @Max(value = 100, message = "Disability percentage cannot exceed 100")
    private Integer disabilityPercentage;

    private Boolean landOwnership;

    @PositiveOrZero(message = "Land area cannot be negative")
    private Double landArea;

    @Size(max = 30, message = "Employment status length must not exceed 30 characters")
    private String employmentStatus;

    @Size(max = 30, message = "Residence type length must not exceed 30 characters")
    private String residenceType;

    @Size(max = 30, message = "Marital status length must not exceed 30 characters")
    private String maritalStatus;

    @Min(value = 1, message = "Family size must be at least 1")
    @Max(value = 50, message = "Family size must be 50 or below")
    private Integer familySize;

    private Boolean bplStatus;

    @Size(max = 30, message = "Ration card type length must not exceed 30 characters")
    private String rationCardType;

    private Boolean minorityStatus;
    private Boolean exServiceman;
    private Integer completenessPercentage;

    public CitizenProfileDto() {}

    public CitizenProfileDto(Long id, Integer age, String gender, String state, String district, String occupation, Double annualIncome, String educationLevel, String category, Boolean disabilityStatus, String disabilityType, Integer disabilityPercentage, Boolean landOwnership, Double landArea, String employmentStatus, String residenceType, String maritalStatus, Integer familySize, Boolean bplStatus, String rationCardType, Boolean minorityStatus, Boolean exServiceman, Integer completenessPercentage) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.state = state;
        this.district = district;
        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.educationLevel = educationLevel;
        this.category = category;
        this.disabilityStatus = disabilityStatus;
        this.disabilityType = disabilityType;
        this.disabilityPercentage = disabilityPercentage;
        this.landOwnership = landOwnership;
        this.landArea = landArea;
        this.employmentStatus = employmentStatus;
        this.residenceType = residenceType;
        this.maritalStatus = maritalStatus;
        this.familySize = familySize;
        this.bplStatus = bplStatus;
        this.rationCardType = rationCardType;
        this.minorityStatus = minorityStatus;
        this.exServiceman = exServiceman;
        this.completenessPercentage = completenessPercentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Integer age;
        private String gender;
        private String state;
        private String district;
        private String occupation;
        private Double annualIncome;
        private String educationLevel;
        private String category;
        private Boolean disabilityStatus;
        private String disabilityType;
        private Integer disabilityPercentage;
        private Boolean landOwnership;
        private Double landArea;
        private String employmentStatus;
        private String residenceType;
        private String maritalStatus;
        private Integer familySize;
        private Boolean bplStatus;
        private String rationCardType;
        private Boolean minorityStatus;
        private Boolean exServiceman;
        private Integer completenessPercentage;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder district(String district) { this.district = district; return this; }
        public Builder occupation(String occupation) { this.occupation = occupation; return this; }
        public Builder annualIncome(Double annualIncome) { this.annualIncome = annualIncome; return this; }
        public Builder educationLevel(String educationLevel) { this.educationLevel = educationLevel; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder disabilityStatus(Boolean disabilityStatus) { this.disabilityStatus = disabilityStatus; return this; }
        public Builder disabilityType(String disabilityType) { this.disabilityType = disabilityType; return this; }
        public Builder disabilityPercentage(Integer disabilityPercentage) { this.disabilityPercentage = disabilityPercentage; return this; }
        public Builder landOwnership(Boolean landOwnership) { this.landOwnership = landOwnership; return this; }
        public Builder landArea(Double landArea) { this.landArea = landArea; return this; }
        public Builder employmentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; return this; }
        public Builder residenceType(String residenceType) { this.residenceType = residenceType; return this; }
        public Builder maritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; return this; }
        public Builder familySize(Integer familySize) { this.familySize = familySize; return this; }
        public Builder bplStatus(Boolean bplStatus) { this.bplStatus = bplStatus; return this; }
        public Builder rationCardType(String rationCardType) { this.rationCardType = rationCardType; return this; }
        public Builder minorityStatus(Boolean minorityStatus) { this.minorityStatus = minorityStatus; return this; }
        public Builder exServiceman(Boolean exServiceman) { this.exServiceman = exServiceman; return this; }
        public Builder completenessPercentage(Integer completenessPercentage) { this.completenessPercentage = completenessPercentage; return this; }

        public CitizenProfileDto build() {
            return new CitizenProfileDto(id, age, gender, state, district, occupation, annualIncome, educationLevel, category, disabilityStatus, disabilityType, disabilityPercentage, landOwnership, landArea, employmentStatus, residenceType, maritalStatus, familySize, bplStatus, rationCardType, minorityStatus, exServiceman, completenessPercentage);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public Double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Double annualIncome) { this.annualIncome = annualIncome; }

    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getDisabilityStatus() { return disabilityStatus; }
    public void setDisabilityStatus(Boolean disabilityStatus) { this.disabilityStatus = disabilityStatus; }

    public String getDisabilityType() { return disabilityType; }
    public void setDisabilityType(String disabilityType) { this.disabilityType = disabilityType; }

    public Integer getDisabilityPercentage() { return disabilityPercentage; }
    public void setDisabilityPercentage(Integer disabilityPercentage) { this.disabilityPercentage = disabilityPercentage; }

    public Boolean getLandOwnership() { return landOwnership; }
    public void setLandOwnership(Boolean landOwnership) { this.landOwnership = landOwnership; }

    public Double getLandArea() { return landArea; }
    public void setLandArea(Double landArea) { this.landArea = landArea; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public String getResidenceType() { return residenceType; }
    public void setResidenceType(String residenceType) { this.residenceType = residenceType; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public Integer getFamilySize() { return familySize; }
    public void setFamilySize(Integer familySize) { this.familySize = familySize; }

    public Boolean getBplStatus() { return bplStatus; }
    public void setBplStatus(Boolean bplStatus) { this.bplStatus = bplStatus; }

    public String getRationCardType() { return rationCardType; }
    public void setRationCardType(String rationCardType) { this.rationCardType = rationCardType; }

    public Boolean getMinorityStatus() { return minorityStatus; }
    public void setMinorityStatus(Boolean minorityStatus) { this.minorityStatus = minorityStatus; }

    public Boolean getExServiceman() { return exServiceman; }
    public void setExServiceman(Boolean exServiceman) { this.exServiceman = exServiceman; }

    public Integer getCompletenessPercentage() { return completenessPercentage; }
    public void setCompletenessPercentage(Integer completenessPercentage) { this.completenessPercentage = completenessPercentage; }
}
