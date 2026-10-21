package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CITIZEN;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLogin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CitizenProfile citizenProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<HouseholdMember> householdMembers = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<SchemeApplication> applications = new java.util.ArrayList<>();

    public java.util.List<SchemeApplication> getApplications() { return applications; }
    public void setApplications(java.util.List<SchemeApplication> applications) { this.applications = applications; }

    public enum UserRole {
        CITIZEN, ADMIN
    }

    public User() {}

    public User(Long id, String email, String password, String fullName, String phone, UserRole role, Boolean active, LocalDateTime createdAt, LocalDateTime lastLogin, CitizenProfile citizenProfile) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role != null ? role : UserRole.CITIZEN;
        this.active = active != null ? active : true;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastLogin = lastLogin;
        this.citizenProfile = citizenProfile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private UserRole role = UserRole.CITIZEN;
        private Boolean active = true;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastLogin;
        private CitizenProfile citizenProfile;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; return this; }
        public Builder citizenProfile(CitizenProfile citizenProfile) { this.citizenProfile = citizenProfile; return this; }

        public User build() {
            return new User(id, email, password, fullName, phone, role, active, createdAt, lastLogin, citizenProfile);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public CitizenProfile getCitizenProfile() { return citizenProfile; }
    public void setCitizenProfile(CitizenProfile citizenProfile) { this.citizenProfile = citizenProfile; }

    public java.util.List<HouseholdMember> getHouseholdMembers() { return householdMembers; }
    public void setHouseholdMembers(java.util.List<HouseholdMember> householdMembers) { this.householdMembers = householdMembers; }
}
