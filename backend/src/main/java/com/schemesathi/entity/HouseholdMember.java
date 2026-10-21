package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "household_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_household_member_user_name_rel", columnNames = {"user_id", "full_name", "relationship"})
    }
)
public class HouseholdMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 30)
    private String relationship; // Parent, Spouse, Child, Sibling, Grandparent, Other

    private Integer age;

    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(length = 50)
    private String occupation;

    private Double annualIncome;

    @Column(length = 50)
    private String educationLevel;

    @Column(length = 30)
    private String category; // General, SC, ST, OBC, EWS

    private Boolean disabilityStatus = false;

    @Column(length = 50)
    private String disabilityType;

    private Integer disabilityPercentage;

    private Boolean landOwnership = false;

    private Double landArea;

    @Column(length = 30)
    private String employmentStatus;

    @Column(length = 30)
    private String residenceType;

    @Column(length = 30)
    private String maritalStatus;

    private Integer familySize;

    private Boolean bplStatus = false;

    @Column(length = 30)
    private String rationCardType;

    private Boolean minorityStatus = false;

    private Boolean exServiceman = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public HouseholdMember() {}

    public HouseholdMember(Long id, User user, String fullName, String relationship, Integer age, String gender, String state, String district, String occupation, Double annualIncome, String educationLevel, String category, Boolean disabilityStatus, String disabilityType, Integer disabilityPercentage, Boolean landOwnership, Double landArea, String employmentStatus, String residenceType, String maritalStatus, Integer familySize, Boolean bplStatus, String rationCardType, Boolean minorityStatus, Boolean exServiceman, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.fullName = fullName;
        this.relationship = relationship;
        this.age = age;
        this.gender = gender;
        this.state = state;
        this.district = district;
        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.educationLevel = educationLevel;
        this.category = category;
        this.disabilityStatus = disabilityStatus != null ? disabilityStatus : false;
        this.disabilityType = disabilityType;
        this.disabilityPercentage = disabilityPercentage;
        this.landOwnership = landOwnership != null ? landOwnership : false;
        this.landArea = landArea;
        this.employmentStatus = employmentStatus;
        this.residenceType = residenceType;
        this.maritalStatus = maritalStatus;
        this.familySize = familySize;
        this.bplStatus = bplStatus != null ? bplStatus : false;
        this.rationCardType = rationCardType;
        this.minorityStatus = minorityStatus != null ? minorityStatus : false;
        this.exServiceman = exServiceman != null ? exServiceman : false;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    /**
     * Converts household member demographics to a detached transient CitizenProfile
     * for evaluation by EligibilityEngine.
     */
    public CitizenProfile toCitizenProfile() {
        return CitizenProfile.builder()
                .age(age)
                .gender(gender)
                .state(state)
                .district(district)
                .occupation(occupation)
                .annualIncome(annualIncome)
                .educationLevel(educationLevel)
                .category(category)
                .disabilityStatus(disabilityStatus != null ? disabilityStatus : false)
                .disabilityType(disabilityType)
                .disabilityPercentage(disabilityPercentage)
                .landOwnership(landOwnership != null ? landOwnership : false)
                .landArea(landArea)
                .employmentStatus(employmentStatus)
                .residenceType(residenceType)
                .maritalStatus(maritalStatus)
                .familySize(familySize)
                .bplStatus(bplStatus != null ? bplStatus : false)
                .rationCardType(rationCardType)
                .minorityStatus(minorityStatus != null ? minorityStatus : false)
                .exServiceman(exServiceman != null ? exServiceman : false)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String fullName;
        private String relationship;
        private Integer age;
        private String gender;
        private String state;
        private String district;
        private String occupation;
        private Double annualIncome;
        private String educationLevel;
        private String category;
        private Boolean disabilityStatus = false;
        private String disabilityType;
        private Integer disabilityPercentage;
        private Boolean landOwnership = false;
        private Double landArea;
        private String employmentStatus;
        private String residenceType;
        private String maritalStatus;
        private Integer familySize;
        private Boolean bplStatus = false;
        private String rationCardType;
        private Boolean minorityStatus = false;
        private Boolean exServiceman = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder relationship(String relationship) { this.relationship = relationship; return this; }
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
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public HouseholdMember build() {
            return new HouseholdMember(id, user, fullName, relationship, age, gender, state, district, occupation, annualIncome, educationLevel, category, disabilityStatus, disabilityType, disabilityPercentage, landOwnership, landArea, employmentStatus, residenceType, maritalStatus, familySize, bplStatus, rationCardType, minorityStatus, exServiceman, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
