package com.schemesathi.database;

import com.schemesathi.entity.*;
import com.schemesathi.repository.*;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FlywayMigrationAndSchemaTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private CitizenProfileRepository citizenProfileRepository;

    @Autowired
    private CitizenDocumentRepository citizenDocumentRepository;

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Test
    @DisplayName("Flyway migrations should be discovered and executed successfully")
    void flywayMigrationsShouldExecuteSuccessfully() {
        assertThat(flyway).isNotNull();
        MigrationInfo[] appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(appliedMigrations[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(appliedMigrations[0].getDescription()).containsIgnoringCase("initial schema");
        assertThat(appliedMigrations[1].getVersion().getVersion()).isEqualTo("2");
        assertThat(appliedMigrations[1].getDescription()).containsIgnoringCase("household members");
    }

    @Test
    @DisplayName("Should successfully persist and retrieve all JPA entities matching Flyway schema")
    @Transactional
    void shouldPersistAndRetrieveEntities() {
        // 1. User
        User user = User.builder()
                .email("migrated_user@schemesathi.test")
                .password("SecureHashedPassword123!")
                .fullName("Test Citizen Profile")
                .phone("9123456780")
                .role(User.UserRole.CITIZEN)
                .build();
        user = userRepository.saveAndFlush(user);
        assertThat(user.getId()).isNotNull();

        // 2. CitizenProfile
        CitizenProfile profile = CitizenProfile.builder()
                .user(user)
                .age(35)
                .gender("Female")
                .state("Telangana")
                .district("Ranga Reddy")
                .occupation("Self Employed")
                .annualIncome(120000.0)
                .educationLevel("Graduate")
                .category("General")
                .disabilityStatus(false)
                .landOwnership(false)
                .build();
        profile = citizenProfileRepository.saveAndFlush(profile);
        assertThat(profile.getId()).isNotNull();

        // 3. CitizenDocument
        CitizenDocument doc = CitizenDocument.builder()
                .user(user)
                .documentType("Aadhaar Card")
                .fileName("aadhaar.pdf")
                .filePath("/uploads/docs/aadhaar.pdf")
                .status(CitizenDocument.DocumentStatus.VERIFIED)
                .confidence(0.98)
                .build();
        doc = citizenDocumentRepository.saveAndFlush(doc);
        assertThat(doc.getId()).isNotNull();

        // 4. Scheme with EligibilityRule and RequiredDocument
        Scheme scheme = Scheme.builder()
                .name("Postgres Migration Test Scheme")
                .description("Scheme to test relational mapping and cascading")
                .objective("Verification")
                .governmentAuthority("Gov of India")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Education")
                .benefits("Scholarship of Rs. 10000")
                .verificationDate(LocalDate.now())
                .active(true)
                .build();

        EligibilityRule rule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("annualIncome")
                .operator("LTE")
                .value("200000")
                .displayLabel("Income Limit")
                .mandatory(true)
                .priority(1)
                .build();

        RequiredDocument reqDoc = RequiredDocument.builder()
                .scheme(scheme)
                .documentName("Income Certificate")
                .description("Proof of family annual income")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().add(rule);
        scheme.getRequiredDocuments().add(reqDoc);

        scheme = schemeRepository.saveAndFlush(scheme);
        assertThat(scheme.getId()).isNotNull();
        assertThat(scheme.getEligibilityRules()).hasSize(1);
        assertThat(scheme.getRequiredDocuments()).hasSize(1);

        // 5. SavedScheme
        SavedScheme savedScheme = SavedScheme.builder()
                .user(user)
                .scheme(scheme)
                .notes("Saved for application")
                .build();
        savedScheme = savedSchemeRepository.saveAndFlush(savedScheme);
        assertThat(savedScheme.getId()).isNotNull();

        // Verify retrieval
        Optional<SavedScheme> fetchedSaved = savedSchemeRepository.findByUserIdAndSchemeId(user.getId(), scheme.getId());
        assertThat(fetchedSaved).isPresent();
        assertThat(fetchedSaved.get().getNotes()).isEqualTo("Saved for application");
    }

    @Test
    @DisplayName("Database unique constraints should reject duplicate user emails")
    void shouldEnforceUserEmailUniqueConstraint() {
        User firstUser = User.builder()
                .email("unique_test@schemesathi.test")
                .password("Password123!")
                .fullName("First User")
                .role(User.UserRole.CITIZEN)
                .build();
        userRepository.saveAndFlush(firstUser);

        User duplicateUser = User.builder()
                .email("unique_test@schemesathi.test")
                .password("Password456!")
                .fullName("Duplicate User")
                .role(User.UserRole.CITIZEN)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    @DisplayName("Database unique constraint should prevent duplicate saved schemes for the same user")
    void shouldEnforceSavedSchemeUniqueConstraint() {
        User user = User.builder()
                .email("bookmark_user@schemesathi.test")
                .password("Password123!")
                .fullName("Bookmark User")
                .role(User.UserRole.CITIZEN)
                .build();
        user = userRepository.saveAndFlush(user);

        Scheme scheme = Scheme.builder()
                .name("Bookmark Test Scheme")
                .description("Test Description")
                .governmentAuthority("Test Authority")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();
        scheme = schemeRepository.saveAndFlush(scheme);

        SavedScheme firstSave = SavedScheme.builder()
                .user(user)
                .scheme(scheme)
                .savedAt(LocalDateTime.now())
                .build();
        savedSchemeRepository.saveAndFlush(firstSave);

        SavedScheme duplicateSave = SavedScheme.builder()
                .user(user)
                .scheme(scheme)
                .savedAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            savedSchemeRepository.saveAndFlush(duplicateSave);
        });
    }

    @Test
    @DisplayName("Should successfully persist and retrieve HouseholdMember matching Flyway schema")
    @Transactional
    void shouldPersistAndRetrieveHouseholdMember() {
        User user = User.builder()
                .email("household_schema_user@schemesathi.test")
                .password("Password123!")
                .fullName("Household Head")
                .role(User.UserRole.CITIZEN)
                .build();
        user = userRepository.saveAndFlush(user);

        HouseholdMember member = HouseholdMember.builder()
                .user(user)
                .fullName("Lakshmi Reddy")
                .relationship("Spouse")
                .age(34)
                .gender("Female")
                .state("Telangana")
                .district("Ranga Reddy")
                .occupation("Homemaker")
                .annualIncome(0.0)
                .educationLevel("Graduate")
                .category("General")
                .disabilityStatus(false)
                .landOwnership(false)
                .maritalStatus("Married")
                .bplStatus(true)
                .build();
        member = householdMemberRepository.saveAndFlush(member);
        assertThat(member.getId()).isNotNull();

        Optional<HouseholdMember> found = householdMemberRepository.findByIdAndUserId(member.getId(), user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Lakshmi Reddy");
        assertThat(found.get().getRelationship()).isEqualTo("Spouse");
        assertThat(found.get().getBplStatus()).isTrue();
    }

    @Test
    @DisplayName("Database unique constraint uk_household_member_user_name_rel should prevent duplicate member for same user")
    void shouldEnforceHouseholdMemberUniqueConstraint() {
        User user = User.builder()
                .email("unique_household_user@schemesathi.test")
                .password("Password123!")
                .fullName("Unique Test User")
                .role(User.UserRole.CITIZEN)
                .build();
        user = userRepository.saveAndFlush(user);

        HouseholdMember member1 = HouseholdMember.builder()
                .user(user)
                .fullName("Rohan Reddy")
                .relationship("Child")
                .age(8)
                .gender("Male")
                .state("Telangana")
                .build();
        householdMemberRepository.saveAndFlush(member1);

        HouseholdMember member2 = HouseholdMember.builder()
                .user(user)
                .fullName("Rohan Reddy")
                .relationship("Child")
                .age(9)
                .gender("Male")
                .state("Telangana")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            householdMemberRepository.saveAndFlush(member2);
        });
    }
}
